package osp.sparkj.more.okswap

import osp.sparkj.more.okswap.interceptors.Interceptor
import java.net.ProxySelector
import java.util.*
import java.util.concurrent.ExecutorService


/**
 * Factory for [calls][Call], which can be used to send HTTP requests and read their responses.
 *
 * ## OkHttpClients Should Be Shared
 *
 * OkHttp performs best when you create a single `OkHttpClient` instance and reuse it for all of
 * your HTTP calls. This is because each client holds its own connection pool and thread pools.
 * Reusing connections and threads reduces latency and saves memory. Conversely, creating a client
 * for each request wastes resources on idle pools.
 *
 * Use `new OkHttpClient()` to create a shared instance with the default settings:
 *
 * ```
 * // The singleton HTTP client.
 * public final OkHttpClient client = new OkHttpClient();
 * ```
 *
 * Or use `new OkHttpClient.Builder()` to create a shared instance with custom settings:
 *
 * ```
 * // The singleton HTTP client.
 * public final OkHttpClient client = new OkHttpClient.Builder()
 *     .addInterceptor(new HttpLoggingInterceptor())
 *     .cache(new Cache(cacheDir, cacheSize))
 *     .build();
 * ```
 *
 * ## Customize Your Client With newBuilder()
 *
 * You can customize a shared OkHttpClient instance with [newBuilder]. This builds a client that
 * shares the same connection pool, thread pools, and configuration. Use the builder methods to
 * configure the derived client for a specific purpose.
 *
 * This example shows a call with a short 500 millisecond timeout:
 *
 * ```
 * OkHttpClient eagerClient = client.newBuilder()
 *     .readTimeout(500, TimeUnit.MILLISECONDS)
 *     .build();
 * Response response = eagerClient.newCall(request).execute();
 * ```
 *
 * ## Shutdown Isn't Necessary
 *
 * The threads and connections that are held will be released automatically if they remain idle. But
 * if you are writing a application that needs to aggressively release unused resources you may do
 * so.
 *
 * Shutdown the dispatcher's executor service with [shutdown()][ExecutorService.shutdown]. This will
 * also cause future calls to the client to be rejected.
 *
 * ```
 * client.dispatcher().executorService().shutdown();
 * ```
 *
 * Clear the connection pool with [evictAll()][ConnectionPool.evictAll]. Note that the connection
 * pool's daemon thread may not exit immediately.
 *
 * ```
 * client.connectionPool().evictAll();
 * ```
 *
 * If your client has a cache, call [close()][Cache.close]. Note that it is an error to create calls
 * against a cache that is closed, and doing so will cause the call to crash.
 *
 * ```
 * client.cache().close();
 * ```
 *
 * OkHttp also uses daemon threads for HTTP/2 connections. These will exit automatically if they
 * remain idle.
 */
open class OkClient<Request,Response> internal constructor(
    builder: Builder<Request,Response>
) : Cloneable, Call.Factory<Request,Response> {

    init {
        mutableListOf<String>()
    }

    @get:JvmName("dispatcher") val dispatcher: Dispatcher<Request, Response> = builder.dispatcher


    /**
     * Returns an immutable list of interceptors that observe the full span of each call: from before
     * the connection is established (if any) until after the response source is selected (either the
     * origin server, cache, or both).
     */
    @get:JvmName("interceptors") val interceptors: List<Interceptor<Request, Response>> =
        builder.interceptors.toImmutableList()

    /**
     * Returns an immutable list of interceptors that observe a single network request and response.
     * These interceptors must call [Interceptor.Chain.proceed] exactly once: it is an error for
     * a network interceptor to short-circuit or repeat a network request.
     */
    @get:JvmName("networkInterceptors") val networkInterceptors: List<Interceptor<Request, Response>> =
        builder.networkInterceptors.toImmutableList()

    @get:JvmName("retryOnConnectionFailure") val retryTimesOnError: Int =
        builder.retryTimesOnError


    /**
     * Default call timeout (in milliseconds). By default there is no timeout for complete calls, but
     * there is for the connect, write, and read actions within a call.
     */
    @get:JvmName("callTimeoutMillis") val callTimeoutMillis: Int = builder.callTimeout

    /** Default connect timeout (in milliseconds). The default is 10 seconds. */
    @get:JvmName("connectTimeoutMillis") val connectTimeoutMillis: Int = builder.connectTimeout

    /** Default read timeout (in milliseconds). The default is 10 seconds. */
    @get:JvmName("readTimeoutMillis") val readTimeoutMillis: Int = builder.readTimeout

    /** Default write timeout (in milliseconds). The default is 10 seconds. */
    @get:JvmName("writeTimeoutMillis") val writeTimeoutMillis: Int = builder.writeTimeout

    constructor() : this(Builder())

    /** Prepares the [request] to be executed at some point in the future. */
    override fun newCall(request: Request): Call<Request,Response> = RealCall(this, request)

    open fun newBuilder(): Builder<Request,Response> = Builder(this)

    @JvmName("-deprecated_dispatcher")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "dispatcher"),
        level = DeprecationLevel.ERROR)
    fun dispatcher(): Dispatcher<Request, Response> = dispatcher

    @JvmName("-deprecated_interceptors")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "interceptors"),
        level = DeprecationLevel.ERROR)
    fun interceptors(): List<Interceptor<Request, Response>> = interceptors

    @JvmName("-deprecated_networkInterceptors")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "networkInterceptors"),
        level = DeprecationLevel.ERROR)
    fun networkInterceptors(): List<Interceptor<Request, Response>> = networkInterceptors

    @JvmName("-deprecated_retryOnConnectionFailure")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "retryOnConnectionFailure"),
        level = DeprecationLevel.ERROR)
    fun retryTimesOnError(): Int = retryTimesOnError

    @JvmName("-deprecated_callTimeoutMillis")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "callTimeoutMillis"),
        level = DeprecationLevel.ERROR)
    fun callTimeoutMillis(): Int = callTimeoutMillis

    @JvmName("-deprecated_connectTimeoutMillis")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "connectTimeoutMillis"),
        level = DeprecationLevel.ERROR)
    fun connectTimeoutMillis(): Int = connectTimeoutMillis

    @JvmName("-deprecated_readTimeoutMillis")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "readTimeoutMillis"),
        level = DeprecationLevel.ERROR)
    fun readTimeoutMillis(): Int = readTimeoutMillis

    @JvmName("-deprecated_writeTimeoutMillis")
    @Deprecated(
        message = "moved to val",
        replaceWith = ReplaceWith(expression = "writeTimeoutMillis"),
        level = DeprecationLevel.ERROR)
    fun writeTimeoutMillis(): Int = writeTimeoutMillis

    class Builder<Request,Response> constructor() {
        internal var dispatcher: Dispatcher<Request, Response> = Dispatcher()
        internal val interceptors: MutableList<Interceptor<Request, Response>> = mutableListOf()
        internal val networkInterceptors: MutableList<Interceptor<Request, Response>> = mutableListOf()
        internal var retryTimesOnError = 3
        internal var callTimeout = 0
        internal var connectTimeout = 10_000
        internal var readTimeout = 10_000
        internal var writeTimeout = 10_000

        internal constructor(okHttpClient: OkClient<Request,Response>) : this() {
            this.dispatcher = okHttpClient.dispatcher
            this.interceptors += okHttpClient.interceptors
            this.networkInterceptors += okHttpClient.networkInterceptors
            this.retryTimesOnError = okHttpClient.retryTimesOnError
            this.callTimeout = okHttpClient.callTimeoutMillis
            this.connectTimeout = okHttpClient.connectTimeoutMillis
            this.readTimeout = okHttpClient.readTimeoutMillis
            this.writeTimeout = okHttpClient.writeTimeoutMillis
        }

        /**
         * Sets the dispatcher used to set policy and execute asynchronous requests. Must not be null.
         */
        fun dispatcher(dispatcher: Dispatcher<Request, Response>) = apply {
            this.dispatcher = dispatcher
        }

        /**
         * Returns a modifiable list of interceptors that observe the full span of each call: from
         * before the connection is established (if any) until after the response source is selected
         * (either the origin server, cache, or both).
         */
        fun interceptors(): MutableList<Interceptor<Request, Response>> = interceptors

        fun addInterceptor(interceptor: Interceptor<Request, Response>) = apply {
            interceptors += interceptor
        }

        @JvmName("-addInterceptor") // Prefix with '-' to prevent ambiguous overloads from Java.
        inline fun addInterceptor(crossinline block: (chain: Interceptor.Chain<Request,Response>) -> Response) =
            addInterceptor(Interceptor { chain -> block(chain) })

        /**
         * Returns a modifiable list of interceptors that observe a single network request and response.
         * These interceptors must call [Interceptor.Chain.proceed] exactly once: it is an error for a
         * network interceptor to short-circuit or repeat a network request.
         */
        fun networkInterceptors(): MutableList<Interceptor<Request, Response>> = networkInterceptors

        fun addNetworkInterceptor(interceptor: Interceptor<Request, Response>) = apply {
            networkInterceptors += interceptor
        }

        @JvmName("-addNetworkInterceptor") // Prefix with '-' to prevent ambiguous overloads from Java.
        inline fun addNetworkInterceptor(crossinline block: (chain: Interceptor.Chain<Request,Response>) -> Response) =
            addNetworkInterceptor(Interceptor { chain -> block(chain) })

        /**
         * Configure a single client scoped listener that will receive all analytic events for this
         * client.
         *
         * @see EventListener for semantics and restrictions on listener implementations.
         */
        fun eventListener(eventListener: EventListener) = apply {
//            this.eventListenerFactory = eventListener.asFactory()
        }

        /**
         * Configure this client to retry or not when a connectivity problem is encountered. By default,
         * this client silently recovers from the following problems:
         *
         * * **Unreachable IP addresses.** If the URL's host has multiple IP addresses,
         *   failure to reach any individual IP address doesn't fail the overall request. This can
         *   increase availability of multi-homed services.
         *
         * * **Stale pooled connections.** The [ConnectionPool] reuses sockets
         *   to decrease request latency, but these connections will occasionally time out.
         *
         * * **Unreachable proxy servers.** A [ProxySelector] can be used to
         *   attempt multiple proxy servers in sequence, eventually falling back to a direct
         *   connection.
         *
         * Set this to false to avoid retrying requests when doing so is destructive. In this case the
         * calling application should do its own recovery of connectivity failures.
         */
        fun retryOnConnectionFailure(retryTimesOnError: Int) = apply {
            this.retryTimesOnError = retryTimesOnError
        }


        fun build(): OkClient<Request,Response> = OkClient<Request,Response>(this)
    }
}