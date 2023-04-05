package osp.sparkj.okswap.connect

/**
 * @author yun.
 * @date 2023/3/17
 * @des [送信要 先通过驿丞 找到 驿站 驿站提供 信鸽 不同信件 不同信鸽 起飞 送信 国省市县]
 * @since [https://github.com/5hmlA]
 * <p><a href="https://github.com/5hmlA">github</a>
 */

interface Nest<LETTER> {
    fun handle(pigeon: Pigeon<LETTER>)
}


interface Pigeon<LETTER> {
    fun fly(LETTER: LETTER)
}

interface Address {}

/**
 * 驿站
 * 两个驿站之间训练出信鸽
 */
interface Relay<ADDR : Address, LETTER> {

    var address: ADDR

    suspend fun train(peer: Relay<ADDR, LETTER>): Pigeon<LETTER>

    fun pigeon(LETTER: LETTER): Pigeon<LETTER>

    fun discard()
}

/**
 * 驿丞
 */
object RelayDeputy {
//    fun <ADDR : Address, LETTER> findRelay(address: Address): Relay<ADDR, LETTER> {
//        //todo
//    }
}