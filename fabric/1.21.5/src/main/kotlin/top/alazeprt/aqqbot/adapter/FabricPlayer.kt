package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import top.alazeprt.aqqbot.AQQBotFabric
import top.alazeprt.aqqbot.mixin.ServerCommonNetworkHandlerMixin
import top.alazeprt.aqqbot.profile.APlayer
import java.net.InetSocketAddress
import java.util.*

class FabricPlayer(val player: ServerPlayerEntity): APlayer {
    override fun kick(reason: String) {
        player.networkHandler.disconnect(Text.of(reason))
    }

    override fun getIP(): InetSocketAddress? {
        val handler = player.networkHandler as ServerCommonNetworkHandlerMixin
        return handler.connection.address as InetSocketAddress?
    }

    override fun getName(): String {
        return player.name.string
    }

    override fun getUUID(): UUID {
        return player.uuid
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Text.of(message))
    }

    override fun sendMessage(message: TextComponent) {
        AQQBotFabric.audience.player(player.uuid).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermissionLevel(4) // TODO: luckperms hook
    }
}