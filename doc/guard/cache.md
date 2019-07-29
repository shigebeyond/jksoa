# 注解 @Cache

直接缓存调用结果, key是参数值串联的字符串, value是调用结果, 缓存时间为 expires 指定的秒数

```
/**
 * 缓存注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cache(
    public val keyPrefix: String = "", //  key的前缀
    public val keySeparator: String = "-", //  key的参数分隔符
    public val expires:Long = 600, //  过期时间（秒）, 默认缓存10min
    public val type: String = "jedis" // 缓存类型, 详见ICache接口与cache.yaml配置文件
)
```

demo

```
@Cache
fun getUserById(id: Int): User
```