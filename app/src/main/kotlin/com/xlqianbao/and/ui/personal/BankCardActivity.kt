package com.xlqianbao.and.ui.personal

import android.text.TextUtils
import com.bumptech.glide.Glide
import com.xlqianbao.and.Constants
import com.xlqianbao.and.Model
import com.xlqianbao.and.R
import com.xlqianbao.and.base.BaseActivity
import com.xlqianbao.and.view.GlideCircleTransform
import kotlinx.android.synthetic.main.activity_bank_card.*
import kotlinx.android.synthetic.main.include_title_bar.*
import java.util.HashMap

/**
 * Created by gao on 2017/9/26.
 */
class BankCardActivity: BaseActivity(){
    private lateinit var map: HashMap<String, Int>
    override fun initView() {
        setContentView( R.layout.activity_bank_card)
        //设置titleBar
        left.setOnClickListener({ onBackPressed() })
        middle.text = "银行卡认证"
    }

    override fun initData() {
        setPixelInsetTop(false, R.color.xl_blue)
        map = HashMap()
        map.put("CITIC", R.drawable.shape_bank_cards_citic)
        map.put("BOC", R.drawable.shape_bank_cards_boc)
        map.put("CMB", R.drawable.shape_bank_cards_cmb)
        map.put("PSBC", R.drawable.shape_bank_cards_psbc)
        map.put("CIB", R.drawable.shape_bank_cards_cib)
        map.put("SPDB", R.drawable.shape_bank_cards_spdb)
        map.put("PAB", R.drawable.shape_bank_cards_pab)
        map.put("ABC", R.drawable.shape_bank_cards_abc)
        map.put("CMBC", R.drawable.shape_bank_cards_cmbc)
        map.put("CCB", R.drawable.shape_bank_cards_ccb)
        map.put("CGB", R.drawable.shape_bank_cards_cgb)
        map.put("ICBC", R.drawable.shape_bank_cards_icbc)
        map.put("BOB", R.drawable.shape_bank_cards_bob)
        map.put("CEB", R.drawable.shape_bank_cards_ceb)
        //获取银行卡信息
        val card = intent.getSerializableExtra("card") as Model.QueryCard
        //填充页面
        tv_card_name.text = card.name
        tv_card_number.text = formatCardNo(card.number)
        setCustomFont(tv_card_number)
        if (map.containsKey(card.code)) {
            bank_card_bg.setBackgroundDrawable(resources.getDrawable(map[card.code]!!))
        }
        val data = getConstantDataByKey(Constants.DZ_BANKLOGO)
        if (data == null) {
            bank_icon.setImageResource(R.drawable.icon_bank_default)
        } else {
            Glide.with(this@BankCardActivity)
                    .load(data.`val` + card.code + ".png")
                    .transform(GlideCircleTransform(this@BankCardActivity))
                    .error(R.drawable.icon_bank_default)
                    .placeholder(R.drawable.icon_bank_default)
                    .into(bank_icon)
        }
    }

    private fun formatCardNo(s: CharSequence?): String {
        if(TextUtils.isEmpty(s)) return ""
        val sb = StringBuilder()
        for (i in 0 until s!!.length) {
            if (i != 4 && i != 8 && s[i] == ' ') {
                continue
            } else {
                sb.append(s[i])
                if (sb.length % 5 == 0 && sb[sb.length - 1] != ' ') {
                    sb.insert(sb.length - 1, ' ')
                }
            }
        }
        return sb.toString()
    }
}