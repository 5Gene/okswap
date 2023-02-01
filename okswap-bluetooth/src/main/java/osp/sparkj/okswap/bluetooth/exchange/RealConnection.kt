package osp.sparkj.okswap.bluetooth.exchange


//创建连接 返回code实现收发数据

interface Connection {

    fun connect()

    fun newCode(): ExchangeCode

}

class RealConnection {

}