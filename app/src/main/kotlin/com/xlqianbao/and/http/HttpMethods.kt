package com.xlqianbao.and.http

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.twotiger.library.utils.RandomUtils
import com.twotiger.library.utils.Signature
import com.twotiger.library.utils.Tools
import com.xlqianbao.and.App
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.base.BaseHttpMethods
import com.xlqianbao.and.base.BaseHttpResult
import com.xlqianbao.and.utils.JsonParseUtil
import io.reactivex.Observer
import io.reactivex.functions.Function
import java.util.*


/**
 * 网络请求的方法实现
 * Created by Makise on 2017/2/4.
 */

class HttpMethods//构造方法私有
private constructor() : BaseHttpMethods(), HttpContract.Methods {
    protected var randomNumber: String = ""
    protected var sign: String = ""

    //在访问HttpMethods时创建单例
    object SingletonHolder {
        internal val INSTANCE = HttpMethods()
    }

    //获取签名
    protected fun genParams(signParams: HashMap<String, String>): HashMap<String, String> {
        randomNumber = RandomUtils.getRandomNumbersAndLetters(32)
        sign = Signature.getSign(signParams, Constants.Appkey, randomNumber)
        signParams.clear()
        signParams.put("nonceStr", randomNumber)
        signParams.put("sign", sign)
        return signParams
    }

    /**
     * 以下开始实现具体的网络请求方法
     */

    override fun constantQuery(dataToken: String, subscriber: Observer<Model.ConstantQuery>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("dataToken", dataToken)
            }
        })

        val observable = mService.constantQuery(params, dataToken)
                .map<String>(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.ConstantQuery::class.java) })
        toSubscribe(observable, subscriber)
    }

    companion object {
        //获取单例
        val instance: HttpMethods
            get() = SingletonHolder.INSTANCE
    }

    override fun borrowInfo(token: String?, subscriber: Observer<Model.BorrowInfo>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
            }
        })

        val observable = mService.borrowInfo(params, token)
                .map<String>(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.BorrowInfo::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun banner(type: String, subscriber: Observer<List<Model.Banner>>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("type", type)
            }
        })
        val observable = mService.getImage(params, type)
                .map<String>(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.JsonListBanner::class.javaObjectType).list })
        toSubscribe(observable, subscriber)
    }

    override fun getImage(type: String, subscriber: Observer<List<Model.ImageBean>>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("type", type)
            }
        })
        val observable = mService.getImage(params, type)
                .map(Function<BaseHttpResult, List<Model.ImageBean>> { result ->
                    if (!HttpResultFunc().checkSign(result)) return@Function ArrayList<Model.ImageBean>()
                    if (result.code != Constants.OK) {
                        throw ResultException(result)
                    }
                    JsonParseUtil.json2DataBean(result.data, Model.JsonListImageBean::class.java).list
                })
        toSubscribe(observable, subscriber)
    }

    override fun userInfo(token: String?, subscriber: Observer<Model.User>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
            }
        })

        val observable = mService.userInfo(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.User::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun proConf(token: String?, subscriber: Observer<Model.ProConf>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
            }
        })
        val observable = mService.proConf(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.ProConf::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun notices(token: String?, type: String, subscriber: Observer<Model.Notices>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
                put("type", type)
            }
        })
        val observable = mService.notices(params, token, type)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.Notices::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun readNotice(token: String?, nids: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
                put("nids", nids)
            }
        })
        val observable = mService.readNotice(params, token, nids)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun apply(token: String?, amount: String, days: Int, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
                put("amount", amount)
                put("days", days.toString())
            }
        })

        val observable = mService.apply(params, token, amount, days)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun hasNotice(token: String?, subscriber: Observer<Model.HasNotice>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
            }
        })

        val observable = mService.hasNotice(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.HasNotice::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun share(version: String, token: String?, type: String, subscriber: Observer<Model.ShareData>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("version", version)
                token?.let { put("token", it) }
                put("type", type)
            }
        })

        val observable = mService.share(params, version, token, type)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.ShareData::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun orderRepay(token: String?, orderId: String?, subscriber: Observer<Model.OrderRepay>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
                orderId?.let { put("orderId", it) }
            }
        })

        val observable = mService.orderRepay(params, token, orderId)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.OrderRepay::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun queryCard(token: String?, subscriber: Observer<Model.QueryCard>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
            }
        })

        val observable = mService.queryCard(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.QueryCard::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun checkPhone(phone: String, subscriber: Observer<Model.CheckPhone>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("phone", phone)
            }
        })

        val observable = mService.checkPhone(params, phone)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.CheckPhone::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun sendValidCode(vticket: String?, type: String?, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                vticket?.let { put("vticket", it) }
                type?.let { put("type", it) }
            }
        })

        val observable = mService.sendValidCode(params, vticket, type)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun login(vticket: String?, code: String?, device: String?, jpushId: String?,
                       channel: String?, subscriber: Observer<Model.User>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                vticket?.let { put("vticket", it) }
                code?.let { put("code", it) }
                device?.let { put("device", it) }
                jpushId?.let { put("jpushId", it) }
                channel?.let { put("channel", it) }
            }
        })

        val observable = mService.login(params, vticket, code, device, jpushId, channel)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.User::class.java) })
        toSubscribe(observable, subscriber)

    }

    override fun update(subscriber: Observer<Model.Update>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("version", "2.0")
                put("packageName", App.instance.packageName)
                put("channel", Tools.getChannelId(App.instance))
            }
        })

        val observable = mService.update(params, "2.0", App.instance.packageName, Tools.getChannelId(App.instance))
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.Update::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun contactUpload(token: String, data: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("data", data)
            }
        })

        val observable = mService.contactUpload(params, token, data)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }


    override fun getAuthId(token: String?, deviceInfo: String, subscriber: Observer<Model.Auth>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                token?.let { put("token", it) }
                put("deviceInfo", deviceInfo)
            }
        })
        val observable = mService.getAuthId(params, token, deviceInfo)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.Auth::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun certPics(token: String, subscriber: Observer<Model.CertPics>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
            }
        })
        val observable = mService.certPics(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map { t -> JsonParseUtil.json2DataBean(t, Model.CertPics::class.java) }
        toSubscribe(observable, subscriber)
    }

    override fun certConfirm(token: String, delta: String, ucid1: String, ucid2: String, ucid3: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("version", "1.1")
                put("token", token)
                put("delta", delta)
                put("ucid1", ucid1)
                put("ucid2", ucid2)
                put("ucid3", ucid3)
            }
        })
        val observable = mService.certConfirm(params, "1.1", token, delta, ucid1, ucid2, ucid3)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun certPic(token: String, list: String, subscriber: Observer<Model.CertPic>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("version", "1.1")
                put("token", token)
                put("list", list)
            }
        })
        val observable = mService.certPic(params, "1.1", token, list)
                .map(BaseHttpMethods.HttpResultFunc())
                .map { t -> JsonParseUtil.json2DataBean(t, Model.CertPic::class.java) }
        toSubscribe(observable, subscriber)
    }

    override fun noticeList(token: String, pno: Int, psize: Int, timestamp: String, subscriber: Observer<Model.MyNoticeList>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("pno", pno.toString())
                put("psize", psize.toString())
                put("timestamp", timestamp)
            }
        })
        val observable = mService.noticeList(params, token, pno, psize, timestamp)
                .map(BaseHttpMethods.HttpResultFunc())
                .map({ s -> JsonParseUtil.json2DataBean(s, Model.MyNoticeList::class.java) })
        toSubscribe(observable, subscriber)
    }

    override fun types(nids: String, subscriber: Observer<Model.AllTypesList>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("nids", nids)
            }
        })

        val observable = mService.types(params, nids)
                .map(Function<BaseHttpResult, Model.AllTypesList> { result ->
                    if (!HttpResultFunc().checkSign(result)) return@Function null
                    if (result.code != Constants.OK) {
                        throw ResultException(result)
                    }
                    val list = JsonParseUtil.json2DataBean(result.data, Model.AllTypesList::class.java)
//                    list.live_time = JSONArray.parseArray(JSON.parseObject<TypesListObject>(result.data, TypesListObject::class.java).live_time, Types::class.java)
//                    list.profession = JSONArray.parseArray(JSON.parseObject<TypesListObject>(result.data, TypesListObject::class.java).profession, Types::class.java)
//                    list.monthIn = JSONArray.parseArray(JSON.parseObject<TypesListObject>(result.data, TypesListObject::class.java).monthIn, Types::class.java)
//                    list.qinshu = JSONArray.parseArray(JSON.parseObject<TypesListObject>(result.data, TypesListObject::class.java).qinshu, Types::class.java)
//                    list.shehui = JSONArray.parseArray(JSON.parseObject<TypesListObject>(result.data, TypesListObject::class.java).shehui, Types::class.java)
                    list
                })

        toSubscribe(observable, subscriber)
    }

    override fun region(parent: String, subscriber: Observer<List<Model.Regions>>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("parent", parent)
            }
        })

        val observable = mService.region(params, parent)
                .map(Function<BaseHttpResult, List<Model.Regions>> { result ->
                    if (!HttpResultFunc().checkSign(result)) return@Function ArrayList<Model.Regions>()
                    if (result.code != Constants.OK) {
                        throw ResultException(result)
                    }
                    JsonParseUtil.json2DataBean(result.data, Model.RegionsList::class.java).regions
//                    JSONArray.parseArray(JSON.parseObject<RegionsListObject>(result.data, RegionsListObject::class.java).regions, Model.Regions::class.java)
                })
        toSubscribe(observable, subscriber)
    }

    override fun queryLiveInfo(token: String, subscriber: Observer<Model.QueryLiveInfo>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
            }
        })
        val observable = mService.queryLiveInfo(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map { s -> JsonParseUtil.json2DataBean(s, Model.QueryLiveInfo::class.java) }
        toSubscribe(observable, subscriber)
    }

    override fun setLiveInfo(token: String, prov: String, city: String, dist: String, addr: String, time: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("prov", prov)
                put("city", city)
                put("dist", dist)
                put("addr", addr)
                put("time", time)
            }
        })
        val observable = mService.setLiveInfo(params, token, prov, city, dist, addr, time)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun queryWorkInfo(token: String, subscriber: Observer<Model.QueryWorkInfo>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
            }
        })
        val observable = mService.queryWorkInfo(params, token)
                .map(BaseHttpMethods.HttpResultFunc())
                .map{s->JsonParseUtil.json2DataBean(s, Model.QueryWorkInfo::class.java)}
        toSubscribe(observable, subscriber)
    }

   override fun queryContacts(token: String, subscriber: Observer<List<Model.QueryContacts>>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
            }
        })
        val observable = mService.queryContacts(params, token)
                .map(Function<BaseHttpResult, List<Model.QueryContacts>> { t ->
                    if (!HttpResultFunc().checkSign(t)) return@Function null
                    if (t.code != Constants.OK) {
                        throw ResultException(t)
                    }
                    if (t.data != null) {
                        JsonParseUtil.json2DataBean(t.data, Model.QueryContactsList::class.javaObjectType).contacts
                    } else {
                        null
                    }
                })
        toSubscribe(observable, subscriber)
    }

    override fun setWorkInfo(token: String, profession: String, monthIn: String, companyName: String,
                    companyProvince: String, companyCity: String, companyDistrict: String, companyTelephone: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("profession", profession)
                put("monthIn", monthIn)
                put("companyName", companyName)
                put("companyProvince", companyProvince)
                put("companyCity", companyCity)
                put("companyDistrict", companyDistrict)
                put("companyTelephone", companyTelephone)
            }
        })
        val observable = mService.setWorkInfo(params, token, profession, monthIn, companyName, companyProvince, companyCity, companyDistrict, companyTelephone)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun setContacts(token: String, rName: String, rPhone: String, sName: String, sPhone: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("rName", rName)
                put("rPhone", rPhone)
                put("sName", sName)
                put("sPhone", sPhone)
            }
        })
        val observable = mService.setContacts(params, token, rName, rPhone, sName, sPhone)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun repay(token: String, orderId: String, code: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("orderId", orderId)
                put("code", code)
            }
        })

        val observable = mService.repay(params, token, orderId, code)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }

    override fun preValidCard(token: String, name: String, idCard: String, number: String, phone: String, subscriber: Observer<Model.PreValidCard>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("name", name)
                put("idCard", idCard)
                put("number", number)
                put("phone", phone)
            }
        })

        val observable = mService.preValidCard(params, token, name, idCard, number, phone)
                .map(BaseHttpMethods.HttpResultFunc())
                .map{s -> JsonParseUtil.json2DataBean(s, Model.PreValidCard::class.java)}
        toSubscribe(observable, subscriber)
    }

    override fun bindCard(token: String, vticket: String, code: String, subscriber: Observer<String>) {
        val params = genParams(object : HashMap<String, String>() {
            init {
                put("token", token)
                put("vticket", vticket)
                put("code", code)
            }
        })

        val observable = mService.bindCard(params, token, vticket, code)
                .map(BaseHttpMethods.HttpResultFunc())
        toSubscribe(observable, subscriber)
    }
}
