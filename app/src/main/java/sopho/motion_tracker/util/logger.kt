package sopho.motion_tracker.util

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
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
            if (!className.contains("AbstractLogger")
                && !className.contains("Logger")
                && !className.contains("SLog")
            ) {
                return className.substringAfterLast('.')
            }
        }
        return AbstractLogger::class.simpleName ?: "AbstractLogger"
    }

    fun inferMessage(): String {
        val stack = Throwable().stackTrace
        for (element in stack) {
            val className = element.className
            if (!className.contains("AbstractLogger")
                && !className.contains("Logger")
                && !className.contains("SLog")
            ) {
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

class FileLogger(context: Context) : AbstractLogger {
    private val tsFmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US)
    var writer = BufferedWriter(
        FileWriter(
            File(
                getDir(context),
                "${
                    java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                        .format(System.currentTimeMillis())
                }.log"
            ), false
        )
    )

    private fun getDir(context: Context): File {
        var dir = File(context.getExternalFilesDir(null), "log")
        if (!dir.exists()) {
            if (!dir.mkdirs() && !dir.exists()) {
                throw IOException("Failed to create log dir: ${dir.absolutePath}")
            }
        }
        return dir
    }

    private fun currentTime(): String {
        return tsFmt.format(System.currentTimeMillis())
    }

    override fun log(level: Int, tag: String, message: String) {
        writer.write("${currentTime()} [$tag]$message")
        writer.newLine()
        writer.flush()
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
