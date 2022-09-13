package com.zhangteng.httputils.result.callback.interfaces

import com.zhangteng.utils.IException

/**
 * Created by swing on 2018/4/24.
 */
interface ICallBack<T, D> {
    fun doOnSubscribe(d: D)
    fun doOnError(iException: IException)
    fun doOnNext(t: T)
    fun doOnCompleted()
}