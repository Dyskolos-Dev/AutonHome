package com.example.autonhome

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ControlScreen(navController: NavController, bluetoothManager: BluetoothManager) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var widgets by remember { mutableStateOf(WidgetStorage.loadWidgets(context)) }

    val scope = rememberCoroutineScope()

    if (showAddDialog) {
        AddWidgetDialog(
            onDismiss = { showAddDialog = false },
            onAddWidget = { newWidget ->
                widgets = widgets + newWidget
                WidgetStorage.saveWidgets(context, widgets)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Contrôle de la maison",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(widgets.size) { index ->
                val widget = widgets[index]
                WidgetCard(
                    widget = widget,
                    onToggle = { newState ->
                        scope.launch {
                            val command = if (newState) widget.commandOn else widget.commandOff
                            bluetoothManager.sendCommand(command)
                        }
                        widgets = widgets.toMutableList().apply {
                            set(index, widget.copy(isOn = newState))
                        }
                        WidgetStorage.saveWidgets(context, widgets)
                    },
                    onDelete = {
                        widgets = widgets.filterIndexed { i, _ -> i != index }
                        WidgetStorage.saveWidgets(context, widgets)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Ajouter un widget")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajouter un widget")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retour")
        }
    }
}

@Composable
fun WidgetCard(
    widget: WidgetData,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WidgetIcon(widget.iconName, widget.name)
            Spacer(modifier = Modifier.height(8.dp))
            Text(widget.name)
            Spacer(modifier = Modifier.height(8.dp))
            Switch(
                checked = widget.isOn,
                onCheckedChange = onToggle
            )
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer le widget")
            }
        }
    }
}

