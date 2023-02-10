# moba-kafka-go

_Kafka as a backend_. We'll implement a MOBA game using Kafka consumers, producers and stream processors. We'll write code in GO (consumer and producer) and in Kotlin (stream processor). Then, we'll scale it!
NOTE: it's an introduction to these topics and we'll write the code as we go.

```
Game 1-A
Controlling Player A2 (3 grenades left)
+------------------------------------------------------------+
|       A1-5              A3-5                          A5-5 |
|             A2-3                    A4-5                   |
|             XXXX  XXXX                    XXXX  XXXX       |
|                                                            |
| XXXX  XXXX                                                 |
|                                                 XXXX  XXXX |
|                                                            |
|       XXXX  XXXX                    XXXX  XXXX             |
|                   B2-5                    B4-5             |
| B1-5                          B3-5              B5-5       |
+------------------------------------------------------------+
```

Producers partition strategy
librdkafka https://docs.confluent.io/5.0.0/clients/librdkafka/CONFIGURATION_8md.html

https://docs.confluent.io/platform/current/clients/producer.html#ak-producer-configuration

Future improvements
split actions topic into 2: moves and grenades. MAke the stream processor read from both
reload player grenades by pressing R -> you get one grenade, up to 5

Start client

```
> cd moba-kafka-go/client-go/cmd/client
> go run client.go 127.0.0.1:29092 actions games 1 A1
```

Start server

```
> cd moba-kafka-go/server-kotlin
> ./gradlew clean build runApp -Pinput=actions.partitions,5 -Poutput=games.partitions,5 -Pport=8092
```

Check the presentation [here](./kafka-moba.pdf)
