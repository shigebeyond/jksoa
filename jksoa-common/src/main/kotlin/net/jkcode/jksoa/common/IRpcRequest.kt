package net.jkcode.jksoa.common

import com.alibaba.fastjson.annotation.JSONField
import net.jkcode.jkutil.invocation.IInvocation
import java.io.Serializable

/**
 * rpc请求
 *   远端方法调用的描述: 方法 + 参数
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IRpcRequest: Serializable, IInvocation, IRpcRequestMeta {

    /**
     * 请求标识，全局唯一
     */
    val id: Long

    /**
     * 服务标识，即接口类全名
     */
    @get:JSONField(serialize=false)
    val serviceId: String
        get() = clazz

    /**
     * 附加数据
     */
    val attachments: Map<String, Any?>?

    /**
     * 获得附加参数
     *    注：调用时需明确指定返回类型，来自动转换参数值为指定类型
     *
     * <code>
     *     val id:Long = req["id"]
     *     // 或
     *     val id = req["id"] as Long
     *
     *     // 相当于
     *     var id = req.attachments["id"]
     * </code>
     *
     * @param key 参数名
     * @param defaultValue 默认值
     * @return
     */
    public fun <T> get(key: String, defaultValue: T? = null): T?{
        return getAttachment(key, defaultValue)
    }

    /**
     * 获得附加参数
     *    注：调用时需明确指定返回类型，来自动转换参数值为指定类型
     * @param key 参数名
     * @param defaultValue 默认值
     * @return
     */
    public fun <T> getAttachment(key: String, defaultValue: T? = null): T?{
        return attachments?.getOrDefault(key, defaultValue) as T?
    }

    /**
     * 设置附加参数
     * @param key
     * @param value
     */
    public operator fun set(key: String, value: Any?) {
        putAttachment(key, value)
    }

    /**
     * 设置附加参数
     * @param key
     * @param value
     */
    public fun putAttachment(key: String, value: Any?)

    /**
     * 设置附加参数
     * @param data
     */
    public fun putAttachments(data: Map<String, Any?>)

    /**
     * 删除附加参数
     * @param key
     * @return
     */
    public fun removeAttachment(key: String): Any?
}