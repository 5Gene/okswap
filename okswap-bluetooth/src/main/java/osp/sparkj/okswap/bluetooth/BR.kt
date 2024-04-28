package osp.sparkj.okswap.bluetooth


//val bluetoothProfileListener = object : BluetoothProfile.ServiceListener {
//    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
//        if (profile == BluetoothProfile.HEADSET) {
//            val bluetoothHeadset = proxy as BluetoothHeadset
//            bluetoothHeadset.connect(device) // 使用之前选择的device进行连接操作
//            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
//        }
//    }
//
//    override fun onServiceDisconnected(profile: Int) {}
//}
//
//bluetoothAdapter.getProfileProxy(context, bluetoothProfileListener, BluetoothProfile.HEADSET)