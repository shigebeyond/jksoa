package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.IRequestInterceptor

// rpc请求处理的拦截器
typealias IRpcRequestInterceptor = IRequestInterceptor<IRpcRequest>

// rpc server启动的拦截器, 只有after()有效, before()无效(不会调用)
typealias IRpcServerInterceptor = IRequestInterceptor<Void?>