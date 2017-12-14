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
class Request(public val serviceName: String, /* 服务名，即接口类全名 */
              public val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
              public val args: Array<out Any>, /* 实参 */
              public val id: Long = SnowflakeIdWorker.instance().nextId() /* 请求标识，全局唯一 */
) {

    /**
     * 构造函数
     *
     * @param intf 接口类
     * @param method 方法
     * @param args 实参
     * @param id 请求标识
     */
    public constructor(intf: Class<out IService>, method: Method, args: Array<out Any>, id: Long = SnowflakeIdWorker.instance().nextId()): this(intf.name, method.getSignature(), args, id){
    }

}