package com.xlqianbao.and

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.alibaba.fastjson.JSON
import com.twotiger.library.utils.LogUtil
import com.xlqianbao.and.ui.MainActivity
import com.xlqianbao.and.ui.WebViewPageActivity
import com.xlqianbao.and.ui.home.HomeActivity

/**
 * 自定义接收器
 *
 *
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
class XlqianbaoPushReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val bundle = intent.extras ?: return
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.action + ", extras: "
                + printBundle(bundle))

        if (JPushInterface.ACTION_REGISTRATION_ID == intent.action) {  //刚注册时的Registration ID
            val regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID)

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED == intent.action) { //接收到推送的自定义消息

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED == intent.action) { //收到了通知 Push。
            val notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID).toLong()
            LogUtil.info("notifactionId收到" + notifactionId)
            LogUtil.info("messageId收到" + bundle.getString(JPushInterface.EXTRA_MSG_ID)!!)

            //            Toast.makeText(context, "收到来自通知JPUSH的通知", Toast.LENGTH_SHORT).show();

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED == intent.action) {  //用户点击了通知。
            val notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID).toLong()
            LogUtil.info("notifactionId点击" + notifactionId)


            val notifactionTitle = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE)
            val notifactionContent = bundle.getString(JPushInterface.EXTRA_EXTRA)

            parscoustommsg(context, notifactionContent, notifactionTitle)

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK == intent.action) {
            Log.d(
                    TAG,
                    "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA)!!)
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE == intent.action) {
            val connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE,
                    false)
            Log.e(TAG, "[MyReceiver]" + intent.action + " connected state change to "
                    + connected)
        } else {
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.action!!)
        }
    }

    /**
     * 处理扩展字段
     *
     * @param context
     * @param extra
     * @param title
     */
    private fun parscoustommsg(context: Context, extra: String?, title: String?) {
        val jPushBean = JSON.parseObject<Model.JPushBean>(extra, Model.JPushBean::class.java)
        val intent: Intent
        if (checkAppIsRunning(context)) {
            //APP运行中
            if (jPushBean != null
                    && "link" == jPushBean!!.type
                    && !TextUtils.isEmpty(jPushBean!!.target)
                    && jPushBean!!.target.startsWith("http")) {
                intent = Intent(context, WebViewPageActivity::class.java)
                intent.putExtra("url", jPushBean!!.target)
                intent.putExtra("title", title)
            } else {
                //没有扩展字段，进入首页
                intent = Intent(context, HomeActivity::class.java)
            }
        } else {
            intent = Intent(context, MainActivity::class.java)
            jPushBean!!.name = title!!
            intent.putExtra("PUSH", jPushBean)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * 检查app是否运行中
     *
     * @param context
     * @return
     */
    private fun checkAppIsRunning(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningTasks(100)
        var isAppRunning = false
        val MY_PKG_NAME = "com.xlqianbao.and"
        for (info in list) {
            if (info.topActivity.packageName == MY_PKG_NAME || info.baseActivity.packageName == MY_PKG_NAME) {
                isAppRunning = true
                Log.i(TAG, info.topActivity.packageName + " info.baseActivity.getPackageName()=" + info.baseActivity.packageName)
                break
            }
        }
        return isAppRunning
    }

    companion object {
        private val TAG = "JPush"

        private fun printBundle(bundle: Bundle): String {
            val sb = StringBuilder()
            for (key in bundle.keySet()) {
                if (key == JPushInterface.EXTRA_NOTIFICATION_ID) {
                    sb.append("\nkey:" + key + ", value:" + bundle.getInt(key))
                } else if (key == JPushInterface.EXTRA_CONNECTION_CHANGE) {
                    sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key))
                } else {
                    sb.append("\nkey:" + key + ", value:" + bundle.getString(key))
                }
            }
            return sb.toString()
        }
    }
}
