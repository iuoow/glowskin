package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DailySkincareLog
import com.example.data.database.Product
import com.example.data.database.SkinDatabase
import com.example.data.database.SkinScanRecord
import com.example.data.remote.AiSkinAnalysisResult
import com.example.data.repository.SkinRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val userId: String = "user_88392",
    val nickname: String = "Glow美肤达人",
    val phoneOrEmail: String = "138****9201",
    val avatarType: Int = 1,
    val isLoggedIn: Boolean = true,
    val loginType: String = "微信快捷登录",
    val vipLevel: String = "SVIP黑金会员",
    val skinGoal: String = "深层补水 / 强健屏障 / 淡褪痘印"
)

data class QuizAnswers(
    val shineAndOil: String = "T区轻度出油，两颊偏干",
    val tightnessAfterWash: String = "洗脸后10分钟感到紧绷发干",
    val poreCondition: String = "鼻翼毛孔明显，伴有黑头",
    val rednessSensitivity: String = "换季或温度变化时面部易泛红刺痛",
    val acneFrequency: String = "生理期前偶尔长1-2颗红肿痘痘",
    val pigmentationSpots: String = "局部有少量红黑痘印或晒斑"
) {
    fun toSummaryString(): String {
        return """
            1. 出油出汗情况：$shineAndOil
            2. 洁面后肤感：$tightnessAfterWash
            3. 毛孔与角质：$poreCondition
            4. 敏感与泛红：$rednessSensitivity
            5. 痘痘与闭口：$acneFrequency
            6. 色素与斑点：$pigmentationSpots
        """.trimIndent()
    }
}

data class ChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SkinViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SkinRepository

    val latestScan: StateFlow<SkinScanRecord?>
    val allScanRecords: StateFlow<List<SkinScanRecord>>
    val dailyLog: StateFlow<DailySkincareLog?>

    // Detection UI State
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentAnalysisResult = MutableStateFlow<AiSkinAnalysisResult?>(null)
    val currentAnalysisResult: StateFlow<AiSkinAnalysisResult?> = _currentAnalysisResult.asStateFlow()

    // Filter & Search State for Products
    val selectedCategory = MutableStateFlow("全部")
    val selectedConcern = MutableStateFlow("全部")
    val searchQuery = MutableStateFlow("")

    val products: StateFlow<List<Product>>

    // User Profile & Authentication State
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // AI Consultant Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "🌸 您好！我是 GlowSkin 专属美肤小助手。您可以问我关于早C晚A使用顺序、换季屏障修复、敏感肌成分避坑或具体护肤品搭配问题哦！"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    init {
        val database = SkinDatabase.getDatabase(application)
        repository = SkinRepository(database.skinDao())

        latestScan = repository.latestScanRecord.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allScanRecords = repository.allScanRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dailyLog = repository.getDailyLog(todayIso).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailySkincareLog(todayIso)
        )

        val skinTypeFlow = latestScan.map { it?.skinType }

        products = combine(
            repository.getRecommendedProducts(null),
            skinTypeFlow,
            selectedCategory,
            selectedConcern,
            searchQuery
        ) { rawProducts, currentSkinType, category, concern, query ->
            rawProducts.filter { product ->
                val categoryMatch = category == "全部" || product.category == category
                val concernMatch = concern == "全部" || product.targetConcerns.contains(concern)
                val queryMatch = query.isEmpty() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.brand.contains(query, ignoreCase = true) ||
                        product.keyIngredients.any { it.contains(query, ignoreCase = true) }

                categoryMatch && concernMatch && queryMatch
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun analyzePhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = repository.runSkinDetection(bitmap, null)
                _currentAnalysisResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun analyzeQuiz(quizAnswers: QuizAnswers) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = repository.runSkinDetection(null, quizAnswers.toSummaryString())
                _currentAnalysisResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun toggleFavorite(productId: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteProduct(productId, isFav)
        }
    }

    fun toggleMorningSkincare(completed: Boolean) {
        viewModelScope.launch {
            val current = dailyLog.value ?: DailySkincareLog(getTodayIso())
            repository.saveDailyLog(current.copy(morningCompleted = completed))
        }
    }

    fun toggleEveningSkincare(completed: Boolean) {
        viewModelScope.launch {
            val current = dailyLog.value ?: DailySkincareLog(getTodayIso())
            repository.saveDailyLog(current.copy(eveningCompleted = completed))
        }
    }

    fun addWaterIntake() {
        viewModelScope.launch {
            val current = dailyLog.value ?: DailySkincareLog(getTodayIso())
            val updated = (current.waterIntakeGlasses + 1).coerceAtMost(12)
            repository.saveDailyLog(current.copy(waterIntakeGlasses = updated))
        }
    }

    fun sendConsultMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(sender = "USER", text = text.trim())
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val skinType = latestScan.value?.skinType
            val aiResponseText = repository.consultAiAdvisor(text, skinType)
            val aiMsg = ChatMessage(sender = "AI", text = aiResponseText)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiThinking.value = false
        }
    }

    fun loginUser(nickname: String, phoneOrEmail: String, loginType: String) {
        _userProfile.value = UserProfile(
            userId = "user_" + (10000..99999).random(),
            nickname = if (nickname.isNotBlank()) nickname else "Glow美肤达人",
            phoneOrEmail = if (phoneOrEmail.isNotBlank()) phoneOrEmail else "138****" + (1000..9999).random(),
            isLoggedIn = true,
            loginType = loginType,
            vipLevel = "SVIP黑金会员"
        )
    }

    fun logoutUser() {
        _userProfile.value = _userProfile.value.copy(
            isLoggedIn = false,
            nickname = "未登录",
            phoneOrEmail = "点击登录以同步数据"
        )
    }

    fun updateUserProfile(nickname: String, skinGoal: String) {
        _userProfile.value = _userProfile.value.copy(
            nickname = nickname.ifBlank { _userProfile.value.nickname },
            skinGoal = skinGoal.ifBlank { _userProfile.value.skinGoal }
        )
    }

    private fun getTodayIso(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
