# Current state

Library is now ready to use, but yet quite useless. 
Plans: add basic logger and few log formats, and thread events monitoring 

# What is KLog about?

## Goals

### Create easy-to-use logging lib.

Hey, there already are a bunch of logging libraries.
Why create another one?

Well, if you know any, that does exactly what I what this to do,
you can ping me anywhere you want.

Consider the example:

```
 3:  fun main() {
 4:      println(foo(566)) // 62
 5:      println(foo(32872)) // 239
 6:      println(foo(113383)) // Occasionally fail
 7:  }
 8:  
 9:  fun foo(x: Int): Int = when {
10:      x <= 0     -> throw AssertionError()
11:      x == 1     -> 0
12:      x % 2 == 0 -> bar(x)
13:      else       -> bzz(x)
14:  } + 1
15:  
16:  fun bar(x: Int): Int = foo(x shr 1)
17:  
18:  fun bzz(x: Int): Int = foo((x shl 2) - (x xor 1))
```

We get this stack trace of an error:

```
Exception in thread "main" java.lang.AssertionError
    at TestKt.foo(Test.kt:10)
    at TestKt.bzz(Test.kt:18)
    at TestKt.foo(Test.kt:13)
    at TestKt.bar(Test.kt:16)
    at TestKt.foo(Test.kt:12)
    at TestKt.bzz(Test.kt:18)
    etc
```

Isn't it obvious that `bzz` is called from `foo(Test.kt:13)`
and `bar` from `foo(Test.kt:13)`?
And they both do call `foo` again?
I completely understand why java or kotlin don't show
parameters of functions in the stack trace, but sometimes I need them.
Without step-by-step-stepping in debug mode,
I want to see tree of calls, with parameters stored.
Of course, it's applicable only for small methods.
But I prefer getting something like this:

```
TestKt.main()
    l4 -> TestKt.foo(566)
        l12 -> TestKt.bar(566)
            l16 -> TestKt.foo(283)
                l13 -> TestKt.bar(283)
                    l18 -> TestKt.foo(850)
```

Besides, that's not the best sample, because its 'tree' is 'bamboo',
but consider [ExceptionParser sample](src/samples/kotlin/expression/withLogging/ExpressionParser.kt).

### Keep its syntax from messing with meaningful code

Compare this:

```
fun foo(x: Int): Int {
    println(/* indent + */ "TestKt.foo($x)")
    val res = when {
        x <= 0     -> {
            println(/* indent + */ "threw AssertionError") 
            // In case someone unknown will catch it
            throw AssertionError()
        }
        x == 1     -> 0
        x % 2 == 0 -> bar(x)
        else       -> bzz(x)
    } + 1
    println(/* indent + */ "returned $res")
    return res
}
```

to this:

```
fun foo(x: Int): Int = Logger.logCall(x) {
    when {
        x <= 0     -> throw AssertionError()
        x == 1     -> 0
        x % 2 == 0 -> bar(x)
        else       -> bzz(x)
    } + 1
}
```

The first is not what I want to come up with, but the second is.

### (In prospect) Create simple and intuitive gui to navigate through that information

## Non-Goals

### Create Super-Ultra-Mega-Log-Engine

Some body movements from user are still required.
I am not reflection-wizard able to look through code evaluation at its runtime.
Besides, this logger, as mentioned above, is not suitable for large methods,
and especially not intended for logging something in release-time.

# Change log

## 1.0

- Add Logger interface
- Add logger differentiation by threads (LoggerTable)

## 0 Developing

### 0.2

- Add more clear sample (ExpressionParser)
- Add desired function contracts (More will be added later)

### 0.1

- Add [this file](README.md)
- Internal changes

### 0.0

- Create project 