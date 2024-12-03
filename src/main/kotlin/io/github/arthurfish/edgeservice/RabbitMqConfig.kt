package io.github.arthurfish.edgeservice

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {

  @Bean
  fun appenderCoreExchange(): TopicExchange {
    return TopicExchange("appender-core-exchange")
  }

  @Bean
  fun responseQueue(): Queue {
    return Queue("response-queue", true) // 持久化队列
  }

  @Bean
  fun responseBinding(
    responseQueue: Queue,
    appenderCoreExchange: TopicExchange
  ): Binding {
    return BindingBuilder.bind(responseQueue).to(appenderCoreExchange).with("result.#")
  }
}