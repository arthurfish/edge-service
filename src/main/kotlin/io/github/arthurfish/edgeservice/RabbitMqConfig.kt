package io.github.arthurfish.edgeservice

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.SimpleMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMqConfig {
  @Bean
  fun converter(): SimpleMessageConverter {
    val converter = SimpleMessageConverter();
    converter.setAllowedListPatterns(listOf("io.github.arthurfish.edgeservice.*", "java.util.*"));
    return converter;
  }

  @Bean
  fun appenderCoreExchange() = TopicExchange("appender-core-exchange")

  @Bean
  fun responseQueue() = Queue("response-queue", true) // 持久化队列

  @Bean
  fun debugQueue() = Queue("response-queue", true) // 持久化队列

  @Bean
  fun responseBinding(
    responseQueue: Queue,
    appenderCoreExchange: TopicExchange
  ): Binding {
    return BindingBuilder.bind(responseQueue).to(appenderCoreExchange).with("result.#")
  }

  @Bean
  fun debugBinding(debugQueue: Queue, exchange: TopicExchange): Binding {
    // 使用具体的路由键来绑定队列，在之前的代码示例中，使用的是 "topic"
    return BindingBuilder.bind(debugQueue).to(exchange).with("#")
  }
}