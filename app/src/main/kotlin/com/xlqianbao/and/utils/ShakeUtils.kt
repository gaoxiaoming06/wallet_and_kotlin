package com.xlqianbao.and.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.*

/**
 * Created by gao on 2017/9/19.
 */
class ShakeUtils(context: Context): SensorEventListener{

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent) {
        //values[0]:X轴，values[1]：Y轴，values[2]：Z轴    
        val x = Math.abs(event.values[0])
        val y = Math.abs(event.values[1])
        val z = Math.abs(event.values[2])

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if ((x >= SENSOR_VALUE || y >= SENSOR_VALUE || z >= SENSOR_VALUE) && canShake) {
                //控制间隔时间
                canShake = false
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        canShake = true
                    }
                }, INTERVAL_VALUE)
                mOnShakeListener?.onShake()
            }
        }
    }

    private val mSensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var mOnShakeListener: OnShakeListener? = null

    //控制是否响应
    private var canShake = true


    fun setOnShakeListener(listener: OnShakeListener) {
        this.mOnShakeListener = listener
    }

    fun onResume() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun onPause() {
        mSensorManager.unregisterListener(this)
    }

    companion object {
        val SENSOR_VALUE:Int = 10     //灵敏度
        val INTERVAL_VALUE:Long = 800  //多久响应一次
    }

    interface OnShakeListener {
        fun onShake()
    }
}