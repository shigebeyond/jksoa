# 合并同group请求

对同类型的请求(请求参数不一样)进行合并, 然后执行批量处理(同时处理多个请求), 将执行结果分别返回给对应的请求.

# GroupFutureSupplierCombiner

针对每个group的(单参数)取值操作合并, 每个group攒够一定数量/时间的请求才执行

```
var id = 0
val groupCombiner = GroupFutureSupplierCombiner<String, User, User>("id"){ names ->
    val us = names.map { name ->
        User(id++, name)
    }
    CompletableFuture.completedFuture(us)
}
val futures = ArrayList<CompletableFuture<User>>()
for (i in (0..2)) {
    futures.add(groupCombiner.add(randomString(7)))
}
futures.print()
```

构建 `GroupFutureSupplierCombiner` 比较复杂, 直接看构造函数定义

```
open class GroupFutureSupplierCombiner<RequestArgumentType /* 请求参数类型 */, ResponseType /* 响应类型 */, BatchItemType: Any /* 批量取值操作的返回列表的元素类型 */>(
        public val reqArgField: String, /* 请求参数对应的响应字段名 */
        public val respField: String = "", /* 要返回的响应字段名, 如果为空则取响应对象 */
        public val one2one: Boolean = true /* 请求对响应是一对一(ResponseType是非List), 还是一对多(ResponseType是List) */,
        flushQuota: Int = 100 /* 触发刷盘的队列大小 */,
        flushTimeoutMillis: Long = 100 /* 触发刷盘的定时时间 */,
        public val batchFutureSupplier:(List<RequestArgumentType>) -> CompletableFuture<List<BatchItemType>> /* 批量取值操作 */
)
```

# 方法级注解 @GroupCombine

1. 定义
主要针对的是单参数的方法, 针对不同参数的多次调用, 直接合并为接收多个参数的批量方法的调用

```
/**
 * 针对group的进行合并的注解
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

```

2. demo
针对 `getUserByNameAsync("a")` / `getUserByNameAsync("b")` 有多次调用, 则合并为一次批量方法调用 `listUsersByNameAsync(listOf("a", "b"))`

```
// group合并
@GroupCombine("listUsersByNameAsync", "name", "", true, 100, 100)
fun getUserByNameAsync(name: String): CompletableFuture<User>
```