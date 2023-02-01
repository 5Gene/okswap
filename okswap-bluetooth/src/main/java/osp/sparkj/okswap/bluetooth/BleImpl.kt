package osp.sparkj.okswap.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.text.TextUtils
import osp.sparkj.okswap.bluetooth.*
import java.util.*

@SuppressLint("MissingPermission")
class BleServer(context: Context) : AbscBluetoothServer() {

    val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
        }

    }

    init {
        bluetoothManager.openGattServer(context, bluetoothGattServerCallback)
    }

    override fun onDeviceConnectStateChange(bluetoothDevice: BluetoothDevice, connected: Boolean) {
        TODO("Not yet implemented")
    }

    override fun write(deviceBytes: DeviceBytes) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }
}


@SuppressLint("MissingPermission")
class BleClient(val context: Context) : AbscBluetoothClient() {

    var connectedGatt: BluetoothGatt? = null
    val discoveryDevices = mutableMapOf<String, BluetoothDeviceWrapper>()

    private val leScanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            println(results)
        }

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            if (discoveryDevices[device.address] == null && !TextUtils.isEmpty(device.name)) {
                discoveryDevices[device.address] =
                    BluetoothDeviceWrapper(device, result.rssi.toShort())
                _fondDeviceFlow.tryEmit(discoveryDevices.values.toList())
            }
        }
    }

    init {
        _bondedDevicesFlow.tryEmit(defaultAdapter.bondedDevices.toList())
    }

    @SuppressLint("MissingPermission")
    override fun scan() {
        val bluetoothLeScanner = defaultAdapter.bluetoothLeScanner
        bluetoothLeScanner.startScan(leScanCallback)
    }

    val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            println(" onConnectionStateChange >> ${gatt.device.name} $status $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedGatt = gatt
                _readFlow.tryEmit(
                    DeviceBytes(
                        device = Device(
                            gatt.device.name,
                            gatt.device.address
                        )
                    )
                )
//                扫描BLE设备服务是安卓系统中关于BLE蓝牙开发的重要一步，一般在设备连接成功后调用，扫描到设备服务后回调onServicesDiscovered()函数
                gatt.discoverServices()
            } else {
                connectedGatt = null
                //连接断开 清理数据

            }
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            super.onServiceChanged(gatt)
            println(" onServiceChanged >> ")
        }

        //        BLE蓝牙协议下数据的通信方式采用BluetoothGattService、BluetoothGattCharacteristic和BluetoothGattDescriptor三个主要的类实现通信。
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            println(" onServicesDiscovered >> ${gatt.device.name}")
            gatt.services.forEach {
                println(it.uuid)
                it.characteristics
            }
//    luetoothGattService 简称服务，是构成BLE设备协议栈的组成单位，一个蓝牙设备协议栈一般由一个或者多个BluetoothGattService组成。
//    BluetoothGattCharacteristic 简称特征，一个服务包含一个或者多个特征，特征作为数据的基本单元。一个BluetoothGattCharacteristic特征包含一个数据值和附加的关于特征的描述BluetoothGattDescriptor。
//    BluetoothGattDescriptor用于描述特征的类，其同样包含一个value值。
            // BLE蓝牙开发主要有负责通信的BluetoothGattService完成的。当且称为通信服务。通信服务通过硬件工程师提供的UUID获取
            //通信服务中包含负责读写的BluetoothGattCharacteristic，且分别称为notifyCharacteristic和writeCharacteristic。
            // notifyCharacteristic负责开启监听，也就是启动收数据的通道，
            // writeCharacteristic负责写入数据

            val bluetoothGattService = gatt.getService(UUID.randomUUID())
            val readCharacteristic =
                bluetoothGattService.getCharacteristic(UUID.fromString("notify"))
            val writeCharacteristic =
                bluetoothGattService.getCharacteristic(UUID.fromString("write"))

//            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("蓝牙模块提供的负责通信服务UUID字符串"));
//            // 例如形式如：49535343-fe7d-4ae5-8fa9-9fafd205e455
//            notifyCharacteristic = service.getCharacteristic(UUID.fromString("notify uuid"));
//            writeCharacteristic =  service.getCharacteristic(UUID.fromString("write uuid"));

            gatt.setCharacteristicNotification(readCharacteristic, true)
//            开启监听
//            mBluetoothGatt.setCharacteristicNotification(notifyCharacteristic, true)
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID);
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            若开启监听成功则会回调BluetoothGattCallback中的onDescriptorWrite()方法

        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //开启监听成功，可以像设备写入命令了
            }
        }

        //        若写入指令成功则回调BluetoothGattCallback中的onCharacteristicWrite()方法，说明将数据已经发送给下位机。
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        //若发送的数据符合通信协议，则下位机会向上位机回复相应的数据。发送的数据通过回调onCharacteristicChanged()方法获取
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
        }
    }

    @SuppressLint("MissingPermission")
    override fun connect(device: BluetoothDevice) {
        device.connectGatt(context, false, bluetoothGattCallback)
    }

    override fun bondState(bluetoothDevice: BluetoothDevice, state: BeginEndState) {
        TODO("Not yet implemented")
    }

    override fun connectState(bluetoothDevice: BluetoothDevice, state: BeginEndState) {
        TODO("Not yet implemented")
    }

    override fun write(byteArray: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }
}
