package com.hzy.uikit.util

import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import java.io.File


/**
 * Nim 相关工具类
 * @author: ziye_huang
 * @date: 2019/5/10
 */
object NimUtil {

    /**
     * 登录接口
     *
     * sdk会自动连接服务器，传递用户信息，返回登录结果。
     *
     * 该操作中途可取消。如果因为网络比较差，或其他原因导致服务器迟迟没有返回，用户也没有主动取消，
     * 在45秒后AbortableFuture的onFailed会被调用到。
     *
     * @param info 登录的用户信息
     */
    fun login(info: LoginInfo, callback: RequestCallback<LoginInfo>) {
        NIMClient.getService(AuthService::class.java).login(info).setCallback(callback)
    }

    /**
     * 登出
     *
     * 如果用户手动登出，不再接收消息和提醒，开发者可以调用 logout 方法，该方法没有回调。
     *
     * 注意: 登出操作，不要放在 Activity(Fragment) 的 onDestroy 方法中。
     */
    fun logout() {
        NIMClient.getService(AuthService::class.java).logout()
    }

    /**
     *  * 创建一条普通文本消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param text        文本消息内容
     * @return IMMessage 生成的消息对象
     */
    fun createTextMessage(sessionId: String, sessionType: SessionTypeEnum, text: String): IMMessage {
        return MessageBuilder.createTextMessage(sessionId, sessionType, text)
    }

    /**
     * 创建一条图片消息
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        图片文件
     * @param displayName 图片文件的显示名，可不同于文件名
     * @return IMMessage 生成的消息对象
     */
    fun createImageMessage(
        sessionId: String,
        sessionType: SessionTypeEnum,
        file: File,
        displayName: String
    ): IMMessage {
        return MessageBuilder.createImageMessage(sessionId, sessionType, file, displayName)
    }

    /**
     * 创建一条音频消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        音频文件对象
     * @param duration    音频文件持续时间，单位是ms
     * @return IMMessage 生成的消息对象
     */
    fun createAudioMessage(sessionId: String, sessionType: SessionTypeEnum, file: File, duration: Long): IMMessage {
        return MessageBuilder.createAudioMessage(sessionId, sessionType, file, duration)
    }

    /**
     * 创建一条视频消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        视频文件对象
     * @param duration    视频文件持续时间
     * @param width       视频宽度
     * @param height      视频高度
     * @param displayName 视频文件显示名，可以为空
     * @return 视频消息
     */
    fun createVideoMessage(
        sessionId: String,
        sessionType: SessionTypeEnum,
        file: File,
        duration: Long,
        width: Int,
        height: Int,
        displayName: String
    ): IMMessage {
        return MessageBuilder.createVideoMessage(sessionId, sessionType, file, duration, width, height, displayName)
    }

    /**
     * 创建一条文件消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param file        文件
     * @param displayName 文件的显示名，可不同于文件名
     * @return IMMessage 生成的消息对象
     */
    fun createFileMessage(sessionId: String, sessionType: SessionTypeEnum, file: File, displayName: String): IMMessage {
        return MessageBuilder.createFileMessage(sessionId, sessionType, file, displayName)
    }

    /**
     * 创建一条地理位置信息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @param lat         纬度
     * @param lng         经度
     * @param addr        地理位置描述信息
     * @return IMMessage 生成的消息对象
     */
    fun createLocationMessage(
        sessionId: String,
        sessionType: SessionTypeEnum,
        lat: Double,
        lng: Double,
        addr: String
    ): IMMessage {
        return MessageBuilder.createLocationMessage(sessionId, sessionType, lat, lng, addr)
    }

    /**
     * 创建一条提示消息
     *
     * @param sessionId   聊天对象ID
     * @param sessionType 会话类型
     * @return IMMessage 生成的消息对象
     */
    fun createTipMessage(sessionId: String, sessionType: SessionTypeEnum): IMMessage {
        return MessageBuilder.createTipMessage(sessionId, sessionType)
    }

    /**
     * 查询最近联系人列表数据
     *
     * @return InvocationFuture
     */
    fun queryRecentContacts(callback: RequestCallback<List<RecentContact>>) {
        NIMClient.getService(MsgService::class.java).queryRecentContacts().setCallback(callback)
    }


    /**
     *  发送消息
     *
     *   @param msg    带发送的消息体，由{@link MessageBuilder}构造
     *   @param resend 如果是发送失败后重发，标记为true，否则填false
     *   @return InvocationFuture 可以设置回调函数。消息发送完成后才会调用，如果出错，会有具体的错误代码。
     */
    fun sendMessage(msg: IMMessage, resend: Boolean, callback: RequestCallback<Void>) {
        return NIMClient.getService(MsgService::class.java).sendMessage(msg, resend).setCallback(callback)
    }

    /**
     * 注册/注销消息接收观察者。
     *     通知的消息列表中的消息不一定全是接收的消息，也有可能是自己发出去，比如其他端发的消息漫游过来，
     *     或者调用MsgService#saveMessageToLocal(IMMessage, boolean)后，notify参数设置为true，通知出来的消息。
     * @param observer 观察者， 参数为收到的消息列表，消息列表中的消息均保证来自同一个聊天对象。
     * @param register true为注册，false为注销
     */
    fun observeReceiveMessage(observer: Observer<List<IMMessage>>, register: Boolean) {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeReceiveMessage(observer, register)
    }

}