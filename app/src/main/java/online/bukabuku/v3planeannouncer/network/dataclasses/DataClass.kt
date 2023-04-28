package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Json

data class DataClass(
    val request: Request,
    val response: List<Response>,
    val terms: String
)