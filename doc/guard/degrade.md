# 注解 @Degrade

就是指定被守护的方法调用发生异常时, 转而调用的后备方法名, 要保证被守护的方法与后备方法要有同样的参数签名+同样的返回值类型

```
/**
 * 降级注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Degrade(
        public val fallbackMethod: String // 后备回调
)
```

demo, 表示 `getUserWhenException()` 调用异常时, 转而调用 `getUserWhenFallback()` 作为结果值

```
// 降级: 有异常后备方法
@Degrade(fallbackMethod = "getUserWhenFallback")
fun getUserWhenException(id: Int): User
```