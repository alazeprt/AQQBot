package top.alazeprt.aqqbot.adapter

import net.fabricmc.loader.api.FabricLoader
import top.alazeprt.aqqbot.util.Cancelable

class FabricCancelable(val task: Thread) : Cancelable {
    override fun cancel() {
        task.interrupt()
    }
}