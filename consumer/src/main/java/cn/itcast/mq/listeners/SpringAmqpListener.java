package cn.itcast.mq.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
@Slf4j
public class SpringAmqpListener {
    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue(String message) {
        log.info("接收到队列消息：" + message);
        throw new RuntimeException("故意的");
    }
}