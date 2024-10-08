package osp.sparkj.okswap.interceptors

import osp.sparkj.okswap.Call
import osp.sparkj.okswap.OkClient
import osp.sparkj.okswap.RealCall


/**
 * @author yun.
 * @date 2023/1/5
 * @des [一句话描述]
 * @since [https://github.com/5hmlA]
 * <p><a href="https://github.com/5hmlA">github</a>
 */
fun interface Interceptor<Request, Response> {

   suspend fun intercept(chain: Chain<Request, Response>): Response

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
//            object : Interceptor<Request, Response> {
//                override suspend fun intercept(chain: Chain<Request, Response>): Response {
//                    return block(chain)
//                }
//            }
    }

    interface Chain<Request, Response> {
        fun request(): Request
        fun call(): Call<Request, Response>

        @Throws(Exception::class)
        suspend fun proceed(request: Request): Response

        fun client(): OkClient<Request, Response>

    }
}

class RealInterceptorChain<Request, Response>(
    private val call: RealCall<Request, Response>,
    private val okClient: OkClient<Request, Response>,
    private val interceptors: List<Interceptor<Request, Response>>,
    private val index: Int,
    private val request: Request,
    private val map: MutableMap<String, Any>
) : Interceptor.Chain<Request, Response> {

    private fun copy(
        index: Int = this.index,
        request: Request = this.request
    ) = RealInterceptorChain(call, okClient, this.interceptors, index, request, map)

    fun map() = map

    override fun request(): Request {
        return request
    }

    override fun call() = call


    override suspend fun proceed(request: Request): Response {
        check(index < interceptors.size)
        val next = copy(index + 1, request)
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }

    override fun client(): OkClient<Request, Response> = okClient
}