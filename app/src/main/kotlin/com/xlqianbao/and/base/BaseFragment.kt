package com.xlqianbao.and.base

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xlqianbao.and.ui.ViewPagerActivity
import com.xlqianbao.and.view.ViewPagerWithNoScroll
import kotlinx.android.synthetic.main.activity_view_pager.*


/**
 * fragment基类
 * Created by Makise on 2017/2/4.
 */

abstract class BaseFragment : Fragment() {

    lateinit var activity: BaseActivity

    lateinit var viewPagerWithNoScroll: ViewPagerWithNoScroll
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        activity = getActivity() as BaseActivity
        if(activity is ViewPagerActivity) {
            viewPagerWithNoScroll = (activity as ViewPagerActivity).viewPager
        }
        return initView(inflater!!, container!!)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
    }

    /**
     * 初始化view

     * @return
     */
    protected abstract fun initView(inflater: LayoutInflater, container: ViewGroup): View

    /**
     * 数据填充
     */
    protected abstract fun initData()

    protected fun showDialog(names: List<String>, callback: ClickCallBack) {
        val array = arrayOfNulls<String>(names.size)
        for (i in names.indices) {
            array[i] = names[i]
        }
        AlertDialog.Builder(activity).setItems(array
        ) { dialogInterface, i ->
            callback.click(names[i])
            dialogSelected()
        }.show()
    }

    /**
     * 选择框点击条目后的回调 用于每次选择操作后 校验用
     */
    protected open fun dialogSelected() {}


    interface ClickCallBack {
        fun click(s: String)
    }

}
