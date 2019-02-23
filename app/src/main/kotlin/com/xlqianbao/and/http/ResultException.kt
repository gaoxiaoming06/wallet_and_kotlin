package com.xlqianbao.and.http

import android.content.Intent
import com.xlqianbao.and.App
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.Model.User
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.base.BaseHttpResult
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.ui.ViewPagerActivity
import com.xlqianbao.and.utils.WebViewCommonSet
import com.xlqianbao.and.utils.rx.RxBus


/**
 * 返回错误处理
 * Created by Makise on 2017/2/4.
 */

class ResultException(baseResult: BaseHttpResult) : RuntimeException(baseResult.codeDesc) {
    init {
        handleException(baseResult)
    }

    /**
     * 处理返回异常信息
     *
     * @param baseResult
     */
    private fun handleException(baseResult: BaseHttpResult) {
        val code = baseResult.code
        when (code) {
            Constants.TOKEN_INVALID,
                //Token失效
            Constants.TOKEN_TIMEOUT,
                //Token过期
            Constants.USER_ERROR -> {
                //用户不存在
                //清空用户信息
                App.instance.saveUser(User())
                //清空webview缓存
                App.instance.clearWebViewCache()
                WebViewCommonSet.clearWebViewCache(BaseActivity.context)
                //发送退出登录的消息
                RxBus.default.postSticky(Model.LoginMessage(false, User()))
                //弹出登录页面
                BaseActivity.instance.startNewActivity(Intent(BaseActivity.instance, ViewPagerActivity::class.java)
                        .putExtra(ViewPagerActivity.VP_ACT_TYPE, ViewPagerActivity.TYPE_REG_LOGIN),
                        R.anim.slide_in_from_bottom, R.anim.silent_anim, false)
            }
            Constants.CAPTCHA_ERROR ->
                //验证码错误
                throw CaptchaErrorException(baseResult.codeDesc)
            Constants.SYSTEM_UPGRADE ->
                //系统升级中
                throw SystemUpgradeException(baseResult.codeDesc)
            Constants.REPAY_STATUS_LIMIT ->
                //已还款
                throw RepayStatusLimitException(baseResult.codeDesc)
        }
    }

    inner class CaptchaErrorException(detailMessage: String) : RuntimeException(detailMessage)

    inner class SystemUpgradeException(detailMessage: String) : RuntimeException(detailMessage)

    inner class RepayStatusLimitException(detailMessage: String) : RuntimeException(detailMessage)
}
