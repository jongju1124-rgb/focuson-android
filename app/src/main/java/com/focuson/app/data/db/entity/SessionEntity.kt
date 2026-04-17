package com.focuson.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_history")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: String,
    val startEpochMs: Long,
    val endEpochMs: Long,
    val actualEndEpochMs: Long? = null,
    val strict: Boolean,
    val completed: Boolean = false,
)
