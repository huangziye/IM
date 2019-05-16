package com.hzy.im

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hzy.im.adapter.RecentContactsAdapter
import com.hzy.uikit.ui.P2pSessionActivity
import com.hzy.uikit.user.UserInfoObserver
import com.hzy.uikit.util.NimUtil
import com.hzy.utils.toast
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mAdapter: RecentContactsAdapter
    private lateinit var mRecentContacts: MutableList<RecentContact>
    private var userInfoObserver: UserInfoObserver? = null
    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages = HashMap<String, MutableSet<IMMessage>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        //设置增加或删除条目的动画
        recyclerView.itemAnimator = DefaultItemAnimator()

        mRecentContacts = mutableListOf()
        mAdapter = RecentContactsAdapter(this, mRecentContacts)
        mAdapter.setOnItemClickListener(object : RecentContactsAdapter.OnRecentItemClickListener{
            override fun setOnItemClickListener(view: View, position: Int) {
                P2pSessionActivity.startActivity(this@MainActivity,mRecentContacts[position])
            }
        })
        recyclerView.adapter = mAdapter
        queryRecentContacts()
        registerObservers(true)
    }

    /**
     * ********************** 收消息，处理状态变化 ************************
     */
    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(MsgServiceObserve::class.java)
        service.observeReceiveMessage(messageReceiverObserver, register)
        service.observeRecentContact(messageObserver, register)
//        service.observeMsgStatus(statusObserver, register)
//        service.observeRecentContactDeleted(deleteObserver, register)

//        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register)
        if (register) {
            registerUserInfoObserver()
        } else {
            unregisterUserInfoObserver()
        }
    }

    private fun registerUserInfoObserver() {
        if (userInfoObserver == null) {
            userInfoObserver = object : UserInfoObserver {
                override fun onUserInfoChanged(accounts: List<String>) {
//                    refreshMessages(false)
                }
            }
        }
//        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true)
    }

    private fun unregisterUserInfoObserver() {
        if (userInfoObserver != null) {
//            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false)
        }
    }

    private fun refreshMessages(unreadChanged: Boolean) {
        sortRecentContacts(mRecentContacts)
        mAdapter.notifyDataSetChanged()

        /*if (unreadChanged) {

            // 方式一：累加每个最近联系人的未读（快）

            var unreadNum = 0
            for (r in mRecentContacts) {
                unreadNum += r.getUnreadCount()
            }

            // 方式二：直接从SDK读取（相对慢）
            //int unreadNum = NIMClient.getService(MsgService.class).getTotalUnreadCount();

            if (callback != null) {
                callback.onUnreadCountChange(unreadNum)
            }

            Badger.updateBadgerCount(unreadNum)
        }*/
    }


    //监听在线消息中是否有@我
    private val messageReceiverObserver =
        Observer<List<IMMessage>> { imMessages ->
            if (imMessages != null) {
                for (imMessage in imMessages) {
                    var cacheMessageSet: MutableSet<IMMessage>? = cacheMessages[imMessage.sessionId]
                    if (cacheMessageSet == null) {
                        cacheMessageSet = HashSet()
                        cacheMessages[imMessage.sessionId] = cacheMessageSet
                    }
                    cacheMessageSet.add(imMessage)
                }
            }
        }

    internal var messageObserver: Observer<List<RecentContact>> =
        Observer { recentContacts ->
            onRecentContactChanged(recentContacts)
        }

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        var index: Int
        for (r in recentContacts) {
            index = -1
            for (i in mRecentContacts.indices) {
                if (r.contactId == mRecentContacts.get(i).getContactId() && r.sessionType == mRecentContacts.get(i).getSessionType()) {
                    index = i
                    break
                }
            }

            if (index >= 0) {
                mRecentContacts.removeAt(index)
            }

            mRecentContacts.add(r)
            /*if (r.sessionType == SessionTypeEnum.Team && cacheMessages[r.contactId] != null) {
                TeamMemberAitHelper.setRecentContactAited(r, cacheMessages[r.contactId])
            }*/
        }

        cacheMessages.clear()

        refreshMessages(true)
    }

    /**
     * 查询最近联系人列表数据
     */
    private fun queryRecentContacts() {
        NimUtil.queryRecentContacts(object : RequestCallback<List<RecentContact>> {
            override fun onSuccess(param: List<RecentContact>?) {
                mRecentContacts.addAll(param!!)
                mAdapter.notifyDataSetChanged()
            }

            override fun onFailed(code: Int) {
                "failed".toast(this@MainActivity)
            }

            override fun onException(exception: Throwable?) {
                exception?.message?.toast(this@MainActivity)
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    /**
     * **************************** 排序 ***********************************
     */
    // 置顶功能可直接使用，也可作为思路，供开发者充分利用RecentContact的tag字段
    val RECENT_TAG_STICKY: Long = 0x0000000000000001 // 联系人置顶tag

    private fun sortRecentContacts(list: List<RecentContact>) {
        if (list.size == 0) {
            return
        }
        Collections.sort(list, comp)
    }

    private val comp = Comparator<RecentContact> { o1, o2 ->
        // 先比较置顶tag
        val sticky = (o1.tag and RECENT_TAG_STICKY) - (o2.tag and RECENT_TAG_STICKY)
        if (sticky != 0L) {
            if (sticky > 0) -1 else 1
        } else {
            val time = o1.time - o2.time
            if (time == 0L) 0 else if (time > 0) -1 else 1
        }
    }
}
