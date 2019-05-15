package com.hzy.im

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.text.TextUtils
import com.hzy.uikit.ui.P2pSessionActivity
import com.hzy.utils.ACache
import com.hzy.utils.AppUtil
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.StatusBarNotificationConfig
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.uinfo.UserInfoProvider
import com.netease.nimlib.sdk.uinfo.model.UserInfo
import com.netease.nimlib.sdk.util.NIMUtil
import java.io.IOException

/**
 *
 * @author: ziye_huang
 * @date: 2019/5/15
 */
class NimApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录）
        NIMClient.init(this, loginInfo(), options())

        if (NIMUtil.isMainProcess(this)) {
            // 注意：以下操作必须在主进程中进行
            // 1、UI相关初始化操作
            // 2、相关Service调用
        }
    }

    /**
     * 如果返回值为 null，则全部使用默认参数。
     */
    private fun options(): SDKOptions? {
        val options = SDKOptions()

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        val config = StatusBarNotificationConfig()
        config.notificationEntrance = P2pSessionActivity::class.java // 点击通知栏跳转到该Activity
        config.notificationSmallIconId = R.mipmap.ic_stat_notify_msg
        // 呼吸灯配置
        config.ledARGB = Color.GREEN
        config.ledOnMs = 1000
        config.ledOffMs = 1500
        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.hzy.im/raw/msg"
        options.statusBarNotificationConfig = config

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用采用默认路径作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        val sdkPath = getAppCacheDir(this) + "/nim" // 可以不设置，那么将采用默认路径
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        options.sdkStorageRootPath = sdkPath

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true

        // 配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
        // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        options.thumbnailSize = (AppUtil.getScreenWidth(this) - 100) / 2

        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = object : UserInfoProvider {
            override fun getUserInfo(account: String?): UserInfo? {
                return null
            }

            override fun getAvatarForMessageNotifier(sessionType: SessionTypeEnum?, sessionId: String?): Bitmap? {
                return null
            }

            override fun getDisplayNameForMessageNotifier(
                account: String?,
                sessionId: String?,
                sessionType: SessionTypeEnum?
            ): String? {
                return null
            }

        }
        return options
    }

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private fun loginInfo(): LoginInfo? {
        val loginInfo = ACache.get(this).getAsObject("loginInfo") ?: return null
        return loginInfo as LoginInfo
    }

    /**
     * 配置 APP 保存图片/语音/文件/log等数据的目录
     * 这里示例用SD卡的应用扩展存储目录
     */
    fun getAppCacheDir(context: Context): String? {
        var storageRootPath: String? = null
        try {
            // SD卡应用扩展存储区(APP卸载后，该目录下被清除，用户也可以在设置界面中手动清除)，请根据APP对数据缓存的重要性及生命周期来决定是否采用此缓存目录.
            // 该存储区在API 19以上不需要写权限，即可配置 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
            if (context.externalCacheDir != null) {
                storageRootPath = context.externalCacheDir.canonicalPath
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (TextUtils.isEmpty(storageRootPath)) {
            // SD卡应用公共存储区(APP卸载后，该目录不会被清除，下载安装APP后，缓存数据依然可以被加载。SDK默认使用此目录)，该存储区域需要写权限!
            storageRootPath = "${Environment.getExternalStorageDirectory()}/${context.packageName}"
        }

        return storageRootPath
    }
}