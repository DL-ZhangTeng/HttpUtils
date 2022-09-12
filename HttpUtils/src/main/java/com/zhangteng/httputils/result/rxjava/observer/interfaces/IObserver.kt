package com.zhangteng.httputils.result.rxjava.observer.interfaces

import com.zhangteng.httputils.result.interfaces.ISubscriber
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
interface IObserver<T> : ISubscriber<T> {
    fun doOnSubscribe(d: Disposable)
}