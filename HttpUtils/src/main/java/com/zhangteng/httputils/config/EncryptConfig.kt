package com.zhangteng.httputils.config

import okhttp3.HttpUrl

object EncryptConfig {
    /**
     * 加密时请求头中放置加密后的AES秘钥的key
     */
    var SECRET = "_secret"

    /**
     * 加解密失败时返回的异常状态码，返回数据结构{"message": "移动端加密失败","status": 2100}
     */
    var SECRET_ERROR = 2100

    /**
     * 服务端公钥，如果获取秘钥失败，使用客户端默认公钥完成后续交互
     */
    var publicKey: String? = null

    /**
     * 服务端公钥获取接口，默认null时使用publicKey进行加解密
     */
    var publicKeyUrl: HttpUrl? = null
}