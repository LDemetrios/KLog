@file:Suppress("UNUSED_VARIABLE", "SameParameterValue")

package org.ldemetrios.klog.example

import  org.ldemetrios.klog.*
import java.lang.StringBuilder
import org.ldemetrios.klog.logFun

fun main(args: Array<String>) {
    loggerGenerator = ToFileLoggerProvider("header.txt")::provideFor
    logThread {
        logValue("args", args.joinToString())
        val sample = ExpressionParser("(x + 1 * 2 ^-3) /2 + 3* x").parseExpression()
        logValue("sample", sample)
    }
}

class ExpressionParser(val source: String) {
    private fun exception(before: String = "") = IllegalArgumentException(
        "Unexpected symbol: ..." + before + source.substring(pointer, minOf(pointer + 10, source.length))
    )
    
    private var pointer = 0
    
    fun parseExpression(): DoubleFunction = logFun {
        skipWs()
        val res = parseExpr()
        if (!eof()) throw exception()
        res
    }
    
    private fun parseExpr(): DoubleFunction = logFun {
        var res = parseTerm()
        skipWs()
        while (test('+', '-')) {
            if (take('+')) res += parseTerm()
            else if (take('-')) res -= parseTerm()
            logValue("res", res)
            skipWs()
        }
        res
    }
    
    private fun parseTerm(): DoubleFunction = logFun {
        var res = parseFactor()
        skipWs()
        while (test('*', '/')) {
            if (take('*')) res *= parseFactor()
            else if (take('/')) res /= parseFactor()
            logValue("res", res)
            skipWs()
        }
        res
    }
    
    private fun parseFactor(): DoubleFunction = logFun {
        val list = mutableListOf<DoubleFunction>(parseClause())
        skipWs()
        while (take('^')) {
            list.add(parseClause())
            skipWs()
        }
        list.logAs("list").foldRight<DoubleFunction, DoubleFunction?>(null) { a, b -> if (b == null) a else a pow b }!!
    }
    
    private fun parseClause(): DoubleFunction = logFun {
        skipWs()
        if (take('(')) {
            val res = parseExpr()
            assert(')')
            res
        } else if (take('-')) {
            -parseClause()
        } else if (testIsNumeric()) {
            DoubleFunction.const(takeNumber())
        } else if (testIsLetter()) {
            when (takeWord().logAs("word")) {
                "x"    -> DoubleFunction.x
                "sqrt" -> DoubleFunction.sqrt(parseClause())
                "exp"  -> DoubleFunction.exp(parseClause())
                "log"  -> DoubleFunction.log(parseClause())
                else   -> throw exception(takeWord())
            }
        } else throw exception()
    }
    
    //-------------------------- Token funs --------------------------
    
    private fun takeNumber(): Double = logFun { //minuses are processed automatically
        val sb = StringBuilder()
        while (testIsNumeric()) sb.append(take())
        sb.toString().toDouble()
    }
    
    private fun testIsNumeric() = between('0', '9') || test('.')
    
    private fun takeWord(): String = logFun {
        val sb = StringBuilder()
        while (testIsLetter()) sb.append(take())
        sb.toString()
    }
    
    private fun testIsLetter() = between('a', 'z') || between('A', 'Z')
    
    //-------------------------- Basic funs --------------------------
    
    private fun take() = if (eof()) '\u0000' else source[pointer++]
    private fun peek() = if (eof()) '\u0000' else source[pointer]
    private fun test(vararg chars: Char) = chars.any { it == peek() }
    private fun between(min: Char, max: Char) = peek() in min..max
    private fun take(expected: Char): Boolean {
        val doTake = (test(expected))
        if (doTake) take()
        return doTake
    }
    
    private fun assert(expected: Char) = if (take(expected)) Unit else throw exception()
    
    private fun skipWs() {
        while (Character.isWhitespace(peek())) take()
    }
    
    private fun eof() = pointer >= source.length
    
    override fun toString() = "\"" + source.substring(0, pointer) + "$" + source.substring(pointer) + "\""
}


