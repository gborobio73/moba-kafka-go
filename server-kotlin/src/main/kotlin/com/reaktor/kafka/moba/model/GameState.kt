package com.reaktor.kafka.moba.model

import com.google.gson.Gson

data class GameState(
    var gameId: Int = 0,
    var players: List<Player> = arrayListOf(),
    var walls: List<Wall> = arrayListOf()
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

data class Player(
    var id: String = "",
    var x: Int = 0,
    var y: Int = 0,
    var gre: Int = 0,
    var health: Boolean = true) {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}

data class Wall(
    var x: Int = 0,
    var y: Int = 0,
    var health: Boolean = true) {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}

fun initialGameState(gameId: Int = 0): GameState {

    val players = listOf(
        Player("A1", 1, 9, 5 ),
        Player("A2", 2, 8, 5),
        Player("A3", 4, 9, 5 ),
        Player("A4", 6, 8, 5 ),
        Player("A5", 9, 9, 5 ),
        Player("B1",  0, 0, 5 ),
        Player("B2", 3, 1,  5  ),
        Player("B3", 5, 0, 5 ),
        Player("B4", 7,  1,  5),
        Player("B5", 8,  0,  5)
    )
    val walls = listOf(
        Wall( 2, 7),
        Wall( 3, 7),
        Wall( 7, 7),
        Wall( 8, 7),
        Wall( 0, 5),
        Wall( 1, 5),
        Wall( 8, 4),
        Wall( 9, 4),
        Wall( 1, 2),
        Wall( 2, 2),
        Wall( 6, 2),
        Wall( 7, 2)

    )
    return GameState(gameId, players, walls)
}
