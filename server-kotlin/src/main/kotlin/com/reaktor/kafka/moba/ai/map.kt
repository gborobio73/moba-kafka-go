package com.reaktor.kafka.moba.ai

import com.reaktor.kafka.moba.model.Action
import com.reaktor.kafka.moba.model.Player

fun getNewCoordinates(v: Action, current: Player): Pair<Int, Int> {
    return when (v.direction.toLowerCase()) {
        "up" -> Pair(current.x, current.y + 1)
        "right" -> Pair(current.x + 1, current.y)
        "down" -> Pair(current.x, current.y -1)
        "left" -> Pair(current.x - 1 , current.y)
        else -> Pair(current.x, current.y)
    }
}
