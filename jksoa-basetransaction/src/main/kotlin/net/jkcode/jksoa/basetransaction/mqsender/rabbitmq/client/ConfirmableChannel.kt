package net.jkcode.jksoa.basetransaction.mqsender.rabbitmq.client

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConfirmListener
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 支持异步确认投递的通道
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-25 11:51 AM
 */
class ConfirmableChannel(protected val channel: Channel): Channel by channel {

    /**
     * <发送编号 to 异步结果>
     */
    protected val publishFutures: TreeMap<Long, CompletableFuture<Void>> = TreeMap()

    init{
        // 1 开启confirm模式
        channel.confirmSelect();

        // 2 添加confirm回调 -- 更新异步结果
        channel.addConfirmListener(object : ConfirmListener {

            // 处理投递确认
            private fun handleConfirm(deliveryTag: Long, multiple: Boolean, ex: Exception?) {
                // 多个投递确认
                if (multiple) {
                    val fs = publishFutures.headMap(deliveryTag + 1)
                    for ((no, f) in fs)
                        if(ex == null)
                            f.complete(null)
                        else
                            f.completeExceptionally(ex)
                    fs.clear()
                    return
                }

                // 单个投递确认
                val f = publishFutures.remove(deliveryTag)
                if(ex == null)
                    f?.complete(null)
                else
                    f?.completeExceptionally(ex)
            }

            // 投递失败: broker丢失消息, 不保证消息能发送到消费者
            override fun handleNack(deliveryTag: Long, multiple: Boolean) {
                handleConfirm(deliveryTag, multiple, Exception("消息丢失"))
            }

            // 投递成功
            override fun handleAck(deliveryTag: Long, multiple: Boolean) {
                handleConfirm(deliveryTag, multiple, null)
            }
        })
    }

    /**
     * 构建投递确认的异步结果
     * @param publishLambda 投递处理
     * @return
     */
    inline fun buildConfirmFuture(publishLambda: () -> Unit): CompletableFuture<Void> {
        val f = CompletableFuture<Void>()
        // 记录 <发送编号 to 异步结果>
        publishFutures.put(channel.nextPublishSeqNo, f)

        try {
            // 发送消息
            publishLambda.invoke()
        }catch (e: Exception){
            f.completeExceptionally(e)
        }

        return f
    }

    /**
     * 投递消息+异步确认
     */
    public fun basicPublishAndAsynConfirm(exchange: String, routingKey: String, props: AMQP.BasicProperties, body: ByteArray): CompletableFuture<Void> {
        return buildConfirmFuture{
            basicPublish(exchange, routingKey, props, body)
        }
    }

    /**
     * 投递消息+异步确认
     */
    public fun basicPublishAndAsynConfirm(exchange: String, routingKey: String, mandatory: Boolean, props: AMQP.BasicProperties, body: ByteArray): CompletableFuture<Void> {
        return buildConfirmFuture{
            basicPublish(exchange, routingKey, mandatory, props, body)
        }
    }

    /**
     * 投递消息+异步确认
     */
    public fun basicPublishAndAsynConfirm(exchange: String, routingKey: String, mandatory: Boolean, immediate: Boolean, props: AMQP.BasicProperties, body: ByteArray): CompletableFuture<Void> {
        return buildConfirmFuture{
            basicPublish(exchange, routingKey, mandatory, immediate, props, body)
        }
    }
}