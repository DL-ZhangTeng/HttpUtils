package com.zhangteng.httputils.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.zhangteng.httputils.fileload.upload.UploadFileSliceApi
import com.zhangteng.httputils.fileload.upload.UploadRetrofit
import com.zhangteng.utils.FileSliceUtils
import com.zhangteng.utils.IResponse
import com.zhangteng.utils.MD5Util.getFileMD5
import com.zhangteng.utils.getFileOrFilesSize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.Serializable

/**
 * description: 文件分片上传
 * author: Swing
 * date: 2022/11/7
 */
class UploadManager private constructor(var builder: Builder) {

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x123) {
                checkFile(
                    builder.checkUrl,
                    builder.uploadUrl,
                    builder.getFilePath,
                    builder.getFileName,
                    builder.getBusType
                )
            }
        }
    }

    /**
     * description 开始分片上传
     */
    fun start() {
        builder.onUpLoadListener?.start()
        object : Thread() {
            override fun run() {
                //文件转md5耗时操作，需要在子线程执行
                builder.fileMD5 = getFileMD5(builder.getFilePath)
                mHandler.sendEmptyMessage(0x123)
            }
        }.start()
    }

    /**
     * description 校验文件
     * @param checkUrl 校验路径
     * @param uploadUrl 上传路径
     * @param mFilePath 文件路径
     * @param mFileName 文件名
     * @param mBusType 文件类型
     * @return
     */
    private fun checkFile(
        checkUrl: String?,
        uploadUrl: String?,
        mFilePath: String?,
        mFileName: String?,
        mBusType: String?
    ) {
        val response: IResponse<SliceFileEntity> = UploadRetrofit.instance
            .retrofit
            .create(UploadFileSliceApi::class.java)
            .checkFile(
                checkUrl,
                mBusType,
                builder.fileMD5,
                mFileName,
                mFilePath.getFileOrFilesSize(1).toLong()
            )
        if (response.isSuccess()) {
            if (response.getResult().isFileExists) {
                builder.onUpLoadListener?.onComplete(response.getResult().sourceId)
            } else {
                builder.haveUploadFileNum = ArrayList()
                if (null != response.getResult().chunkList && response.getResult().chunkList!!.isNotEmpty()) {
                    builder.haveUploadFileNum = response.getResult().chunkList ?: ArrayList()
                }
                try {
                    //分片的大小，可自定义
                    val mBufferSize = (1024 * 1024 * 2).toLong()
                    builder.fileSliceUtils = FileSliceUtils()
                    builder.sliceFileCount =
                        builder.fileSliceUtils.splitFile(File(mFilePath!!), mBufferSize)
                    builder.sliceFileList = builder.fileSliceUtils.sliceFiles
                    uploadFile(uploadUrl, 0, mBufferSize)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * description 上传文件片
     * @param uploadUrl 上传路径
     * @param fileNum 片序号
     */
    private fun uploadFile(uploadUrl: String?, fileNum: Int, mBufferSize: Long) {
        var isRepeat = false
        if (builder.haveUploadFileNum.size > 0) {
            //之前传过的num就不用传
            for (uploadFileNum in builder.haveUploadFileNum) {
                if (fileNum == uploadFileNum) {
                    isRepeat = true
                }
            }
        }
        if (isRepeat && builder.haveUploadFileNum.size > 0) {
            uploadFile(uploadUrl, fileNum + 1, mBufferSize)
        } else {
            val file = builder.sliceFileList[fileNum]
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(builder.getFileName ?: "", file.name, requestFile)

            val response: IResponse<SliceFileEntity> = UploadRetrofit.instance
                .retrofit
                .create(UploadFileSliceApi::class.java)
                .uploadFile(
                    uploadUrl,
                    body,
                    builder.getBusType,
                    builder.fileMD5,
                    fileNum,
                    builder.sliceFileCount,
                    mBufferSize
                )

            if (response.isSuccess()) {
                if (fileNum < (builder.sliceFileCount?.minus(1) ?: 0)) {
                    builder.onUpLoadListener?.onUpload(
                        fileNum + 1,
                        builder.sliceFileCount ?: 0
                    )
                    uploadFile(uploadUrl, fileNum + 1, mBufferSize)
                } else {   //上传完成
                    builder.onUpLoadListener?.onComplete(response.getResult().sourceId)
                    builder.getFilePath = ""
                    builder.getFileName = ""
                    builder.getBusType = ""
                    builder.fileMD5 = ""
                    builder.sliceFileCount = 0
                    builder.haveUploadFileNum.clear()
                    builder.sliceFileList.clear()
                    builder.fileSliceUtils.deleteSliceFiles()
                }
            } else {
                builder.onUpLoadListener?.onUploadFailed(response.getMsg())
                builder.getFilePath = ""
                builder.getFileName = ""
                builder.getBusType = ""
                builder.fileMD5 = ""
                builder.sliceFileCount = 0
                builder.haveUploadFileNum.clear()
                builder.sliceFileList.clear()
                builder.fileSliceUtils.deleteSliceFiles()
            }
        }
    }

    class Builder internal constructor() {
        var checkUrl: String? = null
        var uploadUrl: String? = null
        var getFilePath: String? = null
        var getFileName: String? = null
        var getBusType: String? = null
        var fileMD5: String? = null

        /**
         * description: 校验的返回的已上传的分片序号，已存在的分片无需再上传，可跳过循环
         */
        var haveUploadFileNum: ArrayList<Int> = ArrayList()

        /**
         * description: 文件切割工具类
         */
        var fileSliceUtils: FileSliceUtils = FileSliceUtils()

        /**
         * description: 切割文件个数
         */
        var sliceFileCount: Int? = null
        var sliceFileList: ArrayList<File> = ArrayList()
        var onUpLoadListener: OnUpLoadListener? = null

        fun build(): UploadManager {
            return UploadManager(this)
        }
    }

    interface OnUpLoadListener {
        fun start()
        fun onUpload(currentNum: Int, allNum: Int)
        fun onComplete(fileSourceId: Int)
        fun onUploadFailed(errorMessage: String?)
    }

    class SliceFileEntity : Serializable {
        var isFileExists = false
        var sourceId = 0
        var sourcePath: String? = null
        var chunkList: ArrayList<Int>? = null
    }
}