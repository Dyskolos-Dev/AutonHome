package com.example.autonhome

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddWidgetDialog(
    onDismiss: () -> Unit,
    onAddWidget: (WidgetData) -> Unit
) {
    var widgetName by remember { mutableStateOf("") }
    var commandOn by remember { mutableStateOf("") }
    var commandOff by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<String?>(null) }

    val availableIcons = remember {
        listOf(
            "Lightbulb" to "Lumière",
            "Tv" to "TV",
            "Power" to "Prise",
            "Speaker" to "Son",
            "Lock" to "Serrure"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Ajouter un widget",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = widgetName,
                    onValueChange = { widgetName = it },
                    label = { Text("Nom du widget") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = commandOn,
                    onValueChange = { commandOn = it },
                    label = { Text("Commande ON") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = commandOff,
                    onValueChange = { commandOff = it },
                    label = { Text("Commande OFF") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Choisir une icône :", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    availableIcons.forEach { (iconName, description) ->
                        IconButton(
                            onClick = { selectedIcon = iconName },
                            modifier = Modifier.size(48.dp)
                        ) {
                            WidgetIcon(iconName, description)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (widgetName.isNotBlank() && commandOn.isNotBlank() &&
                                commandOff.isNotBlank() && selectedIcon != null) {
                                onAddWidget(
                                    WidgetData(
                                        name = widgetName,
                                        iconName = selectedIcon!!,
                                        commandOn = commandOn,
                                        commandOff = commandOff
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = widgetName.isNotBlank() && commandOn.isNotBlank() &&
                                commandOff.isNotBlank() && selectedIcon != null
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

