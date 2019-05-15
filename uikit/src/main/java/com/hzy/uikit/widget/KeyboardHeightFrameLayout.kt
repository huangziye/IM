package com.hzy.uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class KeyboardHeightFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), InputAwareLayout.InputView {

    override fun show(height: Int, immediate: Boolean) {
        // TODO
        val layoutParams = layoutParams
        layoutParams.height = height
        getChildAt(0).visibility = View.VISIBLE
        visibility = View.VISIBLE
    }

    override fun hide(immediate: Boolean) {
        visibility = View.GONE
    }

    override val isShowing: Boolean
        get() = visibility == View.VISIBLE

}
