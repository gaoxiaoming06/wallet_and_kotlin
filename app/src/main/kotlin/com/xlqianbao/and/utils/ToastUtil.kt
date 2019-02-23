package com.xlqianbao.and.utils

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import java.util.*

/**
 * Created by Makise on 2016/8/5.
 */
object ToastUtil {
    // Toast
    private var toast: Toast? = null

    private var isError: Boolean = false

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    fun showShort(context: Context, message: CharSequence): Toast {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast!!.setText(message)
        }
        toast!!.show()

        return toast!!
    }

    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    fun showShort(context: Context, message: Int): Toast {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast!!.setText(message)
        }
        toast!!.show()

        return toast!!
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    fun showLong(context: Context, message: CharSequence): Toast {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast!!.setText(message)
        }
        toast!!.show()

        return toast!!
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    fun showLong(context: Context, message: Int) {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast!!.setText(message)
        }
        toast!!.show()
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    fun show(context: Context, message: CharSequence, duration: Int): Toast {
        if (null == toast) {
            toast = Toast.makeText(context, message, duration)
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast!!.setText(message)
        }
        toast!!.show()
        return toast!!
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    fun show(context: Context, message: Int, duration: Int) {
        if (null == toast) {
            toast = Toast.makeText(context, message, duration)
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast!!.setText(message)
        }
        toast!!.show()
    }

    /**
     * Hide the toast, if any.
     */
    fun hideToast() {
        if (null != toast) {
            toast!!.cancel()
        }
    }

    /**
     * 规定时间内只弹一次的toast
     *
     * @param context
     * @param message
     */
    fun showError(context: Context, message: CharSequence, center: Boolean) {
        //判断是否已弹出
        if (isError) return
        isError = true

        if (toast != null)
            toast = null
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        if (center)
            toast!!.setGravity(Gravity.CENTER, 0, 0)
        toast!!.show()

        //3秒后继续弹出
        Timer().schedule(object : TimerTask() {
            override fun run() {
                isError = false
            }
        }, 3000)
    }
}
