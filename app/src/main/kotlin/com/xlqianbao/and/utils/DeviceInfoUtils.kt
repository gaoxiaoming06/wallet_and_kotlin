package com.xlqianbao.and.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics

import com.xlqianbao.and.App

import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale
import java.util.UUID

/**
 * 收集设备信息 工具类
 * Created by Makise on 2017/3/14.
 */

object DeviceInfoUtils {

    val imei:String
        @SuppressLint("MissingPermission")
        get() {
            var tm = App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.deviceId
        }

    //获取手机IMSI号
    val imsi: String
        @SuppressLint("MissingPermission")
        get() {
            val telephonyManager = App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.subscriberId
        }

    @SuppressLint("MissingPermission")
    fun getuniqueId(): String {
        val tm = App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val imei = tm.deviceId
        val simSerialNumber = tm.simSerialNumber
        val androidId = android.provider.Settings.Secure.getString(
                App.instance.applicationContext.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        val deviceUuid = UUID(androidId.hashCode().toLong(), imei.hashCode().toLong() shl 32 or simSerialNumber.hashCode().toLong())
        return deviceUuid.toString()
    }

    /**
     * 获取设备唯一号
     *
     * @return
     */
    val deviceUniqueNumber: String
        @SuppressLint("MissingPermission")
        get() {
            try {
                val json = org.json.JSONObject()
                val tm = App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                var device_id = tm.deviceId

                val wifi = App.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager

                val mac = wifi.connectionInfo.macAddress
                json.put("mac", mac)

                if (TextUtils.isEmpty(device_id)) {
                    device_id = mac
                }

                if (TextUtils.isEmpty(device_id)) {
                    device_id = android.provider.Settings.Secure.getString(
                            App.instance.applicationContext.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                }

                json.put("device_id", device_id)

                return json.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return "未知"
        }

    /**
     * 获取网络类型
     *
     * @return
     */
    val netTypeName: String
        get() {
            val mConnectivityManager =App.instance.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mWiFiNetworkInfo.isConnected) {
                return "WIFI"
            } else {
                val conManager =App.instance.applicationContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = conManager.activeNetworkInfo ?: return "未知"
                val subtype = networkInfo.subtype
                when (subtype) {
                    1, 2, 4, 11, 7 -> return "2G"
                    3, 5, 6, 8, 9, 10, 12, 14 -> return "3G"
                    else -> return "4G"
                }
            }
        }

    /**
     * 获取网络运营商
     *
     * @return
     */
    val mobileType: String
        get() {
            var type = ""
            val iPhoneManager = App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val iNumeric = iPhoneManager.simOperator
            if (iNumeric.isNotEmpty()) {
                if (iNumeric == "46000" || iNumeric == "46002") {
                    type = "中国移动"
                } else if (iNumeric == "46001") {
                    type = "中国联通"
                } else if (iNumeric == "46003") {
                    type = "中国电信"
                }
            }
            return type
        }

    /**
     * 获取分辨率
     *
     * @return
     */
    fun getScreenPixel(context: Context): String {
        val dm = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(dm)
        return dm.heightPixels.toString() + "*" + dm.widthPixels
    }

    /**
     * 获取操作系统版本及API
     *
     * @return
     */
    val osVersion: String
        get() = Build.VERSION.RELEASE + "; API/" + Build.VERSION.SDK

    /**
     * 获取app版本号
     *
     * @param context
     * @return
     */
    fun getAppVersionName(context: Context?): String {
        if (context != null) {
            val pm = context.packageManager
            if (pm != null) {
                val pi: PackageInfo?
                try {
                    pi = pm.getPackageInfo(context.packageName, 0)
                    if (pi != null) {
                        return pi.versionName + "_" + pi.versionCode
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
        return ""
    }

    /**
     * 判断设备是否root
     *
     * @return the boolean`true`: 是<br></br>`false`: 否
     */
    val isDeviceRooted: Int
        get() {
            val su = "su"
            val locations = arrayOf("/system/bin/", "/system/xbin/", "/sbin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/xbin/", "/data/local/bin/", "/data/local/")
            for (location in locations) {
                if (File(location + su).exists()) {
                    return 1
                }
            }
            return 0
        }

    /**
     * 获取设备AndroidID
     *
     * @return AndroidID
     */
    val androidID: String
        @SuppressLint("HardwareIds")
        get() = Settings.Secure.getString(App.instance.applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

    /**
     * 获取wifi mac地址
     *
     * @return
     */
    val wifiBSSID: String
        get() {
            try {
                val wifi = App.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (wifi != null) {
                    val info = wifi.connectionInfo
                    if (info != null) return info.bssid
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return "null"
        }

    /**
     * 获取设备MAC地址
     *
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>`
     *
     * 需添加权限 `<uses-permission android:name="android.permission.INTERNET"/>`
     *
     * @return MAC地址
     */
    val macAddress: String
        get() {
            var macAddress = macAddressByWifiInfo
            if ("02:00:00:00:00:00" != macAddress) {
                return macAddress
            }
            macAddress = macAddressByNetworkInterface
            if ("02:00:00:00:00:00" != macAddress) {
                return macAddress
            }
            macAddress = macAddressByFile
            return if ("02:00:00:00:00:00" != macAddress) {
                macAddress
            } else "please open wifi"
        }

    /**
     * 获取设备MAC地址
     *
     * 需添加权限 `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>`
     *
     * @return MAC地址
     */
    private val macAddressByWifiInfo: String
        @SuppressLint("HardwareIds")
        get() {
            try {
                val wifi =App.instance.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (wifi != null) {
                    val info = wifi.connectionInfo
                    if (info != null) return info.macAddress
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return "02:00:00:00:00:00"
        }

    /**
     * 获取设备MAC地址
     *
     * 需添加权限 `<uses-permission android:name="android.permission.INTERNET"/>`
     *
     * @return MAC地址
     */
    private val macAddressByNetworkInterface: String
        get() {
            try {
                val nis = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (ni in nis) {
                    if (!ni.name.equals("wlan0", ignoreCase = true)) continue
                    val macBytes = ni.hardwareAddress
                    if (macBytes != null && macBytes.size > 0) {
                        val res1 = StringBuilder()
                        for (b in macBytes) {
                            res1.append(String.format("%02x:", b))
                        }
                        return res1.deleteCharAt(res1.length - 1).toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return "02:00:00:00:00:00"
        }

    /**
     * 获取设备MAC地址
     *
     * @return MAC地址
     */
    private val macAddressByFile: String
        get() {
            var result: ShellUtils.CommandResult = ShellUtils.execCmd("getprop wifi.interface", false)
            if (result.result == 0) {
                val name = result.successMsg
                if (name != null) {
                    result = ShellUtils.execCmd("cat /sys/class/net/$name/address", false)
                    if (result.result == 0) {
                        if (result.successMsg != null) {
                            return result.successMsg!!
                        }
                    }
                }
            }
            return "02:00:00:00:00:00"
        }

    /**
     * 获取设备厂商
     *
     * 如Xiaomi
     *
     * @return 设备厂商
     */

    val manufacturer: String
        get() = Build.MANUFACTURER

    /**
     * 获取设备型号
     *
     * 如MI2SC
     *
     * @return 设备型号
     */
    val model: String
        get() {
            var model: String? = Build.MODEL
            if (model != null) {
                model = model.trim { it <= ' ' }.replace("\\s*".toRegex(), "")
            } else {
                model = ""
            }
            return model
        }

    //判断当前设备是否是模拟器。如果返回TRUE，则当前是模拟器，不是返回FALSE
    val isEmulator: Int
        get() = if (EmulatorUtils.CheckEmulatorBuild() ||
                EmulatorUtils.CheckPipes() ||
                EmulatorUtils.CheckDeviceIDS() ||
                EmulatorUtils.CheckEmulatorFiles() ||
                EmulatorUtils.CheckImsiIDS() ||
                EmulatorUtils.CheckOperatorNameAndroid() ||
                EmulatorUtils.CheckQEmuDriverFile() ||
                EmulatorUtils.CheckPhoneNumber())
            1
        else
            0

    /**
     * 获取系统语言
     *
     * @return
     */
    //        Locale locale = App.instance.getApplicationContext().getResources().getConfiguration().locale;
    //        String language = locale.getLanguage();
    //        return language;
    val language: String
        get() = Locale.getDefault().toString()

    /**
     * 检测是否有gps模块
     *
     * @return
     */
    fun hasGPSDevice(): Int {
        val mgr = App.instance.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager ?: return 0
        val providers = mgr.allProviders ?: return 0
        return if (providers.contains(LocationManager.GPS_PROVIDER)) 1 else 0
    }

    /**
     * 获取代理设置信息
     *
     * @return
     */
    val proxyHost: Int
        get() {
            var proHost = android.net.Proxy.getDefaultHost()
            if (TextUtils.isEmpty(proHost))
                proHost = System.getProperty("http.proxyHost")
            return if (TextUtils.isEmpty(proHost)) 0 else 1
        }

    val deviceName: String
        get() = "user:" + Build.USER + "/display:" + Build.DISPLAY

    val cpuABI: String
        get() {
            var abi = Build.CPU_ABI
            var abi2 = Build.CPU_ABI2
            if (TextUtils.isEmpty(abi))
                abi = "null"
            if (TextUtils.isEmpty(abi2))
                abi2 = "null"
            return "abi:$abi/abi2:$abi2"
        }

    val fingerPrint: String
        get() = Build.FINGERPRINT

    val deviceSerial: String
        get() = Build.SERIAL
}
