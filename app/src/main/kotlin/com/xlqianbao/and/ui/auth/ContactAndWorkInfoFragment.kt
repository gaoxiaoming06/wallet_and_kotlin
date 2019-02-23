package com.xlqianbao.and.ui.auth

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.twotiger.library.utils.StringUtils
import com.twotiger.library.widget.contactselector.ContactsActivity
import com.twotiger.library.widget.keyboard.KeyboardListener
import com.twotiger.library.widget.keyboard.SecurityKeyboard
import com.twotiger.library.widget.keyboard.SecurityKeyboardBuilder
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.listener.OnTextChangedListener
import com.xlqianbao.and.ui.ViewPagerActivity
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.utils.rx.RxSubscriptions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_contact_and_work_info.*
import kotlinx.android.synthetic.main.fragment_contact_info.*
import kotlinx.android.synthetic.main.fragment_contact_info.view.*
import kotlinx.android.synthetic.main.fragment_work_info.view.*
import kotlinx.android.synthetic.main.include_title_bar.*
import java.util.*

/**
 * Created by gao on 2017/9/21.
 */
class ContactAndWorkInfoFragment : BaseFragment(), View.OnClickListener {


    private var allTypeList: Model.AllTypesList? = null
    private var names: MutableList<String>? = null
    private var profession: String? = null
    private var monthIn: String? = null
    private var companyName: String? = null
    private var companyProvince: String? = null
    private var companyCity: String? = null
    private var companyDistrict: String? = null
    private var companyTelephone: String? = null
    private var rName: String? = null
    private var rPhone: String? = null
    private var sName: String? = null
    private var sPhone: String? = null
    private var type: String? = null
    private var keyboard: SecurityKeyboard? = null
    private var keyboard2: SecurityKeyboard? = null
    private var mRxSubSticky: Disposable? = null
    private var imm: InputMethodManager? = null

    companion object {

        val TYPE_WORK = "TYPE_WORK"
        val TYPE_CONTACT = "TYPE_CONTACT"
        lateinit var instance: ContactAndWorkInfoFragment

        fun getInstan(): ContactAndWorkInfoFragment {
            return instance
        }
    }

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_contact_and_work_info, container, false)
    }

    override fun initData() {
        left.setOnClickListener({ activity.onBackPressed() })
        instance = this
        names = ArrayList<String>()
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        keyboard = SecurityKeyboardBuilder(activity).build()
        keyboard!!.setType(SecurityKeyboard.Type.Standard)
        keyboard!!.setKeyboardListener(object : KeyboardListener(keyboard, et_qinshu) {
            override fun show() {
                super.show()
                keyboard2!!.dismiss()
            }

            override fun hide() {
                super.hide()
                val phone = StringUtils.cleanStrSpace(et_qinshu.text.toString())
                if (phone != null)
                    tv_error_qinshu.visibility = if (!checkPhone(phone)) View.VISIBLE else View.INVISIBLE
                iv_clear_r.visibility = View.INVISIBLE
            }
        })

        keyboard2 = SecurityKeyboardBuilder(activity).build()
        keyboard2!!.setType(SecurityKeyboard.Type.Standard)
        keyboard2!!.setKeyboardListener(object : KeyboardListener(keyboard2, et_shehui) {
            override fun show() {
                super.show()
                keyboard!!.dismiss()
            }

            override fun hide() {
                super.hide()
                val phone = StringUtils.cleanStrSpace(et_shehui.text.toString())
                if (phone != null)
                    tv_error_shehui.visibility = if (!checkPhone(phone)) View.VISIBLE else View.INVISIBLE
                iv_clear_s.visibility = View.INVISIBLE
            }
        })

    }


    fun setData(type: String, list: Model.AllTypesList) {
        allTypeList = list
        this.type = type
        when (type) {
            TYPE_WORK -> {
                //工作信息
                middle.text = "工作信息"
                work.visibility = View.VISIBLE
                contact.visibility = View.GONE
                //设置点击事件
                work.tv_zhiye.setOnClickListener(this)
                work.tv_yueshouru.setOnClickListener(this)
                work.tv_suozaidi.setOnClickListener(this)
                //禁止光标
                work.et_name.setCursorVisible(false)
                work.et_name.setOnClickListener({ work.et_name.setCursorVisible(true) })
                work.et_name.addTextChangedListener(object : OnTextChangedListener() {
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        dialogSelected()
                    }
                })
                //取数据
                HttpMethods.instance
                        .queryWorkInfo(activity.getToken()!!, object : ProgressSubscriber<Model.QueryWorkInfo>(true) {

                            override fun onNext(workInfo: Model.QueryWorkInfo) {
                                if (workInfo != null) {
                                    profession = workInfo!!.profession
                                    monthIn = workInfo!!.monthIn
                                    companyName = workInfo!!.companyName
                                    companyProvince = workInfo!!.companyProvince
                                    companyCity = workInfo!!.companyCity
                                    companyDistrict = workInfo!!.companyDistrict
                                    companyTelephone = workInfo!!.companyTelephone

                                    work.tv_zhiye.setText(profession)
                                    work.tv_yueshouru.setText(monthIn)
                                    work.et_name.setText(companyName)
                                    work.tv_suozaidi.setText("$companyProvince-$companyCity-$companyDistrict")
                                    work.et_no.setText(companyTelephone)

                                    iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
                                    btn_submit.setEnabled(true)
                                }
                            }
                        })
            }
            TYPE_CONTACT -> {
                //联系人信息
                middle.text = "紧急联系人"
                contact.visibility = View.VISIBLE
                work.visibility = View.GONE
                //设置点击事件
                contact.tv_qinshu.setOnClickListener(this)
                contact.tv_shehui.setOnClickListener(this)
                contact.iv_qinshu.setOnClickListener(this)
                contact.iv_shehui.setOnClickListener(this)
                contact.iv_clear_r.setOnClickListener({ contact.et_qinshu.setText("") })
                contact.iv_clear_s.setOnClickListener({ contact.et_shehui.setText("") })
                //光标
                contact.et_qinshu.setCursorVisible(false)
                contact.et_qinshu.setOnClickListener({ contact.et_qinshu.isCursorVisible = true })
                //取数据
                HttpMethods.instance
                        .queryContacts(activity.getToken()!!, object : ProgressSubscriber<List<Model.QueryContacts>>(true) {
                            override fun onNext(contactses: List<Model.QueryContacts>) {
                                if (contactses != null && contactses.size > 0) {
                                    for (contactse in contactses) {
                                        when (contactse.relationshipType) {
                                            "1" -> {
                                                //社会
                                                sName = contactse.relationship
                                                sPhone = contactse.phone
                                                contact.tv_shehui.setText(sName)
                                                contact.et_shehui.setText(sPhone)
                                                contact.iv_clear_s.setVisibility(View.INVISIBLE)
                                            }
                                            "2" -> {
                                                //亲属
                                                rName = contactse.relationship
                                                rPhone = contactse.phone
                                                contact.tv_qinshu.setText(rName)
                                                contact.et_qinshu.setText(rPhone)
                                                contact.iv_clear_r.setVisibility(View.INVISIBLE)
                                            }
                                        }
                                    }
                                    iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
                                    btn_submit.isEnabled = true
                                }
                            }
                        })
                contact.et_qinshu.setOnFocusChangeListener({ view, b ->
                    if (!b) {
                        val phone = StringUtils.cleanStrSpace(contact.et_qinshu.getText().toString())
                        //手机号输入不正确
                        if (phone != null)
                            contact.tv_error_qinshu.setVisibility(if (!checkPhone(phone)) View.VISIBLE else View.INVISIBLE)
                    }
                })
                contact.et_shehui.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
                    if (!b) {
                        val phone = StringUtils.cleanStrSpace(contact.et_shehui.getText().toString())
                        //手机号输入不正确
                        if (phone != null)
                            contact.tv_error_shehui.visibility = if (!checkPhone(phone)) View.VISIBLE else View.INVISIBLE
                    }
                }

                contact.et_qinshu.addTextChangedListener(object : OnTextChangedListener() {
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        //手机号344格式
                        StringUtils.formatPhoneStr(charSequence, contact.et_qinshu)
                        contact.et_qinshu.setSelection(contact.et_qinshu.getText().length)
                        checkForm()
                        contact.iv_clear_r.visibility = if (TextUtils.isEmpty(charSequence)) View.INVISIBLE else View.VISIBLE
                    }
                })
                contact.et_shehui.addTextChangedListener(object : OnTextChangedListener() {
                    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                        //手机号344格式
                        StringUtils.formatPhoneStr(charSequence, contact.et_shehui)
                        contact.et_shehui.setSelection(contact.et_shehui.getText().length)
                        checkForm()
                        contact.iv_clear_s.setVisibility(if (TextUtils.isEmpty(charSequence)) View.INVISIBLE else View.VISIBLE)
                    }
                })
            }
        }
        btn_submit.setOnClickListener(View.OnClickListener {
            //提交数据
            submit()
        })
    }

    private fun submit() {
        when (type) {
            TYPE_WORK -> {
                companyTelephone = work.et_no.getText().toString().trim()
                if (TextUtils.isEmpty(companyTelephone))
                    companyTelephone = ""
                HttpMethods.instance
                        .setWorkInfo(activity.getToken()!!, profession!!, monthIn!!, companyName!!, companyProvince!!, companyCity!!, companyDistrict!!, companyTelephone!!,
                                object : ProgressSubscriber<String>(true) {
                                    override fun onNext(code: String) {
                                        Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show()
                                        InfoAuthFragment.getInstance(null).setWorkInfo()
                                        viewPagerWithNoScroll.previous()
                                    }
                                })
            }
            TYPE_CONTACT -> HttpMethods.instance
                    .setContacts(activity.getToken()!!, rName!!, rPhone!!, sName!!, sPhone!!,
                            object : ProgressSubscriber<String>(true) {
                                override fun onNext(code: String) {
                                    Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show()
                                    InfoAuthFragment.getInstance(null).setContactsInfo()
                                    viewPagerWithNoScroll.previous()
                                }
                            })
        }
    }

    /**
     * 检查手机号格式
     *
     * @param phone
     * @return
     */
    private fun checkPhone(phone: String?): Boolean {
        val data = activity.getConstantDataByKey(Constants.ZZ_SJH)
        return data != null
                && !TextUtils.isEmpty(phone)
                && phone!!.matches(data.`val`.toRegex())
    }

    override fun dialogSelected() {
        //校验是否填写完整
        checkForm()
    }

    /**
     * 判断字符串中 - 出现两次 即城市选择完整
     *
     * @param s
     * @return
     */
    private fun checkCity(s: String): Boolean {
        var count = 0
        val chars = s.toCharArray()
        for (aChar in chars) {
            if (aChar == '-') {
                count++
            }
        }
        return count == 2
    }

    private fun checkForm() {
        when (type) {
            TYPE_WORK -> {
                companyName = work.et_name.getText().toString().trim()
                if (!TextUtils.isEmpty(companyName) &&
                        !TextUtils.isEmpty(work.tv_zhiye.getText()) &&
                        !TextUtils.isEmpty(work.tv_yueshouru.getText()) &&
                        checkCity(work.tv_suozaidi.getText().toString())) {
                    iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
                    btn_submit.setEnabled(true)
                } else {
                    iv_shadow.setBackgroundResource(R.drawable.pic_bottom_shadow)
                    btn_submit.setEnabled(false)
                }
            }
            TYPE_CONTACT -> {
                rPhone = StringUtils.cleanStrSpace(contact.et_qinshu.getText().toString())
                sPhone = StringUtils.cleanStrSpace(contact.et_shehui.getText().toString())
                if (!TextUtils.isEmpty(rPhone) && rPhone!!.length == 11) {
                    val data = activity.getConstantDataByKey(Constants.ZZ_SJH) ?: return
                    contact.tv_error_qinshu.setVisibility(if (rPhone!!.matches(data.`val`.toRegex())) View.INVISIBLE else View.VISIBLE)
                } else {
                    contact.tv_error_qinshu.setVisibility(View.INVISIBLE)
                }
                if (!TextUtils.isEmpty(sPhone) && sPhone!!.length == 11) {
                    val data = activity.getConstantDataByKey(Constants.ZZ_SJH) ?: return
                    contact.tv_error_shehui.setVisibility(if (sPhone!!.matches(data.`val`.toRegex())) View.INVISIBLE else View.VISIBLE)
                } else {
                    contact.tv_error_shehui.setVisibility(View.INVISIBLE)
                }
                if (checkPhone(rPhone) && checkPhone(sPhone) && !TextUtils.isEmpty(contact.tv_qinshu.getText()) && !TextUtils.isEmpty(contact.tv_shehui.getText())) {
                    iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
                    btn_submit.setEnabled(true)
                } else {
                    iv_shadow.setBackgroundResource(R.drawable.pic_bottom_shadow)
                    btn_submit.setEnabled(false)
                }
            }
        }
    }


    override fun onClick(view: View) {
        names!!.clear()
        when (view.id) {
            R.id.tv_zhiye -> {
                //职业
                //隐藏键盘
                hideKeyboard()
                for (type in allTypeList?.profession!!) {
                    names!!.add(type!!.name!!)
                }
                showDialog(names!!, object : BaseFragment.ClickCallBack {
                    override fun click(s: String) {
                        profession = s
                        work.tv_zhiye.setText(s)
                    }
                })
            }
            R.id.tv_yueshouru -> {
                //月收入
                //隐藏键盘
                hideKeyboard()
                for (type in allTypeList!!.monthIn!!) {
                    names!!.add(type.name!!)
                }
                showDialog(names!!, object : BaseFragment.ClickCallBack {
                    override fun click(s: String) {
                        monthIn = s
                        work.tv_yueshouru.setText(s)
                    }
                })
            }
            R.id.tv_suozaidi -> {
                //所在地
                startNewActivity(Intent(activity, ViewPagerActivity::class.java)
                        .putExtra(ViewPagerActivity.VP_ACT_TYPE, ViewPagerActivity.CHOOSE_ADDR), false)
                mRxSubSticky = RxBus.default
                        .toObservable(Model.ChooseAddrMessage::class.java)
                        .subscribe { msg ->
                            work.tv_suozaidi.setText(msg.addr)
                            companyProvince = msg.prov
                            companyCity = msg.city
                            companyDistrict = msg.dist
                            checkForm()
                            RxSubscriptions.remove(mRxSubSticky!!)
                        }
                RxSubscriptions.add(mRxSubSticky)
            }
            R.id.tv_qinshu -> {
                //亲属关系
                //隐藏键盘
                hideKeyboard()
                for (type in allTypeList!!.qinshu!!) {
                    names!!.add(type.name!!)
                }
                showDialog(names!!, object : BaseFragment.ClickCallBack {
                    override fun click(s: String) {
                        rName = s
                        contact.tv_qinshu.setText(s)
                    }
                })
            }
            R.id.tv_shehui -> {
                //社会关系
                //隐藏键盘
                hideKeyboard()
                for (type in allTypeList!!.shehui!!) {
                    names!!.add(type.name!!)
                }
                showDialog(names!!, object : BaseFragment.ClickCallBack {
                    override fun click(s: String) {
                        sName = s
                        contact.tv_shehui.setText(s)
                    }
                })
            }
            R.id.iv_qinshu -> {
                activity.startNewActivity(Intent(activity, ContactsActivity::class.java), false)
                mRxSubSticky = RxBus.default
                        .toObservable(String::class.java)
                        .subscribe { s ->
                            if (s.startsWith("number:"))
                                contact.et_qinshu.setText(s.substring(7, s.length))
                            RxSubscriptions.remove(mRxSubSticky!!)
                        }
                RxSubscriptions.add(mRxSubSticky)
            }
            R.id.iv_shehui -> {
                activity.startNewActivity(Intent(activity, ContactsActivity::class.java), false)
                mRxSubSticky = RxBus.default
                        .toObservable(String::class.java)
                        .subscribe { t ->
                            if (t.startsWith("number:"))
                                contact.et_shehui.setText(t.substring(7, t.length))
                            RxSubscriptions.remove(mRxSubSticky!!)
                        }
                RxSubscriptions.add(mRxSubSticky)
            }
        }
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            //隐藏时关闭键盘
            hideKeyboard()
        }
    }

    private fun hideKeyboard() {
        if (imm != null)
            imm!!.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        if (keyboard != null && keyboard!!.isShowing)
            keyboard!!.dismiss()
        if (keyboard2 != null && keyboard2!!.isShowing)
            keyboard2!!.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        SecurityKeyboard.destroy(keyboard, keyboard2)
    }

}