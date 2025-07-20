package top.alazeprt.aqqbot.adapter

import com.velocitypowered.api.scheduler.ScheduledTask
import top.alazeprt.aqqbot.util.Cancelable

class VelocityTaskCancelable(val task: ScheduledTask?) : Cancelable {
    override fun cancel() {
        task?.cancel()
    }
}