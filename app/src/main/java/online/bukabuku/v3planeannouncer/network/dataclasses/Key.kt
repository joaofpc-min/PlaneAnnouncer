package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Json

data class Key(
    val api_key: String,
    val expired: String,
    val id: Int,
    val limits_by_hour: Int,
    val limits_by_minute: Int,
    val limits_by_month: Int,
    val limits_total: Int,
    val registered: String,
    val type: String
)