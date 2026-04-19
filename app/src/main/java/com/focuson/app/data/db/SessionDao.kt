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

    // ── 통계용 쿼리 ───────────────────────────────────────

    /** [sinceMs] 이후 시작된 세션 전체 */
    @Query("SELECT * FROM session_history WHERE startEpochMs >= :sinceMs ORDER BY startEpochMs ASC")
    suspend fun findSince(sinceMs: Long): List<SessionEntity>

    /** 전체 세션 (Pro용) */
    @Query("SELECT * FROM session_history ORDER BY startEpochMs ASC")
    suspend fun findAll(): List<SessionEntity>

    /** [sinceMs] 이후 시작된 세션의 총 (실제 또는 예정) 집중 시간 ms */
    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN actualEndEpochMs IS NOT NULL THEN actualEndEpochMs - startEpochMs
                ELSE endEpochMs - startEpochMs
            END
        ), 0) FROM session_history
        WHERE startEpochMs >= :sinceMs
        """
    )
    suspend fun totalFocusedMsSince(sinceMs: Long): Long

    /** [sinceMs] 이후 완주한 세션 수 */
    @Query("SELECT COUNT(*) FROM session_history WHERE startEpochMs >= :sinceMs AND completed = 1")
    suspend fun completedCountSince(sinceMs: Long): Int

    /** [sinceMs] 이후 시작된 세션 수 (완주/중단 모두) */
    @Query("SELECT COUNT(*) FROM session_history WHERE startEpochMs >= :sinceMs")
    suspend fun totalCountSince(sinceMs: Long): Int
}
