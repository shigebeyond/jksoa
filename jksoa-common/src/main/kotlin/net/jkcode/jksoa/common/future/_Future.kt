package net.jkcode.jksoa.common.future

import net.jkcode.jksoa.common.IRpcResponse
import java.util.concurrent.CompletableFuture

// 异步响应
typealias IRpcResponseFuture = CompletableFuture<IRpcResponse>
