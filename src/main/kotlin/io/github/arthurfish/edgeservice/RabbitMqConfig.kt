package io.github.arthurfish.edgeservice

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.HeadersExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.amqp.support.converter.SimpleMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.Header

@Configuration
class RabbitMqConfig {
  @Bean
  fun converter(): MessageConverter = AppenderRabbitmqMessageConverter()

  @Bean
  fun rabbitTemplate(connectionFactory: ConnectionFactory, converter: MessageConverter): RabbitTemplate {
    return RabbitTemplate(connectionFactory).apply {
      messageConverter = converter()
    }
  }

  @Bean
  fun appenderCoreExchange() = HeadersExchange("appender-core-exchange")

  @Bean
  fun responseQueue() = Queue("response-queue", true) // 持久化队列

  @Bean
  fun debugQueue() = Queue("debug-queue", true) // 持久化队列

  @Bean
  fun responseBinding(
    responseQueue: Queue,
    appenderCoreExchange: HeadersExchange
  ): Binding {
    return BindingBuilder.bind(responseQueue)
      .to(appenderCoreExchange)
      .whereAny("channel_operation_result", "request_id").exist()
  }

  @Bean
  fun debugBinding(debugQueue: Queue, exchange: HeadersExchange): Binding {
    // 使用具体的路由键来绑定队列，在之前的代码示例中，使用的是 "topic"
    return BindingBuilder.bind(debugQueue)
      .to(exchange)
      .whereAny("debug").exist()
  }
}