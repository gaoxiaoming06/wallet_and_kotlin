package com.xlqianbao.and.utils

import com.xlqianbao.and.R
import com.xlqianbao.and.ui.home.AuthFragment
import com.xlqianbao.and.ui.home.LoanFragment
import com.xlqianbao.and.ui.home.PersonalFragment

/**
 * Created by Makise on 2017/2/7.
 */

object FragmentFactory {
    val loan = LoanFragment()
    val auth = AuthFragment()
    val personal = PersonalFragment()
    fun getInstanceByBtnID(btnId: Int) = when (btnId) {
        R.id.btn_1 -> loan
        R.id.btn_2 -> auth
        R.id.btn_3 -> personal
        else -> loan
    }
}
