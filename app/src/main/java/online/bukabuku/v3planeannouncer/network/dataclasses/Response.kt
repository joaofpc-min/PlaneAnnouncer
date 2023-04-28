package online.bukabuku.v3planeannouncer.network.dataclasses

import com.squareup.moshi.Json
import javax.annotation.Nullable

data class Response(
    val hex: String,
    //@field:Json(name = "reg_number") val reg_number: String,
    //@field:Json(name = "flag") val flag: String,
    val lat: Double,
    val lng: Double,
    val alt: Int,
    //@field:Json(name = "dir") val dir: Int,
    val speed: Int,
    //@field:Json(name = "v_speed") val v_speed: Double,
    //@field:Json(name = "squawk") val squawk: String,
    //@field:Json(name = "flight_number") val flight_number: String,
    //@field:Json(name = "flight_icao") val flight_icao: String,
    //@field:Json(name = "flight_iata") val flight_iata: String,
    //@field:Json(name = "dep_icao") val dep_icao: String,
     val dep_iata: String?,
    //@field:Json(name = "arr_icao") val arr_icao: String,
     val arr_iata: String?,
    //@field:Json(name = "airline_icao") val airline_icao: String,
    //@field:Json(name = "airline_iata") val airline_iata: String,
    val aircraft_icao: String,
    val updated: Int,
    val status: String
)