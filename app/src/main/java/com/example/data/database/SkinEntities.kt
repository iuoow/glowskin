package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skin_scan_records")
data class SkinScanRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val skinType: String,            // e.g., "混合偏干型", "敏感性油痘肌", "中性耐受肌"
    val overallScore: Int,           // 0-100
    val moistureLevel: Int,          // 0-100%
    val oilLevel: Int,               // 0-100%
    val sensitivityScore: Int,       // 0-100%
    val acneScore: Int,              // 0-100%
    val darkSpotScore: Int,          // 0-100%
    val barrierHealth: String,       // e.g., "健康良好", "轻度受损", "严重受损"
    val primaryConcerns: String,     // Comma separated list
    val aiAnalysisSummary: String,   // AI detailed explanation
    val recommendedIngredients: String, // Comma separated list
    val avoidedIngredients: String      // Comma separated list
)

@Entity(tableName = "favorite_products")
data class FavoriteProduct(
    @PrimaryKey val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_skincare_logs")
data class DailySkincareLog(
    @PrimaryKey val dateIso: String, // YYYY-MM-DD
    val morningCompleted: Boolean = false,
    val eveningCompleted: Boolean = false,
    val waterIntakeGlasses: Int = 0,
    val sleepHours: Float = 7.5f,
    val skinConditionNote: String = "",
    val moodTag: String = "开心"
)

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: String, // "洁面", "精华", "面霜", "防晒", "面膜", "眼霜"
    val suitableSkinTypes: List<String>,
    val targetConcerns: List<String>, // "控油祛痘", "舒缓修复", "深度补水", "美白淡斑", "抗老紧致"
    val matchScore: Int, // e.g. 98%
    val price: String,
    val keyIngredients: List<String>,
    val highlights: String,
    val usageTips: String,
    val rating: Float,
    val isFavorite: Boolean = false
)
