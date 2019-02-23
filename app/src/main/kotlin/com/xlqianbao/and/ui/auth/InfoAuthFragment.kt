package com.xlqianbao.and.ui.auth

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
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
import kotlinx.android.synthetic.main.fragment_info_auth.*
import kotlinx.android.synthetic.main.include_title_bar.*
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by gao on 2017/9/20.
 */
class InfoAuthFragment:BaseFragment(),View.OnClickListener{

    private var allTypeList: Model.AllTypesList? = null
    private lateinit var names: MutableList<String>
    private var regionsMap: HashMap<String, String>? = null
    private var prov: String? = null
    private var city:String? = null
    private var dist:String? = null
    private var addr:String? = null
    private var time:String? = null
    private lateinit var mRxSubSticky: Disposable
    private var imm: InputMethodManager? = null

    companion object {
        var instance: InfoAuthFragment? = null

        fun getInstance(list: Model.AllTypesList?): InfoAuthFragment {
            if (instance == null)
                instance = InfoAuthFragment()
            if (list != null)
                instance?.allTypeList = list
            return instance as InfoAuthFragment
        }
    }


    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_info_auth,container,false)
    }

    override fun initData() {
        names = ArrayList<String>()
        regionsMap = HashMap<String, String>()
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        middle.setText("个人信息")
        //设置点击事件
        tv_city.setOnClickListener(this)
        tv_live_time.setOnClickListener(this)
        tv_work_info.setOnClickListener(this)
        tv_urgent_contact.setOnClickListener(this)
        btn_submit.setOnClickListener(this)
        //光标不闪
        et_address.isCursorVisible = false
        et_address.setOnClickListener({ et_address.isCursorVisible = true })
        et_address.addTextChangedListener(object : OnTextChangedListener() {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                dialogSelected()
            }
        })

        HttpMethods.instance
                .queryLiveInfo(activity.getToken()!!, object : ProgressSubscriber<Model.QueryLiveInfo>(true) {
                    override fun onNext(queryLiveInfo: Model.QueryLiveInfo) {
                        if (queryLiveInfo != null) {
                            prov = queryLiveInfo!!.prov
                            city = queryLiveInfo!!.city
                            dist = queryLiveInfo!!.dist
                            addr = queryLiveInfo!!.addr
                            time = queryLiveInfo!!.time

                            tv_city.setText("$prov-$city-$dist")
                            et_address.setText(addr)
                            tv_live_time.setText(time)
                            iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
                            btn_submit.isEnabled = true
                        }
                    }
                })
        if ("1" == activity.getUser().WorkInfoAF)
            tv_work_info.setText("已填写")
        if ("1" == activity.getUser().ContactInfoAF)
            tv_urgent_contact.setText("已填写")
    }

    override fun dialogSelected() {
        //校验是否填写完整
        addr = et_address.text.toString().trim()
        if (checkCity(tv_city.text.toString()) &&
                !TextUtils.isEmpty(tv_live_time.text) &&
                !TextUtils.isEmpty(addr) &&
                !TextUtils.isEmpty(tv_work_info.text) &&
                !TextUtils.isEmpty(tv_urgent_contact.text)) {
            iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
            btn_submit.isEnabled = true
        } else {
            iv_shadow.setBackgroundResource(R.drawable.pic_bottom_shadow)
            btn_submit.isEnabled = false
        }
    }

    /**
     * 判断字符串中 - 出现两次 即城市选择完整
     *
     * @param s
     * @return
     */
    private fun checkCity(s: String): Boolean {
        val chars = s.toCharArray()
        val count = chars.count { it == '-' }
        return count == 2
    }

    override fun onClick(v: View) {
        names.clear()
        when(v.id){
            R.id.tv_city ->{
                startNewActivity(Intent(activity, ViewPagerActivity::class.java)
                        .putExtra(ViewPagerActivity.VP_ACT_TYPE, ViewPagerActivity.CHOOSE_ADDR), false)
                mRxSubSticky = RxBus.default
                        .toObservable(Model.ChooseAddrMessage::class.java)
                        .subscribe({ tv_city.setText(it.addr)
                            prov = it.prov
                            city = it.city
                            dist = it.dist
                            dialogSelected()
                            RxSubscriptions.remove(mRxSubSticky) })
                RxSubscriptions.add(mRxSubSticky)
            }
            R.id.tv_live_time ->{
                //隐藏键盘
                if (imm != null)
                    imm!!.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
                for (type in allTypeList?.live_time!!) {
                    names.add(type.name!!)
                }
                showDialog(names, object : ClickCallBack {
                    override fun click(s: String) {
                        time = s
                        tv_live_time.setText(s)
                    }
                })
            }
            R.id.tv_work_info ->{
                HttpMethods.instance
                        .types("profession,monthIn", object : ProgressSubscriber<Model.AllTypesList>(true) {
                            override fun onNext(workTypes: Model.AllTypesList) {
                                //把城市信息放进来
                                workTypes.city = allTypeList!!.city
                                ContactAndWorkInfoFragment.getInstan().setData(ContactAndWorkInfoFragment.TYPE_WORK, workTypes)
                                viewPagerWithNoScroll.next()
                            }
                        })
            }
            R.id.tv_urgent_contact -> {
                HttpMethods.instance
                        .types("qinshu,shehui", object : ProgressSubscriber<Model.AllTypesList>(true) {
                            override fun onNext(contactTypes: Model.AllTypesList) {
                                //把城市信息放进来
                                contactTypes.city = allTypeList!!.city
                                ContactAndWorkInfoFragment.getInstan().setData(ContactAndWorkInfoFragment.TYPE_CONTACT, contactTypes)
                                viewPagerWithNoScroll.next()
                            }
                        })
            }
            R.id.btn_submit ->{
                //提交验证
                submit()
            }

        }
    }

    private fun submit() {
        HttpMethods.instance
                .setLiveInfo(activity.getToken()!!, prov!!, city!!, dist!!, addr!!, time!!, object : ProgressSubscriber<String>(true) {
                    override fun onNext(code: String) {
                        Toast.makeText(activity, "保存成功", Toast.LENGTH_SHORT).show()
                        //刷新接口
                        activity.getUserInfo()
                        activity.onBackPressed()
                    }
                })
    }

    fun setWorkInfo() {
        //工作信息已设置的回调
        tv_work_info.setText("已填写")
        dialogSelected()
    }

    fun setContactsInfo() {
        //联系人信息已设置的回调
        tv_urgent_contact.setText("已填写")
        dialogSelected()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            //隐藏键盘
            if (imm != null)
                imm!!.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        }
    }
}