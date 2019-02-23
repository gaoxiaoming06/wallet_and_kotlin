package com.xlqianbao.and.http.subscribers

import android.content.Context
import android.text.TextUtils
import com.twotiger.library.utils.LogUtil
import com.xlqianbao.and.base.BaseActivity
import io.reactivex.observers.DisposableObserver

/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by Makise on 16/8/2.
 */
abstract class ProgressSubscriber<T>(private val needProgress: Boolean) : DisposableObserver<T>() {

    private var mProgressDialogHandler: ProgressDialogHandler? = null

    private val context: Context
    private var showToast: Boolean = false

    init {
        this.context = BaseActivity.context!!
        //默认弹toast
        this.showToast = true
        if (needProgress) {
            mProgressDialogHandler = ProgressDialogHandler(context, true)
        }
    }

    private fun showProgressDialog() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler!!.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget()
        }
    }

    fun dismissProgressDialog() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler!!.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget()
            mProgressDialogHandler = null
        }
        if (!this.isDisposed) {
            this.dispose()
        }
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    override fun onStart() {
        if (needProgress) {
            showProgressDialog()
        }
    }

    /**
     * 完成，隐藏ProgressDialog
     */
    override fun onComplete() {
        if (needProgress) {
            dismissProgressDialog()
        }
    }

    /**
     * 调用此方法隐藏toast提示
     *
     * @return
     */
    fun hideToast(): ProgressSubscriber<T> {
        this.showToast = false
        return this
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog

     * @param e
     */
    override fun onError(e: Throwable) {
        if (needProgress) {
            dismissProgressDialog()
        }
        if (!TextUtils.isEmpty(e.message) && showToast)
            //非空 网络异常 toast
            LogUtil.info(ProgressSubscriber::class.java, e.message)
    }
}