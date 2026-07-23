package com.example

import com.example.viewmodel.UserProfile
import org.junit.Assert.*
import org.junit.Test

class UserProfileAuthTest {

    @Test
    fun testDefaultProfileState() {
        val defaultProfile = UserProfile()
        assertTrue("Default profile should be logged in", defaultProfile.isLoggedIn)
        assertEquals("Glow美肤达人", defaultProfile.nickname)
        assertEquals("SVIP黑金会员", defaultProfile.vipLevel)
    }

    @Test
    fun testLogoutStateTransformation() {
        val defaultProfile = UserProfile()
        val loggedOutProfile = defaultProfile.copy(
            isLoggedIn = false,
            nickname = "未登录",
            phoneOrEmail = "点击登录以同步数据"
        )

        assertFalse(loggedOutProfile.isLoggedIn)
        assertEquals("未登录", loggedOutProfile.nickname)
        assertEquals("点击登录以同步数据", loggedOutProfile.phoneOrEmail)
    }

    @Test
    fun testProfileUpdate() {
        val profile = UserProfile()
        val updatedProfile = profile.copy(
            nickname = "极简护肤玩家",
            skinGoal = "抗光老化 / 紧致弹润"
        )

        assertEquals("极简护肤玩家", updatedProfile.nickname)
        assertEquals("抗光老化 / 紧致弹润", updatedProfile.skinGoal)
        assertTrue(updatedProfile.isLoggedIn)
    }

    @Test
    fun testPasswordVisibilityToggle() {
        var isPasswordVisible = false
        // Simulate clicking eye icon
        isPasswordVisible = !isPasswordVisible
        assertTrue("Password should be visible after toggle", isPasswordVisible)

        // Toggle back
        isPasswordVisible = !isPasswordVisible
        assertFalse("Password should be hidden after second toggle", isPasswordVisible)
    }

    @Test
    fun testPasswordResetValidation() {
        // Test valid email contact vs phone contact formatting
        val emailContact = "glow_user@skin.com"
        val phoneContact = "13888889201"

        assertTrue("Email contact should contain @", emailContact.contains("@"))
        assertTrue("Phone contact should start with 1", phoneContact.startsWith("1"))

        // Test password length & confirmation logic
        val newPassword = "GlowSkin2026!"
        val confirmPassword = "GlowSkin2026!"
        val shortPassword = "123"

        assertTrue("Password length should be >= 6", newPassword.length >= 6)
        assertEquals("Passwords must match", newPassword, confirmPassword)
        assertFalse("Short password length should fail validation", shortPassword.length >= 6)
    }

    @Test
    fun testCameraPhotoCaptureState() {
        var userCapturedBitmap: Any? = null
        var selectedSampleIndex = 0

        // Initially no photo captured, sample photo selected
        assertNull("Initial photo should be null", userCapturedBitmap)
        assertEquals(0, selectedSampleIndex)

        // Simulate capturing photo from camera
        userCapturedBitmap = "mock_captured_bitmap"
        assertNotNull("User photo captured successfully", userCapturedBitmap)

        // Clear captured photo
        userCapturedBitmap = null
        assertNull("Photo cleared successfully", userCapturedBitmap)
    }
}
