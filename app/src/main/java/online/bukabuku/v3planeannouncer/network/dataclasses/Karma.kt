package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Json

data class Karma(
    val is_blocked: Boolean,
    val is_bot: Boolean,
    val is_crawler: Boolean,
    val is_friend: Boolean,
    val is_regular: Boolean
)