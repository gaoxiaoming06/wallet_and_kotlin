package com.xlqianbao.and.view

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

import com.twotiger.library.utils.ScreenUtils
import com.xlqianbao.and.R


/**
 * 自定义的dialog弹窗
 * Created by Makise on 2016/9/20.
 */

class CustomDialog : Dialog {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, theme: Int) : super(context, theme) {}

    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var positiveButtonText: String? = null
        private var negativeButtonText: String? = null
        private var contentView: View? = null
        private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
        private var negativeButtonClickListener: DialogInterface.OnClickListener? = null

        fun setMessage(message: String?): Builder {
            this.message = message
            return this
        }

        /**
         * Set the Dialog message from resource
         *
         * @return
         */
        fun setMessage(message: Int): Builder {
            this.message = context.getText(message) as String
            return this
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        fun setTitle(title: Int): Builder {
            this.title = context.getText(title) as String
            return this
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setContentView(v: View): Builder {
            this.contentView = v
            return this
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        fun setPositiveButton(positiveButtonText: Int,
                              listener: DialogInterface.OnClickListener): Builder {
            this.positiveButtonText = context
                    .getText(positiveButtonText) as String
            this.positiveButtonClickListener = listener
            return this
        }

        fun setPositiveButton(positiveButtonText: String,
                              listener: DialogInterface.OnClickListener): Builder {
            this.positiveButtonText = positiveButtonText
            this.positiveButtonClickListener = listener
            return this
        }

        fun setNegativeButton(negativeButtonText: Int,
                              listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonText = context
                    .getText(negativeButtonText) as String
            this.negativeButtonClickListener = listener
            return this
        }

        fun setNegativeButton(negativeButtonText: String,
                              listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonText = negativeButtonText
            this.negativeButtonClickListener = listener
            return this
        }

        fun create(layoutId: Int): CustomDialog {
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            // instantiate the dialog with the custom Theme
            val dialog = CustomDialog(context, R.style.dialog)
            val layout = inflater.inflate(if (layoutId == 0) R.layout.dialog_normal_layout else layoutId, null)
            dialog.addContentView(layout, LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            // set the dialog title
            if (!TextUtils.isEmpty(title)) {
                (layout.findViewById(R.id.title) as TextView).text = title
            } else {
                (layout.findViewById(R.id.title) as TextView).visibility = View.GONE
            }
            // set the confirm button
            if (positiveButtonText != null) {
                (layout.findViewById(R.id.positiveButton) as Button).text = positiveButtonText
                if (positiveButtonClickListener != null) {
                    (layout.findViewById(R.id.positiveButton) as Button)
                            .setOnClickListener {
                                positiveButtonClickListener!!.onClick(dialog,
                                        DialogInterface.BUTTON_POSITIVE)
                            }
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).visibility = View.GONE
            }
            // set the cancel button
            if (negativeButtonText != null) {
                (layout.findViewById(R.id.negativeButton) as Button).text = negativeButtonText
                if (negativeButtonClickListener != null) {
                    (layout.findViewById(R.id.negativeButton) as Button)
                            .setOnClickListener {
                                negativeButtonClickListener!!.onClick(dialog,
                                        DialogInterface.BUTTON_NEGATIVE)
                            }
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).visibility = View.GONE
            }
            // set the content message
            if (message != null) {
                (layout.findViewById(R.id.message) as TextView).text = message
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                (layout.findViewById(R.id.content) as LinearLayout)
                        .removeAllViews()
                (layout.findViewById(R.id.content) as LinearLayout)
                        .addView(contentView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
            dialog.setContentView(layout)
            return dialog
        }

        @JvmOverloads
        fun show(layoutId: Int = 0) {
            val dialog = this.create(layoutId)
            val params = dialog.window!!.attributes
            params.y = ScreenUtils.dpToPxInt(context, -40f)
            //点击空白不消失
            dialog.setCanceledOnTouchOutside(false)
            dialog.window!!.attributes = params
            dialog.show()

            val dm = DisplayMetrics()
            //获取屏幕信息
            (context as Activity).windowManager.defaultDisplay.getMetrics(dm)
            val screenWidth = dm.widthPixels
            val screenHeigh = dm.heightPixels
            val params2 = dialog.window!!.attributes//获取dialog信息
            params2.width = (screenWidth / 1.2).toInt()
            params2.height = LayoutParams.WRAP_CONTENT
            dialog.window!!.attributes = params2//设置大小
        }
    }

}
