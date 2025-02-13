package com.example.autonhome

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class BluetoothManager(private val context: Context) {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        data class Connected(val device: BluetoothDevice) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                            if (it.bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.UNCATEGORIZED) {
                                val currentList = _scannedDevices.value.toMutableList()
                                if (!currentList.contains(it)) {
                                    currentList.add(it)
                                    _scannedDevices.value = currentList
                                }
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(receiver, filter)
    }

    fun startScan() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            _connectionState.value = ConnectionState.Error("Permission de scan Bluetooth non accordée")
            return
        }

        try {
            if (!bluetoothAdapter?.isEnabled!!) {
                _connectionState.value = ConnectionState.Error("Bluetooth n'est pas activé")
                return
            }

            _scannedDevices.value = emptyList()
            _isScanning.value = true
            bluetoothAdapter.startDiscovery()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Erreur lors du scan: ${e.message}")
            _isScanning.value = false
        }
    }

    fun stopScan() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.cancelDiscovery()
            _isScanning.value = false
        }
    }

    suspend fun getPairedDevices(): List<BluetoothDevice> = withContext(Dispatchers.IO) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            return@withContext emptyList()
        }
        bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    suspend fun connectToDevice(device: BluetoothDevice) = withContext(Dispatchers.IO) {
        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                _connectionState.value = ConnectionState.Error("Permission Bluetooth non accordée")
                return@withContext false
            }

            _connectionState.value = ConnectionState.Connecting

            disconnect()

            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()

            if (bluetoothSocket?.isConnected == true) {
                _connectionState.value = ConnectionState.Connected(device)
                true
            } else {
                _connectionState.value = ConnectionState.Error("Échec de la connexion")
                false
            }
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Erreur inconnue")
            false
        }
    }

    suspend fun sendCommand(command: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (bluetoothSocket?.isConnected != true) {
                return@withContext false
            }

            bluetoothSocket?.outputStream?.write((command + "\n").toByteArray())
            bluetoothSocket?.outputStream?.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bluetoothSocket = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    fun getCurrentDevice(): BluetoothDevice? {
        return when (val state = connectionState.value) {
            is ConnectionState.Connected -> state.device
            else -> null
        }
    }

    fun onDestroy() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        disconnect()
    }
}