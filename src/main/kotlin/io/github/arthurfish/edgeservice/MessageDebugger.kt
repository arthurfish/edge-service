package io.github.arthurfish.edgeservice

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class MessageDebugger {

  @RabbitListener(queues = ["#{debugQueue.name}"])
  fun receiveMessage(@Payload message: Map<String, String>) {
    println("Received message: $message")
    // 这里你可以添加更多的调试逻辑，比如日志记录/断点调试
  }
}
