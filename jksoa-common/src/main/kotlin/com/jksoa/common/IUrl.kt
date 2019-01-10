package com.jksoa.common

/**
 * url与字符串互转的工具类
 *
 * @author shijianhang
 * @create 2017-12-12 下午10:27
 **/
interface IUrl {

    /**
     * 协议
     */
    var protocol: String

    /**
     * ip
     */
    var host: String

    /**
     * 端口
     */
    var port: Int

    /**
     * 路径 = 服务标识 = 接口类名
     */
    var path: String

    /**
     * 参数
     */
    var parameters: MutableMap<String, String>?

    /**
     * 服务标识 = 路径 = 接口类名
     */
    val serviceId: String
        get() = path

    /**
     * 接口类名 = 路径 = 服务标识
     */
    val `interface`: Class<out IService>
        get() = Class.forName(path) as Class<out IService>

    /**
     * 根节点路径
     *    格式为 /jksoa/路径
     */
    val rootPath: String

    /**
     * 子节点名称
     *    格式为 协议:ip:端口
     */
    val childName: String

    /**
     * 子节点路径
     *    格式为 /jksoa/路径/协议:ip:端口
     */
    val childPath: String
}