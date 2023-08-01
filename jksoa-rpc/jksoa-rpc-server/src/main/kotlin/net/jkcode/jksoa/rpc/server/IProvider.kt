package net.jkcode.jksoa.rpc.server

import net.jkcode.jksoa.common.loader.BaseServiceClass
import net.jkcode.jksoa.common.Url

/**
 * 服务提供者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 3:48 PM
 */
abstract class IProvider : BaseServiceClass() {

    /**
     * 服务路径
     */
    public abstract val serviceUrl: Url

    /**
     * 实现类
     */
    public abstract val clazz: Class<*>

}