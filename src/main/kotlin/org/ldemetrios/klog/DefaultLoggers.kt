package org.ldemetrios.klog

import java.util.concurrent.ConcurrentHashMap

internal object NothingLogger : Logger {
    override fun openFunction(
        time: Long,
        caller: Pair<StackTraceElement, StackTraceElement>,
        args: List<Any?>
    ) = Unit
    
    override fun <R> closeFunction(time: Long, returned: FunctionResult<R>) = Unit
    
    override fun logValue(time: Long, caller: StackTraceElement, name: String, value: Any?) = Unit
    
    override fun uncaughtException(p0: Thread?, p1: Throwable?) = p1?.printStackTrace() ?: Unit
}

var loggerGenerator: ((Thread) -> Logger)? = null
    set(value) {
        if (field != null) {
            throw IllegalStateException("Logger generator already set")
        }
        field = value
    }
    get() {
        return field ?: { NothingLogger }
    }
internal val loggerMap = ConcurrentHashMap<Any?, Logger>()

fun defaultLogger(): Logger =
        loggerMap.computeIfAbsent(Thread.currentThread()) { loggerGenerator!!(Thread.currentThread()) }
