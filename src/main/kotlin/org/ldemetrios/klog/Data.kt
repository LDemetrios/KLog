package org.ldemetrios.klog

import java.io.Closeable


sealed interface FunctionResult<out T>

data class Returned<out T>(val result: T) : FunctionResult<T> {
    
    override fun toString() = "returned\\;$result"
}

data class Threw(val throwable: Throwable) : FunctionResult<Nothing> {
    override fun toString() = "returned\\;$throwable"
}

sealed interface ScopeType<out T>

data class StaticScope(val trace: StackTraceElement, val params: List<Any?>) : ScopeType<Nothing>;
data class NonstaticScope<out T>(val trace: StackTraceElement, val instance: T, val params: List<Any?>) : ScopeType<T>;

interface Logger : Thread.UncaughtExceptionHandler, Closeable {
    fun openFunction(
        time: Long,
        caller: Pair<StackTraceElement, StackTraceElement>,
        args: List<Any?>,
    )
    
    fun <R> closeFunction(time: Long, returned: FunctionResult<R>)
    fun logValue(time: Long, caller: StackTraceElement, name: String, value: Any?)
    
    override fun close() = Unit
}

