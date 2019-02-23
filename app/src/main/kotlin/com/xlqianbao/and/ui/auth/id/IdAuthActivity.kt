package com.xlqianbao.and.ui.auth.id

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.media.MediaPlayer
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.megvii.idcardlib.IDCardScanActivity
import com.megvii.livenesslib.LivenessActivity
import com.twotiger.library.utils.ViewUtils
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.ext.toast
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.utils.ImageUtils
import com.xlqianbao.and.utils.ToastUtil
import com.xlqianbao.and.view.CustomDialog
import kotlinx.android.synthetic.main.activity_id_auth.*
import kotlinx.android.synthetic.main.include_title_bar.*
import org.json.JSONException
import org.json.JSONObject
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by gao on 2017/9/19.
 */
class IdAuthActivity: BaseActivity(), View.OnClickListener {
    private var ucid1: String = ""
    private var ucid2:String = ""
    private var ucid3:String = ""
    private var delta:String = ""

    override fun initView() {
        setContentView(R.layout.activity_id_auth)
        left.setOnClickListener({ onBackPressed() })
        middle.text = "身份认证"
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        if ("0" == getUser().IDAF) {
            //未认证时 才允许 点击事件
            iv_face.setOnClickListener(this)
            iv_sfz.setOnClickListener(this)
            iv_sfz_f.setOnClickListener(this)
            btn_submit.text = "立即认证"
        } else {
            //已认证 显示图片
            getCertPics()
        }
    }

    /**
     * 获取服务器上的证件照片
     */
    private fun getCertPics() {
        HttpMethods.instance
                .certPics(getToken()!!, object : ProgressSubscriber<Model.CertPics>(true) {
                    override fun onNext(certPics: Model.CertPics) {
                        Glide.with(this@IdAuthActivity)
                                .load(Constants.HOST + certPics.IDAPath)
                                .asBitmap()
                                .error(R.drawable.rz_img_sfzz)
                                .placeholder(R.drawable.rz_img_sfzz)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: GlideAnimation<in Bitmap>) {
                                        iv_sfz.setImageBitmap(ImageUtils.getRoundedCornerBitmap(this@IdAuthActivity, ImageUtils.ImageCrop2(this@IdAuthActivity, resource, true)))
                                    }
                                })
                        iv_sfz_c.visibility = View.GONE
                        Glide.with(this@IdAuthActivity)
                                .load(Constants.HOST + certPics.IDBPath)
                                .asBitmap()
                                .error(R.drawable.rz_img_sfzf)
                                .placeholder(R.drawable.rz_img_sfzf)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: GlideAnimation<in Bitmap>) {
                                        iv_sfz_f.setImageBitmap(ImageUtils.getRoundedCornerBitmap(this@IdAuthActivity, ImageUtils.ImageCrop2(this@IdAuthActivity, resource, true)))
                                    }
                                })
                        iv_sfz_f_c.visibility = View.GONE
                        Glide.with(this@IdAuthActivity)
                                .load(Constants.HOST + certPics.HTPath)
                                .asBitmap()
                                .error(R.drawable.rz_img_yz)
                                .placeholder(R.drawable.rz_img_yz)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: GlideAnimation<in Bitmap>) {
                                        iv_face.setImageBitmap(ImageUtils.getRoundedCornerBitmap(this@IdAuthActivity, ImageUtils.ImageCrop2(this@IdAuthActivity, resource, false)))
                                    }
                                })
                        iv_face_c.visibility = View.GONE
                    }
                })
    }

    override fun onClick(v: View) {
        if (!isCameraCanUse()) {
            ToastUtil.showError(this, "摄像头权限被禁止", false)
            return
        }
        when (v.id) {
            R.id.iv_sfz ->
                //身份证正面
                startActivityForResult(Intent(this, IDCardScanActivity::class.java)
                        .putExtra("side", 0)
                        .putExtra("isvertical", false), INTO_IDCARDSCAN_PAGE)
            R.id.iv_sfz_f ->
                //身份证反面
                startActivityForResult(Intent(this, IDCardScanActivity::class.java)
                        .putExtra("side", 1)
                        .putExtra("isvertical", false), INTO_IDCARDSCAN_PAGE)
            R.id.iv_face -> {
                //活体
                if (TextUtils.isEmpty(ucid1) || TextUtils.isEmpty(ucid2)) {
                    CustomDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("请先进行身份证拍照")
                            .setNegativeButton("我知道了", DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                            .show(R.layout.dialog_auth_layout)
                    return
                }
                startActivityForResult(Intent(this, LivenessActivity::class.java), PAGE_INTO_LIVENESS)
            }
        }
    }

    fun submit(view: View) {
        if (ViewUtils.isFastDoubleClick(view)) return
        HttpMethods.instance
                .certConfirm(getToken()!!, delta, ucid1, ucid2, ucid3,
                        object : ProgressSubscriber<String>(true) {
                            override fun onNext(code: String) {
                                toast("验证成功",Toast.LENGTH_SHORT)
                                finish()
                            }
                        })
    }

    /**
     * 获得上传所需list
     *
     * @param certParamses
     * @return
     */
    private fun getListParams(vararg certParamses: CertParams): String {
        val certList = ArrayList<Cert>()
        for (certParam in certParamses) {
            certList.add(Cert(byte2Base64StringFun(certParam.pic),
                    certParam.type + "_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".jpg",
                    certParam.type))
        }
        val jsonObject = com.alibaba.fastjson.JSONObject()
        jsonObject.put("certlist", certList)
        return jsonObject.toJSONString()
    }

    //byte[]转base64
    fun byte2Base64StringFun(b: ByteArray): String {
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun uploadCertPic(type: String, listJson: String) {
        //请求接口
        HttpMethods.instance
                .certPic(getToken()!!, listJson, object : ProgressSubscriber<Model.CertPic>(true) {
                    public override fun onStart() {
                        super.onStart()
                        //请求开始 隐藏错误提示
                        when (type) {
                            ID_A -> tv_id_a_tips.visibility = View.INVISIBLE
                            ID_B -> tv_id_b_tips.visibility = View.INVISIBLE
                            HT -> tv_ht_tips.visibility = View.INVISIBLE
                        }
                    }

                    override fun onNext(certPic: Model.CertPic) {
                        if (certPic != null && certPic!!.ucid != null) {
                            toast("上传成功", Toast.LENGTH_SHORT)
                            //给ucid赋值
                            when (type) {
                                ID_A -> ucid1 = certPic.ucid
                                ID_B -> ucid2 = certPic.ucid
                                HT -> ucid3 = certPic.ucid
                            }
                            if (!TextUtils.isEmpty(ucid1) && !TextUtils.isEmpty(ucid2) && !TextUtils.isEmpty(ucid3)) {
                                iv_shadow.setBackgroundResource(R.drawable.btn_top_shadow)
                                btn_submit.isEnabled = true
                            } else {
                                iv_shadow.setBackgroundResource(R.drawable.pic_bottom_shadow)
                                btn_submit.isEnabled = false
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        if (e is SocketTimeoutException ||
                                e is ConnectException ||
                                e is UnknownHostException) {
                            //网络异常
                            when (type) {
                                ID_A -> {
                                    tv_id_a_tips.text = "身份证正面上传失败，请重拍"
                                    tv_id_a_tips.visibility = View.VISIBLE
                                }
                                ID_B -> {
                                    tv_id_b_tips.text = "身份证背面上传失败，请重拍"
                                    tv_id_b_tips.visibility = View.VISIBLE
                                }
                                HT -> {
                                    tv_ht_tips.text = "颜值上传失败，请重扫颜值"
                                    tv_ht_tips.visibility = View.VISIBLE
                                }
                            }
                        } else {
                            //服务器返回的异常信息
                            when (type) {
                                ID_A -> {
                                    tv_id_a_tips.setText(e.message)
                                    tv_id_a_tips.setVisibility(View.VISIBLE)
                                }
                                ID_B -> {
                                    tv_id_b_tips.setText(e.message)
                                    tv_id_b_tips.setVisibility(View.VISIBLE)
                                }
                                HT -> {
                                    tv_ht_tips.setText(e.message)
                                    tv_ht_tips.setVisibility(View.VISIBLE)
                                }
                            }
                        }
                    }
                }.hideToast())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //身份证
            if (requestCode == INTO_IDCARDSCAN_PAGE) {
                //全景图
                val idcardEnvImgData = data?.getByteArrayExtra("idcardEnvImg")
                //裁切边框后的图
                val idcardImgData = data?.getByteArrayExtra("idcardImg")
                val idcardBmp = BitmapFactory.decodeByteArray(idcardImgData, 0, idcardImgData!!.size)
                //人像
                if (data.getIntExtra("side", 0) == 0) {
                    //正面
                    iv_sfz.setImageBitmap(ImageUtils.ImageCrop2(this, ImageUtils.getRoundedCornerBitmap(this, idcardBmp), true))
                    iv_sfz_c.visibility = View.GONE
                    //取人像图
                    val portraitImgData = data.getByteArrayExtra("portraitImg")
                    uploadCertPic(ID_A, getListParams(
                            CertParams(ID_A, idcardImgData),
                            CertParams(ID_A_HEAD, portraitImgData),
                            CertParams(ID_A_ALL, idcardEnvImgData!!)
                    ))
                } else {
                    //反面
                    iv_sfz_f.setImageBitmap(ImageUtils.ImageCrop2(this, ImageUtils.getRoundedCornerBitmap(this, idcardBmp), true))
                    iv_sfz_f_c.setVisibility(View.GONE)
                    uploadCertPic(ID_B, getListParams(
                            CertParams(ID_B, idcardImgData),
                            CertParams(ID_B_ALL, idcardEnvImgData!!)
                    ))
                }
                return
            }
            //活体
            if (requestCode == PAGE_INTO_LIVENESS) {
                val resultOBJ = data?.getStringExtra("result")
                try {
                    val result = JSONObject(resultOBJ)
                    toast(result.getString("result"), Toast.LENGTH_SHORT)
                    val resID = result.getInt("resultcode")
                    if (resID == R.string.verify_success) {
                        doPlay(R.raw.meglive_success)
                        //取delta
                        delta = data!!.getStringExtra("delta")
                        //最好的一张活体
                        val imageBestData = data?.getByteArrayExtra("imageBestData")
                        val imageBest = BitmapFactory.decodeByteArray(imageBestData, 0, imageBestData.size)
                        //全景
                        val imageEnvData = data.getByteArrayExtra("imageEnvData")
                        iv_face.setImageBitmap(ImageUtils.ImageCrop2(this, ImageUtils.getRoundedCornerBitmap(this, imageBest), false))
                        iv_face_c.setVisibility(View.GONE)
                        uploadCertPic(HT, getListParams(
                                CertParams(HT, imageBestData),
                                CertParams(HT_ENV, imageEnvData)
                        ))
                    } else if (resID == R.string.liveness_detection_failed_not_video) {
                        doPlay(R.raw.meglive_failed)
                    } else if (resID == R.string.liveness_detection_failed_timeout) {
                        doPlay(R.raw.meglive_failed)
                    } else if (resID == R.string.liveness_detection_failed) {
                        doPlay(R.raw.meglive_failed)
                    } else {
                        doPlay(R.raw.meglive_failed)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private lateinit var mMediaPlayer: MediaPlayer

    private fun doPlay(rawId: Int) {
        if (mMediaPlayer == null)
            mMediaPlayer = MediaPlayer()

        mMediaPlayer.reset()
        try {
            val localAssetFileDescriptor = resources
                    .openRawResourceFd(rawId)
            mMediaPlayer.setDataSource(
                    localAssetFileDescriptor.fileDescriptor,
                    localAssetFileDescriptor.startOffset,
                    localAssetFileDescriptor.length)
            mMediaPlayer.prepare()
            mMediaPlayer.start()
        } catch (localIOException: Exception) {
            localIOException.printStackTrace()
        }

    }

    /**
     * 测试当前摄像头能否被使用
     *
     * @return
     */
    private fun isCameraCanUse(): Boolean {
        var canUse = true
        var mCamera: Camera? = null
        try {
            mCamera = Camera.open()
            // setParameters 是针对魅族MX5 做的。MX5 通过Camera.open() 拿到的Camera
            // 对象不为null
            val mParameters = mCamera!!.parameters
            mCamera.parameters = mParameters
        } catch (e: Exception) {
            canUse = false
        }

        if (mCamera != null) {
            mCamera.release()
        }
        return canUse
    }

    companion object {
        private val INTO_IDCARDSCAN_PAGE = 100
        private val PAGE_INTO_LIVENESS = 101
        val ID_A = "ID_A"
        val ID_A_ALL = "ID_A_ALL"
        val ID_A_HEAD = "ID_A_HEAD"
        val ID_B = "ID_B"
        val ID_B_ALL = "ID_B_ALL"
        val HT = "HT"
        val HT_ENV = "HT_ENV"
    }

    class CertParams(var type: String, var pic: ByteArray)
    class Cert(var cert: String, var filename: String, var type: String)
}