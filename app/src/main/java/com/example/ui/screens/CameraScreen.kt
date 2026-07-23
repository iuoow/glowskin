package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.FlashAuto
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.RosePrimary
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onPhotoCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "需要相机权限以进行实时肤质拍照", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "需要相机权限",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "GlowSkin 需要访问您的相机，用于实时采集高清皮肤状况并分析各种肤质特征。",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("授权使用相机")
                }
                TextButton(onClick = onClose) {
                    Text("取消并返回", color = Color.White)
                }
            }
        }
    } else {
        CameraPreviewContent(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onPhotoCaptured = onPhotoCaptured,
            onClose = onClose,
            modifier = modifier
        )
    }
}

@Composable
private fun CameraPreviewContent(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onPhotoCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var isCapturing by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()
    }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing, flashMode) {
        imageCapture.flashMode = flashMode
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraScreen", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Real-time Camera Preview View
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Face Frame Overlay Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val ovalWidth = canvasWidth * 0.72f
            val ovalHeight = canvasHeight * 0.48f
            val left = (canvasWidth - ovalWidth) / 2
            val top = (canvasHeight - ovalHeight) / 2.6f

            // Semi-transparent dim background around frame
            drawRect(
                color = Color.Black.copy(alpha = 0.45f)
            )

            // Clear oval hole guide for face alignment
            drawOval(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(ovalWidth, ovalHeight),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            // Dashed oval guide line
            drawOval(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(left, top),
                size = Size(ovalWidth, ovalHeight),
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
                )
            )
        }

        // Top Bar Controls (Close, Flash Toggle, Front/Back Switch)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("camera_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭相机",
                    tint = Color.White
                )
            }

            Text(
                text = "实时肤质拍摄",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Flash Mode Toggle
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Outlined.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Outlined.FlashAuto
                            else -> Icons.Outlined.FlashOff
                        },
                        contentDescription = "闪光灯模式",
                        tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) RosePrimary else Color.White
                    )
                }

                // Lens Switch Button
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("camera_switch_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cameraswitch,
                        contentDescription = "切换前后摄像头",
                        tint = Color.White
                    )
                }
            }
        }

        // Center Alignment Tip Notice
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "请将面部对准虚线框，保证光线均匀",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom Capture Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(enabled = !isCapturing) {
                        isCapturing = true
                        takePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            lensFacing = lensFacing,
                            onPhotoCaptured = { bitmap ->
                                isCapturing = false
                                onPhotoCaptured(bitmap)
                            },
                            onError = { exception ->
                                isCapturing = false
                                Toast
                                    .makeText(
                                        context,
                                        "拍照失败: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        )
                    }
                    .testTag("camera_shutter_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = RosePrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    lensFacing: Int,
    onPhotoCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                try {
                    val bitmap = imageProxy.toBitmap()
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                    val matrix = Matrix().apply {
                        postRotate(rotationDegrees.toFloat())
                        if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            // Mirror front camera picture horizontally
                            postScale(-1f, 1f)
                        }
                    }

                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    )

                    imageProxy.close()
                    onPhotoCaptured(rotatedBitmap)
                } catch (e: Exception) {
                    imageProxy.close()
                    Log.e("CameraScreen", "Error converting image proxy to bitmap", e)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
