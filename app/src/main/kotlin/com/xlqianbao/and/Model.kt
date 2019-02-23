package com.xlqianbao.and

import android.graphics.Bitmap
import java.io.Serializable
import java.math.BigDecimal
import com.xlqianbao.and.Model.MyNotice




/**
 * Created by Makise on 2017/5/22.
 */
class Model {
    data class ConstantData(
            //常量key值
            var key: String,
            //名称
            var name: String,
            //值
            var `val`: String,
            //描述
            var desc: String,
            //版本号
            var ver: String,
            //是否需要提醒用户
            var notice: String
    )

    data class ConstantQuery(
            //版本标识
            var dataToken: String,
            //数据集
            var list: List<ConstantData>
    )

    data class ImageBean(
            //活动名称
            var name: String,
            //图片地址
            var picUrl: String,
            //链接地址  为空不可点击
            var url: String,
            //展示次数
            var viewCount: Int,
            //标识
            var imgId: String
    )

    data class HomeTabMessage(var witchTab: Int)
    data class User(
            var id: Long = 0,
            //昵称
            var nickname: String? = null,
            //姓名
            var name: String? = null,
            //身份证号
            var idCard: String? = null,
            //唯一标识
            var token: String? = null,
            //是否设置交易密码
            var setPayPwd: Boolean = false,
            //是否实名认证
            var realName: Boolean = false,
            //头像url
            var avatarUrl: String? = null,
            //手机号
            var phone: String? = null,

            /**
             * 各种认证标识 1是 0否
             */
            var IDAF: String? = null,
            var PhoneAF: String? = null,
            var LiveInfoAF: String? = null,
            var WorkInfoAF: String? = null,
            var ContactInfoAF: String? = null,
            var BCAF: String? = null,
            var WeiXinAF: String? = null,
            var AliPayAF: String? = null,
            var ZmxyAF: String? = null,
            var JDAF: String? = null,
            var TelBookAF: String? = null
    )

    data class BorrowInfo(var orderId: String,
                          var amount: String,
                          var days: String,
                          var loanFee: String,
                          var loanTime: String,
                          var expectRepayAmt: String,
                          var expectRepayTime: String,
                          var overdueDays: String,
                          var overdueAmt: String,
                          var overdueFee: String,
                          var overdueFlag: String,
                          var status: String,
                          var statusName: String,
                          var tips: String,
            //是否有未读消息
                          var hasUnRead: String)

    data class Banner(
            /**
             * 唯一值
             */
            var bannerId: String = "",
            /**
             * 名称
             */
            var name: String = "",
            /**
             * 图片地址
             */
            var picUrl: String = "",
            /**
             * 点击地址
             */
            var url: String = ""
    )

    data class JsonListBanner(
            var list: List<Banner>
    )

    data class JsonListImageBean(
            var list: List<ImageBean>
    )

    data class ProConf(
            var rate: BigDecimal? = null,
            var min: Int = 0,
            var max: Int = 0,
            var valid: Int = 0,
            var tips: String? = null,
            var days: IntArray? = null
    )

    data class Notices(
            var id: Long = 0,
            var title: String? = null,
            var content: String? = null
    )

    data class HasNotice(var hasUnRead: Int = 0)

    class ShareData : Serializable {
        var shareUrl: String? = null
        var shareTitle: String? = null
        var shareDesc: String? = null
        var sharePic: String? = null
        var shareSmsBody: String? = null
        var mBitmap: Bitmap? = null
        var mBitmapByte: ByteArray? = null

        fun setBitmap(mBitmapByte: ByteArray): ShareData {
            this.mBitmapByte = mBitmapByte
            return this
        }
    }

    class OrderRepay : Serializable {
        var orderId: String? = null
        var amount: BigDecimal? = null
        var bankName: String? = null
        var bankCode: String? = null
    }

    data class LoginMessage(var isNewUser: Boolean, var user: User)

    class QueryCard : Serializable {
        var id: Long = 0
        var number: String? = null
        var name: String? = null
        var code: String? = null
    }

    data class CheckPhone(var vticket: String, var mark: Int)
    data class RegLoginMessage(var phone: CheckPhone, var phone_number: String) : Serializable
    data class Update(var ver: String, var url: String)
    data class ContactInfo(var name: String, var mobile: String)
    data class JPushBean(var name: String, var type: String, var target: String, var style: String) : Serializable
    data class Auth (var AuthID: String)
    data class CertPics (
        var IDAPath: String? = null,
        var IDBPath: String? = null,
        var HTPath: String? = null
    )
    data class CertPic (
        var ucid: String
    )

    data class AllTypesList (
        var live_time: List<Types>? = null,
        var profession: List<Types>? = null,
        var monthIn: List<Types>? = null,
        var qinshu: List<Types>? = null,
        var shehui: List<Types>? = null,
        var city: List<Regions>? = null,
        var workInfo: QueryWorkInfo? = null,
        var contactses: List<QueryContacts>? = null
    ):Serializable

    data class Types (
        var nid: String? = null,
        var name: String? = null
    ):Serializable

    data class RegionsList(var regions: List<Regions>)

    data class Regions (
        var id: String? = null,
        var name: String? = null
    ):Serializable

    data class QueryWorkInfo (
        var profession: String? = null,
        var monthIn: String? = null,
        var companyName: String? = null,
        var companyProvince: String? = null,
        var companyCity: String? = null,
        var companyDistrict: String? = null,
        var companyTelephone: String? = null
    )

    data class QueryContactsList(var contacts: List<QueryContacts>)

    data class QueryContacts (
        var relationshipType: String? = null,
        var relationship: String? = null,
        var phone: String? = null
    )

    data class QueryLiveInfo (
        var prov: String? = null,
        var city: String? = null,
        var dist: String? = null,
        var addr: String? = null,
        var time: String? = null
    )

    data class ChooseAddrMessage(var addr: String) {

        val prov: String
            get() = addr.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        val city: String
            get() = addr.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

        val dist: String
            get() = addr.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
    }

    class MyNotice {
        var id: Long = 0
        //"IMG":图片类型;"TEXT":文字类型;"MIX":混合类型
        var ctype: String? = null
        var title: String? = null
        var text: String? = null
        var img: String? = null
        var vtime: Long = 0
        var clickUrl: String? = null
        //已读标识
        var hasRead: String? = null
    }

    class MyNoticeList {
        var pno: Int = 0
        var psize: Int = 0
        var timestamp: Long = 0
        var list: List<MyNotice>? = null
    }

    data class PreValidCard (
        var vticket: String? = null
    )

}