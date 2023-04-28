package online.bukabuku.v3planeannouncer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [(Planes::class)], version = 1)
abstract class AppsDataBase: RoomDatabase() {

    abstract fun planesDao(): PlanesDao

    companion object {

        fun getDatabase(context: Context): AppsDataBase {
            return Room.databaseBuilder(
                context,
                AppsDataBase::class.java,
                "planes_database"
            ).build()
        }
    }
}