package com.zhangteng.httputils.fileload.upload

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zhangteng.utils.FileSliceUtils
import com.zhangteng.utils.IResponse
import com.zhangteng.utils.MD5Util
import com.zhangteng.utils.getFileOrFilesSize
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

/**
 * description: 上传任务
 * author: Swing
 * date: 2022/11/5
 */
class UploadWorker<T : ISliceFile, R : IResponse<T>>(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    /**
     * description: 文件完整性校验路径，确认文件已上传完成
     */
    var checkUrl: String? = null

    /**
     * description: 分片上传路径
     */
    var uploadUrl: String? = null

    /**
     * description: 文件路径
     */
    var filePath: String? = null

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
     * description: 切割文件大小
     */
    var sliceFileSize: Long? = null

    /**
     * description: 分片文件集合
     */
    var sliceFileList: ArrayList<File> = ArrayList()

    override fun doWork(): Result {
        val inputData = inputData
        checkUrl = inputData.getString(UPLOAD_WORKER_CHECK_URL)
        uploadUrl = inputData.getString(UPLOAD_WORKER_REQUEST_URL)
        filePath = inputData.getString(UPLOAD_WORKER_FILE_PATH)
        sliceFileSize = inputData.getLong(UPLOAD_WORKER_SLICE_SIZE, 0)

        if (checkUrl.isNullOrEmpty()) return Result.failure()
        if (uploadUrl.isNullOrEmpty()) return Result.failure()
        if (filePath.isNullOrEmpty()) return Result.failure()
        if (sliceFileSize!! <= 0) return Result.failure()

        fileName = filePath!!.substring(filePath!!.lastIndexOf("/") + 1)
        busType = filePath!!.substring(filePath!!.lastIndexOf(".") + 1)
        fileMD5 = MD5Util.getFileMD5(filePath)

        return checkFile()
    }

    /**
     * description 校验文件
     */
    private fun checkFile(): Result {
        val response: Response<R> = UploadRetrofit.instance
            .retrofit
            .create(UploadFileSliceApi::class.java)
            .checkFile<T, R>(
                checkUrl,
                busType,
                fileMD5,
                fileName,
                filePath.getFileOrFilesSize(1).toLong()
            )
            .execute()
        if (response.isSuccessful) {
            val iResponse = response.body()
            if (iResponse?.isSuccess() == true) {
                if (iResponse.getResult().isFileExists() == true) {
                    return Result.success(
                        getData(
                            -1,
                            sliceFileCount,
                            filePath,
                            iResponse.getResult().getSourceId()
                        )
                    )
                } else {
                    hasUploadFileNum.clear()
                    if (null != iResponse.getResult().getSliceList() && iResponse.getResult()
                            .getSliceList()!!.isNotEmpty()
                    ) {
                        hasUploadFileNum.addAll(
                            iResponse.getResult().getSliceList() ?: ArrayList()
                        )
                    }
                    return try {
                        sliceFileCount =
                            fileSliceUtils.splitFile(
                                File(filePath!!),
                                sliceFileSize!!
                            )
                        sliceFileList = fileSliceUtils.sliceFiles
                        uploadFile(0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.failure()
                    }
                }
            } else {
                return Result.failure()
            }
        } else {
            return Result.failure()
        }
    }

    /**
     * description 上传文件片
     * @param fileNum 片序号
     */
    private fun uploadFile(fileNum: Int): Result {
        var isRepeat = false
        if (hasUploadFileNum.size > 0) {
            //之前传过的num就不用传
            for (uploadFileNum in hasUploadFileNum) {
                if (fileNum == uploadFileNum) {
                    isRepeat = true
                }
            }
        }
        if (isRepeat && hasUploadFileNum.size > 0) {
            return uploadFile(fileNum + 1)
        } else {
            val file = sliceFileList[fileNum]
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData(fileName ?: "", file.name, requestFile)

            val response: Response<R> = UploadRetrofit.instance
                .retrofit
                .create(UploadFileSliceApi::class.java)
                .uploadFile<T, R>(
                    uploadUrl,
                    body,
                    busType,
                    fileMD5,
                    fileNum,
                    sliceFileCount,
                    sliceFileSize
                )
                .execute()

            if (response.isSuccessful) {
                val iResponse = response.body()
                if (iResponse?.isSuccess() == true) {
                    if (fileNum < sliceFileCount!!.minus(1)) {
                        setProgressAsync(
                            getData(
                                fileNum + 1,
                                sliceFileCount!!,
                                filePath,
                                iResponse.getResult().getSourceId()
                            )
                        )
                        return uploadFile(fileNum + 1)
                    } else {
                        //上传完成
                        return Result.success(
                            getData(
                                fileNum,
                                sliceFileCount,
                                filePath,
                                iResponse.getResult().getSourceId()
                            )
                        )
                    }
                } else {
                    return Result.failure()
                }
            } else {
                return Result.failure()
            }
        }
    }

    /**
     * 获取Data
     *
     * @param currentNum    当前上传文件编号
     * @param allNum        全部文件编号
     * @param filePath      文件路径
     */
    private fun getData(
        currentNum: Int?,
        allNum: Int?,
        filePath: String?,
        sourceId: String?
    ): Data {
        Log.i("UploadWorker", "正在下载：完成$currentNum 大小$allNum")
        val builder: Data.Builder = Data.Builder()
        builder.putString(UPLOAD_WORKER_FILE_PATH, filePath)
        builder.putString(UPLOAD_WORKER_SOURCE_ID, sourceId)
        builder.putFloat(UPLOAD_WORKER_PROGRESS, (currentNum ?: 0) * 100f / (allNum ?: 1))
        builder.putInt(UPLOAD_WORKER_COMPLETED, currentNum ?: 0)
        builder.putInt(UPLOAD_WORKER_TOTAL, allNum ?: 1)
        return builder.build()
    }

    companion object {
        const val UPLOAD_WORKER_CHECK_URL = "upload_worker_check_url"
        const val UPLOAD_WORKER_REQUEST_URL = "upload_worker_request_url"
        const val UPLOAD_WORKER_SLICE_SIZE = "upload_worker_slice_size"

        const val UPLOAD_WORKER_FILE_PATH = "upload_worker_file_path"
        const val UPLOAD_WORKER_SOURCE_ID = "upload_worker_source_id"
        const val UPLOAD_WORKER_PROGRESS = "upload_worker_progress"
        const val UPLOAD_WORKER_COMPLETED = "upload_worker_completed"
        const val UPLOAD_WORKER_TOTAL = "upload_worker_total"
    }
}