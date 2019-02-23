package com.xlqianbao.and.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.net.http.SslError
import android.text.TextUtils
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.twotiger.library.utils.ScreenUtils
import com.twotiger.library.utils.URLUtil
import com.xlqianbao.and.Constants
import com.xlqianbao.and.JavaScriptInterface
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.http.subscribers.ProgressDialogHandler
import com.xlqianbao.and.utils.WebViewCommonSet
import kotlinx.android.synthetic.main.activity_webview_pager.*
import java.io.UnsupportedEncodingException
import java.util.*


/**
 * webview
 * Created by Makise on 2017/2/6.
 */

class WebViewPageActivity : BaseActivity() {
    private var jsInterface: JavaScriptInterface? = null
    private var url_load: String = ""
    private var mProgressDialogHandler: ProgressDialogHandler? = null

    override fun initView() {
        setContentView(R.layout.activity_webview_pager)
        //避免键盘遮挡输入框
        window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    @SuppressLint("JavascriptInterface")
    override fun initData() {
        webview.setWebViewClient(MyWebViewClient())
        webview.setWebChromeClient(MyWebChromeClient())

        url_load = intent.getStringExtra("url")
//        if (StringUtils.isEmpty(url_load))
//            url_load = ""
        loadUrl(url_load, getExtraHeaders())
        checkParams(url_load)

        //开启js
        WebViewCommonSet.setWebview(this@WebViewPageActivity, webview, true)

        //js交互
        jsInterface = JavaScriptInterface(webview, this@WebViewPageActivity)
        webview.addJavascriptInterface(jsInterface, JavaScriptInterface.name())

    }

    /**
     * 检查连接参数(参数控制web显示效果)
     * @param url
     */
    private fun checkParams(url: String?) {
        //检查链接包含参数
        if (url != null) {
            try {
                val isShowShare = URLUtil.getParam(url, Constants.WEBVIEW_SHARE, "no")
                if ("yes" == isShowShare) {
                    //TODO 分享
                } else {
                    //无分享 再去判断是否有link（右上显示文案）
                    val isLink = URLUtil.getParam(url, Constants.WEBVIEW_LINK, "")
                    if (!TextUtils.isEmpty(isLink)) {
                        //TODO 右上显示文案
                    }
                }
                //是否通屏显示
                val isFullScreen = URLUtil.getParam(url, Constants.WEBVIEW_FULLSCREEN, "no")
                if ("yes" == isFullScreen) {
                    //需要通屏显示
                    //                    setPixelInsetTop(true, Color.TRANSPARENT);
                    //去掉padding 为了让titlebar和webview覆盖
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    params.setMargins(0, 0, 0, 0)
                    webview.layoutParams = params
                    title_bar.setBackgroundColor(Color.TRANSPARENT)
                    title_padding.setBackgroundColor(Color.TRANSPARENT)
                    title_padding.setPadding(0, ScreenUtils.dpToPxInt(context, 25f), 0, 0)
                    //正常头部效果
                    //                    binding.navBack.setImageResource(R.drawable.nav_back_blue);
                    //                    binding.navClose.setImageResource(R.drawable.nav_close_blue);
                    //设置webView头部效果
//                    setWebViewTopEffect()
                } else {
                    //不需要通屏显示
                    //恢复webview的padding
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    params.setMargins(0, ScreenUtils.dpToPxInt(this, 44f), 0, 0)
                    webview.layoutParams = params
                    title_padding.setPadding(0, 0, 0, 0)
                    //                    setPixelInsetTop(false, R.color.xl_blue);
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }
    }

//    fun setWebViewTopEffect() {
//        binding!!.divider.visibility = View.GONE
//        binding!!.navTitle.setTextColor(Color.TRANSPARENT)
//        binding!!.webview.onScrollChangedCallback = ObservableWebView.OnScrollChangedCallback { x, y ->
//            if (y <= 0) {   //设置标题的背景颜色
//                //                    binding.navBack.setImageResource(R.drawable.nav_back_blue);
//                //                    binding.navClose.setImageResource(R.drawable.nav_close_blue);
//                binding!!.divider.visibility = View.GONE
//                binding!!.titlePadding.setBackgroundColor(Color.TRANSPARENT)
//                binding!!.titleBar.setBackgroundColor(Color.TRANSPARENT)
//                binding!!.navTitle.setTextColor(Color.TRANSPARENT)
//                //                    setPixelInsetTop(true, Color.TRANSPARENT);
//                binding!!.titlePadding.setPadding(0, ScreenUtils.dpToPxInt(context, 25f), 0, 0)
//                //                    setStatusBarBlack(false);
//            } else if (y > 0 && y <= 200) { //滑动距离小于banner图的高度时，设置背景和字体颜色颜色透明度渐变
//                val scale = y.toFloat() / 200
//                val alpha = 255 * scale
//                binding!!.divider.visibility = View.VISIBLE
//                binding!!.divider.setBackgroundColor(Color.argb(alpha.toInt(), 227, 227, 227))
//                binding!!.navTitle.setTextColor(Color.argb(alpha.toInt(), 85, 85, 85))
//                binding!!.titlePadding.setBackgroundColor(Color.argb(alpha.toInt(), 246, 246, 246))
//                //                    binding.navBack.setImageResource(R.drawable.nav_back_blue);
//                //                    binding.navClose.setImageResource(R.drawable.nav_close_blue);
//                binding!!.titlePadding.setPadding(0, ScreenUtils.dpToPxInt(context, 25f), 0, 0)
//                //                    setPixelInsetTop(true, Color.TRANSPARENT);
//                //                    setStatusBarBlack(false);
//            } else {    //滑动到banner下面设置普通颜色
//                binding!!.divider.visibility = View.VISIBLE
//                //                    binding.titlePadding.setBackgroundColor(getResources().getColor(R.color.xl_blue));
//                //                    binding.navTitle.setTextColor(getResources().getColor(R.color.xl_white));
//                //                    binding.navBack.setImageResource(R.drawable.nav_back);
//                //                    binding.navClose.setImageResource(R.drawable.icon_close_white);
//                binding!!.titlePadding.setPadding(0, 0, 0, 0)
//                //                    setPixelInsetTop(false, R.color.blue);
//                //                    setStatusBarBlack(true);
//            }
//        }
//    }

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
//        if (webview != null) {
            webview.loadUrl(url, additionalHttpHeaders)
//        }
    }

    private fun getExtraHeaders(): Map<String, String> {
        val extraHeaders = HashMap<String, String>()
        if (!TextUtils.isEmpty(getToken())) {
            extraHeaders.put("Token", getToken()!!)
        } else {
            extraHeaders.put("Token", "")
        }
        return extraHeaders
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            super.onReceivedSslError(view, handler, error)
            //接受所有证书
            handler.proceed()
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
//            super.onPageStarted(view, url, favicon)
            if (mProgressDialogHandler == null)
                mProgressDialogHandler = ProgressDialogHandler(this@WebViewPageActivity, true)
            mProgressDialogHandler!!.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget()
        }

        override fun onPageFinished(view: WebView, url: String) {
//            super.onPageFinished(view, url)
            //隐藏loading
            if (mProgressDialogHandler != null) {
                mProgressDialogHandler!!.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            super.onReceivedError(view, errorCode, description, failingUrl)
        }
    }

    internal inner class MyWebChromeClient : WebChromeClient() {

        override fun onReceivedTitle(view: WebView, t: String) {
            super.onReceivedTitle(view, t)
        }

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
        }
    }

    override fun onBackPressed() {
        if (webview.canGoBack()) {
            webview.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        //onresume回来刷新页面
        loadUrl(url_load, getExtraHeaders())
    }
}
