package io.github.arthurfish.edgeservice

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/message")
class MessageController(
  private val rabbitTemplate: RabbitTemplate,
  private val responseHandler: ResponseHandler
) {

  @PostMapping
  fun sendMessage(@RequestBody message: Map<String, Any>): DeferredResult<String> {
    val requestId = UUID.randomUUID().toString() // 生成唯一 requestId
    val deferredResult = DeferredResult<String>(5000L) // 超时时间 5 秒

    // 添加 requestId 到消息
    val rabbitMessage = message.toMutableMap()
    rabbitMessage["requestId"] = requestId

    // 将 DeferredResult 与 requestId 关联
    responseHandler.registerRequest(requestId, deferredResult)

    // 将消息发送到 RabbitMQ
    rabbitTemplate.convertAndSend(
      "appender-core-exchange", // 交换机名称
      "appender.core.message", // 路由键
      rabbitMessage
    )

    // 处理超时逻辑
    deferredResult.onTimeout {
      responseHandler.removeRequest(requestId)
      deferredResult.setErrorResult("Request timed out.")
    }

    return deferredResult
  }
}