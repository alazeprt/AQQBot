package top.alazeprt.aqqbot.mixin

import net.minecraft.network.ClientConnection
import net.minecraft.server.network.ServerPlayNetworkHandler
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow

@Mixin(ServerPlayNetworkHandler::class)
class PlayNetworkMixin {
    @Shadow
    lateinit var connection: ClientConnection
}