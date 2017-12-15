package com.jksoa.protocol

/**
 * 协议类型
 *
 * @ClassName: Protocol
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
public enum class ProtocolType {
    RMI{
        override val protocol: IProtocol = RmiProtocol
    };

    /**
     * 获得对应的协议
     */
    public abstract val protocol: IProtocol
}
