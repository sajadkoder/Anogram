package com.anogram.app.data.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class BleGattService : Service() {

    @Inject
    lateinit var bleManager: BleManager

    private var bluetoothGattServer: BluetoothGattServer? = null
    private val binder = LocalBinder()
    
    private var messageCharacteristic: BluetoothGattCharacteristic? = null
    
    private val connectedDevices = mutableMapOf<String, BluetoothGatt>()

    companion object {
        private const val TAG = "BleGattService"
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleGattService = this@BleGattService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startGattServer()
    }

    override fun onDestroy() {
        stopGattServer()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothGattServer = bluetoothManager.openGattServer(this, gattServerCallback)

        val service = android.bluetooth.BluetoothGattService(
            BleConstants.SERVICE_UUID,
            android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        messageCharacteristic = BluetoothGattCharacteristic(
            BleConstants.MESSAGE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(messageCharacteristic)

        bluetoothGattServer?.addService(service)
        
        startAdvertising()
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        
        if (advertiser == null) {
            Log.e(TAG, "BLE advertiser not available")
            return
        }

        val advertiseSettings = android.bluetooth.le.AdvertiseSettings.Builder()
            .setAdvertiseMode(android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val advertiseData = android.bluetooth.le.AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(android.os.ParcelUuid(BleConstants.SERVICE_UUID))
            .build()

        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
    }

    private val advertiseCallback = object : android.bluetooth.le.AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: android.bluetooth.le.AdvertiseSettings?) {
            Log.d(TAG, "BLE advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE advertising failed: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopGattServer() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        connectedDevices.values.forEach { it.close() }
        connectedDevices.clear()
        bluetoothGattServer?.close()
    }

    fun sendMessage(message: BleMessage) {
        val messageBytes = serializeMessage(message)
        
        connectedDevices.values.forEach { gatt ->
            messageCharacteristic?.let { char ->
                char.value = messageBytes
                gatt.writeCharacteristic(char)
            }
        }
    }

    fun broadcastMessage(message: BleMessage) {
        if (message.hopCount < BleConstants.MAX_RELAY_HOPS) {
            val relayedMessage = message.copy(
                hopCount = message.hopCount + 1,
                isRelayed = true
            )
            sendMessage(relayedMessage)
        }
    }

    private fun serializeMessage(message: BleMessage): ByteArray {
        return "${message.id}|${message.senderId}|${message.senderName}|${message.content}|${message.timestamp}|${message.hopCount}".toByteArray()
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

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(device: android.bluetooth.BluetoothDevice, status: Int, newState: Int) {
            val address = device.address
            
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevices[address] = device.connectGatt(this@BleGattService, false, gattCallback)
                bleManager.onPeerConnected(address)
                Log.d(TAG, "Device connected: $address")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevices.remove(address)
                bleManager.onPeerDisconnected(address)
                Log.d(TAG, "Device disconnected: $address")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServiceAdded(status: Int, service: android.bluetooth.BluetoothGattService?) {
            Log.d(TAG, "GATT service added: $status")
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: android.bluetooth.BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.MESSAGE_CHARACTERISTIC_UUID) {
                characteristic.value = "AnoGram".toByteArray()
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.value)
            } else {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_READ_NOT_PERMITTED, offset, null)
            }
        }
    }

    private val gattCallback = object : android.bluetooth.BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: android.bluetooth.BluetoothGatt, status: Int, newState: Int) {
            val address = gatt.device.address
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bleManager.updatePeerConnectionStatus(address, true)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bleManager.updatePeerConnectionStatus(address, false)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: android.bluetooth.BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered for ${gatt.device.address}")
            }
        }

        override fun onCharacteristicChanged(
            gatt: android.bluetooth.BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.MESSAGE_CHARACTERISTIC_UUID) {
                val message = deserializeMessage(characteristic.value)
                message?.let {
                    bleManager.onMessageReceived(it)
                    broadcastMessage(it)
                }
            }
        }
    }
}
