package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Product
import com.example.data.database.SkinScanRecord
import com.example.ui.theme.*
import com.example.viewmodel.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAndProfileScreen(
    userProfile: UserProfile,
    records: List<SkinScanRecord>,
    favoriteProducts: List<Product>,
    onToggleFavorite: (String, Boolean) -> Unit,
    onLoginUser: (String, String, String) -> Unit,
    onLogoutUser: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToDetect: () -> Unit
) {
    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCompareDialog by remember { mutableStateOf(false) }
    var showIngredientMatcher by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }

    var isDailyReminderEnabled by remember { mutableStateOf(true) }
    var streakDays by remember { mutableIntStateOf(7) }
    var hasCheckedInToday by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "美肤档案与个人中心",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (userProfile.isLoggedIn) {
                            showEditProfileDialog = true
                        } else {
                            showAuthDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = RosePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card & Auth State Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, RoseBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clickable {
                                        if (!userProfile.isLoggedIn) showAuthDialog = true
                                        else showEditProfileDialog = true
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (userProfile.isLoggedIn) Icons.Default.Face else Icons.Default.AccountCircle,
                                        contentDescription = "Avatar",
                                        tint = RosePrimary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                if (userProfile.isLoggedIn) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = userProfile.nickname,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ChampagneGold.copy(alpha = 0.3f)
                                        ) {
                                            Text(
                                                text = userProfile.vipLevel,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFD81B60),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${userProfile.loginType} · ${userProfile.phoneOrEmail}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedText
                                    )
                                    Text(
                                        text = "美肤目标：${userProfile.skinGoal}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RosePrimary,
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        text = "未登录美肤账号",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "点击一键登录，多端同步皮肤档案",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedText
                                    )
                                }
                            }

                            // Right Action Button
                            if (userProfile.isLoggedIn) {
                                OutlinedButton(
                                    onClick = { showEditProfileDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, RosePrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = RosePrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("编辑", style = MaterialTheme.typography.labelSmall, color = RosePrimary, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { showAuthDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("快速登录", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = RoseBorder.copy(alpha = 0.4f))

                        // Stats & Check-in Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProfileStatItem("检测次数", "${records.size} 次")
                                ProfileStatItem("打卡天数", "${streakDays} 天")
                                ProfileStatItem("收藏护肤品", "${favoriteProducts.size} 件")
                            }

                            Button(
                                onClick = {
                                    if (!hasCheckedInToday) {
                                        hasCheckedInToday = true
                                        streakDays++
                                        Toast.makeText(context, "今日测肤护肤打卡成功！连续打卡 ${streakDays} 天 🎉", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "今天已经打过卡啦，明天继续保持哦！", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasCheckedInToday) SageGreen else RosePrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasCheckedInToday) Icons.Default.Check else Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (hasCheckedInToday) "已打卡" else "打卡",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Refactored Feature Grid Section ("精选美肤服务")
            item {
                Text(
                    text = "精选美肤服务",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickServiceCard(
                            title = "Before & After",
                            subtitle = "肤质多维改善对比",
                            icon = Icons.Default.Compare,
                            badgeText = "NEW",
                            modifier = Modifier.weight(1f),
                            onClick = { showCompareDialog = true }
                        )

                        QuickServiceCard(
                            title = "成分库避雷匹配",
                            subtitle = "查防敏与安全等级",
                            icon = Icons.Default.Science,
                            badgeText = "工具",
                            modifier = Modifier.weight(1f),
                            onClick = { showIngredientMatcher = true }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickServiceCard(
                            title = "个性化护理方案",
                            subtitle = "日常美肤步骤与技巧",
                            icon = Icons.Default.AutoFixHigh,
                            badgeText = null,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRoutine
                        )

                        QuickServiceCard(
                            title = "正品护肤推荐",
                            subtitle = "供应商直购对接中",
                            icon = Icons.Default.ShoppingBag,
                            badgeText = "即将推出",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProducts
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickServiceCard(
                            title = "测肤打卡提醒",
                            subtitle = if (isDailyReminderEnabled) "20:00 每日提醒" else "提醒未开启",
                            icon = Icons.Default.NotificationsActive,
                            badgeText = null,
                            modifier = Modifier.weight(1f),
                            onClick = { showReminderSettings = true }
                        )

                        QuickServiceCard(
                            title = "再次深度测肤",
                            subtitle = "AI相机 / 问卷诊断",
                            icon = Icons.Default.CameraAlt,
                            badgeText = null,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDetect
                        )
                    }
                }
            }

            // Skin Evolution Trend Curve Chart Section
            if (records.size >= 2) {
                item {
                    Text(
                        text = "肤质水分与综合得分演变趋势",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(10.dp).background(RosePrimary, CircleShape))
                                    Text("综合得分", style = MaterialTheme.typography.labelSmall, color = MutedText)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF0288D1), CircleShape))
                                    Text("水分值%", style = MaterialTheme.typography.labelSmall, color = MutedText)
                                }
                                Text("连续追踪", style = MaterialTheme.typography.labelSmall, color = RosePrimary, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SkinTrendLineCanvas(records)
                        }
                    }
                }
            }

            // Historical Scan Timeline Section
            item {
                Text(
                    text = "历史肤质检测记录 (${records.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "暂无检测记录，快去“肤质检测”测一测吧！", style = MaterialTheme.typography.bodyMedium, color = MutedText)
                        }
                    }
                }
            } else {
                items(records) { record ->
                    HistoryRecordCard(record = record)
                }
            }

            // Favorite Products Section
            if (favoriteProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "已收藏的护肤品 (${favoriteProducts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(favoriteProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.brand, style = MaterialTheme.typography.labelSmall, color = MutedText)
                                Text(text = product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(text = "适配: ${product.suitableSkinTypes.joinToString()}", style = MaterialTheme.typography.labelSmall, color = RosePrimary)
                            }
                            IconButton(onClick = { onToggleFavorite(product.id, false) }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Unfavorite", tint = RosePrimary)
                            }
                        }
                    }
                }
            }

            // Account Security & Logout Section
            if (userProfile.isLoggedIn) {
                item {
                    OutlinedButton(
                        onClick = {
                            onLogoutUser()
                            Toast.makeText(context, "已安全退出当前账号", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("退出登录 / 切换账号", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Sheet 1: Auth / Login / Register Modal
    if (showAuthDialog) {
        AuthLoginModalDialog(
            onDismiss = { showAuthDialog = false },
            onLoginSuccess = { nickname, phone, loginType ->
                onLoginUser(nickname, phone, loginType)
                showAuthDialog = false
                Toast.makeText(context, "登录成功！欢迎回来，$nickname ✨", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Sheet 2: Edit Profile Modal
    if (showEditProfileDialog) {
        EditProfileModalDialog(
            currentProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, goal ->
                onUpdateProfile(name, goal)
                showEditProfileDialog = false
                Toast.makeText(context, "个人资料已更新！", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog: Before & After Comparison
    if (showCompareDialog) {
        BeforeAfterCompareModal(
            records = records,
            onDismiss = { showCompareDialog = false }
        )
    }

    // Modal Dialog: Ingredient Matcher Tool
    if (showIngredientMatcher) {
        IngredientMatcherModal(
            currentSkinType = records.firstOrNull()?.skinType ?: "敏感混合偏干",
            onDismiss = { showIngredientMatcher = false }
        )
    }

    // Modal Dialog: Reminders Settings
    if (showReminderSettings) {
        AlertDialog(
            onDismissRequest = { showReminderSettings = false },
            confirmButton = {
                Button(
                    onClick = {
                        showReminderSettings = false
                        Toast.makeText(context, if (isDailyReminderEnabled) "设置成功！每天 20:00 将提醒您进行测肤与夜间护肤" else "已关闭提醒", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                ) {
                    Text("保存设置")
                }
            },
            title = { Text("测肤与护肤打卡提醒", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("开启每日测肤护肤提醒")
                        Switch(
                            checked = isDailyReminderEnabled,
                            onCheckedChange = { isDailyReminderEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RosePrimary)
                        )
                    }

                    HorizontalDivider(color = RoseBorder)

                    Text("提醒时间：每天 20:00 (夜间洁面后)", style = MaterialTheme.typography.bodySmall, color = MutedText)
                    Text("坚持打卡能更好地跟踪肌肤屏障与水油改善趋势哦！", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun AuthLoginModalDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: (nickname: String, phone: String, loginType: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 快捷一键登录, 1: 手机号验证码
    var phoneNumber by remember { mutableStateOf("13888889201") }
    var smsCode by remember { mutableStateOf("888888") }
    var agreementChecked by remember { mutableStateOf(true) }

    var isSendingCode by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(60) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape,
                    color = RosePrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Glow 登录 / 快速注册", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("登录后开启 AI 美肤智能档案与云端同步", style = MaterialTheme.typography.labelSmall, color = MutedText)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    contentColor = RosePrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("一键/社交登录", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("手机验证码", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                if (selectedTab == 0) {
                    // Social / 1-Click Login Tab
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // WeChat One-Click OAuth
                        Button(
                            onClick = {
                                if (!agreementChecked) {
                                    Toast.makeText(context, "请先勾选《用户协议》与《隐私政策》", Toast.LENGTH_SHORT).show()
                                } else {
                                    onLoginSuccess("微信美肤达人", "138****9201", "微信快捷登录")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07C160))
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("微信一键授权登录", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Alipay One-Click OAuth
                        Button(
                            onClick = {
                                if (!agreementChecked) {
                                    Toast.makeText(context, "请先勾选《用户协议》与《隐私政策》", Toast.LENGTH_SHORT).show()
                                } else {
                                    onLoginSuccess("支付宝美肤通", "159****3820", "支付宝快捷登录")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1677FF))
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("支付宝快捷登录", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Local Phone 1-Click
                        OutlinedButton(
                            onClick = {
                                if (!agreementChecked) {
                                    Toast.makeText(context, "请先勾选《用户协议》与《隐私政策》", Toast.LENGTH_SHORT).show()
                                } else {
                                    onLoginSuccess("手机号用户8839", "138****8839", "本机号码一键登录")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, RosePrimary)
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("本机号码一键登录 (138****8839)", fontWeight = FontWeight.Bold, color = RosePrimary)
                        }
                    }
                } else {
                    // SMS Code Login Tab
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("手机号码") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RosePrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = smsCode,
                                onValueChange = { smsCode = it },
                                label = { Text("短信验证码") },
                                leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = RosePrimary) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (phoneNumber.length < 11) {
                                        Toast.makeText(context, "请输入有效的11位手机号", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isSendingCode = true
                                        Toast.makeText(context, "验证码已发送至 $phoneNumber，演示验证码：888888", Toast.LENGTH_LONG).show()
                                        coroutineScope.launch {
                                            for (i in 60 downTo 1) {
                                                countdownSeconds = i
                                                delay(1000)
                                            }
                                            isSendingCode = false
                                        }
                                    }
                                },
                                enabled = !isSendingCode,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                                modifier = Modifier.height(54.dp)
                            ) {
                                Text(if (isSendingCode) "${countdownSeconds}s" else "获取验证码", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (!agreementChecked) {
                                    Toast.makeText(context, "请先勾选《用户协议》与《隐私政策》", Toast.LENGTH_SHORT).show()
                                } else if (smsCode.isBlank()) {
                                    Toast.makeText(context, "请输入验证码", Toast.LENGTH_SHORT).show()
                                } else {
                                    onLoginSuccess("美肤精选官", phoneNumber.take(3) + "****" + phoneNumber.takeLast(4), "手机号验证码登录")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                        ) {
                            Text("确认登录 / 注册", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = RoseBorder)

                // Privacy Agreement Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = agreementChecked,
                        onCheckedChange = { agreementChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = RosePrimary)
                    )
                    Text(
                        text = "我已阅读并同意《用户服务协议》和《隐私政策》",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        fontSize = 10.5.sp
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("暂不登录，先去体验", color = MutedText)
                }
            }
        }
    )
}

@Composable
fun EditProfileModalDialog(
    currentProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (nickname: String, skinGoal: String) -> Unit
) {
    var nickname by remember { mutableStateOf(currentProfile.nickname) }
    var skinGoal by remember { mutableStateOf(currentProfile.skinGoal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSave(nickname, skinGoal) },
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
            ) {
                Text("保存更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text("编辑个人美肤资料", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = skinGoal,
                    onValueChange = { skinGoal = it },
                    label = { Text("美肤改善目标") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "绑定账号：${currentProfile.phoneOrEmail} (${currentProfile.loginType})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText
                )
            }
        }
    )
}

@Composable
fun QuickServiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = RosePrimary.copy(alpha = 0.14f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = RosePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RosePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RosePrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    fontSize = 10.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun BeforeAfterCompareModal(
    records: List<SkinScanRecord>,
    onDismiss: () -> Unit
) {
    var beforeRecordIndex by remember { mutableIntStateOf(if (records.size > 1) 1 else 0) }
    var afterRecordIndex by remember { mutableIntStateOf(0) }

    val before = records.getOrNull(beforeRecordIndex) ?: records.firstOrNull()
    val after = records.getOrNull(afterRecordIndex) ?: records.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)) {
                Text("完成")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Compare, contentDescription = null, tint = RosePrimary)
                Text("肤质改善对比 (Before & After)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (records.isEmpty() || before == null || after == null) {
                Text("需要至少 1 次检测记录才能进行对比。按提示进行一次深度测肤吧！")
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("对比基准 (Before)", style = MaterialTheme.typography.labelSmall, color = MutedText)
                            val bDate = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(before.timestamp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text("${before.skinType}\n$bDate", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("最新肤质 (After)", style = MaterialTheme.typography.labelSmall, color = RosePrimary, fontWeight = FontWeight.Bold)
                            val aDate = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(after.timestamp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RosePrimary.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text("${after.skinType}\n$aDate", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = RosePrimary)
                            }
                        }
                    }

                    HorizontalDivider(color = RoseBorder)

                    val scoreDiff = after.overallScore - before.overallScore
                    val moistureDiff = after.moistureLevel - before.moistureLevel
                    val oilDiff = after.oilLevel - before.oilLevel
                    val sensitivityDiff = before.sensitivityScore - after.sensitivityScore

                    CompareRowItem("综合美肤得分", "${before.overallScore} 分", "${after.overallScore} 分", if (scoreDiff >= 0) "+$scoreDiff 分 ↗" else "$scoreDiff 分 ↘", isPositive = scoreDiff >= 0)
                    CompareRowItem("水分含水量", "${before.moistureLevel}%", "${after.moistureLevel}%", if (moistureDiff >= 0) "+$moistureDiff% 提升" else "$moistureDiff%", isPositive = moistureDiff >= 0)
                    CompareRowItem("出油发亮程度", "${before.oilLevel}%", "${after.oilLevel}%", if (oilDiff <= 0) "$oilDiff% 趋于平衡" else "+$oilDiff%", isPositive = oilDiff <= 0)
                    CompareRowItem("敏感泛红值", "${before.sensitivityScore}%", "${after.sensitivityScore}%", if (sensitivityDiff >= 0) "-$sensitivityDiff% 屏障变稳" else "+${-sensitivityDiff}%", isPositive = sensitivityDiff >= 0)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SageGreen.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SageGreen)
                            Text(
                                text = "结论：角质屏障完整度提升，泛红频率降低，水分锁水率良好！",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CompareRowItem(
    label: String,
    beforeVal: String,
    afterVal: String,
    diffText: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = "$beforeVal → $afterVal", style = MaterialTheme.typography.labelSmall, color = MutedText)
        }
        Text(
            text = diffText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) SageGreen else Color(0xFFD32F2F)
        )
    }
}

@Composable
fun IngredientMatcherModal(
    currentSkinType: String,
    onDismiss: () -> Unit
) {
    var selectedIngredient by remember { mutableStateOf<IngredientInfo?>(null) }

    val presetIngredients = listOf(
        IngredientInfo("烟酰胺 (3%)", "提亮美白 / 控油抗痘", "适合油脂分泌旺盛与暗沉肌肤，建耐受后使用", 95, true),
        IngredientInfo("积雪草苷", "修护屏障 / 舒缓泛红", "强效退红镇静，全肤质适用，敏弱肌首选", 99, true),
        IngredientInfo("透明质酸钠 (玻尿酸)", "多重深层补水锁水", "抓水锁水，补充基底水分", 98, true),
        IngredientInfo("视黄醇 (维A醇)", "抗老紧致 / 促进胶原", "晚间使用，需避光并防晒，初次需低浓度建耐受", 88, false),
        IngredientInfo("水杨酸 (2%)", "疏通毛孔 / 溶解黑头", "脂溶性酸，干敏肌局部T区湿敷，不可全脸频繁使用", 78, false),
        IngredientInfo("变性乙醇 (酒精)", "挥发清爽 / 促进吸收", "⚠️ 敏感肌与极干肌建议避开，易破坏角质脂质", 35, false),
        IngredientInfo("神经酰胺 NP", "补充角质间脂质", "强化细胞间质，修复泛红皮脂膜", 97, true),
        IngredientInfo("传明酸 (凝血酸)", "抑制黑色素 / 淡斑", "温和不刺激，可与烟酰胺复配淡化痘印", 94, true)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)) {
                Text("关闭")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Science, contentDescription = null, tint = RosePrimary)
                Text("护肤品成分安全与适配匹配", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "针对您当前的“$currentSkinType”进行成分安全比对：",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetIngredients) { item ->
                        FilterChip(
                            selected = selectedIngredient == item,
                            onClick = { selectedIngredient = item },
                            label = { Text(item.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RosePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                selectedIngredient?.let { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isSafe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color(0xFFFFEBEE)
                        ),
                        border = BorderStroke(1.dp, if (item.isSafe) RoseBorder else Color(0xFFFFCDD2))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (item.isSafe) SageGreen else Color(0xFFD32F2F)
                                ) {
                                    Text(
                                        text = "匹配度 ${item.matchScore}%",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "核心功效：${item.effect}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(text = item.advice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("点击上方成分，查看对于您肤质的安全性与使用建议", style = MaterialTheme.typography.labelSmall, color = MutedText)
                    }
                }
            }
        }
    )
}

private data class IngredientInfo(
    val name: String,
    val effect: String,
    val advice: String,
    val matchScore: Int,
    val isSafe: Boolean
)

@Composable
fun SkinTrendLineCanvas(records: List<SkinScanRecord>) {
    val displayRecords = records.take(6).reversed()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        if (displayRecords.size < 2) return@Canvas

        val maxVal = 100f
        val stepX = width / (displayRecords.size - 1)

        val scorePoints = displayRecords.mapIndexed { idx, rec ->
            val x = idx * stepX
            val y = height - (rec.overallScore.toFloat() / maxVal * height)
            Offset(x, y)
        }

        val moisturePoints = displayRecords.mapIndexed { idx, rec ->
            val x = idx * stepX
            val y = height - (rec.moistureLevel.toFloat() / maxVal * height)
            Offset(x, y)
        }

        val scorePath = Path().apply {
            moveTo(scorePoints.first().x, scorePoints.first().y)
            for (i in 1 until scorePoints.size) {
                lineTo(scorePoints[i].x, scorePoints[i].y)
            }
        }
        drawPath(
            path = scorePath,
            color = RosePrimary,
            style = Stroke(width = 3.dp.toPx())
        )

        val moisturePath = Path().apply {
            moveTo(moisturePoints.first().x, moisturePoints.first().y)
            for (i in 1 until moisturePoints.size) {
                lineTo(moisturePoints[i].x, moisturePoints[i].y)
            }
        }
        drawPath(
            path = moisturePath,
            color = Color(0xFF0288D1),
            style = Stroke(width = 2.dp.toPx())
        )

        scorePoints.forEach { pt ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
            drawCircle(color = RosePrimary, radius = 3.5.dp.toPx(), center = pt)
        }

        moisturePoints.forEach { pt ->
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFF0288D1), radius = 2.5.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RosePrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedText)
    }
}

@Composable
fun HistoryRecordCard(record: SkinScanRecord) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = MutedText)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = record.skinType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RosePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "综合得分：${record.overallScore} 分", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = "水分:${record.moistureLevel}%  油脂:${record.oilLevel}%  敏感:${record.sensitivityScore}%", style = MaterialTheme.typography.labelSmall, color = MutedText)
            }

            val concernsList = record.primaryConcerns.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (concernsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    concernsList.forEach { concern ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = concern,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
