package com.reaktor.kafka.moba.ai

import com.reaktor.kafka.moba.model.*

fun computeGrenadeThrowAndAddDelta(gameId: Int, action: Action, state: GameState): GameStateWithDelta {
    val current = getCurrentPlayer(action, state) ?: return GameStateWithDelta(gameId, null, state)

    if(current.gre == 0 || !current.health) {
        println(">>>> grenade:${current.id} does not have more grenades or does not have health")
        return GameStateWithDelta(gameId, null, state)
    }
    val (landingX, landingY) = getNewCoordinates(action, current)
    println(">>>> grenade:${current.id} throws grenade to x $landingX, y $landingY")

    // update grenades count for current player
    val player = Player(current.id, current.x, current.y, current.gre -1 )
    val players = state.players
        .map { pl ->
            when (pl.id) {
                current.id -> player
                else -> pl
            }
        }

    // find if the grenade hit a player
    val deadPlayerId = state.players.find { pl -> pl.x == landingX && pl.y == landingY }?.id
    if (deadPlayerId != null) {
        val deadPlayer = Player(deadPlayerId, landingX, landingY, 0, false)
        val newPlayers = players
            .map { pl ->
                when (pl.id) {
                    deadPlayerId -> deadPlayer
                    else -> pl
                }
            }
        val delta = Delta(arrayListOf(player, deadPlayer), null)
        return GameStateWithDelta(
            gameId,
            delta,
            GameState(gameId, newPlayers, state.walls)
        )
    }

    // find if the grenade hit a wall
    if (state.walls.find { wll -> wll.x == landingX && wll.y == landingY } != null) {
        val newWall = Wall(landingX, landingY, false)
        val walls = state.walls
            .map { wll ->
                when {
                    wll.x == landingX && wll.y == landingY -> newWall
                    else -> wll
                }
        }
        return GameStateWithDelta(
            gameId,
            Delta(listOf(player), newWall),
            GameState(gameId, players, walls))
    }
    return GameStateWithDelta(
        gameId,
        Delta(listOf(player), null),
        GameState(gameId, players, state.walls)
    )
}

fun computeGrenadeThrow(gameId: Int, action: Action, state: GameState): GameState {
    val current = getCurrentPlayer(action, state) ?: return GameState(gameId, state.players, state.walls)

    if(current.gre == 0 || !current.health) {
        println(">>>> grenade:${current.id} does not have more grenades or does not have health")
        return GameState(gameId, state.players, state.walls)
    }
    val (landingX, landingY) = getNewCoordinates(action, current)
    println(">>>> grenade:${current.id} throws grenade to x $landingX, y $landingY")

    // update grenades count for current player
    val player = Player(current.id, current.x, current.y, current.gre -1 )
    val players = state.players
        .map { pl ->
            when (pl.id) {
                current.id -> player
                else -> pl
            }
        }

    // find if the grenade hit a player
    val deadPlayerId = state.players.find { pl -> pl.x == landingX && pl.y == landingY }?.id
    if (deadPlayerId != null) {
        val deadPlayer = Player(deadPlayerId, landingX, landingY, 0, false)
        val newPlayers = players
            .map { pl ->
                when (pl.id) {
                    deadPlayerId -> deadPlayer
                    else -> pl
                }
            }
        return GameState(gameId, newPlayers, state.walls)
    }

    // find if the grenade hit a wall
    if (state.walls.find { wll -> wll.x == landingX && wll.y == landingY } != null) {
        val newWall = Wall(landingX, landingY, false)
        val walls = state.walls
            .map { wll ->
                when {
                    wll.x == landingX && wll.y == landingY -> newWall
                    else -> wll
                }
            }
        return GameState(gameId, players, walls)
    }
    return GameState(gameId, players, state.walls)
}

private fun getCurrentPlayer(action: Action, state: GameState): Player? {
    return state.players.find { player -> player.id == action.playerId }
}
