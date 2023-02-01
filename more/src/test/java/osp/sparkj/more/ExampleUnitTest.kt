package osp.sparkj.more

import android.os.SystemClock
import kotlinx.coroutines.*
import org.junit.Test

import osp.sparkj.more.okswap.OkClient
import osp.sparkj.more.okswap.interceptors.Interceptor
import osp.sparkj.more.okswap.oklog
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
            println("========= ${throwable.message}")
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val client = OkClient.Builder<Request,Response>()
            .addInterceptor(Interceptor<Request, Response> { it.proceed(it.request()) })
            .addNetworkInterceptor(Interceptor<Request, Response> {
                println("${Thread.currentThread().id} >> ${Thread.currentThread().name} 111 ")
                val resp = it.proceed(it.request())
                Response("我串改了 > ${resp.body}")
            })
            .addNetworkInterceptor(Interceptor<Request, Response> {
                println("${Thread.currentThread().id} >> ${Thread.currentThread().name} 222 ")
                it.request().url.oklog()
                delay(2000)
                Response(it.request().url.substring(it.request().url.length-1))
                it.proceed(it.request())
            })
            .addNetworkInterceptor(Interceptor<Request, Response> {
                suspendCoroutine<Response> {
                    it.resumeWithException(RuntimeException("主动报错"))
                    it.resumeWithException(RuntimeException("主动报错"))
                }
            })
            .build()

        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试1")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }

//        presstest(scope, handler, client)
        println("response.body")
        runBlocking {
            delay(26000)
        }
    }

    private fun presstest(
        scope: CoroutineScope,
        handler: CoroutineExceptionHandler,
        client: OkClient<Request, Response>
    ) {
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试1")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试2")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试3")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试4")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试5")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试6")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试7")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试8")).enqueue()
                println("${System.currentTimeMillis() / 1000} == ${response.body}")
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
    }
}

data class Request(val url: String)
data class Response(val body: String)