package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SkinScanRecord::class, FavoriteProduct::class, DailySkincareLog::class],
    version = 1,
    exportSchema = false
)
abstract class SkinDatabase : RoomDatabase() {
    abstract fun skinDao(): SkinDao

    companion object {
        @Volatile
        private var INSTANCE: SkinDatabase? = null

        fun getDatabase(context: Context): SkinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkinDatabase::class.java,
                    "glowskin_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
