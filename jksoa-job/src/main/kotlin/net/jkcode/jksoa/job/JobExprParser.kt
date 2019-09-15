package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.getMethodByClassAndSignature
import net.jkcode.jkmvc.validator.ArgsParser
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.invocation.IInvocation
import net.jkcode.jksoa.common.invocation.Invocation
import net.jkcode.jksoa.common.invocation.ShardingInvocation
import net.jkcode.jksoa.job.job.InvocationJob

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
     *     方法实参列表, 是以()包围多个参数, 参数之间用`,`分隔
     *     格式为: lpc net.jkcode.jksoa.rpc.example.SimpleService echo(String) ("hello")
     * <code>
     *     val job = IJobFactory::parseJob("lpc net.jkcode.jksoa.rpc.example.SimpleService echo(String) ("hello")");
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

        // 2 其他作业类型: lpc / rpc / shardingLpc / shardingRpc
        val subexprs = expr.split(' ', limit = 5)
        if(subexprs.size < 4)
            throw JobException("其他作业表达式是由5个元素组成, 5个元素之间以空格分隔: 1 作业类型 2 类名 3 方法签名 4 方法实参列表 5 分片处理之每个分片的参数个数, 非分片处理可省略")
        val type: String = subexprs[0]
        val clazz: String = subexprs[1]
        val methodSignature: String = subexprs[2]
        val argsExpr: String = subexprs[3]
        val argsPerSharding: Int = if(subexprs.size == 4) 0 else subexprs[4].toInt()

        val method = getMethodByClassAndSignature(clazz, methodSignature)
        val args = ArgsParser.parse(argsExpr, method)
        val inv: IInvocation = when(type){
            "lpc" -> Invocation(method, args)
            "rpc" -> RpcRequest(method, args)
            "shardingLpc" -> ShardingInvocation(method, args, argsPerSharding)
            "shardingRpc" -> ShardingRpcRequest(method, args, argsPerSharding)
            else -> throw JobException("无效作业类型: $type")
        }
        return InvocationJob(inv)
    }


}