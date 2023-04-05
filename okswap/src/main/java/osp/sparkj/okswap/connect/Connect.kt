package osp.sparkj.okswap.connect

/**
 * @author yun.
 * @date 2023/3/17
 * @des [一句话描述]
 * @since [https://github.com/5hmlA]
 * <p><a href="https://github.com/5hmlA">github</a>
 */

interface Nest {
    fun handle(pigeon: Pigeon)
}


interface Pigeon {
    fun fly(byteArray: ByteArray)
}

interface Address {}

/**
 * 驿站
 * 两个驿站之间训练出信鸽
 */
interface Relay<ADDR : Address> {

    var address: ADDR
    suspend fun train(peer: Relay<ADDR>): Pigeon
}