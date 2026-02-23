package com.anogram.app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<BlePeer>>(emptyList())
    val discoveredPeers: StateFlow<List<BlePeer>> = _discoveredPeers.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _connectedPeers = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeers: StateFlow<Set<String>> = _connectedPeers.asStateFlow()

    private val _lastMessage = MutableStateFlow<BleMessage?>(null)
    val lastMessage: StateFlow<BleMessage?> = _lastMessage.asStateFlow()

    private val pendingMessages = mutableListOf<BleMessage>()
    
    private var myDeviceId: String = ""
    private var myDeviceName: String = "AnoGram"

    init {
        updateBluetoothState()
    }

    fun updateBluetoothState() {
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun setDeviceInfo(id: String, name: String) {
        myDeviceId = id
        myDeviceName = name
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (_isScanning.value) return
        
        _discoveredPeers.value = emptyList()
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        scanner?.startScan(null, scanSettings, scanCallback)
        _isScanning.value = true
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanner?.stopScan(scanCallback)
        _isScanning.value = false
    }

    fun getPendingMessages(): List<BleMessage> = pendingMessages.toList()

    fun queueMessage(message: BleMessage) {
        pendingMessages.add(message)
    }

    fun markMessageDelivered(messageId: String) {
        pendingMessages.removeAll { it.id == messageId }
    }

    fun onMessageReceived(message: BleMessage) {
        if (message.senderId != myDeviceId) {
            _lastMessage.value = message
        }
    }

    fun onPeerConnected(address: String) {
        _connectedPeers.value = _connectedPeers.value + address
    }

    fun onPeerDisconnected(address: String) {
        _connectedPeers.value = _connectedPeers.value - address
    }

    fun updatePeerConnectionStatus(address: String, connected: Boolean) {
        _discoveredPeers.value = _discoveredPeers.value.map { peer ->
            if (peer.deviceAddress == address) {
                peer.copy(isConnected = connected)
            } else peer
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: return
            
            if (name.contains(BleConstants.ADVERTISE_NAME, ignoreCase = true)) {
                val peer = BlePeer(
                    deviceAddress = device.address,
                    deviceName = name,
                    rssi = result.rssi,
                    lastSeen = System.currentTimeMillis()
                )
                
                val currentPeers = _discoveredPeers.value.toMutableList()
                val existingIndex = currentPeers.indexOfFirst { it.deviceAddress == device.address }
                
                if (existingIndex >= 0) {
                    currentPeers[existingIndex] = peer
                } else {
                    currentPeers.add(peer)
                }
                
                _discoveredPeers.value = currentPeers.sortedByDescending { it.rssi }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }
}
