package osp.sparkj.gpt.answer1

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 *
 思路
职责链模式：

创建一个拦截器链接口，让每个拦截器都可以执行自己的逻辑并将处理结果传递到下一个拦截器。
类似于 okhttp 中的 Interceptor 和 RealInterceptorChain，每个拦截器实现一个 intercept 方法，并通过链式调用使请求传递到下一个拦截器。
协程上下文支持：

为了在不同的拦截器中共享数据，可以使用协程上下文。在拦截器链的实现中，我们可以将协程上下文与拦截器链的生命周期绑定，从而让上下文在整个链路中共享。
并发支持：

使用线程安全的数据结构，如 ConcurrentHashMap 或 ThreadLocal，来确保多线程环境下的安全性。
如果不同的协程需要隔离上下文，可以考虑 ThreadLocal，否则使用 ConcurrentHashMap 来存储共享数据。
数据共享与访问控制：

通过定义私有的拦截器链实现类（例如 RealInterceptorChain），限制外部访问。
提供数据访问接口，允许各个拦截器访问和修改上下文数据。

 解释
职责链实现：

Interceptor 接口定义了 intercept 方法，每个拦截器通过实现此接口来执行不同的逻辑。
RealInterceptorChain 类实现了 Chain 接口，并且以递归的方式调用每个拦截器的 intercept 方法，确保链式调用。
协程上下文和共享数据：

RealInterceptorChain 中包含了 CoroutineContext 和一个 ConcurrentHashMap 类型的 sharedData。
addData 和 getData 方法用于在上下文中添加和获取数据，确保各拦截器可以访问共享数据。
并发安全：

sharedData 使用了 ConcurrentHashMap，保证多线程环境下的数据安全。
由于每个拦截器链实例的 sharedData 都是相同的引用，因此各个拦截器之间可以安全共享数据。
优势：

通过这种方式，可以在不显式传递数据的情况下让拦截器共享状态。
使用协程上下文，让代码可以在协程环境中运行，并支持异步逻辑。
 */
// 定义拦截器接口
interface Interceptor {
    suspend fun intercept(chain: Chain): Result
}

// 定义返回结果
data class Result(val message: String)

// 定义拦截器链接口
interface Chain {
    val context: CoroutineContext // 协程上下文
    suspend fun proceed(): Result // 继续到下一个拦截器
    fun addData(key: String, value: Any) // 添加共享数据
    fun getData(key: String): Any? // 获取共享数据
}

// 拦截器链的具体实现
class RealInterceptorChain(
    private val interceptors: List<Interceptor>,
    private val index: Int,
    override val context: CoroutineContext = Dispatchers.Default,
    private val sharedData: ConcurrentHashMap<String, Any> = ConcurrentHashMap() // 共享数据
) : Chain {

    override suspend fun proceed(): Result {
        // 检查是否达到链尾
        if (index >= interceptors.size) throw AssertionError("No more interceptors")

        // 创建下一个拦截器链实例
        val next = RealInterceptorChain(interceptors, index + 1, context, sharedData)

        // 调用当前拦截器
        return interceptors[index].intercept(next)
    }

    // 添加数据
    override fun addData(key: String, value: Any) {
        sharedData[key] = value
    }

    // 获取数据
    override fun getData(key: String): Any? = sharedData[key]
}

// 一个示例拦截器
class ExampleInterceptor : Interceptor {
    override suspend fun intercept(chain: Chain): Result {
        println("ExampleInterceptor before proceed")

        // 添加数据到上下文
        chain.addData("ExampleData", "This is example data")

        // 调用下一个拦截器
        val result = chain.proceed()

        println("ExampleInterceptor after proceed with result: ${result.message}")
        return result
    }
}

// 使用示例
suspend fun main() {
    val interceptors = listOf(
        ExampleInterceptor(),
        object : Interceptor {
            override suspend fun intercept(chain: Chain): Result {
                val data = chain.getData("ExampleData")
                println("AnonymousInterceptor got data: $data")
                return chain.proceed()
            }
        },
        object : Interceptor {
            override suspend fun intercept(chain: Chain): Result {
                return Result("Final result")
            }
        }
    )

    val chain = RealInterceptorChain(interceptors, 0)
    val result = chain.proceed()
    println("Final result: ${result.message}")
}
