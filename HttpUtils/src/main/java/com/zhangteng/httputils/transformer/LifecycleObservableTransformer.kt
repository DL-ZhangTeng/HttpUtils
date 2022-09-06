package com.zhangteng.httputils.transformer

import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * description: 带生命周期处理的数据源转换器
 * author: Swing
 * date: 2021/10/9
 */
class LifecycleObservableTransformer<T> : ObservableTransformer<T, T> {
    private var tag: Any? = null
    private var disposable: Disposable? = null

    constructor() {}
    constructor(tag: Any?) {
        this.tag = tag
        if (tag is LifecycleOwner) {
            HttpLifecycleEventObserver.bind(tag)
        }
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { d: Disposable? ->
                disposable = d
                if (tag == null) {
                    HttpUtils.instance.addDisposable(d)
                } else {
                    HttpUtils.instance.addDisposable(d, tag)
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                if (disposable != null) {
                    HttpUtils.instance.cancelSingleRequest(disposable)
                    disposable = null
                }
            }
    }
}