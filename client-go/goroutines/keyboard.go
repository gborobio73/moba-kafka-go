package goroutines

import (
	"client/terminal"
	"fmt"
	"sync"

	kb "github.com/eiannone/keyboard"
)

type KeyAction struct {
	Type      string
	Direction string
}

func StartKeyboard(keyChannel chan<- KeyAction, wg *sync.WaitGroup, exitChannels ...chan bool) error {
	keysEvents, err := kb.GetKeys(10)
	if err != nil {
		return fmt.Errorf("Error initializing keybard lib: %s\n", err)
	}

	writeKeyboardLog("Press ESC, Q or ^C to quit")
	wg.Add(1)
	go func() {
		defer func() {
			_ = kb.Close()
		}()
		for {
			event := <-keysEvents
			if event.Err != nil {
				writeKeyboardLog(fmt.Sprintf("error reading key: %s", err))
			}

			if event.Key == kb.KeyEsc || event.Key == kb.KeyCtrlC || event.Rune == rune('q') {
				for i, channel := range exitChannels {
					select {
					case channel <- true:
					default:
						writeKeyboardLog(fmt.Sprintf("Unable to send message to channel %d", i))
					}
				}
				wg.Done()
				break
			}
			if event.Rune == rune('w') {
				sendMessageToChannel(keyChannel, KeyAction{"gre", "up"})
			} else if event.Rune == rune('s') {
				sendMessageToChannel(keyChannel, KeyAction{"gre", "down"})
			} else if event.Rune == rune('a') {
				sendMessageToChannel(keyChannel, KeyAction{"gre", "left"})
			} else if event.Rune == rune('d') {
				sendMessageToChannel(keyChannel, KeyAction{"gre", "right"})
			} else if event.Key == kb.KeyArrowUp {
				sendMessageToChannel(keyChannel, KeyAction{"mov", "up"})
			} else if event.Key == kb.KeyArrowDown {
				sendMessageToChannel(keyChannel, KeyAction{"mov", "down"})
			} else if event.Key == kb.KeyArrowLeft {
				sendMessageToChannel(keyChannel, KeyAction{"mov", "left"})
			} else if event.Key == kb.KeyArrowRight {
				sendMessageToChannel(keyChannel, KeyAction{"mov", "right"})
			} else {
				continue
			}
		}
	}()
	return nil
}

func sendMessageToChannel(keyChannel chan<- KeyAction, keyAction KeyAction) {
	select {
	case keyChannel <- keyAction:
	default:
		writeKeyboardLog(fmt.Sprintf("Unable to send message %q", keyAction))
	}
}

func writeKeyboardLog(message string) {
	pos := terminal.MoveCursorToPosition(17, 0)
	fmt.Printf("%s%s%s>> [keyboard] %s\r%s", terminal.SaveCursorPosition, pos, terminal.ClearLine, message, terminal.RestoreCursorPosition)
}
