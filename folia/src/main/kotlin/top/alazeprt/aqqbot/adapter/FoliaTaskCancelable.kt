package top.alazeprt.aqqbot.adapter

import org.bukkit.plugin.Plugin
import top.alazeprt.aqqbot.util.Cancelable

class FoliaTaskCancelable(val plugin: Plugin): Cancelable {
    override fun cancel() {
        plugin.server.globalRegionScheduler.cancelTasks(plugin)
        plugin.server.asyncScheduler.cancelTasks(plugin)
    }
}