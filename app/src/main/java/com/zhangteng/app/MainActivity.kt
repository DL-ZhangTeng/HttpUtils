package com.zhangteng.app

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.zhangteng.app.http.Api
import com.zhangteng.app.http.BaseResult
import com.zhangteng.app.http.entity.HomeListBean
import com.zhangteng.app.http.entity.SliceFileBean
import com.zhangteng.httputils.fileload.download.DownloadManager
import com.zhangteng.httputils.fileload.download.OnDownloadListener
import com.zhangteng.httputils.fileload.upload.OnUpLoadListener
import com.zhangteng.httputils.fileload.upload.UploadManager
import com.zhangteng.httputils.gson.FailOverGson
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.result.coroutine.*
import com.zhangteng.httputils.result.coroutine.callback.DeferredCallBack
import com.zhangteng.httputils.result.coroutine.callback.DeferredDownloadCallBack
import com.zhangteng.httputils.result.coroutine.callback.DeferredUploadCallBack
import com.zhangteng.httputils.result.flow.callback.FlowCallBack
import com.zhangteng.httputils.result.flow.callback.FlowDownloadCallBack
import com.zhangteng.httputils.result.flow.callback.FlowUploadCallBack
import com.zhangteng.httputils.result.flow.flowGo
import com.zhangteng.httputils.result.flow.flowGoIResponse
import com.zhangteng.httputils.result.flow.launchGoFlowIResponse
import com.zhangteng.httputils.result.rxjava.observer.CommonObserver
import com.zhangteng.httputils.result.rxjava.observer.DownloadObserver
import com.zhangteng.httputils.result.rxjava.observer.UploadObserver
import com.zhangteng.httputils.result.rxjava.transformer.LifecycleObservableTransformer
import com.zhangteng.httputils.result.rxjava.transformer.ProgressDialogObservableTransformer
import com.zhangteng.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity(), IStateView {
    private val mStateViewHelper by lazy { createStateViewHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        downloadFileByDownloadManager()
    }

    override fun onDestroy() {
        super.onDestroy()
        HttpUtils.instance.cancelAllRequest()
    }

    fun launchGo_DeferredExUtils() {
        GlobalScope.launch {
            launchGo({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGo_DeferredExUtils")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGo_DeferredExUtils")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun launchGoIResponse_DeferredExUtils() {
        GlobalScope.launch {
            launchGoIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoIResponse_DeferredExUtils")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoIResponse_DeferredExUtils")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun launchGoDeferred() {
        GlobalScope.launch {
            launchGoDeferred({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeListByDeferred(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoDeferred")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoDeferred")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun launchGoDeferredIResponse() {
        GlobalScope.launch {
            launchGoDeferredIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeListByDeferred(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoDeferredIResponse")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoDeferredIResponse")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun deferredGo() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGo({
                    FailOverGson.failOverGson.toJson(it).e("deferredGo")
                }, {
                    FailOverGson.failOverGson.toJson(it).e("deferredGo")
                }, {

                },
                    this@MainActivity
                )
        }
    }

    fun deferredGoIResponse() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGoIResponse({
                    FailOverGson.failOverGson.toJson(it).e("deferredGoIResponse")
                }, {
                    FailOverGson.failOverGson.toJson(it).e("deferredGoIResponse")
                }, {

                },
                    this@MainActivity
                )
        }
    }

    fun deferredGo_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGo(object :
                    DeferredCallBack<BaseResult<HomeListBean>>(
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("deferredGo_ICallBack")
                    }

                    override fun onSuccess(t: BaseResult<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("deferredGo_ICallBack")
                    }
                })
        }
    }

    fun deferredGoIResponse_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByDeferred(0)
                .deferredGoIResponse(object :
                    DeferredCallBack<IResponse<HomeListBean>>(
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException)
                            .e("deferredGoIResponse_ICallBack")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("deferredGoIResponse_ICallBack")
                    }
                })
        }
    }

    fun launchGo_FlowExUtils() {
        GlobalScope.launch {
            com.zhangteng.httputils.result.flow.launchGo({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGo_FlowExUtils")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGo_FlowExUtils")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun launchGoIResponse_FlowExUtils() {
        GlobalScope.launch {
            com.zhangteng.httputils.result.flow.launchGoIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoIResponse_FlowExUtils")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoIResponse_FlowExUtils")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun launchGoFlow() {
        GlobalScope.launch {
            com.zhangteng.httputils.result.flow.launchGoFlow({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeListByFlow(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoFlow")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoFlow")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun launchGoFlowIResponse() {
        GlobalScope.launch {
            launchGoFlowIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeListByFlow(0)
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoFlowIResponse")
            }, {
                FailOverGson.failOverGson.toJson(it).e("launchGoFlowIResponse")
            }, {

            },
                this@MainActivity
            )
        }
    }

    fun flowGo() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGo({
                    FailOverGson.failOverGson.toJson(it).e("flowGo")
                }, {
                    FailOverGson.failOverGson.toJson(it).e("flowGo")
                }, {

                },
                    this@MainActivity
                )
        }
    }

    fun flowGoIResponse() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGoIResponse({
                    FailOverGson.failOverGson.toJson(it).e("flowGoIResponse")
                }, {
                    FailOverGson.failOverGson.toJson(it).e("flowGoIResponse")
                }, {

                },
                    this@MainActivity
                )
        }
    }

    fun flowGo_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGo(object :
                    FlowCallBack<BaseResult<HomeListBean>>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("flowGo_ICallBack")
                    }

                    override fun onSuccess(t: BaseResult<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("flowGo_ICallBack")
                    }
                })
        }
    }

    fun flowGoIResponse_ICallBack() {
        GlobalScope.launch {
            HttpUtils.instance.ConfigGlobalHttpUtils()
                .createService(Api::class.java)
                .getHomeListByFlow(0)
                .flowGoIResponse(object :
                    FlowCallBack<IResponse<HomeListBean>>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("flowGoIResponse_ICallBack")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        FailOverGson.failOverGson.toJson(t).e("flowGoIResponse_ICallBack")
                    }
                })
        }
    }

    fun observableGoCompose() {
        HttpUtils.instance
            .ConfigGlobalHttpUtils()
            .createService(Api::class.java)
            .getHomeListByObservable(0)
            //页面销毁自动取消请求
            .compose(LifecycleObservableTransformer(this@MainActivity))
            //自动处理网络加载中动画
            .compose(ProgressDialogObservableTransformer(this@MainActivity))
            .subscribe(object : CommonObserver<IResponse<HomeListBean>>() {
                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("rxjavaGo")
                }

                override fun onSuccess(t: IResponse<HomeListBean>) {
                    FailOverGson.failOverGson.toJson(t).e("rxjavaGo")
                }
            })
    }

    fun observableGoObserver() {
        HttpUtils.instance
            .ConfigGlobalHttpUtils()
            .createService(Api::class.java)
            .getHomeListByObservable(0)
            //页面销毁自动取消请求
            //自动处理网络加载中动画
            .subscribe(object : CommonObserver<IResponse<HomeListBean>>(this@MainActivity) {
                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("rxjavaGo")
                }

                override fun onSuccess(t: IResponse<HomeListBean>) {
                    FailOverGson.failOverGson.toJson(t).e("rxjavaGo")
                }
            })
    }

    fun downloadFileByDeferred() {
        GlobalScope.launch {
            HttpUtils.instance.DownloadRetrofit()
                .downloadFileByDeferred("")
                .deferredGo(object :
                    DeferredDownloadCallBack(
                        "name",
                        this@MainActivity
                    ) {
                    override fun onSuccess(
                        bytesRead: Long,
                        contentLength: Long,
                        progress: Float,
                        done: Boolean,
                        filePath: String?
                    ) {
                        progress.toString().e("downloadFileByDeferred")
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("downloadFileByDeferred")
                    }

                })
        }
    }

    fun downloadFileByFlow() {
        GlobalScope.launch {
            HttpUtils.instance.DownloadRetrofit()
                .downloadFileByFlow("")
                .flowGo(object :
                    FlowDownloadCallBack(
                        "name",
                        this@MainActivity
                    ) {
                    override fun onSuccess(
                        bytesRead: Long,
                        contentLength: Long,
                        progress: Float,
                        done: Boolean,
                        filePath: String?
                    ) {
                        progress.toString().e("downloadFileByFlow")
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("downloadFileByFlow")
                    }

                })
        }
    }

    fun downloadFileByObservable() {
        HttpUtils.instance.DownloadRetrofit()
            .downloadFileByObservable("")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                DownloadObserver(
                    "name",
                    this@MainActivity
                ) {
                override fun onSuccess(
                    bytesRead: Long,
                    contentLength: Long,
                    progress: Float,
                    done: Boolean,
                    filePath: String?
                ) {
                    progress.toString().e("downloadFileByObservable")
                }

                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("downloadFileByObservable")
                }

            })
    }

    /**
     * description 使用WorkerManager下载文件
     * @param
     * @return
     */
    fun downloadFileByDownloadManager() {
        DownloadManager.Builder()
            .apply {
                downloadUrl = "https://tp.kaishuihu.com/apk/fdy_1-1.0.0-2021-12-23.apk"
                isNetworkReconnect = true
                onDownloadListener = object : OnDownloadListener {
                    override fun start() {
                        Log.i("MainActivity", "开始下载")
                    }

                    override fun onDownload(
                        bytesRead: Long,
                        contentLength: Long,
                        progress: Float,
                        done: Boolean,
                        filePath: String?
                    ) {
                        Log.i("MainActivity", "正在下载：进度$progress 完成$bytesRead 大小$contentLength")
                    }

                    override fun onComplete(file: File) {
                        Log.i("MainActivity", "下载成功")
                    }

                    override fun onError(e: Exception) {
                        Log.i("MainActivity", "下载失败")
                    }
                }
            }
            .build()
            .startByWorker()
    }

    fun uploadFileByDeferred() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFileByDeferred<SliceFileBean, BaseResult<SliceFileBean>>("", "", "")
                .deferredGo(object :
                    DeferredUploadCallBack<SliceFileBean, BaseResult<SliceFileBean>>(
                        1,
                        100,
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("uploadFileByDeferred")
                    }

                    override fun onSuccess(t: BaseResult<SliceFileBean>) {
                        super.onSuccess(t)
                        FailOverGson.failOverGson.toJson(t).e("uploadFileByDeferred")
                    }

                    override fun onSuccess(
                        currentNum: Int,
                        allNum: Int,
                        progress: Float,
                        done: Boolean,
                        filePath: String?,
                        sourceId: String?
                    ) {

                    }
                })
        }
    }

    fun uploadFileByFlow() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFileByFlow<SliceFileBean, BaseResult<SliceFileBean>>("", "", "")
                .flowGo(object :
                    FlowUploadCallBack<SliceFileBean, BaseResult<SliceFileBean>>(
                        1,
                        100,
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("uploadFileByFlow")
                    }

                    override fun onSuccess(t: BaseResult<SliceFileBean>) {
                        super.onSuccess(t)
                        FailOverGson.failOverGson.toJson(t).e("uploadFileByFlow")
                    }

                    override fun onSuccess(
                        currentNum: Int,
                        allNum: Int,
                        progress: Float,
                        done: Boolean,
                        filePath: String?,
                        sourceId: String?
                    ) {

                    }
                })
        }
    }

    fun uploadFileByObservable() {
        HttpUtils.instance.UploadRetrofit()
            .uploadFileByObservable<SliceFileBean, BaseResult<SliceFileBean>>("", "")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :
                UploadObserver<SliceFileBean, BaseResult<SliceFileBean>>(
                    1,
                    100,
                    this@MainActivity
                ) {
                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("uploadFileByObservable")
                }

                override fun onSuccess(t: BaseResult<SliceFileBean>) {
                    super.onSuccess(t)
                    FailOverGson.failOverGson.toJson(t).e("uploadFileByObservable")
                }

                override fun onSuccess(
                    currentNum: Int,
                    allNum: Int,
                    progress: Float,
                    done: Boolean,
                    filePath: String?,
                    sourceId: String?
                ) {

                }
            })
    }

    fun uploadFilesByDeferred() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFilesByDeferred<SliceFileBean, BaseResult<SliceFileBean>>(
                    "",
                    listOf(""),
                    listOf("")
                )
                .deferredGo(object :
                    DeferredUploadCallBack<SliceFileBean, BaseResult<SliceFileBean>>(
                        1,
                        100,
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("uploadFilesByDeferred")
                    }

                    override fun onSuccess(t: BaseResult<SliceFileBean>) {
                        super.onSuccess(t)
                        FailOverGson.failOverGson.toJson(t).e("uploadFilesByDeferred")
                    }

                    override fun onSuccess(
                        currentNum: Int,
                        allNum: Int,
                        progress: Float,
                        done: Boolean,
                        filePath: String?,
                        sourceId: String?
                    ) {

                    }
                })
        }
    }

    fun uploadFilesByFlow() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFilesByFlow<SliceFileBean, BaseResult<SliceFileBean>>(
                    "",
                    listOf(""),
                    listOf("")
                )
                .flowGo(object :
                    FlowUploadCallBack<SliceFileBean, BaseResult<SliceFileBean>>(
                        1,
                        100,
                        this@MainActivity
                    ) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        FailOverGson.failOverGson.toJson(iException).e("uploadFilesByFlow")
                    }

                    override fun onSuccess(t: BaseResult<SliceFileBean>) {
                        super.onSuccess(t)
                        FailOverGson.failOverGson.toJson(t).e("uploadFilesByFlow")
                    }

                    override fun onSuccess(
                        currentNum: Int,
                        allNum: Int,
                        progress: Float,
                        done: Boolean,
                        filePath: String?,
                        sourceId: String?
                    ) {

                    }
                })
        }
    }

    fun uploadFilesByObservable() {
        HttpUtils.instance.UploadRetrofit()
            .uploadFilesByObservable<SliceFileBean, BaseResult<SliceFileBean>>(
                "",
                listOf(""),
                listOf("")
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : UploadObserver<SliceFileBean, BaseResult<SliceFileBean>>(
                1,
                100,
                this@MainActivity
            ) {
                override fun onFailure(iException: IException?) {
                    FailOverGson.failOverGson.toJson(iException).e("uploadFilesByObservable")
                }

                override fun onSuccess(t: BaseResult<SliceFileBean>) {
                    super.onSuccess(t)
                    FailOverGson.failOverGson.toJson(t).e("uploadFilesByObservable")
                }

                override fun onSuccess(
                    currentNum: Int,
                    allNum: Int,
                    progress: Float,
                    done: Boolean,
                    filePath: String?,
                    sourceId: String?
                ) {

                }
            })
    }

    /**
     * description 使用WorkerManager上传文件
     * @param
     * @return
     */
    fun uploadFilesByUploadManager() {
        UploadManager.Builder<SliceFileBean, BaseResult<SliceFileBean>>()
            .apply {
                checkUrl = ""
                uploadUrl = ""
                filePath = ""
                sliceFileSize = 10 * 1024 * 1024
                isNetworkReconnect = true
                onUpLoadListener = object : OnUpLoadListener {
                    override fun start() {
                        Log.i("MainActivity", "开始上传")
                    }

                    override fun onUpload(
                        currentNum: Int,
                        allNum: Int,
                        progress: Float,
                        done: Boolean,
                        filePath: String?,
                        sourceId: String?
                    ) {
                        Log.i("MainActivity", "正在上传：进度$progress 完成$currentNum 大小$allNum")
                    }

                    override fun onComplete(file: File?, sourceId: String?) {
                        Log.i("MainActivity", "上传成功")
                    }

                    override fun onError(e: Exception) {
                        Log.i("MainActivity", "上传失败")
                    }

                }
            }
            .build()
            .startByWorker()
    }

    /**
     * description 创建 StateViewHelper类，并回调重试请求、取消请求监听
     */
    override fun createStateViewHelper(): StateViewHelper {
        return StateViewHelper().apply {
            againRequestListener = object : StateViewHelper.AgainRequestListener {
                override fun request(view: View) {
                    againRequestByStateViewHelper(view)
                }
            }
            cancelRequestListener = object : StateViewHelper.CancelRequestListener {
                override fun cancel(dialog: DialogInterface) {
                    cancelRequestByStateViewHelper(dialog)
                }
            }
        }
    }

    /**
     * description 无网络视图
     * @param contentView 被替换的View
     */
    override fun showNoNetView(contentView: View?) {
        mStateViewHelper.showNoNetView(contentView)
    }

    /**
     * description 超时视图
     * @param contentView 被替换的View
     */
    override fun showTimeOutView(contentView: View?) {
        mStateViewHelper.showTimeOutView(contentView)
    }

    /**
     * description 无数据视图
     * @param contentView 被替换的View
     */
    override fun showEmptyView(contentView: View?) {
        mStateViewHelper.showEmptyView(contentView)
    }

    /**
     * description 错误视图
     * @param contentView 被替换的View
     */
    override fun showErrorView(contentView: View?) {
        mStateViewHelper.showErrorView(contentView)
    }

    /**
     * description 未登录视图
     * @param contentView 被替换的View
     */
    override fun showNoLoginView(contentView: View?) {
        mStateViewHelper.showNoLoginView(contentView)
    }

    /**
     * description 业务视图
     * @param contentView 要展示的View
     */
    override fun showContentView(contentView: View?) {
        mStateViewHelper.showContentView(contentView)
    }

    /**
     * description 加载中弹窗
     * @param mLoadingText 加载中...
     */
    override fun showProgressDialog(mLoadingText: String?) {
        mStateViewHelper.showProgressDialog(this, mLoadingText = mLoadingText)
    }

    /**
     * description 关闭加载中弹窗
     */
    override fun dismissProgressDialog() {
        mStateViewHelper.dismissProgressDialog()
    }

    /**
     * description 状态View重新请求回调
     * @param view 重试按钮
     */
    override fun againRequestByStateViewHelper(view: View) {

    }

    /**
     * description 加载中取消回调
     * @param dialog 加载中弹窗
     */
    override fun cancelRequestByStateViewHelper(dialog: DialogInterface) {

    }
}