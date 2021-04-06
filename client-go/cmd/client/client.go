package main

import (
	"client/game_state"
	"client/goroutines"
	"client/terminal"
	"fmt"
	"os"
	"strconv"
	"sync"
)

func main() {

	fmt.Printf(terminal.ClearConsole)

	if len(os.Args) != 6 {
		fmt.Fprintf(os.Stderr, "Usage: %s <broker> <actions_topic> <games_topic> <game> <player>\n",
			os.Args[0])
		os.Exit(1)
	}

	broker, actionsTopic, gamesTopic, player := os.Args[1], os.Args[2], os.Args[3], os.Args[5]
	gameId, _ := strconv.Atoi(os.Args[4])

	// communication channels
	producerExitChannel, consumerExitChannel, screenExitChannel := make(chan bool, 10), make(chan bool, 10), make(chan bool, 10)
	screenChannel := make(chan game_state.GameState, 10)
	keyboardChannel := make(chan goroutines.KeyAction, 10)
	defer closeChannels(keyboardChannel, screenChannel, producerExitChannel, consumerExitChannel, screenExitChannel)

	var wg sync.WaitGroup

	// start keyboard
	keybardErr := goroutines.StartKeyboard(keyboardChannel, &wg, producerExitChannel, consumerExitChannel, screenExitChannel)
	if keybardErr != nil {
		fmt.Fprintf(os.Stderr, "Unexpected error when starting keyboard: %s\n", keybardErr)
		return
	}

	// kafka producer
	prodErr := goroutines.StartKafkaProducer(int32(gameId), player, broker, actionsTopic, keyboardChannel, producerExitChannel, &wg)
	if prodErr != nil {
		fmt.Fprintf(os.Stderr, "Unexpected error when starting producer: %s\n", prodErr)
		return
	}

	// screen renderer
	goroutines.StartScreenRenderer(screenChannel, int32(gameId), player, screenExitChannel, &wg)

	// kafka consumer
	consumerErr := goroutines.StartKafkaConsumer(int32(gameId), broker, gamesTopic, screenChannel, consumerExitChannel, &wg)
	if consumerErr != nil {
		fmt.Fprintf(os.Stderr, "Unexpected error when starting consumer: %s\n", consumerErr)
		return
	}

	// wait until all goroutines have exited
	wg.Wait() // blocking call
	pos := terminal.MoveCursorToPosition(23, 0)
	fmt.Printf("%s%sAll goroutines finished\n", terminal.ShowCursor, pos)
}

func closeChannels(keyboard chan goroutines.KeyAction, screen chan game_state.GameState, exitChannels ...chan bool) {
	close(keyboard)
	close(screen)
	for _, channel := range exitChannels {
		close(channel)
	}
	fmt.Println("All channels closed \nBye")
}
