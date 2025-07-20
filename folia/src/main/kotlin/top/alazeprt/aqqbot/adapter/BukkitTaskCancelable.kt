package top.alazeprt.aqqbot.adapter

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import top.alazeprt.aqqbot.util.Cancelable

class BukkitTaskCancelable(val plugin: Plugin): Cancelable {
    override fun cancel() {
        plugin.server.globalRegionScheduler.cancelTasks(plugin)
        plugin.server.asyncScheduler.cancelTasks(plugin)
    }
}