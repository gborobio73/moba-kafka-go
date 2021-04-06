package goroutines

import (
	"client/game_state"
	utils "client/goroutines/utils"
	"encoding/json"
	"fmt"
	"sync"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func StartKafkaProducer(gameId int32, player string, broker string, topic string, keyAction <-chan KeyAction, exit <-chan bool, wg *sync.WaitGroup) error {
	kafkaProducer, err := kafka.NewProducer(&kafka.ConfigMap{
		"bootstrap.servers": broker,
	})
	if err != nil {
		return fmt.Errorf("Failed to create producer: %s", err)
	}
	utils.LogProducerCreated(kafkaProducer)

	wg.Add(1)
	go func() {
		for {
			select {
			case <-exit:
				utils.CloseProducer(kafkaProducer)
				wg.Done()
				return
			case action := <-keyAction:
				sendMessage(kafkaProducer, topic, action, player, gameId)
			}
		}
	}()
	return nil
}

func sendMessage(producer *kafka.Producer, topic string, action KeyAction, playerId string, gameId int32) {
	key := fmt.Sprintf("%d*%s", gameId, playerId)

	message := &game_state.Action{
		PlayerId: playerId, GameId: gameId, ActType: action.Type, Direction: action.Direction,
	}
	value, jsonErr := json.Marshal(message)
	if jsonErr != nil {
		utils.LogProducerFailedToSerialize(jsonErr)
		return
	}

	err := producer.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Value:          []byte(value),
		Key:            []byte(key),
	}, nil)
	if err != nil {
		utils.LogProducerFailedToQueueMessage(err)
		return
	}

	e := <-producer.Events() // blocking call: we get the result of sending the message in the events channel
	m := e.(*kafka.Message)
	if m.TopicPartition.Error != nil {
		utils.LogProducerDeliveryFailed(m)
	} else {
		utils.LogProducerDeliverySuccessful(m, value)
	}
}
