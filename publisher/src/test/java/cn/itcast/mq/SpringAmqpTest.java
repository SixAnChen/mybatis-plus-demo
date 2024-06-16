package cn.itcast.mq;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: SixAnChen
 */
@SpringBootTest
@Slf4j
@SuppressWarnings("all")
public class SpringAmqpTest {
    @Autowired
    public RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到队列
     */
    @Test
    void testSendMsg() {
        rabbitTemplate.convertAndSend("simple.queue", "hello, spring amqp");
    }

    /**
     * work 模型：一个生产者对应多个消费者
     * @throws InterruptedException
     */
    @Test
    void testworkQueue() throws InterruptedException {
        String queueName = "work.queue";
        for (int i = 1; i <= 50; i++) {
            String queueMsg = "hello, worker, message_" + i;
            rabbitTemplate.convertAndSend(queueName, queueMsg);
            Thread.sleep(20);
        }
    }

    /**
     * 发送消息到 fanout 交换机
     */
    @Test
    void testSendFanout() {
        String exchangeName = "test.fanout";
        String msg = "hello, fanout exchange";
        rabbitTemplate.convertAndSend(exchangeName, null, msg);
    }

    /**
     * 发送消息到 direct 交换机
     */
    @Test
    void testSendDirect() {
        String exchangeName = "test.direct";
        String msg = "blue message";
        rabbitTemplate.convertAndSend(exchangeName, "blue", msg);
    }

    /**
     * 发送消息到 topic 交换机
     */
    @Test
    void testSendTopic() {
        String exchangeName = "test.topic";
        String msg = "china news";
        rabbitTemplate.convertAndSend(exchangeName, "china.news", msg);
    }

    @Test
    void testSendObject() {
        Map<String, Object> msg = new HashMap<>(2);
        msg.put("name", "张三");
        msg.put("age", 20);
        rabbitTemplate.convertAndSend("object.queue", msg);
    }

    @Test
    void testPublisherConfirm() throws InterruptedException {
        // 创建 CorrelationData 对象
        CorrelationData cd = new CorrelationData();
        //  给Future添加ConfirmCallback
        cd.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable throwable) {
                // 处理发送消息失败的情况, 基本不会触发
                System.out.println("发送消息失败：" + throwable.getMessage());
            }

            @Override
            public void onSuccess(CorrelationData.Confirm confirm) {
                // 处理发送消息成功的逻辑
                if (confirm.isAck()) {
                    log.debug("消息发送成功, 收到 ack");
                } else {
                    log.error("消息发送失败, 收到 nack, reason: {} ", confirm.getReason());
                }
            }
        });
        //  发送消息
        rabbitTemplate.convertAndSend("test.direct", "yellow", "这是一条测试消息", cd);
        // Thread.sleep(1000);
    }



    @Test
    void testPageOut() {
        Message message = MessageBuilder
                .withBody("hello".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT).build();
        for (int i = 0; i < 1000000; i++) {
            rabbitTemplate.convertAndSend("lazy.queue", message);
        }
    }
}