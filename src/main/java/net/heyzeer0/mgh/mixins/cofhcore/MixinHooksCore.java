package net.heyzeer0.mgh.mixins.cofhcore;

import cofh.lib.util.helpers.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EverNife on 07/06/2019.
 */

@Pseudo
@Mixin(targets = "cofh/asmhooks/HooksCore", remap = false)
public abstract class MixinHooksCore {

    @Overwrite
    public static List getEntityCollisionBoxes(World world, Entity entity, AxisAlignedBB bb) {

        if (!entity.canBePushed()) {
            List collidingBoundingBoxes = new ArrayList();
            int i = MathHelper.floor(bb.minX);
            int j = MathHelper.floor(bb.maxX + 1.0D);
            int k = MathHelper.floor(bb.minY);
            int l = MathHelper.floor(bb.maxY + 1.0D);
            int i1 = MathHelper.floor(bb.minZ);
            int j1 = MathHelper.floor(bb.maxZ + 1.0D);

            for (int x = i; x < j; ++x) {
                boolean xBound = x >= -30000000 & x < 30000000;
                for (int z = i1; z < j1; ++z) {
                    boolean def = xBound & z >= -30000000 & z < 30000000;
                    if (!world.blockExists(x, 64, z)) {
                        continue;
                    }
                    if (def) {
                        for (int y = k - 1; y < l; ++y) {
                            world.getBlock(x, y, z).addCollisionBoxesToList(world, x, y, z, bb, collidingBoundingBoxes, entity);
                        }
                    } else {
                        for (int y = k - 1; y < l; ++y) {
                            Blocks.bedrock.addCollisionBoxesToList(world, x, y, z, bb, collidingBoundingBoxes, entity);
                        }
                    }
                }
            }

            return collidingBoundingBoxes;
        }
        return world.getCollidingBoundingBoxes(entity, bb);
    }

}
