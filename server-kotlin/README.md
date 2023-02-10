# Overview

Produce messages to and consume messages from a Kafka cluster using the Kotlin
version of Java Producer and Consumer, and Kafka Streams API.

## Documentation

This serves is based on:

<https://docs.confluent.io/platform/current/clients/examples/kotlin.html>

Run the server with default config (see gradle.properties)

```shell
./gradlew runApp
```

Run the server with specific conf

```shell
 ./gradlew clean build runApp -Pinput=actions.partitions,5 -Poutput=games.partitions,5 -Pport=8091
```
