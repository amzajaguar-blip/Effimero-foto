package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.BurnEntity
import com.example.data.model.MonumentBlockEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AshesDao {

    @Query("SELECT * FROM users WHERE id = 'local_priest' LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = 'local_priest' LIMIT 1")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Burns ---
    @Query("SELECT * FROM burns ORDER BY burnedAt DESC")
    fun getAllBurns(): Flow<List<BurnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBurn(burn: BurnEntity)

    @Query("SELECT * FROM burns WHERE id = :id LIMIT 1")
    suspend fun getBurnById(id: String): BurnEntity?

    @Update
    suspend fun updateBurn(burn: BurnEntity)

    // --- Monument Blocks ---
    @Query("SELECT * FROM monument_blocks ORDER BY positionIndex ASC")
    fun getMonumentBlocks(): Flow<List<MonumentBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: MonumentBlockEntity)

    @Query("DELETE FROM monument_blocks WHERE id = :id")
    suspend fun deleteBlockById(id: String)

    @Query("DELETE FROM monument_blocks")
    suspend fun clearAllBlocks()
}

@Database(
    entities = [UserEntity::class, BurnEntity::class, MonumentBlockEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AshesDatabase : RoomDatabase() {
    abstract val dao: AshesDao
}
