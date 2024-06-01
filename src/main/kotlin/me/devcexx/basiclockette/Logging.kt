package me.devcexx.basiclockette

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.reflect.KClass

interface Logging {
    val logger: Logger
}

// Since I'm not finding a good way to make Paper
// enable Log4j to publish DEBUG messages,
// if this flag I'm just routing all the debug messages
// as INFO.
private val isPluginDebugEnabled = System.getProperty("basiclockette.debug") == "1"

fun <T : Any> logFor(clazz: KClass<T>): Logging =
    if (isPluginDebugEnabled) {
        object : Logging {
            override val logger: Logger by lazy {
                val logger = Logger.getLogger(clazz.java.name)
                logger.level = Level.ALL
                logger.addHandler(
                    object : Handler() {
                        override fun publish(logRecord: LogRecord) {
                            if (logRecord.level.intValue() < Level.INFO.intValue()) {
                                logRecord.message = "[D] ${logRecord.message}"
                                logRecord.level = Level.INFO
                            }
                        }

                        override fun flush() {}

                        override fun close() {}
                    },
                )
                logger
            }
        }
    } else {
        object : Logging {
            override val logger: Logger by lazy {
                val logger = Logger.getLogger(clazz.java.name)
                logger
            }
        }
    }

inline fun <reified T : Any> logFor(): Logging = logFor(T::class)
