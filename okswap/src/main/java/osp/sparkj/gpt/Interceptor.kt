import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.*

// 定义拦截器接口，支持泛型 Request 和 Response
interface Interceptor<Request, Response> {
    suspend fun intercept(chain: Chain<Request, Response>): Response
}

// 定义拦截器链接口，支持泛型 Request 和 Response
interface Chain<Request, Response> {
    val context: CoroutineContext // 协程上下文
    val request: Request // 当前的请求对象
    suspend fun proceed(request: Request = this.request): Response // 执行下一个拦截器
    fun <T : CoroutineContext.Element> put(key: CoroutineContext.Key<T>, value: T) // 添加共享数据到上下文
    fun <T : CoroutineContext.Element> get(key: CoroutineContext.Key<T>): T? // 获取上下文中的共享数据
}

// 拦截器链的具体实现
private class RealInterceptorChain<Request, Response>(
    private val interceptors: List<Interceptor<Request, Response>>,
    private val index: Int,
    override val request: Request,
    override val context: CoroutineContext
) : Chain<Request, Response> {
    // 调用下一个拦截器
    override suspend fun proceed(request: Request): Response {
        if (index >= interceptors.size) throw AssertionError("No more interceptors")

        // 创建下一个拦截器链实例，传递更新后的请求
        val next = RealInterceptorChain(interceptors, index + 1, request, context)
        return interceptors[index].intercept(next)
    }

    // 在上下文中存储数据
    override fun <T : CoroutineContext.Element> put(key: CoroutineContext.Key<T>, value: T) {
        (context as? MutableCoroutineContext)?.put(key, value)
    }

    // 从上下文中获取数据
    override fun <T : CoroutineContext.Element> get(key: CoroutineContext.Key<T>): T? = context[key]
}

// 扩展协程上下文，支持动态添加数据
private class MutableCoroutineContext : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<MutableCoroutineContext>

    private val data = ConcurrentHashMap<CoroutineContext.Key<*>, Any>()

    fun <T:CoroutineContext.Element> put(key: CoroutineContext.Key<T>, value: T) {
        data[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override operator fun <T:CoroutineContext.Element> get(key: CoroutineContext.Key<T>): T? {
        return data[key] as? T
    }
}

// 示例拦截器实现：记录日志
class LoggingInterceptor<Request, Response> : Interceptor<Request, Response> {
    override suspend fun intercept(chain: Chain<Request, Response>): Response {
        println("LoggingInterceptor: Request = ${chain.request}")
        val response = chain.proceed(chain.request) // 继续下一个拦截器
        println("LoggingInterceptor: Response = $response")
        return response
    }
}

// 示例拦截器实现：修改请求数据
class ModifyRequestInterceptor : Interceptor<String, String> {
    override suspend fun intercept(chain: Chain<String, String>): String {
        println("ModifyRequestInterceptor: Modifying request")
        val modifiedRequest = "${chain.request}-Modified"
        return chain.proceed(modifiedRequest)
    }
}

// 示例拦截器实现：终止链并返回结果
class FinalInterceptor : Interceptor<String, String> {
    override suspend fun intercept(chain: Chain<String, String>): String {
        println("FinalInterceptor: Generating response")
        return "Response for ${chain.request}"
    }
}

// 使用示例
suspend fun main() {
    val interceptors = listOf(
        LoggingInterceptor<String, String>(),
        ModifyRequestInterceptor(),
        FinalInterceptor()
    )

    // 构造初始拦截器链
    val initialContext = MutableCoroutineContext()
    val initialRequest = "InitialRequest"
    val chain = RealInterceptorChain(interceptors, 0, initialRequest, initialContext)

    // 执行链路
    val result = chain.proceed()
    println("Final result: $result")
}
