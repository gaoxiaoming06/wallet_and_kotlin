package com.xlqianbao.and.ui.auth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.twotiger.library.utils.CountDownTimer
import com.twotiger.library.utils.ViewUtils
import com.twotiger.library.widget.keyboard.KeyboardListener
import com.twotiger.library.widget.keyboard.SecurityKeyboard
import com.twotiger.library.widget.keyboard.SecurityKeyboardBuilder
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.listener.OnTextChangedListener
import kotlinx.android.synthetic.main.fragment_bank_auth_step_two.*
import kotlinx.android.synthetic.main.include_title_bar.*

/**
 * Created by gao on 2017/9/26.
 */
class BankAuthStepTwoFragment:BaseFragment(){

    private var number: String? = null
    private var phone:String? = null
    private var vticket:String? = null
    private var tempPhone: String? = null

    private lateinit var timer: CountDownTimer
    private lateinit var codeKeyboard: SecurityKeyboard

    companion object {
        private lateinit var instance: BankAuthStepTwoFragment
        fun getInstan(): BankAuthStepTwoFragment {
            return instance
        }
    }

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_bank_auth_step_two, container, false)
    }

    override fun initData() {
        left.setOnClickListener{activity.onBackPressed()}
        middle.text = "银行卡认证"

        instance = this

        codeKeyboard = SecurityKeyboardBuilder(activity).build()
        codeKeyboard.setType(SecurityKeyboard.Type.Standard)
        codeKeyboard.setKeyboardListener(object : KeyboardListener(codeKeyboard, et_code) {

        }.needZeroBeginning())

        //初始化计时器
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_code.text = (millisUntilFinished / 1000).toString() + "s重发"
            }

            override fun onFinish() {
                tv_code.isEnabled = true
                tv_code.text = "重新发送"
            }
        }

        tv_code.setOnClickListener{v -> sendVcode(v) }
        btn_submit.setOnClickListener{v -> submit(v) }

        et_code.addTextChangedListener(object : OnTextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                btn_submit.isEnabled = charSequence.length == 6
            }
        })
    }

    fun setData(number: String, phone: String, vticket: String) {
        this.number = number
        this.phone = phone
        this.vticket = vticket
        if (phone != tempPhone) {
            tempPhone = phone
//            timer.start()
//            binding.tvCode.setEnabled(false)
//            codeKeyboard.show()
        }
    }

    fun sendVcode(view: View) {
        val user = activity.getUser()
        HttpMethods.instance
                .preValidCard(user.token!!, user.name!!, user.idCard!!, number!!, phone!!,
                        object : ProgressSubscriber<Model.PreValidCard>(true) {
                            override fun onNext(preValidCard: Model.PreValidCard) {
                                //发送验证码成功
                                vticket = preValidCard.vticket
                                Toast.makeText(activity, "短信验证码已发送", Toast.LENGTH_SHORT).show()
                                timer.start()
                                tv_code.isEnabled = false
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                ll_error.visibility = View.VISIBLE
                                tv_error.text = e.message
                            }
                        })
    }

    fun submit(view: View) {
        if (ViewUtils.isFastDoubleClick(view)) return
        val code = et_code.text.toString()
        HttpMethods.instance
                .bindCard(activity.getToken()!!, vticket!!, code,
                        object : ProgressSubscriber<String>(true) {
                            override fun onNext(code: String) {
                                //绑定成功
                                Toast.makeText(activity, "绑卡成功", Toast.LENGTH_SHORT).show()
                                activity.finish()
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                ll_error.visibility = View.VISIBLE
                                tv_error.text = e.message
                            }
                        })
    }


    override fun onDestroy() {
        super.onDestroy()
        SecurityKeyboard.destroy(codeKeyboard)
        timer.cancel()
    }

}