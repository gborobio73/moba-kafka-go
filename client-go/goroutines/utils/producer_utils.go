package utils

import (
	"client/terminal"
	"fmt"

	"github.com/confluentinc/confluent-kafka-go/kafka"
)

func CloseProducer(kafkaProducer *kafka.Producer) {
	LogProducer("Closing producer")
	kafkaProducer.Close()
	LogProducer("Closed")
}

func LogProducer(message string) {
	pos := terminal.MoveCursorToPosition(20, 0)
	fmt.Printf("%s%s%s>> [producer] %s\r%s", terminal.SaveCursorPosition, pos, terminal.ClearFromCursor, message, terminal.RestoreCursorPosition)
}

func LogProducerFailedToSerialize(jsonErr error) {
	LogProducer(fmt.Sprintf("Failed to serialize to json: %s\n", jsonErr))
}

func LogProducerFailedToQueueMessage(err error) {
	LogProducer(fmt.Sprintf("Failed to queue message: %s\n", err))
}

func LogProducerDeliveryFailed(m *kafka.Message) {
	LogProducer(fmt.Sprintf("Delivery failed: %v\n", m.TopicPartition.Error))
}

func LogProducerDeliverySuccessful(m *kafka.Message, value []byte) {
	LogProducer(fmt.Sprintf("Delivered message to %s [%d] at offset %v: '%s'\n",
		*m.TopicPartition.Topic, m.TopicPartition.Partition, m.TopicPartition.Offset, value))
}

func LogProducerCreated(kafkaProducer *kafka.Producer) {
	LogProducer(fmt.Sprintf("Created Producer %v", kafkaProducer))
}
