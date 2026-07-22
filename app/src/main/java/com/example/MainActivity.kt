package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.GlowSkinTheme
import com.example.ui.theme.RosePrimary
import com.example.viewmodel.SkinViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SkinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GlowSkinTheme {
                MainAppContent(viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home, Icons.Outlined.Home)
    object Detect : Screen("detect", "肤质检测", Icons.Default.CameraAlt, Icons.Outlined.CameraAlt)
    object Routine : Screen("routine", "改善指南", Icons.Default.AutoFixHigh, Icons.Outlined.AutoFixHigh)
    object Products : Screen("products", "护肤推荐", Icons.Default.ShoppingBag, Icons.Outlined.ShoppingBag)
    object Consult : Screen("consult", "AI问答", Icons.Default.SupportAgent, Icons.Outlined.SupportAgent)
    object Profile : Screen("profile", "我的档案", Icons.Default.Person, Icons.Outlined.Person)
}

@Composable
fun MainAppContent(viewModel: SkinViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var initialDetectionMode by remember { mutableIntStateOf(0) } // 0 = photo, 1 = quiz

    val latestScan by viewModel.latestScan.collectAsStateWithLifecycle()
    val allScanRecords by viewModel.allScanRecords.collectAsStateWithLifecycle()
    val dailyLog by viewModel.dailyLog.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val currentResult by viewModel.currentAnalysisResult.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedConcern by viewModel.selectedConcern.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()

    // Core 4 Bottom Navigation Tabs
    val bottomNavScreens = listOf(
        Screen.Home,
        Screen.Detect,
        Screen.Consult,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                bottomNavScreens.forEach { screen ->
                    val selected = currentScreen.route == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RosePrimary,
                            selectedTextColor = RosePrimary,
                            indicatorColor = RosePrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    latestScan = latestScan,
                    dailyLog = dailyLog,
                    recommendedProducts = products,
                    onStartPhotoScan = {
                        initialDetectionMode = 0
                        currentScreen = Screen.Detect
                    },
                    onStartQuizScan = {
                        initialDetectionMode = 1
                        currentScreen = Screen.Detect
                    },
                    onNavigateToProducts = { currentScreen = Screen.Products },
                    onNavigateToRoutine = { currentScreen = Screen.Routine },
                    onToggleMorning = { viewModel.toggleMorningSkincare(it) },
                    onToggleEvening = { viewModel.toggleEveningSkincare(it) },
                    onAddWater = { viewModel.addWaterIntake() },
                    onToggleFavorite = { id, isFav -> viewModel.toggleFavorite(id, isFav) }
                )

                Screen.Detect -> DetectionScreen(
                    isAnalyzing = isAnalyzing,
                    currentResult = currentResult,
                    initialMode = initialDetectionMode,
                    onAnalyzePhoto = { bitmap -> viewModel.analyzePhoto(bitmap) },
                    onAnalyzeQuiz = { quizAnswers -> viewModel.analyzeQuiz(quizAnswers) },
                    onNavigateToProducts = { currentScreen = Screen.Products }
                )

                Screen.Routine -> RoutineAndAdviceScreen()

                Screen.Products -> ProductRecommendScreen(
                    products = products,
                    selectedCategory = selectedCategory,
                    selectedConcern = selectedConcern,
                    searchQuery = searchQuery,
                    onCategorySelected = { viewModel.selectedCategory.value = it },
                    onConcernSelected = { viewModel.selectedConcern.value = it },
                    onQueryChanged = { viewModel.searchQuery.value = it },
                    onToggleFavorite = { id, isFav -> viewModel.toggleFavorite(id, isFav) }
                )

                Screen.Consult -> AiConsultantScreen(
                    messages = chatMessages,
                    isThinking = isAiThinking,
                    onSendMessage = { viewModel.sendConsultMessage(it) }
                )

                Screen.Profile -> HistoryAndProfileScreen(
                    records = allScanRecords,
                    favoriteProducts = products.filter { it.isFavorite },
                    onToggleFavorite = { id, isFav -> viewModel.toggleFavorite(id, isFav) },
                    onNavigateToProducts = { currentScreen = Screen.Products },
                    onNavigateToRoutine = { currentScreen = Screen.Routine },
                    onNavigateToDetect = { currentScreen = Screen.Detect }
                )
            }
        }
    }
}
