package com.zhangteng.app.http.entity

import com.zhangteng.httputils.fileload.upload.ISliceFile

data class SliceFileBean(
    private val isFileExists: Boolean?,
    private val sourceId: String?,
    private val sourcePath: String?,
    private val sliceList: ArrayList<Int>?,
) : ISliceFile {
    override fun isFileExists(): Boolean? {
        return isFileExists
    }

    override fun getSourceId(): String? {
        return sourceId
    }

    override fun getSourcePath(): String? {
        return sourcePath
    }

    override fun getSliceList(): ArrayList<Int>? {
        return sliceList
    }
}