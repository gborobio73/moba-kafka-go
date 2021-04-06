package com.reaktor.kafka.moba.serde

import com.reaktor.kafka.moba.model.*
import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.confluent.kafka.serializers.KafkaJsonSerializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes

fun deltaSerde(): Serde<Delta> {

    val properties = hashMapOf("json.value.type" to Delta::class.java)

    val serializer = KafkaJsonSerializer<Delta>()
    serializer.configure(properties, false)

    val deserializer = KafkaJsonDeserializer<Delta>()
    deserializer.configure(properties, false)

    return Serdes.serdeFrom(serializer, deserializer)
}

fun gameStateWithDeltaSerde(): Serde<GameStateWithDelta> {

    val properties = hashMapOf("json.value.type" to GameStateWithDelta::class.java)

    val serializer = KafkaJsonSerializer<GameStateWithDelta>()
    serializer.configure(properties, false)

    val deserializer = KafkaJsonDeserializer<GameStateWithDelta>()
    deserializer.configure(properties, false)

    return Serdes.serdeFrom(serializer, deserializer)
}


