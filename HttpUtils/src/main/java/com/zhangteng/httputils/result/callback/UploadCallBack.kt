package com.zhangteng.httputils.result.callback

import com.zhangteng.httputils.fileload.upload.ISliceFile
import com.zhangteng.utils.ILoadingView
import com.zhangteng.utils.IResponse

/**
 * description: 上传回调
 * author: Swing
 * date: 2022/11/8
 */
abstract class UploadCallBack<T : ISliceFile, R : IResponse<T>, D>(
    protected var currentNum: Int = 0,
    protected var allNum: Int = 1,
    iLoadingView: ILoadingView? = null
) : CommonCallBack<R, D>(iLoadingView) {

    /**
     * 成功回调，有可能在子线程回调
     *
     * @param currentNum    当前上传文件编号
     * @param allNum        全部文件编号
     * @param progress      当前进度，0：任务待开始;100：上传成功
     * @param done          是否上传完成，false：上传中；true：上传成功
     * @param filePath      文件路径
     * @param sourceId      文件资源id
     */
    protected abstract fun onSuccess(
        currentNum: Int,
        allNum: Int,
        progress: Float,
        done: Boolean,
        filePath: String?,
        sourceId: String?
    )
}