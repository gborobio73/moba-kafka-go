package com.reaktor.kafka.moba.model

import com.google.gson.Gson

data class GameStateWithDelta (
    var gameId: Int = 0,
    var delta : Delta? = null,
    var state: GameState = GameState()
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

data class Delta(var player: List<Player> = listOf(),
                 var wall: Wall? = null){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
