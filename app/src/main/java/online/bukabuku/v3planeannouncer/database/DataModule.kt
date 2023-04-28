package online.bukabuku.v3planeannouncer.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppsDataBase =
        AppsDataBase.getDatabase(context)

    @Provides
    fun provideDao(database: AppsDataBase): PlanesDao {
        return database.planesDao()
    }
}
