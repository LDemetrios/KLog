package logger


fun <R> staticScope(vararg params: Any?, function: () -> R): R {
    return function()
}

fun <T, R> T.scope(vararg params: Any?, function: T.() -> R): R {
    return function()
}

fun logValue(name: String, value: Any?) {

}

fun <T> T.logAs(name: String): T {
    return this
}

