/**
 * Copyright (C) 2014 Open Whisper Systems
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package com.hzy.uikit.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Build.VERSION_CODES
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.hzy.uikit.R
import com.hzy.uikit.util.ServiceUtil

import java.lang.reflect.Field
import java.util.HashSet

/**
 * LinearLayout that, when a view container, will report back when it thinks a soft keyboard
 * has been opened and what its height would be.
 */
open class KeyboardAwareLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val rect = Rect()
    private val hiddenListeners = HashSet<OnKeyboardHiddenListener>()
    private val shownListeners = HashSet<OnKeyboardShownListener>()
    private val minKeyboardSize: Int
    private val minCustomKeyboardSize: Int
    private val defaultCustomKeyboardSize: Int
    private val minCustomKeyboardTopMargin: Int
    private val statusBarHeight: Int

    private var viewInset: Int = 0

    var isKeyboardOpen = false
        private set
    private var rotation = -1

    val keyboardHeight: Int
        get() = if (isLandscape) keyboardLandscapeHeight else keyboardPortraitHeight

    val isLandscape: Boolean
        get() {
            val rotation = deviceRotation
            return rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270
        }

    private val deviceRotation: Int
        get() = ServiceUtil.getWindowManager(context).defaultDisplay.rotation

    private val keyboardLandscapeHeight: Int
        get() = Math.max(height, rootView.height) / 2

    private//return Util.clamp(keyboardHeight, minCustomKeyboardSize, getRootView().getHeight() - minCustomKeyboardTopMargin);
    var keyboardPortraitHeight: Int
        get() {
            val keyboardHeight = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("keyboard_height_portrait", defaultCustomKeyboardSize)
            return Math.min(
                Math.max(keyboardHeight, minCustomKeyboardSize),
                rootView.height - minCustomKeyboardTopMargin
            )
        }
        set(height) = PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putInt("keyboard_height_portrait", height).apply()

    init {
        val statusBarRes = resources.getIdentifier("status_bar_height", "dimen", "android")
        minKeyboardSize = resources.getDimensionPixelSize(R.dimen.min_keyboard_size)
        minCustomKeyboardSize = resources.getDimensionPixelSize(R.dimen.min_custom_keyboard_size)
        defaultCustomKeyboardSize = resources.getDimensionPixelSize(R.dimen.default_custom_keyboard_size)
        minCustomKeyboardTopMargin = resources.getDimensionPixelSize(R.dimen.min_custom_keyboard_top_margin)
        statusBarHeight = if (statusBarRes > 0) resources.getDimensionPixelSize(statusBarRes) else 0
        viewInset = getViewInset()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        updateRotation()
        updateKeyboardState()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun updateRotation() {
        val oldRotation = rotation
        rotation = deviceRotation
        if (oldRotation != rotation) {
            Log.i(TAG, "rotation changed")
            onKeyboardClose()
        }
    }

    private fun updateKeyboardState() {
        if (isLandscape) {
            if (isKeyboardOpen) onKeyboardClose()
            return
        }

        if (viewInset == 0 && Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP)
            viewInset = getViewInset()
        val availableHeight = this.rootView.height - statusBarHeight - viewInset
        getWindowVisibleDisplayFrame(rect)

        val keyboardHeight = availableHeight - (rect.bottom - rect.top)

        if (keyboardHeight > minKeyboardSize) {
            if (keyboardHeight != keyboardHeight) keyboardPortraitHeight = keyboardHeight
            if (!isKeyboardOpen) onKeyboardOpen(keyboardHeight)
        } else if (isKeyboardOpen) {
            onKeyboardClose()
        }
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private fun getViewInset(): Int {
        try {
            val attachInfoField = View::class.java.getDeclaredField("mAttachInfo")
            attachInfoField.isAccessible = true
            val attachInfo = attachInfoField.get(this)
            if (attachInfo != null) {
                val stableInsetsField = attachInfo.javaClass.getDeclaredField("mStableInsets")
                stableInsetsField.isAccessible = true
                val insets = stableInsetsField.get(attachInfo) as Rect
                return insets.bottom
            }
        } catch (nsfe: NoSuchFieldException) {
            Log.w(TAG, "field reflection error when measuring view inset", nsfe)
        } catch (iae: IllegalAccessException) {
            Log.w(TAG, "access reflection error when measuring view inset", iae)
        }

        return 0
    }

    protected fun onKeyboardOpen(keyboardHeight: Int) {
        Log.i(TAG, "onKeyboardOpen($keyboardHeight)")
        isKeyboardOpen = true

        notifyShownListeners()
    }

    protected fun onKeyboardClose() {
        Log.i(TAG, "onKeyboardClose()")
        isKeyboardOpen = false
        notifyHiddenListeners()
    }

    fun postOnKeyboardClose(runnable: Runnable) {
        if (isKeyboardOpen) {
            addOnKeyboardHiddenListener(object : OnKeyboardHiddenListener {
                override fun onKeyboardHidden() {
                    removeOnKeyboardHiddenListener(this)
                    runnable.run()
                }
            })
        } else {
            runnable.run()
        }
    }

    fun postOnKeyboardOpen(runnable: Runnable) {
        if (!isKeyboardOpen) {
            addOnKeyboardShownListener(object : OnKeyboardShownListener {
                override fun onKeyboardShown() {
                    removeOnKeyboardShownListener(this)
                    runnable.run()
                }
            })
        } else {
            runnable.run()
        }
    }

    fun addOnKeyboardHiddenListener(listener: OnKeyboardHiddenListener) {
        hiddenListeners.add(listener)
    }

    fun removeOnKeyboardHiddenListener(listener: OnKeyboardHiddenListener) {
        hiddenListeners.remove(listener)
    }

    fun addOnKeyboardShownListener(listener: OnKeyboardShownListener) {
        shownListeners.add(listener)
    }

    fun removeOnKeyboardShownListener(listener: OnKeyboardShownListener) {
        shownListeners.remove(listener)
    }

    private fun notifyHiddenListeners() {
        val listeners = HashSet(hiddenListeners)
        for (listener in listeners) {
            listener.onKeyboardHidden()
        }
    }

    private fun notifyShownListeners() {
        val listeners = HashSet(shownListeners)
        for (listener in listeners) {
            listener.onKeyboardShown()
        }
    }

    interface OnKeyboardHiddenListener {
        fun onKeyboardHidden()
    }

    interface OnKeyboardShownListener {
        fun onKeyboardShown()
    }

    companion object {
        private val TAG = KeyboardAwareLinearLayout::class.java.simpleName
    }
}
