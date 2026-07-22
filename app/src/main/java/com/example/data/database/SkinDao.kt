package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinDao {
    @Query("SELECT * FROM skin_scan_records ORDER BY timestamp DESC")
    fun getAllScanRecords(): Flow<List<SkinScanRecord>>

    @Query("SELECT * FROM skin_scan_records ORDER BY timestamp DESC LIMIT 1")
    fun getLatestScanRecord(): Flow<SkinScanRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanRecord(record: SkinScanRecord): Long

    @Query("DELETE FROM skin_scan_records WHERE id = :id")
    suspend fun deleteScanRecord(id: Long)

    // Favorites
    @Query("SELECT productId FROM favorite_products")
    fun getFavoriteProductIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteProduct(favorite: FavoriteProduct)

    @Query("DELETE FROM favorite_products WHERE productId = :productId")
    suspend fun removeFavoriteProduct(productId: String)

    // Daily Logs
    @Query("SELECT * FROM daily_skincare_logs WHERE dateIso = :dateIso LIMIT 1")
    fun getDailyLog(dateIso: String): Flow<DailySkincareLog?>

    @Query("SELECT * FROM daily_skincare_logs ORDER BY dateIso DESC LIMIT 30")
    fun getRecentDailyLogs(): Flow<List<DailySkincareLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyLog(log: DailySkincareLog)
}
