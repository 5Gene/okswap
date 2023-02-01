package osp.sparkj.okswap.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.*


data class Device(val name: String = "", val address: String = "")


class ThraadHander(name: String, callback: Handler.Callback) {

    val handler: Handler

    init {
        val handlerThread = HandlerThread(name)
        handlerThread.start()
        handler = Handler(handlerThread.looper, callback)
    }
}

data class BluetoothDeviceWrapper(val bluetoothDevice: BluetoothDevice, val rssi: Short)

val uuid = UUID.fromString("45de1f06-0d19-4eff-a7d6-1c6428b94297")


interface ServerEventListener {
    fun onDeviceConnected(bluetoothDevice: BluetoothDevice)
    fun onDeviceDisConnected(bluetoothDevice: BluetoothDevice)
}

sealed class BeginEndState {
    object Begin : BeginEndState()
    data class End(val succeed: Boolean) : BeginEndState()
}

interface ClientEventListener {
    fun bondState(bluetoothDevice: BluetoothDevice, state: BeginEndState)
    fun connectState(bluetoothDevice: BluetoothDevice, state: BeginEndState)
}

class LogClientEventListener : ClientEventListener {
    @SuppressLint("MissingPermission")
    override fun bondState(bluetoothDevice: BluetoothDevice, state: BeginEndState) {
        println("bondState > ${bluetoothDevice.name} > $state")
    }

    @SuppressLint("MissingPermission")
    override fun connectState(bluetoothDevice: BluetoothDevice, state: BeginEndState) {
        println("connectState > ${bluetoothDevice.name} > $state")
    }
}


data class DeviceBytes(val device: Device = Device(), val byteArray: ByteArray? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceBytes

        if (device != other.device) return false
        if (byteArray != null) {
            if (other.byteArray == null) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
        } else if (other.byteArray != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + (byteArray?.contentHashCode() ?: 0)
        return result
    }
}


interface BluetoothClient {
    fun scan()
    fun connect(device: BluetoothDevice)
    fun bondState(bluetoothDevice: BluetoothDevice, state: BeginEndState)
    fun connectState(bluetoothDevice: BluetoothDevice, state: BeginEndState)
    fun write(byteArray: ByteArray)
    fun release()
    val readFlow: SharedFlow<DeviceBytes>
    val bondedDevicesFlow: SharedFlow<List<BluetoothDevice>>
    val fondDeviceFlow: SharedFlow<List<BluetoothDeviceWrapper>>
}

abstract class AbscBluetoothClient : BluetoothClient {
    internal val defaultAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    internal val _readFlow: MutableSharedFlow<DeviceBytes> =
        MutableSharedFlow(extraBufferCapacity = 1)

    override val readFlow: SharedFlow<DeviceBytes>
        get() = _readFlow

    internal val _bondedDevicesFlow: MutableSharedFlow<List<BluetoothDevice>> =
        MutableSharedFlow(extraBufferCapacity = 1, replay = 1)
    override val bondedDevicesFlow: SharedFlow<List<BluetoothDevice>>
        get() = _bondedDevicesFlow

    internal val _fondDeviceFlow: MutableSharedFlow<List<BluetoothDeviceWrapper>> =
        MutableSharedFlow(extraBufferCapacity = 1)
    override val fondDeviceFlow: SharedFlow<List<BluetoothDeviceWrapper>>
        get() = _fondDeviceFlow

}

val MSG_CONNECT = 0
val MSG_WRITE = 1

interface BluetoothServer {
    fun onDeviceConnectStateChange(bluetoothDevice: BluetoothDevice, connected: Boolean)
    fun write(deviceBytes: DeviceBytes)
    fun release()
    val readFlow: SharedFlow<DeviceBytes>
    val connectDeviceFlow: SharedFlow<List<BluetoothDevice>>
}

abstract class AbscBluetoothServer : BluetoothServer {
    internal val _readFlow =
        MutableSharedFlow<DeviceBytes>(extraBufferCapacity = 1)

    override val readFlow: SharedFlow<DeviceBytes>
        get() = _readFlow

    internal val _connectDeviceFlow =
        MutableSharedFlow<List<BluetoothDevice>>(extraBufferCapacity = 1)
    override val connectDeviceFlow: SharedFlow<List<BluetoothDevice>>
        get() = _connectDeviceFlow
}

fun Int.showBoundState(): String {
    return if (this == BluetoothDevice.BOND_BONDED) {
        "BOND_BONDED"
    } else if (this == BluetoothDevice.BOND_BONDING) {
        "BOND_BONDING"
    } else {
        "BOND_NONE"
    }
}