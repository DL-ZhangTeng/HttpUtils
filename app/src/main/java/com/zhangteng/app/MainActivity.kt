package com.zhangteng.app

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
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
import com.zhangteng.utils.IException
import com.zhangteng.utils.IResponse
import com.zhangteng.utils.StateViewHelper
import com.zhangteng.utils.e
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity() {
    private var mProgressDialog: Dialog? = null
    private var mLoadTextView: TextView? = null
    private var mAnimation: Animation? = null
    private var mLoadImageView: ImageView? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mProgressDialog = Dialog(this, R.style.progress_dialog)
        val view = View.inflate(this, StateViewHelper.loadingLayout, null)
        mLoadTextView = view.findViewById(R.id.loadView)
        mLoadImageView = view.findViewById(R.id.progress_bar)
        mLoadImageView?.setImageResource(StateViewHelper.loadingImage)
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.loadings)
            .apply { interpolator = LinearInterpolator() }
        mLoadImageView?.startAnimation(mAnimation)
        mLoadTextView?.text = StateViewHelper.loadingText
        mProgressDialog?.setContentView(view)
        mProgressDialog?.setCancelable(true)
        mProgressDialog?.setCanceledOnTouchOutside(false)

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
                mProgressDialog,
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
                mProgressDialog,
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
                mProgressDialog,
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
                mProgressDialog,
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
                    mProgressDialog,
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
                    mProgressDialog,
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
                    DeferredCallBack<BaseResult<HomeListBean>>(mProgressDialog, this@MainActivity) {
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
                    DeferredCallBack<IResponse<HomeListBean>>(mProgressDialog, this@MainActivity) {
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
                mProgressDialog,
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
                mProgressDialog,
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
                mProgressDialog,
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
                mProgressDialog,
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
                    mProgressDialog,
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
                    mProgressDialog,
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
                    FlowCallBack<BaseResult<HomeListBean>>(mProgressDialog, this@MainActivity) {
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
                    FlowCallBack<IResponse<HomeListBean>>(mProgressDialog, this@MainActivity) {
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

    fun downloadFileByDeferred() {
        GlobalScope.launch {
            HttpUtils.instance.DownloadRetrofit()
                .downloadFileByDeferred("")
                .deferredGo(object :
                    DeferredDownloadCallBack(
                        "name",
                        mProgressDialog,
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
                        mProgressDialog,
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
                    mProgressDialog,
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
                    DeferredCallBack<ResponseBody>(mProgressDialog, this@MainActivity) {
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
                    FlowCallBack<ResponseBody>(mProgressDialog, this@MainActivity) {
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
            .subscribe(object : CommonObserver<ResponseBody>(mProgressDialog, this@MainActivity) {
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
                    DeferredCallBack<ResponseBody>(mProgressDialog, this@MainActivity) {
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
                    FlowCallBack<ResponseBody>(mProgressDialog, this@MainActivity) {
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
            .subscribe(object : CommonObserver<ResponseBody>(mProgressDialog, this@MainActivity) {
                override fun onFailure(iException: IException?) {
                    Gson().toJson(iException).e("uploadFilesByObservable")
                }

                override fun onSuccess(t: ResponseBody) {
                    Gson().toJson(t).e("uploadFilesByObservable")
                }
            })
    }
}