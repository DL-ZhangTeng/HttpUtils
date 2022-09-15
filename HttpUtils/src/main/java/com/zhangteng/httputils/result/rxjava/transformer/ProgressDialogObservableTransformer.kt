package com.zhangteng.httputils.result.rxjava.transformer

import com.zhangteng.utils.ILoadingView
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
class ProgressDialogObservableTransformer<T>(private var iLoadingView: ILoadingView?) :
    ObservableTransformer<T, T> {

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                iLoadingView?.showProgressDialog()
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                iLoadingView?.dismissProgressDialog()
            }
    }
}