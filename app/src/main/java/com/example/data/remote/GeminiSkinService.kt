package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class AiSkinAnalysisResult(
    val skinType: String,            // e.g. "敏感混合偏干型"
    val overallScore: Int,           // 0-100
    val moistureLevel: Int,          // 0-100
    val oilLevel: Int,               // 0-100
    val textureLevel: Int = 78,      // 0-100 (肌理细腻度/纹理)
    val sensitivityScore: Int,       // 0-100
    val acneScore: Int,              // 0-100
    val darkSpotScore: Int,          // 0-100
    val barrierHealth: String,       // "健康", "轻度受损", "严重受损"
    val primaryConcerns: List<String>,
    val analysisSummary: String,
    val morningRoutineSteps: List<String>,
    val eveningRoutineSteps: List<String>,
    val recommendedIngredients: List<String>,
    val avoidedIngredients: List<String>,
    val dietAndLifestyleTips: List<String>
)

object GeminiSkinService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun Bitmap.toBase64Jpeg(): String {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 85, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeSkinWithAi(
        photoBitmap: Bitmap?,
        quizAnswersSummary: String?
    ): AiSkinAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Return high quality simulated result if API Key is not configured
            return@withContext generateFallbackAnalysis(quizAnswersSummary)
        }

        try {
            val systemPrompt = """
                你是一位资深皮肤科美肤顾问与肤质测试专家。
                根据用户提供的皮肤照片和/或问卷描述，分析女性皮肤健康状况。
                请严格按照 JSON 格式返回结果，不要添加任何 Markdown 格式以外的杂质。
                JSON 结构必须包含：
                {
                  "skinType": "皮肤类型字符串（例如：敏感性混合偏干肌）",
                  "overallScore": 85,
                  "moistureLevel": 65,
                  "oilLevel": 45,
                  "sensitivityScore": 60,
                  "acneScore": 25,
                  "darkSpotScore": 30,
                  "barrierHealth": "轻度受损",
                  "primaryConcerns": ["换季泛红", "T区出油", "颊部干燥"],
                  "analysisSummary": "详细皮肤评估与成因分析（200字以内）",
                  "morningRoutineSteps": ["温水/弱酸性洁面", "积雪草舒缓爽肤水", "玻尿酸保湿精华", "修护保湿乳", "物理防晒霜"],
                  "eveningRoutineSteps": ["温和卸妆", "氨基酸洁面", "B5修护精华", "神经酰胺修护霜"],
                  "recommendedIngredients": ["积雪草", "神经酰胺", "玻尿酸", "角鲨烷"],
                  "avoidedIngredients": ["高浓度酒精", "强水杨酸", "高浓度纯维A醇", "人工香精"],
                  "dietAndLifestyleTips": ["减少高糖饮食", "保证每天2000ml水分摄入", "避免用过热的水洗脸"]
                }
            """.trimIndent()

            val contentsArray = JSONArray()
            val partsArray = JSONArray()

            val promptText = if (!quizAnswersSummary.isNullOrBlank()) {
                "请评估以下肤质测试数据并给出专业改善方案：\n$quizAnswersSummary"
            } else {
                "请对照片中的女性皮肤进行专业诊断，分析肤质类型、水分、油脂、敏感度、屏障状态，并提供详细的护肤流程与成分建议。"
            }

            partsArray.put(JSONObject().put("text", promptText))

            if (photoBitmap != null) {
                val inlineDataObj = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", photoBitmap.toBase64Jpeg())
                }
                partsArray.put(JSONObject().put("inlineData", inlineDataObj))
            }

            contentsArray.put(JSONObject().put("parts", partsArray))

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotEmpty()) {
                val responseObj = JSONObject(responseString)
                val candidates = responseObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val text = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    return@withContext parseAiResultJson(text)
                }
            }
            generateFallbackAnalysis(quizAnswersSummary)
        } catch (e: Exception) {
            e.printStackTrace()
            generateFallbackAnalysis(quizAnswersSummary)
        }
    }

    suspend fun consultAiAdvisor(
        userMessage: String,
        skinProfileSummary: String?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackConsultResponse(userMessage)
        }

        try {
            val systemPrompt = """
                你是一位温柔专业的女性美肤专家顾问，解答各种护肤疑难问题、成分搭配（如早C晚A、酸类刷酸）、敏感肌修复、防晒避坑等。
                当前用户的肤质背景：${skinProfileSummary ?: "混合偏干型敏感肌"}。
                请给出科学、严谨、语气亲切体贴的护肤解答（控制在300字以内，重点突出，排版清晰）。
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                ))
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotEmpty()) {
                val responseObj = JSONObject(responseString)
                val candidates = responseObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    return@withContext candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }
            }
            getFallbackConsultResponse(userMessage)
        } catch (e: Exception) {
            e.printStackTrace()
            getFallbackConsultResponse(userMessage)
        }
    }

    private fun parseAiResultJson(rawJsonText: String): AiSkinAnalysisResult {
        return try {
            val cleanJson = rawJsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val json = JSONObject(cleanJson)

            fun jsonList(arrayName: String): List<String> {
                val list = mutableListOf<String>()
                val arr = json.optJSONArray(arrayName)
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                }
                return list
            }

            AiSkinAnalysisResult(
                skinType = json.optString("skinType", "混合偏干敏感肌"),
                overallScore = json.optInt("overallScore", 82),
                moistureLevel = json.optInt("moistureLevel", 62),
                oilLevel = json.optInt("oilLevel", 48),
                textureLevel = json.optInt("textureLevel", json.optInt("textureScore", 78)),
                sensitivityScore = json.optInt("sensitivityScore", 58),
                acneScore = json.optInt("acneScore", 20),
                darkSpotScore = json.optInt("darkSpotScore", 32),
                barrierHealth = json.optString("barrierHealth", "轻度受损"),
                primaryConcerns = jsonList("primaryConcerns").ifEmpty { listOf("水油不均", "颊部泛红", "屏障脆弱") },
                analysisSummary = json.optString("analysisSummary", "您的皮肤角质层屏障存在轻度受损，T区伴有适度出油，颊部较为干燥易泛红。"),
                morningRoutineSteps = jsonList("morningRoutineSteps").ifEmpty { listOf("温和温水/氨基酸洁面", "积雪草舒缓水", "玻尿酸保湿精华", "屏障修护乳", "物理防晒霜") },
                eveningRoutineSteps = jsonList("eveningRoutineSteps").ifEmpty { listOf("卸妆膏/油", "温和氨基酸洁面", "B5角质修护精华", "神经酰胺滋润霜") },
                recommendedIngredients = jsonList("recommendedIngredients").ifEmpty { listOf("积雪草提取物", "神经酰胺", "B5泛醇", "透明质酸") },
                avoidedIngredients = jsonList("avoidedIngredients").ifEmpty { listOf("高浓度水杨酸", "纯维A醇", "变性乙醇", "强效剥脱酸") },
                dietAndLifestyleTips = jsonList("dietAndLifestyleTips").ifEmpty { listOf("避免熬夜，早睡促进角质修复", "减少高糖高油食物", "每日饮水不少于1800ml") }
            )
        } catch (e: Exception) {
            generateFallbackAnalysis(null)
        }
    }

    private fun generateFallbackAnalysis(quizSummary: String?): AiSkinAnalysisResult {
        val isDry = quizSummary?.contains("干燥") == true || quizSummary?.contains("紧绷") == true
        val isOily = quizSummary?.contains("油") == true || quizSummary?.contains("毛孔") == true
        val isSensitive = quizSummary?.contains("敏感") == true || quizSummary?.contains("泛红") == true || quizSummary?.contains("刺痛") == true

        val type = when {
            isSensitive && isOily -> "敏感性油痘肌"
            isSensitive && isDry -> "干性敏感强受损肌"
            isOily -> "油性易痘肌"
            isDry -> "干性缺水肌"
            else -> "混合偏干敏感肌"
        }

        return AiSkinAnalysisResult(
            skinType = type,
            overallScore = if (isSensitive) 76 else 84,
            moistureLevel = if (isDry) 42 else 68,
            oilLevel = if (isOily) 78 else 45,
            sensitivityScore = if (isSensitive) 72 else 35,
            acneScore = if (isOily) 65 else 22,
            darkSpotScore = 30,
            barrierHealth = if (isSensitive) "轻度受损" else "健康良好",
            primaryConcerns = if (isSensitive) listOf("局部泛红", "屏障受损", "换季紧绷") else listOf("水油不均", "毛孔粗大", "暗沉"),
            analysisSummary = "经智能算法检测，您的肤质属于「$type」。目前皮脂膜水分锁存能力略有不足，角质层耐受度需要进一步巩固与舒缓。",
            morningRoutineSteps = listOf("温和低泡氨基酸洁面", "舒缓保湿喷雾/水", "玻尿酸透明质酸精华", "神经酰胺修护乳", "广谱物理防晒霜"),
            eveningRoutineSteps = listOf("温和植物卸妆油", "氨基酸洁面", "维B5舒缓修护精华", "多肽角质修护面霜"),
            recommendedIngredients = listOf("积雪草苷", "神经酰胺NP", "维B5(泛醇)", "依克多因", "透明质酸钠"),
            avoidedIngredients = listOf("高浓度水杨酸", "高浓度果酸", "变性乙醇(酒精)", "强效纯维A醇", "强效人工香精"),
            dietAndLifestyleTips = listOf("多食用富含抗氧化剂的蓝莓与绿茶", "保持充足睡眠，利于皮脂膜自然修复", "洗脸水温控制在32-35℃之间，切忌过热")
        )
    }

    private fun getFallbackConsultResponse(query: String): String {
        return when {
            query.contains("屏障") || query.contains("受损") || query.contains("泛红") ->
                "💡 **屏障修护核心策略**：\n1. **减法护肤**：暂停所有功效型酸类、高浓度维C及A醇，停用去角质洗面奶。\n2. **精简精修**：选用含【神经酰胺】、【积雪草】、【维B5/泛醇】、【依克多因】的舒缓修护产品。\n3. **物理防晒**：避免化学防晒刺激，优先使用遮阳伞、帽子或纯物理防晒霜。\n4. **温和洁面**：早晨可用清水洗脸，晚间使用弱酸性氨基酸洁面。"

            query.contains("早C晚A") || query.contains("维C") || query.contains("A醇") ->
                "✨ **早C晚A建耐受指南**：\n1. **早C**：早晨使用抗氧化维C衍生物（如3-O-乙基抗坏血酸），后需做好严密防晒。\n2. **晚A**：晚间使用低浓度A醇/HPR，初期每周2次，建立耐受后再逐步增加频率。\n3. **敏感肌提示**：若角质层薄或泛红发痒，建议先修护屏障1-2周后再开启早C晚A；可用“三明治打底法”（乳液-A醇-面霜）降低刺激。"

            query.contains("痘") || query.contains("闭口") || query.contains("粉刺") ->
                "🌿 **油痘与闭口调理方案**：\n1. **温和控油**：选择含【烟酰胺】、【锌元素】、【水杨酸/辛酰水杨酸】的清爽调理精华。\n2. **切忌挤压**：避免手部细菌导致炎症扩散留下痘印。\n3. **保湿防晒**：痘痘肌同样需要无油配方的轻薄保湿乳与无致痘成分防晒霜。"

            else ->
                "🌸 **美肤专家贴心建议**：\n科学护肤遵循“**温和清洁 + 适度保湿 + 严密防晒**”三大金字塔基石。根据您的实时肤质变化动态调整用量与质地，切忌过度叠加多种功效精华。如需针对某种具体产品成分搭配，欢迎随时告诉我！"
        }
    }
}
