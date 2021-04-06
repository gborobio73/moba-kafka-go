// Example function-based Apache Kafka producer
package main

import (
	"client/game_state"
	"encoding/json"
	"fmt"
	"math/rand"
	"os"
	"os/signal"
	"strconv"
	"time"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

// go run zombie_ai.go 127.0.0.1:29092 actions.test.1 27 A2
func main() {

	if len(os.Args) != 5 {
		fmt.Fprintf(os.Stderr, "Usage: %s <broker> <topic> <game> <skip_player>\n", os.Args[0])
		os.Exit(1)
	}

	broker := os.Args[1]
	topic := os.Args[2]
	gameId, _ := strconv.Atoi(os.Args[3])
	player := os.Args[4]

	p, err := kafka.NewProducer(&kafka.ConfigMap{"bootstrap.servers": broker})

	if err != nil {
		fmt.Printf("Failed to create producer: %s\n", err)
		os.Exit(1)
	}

	fmt.Printf("Created Producer %v\n", p)

	// Optional delivery channel, if not specified the Producer object's
	// .Events channel is used.
	deliveryChan := make(chan kafka.Event)

	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	go func() {
		<-c
		fmt.Println("\nexiting")
		close(deliveryChan)
		os.Exit(0)
	}()

	for range time.Tick(time.Millisecond * 1000) {
		action := NewZombiAction(int32(gameId))
		if action.PlayerId != player {
			key := fmt.Sprintf("%d*%s", gameId, action.PlayerId)
			value, jsonErr := json.Marshal(action)
			if jsonErr != nil {
				fmt.Printf("Failed to serialize to json: %s\n", err)
			}
			msgErr := sendMessage(p, deliveryChan, topic, key, string(value))
			if msgErr != nil {
				fmt.Printf("Failed to queue message: %s\n", err)
			}
		}
	}
}

func sendMessage(producer *kafka.Producer, deliveryChan chan kafka.Event, topic string, key string, message string) error {
	err := producer.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Value:          []byte(message),
		Key:            []byte(key),
		Headers:        []kafka.Header{{Key: "myTestHeader", Value: []byte("header values are binary")}},
	}, deliveryChan)

	if err != nil {
		return fmt.Errorf(fmt.Sprintf("Failed to queue message: %s\n", err))
	}
	e := <-deliveryChan
	m := e.(*kafka.Message)

	if m.TopicPartition.Error != nil {
		return fmt.Errorf(fmt.Sprintf("Delivery failed: %v\n", m.TopicPartition.Error))
	}
	fmt.Printf("Delivered message '%s' to topic '%s' [%d] at offset %v\n",
		message, *m.TopicPartition.Topic, m.TopicPartition.Partition, m.TopicPartition.Offset)
	return nil
}

func NewZombiAction(gameId int32) *game_state.Action {

	player := newRandomPlayer()
	action := newRandomAction()
	direction := newRandomDirection()

	return game_state.NewAction(player, gameId, action, direction)
}

func newRandomDirection() string {
	rand.Seed(time.Now().UnixNano())
	num := randomNumber(1, 4)
	switch num {
	case 1:
		return "up"
	case 2:
		return "right"
	case 3:
		return "down"
	default:
		return "left"
	}
}

func newRandomPlayer() string {
	rand.Seed(time.Now().UnixNano())
	if randomNumber(1, 2)%2 == 0 {
		return fmt.Sprintf("A%d", randomNumber(1, 5))
	}
	return fmt.Sprintf("B%d", randomNumber(1, 5))
}

func newRandomAction() string {
	rand.Seed(time.Now().UnixNano())
	if randomNumber(1, 10) == 7 {
		return "gre"
	}
	return "mov"
}

func randomNumber(min int, max int) int {
	return rand.Intn(max-min+1) + min
}
