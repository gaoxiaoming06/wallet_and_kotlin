package com.xlqianbao.and.ui.personal

import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.bumptech.glide.Glide
import com.twotiger.library.utils.ArithUtils
import com.twotiger.library.utils.CountDownTimer
import com.twotiger.library.widget.keyboard.KeyboardListener
import com.twotiger.library.widget.keyboard.SecurityKeyboard
import com.twotiger.library.widget.keyboard.SecurityKeyboardBuilder
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.listener.OnTextChangedListener
import com.xlqianbao.and.view.GlideCircleTransform
import kotlinx.android.synthetic.main.activity_order_repay.*

/**
 * Created by gao on 2017/9/22.
 */
class OrderRepayActivity:BaseActivity(){


    private lateinit var keyboard: SecurityKeyboard
    private var orderRepay: Model.OrderRepay? = null
    private lateinit var timer: CountDownTimer

    companion object {
        val EXTRA_DATA = "EXTRA_DATA"
    }

    override fun initView() {
        setContentView(R.layout.activity_order_repay)
        left.setOnClickListener({ startAnim(true); })
        setCustomFont(tv_amount)
    }

    override fun initData() {
        orderRepay = intent.getSerializableExtra(EXTRA_DATA) as Model.OrderRepay
        keyboard = SecurityKeyboardBuilder(this).build()
        keyboard.setType(SecurityKeyboard.Type.Standard)
        keyboard.hideShadow()
        keyboard.setKeyboardListener(object : KeyboardListener(keyboard, et_code) {
            override fun show() {
                super.show()
                val params = keyboard_area.layoutParams
                params.height = keyboard.keyboardHeight
                keyboard_area.layoutParams = params
            }

            override fun hide() {
                super.hide()
                val params = keyboard_area.getLayoutParams()
                params.height = 0
                keyboard_area.layoutParams = params
            }
        }.needZeroBeginning())

        //设置还款金额
        tv_amount.text = ArithUtils.coverMoneyCommaMaybeSmall(orderRepay?.amount.toString())
        //设置银行卡名称
        tv_bankName.text = orderRepay?.bankName
        //设置银行卡icon
        val data = getConstantDataByKey(Constants.DZ_BANKLOGO)
        if (data != null) {
            Glide.with(this@OrderRepayActivity)
                    .load(data.`val` + orderRepay?.bankCode + ".png")
                    .transform(GlideCircleTransform(this@OrderRepayActivity))
                    .error(R.drawable.my_icon_hk_gray)
                    .placeholder(R.drawable.my_icon_hk_gray)
                    .into(iv_bankIcon)
        }
        //监听输入框
        et_code.addTextChangedListener(object : OnTextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                btn_repay.isEnabled = charSequence.length == 6
            }
        })
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
        timer.start()
        startAnim(false)
    }

    /**
     * 发送验证码
     *
     * @param view
     */
    fun sendVCode(view: View) {
        HttpMethods.instance
                .orderRepay(getToken(), orderRepay?.orderId, object : ProgressSubscriber<Model.OrderRepay>(true) {
                    override fun onNext(orderRepay: Model.OrderRepay) {
                        //验证码已发送
                        Toast.makeText(this@OrderRepayActivity, "短信验证码已发送", Toast.LENGTH_SHORT).show()
                        timer.start()
                    }
                })
    }

    /**
     * 充值
     *
     * @param view
     */
    fun repay(view: View) {
        HttpMethods.instance
                .repay(getToken()!!, orderRepay?.orderId!!, et_code.text.toString().trim(),
                        object : ProgressSubscriber<String>(true) {
                            override fun onNext(code: String) {
                                //还款成功
                                Toast.makeText(this@OrderRepayActivity, "还款成功", Toast.LENGTH_SHORT).show()
                                startAnim(true)
                            }
                        })
    }

    /**
     * 位移动画
     *
     * @param isExit true 为退出动画 false为进入
     */
    private fun startAnim(isExit: Boolean) {
        val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, if (isExit) 0f else 1f,
                Animation.RELATIVE_TO_SELF, if (isExit) 1f else 0f
        )
        translateAnimation.fillAfter = true
        translateAnimation.duration = 300
        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                ll_main.visibility = View.VISIBLE
                if (isExit) {
                    finish()
                    overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        ll_main.startAnimation(translateAnimation)
    }


    override fun onBackPressed() {
        startAnim(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        SecurityKeyboard.destroy(keyboard)
    }

}