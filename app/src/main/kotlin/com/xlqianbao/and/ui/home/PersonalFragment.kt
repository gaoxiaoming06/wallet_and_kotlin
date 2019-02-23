package com.xlqianbao.and.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.twotiger.library.utils.ArithUtils
import com.twotiger.library.utils.ViewUtils
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.Model.*
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.ext.toast
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.ResultException
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.personal.MyNoticesActivity
import com.xlqianbao.and.ui.personal.OrderRepayActivity
import com.xlqianbao.and.ui.personal.SettingActivity
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.wxapi.WXEntryActivity
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.include_personal_center_header_login.*
import kotlinx.android.synthetic.main.include_personal_center_header_nologin.*
import java.io.ByteArrayOutputStream


/**
 * Created by Makise on 2017/9/1.
 */
class PersonalFragment : BaseFragment(), View.OnClickListener {
    private var user: User? = null
    //订单id
    private var orderId: String? = null
    private var curStatus: String? = null
    //正在请求接口
    private var canGetData = false
    private var canCreateOrder = false
    //页面在前台时 才会显示分享
    private var canShowShare = false

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_personal, null)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        //点击事件
        toLogin.setOnClickListener(this)
        btn.setOnClickListener(this)
        ll_toBankCard.setOnClickListener(this)
        ll_toBorrowRecord.setOnClickListener(this)
        ll_toInviteFriend.setOnClickListener(this)
        ll_toMyNotices.setOnClickListener(this)
        ll_toSetting.setOnClickListener(this)

        activity.setCustomFont(tv_amount)

        user = activity.getUser()
        canGetData = true
        canCreateOrder = true
        curStatus = ""
        checkLoginStatus()
        //公众号常量
        var data = activity.getConstantDataByKey(Constants.CL_WXFWH)
        if (data != null) {
            tv_wxfwh.text = "有问题反馈微信公众号：" + data.`val`
        }
    }

    /**
     * 判断登录状态 填充页面数据
     */
    private fun checkLoginStatus() {
        //判断登陆状态
        user = activity.getUser()
        if (user?.token != null) {
            //已登录
            setPageStatus(true)
            getBorrowInfo()
            if (user?.realName!!) {
                //已实名
                tv_nickName.text = user?.name
            } else {
                //未实名
                activity.setCustomFont(tv_nickName)
                tv_nickName.text = user?.phone
            }
        } else {
            //未登录
            setPageStatus(false)
            //状态重置
            curStatus = ""
            //恢复默认灰条
            tv_over_day.text = ""
            tv_over_day.alpha = 1f
            tv_over_day.setBackgroundColor(resources.getColor(R.color.xl_gray_background))
            //恢复默认ui
            tv_amount.text = "0"
            btn.text = "立即借款"
            btn.setBackgroundResource(R.drawable.selector_btn_personal_blue)
            //隐藏news图标
            iv_news.visibility = View.GONE
        }
    }

    private fun setPageStatus(bool: Boolean) {
        ll_toBankCard.visibility = if (bool) View.VISIBLE else View.GONE
        divider_1.visibility = if (bool) View.VISIBLE else View.GONE
        ll_toMyNotices.visibility = if (bool) View.VISIBLE else View.GONE
        divider_2.visibility = if (bool) View.VISIBLE else View.GONE
        header_login.visibility = if (bool) View.VISIBLE else View.GONE
        header_nologin.visibility = if (bool) View.GONE else View.VISIBLE
    }

    /**
     * 获取借款信息
     */
    private fun getBorrowInfo() {
        if (!canGetData) return
        HttpMethods.instance
                .hasNotice(activity.getToken(), object : ProgressSubscriber<Model.HasNotice>(false) {
                    override fun onNext(notice: Model.HasNotice) {
                        //检查是否有未读消息
                        iv_news.visibility = if (1 == notice.hasUnRead) View.VISIBLE else View.GONE
                    }
                })
        HttpMethods.instance
                .borrowInfo(activity.getToken(), object : ProgressSubscriber<BorrowInfo>(true) {
                    public override fun onStart() {
                        super.onStart()
                        canGetData = false
                    }

                    override fun onNext(borrowInfo: Model.BorrowInfo) {
                        canGetData = true
                        if (borrowInfo != null) {
                            //订单id
                            orderId = borrowInfo.orderId
                            //设置待还金额
                            tv_amount.text = ArithUtils.coverMoneyCommaMaybeSmall(borrowInfo.amount)
                            curStatus = borrowInfo.status
                            when (borrowInfo.status) {
                                "WAIT_LOAN",
                                    //放款中
                                "LOAN_ING" -> {
                                    //借款中
                                    btn.text = "立即还款"
                                    if ("0" == borrowInfo.overdueFlag) {
                                        //没有逾期
                                        btn.setBackgroundResource(R.drawable.selector_btn_personal_green)
                                    } else {
                                        //有逾期
                                        btn.setBackgroundResource(R.drawable.selector_btn_personal_red)
                                    }
                                }
                                else ->
                                    //默认蓝色背景
                                    btn.setBackgroundResource(R.drawable.selector_btn_personal_blue)
                            }
                            //逾期提醒
                            if (!TextUtils.isEmpty(borrowInfo.tips)) {
                                tv_over_day.text = borrowInfo.tips
                                tv_over_day.alpha = 0.7f
                                tv_over_day.setBackgroundColor(if ("1" == borrowInfo.overdueFlag) resources.getColor(R.color.xl_red) else resources.getColor(R.color.xl_green))
                            }
                        } else {
                            //状态重置
                            curStatus = ""
                            //恢复默认灰条
                            tv_over_day.text = ""
                            tv_over_day.alpha = 1f
                            tv_over_day.setBackgroundColor(resources.getColor(R.color.xl_gray_background))
                            //恢复默认ui
                            tv_amount.text = "0"
                            btn.text = "立即借款"
                            btn.setBackgroundResource(R.drawable.selector_btn_personal_blue)
                            //隐藏未读消息
                            iv_news.visibility = View.GONE
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        canGetData = true
                    }
                })
    }

    override fun onClick(view: View) {
        if (ViewUtils.isFastDoubleClick(view)) return
        when (view.id) {
            R.id.toLogin -> activity.toLogin()
            R.id.btn -> {
                //创建还款订单
                user = activity.getUser()
                if ("0".equals(user?.IDAF) || "0".equals(user?.LiveInfoAF) || "0".equals(user?.WorkInfoAF) || "0".equals(user?.ContactInfoAF) || "0".equals(user?.BCAF)) {
                    //未认证 先去认证
                    toast("请先完成必备认证", Toast.LENGTH_SHORT)
                    RxBus.default.postSticky(Model.HomeTabMessage(R.id.btn_2))
                    return
                }
                when (curStatus) {
                    "WAIT_LOAN", "LOAN_ING", "OVERDUE" -> createOrderRepay()
                    else -> RxBus.default.postSticky(Model.HomeTabMessage(R.id.btn_1))
                }
            }
            R.id.ll_toBankCard -> {
                //银行卡
                if ("0" == activity.getUser().IDAF) {
                    //未实名 先去实名
                    RxBus.default.postSticky(HomeTabMessage(R.id.btn_2))
                    return
                }
//                activity.checkBindCardStatus(BankCardActivity::class.java)
            }
//            R.id.ll_toBorrowRecord -> activity.checkLoginStatus(BorrowRecordActivity::class.java)
            R.id.ll_toInviteFriend -> {
                //邀请好友
                if (activity.getUser().token == null) {
                    activity.toLogin()
                    return
                }
                inviteFriend()
            }
            R.id.ll_toMyNotices -> {
                //我的消息
                if (activity.getUser().token == null) {
                    activity.toLogin()
                    return
                }
                activity.startNewActivity(Intent(activity, MyNoticesActivity::class.java), false)
            }
            R.id.ll_toSetting -> activity.startNewActivity(Intent(activity, SettingActivity::class.java), false)
        }
    }

    private fun inviteFriend() {
        HttpMethods.instance
                .share("", activity.getToken(), "wode",
                        object : ProgressSubscriber<ShareData>(true) {
                            override fun onNext(shareData: ShareData) {
                                canShowShare = true
                                ll_toInviteFriend.tag = object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: GlideAnimation<in Bitmap>) {
                                        //下载成功
                                        if (canShowShare) {
                                            canShowShare = false
                                            val stream = ByteArrayOutputStream()
                                            resource.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                            val byteArray = stream.toByteArray()
                                            activity.startNewActivity(Intent(activity, WXEntryActivity::class.java)
                                                    .putExtra(WXEntryActivity.SHARE_DATA, shareData.setBitmap(byteArray)),
                                                    R.anim.fade_in_anim, R.anim.silent_anim, false)
                                        }
                                    }

                                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                                        super.onLoadFailed(e, errorDrawable)
                                        //失败
                                        if (canShowShare) {
                                            canShowShare = false
                                            activity.startNewActivity(Intent(activity, WXEntryActivity::class.java)
                                                    .putExtra(WXEntryActivity.SHARE_DATA, shareData),
                                                    R.anim.fade_in_anim, R.anim.silent_anim, false)
                                        }
                                    }
                                }
                                Glide.with(this@PersonalFragment)
                                        .load(shareData.sharePic)
                                        .asBitmap()
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .into(ll_toInviteFriend.tag as SimpleTarget<Bitmap>)
                            }
                        })
    }

    private fun createOrderRepay() {
        if (!canCreateOrder) return
        if (TextUtils.isEmpty(orderId)) return
        HttpMethods.instance
                .orderRepay(activity.getToken(), orderId, object : ProgressSubscriber<Model.OrderRepay>(true) {
                    public override fun onStart() {
                        super.onStart()
                        canCreateOrder = false
                    }

                    override fun onNext(orderRepay: OrderRepay) {
                        canCreateOrder = true
                        if (orderRepay == null) return
                        //创建成功 验证码已发送 弹出
                        Toast.makeText(activity, "短信验证码已发送", Toast.LENGTH_SHORT).show()
                        startNewActivity(Intent(activity, OrderRepayActivity::class.java)
                                .putExtra(OrderRepayActivity.EXTRA_DATA, orderRepay),
                                R.anim.fade_in_anim, R.anim.silent_anim, false)
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        canCreateOrder = true
                        if (e is ResultException.RepayStatusLimitException) {
                            //已还款 刷新状态
                            getBorrowInfo()
                        }
                    }
                })
    }

    override fun onResume() {
        super.onResume()
        checkLoginStatus()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            checkLoginStatus()
        }
    }
}