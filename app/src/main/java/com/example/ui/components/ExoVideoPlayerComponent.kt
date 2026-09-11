package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoMetadata
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * A sleek, high-fidelity VideoPlayer component built with Google's ExoPlayer (AndroidX Media3)
 * that allows users to preview local video files from device storage before triggering
 * the AI editing & autonomous processing pipeline.
 */
@OptIn(UnstableApi::class)
@Composable
fun ExoVideoPlayerComponent(
    videoUriString: String?,
    videoTitle: String = "معاينة الفيديو المحدد",
    aspectRatio: String = "16:9",
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    onPlaybackEnded: () -> Unit = {}
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var isBuffering by remember { mutableStateOf(true) }
    var showControlsOverlay by remember { mutableStateOf(true) }

    // Parse aspect ratio float
    val ratioFloat = remember(aspectRatio) {
        when (aspectRatio) {
            "9:16" -> 9f / 16f
            "1:1" -> 1f
            "4:5" -> 4f / 5f
            "21:9" -> 21f / 9f
            else -> 16f / 9f
        }
    }

    // Initialize ExoPlayer instance
    val exoPlayer = remember(videoUriString) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = autoPlay
            repeatMode = Player.REPEAT_MODE_OFF

            if (!videoUriString.isNullOrBlank()) {
                try {
                    val mediaItem = MediaItem.fromUri(Uri.parse(videoUriString))
                    setMediaItem(mediaItem)
                    prepare()
                } catch (e: Exception) {
                    playbackError = "تعذر تشغيل المقطع: ${e.localizedMessage}"
                }
            }
        }
    }

    // Register Player Listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        playbackError = null
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                        playbackError = null
                    }
                    Player.STATE_ENDED -> {
                        isPlaying = false
                        onPlaybackEnded()
                    }
                    Player.STATE_IDLE -> {
                        isBuffering = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playbackError = "خطأ في الترميز أو الملف المحلي: ${error.errorCodeName}"
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Coroutine ticker for current playback position
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (durationMs <= 0L && exoPlayer.duration > 0) {
                durationMs = exoPlayer.duration
            }
            delay(250)
        }
    }

    // Auto-hide controls overlay after 3 seconds of playing
    LaunchedEffect(showControlsOverlay, isPlaying) {
        if (showControlsOverlay && isPlaying) {
            delay(3500)
            showControlsOverlay = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianDark)
            .border(1.dp, BorderHighlight, RoundedCornerShape(14.dp))
            .testTag("exoplayer_video_preview")
    ) {
        // Top Player Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioNavySurface.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CyberCyan.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "ExoPlayer Preview",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = videoTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "مشغل ExoPlayer • معاينة قبل بدء المعالجة",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontSize = 9.sp
                    )
                }
            }

            // Aspect ratio & Mute Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ObsidianDark,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = aspectRatio,
                        fontSize = 9.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        exoPlayer.volume = if (isMuted) 0f else 1f
                    },
                    modifier = Modifier.size(28.dp).testTag("exoplayer_mute_btn")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "كتم الصوت" else "تشغيل الصوت",
                        tint = if (isMuted) ElectricAmber else CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // ExoPlayer Surface Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 320.dp)
                .aspectRatio(ratioFloat.coerceIn(0.6f, 1.8f))
                .background(Color.Black)
                .clickable { showControlsOverlay = !showControlsOverlay },
            contentAlignment = Alignment.Center
        ) {
            // AndroidView embedding AndroidX Media3 PlayerView
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Custom branded overlay controls
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )

            // Buffering Spinner
            if (isBuffering) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(ObsidianDark.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = CyberCyan,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            // Error Notice Banner
            playbackError?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianDark.copy(alpha = 0.9f))
                        .border(1.dp, ClimaxCrimson.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = ClimaxCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Custom Branded Overlay Controls
            androidx.compose.animation.AnimatedVisibility(
                visible = showControlsOverlay && playbackError == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Central Large Play / Pause Button
                    FilledIconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                if (exoPlayer.playbackState == Player.STATE_ENDED) {
                                    exoPlayer.seekTo(0)
                                }
                                exoPlayer.play()
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CyberCyan,
                            contentColor = ObsidianDark
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("exoplayer_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Bottom Progress & Scrubber Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioNavySurface)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Time Slider
            val maxDur = if (durationMs > 0) durationMs.toFloat() else 1f
            Slider(
                value = currentPositionMs.toFloat().coerceIn(0f, maxDur),
                onValueChange = { targetPos ->
                    currentPositionMs = targetPos.toLong()
                    exoPlayer.seekTo(targetPos.toLong())
                },
                valueRange = 0f..maxDur,
                colors = SliderDefaults.colors(
                    thumbColor = CyberCyan,
                    activeTrackColor = CyberCyan,
                    inactiveTrackColor = SlateGlassVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("exoplayer_scrubber")
            )

            // Time Stamps & Playback Control Helpers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTimeMs(currentPositionMs)} / ${formatTimeMs(durationMs)}",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Rewind 5s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier.size(24.dp).testTag("exoplayer_rewind_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay5,
                            contentDescription = "Rewind 5 seconds",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Quick Forward 5s
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 5000L).coerceAtMost(durationMs)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier.size(24.dp).testTag("exoplayer_forward_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward5,
                            contentDescription = "Forward 5 seconds",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenient overload accepting a [VideoMetadata] object.
 */
@Composable
fun ExoVideoPlayerComponent(
    video: VideoMetadata,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    onPlaybackEnded: () -> Unit = {}
) {
    ExoVideoPlayerComponent(
        videoUriString = video.uriString,
        videoTitle = video.fileName,
        aspectRatio = video.aspectRatio,
        modifier = modifier,
        autoPlay = autoPlay,
        onPlaybackEnded = onPlaybackEnded
    )
}

private fun formatTimeMs(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
