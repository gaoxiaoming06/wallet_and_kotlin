package com.xlqianbao.and.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.provider.MediaStore
import com.twotiger.library.utils.LogUtil
import com.twotiger.library.utils.ScreenUtils
import com.xlqianbao.and.R
import com.xlqianbao.and.ui.auth.id.IdAuthActivity
import java.io.*
import java.lang.ref.SoftReference

/**
 * Created by gao on 2017/9/19.
 */
object ImageUtils{
    /**
     * 保存方法
     */
    fun saveImage(vararg bmps: Bitmap) {
        for (bmp in bmps) {
            val appDir = File(Environment.getExternalStorageDirectory(), "Boohee")
            if (!appDir.exists()) {
                appDir.mkdir()
            }
            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val file = File(appDir, fileName)
            try {
                val fos = FileOutputStream(file)
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                fos.flush()
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 根据图片路径删除图片 uri
     * @param context
     * @param imgPath
     */
    private fun DeleteImage(context: Context, imgPath: String) {
        val resolver = context.contentResolver
        val cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=?",
                arrayOf(imgPath), null)
        val result: Boolean
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uri = ContentUris.withAppendedId(contentUri, id)
            val count = context.contentResolver.delete(uri, null, null)
            result = count == 1
        } else {
            val file = File(imgPath)
            result = file.delete()
        }
        if (result) {
            LogUtil.info(IdAuthActivity::class.java, "delete bitmap success!")
        }
    }

    /**
     * 裁切框选区域（身份证）
     */
    fun ImageCrop(context: Context, bitmap: Bitmap): Bitmap {
        var bitmap = bitmap

        val margin = context.resources.getDimensionPixelSize(R.dimen.dp48)

        //        int[] dh = MyUtil.getWidthAndHeight();
        //        int width = dh[1] - margin;
        //        int height = dh[0] - margin * 2;

        //压缩图片尺寸到屏幕大小
        bitmap = Bitmap.createScaledBitmap(bitmap, ScreenUtils.getScreenHeighNotcontain(context), ScreenUtils.getScreenWidth(context), false)

        val b = BitmapFactory.decodeResource(context.resources, R.drawable.rz_img_sfz_face)
        val width = b.height
        val height = b.width

        //        int w = bitmap.getWidth(); // 得到图片的宽，高
        //        int h = bitmap.getHeight();
        //
        //        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长
        //
        //        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        //        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        //        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
        return Bitmap.createBitmap(bitmap, margin * 2, margin, width + margin, height + margin / 2, null, false)
    }

    /**
     * 图片转byte[]
     *
     * @param bmp
     * @param needRecycle
     * @return
     */
    fun bmpToByteArray(bmp: Bitmap, needRecycle: Boolean): ByteArray {
        val output = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, output)
        if (needRecycle) {
            bmp.recycle()
        }
        val result = output.toByteArray()
        try {
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }


    /**
     * 缩放图
     *
     * @param bitmap 源
     * @param height 是否压缩高
     * @return
     */
    fun ImageCrop2(context: Context, bitmap: Bitmap, height: Boolean): Bitmap {
        val b = BitmapFactory.decodeResource(context.resources, R.drawable.rz_img_sfzz)
        if (height) {
            return Bitmap.createScaledBitmap(bitmap, b.width, b.height, false)
        } else {
            //不压缩高度 等比例缩放
            val i = b.width.toFloat() / bitmap.width.toFloat()
            val height2 = (bitmap.height * i).toInt()
            return Bitmap.createScaledBitmap(bitmap, b.width, height2, false)
        }
    }

    /**
     * bitmap加圆角
     *
     * @param bitmap
     * @return
     */
    fun getRoundedCornerBitmap(context: Context, bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = ScreenUtils.dpToPx(context, 12f)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    /**
     * byte[]转bitmap
     *
     * @param imgByte
     * @return
     */
    fun byteToBitmap(imgByte: ByteArray?): Bitmap? {
        var imgByte = imgByte
        var input: InputStream? = null
        var bitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        input = ByteArrayInputStream(imgByte)
        val softRef = SoftReference(BitmapFactory.decodeStream(
                input, null, options))
        bitmap = softRef.get()
        if (imgByte != null) {
            imgByte = null
        }

        try {
            if (input != null) {
                input.close()
            }
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return bitmap
    }
}