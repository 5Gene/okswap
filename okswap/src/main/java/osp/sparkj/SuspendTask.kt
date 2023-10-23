package osp.sparkj

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine


val handles = mutableMapOf<String, Channel<(Continuation<in Any>) -> Unit>>()


class ThreadScope(
    val name: String
) : CoroutineScope, Closeable {
    override fun close() {
        coroutineContext.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val coroutineContext: CoroutineContext
        get() = CoroutineName(name) + SupervisorJob() + Dispatchers.IO.limitedParallelism(1)
}

@OptIn(ExperimentalContracts::class)
public suspend inline fun <T> suspendTaskCoroutine(
    threadName: String,
    crossinline block: (Continuation<T>) -> Unit
): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return suspendCoroutine { continuation ->
        val a = handles[""]
        val channel = handles.getOrDefault(threadName,
            Channel<(Continuation<T>) -> Unit>().apply {
                ThreadScope(threadName).launch {
                    while (isActive) {
                        val running = receive()
                        running(continuation)
                    }
                }
            }
        )
        channel.send(block)
    }
}


suspend fun test() = suspendCoroutine<Unit> { }

fun main() = runBlocking<Unit> {
    val channel = Channel<String>()
    launch {
        channel.send("A1")
        channel.send("A2")
        log("A done")
    }
    launch {
        channel.send("B1")
        log("B done")
    }
    launch {
        while (true) {
            val x = channel.receive()
            log(x)
            delay(1000)
        }
    }
}

fun log(message: Any?) {
    println("[${Thread.currentThread().name}] $message")
}
