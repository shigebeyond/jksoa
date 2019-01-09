package com.jksoa.common

import com.jkmvc.common.SnowflakeIdWorker
import com.jkmvc.common.getSignature
import java.lang.reflect.Method

/**
 * rpc请求
 *
 * @ClassName: Request
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
class RpcRequest(public override val serviceId: String, /* 服务标识，即接口类全名 */
              public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
              public override val args: Array<Any?>, /* 实参 */
              public override val id: Long = SnowflakeIdWorker.instance().nextId() /* 请求标识，全局唯一 */
): IRpcRequest {

    /**
     * 构造函数
     *
     * @param intf 接口类
     * @param method 方法
     * @param args 实参
     * @param id 请求标识
     */
    public constructor(intf: Class<out IService>, method: Method, args: Array<Any?>, id: Long = SnowflakeIdWorker.instance().nextId()): this(intf.name, method.getSignature(), args, id){
    }

    /**
     * 构造函数
     *
     * @param intf 接口类
     * @param methodSignature 方法签名
     * @param args 实参
     * @param id 请求标识
     */
    public constructor(intf: Class<out IService>, methodSignature: String, args: Array<Any?>, id: Long = SnowflakeIdWorker.instance().nextId()): this(intf.name, methodSignature, args, id){
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "id=$id, service=$serviceId.$methodSignature, args=" + args.joinToString(", ", "[", "]");
    }

}