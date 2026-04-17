package com.focuson.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_rule")
data class BlockRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: String,                 // PresetMode.id
    val kind: String,                   // "app" | "site"
    val value: String,                  // 패키지명 또는 도메인 패턴
    val enabled: Boolean = true,
)
