package net.jkcode.jksoa.rpc.example

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.rpc.client.jphp.IP2pService
import java.rmi.RemoteException

/**
 * 远程调用php代码的服务接口
 *
 * @author shijianhang
 * @create 2022-10-15 下午7:37
 **/
@RemoteService
interface IP2pTestService:IP2pService{
}