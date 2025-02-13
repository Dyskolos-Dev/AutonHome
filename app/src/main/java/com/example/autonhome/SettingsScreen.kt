package com.example.autonhome

import android.bluetooth.BluetoothDevice
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    bluetoothManager: BluetoothManager,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onScanRequest: () -> Unit
) {
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    val connectionState by bluetoothManager.connectionState.collectAsState()
    val scannedDevices by bluetoothManager.scannedDevices.collectAsState()
    val isScanning by bluetoothManager.isScanning.collectAsState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        pairedDevices = bluetoothManager.getPairedDevices()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mode sombre")
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        onToggleDarkMode()
                        ThemeManager.saveThemePreference(context, !isDarkMode)
                    }
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "État de la connexion Bluetooth",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    when (connectionState) {
                        is BluetoothManager.ConnectionState.Connected -> {
                            val device = (connectionState as BluetoothManager.ConnectionState.Connected).device
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        "Connecté à :",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        device.name ?: "Appareil inconnu",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { bluetoothManager.disconnect() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Déconnecter")
                                }
                            }
                        }
                        is BluetoothManager.ConnectionState.Connecting -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connexion en cours...")
                            }
                        }
                        is BluetoothManager.ConnectionState.Error -> {
                            Text(
                                "Erreur : ${(connectionState as BluetoothManager.ConnectionState.Error).message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is BluetoothManager.ConnectionState.Disconnected -> {
                            Text("Non connecté")
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (isScanning) {
                        bluetoothManager.stopScan()
                    } else {
                        onScanRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isScanning) "Arrêter le scan" else "Scanner les appareils SPP")
            }
        }

        if (isScanning) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }

        if (scannedDevices.isNotEmpty()) {
            item {
                Text(
                    "Appareils SPP détectés",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            items(scannedDevices) { device ->
                DeviceCard(device, bluetoothManager, connectionState)
            }
        }

        item {
            Text(
                "Appareils Bluetooth appairés",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(pairedDevices) { device ->
            DeviceCard(device, bluetoothManager, connectionState)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Crédits",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Projet réalisé par :")
                TextButton(onClick = { uriHandler.openUri("https://dyskolos.fr") }) {
                    Text("Baptiste BEUILLÉ")
                }
                Text("Halis TUGLU")
                Text("Gabriel GENEVRIER")
                Text("Phileas DURIS-DAUPHIN")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Application pensée et réalisée par Baptiste BEUILLÉ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Le projet AutonHome est né du projet de terminale de la spécialité Sciences de l'Ingénieur à Sainte Marie La Grand'Grange (Promo 2024/2025)",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Remerciements à notre professeur D.VERICEL",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retour")
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: BluetoothDevice,
    bluetoothManager: BluetoothManager,
    connectionState: BluetoothManager.ConnectionState
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                is BluetoothManager.ConnectionState.Connected -> {
                    if (connectionState.device == device)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                }
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    device.name ?: "Appareil inconnu",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {
                    scope.launch {
                        when (connectionState) {
                            is BluetoothManager.ConnectionState.Connected -> {
                                if (connectionState.device == device) {
                                    bluetoothManager.disconnect()
                                } else {
                                    bluetoothManager.connectToDevice(device)
                                }
                            }
                            else -> bluetoothManager.connectToDevice(device)
                        }
                    }
                },
                enabled = connectionState !is BluetoothManager.ConnectionState.Connecting
            ) {
                Text(
                    when {
                        connectionState is BluetoothManager.ConnectionState.Connected &&
                                connectionState.device == device -> "Déconnecter"
                        else -> "Connecter"
                    }
                )
            }
        }
    }
}