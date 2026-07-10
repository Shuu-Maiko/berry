package com.shuu.berry.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String NOTIFICATION_QUEUE = "berry.notification.queue";
  public static final String NOTIFICATION_EXCHANGE = "berry.notification.exchange";
  public static final String NOTIFICATION_ROUTING_KEY = "berry.notification.routing.key";

  public static final String NOTIFICATION_DLQ = "berry.notification.dlq";
  public static final String NOTIFICATION_DLX = "berry.notification.dlx";
  public static final String NOTIFICATION_DLQ_ROUTING_KEY = "berry.notification.dlq.routing.key";

  @Bean
  public Queue notificationQueue() {
    return QueueBuilder.durable(NOTIFICATION_QUEUE)
        .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
        .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY)
        .build();
  }

  @Bean
  public DirectExchange notificationExchange() {
    return new DirectExchange(NOTIFICATION_EXCHANGE);
  }

  @Bean
  public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_ROUTING_KEY);
  }

  @Bean
  public Queue notificationDlq() {
    return QueueBuilder.durable(NOTIFICATION_DLQ).build();
  }

  @Bean
  public DirectExchange notificationDlx() {
    return new DirectExchange(NOTIFICATION_DLX);
  }

  @Bean
  public Binding notificationDlqBinding(Queue notificationDlq, DirectExchange notificationDlx) {
    return BindingBuilder.bind(notificationDlq).to(notificationDlx).with(NOTIFICATION_DLQ_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter());
    return template;
  }
}
