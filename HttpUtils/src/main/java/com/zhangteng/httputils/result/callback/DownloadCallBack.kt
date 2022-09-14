package com.zhangteng.httputils.result.callback

import com.zhangteng.utils.IStateView
import okhttp3.ResponseBody

/**
 * description: 下载回调
 * author: Swing
 * date: 2022/9/13
 */
abstract class DownloadCallBack<D>(
    protected var fileName: String? = null,
    iStateView: IStateView? = null
) : CommonCallBack<ResponseBody, D>(iStateView) {

    /**
     * 成功回调，有可能在子线程回调
     *
     * @param bytesRead     已经下载文件的大小
     * @param contentLength 文件的大小
     * @param progress      当前进度
     * @param done          是否下载完成
     * @param filePath      文件路径
     */
    protected abstract fun onSuccess(
        bytesRead: Long,
        contentLength: Long,
        progress: Float,
        done: Boolean,
        filePath: String?
    )
}