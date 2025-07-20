package top.alazeprt.aqqbot.adapter

import org.bukkit.scheduler.BukkitTask
import top.alazeprt.aqqbot.util.Cancelable

class BukkitTaskCancelable(private val task: BukkitTask): Cancelable {
    override fun cancel() {
        task.cancel()
    }
}