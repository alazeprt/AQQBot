package top.alazeprt.aqqbot.util

import com.alessiodp.libby.logging.LogLevel
import com.alessiodp.libby.logging.adapters.LogAdapter
import org.apache.logging.log4j.Logger

class FabricLogAdapter(val logger: Logger) : LogAdapter {
    override fun log(level: LogLevel, message: String?) {
        when (level) {
            LogLevel.DEBUG -> logger.debug(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
        }
    }

    override fun log(
        level: LogLevel,
        message: String?,
        throwable: Throwable?
    ) {
        when (level) {
            LogLevel.DEBUG -> logger.debug(message, throwable)
            LogLevel.INFO -> logger.info(message, throwable)
            LogLevel.WARN -> logger.warn(message, throwable)
            LogLevel.ERROR -> logger.error(message, throwable)
        }
    }
}