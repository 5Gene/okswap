package osp.sparkj.okswap.interceptors

import osp.sparkj.okswap.oklog

class RetryInterceptor<Request, Response> : Interceptor<Request, Response> {

    override suspend fun intercept(chain: Interceptor.Chain<Request, Response>): Response {
        var exception: Exception? = null
        val cha = chain as RealInterceptorChain
        cha.client()
        for (i in 0..chain.client().retryTimesOnError) {
            try {
                return chain.proceed(chain.request())
            } catch (e: Exception) {
                e.printStackTrace()
                "intercept error: ${e.message} > already retry $i".oklog()
                exception = e
            }
        }
        throw exception ?: java.lang.RuntimeException("RetryInterceptor")
    }
}