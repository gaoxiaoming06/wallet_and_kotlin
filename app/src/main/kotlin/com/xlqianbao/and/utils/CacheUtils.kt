package com.xlqianbao.and.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment

import java.io.File
import java.io.FileOutputStream

/**
 * @Description: 缓存工具类，例如图片缓存sd卡等(这里描述这个类的作用)
 * *
 * @Author gao
 * *
 * @Date 2015年9月11日 下午1:48:41
 */
object CacheUtils {

    //默认图片保存路径
    val PATHCACHE = Environment.getExternalStorageDirectory().toString() + "/app名/cache/picture/"


    fun cacheBitmapToSDCard(bit: Bitmap, name: String) {
        var name = name
        name += ".png"
        val fileDir = File(PATHCACHE)
        if (!fileDir.exists()) {
            fileDir.mkdirs() // 创建文件夹
        }
        val f = File(PATHCACHE, name)
        if (f.exists()) {
            f.delete()
        }
        try {
            val out = FileOutputStream(f)
            bit.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getBitmapFormSDCard(name: String): Bitmap? {
        var name = name
        name += ".png"
        val mFile = File(PATHCACHE + name)
        //若该文件存在
        if (mFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(PATHCACHE + name)
            return bitmap
        } else {
            return null
        }
    }
}
