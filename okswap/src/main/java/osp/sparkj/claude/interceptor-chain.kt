package osp.sparkj.claude

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 关键点和原理：

核心接口设计：
Request 和 Response：定义基础的请求和响应接口
Interceptor：拦截器接口，包含 intercept 方法
Chain：责任链接口，提供共享数据的存取和链的传递

数据共享机制：
使用 ConcurrentHashMap 在 RealInterceptorChain 中存储共享数据
通过 getData 和 setData 方法提供类型安全的数据访问
共享数据在整个调用链中传递

幂等性控制：
RequestManager 使用 ConcurrentHashMap 存储活动请求
相同 id 的请求会返回同一个 Deferred<Response>
请求完成后自动从活动请求中移除

协程支持：
所有拦截器操作都是挂起函数
通过 CoroutineContext 传递协程上下文
使用 async 处理并发请求

线程安全：
使用 ConcurrentHashMap 确保并发安全
责任链本身是不可变的，每次调用创建新的链节点
 */
// 定义请求和响应的基础接口
interface Request {
    val id: String  // 用于幂等性控制
    val data: Any
}

interface Response {
    val data: Any
}

// 定义拦截器接口
interface Interceptor {
    suspend fun intercept(chain: Chain): Response
}

// 定义链接口
interface Chain {
    val request: Request
    val context: CoroutineContext
    
    // 获取共享数据
    fun <T> getData(key: String): T?
    
    // 设置共享数据
    fun setData(key: String, value: Any)
    
    // 继续调用链
    suspend fun proceed(request: Request): Response
}

// 实现真实的拦截器链
internal class RealInterceptorChain private constructor(
    private val interceptors: List<Interceptor>,
    private val index: Int,
    override val request: Request,
    override val context: CoroutineContext,
    private val sharedData: ConcurrentHashMap<String, Any> = ConcurrentHashMap()
) : Chain {
    
    companion object {
        fun create(
            interceptors: List<Interceptor>,
            request: Request,
            context: CoroutineContext
        ): Chain = RealInterceptorChain(
            interceptors = interceptors,
            index = 0,
            request = request,
            context = context
        )
    }

    override fun <T> getData(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return sharedData[key] as? T
    }

    override fun setData(key: String, value: Any) {
        sharedData[key] = value
    }

    override suspend fun proceed(request: Request): Response {
        check(index < interceptors.size) { "已到达拦截器链末尾" }
        
        // 创建下一个链节点
        val next = RealInterceptorChain(
            interceptors = interceptors,
            index = index + 1,
            request = request,
            context = context,
            sharedData = sharedData
        )
        
        val interceptor = interceptors[index]
        return interceptor.intercept(next)
    }
}

// 请求管理器实现幂等控制
class RequestManager {
    private val activeRequests = ConcurrentHashMap<String, Deferred<Response>>()
    
    suspend fun executeRequest(
        request: Request,
        interceptors: List<Interceptor>,
        context: CoroutineContext = EmptyCoroutineContext
    ): Response {
        return activeRequests.getOrPut(request.id) {
            CoroutineScope(context).async {
                try {
                    val chain = RealInterceptorChain.create(
                        interceptors = interceptors,
                        request = request,
                        context = context
                    )
                    chain.proceed(request)
                } finally {
                    activeRequests.remove(request.id)
                }
            }
        }.await()
    }
}

// 示例拦截器
class LoggingInterceptor : Interceptor {
    override suspend fun intercept(chain: Chain): Response {
        println("请求开始: ${chain.request.id}")
        val response = chain.proceed(chain.request)
        println("请求结束: ${chain.request.id}")
        return response
    }
}

class CacheInterceptor : Interceptor {
    override suspend fun intercept(chain: Chain): Response {
        // 从上下文中获取缓存key
        val cacheKey = chain.getData<String>("cacheKey")
        // 处理缓存逻辑
        return chain.proceed(chain.request)
    }
}

suspend fun main() {
    val requestManager = RequestManager()

    // 创建拦截器列表
    val interceptors = listOf(
        LoggingInterceptor(),
        CacheInterceptor()
    )

    // 创建请求
    val request = object : Request {
        override val id = "request-1"
        override val data = "test data"
    }

    // 执行请求
    val response = requestManager.executeRequest(
        request = request,
        interceptors = interceptors,
        context = Dispatchers.IO
    )
}
