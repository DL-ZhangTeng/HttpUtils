package com.zhangteng.httputils.result.coroutine.callback

import com.zhangteng.httputils.fileload.upload.ISliceFile
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.UploadCallBack
import com.zhangteng.httputils.fileload.upload.UploadManager
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.IResponse
import kotlinx.coroutines.Deferred

/**
 * description: Deferred上传回调
 * author: Swing
 * date: 2022/11/8
 */
abstract class DeferredUploadCallBack<T : ISliceFile, R : IResponse<T>>(
    currentNum: Int = 1,
    allNum: Int = 1,
    iLoadingView: ILoadingView? = null
) : UploadCallBack<T, R, Deferred<R>>(currentNum, allNum, iLoadingView) {

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }

    override fun onSuccess(t: R) {
        if (t.isSuccess()) {
            if (t.getResult().isFileExists() == true) {
                onSuccess(
                    currentNum,
                    allNum,
                    100f,
                    true,
                    t.getResult().getSourcePath(),
                    t.getResult().getSourceId()
                )
            } else {
                onSuccess(
                    currentNum,
                    allNum,
                    0f,
                    false,
                    t.getResult().getSourcePath(),
                    t.getResult().getSourceId()
                )
            }
        } else {
            onSuccess(
                currentNum,
                allNum,
                0f,
                false,
                t.getResult().getSourcePath(),
                t.getResult().getSourceId()
            )
        }
    }
}