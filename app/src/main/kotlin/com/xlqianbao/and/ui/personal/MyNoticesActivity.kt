package com.xlqianbao.and.ui.personal

import android.view.View
import android.widget.TextView
import com.handmark.pulltorefresh.library.PullToRefreshBase
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.adapter.MyNoticesAdapter
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.view.refresh.RefreshProgressLayout
import kotlinx.android.synthetic.main.activity_my_notices.*
import kotlinx.android.synthetic.main.include_title_bar.*


/**
 * Created by Makise on 2017/9/21.
 */
class MyNoticesActivity : BaseActivity(), RefreshProgressLayout.OnRefreshListener {

    var timeStampReturned: String? = null
    private var PNO = 1//页码
    private val PSIZE = 20//每页几条数据
    lateinit private var adapter: MyNoticesAdapter
    lateinit private var list: MutableList<Model.MyNotice>
    //首次进入页面中间loading
    private var firstLoad: Boolean = false

    override fun initView() {
        setContentView(R.layout.activity_my_notices)
        //设置titleBar
        left.setOnClickListener { onBackPressed() }
        middle.text = "我的消息"

        listview.mode = PullToRefreshBase.Mode.DISABLED
        swipe_refresh.setOnRefreshListener(this)
        swipe_refresh.setColorSchemeResources(R.color.xl_blue)
        //为了保证pulltorefreshlistview跟swipeRefreshview不冲突，必须添加这行代码
        swipe_refresh.setChildListView(listview)
        listview.setOnLastItemVisibleListener(PullToRefreshBase.OnLastItemVisibleListener {
            val view = listview.childviewOfFooterview
            val tv = view.findViewById(R.id.tv_footer) as TextView
            val text = tv.text.toString().trim { it <= ' ' }
            if ("没有更多内容了" == text) return@OnLastItemVisibleListener
            getData(false)
        })
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        list = ArrayList()
        firstLoad = true
        timeStampReturned = ""
        getData(true)
    }

    private fun getData(isPullDown: Boolean) {
        if (isPullDown) PNO = 1
        HttpMethods.instance
                .noticeList(getToken()!!, PNO, PSIZE, timeStampReturned!!,
                        object : ProgressSubscriber<Model.MyNoticeList>(firstLoad) {
                            override fun onNext(myNoticeList: Model.MyNoticeList) {
                                swipe_refresh.isRefreshing = false
                                //初次请求完成
                                firstLoad = false
                                //请求成功PNO++
                                PNO++
                                if (myNoticeList.list!!.size !== 0) {
                                    //隐藏错误提示
                                    tv_no_data.visibility = View.GONE
                                    if (isPullDown) {
                                        //下拉刷新
                                        timeStampReturned = myNoticeList.timestamp.toString()
                                        list.clear()
                                        list.addAll(myNoticeList.list!!)
                                        adapter = MyNoticesAdapter(this@MyNoticesActivity, list)
                                        listview.setAdapter(adapter)
                                        adapter.notifyDataSetChanged()
                                    } else {
                                        list.addAll(myNoticeList.list!!)
                                        adapter = MyNoticesAdapter(this@MyNoticesActivity, list)
                                        adapter.notifyDataSetChanged()
                                        listview.changeMyFooterViewContent(true)
                                    }
                                } else {
                                    if (list.isNotEmpty()) {
                                        //没有更多数据啦
                                        listview.changeMyFooterViewContent(false)
                                    } else {
                                        //没有消息
                                        swipe_refresh.visibility = View.GONE
                                        tv_no_data.text = "暂无消息"
                                        tv_no_data.visibility = View.VISIBLE
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                swipe_refresh.isRefreshing = false
                                firstLoad = false
                                tv_no_data.text = "加载失败，下拉重试"
                                tv_no_data.visibility = View.VISIBLE
                            }
                        })
    }
    override fun onRefresh() {
        getData(true);
    }
}