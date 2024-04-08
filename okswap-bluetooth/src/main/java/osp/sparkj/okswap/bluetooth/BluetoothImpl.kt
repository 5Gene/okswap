package osp.sparkj.okswap.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import kotlin.concurrent.thread


@SuppressLint("MissingPermission")
class BrClient(
    private val context: Context,
    private val clientEventListener: ClientEventListener = LogClientEventListener()
) : AbscBluetoothClient() {

    var bluetoothSocket: BluetoothSocket? = null
    private var connectThread: ConnectThread? = null

    @SuppressLint("MissingPermission")
    val readerHandler: Handler = ThraadHander("client-read") {
        bluetoothSocket = it.obj as BluetoothSocket
        bluetoothSocket?.run {
            val address = remoteDevice.address
            val name = remoteDevice.name
            _readFlow.tryEmit(DeviceBytes(Device(name, address), null))
            connectState(remoteDevice, BeginEndState.End(true))
            //开启循环读数据
            readFromSocket(name, address, this)
        }
        return@ThraadHander true
    }.handler

    private fun readFromSocket(address: String, name: String, bluetoothSocket: BluetoothSocket) {
        thread {
            try {
                val byteArray = ByteArray(1024)
                while (bluetoothSocket.isConnected) {
                    val length = bluetoothSocket.inputStream.read(byteArray)
                    val availableBytes = ByteArray(length)
                    System.arraycopy(byteArray, 0, availableBytes, 0, length)
                    println(_readFlow.tryEmit(DeviceBytes(Device(name, address), availableBytes)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _readFlow.tryEmit(DeviceBytes())
                connectState(bluetoothSocket.remoteDevice, BeginEndState.End(false))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val writerHandler: Handler = ThraadHander("client-write") {
        bluetoothSocket?.run {
            if (isConnected) {
                val byteArray = it.obj as ByteArray
                println("write > $byteArray")
                outputStream.write(byteArray)
            }
        }
        return@ThraadHander true
    }.handler

    init {
        context.registerReceiver(object : BroadcastReceiver() {
            val discoveryDevices = mutableMapOf<String, BluetoothDeviceWrapper>()

            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
//                        val bluetoothDevice =
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                intent.getParcelableExtra(
//                                    BluetoothDevice.EXTRA_DEVICE,
//                                    BluetoothDevice::class.java
//                                )
//                            } else {
//                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                            }
                        val bluetoothDevice: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        if (bluetoothDevice != null && !TextUtils.isEmpty(bluetoothDevice.name) && bluetoothDevice.name != "null") {
                            discoveryDevices[bluetoothDevice.address] = BluetoothDeviceWrapper(
                                bluetoothDevice = bluetoothDevice,
                                rssi = intent.extras?.getShort(BluetoothDevice.EXTRA_RSSI) ?: -100
                            )
                            _fondDeviceFlow.tryEmit(discoveryDevices.values.toList())
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        defaultAdapter.cancelDiscovery()
                        _fondDeviceFlow.tryEmit(discoveryDevices.values.toList())
                        discoveryDevices.clear()
                    }

                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
//                        val bluetoothDevice =
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                intent.getParcelableExtra(
//                                    BluetoothDevice.EXTRA_DEVICE,
//                                    BluetoothDevice::class.java
//                                )
//                            } else {
//                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                            }
                        val bluetoothDevice: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        bluetoothDevice?.run {
                            if (bluetoothDevice.bondState == BluetoothDevice.BOND_BONDED) {
                                _bondedDevicesFlow.tryEmit(defaultAdapter.bondedDevices.toList())
                                bondState(this, BeginEndState.End(true))
                            }
                            println(bluetoothDevice.bondState.showBoundState())
                        }
                    }
                }
            }
        }, IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        })
        BluetoothAdapter.STATE_ON
        _bondedDevicesFlow.tryEmit(defaultAdapter.bondedDevices.toList())
    }

    override fun scan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("no premission ")
            return
        }

        defaultAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice) {
        defaultAdapter.cancelDiscovery()
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            bondState(device, BeginEndState.Begin)
            device.createBond()
        } else {
            release()
            //连接
            connectThread?.release = true
            connectState(device, BeginEndState.Begin)
            connectThread = ConnectThread(device = device).also { it.start() }
        }
    }

    override fun bondState(bluetoothDevice: BluetoothDevice, state: BeginEndState) {
        clientEventListener.bondState(bluetoothDevice, state)
    }

    override fun connectState(bluetoothDevice: BluetoothDevice, state: BeginEndState) {
        clientEventListener.connectState(bluetoothDevice, state)
    }

    override fun write(byteArray: ByteArray) {
        writerHandler.sendMessage(Message.obtain().apply { obj = byteArray })
    }

    override fun release() {
        bluetoothSocket?.run {
            connectState(remoteDevice, BeginEndState.End(false))
            close()
        }
    }

    inner class ConnectThread(val device: BluetoothDevice) : Thread() {

        var release = false
        override fun run() {
            super.run()
            try {
                val bluetoothSocket: BluetoothSocket =
                    device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket.connect()
                if (release) {
                    bluetoothSocket.close()
                    connectState(device, BeginEndState.End(false))
                    return
                }
                readerHandler.sendMessage(Message.obtain().apply { obj = bluetoothSocket })
            } catch (e: Exception) {
                connectState(device, BeginEndState.End(false))
            }
        }
    }

}

class BrServer : AbscBluetoothServer() {
    private val activeDevices = mutableMapOf<String, BluetoothSocket>()

    @SuppressLint("MissingPermission")
    val readerHandler: Handler = ThraadHander("server-read") {
        when (it.what) {
            MSG_CONNECT -> {
                val bluetoothSocket = it.obj as BluetoothSocket
                val address = bluetoothSocket.remoteDevice.address
                val name = bluetoothSocket.remoteDevice.name
                activeDevices[address] = bluetoothSocket
                _readFlow.tryEmit(DeviceBytes(Device(name, address)))
                onDeviceConnectStateChange(bluetoothDevice = bluetoothSocket.remoteDevice, true)
                readFromSocket(address, name, bluetoothSocket)
            }
        }
        return@ThraadHander true
    }.handler

    @SuppressLint("MissingPermission")
    private val writerHandler: Handler = ThraadHander("server-write") {
        if (it.what == MSG_WRITE) {
            val deviceBytes = it.obj as DeviceBytes
            val address = deviceBytes.device.address
            activeDevices[address]?.run {
                if (isConnected) {
                    outputStream.write(deviceBytes.byteArray!!)
                } else {
                    println("error >> device disconnected $address")
                    activeDevices.remove(address)
                }
            } ?: println("error >> no device found $address")
        }
        return@ThraadHander true
    }.handler

    inner class ServerThread : Thread() {

        var stop = false

        @SuppressLint("MissingPermission")
        override fun run() {
            val defaultAdapter = BluetoothAdapter.getDefaultAdapter()
            val listenUsingRfcommWithBluetoothServerSocket =
                defaultAdapter.listenUsingRfcommWithServiceRecord("demo", uuid)
            while (!stop) {
                val bluetoothSocket = listenUsingRfcommWithBluetoothServerSocket.accept()
                println("ServerThread device connected ${bluetoothSocket.remoteDevice.address} ${bluetoothSocket.remoteDevice.toString()}")
                val obtain = Message.obtain()
                obtain.obj = bluetoothSocket
                obtain.what = MSG_CONNECT
                readerHandler.sendMessage(obtain)
            }
            listenUsingRfcommWithBluetoothServerSocket.close()
        }
    }

    val serverThread = ServerThread()

    init {
        //开线程监听客户端设备连接
        //1, 通过listenUsingRfcommWithServiceRecord(String，UUID)来获取BluetoothServerSocket对象。字符串String是代表服务的可识别名称，可以是应用名称。UUID是通用唯一标识符，这是客户端连接协议的基础。当客户端尝试连接此设备时，它会携带 UUID，从而对其想要连接的服务进行唯一标识。为了让服务器接受连接，这些 UUID 必须互相匹配。UUID可以在网上随机生成，由于数量庞大，重复的概率几乎为无，接着用fromString(String)来初始化一个UUID。
        //2, 通过accept()来监听请求。这是一个阻塞调用，故而应在子线程中调用。当服务器接受连接或异常发生时，该调用便会返回。只有当远程设备发送包含 UUID 的连接请求，并且该 UUID 与使用此侦听服务器套接字注册的 UUID 相匹配时，服务器才会接受连接。连接成功后，accept() 将返回已连接的 BluetoothSocket。
        //3, 当我们连接成功的时候，及时调用close(),释放服务器套接字及其所有资源，但不会关闭 accept() 所返回的已连接的 BluetoothSocket。
        serverThread.start()
    }

    private fun readFromSocket(address: String, name: String, bluetoothSocket: BluetoothSocket) {
        thread {
            try {
                val byteArray = ByteArray(1024)
                while (bluetoothSocket.isConnected) {
                    val length = bluetoothSocket.inputStream.read(byteArray)
                    val availableBytes = ByteArray(length)
                    System.arraycopy(byteArray, 0, availableBytes, 0, length)
                    println("read > ${java.lang.String(availableBytes)}")
                    println(_readFlow.tryEmit(DeviceBytes(Device(name, address), availableBytes)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _readFlow.tryEmit(DeviceBytes())
            }
            onDeviceConnectStateChange(bluetoothDevice = bluetoothSocket.remoteDevice, false)
            activeDevices.remove(address)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDeviceConnectStateChange(bluetoothDevice: BluetoothDevice, connected: Boolean) {
        _connectDeviceFlow.tryEmit(activeDevices.values.map { it.remoteDevice })
        println("onDeviceConnectStateChange >> ${bluetoothDevice.name} : $connected")
    }

    override fun write(deviceBytes: DeviceBytes) {
        val obtain = Message.obtain()
        obtain.obj = deviceBytes
        obtain.what = MSG_WRITE
        writerHandler.sendMessage(obtain)
    }

    override fun release() {
        serverThread.stop = true
        activeDevices.values.forEach {
            it.close()
            onDeviceConnectStateChange(it.remoteDevice, false)
        }
        activeDevices.clear()
    }

}