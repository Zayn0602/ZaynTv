package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY sortOrder ASC, channelNumber ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId ORDER BY sortOrder ASC, channelNumber ASC")
    fun getChannelsByProvider(providerId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId")
    suspend fun getChannelsByProviderOnce(providerId: String): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Query("UPDATE channels SET isFavorite = :isFav WHERE id = :channelId")
    suspend fun updateFavorite(channelId: String, isFav: Boolean)

    @Query("UPDATE channels SET customNumber = :customNumber WHERE id = :channelId")
    suspend fun updateCustomNumber(channelId: String, customNumber: Int?)

    @Query("UPDATE channels SET customName = :customName WHERE id = :channelId")
    suspend fun updateCustomName(channelId: String, customName: String?)

    @Query("UPDATE channels SET isCustomHidden = :hidden WHERE id = :channelId")
    suspend fun updateHidden(channelId: String, hidden: Boolean)

    @Query("UPDATE channels SET isParentalLocked = :locked WHERE id = :channelId")
    suspend fun updateParentalLocked(channelId: String, locked: Boolean)

    @Query("UPDATE channels SET customGroup = :groupName WHERE id = :channelId")
    suspend fun updateCustomGroup(channelId: String, groupName: String?)

    @Query("UPDATE channels SET sortOrder = :order WHERE id = :channelId")
    suspend fun updateSortOrder(channelId: String, order: Int)

    @Query("UPDATE channels SET customName = NULL, customNumber = NULL, customGroup = NULL, isCustomHidden = 0 WHERE providerId = :providerId")
    suspend fun restoreOriginalProviderList(providerId: String)

    @Query("UPDATE channels SET isCustomHidden = 0")
    suspend fun unhideAllChannels()

    @Query("UPDATE channels SET customName = NULL, customNumber = NULL, customGroup = NULL")
    suspend fun resetAllCustomizations()

    @Query("DELETE FROM channels WHERE providerId = :providerId")
    suspend fun deleteChannelsByProvider(providerId: String)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY lastUpdated DESC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: String): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Query("DELETE FROM providers WHERE id = :providerId")
    suspend fun deleteProviderById(providerId: String)
}

@Dao
interface FilterRuleDao {
    @Query("SELECT * FROM filter_rules")
    fun getAllRules(): Flow<List<FilterRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: FilterRuleEntity)

    @Query("DELETE FROM filter_rules WHERE id = :id")
    suspend fun deleteRuleById(id: String)

    @Query("DELETE FROM filter_rules")
    suspend fun clearRules()
}

@Dao
interface CustomGroupDao {
    @Query("SELECT * FROM custom_groups ORDER BY orderIndex ASC")
    fun getAllCustomGroups(): Flow<List<CustomGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: CustomGroupEntity)

    @Query("DELETE FROM custom_groups WHERE id = :id")
    suspend fun deleteGroupById(id: String)
}
