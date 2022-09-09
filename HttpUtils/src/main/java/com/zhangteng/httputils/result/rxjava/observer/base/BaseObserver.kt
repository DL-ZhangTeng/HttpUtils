package com.zhangteng.httputils.result.rxjava.observer.base

import com.zhangteng.httputils.result.rxjava.observer.interfaces.ISubscriber
import com.zhangteng.utils.IException.Companion.handleException
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
abstract class BaseObserver<T : Any> : Observer<T>, ISubscriber<T> {
    protected val isHideToast: Boolean
        get() = false

    override fun onSubscribe(d: Disposable) {
        doOnSubscribe(d)
    }

    override fun onNext(o: T) {
        doOnNext(o)
    }

    override fun onError(e: Throwable) {
        doOnError(handleException(e))
    }

    override fun onComplete() {
        doOnCompleted()
    }
}