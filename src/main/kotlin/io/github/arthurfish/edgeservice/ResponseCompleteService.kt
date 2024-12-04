package io.github.arthurfish.edgeservice

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.ConcurrentHashMap

@Service
class ResponseConsumer(
  private val responseCompleteService: ResponseCompleteService
) {

  @RabbitListener(queues = ["response-queue"])
  fun handleResponse(message: Map<String, Any>) {
    val requestId = message["requestId"] as? String
    val result = message["result"] as? String

    if (requestId != null && result != null) {
      // 将响应映射回 HTTP 请求
      responseCompleteService.completeRequest(requestId, result)
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
    deferredResult?.setResult(response)
  }

  /**
   * 移除超时的请求
   */
  fun removeRequest(requestId: String) {
    requestMap.remove(requestId)
  }
}