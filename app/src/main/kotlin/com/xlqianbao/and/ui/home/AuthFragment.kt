package com.xlqianbao.and.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.twotiger.library.utils.ViewUtils
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.Model.ConstantData
import com.xlqianbao.and.Model.User
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.ext.toast
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.ViewPagerActivity
import com.xlqianbao.and.ui.WebViewPageActivity
import com.xlqianbao.and.ui.auth.ContactAuthActivity
import com.xlqianbao.and.ui.auth.id.IdAuthActivity
import com.xlqianbao.and.ui.personal.BankCardActivity
import kotlinx.android.synthetic.main.include_auth_footer.*
import kotlinx.android.synthetic.main.include_auth_header.*


/**
 * Created by Makise on 2017/8/31.
 */
class AuthFragment : BaseFragment(), View.OnClickListener {

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_auth, null)
    }

    override fun initData() {
        rl_toIdAuth.setOnClickListener(this)
        rl_toPhoneAuth.setOnClickListener(this)
        rl_toInfoAuth.setOnClickListener(this)
        rl_toBankCardAuth.setOnClickListener(this)
        ll_toTxlAF.setOnClickListener(this)
        ll_toZhimaAF.setOnClickListener(this)
        checkAFStatus()
    }

    override fun onClick(view: View) {
        if (ViewUtils.isFastDoubleClick(view)) return
        if (activity.getToken() == null) {
            //去登陆
            activity.toLogin()
            return
        }
        val data: ConstantData?
        when (view.id) {
            R.id.rl_toIdAuth -> {
                //身份认证
                activity.startNewActivity(Intent(activity, IdAuthActivity::class.java), false)
            }
            R.id.rl_toPhoneAuth -> {
                //手机认证
                if ("0".equals(activity.getUser().IDAF)) {
                    toast("请先完成身份认证", Toast.LENGTH_SHORT)
                    return
                }
                data = activity.getConstantDataByKey(Constants.DZ_PHONE_AF)
                if (data == null) return
                startNewActivity(Intent(activity, WebViewPageActivity::class.java)
                        .putExtra("url", data.`val`), false)
            }
            R.id.rl_toInfoAuth -> {
                //个人信息认证
                toInfoAuth()
            }
            R.id.rl_toBankCardAuth -> {
                //银行卡认证
                if ("0".equals(activity.getUser().IDAF)) {
                    toast("请先完成身份认证", Toast.LENGTH_SHORT)
                    return
                }
                //已登录 检查绑卡状态 未绑卡去绑卡 已绑去卡展示页
                activity.checkBindCardStatus(BankCardActivity::class.java)
            }
            R.id.ll_toTxlAF -> {
                //通讯录认证
                startNewActivity(Intent(activity, ContactAuthActivity::class.java), false)
            }
            R.id.ll_toZhimaAF -> {
                //芝麻信用认证
                data = activity.getConstantDataByKey(Constants.DZ_AUTH_ZM)
                if (data == null) return
                startNewActivity(Intent(activity, WebViewPageActivity::class.java)
                        .putExtra("url", data.`val`), false)
            }
        }
    }

    private fun toInfoAuth() {
        if (activity.getToken() == null) {
            activity.toLogin()
            return
        }
        HttpMethods.instance
                .types("live_time", object : ProgressSubscriber<Model.AllTypesList>(true) {
                    override fun onNext(allTypesList: Model.AllTypesList) {
                        HttpMethods.instance
                                .region("1", object : ProgressSubscriber<List<Model.Regions>>(true) {
                                    override fun onNext(regionses: List<Model.Regions>) {
                                        allTypesList.city = regionses
                                        activity.startNewActivity(Intent(activity, ViewPagerActivity::class.java)
                                                .putExtra(ViewPagerActivity.VP_ACT_TYPE, ViewPagerActivity.INFO_AUTH)
                                                .putExtra(ViewPagerActivity.EXTRA_DATA, allTypesList), false)
                                    }
                                })
                    }
                })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            checkAFStatus()
    }

    private fun checkAFStatus() {
        if (activity.getToken() == null) {
            //未登录全部显示蓝色
            iv_sfz.setImageResource(R.drawable.rz_icon_sfz)
            iv_phone.setImageResource(R.drawable.rz_icon_mob)
            iv_info.setImageResource(R.drawable.rz_icon_my)
            iv_card.setImageResource(R.drawable.rz_icon_card)
            iv_txl.setImageResource(R.drawable.rz_list_moblist)
            iv_zhima.setImageResource(R.drawable.rz_list_zhima)
            return
        } else {
            //登录了 先根据本地user对象显示 再去检查认证状态
            setIconStatus(activity.getUser())
        }
        //检查账户认证状态
        HttpMethods.instance
                .userInfo(activity.getToken(), object : ProgressSubscriber<User>(true) {
                    override fun onNext(user: User) {
                        activity.saveUser(user)
                        setIconStatus(user)
                    }
                })
    }

    private fun setIconStatus(user: Model.User) {
        iv_sfz.setImageResource(if ("0" == user.IDAF) R.drawable.rz_icon_sfz_gray else R.drawable.rz_icon_sfz)
        iv_phone.setImageResource(if ("0" == user.PhoneAF) R.drawable.rz_icon_mob_gray else R.drawable.rz_icon_mob)
        iv_info.setImageResource(if ("0" == user.LiveInfoAF || "0" == user.WorkInfoAF || "0" == user.ContactInfoAF) R.drawable.rz_icon_my_gray else R.drawable.rz_icon_my)
        iv_card.setImageResource(if ("0" == user.BCAF) R.drawable.rz_icon_card_gray else R.drawable.rz_icon_card)
        iv_txl.setImageResource(if ("0" == user.TelBookAF) R.drawable.rz_list_moblist_gray else R.drawable.rz_list_moblist)
        iv_zhima.setImageResource(if ("0" == user.ZmxyAF) R.drawable.rz_list_zhima_gray else R.drawable.rz_list_zhima)
    }
}