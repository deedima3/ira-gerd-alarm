package com.example.cutesyalarm.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cutesyalarm.model.Alarm
import com.example.cutesyalarm.model.AlarmCategory
import com.example.cutesyalarm.model.getDefaultAlarms
import com.example.cutesyalarm.ui.components.AlarmCard
import com.example.cutesyalarm.ui.theme.CuteColors
import com.example.cutesyalarm.util.AlarmScheduler
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Alarm states
    var alarms by remember { mutableStateOf(getDefaultAlarms()) }
    var enabledAlarms by remember { mutableStateOf(alarms.associate { it.id to true }) }
    
    // Initialize alarms on first launch
    LaunchedEffect(Unit) {
        alarms.forEach { alarm ->
            AlarmScheduler.scheduleAlarm(
                context = context,
                alarmId = alarm.id,
                title = alarm.title,
                time = alarm.time,
                isEnabled = enabledAlarms[alarm.id] ?: true
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Alarms",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CuteColors.Background,
                    titleContentColor = CuteColors.TextPrimary
                ),
                actions = {
                    // Test Alarm Button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                // Schedule test alarm 10 seconds from now
                                val triggerTime = System.currentTimeMillis() + 10_000
                                AlarmScheduler.scheduleAlarmAtTime(
                                    context = context,
                                    alarmId = "test_alarm_10s",
                                    title = "🧪 Test Alarm",
                                    triggerTimeMillis = triggerTime
                                )
                                snackbarHostState.showSnackbar(
                                    message = "⏰ Test alarm set for 10 seconds!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CuteColors.LightMint)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Test Alarm (10s)",
                            tint = CuteColors.TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { /* Settings */ },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CuteColors.Cream)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CuteColors.TextPrimary
                        )
                    }
                }
            )
        },
        containerColor = CuteColors.Background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header section with greeting
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) + 
                        slideInVertically(animationSpec = tween(500))
            ) {
                GreetingHeader()
            }
            
            // Category summary
            AlarmSummaryCard(
                medicineCount = alarms.count { it.category == AlarmCategory.MEDICINE && enabledAlarms[it.id] == true },
                mealCount = alarms.count { it.category == AlarmCategory.MEAL && enabledAlarms[it.id] == true }
            )
            
            // Alarm list
            Text(
                text = "Your Reminders",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = alarms,
                    key = { it.id }
                ) { alarm ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)) + 
                                slideInHorizontally(animationSpec = tween(300))
                    ) {
                        AlarmCard(
                            alarm = alarm,
                            isEnabled = enabledAlarms[alarm.id] ?: true,
                            onToggle = { isEnabled ->
                                enabledAlarms = enabledAlarms.toMutableMap().apply {
                                    put(alarm.id, isEnabled)
                                }
                                
                                if (isEnabled) {
                                    AlarmScheduler.scheduleAlarm(
                                        context = context,
                                        alarmId = alarm.id,
                                        title = alarm.title,
                                        time = alarm.time,
                                        isEnabled = true
                                    )
                                } else {
                                    AlarmScheduler.cancelAlarm(context, alarm.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingHeader() {
    val currentHour = java.time.LocalTime.now().hour
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 18 -> "Good Afternoon"
        else -> "Good Evening"
    }
    
    val emoji = when {
        currentHour < 12 -> "☀️"
        currentHour < 18 -> "🌤️"
        else -> "🌙"
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "$emoji $greeting!",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = CuteColors.TextPrimary
            )
        )
        Text(
            text = "Take care of yourself today",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = CuteColors.TextSecondary
            )
        )
    }
}

@Composable
fun AlarmSummaryCard(
    medicineCount: Int,
    mealCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CuteColors.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Medicine counter
            SummaryItem(
                icon = "💊",
                count = medicineCount,
                label = "Medicine",
                color = CuteColors.PrimaryPink
            )
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(CuteColors.TextLight.copy(alpha = 0.3f))
            )
            
            // Meal counter
            SummaryItem(
                icon = "🍽️",
                count = mealCount,
                label = "Meals",
                color = CuteColors.Mint
            )
        }
    }
}

@Composable
fun SummaryItem(
    icon: String,
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CuteColors.TextSecondary
            )
        )
    }
}