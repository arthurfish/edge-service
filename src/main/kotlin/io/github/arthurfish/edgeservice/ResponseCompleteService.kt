package io.github.arthurfish.edgeservice

import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.ConcurrentHashMap

@Service
class ResponseConsumer(
  private val responseCompleteService: ResponseCompleteService
) {

  private val log = LoggerFactory.getLogger(ResponseConsumer::class.java)

  @RabbitListener(queues = ["response-queue"])
  fun handleResponse(message: Map<String, String>) {
    val requestId = message["requestId"] as? String
    log.info("Response-queue: ACTIVATE.")
    if (requestId != null) {
      // 将响应映射回 HTTP 请求
      responseCompleteService.completeRequest(requestId, message.toString())
    }
  }
}

@Service
class ResponseCompleteService {

  // 保存 requestId 和 DeferredResult 的映射
  private val requestMap = ConcurrentHashMap<String, DeferredResult<String>>()

  /**
   * 注册一个新的请求
   */
  fun registerRequest(requestId: String, deferredResult: DeferredResult<String>) {
    requestMap[requestId] = deferredResult
  }

  /**
   * 根据 requestId 查找并完成对应的请求
   */
  fun completeRequest(requestId: String, response: String) {
    val deferredResult = requestMap.remove(requestId)
    val responseEntity = ResponseEntity
      .ok()
      .contentType(MediaType.APPLICATION_JSON)
      .body(response)
    deferredResult?.setResult(responseEntity.toString())
  }


  /**
   * 移除超时的请求
   */
  fun removeRequest(requestId: String) {
    requestMap.remove(requestId)
  }
}