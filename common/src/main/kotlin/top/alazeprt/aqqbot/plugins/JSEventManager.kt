package top.alazeprt.aqqbot.plugins

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.SubscribeAQQBotEvent
import top.alazeprt.aqqbot.api.event.qq.PostBindEvent
import top.alazeprt.aqqbot.api.event.qq.PostInformationEvent
import top.alazeprt.aqqbot.api.event.qq.PostRemoteCommandEvent
import top.alazeprt.aqqbot.api.event.qq.PostUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.PreBindEvent
import top.alazeprt.aqqbot.api.event.qq.PreInformationEvent
import top.alazeprt.aqqbot.api.event.qq.PreRemoteCommandEvent
import top.alazeprt.aqqbot.api.event.qq.PreUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.ReceiveMessageEvent
import top.alazeprt.aqqbot.util.LogLevel
import java.lang.reflect.Method

class JSEventManager(val plugin: AQQBot) {
    private val listeners = mutableMapOf<String, MutableList<Any>>()

    fun register(eventName: String, function: Any) {
        println("Registering ...")
        listeners.getOrPut(eventName) { mutableListOf() }.add(function)
        plugin.log(LogLevel.DEBUG, "Registered handler for $eventName")
    }

    fun unregister(eventName: String, function: Any) {
        listeners[eventName]?.remove(function)
    }

    private fun callEvent(eventName: String, instance: Any) {

        println("callEvent: $eventName")

        val handlers = listeners[eventName] ?: return

        handlers.forEach { function ->
            try {
                val callMethod = function.javaClass.getMethod("call", Any::class.java, Array<Any>::class.java)
                callMethod.invoke(function, null, arrayOf(instance))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    internal fun load() {
        AQQBotAPI.registerEvent(this)
    }

    internal fun unload() {
        AQQBotAPI.unregisterEvent(this)
        listeners.clear()
        plugin.log(LogLevel.INFO, "JS event manager unloaded")
    }

    @SubscribeAQQBotEvent
    fun onPostBind(event: PostBindEvent) = callEvent("PostBindEvent", event)

    @SubscribeAQQBotEvent
    fun onPostUnbind(event: PostUnbindEvent) = callEvent("PostUnbindEvent", event)

    @SubscribeAQQBotEvent
    fun onPostInformation(event: PostInformationEvent) = callEvent("PostInformationEvent", event)

    @SubscribeAQQBotEvent
    fun onPostRemoteCommand(event: PostRemoteCommandEvent) = callEvent("PostRemoteCommandEvent", event)

    @SubscribeAQQBotEvent
    fun onPreBind(event: PreBindEvent) = callEvent("PreBindEvent", event)

    @SubscribeAQQBotEvent
    fun onPreUnbind(event: PreUnbindEvent) = callEvent("PreUnbindEvent", event)

    @SubscribeAQQBotEvent
    fun onPreInformation(event: PreInformationEvent) = callEvent("PreInformationEvent", event)

    @SubscribeAQQBotEvent
    fun onPreRemoteCommand(event: PreRemoteCommandEvent) = callEvent("PreRemoteCommandEvent", event)

    @SubscribeAQQBotEvent
    fun onReceiveMessage(event: ReceiveMessageEvent) = callEvent("ReceiveMessageEvent", event)
}