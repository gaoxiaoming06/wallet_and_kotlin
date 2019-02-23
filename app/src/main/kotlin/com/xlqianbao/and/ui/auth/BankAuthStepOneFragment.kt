package com.xlqianbao.and.ui.auth

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.twotiger.library.utils.StringUtils
import com.twotiger.library.utils.ViewUtils
import com.twotiger.library.widget.keyboard.KeyboardListener
import com.twotiger.library.widget.keyboard.SecurityKeyboard
import com.twotiger.library.widget.keyboard.SecurityKeyboardBuilder
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.listener.OnTextChangedListener
import kotlinx.android.synthetic.main.activity_guide.*
import kotlinx.android.synthetic.main.fragment_bank_auth_step_one.*
import kotlinx.android.synthetic.main.include_title_bar.*

/**
 * Created by gao on 2017/9/26.
 */
class BankAuthStepOneFragment : BaseFragment() {
    private lateinit var cardNoKeyboard: SecurityKeyboard
    private lateinit var phoneNoKeyboard: SecurityKeyboard
    private lateinit var user: Model.User
    private var tempPhone: String? = null

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_bank_auth_step_one, container, false)
    }

    override fun initData() {
        left.setOnClickListener { activity.onBackPressed() }

        btn_next.setOnClickListener{v -> next(v) }
        iv_card_x.setOnClickListener({v ->  clearCardNo(v) })
        iv_phone_x.setOnClickListener{v -> clearPhoneNo(v) }

        middle.text = "银行卡认证"
        iv_camera.setOnClickListener({
            //                activity.startNewActivity(new Intent(activity, ScanCamera.class), false);
        })

        user = activity.getUser()

        cardNoKeyboard = SecurityKeyboardBuilder(activity).build()
        cardNoKeyboard.setType(SecurityKeyboard.Type.Standard)
        cardNoKeyboard.setRedKey("下一项", {
            cardNoKeyboard.dismiss()
            et_phone_no.requestFocus()
            phoneNoKeyboard.show()
        })
        cardNoKeyboard.setKeyboardListener(object : KeyboardListener(cardNoKeyboard, et_card_no) {
            override fun show() {
                super.show()
                phoneNoKeyboard.dismiss()
            }
        })
        cardNoKeyboard.show()

        phoneNoKeyboard = SecurityKeyboardBuilder(activity).build()
        phoneNoKeyboard.setType(SecurityKeyboard.Type.Standard)
        phoneNoKeyboard.setKeyboardListener(object : KeyboardListener(phoneNoKeyboard, et_phone_no) {
            override fun show() {
                super.show()
                cardNoKeyboard.dismiss()
            }
        })

        //设置清除按钮的显示
        et_card_no.addTextChangedListener(object : OnTextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //判断卡号已输入且手机号输入完整，显示下一步
                btn_next.isEnabled = et_card_no.text.length >= 14 && et_phone_no.text.length === 13
                //格式化
                StringUtils.formatCardNo(charSequence, et_card_no)
                //控制清除按钮的显隐
                iv_card_x.visibility = if (charSequence.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                //无内容时不显示错误提示
                if (charSequence.length == 0) {
                    //                    errorText.set("");
                    et_card_no.textSize = 14F
                    et_card_no.setTextColor(resources.getColor(R.color.xl_gray_headline))
                } else {
                    et_card_no.textSize = 16F
                    et_card_no.setTextColor(resources.getColor(R.color.xl_gray_headline))
                }
            }
        })
        et_phone_no.addTextChangedListener(object : OnTextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //判断卡号已输入且手机号输入完整，显示下一步
                btn_next.isEnabled = et_card_no.text.length >= 14 && et_phone_no.text.length === 13
                //格式化
                StringUtils.formatPhoneStr(charSequence, et_phone_no)
                //控制清除按钮的显隐
                iv_phone_x.visibility = if(charSequence.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                //无内容时不显示错误提示
                if (charSequence.isEmpty()) {
                    //                    errorText.set("");
                    et_phone_no.textSize = 14F
                    et_phone_no.setTextColor(resources.getColor(R.color.xl_gray))
                } else {
                    et_phone_no.textSize = 16F
                    et_phone_no.setTextColor(resources.getColor(R.color.xl_gray_headline))
                }
            }
        })
    }

    fun clearPhoneNo(view: View) {
        iv_phone_x.visibility = View.INVISIBLE
        et_phone_no.setText("")
    }

    fun clearCardNo(view: View) {
        iv_card_x.visibility = View.INVISIBLE
        et_card_no.setText("")
    }

    fun next(view: View) {
        if (ViewUtils.isFastDoubleClick(view)) return
        val number = StringUtils.cleanStrSpace(et_card_no.text.toString())
        val phone = StringUtils.cleanStrSpace(et_phone_no.text.toString())
        val data = activity.getConstantDataByKey(Constants.ZZ_SJH) ?: return
            //隐藏键盘
        cardNoKeyboard.dismiss()
        phoneNoKeyboard.dismiss()
        if (!phone.matches(data.`val`.toRegex())) {
            ll_error.visibility = View.VISIBLE
            tv_error.text = data.desc
            return
        }
        if (phone == tempPhone) {
            viewPagerWithNoScroll.next()
            return
        }
        tempPhone = phone
        HttpMethods.instance
                .preValidCard(user.token!!, user.name!!, user.idCard!!, number, phone,
                        object : ProgressSubscriber<Model.PreValidCard>(true) {
                            override fun onNext(preValidCard: Model.PreValidCard) {
                                //验证通过 获得vticket
                                Toast.makeText(activity, "短信验证码已发送", Toast.LENGTH_SHORT).show()
                                BankAuthStepTwoFragment.getInstan().setData(number, phone, preValidCard.vticket!!)
                                viewPagerWithNoScroll.next()
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
        SecurityKeyboard.destroy(cardNoKeyboard, phoneNoKeyboard)
    }
}
