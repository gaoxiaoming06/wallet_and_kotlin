package com.xlqianbao.and.utils

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager

import com.xlqianbao.and.App

import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * 检测设备是否是模拟器
 * Created by Makise on 2017/4/19.
 */

object EmulatorUtils {
    private val known_pipes = arrayOf("/dev/socket/qemud", "/dev/qemu_pipe")
    private val known_qemu_drivers = arrayOf("goldfish")
    private val known_files = arrayOf("/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu-props")
    private val known_numbers = arrayOf("15555215554", "15555215556", "15555215558", "15555215560", "15555215562", "15555215564", "15555215566", "15555215568", "15555215570", "15555215572", "15555215574", "15555215576", "15555215578", "15555215580", "15555215582", "15555215584")
    private val known_device_ids = arrayOf("000000000000000" // 默认ID
    )
    private val known_imsi_ids = arrayOf("310260000000000" // 默认的 imsi id
    )

    /**
     * 检测“/dev/socket/qemud”，“/dev/qemu_pipe”这两个通道
     *
     * @return
     */
    fun CheckPipes(): Boolean {
        //        Log.v("Result:", "Not Find pipes!");
        //                Log.v("Result:", "Find pipes!");
        return known_pipes.indices
                .map { known_pipes[it] }
                .map { File(it) }
                .any { it.exists() }
    }

    /**
     * 检测驱动文件内容
     * 读取文件内容，然后检查已知QEmu的驱动程序的列表
     *
     * @return
     */
    fun CheckQEmuDriverFile(): Boolean {
        val driver_file = File("/proc/tty/drivers")
        if (driver_file.exists() && driver_file.canRead()) {
            val data = ByteArray(driver_file.length().toInt())
            try {
                val inStream = FileInputStream(driver_file)
                inStream.read(data)
                inStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val driver_data = String(data)
            known_qemu_drivers
                    .filter { driver_data.contains(it) }
                    .forEach {
                        //                    Log.v("Result:", "Find known_qemu_drivers!");
                        return true
                    }
        }
        //        Log.v("Result:", "Not Find known_qemu_drivers!");
        return false
    }

    /**
     * 检测模拟器上特有的几个文件
     *
     * @return
     */
    fun CheckEmulatorFiles(): Boolean {
        //        Log.v("Result:", "Not Find Emulator Files!");
        //                Log.v("Result:", "Find Emulator Files!");
        return known_files.indices
                .map { known_files[it] }
                .map { File(it) }
                .any { it.exists() }
    }

    @SuppressLint("MissingPermission")
            /**
             * 检测模拟器默认的电话号码
             *
             * @return
             */
    fun CheckPhoneNumber(): Boolean {
        val telephonyManager = App.instance.applicationContext
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val phonenumber = telephonyManager.line1Number

        for (number in known_numbers) {
            if (number.equals(phonenumber, ignoreCase = true)) {
                //                Log.v("Result:", "Find PhoneNumber!");
                return true
            }
        }
        //        Log.v("Result:", "Not Find PhoneNumber!");
        return false
    }

    @SuppressLint("MissingPermission")
            /**
             * 检测设备IDS 是不是 “000000000000000”
             *
             * @return
             */
    fun CheckDeviceIDS(): Boolean {
        val telephonyManager = App.instance.applicationContext
                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val device_ids = telephonyManager.deviceId

        for (know_deviceid in known_device_ids) {
            if (know_deviceid.equals(device_ids, ignoreCase = true)) {
                //                Log.v("Result:", "Find ids: 000000000000000!");
                return true
            }
        }
        //        Log.v("Result:", "Not Find ids: 000000000000000!");
        return false
    }

    @SuppressLint("MissingPermission")
            /**
             * 检测imsi id是不是“310260000000000”
             *
             * @return
             */
    fun CheckImsiIDS(): Boolean {
        val telephonyManager = App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val imsi_ids = telephonyManager.subscriberId

        for (know_imsi in known_imsi_ids) {
            if (know_imsi.equals(imsi_ids, ignoreCase = true)) {
                //                Log.v("Result:", "Find imsi ids: 310260000000000!");
                return true
            }
        }
        //        Log.v("Result:", "Not Find imsi ids: 310260000000000!");
        return false
    }

    /**
     * 检测手机上的一些硬件信息
     *
     * @return
     */
    fun CheckEmulatorBuild(): Boolean {
        val BOARD = android.os.Build.BOARD
        val BOOTLOADER = android.os.Build.BOOTLOADER
        val BRAND = android.os.Build.BRAND
        val DEVICE = android.os.Build.DEVICE
        val HARDWARE = android.os.Build.HARDWARE
        val MODEL = android.os.Build.MODEL
        val PRODUCT = android.os.Build.PRODUCT
        return !(BOARD !== "unknown" && BOOTLOADER !== "unknown" && BRAND !== "generic" && DEVICE !== "generic" && MODEL !== "sdk" && PRODUCT !== "sdk" && HARDWARE !== "goldfish")
        //        Log.v("Result:", "Not Find Emulator by EmulatorBuild!");
    }

    /**
     * 检测手机运营商家
     *
     * @return
     */
    fun CheckOperatorNameAndroid(): Boolean {
        val szOperatorName = (App.instance.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkOperatorName

        return szOperatorName.toLowerCase() == "android"
        //        Log.v("Result:", "Not Find Emulator by OperatorName!");
    }
}
