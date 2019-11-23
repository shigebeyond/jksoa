package net.jkcode.jksoa.rpc.tests

import net.jkcode.jkutil.common.randomLong
import net.jkcode.jksoa.zk.ZkClientFactory
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNodeExistsException
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ZkTests {

    val zkClient: ZkClient = ZkClientFactory.instance()


    /**
     * 命令: create /jksoa/test 1
     */
    @Test
    fun testCreatePersistent(){
        // val root = "/jksoa/test/" // java.lang.IllegalArgumentException: Path must not end with / character
        val root = "/jksoa/test"
        if (!zkClient.exists(root))
            zkClient.createPersistent(root, true)
    }

    @Test
    fun testCreateEphemeral(){
        // 创建2个同样的节点
        zkClient.createEphemeral("/jksoa/test", "123")
        println("first success")
        try {
            zkClient.createEphemeral("/jksoa/test", "456")
            println("second success")
        }catch (e: ZkNodeExistsException){
            println("second fail")
            e.printStackTrace()
        }
    }

    @Test
    fun testCreateEphemeralSequential(){
        for(i in 0..10) {
            //val pref = randomString(4)
            val pref = randomLong(10000)
            val path = zkClient.createEphemeralSequential("/jksoa/test/$pref-", i)
            println("创建顺序节点: $path")
        }
        val children = zkClient.getChildren("/jksoa/test")
        println(children)
        TimeUnit.MINUTES.sleep(2)
    }

    @Test
    fun testWrite(){
        // 创建节点
        zkClient.createEphemeral("/jksoa/test/a", "123")

        // 创建的是临时节点，在程序结束前中读取
        testRead()
    }

    @Test
    fun testRead(){
        println("读节点")
        // 读孩子
        println(zkClient.getChildren("/jksoa/test/"))

        // 读数据
        val content: String = zkClient.readData("/jksoa/test/a")
        println(content)
    }

}