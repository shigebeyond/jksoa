package net.jkcode.jksoa.common

import net.jkcode.jkutil.common.buildQueryString
import net.jkcode.jkutil.common.getAndConvert
import net.jkcode.jkutil.common.joinHashCode
import java.net.InetSocketAddress

/**
 * url与字符串互转的工具类
 *
 * @author shijianhang
 * @create 2017-12-12 下午10:27
 **/
open class Url(public override var protocol: String, // 协议
               public override var host: String, // ip
               public override var port: Int, // 端口
               public override var path: String = "", // 路径 = 服务标识 = 接口类名 */
               public override var parameters: Map<String, Any?> = emptyMap() // 参数
) : IUrl {

    /**
     * 解析参数
     * @param protocol
     * @param host
     * @param port
     * @param path
     * @param parameters
     */
    public constructor(protocol: String, host: String, port: Int, path: String, parameters: String) : this(protocol, host, port, path, parseParams(parameters))

    /**
     * 解析url字符串
     * @param url
     */
    public constructor(url: String) : this("", "", -1) {
        parseUrl(url)
    }

    /**
     * 解析地址
     * @param addr 地址
     */
    public constructor(protocol: String, addr: InetSocketAddress) : this(protocol, addr.hostName, addr.port){
    }

    companion object{

        /**
         * 路径前缀
         */
        public val PathPrefix: String = "/jksoa/"

        /**
         * 服务标识转服务路径
         *
         * @param serviceId
         * @return
         */
        public fun serviceId2serviceRegistryPath(serviceId: String): String {
            return "$PathPrefix$serviceId"
        }

        /**
         * 服务路径转服务标识
         *
         * @param path
         * @return
         */
        public fun serviceRegistryPath2serviceId(path: String): String {
            return path.substring(PathPrefix.length)
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
         * 参数的正则
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
     * 哈希值
     */
    protected val hash: Int by lazy{
        //toString().hashCode()
        val h = joinHashCode(protocol, host, path) * 10000 + port
        h xor parameters.hashCode()
    }

    /**
     * 服务路径
     *    格式为 /jksoa/服务
     */
    public override val serviceRegistryPath: String by lazy{
        serviceId2serviceRegistryPath(path)
    }

    /**
     * 服务节点名称
     *    格式为 协议:ip:端口
     */
    public override val serverName: String by lazy{
        "$protocol:$host:$port"
    }

    /**
     * 服务节点路径
     *    格式为 /jksoa/服务/协议:ip:端口
     */
    public override val serverRegistryPath: String by lazy{
        "$serviceRegistryPath/$serverName"
    }

    /**
     * 转化为仅包含服务节点信息的url
     */
    public override val serverPart: Url by lazy{
        Url(protocol, host, port)
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
        // 路径(服务)
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
     *    格式为 协议://ip:端口/路径(服务)?参数
     *
     * @return
     */
    public override fun toString(): String {
        return toString(true)
    }

    /**
     * 转为字符串
     *    格式为 协议://ip:端口/路径(服务)?参数
     *
     * @param withQuery 是否带query string
     * @return
     */
    public fun toString(withQuery: Boolean): String {
        // url
        val str = StringBuilder(protocol).append("://").append(host)
        if(port >= 0)
            str.append(':').append(port)
        if(path.isNotEmpty())
            str.append("/").append(path)
        // 参数
        if(withQuery && parameters.isNotEmpty()) {
            str.append('?')
            parameters.buildQueryString(str)
        }

        return str.toString()
    }

    /**
     * 将参数转为查询字符串
     * @return
     */
    public fun getQueryString(): String {
        val str = StringBuilder()
        parameters.buildQueryString(str)
        return str.toString()
    }

    /**
     * 获得哈希码
     *
     * @return
     */
    public override fun hashCode(): Int {
        return hash
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