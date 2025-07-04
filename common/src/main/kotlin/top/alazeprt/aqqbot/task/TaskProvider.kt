package top.alazeprt.aqqbot.task

import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.Cancelable
import java.util.concurrent.CompletableFuture

interface TaskProvider {
    // TODO: add cancel() method for every task submitted
    fun submit(task: Runnable): Cancelable

    fun submitAsync(task: Runnable): Cancelable

    fun submitLater(delay: Long, task: Runnable): Cancelable

    fun submitLaterAsync(delay: Long, task: Runnable): Cancelable

    fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable

    fun submitTimerAsync(delay: Long, period: Long, task: Runnable): Cancelable

    fun submitCommand(command: String, groupId: Long): CompletableFuture<AExecution>
}