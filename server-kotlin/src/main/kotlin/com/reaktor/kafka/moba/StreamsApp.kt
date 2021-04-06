@file:JvmName("StreamsApp")

package com.reaktor.kafka.moba

import com.reaktor.kafka.moba.api.InstanceResolverRestApi
import com.reaktor.kafka.moba.api.RestApi
import com.reaktor.kafka.moba.streams.buildAggregatedStreams
import com.reaktor.kafka.moba.streams.createTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig
import java.util.*
import kotlin.system.exitProcess

const val DEFAULT_HOST = "localhost"
fun main(args: Array<String>) {
  println(args.size)
  if (args.size != 3) {
    println("Please provide command line arguments:\n" +
            "   <input,partitions> <output,partitions> <port>")
    exitProcess(1)
  }

  val inputTopic = args[0].split(',')[0] //actions,1
  val inputTopicPartitions = args[0].split(',')[1].toInt()
  val outputTopic = args[1].split(',')[0] //games,1
  val outputTopicPartitions = args[1].split(',')[1].toInt()
  val port = args[2]

  val props = Properties()
  props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "127.0.0.1:29092"
  val applicationId = "moba-streams-app--${args[0]}-${args[1]}".replace(",","-" )
  props[StreamsConfig.APPLICATION_ID_CONFIG] = applicationId
  props[StreamsConfig.APPLICATION_SERVER_CONFIG] = "$DEFAULT_HOST:$port"
  props[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0
  props[StreamsConfig.REPLICATION_FACTOR_CONFIG] = 1
  props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

  createTopic(inputTopic, inputTopicPartitions, 1, props)
  createTopic(outputTopic, outputTopicPartitions, 1, props)
  // createTopic("games.delta.test.1", 1, 1, props) // -> only for deltaStreams

  val storeName = "$outputTopic-store"
  val streams = buildAggregatedStreams(inputTopic, outputTopic, props, storeName)

  val api = RestApi(streams, DEFAULT_HOST, port.toInt())
  val stop = api.start()

  streams.start()
  Runtime.getRuntime().addShutdownHook(Thread {
    streams.close()
    stop()
  })
}