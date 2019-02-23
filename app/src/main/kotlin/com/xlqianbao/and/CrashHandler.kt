package com.xlqianbao.and

import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast

/**
 * 全局异常捕获
 * Created by Makise on 2017/2/4.
 */

class CrashHandler
/**
 * 保证只有一个CrashHandler实例
 */
private constructor() : Thread.UncaughtExceptionHandler {
    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    // 程序的Context对象
    private var mContext: Context? = null

    /**
     * 初始化

     * @param context
     */
    fun init(context: Context) {
        mContext = context
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (ex.message!!.contains("Add `onError` handling")) {
            //经测试 该异常不会影响程序正常运行 暂时不做处理
            return
        }
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                Log.e(TAG, "error : ", e)
            }

            //退出程序
            App.instance.exit()
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.

     * @param ex
     * *
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        // 使用Toast来显示异常信息
        object : Thread() {
            override fun run() {
                Looper.prepare()
                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show()
                Looper.loop()
            }
        }.start()
        ex.printStackTrace()
        return true
    }

    companion object {
        val TAG = "CrashHandler"
        // CrashHandler实例
        /**
         * 获取CrashHandler实例 ,单例模式
         */
        val instance = CrashHandler()
    }
}
