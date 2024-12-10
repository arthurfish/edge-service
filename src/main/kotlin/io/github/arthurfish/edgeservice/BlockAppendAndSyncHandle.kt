package io.github.arthurfish.edgeservice

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.HeadersExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.util.LinkedList


@Configuration
class BlockSyncRabbitMqConfig {

  @Bean
  fun responseBindingAboutPlainBlockOperationResult(
    responseQueue: Queue,
    appenderCoreExchange: HeadersExchange
  ): Binding {
    return BindingBuilder.bind(responseQueue)
      .to(appenderCoreExchange)
      .whereAll("channel_block_result", "request_id").exist()
  }

  @Bean
  fun blockSyncQueue() = Queue("block-sync-queue", true) // 持久化队列
}

@Service
class BlockResponseHandler(private val rabbitTemplate: RabbitTemplate,
  private val completeService: ResponseCompleteService) {
  private val log = LoggerFactory.getLogger(BlockResponseHandler::class.java)
  private val syncBlockCache: LinkedList<Map<String, String>> = LinkedList()


  @RabbitListener(queues = ["#{blockSyncQueue.name}"])
  fun blockSyncHandle(message: Map<String, String>){
    val channelBlockResult = message["channel_block_result"]!!
    val requestId = message["request_id"]!!
    if(channelBlockResult == "block_accepted"){
      completeService.completeRequest(requestId, message.toString())
    }else if(channelBlockResult == "syncing"){
      syncBlockCache.add(message)
    }else if(channelBlockResult == "sync_complete"){
      completeService.completeRequest(requestId, message.toString())
    }
  }
}