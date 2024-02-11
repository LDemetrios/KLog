package org.ldemetrios.klog

import java.io.File

val launchTime = time()

class ToFileLoggerProvider(val headerFile: String) {
    private val file = File("log$launchTime/" + headerFile)
    
    init {
        file.parentFile.run {
            if (!exists()) mkdirs()
        }
        file.createNewFile()
    }
    
    fun provideFor(thread: Thread): Logger {
        synchronized(this) {
            val threadLogFile = "log$launchTime/" + thread.name + "(" + thread.threadId() + ").log"
            File(threadLogFile).parentFile.run {
                if (!exists()) mkdirs()
            }
            File(threadLogFile).createNewFile()
            file.appendText(threadLogFile + "\n")
            val logger = ToFileLogger(threadLogFile)
            thread.uncaughtExceptionHandler = logger
            return logger
        }
    }
}

internal class ToFileLogger(val fileName: String) : Logger {
    private var depth = 0
    val file = File(fileName)
    
    private fun write(vararg els: Any?, rest: List<Any?> = listOf()) {
        file.appendText(
            (els.toList() + rest).joinToString("\\;").replace("\n", "\\n") + "\n"
        )
    }
    
    override fun openFunction(time: Long, caller: Pair<StackTraceElement, StackTraceElement>, args: List<Any?>) {
        write(
            "open", depth, time,
            caller.second.lineNumber,
            caller.first.className,
            caller.first.methodName,
            rest = args
        )
        depth++
    }
    
    override fun <R> closeFunction(time: Long, returned: FunctionResult<R>) {
        write(
            "close",depth,time,
            when(returned){
                is Returned -> "returned"
                is Threw -> "threw"
            },
            when(returned){
                is Returned -> returned.result
                is Threw -> returned.throwable
            },
        )
        depth--
    }
    
    override fun logValue(time: Long, caller: StackTraceElement, name: String, value: Any?) {
        write("value", time,caller.lineNumber,name,value)
    }
    
    override fun uncaughtException(p0: Thread?, p1: Throwable?) {
        write("uncaught",time(),p1)
    }
}
