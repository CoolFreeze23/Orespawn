package danger.orespawn.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Crystal dimension grass block. Supports planting on top.
 * In 1.21, plant sustainability is handled via block tags rather than canSustainPlant.
 * Register this block under the appropriate plant-supporting tags.
 */
public class CrystalGrass extends Block {
    public CrystalGrass(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
