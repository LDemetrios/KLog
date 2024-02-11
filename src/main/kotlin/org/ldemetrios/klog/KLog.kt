package org.ldemetrios.klog

import kotlin.math.log

fun time() = System.nanoTime()

fun <R> Logger.logFunction(args: List<Any?>, block: () -> R): R {
    val trace = Thread.currentThread().stackTrace
    val caller = trace[3] to trace[5]
    val time = time()
    openFunction(time, caller, args)
    val result = try {
        block()
    } catch (e: Throwable) {
        closeFunction(time(), Threw(e))
        throw e
    }
    closeFunction(time(), Returned(result))
    return result
}

fun <R> logFun(vararg args: Any?, body: () -> R): R {
    return defaultLogger().logFunction(args.toList(), body)
}

fun <R> Logger.logFun(vararg args: Any?, body: () -> R): R {
    return this.logFunction(args.toList(), body)
}

fun <T> logValue(name: String, value: T): T {
    defaultLogger().logValue(time(), Thread.currentThread().stackTrace[2], name, value)
    return value
}

fun <T> Logger.logValue(name: String, value: T): T {
    this.logValue(time(), Thread.currentThread().stackTrace[2], name, value)
    return value
}

fun <T> T.logAs(name: String): T {
    defaultLogger().logValue(time(), Thread.currentThread().stackTrace[2], name, this)
    return this
}

fun <T> T.logAsWith(name: String, logger: Logger): T {
    logger.logValue(time(), Thread.currentThread().stackTrace[2], name, this)
    return this
}

fun logThread(withLogger: Logger, func: () -> Unit) {
    try {
        loggerMap.put(Thread.currentThread(), withLogger)
        func()
        loggerMap.remove(Thread.currentThread())
    } finally {
        withLogger.close()
    }
}

fun logThread(func: () -> Unit) = logThread(defaultLogger(), func)

