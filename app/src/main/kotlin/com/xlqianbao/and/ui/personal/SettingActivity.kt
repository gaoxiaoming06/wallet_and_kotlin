package com.xlqianbao.and.ui.personal

import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import com.twotiger.library.utils.PackageUtils
import com.xlqianbao.and.App
import com.xlqianbao.and.Model.LoginMessage
import com.xlqianbao.and.Model.User
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.ext.toast
import com.xlqianbao.and.utils.WebViewCommonSet
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.view.CustomDialog
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.include_title_bar.*

/**
 * 设置页面
 * Created by Makise on 2017/2/6.
 */

class SettingActivity : BaseActivity() {

    override fun initView() {
        setContentView(R.layout.activity_setting)
        left.setOnClickListener({ onBackPressed() })
        middle.text = "设置"
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        //版本号
        tv_ver.text = "v " + PackageUtils.getAppVersionName(this)
        if (getUser().token == null) {
            tv_logout.visibility = View.GONE
        }
    }

    fun checkUpdate(view: View) {
        //检查版本升级
        checkUpdate(true)
    }

    fun logout(view: View) {
        //退出登录
        CustomDialog.Builder(this)
                .setMessage("退出登录")
                .setPositiveButton("取消", DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .setNegativeButton("确定", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    saveUser(User())
                    toast("您已退出登录", Toast.LENGTH_SHORT)
                    //清空webview缓存
                    App.instance.clearWebViewCache()
                    WebViewCommonSet.clearWebViewCache(this@SettingActivity)
                    RxBus.default.postSticky(LoginMessage(false, User()))
                    finish()
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
                }).show()
    }
}
