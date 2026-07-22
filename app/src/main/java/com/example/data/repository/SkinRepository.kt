package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.database.*
import com.example.data.remote.AiSkinAnalysisResult
import com.example.data.remote.GeminiSkinService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SkinRepository(private val skinDao: SkinDao) {

    val allScanRecords: Flow<List<SkinScanRecord>> = skinDao.getAllScanRecords()
    val latestScanRecord: Flow<SkinScanRecord?> = skinDao.getLatestScanRecord()
    val favoriteProductIds: Flow<List<String>> = skinDao.getFavoriteProductIds()

    // Sample Curated Skincare Products List
    private val sampleProducts = listOf(
        Product(
            id = "p1",
            name = "积雪草屏障特安舒缓精华液",
            brand = "修丽可 / SkinCeuticals",
            category = "精华",
            suitableSkinTypes = listOf("敏感肌", "混合偏干型", "干性敏感强受损肌", "敏感性油痘肌"),
            targetConcerns = listOf("舒缓修复", "屏障受损", "局部泛红"),
            matchScore = 98,
            price = "¥380 / 30ml",
            keyIngredients = listOf("95%高纯积雪草苷", "神经酰胺NP", "依克多因"),
            highlights = "快速褪红舒缓，重塑皮脂膜天然屏障，无酒精香精防腐剂",
            usageTips = "早晚洁面后使用，取2-3滴轻拍于面部及红肿区域",
            rating = 4.9f
        ),
        Product(
            id = "p2",
            name = "透明质酸多重深度补水霜",
            brand = "理肤泉 / La Roche-Posay",
            category = "面霜",
            suitableSkinTypes = listOf("干性缺水肌", "混合偏干型", "中性耐受肌"),
            targetConcerns = listOf("深度补水", "换季紧绷", "干纹细纹"),
            matchScore = 95,
            price = "¥260 / 50ml",
            keyIngredients = listOf("微分子玻尿酸", "温泉水因子", "角鲨烷"),
            highlights = "长效48小时锁水，啫喱丝滑质地，清爽不粘腻",
            usageTips = "精华后涂抹，双手掌心揉开后按压面部吸收",
            rating = 4.8f
        ),
        Product(
            id = "p3",
            name = "辛酰水杨酸清痘细致调理理肤水",
            brand = "理肤泉 K+ / La Roche-Posay",
            category = "爽肤水",
            suitableSkinTypes = listOf("油性易痘肌", "敏感性油痘肌", "混合偏油"),
            targetConcerns = listOf("控油祛痘", "毛孔粗大", "闭口粉刺"),
            matchScore = 92,
            price = "¥220 / 200ml",
            keyIngredients = listOf("LHA辛酰水杨酸", "吡啶酮乙醇胺盐", "温泉水"),
            highlights = "温和疏通毛孔，抑制油脂过量分泌，调理黑头闭口",
            usageTips = "用棉片轻轻擦拭T区及易出油部位，避开眼周",
            rating = 4.7f
        ),
        Product(
            id = "p4",
            name = "光感光玥胶原蛋白弹润紧致眼霜",
            brand = "雅诗兰黛 / Estee Lauder",
            category = "眼霜",
            suitableSkinTypes = listOf("所有肤质", "混合偏干型", "干性缺水肌"),
            targetConcerns = listOf("抗老紧致", "眼周细纹", "黑眼圈"),
            matchScore = 94,
            price = "¥560 / 15ml",
            keyIngredients = listOf("三胜肽复合因子", "咖啡因", "二裂酵母"),
            highlights = "淡化眼周细纹与干纹，紧致提拉，击退熬夜暗沉",
            usageTips = "无名指取黄豆大小，点按眼周骨骼处顺时针按摩",
            rating = 4.9f
        ),
        Product(
            id = "p5",
            name = "纯物理温和高倍防晒乳 SPF50+ PA++++",
            brand = "安热沙 / Anessa",
            category = "防晒",
            suitableSkinTypes = listOf("敏感肌", "干性敏感强受损肌", "混合偏干型"),
            targetConcerns = listOf("紫外线防护", "屏障受损", "美白淡斑"),
            matchScore = 97,
            price = "¥210 / 60ml",
            keyIngredients = listOf("二氧化钛", "氧化锌", "甘草酸二钾"),
            highlights = "纯物理防晒配方，无刺激无紫外线吸收剂，敏感肌专用",
            usageTips = "出门前15分钟均匀涂抹一元硬币大小于全脸",
            rating = 4.8f
        ),
        Product(
            id = "p6",
            name = "烟酰胺光感亮白透亮淡斑精华液",
            brand = "OLAY / 欧莱雅",
            category = "精华",
            suitableSkinTypes = listOf("中性耐受肌", "混合偏干型", "油性易痘肌"),
            targetConcerns = listOf("美白淡斑", "暗沉无光", "痘印修复"),
            matchScore = 91,
            price = "¥320 / 30ml",
            keyIngredients = listOf("5%高纯烟酰胺", "酰本胺", "革糖素"),
            highlights = "阻断黑色素转运，提亮面部气色，淡化红黑痘印",
            usageTips = "夜间使用效果更佳，避开皮肤破损炎症区域",
            rating = 4.7f
        )
    )

    fun getRecommendedProducts(skinType: String?): Flow<List<Product>> {
        return favoriteProductIds.map { favoriteIds ->
            sampleProducts.map { product ->
                val isFav = favoriteIds.contains(product.id)
                val matchesSkin = skinType == null || product.suitableSkinTypes.any { skinType.contains(it) || it.contains(skinType) }
                val updatedScore = if (matchesSkin) product.matchScore else (product.matchScore - 12).coerceAtLeast(70)
                product.copy(isFavorite = isFav, matchScore = updatedScore)
            }.sortedByDescending { it.matchScore }
        }
    }

    suspend fun toggleFavoriteProduct(productId: String, isFavorite: Boolean) {
        if (isFavorite) {
            skinDao.addFavoriteProduct(FavoriteProduct(productId))
        } else {
            skinDao.removeFavoriteProduct(productId)
        }
    }

    suspend fun runSkinDetection(
        photoBitmap: Bitmap?,
        quizSummary: String?
    ): AiSkinAnalysisResult {
        val result = GeminiSkinService.analyzeSkinWithAi(photoBitmap, quizSummary)

        // Save scan record to Room
        val record = SkinScanRecord(
            skinType = result.skinType,
            overallScore = result.overallScore,
            moistureLevel = result.moistureLevel,
            oilLevel = result.oilLevel,
            sensitivityScore = result.sensitivityScore,
            acneScore = result.acneScore,
            darkSpotScore = result.darkSpotScore,
            barrierHealth = result.barrierHealth,
            primaryConcerns = result.primaryConcerns.joinToString(","),
            aiAnalysisSummary = result.analysisSummary,
            recommendedIngredients = result.recommendedIngredients.joinToString(","),
            avoidedIngredients = result.avoidedIngredients.joinToString(",")
        )
        skinDao.insertScanRecord(record)

        return result
    }

    suspend fun consultAiAdvisor(userMessage: String, skinType: String?): String {
        return GeminiSkinService.consultAiAdvisor(userMessage, skinType)
    }

    fun getDailyLog(dateIso: String = getTodayIso()): Flow<DailySkincareLog?> {
        return skinDao.getDailyLog(dateIso)
    }

    suspend fun saveDailyLog(log: DailySkincareLog) {
        skinDao.saveDailyLog(log)
    }

    private fun getTodayIso(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
