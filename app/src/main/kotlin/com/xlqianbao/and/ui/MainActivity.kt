package com.xlqianbao.and.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.twotiger.library.utils.*
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.home.HomeActivity
import com.xlqianbao.and.utils.CacheUtils
import com.xlqianbao.and.utils.JsonParseUtil
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 启动页
 */
class MainActivity : BaseActivity() {
    private var img_list: List<Model.ImageBean>? = null

    override fun initView() {
        setContentView(R.layout.activity_main)
    }

    override fun initData() {
        setPixelInsetTop(true, R.color.transparent)
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //获取常量数据
        getConstantData()
        //刷新用户信息
        if (getToken() != null)
            getUserInfo()
        //将上次缓存的img list取出来
        val imgListString = PreferencesUtils.getString(context, IMGLIST)
        val tempBean: Model.JsonListImageBean? = JsonParseUtil.json2DataBean(imgListString, Model.JsonListImageBean::class.java)
        img_list = tempBean?.list
        if (img_list != null && !StringUtils.isEmpty(img_list!![0].imgId)) {
            //比较已展示次数小于待展示次数，则展示图片
            if (PreferencesUtils.getInt(context, img_list!![0].imgId, 0) < img_list!![0].viewCount) {
                //将对应imgId命名的图片取出来
                val cacheBitmap = CacheUtils.getBitmapFormSDCard(img_list!![0].imgId)
                showPic(cacheBitmap)
                initlistener()
            } else {
                bootImageAnimation()
            }
        } else {
            bootImageAnimation()
        }
        requestImage()
    }

    private fun initlistener() {
        splash_image.setOnClickListener(View.OnClickListener { view ->
            if (ViewUtils.isFastDoubleClick(view)) return@OnClickListener
            if (ListUtils.isEmpty(img_list) || StringUtils.isEmpty(img_list!![0].url)) return@OnClickListener
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            intent.putExtra("NEED_TO_WEB_URL", img_list!![0].url)
            startNewActivity(intent, true)
        })
    }

    private fun requestImage() {
        if (!NetWorkUtil.isNetworkConnected(context)) return
        HttpMethods.instance
                .getImage(Constants.BOOT_PAGE_IMAGE,
                        object : ProgressSubscriber<List<Model.ImageBean>>(false) {
                            override fun onNext(pages: List<Model.ImageBean>) {
                                if (!ListUtils.isEmpty(pages)) {
                                    PreferencesUtils.putString(context, IMGLIST, JsonParseUtil.dataBean2Json(pages))
                                    downLoadPicAndSave(Constants.HOST + pages.get(0).picUrl, pages.get(0).imgId)
                                }
                            }
                        })
    }

    /**
     * 下载图片并以对应的imgId保存

     * @param url
     * *
     * @param imgId
     */
    private fun downLoadPicAndSave(url: String, imgId: String) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                        // 下载成功
                        CacheUtils.cacheBitmapToSDCard(resource!!, imgId)
                    }

                })
    }

    /**
     * 启动页动画
     */
    private fun bootImageAnimation() {
        //显示图片
        showPic(null)
        initlistener()
        //动画
        //        binding.splashSvg1.setVisibility(View.VISIBLE);
        //        binding.splashSvg2.setVisibility(View.VISIBLE);
        //        binding.splashImage.setBackgroundResource(R.drawable.splash_image);
        //        final SVGPathView pathView1 = binding.splashSvg1;
        //        final SVGPathView pathView2 = binding.splashSvg2;
        //        pathView1.setSvgPath(getResources().getString(R.string.path1));
        //        pathView2.setSvgPath(getResources().getString(R.string.logo));
        //        pathView1.start();
        //        pathView1.setOnStateChangeListener(new SVGPathView.SVGStateChangedListener() {
        //            @Override
        //            public void onStateChanged(int state) {
        //                if (state == SVGPathView.FINISHED) {
        //                    pathView2.start();
        //                }
        //            }
        //        });
        //        pathView2.setOnStateChangeListener(new SVGPathView.SVGStateChangedListener() {
        //            @Override
        //            public void onStateChanged(int state) {
        //                if (state == SVGPathView.FINISHED) {
        //                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        //                    intent.putExtra("PUSH", getIntent().getSerializableExtra("PUSH"));
        //                    startNewActivity(intent, R.anim.fade_in_anim, R.anim.fade_out_anim, true);
        //                }
        //            }
        //        });
    }

    /**
     * 展示图片

     * @param bitmap
     */
    private fun showPic(bitmap: Bitmap?) {
        //隐藏动态logo
        //        binding.llSvgLogo.setVisibility(View.GONE);
        //显示图片
        //        binding.splashImage.setVisibility(View.VISIBLE);
        val aa = AlphaAnimation(1f, 1f)
        aa.duration = 2000

        if (bitmap != null) {
            splash_image.setBackgroundDrawable(BitmapDrawable(bitmap))
        } else {
            splash_image.setBackgroundResource(R.drawable.splash_image)
        }
        splash_image.startAnimation(aa)
        aa.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                //已展示次数+1
                if (img_list != null && img_list!!.size > 0) {
                    var hasShowCount = PreferencesUtils.getInt(context, img_list!![0].imgId, 0)
                    PreferencesUtils.putInt(context, img_list!![0].imgId, ++hasShowCount)
                }
            }

            override fun onAnimationEnd(animation: Animation) {
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                intent.putExtra("PUSH", getIntent().getSerializableExtra("PUSH"))
                startNewActivity(intent, R.anim.fade_in_anim, R.anim.fade_out_anim, true)
            }

            override fun onAnimationRepeat(arg0: Animation) {

            }
        })
    }

    override fun onBackPressed() {
        //        super.onBackPressed();
    }

    companion object {
        var IMGLIST = "imageList"
    }
}
