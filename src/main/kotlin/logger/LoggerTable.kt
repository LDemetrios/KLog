package logger

import util.StackTrace
import java.lang.Thread.UncaughtExceptionHandler
import java.lang.Thread.currentThread

object LoggerTable {
    private var defaultLogger: ((Thread) -> Logger)? = null
    
    private val table = mutableMapOf<Thread, Logger>()
    
    private operator fun get(t: Thread): Logger? = table.compute(t) { th, log -> log ?: defaultLogger?.invoke(th) }
    
    fun getCurrent() = this[currentThread()]
    
    fun registerLogger(thread: Thread, logger: Logger) {
        table.compute(thread) { th, old -> if (old == null) logger else throw IllegalStateException("Thread $th already has logger") }
    }
}

interface Logger : UncaughtExceptionHandler {
    fun openScope(type: ScopeType<*>)
    fun <R> closeScope(returned: FunctionResult<R>)
    fun logValue(name: String, value: Any?, stackTrace: Array<StackTraceElement>)
}

sealed interface FunctionResult<out T>

data class Returned<out T>(val result: T) : FunctionResult<T>
data class Threw(val throwable: Throwable) : FunctionResult<Nothing>

sealed interface ScopeType<out T>

data class StaticScope(val trace: StackTrace, val params: Array<out Any?>) : ScopeType<Nothing> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as StaticScope
        
        if (!trace.contentEquals(other.trace)) return false
        if (!params.contentEquals(other.params)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = trace.contentHashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}

data class NonstaticScope<out T>(val trace: StackTrace, val instance: T, val params: Array<out Any?>) : ScopeType<T> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as NonstaticScope<*>
        
        if (!trace.contentEquals(other.trace)) return false
        if (instance != other.instance) return false
        if (!params.contentEquals(other.params)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = trace.contentHashCode()
        result = 31 * result + (instance?.hashCode() ?: 0)
        result = 31 * result + params.contentHashCode()
        return result
    }
}
