package net.jkcode.jksoa.rpc.tests

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSON.DEFAULT_PARSER_FEATURE
import com.alibaba.fastjson.parser.ParserConfig
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.getIntranetHost
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jksoa.rpc.server.IRpcServer
import net.jkcode.jksoa.rpc.server.handler.RpcRequestHandler
import net.jkcode.jksoa.rpc.server.protocol.jkr.JkrRpcServer
import net.jkcode.jksoa.rpc.server.protocol.rmi.RmiRpcServer
import net.jkcode.jkutil.common.makeThreads
import org.junit.Test
import kotlin.reflect.jvm.javaMethod

/**
 * 测试json rpc
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class JsonRpcTests {

    /**
     * json解析
     */
    @Test
    fun testRpcRequestJson(){
        val clazz = RpcRequest::class.java
        val json = "{\"clazz\":\"com.shikee.service.excel.IExcelService\",\"methodSignature\":\"safeParseExcelWithHead\",\"args\":[\"tb\\u5bc6\\u7801--2.69M.xlsx\",\"X3oqkfUx\",{\"\\u8ba2\\u5355\\u7f16\\u53f7\":\"order_no\",\"\\u4e70\\u5bb6\\u4f1a\\u5458\\u540d\":\"order_buyer_username\",\"\\u4e70\\u5bb6\\u5b9e\\u9645\\u652f\\u4ed8\\u91d1\\u989d\":\"order_actual_money\",\"\\u8ba2\\u5355\\u72b6\\u6001\":\"order_status\",\"\\u8ba2\\u5355\\u521b\\u5efa\\u65f6\\u95f4\":\"order_time\",\"\\u8ba2\\u5355\\u4ed8\\u6b3e\\u65f6\\u95f4\":\"order_pay_time\",\"\\u6536\\u8d27\\u4eba\\u59d3\\u540d\":\"order_recipient\",\"\\u6536\\u8d27\\u5730\\u5740\":\"order_recipient_address\",\"\\u8054\\u7cfb\\u7535\\u8bdd\":\"order_recipient_tel\",\"\\u8054\\u7cfb\\u624b\\u673a\":\"order_recipient_mobile\"},\"*\"],\"id\":192263763}"
        val msg = JSON.parseObject(json, clazz);
        println(msg)
    }

    /**
     * 多线程测试json解析
     *   报错: com.alibaba.fastjson.JSONException: default constructor not found. class net.jkcode.jksoa.common.RpcRequest
     */
    @Test
    fun testRpcRequestJson2(){
        makeThreads(20) {
            val clazz = RpcRequest::class.java
            val json = "{\"clazz\":\"com.shikee.service.excel.IExcelService\",\"methodSignature\":\"safeParseExcelWithHead\",\"args\":[\"tb\\u5bc6\\u7801--2.69M.xlsx\",\"X3oqkfUx\",{\"\\u8ba2\\u5355\\u7f16\\u53f7\":\"order_no\",\"\\u4e70\\u5bb6\\u4f1a\\u5458\\u540d\":\"order_buyer_username\",\"\\u4e70\\u5bb6\\u5b9e\\u9645\\u652f\\u4ed8\\u91d1\\u989d\":\"order_actual_money\",\"\\u8ba2\\u5355\\u72b6\\u6001\":\"order_status\",\"\\u8ba2\\u5355\\u521b\\u5efa\\u65f6\\u95f4\":\"order_time\",\"\\u8ba2\\u5355\\u4ed8\\u6b3e\\u65f6\\u95f4\":\"order_pay_time\",\"\\u6536\\u8d27\\u4eba\\u59d3\\u540d\":\"order_recipient\",\"\\u6536\\u8d27\\u5730\\u5740\":\"order_recipient_address\",\"\\u8054\\u7cfb\\u7535\\u8bdd\":\"order_recipient_tel\",\"\\u8054\\u7cfb\\u624b\\u673a\":\"order_recipient_mobile\"},\"*\"],\"id\":192263763}"
            val msg = JSON.parseObject(json, clazz);
            println(msg)
        }

        println("done")
    }

    /**
     * 多线程测试json解析
     */
    @Test
    fun testRpcRequestJson3(){
        val clazz = RpcRequest::class.java
        // bug: 并发下报错 com.alibaba.fastjson.JSONException: default constructor not found. class net.jkcode.jksoa.common.RpcRequest
        // 原因: `config.getDeserializer(clazz)` 并非线程安全, 并发下会导致多次创建 JavaBeanDeserializer, 进而导致类元数据的获得方法 JavaBeanInfo.build() 重复调用, 而 JavaBeanInfo.build() 中调用了kotlin类的元数据api 也不是线程安全的, 从而导致获得元数据失败
        // fix: 预先调用 `config.getDeserializer(clazz)`, 从而预先创建并缓存该类的 JavaBeanDeserializer
        val config = ParserConfig.global
        config.getDeserializer(clazz)

        makeThreads(100) {
            val json = "{\"clazz\":\"com.shikee.service.excel.IExcelService\",\"methodSignature\":\"safeParseExcelWithHead\",\"args\":[\"tb\\u5bc6\\u7801--2.69M.xlsx\",\"X3oqkfUx\",{\"\\u8ba2\\u5355\\u7f16\\u53f7\":\"order_no\",\"\\u4e70\\u5bb6\\u4f1a\\u5458\\u540d\":\"order_buyer_username\",\"\\u4e70\\u5bb6\\u5b9e\\u9645\\u652f\\u4ed8\\u91d1\\u989d\":\"order_actual_money\",\"\\u8ba2\\u5355\\u72b6\\u6001\":\"order_status\",\"\\u8ba2\\u5355\\u521b\\u5efa\\u65f6\\u95f4\":\"order_time\",\"\\u8ba2\\u5355\\u4ed8\\u6b3e\\u65f6\\u95f4\":\"order_pay_time\",\"\\u6536\\u8d27\\u4eba\\u59d3\\u540d\":\"order_recipient\",\"\\u6536\\u8d27\\u5730\\u5740\":\"order_recipient_address\",\"\\u8054\\u7cfb\\u7535\\u8bdd\":\"order_recipient_tel\",\"\\u8054\\u7cfb\\u624b\\u673a\":\"order_recipient_mobile\"},\"*\"],\"id\":192263763}"
            val msg = JSON.parseObject<Any>(json, clazz, config);
            println(msg)
        }

        println("done")
    }


}