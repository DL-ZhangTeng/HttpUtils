package com.zhangteng.httputils.result.rxjava.transformer

import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.utils.i
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
open class LifecycleObservableTransformer<T> : ObservableTransformer<T, T> {
    private var tag: Any? = null
    private var disposable: Disposable? = null

    constructor()
    constructor(tag: Any?) {
        this.tag = tag
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { d: Disposable ->
                disposable = d
                if (tag == null) {
                    HttpUtils.instance.addDisposable(d)
                } else {
                    HttpUtils.instance.addDisposable(d, tag)
                }
                if (tag is LifecycleOwner) {
                    HttpLifecycleEventObserver.bind(tag as LifecycleOwner)
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                //页面销毁状态自动取消网络请求
                if (tag is LifecycleOwner && HttpLifecycleEventObserver.isLifecycleDestroy(tag as LifecycleOwner?)) {
                    //观察者会清理全部请求
                    disposable = null
                } else if (disposable != null) {
                    //主动取消并清理请求集合
                    try {
                        HttpUtils.instance.cancelSingleRequest(disposable!!)
                    } catch (e: IllegalStateException) {
                        e.message.i("cancelSingleRequest")
                    } finally {
                        disposable = null
                    }
                }
            }
    }
}