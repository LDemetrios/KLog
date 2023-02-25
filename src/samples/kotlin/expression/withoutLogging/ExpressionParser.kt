@file:Suppress("UNUSED_PARAMETER", "SameParameterValue", "UNUSED_VARIABLE")

package expression.withoutLogging

import expression.DoubleFunction
import java.lang.StringBuilder

fun main(args: Array<String>) {
    val sample = ExpressionParser("(x + 1 * 2 ^-3) /2 + 3* x").parseExpression()
}

class ExpressionParser(val source: String) {
    private fun exception(before: String = "") = IllegalArgumentException(
        "Unexpected symbol: ..." + before + source.substring(pointer, minOf(pointer + 10, source.length))
    )
    
    private var pointer = 0
    
    fun parseExpression(): DoubleFunction {
        skipWs()
        val res = parseExpr()
        if (!eof()) throw exception()
        return res
    }
    
    private fun parseExpr(): DoubleFunction {
        var res = parseTerm()
        skipWs()
        while (test('+', '-')) {
            if (take('+')) res += parseTerm()
            else if (take('-')) res -= parseTerm()
            skipWs()
        }
        return res
    }
    
    private fun parseTerm(): DoubleFunction {
        var res = parseFactor()
        skipWs()
        while (test('*', '/')) {
            if (take('*')) res *= parseFactor()
            else if (take('/')) res /= parseFactor()
            skipWs()
        }
        return res
    }
    
    private fun parseFactor(): DoubleFunction {
        val list = mutableListOf(parseClause())
        skipWs()
        while (take('^')) {
            list.add(parseClause())
            skipWs()
        }
        return list.foldRight<DoubleFunction, DoubleFunction?>(null) { a, b -> if (b == null) a else a pow b }!!
    }
    
    private fun parseClause(): DoubleFunction {
        skipWs()
        return if (take('(')) {
            val res = parseExpr()
            assert(')')
            res
        } else if (take('-')) {
            -parseClause()
        } else if (testIsNumeric()) {
            DoubleFunction.const(takeNumber())
        } else if (testIsLetter()) {
            when (takeWord()) {
                "x"    -> DoubleFunction.x
                "sqrt" -> DoubleFunction.sqrt(parseClause())
                "exp"  -> DoubleFunction.exp(parseClause())
                "log"  -> DoubleFunction.log(parseClause())
                else   -> throw exception(takeWord())
            }
        } else throw exception()
    }
    
    //-------------------------- Token funs --------------------------
    
    private fun takeNumber(): Double { //minuses are processed automatically
        val sb = StringBuilder()
        while (testIsNumeric()) sb.append(take())
        return sb.toString().toDouble()
    }
    
    private fun testIsNumeric() = between('0', '9') || test('.')
    
    private fun takeWord(): String {
        val sb = StringBuilder()
        while (testIsLetter()) sb.append(take())
        return sb.toString()
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


