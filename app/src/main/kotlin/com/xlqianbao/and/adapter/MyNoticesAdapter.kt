package com.xlqianbao.and.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.twotiger.library.utils.DateUtil
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.ext.startNewActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.ui.WebViewPageActivity
import kotlinx.android.synthetic.main.item_my_notice.view.*

/**
 * Created by Makise on 2017/3/20.
 */

class MyNoticesAdapter(private val context: Context, private val list: MutableList<Model.MyNotice>) : BaseAdapter() {

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(i: Int): Any {
        return list[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        val holder: ViewHolder
        var tempView: View
        if (view == null) {
            val layout: View = LayoutInflater.from(context).inflate(R.layout.item_my_notice, null)
            holder = ViewHolder()
            holder.rootView = layout
            holder.tvTitle = layout.tv_title
            holder.ivNews = layout.iv_news
            holder.tvContent = layout.tv_content
            holder.ivContent = layout.iv_content
            holder.tvTime = layout.tv_time
            holder.tvDetail = layout.tv_detail
            tempView = layout
            tempView.tag = holder
        } else {
            tempView = view
            holder = tempView.tag as ViewHolder
        }
        holder.setData(list[i])
        return tempView
    }

    internal inner class ViewHolder {
        var rootView: View? = null
        var tvTitle: TextView? = null
        var ivNews: ImageView? = null
        var tvContent: TextView? = null
        var ivContent: ImageView? = null
        var tvTime: TextView? = null
        var tvDetail: TextView? = null

        fun setData(notice: Model.MyNotice) {
            //设置标题
            tvTitle!!.setText(notice.title)
            //是否未读
            ivNews!!.visibility = if ("0" == notice.hasRead) View.VISIBLE else View.GONE
            //判断类型 设置相应内容
            when (notice.ctype) {
                "TEXT" -> {
                    //文本内容
                    tvContent!!.visibility = View.VISIBLE
                    ivContent!!.visibility = View.GONE
                    tvContent!!.setText(notice.text)
                }
                "IMG",
                    //图片内容
                "MIX" -> {
                    //混合内容
                    ivContent!!.visibility = View.VISIBLE
                    tvContent!!.visibility = View.GONE
                    Glide.with(context)
                            .load(notice.img)
                            .error(R.drawable.banner_default)
                            .placeholder(R.drawable.banner_default)
                            .into(ivContent)
                }
            }
            tvTime!!.text = DateUtil.getDateStringForLong(notice.vtime, "yyyy/MM/dd HH:mm:ss")
            rootView!!.setOnClickListener {
                //查看详情
                val intent = Intent(context, WebViewPageActivity::class.java)
                intent.putExtra("url", notice.clickUrl)
                (context as BaseActivity).startNewActivity(intent, R.anim.push_left_in, R.anim.push_left_out, false)
                //发送已读回执给服务器
                HttpMethods.instance
                        .readNotice(context.getToken(), notice.id.toString(),
                                object : ProgressSubscriber<String>(false) {
                                    override fun onNext(t: String) {}
                                })
            }
        }
    }
}
