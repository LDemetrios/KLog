package logger


fun <R> staticScope(vararg params: Any?, function: () -> R): R {
    val trace = Thread.currentThread().stackTrace
    return scope0(StaticScope(trace, params), function)
}

private fun <R> scope0(type: ScopeType<*>, function: () -> R): R {
    val logger = LoggerTable.getCurrent()
    logger?.openScope(type)
    try {
        val result = function()
        logger?.closeScope(Returned(result))
        return result
    } catch (e: Exception) {
        logger?.closeScope(Threw(e))
        throw e
    } catch (e: AssertionError) {
        logger?.closeScope(Threw(e))
        throw e
    } catch (e: ThreadDeath) {
        logger?.closeScope(Threw(e))
        throw e
    }
}

fun <T, R> T.scope(vararg params: Any?, function: T.() -> R): R {
    val trace = Thread.currentThread().stackTrace
    return scope0(NonstaticScope(trace, this, params)) { this.function() }
}

fun logValue(name: String, value: Any?) {
    LoggerTable.getCurrent()?.logValue(name, value, Thread.currentThread().stackTrace)
}

fun <T> T.logAs(name: String): T {
    LoggerTable.getCurrent()?.logValue(name, this, Thread.currentThread().stackTrace)
    return this
}

