package com.xlqianbao.and.ui.auth

import android.content.Intent
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSONObject
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.ext.toast
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.WebViewPageActivity
import com.xlqianbao.and.utils.ContactUtils
import kotlinx.android.synthetic.main.activity_contact_auth.*
import kotlinx.android.synthetic.main.include_title_bar.*


/**
 * Created by Makise on 2017/9/14.
 */
class ContactAuthActivity : BaseActivity(), View.OnClickListener {

    //已读协议
    private var hasRead: Boolean = false

    override fun initView() {
        setContentView(R.layout.activity_contact_auth)
        left.setOnClickListener({ onBackPressed() })
        middle.text = "通讯录授权"
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        //判断是否已认证
        if ("1" == getUser().TelBookAF) {
            rl_auth_success.visibility = View.VISIBLE
            ll_auth.visibility = View.GONE
            return
        }
        hasRead = true
        tv_xy1.setOnClickListener(this)
        tv_xy2.setOnClickListener(this)
        btn_submit.setOnClickListener(this)
        cb_xy.setOnCheckedChangeListener({ _, b -> hasRead = b })
    }

    private fun upload(list: List<Model.ContactInfo>) {
        //临时集合
        val tempJsonObj = JSONObject()
        //姓名
        val nameList = arrayListOf<String>()
        list.mapTo(nameList) { it.name }
        tempJsonObj.put("names", nameList)
        //手机号
        val phoneList = arrayListOf<String>()
        list.mapTo(phoneList) { it.mobile }
        tempJsonObj.put("phones", phoneList)
        //生成data Json串
        val data = tempJsonObj.toJSONString()
        HttpMethods.instance
                .contactUpload(getToken()!!, data, object : ProgressSubscriber<String>(true) {
                    override fun onNext(code: String) {
                        //上传成功
                        Toast.makeText(this@ContactAuthActivity, "授权成功", Toast.LENGTH_SHORT).show()
                        rl_auth_success.visibility = View.VISIBLE
                        ll_auth.visibility = View.GONE
                    }
                })
    }

    override fun onClick(view: View) {
        var data: Model.ConstantData?
        when (view.id) {
            R.id.tv_xy1 -> {
                data = getConstantDataByKey(Constants.DZ_TXLSQXY)
                if (data == null) return
                startNewActivity(Intent(this, WebViewPageActivity::class.java)
                        .putExtra("url", data.`val`), false)
            }
            R.id.tv_xy2 -> {
                data = getConstantDataByKey(Constants.DZ_XXSJJSYGZ)
                if (data == null) return
                startNewActivity(Intent(this, WebViewPageActivity::class.java)
                        .putExtra("url", data.`val`), false)
            }
            R.id.btn_submit -> {
                //提交
                if (!hasRead) {
                    toast("请同意协议", Toast.LENGTH_SHORT)
                    return
                }
                val list = ContactUtils.loadContacts(this@ContactAuthActivity)
                if (list == null) {
                    Toast.makeText(this@ContactAuthActivity, "未获得授权或者未获取到通讯录数据", Toast.LENGTH_SHORT).show()
                } else {
                    //上传
                    upload(list)
                }
            }
        }
    }
}