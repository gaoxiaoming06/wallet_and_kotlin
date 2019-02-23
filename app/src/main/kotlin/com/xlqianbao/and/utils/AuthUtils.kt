package com.xlqianbao.and.utils

import android.os.Environment
import android.text.TextUtils
import cn.jpush.android.api.JPushInterface
import com.alibaba.fastjson.JSONObject
import com.twotiger.library.utils.PreferencesUtils
import com.twotiger.library.utils.ScreenUtils
import com.xlqianbao.and.App
import com.xlqianbao.and.Constants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

object AuthUtils {

    private val fileName = "xl_auth_id"

    private val SD_DIR = Environment.getExternalStorageDirectory().toString() + File.separator

    /**
     * 获取设备信息
     *
     * @return
     */
    val deviceInfo: String
        get() {
            val jsonObject = JSONObject()
            jsonObject.put("deviceName", DeviceInfoUtils.deviceName)
            jsonObject.put("deviceBrand", DeviceInfoUtils.manufacturer)
            jsonObject.put("deviceModel", DeviceInfoUtils.model)
            jsonObject.put("isSocketProxy", DeviceInfoUtils.proxyHost)
            jsonObject.put("canGps", DeviceInfoUtils.hasGPSDevice())
            jsonObject.put("language", DeviceInfoUtils.language)
            jsonObject.put("networkState", DeviceInfoUtils.netTypeName)
            jsonObject.put("netCarrier", DeviceInfoUtils.mobileType)
            jsonObject.put("devicePixel", ScreenUtils.getScreenPixel(App.instance.applicationContext))
            jsonObject.put("osVersion", DeviceInfoUtils.osVersion)
            jsonObject.put("imsi", DeviceInfoUtils.imsi)
            jsonObject.put("wifi_mac", DeviceInfoUtils.wifiBSSID)
            jsonObject.put("imei", DeviceInfoUtils.imei)
            jsonObject.put("androidId", DeviceInfoUtils.androidID)
            jsonObject.put("isRoot", DeviceInfoUtils.isDeviceRooted)
            jsonObject.put("mac", DeviceInfoUtils.macAddress)
            jsonObject.put("isSimulator", DeviceInfoUtils.isEmulator)
            jsonObject.put("cpu_abi", DeviceInfoUtils.cpuABI)
            jsonObject.put("fingerprint", DeviceInfoUtils.fingerPrint)
            jsonObject.put("serial", DeviceInfoUtils.deviceSerial)
            jsonObject.put("jpushId", JPushInterface.getRegistrationID(App.instance.applicationContext))
            return jsonObject.toJSONString()
        }

    /**
     * 存储authId到本地文件中
     *
     * @param authId
     */
    fun saveAuthId(authId: String) {
        //存到sp
        PreferencesUtils.putString(App.instance.applicationContext, Constants.SP_AUTH_ID, authId)
        //判断有sd卡 存到sd卡中
        val hasSDCard = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        if (hasSDCard)
            saveFile(authId, SD_DIR + fileName)
    }

    private fun saveFile(content: String, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                val dir = File(file.parent)
                dir.mkdirs()
                file.createNewFile()
            }
            val outStream = FileOutputStream(file)
            outStream.write(content.toByteArray())
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 获取authId
     *
     * @return
     */
    //从sp中取
    //从文件中取
    //sd卡中
    val authId: String
        get() {
            val authId = PreferencesUtils.getString(App.instance.applicationContext, Constants.SP_AUTH_ID, "")
            if (!TextUtils.isEmpty(authId)) return authId
            val file: File
            try {
                file = File(SD_DIR, fileName)
                if (file.exists()) {
                    val fis = FileInputStream(file)
                    val b = ByteArray(fis.available())
                    fis.read(b)
                    return fis.toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ""
        }
}
