package osp.sparkj.more


/**
 * @author yun.
 * @date 2023/1/5
 * @des [一句话描述]
 * @since [https://github.com/mychoices]
 * <p><a href="https://github.com/mychoices">github</a>
 */
fun interface Interceptor<Request, Response> {


    fun intercept(chain: Chain<Request, Response>): Response

    companion object {
        /**
         * Constructs an interceptor for a lambda. This compact syntax is most useful for inline
         * interceptors.
         *
         * ```
         * val interceptor = Interceptor { chain: Interceptor.Chain ->
         *     chain.proceed(chain.request())
         * }
         * ```
         */
        inline operator fun <Request, Response> invoke(crossinline block: (chain: Chain<Request, Response>) -> Response): Interceptor<Request, Response> =
            Interceptor { block(it) }
    }

    interface Chain<Request, Response> {
        fun request(): Request

        @Throws(Exception::class)
        fun proceed(request: Request): Response

    }
}

class RealInterceptorChain<Request, Response>(
    private val interceptors: List<Interceptor<Request, Response>>,
    private val index: Int,
    internal val request: Request
) : Interceptor.Chain<Request, Response> {

    internal fun copy(
        index: Int = this.index,
        request: Request = this.request
    ) = RealInterceptorChain(this.interceptors, index, request)

    override fun request(): Request {
        return request
    }

    override fun proceed(request: Request): Response {
        check(index < interceptors.size)
        val next = copy(index + 1, request)
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}