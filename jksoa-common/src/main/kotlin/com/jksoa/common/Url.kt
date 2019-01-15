package com.jksoa.common

import com.jkmvc.common.getAndConvert

/**
 * url与字符串互转的工具类
 *
 * @author shijianhang
 * @create 2017-12-12 下午10:27
 **/
class Url(override var protocol: String /* 协议 */,

          override var host: String /* ip */,

          override var port: Int /* 端口 */,

          override var path: String /* 路径 = 服务标识 = 接口类名 */ = "",

          override var parameters: Map<String, Any?> = emptyMap() /* 参数 */
) : IUrl {

    /**
     * 解析url字符串
     * @param url
     */
    public constructor(url: String) : this("", "", -1) {
        parseUrl(url)
    }

    companion object{

        /**
         * 路径前缀
         */
        public val UrlPrefix: String = "/jksoa/"

        /**
         * 服务标识转根节点路径
         *
         * @param serviceId
         * @return
         */
        public fun serviceId2rootPath(serviceId: String): String {
            return "$UrlPrefix$serviceId"
        }

        /**
         * 服务标识转根节点路径
         *
         * @param rootPath
         * @return
         */
        public fun rootPath2serviceId(rootPath: String): String {
            return rootPath.substring(UrlPrefix.length)
        }

        /**
         * 端口的正则
         */
        protected val RegexPort: String = "(:(\\d+))?"

        /**
         * 参数字符串的正则
         */
        protected val RegexParamStr: String = "(\\?(.+))?"

        /**
         * url的正则
         */
        protected val RegexUrl: Regex = ("(\\w+)://([^:/]+)${RegexPort}/?([^?]*)${RegexParamStr}").toRegex()

        /**
         * 函数参数的正则
         */
        protected val RegexParam: Regex = "([^=]+)=([^&]+)".toRegex()

        /**
         * 解析参数
         *
         * @param paramStr
         */
        public fun parseParams(paramStr: String): HashMap<String, String> {
            val params = HashMap<String, String>()
            val matches = RegexParam.findAll(paramStr)
            for(m in matches){
                val key = m.groups[1]!!.value
                val value = m.groups[2]!!.value
                params[key] = value
            }
            return params
        }
    }

    /**
     * 根节点路径
     *    格式为 /jksoa/路径
     */
    public override val rootPath: String by lazy{
        serviceId2rootPath(path)
    }

    /**
     * 子节点名称
     *    格式为 协议:ip:端口
     */
    public override val childName: String by lazy{
        "$protocol:$host:$port"
    }

    /**
     * 子节点路径
     *    格式为 /jksoa/路径/协议:ip:端口
     */
    public override val childPath: String by lazy{
        "$rootPath/$childName"
    }

    /**
     * 解析url
     *
     * @param url
     */
    protected fun parseUrl(url: String) {
        val match = RegexUrl.find(url)
        if (match == null)
            throw Exception("url格式错误: $url")
        // 协议
        protocol = match.groups[1]!!.value
        // ip
        host = match.groups[2]!!.value
        // 端口
        val portStr = match.groups[4]?.value
        if (portStr != null)
            port = portStr.toInt()
        // 路径
        path = match.groups[5]!!.value
        // 解析参数
        val paramStr = match.groups[7]?.value
        if (paramStr != null)
            parameters = parseParams(paramStr)
    }

    /**
     * 获得配置项的值
     *    注：调用时需明确指定返回类型，来自动转换参数值为指定类型
     * @param key
     * @param defaultValue
     * @return
     */
    public inline fun <reified T:Any> getParameter(key: String, defaultValue: T? = null): T?{
        return parameters.getAndConvert(key, defaultValue)
    }

    /**
     * 转为字符串
     *    格式为 协议://ip:端口/路径?参数
     *
     * @return
     */
    public override fun toString(): String {
        return toString(true)
    }

    /**
     * 转为字符串
     *    格式为 协议://ip:端口/路径?参数
     *
     * @param withQuery 是否带query string
     * @return
     */
    public fun toString(withQuery: Boolean): String {
        // url
        val str = StringBuilder(protocol).append("://").append(host)
        if(port >= 0)
            str.append(':').append(port)
        str.append("/").append(path)
        // 参数
        if(withQuery){
            parameters.entries.joinTo(str, "&", "?"){
                // fix bug: java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
                //it.key + '=' + it.value
                "${it.key}=${it.value}"
            }
        }
        return str.toString()
    }

    /**
     * 获得哈希码
     *
     * @return
     */
    public override fun hashCode(): Int {
        return protocol.hashCode() + host.hashCode() + port + path.hashCode() + parameters.hashCode()
    }

    /**
     * 检查是否等于指定对象
     *
     * @param obj
     * @return
     */
    public override fun equals(obj: Any?):Boolean {
        if (obj == null || obj !is Url)
            return false

        return this.protocol == obj.protocol
                && this.host == obj.host
                && this.port == obj.port
                && this.path == obj.path
                && this.parameters == obj.parameters // 2个对象的 parameters 使用同样的map类，实例与生成的字符串都应该是一样的
    }

}