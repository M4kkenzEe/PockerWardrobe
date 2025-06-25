package com.ownstd.project.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

@Database(entities = [FeedEntity::class], version = 1)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun getDao(): FeedDao
}

@Entity
data class FeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String
)

@Dao
interface FeedDao {
    @Insert
    suspend fun insert(item: FeedEntity)

    @Query("DELETE FROM FeedEntity")
    suspend fun deleteAll()

    @Query("SELECT count(*) FROM FeedEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM FeedEntity WHERE id = :id")
    suspend fun getById(id: Long): FeedEntity

    @Query("SELECT * FROM FeedEntity")
    fun getAllAsFlow(): Flow<List<FeedEntity>>
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<FeedDatabase>
): FeedDatabase {

    return builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}