package com.xlqianbao.and

import android.app.Activity
import android.app.Application
import android.util.Log
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import cn.jpush.android.api.JPushInterface
import com.baidu.location.BDLocation
import com.baidu.location.BDLocationListener
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.megvii.idcardquality.IDCardQualityLicenseManager
import com.megvii.licensemanager.Manager
import com.megvii.livenessdetection.LivenessLicenseManager
import com.tendcloud.tenddata.TCAgent
import com.twotiger.library.utils.PackageUtils
import com.twotiger.library.utils.PreferencesUtils
import com.twotiger.library.utils.Tools
import com.xlqianbao.and.utils.JsonParseUtil
import java.util.*


/**
 * 自定义application入口
 * Created by Makise on 2017/2/4.
 */

class App : Application() {
    var constantDataMap: Map<String, Model.ConstantData>? = null // 已key为键保存起来的常量数据集合
    lateinit private var activitys: LinkedList<Activity> //activity实例集合
    //百度定位
    lateinit var mLocationClient: LocationClient
    var myListener: BDLocationListener = MyLocationListener()

    private lateinit var idCardLicenseManager: IDCardQualityLicenseManager
    private lateinit var licenseManager: LivenessLicenseManager
    private lateinit var manager: Manager

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        CrashHandler.instance.init(this)
        instance = this
        activitys = LinkedList()
        //jpush
        initJpush()
        //td统计
        initTCAgent()
        //初始化定位
        initLocation()
        //联网授权faceid
        initFaceId()
    }

    /**
     * 初始化faceid 授权相关
     */
    private fun initFaceId() {
        //face id授权
        Thread(Runnable {
            //身份证
            idCardLicenseManager = IDCardQualityLicenseManager(this@App)
            //活体
            licenseManager = LivenessLicenseManager(this@App)
            //注册
            manager = Manager(this@App)
            manager.registerLicenseManager(idCardLicenseManager)
            manager.registerLicenseManager(licenseManager)
            //授权
            takeFaceIdLicense()
        }).start()
    }

    /**
     * 联网授权faceid
     */
    private fun takeFaceIdLicense() {
        manager.takeLicenseFromNetwork(Tools.getDeviceId(this@App))
        if (idCardLicenseManager.checkCachedLicense() > 0 && licenseManager.checkCachedLicense() > 0) {

        } else {
            //授权失败 5s重新请求
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    takeFaceIdLicense()
                }
            }, 5000)
        }
    }

    private fun initJpush() {
        JPushInterface.init(this)
        JPushInterface.setDebugMode(BuildConfig.DEBUG)
    }

    private fun initTCAgent() {
        TCAgent.LOG_ON = BuildConfig.DEBUG
        TCAgent.init(this)
    }

    private fun initLocation() {
        //清空sp中的inhands信息
        PreferencesUtils.putString(applicationContext, Constants.SP_IN_HANDS, "NO")
        //清空定位信息
        PreferencesUtils.putString(applicationContext, Constants.SP_LOCATION, "")
        //声明LocationClient类
        mLocationClient = LocationClient(applicationContext)
        //设置定位参数
        initLocationParams()
        //注册监听函数
        mLocationClient.registerLocationListener(myListener)
        //启动定位
        mLocationClient.start()
    }

    // 添加Activity到容器中
    fun addActivity(activity: Activity) {
        if (activitys.isEmpty()) {
            activitys.add(activity)
        } else if (!activitys.contains(activity)) {
            activitys.add(activity)
        }
    }

    // 移除Activity到容器中
    fun removeActivity(activity: Activity) {
        if (activitys.contains(activity)) {
            activitys.remove(activity)
        }
    }

    /**
     * 完全退出
     */
    fun exit() {
        //activity组件finish
        if (!activitys.isEmpty()) {
            for (activity in activitys) {
                activity.finish()
            }
        }
        //完全退出
        System.exit(0)
    }

    /**
     * 获取用户

     * @return
     */
    val user: Model.User
        get() {
            val userInfo = PreferencesUtils.getString(this, Constants.USER_TOKEN)
            return if (userInfo != null) {
                JsonParseUtil.json2DataBean(userInfo, Model.User::class.java)
            } else {
                Model.User()
            }
        }

    /**
     * 持久化登录信息

     * @param user
     */
    fun saveUser(user: Model.User?) {
        var tempUser = user
        if (tempUser == null) {
            tempUser = Model.User()
        }
        PreferencesUtils.putString(this, Constants.USER_TOKEN, JsonParseUtil.dataBean2Json(tempUser))
    }

    /**
     * 清除WebView缓存
     */
    fun clearWebViewCache() {
        // 清除cookie即可彻底清除缓存
        CookieSyncManager.createInstance(applicationContext)
        CookieManager.getInstance().removeAllCookie()
    }

    /**
     * 是否版本升级

     * @return true表示升级，false表示没有升级
     */
    val isUpgrade: Boolean
        get() {
            val versionPre = PreferencesUtils.getString(applicationContext, Constants.SP_VERSION_NAME, null)
            val versionNow = PackageUtils.getAppVersionName(applicationContext)
            return versionNow != versionPre
        }

    private fun initLocationParams() {
        val option = LocationClientOption()
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll")
        //可选，默认gcj02，设置返回的定位结果坐标系

        val span = 5000
        option.setScanSpan(span)
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(false)
        //可选，设置是否需要地址信息，默认不需要

        option.isOpenGps = true
        //可选，默认false,设置是否使用gps

        option.isLocationNotify = true
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(false)
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(false)
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(true)
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false)
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false)
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.locOption = option
    }

    inner class MyLocationListener : BDLocationListener {

        override fun onReceiveLocation(location: BDLocation) {
            //获取定位结果
            val sb = StringBuffer(256)

            sb.append("time : ")
            sb.append(location.time)    //获取定位时间

            sb.append("\nerror code : ")
            sb.append(location.locType)    //获取类型类型

            sb.append("\nlatitude : ")
            sb.append(location.latitude)    //获取纬度信息

            sb.append("\nlontitude : ")
            sb.append(location.longitude)    //获取经度信息

            sb.append("\nradius : ")
            sb.append(location.radius)    //获取定位精准度

            if (location.locType == BDLocation.TypeGpsLocation) {

                // GPS定位结果
                sb.append("\nspeed : ")
                sb.append(location.speed)    // 单位：公里每小时

                sb.append("\nsatellite : ")
                sb.append(location.satelliteNumber)    //获取卫星数

                sb.append("\nheight : ")
                sb.append(location.altitude)    //获取海拔高度信息，单位米

                sb.append("\ndirection : ")
                sb.append(location.direction)    //获取方向信息，单位度

                sb.append("\naddr : ")
                sb.append(location.addrStr)    //获取地址信息

                sb.append("\ndescribe : ")
                sb.append("gps定位成功")
                //写入sp定位信息
                PreferencesUtils.putString(instance, Constants.SP_LOCATION,
                        location.longitude.toString() + "," + location.latitude + "," + location.altitude)
                //停止定位服务
                mLocationClient.stop()

            } else if (location.locType == BDLocation.TypeNetWorkLocation) {

                // 网络定位结果
                sb.append("\naddr : ")
                sb.append(location.addrStr)    //获取地址信息

                sb.append("\noperationers : ")
                sb.append(location.operators)    //获取运营商信息

                sb.append("\ndescribe : ")
                sb.append("网络定位成功")
                //写入sp定位信息
                PreferencesUtils.putString(instance, Constants.SP_LOCATION,
                        location.longitude.toString() + "," + location.latitude + "," + location.altitude)
                //停止定位服务
                mLocationClient.stop()

            } else if (location.locType == BDLocation.TypeOffLineLocation) {

                // 离线定位结果
                sb.append("\ndescribe : ")
                sb.append("离线定位成功，离线定位结果也是有效的")
                //写入sp定位信息
                PreferencesUtils.putString(instance, Constants.SP_LOCATION,
                        location.longitude.toString() + "," + location.latitude + "," + location.altitude)
                //停止定位服务
                mLocationClient.stop()

            } else if (location.locType == BDLocation.TypeServerError) {

                sb.append("\ndescribe : ")
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因")

            } else if (location.locType == BDLocation.TypeNetWorkException) {

                sb.append("\ndescribe : ")
                sb.append("网络不同导致定位失败，请检查网络是否通畅")

            } else if (location.locType == BDLocation.TypeCriteriaException) {

                sb.append("\ndescribe : ")
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机")

            }

            sb.append("\nlocationdescribe : ")
            sb.append(location.locationDescribe)    //位置语义化信息

            val list = location.poiList    // POI数据
            if (list != null) {
                sb.append("\npoilist size = : ")
                sb.append(list.size)
                for (p in list) {
                    sb.append("\npoi= : ")
                    sb.append(p.id + " " + p.name + " " + p.rank)
                }
            }
            if (BuildConfig.DEBUG)
                Log.i("BaiduLocationApiDem", sb.toString())
        }

        override fun onConnectHotSpotMessage(s: String, i: Int) {

        }
    }
}
