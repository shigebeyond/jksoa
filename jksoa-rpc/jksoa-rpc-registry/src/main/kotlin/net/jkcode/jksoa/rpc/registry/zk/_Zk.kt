package net.jkcode.jksoa.rpc.registry.zk

import net.jkcode.jksoa.common.Url
import org.I0Itec.zkclient.ZkClient
import java.util.*


/**
 * 将子节点转为url
 *
 * @param parentPath
 * @param currentChilds
 * @return
 */
public fun ZkClient.nodeChilds2Urls(parentPath: String, currentChilds: List<String>): List<Url> {
    if(currentChilds.isEmpty())
        return emptyList()

    // 服务标识
    val serviceId = Url.serviceRegistryPath2serviceId(parentPath)

    // 遍历节点
    val urls = ArrayList<Url>()
    for (node in currentChilds) {
        // 读节点数据为参数
        val params = this.readData<String>("$parentPath/$node", true)
        // 解析节点名为 - 协议:ip:端口
        val (protocol, ip, port) = node.split(':')
        // 转为url
        val url = Url(protocol, ip, port.toInt(), serviceId, params)
        urls.add(url)
    }
    return urls
}