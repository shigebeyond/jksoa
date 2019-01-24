package com.jksoa.job

/**
 * 作业解析器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:06 PM
 */
interface IJobParser {

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
    public fun parse(expr:String): IJob

}