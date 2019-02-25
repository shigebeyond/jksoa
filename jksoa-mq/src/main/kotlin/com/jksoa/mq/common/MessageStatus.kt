package net.jkcode.jksoa.mq.common

/**
 * 消息状态：0 未处理 1 锁定 2 完成 3 失败(超过时间或超过重试次数)
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-25 9:43 AM
 */
enum class MessageStatus {
    UNDO, // 未处理
    LOCKED, // 锁定
    DONE, // 完成
    FAIL // 失败
}