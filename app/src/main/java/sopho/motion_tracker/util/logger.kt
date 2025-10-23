package sopho.motion_tracker.util

import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

interface AbstractLogger {

    fun log(level: Int, tag: String, message: String)

    fun v(tag: String, message: String) =
        log(Log.VERBOSE, tag, message)

    fun v(message: String) =
        log(Log.VERBOSE, inferTag(), inferMessage() + message)

    fun d(tag: String, message: String) =
        log(Log.DEBUG, tag, message)

    fun d(message: String) =
        log(Log.DEBUG, inferTag(), inferMessage() + message)

    fun i(tag: String, message: String) =
        log(Log.INFO, tag, message)

    fun i(message: String) =
        log(Log.INFO, inferTag(), inferMessage() + message)

    fun w(tag: String, message: String) =
        log(Log.WARN, tag, message)

    fun w(message: String) =
        log(Log.WARN, inferTag(), inferMessage() + message)

    fun e(tag: String, message: String) =
        log(Log.ERROR, tag, message)

    fun e(message: String) =
        log(Log.ERROR, inferTag(), inferMessage() + message)

    fun inferTag(): String {
        val stack = Throwable().stackTrace
        for (element in stack) {
            val className = element.className
            if (!className.contains("AbstractLogger") && !className.contains("Logger")) {
                return className.substringAfterLast('.')
            }
        }
        return AbstractLogger::class.simpleName ?: "AbstractLogger"
    }

    fun inferMessage(): String {
        val stack = Throwable().stackTrace
        for (element in stack) {
            val className = element.className
            if (!className.contains("AbstractLogger") && !className.contains("Logger")) {
                return "[${element.methodName}(${element.fileName}:${element.lineNumber})]"
            }
        }
        return ""
    }
}

object StdLogger : AbstractLogger {
    override fun log(
        level: Int,
        tag: String,
        message: String
    ) {
        Log.println(level, tag, "[Sopho]$message")
    }

}

object FileLogger : AbstractLogger {
    override fun log(level: Int, tag: String, message: String) {

    }
}

object SLog : AbstractLogger {

    private val delegates = CopyOnWriteArrayList<AbstractLogger>()

    fun addLogger(logger: AbstractLogger) {
        delegates += logger
    }

    fun removeLogger(logger: AbstractLogger) {
        delegates -= logger
    }

    fun clearLoggers() {
        delegates.clear()
    }

    override fun log(level: Int, tag: String, message: String) {
        for (d in delegates) {
            runCatching { d.log(level, tag, message) }
        }
    }

    init {
        addLogger(StdLogger)
    }
}
