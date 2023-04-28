package online.bukabuku.v3planeannouncer.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Planes)
    @Query("DELETE FROM planes")
    suspend fun deleteAll()

    @Query("SELECT * FROM planes")
    fun getAll(): Flow<List<Planes>>
    //@Query("SELECT * FROM planes WHERE airline_icao = :airline ORDER BY id")
    //@Query("SELECT * FROM planes WHERE airline_icao = :airline")
    //fun getByAirline(airline: String): Flow<List<Planes>>
    /*@Query("SELECT * FROM planes WHERE status = 'en-route'")
    fun getActive(): Flow<List<Planes>>*/
}
