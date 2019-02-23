package com.xlqianbao.and.base

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.readystatesoftware.systembartint.SystemBarTintManager
import com.twotiger.library.utils.*
import com.twotiger.library.version.UpdateVersionUtil
import com.xlqianbao.and.App
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.Model.HomeTabMessage
import com.xlqianbao.and.Model.User
import com.xlqianbao.and.R
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.ViewPagerActivity
import com.xlqianbao.and.ui.home.HomeActivity
import com.xlqianbao.and.utils.AuthUtils
import com.xlqianbao.and.utils.JsonParseUtil
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.view.CustomDialog
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList


/**
 * activity基类
 * Created by Makise on 2017/2/4.
 */

abstract class BaseActivity : AppCompatActivity() {
    lateinit private var application: App
    private var mExitTime: Long = 0
    lateinit protected var view: View
    lateinit var tintManager: SystemBarTintManager
    lateinit var config: SystemBarTintManager.SystemBarConfig
    private var canGetNotices: Boolean = false
    //自定义字体
    lateinit var customFont: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        instance = this
        application = App.instance
        application.addActivity(this)
        //载入字体
        customFont = Typeface.createFromAsset(assets, "fonts/Gotham-Light.otf")
        // 4.4 festuree
        // create our manager instance after the content view is set
        tintManager = SystemBarTintManager(this)
        // enable status bar tint
        tintManager.isStatusBarTintEnabled = true
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true)
        // set a custom tint color for all system bars
        config = tintManager.config

        initView()
        view = getWindow().getDecorView().findViewById(android.R.id.content)
        initData()
    }

    /**
     * 初始化view
     */
    protected abstract fun initView()

    /**
     * 初始化变量
     */
    protected abstract fun initData()

    /**
     * 设置statusBar，默认为true，即默认忽略了statusBar的高度。

     * @param flag  false不顶上去
     * *
     * @param color 指定statusBar 颜色
     */
    fun setPixelInsetTop(flag: Boolean, color: Int) {
        //设置状态栏颜色
        if (flag) {
            //顶上去的
            view.setPadding(0, 0, 0, config.pixelInsetBottom)
            tintManager.setTintResource(color)
        } else {
            //不顶上去的
//            if (!setStatusBarBlack(true)) {
            //小米和魅族的设置深色状态栏，不成功就直接设置成黑色状态栏
            //                color = R.color.black;
//            }
            view.setPadding(0, config.getPixelInsetTop(false), 0, config.pixelInsetBottom)
            tintManager.setTintResource(color)
        }
    }

    /**
     * 设置深色状态栏

     * @param dark
     */
    fun setStatusBarBlack(dark: Boolean): Boolean {
        var result: Boolean
        //设置魅族or小米
        result = FlymeSetStatusBarLightMode(window, dark)
        if (result) return result
        result = MIUISetStatusBarLightMode(window, dark)
        if (result) return result
        return result
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户

     * @param window 需要设置的窗口
     * *
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * *
     * @return boolean 成功执行返回true
     */
    fun FlymeSetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            try {
                val lp = window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.setAccessible(true)
                meizuFlags.setAccessible(true)
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                window.attributes = lp
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上

     * @param window 需要设置的窗口
     * *
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * *
     * @return boolean 成功执行返回true
     */
    fun MIUISetStatusBarLightMode(window: Window?, dark: Boolean): Boolean {
        var result = false
        if (window != null) {
            val clazz = window.javaClass
            try {
                var darkModeFlag: Int
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag)//清除黑色字体
                }
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    fun getToken(): String? {
        return application.user.token
    }

    /**
     * 获取消息弹窗
     */
    fun getNotices() {
        if (getToken() == null || !canGetNotices) return
        HttpMethods.instance
                .notices(getToken(), "borrow", object : ProgressSubscriber<Model.Notices>(true) {
                    override fun onNext(notices: Model.Notices) {
                        canGetNotices = true
                        if (notices == null) return
                        //只有在homeAct才弹窗
                        if (!PackageUtils.isTopActivity(this@BaseActivity, HomeActivity::class.java.name))
                            return
                        CustomDialog.Builder(context)
                                .setTitle(notices.title)
                                .setMessage(notices.content)
                                .setNegativeButton("我知道了", DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    HttpMethods.instance
                                            .readNotice(getToken(), notices!!.id.toString(), object : ProgressSubscriber<String>(false) {
                                                override fun onNext(code: String) {
                                                    //发送已读回执给服务器
                                                }
                                            })
                                }).show(R.layout.dialog_home_layout)
                    }

                    public override fun onStart() {
                        super.onStart()
                        //单次请求
                        canGetNotices = false
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        canGetNotices = true
                    }
                })
    }

    /**
     * 传入任意数量的view tv et等 设置字体
     *
     * @param views
     */
    fun setCustomFont(vararg views: View) {
        for (view in views) {
            //因instanceof特性 先判断是否为edittext 并return 不继续判断
            if (view is EditText) {
                view.typeface = customFont
                view.includeFontPadding = false
                return
            }
            if (view is TextView) {
                view.typeface = customFont
                view.includeFontPadding = false
            }
        }
    }

    /**
     * 获取授权id
     */
    fun getAuthId() {
        val authId = AuthUtils.authId
        if (!TextUtils.isEmpty(authId)) return
        HttpMethods.instance
                .getAuthId(getToken(), AuthUtils.deviceInfo,
                        object : ProgressSubscriber<Model.Auth>(false) {
                            override fun onNext(auth: Model.Auth) {
                                AuthUtils.saveAuthId(auth.AuthID)
                            }

                            override fun onError(e: Throwable) {
                                //                                super.onError(e);
                                getAuthId()
                            }
                        })
    }

    /**
     * 获取本地datatoken数据

     * @return
     */
    private val loacalDataToken: String
        get() {
            var dataToken = ""
            val constantDataStr = PreferencesUtils.getString(context, Constants.CONSTANT_DATA)
            if (!StringUtils.isEmpty(constantDataStr)) {
                val constantQuery = JsonParseUtil.json2DataBean(constantDataStr, Model.ConstantQuery::class.java)
                dataToken = constantQuery.dataToken
            }
            return dataToken
        }

    /**
     * 获取本地存储的常量list数据

     * @return
     */
    private val localDataList: List<Model.ConstantData>?
        get() {
            var mList: List<Model.ConstantData>
            val constantDataStr = PreferencesUtils.getString(context, Constants.CONSTANT_DATA)
            if (!StringUtils.isEmpty(constantDataStr)) {
                val constantQuery = JsonParseUtil.json2DataBean(constantDataStr, Model.ConstantQuery::class.java)
                mList = constantQuery.list
                return mList
            }
            return null
        }

    /**
     * 通过key获得常量数据（内存或者本地）
     * 如果没哟数据则请求

     * @param key
     * *
     * @return
     */
    fun getConstantDataByKey(key: String): Model.ConstantData? {
        var constantDataMap: Map<String, Model.ConstantData>? = application.constantDataMap
        if (constantDataMap == null) {
            constantDataMap = changeListToMap(localDataList!!)
        }
        if (constantDataMap == null || constantDataMap[key] == null) {
            //说明内存和本地都没有，或者数据中没有对应的key  需要从新加载
            getConstantData()
            return null
        }
        val mData = constantDataMap[key]
        return mData
    }

    /**
     * 比较两个的差异list 并生成新的list (基于网络请求获得的list)

     * @param newList
     * *
     * @param oldList
     */
    protected fun getDifferentFromTwoList(newList: List<Model.ConstantData>,
                                          oldList: List<Model.ConstantData>?): List<Model.ConstantData> {
        if (ListUtils.isEmpty(oldList)) {
            return newList
        }
        val result = ArrayList<Model.ConstantData>()
        for (i in newList.indices) {
            var isCommon = false
            for (j in oldList!!.indices) {
                if (oldList[j].key == newList[i].key) {
                    if (Integer.parseInt(newList[i].ver) > Integer.parseInt(oldList[j]
                            .ver)) {
                        result.add(newList[i])
                    } else {
                        result.add(oldList[j])
                    }
                    isCommon = true
                }
            }
            if (!isCommon) {
                result.add(newList[i])
            }
        }
        return result
    }

    fun getConstantData() {
        HttpMethods.instance
                .constantQuery(loacalDataToken, object : ProgressSubscriber<Model.ConstantQuery>(false) {
                    override fun onNext(constantQuery: Model.ConstantQuery) {
                        if (loacalDataToken != constantQuery.dataToken) {
                            ////比较本地datatoken与请求datatoken不相同，更新
                            val mDataList = constantQuery.list
                            val localDataList: List<Model.ConstantData>? = localDataList
                            val newList = getDifferentFromTwoList(mDataList, localDataList)
                            changeListToMap(newList)
                            val newConstantQuery = Model.ConstantQuery("", listOf())
                            newConstantQuery.list = newList
                            newConstantQuery.dataToken = constantQuery.dataToken
                            val newConstantDataStr = JsonParseUtil.dataBean2Json(newConstantQuery)
                            PreferencesUtils.putString(context, Constants.CONSTANT_DATA,
                                    newConstantDataStr)
                        }
                    }
                })
    }

    /**
     * 更新用户信息
     */
    fun getUserInfo() {
        HttpMethods.instance
                .userInfo(getToken()!!, object : ProgressSubscriber<Model.User>(false) {
                    override fun onNext(user: Model.User) {
                        saveUser(user)
                    }
                })
    }

    fun saveUser(user: Model.User) {
        application.saveUser(user)
    }

    /**
     * 将list数据转为map, 并将map存储到内存

     * @return
     */
    private fun changeListToMap(sourceList: List<Model.ConstantData>): Map<String, Model.ConstantData>? {
        val map = HashMap<String, Model.ConstantData>()
        if (ListUtils.isEmpty(sourceList)) {
            return null
        }
        for (i in sourceList.indices) {
            map.put(sourceList[i].key, sourceList[i])
        }
        application.constantDataMap = map
        return map
    }

    fun getUser(): User = application.user

    fun toLogin() {
        startNewActivity(Intent(this@BaseActivity, ViewPagerActivity::class.java)
                .putExtra(ViewPagerActivity.VP_ACT_TYPE, ViewPagerActivity.TYPE_REG_LOGIN),
                R.anim.slide_in_from_bottom, R.anim.silent_anim, false)
    }

    /**
     * 检查登录状态
     *
     * @param toPage 要 跳转的页面
     */
    fun checkLoginStatus(toPage: Class<*>) {
        checkLoginStatus(Intent(this, toPage))
    }

    fun checkLoginStatus(toPage: Intent?) {
        if (getToken() != null && toPage != null) {
            //已登录 跳转页面
            startNewActivity(toPage, false)
        } else {
            //未登录
            toLogin()
        }
    }

    /**
     * 检查绑卡状态
     *
     * @param toPage 如已绑卡 要跳转的页面
     */
    fun checkBindCardStatus(toPage: Class<*>?) {
        HttpMethods.instance
                .queryCard(getToken(), object : ProgressSubscriber<Model.QueryCard>(true) {
                    override fun onNext(queryCard: Model.QueryCard) {
                        if (queryCard != null) {
                            //已绑卡 跳转页面
                            if (toPage != null)
                                startNewActivity(Intent(this@BaseActivity, toPage).putExtra("card", queryCard), false)
                        } else {
                            //未绑卡 检查认证状态
                            if ("0" == getUser().IDAF) {
                                //未实名 跳到认证页面
                                Toast.makeText(this@BaseActivity, "请先完成身份认证", Toast.LENGTH_SHORT).show()
                                RxBus.default.postSticky(HomeTabMessage(R.id.btn_2))
                                return
                            }
                            //已实名 认证已通过 去绑卡
                            startNewActivity(Intent(this@BaseActivity, ViewPagerActivity::class.java)
                                    .putExtra(ViewPagerActivity.VP_ACT_TYPE, ViewPagerActivity.BIND_CARD), false)
                        }
                    }
                })
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (PackageUtils.isTopActivity(this, HomeActivity::class.java.getName())) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                    if (System.currentTimeMillis() - mExitTime > 2000) {
                        Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                        mExitTime = System.currentTimeMillis()
                    } else {
                        exitApp()
                    }
                }
                return true
            }
            mExitTime = 0
            return super.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * 适配部分机型的光标闪烁，但是键盘不需要弹出
     */
    fun disableShowSoftInput(vararg editText: EditText) {
        for (editText in editText) {
            val cls = EditText::class.java
            var method: Method
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(editText, false)
            } catch (e: Exception) {
                // TODO: handle exception
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(editText, false)
            } catch (e: Exception) {
                // TODO: handle exception
            }

        }
    }

    /**
     * 检测版本升级的方法
     */
    fun checkUpdate(showToast: Boolean) {
        HttpMethods.instance
                .update(object : ProgressSubscriber<Model.Update>(true) {
                    override fun onNext(update: Model.Update) {
                        val curVerCode = PackageUtils.getAppVersionCode(this@BaseActivity)
                        if (Integer.parseInt(update.ver) > curVerCode) {
                            //有新版本
                            CustomDialog.Builder(instance)
                                    .setMessage("检测到新版本，是否升级？" + if (NetWorkUtil.isWifiConnected(instance)) "" else "\n（当前非WIFI环境）")
                                    .setPositiveButton("取消", DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.dismiss() })
                                    .setNegativeButton("确定", DialogInterface.OnClickListener { dialogInterface, _ ->
                                        UpdateVersionUtil.beginToDownload(instance, resources.getString(R.string.app_name), R.drawable.ic_launcher, update.url)
                                        dialogInterface.dismiss()
                                    }).show()
                            return
                        }
                        if (showToast)
                            Toast.makeText(this@BaseActivity, "当前已是最新版本", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    /**
     * 退出App
     */
    fun exitApp() {
        application.exit()
    }

    companion object {
        lateinit var context: Context
        lateinit var instance: BaseActivity
//            protected set
    }
}
