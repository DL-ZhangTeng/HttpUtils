package com.zhangteng.httputils.config

import okhttp3.HttpUrl

object EncryptConfig {
    /**
     * 加密时请求头中放置加密后的AES秘钥的key
     */
    var SECRET = "_secret"

    /**
     * 加解密失败时返回的异常状态码，返回数据结构{"message": "移动端加密失败","status": SECRET_ERROR}
     */
    var SECRET_ERROR = 2100
    var publicKey = ""
    var publicKeyUrl: HttpUrl? = null
}