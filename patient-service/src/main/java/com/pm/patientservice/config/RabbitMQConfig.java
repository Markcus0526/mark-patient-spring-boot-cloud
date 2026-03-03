package com.pm.patientservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue patientRabbitQueue() {
        return new Queue("patientRabbitQueue");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new org.springframework.amqp.support.converter.SimpleMessageConverter();
    }

//    @Bean
//    public MessageConverter jsongMessageConverter() {
//        return new JacksonJsonMessageConverter();
//    }

//    @Bean
//    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(jsongMessageConverter());
//        return rabbitTemplate;
//    }
}
