package io.github.arthurfish.edgeservice

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
class ResponseConsumer(
  private val responseHandler: ResponseHandler
) {

  @RabbitListener(queues = ["response-queue"])
  fun handleResponse(message: Map<String, Any>) {
    val requestId = message["requestId"] as? String
    val result = message["result"] as? String

    if (requestId != null && result != null) {
      // 将响应映射回 HTTP 请求
      responseHandler.completeRequest(requestId, result)
    }
  }
}