package com.zhangteng.httputils.result.interfaces

import com.zhangteng.utils.IException

/**
 * Created by swing on 2018/4/24.
 */
interface ISubscriber<T> {
    fun doOnError(iException: IException)
    fun doOnNext(t: T)
    fun doOnCompleted()
}