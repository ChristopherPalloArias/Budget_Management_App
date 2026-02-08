package com.microservice.transaction.infrastructure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {
    public static final String TRANSACTION_EXCHANGE = "transaction-exchange";
    public static final String TRANSACTION_CREATED_QUEUE = "transaction.created.queue";
    public static final String TRANSACTION_UPDATED_QUEUE = "transaction.updated.queue";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public Queue createdQueue() {
        return new Queue(TRANSACTION_CREATED_QUEUE, true);
    }

    @Bean
    public Queue updatedQueue() {
        return new Queue(TRANSACTION_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding bindingCreated(Queue createdQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(createdQueue)
                .to(transactionExchange)
                .with("transaction.created");
    }

    @Bean
    public Binding bindingUpdated(Queue updatedQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(updatedQueue)
                .to(transactionExchange)
                .with("transaction.updated");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}

