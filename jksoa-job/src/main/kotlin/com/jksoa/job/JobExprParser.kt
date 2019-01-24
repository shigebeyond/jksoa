package com.jksoa.job

import com.jkmvc.common.getMethodBySignature
import com.jkmvc.common.mapToArray
import com.jkmvc.common.trim
import com.jkmvc.validator.ArgsParser
import com.jksoa.job.job.local.LpcJob
import com.jksoa.job.job.local.ShardingLpcJob
import com.jksoa.job.job.remote.RpcJob
import com.jksoa.job.job.remote.ShardingRpcJob
import java.lang.reflect.Method

/**
 * 作业表达式的解析器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:06 PM
 */
object JobExprParser: IJobParser {

    /**
     * 编译作业表达式
     *     作业表达式是由4个元素组成, 4个元素之间以空格分隔: 1 作业类型 2 类名 3 方法签名 4 方法实参列表
     *     当作业类型是custom, 则后面两个元素为空
     *     方法实参列表, 是以()包围多个参数, 参数之间用,分隔
     *     格式为: lpc com.jksoa.example.SystemService echo(String) ("hello")
     * <code>
     *     val job = IJobFactory::parseJob("lpc com.jksoa.example.SystemService echo(String) ("hello")");
     * </code>
     *
     * @param expr 作业表达式
     * @return
     */
    public override fun parse(expr:String): IJob {
        if(expr.isEmpty())
            throw JobException("作业表达式为空")

        // 解析元素
        // 作业表达式是由4个元素组成, 4个元素之间以空格分隔: 1 作业类型 2 类名 3 方法签名 4 方法实参列表
        // 1 当作业类型是custom, 则后面两个元素为空
        if(expr.startsWith("custom")){ // 自定义的作业类型
            val subexprs = expr.split(' ', limit = 2)
            if(subexprs.size != 2)
                throw JobException("自定义的作业表达式是由2个元素组成, 2个元素之间以空格分隔: 1 作业类型 2 自定义的作业类名")
            val clazz = subexprs[1]
            val c = Class.forName(clazz)
            return c.newInstance() as IJob
        }

        // 2 其他作业类型: lpc / shardingLpc / rpc / shardingRpc
        val subexprs = expr.split(' ', limit = 4)
        if(subexprs.size != 4)
            throw JobException("其他作业表达式是由4个元素组成, 4个元素之间以空格分隔: 1 作业类型 2 类名 3 方法签名 4 方法实参列表")
        val (type, clazz, methodSignature, argsExpr) = subexprs
        val c = Class.forName(clazz) // ClassNotFoundException
        val m = c.getMethodBySignature(methodSignature)
        if(m == null)
            throw JobException("Class [$clazz] has no method [$methodSignature]") // 无函数

        return when(type){
            "lpc" -> LpcJob(m, ArgsParser.parse(argsExpr, m!!) /* 有括号 */)
            "rpc" -> RpcJob(m, ArgsParser.parse(argsExpr, m!!) /* 有括号 */)
            "shardingLpc" -> ShardingLpcJob(m, parseShardingArgses(argsExpr, m!!))
            "shardingRpc" -> ShardingRpcJob(m, parseShardingArgses(argsExpr, m!!) )
            else -> throw JobException("无效作业类型: $type")
        }
    }

    /**
     * 解析分片参数
     *
     * @param argExpr 参数表达式
     * @param method 方法
     * @return
     */
    private fun parseShardingArgses(argExpr: String, method: Method): Array<Array<*>> {
        val argses = argExpr.trim("(", ")").split("),(")
        return argses.mapToArray { args ->
            ArgsParser.parse(args, method) // 无括号
        }
    }

}