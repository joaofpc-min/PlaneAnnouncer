package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Json

data class Request(
    val client: Client,
    val currency: String,
    val host: String,
    val id: String,
    val key: Key,
    val lang: String,
    val method: String,
    val params: Params,
    val pid: Int,
    val server: String,
    val time: Int,
    val version: Int
)