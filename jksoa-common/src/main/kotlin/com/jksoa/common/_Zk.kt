package com.jksoa.common

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

    // 遍历节点
    val urls = ArrayList<Url>()
    for (node in currentChilds) {
        // 构造节点路径
        val nodePath = "$parentPath/$node"
        // 读节点数据
        val data = this.readData<String>(nodePath, true)
        // 转为url
        val url = Url(data)
        urls.add(url)
    }
    return urls
}