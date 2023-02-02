package osp.sparkj

import kotlinx.coroutines.*
import org.junit.Test

import osp.sparkj.okswap.OkClient
import osp.sparkj.okswap.interceptors.Interceptor
import osp.sparkj.okswap.oklog
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
//                it.proceed(it.request())
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
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }

        presstest(scope, handler, client)
//        println("response.body")
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
                withContext(Dispatchers.Main) {
                    "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
                }


            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试2")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试3")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试4")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试5")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试6")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试7")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
        scope.launch(handler) {
            try {
                val response = client.newCall(Request("请求测试8")).enqueue()
                "${System.currentTimeMillis() / 1000} == ${response.body}".soutThread()
            } catch (e: Exception) {
                println("---------${e.message}")
            }
        }
    }
}

fun String.soutThread() {
    val threadname = "${Thread.currentThread().id} >> ${Thread.currentThread().name}"
    println("$threadname $this")
}

data class Request(val url: String)
data class Response(val body: String)