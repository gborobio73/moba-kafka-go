package com.reaktor.kafka.moba.streams

import com.reaktor.kafka.moba.model.Delta
import com.reaktor.kafka.moba.model.GameState
import com.reaktor.kafka.moba.model.GameStateWithDelta
import org.apache.kafka.streams.processor.StreamPartitioner

class Partitioner : StreamPartitioner<Int, GameState> {
    override fun partition(topic: String?, key: Int?, value: GameState?, numPartitions: Int): Int? {
        return calculatePartition(key, numPartitions)
    }
}

class DeltaPartitioner : StreamPartitioner<Int, Delta> {
    override fun partition(topic: String?, key: Int?, value: Delta?, numPartitions: Int): Int? {
        return calculatePartition(key, numPartitions)
    }
}

class GameStateWithDeltaPartitioner : StreamPartitioner<Int, GameStateWithDelta> {
    override fun partition(topic: String?, key: Int?, value: GameStateWithDelta?, numPartitions: Int): Int? {
        return calculatePartition(key, numPartitions)
    }
}

private fun calculatePartition(key: Int?, numPartitions: Int): Int? {
    if (key == null) return null // default partitioner
    val partition = key % numPartitions
    println(">>>> returning partition [$partition], key $key, partitions $numPartitions ")
    return partition
}