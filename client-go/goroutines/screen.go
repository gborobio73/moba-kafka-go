package goroutines

import (
	"client/game_state"
	"client/terminal"
	"fmt"
	"strings"
	"sync"
)

func StartScreenRenderer(channel <-chan game_state.GameState, gameId int32, player string, exit chan bool, wg *sync.WaitGroup) {
	wg.Add(1)
	pos := terminal.MoveCursorToPosition(0, 0)
	fmt.Printf("%s%s\n   *** Game %d, Player %s ***", terminal.HideCursor, pos, gameId, player)
	writeGameState(createStartingLayout())

	go func() {
		for {
			select {
			case <-exit:
				wg.Done()
				return
			case state := <-channel:
				layout := createEmptyLayout()
				for _, player := range state.Players {
					y, x := (player.Y-9)*-1, player.X
					if !player.Health && len(strings.Trim(layout[y][x], " ")) == 0 {
						layout[y][x] = " :/ "
					} else if player.Health {
						layout[y][x] = fmt.Sprintf("%s-%d", player.Id, player.Gre)
					}
				}
				for _, wall := range state.Walls {
					if wall.Health {
						layout[(wall.Y-9)*-1][wall.X] = "XXXX"
					}
				}
				writeGameState(layout)
			}
		}
	}()
}

func createEmptyLayout() [10][10]string {
	layout := [10][10]string{}
	for i := 0; i < 10; i++ {
		for j := 0; j < 10; j++ {
			layout[i][j] = "    "
		}
	}
	return layout
}

func writeGameState(layout [10][10]string) {
	var sb strings.Builder
	sb.WriteString("\n   +------------------------------------------------------------+")
	for i, row := range layout {
		sb.WriteString(fmt.Sprintf("\n%d  | %s  %s  %s  %s  %s  %s  %s  %s  %s  %s |", 9-i, row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9]))
	}
	sb.WriteString("\n   +------------------------------------------------------------+")
	sb.WriteString(fmt.Sprintf("\n      %d     %d     %d     %d     %d     %d     %d     %d     %d     %d", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
	pos := terminal.MoveCursorToPosition(2, 0)
	fmt.Printf("%s%s", pos, sb.String())
}

func createStartingLayout() [10][10]string {
	layout := createEmptyLayout()
	layout[0][1] = "A1-5"
	layout[1][2] = "A2-5"
	layout[0][4] = "A3-5"
	layout[1][6] = "A4-5"
	layout[0][9] = "A5-5"
	layout[9][0] = "B1-5"
	layout[8][3] = "B2-5"
	layout[9][5] = "B3-5"
	layout[8][7] = "B4-5"
	layout[9][8] = "B5-5"
	layout[2][2] = "XXXX"
	layout[2][3] = "XXXX"
	layout[2][7] = "XXXX"
	layout[2][8] = "XXXX"
	layout[4][0] = "XXXX"
	layout[4][1] = "XXXX"
	layout[5][8] = "XXXX"
	layout[5][9] = "XXXX"
	layout[7][1] = "XXXX"
	layout[7][2] = "XXXX"
	layout[7][6] = "XXXX"
	layout[7][7] = "XXXX"
	return layout
}
