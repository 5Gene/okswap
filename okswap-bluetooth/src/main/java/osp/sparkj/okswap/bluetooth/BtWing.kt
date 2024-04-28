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
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Volatile
internal var sBluetoothLifecycleOwner: BluetoothLifecycleOwner? = null

@Volatile
internal var sBluetoothScope: BluetoothScope? = null

val Context.bluetoothLifecycleOwner: LifecycleOwner
    get() = sBluetoothLifecycleOwner ?: BluetoothLifecycleOwner(this.applicationContext).also {
        check(Thread.currentThread() == Looper.getMainLooper().thread) {
            "The initial execution must occur on the main thread."
        }
        sBluetoothLifecycleOwner = it
    }

val Context.bluetoothScope: CoroutineScope
    get() = sBluetoothScope ?: BluetoothScope(this.applicationContext).also {
        check(Thread.currentThread() == Looper.getMainLooper().thread) {
            "The initial execution must occur on the main thread."
        }
        sBluetoothScope = it
    }

fun Context.bluetoothLifecycle(block: BluetoothLifecycleOwner.() -> Unit) {
    (bluetoothLifecycleOwner as BluetoothLifecycleOwner).block()
}

fun Intent.getBluetoothDevice(): BluetoothDevice? = if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
    getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
} else getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

@SuppressLint("MissingPermission")
suspend fun Context.boundDevice(address: String) = suspendCancellableCoroutine<Result<Boolean>> {
    val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
    if (bluetoothDevice.isBounded) {
        it.resume(Result.success(true))
        return@suspendCancellableCoroutine
    }
    val boundStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (address != intent.getBluetoothDevice()?.address) {
                "boundStateReceiver onReceive not target address, target is $address".btLog()
                return
            }
            val boundState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
            when (boundState) {
                BluetoothDevice.BOND_NONE -> {
                    unregisterReceiver(this)
                    /**
                     * Used as an int extra field in {@link #ACTION_PAIRING_REQUEST}
                     * intents for unbond reason.
                     * Possible value are :
                     *  - {@link #UNBOND_REASON_AUTH_FAILED}
                     *  - {@link #UNBOND_REASON_AUTH_REJECTED}
                     *  - {@link #UNBOND_REASON_AUTH_CANCELED}
                     *  - {@link #UNBOND_REASON_REMOTE_DEVICE_DOWN}
                     *  - {@link #UNBOND_REASON_DISCOVERY_IN_PROGRESS}
                     *  - {@link #UNBOND_REASON_AUTH_TIMEOUT}
                     *  - {@link #UNBOND_REASON_REPEATED_ATTEMPTS}
                     *  - {@link #UNBOND_REASON_REMOTE_AUTH_CANCELED}
                     *  - {@link #UNBOND_REASON_REMOVED}
                     *
                     * {@hide}
                     */
//                    @SystemApi
//                    @SuppressLint("ActionValue")
//                    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";
                    val reason = intent.getIntExtra("android.bluetooth.device.extra.REASON", BluetoothDevice.ERROR)
                    "BOUND_NONE reason is $reason".btLog()
                    it.resume(Result.failure(RuntimeException("reason: $reason")))
                }

                BluetoothDevice.BOND_BONDED -> {
                    unregisterReceiver(this)
                    it.resume(Result.success(true))
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


//fun headSetConnect() {
//
//    val bluetoothProfileListener = object : BluetoothProfile.ServiceListener {
//        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
//            if (profile == BluetoothProfile.HEADSET) {
//                val bluetoothHeadset = proxy as BluetoothHeadset
//                bluetoothHeadset.connect(device) // 使用之前选择的device进行连接操作
//                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
//            }
//        }
//
//        override fun onServiceDisconnected(profile: Int) {}
//    }
//
//    bluetoothAdapter.getProfileProxy(context, bluetoothProfileListener, BluetoothProfile.HEADSET)
//}

@Composable
fun isInPreview() = LocalInspectionMode.current

fun String.btLog() {
    Log.d("BT_swap", this)
}

fun Throwable.btLog() {
    Log.e("BT_swap", Log.getStackTraceString(this))
}
