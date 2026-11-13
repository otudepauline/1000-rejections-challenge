package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AskDao {
    @Query("SELECT * FROM asks ORDER BY dateEpochMillis DESC, id DESC")
    fun getAllAsks(): Flow<List<AskEntity>>

    @Query("SELECT * FROM asks WHERE id = :id")
    fun getAskById(id: Long): Flow<AskEntity?>

    @Query("SELECT * FROM asks WHERE id = :id")
    suspend fun getAskByIdOnce(id: Long): AskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsk(ask: AskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asks: List<AskEntity>)

    @Update
    suspend fun updateAsk(ask: AskEntity)

    @Delete
    suspend fun deleteAsk(ask: AskEntity)

    @Query("DELETE FROM asks WHERE id = :id")
    suspend fun deleteAskById(id: Long)

    @Query("DELETE FROM asks")
    suspend fun deleteAll()
}
