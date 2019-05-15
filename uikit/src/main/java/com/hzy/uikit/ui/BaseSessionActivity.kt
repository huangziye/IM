package com.hzy.uikit.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

/**
 * 会话基类
 * @author: ziye_huang
 * @date: 2019/5/15
 */
abstract class BaseSessionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置竖屏显示
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initBeforeViews()
        setContentView(initLayoutId())
        initAfterViews()
    }

    /**
     * 布局文件
     */
    abstract fun initLayoutId(): Int

    /**
     * {@link AppCompatActivity#setContentView(int)}之前调用
     */
    protected open fun initBeforeViews() {

    }

    /**
     * {@link AppCompatActivity#setContentView(int)}之后调用
     */
    protected open fun initAfterViews() {

    }

    /**
     * 隐藏输入法
     */
    protected fun hideInputMethod() {
        val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        hideInputMethod()
    }

}