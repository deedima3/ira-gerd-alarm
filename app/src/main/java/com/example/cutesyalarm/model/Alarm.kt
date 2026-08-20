package com.example.cutesyalarm.model

import java.time.LocalTime

data class Alarm(
    val id: String,
    val title: String,
    val time: LocalTime,
    val category: AlarmCategory,
    val isEnabled: Boolean = true,
    val icon: String,
    val color: Int
) {
    companion object {
        /**
         * Generates a stable, deterministic ID based on alarm properties.
         * This ensures the same alarm always has the same ID across app restarts,
         * preventing duplicate alarms and lost preferences.
         */
        fun generateStableId(title: String, time: LocalTime, category: AlarmCategory): String {
            return "${category.name}_${time.hour}_${time.minute}_${title.hashCode()}"
        }
    }
}

enum class AlarmCategory {
    MEDICINE,
    MEAL,
    PREP
}

// Predefined alarms as requested
fun getDefaultAlarms(): List<Alarm> {
    val alarms = listOf(
        // Buy groceries / prepare food, 1 hour before each medicine alarm
        Triple("Buy Groceries / Prepare Food", LocalTime.of(7, 0), AlarmCategory.PREP),
        Triple("Buy Groceries / Prepare Food", LocalTime.of(11, 0), AlarmCategory.PREP),
        Triple("Buy Groceries / Prepare Food", LocalTime.of(17, 0), AlarmCategory.PREP),
        // Medicine alarms
        Triple("Drink Medicine", LocalTime.of(8, 0), AlarmCategory.MEDICINE),
        Triple("Drink Medicine", LocalTime.of(12, 0), AlarmCategory.MEDICINE),
        Triple("Drink Medicine", LocalTime.of(18, 0), AlarmCategory.MEDICINE),
        // Eat alarms
        Triple("Eat", LocalTime.of(9, 0), AlarmCategory.MEAL),
        Triple("Eat", LocalTime.of(13, 0), AlarmCategory.MEAL),
        Triple("Eat", LocalTime.of(19, 0), AlarmCategory.MEAL)
    )
    
    return alarms.map { (title, time, category) ->
        val icon = when (category) {
            AlarmCategory.MEDICINE -> "💊"
            AlarmCategory.MEAL -> "🍽️"
            AlarmCategory.PREP -> "🛒"
        }
        val color = when (category) {
            AlarmCategory.MEDICINE -> 0xFFFFB7D5.toInt() // Pastel pink
            AlarmCategory.MEAL -> 0xFFB4E7CE.toInt() // Pastel mint
            AlarmCategory.PREP -> 0xFFE6E6FA.toInt() // Pastel lavender
        }
        
        Alarm(
            id = Alarm.generateStableId(title, time, category),
            title = title,
            time = time,
            category = category,
            icon = icon,
            color = color
        )
    }
}