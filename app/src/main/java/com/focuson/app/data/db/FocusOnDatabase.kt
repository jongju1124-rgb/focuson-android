package com.focuson.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.focuson.app.data.db.entity.BlockRuleEntity
import com.focuson.app.data.db.entity.SessionEntity

@Database(
    entities = [BlockRuleEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FocusOnDatabase : RoomDatabase() {
    abstract fun blockRuleDao(): BlockRuleDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var instance: FocusOnDatabase? = null

        fun get(context: Context): FocusOnDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusOnDatabase::class.java,
                    "focuson.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
