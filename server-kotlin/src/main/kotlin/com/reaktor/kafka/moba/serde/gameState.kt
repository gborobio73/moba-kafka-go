package com.reaktor.kafka.moba.serde

import com.reaktor.kafka.moba.model.*
import io.confluent.kafka.serializers.KafkaJsonDeserializer
import io.confluent.kafka.serializers.KafkaJsonSerializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes

fun actionSerde(): Serde<Action> {

    val properties = hashMapOf("json.value.type" to Action::class.java)

    val serializer = KafkaJsonSerializer<Action>()
    serializer.configure(properties, false)

    val deserializer = KafkaJsonDeserializer<Action>()
    deserializer.configure(properties, false)

    return Serdes.serdeFrom(serializer, deserializer)
}

fun gameStateSerde(): Serde<GameState> {

    val properties = hashMapOf("json.value.type" to GameState::class.java)

    val serializer = KafkaJsonSerializer<GameState>()
    serializer.configure(properties, false)

    val deserializer = KafkaJsonDeserializer<GameState>()
    deserializer.configure(properties, false)

    return Serdes.serdeFrom(serializer, deserializer)
}
