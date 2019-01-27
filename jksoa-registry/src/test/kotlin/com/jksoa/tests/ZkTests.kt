package com.jksoa.tests

import com.jksoa.common.zk.ZkClientFactory
import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkNodeExistsException
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ZkTests {

    val zkClient: ZkClient = ZkClientFactory.instance()


    @Test
    fun testCreatePersistent(){
        // val root = "/root/" // java.lang.IllegalArgumentException: Path must not end with / character
        val root = "/root"
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
    fun testWrite(){
        println("创建节点")
        val dir = "/a"
        if (!zkClient.exists(dir))
            zkClient.createPersistent(dir, true)

        // 创建节点
        zkClient.createEphemeral("/a/b", "123")

        // 创建的是临时节点，在程序结束前中读取
        testRead()
    }

    @Test
    fun testRead(){
        println("读节点")
        // 读孩子
        println(zkClient.getChildren("/a"))

        // 读数据
        val content: String = zkClient.readData("/a/b")
        println(content)
    }

}