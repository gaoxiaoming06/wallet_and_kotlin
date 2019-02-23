package com.xlqianbao.and.ext

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.xlqianbao.and.R

/**
 * Activity的扩展类
 * Created by Makise on 2017/5/23.
 */

/**
 * activity跳转

 * @param intent
 * *
 * @param isFinish
 */
fun Activity.startNewActivity(intent: Intent, isFinish: Boolean) {
    startNewActivity(intent, R.anim.push_left_in, R.anim.push_left_out, isFinish)
}

/**
 * activity按照一定的动画效果跳转

 * @param intent
 * *
 * @param enterAnim
 * *
 * @param exitAnim
 * *
 * @param isFinish
 */
fun Activity.startNewActivity(intent: Intent, enterAnim: Int, exitAnim: Int, isFinish: Boolean) {
    startActivity(intent)
    overridePendingTransition(enterAnim, exitAnim)
    if (isFinish) {
        finish()
    }
}

fun Activity.toast(message: String, duration: Int) {
    Toast.makeText(this, message, duration).show()
}