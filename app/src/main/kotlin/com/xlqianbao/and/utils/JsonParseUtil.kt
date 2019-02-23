package com.xlqianbao.and.utils

import com.google.gson.GsonBuilder

/**
 * Created by gao on 2017/8/31.
 */
object JsonParseUtil {

    fun <T> json2DataBean(jsonStr: String?, classOfT: Class<T>): T {
        return GsonBuilder().create().fromJson<T>(jsonStr, classOfT)
    }

    fun dataBean2Json(obj: Any): String {
        return GsonBuilder().create().toJson(obj)
    }

}
