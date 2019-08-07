# 合并同key请求

对同样的请求(请求参数一样)进行合并, 最终只执行一次, 将执行结果返回给所有请求.

# KeyFutureSupplierCombiner

针对每个key的(单参数)取值操作合并, 也等同于取值操作去重

构建 `KeyFutureSupplierCombiner` 只需要一个参数, 为取值操作, 其返回值类型是 `CompletableFuture`, 直接调用作为源异步结果

```
val keyCombiner = KeyFutureSupplierCombiner<Int, User>{ id ->
    Thread.sleep(10)
    val user = User(id, randomString(7))
    CompletableFuture.completedFuture(user)
}
val futures = ArrayList<CompletableFuture<User>>()
for (i in (0..2)) {
    futures.add(keyCombiner.add(1))
}
futures.print()
```


# 方法级注解 `@KeyCombine`

1. 定义

主要针对的是单参数的方法, 针对同一个参数值的多次调用, 直接合并为一次调用

```
/**
 * 针对key的进行合并的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KeyCombine()
```

2. demo

针对id=1有多次调用, 则合并为一次调用

```
// key合并
@KeyCombine
fun getUserByIdAsync(id: Int): CompletableFuture<User>
```