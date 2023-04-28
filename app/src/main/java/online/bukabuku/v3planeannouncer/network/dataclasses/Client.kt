package online.bukabuku.v3planeannouncer.network.dataclasses

data class Client(
    val agent: Agent,
    val connection: Connection,
    val device: Device,
    val geo: Geo,
    val ip: String,
    val karma: Karma
)