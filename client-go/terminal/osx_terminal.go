package terminal

import "fmt"

const SaveCursorPosition = "\033[s"
const RestoreCursorPosition = "\033[u"
const ClearLineAndRestoreCursor = "\033[u\033[K"
const ClearLine = "\033[2K"
const ClearConsole = "\033[H\033[2J"
const ClearFromCursor = "\033[J"
const HideCursor = "\033[?25l"
const ShowCursor = "\033[?25h"

func MoveCursorToPosition(row int, column int) string {
	return fmt.Sprintf("\033[%d;%dH", row, column)
}
