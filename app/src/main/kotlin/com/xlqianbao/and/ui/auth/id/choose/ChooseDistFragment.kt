package com.xlqianbao.and.ui.auth.id.choose

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.twotiger.library.utils.ScreenUtils
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseFragment
import com.xlqianbao.and.http.HttpMethods
import com.xlqianbao.and.http.subscribers.ProgressSubscriber
import com.xlqianbao.and.utils.rx.RxBus
import kotlinx.android.synthetic.main.fragment_choose_dist.*
import kotlinx.android.synthetic.main.include_title_bar.*

/**
 * Created by gao on 2017/9/21.
 */
class ChooseDistFragment:BaseFragment(){

    companion object {
        lateinit var instance: ChooseDistFragment

        fun getInstan(): ChooseDistFragment {
            return instance
        }
    }

    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        val view = inflater.inflate(R.layout.fragment_choose_dist, container, false)

        return view
    }

    override fun initData() {
        instance = this
        left.setOnClickListener({ activity.onBackPressed() })
        middle.text = "选择区"
    }

    fun setData(id: String, name: String) {
        root.removeAllViews()
        HttpMethods.instance
                .region(id, object : ProgressSubscriber<List<Model.Regions>>(true) {
                    override fun onNext(regionses: List<Model.Regions>) {
                        for (regions in regionses) {
                            val tv = TextView(activity)
                            tv.hint = regions.id
                            tv.text = regions.name
                            tv.textSize = 16f
                            tv.setTextColor(resources.getColor(R.color.xl_gray_headline))
                            tv.gravity = Gravity.CENTER_VERTICAL
                            tv.setOnClickListener { view ->
                                val addr = name + "-" + (view as TextView).text.toString()
                                RxBus.default.post(Model.ChooseAddrMessage(addr))
                                activity.finish()
                            }
                            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(activity, 64f))
                            params.leftMargin = ScreenUtils.dpToPxInt(activity, 16f)
                            tv.layoutParams = params
                            root.addView(tv)
                        }
                    }
                })
    }
}