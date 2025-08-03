package top.alazeprt.aqqbot.api.webhook

import top.alazeprt.aqqbot.AQQBot
import java.net.InetSocketAddress

object WebhookProvider {
    private var webhookServer: AQQBotWebhookServer? = null

    fun create(plugin: AQQBot, address: InetSocketAddress) {
        webhookServer = AQQBotWebhookServer(plugin, address)
    }

    fun start() {
        webhookServer?.start()
    }

    fun stop() {
        webhookServer?.stop()
    }
}