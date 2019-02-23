package com.xlqianbao.and.ui.home

import android.content.DialogInterface
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.twotiger.library.ui.looplayout.ViewpagerWithIndicator
import com.twotiger.library.utils.ArithUtils
import com.twotiger.library.utils.ViewUtils
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.WebViewPageActivity
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.view.CustomDialog
import com.xlqianbao.and.view.RulerDays
import com.xlqianbao.and.view.RulerMoney
import kotlinx.android.synthetic.main.fragment_loan.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*


/**
 * Created by gao on 2017/8/30.
 */
class LoanFragment : BaseFragment() {

    //费率
    private var dayRate: BigDecimal? = null
    //当前选择的金额
    private lateinit var curAmount: BigDecimal
    //当前选择的天数
    private var curDays: BigDecimal? = null
    //格式化
    private lateinit var df: DecimalFormat
    private lateinit var moneyList: ArrayList<String>
    private var dayList: ArrayList<String>? = null

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.fragment_loan, container, false)
        return view
    }

    override fun initData() {
        activity.setCustomFont(
                red_money,
                tv_amount,
                tv_fee,
                tv_days
        )
        //初始化
        df = DecimalFormat("0.00")
        curAmount = BigDecimal("500")
        curDays = BigDecimal("7")
        moneyList = ArrayList<String>()
        dayList = ArrayList<String>()
        //获取逾期提醒
        getBorrowInfo()
        //获取banner数据
        getBanner()
        //获取产品配置数据
        getProConf()

        btn.setOnClickListener(View.OnClickListener { view ->
            //借款申请
            if (ViewUtils.isFastDoubleClick(view)) return@OnClickListener
            apply()
        })
    }

    /**
     * 提交借款申请
     */
    private fun apply() {
        if (activity.getToken() == null) {
            //去登录
            activity.toLogin()
            return
        }
        if (dayRate == null) {
            getProConf()
            return
        }
        val user = activity.getUser()
        if ("0" == user.IDAF || "0" == user.LiveInfoAF || "0" == user.WorkInfoAF || "0" == user.ContactInfoAF || "0" == user.BCAF) {
            //未认证 先去认证
            Toast.makeText(activity, "请先完成必备认证", Toast.LENGTH_SHORT).show()
            RxBus.default.postSticky(Model.HomeTabMessage(R.id.btn_2))
            return
        }
        HttpMethods.instance
                .apply(activity.getToken(), df.format(curAmount), curDays?.toInt()!!,
                        object : ProgressSubscriber<String>(true) {
                            override fun onNext(code: String) {
                                CustomDialog.Builder(activity)
                                        .setTitle("借款审核中")
                                        .setMessage("系统正在对您的借款进行审核，通过后将直接放款至您绑定的银行卡中")
                                        .setNegativeButton("我知道了", DialogInterface.OnClickListener { dialogInterface, _ ->
                                            dialogInterface.dismiss()
                                        }).show(R.layout.dialog_home_layout)
                            }
                        })
    }

    fun getBorrowInfo() {
        if (activity.getToken() == null) {
            tv_over_day.visibility = View.GONE
            return
        }
        HttpMethods.instance
                .borrowInfo(activity.getToken()!!, object : ProgressSubscriber<Model.BorrowInfo>(false) {
                    override fun onNext(borrowInfo: Model.BorrowInfo) {
                        if (borrowInfo != null) {
                            if (!TextUtils.isEmpty(borrowInfo!!.tips)) {
                                tv_over_day.visibility = View.VISIBLE
                                tv_over_day.text = borrowInfo!!.tips
                                tv_over_day
                                        .setBackgroundColor(if ("1" == borrowInfo!!.overdueFlag) resources.getColor(R.color.xl_red) else resources.getColor(R.color.xl_green))
                            } else {
                                tv_over_day.visibility = View.GONE
                            }
                        } else {
                            tv_over_day.visibility = View.GONE
                        }
                    }
                })
    }

    /**
     * 获取banner数据
     */
    private fun getBanner() {
        HttpMethods.instance
                .banner("BANNER", object : ProgressSubscriber<List<Model.Banner>>(false) {
                    override fun onNext(banners: List<Model.Banner>) {
                        if (banners != null) {
                            if (banners.size > 0) {
                                vp_wi.setData(banners, object : ViewpagerWithIndicator.OnItemClickListener {
                                    override fun loadItemContent(position: Int, view: ImageView) {
                                        val ban = banners[position]
//                                        Glide.with(activity)
//                                                .load(Constants.HOST + ban.picUrl)
//                                                .apply(RequestOptions()
//                                                        .error(R.drawable.banner_default)
//                                                        .placeholder(R.drawable.banner_default))
//                                                .into(view)
                                        Glide.with(activity)
                                                .load(Constants.HOST + ban.picUrl)
                                                .error(R.drawable.banner_default)
                                                .placeholder(R.drawable.banner_default)
                                                .into(view)
                                    }

                                    override fun onItemClick(position: Int) {
                                        val ban = banners[position]
                                        if (!TextUtils.isEmpty(ban.url) && ban.url.startsWith("http")) {
//                                            TCAgent.onEvent(activity, "首页" + if (activity.getToken() != null) "（注册前）" else "（注册后）", "Banner")
                                            val intent = Intent(activity, WebViewPageActivity::class.java)
                                            intent.putExtra("url", ban.url)
                                            intent.putExtra("title", ban.name)
                                            activity.startNewActivity(intent, false)
                                        }
                                    }
                                })
                            }
                        }
                    }

                })
    }

    /**
     * 获取产品配置 金额、天数
     */
    private fun getProConf() {
        HttpMethods.instance
                .proConf(activity.getToken(), object : ProgressSubscriber<Model.ProConf>(false) {
                    override fun onNext(proConf: Model.ProConf) {
                        //拿到费率
                        dayRate = proConf.rate

                        //借款金额数据
                        if (moneyList.size <= 0) {
                            var i = proConf.min
                            while (i <= proConf.max) {
                                moneyList.add(i.toString())
                                i += 100
                            }
                            ruler_money.setScrollingListener(object : RulerMoney.OnWheelScrollListener {
                                override fun onChanged(wheel: RulerMoney, oldValue: Int, newValue: Int) {
                                    red_money.text = moneyList.get(newValue)
                                    //记录当前选择金额
                                    curAmount = BigDecimal(moneyList.get(newValue))
                                    if (curAmount!!.compareTo(BigDecimal(proConf.valid)) == 1) {
                                        //大于用户可借额度显示tips
                                        tv_tips.text = proConf.tips
                                        tv_tips.visibility = View.VISIBLE
                                        btn.isEnabled = false
                                    } else {
                                        tv_tips.visibility = View.INVISIBLE
                                        btn.isEnabled = true
                                    }
                                    //计算
                                    CalcRate()
                                }

                                override fun onScrollingStarted(wheel: RulerMoney) {}

                                override fun onScrollingFinished(wheel: RulerMoney) {}

                            })
                            ruler_money.setData(moneyList, proConf.valid.toString())
                        }

                        //借款天数数据
                        if (dayList!!.size <= 0) {
                            for (day in proConf.days!!) {
                                dayList!!.add(day.toString())
                            }
                            ruler_days.setScrollingListener(object : RulerDays.OnWheelScrollListener {
                                override fun onChanged(wheel: RulerDays, oldValue: Int, newValue: Int) {
                                    if (newValue % 5 == 0) {
                                        //记录当前选择天数
                                        curDays = BigDecimal(dayList!!.get(newValue / 5))
                                        //计算
                                        CalcRate()
                                    }
                                }

                                override fun onScrollingStarted(wheel: RulerDays) {}

                                override fun onScrollingFinished(wheel: RulerDays) {}
                            })
                            ruler_days.setData(dayList!!)
                        }
                    }
                })
    }

    private fun CalcRate() {
        if (dayRate == null) {
            getProConf()
            return
        }
        val fee = curAmount?.multiply(dayRate)?.multiply(curDays)
        //到账金额
        tv_amount.text = getSpString2(ArithUtils.coverMoneyCommaMaybeSmall(df.format(curAmount.subtract(fee))) + " 元")
        //手续费
        tv_fee.text = getSpString2(ArithUtils.coverMoneyCommaMaybeSmall(df.format(fee)) + " 元")
        //天数
        tv_days.text = getSpString2(curDays.toString() + " 天")
    }

    private fun getSpString2(s: String): SpannableString {
        val sp = SpannableString(s)
        sp.setSpan(AbsoluteSizeSpan(16, true), 0, s.length - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        sp.setSpan(ForegroundColorSpan(resources.getColor(R.color.xl_gray_headline)),
                0, s.length - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return sp
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            //显示了 请求下消息接口
            activity.getNotices()
            //获取banner数据
            getBanner()
            getBorrowInfo()
            //费率为空 重新请求
            if (dayRate == null) {
                getProConf()
            }
        }
    }
}