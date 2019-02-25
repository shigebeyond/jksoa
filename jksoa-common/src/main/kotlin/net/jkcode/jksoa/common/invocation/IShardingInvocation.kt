package net.jkcode.jksoa.common.invocation

import com.jkmvc.common.toExpr

/**
 * 分片方法调用的描述
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IShardingInvocation: IInvocationMethod {

    /**
     * 分片总数
     */
    val shardingSize: Int
        get() = shardingArgses.size

    /**
     * 分片要调用的实参
     */
    val shardingArgses: Array<Array<*>>

    /**
     * 转为描述
     *
     * @return
     */
    override fun toDesc(): String {
        return "method=$clazz.$methodSignature, shardingSize=$shardingSize, shardingArgses=" + shardingArgses.joinToString(", ", "[", "]"){ args ->
            args.joinToString(", ", "(", ")"){
                it.toExpr()
            }
        }
    }

    /**
     * 转为作业表达式
     * @return
     */
    override fun toExpr(): String {
        return "$clazz $methodSignature " + shardingArgses.joinToString(","){ args ->
            args.joinToString(",", "(", ")"){
                it.toExpr()
            }
        }
    }
}