package osp.sparkj.okswap.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class BluetoothLifecycleRegistry(private val lifecycleOwner: LifecycleOwner) : Lifecycle() {

    val observers = mutableListOf<LifecycleEventObserver>()
    override val currentState: State = State.STARTED

    var currentEvent: Lifecycle.Event = Event.ON_CREATE
        set(value) {
            if (value == field) {
                return
            }
            val offset = value.ordinal - field.ordinal
            if (offset < -1 && offset > -5) {
                return
            }
            field = value
            observers.forEach {
                it.onStateChanged(lifecycleOwner, value)
            }
        }

    override fun addObserver(observer: LifecycleObserver) {
        if (observer !is LifecycleEventObserver) {
            throw RuntimeException("only support LifecycleEventObserver")
        }
        observers.add(observer)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

}

@SuppressLint("MissingPermission")
class BluetoothLifecycleOwner(context: Context, val filter: (String) -> Boolean = { _ -> true }) : LifecycleOwner, BroadcastReceiver() {

    override val lifecycle: Lifecycle
        get() = _lifecycleRegistry


    private val _lifecycleRegistry: BluetoothLifecycleRegistry = BluetoothLifecycleRegistry(this)
    private val _bluetoothAdapter = context.bluetoothAdapter

    init {
        if (Build.VERSION.SDK_INT > VERSION_CODES.S) {
            assert(
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        ContextCompat.registerReceiver(context, this, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            //ACTION_CONNECTION_STATE_CHANGED仅当第一个设备连接或最后一个设备断开连接时发送
            //addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }, RECEIVER_EXPORTED)
        _lifecycleRegistry.currentEvent = if (_bluetoothAdapter.isEnabled) {
            Lifecycle.Event.ON_CREATE
        } else {
            Lifecycle.Event.ON_DESTROY
        }

        val connectedDevices = _bluetoothAdapter.bondedDevices.filter { it.isConnected }
        if (connectedDevices.isNotEmpty()) {
            _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_RESUME
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        println("onReceive() called with: context = $context, intent.action = ${intent.action}")
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_CREATE
                    }

                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_DESTROY
                    }
                }
            }

//            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
//                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR)
//                when (state) {
//                    BluetoothAdapter.STATE_CONNECTED -> {
//                        //从没设备连接到有设备连接(第一个设备连上)会收到这个广播
//                        _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_RESUME
//                    }
//
//                    BluetoothAdapter.STATE_DISCONNECTED -> {
//                        所有已连接的设备都断开,当最后一个已连接的设备断开才收到这个广播
//                        _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_PAUSE
//                    }
//                }
//            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
//                val device = intent.getBluetoothDevice()
                _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_PAUSE
            }

            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                _lifecycleRegistry.currentEvent = Lifecycle.Event.ON_RESUME
            }
        }
    }
}

class BluetoothScope(context: Context, override val coroutineContext: CoroutineContext = SupervisorJob()) : CoroutineScope,
    LifecycleEventObserver {
    init {
        val bluetoothLifecycle = context.bluetoothLifecycleOwner.lifecycle as BluetoothLifecycleRegistry
        if (bluetoothLifecycle.currentEvent == Lifecycle.Event.ON_DESTROY) {
            coroutineContext.cancel()
        } else {
            launch(Dispatchers.Main.immediate) {
                bluetoothLifecycle.addObserver(this@BluetoothScope)
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            coroutineContext.cancel()
        }
    }
}

//@SuppressLint("MissingPermission")
//suspend fun Context.getHFPconnectedDevices() = suspendCancellableCoroutine<List<BluetoothDevice>> {
//    var bluetoothHeadset: BluetoothHeadset? = null
//    val listener = object : BluetoothProfile.ServiceListener {
//        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
//            if (profile == BluetoothProfile.HEADSET) {
//                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
//                bluetoothHeadset = proxy as BluetoothHeadset
//                // 获取已连接的 HFP 设备
//                val connectedDevices: List<BluetoothDevice> = bluetoothHeadset?.connectedDevices ?: emptyList()
//                it.resume(connectedDevices)
//            }
//        }
//
//        override fun onServiceDisconnected(profile: Int) {
//            Log.e("Bluetooth", "onServiceDisconnected.")
//            if (profile == BluetoothProfile.HEADSET) {
//                bluetoothHeadset = null
//            }
//        }
//    }
//
//    bluetoothAdapter.getProfileProxy(this, listener, BluetoothProfile.HEADSET)
//    it.invokeOnCancellation {
//        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
//    }
//
//}

//@Composable
//fun bluetoothState(block: @Composable (Boolean, () -> Unit) -> Unit) {
//
//    val open = mutableStateOf(LocalContext.current.bluetoothAdapter.isEnabled)
//    val openBluetooth = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { _ ->
//        open.value = bluetoothAdapter.isEnabled
//    }
//
//    block(open.value) {
//        openBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
//    }
//}

fun bt_log(vararg msgs: String) {
    println("${Thread.currentThread().name}: ${msgs.joinToString(", ")}")
}