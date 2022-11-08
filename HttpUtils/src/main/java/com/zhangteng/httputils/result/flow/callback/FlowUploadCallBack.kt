package com.zhangteng.httputils.result.flow.callback

import com.zhangteng.httputils.fileload.upload.ISliceFile
import com.zhangteng.httputils.lifecycle.cancelSingleRequest
import com.zhangteng.httputils.result.callback.UploadCallBack
import com.zhangteng.utils.IException
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.IResponse
import kotlin.coroutines.CoroutineContext

/**
 * description: Flow上传回调
 * author: Swing
 * date: 2022/11/8
 */
abstract class FlowUploadCallBack<T : ISliceFile, R : IResponse<T>>(
    currentNum: Int = 0,
    allNum: Int = 1,
    iLoadingView: ILoadingView? = null
) : UploadCallBack<T, R, CoroutineContext>(currentNum, allNum, iLoadingView) {

    override fun doOnError(iException: IException) {
        super.doOnError(iException)
        disposable.cancelSingleRequest()
    }

    override fun doOnCompleted() {
        super.doOnCompleted()
        disposable.cancelSingleRequest()
    }

    override fun onSuccess(t: R) {
        if (t.isSuccess()) {
            onSuccess(
                currentNum,
                allNum,
                (currentNum + 1) * 100f / allNum,
                true,
                t.getResult().getSourcePath(),
                t.getResult().getSourceId()
            )
        } else {
            onSuccess(
                currentNum,
                allNum,
                currentNum * 100f / allNum,
                false,
                t.getResult().getSourcePath(),
                t.getResult().getSourceId()
            )
        }
    }
}