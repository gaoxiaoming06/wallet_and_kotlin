package com.xlqianbao.and.wxapi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.tencent.connect.share.QQShare
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.tendcloud.tenddata.it
import com.twotiger.library.widget.contactselector.ContactsActivity
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.utils.WXShareUtils
import com.xlqianbao.and.utils.rx.RxBus
import com.xlqianbao.and.utils.rx.RxSubscriptions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_wx_entry.*

class WXEntryActivity : Activity(), IWXAPIEventHandler, View.OnClickListener {
    // IWXAPI 是第三方app和微信通信的openapi接口
    private var api: IWXAPI? = null
    //qq分享
    private var mTencent: Tencent? = null
    private var shareToQQListener: IUiListener? = null
    private var data: Model.ShareData? = null
    lateinit private var mRxSubSticky: Disposable

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wx_entry)

        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID, true)

        // 将该app注册到微信
        api!!.registerApp(Constants.WX_APP_ID)

        //初始化qq分享
        mTencent = Tencent.createInstance("1105987579", applicationContext)
        //初始化监听
        shareToQQListener = object : IUiListener {
            override fun onComplete(o: Any) {
                Toast.makeText(this@WXEntryActivity, "分享成功", Toast.LENGTH_SHORT).show()
            }

            override fun onError(uiError: UiError) {
                Toast.makeText(this@WXEntryActivity, "uiError:" + uiError.errorMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {

            }
        }

        //注册点击事件
        toSession.setOnClickListener(this)
        toTimeLine.setOnClickListener(this)
        toQQShare.setOnClickListener(this)
        toSmsShare.setOnClickListener(this)
        cancel.setOnClickListener(this)

        //取分享数据
        data = intent.getSerializableExtra(SHARE_DATA) as Model.ShareData

        //判断是否启动成功
        //        Toast.makeText(WXEntryActivity.this, "launch result = " + api.openWXApp(), Toast.LENGTH_LONG).show()

        //判断版本
        //        int wxSdkVersion = api.getWXAppSupportAPI()
        //        if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
        //            Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline supported", Toast.LENGTH_LONG).show()
        //        } else {
        //            Toast.makeText(WXEntryActivity.this, "wxSdkVersion = " + Integer.toHexString(wxSdkVersion) + "\ntimeline not supported", Toast.LENGTH_LONG).show()
        //        }

        api!!.handleIntent(intent, this)

        startAnim(false)
    }

    /**
     * 位移动画
     *
     * @param isExit true 为退出动画 false为进入
     */
    private fun startAnim(isExit: Boolean) {
        val translateAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, if (isExit) 0f else 1f,
                Animation.RELATIVE_TO_SELF, if (isExit) 1f else 0f
        )
        translateAnimation.fillAfter = true
        translateAnimation.duration = 300
        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                ll_main.setVisibility(View.VISIBLE)
                if (isExit) {
                    finish()
                    overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        ll_main.startAnimation(translateAnimation)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        api!!.handleIntent(intent, this)
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    override fun onReq(req: BaseReq) {
        //        switch (req.getType()) {
        //            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
        //                goToGetMsg();
        //                break;
        //            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
        //                goToShowMsg((ShowMessageFromWX.Req) req);
        //                break;
        //            default:
        //                break;
        //        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    override fun onResp(resp: BaseResp) {
        when (resp.errCode) {
            BaseResp.ErrCode.ERR_OK -> Toast.makeText(this, "分享成功", Toast.LENGTH_SHORT).show()
        }//            case BaseResp.ErrCode.ERR_USER_CANCEL:
        //                result = "取消分享";
        //                break;
        //            case BaseResp.ErrCode.ERR_AUTH_DENIED:
        //                result = "分享被拒绝";
        //                break;
        //            default:
        //                result = "分享返回";
        //                break;
        finish()
        overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
    }

    fun share(isTimeline: Boolean) {
        if (!api!!.isWXAppInstalled) {
            Toast.makeText(this, "您还未安装微信客户端", Toast.LENGTH_SHORT).show()
            return
        }
        val webpage = WXWebpageObject()
        webpage.webpageUrl = data!!.shareUrl
        val msg = WXMediaMessage(webpage)
        msg.title = data!!.shareTitle
        msg.description = data!!.shareDesc
        val thumb: Bitmap
        if (data!!.mBitmapByte != null) {
            data!!.mBitmap = BitmapFactory.decodeByteArray(data!!.mBitmapByte, 0, data!!.mBitmapByte!!.size)
            if (data!!.mBitmap != null && !data!!.mBitmap!!.isRecycled()) {
                thumb = data!!.mBitmap!!
            } else {
                thumb = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
            }
        } else {
            thumb = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
        }
        msg.thumbData = WXShareUtils.bmpToByteArray(thumb, true)

        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction("webpage")
        req.message = msg
        req.scene = if (isTimeline) SendMessageToWX.Req.WXSceneTimeline else SendMessageToWX.Req.WXSceneSession
        api!!.sendReq(req)
    }

    private fun buildTransaction(type: String?): String {
        return if (type == null) System.currentTimeMillis().toString() else type + System.currentTimeMillis()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.toSession -> share(false)
            R.id.toTimeLine -> share(true)
            R.id.toQQShare -> {
                //分享到qq
                val params = Bundle()
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                params.putString(QQShare.SHARE_TO_QQ_TITLE, data!!.shareTitle)
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, data!!.shareDesc)
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, data!!.shareUrl)
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, data!!.sharePic)
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME, resources.getString(R.string.app_name))
                //                        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  "其他附加功能");
                mTencent!!.shareToQQ(this, params, shareToQQListener)
            }
            R.id.toSmsShare -> {
                //分享到短信
                startActivity(Intent(this, ContactsActivity::class.java))
                mRxSubSticky = RxBus.default
                        .toObservable(String::class.java)
                        .subscribe({
                            if (it.startsWith("number:")) {
                                //拿到号码 调取短信发送
                                startActivity(Intent(Intent.ACTION_SENDTO)
                                        .setData(Uri.parse("smsto:" + it.substring(7, it.length)))
                                        .putExtra("sms_body", data!!.shareSmsBody))
                            }
                            RxSubscriptions.remove(mRxSubSticky)
                        })
                RxSubscriptions.add(mRxSubSticky)
            }
        }
        startAnim(true)
    }

    override fun onBackPressed() {
        //        super.onBackPressed();
        startAnim(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (mTencent != null)
            Tencent.onActivityResultData(requestCode, resultCode, data, shareToQQListener)
    }

    companion object {

        val SHARE_DATA = "SHARE_DATA"

        private val TIMELINE_SUPPORTED_VERSION = 0x21020001
    }
}