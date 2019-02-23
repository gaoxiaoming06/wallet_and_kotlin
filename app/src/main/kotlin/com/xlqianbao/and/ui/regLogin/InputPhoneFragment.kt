package com.xlqianbao.and.ui.regLogin

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.twotiger.library.utils.LogUtil
import com.twotiger.library.utils.NetWorkUtil
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
import com.xlqianbao.and.http.subscribers.ProgressDialogHandler
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.utils.AuthUtils
import com.xlqianbao.and.utils.rx.RxTask
import kotlinx.android.synthetic.main.fragment_input_phone.*
import kotlinx.android.synthetic.main.include_title_bar.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Liz on 2017/9/4.
 * desc:注册登录 输入手机号页面
 */

class InputPhoneFragment : BaseFragment(), OnClickListener {
    var tempPhoneNumber: String? = null
    //生成的图形验证码
    private var code: String? = null

    private var keyboard: SecurityKeyboard? = null
    private var keyboard2: SecurityKeyboard? = null
    //判断当前页面是否对用户可见
    private var mIsVisibleToUser: Boolean = false
    //loading
    private var mProgressDialogHandler: ProgressDialogHandler? = null

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_input_phone, null)
    }

    override fun initData() {
        mProgressDialogHandler = ProgressDialogHandler(activity, true)
        //titleBar
        left.text = ""
        left.setOnClickListener(this)
        showClear.setOnClickListener(this)
        iv_code.setOnClickListener(this)
        ll_root.setOnClickListener(this)
        btn_next_step.setOnClickListener(this)
        var drawable: Drawable = resources.getDrawable(R.drawable.icon_close)
        drawable.bounds = Rect(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        left.setCompoundDrawables(drawable, null, null, null)
        title_bg.setBackgroundResource(R.drawable.shape_left_right_radius)
        divider.visibility = GONE

        //适配部分机型的光标闪烁，但是键盘不需要弹出
        activity.disableShowSoftInput(et_phone, et_code)
        keyboard = SecurityKeyboardBuilder(activity).build()
        keyboard!!.setType(SecurityKeyboard.Type.Standard)
        keyboard!!.setKeyboardListener(object : KeyboardListener(keyboard, et_phone) {
            override fun show() {
                super.show()
                keyboard2!!.dismiss()
                if (ll_code.visibility == VISIBLE) {
                    keyboard!!.setRedKey("下一项", OnClickListener {
                        if (check(false, false)) {
                            keyboard!!.dismiss()
                            et_code.requestFocus()
                            keyboard2!!.show()
                        }
                    })
                } else {
                    keyboard!!.setRedKey("输入完成", OnClickListener {
                        if (check(true, false))
                            keyboard!!.dismiss()
                    })
                }
            }
        })
        //页面可见才弹键盘
        if (mIsVisibleToUser)
            keyboard!!.show()

        keyboard2 = SecurityKeyboardBuilder(activity).build()
        keyboard2!!.setType(SecurityKeyboard.Type.Standard)
        keyboard2!!.setTitle("请输入图形验证码")
        keyboard2!!.setRedKey("输入完成", {
            if (check(true, false))
                keyboard2!!.dismiss()
        })
        keyboard2!!.setKeyboardListener(object : KeyboardListener(keyboard2, et_code) {
            override fun show() {
                super.show()
                keyboard!!.dismiss()
            }
        }.needZeroBeginning())

        //文本框内容变化的监听
        et_phone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //手机号344格式
                StringUtils.formatPhoneStr(charSequence, et_phone)
                //不为空时显示清除按钮
                showClear.visibility = if (charSequence.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                //无内容时不显示错误提示图标
                if (charSequence.isEmpty()) {
                    error_text.text = ""
                    error_text.visibility = INVISIBLE
                    et_phone.textSize = 14f
                    et_phone.setTextColor(resources.getColor(R.color.xl_gray))
                } else {
                    et_phone.textSize = 16f
                    et_phone.setTextColor(resources.getColor(R.color.xl_gray_headline))
                }
                //手机号输入满11位才可点击下一步
                if (ll_code.visibility == VISIBLE) {
                    btn_next_step.isEnabled = (charSequence.length == 13 && et_code.text.toString().length === 4)
                } else {
                    btn_next_step.isEnabled = (charSequence.length == 13)
                }
            }
        })

        //文本框内容变化的监听
        et_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (ll_code.visibility == VISIBLE) {
                    btn_next_step.isEnabled = (charSequence.length == 4)
                }
                if (charSequence.isEmpty()) {
                    et_code.textSize = 14f
                    et_code.setTextColor(resources.getColor(R.color.xl_gray))
                } else {
                    et_code.textSize = 16f
                    et_code.setTextColor(resources.getColor(R.color.xl_gray_headline))
                }
            }
        })
    }

    /**
     * 校验
     *
     * @param allCheck 是否检查图形验证码
     * @param shakeTip 是否晃动提醒
     * @return
     */
    private fun check(allCheck: Boolean, shakeTip: Boolean): Boolean {
        val inputPhoneNumber = StringUtils.cleanStrSpace(et_phone.text.toString())
        val data = activity.getConstantDataByKey(Constants.ZZ_SJH)
        if (data == null) {
            error_text.text = "网络不佳，请稍后重试"
            error_text.visibility = VISIBLE
            return false
        }
        if (TextUtils.isEmpty(inputPhoneNumber) || !inputPhoneNumber.matches(data.`val`.toRegex())) {
            //校验不通过
            error_text.text = data.desc
            error_text.visibility = VISIBLE
            //晃动控件
            if (shakeTip)
                et_phone.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.et_shake))
            return false
        }
        if (allCheck && ll_code.visibility == VISIBLE && code != et_code.text.toString()) {
            error_text.text = "请输入正确的图形验证码"
            error_text.visibility = VISIBLE
            //晃动控件
            if (shakeTip)
                et_code.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.et_shake))
            return false
        }
        error_text.text = ""
        error_text.visibility = INVISIBLE
        return true
    }

    fun changeCode() {
        //生成验证码
        val randomStr = doSort(arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"))
        code = randomStr[0] + randomStr[1] + randomStr[2] + randomStr[3]
        iv_code.text = code
    }

    /**
     * 随机排序
     *
     * @param sArr
     * @return
     */
    private fun doSort(sArr: Array<String>): Array<String?> {
        val tempArr = arrayOfNulls<String>(sArr.size)
        val random = Random(Date().time)

        var randomIndex = -1
        for (i in 0 until tempArr.size) {
            while (tempArr[i] == null) {
                randomIndex = random.nextInt(sArr.size)
                if (sArr[randomIndex].isNotEmpty()) {
                    tempArr[i] = sArr[randomIndex]
                    sArr[randomIndex] = ""
                }
            }
        }
        return tempArr
    }

    /**
     * 清除按钮操作
     *
     * @param view
     */
    fun clear() {
        et_phone.setText("")
        error_text.text = ""
        error_text.visibility = INVISIBLE
        showClear.visibility = INVISIBLE
    }

    /**
     * 下一步按钮 校验操作
     *
     */
    private fun next() {
        if (ViewUtils.isFastDoubleClick(view)) return
        val inputPhoneNumber = StringUtils.cleanStrSpace(et_phone.text.toString())
        if (check(true, true)) {
            //手机号码校验通过
            if (inputPhoneNumber == tempPhoneNumber) {
                toRegLoginPage()
                return
            }
            if (TextUtils.isEmpty(AuthUtils.authId)) {
                // 拿不到authId
                error_text.text = "网络异常"
                error_text.visibility = VISIBLE
                return
            }
            HttpMethods.instance
                    .checkPhone(inputPhoneNumber, object : ProgressSubscriber<Model.CheckPhone>(false) {
                        public override fun onStart() {
                            super.onStart()
                            showProgress()
                        }

                        override fun onNext(phone: Model.CheckPhone) {
                            //检查是否超验证码上限
                            HttpMethods.instance
                                    .sendValidCode(phone.vticket, "sms",
                                            object : ProgressSubscriber<String>(false) {
                                                override fun onNext(s: String) {
                                                    Log.d("InputPhoneFragment", "send")
                                                    Toast.makeText(activity, "短信验证码已发送", Toast.LENGTH_SHORT).show()
                                                    tempPhoneNumber = inputPhoneNumber
                                                    //给注册登录页面设置数据
                                                    RegLoginFragment.instance.setData(Model.RegLoginMessage(phone, et_phone.text.toString()))
                                                    //跳转到注册登录页 并且下次进入显示验证码
                                                    dismissProgress()
                                                    toRegLoginPage()
                                                }

                                                override fun onError(e: Throwable) {
                                                    super.onError(e)
                                                    dismissProgress()
                                                    LogUtil.info(InputPhoneFragment::class.java, e.message)
                                                    error_text.text = NetWorkUtil.networkStatusInfo(e)
                                                    error_text.visibility = VISIBLE
                                                }
                                            })
                        }

                        override fun onError(e: Throwable) {
                            super.onError(e)
                            dismissProgress()
                            LogUtil.info(InputPhoneFragment::class.java, e.message)
                            error_text.text = NetWorkUtil.networkStatusInfo(e)
                            error_text.visibility = VISIBLE
                        }
                    })
        }
    }

    private fun showProgress() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler!!.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget()
        }
    }

    fun dismissProgress() {
        if (mProgressDialogHandler != null) {
            mProgressDialogHandler!!.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget()
            mProgressDialogHandler = null
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.left -> activity.onBackPressed()
            R.id.showClear -> clear()
            R.id.iv_code -> changeCode()
            R.id.ll_root -> hideKeyboard()
            R.id.btn_next_step -> next()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
        //从输入验证码界面回到了输入手机号页面
        if (isVisibleToUser && ll_code?.visibility == VISIBLE) {
            //设置圆角背景
            title_bg.setBackgroundResource(R.drawable.shape_left_right_radius)
            //显示输入验证码键盘
            keyboard2!!.show(650)
        }
    }

    fun toRegLoginPage() {
        //进入注册登录页
        viewPagerWithNoScroll.next()
        //去掉圆角背景 解决滑动过程中的不和谐
        title_bg.setBackgroundResource(R.color.xl_white)
        hideKeyboard()
        //1s后显示或更换验证码
        RxTask.doInUIThreadDelay(object : RxTask.UITask() {
            override fun doInUIThread() {
                //隐藏键盘
                if (ll_code.visibility != VISIBLE) {
                    //显示验证码
                    ll_code.visibility = VISIBLE
                    btn_next_step.isEnabled = false
                }
                //刷新验证码
                et_code.setText("")
                changeCode()
                iv_code.text = code
            }
        }, 1000, TimeUnit.MILLISECONDS)
    }

    /**
     * 点击空白处隐藏键盘 代替popupwindow的setOutsideTouchable
     *
     * @param view
     */
    fun hideKeyboard() {
        check(true, false)
        //隐藏键盘
        keyboard!!.dismiss()
        keyboard2!!.dismiss()
    }


    override fun onDestroy() {
        super.onDestroy()
        //释放
        SecurityKeyboard.destroy(keyboard, keyboard2)
    }
}