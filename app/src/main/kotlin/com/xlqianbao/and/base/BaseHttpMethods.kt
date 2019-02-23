package com.xlqianbao.and.base

import android.text.TextUtils
import com.twotiger.library.utils.LogUtil
import com.twotiger.library.utils.PreferencesUtils
import com.twotiger.library.utils.Signature
import com.twotiger.library.utils.Tools
import com.xlqianbao.and.App
import com.xlqianbao.and.BuildConfig
import com.xlqianbao.and.Constants
import com.xlqianbao.and.http.HttpContract
import com.xlqianbao.and.http.ResultException
import com.xlqianbao.and.utils.AuthUtils
import com.xlqianbao.and.utils.LocationUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 网络请求基类
 * Created by Makise on 2017/2/4.
 */

open class BaseHttpMethods protected constructor() {
    protected var mService: HttpContract.Services
    private val retrofit: Retrofit

    init {
        //手动创建一个OkHttpClient并设置超时时间
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)

        //debug包打印log
        if (BuildConfig.DEBUG)
            httpClientBuilder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))

        //设置统一的User-Agent
        httpClientBuilder.interceptors().add(Interceptor { chain ->
            val request = chain.request()
                    .newBuilder()
                    .addHeader("User-Agent", Tools.getUserAgent(BaseActivity.context, Constants.APP_NAME))
                    .addHeader("AuthId", AuthUtils.authId)
                    .addHeader("HasLocation", if (LocationUtils.isLocationEnabled() && LocationUtils.isGpsEnabled()) "YES" else "NO")
                    .addHeader("Location", PreferencesUtils.getString(App.instance, Constants.SP_LOCATION, "0"))
                    .addHeader("Inhands", PreferencesUtils.getString(App.instance, Constants.SP_IN_HANDS, "NO"))
                    .build()
            chain.proceed(request)
        })

        retrofit = Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(Constants.HOST)
                .build()

        mService = retrofit.create<HttpContract.Services>(HttpContract.Services::class.java)
    }

    //添加线程管理并订阅
    protected fun <T> toSubscribe(o: Observable<T>, s: Observer<T>) {
        o.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                //防手抖
                .throttleFirst(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s)
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     *
     * Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    class HttpResultFunc : Function<BaseHttpResult, String> {
        override fun apply(baseHttpResult: BaseHttpResult): String {
            if (!checkSign(baseHttpResult)) return ""
            val code = baseHttpResult.code
            if (code != Constants.OK) {
                throw ResultException(baseHttpResult)
            }
            return if (baseHttpResult.data == null) "" else baseHttpResult.data!!
        }

        @Synchronized
        fun checkSign(result: BaseHttpResult): Boolean {
            if (TextUtils.isEmpty(result.sign) || TextUtils.isEmpty(result.nonceStr)) {
                LogUtil.info("API返回的数据签名数据不存在，有可能被第三方篡改!!!")
                return false
            }
            val map = HashMap<String, String>()
            map.put("codeDesc", result.codeDesc)
            map.put("nonceStr", result.nonceStr)
            map.put("code", result.code)
            if (result.data != null) {
                result.data?.let { map.put("data", it) }
            }
            val sign = Signature.getSign(map, Constants.Appkey)
            if (sign != result.sign) {
                //签名验不过，表示这个API返回的数据有可能已经被篡改了
                LogUtil.info("API返回的数据签名验证不通过，有可能被第三方篡改!!!")
                return false
            }
            LogUtil.info("恭喜，API返回的数据签名验证通过!!!")
            return true
        }
    }

    companion object {
        private val DEFAULT_TIMEOUT = 15
    }
}
