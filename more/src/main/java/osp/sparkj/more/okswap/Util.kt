package osp.sparkj.more.okswap

import java.util.*
import java.util.concurrent.ThreadFactory

fun <T> List<T>.toImmutableList(): List<T> {
    return Collections.unmodifiableList(this)
}


fun threadFactory(
    name: String,
    daemon: Boolean
): ThreadFactory = ThreadFactory { runnable ->
    Thread(runnable, name).apply {
        isDaemon = daemon
    }
}

inline fun threadName(name: String, block: () -> Unit) {
    val currentThread = Thread.currentThread()
    val oldName = currentThread.name
    currentThread.name = name
    try {
        block()
    } finally {
        currentThread.name = oldName
    }
}

fun String.oklog(){
    println(this)
}