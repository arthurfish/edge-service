package io.github.arthurfish.edgeservice

class RequestJsonMessageModel(
  val headers: Map<String, String>,
  val topic: String = "default.topic",
  val payload: Map<String, String>,
)