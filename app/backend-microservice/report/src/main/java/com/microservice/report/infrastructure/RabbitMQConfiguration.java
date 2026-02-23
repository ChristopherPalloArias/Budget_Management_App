package com.microservice.report.infrastructure;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    @Value("${rabbitmq.queues.transaction-updated}")
    private String transactionUpdatedQueue;

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }

    @Bean
    public Queue createdQueue() {
        return new Queue(transactionCreatedQueue, true);
    }

    @Bean
    public Queue updatedQueue() {
        return new Queue(transactionUpdatedQueue, true);
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