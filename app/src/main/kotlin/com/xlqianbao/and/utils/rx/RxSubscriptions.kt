package com.xlqianbao.and.utils.rx

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Makise on 2016/8/5.
 */
object RxSubscriptions {
    private val mSubscriptions = CompositeDisposable()

    val isUnsubscribed: Boolean
        get() = mSubscriptions.isDisposed

    fun add(d: Disposable?) {
        if (d != null) {
            mSubscriptions.add(d)
        }
    }

    fun remove(vararg d: Disposable) {
        for (disposable in d) {
            if (disposable != null) {
                mSubscriptions.remove(disposable)
            }
        }
    }

    fun clear() {
        mSubscriptions.clear()
    }

    fun unsubscribe() {
        mSubscriptions.dispose()
    }

    //    public static boolean hasSubscriptions() {
    //        return mSubscriptions.hasSubscriptions();
    //    }
}
