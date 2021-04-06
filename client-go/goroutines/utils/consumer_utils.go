package utils

import (
	"client/terminal"
	"fmt"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func CreateNewConsumer(broker string, gameId int32) (*kafka.Consumer, error) {
	return kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":     broker,
		"broker.address.family": "v4",
		"group.id":              fmt.Sprintf("group-game-%d", gameId),
		"session.timeout.ms":    6000,
		"auto.offset.reset":     "earliest",
	})
}

func CloseConsumer(c *kafka.Consumer) {
	LogConsumer("Closing consumer", 18)
	c.Close()
	LogConsumer("Closed", 18)
}

func LogConsumer(message string, pos int) {
	posCtr := fmt.Sprintf("\033[%d;%dH", pos, 0)
	fmt.Printf("%s%s%s>> [consumer] %s\n%s", terminal.SaveCursorPosition, posCtr, terminal.ClearLine, message, terminal.RestoreCursorPosition)
}

func LogConsumerReceivedMessage(topic string, e *kafka.Message) {
	LogConsumer(fmt.Sprintf("Received message from %s[%d] from offset %d", topic, e.TopicPartition.Partition, e.TopicPartition.Offset), 19)
}

func LogConsumerUnableToParseJson(jsonErr error) {
	LogConsumer(fmt.Sprintf("Unable to parse json: %s", jsonErr), 19)
}

func LogConsumerUnableToSendMessageToScreen(gameId int32) {
	LogConsumer(fmt.Sprintf("Unable to send message to screen channel: game %d", gameId), 19)
}

func LogConsumerIgnoringGame(gameId int32) {
	LogConsumer(fmt.Sprintf("Ignoring state for game %d", gameId), 19)
}

func LogConsumerKafkaError(e kafka.Error) {
	LogConsumer(fmt.Sprintf("%% kafka error: %v: %v", e.Code(), e), 19)
}

func LogConsumerIgnoringMessage(e kafka.Event) {
	var message string
	if len(e.String()) >= 50 {
		message = e.String()[0:50]
	} else {
		message = e.String()
	}
	LogConsumer(fmt.Sprintf("Ignored %v", message), 19)
}

func LogConsumerConsumingFromTopic(topic string) {
	LogConsumer(fmt.Sprintf("Consuming from %s", topic), 18)
}

func LogConsumerConsumingFromTopicAndPartition(topic string, partitionNumber int32) {
	LogConsumer(fmt.Sprintf("Consuming from %s[%d]", topic, partitionNumber), 18)
}
