package net.jkcode.jksoa.common.invocation

import net.jkcode.jkutil.common.toExpr
import java.util.*

/**
 * 分片方法调用的描述
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IShardingInvocation: IInvocation {

    /**
     * 每个分片的参数个数
     */
    val argsPerSharding: Int

    /**
     * 分片总数
     */
    val shardingSize: Int
        get() = args.size / argsPerSharding

    /**
     * 获得指定分片的实参
     * @param iSharding 分片序号
     * @return
     */
    fun getShardingArgs(iSharding: Int): Array<Any?> {
        val shardingArgs: Array<Any?> = arrayOfNulls(argsPerSharding)
        System.arraycopy(args, argsPerSharding * iSharding, shardingArgs, 0, argsPerSharding)
        return shardingArgs
    }

    /**
     * 转为描述
     *
     * @return
     */
    override fun toDesc(): String {
        return super.toDesc() + ", argsPerSharding=" + argsPerSharding
    }

    /**
     * 转为作业表达式
     * @return
     */
    override fun toExpr(): String {
        return super.toExpr() + " " + argsPerSharding
    }
}