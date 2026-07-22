package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.remote.AiSkinAnalysisResult
import com.example.ui.components.SkinAnalysisDataVisualizationCard
import com.example.ui.theme.GlowSkinTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun skin_analysis_chart_screenshot() {
    val sampleResult = AiSkinAnalysisResult(
      skinType = "混合偏干性",
      overallScore = 85,
      moistureLevel = 68,
      oilLevel = 45,
      textureLevel = 82,
      sensitivityScore = 20,
      acneScore = 15,
      darkSpotScore = 25,
      barrierHealth = "健康",
      primaryConcerns = listOf("干燥少水", "微观角质粗糙"),
      analysisSummary = "肤质健康状况良好，水油相对平衡。",
      recommendedIngredients = listOf("透明质酸", "神经酰胺"),
      avoidedIngredients = listOf("高浓度酒精"),
      morningRoutineSteps = listOf("温和洁面", "保湿水"),
      eveningRoutineSteps = listOf("卸妆洁面", "修护晚霜"),
      dietAndLifestyleTips = listOf("多饮水，少吃高糖食品")
    )

    composeTestRule.setContent {
      GlowSkinTheme {
        SkinAnalysisDataVisualizationCard(result = sampleResult)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/skin_analysis_chart.png")
  }
}

