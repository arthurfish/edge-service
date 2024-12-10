package io.github.arthurfish.edgeservice

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult
import java.util.*

@RestController
@RequestMapping("/message")
class MessageMappingController(
  private val rabbitTemplate: RabbitTemplate,
  private val responseCompleteService: ResponseCompleteService
) {
  private val logger = LoggerFactory.getLogger(MessageMappingController::class.java)
  @PostMapping
  fun sendMessage(@RequestBody requestJson: RequestJsonMessageModel): DeferredResult<String> {
    logger.info("edge-service: Received request. requestJson: $requestJson")
    val requestId = UUID.randomUUID().toString() // 生成唯一 requestId
    val deferredResult = DeferredResult<String>(5000L) // 超时时间 5 秒
    val topic = requestJson.topic

    // 添加 requestId 到消息
    val rabbitMessage = requestJson.payload.toMutableMap()
    rabbitMessage["request_id"] = requestId

    // 将 DeferredResult 与 requestId 关联
    responseCompleteService.registerRequest(requestId, deferredResult)

    val message = MessageBuilder.withBody(rabbitMessage.toString().toByteArray())
      .copyHeaders(requestJson.headers)
      .setContentType("application/json")
      .build()
    // 将消息发送到 RabbitMQ
    rabbitTemplate.convertAndSend(
      "appender-core-exchange", // 交换机名称
      topic, // 路由键
      message
    )

    // 处理超时逻辑
    deferredResult.onTimeout {
      responseCompleteService.removeRequest(requestId)
      deferredResult.setErrorResult("Request timed out.")
    }

    return deferredResult
  }
}