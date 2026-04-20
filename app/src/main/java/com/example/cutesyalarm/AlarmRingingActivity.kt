package com.example.cutesyalarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import com.example.cutesyalarm.receiver.AlarmReceiver
import com.example.cutesyalarm.service.AlarmService
import com.example.cutesyalarm.ui.theme.CuteColors
import com.example.cutesyalarm.ui.theme.CutesyAlarmTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.random.Random
import kotlin.math.roundToInt

class AlarmRingingActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAlarmWindow()
        showAlarm(intent)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle new alarm intents when activity is already running
        intent?.let { showAlarm(it) }
    }
    
    private fun setupAlarmWindow() {
        // Show on lock screen and wake up device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        
        // Request keyguard dismissal for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissError() {}
                    override fun onDismissSucceeded() {}
                    override fun onDismissCancelled() {}
                })
            }
        }
        
        // Always add window flags for maximum compatibility
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
        
        // Additional flags to ensure activity shows on top
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        
        // Bring activity to front even if app is already running
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setTaskDescription(android.app.ActivityManager.TaskDescription.Builder()
                .setLabel("⏰ Alarm")
                .build())
        }
    }
    
    private fun showAlarm(intent: Intent) {
        val alarmTitle = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_TITLE) ?: "Alarm"
        val alarmTime = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_TIME) ?: "00:00"
        
        // NOTE: Alarm sound is handled by AlarmService - don't play here to avoid double sound
        
        setContent {
            CutesyAlarmTheme {
                AlarmRingingScreen(
                    title = alarmTitle,
                    time = alarmTime,
                    onDismiss = {
                        AlarmService.stopAlarm(this)
                        finish()
                    },
                    onSnooze = {
                        AlarmService.stopAlarm(this)
                        // Could implement snooze logic here
                        finish()
                    }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Ensure alarm is stopped when activity is destroyed
        AlarmService.stopAlarm(this)
    }
}

@Composable
fun AlarmRingingScreen(
    title: String,
    time: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CuteColors.SecondaryPink,
                        CuteColors.LightMint,
                        CuteColors.Lavender
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Pulsing icon
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(CuteColors.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (title.contains("Medicine", ignoreCase = true)) "💊" else "🍽️",
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )
            }
            
            // Time display
            Text(
                text = time,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CuteColors.TextPrimary
                )
            )
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = CuteColors.TextPrimary
                ),
                textAlign = TextAlign.Center
            )
            
            // Instructions
            Text(
                text = "Drag the icon to the target to dismiss",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CuteColors.TextSecondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Drag and drop puzzle
            DragToDismissPuzzle(
                icon = if (title.contains("Medicine", ignoreCase = true)) "💊" else "🍽️",
                onDismiss = onDismiss
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Snooze button
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CuteColors.White.copy(alpha = 0.7f)
                ),
                border = null
            ) {
                Text(
                    text = "😴 Snooze 5 min",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CuteColors.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun DragToDismissPuzzle(
    icon: String,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val iconSizePx = with(density) { 80.dp.toPx() }
    val targetSizePx = with(density) { 100.dp.toPx() }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { 200.dp.toPx() } // Box height
    
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var iconBounds by remember { mutableStateOf<Rect?>(null) }
    var targetBounds by remember { mutableStateOf<Rect?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    
    // Random target position (kept stable across recompositions)
    val targetOffsetX by remember { 
        mutableFloatStateOf(
            Random.nextFloat() * (screenWidthPx - targetSizePx) - (screenWidthPx / 2 - targetSizePx / 2)
        )
    }
    val targetOffsetY by remember { 
        mutableFloatStateOf(
            Random.nextFloat() * (screenHeightPx - targetSizePx) - (screenHeightPx / 2 - targetSizePx / 2)
        )
    }
    
    val scope = rememberCoroutineScope()
    
    val targetScale by animateFloatAsState(
        targetValue = if (isDragging) 1.15f else 1f,
        animationSpec = tween(300),
        label = "targetScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Target zone (drop area) - positioned randomly
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        targetOffsetX.roundToInt(),
                        targetOffsetY.roundToInt()
                    )
                }
                .size(100.dp)
                .scale(targetScale)
                .clip(CircleShape)
                .background(
                    if (isDragging) CuteColors.PrimaryPink.copy(alpha = 0.5f)
                    else CuteColors.White.copy(alpha = 0.6f)
                )
                .onGloballyPositioned { coordinates ->
                    targetBounds = coordinates.boundsInWindow()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎯",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
        
        // Draggable icon - starts at center
        Box(
            modifier = Modifier
                .offset { 
                    IntOffset(
                        dragOffsetX.roundToInt(),
                        dragOffsetY.roundToInt()
                    )
                }
                .size(80.dp)
                .clip(CircleShape)
                .background(CuteColors.PrimaryPink)
                .onGloballyPositioned { coordinates ->
                    // Track the icon's actual window position including drag offset
                    val baseBounds = coordinates.boundsInWindow()
                    iconBounds = Rect(
                        left = baseBounds.left,
                        top = baseBounds.top,
                        right = baseBounds.right,
                        bottom = baseBounds.bottom
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            
                            // Check if dropped in target using actual window coordinates
                            iconBounds?.let { icon ->
                                targetBounds?.let { target ->
                                    // Check if centers are within threshold distance
                                    val iconCenterX = icon.center.x
                                    val iconCenterY = icon.center.y
                                    val targetCenterX = target.center.x
                                    val targetCenterY = target.center.y
                                    
                                    val distance = kotlin.math.sqrt(
                                        (iconCenterX - targetCenterX).let { it * it } +
                                        (iconCenterY - targetCenterY).let { it * it }
                                    )
                                    
                                    // Success if centers are within threshold
                                    if (distance < (targetSizePx / 2 + iconSizePx / 2) * 0.7f) {
                                        isSuccess = true
                                        // Use coroutine to delay dismiss and avoid crash during gesture processing
                                        scope.launch {
                                            delay(300)
                                            onDismiss()
                                        }
                                    } else {
                                        // Reset position if not in target
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
        
        // Success animation
        if (isSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CuteColors.PrimaryPink.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✨ Great Job! ✨",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CuteColors.TextPrimary
                    )
                )
            }
        }
    }
}