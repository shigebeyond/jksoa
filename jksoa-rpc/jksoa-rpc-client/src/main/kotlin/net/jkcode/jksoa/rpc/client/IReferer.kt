package net.jkcode.jksoa.rpc.client

import net.jkcode.jksoa.common.loader.IServiceClass
import java.io.Closeable

/**
 * 服务的引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
abstract class IReferer: IServiceClass(), Closeable {
}