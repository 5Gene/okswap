package osp.sparkj.more.okswap

import kotlinx.coroutines.flow.StateFlow


/**
 * A call is a request that has been prepared for execution. A call can be canceled. As this object
 * represents a single request/response pair (stream), it cannot be executed twice.
 */
interface Call<Request, Response> : Cloneable {

    /** Returns the original request that initiated this call. */
    fun request(): Request

    /**
     * Schedules the request to be executed at some point in the future.
     *
     * The [dispatcher][OkHttpClient.dispatcher] defines when the request will run: usually
     * immediately unless there are several other requests currently being executed.
     *
     * This client will later call back `responseCallback` with either an HTTP response or a failure
     * exception.
     *
     * @throws IllegalStateException when the call has already been executed.
     */
    suspend fun enqueue(): Response

    /** Cancels the request, if possible. Requests that are already complete cannot be canceled. */
    fun cancel()

    /**
     * Returns true if this call has been either [executed][execute] or [enqueued][enqueue]. It is an
     * error to execute a call more than once.
     */
    fun isExecuted(): Boolean

    fun isCanceled(): Boolean

    /**
     * Create a new, identical call to this one which can be enqueued or executed even if this call
     * has already been.
     */
    public override fun clone(): Call<Request, Response>

    fun interface Factory<Request, Response> {
        fun newCall(request: Request): Call<Request, Response>
    }
}
