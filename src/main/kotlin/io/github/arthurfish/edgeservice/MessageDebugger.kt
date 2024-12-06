package io.github.arthurfish.edgeservice

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class MessageDebugger {

  @RabbitListener(queues = ["#{debugQueue.name}"])
  fun receiveMessage(@Payload message: Map<String, String>) {
    println("Received message: $message")
  }
}
