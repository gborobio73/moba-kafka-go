package game_state

type GameState struct {
	GameId  int32    `json:"gameId"`
	Players []Player `json:"players"`
	Walls   []Wall   `json:"walls"`
}

type Player struct {
	Id     string `json:"id"`
	X      int    `json:"x"`
	Y      int    `json:"y"`
	Gre    int    `json:"gre"`
	Health bool   `json:"health"`
}

type Wall struct {
	X      int  `json:"x"`
	Y      int  `json:"y"`
	Health bool `json:"health"`
}
