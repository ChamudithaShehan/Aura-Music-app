package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PinnedItemEntity
import com.example.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 15")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearch(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}

@Dao
interface PinnedItemDao {
    @Query("SELECT * FROM pinned_items")
    fun getAllPinnedItems(): Flow<List<PinnedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinItem(item: PinnedItemEntity)

    @Query("DELETE FROM pinned_items WHERE id = :id")
    suspend fun unpinItem(id: String)
}
