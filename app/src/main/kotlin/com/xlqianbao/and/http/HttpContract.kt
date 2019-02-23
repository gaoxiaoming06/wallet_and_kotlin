package com.xlqianbao.and.http

import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.base.BaseHttpResult
import io.reactivex.Observable
import io.reactivex.Observer
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


/**
 * 网络请求的契约类
 * Created by Makise on 2017/2/4.
 */

interface HttpContract {
    interface Services {
        @FormUrlEncoded
        @POST(Constants.CONSTANT_QUERY)
        fun constantQuery(
                @FieldMap param: Map<String, String>,
                @Field("dataToken") dataToken: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.BORROW_INFO)
        fun borrowInfo(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.IMAGE)
        fun getImage(
                @FieldMap param: Map<String, String>,
                @Field("type") type: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.USER_INFO)
        fun userInfo(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.PRO_CONF)
        fun proConf(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.NOTICES)
        fun notices(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?,
                @Field("type") type: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.READ_NOTICE)
        fun readNotice(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?,
                @Field("nids") nids: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.APPLY)
        fun apply(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?,
                @Field("amount") amount: String,
                @Field("days") days: Int
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.HAS_NOTICE)
        fun hasNotice(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.SHARE)
        fun share(
                @FieldMap param: Map<String, String>,
                @Field("version") version: String,
                @Field("token") token: String?,
                @Field("type") type: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.ORDER_REPAY)
        fun orderRepay(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?,
                @Field("orderId") orderId: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.QUERY_CARD)
        fun queryCard(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?
        ): Observable<BaseHttpResult>


        @FormUrlEncoded
        @POST(Constants.CHECK_PHONE)
        fun checkPhone(
                @FieldMap param: Map<String, String>,
                @Field("phone") phone: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.SEND_VALID_CODE)
        fun sendValidCode(
                @FieldMap param: Map<String, String>,
                @Field("vticket") vticket: String?,
                @Field("type") type: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.LOGIN)
        fun login(
                @FieldMap param: Map<String, String>,
                @Field("vticket") vticket: String?,
                @Field("code") code: String?,
                @Field("device") device: String?,
                @Field("jpushId") jpushId: String?,
                @Field("channel") channel: String?
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.UPDATE)
        fun update(
                @FieldMap param: Map<String, String>,
                @Field("version") version: String,
                @Field("packageName") packageName: String,
                @Field("channel") channel: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.CONTACT_UPLOAD)
        fun contactUpload(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("data") data: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.GET_AUTH_ID)
        fun getAuthId(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String?,
                @Field("deviceInfo") deviceInfo: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.CERT_PICS)
        fun certPics(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.CERT_CONFIRM)
        fun certConfirm(
                @FieldMap param: Map<String, String>,
                @Field("version") version: String,
                @Field("token") token: String,
                @Field("delta") delta: String,
                @Field("ucid1") ucid1: String,
                @Field("ucid2") ucid2: String,
                @Field("ucid3") ucid3: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.CERT_PIC)
        fun certPic(
                @FieldMap param: Map<String, String>,
                @Field("version") version: String,
                @Field("token") token: String,
                @Field("list") list: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.NOTICE_LIST)
        fun noticeList(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("pno") pno: Int,
                @Field("psize") psize: Int,
                @Field("timestamp") timestamp: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.TYPES)
        fun types(
                @FieldMap param: Map<String, String>,
                @Field("nids") nids: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.REGION)
        fun region(
                @FieldMap param: Map<String, String>,
                @Field("parent") parent: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.QUERY_LIVE_INFO)
        fun queryLiveInfo(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.SET_LIVE_INFO)
        fun setLiveInfo(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("prov") prov: String,
                @Field("city") city: String,
                @Field("dist") dist: String,
                @Field("addr") addr: String,
                @Field("time") time: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.QUERY_WORK_INFO)
        fun queryWorkInfo(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.QUERY_CONTACTS)
        fun queryContacts(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.SET_WORK_INFO)
        fun setWorkInfo(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("profession") profession: String,
                @Field("monthIn") monthIn: String,
                @Field("companyName") companyName: String,
                @Field("companyProvince") companyProvince: String,
                @Field("companyCity") companyCity: String,
                @Field("companyDistrict") companyDistrict: String,
                @Field("companyTelephone") companyTelephone: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.SET_CONTACTS)
        fun setContacts(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("rName") rName: String,
                @Field("rPhone") rPhone: String,
                @Field("sName") sName: String,
                @Field("sPhone") sPhone: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.REPAY)
        fun repay(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("orderId") orderId: String,
                @Field("code") code: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.PRE_VALID_CARD)
        fun preValidCard(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("name") name: String,
                @Field("idCard") idCard: String,
                @Field("number") number: String,
                @Field("phone") phone: String
        ): Observable<BaseHttpResult>

        @FormUrlEncoded
        @POST(Constants.BIND_CARD)
        fun bindCard(
                @FieldMap param: Map<String, String>,
                @Field("token") token: String,
                @Field("vticket") vticket: String,
                @Field("code") code: String
        ): Observable<BaseHttpResult>

    }

    interface Methods {
        fun constantQuery(dataToken: String, subscriber: Observer<Model.ConstantQuery>)
        fun borrowInfo(token: String?, subscriber: Observer<Model.BorrowInfo>)
        fun banner(type: String, subscriber: Observer<List<Model.Banner>>)
        fun getImage(type: String, subscriber: Observer<List<Model.ImageBean>>)
        fun userInfo(token: String?, subscriber: Observer<Model.User>)
        fun proConf(token: String?, subscriber: Observer<Model.ProConf>)
        fun notices(token: String?, type: String, subscriber: Observer<Model.Notices>)
        fun readNotice(token: String?, nids: String, subscriber: Observer<String>)
        fun apply(token: String?, amount: String, days: Int, subscriber: Observer<String>)
        fun hasNotice(token: String?, subscriber: Observer<Model.HasNotice>)
        fun share(version: String, token: String?, type: String, subscriber: Observer<Model.ShareData>)
        fun orderRepay(token: String?, orderId: String?, subscriber: Observer<Model.OrderRepay>)
        fun queryCard(token: String?, subscriber: Observer<Model.QueryCard>)
        fun checkPhone(phone: String, subscriber: Observer<Model.CheckPhone>)
        fun sendValidCode(vticket: String?, type: String?, subscriber: Observer<String>)
        fun login(vticket: String?, code: String?, device: String?, jpushId: String?, channel: String?, subscriber: Observer<Model.User>)
        fun update(subscriber: Observer<Model.Update>)
        fun contactUpload(token: String, data: String, subscriber: Observer<String>)
        fun getAuthId(token: String?, deviceInfo: String, subscriber: Observer<Model.Auth>)
        fun certPics(token: String, subscriber: Observer<Model.CertPics>)
        fun certConfirm(token: String, delta: String, ucid1: String, ucid2: String, ucid3: String, subscriber: Observer<String>)
        fun certPic(token: String, list: String, subscriber: Observer<Model.CertPic>)
        fun types(nids: String, subscriber: Observer<Model.AllTypesList>)
        fun region(parent: String, subscriber: Observer<List<Model.Regions>>)
        fun queryLiveInfo(token: String, subscriber: Observer<Model.QueryLiveInfo>)
        fun setLiveInfo(token: String, prov: String, city: String, dist: String, addr: String, time: String, subscriber: Observer<String>)

        fun noticeList(token: String, pno: Int, psize: Int, timestamp: String, subscriber: Observer<Model.MyNoticeList>)
        fun queryWorkInfo(token: String, subscriber: Observer<Model.QueryWorkInfo>)
        fun queryContacts(token: String, subscriber: Observer<List<Model.QueryContacts>>)
        fun setWorkInfo(token: String, profession: String, monthIn: String, companyName: String, companyProvince: String, companyCity: String, companyDistrict: String, companyTelephone: String, subscriber: Observer<String>)

        fun setContacts(token: String, rName: String, rPhone: String, sName: String, sPhone: String, subscriber: Observer<String>)
        fun repay(token: String, orderId: String, code: String, subscriber: Observer<String>)
        fun preValidCard(token: String, name: String, idCard: String, number: String, phone: String, subscriber: Observer<Model.PreValidCard>)
        fun bindCard(token: String, vticket: String, code: String, subscriber: Observer<String>)
    }
}
