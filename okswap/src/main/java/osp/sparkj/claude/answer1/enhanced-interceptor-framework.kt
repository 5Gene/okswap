package osp.sparkj.claude.answer1

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

// 基础接口
interface Request
interface Response

// 增强的拦截器接口，添加优先级
interface Interceptor {
    val priority: Int
        get() = NORMAL_PRIORITY // 默认优先级
        
    suspend fun intercept(chain: Chain): Response
    
    companion object {
        const val HIGH_PRIORITY = 100
        const val NORMAL_PRIORITY = 50
        const val LOW_PRIORITY = 0
    }
}

// 错误处理器接口
fun interface ErrorHandler {
    suspend fun handleError(error: Throwable, chain: Chain): Response?
}

// 增强的Chain接口
interface Chain {
    val request: Request
    val isCancelled: Boolean
    
    suspend fun proceed(request: Request): Response
    suspend fun <T: Any> getContext(key: ContextKey<T>): T?
    suspend fun <T: Any> setContext(key: ContextKey<T>, value: T)
    suspend fun removeContext(key: ContextKey<*>)
    
    // 作用域支持
    suspend fun <T> withContext(block: suspend Chain.() -> T): T
    
    // 错误处理
    suspend fun addErrorHandler(handler: ErrorHandler)
    
    // 取消操作
    fun cancel()
}

// 类型安全的上下文Key
class ContextKey<T : Any>(val name: String)

// 内部上下文存储类
private class InterceptorContext {
    private val mutex = Mutex()
    private val storage = mutableMapOf<String, Any>()
    private val errorHandlers = mutableListOf<ErrorHandler>()
    private val isCancelled = AtomicBoolean(false)
    
    suspend fun <T : Any> set(key: ContextKey<T>, value: T) {
        mutex.withLock {
            storage[key.name] = value
        }
    }
    
    suspend fun remove(key: ContextKey<*>) {
        mutex.withLock {
            storage.remove(key.name)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> get(key: ContextKey<T>): T? {
        return mutex.withLock {
            storage[key.name] as? T
        }
    }
    
    suspend fun addErrorHandler(handler: ErrorHandler) {
        mutex.withLock {
            errorHandlers.add(handler)
        }
    }
    
    suspend fun handleError(error: Throwable, chain: Chain): Response? {
        return mutex.withLock {
            for (handler in errorHandlers.reversed()) {
                val response = handler.handleError(error, chain)
                if (response != null) return response
            }
            null
        }
    }
    
    fun cancel() {
        isCancelled.set(true)
    }
    
    fun isCancelled(): Boolean = isCancelled.get()
}

// 增强的责任链实现
private class RealInterceptorChain private constructor(
    private val interceptors: List<Interceptor>,
    private val index: Int,
    override val request: Request,
    private val context: InterceptorContext
) : Chain {
    
    override val isCancelled: Boolean
        get() = context.isCancelled()
    
    override suspend fun proceed(request: Request): Response {
        // 检查取消状态
        if (isCancelled) {
            throw CancellationException("Chain was cancelled")
        }
        
        // 确保当前协程仍然活跃
        currentCoroutineContext().ensureActive()
        
        check(index < interceptors.size) { "index超出拦截器列表范围" }
        
        try {
            val next = RealInterceptorChain(
                interceptors = interceptors,
                index = index + 1,
                request = request,
                context = context
            )
            
            return interceptors[index].intercept(next)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 尝试错误处理
            val response = context.handleError(e, this)
            return response ?: throw e
        }
    }
    
    override suspend fun <T : Any> getContext(key: ContextKey<T>): T? = 
        context.get(key)
    
    override suspend fun <T : Any> setContext(key: ContextKey<T>, value: T) {
        context.set(key, value)
    }
    
    override suspend fun removeContext(key: ContextKey<*>) {
        context.remove(key)
    }
    
    override suspend fun <T> withContext(block: suspend Chain.() -> T): T {
        val snapshot = InterceptorContext()
        val backupChain = RealInterceptorChain(
            interceptors = interceptors,
            index = index,
            request = request,
            context = snapshot
        )
        return block(backupChain)
    }
    
    override suspend fun addErrorHandler(handler: ErrorHandler) {
        context.addErrorHandler(handler)
    }
    
    override fun cancel() {
        context.cancel()
    }
    
    companion object {
        fun create(
            interceptors: List<Interceptor>,
            request: Request
        ): Chain {
            // 根据优先级排序拦截器
            val sortedInterceptors = interceptors.sortedByDescending { it.priority }
            return RealInterceptorChain(
                interceptors = sortedInterceptors,
                index = 0,
                request = request,
                context = InterceptorContext()
            )
        }
    }
}

// 增强的客户端类
class Client(interceptors: List<Interceptor>) {
    private val interceptors = interceptors.sortedByDescending { it.priority }
    
    suspend fun execute(request: Request): Response {
        return RealInterceptorChain.create(
            interceptors = interceptors,
            request = request
        ).proceed(request)
    }
}

// 示例拦截器实现
class LoggingInterceptor : Interceptor {
    override val priority = Interceptor.HIGH_PRIORITY
    
    override suspend fun intercept(chain: Chain): Response {
        println("请求开始")
        val response = chain.proceed(chain.request)
        println("请求结束")
        return response
    }
}

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override val priority = Interceptor.HIGH_PRIORITY + 1
    
    override suspend fun intercept(chain: Chain): Response {
        var lastException: Exception? = null
        repeat(maxRetries) { attempt ->
            try {
                return chain.proceed(chain.request)
            } catch (e: Exception) {
                lastException = e
                println("重试次数: ${attempt + 1}")
            }
        }
        throw lastException ?: IllegalStateException("Unknown error")
    }
}

class TimeoutInterceptor : Interceptor {
    override suspend fun intercept(chain: Chain): Response {
        // 设置超时计时器
        val startTime = System.currentTimeMillis()
        chain.setContext(ContextKeys.START_TIME, startTime)
        
        return chain.withContext {
            val response = proceed(request)
            val endTime = System.currentTimeMillis()
            setContext(ContextKeys.REQUEST_DURATION, endTime - startTime)
            response
        }
    }
}

// 预定义上下文键
object ContextKeys {
    val REQUEST_ID = ContextKey<String>("requestId")
    val START_TIME = ContextKey<Long>("startTime")
    val REQUEST_DURATION = ContextKey<Long>("requestDuration")
    val ERROR_COUNT = ContextKey<Int>("errorCount")
}

// 示例使用
suspend fun main() {
    val client = Client(listOf(
        LoggingInterceptor(),
        RetryInterceptor(),
        TimeoutInterceptor()
    ))
    
    // 创建错误处理器
    val errorHandler = ErrorHandler { error, chain ->
        val errorCount = (chain.getContext(ContextKeys.ERROR_COUNT) ?: 0) + 1
        chain.setContext(ContextKeys.ERROR_COUNT, errorCount)
        
        if (error is IllegalStateException) {
            // 返回降级响应
            object : Response {}
        } else null // 继续传播其他错误
    }
    
    try {
        val response = client.execute(object : Request {})
    } catch (e: CancellationException) {
        println("请求被取消")
    } catch (e: Exception) {
        println("请求失败: ${e.message}")
    }
}
