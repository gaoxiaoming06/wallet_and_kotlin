package com.xlqianbao.and

/**
 * 常量
 * Created by Makise on 2017/2/4.
 */

object Constants {
    //传递给提取UA方法使用的appName标识字段
    const val APP_NAME = "xlqianbao"
    //指向buil.gradle文件，debug包走测试地址，release包走线上地址
    var HOST = BuildConfig.HOST

    //activity_wx_entry
    const val WX_APP_ID = "wx664e73f133e48455"
    //appkey
    const val Appkey = "042460293a7f991d9c2c6c175d190d33"

    /**
     * 接口
     */
    //手机号检测
    const val CHECK_PHONE = "app/checkPhone"
    //发送验证码
    const val SEND_VALID_CODE = "app/sendValidCode"
    //登陆
    const val LOGIN = "app/login"
    //获取用户信息
    const val USER_INFO = "app/userInfo"
    //图片接口
    const val IMAGE = "app/image"
    //图片接口Type参数
    const val BOOT_PAGE_IMAGE = "BOOTPAGE"
    const val GUIDE_PAGE_IMAGE = "GUIDEPAGE"
    const val POPUP_IMAGE = "POPUP"
    //常量接口
    const val CONSTANT_QUERY = "app/constantQuery"
    //升级检测接口
    const val UPDATE = "app/update"
    //借款信息
    const val BORROW_INFO = "app/borrowInfo"
    //获取用户银行卡
    const val QUERY_CARD = "app/queryCard"
    //费率
    const val RATE = "app/rate"
    //借款申请
    const val APPLY = "app/apply"
    //创建还款订单
    const val ORDER_REPAY = "app/orderRepay"
    //充值
    const val REPAY = "app/repay"
    //校验四要素(姓名\身份证\卡号\手机号)
    const val PRE_VALID_CARD = "app/preValidCard"
    //绑定银行卡
    const val BIND_CARD = "app/bindCard"
    //查询现居城市 list
    const val REGION = "app/region"
    //查询各种常量
    const val TYPES = "app/types"
    //借款列表
    const val BORROW_LIST = "app/borrowList"
    //消息
    const val NOTICES = "app/notices"
    //消息已读
    const val READ_NOTICE = "app/readNotice"
    //居住信息
    const val SET_LIVE_INFO = "app/setLiveInfo"
    //工作信息
    const val SET_WORK_INFO = "app/setWorkInfo"
    //紧急联系人
    const val SET_CONTACTS = "app/setContacts"
    //查询居住信息
    const val QUERY_LIVE_INFO = "app/queryLiveInfo"
    //查询工作信息
    const val QUERY_WORK_INFO = "app/queryWorkInfo"
    //查询紧急联系人
    const val QUERY_CONTACTS = "app/queryContacts"
    //上传证件信息
    const val CERT_PIC = "app/certPic"
    //认证身份信息
    const val CERT_CONFIRM = "app/certConfirm"
    //分享
    const val SHARE = "app/share"
    //证件查询
    const val CERT_PICS = "app/certPics"
    //获取授权id
    const val GET_AUTH_ID = "app/getAuthId"
    //消息列表
    const val NOTICE_LIST = "app/noticeList"
    //通讯录上传
    const val CONTACT_UPLOAD = "app/contactUpload"
    //产品配置
    const val PRO_CONF = "app/proConf"
    //有无未读消息
    const val HAS_NOTICE = "app/hasNotice"


    //APP审核状态查询
    const val AUDIT_STATE_QUERY = "app/auditStateQuery"
    //获取地理位置
    const val GET_LOCATION = "app/getLocation"
    //获取贷款超市产品列表
    const val PROMOTE_PRODUCT_QUERY = "app/promoteProductQuery"
    //获取贷款超市推荐产品列表
    const val RECOM_PROMOTE_PRODUCT_QUERY = "app/recomPromoteProductQuery"

    /**
     * 状态码
     */
    const val OK = "OK"
    const val CAPTCHA_ERROR = "CAPTCHA_ERROR"
    const val SYSTEM_UPGRADE = "SYSTEM_UPGRADE"
    const val REPAY_STATUS_LIMIT = "REPAY_STATUS_LIMIT"
    const val TOKEN_INVALID = "TOKEN_INVALID"
    const val TOKEN_TIMEOUT = "TOKEN_TIMEOUT"
    const val USER_ERROR = "USER_ERROR"

    /**
     * 常量
     */
    const val ZZ_SJH = "zz_sjh"
    const val DZ_BANKLOGO = "dz_bankLogo"
    const val CL_WXFWH = "cl_wxfwh"
    const val DZ_PHONE_AF = "dz_phoneAF"
    const val DZ_ZCXY = "dz_zcxy"
    const val DZ_AUTH_ZM = "dz_authZm"
    const val DZ_TXLSQXY = "dz_txlsqxy"
    const val DZ_XXSJJSYGZ = "dz_xxsjjsygz"
    const val DZ_GYWM = "dz_gywm"
    const val DZ_CJWT = "dz_cjwt"

    /**
     * SP Key
     */
    //version
    const val SP_VERSION_NAME = "SP_VERSION_NAME"
    //user_token
    const val USER_TOKEN = "USER_TOKEN"
    //constantData
    const val CONSTANT_DATA = "CONSTANT_DATA"
    //location
    const val SP_LOCATION = "SP_LOCATION"
    //inHands
    const val SP_IN_HANDS = "SP_IN_HANDS"
    //authId
    const val SP_AUTH_ID = "SP_AUTH_ID"
    //SP_AUDIT_STATE
    const val SP_AUDIT_STATE = "SP_AUDIT_STATE"

    /**
     * webview约定参数
     */
    const val WEBVIEW_SHARE = "_share_"
    const val WEBVIEW_LINK = "_link_"
    const val WEBVIEW_FULLSCREEN = "_fullscreen_"
    const val WEBVIEW_HIDE_TITLE_BAR = "_hidetitle_"
}
