package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAndAdviceScreen() {
    var selectedRoutineTab by remember { mutableIntStateOf(0) } // 0 = Morning, 1 = Evening, 2 = Ingredients, 3 = Lifestyle

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "肤质改善与护肤指南",
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
            SecondaryTabRow(
                selectedTabIndex = selectedRoutineTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = RosePrimary
            ) {
                Tab(
                    selected = selectedRoutineTab == 0,
                    onClick = { selectedRoutineTab = 0 },
                    text = { Text("☀️ 早晨流程", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedRoutineTab == 1,
                    onClick = { selectedRoutineTab = 1 },
                    text = { Text("🌙 晚间修复", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedRoutineTab == 2,
                    onClick = { selectedRoutineTab = 2 },
                    text = { Text("🧪 成分避坑", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedRoutineTab == 3,
                    onClick = { selectedRoutineTab = 3 },
                    text = { Text("🥗 饮食作息", fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedRoutineTab) {
                    0 -> {
                        item {
                            RoutineHeaderCard(
                                title = "早晨护肤：防守与防护",
                                subtitle = "核心目标：温和清洁 + 锁水保湿 + 全广谱防晒，防御紫外线与空气污染。"
                            )
                        }
                        items(getMorningSteps()) { step ->
                            RoutineStepCard(step)
                        }
                    }
                    1 -> {
                        item {
                            RoutineHeaderCard(
                                title = "晚间护肤：深度修护与皮脂膜充盈",
                                subtitle = "核心目标：卸除彩妆污垢 + 补充角质层脂质 + 促进夜间自我修复。"
                            )
                        }
                        items(getEveningSteps()) { step ->
                            RoutineStepCard(step)
                        }
                    }
                    2 -> {
                        item {
                            IngredientsGuideCard()
                        }
                    }
                    3 -> {
                        item {
                            LifestyleGuideCard()
                        }
                    }
                }
            }
        }
    }
}

data class SkincareStep(
    val stepIndex: Int,
    val category: String,
    val name: String,
    val timing: String,
    val details: String,
    val keyIngredients: String
)

private fun getMorningSteps(): List<SkincareStep> = listOf(
    SkincareStep(
        stepIndex = 1,
        category = "洁面",
        name = "温和低泡氨基酸洁面",
        timing = "7:30 AM",
        details = "早晨面部油脂分泌较少，使用温水洗脸或轻微低泡氨基酸洁面，避免过度去油脂破坏皮脂膜。",
        keyIngredients = "椰油酰甘氨酸钾、苹果氨基酸"
    ),
    SkincareStep(
        stepIndex = 2,
        category = "爽肤/喷雾",
        name = "积雪草舒缓保湿水",
        timing = "7:35 AM",
        details = "按压或拍打于全脸，快速补充角质层水分，舒缓夜间睡眠水分蒸发导致的干紧感。",
        keyIngredients = "积雪草苷、依克多因、马齿苋提取物"
    ),
    SkincareStep(
        stepIndex = 3,
        category = "精华",
        name = "抗氧化/玻尿酸补水精华",
        timing = "7:38 AM",
        details = "滴取2-3滴涂抹面部，预先中和白天紫外线产生的自由基，增强肌肤抗光老屏障。",
        keyIngredients = "3-O-乙基抗坏血酸(维C衍生物)、透明质酸"
    ),
    SkincareStep(
        stepIndex = 4,
        category = "乳液/面霜",
        name = "神经酰胺轻薄修护乳",
        timing = "7:42 AM",
        details = "掌心稍微揉开后轻轻按压吸收，形成天然屏障保护膜，牢牢锁住精华水分。",
        keyIngredients = "神经酰胺NP、角鲨烷、植物甾醇"
    ),
    SkincareStep(
        stepIndex = 5,
        category = "防晒",
        name = "广谱纯物理防晒霜 (SPF50+)",
        timing = "7:45 AM (出门前15分钟)",
        details = "用量约为一元硬币大小，顺着皮肤纹理单向推开成膜，切忌来回搓揉导致起皮。",
        keyIngredients = "二氧化钛、氧化锌、甘草酸二钾"
    )
)

private fun getEveningSteps(): List<SkincareStep> = listOf(
    SkincareStep(
        stepIndex = 1,
        category = "卸妆",
        name = "植萃温和卸妆油/膏",
        timing = "8:30 PM",
        details = "干手干脸按压均匀揉开，遇水充分乳化后用温水冲洗干净，彻底卸除防晒霜与彩妆。",
        keyIngredients = "荷荷巴油、甜杏仁油、天然乳化剂"
    ),
    SkincareStep(
        stepIndex = 2,
        category = "洁面",
        name = "弱酸性氨基酸洁面乳",
        timing = "8:35 PM",
        details = "打出丰富细腻泡沫后轻轻按摩面部30秒，水温控制在32-35℃，不要使用搓澡巾或洗脸仪。",
        keyIngredients = "月桂酰谷氨酸钠、B5泛醇"
    ),
    SkincareStep(
        stepIndex = 3,
        category = "修护精华",
        name = "维B5高能屏障修复精华",
        timing = "8:40 PM",
        details = "重点涂抹于颊部泛红区或干燥区域，促进夜间上皮细胞自我修复与胶原合成。",
        keyIngredients = "维B5(5%泛醇)、依克多因、β-葡聚糖"
    ),
    SkincareStep(
        stepIndex = 4,
        category = "晚霜/修护面霜",
        name = "多肽角质层修护面霜",
        timing = "8:45 PM",
        details = "晚间充当厚敷修护面霜，密集锁存水份与营养，翌日晨起肌肤焕发自然高光。",
        keyIngredients = "棕榈酰三肽-5、重组胶原蛋白、神经酰胺"
    )
)

@Composable
fun RoutineHeaderCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RosePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun RoutineStepCard(step: SkincareStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RoseBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = RosePrimary,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${step.stepIndex}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = step.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = step.timing,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = step.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = step.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "💡 推荐核心成分：${step.keyIngredients}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RosePrimary
                )
            }
        }
    }
}

@Composable
fun IngredientsGuideCard() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Red Ingredients (Avoid)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CoralDanger.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CoralDanger)
                    Text(text = "敏感肌/屏障薄建议避雷成分", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CoralDanger)
                }
                Spacer(modifier = Modifier.height(12.dp))

                IngredientDetailItem("变性乙醇 (酒精)", "挥发带走皮肤水分，长期使用削弱皮脂膜，加重泛红刺痛。")
                IngredientDetailItem("高浓度高活性纯维A醇", "刺激性极强，未经建立耐受直接使用易引发爆痘、脱皮、烧灼感。")
                IngredientDetailItem("高浓度水杨酸 / 纯果酸", "剥脱角质层过猛，敏感肌刷酸极易导致角质层进一步变薄受损。")
                IngredientDetailItem("强效合成香精与异噻唑啉酮防腐剂", "常见接触性过敏原，易诱发红斑与痒感。")
            }
        }

        // Green Ingredients (Recommend)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SageGreen)
                    Text(text = "优先推荐的高能修护成分", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SageGreen)
                }
                Spacer(modifier = Modifier.height(12.dp))

                IngredientDetailItem("积雪草苷 (Centella)", "源于天然植萃，卓越褪红止痒，促进受损上皮快速愈合。")
                IngredientDetailItem("神经酰胺 NP / EOP", "模拟人体皮肤细胞间质，直击屏障缝隙，补充天然脂质。")
                IngredientDetailItem("维B5 (泛醇 Panthenol)", "深度渗透角质层，强效保湿锁水，加速组织再生。")
                IngredientDetailItem("依克多因 (Ectoin)", "极地极端极端微生物防护因子，抵御光老化与环境污染刺激。")
            }
        }
    }
}

@Composable
fun IngredientDetailItem(name: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "• $name", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MutedText, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
fun LifestyleGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, RoseBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🥗 女性内调与美肤作息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            HabitItem("睡够美容觉", "晚间10点至次日2点为生长激素与皮肤自噬修护高峰期，保持7.5小时睡眠。")
            HabitItem("抗糖化与抗氧化饮食", "减少高升糖(GI)奶茶甜食，多食用蓝莓、番茄、黑巧与绿茶。")
            HabitItem("防晒习惯常态化", "即便阴天紫外线穿透力依然强，户外出游随时戴遮阳帽与防晒墨镜。")
            HabitItem("避免频繁去角质", "摒弃磨砂膏与去角质啫喱，皮肤自身角质代换周期约为28天。")
        }
    }
}

@Composable
fun HabitItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = RosePrimary, modifier = Modifier.size(20.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
    }
}
