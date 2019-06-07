package net.heyzeer0.mgh.mixins.cofhcore;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by HeyZeer0 on 24/06/2017.
 * Copyright © HeyZeer0 - 2016
 */
@Pseudo
@Mixin(targets = "cofh/lib/util/helpers/SecurityHelper", remap = false)
public abstract class MixinSecurityHelper {

    private static final Map<UUID,GameProfile> cachedGameProfileMap = new HashMap<UUID, GameProfile>();
    @Overwrite
    public static GameProfile getProfile(UUID uuid, String name) {
        GameProfile owner = null;
        try {
            if (cachedGameProfileMap.containsKey(uuid)){
                return cachedGameProfileMap.get(uuid);
            }
            owner = MinecraftServer.getServer().func_152358_ax().func_152652_a(uuid);
            if (owner == null) {
                GameProfile temp = new GameProfile(uuid, name);
                owner = MinecraftServer.getServer().func_147130_as().fillProfileProperties(temp, true);
                if (owner != temp) {
                    MinecraftServer.getServer().func_152358_ax().func_152649_a(owner);
                }
            }
            cachedGameProfileMap.put(uuid,owner);
        }catch (Exception ignored){}
        return owner;
    }
}
