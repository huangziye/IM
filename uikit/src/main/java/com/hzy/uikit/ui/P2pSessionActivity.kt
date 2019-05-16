package com.hzy.uikit.ui

import android.content.Context
import android.content.Intent
import com.hzy.uikit.R
import com.hzy.uikit.session.SessionInputPanel
import com.hzy.uikit.widget.KeyboardAwareLinearLayout
import com.netease.nimlib.sdk.msg.model.RecentContact
import kotlinx.android.synthetic.main.activity_p2p_session.*

/**
 *
 * @author: ziye_huang
 * @date: 2019/5/15
 */
class P2pSessionActivity : BaseSessionActivity(), KeyboardAwareLinearLayout.OnKeyboardShownListener,
    KeyboardAwareLinearLayout.OnKeyboardHiddenListener, SessionInputPanel.OnInputPanelStateChangeListener {

    fun initView() {
        inputPanelFrameLayout.init(this,rootLinearLayout)
        inputPanelFrameLayout.setOnInputPanelStateChangeListener(this)
    }

    override fun initLayoutId(): Int {
        return R.layout.activity_p2p_session
    }

    override fun initBeforeViews() {
        super.initBeforeViews()
    }

    override fun initAfterViews() {
        super.initAfterViews()
        initView()
    }

    override fun onKeyboardHidden() {

    }

    override fun onKeyboardShown() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInputPanelExpanded() {
//        msgRecyclerView.scrollToPosition(mad)
    }

    override fun onInputPanelCollapsed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        fun startActivity(context: Context, recentContact: RecentContact) {
            val intent = Intent()
            /*intent.putExtra(Extras.EXTRA_ACCOUNT, contactId)
            intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization)
            if (anchor != null) {
                intent.putExtra(Extras.EXTRA_ANCHOR, anchor)
            }*/
            intent.putExtra("sessionId", recentContact.fromAccount)
            intent.setClass(context, P2pSessionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        }
    }

}