package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Json

data class Geo(
    //val city: String,
    val continent: String,
    val country: String,
    val country_code: String,
    val lat: Double,
    val lng: Double,
    //val timezone: String
)