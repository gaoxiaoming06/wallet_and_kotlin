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
import kotlinx.android.synthetic.main.fragment_choose_prov.*
import kotlinx.android.synthetic.main.include_title_bar.*

/**
 * Created by gao on 2017/9/21.
 */
class ChooseProvFragment:BaseFragment(){
    override fun initView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_choose_prov, container, false)
    }

    override fun initData() {

        left.setOnClickListener({ activity.onBackPressed() })
        middle.text = "选择省"

        HttpMethods.instance
                .region("1", object : ProgressSubscriber<List<Model.Regions>>(true) {
                    override fun onNext(regionses: List<Model.Regions>) {
                        for (regions in regionses) {
                            val tv = TextView(activity)
                            tv.setHint(regions.id)
                            tv.setText(regions.name)
                            tv.textSize = 16f
                            tv.setTextColor(resources.getColor(R.color.xl_gray_headline))
                            tv.gravity = Gravity.CENTER_VERTICAL
                            tv.setOnClickListener { view ->
                                val tv1 = view as TextView
                                ChooseCityFragment.getInstan().setData(tv1.hint.toString(), tv1.text.toString())
                                viewPagerWithNoScroll.next()
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