package com.focuson.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.focuson.app.data.db.entity.BlockRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockRuleDao {
    @Query("SELECT * FROM block_rule WHERE modeId = :modeId")
    fun observeByMode(modeId: String): Flow<List<BlockRuleEntity>>

    @Query("SELECT * FROM block_rule WHERE modeId = :modeId")
    suspend fun findByMode(modeId: String): List<BlockRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rules: List<BlockRuleEntity>)

    @Query("DELETE FROM block_rule WHERE modeId = :modeId AND kind = :kind")
    suspend fun deleteByModeAndKind(modeId: String, kind: String)

    @Query("DELETE FROM block_rule WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun replaceByModeAndKind(modeId: String, kind: String, rules: List<BlockRuleEntity>) {
        deleteByModeAndKind(modeId, kind)
        if (rules.isNotEmpty()) upsertAll(rules)
    }
}
