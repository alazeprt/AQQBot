package top.alazeprt.aqqbot.mixin

import net.minecraft.network.MessageType
import net.minecraft.server.PlayerManager
import net.minecraft.text.Text
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import top.alazeprt.aqqbot.AQQBotFabric
import top.alazeprt.aqqbot.adapter.FabricPlayer
import top.alazeprt.aqqbot.event.AChatEvent
import java.util.UUID

@Mixin(PlayerManager::class)
class ChatMixin {
    @Inject(method = ["broadcastChatMessage"], at = [At("HEAD")])
    fun broadcastChatMessage(text: Text, messageType: MessageType, uUID: UUID) {
        if (messageType == MessageType.CHAT) {
            val player = AQQBotFabric.server.playerManager.getPlayer(uUID)
            if (player != null) {
                AChatEvent(AQQBotFabric.instance, FabricPlayer(player), text.string).handle()
            }
        }
    }
}