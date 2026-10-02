package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChannelEntity::class,
        ProviderEntity::class,
        FilterRuleEntity::class,
        CustomGroupEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ZaynDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun providerDao(): ProviderDao
    abstract fun filterRuleDao(): FilterRuleDao
    abstract fun customGroupDao(): CustomGroupDao

    companion object {
        @Volatile
        private var INSTANCE: ZaynDatabase? = null

        fun getInstance(context: Context): ZaynDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZaynDatabase::class.java,
                    "zayntv_master.db"
                )
                .fallbackToDestructiveMigration(false)
                .build().also { INSTANCE = it }
            }
        }
    }
}
