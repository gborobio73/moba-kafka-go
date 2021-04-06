package game_state

type Action struct {
	PlayerId  string `json:"playerId"`
	GameId    int32  `json:"gameId"`
	ActType   string `json:"actType"`
	Direction string `json:"direction"`
}

func NewAction(playerId string, gameId int32, actType string, direction string) *Action {
	return &Action{playerId, gameId, actType, direction}
}
