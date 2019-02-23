package com.xlqianbao.and.ui.regLogin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import cn.jpush.android.api.JPushInterface
import com.twotiger.library.utils.*
import com.twotiger.library.widget.keyboard.KeyboardListener
import com.twotiger.library.widget.keyboard.SecurityKeyboard
import com.twotiger.library.widget.keyboard.SecurityKeyboardBuilder
import com.xlqianbao.and.App
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.ResultException
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.WebViewPageActivity
import com.xlqianbao.and.utils.ToastUtil
import com.xlqianbao.and.utils.rx.RxBus
import kotlinx.android.synthetic.main.fragment_reg_login.*
import kotlinx.android.synthetic.main.include_title_bar.*

/**
 * Created by Liz on 2017/9/4.
 * desc:
 */
class RegLoginFragment : BaseFragment(), View.OnClickListener {
    private var keyboard: SecurityKeyboard? = null
    lateinit private var timer: CountDownTimer
    private var canSend: Boolean = true

    private var regLoginMsg: Model.RegLoginMessage? = null
    private var phone: Model.CheckPhone? = null

    //防止页面重复进入倒计时按钮错乱问题
    private var tempPhoneNumber: String? = null

    companion object {
        //获取单例
        lateinit var instance: RegLoginFragment
            private set
    }

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_reg_login, null)
    }

    override fun initData() {
        instance = this
        tempPhoneNumber = ""
        //titleBar
        iv_code.setOnClickListener(this)
        voice_code.setOnClickListener(this)
        btn_finish.setOnClickListener(this)
        left.setOnClickListener(this)
        tv_fwxy.setOnClickListener(this)
        ll_root.setOnClickListener(this)

        keyboard = SecurityKeyboardBuilder(activity).build()
        keyboard.run {
            this!!.setType(SecurityKeyboard.Type.Standard)
            setTitle("请输入短信验证码")
            setKeyboardListener(object : KeyboardListener(keyboard, et_code) {}.needZeroBeginning())
        }

        et_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (charSequence!!.isEmpty()) {
                    error_text.text = ""
                    error_text.visibility = View.INVISIBLE
                    et_code.textSize = 14f
                    et_code.setTextColor(resources.getColor(R.color.xl_gray))
                } else {
                    et_code.textSize = 16f
                    et_code.setTextColor(resources.getColor(R.color.xl_gray_headline))
                }
                btn_finish.isEnabled = et_code.text.toString().length == 6
            }
        })

        //初始化计时器
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                iv_code.text = (millisUntilFinished / 1000).toString() + "秒"
            }

            override fun onFinish() {
                iv_code.isEnabled = true
                iv_code.text = "重新发送"
                canSend = true
                voice_code.text = Html.fromHtml("<font color='#999999'>收不到短信，请尝试</font> <font color='#5f8bf3'>语音获取</font>")
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putSerializable("msg", regLoginMsg)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) return
        setData(savedInstanceState.getSerializable("msg") as Model.RegLoginMessage)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.iv_code -> sendVCode()
            R.id.voice_code -> if (canSend) getCode()
            R.id.btn_finish -> enter()
            R.id.left -> {
                activity.onBackPressed()
                hideKeyboard()
            }
            R.id.tv_fwxy -> toFwxy()
            R.id.ll_root -> hideKeyboard()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            et_code.setText("")
            if (keyboard != null)
                keyboard!!.show(650)
        }
    }

    private fun toFwxy() {
        val data = activity.getConstantDataByKey(Constants.DZ_ZCXY) ?: return
        val intent = Intent(activity, WebViewPageActivity::class.java)
        intent.putExtra("url", data.`val`)
        intent.putExtra("title", data.name)
        activity.startNewActivity(intent, false)
    }

    private fun getCode() {
        if (phone == null) return
        if (ViewUtils.isFastDoubleClick(view)) return
        HttpMethods.instance.sendValidCode(phone!!.vticket, "voice", object : ProgressSubscriber<String>(true) {
            override fun onNext(t: String) {
                //3秒提示框
                ToastUtil.showError(activity, "验证码将以电话的形式获取，请注意接听", false)
                voice_code.text = Html.fromHtml("<font color='#999999'>收不到短信，请尝试 语音获取</font>")
                canSend = false
            }
        })
    }

    fun setData(regLoginMessage: Model.RegLoginMessage?) {
        if (regLoginMessage == null) return
        regLoginMsg = regLoginMessage
        phone = regLoginMsg!!.phone
        if (regLoginMsg!!.phone_number != tempPhoneNumber) {
            timer.cancel()
            timer.start()
            iv_code.isEnabled = false
            ll_send_code.visibility = View.VISIBLE
            tempPhoneNumber = regLoginMsg!!.phone_number
        }
        //把传来的手机号码设置到页面上显示
        tv_phone.text = regLoginMsg!!.phone_number
        //如是注册 显示同意协议
        ll_agree_rights.visibility = if (phone!!.mark == 0) View.VISIBLE else View.INVISIBLE
        //判断显示注册还是登录布局
        btn_finish.text = if (phone!!.mark == 0) "注册" else "登录"
        middle.text = if (phone!!.mark == 0) "注册" else "登录"
    }

    private fun sendVCode() {
        if (phone == null) return
        if (ViewUtils.isFastDoubleClick(view)) return
        HttpMethods.instance.sendValidCode(phone!!.vticket, "sms",
                object : ProgressSubscriber<String>(true) {
                    override fun onNext(s: String) {
                        ll_send_code.visibility = View.VISIBLE
                        timer.cancel()
                        timer.start()
                        iv_code.isEnabled = false
                    }

                    override fun onComplete() {
                        super.onComplete()
                        Toast.makeText(activity, "短信验证码已发送", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        LogUtil.info(RegLoginFragment::class.java, e.message)
                        error_text.text = NetWorkUtil.networkStatusInfo(e)
                    }
                })
    }

    private fun enter() {
        if (ViewUtils.isFastDoubleClick(view)) return
        if (TextUtils.isEmpty(et_code.text.toString())) {
            Toast.makeText(activity, "请输入短信验证码！", Toast.LENGTH_SHORT).show()
            et_code.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.et_shake))
            return
        }
        HttpMethods.instance
                .login(phone!!.vticket, et_code.text.toString(), Tools.getDeviceId(activity), JPushInterface.getRegistrationID(activity), Tools.getChannelId(activity),
                        object : ProgressSubscriber<Model.User>(true) {
                            override fun onNext(user: Model.User) {
                                if (user.token != null) {
                                    //持久化user信息
                                    App.instance.saveUser(user)
                                    if (phone!!.mark == 0) {//注册
                                        //关闭加息计划页面
                                        RxBus.default.postSticky("close")
                                        //切换到我的页面
                                        RxBus.default.postSticky(Model.HomeTabMessage(R.id.btn_3))
                                        //发送user信息到各个页面
                                        RxBus.default.postSticky(Model.LoginMessage(true, user))
                                    } else {
                                        //发送user信息到各个页面
                                        RxBus.default.postSticky(Model.LoginMessage(false, user))
                                    }
                                    activity.finish()
                                    activity.overridePendingTransition(R.anim.silent_anim, R.anim.slide_out_to_bottom)
                                }
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                //显示错误信息
                                error_text.text = NetWorkUtil.networkStatusInfo(e)
                                //验证码错误 晃动控件
                                if (e is ResultException.CaptchaErrorException)
                                    et_code.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.et_shake))
                            }
                        })
    }


    /**
     * 点击空白处隐藏键盘 代替popupwindow的setOutsideTouchable
     *
     * @param view
     */
    private fun hideKeyboard() {
        //隐藏键盘
        keyboard!!.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        //释放
        SecurityKeyboard.destroy(keyboard)
        timer.cancel()
    }
}