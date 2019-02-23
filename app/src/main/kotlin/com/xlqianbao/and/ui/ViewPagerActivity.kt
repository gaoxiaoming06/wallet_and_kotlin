package com.xlqianbao.and.ui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.ViewGroup
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.ui.auth.BankAuthStepOneFragment
import com.xlqianbao.and.ui.auth.BankAuthStepTwoFragment
import com.xlqianbao.and.ui.auth.ContactAndWorkInfoFragment
import com.xlqianbao.and.ui.auth.InfoAuthFragment
import com.xlqianbao.and.ui.auth.id.choose.ChooseCityFragment
import com.xlqianbao.and.ui.auth.id.choose.ChooseDistFragment
import com.xlqianbao.and.ui.auth.id.choose.ChooseProvFragment
import com.xlqianbao.and.ui.regLogin.InputPhoneFragment
import com.xlqianbao.and.ui.regLogin.RegLoginFragment
import com.xlqianbao.and.view.ViewPagerWithNoScroll
import kotlinx.android.synthetic.main.activity_view_pager.*


/**
 * viewpagerAct 适用于常规流程页面
 * Created by Makise on 2017/2/6.
 */

class ViewPagerActivity : BaseActivity() {

    companion object {
        //putExtra name
        const val VP_ACT_TYPE = "VP_ACT_TYPE"
        const val EXTRA_DATA = "EXTRA_DATA"

        //putExtra type
        //注册登录
        const val TYPE_REG_LOGIN = "TYPE_REG_LOGIN"
        //绑卡
        const val BIND_CARD = "BIND_CARD"
        //个人信息认证
        const val INFO_AUTH = "INFO_AUTH"
        //省市区选择
        const val CHOOSE_ADDR = "CHOOSE_ADDR"


    }

    lateinit private var fragmentList: ArrayList<Fragment>
    //区分要载入的页面
    lateinit private var type: String

    lateinit public var viewPager:ViewPagerWithNoScroll

    override fun initView() {
        setContentView(R.layout.activity_view_pager)
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        type = intent.getStringExtra(VP_ACT_TYPE)
        fragmentList = ArrayList()

        viewPager=view_pager

        //设置状态栏
        if (TYPE_REG_LOGIN.equals(type)) {
            view.setPadding(0, config.getPixelInsetTop(false), 0, config.getPixelInsetBottom())
            tintManager.setTintResource(R.color.black)
        } else {
            setPixelInsetTop(false, R.color.xl_blue)
        }

        //add fragment
        when (type) {
            TYPE_REG_LOGIN -> {
                //注册登录
                fragmentList.add(InputPhoneFragment())
                fragmentList.add(RegLoginFragment())
            }
            BIND_CARD -> {
                //绑卡
                fragmentList.add(BankAuthStepOneFragment())
                fragmentList.add(BankAuthStepTwoFragment())
            }
            INFO_AUTH -> {
                //个人信息认证

                fragmentList.add(InfoAuthFragment.getInstance(intent.getSerializableExtra(EXTRA_DATA) as Model.AllTypesList?))
                fragmentList.add(ContactAndWorkInfoFragment())
            }
            CHOOSE_ADDR -> {
                fragmentList.add(ChooseProvFragment())
                fragmentList.add(ChooseCityFragment())
                fragmentList.add(ChooseDistFragment())
            }
        }

        //setAdapter
        view_pager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getCount(): Int {
                return fragmentList.size
            }

            override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any) {}
        }

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                //注册登录两个页面的状态栏效果不一样
                if (TYPE_REG_LOGIN == type) {
                    if (position == 0) {
                        setStatusBarBlack(false)
                        view.setPadding(0, config.getPixelInsetTop(false), 0, config.getPixelInsetBottom())
                        tintManager.setTintResource(R.color.black)
                    } else if (position == 1) {
                        setPixelInsetTop(false, R.color.xl_blue)
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        val currentItem = view_pager.currentItem
        //设置不同页面对返回键的处理
        if (currentItem >= 1) {
            when (type) {
                TYPE_REG_LOGIN, BIND_CARD, INFO_AUTH, CHOOSE_ADDR -> view_pager.previous()
            }
            return
        }
        super.onBackPressed()
        if (TYPE_REG_LOGIN == type)
            overridePendingTransition(R.anim.silent_anim, R.anim.slide_out_to_bottom)
    }
}
