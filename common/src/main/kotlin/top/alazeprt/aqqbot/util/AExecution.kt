package top.alazeprt.aqqbot.util

import java.util.concurrent.CompletableFuture

interface AExecution {
    fun getRawString(): String

    fun getFormattedString(): String

    fun execute(command: String): CompletableFuture<AExecution>
}