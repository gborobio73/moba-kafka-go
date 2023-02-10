# The client

```shell
go run client.go 127.0.0.1:29092 actions games 1 A2
```

where 127.0.0.1:29092 is teh kafka broker address, actions games are the input and output topics, 1 is the game id, and A2 is the player you are controlling.

Run the zombie AI

```shell
go run zombie_ai.go 127.0.0.1:29092 actions.test.2 43-L A2
```

# Some Go stuff

get dependencies:

`go get -d ./...`

# Some Kafka stuff

#### list topics

```shell
docker exec -it broker /usr/bin/kafka-topics --list --bootstrap-server broker:9092

docker exec -it broker /usr/bin/kafka-topics --describe --topic actions.test.partitions.1 --bootstrap-server broker:9092
```

#### consumer

```shell
docker exec -it broker /usr/bin/kafka-console-consumer --topic games.test.1 --partition 0 --bootstrap-server broker:9092 --from-beginning \
 --property print.key=true \
 --property key.separator=" : " \
 --key-deserializer "org.apache.kafka.common.serialization.IntegerDeserializer"
```

#### consumer partition

```shell
docker exec -it broker /usr/bin/kafka-console-consumer --topic games.test.partitions.1 --partition 0 --bootstrap-server broker:9092 --from-beginning --property print.key=true
```

#### alter partitions

```shell
docker exec -it broker /usr/bin/kafka-topics --alter --topic screens.0 --partitions 4 --bootstrap-server broker:9092
```

#### groups

```shell
docker exec -it broker /usr/bin/kafka-consumer-groups --list --bootstrap-server broker:9092
docker exec -it broker /usr/bin/kafka-consumer-groups --describe --group aggregating_reduce_partitions --bootstrap-server broker:9092
```

Producers and partitions
https://docs.confluent.io/platform/current/clients/producer.html#concepts

### SCALING

#### Repartition topics

```shell
docker exec -it broker /usr/bin/kafka-topics --alter --topic screens.0 --partitions 4 --bootstrap-server broker:9092
```

#### Clean the store topics

```shell
docker exec -it broker /usr/bin/kafka-streams-application-reset --application-id aggregating_reduce_partitions --bootstrap-servers broker:9092
```

#### error

topic aggregating_reduce_partitions-arenas-aggregate.3-repartition has invalid partitions: expected: 4; actual: 1. Use 'kafka.tools.StreamsResetter' tool to clean up invalid topics before processing.
