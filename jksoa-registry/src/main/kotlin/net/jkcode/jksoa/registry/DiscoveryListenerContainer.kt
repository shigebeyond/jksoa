package net.jkcode.jksoa.registry

import net.jkcode.jksoa.common.Url
import java.util.*

/**
 * 服务发现的监听器的容器：监听某个服务的地址变化
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
open class DiscoveryListenerContainer(
        public override val serviceId: String, // 服务标识，即接口类全名
        protected val list: MutableList<IDiscoveryListener> = LinkedList() // 代理list
): IDiscoveryListener, MutableList<IDiscoveryListener> by list {

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        for(l in list)
            l.handleServiceUrlAdd(url, allUrls)
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(serverName: String, allUrls: Collection<Url>) {
        for(l in list)
            l.handleServiceUrlRemove(serverName, allUrls)
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     * @param url
     */
    public override fun handleParametersChange(url: Url) {
        for(l in list)
            l.handleParametersChange(url)
    }

}