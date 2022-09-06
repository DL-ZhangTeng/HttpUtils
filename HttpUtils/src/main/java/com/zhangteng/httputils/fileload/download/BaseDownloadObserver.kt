package com.zhangteng.httputils.fileload.download

import io.reactivex.Observer
import okhttp3.ResponseBody

/**
 * Created by swing on 2018/4/24.
 */
abstract class BaseDownloadObserver : Observer<ResponseBody?> {
    /**
     * 失败回调
     *
     * @param e 错误信息
     */
    protected abstract fun doOnError(e: Throwable?)
    override fun onError(e: Throwable) {
        doOnError(e)
    }
}