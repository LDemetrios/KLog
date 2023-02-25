package util

infix fun <A, B, C> Pair<A, B>.and(third:C) = Triple(first, second, third)

typealias StackTrace = Array<StackTraceElement>


