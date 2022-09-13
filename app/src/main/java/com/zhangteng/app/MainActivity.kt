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
import com.zhangteng.app.http.entity.HomeListBean
import com.zhangteng.httputils.http.HttpUtils
import com.zhangteng.httputils.result.coroutine.*
import com.zhangteng.httputils.result.coroutine.callback.DeferredCallBack
import com.zhangteng.httputils.result.coroutine.callback.DeferredDownloadCallBack
import com.zhangteng.httputils.result.flow.callback.FlowCallBack
import com.zhangteng.httputils.result.flow.flowGoIResponse
import com.zhangteng.httputils.result.flow.launchGoFlowIResponse
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

        GlobalScope.launch {
            launchGo({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                Gson().toJson(it).e("launchGo")
            }, {
                Gson().toJson(it).e("launchGo")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

        GlobalScope.launch {
            launchGoIResponse({
                HttpUtils.instance.ConfigGlobalHttpUtils()
                    .createService(Api::class.java)
                    .getHomeList(0)
            }, {
                Gson().toJson(it).e("launchGoIResponse")
            }, {
                Gson().toJson(it).e("launchGoIResponse")
            }, {

            },
                mProgressDialog,
                this@MainActivity
            )
        }

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
                        Gson().toJson(iException).e("flowGoIResponse")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        Gson().toJson(t).e("flowGoIResponse")
                    }
                })
        }

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
                        Gson().toJson(iException).e("flowGoIResponse")
                    }

                    override fun onSuccess(t: IResponse<HomeListBean>) {
                        Gson().toJson(t).e("flowGoIResponse")
                    }
                })
        }

        GlobalScope.launch {
            HttpUtils.instance.UploadRetrofit()
                .uploadFileByDeferred("", "", "")
                .deferredGo(object :
                    DeferredCallBack<ResponseBody>(mProgressDialog, this@MainActivity) {
                    override fun isHideToast(): Boolean {
                        return true
                    }

                    override fun onFailure(iException: IException?) {
                        Gson().toJson(iException).e("deferredGo")
                    }

                    override fun onSuccess(t: ResponseBody) {
                        Gson().toJson(t).e("deferredGo")
                    }
                })
        }

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
                        progress.toString().e("deferredGo")
                    }

                    override fun onFailure(iException: IException?) {
                        Gson().toJson(iException).e("deferredGo")
                    }

                })
        }
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
                    progress.toString().e("deferredGo")
                }

                override fun onFailure(iException: IException?) {
                    Gson().toJson(iException).e("deferredGo")
                }

            })
    }

    override fun onDestroy() {
        super.onDestroy()
        HttpUtils.instance.cancelAllRequest()
    }
}