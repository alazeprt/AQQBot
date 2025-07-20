package top.alazeprt.aqqbot.api

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.util.GroupConfiguration

object AQQBotAPI {
    private lateinit var plugin: AQQBot
    private lateinit var events: MutableList<Class<Any>>

    fun getAdapter(): AQQBotAdapter {
        return plugin.adapter
    }

    fun getDataProvider(): DataProvider {
        return plugin.dataProvider
    }

    fun getGeneralConfig(): GroupConfiguration {
        return plugin.generalConfig
    }

    fun getBotConfig(): FileConfiguration {
        return plugin.botConfig
    }

    fun getMessageConfig(): MessageManager {
        return plugin.messageManager
    }

    fun registerEvent(clazz: Class<Any>) {
        events.add(clazz)
    }

    fun unregisterEvent(clazz: Class<Any>) {
        if (events.contains(clazz)) {
            events.remove(clazz)
        }
    }

    internal fun fireEvent(event: APIEvent) {
        for (clazz in events) {
            clazz.declaredMethods.forEach { method ->
                if (method.parameterCount == 0) {
                    method.invoke(clazz)
                } else if (method.parameterCount == 1 && method.parameters[0].type == APIEvent::class.java) {
                    method.invoke(clazz, event)
                }
            }
        }
    }
}