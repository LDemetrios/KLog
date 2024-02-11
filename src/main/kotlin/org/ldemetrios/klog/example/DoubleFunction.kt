package org.ldemetrios.klog.example

interface DoubleFunction : (Double) -> Double {
    override operator fun invoke(x: Double): Double
    
    val derivative: DoubleFunction
    
    companion object {
        val zero = object : DoubleFunction {
            override fun invoke(x: Double) = .0
            override val derivative: DoubleFunction get() = this
        }
        
        fun const(c: Number) = const(c.toDouble())
        fun const(c: Double) = object : DoubleFunction {
            override fun invoke(x: Double) = c
            override val derivative: DoubleFunction get() = zero
        }
        
        val one = const(1.0)
        
        val x: DoubleFunction = object : DoubleFunction {
            override fun invoke(x: Double) = x
            override val derivative: DoubleFunction get() = one
        }
        
        val exp: DoubleFunction = object : DoubleFunction {
            override fun invoke(x: Double) = kotlin.math.exp(x)
            override val derivative: DoubleFunction get() = this
        }
        val log: DoubleFunction = object : DoubleFunction {
            override fun invoke(x: Double) = kotlin.math.ln(x)
            override val derivative: DoubleFunction get() = one / x
        }
        
        val sqrt: DoubleFunction = object : DoubleFunction {
            override fun invoke(x: Double) = kotlin.math.sqrt(x)
            override val derivative: DoubleFunction get() = const(.5) / this
        }
    }
    
    operator fun plus(g: DoubleFunction): DoubleFunction = object : DoubleFunction {
        override fun invoke(x: Double) = this@DoubleFunction(x) + g(x)
        override val derivative: DoubleFunction get() : DoubleFunction = this@DoubleFunction.derivative + g.derivative
    }
    
    operator fun minus(g: DoubleFunction): DoubleFunction = object : DoubleFunction {
        override fun invoke(x: Double) = this@DoubleFunction(x) - g(x)
        override val derivative: DoubleFunction get() : DoubleFunction = this@DoubleFunction.derivative - g.derivative
    }
    
    operator fun times(g: DoubleFunction): DoubleFunction = object : DoubleFunction {
        override fun invoke(x: Double) = this@DoubleFunction(x) * g(x)
        override val derivative: DoubleFunction
            get() : DoubleFunction =
                this@DoubleFunction.derivative * g + this@DoubleFunction * g.derivative
    }
    
    operator fun div(g: DoubleFunction): DoubleFunction = object : DoubleFunction {
        override fun invoke(x: Double) = this@DoubleFunction(x) / g(x)
        override val derivative: DoubleFunction
            get() : DoubleFunction =
                (this@DoubleFunction.derivative * g - this@DoubleFunction * g.derivative) / g / g
    }
    
    operator fun unaryMinus(): DoubleFunction = object : DoubleFunction {
        override fun invoke(x: Double) = -this@DoubleFunction(x)
        override val derivative: DoubleFunction get() : DoubleFunction = -this@DoubleFunction.derivative
    }
    
    operator fun invoke(g: DoubleFunction): DoubleFunction = object : DoubleFunction {
        override fun invoke(x: Double) = this@DoubleFunction(g(x))
        override val derivative: DoubleFunction get() : DoubleFunction = g.derivative * this@DoubleFunction.derivative(g)
    }
    
    infix fun pow(g: DoubleFunction) = exp(g * log(this))
}