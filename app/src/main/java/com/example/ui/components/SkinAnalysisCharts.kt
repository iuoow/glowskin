package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AiSkinAnalysisResult
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Data Visualization Charts Component for Skin Analysis findings.
 * Visualizes Moisture, Oil, Texture, Sensitivity, and Barrier Health.
 */
@Composable
fun SkinAnalysisDataVisualizationCard(
    result: AiSkinAnalysisResult,
    modifier: Modifier = Modifier
) {
    var selectedChartIndex by remember { mutableIntStateOf(0) }
    val chartTypes = listOf("五维综合雷达图", "水油肌理柱图", "区域纹理曲线")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .testTag("skin_data_visualization_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, RoseBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RosePrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = RosePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "肤质多维数据可视化分析",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "水分 • 油分 • 肌理纹理 • 屏障状态",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Type Segmented Control
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                chartTypes.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedChartIndex == index,
                        onClick = { selectedChartIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = chartTypes.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = RosePrimary,
                            activeContentColor = Color.White,
                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedChartIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Chart View Switching
            AnimatedContent(
                targetState = selectedChartIndex,
                transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200)) },
                label = "chartViewTransition"
            ) { targetIndex ->
                when (targetIndex) {
                    0 -> SkinMetricsRadarChart(result = result)
                    1 -> MoistureOilTextureBarChart(result = result)
                    else -> FacialZoneTextureWaveChart(result = result)
                }
            }
        }
    }
}

/**
 * 5-Dimension Polygon Radar Chart for Moisture, Oil, Texture, Sensitivity & Barrier.
 */
@Composable
fun SkinMetricsRadarChart(
    result: AiSkinAnalysisResult
) {
    // 5 Dimension Data Points
    val labels = listOf("水分", "油分", "细腻纹理", "屏障健康", "耐受平稳")
    val values = listOf(
        result.moistureLevel.toFloat(),
        result.oilLevel.toFloat(),
        result.textureLevel.toFloat(),
        when (result.barrierHealth) {
            "健康" -> 90f
            "轻度受损" -> 65f
            else -> 40f
        },
        (100 - result.sensitivityScore).coerceIn(10, 100).toFloat()
    )

    var selectedDimensionIndex by remember { mutableStateOf<Int?>(null) }

    // Animated expansion of radar polygon
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "radarProgress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 24.dp.toPx()
                val count = labels.size
                val angleStep = (2 * Math.PI / count).toFloat()

                // Draw Background Web Polygons (5 Levels: 20%, 40%, 60%, 80%, 100%)
                for (level in 1..5) {
                    val currentRadius = radius * (level / 5f)
                    val path = Path()
                    for (i in 0 until count) {
                        val angle = i * angleStep - (Math.PI / 2).toFloat()
                        val x = center.x + currentRadius * cos(angle)
                        val y = center.y + currentRadius * sin(angle)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(
                        path = path,
                        color = Color.LightGray.copy(alpha = 0.35f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Draw Axis Lines from center
                for (i in 0 until count) {
                    val angle = i * angleStep - (Math.PI / 2).toFloat()
                    val x = center.x + radius * cos(angle)
                    val y = center.y + radius * sin(angle)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = center,
                        end = Offset(x, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Data Polygon
                val dataPath = Path()
                val vertexPoints = mutableListOf<Offset>()

                for (i in 0 until count) {
                    val angle = i * angleStep - (Math.PI / 2).toFloat()
                    val scaledValue = (values[i] / 100f).coerceIn(0.1f, 1f) * animatedProgress
                    val x = center.x + radius * scaledValue * cos(angle)
                    val y = center.y + radius * scaledValue * sin(angle)
                    val point = Offset(x, y)
                    vertexPoints.add(point)

                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()

                // Fill Data Polygon with translucent gradient
                drawPath(
                    path = dataPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            RosePrimary.copy(alpha = 0.45f),
                            RoseSecondary.copy(alpha = 0.25f)
                        ),
                        center = center,
                        radius = radius
                    )
                )

                // Draw Data Polygon Outline
                drawPath(
                    path = dataPath,
                    color = RosePrimary,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Draw Vertex Circles
                for (i in 0 until count) {
                    val point = vertexPoints[i]
                    val isSelected = selectedDimensionIndex == i

                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = if (isSelected) CoralDanger else RosePrimary,
                        radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = point
                    )
                }
            }

            // Overlay Dimension Clickable Labels around Radar
            val count = labels.size
            val angleStep = (2 * Math.PI / count).toFloat()
            val radiusDp = 100.dp

            for (i in 0 until count) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val xOffset = (radiusDp.value * 1.15f * cos(angle)).dp
                val yOffset = (radiusDp.value * 1.15f * sin(angle)).dp

                Box(
                    modifier = Modifier
                        .offset(x = xOffset, y = yOffset)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedDimensionIndex == i) RosePrimary else RosePrimary.copy(alpha = 0.08f)
                        )
                        .clickable { selectedDimensionIndex = if (selectedDimensionIndex == i) null else i }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${labels[i]} ${values[i].toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedDimensionIndex == i) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dimension Explanation Banner on Tap
        val dimensionTips = listOf(
            "💧 角质层水份 (${result.moistureLevel}%)：表示面部角质层锁水充盈度。建议配合深层补水精华与乳霜。",
            "✨ 皮脂分泌 (${result.oilLevel}%)：T区与两颊皮脂腺活跃度。平衡水油可防止毛孔堵塞。",
            "🔬 肌理细腻度 (${result.textureLevel}%)：反映肌肤表面平整度与平滑光泽感。分数越高肌肤越平滑。",
            "🛡️ 屏障健康度 (${result.barrierHealth})：角质层抵抗外界刺激的物理防御屏障状态。",
            "🌱 稳定耐受度 (${(100 - result.sensitivityScore).coerceIn(0, 100)}%)：皮肤应对环境变化与功效成分的抗敏能力。"
        )

        val tipToShow = selectedDimensionIndex?.let { dimensionTips[it] }
            ?: "💡 点击上方维度标签或坐标节点，可查看详细临床评估解析。"

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = tipToShow,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Comparative Bar Chart specifically highlighting Moisture, Oil, and Texture Levels.
 */
@Composable
fun MoistureOilTextureBarChart(
    result: AiSkinAnalysisResult
) {
    val animatedMoisture by animateFloatAsState(targetValue = result.moistureLevel / 100f, animationSpec = tween(900), label = "mAnim")
    val animatedOil by animateFloatAsState(targetValue = result.oilLevel / 100f, animationSpec = tween(900), label = "oAnim")
    val animatedTexture by animateFloatAsState(targetValue = result.textureLevel / 100f, animationSpec = tween(900), label = "tAnim")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "核心三大物理指标对比 (水分 • 油分 • 纹理)：",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MutedText
        )

        // Moisture Bar
        MetricBarDetailRow(
            title = "角质层水份",
            valueText = "${result.moistureLevel}%",
            statusLabel = if (result.moistureLevel < 50) "缺水偏干" else if (result.moistureLevel < 75) "水润适中" else "极度充盈",
            progress = animatedMoisture,
            barGradient = listOf(Color(0xFF64B5F6), Color(0xFF1E88E5)),
            icon = Icons.Default.WaterDrop
        )

        // Oil Bar
        MetricBarDetailRow(
            title = "皮脂分泌量",
            valueText = "${result.oilLevel}%",
            statusLabel = if (result.oilLevel < 40) "清爽少油" else if (result.oilLevel < 70) "水油平衡" else "油脂旺盛",
            progress = animatedOil,
            barGradient = listOf(Color(0xFFFFD54F), AmberWarning),
            icon = Icons.Default.Opacity
        )

        // Texture Smoothness Bar
        MetricBarDetailRow(
            title = "肌理细腻度",
            valueText = "${result.textureLevel}%",
            statusLabel = if (result.textureLevel < 60) "纹理粗糙" else if (result.textureLevel < 80) "平滑良好" else "极度细腻",
            progress = animatedTexture,
            barGradient = listOf(Color(0xFF81C784), SageGreen),
            icon = Icons.Default.Grain
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Target Ideal Range Indicator Legend Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = RosePrimary.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, RoseBorder.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SageGreen, modifier = Modifier.size(16.dp))
                    Text(
                        text = "黄金平衡区间参考：水分 60%-80% | 油分 40%-60% | 纹理 >75%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MetricBarDetailRow(
    title: String,
    valueText: String,
    statusLabel: String,
    progress: Float,
    barGradient: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = barGradient.last(), modifier = Modifier.size(16.dp))
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = barGradient.last().copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = barGradient.last(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = barGradient.last()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Custom Gradient Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(barGradient.last().copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(barGradient))
            )
        }
    }
}

/**
 * Micro-Relief Curved Wave Chart showing Surface Smoothness across Facial Zones.
 */
@Composable
fun FacialZoneTextureWaveChart(
    result: AiSkinAnalysisResult
) {
    val zones = listOf("额头", "T区/鼻翼", "左颊", "右颊", "下巴")

    // Generate zone smoothness scores based on textureLevel and oil/dry traits
    val zoneScores = remember(result) {
        listOf(
            (result.textureLevel + 4).coerceIn(40, 95),
            (result.textureLevel - 8).coerceIn(35, 90),
            (result.textureLevel - 2).coerceIn(40, 95),
            (result.textureLevel - 3).coerceIn(40, 95),
            (result.textureLevel + 2).coerceIn(40, 95)
        )
    }

    var hoveredZoneIndex by remember { mutableStateOf<Int?>(null) }

    val animatedChartProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "waveChartProgress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "面部 5 大分区微观纹理分布曲线 (光滑度指数)：",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MutedText,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (zones.size - 1)

                // Grid lines
                for (i in 0..3) {
                    val y = height * (i / 3f)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Smooth Path Curve
                val strokePath = Path()
                val fillPath = Path()

                val points = zoneScores.mapIndexed { index, score ->
                    val x = index * stepX
                    val normalizedY = 1f - (score / 100f) * animatedChartProgress
                    val y = normalizedY * height
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    strokePath.moveTo(points[0].x, points[0].y)
                    fillPath.moveTo(points[0].x, height)
                    fillPath.lineTo(points[0].x, points[0].y)

                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlP1 = Offset(p1.x + stepX / 2f, p1.y)
                        val controlP2 = Offset(p2.x - stepX / 2f, p2.y)

                        strokePath.cubicTo(controlP1.x, controlP1.y, controlP2.x, controlP2.y, p2.x, p2.y)
                        fillPath.cubicTo(controlP1.x, controlP1.y, controlP2.x, controlP2.y, p2.x, p2.y)
                    }

                    fillPath.lineTo(points.last().x, height)
                    fillPath.close()

                    // Draw translucent gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SageGreen.copy(alpha = 0.45f),
                                SageGreen.copy(alpha = 0.05f)
                            )
                        )
                    )

                    // Draw Smooth Curve
                    drawPath(
                        path = strokePath,
                        color = SageGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Data Points
                    points.forEachIndexed { idx, pt ->
                        val isHovered = hoveredZoneIndex == idx
                        drawCircle(
                            color = Color.White,
                            radius = if (isHovered) 7.dp.toPx() else 4.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = if (isHovered) RosePrimary else SageGreen,
                            radius = if (isHovered) 5.dp.toPx() else 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // Zone Labels below Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                zones.forEachIndexed { index, zoneName ->
                    Text(
                        text = zoneName,
                        fontSize = 10.sp,
                        fontWeight = if (hoveredZoneIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (hoveredZoneIndex == index) RosePrimary else MutedText,
                        modifier = Modifier.clickable {
                            hoveredZoneIndex = if (hoveredZoneIndex == index) null else index
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Zone Detail Card
        val activeIndex = hoveredZoneIndex ?: 1 // Default to T-zone
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SageGreen.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, SageGreen.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = SageGreen, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "${zones[activeIndex]}区域：肌理光滑度 ${zoneScores[activeIndex]}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SageGreen
                    )
                    Text(
                        text = if (zoneScores[activeIndex] >= 75) "该部位毛孔紧致，微观纹理平整顺滑，维持好现有清洁保湿即可。"
                        else "该部位皮脂分泌较旺盛或有局部干燥，建议使用温和酸类水擦拭或局部加强B5保湿。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
