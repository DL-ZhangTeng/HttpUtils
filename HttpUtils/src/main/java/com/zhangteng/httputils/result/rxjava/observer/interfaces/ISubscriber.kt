package com.zhangteng.httputils.result.rxjava.observer.interfaces

import com.zhangteng.utils.IException
import io.reactivex.disposables.Disposable

/**
 * Created by swing on 2018/4/24.
 */
interface ISubscriber<T> {
    fun doOnSubscribe(d: Disposable)
    fun doOnError(iException: IException)
    fun doOnNext(t: T)
    fun doOnCompleted()
}