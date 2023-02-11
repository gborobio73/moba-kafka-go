package com.reaktor.kafka.moba.ai

import com.reaktor.kafka.moba.model.*

fun computePlayerMoveAndAddDelta(gameId: Int, action: Action, state: GameState): GameStateWithDelta {
    if (playerCannotMove(action, state)) {
        return GameStateWithDelta(gameId, null, GameState(gameId, state.players, state.walls))
    }
    val current = getCurrentPlayer(action, state)
    val (x, y) = getNewCoordinates(action, current!!)
    val player = Player(current.id, x, y, current.gre)
    val players = state.players
        .map { pl ->
            if(pl.id == action.playerId) player
            else pl
        }
    return GameStateWithDelta(
        gameId,
        Delta(arrayListOf(player), null),
        GameState(gameId, players, state.walls)
    )
}

fun computePlayerMove(gameId: Int, action: Action, state: GameState): GameState {
    if (playerCannotMove(action, state)) {
        return GameState(gameId, state.players, state.walls)
    }
    val current = getCurrentPlayer(action, state)
    val (x, y) = getNewCoordinates(action, current!!)
    val player = Player(current.id, x, y, current.gre)
    val players = state.players
        .map { pl ->
            if(pl.id == action.playerId) player
            else pl
        }
    return GameState(gameId, players, state.walls)
}


private fun playerCannotMove(action: Action, state: GameState): Boolean {
    val player = getCurrentPlayer(action, state) ?: return true

    val (x, y) = getNewCoordinates(action, player)
    fun outsideLimits(): Boolean {
        return x !in 0..9 || y !in 0..9
    }
    fun thereIsSomethingThere(): Boolean {
        return (state.walls.find { wall -> wall.x == x && wall.y == y && wall.health } != null) ||
            (state.players.find { player -> player.x == x && player.y == y && player.health } != null)
    }
    fun isDead(): Boolean {
        return !player.health
    }
    return outsideLimits() || thereIsSomethingThere() || isDead()
}

private fun getCurrentPlayer(action: Action, state: GameState): Player? {
    return state.players.find { player -> player.id == action.playerId }
}