package com.example.cutesyalarm.model

import java.time.LocalTime
import java.util.UUID

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val time: LocalTime,
    val category: AlarmCategory,
    val isEnabled: Boolean = true,
    val icon: String,
    val color: Int
)

enum class AlarmCategory {
    MEDICINE,
    MEAL
}

// Predefined alarms as requested
fun getDefaultAlarms(): List<Alarm> {
    return listOf(
        // Medicine alarms
        Alarm(
            title = "Drink Medicine",
            time = LocalTime.of(8, 0),
            category = AlarmCategory.MEDICINE,
            icon = "💊",
            color = 0xFFFFB7D5.toInt() // Pastel pink
        ),
        Alarm(
            title = "Drink Medicine",
            time = LocalTime.of(12, 0),
            category = AlarmCategory.MEDICINE,
            icon = "💊",
            color = 0xFFFFB7D5.toInt()
        ),
        Alarm(
            title = "Drink Medicine",
            time = LocalTime.of(18, 0),
            category = AlarmCategory.MEDICINE,
            icon = "💊",
            color = 0xFFFFB7D5.toInt()
        ),
        // Eat alarms
        Alarm(
            title = "Eat",
            time = LocalTime.of(9, 0),
            category = AlarmCategory.MEAL,
            icon = "🍽️",
            color = 0xFFB4E7CE.toInt() // Pastel mint
        ),
        Alarm(
            title = "Eat",
            time = LocalTime.of(13, 0),
            category = AlarmCategory.MEAL,
            icon = "🍽️",
            color = 0xFFB4E7CE.toInt()
        ),
        Alarm(
            title = "Eat",
            time = LocalTime.of(19, 0),
            category = AlarmCategory.MEAL,
            icon = "🍽️",
            color = 0xFFB4E7CE.toInt()
        )
    )
}