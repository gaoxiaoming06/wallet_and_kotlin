package com.xlqianbao.and.base

/**

 * Created by Makise on 2017/2/4.
 */

data class BaseHttpResult(
        //用来模仿resultCode和resultMessage
        var sign: String,
        var codeDesc: String,
        var nonceStr: String,
        var code: String,

        //用来模仿Data
        var data: String?
)
