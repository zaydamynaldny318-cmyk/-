package com.example.ui.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Resolves the appropriate system permission based on Android SDK level:
 * - Android 13+ (API 33+): Manifest.permission.READ_MEDIA_VIDEO
 * - Android 12 and below: Manifest.permission.READ_EXTERNAL_STORAGE
 */
fun getVideoReadPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

/**
 * State holder for video permission handling using Accompanist.
 */
@OptIn(ExperimentalPermissionsApi::class)
class VideoPermissionController(
    val permissionState: PermissionState,
    private val context: Context,
    private val onActionApproved: () -> Unit
) {
    var showRationaleDialog by mutableStateOf(false)
        private set

    var showSettingsDialog by mutableStateOf(false)
        private set

    val isGranted: Boolean
        get() = permissionState.status.isGranted

    /**
     * Executes the target action if permission is already granted;
     * otherwise triggers Accompanist permission request or displays rationale dialog.
     */
    fun checkAndRequestPermission() {
        when {
            permissionState.status.isGranted -> {
                onActionApproved()
            }
            permissionState.status.shouldShowRationale -> {
                showRationaleDialog = true
            }
            else -> {
                // First request or previously denied with Don't Ask Again
                permissionState.launchPermissionRequest()
            }
        }
    }

    fun onRationaleDismiss() {
        showRationaleDialog = false
    }

    fun onRationaleConfirm() {
        showRationaleDialog = false
        permissionState.launchPermissionRequest()
    }

    fun onOpenAppSettings() {
        showSettingsDialog = false
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun onSettingsDismiss() {
        showSettingsDialog = false
    }
}

/**
 * Composable helper that remembers and provides a [VideoPermissionController]
 * backed by Accompanist rememberPermissionState.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberVideoPermissionController(
    onPermissionGranted: () -> Unit
): VideoPermissionController {
    val context = LocalContext.current
    val permissionToRequest = remember { getVideoReadPermission() }

    val permissionState = rememberPermissionState(
        permission = permissionToRequest
    ) { granted ->
        if (granted) {
            onPermissionGranted()
        }
    }

    return remember(permissionState, context) {
        VideoPermissionController(
            permissionState = permissionState,
            context = context,
            onActionApproved = onPermissionGranted
        )
    }
}

/**
 * Rationale & Settings Dialog for READ_MEDIA_VIDEO permission with M3 Dark Studio styling.
 */
@Composable
fun VideoPermissionDialogs(
    controller: VideoPermissionController
) {
    if (controller.showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { controller.onRationaleDismiss() },
            modifier = Modifier.testTag("video_permission_rationale_dialog"),
            containerColor = StudioNavySurface,
            icon = {
                Surface(
                    color = CyberCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Permission Rationale",
                            tint = CyberCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "إذن الوصول إلى ملفات الفيديو",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "يتطلب تطبيق زايد للمونتاج إذن قراءة الفيديوهات (READ_MEDIA_VIDEO) من أجل:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• استخراج الإطارات والمقاييس الفنية وتحليل المشاهد بـ Gemini Vision.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Text(
                        text = "• عزل الضوضاء وتفريغ الكلام الصوتي وخفض الموسيقى آلياً.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                    Text(
                        text = "• تطبيق تأثيرات الزووم السلس والرندرة بدون إتلاف الملف الأصلي.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { controller.onRationaleConfirm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("rationale_grant_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("منح الإذن الآن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { controller.onRationaleDismiss() },
                    modifier = Modifier.testTag("rationale_cancel_btn")
                ) {
                    Text("لاحقاً", color = TextSecondary)
                }
            }
        )
    }

    if (controller.showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { controller.onSettingsDismiss() },
            modifier = Modifier.testTag("video_permission_settings_dialog"),
            containerColor = StudioNavySurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Permission Denied",
                    tint = ElectricAmber,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "الإذن مطلوب للمتابعة",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "تم رفض إذن الوصول لملفات الفيديو سابقاً. يرجى تفعيل إذن 'الملفات والوسائط' يدوياً من إعدادات التطبيق لتتمكن من اختيار الفيديوهات ومعالجتها.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { controller.onOpenAppSettings() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmber,
                        contentColor = ObsidianDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("open_app_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح الإعدادات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { controller.onSettingsDismiss() }
                ) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }
}
