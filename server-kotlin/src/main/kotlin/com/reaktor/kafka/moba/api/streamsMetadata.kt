package com.reaktor.kafka.moba.api

import com.reaktor.kafka.moba.streams.Partitioner
import org.apache.kafka.common.serialization.IntegerSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.StreamsMetadata
import java.util.stream.Collectors

fun streamsAllMetadata(streams: KafkaStreams): List<HostStoreInfo> {
    // Get metadata for all the instances of this Kafka Streams application
    return streams.allMetadata().stream().map { metadata ->
        HostStoreInfo(
            metadata.host(),
            metadata.port(),
            metadata.stateStoreNames()
        )
    }.collect(Collectors.toList())
}

fun streamsMetadataForStoreAndKey(
    streams: KafkaStreams,
    store: String,
    key: Int
): HostStoreInfo {
//    val metadata = streams.metadataForKey(store, key, IntegerSerializer())
    val metadata = streams.metadataForKey(store, key, Partitioner())
    return HostStoreInfo(
        metadata.host(),
        metadata.port(),
        metadata.stateStoreNames()
    )
}