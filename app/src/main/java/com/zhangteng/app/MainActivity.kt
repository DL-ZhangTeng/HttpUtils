package com.zhangteng.app

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.zhangteng.app.http.Api
import com.zhangteng.app.http.BaseResult
import com.zhangteng.app.http.entity.HomeListBean
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.result.coroutine.*
import com.zhangteng.httputils.result.coroutine.callback.DeferredCallBack
import com.zhangteng.httputils.result.coroutine.callback.DeferredDownloadCallBack
import com.zhangteng.httputils.result.flow.callback.FlowCallBack
import com.zhangteng.httputils.result.flow.callback.FlowDownloadCallBack
import com.zhangteng.httputils.result.flow.flowGo
import com.zhangteng.httputils.result.flow.flowGoIResponse
import com.zhangteng.httputils.result.flow.launchGoFlowIResponse
import com.zhangteng.httputils.result.rxjava.observer.CommonObserver
import com.zhangteng.httputils.result.rxjava.observer.DownloadObserver
import com.zhangteng.httputils.result.rxjava.transformer.LifecycleObservableTransformer
import com.zhangteng.httputils.result.rxjava.transformer.ProgressDialogObservableTransformer
import com.zhangteng.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity(), IStateView {
    private val mStateViewHelper by lazy { createStateViewHelper() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchGo_DeferredExUtils()
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
                Gson().toJson(it).e("launchGo_DeferredExUtils")
            }, {
                Gson().toJson(it).e("launchGo_DeferredExUtils")
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
                Gson().toJson(it).e("launchGoIResponse_DeferredExUtils")
            }, {
                Gson().toJson(it).e("launchGoIResponse_DeferredExUtils")
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
                Gson().toJson(it).e("launchGoDeferred")
            }, {
                Gson().toJson(it).e("launchGoDeferred")
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
                Gson().toJson(it).e("launchGoDeferredIResponse")
            }, {
                Gson().toJson(it).e("launchGoDeferredIResponse")
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
                    Gson().toJson(it).e("deferredGo")
                }, {
                    Gson().toJson(it).e("deferredGo")
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
                    Gson().toJson(it).e("deferredGoIResponse")
                }, {
                    Gson().toJson(it).e("deferredGoIResponse")
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
                        Gson().toJson(iException).e("deferredGo_ICallBack")
                    }

                    override fun onSuccess(t: BaseResult<HomeListBean>) {
                        Gson().toJson(t).e("deferredGo_ICallBack")
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
                        Gson().toJson(iException).e("deferredGoIResponse_ICallBack")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        Gson().toJson(t).e("deferredGoIResponse_ICallBack")
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
                Gson().toJson(it).e("launchGo_FlowExUtils")
            }, {
                Gson().toJson(it).e("launchGo_FlowExUtils")
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
                Gson().toJson(it).e("launchGoIResponse_FlowExUtils")
            }, {
                Gson().toJson(it).e("launchGoIResponse_FlowExUtils")
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
                Gson().toJson(it).e("launchGoFlow")
            }, {
                Gson().toJson(it).e("launchGoFlow")
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
                Gson().toJson(it).e("launchGoFlowIResponse")
            }, {
                Gson().toJson(it).e("launchGoFlowIResponse")
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
                    Gson().toJson(it).e("flowGo")
                }, {
                    Gson().toJson(it).e("flowGo")
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
                    Gson().toJson(it).e("flowGoIResponse")
                }, {
                    Gson().toJson(it).e("flowGoIResponse")
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
                        Gson().toJson(iException).e("flowGo_ICallBack")
                    }

                    override fun onSuccess(t: BaseResult<HomeListBean>) {
                        Gson().toJson(t).e("flowGo_ICallBack")
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
                        Gson().toJson(iException).e("flowGoIResponse_ICallBack")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        Gson().toJson(t).e("flowGoIResponse_ICallBack")
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
                    Gson().toJson(iException).e("rxjavaGo")
                }

                override fun onSuccess(t: IResponse<HomeListBean>) {
                    Gson().toJson(t).e("rxjavaGo")
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
                    Gson().toJson(iException).e("rxjavaGo")
                }

                override fun onSuccess(t: IResponse<HomeListBean>) {
                    Gson().toJson(t).e("rxjavaGo")
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
                        Gson().toJson(iException).e("downloadFileByDeferred")
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
                        Gson().toJson(iException).e("downloadFileByFlow")
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
                    Gson().toJson(iException).e("downloadFileByObservable")
                }

            })
    }

    fun uploadFileByDeferred() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFileByDeferred("", "", "")
                .deferredGo(object :
                    DeferredCallBack<ResponseBody>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        Gson().toJson(iException).e("uploadFileByDeferred")
                    }

                    override fun onSuccess(t: ResponseBody) {
                        Gson().toJson(t).e("uploadFileByDeferred")
                    }
                })
        }
    }

    fun uploadFileByFlow() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFileByFlow("", "", "")
                .flowGo(object :
                    FlowCallBack<ResponseBody>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        Gson().toJson(iException).e("uploadFileByFlow")
                    }

                    override fun onSuccess(t: ResponseBody) {
                        Gson().toJson(t).e("uploadFileByFlow")
                    }
                })
        }
    }

    fun uploadFileByObservable() {
        HttpUtils.instance.UploadRetrofit()
            .uploadFileByObservable("", "")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<ResponseBody>(this@MainActivity) {
                override fun onFailure(iException: IException?) {
                    Gson().toJson(iException).e("uploadFileByObservable")
                }

                override fun onSuccess(t: ResponseBody) {
                    Gson().toJson(t).e("uploadFileByObservable")
                }
            })
    }

    fun uploadFilesByDeferred() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFilesByDeferred("", listOf(""), listOf(""))
                .deferredGo(object :
                    DeferredCallBack<ResponseBody>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        Gson().toJson(iException).e("uploadFilesByDeferred")
                    }

                    override fun onSuccess(t: ResponseBody) {
                        Gson().toJson(t).e("uploadFilesByDeferred")
                    }
                })
        }
    }

    fun uploadFilesByFlow() {
        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFilesByFlow("", listOf(""), listOf(""))
                .flowGo(object :
                    FlowCallBack<ResponseBody>(this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        Gson().toJson(iException).e("uploadFilesByFlow")
                    }

                    override fun onSuccess(t: ResponseBody) {
                        Gson().toJson(t).e("uploadFilesByFlow")
                    }
                })
        }
    }

    fun uploadFilesByObservable() {
        HttpUtils.instance.UploadRetrofit()
            .uploadFilesByObservable("", listOf(""), listOf(""))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CommonObserver<ResponseBody>(this@MainActivity) {
                override fun onFailure(iException: IException?) {
                    Gson().toJson(iException).e("uploadFilesByObservable")
                }

                override fun onSuccess(t: ResponseBody) {
                    Gson().toJson(t).e("uploadFilesByObservable")
                }
            })
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