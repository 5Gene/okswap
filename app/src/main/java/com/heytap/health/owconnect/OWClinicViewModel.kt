package com.heytap.health.owconnect

import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface Clinic {
    val patient: BluetoothDevice
    fun next()
}

class OWClinicViewModel() : ViewModel(), Clinic {
    val basicMsgs = basicDiagnose()
    private val owdoctors = listOf<OWDoctor>(BoundStateDoctor(), NearByDoctor())
    private var doctorIndex = 0
    private val _doctor = MutableStateFlow<OWDoctor>(owdoctors.first())
    val doctor: Flow<OWDoctor> = _doctor


    override val patient: BluetoothDevice
        get() = sBluetoothAdapter.getRemoteDevice("48:29:D6:D6:A6:26")!!


    private fun basicDiagnose() = listOf(
        BasicMsg(ui = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "时间")
                Text(
                    text = System.currentTimeMillis().showTime(LocalContext.current),
                    style = MaterialTheme.typography.caption
                )
            }
        }),
        BasicMsg(title = "APP版本", desc = "6.6.6"),
//          或者获取系统oaf版本
        BasicMsg(title = "设备快连版本", desc = "6.6.6"),
        BasicMsg(title = "保活设置", desc = "已设置"),
        BasicMsg(title = "设备型号", desc = "${Build.MANUFACTURER} ${Build.MODEL}"),
        BasicMsg(title = "操作系统", desc = Build.VERSION.RELEASE),
        BasicMsg(title = "系统蓝牙", desc = "已连接"),
    )

    fun connectDiagnose() {
        doctorIndex = 0
        val first = owdoctors.first()
        if (first.diagnoses(this)) {
            _doctor.tryEmit(first)
        }
    }

    override fun next() {
        doctorIndex++
        val owDoctor = owdoctors[doctorIndex]
        if (owDoctor.diagnoses(this)) {
            _doctor.tryEmit(owDoctor)
        }
    }

}

