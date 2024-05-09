package com.heytap.health.owconnect

import kotlinx.coroutines.flow.MutableStateFlow


/**
 * 记录完整的连接流程 连接原因 连接各个阶段 结果
 * 连接日志保存到数据库
 * 内存只留最近一次的连接记录  蓝牙连上没  连接失败过程中蓝牙连上没
 */
object OWWatchDog {

    /**
     * 最近的连接记录
     */
    val recentEvents = mutableListOf<OWConnection>()

    val dog = MutableStateFlow<OWConnection>(IDLE)

    fun toConnect(mac: String, reason: String) {
        dog.tryEmit(dog.value.copy(mac = mac,events = listOf(ConnectEvent(time = System.currentTimeMillis(), step = OWStep.REQUEST, msg = reason))))
    }

    fun hfpOK() {
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.SYS_BT, msg = "HFP OK")))
    }

    fun kscOK() {
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.KSC, msg = "KSC OK")))
    }

    fun kscFail(code: Int, reason: String) {
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.KSC, code = code, msg = reason)))
        recentEvents.add(dog.value)
    }

    fun bb15OK() {
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.BB15, msg = "BB15 OK")))
    }

    fun bb15Fail(mac: String, code: Int, reason: String) {
        if (dog.value.mac != mac) {
            //不是同一个设备的连接失败,那么就是主动断开的,此错误忽略
            return
        }
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.BB15, code = code, msg = reason)))
        recentEvents.add(dog.value)
    }

    fun aa15OK() {
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.AA15, msg = "AA15 OK")))
    }

    fun aa15Fail(mac: String, code: Int, reason: String) {
        if (dog.value.mac != mac) {
            //不是同一个设备的连接失败,那么就是主动断开的,此错误忽略
            return
        }
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.AA15, code = code, msg = reason)))
        recentEvents.add(dog.value)
    }

    fun connected() {
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.SUCCESS, msg = "connected")))
        recentEvents.add(dog.value)
    }

    /**
     * 已连接出现的中断
     * 1 蓝牙断开连接
     * 2 BB15断开
     * 3 AA15断开
     * 4 非切换设备的断开
     */
    fun interrupted(mac: String, reason: String) {
        if (dog.value.mac != mac) {
            //不是同一个设备的连接失败,那么就是主动断开的,此错误忽略
            return
        }
        //可能是拉锯导致 这里可以计算上次连接到当前断开的时长
        dog.tryEmit(dog.value.copy(events = dog.value.events + ConnectEvent(time = System.currentTimeMillis(), step = OWStep.INTERRUPTED, msg = reason)))
        recentEvents.add(dog.value)
    }
}