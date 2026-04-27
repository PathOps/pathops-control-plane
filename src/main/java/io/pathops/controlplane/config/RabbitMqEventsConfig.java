package io.pathops.controlplane.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class RabbitMqEventsConfig {

    @Value("${pathops.events.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${pathops.events.rabbitmq.projector-queue}")
    private String projectorQueueName;

    @Value("${pathops.events.rabbitmq.tenant-created-routing-key}")
    private String tenantCreatedRoutingKey;

    @Bean
    TopicExchange pathopsEventsExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue pathopsProjectorEventsQueue() {
        return QueueBuilder
            .durable(projectorQueueName)
            .build();
    }

    @Bean
    Binding tenantCreatedProjectorBinding(
        Queue pathopsProjectorEventsQueue,
        TopicExchange pathopsEventsExchange
    ) {
        return BindingBuilder
            .bind(pathopsProjectorEventsQueue)
            .to(pathopsEventsExchange)
            .with(tenantCreatedRoutingKey);
    }
}