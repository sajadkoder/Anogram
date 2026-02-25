package com.anogram.app.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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

    private var bleGattService: BleGattService? = null
    private var isServiceBound = false

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

    private val gattClients = mutableMapOf<String, BluetoothGatt>()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleGattService.LocalBinder
            bleGattService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleGattService = null
            isServiceBound = false
        }
    }

    init {
        updateBluetoothState()
        bindService()
    }

    private fun bindService() {
        val intent = Intent(context, BleGattService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun updateBluetoothState() {
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun setDeviceInfo(id: String, name: String) {
        myDeviceId = id
        myDeviceName = name
    }

    fun getMyDeviceId(): String = myDeviceId
    fun getMyDeviceName(): String = myDeviceName

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

    @SuppressLint("MissingPermission")
    fun connectToPeer(peer: BlePeer) {
        val device = bluetoothAdapter?.getRemoteDevice(peer.deviceAddress)
        device?.let { dev ->
            val gatt = dev.connectGatt(context, false, gattClientCallback)
            gattClients[peer.deviceAddress] = gatt
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnectPeer(address: String) {
        gattClients[address]?.close()
        gattClients.remove(address)
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
        if (connected) {
            _connectedPeers.value = _connectedPeers.value + address
        } else {
            _connectedPeers.value = _connectedPeers.value - address
        }
    }

    fun sendBleMessage(content: String) {
        val message = BleMessage(
            senderId = myDeviceId,
            senderName = myDeviceName,
            content = content
        )
        
        if (isServiceBound && bleGattService != null) {
            bleGattService?.sendMessage(message)
        } else {
            pendingMessages.add(message)
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

    private val gattClientCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                updatePeerConnectionStatus(address, true)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                updatePeerConnectionStatus(address, false)
                gattClients.remove(address)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                android.util.Log.d("BleManager", "Services discovered for ${gatt.device.address}")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: android.bluetooth.BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.MESSAGE_CHARACTERISTIC_UUID) {
                val data = characteristic.value
                if (data != null) {
                    val message = deserializeMessage(data)
                    message?.let { onMessageReceived(it) }
                }
            }
        }
    }

    private fun deserializeMessage(data: ByteArray): BleMessage? {
        return try {
            val parts = String(data).split("|")
            if (parts.size >= 6) {
                BleMessage(
                    id = parts[0],
                    senderId = parts[1],
                    senderName = parts[2],
                    content = parts[3],
                    timestamp = parts[4].toLong(),
                    hopCount = parts[5].toInt()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
