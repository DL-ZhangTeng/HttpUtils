package com.zhangteng.httputils.result.rxjava.transformer

import android.app.Dialog
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * description: 带加载中动画处理的数据源转换器
 * author: Swing
 * date: 2021/10/9
 */
class ProgressDialogObservableTransformer<T> : ObservableTransformer<T, T> {
    private var mProgressDialog: Dialog? = null

    constructor()
    constructor(mProgressDialog: Dialog?) {
        this.mProgressDialog = mProgressDialog
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                if (mProgressDialog != null && !mProgressDialog!!.isShowing) {
                    mProgressDialog!!.show()
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                }
            }
    }
}