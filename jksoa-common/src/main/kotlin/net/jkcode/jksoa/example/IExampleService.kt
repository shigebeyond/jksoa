package net.jkcode.jksoa.example

import net.jkcode.jksoa.client.combiner.annotation.GroupCombine
import net.jkcode.jksoa.client.combiner.annotation.KeyCombine
import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.ServiceMeta
import java.io.Serializable
import java.rmi.Remote
import java.rmi.RemoteException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

data class User(public val id: Int, public val name: String): Serializable {}

/**
 * 示例服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@ServiceMeta(version = 1)
interface IExampleService : IService /*, Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    fun sayHi(name: String): String

    @JvmDefault
    fun getUserById(id: Int): User{
        return getUserByIdAsync(id).get()
    }

    @KeyCombine
    fun getUserByIdAsync(id: Int): CompletableFuture<User>

    @JvmDefault
    fun getUserByName(name: String): User{
        return getUserByNameAsync(name).get()
    }

    @GroupCombine("listUsersByName", "name", "", true, 100, 100)
    fun getUserByNameAsync(name: String): CompletableFuture<User>

    @JvmDefault
    fun listUsersByName(names: List<String>): List<User>{
        return listUsersByNameAsync(names).get()
    }

    fun listUsersByNameAsync(names: List<String>): CompletableFuture<List<User>>
}