package main

import (
	"fmt"
	"strconv"
	"testing"
)

func TestZombieAI(t *testing.T) {

	t.Run("get random player ", func(t *testing.T) {
		action := NewZombiAction(27)
		letter := string(action.PlayerId[0])
		number, _ := strconv.Atoi(string(action.PlayerId[1]))
		if letter != "A" && letter != "B" {
			t.Errorf("player should be either A or B; got %s", letter)
		}

		if number < 1 || number > 5 {
			t.Errorf("player number should be between 1 and 5; got %d (%s)", number, action.PlayerId)
		}
	})

	t.Run("get random action ", func(t *testing.T) {
		action := NewZombiAction(27).ActType

		if action != "mov" && action != "gre" {
			t.Errorf("action should be either 'mov' or 'gre' ; got %s", action)
		}
	})

	t.Run("get random direction ", func(t *testing.T) {
		direction := NewZombiAction(27).Direction
		fmt.Println(direction)
		if direction != "up" && direction != "down" && direction != "left" && direction != "right" {
			t.Errorf("direction should be 'up', 'right', 'down' or 'left' ; got %s", direction)
		}
	})

}
