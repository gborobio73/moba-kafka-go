package com.reaktor.kafka.moba.streams

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.errors.TopicExistsException
import java.util.*
import java.util.concurrent.ExecutionException

fun createTopic(topic: String,
                partitions: Int,
                replication: Short,
                props: Properties
) {
  val newTopic = NewTopic(topic, partitions, replication)

  try {
    with(AdminClient.create(props)) {
      createTopics(listOf(newTopic)).all().get()
    }
  } catch (e: ExecutionException) {
    if (e.cause !is TopicExistsException) throw e
  }
}