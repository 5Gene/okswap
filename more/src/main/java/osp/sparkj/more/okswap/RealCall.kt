package osp.sparkj.more.okswap

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import osp.sparkj.more.okswap.interceptors.Interceptor
import osp.sparkj.more.okswap.interceptors.RealInterceptorChain
import osp.sparkj.more.okswap.interceptors.RetryInterceptor
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RealCall<Request, Response>(private val client: OkClient<Request, Response>, private val originRequest: Request) : Call<Request, Response> {
    @Volatile
    private var canceled = false
    private val executed = AtomicBoolean()

    override fun request() = originRequest

    override suspend fun enqueue() = suspendCancellableCoroutine<Response> {
        check(executed.compareAndSet(false, true)) { "Already Executed" }

        client.dispatcher.enqueue(AsyncCall(object : Callback<Response> {
            override fun onFailure(e: Exception) {
                it.resumeWithException(e)
            }

            override fun onResponse(response: Response) {
                it.resume(response)
            }
        }))
    }

    override fun cancel() {
        canceled = true
    }

    override fun isExecuted() = executed.get()

    override fun isCanceled() = canceled

    override fun clone() = RealCall(client, originRequest)

    internal inner class AsyncCall(private val responseCallback: Callback<Response>) {

        private val request: Request
            get() = originRequest

        val call: RealCall<Request, Response>
            get() = this@RealCall

        /**
         * Attempt to enqueue this async call on [executorScope]. This will attempt to clean up
         * if the executor has been shut down by reporting the call as failed.
         */
        fun executeOn(executorScope: CoroutineScope) {
            val handler = CoroutineExceptionHandler { _, exception ->
                "executeOn error $exception".oklog()
                responseCallback.onFailure(java.lang.RuntimeException(exception))
            }
            executorScope.launch(handler) {
                run()
            }
        }

        private suspend fun run() {
            threadName("OkClient-${request}") {
                try {
                    val response = getResponseWithInterceptorChain()
                    responseCallback.onResponse(response)
                } catch (e: Exception) {
                    cancel()
                    responseCallback.onFailure(e)
                } finally {
                    client.dispatcher.finished(this)
                }
            }
        }
    }

    @Throws(IOException::class)
    internal suspend fun getResponseWithInterceptorChain(): Response {
        // Build a full stack of interceptors.
        val interceptors = mutableListOf<Interceptor<Request, Response>>()
        interceptors += client.interceptors
        interceptors += RetryInterceptor()
        interceptors += client.networkInterceptors

        val chain = RealInterceptorChain(
            okClient = client,
            interceptors = interceptors,
            index = 0,
            request = originRequest,
        )
        return chain.proceed(originRequest)
    }
}