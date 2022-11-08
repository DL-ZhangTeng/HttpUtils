package com.zhangteng.httputils.fileload.upload

import java.io.File

/**
 * description: 上传回调
 * author: Swing
 * date: 2022/11/7
 */
interface OnUpLoadListener {
    fun start()

    /**
     * 上传进度监听
     *
     * @param currentNum     当前上传文件编号
     * @param allNum 全部文件编号
     * @param progress      当前进度，0：任务待开始;100：上传成功
     * @param done          是否上传完成，false：上传中；true：上传成功
     * @param filePath      文件路径
     * @param sourceId      文件资源id
     */
    fun onUpload(
        currentNum: Int,
        allNum: Int,
        progress: Float,
        done: Boolean,
        filePath: String?,
        sourceId: String?
    )

    fun onComplete(file: File?, sourceId: String?)

    fun onError(e: Exception)
}