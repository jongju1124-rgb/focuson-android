package com.focuson.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.focuson.app.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session_history ORDER BY startEpochMs DESC LIMIT 50")
    fun observeRecent(): Flow<List<SessionEntity>>

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM session_history WHERE id = :id")
    suspend fun findById(id: Long): SessionEntity?
}
