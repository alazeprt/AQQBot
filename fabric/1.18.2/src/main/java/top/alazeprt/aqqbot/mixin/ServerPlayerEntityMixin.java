package top.alazeprt.aqqbot.mixin;

import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.alazeprt.aqqbot.AQQBotFabric;
import top.alazeprt.aqqbot.adapter.FabricAdapter;
import top.alazeprt.aqqbot.adapter.FabricPlayer;
import top.alazeprt.aqqbot.event.AChatEvent;
import top.alazeprt.aqqbot.profile.APlayer;

import java.util.UUID;
import java.util.function.Function;

@Mixin(PlayerManager.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"))
    public void broadcast(Text text, MessageType messageType, UUID uUID, CallbackInfo ci) {
        if (messageType == MessageType.CHAT) {
            APlayer player = FabricAdapter.INSTANCE.getOnlinePlayer(uUID);
            new AChatEvent(AQQBotFabric.instance, player,
                    text.getString().replace("<" + player.getName() + "> ", "")).handle();
        }
    }

    @Inject(method = "broadcast(Lnet/minecraft/text/Text;Ljava/util/function/Function;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"))
    public void broadcast(Text text, Function<ServerPlayerEntity, Text> function, MessageType messageType, UUID uUID, CallbackInfo ci) {
        if (messageType == MessageType.CHAT) {
            APlayer player = FabricAdapter.INSTANCE.getOnlinePlayer(uUID);
            new AChatEvent(AQQBotFabric.instance, player,
                    text.getString().replace("<" + player.getName() + "> ", "")).handle();
        }
    }
}
