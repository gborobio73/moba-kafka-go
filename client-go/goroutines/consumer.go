package goroutines

import (
	"client/game_state"
	utils "client/goroutines/utils"
	"encoding/json"
	"fmt"
	"sync"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func StartKafkaConsumer(gameId int32, broker string, topic string, screenChannel chan game_state.GameState, exitChannel chan bool, wg *sync.WaitGroup) error {
	c, errConsumer := utils.CreateNewConsumer(broker, gameId)
	if errConsumer != nil {
		return fmt.Errorf("Failed to create consumer: %s\n", errConsumer)
	}

	// This is to subscribe to any partition
	// errSubs := c.Subscribe(topic, nil)
	// if errSubs != nil {
	// 	utils.CloseConsumer(c)
	// 	return fmt.Errorf("Failed to subscribe to topic '%s': %s\n", topic, errSubs)
	// }
	// utils.LogConsumerConsumingFromTopic(topic)

	metadata, metaErr := c.GetMetadata(&topic, false, 5000)
	if metaErr != nil {
		utils.CloseConsumer(c)
		return fmt.Errorf("Failed to get metadata from topic '%s': %s\n", topic, metaErr)
	}
	numPartitions := int32(len((*metadata).Topics[topic].Partitions))
	partitionNumber := gameId % numPartitions
	partition := kafka.TopicPartition{Topic: &topic, Partition: partitionNumber, Offset: kafka.OffsetEnd}
	utils.LogConsumerConsumingFromTopicAndPartition(topic, partitionNumber)
	errPart := c.Assign([]kafka.TopicPartition{partition})
	if errPart != nil {
		utils.CloseConsumer(c)
		return fmt.Errorf("Failed to assign partition %q\n: %s", partition, errPart)
	}

	wg.Add(1)
	go func() {
		for {
			select {
			case <-exitChannel:
				utils.CloseConsumer(c)
				wg.Done()
				return
			default:
				ev := c.Poll(100)
				if ev == nil {
					continue
				}
				consumeMessage(ev, screenChannel, gameId, topic)
			}
		}
	}()
	return nil
}

func consumeMessage(ev kafka.Event, channel chan game_state.GameState, gameId int32, topic string) {
	switch e := ev.(type) { // check what's the type of the event: https://tour.golang.org/methods/16
	case *kafka.Message:
		utils.LogConsumerReceivedMessage(topic, e)
		state := &game_state.GameState{Players: []game_state.Player{}, Walls: []game_state.Wall{}}
		jsonErr := json.Unmarshal(e.Value, &state)
		if jsonErr != nil {
			utils.LogConsumerUnableToParseJson(jsonErr)
			return
		}
		if state.GameId == gameId {
			select {
			case channel <- *state:
			default:
				utils.LogConsumerUnableToSendMessageToScreen(state.GameId)
			}
		} else {
			utils.LogConsumerIgnoringGame(state.GameId)
		}
	case kafka.Error:
		utils.LogConsumerKafkaError(e)
	default:
		utils.LogConsumerIgnoringMessage(e)
	}
}
