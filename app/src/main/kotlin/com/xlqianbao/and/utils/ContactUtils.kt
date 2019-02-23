package com.xlqianbao.and.utils

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.TextUtils
import android.util.Log
import com.tendcloud.tenddata.TCAgent
import com.xlqianbao.and.BuildConfig
import com.xlqianbao.and.Model
import java.util.*

/**
 * 获取通讯录信息
 * Created by Makise on 2017/6/29.
 */

object ContactUtils {
    /**
     * 判断返回的list 为null时即未获得权限或没有数据
     *
     * @param context
     * @return
     */
    fun loadContacts(context: Context): List<Model.ContactInfo>? {
        var mAllContactsList: MutableList<Model.ContactInfo>? = null
        try {
            val resolver = context.contentResolver
            val phoneCursor = resolver.query(Phone.CONTENT_URI, arrayOf(Phone.DISPLAY_NAME, Phone.NUMBER, "sort_key"), null, null, "sort_key COLLATE LOCALIZED ASC")
            if (phoneCursor == null || phoneCursor.count == 0) {
                //未获得读取联系人权限 或 未获得联系人数据
                return null
            }
            val PHONES_NUMBER_INDEX = phoneCursor.getColumnIndex(Phone.NUMBER)
            val PHONES_DISPLAY_NAME_INDEX = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME)
            if (phoneCursor.count > 0) {
                //获取联系人数据
                mAllContactsList = ArrayList<Model.ContactInfo>()
                while (phoneCursor.moveToNext()) {
                    val phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX)
                    if (TextUtils.isEmpty(phoneNumber))
                        continue
                    val contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX)
                    //添加到集合
                    mAllContactsList.add(Model.ContactInfo(contactName, phoneNumber))
                }
            }
            phoneCursor.close()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                //log
                Log.e("loadContacts", e.localizedMessage)
            } else {
                //发送错误报告到td
                TCAgent.onError(context, e)
            }
        }

        return mAllContactsList
    }
}
