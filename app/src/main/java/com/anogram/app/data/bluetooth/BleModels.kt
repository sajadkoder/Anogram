package com.anogram.app.data.bluetooth

import java.util.UUID

object BleConstants {
    val SERVICE_UUID: UUID = UUID.fromString("0000FFFF-0000-1000-8000-00805F9B34FB")
    val MESSAGE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFFD-0000-1000-8000-00805F9B34FB")
    val PEER_INFO_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFFC-0000-1000-8000-00805F9B34FB")
    val CHUNK_SIZE = 512

    const val ADVERTISE_NAME = "AnoGram"
    const val SCAN_DURATION = 10000L
    const val CONNECTION_TIMEOUT = 15000L
    const val MAX_RELAY_HOPS = 7
}

data class BlePeer(
    val deviceAddress: String,
    val deviceName: String,
    val rssi: Int,
    var isConnected: Boolean = false,
    var lastSeen: Long = System.currentTimeMillis()
)

data class BleMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hopCount: Int = 0,
    val isRelayed: Boolean = false,
    val isDelivered: Boolean = false
)

data class PeerInfo(
    val id: String,
    val name: String,
    val publicKey: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PeerInfo
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
