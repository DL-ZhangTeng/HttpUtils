package com.zhangteng.httputils.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.work.*
import com.zhangteng.httputils.fileload.upload.UploadFileSliceApi
import com.zhangteng.httputils.fileload.upload.UploadRetrofit
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.utils.FileSliceUtils
import com.zhangteng.utils.IResponse
import com.zhangteng.utils.MD5Util.getFileMD5
import com.zhangteng.utils.getFileOrFilesSize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

/**
 * description: 文件分片上传
 * author: Swing
 * date: 2022/11/7
 */
class UploadManager<T : UploadManager.ISliceFile, R : IResponse<T>> private constructor(var builder: Builder<T, R>) {

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0x123) {
                checkFile()
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
                builder.fileMD5 = getFileMD5(builder.filePath)
                mHandler.sendEmptyMessage(0x123)
            }
        }.start()
    }

    /**
     * description 使用WorkManager方式开启上传，实现了分片上传、断网重连
     */
    fun startByWorker() {
        //构建上传任务
        val data = Data.Builder()
        data.putString(UploadWorker.UPLOAD_WORKER_CHECK_URL, builder.checkUrl)
        data.putString(UploadWorker.UPLOAD_WORKER_REQUEST_URL, builder.uploadUrl)
        data.putString(UploadWorker.UPLOAD_WORKER_FILE_PATH, builder.filePath)
        data.putLong(UploadWorker.UPLOAD_WORKER_SLICE_SIZE, builder.sliceFileSize!!)

        val requestBuilder =
            OneTimeWorkRequest.Builder(UploadWorker::class.java).setInputData(data.build())

        if (builder.isNetworkReconnect) {
            //网络连接正常时执行
            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            requestBuilder.setConstraints(constraints)
        }
        val request = requestBuilder.build()

        val manager = WorkManager.getInstance(HttpUtils.instance.context!!)
        manager.getWorkInfoByIdLiveData(request.id)
            .observeForever { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        val filePath =
                            workInfo.outputData.getString(UploadWorker.UPLOAD_WORKER_FILE_PATH)
                        val sourceId =
                            workInfo.outputData.getString(UploadWorker.UPLOAD_WORKER_SOURCE_ID)
                        val progress =
                            workInfo.outputData.getFloat(
                                UploadWorker.UPLOAD_WORKER_PROGRESS,
                                100f
                            )
                        val completed =
                            workInfo.outputData.getInt(
                                UploadWorker.UPLOAD_WORKER_COMPLETED,
                                0
                            )
                        val totalSize =
                            workInfo.outputData.getInt(UploadWorker.UPLOAD_WORKER_TOTAL, 0)
                        if (filePath.isNullOrEmpty()) {
                            builder.onUpLoadListener?.onError(Exception("上传失败，请稍后重试"))
                            Log.i("UploadManager", "上传失败，请稍后重试")
                        } else {
                            builder.onUpLoadListener?.onUpload(
                                completed,
                                totalSize,
                                progress,
                                completed == totalSize && progress == 100f,
                                filePath,
                                sourceId
                            )
                            builder.onUpLoadListener?.onComplete(File(filePath), sourceId)
                            Log.i("UploadManager", "正在上传：进度$progress 完成$completed 大小$totalSize")
                            Log.i("UploadManager", "上传完成")
                        }
                    }
                    WorkInfo.State.RUNNING -> {
                        val filePath =
                            workInfo.progress.getString(UploadWorker.UPLOAD_WORKER_FILE_PATH)
                        val sourceId =
                            workInfo.progress.getString(UploadWorker.UPLOAD_WORKER_SOURCE_ID)
                        val progress =
                            workInfo.progress.getFloat(UploadWorker.UPLOAD_WORKER_PROGRESS, 0f)
                        val completed =
                            workInfo.progress.getInt(UploadWorker.UPLOAD_WORKER_COMPLETED, 0)
                        val totalSize =
                            workInfo.progress.getInt(UploadWorker.UPLOAD_WORKER_TOTAL, 0)
                        if (!filePath.isNullOrEmpty()) {
                            builder.onUpLoadListener?.onUpload(
                                completed,
                                totalSize,
                                progress,
                                completed == totalSize && progress == 100f,
                                filePath,
                                sourceId
                            )
                            Log.i("UploadManager", "正在上传：进度$progress 完成$completed 大小$totalSize")
                        }
                    }
                    WorkInfo.State.ENQUEUED -> {
                        val filePath =
                            workInfo.outputData.getString(UploadWorker.UPLOAD_WORKER_FILE_PATH)
                        val sourceId =
                            workInfo.progress.getString(UploadWorker.UPLOAD_WORKER_SOURCE_ID)
                        val totalSize =
                            workInfo.outputData.getInt(UploadWorker.UPLOAD_WORKER_TOTAL, 0)
                        builder.onUpLoadListener?.start()
                        builder.onUpLoadListener?.onUpload(
                            0,
                            totalSize,
                            0f,
                            false,
                            filePath,
                            sourceId
                        )
                        Log.i("UploadManager", "等待上传")
                    }
                    WorkInfo.State.BLOCKED -> {
                        val filePath =
                            workInfo.outputData.getString(UploadWorker.UPLOAD_WORKER_FILE_PATH)
                        val sourceId =
                            workInfo.progress.getString(UploadWorker.UPLOAD_WORKER_SOURCE_ID)
                        val totalSize =
                            workInfo.outputData.getInt(UploadWorker.UPLOAD_WORKER_TOTAL, 0)
                        if (!filePath.isNullOrEmpty()) {
                            builder.onUpLoadListener?.onUpload(
                                0,
                                totalSize,
                                -1f,
                                false,
                                filePath,
                                sourceId
                            )
                            Log.i("UploadManager", "上传阻塞")
                        }
                    }
                    WorkInfo.State.CANCELLED -> {
                        builder.onUpLoadListener?.onError(Exception("取消上传"))
                        Log.i("UploadManager", "取消上传")
                    }
                    WorkInfo.State.FAILED -> {
                        builder.onUpLoadListener?.onError(Exception("上传失败，请稍后重试"))
                        Log.i("UploadManager", "上传失败，请稍后重试")
                    }
                }
            }
        manager.enqueue(request)
    }

    /**
     * description 校验文件
     */
    private fun checkFile() {
        val response: Response<R> = UploadRetrofit.instance
            .retrofit
            .create(UploadFileSliceApi::class.java)
            .checkFile<T, R>(
                builder.checkUrl,
                builder.busType,
                builder.fileMD5,
                builder.fileName,
                builder.filePath.getFileOrFilesSize(1).toLong()
            )
            .execute()
        if (response.isSuccessful) {
            val iResponse = response.body()
            if (iResponse?.isSuccess() == true) {
                if (iResponse.getResult().isFileExists() == true) {
                    builder.onUpLoadListener?.onComplete(
                        File(builder.filePath!!),
                        iResponse.getResult().getSourceId()
                    )
                } else {
                    builder.hasUploadFileNum.clear()
                    if (null != iResponse.getResult().getSliceList() && iResponse.getResult()
                            .getSliceList()!!.isNotEmpty()
                    ) {
                        builder.hasUploadFileNum.addAll(
                            iResponse.getResult().getSliceList() ?: ArrayList()
                        )
                    }
                    try {
                        builder.sliceFileCount =
                            builder.fileSliceUtils.splitFile(
                                File(builder.filePath!!),
                                builder.sliceFileSize!!
                            )
                        builder.sliceFileList = builder.fileSliceUtils.sliceFiles
                        uploadFile(0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * description 上传文件片
     * @param fileNum 片序号
     */
    private fun uploadFile(fileNum: Int) {
        var isRepeat = false
        if (builder.hasUploadFileNum.size > 0) {
            //之前传过的num就不用传
            for (uploadFileNum in builder.hasUploadFileNum) {
                if (fileNum == uploadFileNum) {
                    isRepeat = true
                }
            }
        }
        if (isRepeat && builder.hasUploadFileNum.size > 0) {
            uploadFile(fileNum + 1)
        } else {
            val file = builder.sliceFileList[fileNum]
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(builder.fileName ?: "", file.name, requestFile)

            val response: Response<R> = UploadRetrofit.instance
                .retrofit
                .create(UploadFileSliceApi::class.java)
                .uploadFile<T, R>(
                    builder.uploadUrl,
                    body,
                    builder.busType,
                    builder.fileMD5,
                    fileNum,
                    builder.sliceFileCount,
                    builder.sliceFileSize
                )
                .execute()

            if (response.isSuccessful) {
                val iResponse = response.body()
                if (iResponse?.isSuccess() == true) {
                    if (fileNum < builder.sliceFileCount!!.minus(1)) {
                        builder.onUpLoadListener?.onUpload(
                            fileNum + 1,
                            builder.sliceFileCount!!,
                            (fileNum + 1) * 100f / builder.sliceFileCount!!,
                            fileNum + 1 == builder.sliceFileCount!!,
                            builder.filePath,
                            iResponse.getResult().getSourceId()
                        )
                        uploadFile(fileNum + 1)
                    } else {   //上传完成
                        builder.onUpLoadListener?.onComplete(
                            File(builder.filePath!!),
                            iResponse.getResult().getSourceId()
                        )
                        builder.filePath = ""
                        builder.fileName = ""
                        builder.busType = ""
                        builder.fileMD5 = ""
                        builder.sliceFileCount = 0
                        builder.hasUploadFileNum.clear()
                        builder.sliceFileList.clear()
                        builder.fileSliceUtils.deleteSliceFiles()
                    }
                } else {
                    builder.onUpLoadListener?.onError(Exception(iResponse?.getMsg()))
                    builder.filePath = ""
                    builder.fileName = ""
                    builder.busType = ""
                    builder.fileMD5 = ""
                    builder.sliceFileCount = 0
                    builder.hasUploadFileNum.clear()
                    builder.sliceFileList.clear()
                    builder.fileSliceUtils.deleteSliceFiles()
                }
            } else {
                builder.onUpLoadListener?.onError(Exception(response.errorBody()?.string()))
                builder.filePath = ""
                builder.fileName = ""
                builder.busType = ""
                builder.fileMD5 = ""
                builder.sliceFileCount = 0
                builder.hasUploadFileNum.clear()
                builder.sliceFileList.clear()
                builder.fileSliceUtils.deleteSliceFiles()
            }
        }
    }

    class Builder<T : ISliceFile, R : IResponse<T>> {
        /**
         * description: 文件完整性校验路径，确认文件已上传完成
         *              必需
         */
        var checkUrl: String? = null

        /**
         * description: 分片上传路径
         *              必需
         */
        var uploadUrl: String? = null

        /**
         * description: 文件路径
         *              必需
         */
        var filePath: String? = null

        /**
         * description: 切割文件大小
         *              必需
         */
        var sliceFileSize: Long? = null

        /**
         * description: 文件名
         */
        var fileName: String? = null

        /**
         * description: 文件类型
         */
        var busType: String? = null

        /**
         * description: 文件md5
         */
        var fileMD5: String? = null

        /**
         * description: 校验的返回的已上传的分片序号，已存在的分片无需再上传，可跳过循环
         */
        var hasUploadFileNum: ArrayList<Int> = ArrayList()

        /**
         * description: 文件切割工具类
         */
        var fileSliceUtils: FileSliceUtils = FileSliceUtils()

        /**
         * description: 切割文件个数
         */
        var sliceFileCount: Int? = null

        /**
         * description: 分片文件集合
         */
        var sliceFileList: ArrayList<File> = ArrayList()

        /**
         * description: 是否需要断网重连 默认不重连，断点续传上传参数
         */
        var isNetworkReconnect: Boolean = false

        /**
         * description: 文件上传回调
         */
        var onUpLoadListener: OnUpLoadListener? = null

        fun build(): UploadManager<T, R> {
            if (checkUrl == null) {
                Log.e("UploadManager", "The checkUrl cannot be empty")
                throw Exception("The checkUrl cannot be empty")
            }
            if (uploadUrl == null) {
                Log.e("UploadManager", "The uploadUrl cannot be empty")
                throw Exception("The uploadUrl cannot be empty")
            }
            if (filePath == null) {
                Log.e("UploadManager", "The filePath cannot be empty")
                throw Exception("The filePath cannot be empty")
            }
            if (sliceFileSize == null) {
                Log.e("UploadManager", "The sliceFileSize cannot be empty")
                throw Exception("The sliceFileSize cannot be empty")
            }

            if (fileName == null) {
                fileName = filePath!!.substring(filePath!!.lastIndexOf("/") + 1)
            }
            if (busType == null) {
                busType = filePath!!.substring(filePath!!.lastIndexOf(".") + 1)
            }
            return UploadManager(this)
        }
    }

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

    /**
     * description: 校验或分片文件上传响应体
     * author: Swing
     * date: 2022/11/7
     */
    interface ISliceFile {
        /**
         * description 文件是否存在
         */
        fun isFileExists(): Boolean?

        /**
         * description 文件资源id
         */
        fun getSourceId(): String?

        /**
         * description 文件路径
         */
        fun getSourcePath(): String?

        /**
         * description 文件分片列表
         */
        fun getSliceList(): ArrayList<Int>?
    }
}