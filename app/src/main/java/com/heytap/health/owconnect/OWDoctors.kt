package com.heytap.health.owconnect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import osp.sparkj.okswap.bluetooth.isBounded
import java.text.SimpleDateFormat

val MD_HM = SimpleDateFormat("MM-dd HH:mm")

fun Long.showTime(context: Context? = null): String {
    if (context == null) {
        return MD_HM.format(this)
    }
    return DateUtils.formatDateTime(
        context, this, DateUtils.FORMAT_SHOW_DATE
                or DateUtils.FORMAT_SHOW_TIME
                or DateUtils.FORMAT_NO_YEAR
                or DateUtils.FORMAT_NO_MIDNIGHT
    )
}

/**
 * 数据库
 * 1 保存连接日志
 * 2 保存进程被杀和启动日志 几点被杀几点起来
 * 3 保存系统蓝牙连接断开监听记录
 */

/**
 * 可能的连接问题
 * 1 蓝牙连不上
 * 2 KSC协商失败 连不上,超时未回复
 * 3 BB15连不上 - 认证失败 -PD超时 等
 * 4 AA15连上不上 - 认证失败.等
 */
enum class OWStep(val desc: String) {
    REQUEST("REQ"), SYS_BT("HFP"), KSC("KSC"), BB15("BB15"), AA15("AA15"), SUCCESS("CONNECTED"),

    /**
     * 连接中断 已连接的前提下出现断开, 不包括切换设备的主动断开
     */
    INTERRUPTED("INTERRUPTED")
}

data class ConnectEvent(val time: Long, val step: OWStep, val code: Int = 0, val msg: String) {
    fun isSuccess(): Boolean = code == 0

    fun log() = "${time.showTime()} ${step.desc} $code: $msg"
}

//要保存到数据库
data class OWConnection(val mac: String, val events: List<ConnectEvent> = mutableListOf()) {
    fun isSucceed(): Boolean = if (events.isEmpty()) false else events.last().step == OWStep.SUCCESS

    fun failReason(): String? {
        if (events.isEmpty()) {
            return "null"
        }
        val last = events.last()
        if (!last.isSuccess()) {
            return last.msg
        }
        return "null"
    }

    fun isConnecting() = !isSucceed() && failReason() != null

    fun progress() = (events.last().step.ordinal + 1.0) / 7

    /**
     * 系统蓝牙已经连接 那么可能是大小和服务的问题
     * 系统蓝牙未连接 那么有可能手表和手机不再一起相隔很远需要用户确认
     */
    fun hfpConnected(): Boolean = events.find { it.step == OWStep.SYS_BT } != null

    fun connectResult(): String {
        //几点几分发起连接,什么原因连接失败
        val request = events.first()
        val last = events.last()
        if (last.step == OWStep.SUCCESS) {
            return "${request.time.showTime()} [${request.msg}] to connect -> result: [connected] at ${last.time.showTime()}"
        } else if (last.isSuccess()) {
            return "${request.time.showTime()} [${request.msg}] to connect -> connecting: step passed ${last.step}"
        }
        if (hfpConnected()) {
            //系统蓝牙已经连接
            return ""
        }
        //系统蓝牙 没链接  自动重连的时候 那么可能是在拉锯中
        return ""
    }
}

val IDLE = OWConnection("")

/**
 * 从捞出的日志中看连接不上的原因
 *
 * 1, 先检查蓝牙开关, 和 hfp连接状态, 最近一次连接如果hfp未连接上 那么提示用户是否设备再20米以内,是否隔墙了
 * 2, 检查1天内连接进程被杀次数 分别是那几天, 引导用户设置保活, 确认最近任务栏是否有加锁
 * 3, 检查是否是 KSC 协商问题 配对完前
 * 4, 判断 BB15 问题  连接失败, 如果是认证限制问题 就提示需要重置设备
 * 5, 判断 AA15 这种情况前提是BB15正常连接, 如果是连不上就提示用户开关手表蓝牙, 如果是认证限制问题 就提示需要重置设备
 * 6, 看是否是主动触发的断开 ssoid不对 ,获取用户信息失败
 */

lateinit var globalContext: Context

val sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

interface OWDoctor {

    fun diagnoses(clinic: Clinic): Boolean

    @Composable
    fun Suggestion()
}

abstract class BaseOWDoctor : OWDoctor {
    var clinic: Clinic? = null
    var patient: BluetoothDevice? = null
    override fun diagnoses(clinic: Clinic): Boolean {
        this.clinic = clinic
        patient = clinic.patient
        //蓝牙没关/系统蓝牙没连上 要提示打开蓝牙 ,由用户确认设备是否在附近
        return true
    }
}

class NearByDoctor : BaseOWDoctor() {
    override fun diagnoses(clinic: Clinic): Boolean {
        super.diagnoses(clinic)
        //蓝牙没关/系统蓝牙没连上 要提示打开蓝牙 ,由用户确认设备是否在附近
        val hfpConnected = false
        //最近一次 有连上过就不提示
        //已经绑定但是hfp没连接 需要看设备是否在附件
        return !hfpConnected
    }

    @Preview
    @Composable
    override fun Suggestion() {
        // 系统蓝牙没打开  系统蓝牙一直没连上,提示是否手表在15米内
        Column {
            Spacer(modifier = Modifier.size(20.dp))
            Text(text = "蓝牙已打开")
            val hfpConnected = false
            if (!hfpConnected) {
                //发现最近几次连接都没能连上
                Text(text = "请确认最近两次连接设备${patient?.address}是否在手机附近20米内")
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = {
                        println("手表不再附近")
                    }) {
                        Text(text = "手表不在附近")
                    }
                    Button(onClick = {
                        println("手表在附近,下一个检测")
                        clinic?.next()
                    }) {
                        Text(text = "手表在附近")
                    }
                }
            } else {
                //判断是否在预览环境
                LocalInspectionMode.current
                Text(text = "${patient?.address} 已经连接")
            }
        }

    }
}

class BoundStateDoctor : BaseOWDoctor() {
    override fun diagnoses(clinic: Clinic): Boolean {
        super.diagnoses(clinic)
        //蓝牙没关/系统蓝牙没连上 要提示打开蓝牙 ,由用户确认设备是否在附近
        return !patient!!.isBounded
    }

    @Preview(name = "BoundStateDoctor")
    @Composable
    override fun Suggestion() {
        //最近一次 有连上过就不提示
        // 系统蓝牙没打开  系统蓝牙一直没连上,提示是否手表在15米内
        Column {
            Spacer(modifier = Modifier.size(20.dp))
            var pinCode by remember {
                mutableStateOf<Boolean>(true)
            }
            if (!pinCode) {
                //建议用户开关手机手表蓝牙 如果还不行各自重启，抓蓝牙日志
                Column {
                    Text(text = "蓝牙通道可能存在问题")
                    Text(text = "建议执行以下操作")
                    Text(text = "关手机蓝牙然后关手表蓝牙，然后开手机蓝牙再开手表蓝牙，看能否自动连上/重新配对")
                    Text(text = "如果上述步骤还不行，那么重启手机和手表")
                    Text(text = "如果上述步骤还不行，那么重启手机和重置手表")
                    Text(text = "帮助改进连接体验引导抓双端蓝牙日志")
                }
            } else {
                Text(text = "设备未绑定")
                Text(text = "手机手表是否都弹出pin码")
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = {
                        println("都弹出")
                        clinic?.next()
                    }) {
                        Text(text = "都弹出")
                    }
                    Button(onClick = {
                        println("手表没弹出")
                        pinCode = false

                    }) {
                        Text(text = "没有都弹出")
                    }
                }
            }
        }

    }
}