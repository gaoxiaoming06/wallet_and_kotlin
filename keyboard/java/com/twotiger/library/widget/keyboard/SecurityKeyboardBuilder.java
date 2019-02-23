package com.twotiger.library.widget.keyboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;


/**
 * Created by Makise on 2016/8/9.
 */
public class SecurityKeyboardBuilder extends PopupWindow {

    public SecurityKeyboard pwdView;
    private View rootView; // 总的布局

    public SecurityKeyboardBuilder(Context context) {
        super(context);
        this.rootView = LayoutInflater.from(context).inflate(R.layout.security_keyboard, null);
        this.pwdView = (SecurityKeyboard) rootView.findViewById(R.id.pwd_view);
        this.pwdView.setData(this, context, rootView);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setAnimationStyle(R.style.mypopwindow_anim_style);
        setContentView(rootView);
    }

    /**
     * 返回一个键盘对象
     *
     * @return
     */
    public SecurityKeyboard build() {
        if (pwdView != null)
            return pwdView;
        return null;
    }
}