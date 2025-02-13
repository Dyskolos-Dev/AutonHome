package com.example.autonhome

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autonhome.ui.theme.AutonHomeTheme

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothManager: BluetoothManager

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN
    )

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            checkBluetoothAndScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothManager = BluetoothManager(this)

        setContent {
            var isDarkMode by remember { mutableStateOf(ThemeManager.loadThemePreference(this)) }

            AutonHomeTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutonHomeApp(
                        bluetoothManager = bluetoothManager,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = {
                            isDarkMode = !isDarkMode
                            ThemeManager.saveThemePreference(this, isDarkMode)
                        },
                        onScanRequest = { checkAndRequestPermissions() }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            checkBluetoothAndScan()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun checkBluetoothAndScan() {
        if (bluetoothManager.bluetoothAdapter?.isEnabled == true) {
            bluetoothManager.startScan()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            bluetoothManager.startScan()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.onDestroy()
    }
}

@Composable
fun AutonHomeApp(
    bluetoothManager: BluetoothManager,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onScanRequest: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("control") { ControlScreen(navController, bluetoothManager) }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                bluetoothManager = bluetoothManager,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onScanRequest = onScanRequest
            )
        }
    }
}