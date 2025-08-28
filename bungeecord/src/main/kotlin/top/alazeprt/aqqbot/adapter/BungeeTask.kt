package top.alazeprt.aqqbot.adapter

import net.md_5.bungee.api.scheduler.ScheduledTask
import top.alazeprt.aqqbot.util.Cancelable

class BungeeTask(val schedulerTask: ScheduledTask): Cancelable {
    override fun cancel() {
        schedulerTask.cancel()
    }
}