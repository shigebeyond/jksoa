package net.jkcode.jksoa.rpc.client.combiner.annotation

import java.lang.reflect.Method

/**
 * 针对group的进行合并的注解
 *    异步执行, 注意Threadlocal无法传递
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GroupCombine(
    public val batchMethod: String, // 批量操作的方法名
    public val reqArgField: String, // 请求参数对应的响应字段名
    public val respField: String = "", // 要返回的响应字段名, 如果为空则取响应对象
    public val one2one: Boolean = true, // 请求对响应是一对一(ResponseType是非List), 还是一对多(ResponseType是List)
    public val flushQuota: Int = 100, // 触发刷盘的队列大小
    public val flushTimeoutMillis: Long = 100 // 触发刷盘的定时时间
)


/**
 * 获得group合并的注解
 */
public val Method.groupCombine: GroupCombine?
    get(){
        return getAnnotation(GroupCombine::class.java)
    }
