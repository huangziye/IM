package com.hzy.uikit.session

import android.content.Context
import android.text.Selection
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.hzy.uikit.R
import com.hzy.uikit.widget.InputAwareLayout
import com.lqr.emoji.IEmotionExtClickListener
import com.lqr.emoji.IEmotionSelectedListener
import kotlinx.android.synthetic.main.session_input_panel.view.*

/**
 * 输入面板
 * @author: ziye_huang
 * @date: 2019/5/15
 */
class SessionInputPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IEmotionSelectedListener, View.OnClickListener {

    private var mActivity: FragmentActivity? = null
    private var mRootInputAwareLayout: InputAwareLayout? = null
    private var mOnInputPanelStateChangeListener: OnInputPanelStateChangeListener? = null

    fun init(activity: FragmentActivity, rootInputAwareLayout: InputAwareLayout) {
        var view = LayoutInflater.from(activity).inflate(R.layout.session_input_panel, this, true)
        mActivity = activity
        mRootInputAwareLayout = rootInputAwareLayout

        //listener
        extImageView.setOnClickListener(this)

        // emotion
        emotionLayout.attachEditText(editText)
        emotionLayout.setEmotionAddVisiable(true)
        emotionLayout.setEmotionSettingVisiable(true)
        emotionLayout.setEmotionSelectedListener(this)
        emotionLayout.setEmotionExtClickListener(object:IEmotionExtClickListener{
            override fun onEmotionAddClick(view: View?) {
                Toast.makeText(activity, "add", Toast.LENGTH_SHORT).show()
            }

            override fun onEmotionSettingClick(view: View?) {
                Toast.makeText(activity, "setting", Toast.LENGTH_SHORT).show()
            }
        })

        editText.setOnKeyListener(object: OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_DOWN) {
                    val buffer = (v as EditText).text
                    // If the cursor is at the end of a MentionSpan then remove the whole span
                    val start = Selection.getSelectionStart(buffer)
                    val end = Selection.getSelectionEnd(buffer)
                    if (start == end) {
                        /*val mentions = buffer.getSpans(start, end, MentionSpan::class.java)
                        if (mentions.size > 0) {
                            buffer.replace(
                                buffer.getSpanStart(mentions[0]),
                                buffer.getSpanEnd(mentions[0]),
                                ""
                            )
                            buffer.removeSpan(mentions[0])
                            return true
                        }*/
                    }
                    return false
                }
                return false
            }
        })




    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.extImageView -> {
                if (mRootInputAwareLayout?.currentInput === extContainerFrameLayout) {
                    mRootInputAwareLayout?.showSoftkey(editText)
                    hideConversationExtension()
                } else {
                    extImageView.setImageResource(R.mipmap.ic_chat_emoji)
                    showConversationExtension()
                }
            }
        }
    }


    private fun hideConversationExtension() {
        mOnInputPanelStateChangeListener?.onInputPanelCollapsed()
    }

    private fun showConversationExtension() {
        mOnInputPanelStateChangeListener?.onInputPanelExpanded()
    }

    /**
     * 设置输入面板状态变化listener
     */
    fun setOnInputPanelStateChangeListener(listener: OnInputPanelStateChangeListener) {
        mOnInputPanelStateChangeListener = listener
    }

    fun bind(activity: FragmentActivity, inputAwareLayout: InputAwareLayout) {

    }

    override fun onEmojiSelected(key: String) {

    }

    override fun onStickerSelected(categoryName: String, stickerName: String, stickerBitmapPath: String) {

    }

    /**
     * 输入面板状态变化
     */
    interface OnInputPanelStateChangeListener {
        /**
         * 输入面板展开
         */
        fun onInputPanelExpanded()

        /**
         * 输入面板关闭
         */
        fun onInputPanelCollapsed()
    }
}
