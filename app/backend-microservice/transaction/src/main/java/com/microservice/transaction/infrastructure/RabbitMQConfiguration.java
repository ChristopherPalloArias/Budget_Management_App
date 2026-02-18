package com.microservice.transaction.infrastructure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
    @Value("${rabbitmq.queues.transaction-exchange}")
    private String transactionExchange;
    @Value("${rabbitmq.queues.transaction-created}")
    private String transactionCreatedQueue;

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }

    @Bean
    public Queue createdQueue() {
        return new Queue(transactionCreatedQueue, true);
    }

    @Bean
    public Binding bindingCreated(Queue createdQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(createdQueue)
                .to(transactionExchange)
                .with("transaction.created");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}