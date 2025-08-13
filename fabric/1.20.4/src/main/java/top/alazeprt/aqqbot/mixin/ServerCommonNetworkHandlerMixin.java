package top.alazeprt.aqqbot.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerCommonNetworkHandler.class)
public interface ServerCommonNetworkHandlerMixin {
    @Accessor("connection")
    ClientConnection getConnection();
}
