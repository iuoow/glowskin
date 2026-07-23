package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.screens.CameraScreen
import com.example.ui.theme.GlowSkinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CameraScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cameraScreen_displaysPermissionNoticeWhenDenied() {
        composeTestRule.setContent {
            GlowSkinTheme {
                CameraScreen(
                    onPhotoCaptured = {},
                    onClose = {}
                )
            }
        }

        // Verify permission header is displayed when permission is not granted by default in test environment
        composeTestRule.onNodeWithText("需要相机权限").assertIsDisplayed()
        composeTestRule.onNodeWithText("授权使用相机").assertIsDisplayed()
    }
}
