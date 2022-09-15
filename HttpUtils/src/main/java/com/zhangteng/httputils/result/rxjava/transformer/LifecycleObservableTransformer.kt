package com.zhangteng.httputils.result.rxjava.transformer

import androidx.lifecycle.LifecycleOwner
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.lifecycle.HttpLifecycleEventObserver
import com.zhangteng.utils.ILoadingView
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
open class LifecycleObservableTransformer<T>(private var iLoadingView: ILoadingView?) :
    ObservableTransformer<T, T> {
    private var disposable: Disposable? = null

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { d: Disposable ->
                disposable = d
                if (iLoadingView == null) {
                    HttpUtils.instance.addDisposable(d)
                } else {
                    HttpUtils.instance.addDisposable(d, iLoadingView)
                }
                if (iLoadingView is LifecycleOwner) {
                    HttpLifecycleEventObserver.bind(iLoadingView as LifecycleOwner)
                }
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                //页面销毁状态自动取消网络请求
                if (iLoadingView is LifecycleOwner && HttpLifecycleEventObserver.isLifecycleDestroy(
                        iLoadingView as LifecycleOwner?
                    )
                ) {
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