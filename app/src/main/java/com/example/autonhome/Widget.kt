package com.example.autonhome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

data class WidgetData(
    val name: String,
    val iconName: String,
    val commandOn: String,
    val commandOff: String,
    val isOn: Boolean = false
)

@Composable
fun WidgetIcon(iconName: String, contentDescription: String? = null) {
    when (iconName) {
        "Lightbulb" -> Icon(Icons.Filled.Lightbulb, contentDescription = contentDescription)
        "Tv" -> Icon(Icons.Filled.Tv, contentDescription = contentDescription)
        "Power" -> Icon(Icons.Filled.Power, contentDescription = contentDescription)
        "Speaker" -> Icon(Icons.Filled.Speaker, contentDescription = contentDescription)
        "Lock" -> Icon(Icons.Filled.Lock, contentDescription = contentDescription)
        else -> Icon(Icons.Filled.DeviceUnknown, contentDescription = contentDescription)
    }
}