package com.xlqianbao.and.ui.home

import android.annotation.TargetApi
import android.os.Build
import android.support.v4.app.FragmentManager
import android.widget.RadioButton
import com.twotiger.library.utils.PreferencesUtils
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.utils.FragmentFactory
import com.xlqianbao.and.utils.ShakeUtils
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.utils.rx.RxSubscriptions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_home.*

/**
 * 首页
 */
class HomeActivity : BaseActivity() {
    var mainFragmentManager: FragmentManager? = null
    private var mRxSubSticky: Disposable? = null
    private lateinit var shakeUtils: ShakeUtils

    override fun initView() {
        setContentView(R.layout.activity_home)
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        if (mainFragmentManager == null)
            mainFragmentManager = supportFragmentManager
        //检测版本升级
        checkUpdate(false)
        //请求消息
        getNotices()
        //生成授权id 拿不到的时候 循环请求
        getAuthId()
        //Rxbus 实现首页标签的切换  传入对应的id
        mRxSubSticky = RxBus.default
                .toObservable(Model.HomeTabMessage::class.java)
                .subscribe { bottom_menu.check(it.witchTab) }
        RxSubscriptions.add(mRxSubSticky)
        bottom_menu.setOnCheckedChangeListener { group, checkedId -> changeFragment(checkedId, false) }

        shakeUtils = ShakeUtils(this)
        shakeUtils.setOnShakeListener(object : ShakeUtils.OnShakeListener {
            override fun onShake() {
                //存为yes
                PreferencesUtils.putString(applicationContext, Constants.SP_IN_HANDS, "YES")
            }
        })
    }

    /**
     * 切换Fragment

     * @param fragmentId
     * *
     * @param isAddToBackStackAndShowBack
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    fun changeFragment(fragmentId: Int, isAddToBackStackAndShowBack: Boolean) {
        val toFragment = FragmentFactory.getInstanceByBtnID(fragmentId)
        val fromFragment = FragmentFactory.getInstanceByBtnID(currentBtn)
        val toTag = toFragment.javaClass.getSimpleName()
        val ft = mainFragmentManager!!.beginTransaction()
        /**
         * 如果要切换到的Fragment没有被Fragment事务添加，则隐藏被切换的Fragment，添加要切换的Fragment
         * 否则，则隐藏被切换的Fragment，显示要切换的Fragment
         */
        if (!toFragment.isAdded && toFragment.tag == null) {
            ft.add(R.id.main_fragment, toFragment, toTag)
        }
        if (fromFragment.isAdded) {
            ft.hide(fromFragment)
        }
        ft.show(toFragment)
        currentBtn = fragmentId
        if (isAddToBackStackAndShowBack) {
            ft.addToBackStack(null)
        }
        ft.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        //        super.onBackPressed();
    }

    override fun onResume() {
        super.onResume()
        val toFragment = bottom_menu.checkedRadioButtonId
        val tmp = intent.getIntExtra("BUTTON", toFragment)
        if (tmp > 0 && tmp != toFragment) {//需要更换当前显示页面
            (bottom_menu.findViewById(tmp) as RadioButton).isChecked = true
            intent.removeExtra("BUTTON")
        } else {
            changeFragment(toFragment, false)
        }
        shakeUtils.onResume()
    }

    override fun onPause() {
        super.onPause()
        shakeUtils.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        RxSubscriptions.remove(mRxSubSticky!!)
    }

    companion object {
        //当前显示的fragment btnId
        private var currentBtn = 0
    }
}
