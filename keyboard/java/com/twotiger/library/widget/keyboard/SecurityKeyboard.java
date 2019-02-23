package com.twotiger.library.widget.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * 键盘的核心类
 * Created by Makise on 2016/8/8.
 */
public class SecurityKeyboard extends RelativeLayout implements View.OnClickListener {
    public TextView resend;
    public KeyboardListener listener;
    public boolean canSend;
    private Context context;
    private GridView gridView;    //用GrideView布局模拟键盘
    private ArrayList<Map<String, String>> valueList;    //Adapter适配需要 不能用数组
    private ArrayList<TextView> nums;
    private PopupWindow popWindow;
    private ImageView close, pb_loading;
    private View view;
    private LogoView logoView;
    private TextView tip, errorText, btnDone, title, findPwd;
    private RelativeLayout titleBar, error;
    private CountDownTimer timer;
    private StringBuffer inputNum;
    private boolean showTextBox, showRight, showError, showClose, showJDIcon;
    private LinearLayout text_box, right, input_layout, del, jdSafeIcon, resultRL;

    private String tenkey;

    private Window window;
    private View rootView;
    private Type currentType;
    private Animation loadingAnim;
    //键盘是否可输入
    private boolean canInput;
    //是否隐藏已输入的字符
    private boolean showInput;
    //GrideView的适配器
    BaseAdapter adapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return valueList.size();
        }

        @Override
        public Object getItem(int position) {
            return valueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(context, R.layout.item_keyboard, null);
                viewHolder.btnKey = (TextView) convertView.findViewById(R.id.btn_keys);
                if (position == 11) {
                    viewHolder.ll_img = (LinearLayout) convertView.findViewById(R.id.ll_img);
                    viewHolder.btnImg = (ImageView) convertView.findViewById(R.id.btn_img);
                }
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (!showRight) {
                viewHolder.btnKey.setWidth(0);
            }
            viewHolder.btnKey.setText(valueList.get(position).get("name"));
            if (position == 9) {
                if (TextUtils.isEmpty(tenkey)) {
                    viewHolder.btnKey.setBackgroundResource(R.drawable.selector_key_del);
                    viewHolder.btnKey.setEnabled(false);
                    viewHolder.btnKey.setTextSize(12);
                    viewHolder.btnKey.setTextColor(Color.parseColor("#999999"));
                } else {
                    if ("●".equals(tenkey)) {
                        //圆点变大一点
                        viewHolder.btnKey.setTextSize(12);
                    }
                    viewHolder.btnKey.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //拿到 ● 或 X
                            String currentNum = valueList.get(position).get("name");
                            if (currentNum.equals("●"))
                                currentNum = ".";
                            listener.input(currentNum);
                        }
                    });
                }
            }
            if (position == 11) {
                viewHolder.btnKey.setVisibility(GONE);
                viewHolder.ll_img.setVisibility(VISIBLE);
                if (showRight) {
                    viewHolder.btnImg.setImageResource(R.mipmap.icon_shouqi);
                }
                viewHolder.ll_img.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (showRight) {
                            popWindow.dismiss();
                        } else {
                            del();
                        }
                    }
                });
            }

            //给数字键盘0-9设置的点击事件
            if (position < 11 && position != 9) {
                viewHolder.btnKey.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (canInput) {
                            //拿到数字
                            String currentNum = valueList.get(position).get("name");
                            listener.input(currentNum);
                            if (showTextBox) {
                                //给输入框添加圆点
                                for (final TextView num : nums) {
                                    if (num.getVisibility() == INVISIBLE) {
                                        num.setTextSize(24);
                                        num.setText(currentNum);
                                        //是否显示已输入的字符
                                        if (!showInput) {
                                            RxTask.doInUIThreadDelay(new RxTask.UITask() {
                                                @Override
                                                public void doInUIThread() {
                                                    num.setTextSize(12);
                                                    num.setText("●");
                                                }
                                            }, 200, TimeUnit.MILLISECONDS);
                                        }
                                        num.setVisibility(VISIBLE);
                                        break;
                                    }
                                }
                                //不足6个数时，累加
                                if (inputNum.length() < 6)
                                    inputNum.append(currentNum);

                                //满6个数传入监听
                                if (inputNum.length() == 6) {
                                    listener.inputComplete(inputNum.toString());
                                }
                            }
                        }
                    }
                });
            }
            return convertView;
        }
    };

    public SecurityKeyboard(Context context) {
        this(context, null);
    }

    public SecurityKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        view = View.inflate(context, R.layout.keyboard, null);

        valueList = new ArrayList<>();

        gridView = (GridView) view.findViewById(R.id.gv_keybord);

        logoView = (LogoView) view.findViewById(R.id.logo_view);

        resultRL = (LinearLayout) view.findViewById(R.id.result);

        //顶部
        titleBar = (RelativeLayout) view.findViewById(R.id.title_bar);
        error = (RelativeLayout) view.findViewById(R.id.error);
        jdSafeIcon = (LinearLayout) view.findViewById(R.id.jd_safe_icon);

        tip = (TextView) view.findViewById(R.id.tv_tip);
        errorText = (TextView) view.findViewById(R.id.tv_error);
        del = (LinearLayout) view.findViewById(R.id.del);
        btnDone = (TextView) view.findViewById(R.id.done);
        title = (TextView) view.findViewById(R.id.title);
        resend = (TextView) view.findViewById(R.id.reSend);
        findPwd = (TextView) view.findViewById(R.id.tv_find_pwd);

        pb_loading = (ImageView) view.findViewById(R.id.pb_loading);

        del.setOnClickListener(this);

        btnDone.setOnClickListener(this);

        inputNum = new StringBuffer();

        text_box = (LinearLayout) view.findViewById(R.id.text_box);
        right = (LinearLayout) view.findViewById(R.id.right);
        input_layout = (LinearLayout) view.findViewById(R.id.input_layout);


        //加载中动画初始化
        loadingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_loading);
        loadingAnim.setInterpolator(new LinearInterpolator());

        timer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                resend.setText(millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                resend.setEnabled(true);
                resend.setText("重新发送");
                canSend = true;
            }
        };

        close = (ImageView) view.findViewById(R.id.close);

        addView(view);      //必须要，不然不显示控件
    }

    /**
     * 释放键盘
     */
    public static void destroy(SecurityKeyboard... keyboards) {
        for (SecurityKeyboard keyboard : keyboards) {
            if (keyboard != null) {
                keyboard.dismiss();
                keyboard = null;
            }
        }
    }

    /**
     * 获取输入区域键盘高度
     *
     * @return
     */
    public int getKeyboardHeight() {
        //计算高度 解决未显示时 拿不到高度的问题
        int width = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        input_layout.measure(width, height);
        return input_layout.getMeasuredHeight();
    }

    public void setTitle(String title) {
        setTitle(title, 0, 0);
    }

    public void setTitle(String title, int color) {
        setTitle(title, color, 0);
    }

    public void setTitle(String title, int color, int fontSize) {
        if (!TextUtils.isEmpty(title))
            this.title.setText(title);
        if (color != 0)
            this.title.setTextColor(color);
        if (fontSize != 0)
            this.title.setTextSize(fontSize);
    }

    public void setReSendOnClickListener(OnClickListener listener) {
        this.resend.setOnClickListener(listener);
    }

    public void setAnimListener(LogoView.LogoViewListener listener) {
        logoView.setListener(listener);
    }

    public void setData(SecurityKeyboardBuilder securityKeyboardBuilder, Context context, View rootView) {
        this.popWindow = securityKeyboardBuilder;
        this.context = context;
        this.rootView = rootView;
        this.window = ((Activity) context).getWindow();
    }

    public boolean isShowing() {
        return popWindow.isShowing();
    }

    public void setKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }

    private void setView() {
        /* 初始化按钮上应该显示的数字 */
        for (int i = 1; i < 13; i++) {
            Map<String, String> map = new HashMap<String, String>();
            if (i < 10) {
                map.put("name", String.valueOf(i));
            } else if (i == 10) {
                map.put("name", TextUtils.isEmpty(tenkey) ? "小蓝钱包\n安全键盘" : tenkey);
            } else if (i == 12) {
                map.put("name", showRight ? "收起" : "<");
            } else if (i == 11) {
                map.put("name", String.valueOf(0));
            }
            valueList.add(map);
        }
        close.setOnClickListener(this);
        nums = new ArrayList<>();
        canSend = true;

        nums.add((TextView) view.findViewById(R.id.num1));
        nums.add((TextView) view.findViewById(R.id.num2));
        nums.add((TextView) view.findViewById(R.id.num3));
        nums.add((TextView) view.findViewById(R.id.num4));
        nums.add((TextView) view.findViewById(R.id.num5));
        nums.add((TextView) view.findViewById(R.id.num6));

        gridView.setAdapter(adapter);
    }

    public void clearTextBox() {
        for (TextView num : nums) {
            num.setVisibility(INVISIBLE);
        }
        inputNum.delete(0, inputNum.length());
        canInput = true;
    }

    public void sendVcode() {
        if (canSend) {
            Toast.makeText(context, "短信验证码已发送", Toast.LENGTH_SHORT).show();
            resend.setVisibility(VISIBLE);
            timer.cancel();
            timer.start();
            canSend = false;
            resend.setEnabled(false);
        }
    }

    /**
     * 执行动画
     */
    public void startAnim(final boolean isSuccess, String info) {
        //执行动画的时候不允许输入
        canInput = false;
        errorText.setText("");
        errorText.setVisibility(INVISIBLE);
        //隐藏发送验证码按钮
        resend.setVisibility(INVISIBLE);
        if (!isSuccess) info += "，请重试";
        final String tipText = info;
        TranslateAnimation trans = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        trans.setDuration(500);
        final TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f
        );
        translate.setDuration(500);

        input_layout.startAnimation(trans);
        input_layout.setVisibility(INVISIBLE);
        trans.setAnimationListener(new AnimListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                resultRL.setVisibility(VISIBLE);
                logoView.startAnimation(translate);
                translate.setAnimationListener(new AnimListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        super.onAnimationStart(animation);
                        logoView.start(isSuccess);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        tip.setText(tipText);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.close) {
            //收起键盘
            popWindow.dismiss();
            return;
        }
        if (view.getId() == R.id.del) {
            if (canInput) {
                del();
                errorText.setText("");
            }
            return;
        }
        if (view.getId() == R.id.done) {
            //按下输入完成键
            listener.done();
        }
    }

    /**
     * 延时打开键盘
     */
    public void show(int milliseconds) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        RxTask.doInUIThreadDelay(new RxTask.UITask() {
            @Override
            public void doInUIThread() {
                if (!popWindow.isShowing()) {
                    popWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
                    //必须在setlistener之后调用show才会成功回调键盘的show监听
                    if (listener != null)
                        listener.show();
                    if (currentType == Type.Extend) {
                        RxTask.doInUIThreadDelay(new RxTask.UITask() {
                            @Override
                            public void doInUIThread() {
                                WindowManager.LayoutParams lp = window.getAttributes();
                                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                lp.alpha = 0.5f;
                                window.setAttributes(lp);
                            }
                        }, 100, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }, milliseconds, TimeUnit.MILLISECONDS);
    }

    public void show() {
        show(50);
    }

    /**
     * 直接关闭键盘
     */
    public void dismiss() {
        if (popWindow != null && popWindow.isShowing())
            popWindow.dismiss();
    }

    /**
     * 延迟关闭键盘
     *
     * @param millseconds
     */
    public void dismiss(int millseconds) {
        RxTask.doInUIThreadDelay(new RxTask.UITask() {
            @Override
            public void doInUIThread() {
                popWindow.dismiss();
            }
        }, millseconds, TimeUnit.MILLISECONDS);
    }

    public void hideShadow() {
        rootView.findViewById(R.id.top_shadow).setVisibility(GONE);
    }

    /**
     * 根据枚举来配置相应的键盘
     *
     * @param type
     */
    public void setType(Type type) {
        int rlHeight = 0;
        int color = 0;
        this.currentType = type;
        switch (type) {
            case Standard:
                rlHeight = 40;
                color = Color.parseColor("#ffffff");
                this.showTextBox = false;
                this.showRight = true;
                this.showError = false;
                this.showClose = false;
                this.showJDIcon = false;

                rootView.findViewById(R.id.bg).setVisibility(GONE);
                rootView.findViewById(R.id.top_corner).setVisibility(GONE);
                rootView.findViewById(R.id.top_shadow).setVisibility(VISIBLE);
                popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        //键盘隐藏状态的回调
                        listener.hide();
                    }
                });
                break;
            case Extend:
                rlHeight = 50;
                color = Color.parseColor("#eeeeee");
                this.showTextBox = true;
                this.showRight = false;
                this.showError = true;
                this.showClose = true;
                this.showJDIcon = true;

                rootView.findViewById(R.id.close).setPadding(dp2px(16), 0, dp2px(16), dp2px(6));
                rootView.findViewById(R.id.title).setPadding(0, 0, 0, dp2px(6));
                rootView.findViewById(R.id.reSend).setPadding(0, 0, 0, dp2px(6));

                rootView.findViewById(R.id.bg).setVisibility(VISIBLE);
                rootView.findViewById(R.id.top_corner).setVisibility(VISIBLE);
                rootView.findViewById(R.id.top_shadow).setVisibility(GONE);
                popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.alpha = 1f;
                        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        window.setAttributes(lp);
                        listener.hide();
                    }
                });
                break;
        }
        popWindow.setBackgroundDrawable(new BitmapDrawable());// 这样设置才能点击屏幕外dismiss窗口
        //可输入
        canInput = true;

        //改变RL高度
        ViewGroup.LayoutParams params = titleBar.getLayoutParams();
        params.height = dp2px(rlHeight);
        titleBar.setLayoutParams(params);

        ViewGroup.LayoutParams params2 = error.getLayoutParams();
        params2.height = dp2px(rlHeight);
        error.setLayoutParams(params2);

        //设置颜色
        titleBar.setBackgroundColor(color);

        //各控件显隐
        text_box.setVisibility(showTextBox ? VISIBLE : GONE);
        right.setVisibility(showRight ? VISIBLE : GONE);
        error.setVisibility(showError ? VISIBLE : GONE);
        close.setVisibility(showClose ? VISIBLE : GONE);
        jdSafeIcon.setVisibility(showJDIcon ? VISIBLE : GONE);
        setView();
    }

    /**
     * 退格键的方法
     */
    public void del() {
        listener.del();
        //不为零时减一
        if (showTextBox) {
            if (inputNum.length() > 0) {
                inputNum = inputNum.deleteCharAt(inputNum.length() - 1);
            }
            for (int i = nums.size() - 1; i >= 0; i--) {
                TextView num = nums.get(i);
                if (num.getVisibility() == View.VISIBLE) {
                    num.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        }
    }

    /**
     * 显示忘记密码并设置点击事件
     *
     * @param listener
     */
    public void setForgetPwdOnClickListener(OnClickListener listener) {
        this.findPwd.setVisibility(VISIBLE);
        this.findPwd.setOnClickListener(listener);
    }

    /**
     * 隐藏忘记密码按钮
     */
    public void hideForgetPwd() {
        this.findPwd.setVisibility(GONE);
        this.findPwd.setOnClickListener(null);
    }

    /**
     * 设置红键文本和点击事件
     *
     * @param text
     * @param listener
     */
    public void setRedKey(String text, OnClickListener listener) {
        this.btnDone.setText(text);
        this.btnDone.setOnClickListener(listener);
    }

    /**
     * 设置tip显示文本
     *
     * @param tip
     */
    public void showTip(String tip) {
        hideLoading();
        this.errorText.setText(tip);
        this.errorText.setVisibility(VISIBLE);
    }

    /**
     * 隐藏tip
     */
    public void hideTip() {
        this.errorText.setText("");
        this.errorText.setVisibility(INVISIBLE);
    }

    public void showLoading() {
        //显示菊花转
        pb_loading.setVisibility(VISIBLE);
        pb_loading.startAnimation(loadingAnim);
        errorText.setVisibility(GONE);
        findPwd.setVisibility(GONE);
        //禁止输入
        canInput = false;
    }

    public void hideLoading() {
        //隐藏
        pb_loading.setVisibility(GONE);
        pb_loading.clearAnimation();
        //允许输入
        canInput = true;
    }

    /**
     * 设置第十个键
     *
     * @param key
     */
    public void setTenKey(String key) {
        this.tenkey = key;
    }

    /**
     * 是否显示输入的字符 默认为false 不显示
     *
     * @param showInput
     */
    public void showInput(boolean showInput) {
        this.showInput = showInput;
    }

    public enum Type {
        Standard, Extend
    }

    //dp转px
    public int dp2px(int dp) {
        if (context == null) return -1;
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public interface KeyboardListener {
        void input(String s);

        void del();

        void done();

        void inputComplete(String str);

        void show();

        void hide();
    }

    /**
     * 存放控件
     */
    public final class ViewHolder {
        public TextView btnKey;
        public ImageView btnImg;
        public LinearLayout ll_img;
    }
}