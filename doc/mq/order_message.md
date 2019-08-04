# 有序消息

https://blog.csdn.net/earthhour/article/details/78323026


有序消息, 指的是可以按照消息的发送顺序来消费。

jksoa-mq可以保证单个队列中消息有序。

但是默认情况下, 消息发送是会采用轮询的方式发送到不同的队列中。如图：

![produce_concurrently](img/produce_concurrently.png)

而消费端消费的时候，是会分配到多个queue的，多个queue是同时拉取提交消费。如图：


当DefaultMQPushConsumer 被设置好，你可能需要决定消费是顺序的还是并发的
⊙顺序
有序的消息意味着消息的使用顺序与生产者为每个消息队列发送的顺序相同。如果你的使用场景要求必是须顺序的，你要确保只用一个队列存放消息。
警告：如果消费顺序被指定，最大的消费并发数就是这个消费者组的消息队列的订阅数

⊙并发：
当消费消息是并发的，最大的消息消费数只受限于每个消费客户端线程池规定的数。
警告：这个模式下顺序不在被保证。

但是同一条queue里面，jksoa-mq的确是能保证FIFO的。那么要做到顺序消息，应该怎么实现呢——把消息确保投递到同一条queue。

下面用订单进行示例。一个订单的顺序流程是：创建、付款、推送、完成。订单号相同的消息会被先后发送到同一个队列中，消费时，同一个OrderId获取到的肯定是同一个队列。

jksoa-mq消息生产端示例代码如下：


/**
 * Producer，发送顺序消息
 */
public class Producer {
	
    public static void main(String[] args) throws IOException {
        try {
            DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");
 
            producer.setNamesrvAddr("10.11.11.11:9876;10.11.11.12:9876");
 
            producer.start();
 
            String[] tags = new String[] { "TagA", "TagC", "TagD" };
            
            // 订单列表
            List<OrderDemo> orderList =  new Producer().buildOrders();
            
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = sdf.format(date);
            for (int i = 0; i < 10; i++) {
                // 加个时间后缀
                String body = dateStr + " Hello jksoa-mq " + orderList.get(i);
                Message msg = new Message("TopicTestjjj", tags[i % tags.length], "KEY" + i, body.getBytes());
 
                SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        Long id = (Long) arg;
                        long index = id % mqs.size();
                        return mqs.get((int)index);
                    }
                }, orderList.get(i).getOrderId());//订单id
 
                System.out.println(sendResult + ", body:" + body);
            }
            
            producer.shutdown();
 
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (MQBrokerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.in.read();
    }
    
    /**
     * 生成模拟订单数据 
     */
    private List<OrderDemo> buildOrders() {
    	List<OrderDemo> orderList = new ArrayList<OrderDemo>();
 
    	OrderDemo orderDemo = new OrderDemo();
        orderDemo.setOrderId(15103111039L);
    	orderDemo.setDesc("创建");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103111065L);
    	orderDemo.setDesc("创建");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103111039L);
    	orderDemo.setDesc("付款");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103117235L);
    	orderDemo.setDesc("创建");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103111065L);
    	orderDemo.setDesc("付款");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103117235L);
    	orderDemo.setDesc("付款");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103111065L);
    	orderDemo.setDesc("完成");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103111039L);
    	orderDemo.setDesc("推送");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103117235L);
    	orderDemo.setDesc("完成");
    	orderList.add(orderDemo);
    	
    	orderDemo = new OrderDemo();
    	orderDemo.setOrderId(15103111039L);
    	orderDemo.setDesc("完成");
    	orderList.add(orderDemo);
    	
    	return orderList;
    }
输出：


从图中红色框可以看出，orderId等于15103111039的订单被顺序放入queueId等于7的队列。queueOffset同时在顺序增长。

发送时有序，接收（消费）时也要有序，才能保证顺序消费。如下这段代码演示了普通消费（非有序消费）的实现方式。


/**
 * 普通消息消费
 */
public class Consumer {
 
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name_3");
        consumer.setNamesrvAddr("10.11.11.11:9876;10.11.11.12:9876");
        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
 
        consumer.subscribe("TopicTestjjj", "TagA || TagC || TagD");
 
        consumer.registerMessageListener(new MessageListenerConcurrently() {
 
            Random random = new Random();
 
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.print(Thread.currentThread().getName() + " Receive New Messages: " );
                for (MessageExt msg: msgs) {
                    System.out.println(msg + ", content:" + new String(msg.getBody()));
                }
                try {
                    //模拟业务逻辑处理中...
                    TimeUnit.SECONDS.sleep(random.nextInt(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
 
        consumer.start();
 
        System.out.println("Consumer Started.");
    }
}

输出：


可见，订单号为15103111039的订单被消费时顺序完成乱了。所以用MessageListenerConcurrently这种消费者是无法做到顺序消费的，采用下面这种方式就做到了顺序消费：


/**
 * 顺序消息消费，带事务方式（应用可控制Offset什么时候提交）
 */
public class ConsumerInOrder {
 
    public static void main(String[] args) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name_3");
        consumer.setNamesrvAddr("10.11.11.11:9876;10.11.11.12:9876");
        /**
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
 
        consumer.subscribe("TopicTestjjj", "TagA || TagC || TagD");
 
        consumer.registerMessageListener(new MessageListenerOrderly() {
 
            Random random = new Random();
 
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                context.setAutoCommit(true);
                System.out.print(Thread.currentThread().getName() + " Receive New Messages: " );
                for (MessageExt msg: msgs) {
                    System.out.println(msg + ", content:" + new String(msg.getBody()));
                }
                try {
                    //模拟业务逻辑处理中...
                    TimeUnit.SECONDS.sleep(random.nextInt(10));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });
 
        consumer.start();
 
        System.out.println("Consumer Started.");
    }
}
输出：

MessageListenerOrderly能够保证顺序消费，从图中我们也看到了期望的结果。图中的输出是只启动了一个消费者时的输出，看起来订单号还是混在一起，但是每组订单号之间是有序的。因为消息发送时被分配到了三个队列（参见前面生产者输出日志），那么这三个队列的消息被这唯一消费者消费。

如果启动2个消费者呢？那么其中一个消费者对应消费2个队列，另一个消费者对应消费剩下的1个队列。

如果启动3个消费者呢？那么每个消费者都对应消费1个队列，订单号就区分开了。输出变为这样：

消费者1输出：



消费者2输出：



消费者3输出：



很完美，有木有？！

按照这个示例，把订单号取了做了一个取模运算再丢到selector中，selector保证同一个模的都会投递到同一条queue。即： 相同订单号的--->有相同的模--->有相同的queue。最后就会类似这样：



总结：

jksoa-mq的顺序消息需要满足2点：

1.Producer端保证发送消息有序，且发送到同一个队列。
2.consumer端保证消费同一个队列。

