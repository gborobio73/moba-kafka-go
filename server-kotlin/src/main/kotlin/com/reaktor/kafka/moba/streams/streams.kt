package com.reaktor.kafka.moba.streams

import com.reaktor.kafka.moba.ai.computePlayerMove
import com.reaktor.kafka.moba.ai.computeGrenateThrow
import com.reaktor.kafka.moba.model.Action
import com.reaktor.kafka.moba.model.GameState
import com.reaktor.kafka.moba.model.initialGameState
import com.reaktor.kafka.moba.serde.actionSerde
import com.reaktor.kafka.moba.serde.gameStateSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import java.util.*

fun buildConsumerStreams(inputTopic: String, props: Properties): KafkaStreams {
    val builder = StreamsBuilder()
    val consumed = builder.stream(inputTopic, Consumed.with(Serdes.String(), actionSerde()))
    consumed.foreach {
        k: String, v: Action -> println("consumed key $k, value $v")
    }
    return KafkaStreams(builder.build(), props)
}

fun buildConsumerAndProducerStreams(inputTopic: String, outputTopic: String, props: Properties): KafkaStreams {
    val builder = StreamsBuilder()
    val consumed = builder.stream(inputTopic, Consumed.with(Serdes.String(), actionSerde()))
    consumed.print(Printed.toSysOut<String, Action>().withLabel("Consumed record"))

    val produced = consumed.map {
        _: String, v: Action ->
            val gameState = initialGameState(v.gameId)
            KeyValue(v.gameId, gameState)
    }
    produced.print(Printed.toSysOut<Int, GameState>().withLabel("Produced record"))
    produced.to(outputTopic, Produced.with(Serdes.Integer(), gameStateSerde()))

    return KafkaStreams(builder.build(), props)
}

fun buildAggregatedStreams(inputTopic: String, outputTopic: String, props: Properties, storeName: String): KafkaStreams {
    val builder = StreamsBuilder()
    val consumed = builder.stream(inputTopic, Consumed.with(Serdes.String(), actionSerde()))
    consumed.print(Printed.toSysOut<String, Action>().withLabel("Consumed record"))

    val aggregated = consumed
        .map {
            _: String, v: Action -> KeyValue(v.gameId, v)
        }
        .groupByKey(Grouped.with(Serdes.Integer(), actionSerde()))
        .aggregate(
            { initialGameState()},
            { gameId: Int, action: Action, aggregated: GameState ->
                // produce a new game state
                println(">>>> Computing aggregation for game $gameId")
                when (action.actType) {
                    "mov" -> computePlayerMove(gameId, action, aggregated)
                    "gre" -> computeGrenateThrow(gameId, action, aggregated)
                    else -> {
                        println(">>>> Unknown player action '${action.actType}'")
                        GameState(gameId, aggregated.players, aggregated.walls)
                    }
                }
            },
            Materialized.`as`<Int, GameState, KeyValueStore<Bytes, ByteArray>>(storeName)
                .withKeySerde(Serdes.Integer())
                .withValueSerde(gameStateSerde())
        ).toStream()
    aggregated.print(Printed.toSysOut<Int, GameState>().withLabel("Aggregated record"))
    aggregated.to(outputTopic, Produced.with(Serdes.Integer(), gameStateSerde(), Partitioner()))
    return KafkaStreams(builder.build(), props)
}
