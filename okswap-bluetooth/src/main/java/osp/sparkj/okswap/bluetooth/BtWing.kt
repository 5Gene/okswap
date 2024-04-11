package osp.sparkj.okswap.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


internal var sBluetoothLifecycleOwner: BluetoothLifecycleOwner? = null
internal var sBluetoothScope: BluetoothScope? = null

val Context.bluetoothLifecycleOwner: LifecycleOwner
    get() = sBluetoothLifecycleOwner ?: BluetoothLifecycleOwner(this.applicationContext).also { sBluetoothLifecycleOwner = it }

val Context.bluetoothScope: CoroutineScope
    get() = sBluetoothScope ?: BluetoothScope(this.applicationContext).also { sBluetoothScope = it }

fun Context.bluetoothLifecycle(block: BluetoothLifecycleOwner.() -> Unit) {
    (bluetoothLifecycleOwner as BluetoothLifecycleOwner).block()
}

fun Intent.getBluetoothDevice(): BluetoothDevice? = if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
    getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
} else getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

@SuppressLint("MissingPermission")
suspend fun Context.boundDevice(address: String) = suspendCancellableCoroutine<Boolean> {
    val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
    if (bluetoothDevice.isBounded) {
        it.resume(true)
        return@suspendCancellableCoroutine
    }
    val boundStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val bluetoothDevice: BluetoothDevice = intent.getBluetoothDevice() ?: return
            if (bluetoothDevice.address != address) {
                return
            }
            val boundState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
            when (boundState) {
                BluetoothDevice.BOND_NONE -> {
                    unregisterReceiver(this)
                    it.resume(false)
                }

                BluetoothDevice.BOND_BONDED -> {
                    unregisterReceiver(this)
                    it.resume(true)
                }
            }
        }
    }
    ContextCompat.registerReceiver(
        this,
        boundStateReceiver,
        IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
        ContextCompat.RECEIVER_EXPORTED
    )
    it.invokeOnCancellation {
        unregisterReceiver(boundStateReceiver)
    }
    bluetoothDevice.createBond()
}

val BluetoothDevice.isConnected: Boolean
    get() {
        val mechod = javaClass.getDeclaredMethod("isConnected")
        return mechod.invoke(this) as Boolean
    }

val BluetoothDevice.isBounded: Boolean
    @SuppressLint("MissingPermission")
    get() {
        return bondState == BluetoothDevice.BOND_BONDED
    }

val Context.bluetoothAdapter: BluetoothAdapter
    get() = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
