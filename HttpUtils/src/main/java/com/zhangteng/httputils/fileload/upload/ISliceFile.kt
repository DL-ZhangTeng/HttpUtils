package com.zhangteng.httputils.fileload.upload

/**
 * description: 校验或分片文件上传响应体
 * author: Swing
 * date: 2022/11/7
 */
interface ISliceFile {
    /**
     * description 文件资源id
     */
    fun getSourceId(): String?

    /**
     * description 文件路径
     */
    fun getSourcePath(): String?

    /**
     * description 文件是否存在,校验文件是否上传完成时使用
     */
    fun isFileExists(): Boolean?

    /**
     * description 已上传的分片序号,校验文件是否上传完成时使用
     */
    fun getSliceList(): ArrayList<Int>?
}