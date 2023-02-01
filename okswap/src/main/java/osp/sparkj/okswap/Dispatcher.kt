package osp.sparkj.okswap

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.*

class Dispatcher<Request, Response> constructor() {
    /**
     * The maximum number of requests to execute concurrently. Above this requests queue in memory,
     * waiting for the running calls to complete.
     *
     * If more than [maxRequests] requests are in flight when this is invoked, those requests will
     * remain in flight.
     */
    @get:Synchronized
    var maxRequests = 6
        set(maxRequests) {
            require(maxRequests >= 1) { "max < 1: $maxRequests" }
            synchronized(this) {
                field = maxRequests
            }
            promoteAndExecute()
        }


    /**
     * A callback to be invoked each time the dispatcher becomes idle (when the number of running
     * calls returns to zero).
     *
     * Note: The time at which a [call][Call] is considered idle is different depending on whether it
     * was run [asynchronously][Call.enqueue] or [synchronously][Call.execute]. Asynchronous calls
     * become idle after the [onResponse][Callback.onResponse] or [onFailure][Callback.onFailure]
     * callback has returned. Synchronous calls become idle once [execute()][Call.execute] returns.
     * This means that if you are doing synchronous calls the network layer will not truly be idle
     * until every returned [Response] has been closed.
     */
    @set:Synchronized
    @get:Synchronized
    var idleCallback: Runnable? = null

    private var executorServiceOrNull: CoroutineScope? = null

    @get:Synchronized
    @get:JvmName("executorService")
    val executorScope: CoroutineScope
        get() {
            if (executorServiceOrNull == null) {
                executorServiceOrNull = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            }
            return executorServiceOrNull!!
        }

    /** Ready async calls in the order they'll be run. */
    private val readyAsyncCalls = ArrayDeque<RealCall<Request, Response>.AsyncCall>()

    /** Running asynchronous calls. Includes canceled calls that haven't finished yet. */
    private val runningAsyncCalls = ArrayDeque<RealCall<Request, Response>.AsyncCall>()

    internal fun enqueue(call: RealCall<Request, Response>.AsyncCall) {
        synchronized(this) {
            readyAsyncCalls.add(call)
        }
        promoteAndExecute()
    }

    /**
     * Cancel all calls currently enqueued or executing. Includes calls executed both
     * [synchronously][Call.execute] and [asynchronously][Call.enqueue].
     */
    @Synchronized
    fun cancelAll() {
        for (call in readyAsyncCalls) {
            call.call.cancel()
        }
        for (call in runningAsyncCalls) {
            call.call.cancel()
        }
    }

    /**
     * Promotes eligible calls from [readyAsyncCalls] to [runningAsyncCalls] and runs them on the
     * executor service. Must not be called with synchronization because executing calls can call
     * into user code.
     *
     * @return true if the dispatcher is currently running calls.
     */
    private fun promoteAndExecute(): Boolean {
        val executableCalls = mutableListOf<RealCall<Request, Response>.AsyncCall>()
        val isRunning: Boolean
        synchronized(this) {
            val i = readyAsyncCalls.iterator()
            while (i.hasNext()) {
                val asyncCall = i.next()

                if (runningAsyncCalls.size >= this.maxRequests) break // Max capacity.

                i.remove()
                executableCalls.add(asyncCall)
                runningAsyncCalls.add(asyncCall)
            }
            isRunning = runningCallsCount() > 0
        }

        for (i in 0 until executableCalls.size) {
            val asyncCall = executableCalls[i]
            asyncCall.executeOn(executorScope)
        }

        return isRunning
    }

    /** Used by [AsyncCall.run] to signal completion. */
    internal fun finished(call: RealCall<Request, Response>.AsyncCall) {
        finished(runningAsyncCalls, call)
    }

    private fun <T> finished(calls: Deque<T>, call: T) {
        val idleCallback: Runnable?
        synchronized(this) {
            if (!calls.remove(call)) throw AssertionError("Call wasn't in-flight!")
            idleCallback = this.idleCallback
        }

        val isRunning = promoteAndExecute()

        if (!isRunning && idleCallback != null) {
            idleCallback.run()
        }
    }

    /** Returns a snapshot of the calls currently awaiting execution. */
    @Synchronized
    fun queuedCalls(): List<Call<Request, Response>> {
        return Collections.unmodifiableList(readyAsyncCalls.map { it.call })
    }

    @Synchronized
    fun queuedCallsCount(): Int = readyAsyncCalls.size

    @Synchronized
    fun runningCallsCount(): Int = runningAsyncCalls.size

    fun cancelExecutor() {
        executorScope.cancel()
    }
}