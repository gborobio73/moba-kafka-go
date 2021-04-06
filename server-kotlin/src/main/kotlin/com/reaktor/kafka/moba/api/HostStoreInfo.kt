package com.reaktor.kafka.moba.api

data class HostStoreInfo(
    var host: String = "",
    var port: Int = 0,
    var storeNames: Set<String>? = null
){
    override fun toString(): String {
        return "HostStoreInfo{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", storeNames=" + storeNames +
                '}'
    }
}