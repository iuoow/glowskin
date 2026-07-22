package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.AiSkinAnalysisResult
import com.example.ui.components.SkinAnalysisDataVisualizationCard
import com.example.ui.theme.*
import com.example.viewmodel.QuizAnswers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    isAnalyzing: Boolean,
    currentResult: AiSkinAnalysisResult?,
    initialMode: Int = 0, // 0 = Photo, 1 = Quiz
    onAnalyzePhoto: (Bitmap) -> Unit,
    onAnalyzeQuiz: (QuizAnswers) -> Unit,
    onNavigateToProducts: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialMode) }
    var quizAnswers by remember { mutableStateOf(QuizAnswers()) }
    var selectedSamplePhotoIndex by remember { mutableIntStateOf(0) }
    var userCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "智能肤质检测与诊断",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode Selector Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RosePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("AI 相机测肤", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("深度问卷诊断", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (isAnalyzing) {
                // Multi-step Upload & AI Analysis Progress View
                UploadAnalysisProgressView(
                    analyzingBitmap = userCapturedBitmap ?: createSampleBitmap(selectedSamplePhotoIndex)
                )
            } else if (currentResult != null) {
                // Analysis Results View
                AnalysisResultView(
                    result = currentResult,
                    onRetest = { /* Reset if needed */ },
                    onNavigateToProducts = onNavigateToProducts
                )
            } else {
                // Input view (Photo or Quiz)
                if (selectedTab == 0) {
                    PhotoDetectionContent(
                        userBitmap = userCapturedBitmap,
                        selectedSampleIndex = selectedSamplePhotoIndex,
                        onPhotoCaptured = { bitmap -> userCapturedBitmap = bitmap },
                        onSelectSample = {
                            selectedSamplePhotoIndex = it
                            userCapturedBitmap = null
                        },
                        onStartScan = {
                            val bitmapToAnalyze = userCapturedBitmap ?: createSampleBitmap(selectedSamplePhotoIndex)
                            onAnalyzePhoto(bitmapToAnalyze)
                        }
                    )
                } else {
                    QuizDetectionContent(
                        answers = quizAnswers,
                        onAnswersChanged = { quizAnswers = it },
                        onSubmitQuiz = { onAnalyzeQuiz(quizAnswers) }
                    )
                }
            }
        }
    }
}

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.25f),
            Color.LightGray.copy(alpha = 0.65f),
            Color.LightGray.copy(alpha = 0.25f),
        )

        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(850, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = translateAnimation.value - 200f, y = translateAnimation.value - 200f),
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun UploadAnalysisProgressView(
    analyzingBitmap: Bitmap?
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var currentStep by remember { mutableIntStateOf(0) }

    // Dynamic progress bar calculation
    LaunchedEffect(Unit) {
        progress = 0.08f
        currentStep = 0

        // Step 1: Image optimization & encoding
        kotlinx.coroutines.delay(400)
        progress = 0.30f
        currentStep = 1

        // Step 2: Establish TLS encrypted pipe to Gemini Cloud
        kotlinx.coroutines.delay(700)
        progress = 0.60f
        currentStep = 2

        // Step 3: Gemini Vision multi-modal analysis
        kotlinx.coroutines.delay(1000)
        progress = 0.88f
        currentStep = 3

        // Step 4: Generating report
        kotlinx.coroutines.delay(800)
        progress = 0.98f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "uploadProgressAnimation"
    )

    val steps = listOf(
        "图像尺寸压缩与肤质微观特征降噪",
        "建立 TLS 1.3 级安全通道上传中",
        "Gemini 2.5 AI 视觉网络深度识别解析",
        "汇总角质层状态与屏障建议报告"
    )

    val tips = listOf(
        "💡 测肤提示：保持光线均匀、避免强光直射能提升检测精度",
        "🔬 Gemini 正精细对比 T 区油分与两颊水分指标",
        "✨ 算法正在比对数百种成分与您的敏感度适配度"
    )
    var tipIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            tipIndex = (tipIndex + 1) % tips.size
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("upload_progress_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Photo Preview with animated vertical laser scanning line
            Card(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, RosePrimary)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (analyzingBitmap != null) {
                        Image(
                            bitmap = analyzingBitmap.asImageBitmap(),
                            contentDescription = "Analyzing photo preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_skin_analysis_1784721762669),
                            contentDescription = "Analyzing sample photo preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Scanner beam overlay
                    val infiniteTransition = rememberInfiniteTransition(label = "scanningBeam")
                    val scanOffsetY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 140f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "laserLine"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .offset(y = scanOffsetY.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, RosePrimary, Color.Transparent)
                                )
                            )
                    )
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "图像上传与 Gemini AI 测肤解析中...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RosePrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Smooth Linear Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = RosePrimary,
                    trackColor = RoseSecondary.copy(alpha = 0.3f)
                )
            }
        }

        // Multi-step Progress Checklist Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RoseBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    steps.forEachIndexed { index, stepName ->
                        val isCompleted = index < currentStep
                        val isCurrent = index == currentStep

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isCompleted) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = RosePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (isCurrent) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = RosePrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.RadioButtonUnchecked,
                                    contentDescription = "Pending",
                                    tint = MutedText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) RosePrimary else if (isCompleted) MaterialTheme.colorScheme.onSurface else MutedText
                            )
                        }
                    }
                }
            }
        }

        item {
            // Dynamic Rotating Tip Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RosePrimary.copy(alpha = 0.08f)
            ) {
                AnimatedContent(
                    targetState = tips[tipIndex],
                    transitionSpec = { fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { -it / 2 } },
                    label = "tipTransition"
                ) { tip ->
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.labelSmall,
                        color = RosePrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Skeleton Preview of the Incoming Report
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                Text(
                    text = "正在生成卡片式报告骨架...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RosePrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnalysisReportSkeletonView()
        }
    }
}

@Composable
fun AnalysisReportSkeletonView() {
    val brush = shimmerBrush()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Skeleton Header Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Skeleton Pill for Skin Type
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Skeleton Circle for Score
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Skeleton Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(brush)
                            )
                        }
                    }
                }
            }
        }

        // Skeleton AI Analysis & Concerns Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(brush)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        // Skeleton Routine Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(12.dp))
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(brush)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PhotoDetectionContent(
    userBitmap: Bitmap?,
    selectedSampleIndex: Int,
    onPhotoCaptured: (Bitmap?) -> Unit,
    onSelectSample: (Int) -> Unit,
    onStartScan: () -> Unit
) {
    val context = LocalContext.current

    // Initialize Shutter Sound Player
    val soundPlayer = remember {
        MediaActionSound().apply {
            try {
                load(MediaActionSound.SHUTTER_CLICK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                soundPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Flash Animation State
    var showShutterFlash by remember { mutableStateOf(false) }

    LaunchedEffect(showShutterFlash) {
        if (showShutterFlash) {
            kotlinx.coroutines.delay(220)
            showShutterFlash = false
        }
    }

    val flashAlpha by animateFloatAsState(
        targetValue = if (showShutterFlash) 0.95f else 0f,
        animationSpec = tween(if (showShutterFlash) 30 else 220),
        label = "flashAlphaAnimation"
    )

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Play Shutter Sound Effect
            try {
                soundPlayer.play(MediaActionSound.SHUTTER_CLICK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Trigger visual shutter flash transition
            showShutterFlash = true
            onPhotoCaptured(bitmap)
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                }
                onPhotoCaptured(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Buttons Row (Camera & Gallery)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_camera_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("调用摄像头拍照", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_gallery_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RosePrimary),
                    border = BorderStroke(1.5.dp, RosePrimary)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("从相册选择", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        item {
            // Camera / Image Display Frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, RoseBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Smooth Transition Animation when captured photo changes
                    AnimatedContent(
                        targetState = userBitmap,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.90f)) togetherWith
                                    (fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f))
                        },
                        label = "photoCapturedTransition"
                    ) { bitmap ->
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "User Captured Skin Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_skin_analysis_1784721762669),
                                contentDescription = "Skin Scan Frame",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Overlay scanning line frame with animated laser line
                    Box(
                        modifier = Modifier
                            .size(200.dp, 240.dp)
                            .border(2.dp, RosePrimary, RoundedCornerShape(24.dp))
                            .background(Color.Transparent)
                    ) {
                        // Scanner line animation
                        val infiniteTransition = rememberInfiniteTransition(label = "scanLaser")
                        val laserPosY by infiniteTransition.animateFloat(
                            initialValue = 10f,
                            targetValue = 220f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "laserPos"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .offset(y = laserPosY.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, RosePrimary, Color.Transparent)
                                    )
                                )
                        )

                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = if (userBitmap != null) "已捕捉照片，等待 AI 解析" else "请将面部平视对准框内",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (userBitmap != null) {
                        IconButton(
                            onClick = { onPhotoCaptured(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "清除照片", tint = Color.White)
                        }
                    }

                    // Shutter White Flash Effect Overlay
                    if (flashAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = flashAlpha))
                        )
                    }
                }
            }
        }

        if (userBitmap == null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "或选择预设样本肤质体验：",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val samples = listOf("敏感混合偏干", "T区油痘易发", "干性缺水泛红", "耐受中性肌肤")

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(samples.size) { index ->
                            FilterChip(
                                selected = selectedSampleIndex == index,
                                onClick = { onSelectSample(index) },
                                label = { Text(samples[index]) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RosePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("start_photo_analysis_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (userBitmap != null) "发送图像至 Gemini 进行 AI 深度解析" else "开始 AI 智能光效测肤",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = RosePrimary)
                    Text(
                        text = "隐私保护提示：测肤图像仅用于 Gemini AI 肤质诊断，加密传输且不会存储或用于第三方个人识别。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun QuizDetectionContent(
    answers: QuizAnswers,
    onAnswersChanged: (QuizAnswers) -> Unit,
    onSubmitQuiz: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val questions = listOf(
        QuizQuestionItem(
            title = "出油与发亮情况",
            subtitle = "评估面部T区与两颊皮脂腺的分泌活跃度",
            icon = Icons.Default.Opacity,
            options = listOf(
                "全脸出油严重，油光满面",
                "T区轻度出油，两颊偏干",
                "全脸很少出油，经常感觉干燥",
                "油水分泌平衡，无明显油光"
            ),
            getSelected = { answers.shineAndOil },
            updateAnswer = { onAnswersChanged(answers.copy(shineAndOil = it)) }
        ),
        QuizQuestionItem(
            title = "洁面后不涂护肤品的感受",
            subtitle = "测量清洁后角质层自然经皮水分流失与紧绷感",
            icon = Icons.Default.WaterDrop,
            options = listOf(
                "10分钟内感到全脸紧绷发干甚至刺痛",
                "两颊轻微紧绷，T区舒适",
                "感觉清爽舒适，无紧绷感",
                "半小时内很快又大量出油"
            ),
            getSelected = { answers.tightnessAfterWash },
            updateAnswer = { onAnswersChanged(answers.copy(tightnessAfterWash = it)) }
        ),
        QuizQuestionItem(
            title = "毛孔与黑头状态",
            subtitle = "观察毛孔粗大程度与角栓氧化结晶分布",
            icon = Icons.Default.FilterVintage,
            options = listOf(
                "鼻翼与两颊毛孔粗大，有明显黑头",
                "鼻翼毛孔明显，伴有黑头",
                "毛孔极其细小，几乎看不到黑头",
                "偶有闭口粉刺"
            ),
            getSelected = { answers.poreCondition },
            updateAnswer = { onAnswersChanged(answers.copy(poreCondition = it)) }
        ),
        QuizQuestionItem(
            title = "敏感与泛红频繁度",
            subtitle = "评估外界环境刺激下的皮肤神经敏感与屏障防御",
            icon = Icons.Default.Shield,
            options = listOf(
                "换季或温度变化时面部易泛红刺痛",
                "使用新护肤品容易痒或起小红粒",
                "极少泛红发痒，耐受度高",
                "日晒后很快泛红灼热"
            ),
            getSelected = { answers.rednessSensitivity },
            updateAnswer = { onAnswersChanged(answers.copy(rednessSensitivity = it)) }
        ),
        QuizQuestionItem(
            title = "痘痘与闭口生长频率",
            subtitle = "诊断毛囊皮脂腺炎症发炎与角化异常频率",
            icon = Icons.Default.BugReport,
            options = listOf(
                "频繁长红肿脓包痘痘",
                "生理期前偶尔长1-2颗红肿痘痘",
                "下巴额头多闭口粉刺",
                "几乎不长痘痘"
            ),
            getSelected = { answers.acneFrequency },
            updateAnswer = { onAnswersChanged(answers.copy(acneFrequency = it)) }
        ),
        QuizQuestionItem(
            title = "色素沉着与痘印斑点",
            subtitle = "检查黑色素沉淀、炎症后痘印与晒斑分布",
            icon = Icons.Default.AutoAwesome,
            options = listOf(
                "局部有少量红黑痘印或晒斑",
                "面部暗沉发黄无光泽",
                "有顽固色斑需要淡化",
                "肤色均匀无明显瑕疵"
            ),
            getSelected = { answers.pigmentationSpots },
            updateAnswer = { onAnswersChanged(answers.copy(pigmentationSpots = it)) }
        )
    )

    val currentQuestion = questions[currentQuestionIndex]
    val animatedProgress by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1) / 6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "quizProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Header Progress Bar Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RoseBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = RosePrimary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${currentQuestionIndex + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "深度问卷诊断",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "进度 ${currentQuestionIndex + 1} / 6 (${(animatedProgress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = RosePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Linear Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = RosePrimary,
                        trackColor = RosePrimary.copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step Dots Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(6) { idx ->
                            val isCompleted = questions[idx].getSelected().isNotEmpty()
                            val isCurrent = idx == currentQuestionIndex

                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCurrent) RosePrimary
                                        else if (isCompleted) SageGreen
                                        else Color.LightGray.copy(alpha = 0.5f)
                                    )
                                    .clickable { currentQuestionIndex = idx }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Single-Question Card View
            AnimatedContent(
                targetState = currentQuestionIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "questionTransition"
            ) { targetIndex ->
                val q = questions[targetIndex]
                val selectedOpt = q.getSelected()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, RoseBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RosePrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = q.icon,
                                        contentDescription = null,
                                        tint = RosePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = q.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = q.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Option Items
                        q.options.forEach { option ->
                            val isSelected = selectedOpt == option

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable {
                                        q.updateAnswer(option)
                                        if (currentQuestionIndex < 5) {
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(280)
                                                if (currentQuestionIndex < 5) {
                                                    currentQuestionIndex++
                                                }
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) RosePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) RosePrimary else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            q.updateAnswer(option)
                                            if (currentQuestionIndex < 5) {
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(280)
                                                    if (currentQuestionIndex < 5) {
                                                        currentQuestionIndex++
                                                    }
                                                }
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = RosePrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Wizard Navigation Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentQuestionIndex > 0) {
                OutlinedButton(
                    onClick = { currentQuestionIndex-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RosePrimary)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("上一题", fontWeight = FontWeight.Bold)
                }
            }

            if (currentQuestionIndex < 5) {
                Button(
                    onClick = { currentQuestionIndex++ },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("quiz_next_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("下一题", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(
                    onClick = onSubmitQuiz,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("submit_quiz_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("提交诊断", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class QuizQuestionItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val options: List<String>,
    val getSelected: () -> String,
    val updateAnswer: (String) -> Unit
)

@Composable
fun QuizQuestionCard(
    stepNumber: String,
    questionTitle: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RoseBorder.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = RosePrimary,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = stepNumber, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Text(text = questionTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            options.forEach { option ->
                val isSelected = selectedOption == option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onSelect(option) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) RosePrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(option) },
                        colors = RadioButtonDefaults.colors(selectedColor = RosePrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisResultView(
    result: AiSkinAnalysisResult,
    onRetest: () -> Unit,
    onNavigateToProducts: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Overall Health Score & Barrier Status Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, RoseBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = RosePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = result.skinType,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RosePrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (result.barrierHealth) {
                                "健康" -> SageGreen.copy(alpha = 0.15f)
                                "轻度受损" -> AmberWarning.copy(alpha = 0.15f)
                                else -> CoralDanger.copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = when (result.barrierHealth) {
                                        "健康" -> SageGreen
                                        "轻度受损" -> AmberWarning
                                        else -> CoralDanger
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "屏障：${result.barrierHealth}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (result.barrierHealth) {
                                        "健康" -> SageGreen
                                        "轻度受损" -> AmberWarning
                                        else -> CoralDanger
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular Score Gauge with Accent Circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(110.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { result.overallScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = RosePrimary,
                            strokeWidth = 10.dp,
                            trackColor = RoseSecondary.copy(alpha = 0.25f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${result.overallScore}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = RosePrimary
                            )
                            Text(
                                text = "综合健康度",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ScoreBadge("水份", "${result.moistureLevel}%", Color(0xFF2196F3))
                        ScoreBadge("油分", "${result.oilLevel}%", AmberWarning)
                        ScoreBadge("纹理", "${result.textureLevel}%", SageGreen)
                        ScoreBadge("敏感", "${result.sensitivityScore}%", CoralDanger)
                    }
                }
            }
        }

        // Card 2: Interactive Data Visualization Chart (Moisture, Oil, Texture, Radar)
        item {
            SkinAnalysisDataVisualizationCard(result = result)
        }

        // Card 3: AI Skin Analysis Diagnosis & Concerns
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RoseBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RosePrimary)
                        Text(
                            text = "Gemini AI 智能诊断分析",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "核心关注问题：",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(result.primaryConcerns.size) { index ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(result.primaryConcerns[index], fontSize = 12.sp) },
                                icon = { Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = RosePrimary,
                                    iconContentColor = RosePrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Medical Quote Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RosePrimary.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, RosePrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = RosePrimary)
                            Text(
                                text = result.analysisSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Card 4: Customized Morning & Evening Skincare Routine
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RoseBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = RosePrimary)
                        Text(
                            text = "早晚分时理肤步骤",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Morning Routine Section
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                        Text("☀️ 日间防光护屏步骤：", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = AmberWarning)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    result.morningRoutineSteps.forEachIndexed { idx, step ->
                        Text(
                            text = "  ${idx + 1}. $step",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = RoseBorder.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Evening Routine Section
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(18.dp))
                        Text("🌙 夜间密集修护步骤：", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    result.eveningRoutineSteps.forEachIndexed { idx, step ->
                        Text(
                            text = "  ${idx + 1}. $step",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Card 5: Ingredient Guide (Do's and Don'ts)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RoseBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = RosePrimary)
                        Text(
                            text = "护肤成分挑选红黑榜",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "✅ 建议优先选用成分：",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SageGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(result.recommendedIngredients.size) { index ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SageGreen.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SageGreen, modifier = Modifier.size(14.dp))
                                    Text(result.recommendedIngredients[index], style = MaterialTheme.typography.labelSmall, color = SageGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "❌ 建议谨慎避雷成分：",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CoralDanger
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(result.avoidedIngredients.size) { index ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CoralDanger.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = null, tint = CoralDanger, modifier = Modifier.size(14.dp))
                                    Text(result.avoidedIngredients[index], style = MaterialTheme.typography.labelSmall, color = CoralDanger, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 6: Diet & Lifestyle Tips
        if (result.dietAndLifestyleTips.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, RoseBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = RosePrimary)
                            Text(
                                text = "膳食与作息调理建议",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        result.dietAndLifestyleTips.forEach { tip ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("•", fontWeight = FontWeight.Bold, color = RosePrimary)
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onNavigateToProducts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("view_matching_products_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("查看匹配该肤质的定制护肤品推荐", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRetest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("retest_skin_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RosePrimary),
                    border = BorderStroke(1.5.dp, RosePrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("重新测量肤质", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricProgressBarItem(label: String, scoreValue: Int, barColor: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(text = "$scoreValue%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = barColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { scoreValue / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun ScoreBadge(title: String, scoreText: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = scoreText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MutedText)
    }
}

private fun createSampleBitmap(index: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.rgb(255, 230, 235)
    }
    canvas.drawRect(0f, 0f, 200f, 200f, paint)
    return bitmap
}
