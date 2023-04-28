package online.bukabuku.v3planeannouncer.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Planes(
    /*@PrimaryKey(autoGenerate = true)
    val id: Int = 0,*/
    @PrimaryKey
    @ColumnInfo(name = "hex")
    val hex: String,
    @ColumnInfo(name = "aircraft_icao")
    val aircraft_icao: String?,
    /*@ColumnInfo(name = "aircraft_iata")
    val airline_iata: String,*/
    /*@ColumnInfo(name = "airline_icao")
    val airline_icao: String,*/
    @ColumnInfo(name = "alt")
    val alt: Int,
    @ColumnInfo(name = "arr_iata")
    val arr_iata: String?,
    /*@ColumnInfo(name = "arr_icao")
    val arr_icao: String,*/
    @ColumnInfo(name = "dep_iata")
    val dep_iata: String?,
    /*@ColumnInfo(name = "dep_icao")
    val dep_icao: String,*/
    /*@ColumnInfo(name = "dir")
    val dir: Int,
    @ColumnInfo(name = "flag")
    val flag: String,*/
    /*@ColumnInfo(name = "flight_iata")
    val flight_iata: String,
    @ColumnInfo(name = "flight_icao")
    val flight_icao: String,*/
    /*@ColumnInfo(name = "flight_number")
    val flight_number: String,*/
    @ColumnInfo(name = "lat")
    val lat: Double,
    @ColumnInfo(name = "lng")
    val lng: Double,
    /*@ColumnInfo(name = "reg_number")
    val reg_number: String,*/
    @ColumnInfo(name = "speed")
    val speed: Int,
    /*@ColumnInfo(name = "squawk")
    val squawk: String,*/
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "updated")
    val updated: Int,
    /*@ColumnInfo(name = "v_speed")
    val v_speed: Double*/
)