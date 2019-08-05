package net.jkcode.jksoa.mq.consumer.suspend

/**
 * 拉取消费的暂停进度
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-05 6:44 PM
 */
data class PullConsumeSuspendProgress(
        public val endTime: Long, // 暂停的结束时间
        public val startId: Long // 开始消息id
)