package top.alazeprt.aqqbot.task

import top.alazeprt.aqqbot.util.AExecution
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

interface TaskProvider {
    fun submit(task: Runnable): Future<*>

    fun submitAsync(task: Runnable): Future<*>

    fun submitLater(delay: Long, task: Runnable): Future<*>

    fun submitLaterAsync(delay: Long, task: Runnable): Future<*>

    fun submitTimer(delay: Long, period: Long, task: Runnable): Future<*>

    fun submitTimerAsync(delay: Long, period: Long, task: Runnable): Future<*>

    fun submitCommand(command: String): CompletableFuture<AExecution>
}