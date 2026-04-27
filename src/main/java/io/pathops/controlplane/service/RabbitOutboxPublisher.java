package io.pathops.controlplane.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import io.pathops.controlplane.model.OutboxEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitOutboxPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(OutboxEvent outboxEvent) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setMessageId(outboxEvent.getEventId().toString());
        props.setType(outboxEvent.getEventType());
        props.setTimestamp(Date.from(Instant.now()));

        Message message = new Message(
            outboxEvent.getPayload().getBytes(StandardCharsets.UTF_8),
            props
        );

        rabbitTemplate.send(
            outboxEvent.getExchangeName(),
            outboxEvent.getRoutingKey(),
            message
        );
    }
}