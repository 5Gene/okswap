package osp.sparkj.more.okswap.interceptors

import osp.sparkj.more.okswap.oklog

class RetryInterceptor<Request, Response> : Interceptor<Request, Response> {

    override suspend fun intercept(chain: Interceptor.Chain<Request, Response>): Response {
        var exception: Exception? = null
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