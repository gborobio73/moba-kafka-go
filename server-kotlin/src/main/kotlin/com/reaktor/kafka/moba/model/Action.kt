package com.reaktor.kafka.moba.model

import com.google.gson.Gson

data class Action(
    var playerId: String = "",
    var gameId: Int = 0,
    var actType: String = "",
    var direction: String = ""
) {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}

