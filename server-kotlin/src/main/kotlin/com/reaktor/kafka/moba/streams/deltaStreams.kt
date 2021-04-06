package com.reaktor.kafka.moba.streams

import com.reaktor.kafka.moba.ai.computeGrenateThrowAndAddDelta
import com.reaktor.kafka.moba.ai.computePlayerMoveAndAddDelta
import com.reaktor.kafka.moba.model.*
import com.reaktor.kafka.moba.serde.*
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import java.util.*

fun buildAggregatedWithDeltaStreams(inputTopic: String,
                     outputTopic: String,
                     outputDeltaTopic: String,
                     props: Properties): KafkaStreams {
    val builder = StreamsBuilder()
    val consumed = builder.stream(inputTopic, Consumed.with(Serdes.String(), actionSerde()))
    consumed.print(Printed.toSysOut<String, Action>().withLabel("Consumed record"))
    val aggregated = consumed
        // Set key to game id
        .map { _, v -> KeyValue(v.gameId, v) }
        // Group by game id
        .groupByKey(Grouped.with(Serdes.Integer(), actionSerde()))
        // reduce to the new game state
        .aggregate(
            { GameStateWithDelta(0, null, initialGameState()) },
            { gameId, action, aggregated ->
                when (action.actType) {
                    "mov" -> {
                        computePlayerMoveAndAddDelta(gameId, action, aggregated.state)
                    }
                    "gre" -> {
                        computeGrenateThrowAndAddDelta(gameId, action, aggregated.state)
                    }
                    else -> {
                        println(">>>> Unknown player action '${action.actType}'")
                        GameStateWithDelta(
                            gameId,
                            null,
                            GameState(gameId, aggregated.state.players, aggregated.state.walls))
                    }
                }
            },
            Materialized.`as`<Int, GameStateWithDelta, KeyValueStore<Bytes, ByteArray>>("games-with-delta-store")
                .withKeySerde(Serdes.Integer())
                .withValueSerde(gameStateWithDeltaSerde())
        )
        .toStream()

    aggregated.print(Printed.toSysOut<Int, GameStateWithDelta>().withLabel("New Game Sate With Delta"))
    aggregated.to(outputTopic, Produced.with(Serdes.Integer(), gameStateWithDeltaSerde(), GameStateWithDeltaPartitioner()))

    val delta = aggregated
        .filter { _, v ->
            v != null
        }.map { k, v ->
            KeyValue(k, v.delta)
        }

    delta.print(Printed.toSysOut<Int, Delta>().withLabel("New Delta"))
    delta.to(outputDeltaTopic, Produced.with(Serdes.Integer(), deltaSerde(), DeltaPartitioner()))

    val gameState = aggregated
        .filter { _, v ->
            v != null
        }
        .map { k, v ->
            KeyValue(k, v.state)
        }
    gameState.print(Printed.toSysOut<Int, GameState>().withLabel("New Game State"))
    gameState.to(outputTopic, Produced.with(Serdes.Integer(), gameStateSerde(), Partitioner()))

    return KafkaStreams(builder.build(), props)
}