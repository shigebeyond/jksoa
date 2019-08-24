# 序列器
jksoa-rpc 提供了多种序列化实现, 默认为fast-serialization序列化技术.

## 类族

序列器的接口是 `ISerializer`

```
package net.jkcode.jkmvc.serialize

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * 序列器
 *
 * @author shijianhang
 * @create 2017-10-04 下午3:29
 **/
interface ISerializer {

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    fun serialize(obj: Any): ByteArray?

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    fun unserialize(bytes: ByteArray): Any? {
        return unserialize(ByteArrayInputStream(bytes))
    }

    /**
     * 反序列化
     *
     * @param input
     * @return
     */
    fun unserialize(input: InputStream): Any?
}
```

类族为

```
ISerializer
	JdkSerializer
	FstSerializer -- fast-serialization序列化技术
	ProtostuffSerializer
	HessianSerializer
	KryoSerializer
```


# 扩展序列器

## 1. 实现接口`ISerializer`

参考 `FstSerializer`

```
package net.jkcode.jkmvc.serialize

import org.nustaq.serialization.FSTConfiguration
import java.io.InputStream

/**
 * 基于fast-serialization的序列化
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-10 4:18 PM
 */
class FstSerializer: ISerializer {

    /**
     * 配置
     */
    public val conf = FSTConfiguration.createDefaultConfiguration()

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public override fun serialize(obj: Any): ByteArray? {
        return conf.asByteArray(obj)
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public override fun unserialize(bytes: ByteArray): Any? {
        return conf.getObjectInput(bytes).readObject()
    }

    /**
     * 反序列化
     *
     * @param input
     * @return
     */
    public override fun unserialize(input: InputStream): Any? {
        return conf.getObjectInput(input).readObject()
    }

}
```

## 2. 在`serializer.yaml`配置序列器名+实现类

```
# 序列器的类型
jdk: net.jkcode.jkmvc.serialize.JdkSerializer
fst: net.jkcode.jkmvc.serialize.FstSerializer
kryo: net.jkcode.jkmvc.serialize.KryoSerializer
hessian: net.jkcode.jkmvc.serialize.HessianSerializer
protostuff: net.jkcode.jkmvc.serialize.ProtostuffSerializer
```

## 3. 通过序列器名来引用序列器

```
val serializer: ISerializer = ISerializer.instance("序列器名") // 获得序列器
val obj = "hello world"
val bs = instance.serialize(obj) // 序列化
if(bs != null) {
    val obj2 = instance.unserialize(bs!!) // 反序列化
    println(obj2)
}
```