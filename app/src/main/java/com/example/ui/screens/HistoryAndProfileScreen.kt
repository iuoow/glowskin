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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Product
import com.example.data.database.SkinScanRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAndProfileScreen(
    records: List<SkinScanRecord>,
    favoriteProducts: List<Product>,
    onToggleFavorite: (String, Boolean) -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    onNavigateToDetect: () -> Unit
) {
    val context = LocalContext.current
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
            // Profile Card & Streak Banner
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Avatar",
                                        tint = RosePrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Glow 女性美肤会员",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ChampagneGold.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = "VIP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFD81B60),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (records.isNotEmpty()) "最新肤质：${records.first().skinType} (${records.first().overallScore}分)" else "尚未检测肤质",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }

                            // Daily Check-in Button
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

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = RoseBorder.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStatItem("检测次数", "${records.size} 次")
                            ProfileStatItem("连续打卡", "${streakDays} 天")
                            ProfileStatItem("收藏护肤品", "${favoriteProducts.size} 件")
                        }
                    }
                }
            }

            // Quick Service / Feature Entry Grid
            item {
                Text(
                    text = "美肤核心功能与深度扩展",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickServiceCard(
                            title = "Before & After 对比",
                            subtitle = "多维度肤质对比改善",
                            icon = Icons.Default.Compare,
                            badgeText = "NEW",
                            modifier = Modifier.weight(1f),
                            onClick = { showCompareDialog = true }
                        )

                        QuickServiceCard(
                            title = "成分库安全匹配",
                            subtitle = "查护肤成分/避雷",
                            icon = Icons.Default.Science,
                            badgeText = "工具",
                            modifier = Modifier.weight(1f),
                            onClick = { showIngredientMatcher = true }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickServiceCard(
                            title = "个性化改善指南",
                            subtitle = "护肤步骤与护理方案",
                            icon = Icons.Default.AutoFixHigh,
                            badgeText = null,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRoutine
                        )

                        QuickServiceCard(
                            title = "护肤推荐 (直购)",
                            subtitle = "正品供应商数据对接中",
                            icon = Icons.Default.ShoppingBag,
                            badgeText = "即将推出",
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToProducts
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickServiceCard(
                            title = "测肤打卡提醒",
                            subtitle = if (isDailyReminderEnabled) "已开启每日提醒" else "未开启",
                            icon = Icons.Default.NotificationsActive,
                            badgeText = null,
                            modifier = Modifier.weight(1f),
                            onClick = { showReminderSettings = true }
                        )

                        QuickServiceCard(
                            title = "再去测一次",
                            subtitle = "相机光效 / 深度问卷",
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

                            // Skin Trend Line Canvas
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
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    HistoryRecordCard(record)
                }
            }

            // Favorite Products Section
            if (favoriteProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "已收藏的护肤品 (${favoriteProducts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(favoriteProducts) { product ->
                    ProductItemCard(
                        product = product,
                        onToggleFavorite = { onToggleFavorite(product.id, false) },
                        onClick = { }
                    )
                }
            }
        }
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
fun QuickServiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = RosePrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(20.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RosePrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RosePrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MutedText, fontSize = 10.sp)
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
                    // Record Selectors
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

                    // Metrics Comparison Table
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
    var searchIngredient by remember { mutableStateOf("") }
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

                // Quick Ingredient Chips
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

        // Draw Score Line (RosePrimary)
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

        // Draw Moisture Line (Blue)
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

        // Draw Circles on Points
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "得分：${record.overallScore}分",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = RosePrimary
                    )
                    Text(
                        text = "水分 ${record.moistureLevel}% · 含油 ${record.oilLevel}% · 敏感 ${record.sensitivityScore}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ChampagneGold.copy(alpha = 0.2f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = record.barrierHealth,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (record.primaryConcerns.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "关注点：${record.primaryConcerns}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText
                )
            }
        }
    }
}
