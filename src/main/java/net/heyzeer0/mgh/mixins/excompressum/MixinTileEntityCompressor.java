package net.heyzeer0.mgh.mixins.excompressum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Created by Frani on 28/06/2017.
 */

@Pseudo
@Mixin(targets = "net/blay09/mods/excompressum/tile/TileEntityAutoCompressor", remap = false)
public abstract class MixinTileEntityCompressor {

    @Overwrite
    public String func_145825_b() {
        return "Auto Compressor";
    }

}
