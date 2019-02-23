package com.xlqianbao.and.http.subscribers

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.xlqianbao.and.R


/**
 * 网络请求时显示的loading
 * Created by Makise on 16/8/2.
 */
class ProgressDialogHandler(private val context: Context, private val cancelable: Boolean) : Handler() {
    private var dialog: AlertDialog? = null

    private fun initProgressDialog() {
        if (context is Activity && context.isFinishing) {
            return
        }
        if (dialog == null) {
            val builder: AlertDialog.Builder
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                builder = AlertDialog.Builder(context, R.style.dialogTransparent)
            } else {
                builder = AlertDialog.Builder(context)
            }
            dialog = builder.create()
            dialog!!.setCanceledOnTouchOutside(false)
            dialog!!.setCancelable(cancelable)
        }
        val view = View.inflate(context, R.layout.dialog_loading, null)
        val iv_load = view.findViewById(R.id.iv_load) as ImageView
        val operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_loading)
        operatingAnim.interpolator = LinearInterpolator()
        iv_load.startAnimation(operatingAnim)
        if (!dialog!!.isShowing) {
            dialog!!.show()
        }
        dialog!!.window!!.setContentView(view)
    }

    private fun dismissProgressDialog() {
        if (context is Activity && context.isFinishing) {
            return
        }
        if (dialog != null)
            dialog!!.dismiss()
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            SHOW_PROGRESS_DIALOG -> initProgressDialog()
            DISMISS_PROGRESS_DIALOG -> dismissProgressDialog()
        }
    }

    companion object {

        val SHOW_PROGRESS_DIALOG = 1
        val DISMISS_PROGRESS_DIALOG = 2
    }

}
