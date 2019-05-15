package com.hzy.uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.hzy.uikit.util.ServiceUtil

class InputAwareLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    KeyboardAwareLinearLayout(context, attrs, defStyle), KeyboardAwareLinearLayout.OnKeyboardShownListener {
    var currentInput: InputView? = null
        private set

    val isInputOpen: Boolean
        get() = isKeyboardOpen || currentInput != null && currentInput!!.isShowing

    init {
        addOnKeyboardShownListener(this)
    }

    override fun onKeyboardShown() {
        hideAttachedInput(true)
    }

    fun show(imeTarget: EditText, input: InputView) {
        if (isKeyboardOpen) {
            hideSoftkey(imeTarget, Runnable {
                hideAttachedInput(true)
                input.show(keyboardHeight, true)
                currentInput = input
            })
        } else {
            if (currentInput != null) currentInput!!.hide(true)
            input.show(keyboardHeight, currentInput != null)
            currentInput = input
        }
    }

    fun hideCurrentInput(imeTarget: EditText) {
        if (isKeyboardOpen)
            hideSoftkey(imeTarget, null)
        else
            hideAttachedInput(false)
    }

    fun hideAttachedInput(instant: Boolean) {
        if (currentInput != null) currentInput!!.hide(instant)
        currentInput = null
    }

    fun showSoftkey(inputTarget: EditText) {
        postOnKeyboardOpen(Runnable { hideAttachedInput(true) })
        inputTarget.post {
            inputTarget.requestFocus()
            ServiceUtil.getInputMethodManager(inputTarget.context).showSoftInput(inputTarget, 0)
        }
    }

    fun hideSoftkey(inputTarget: EditText, runAfterClose: Runnable?) {
        if (runAfterClose != null) postOnKeyboardClose(runAfterClose)

        ServiceUtil.getInputMethodManager(inputTarget.context)
            .hideSoftInputFromWindow(inputTarget.windowToken, 0)
    }

    interface InputView {

        val isShowing: Boolean
        fun show(height: Int, immediate: Boolean)

        fun hide(immediate: Boolean)
    }
}

