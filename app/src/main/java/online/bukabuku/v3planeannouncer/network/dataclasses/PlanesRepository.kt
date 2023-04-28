package online.bukabuku.v3planeannouncer.network.dataclasses

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import online.bukabuku.v3planeannouncer.database.Planes
import online.bukabuku.v3planeannouncer.database.PlanesDao
import javax.inject.Inject

class PlanesRepository @Inject constructor(private val planesDao: PlanesDao) {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertPlanes(mPlane: Planes) {
        withContext(Dispatchers.IO) {
            planesDao.insert(mPlane)
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun clearPlanes() {
        withContext(Dispatchers.IO) {
            planesDao.deleteAll()
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun allPlanes() : Flow<List<Planes>> {
        val ListPlane : Flow<List<Planes>>
        withContext(Dispatchers.IO) {
            ListPlane = planesDao.getAll()
        }
        return ListPlane
    }

}