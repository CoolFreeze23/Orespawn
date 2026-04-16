/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.gen.feature.WorldGenerator
 *  net.minecraftforge.event.terraingen.DecorateBiomeEvent$Decorate
 *  net.minecraftforge.event.terraingen.DecorateBiomeEvent$Decorate$EventType
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package danger.orespawn.world.gen.ores;

import danger.orespawn.init.ModBlocks;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntHillGenerator
extends WorldGenerator {
    public boolean func_180709_b(World worldIn, Random rand, BlockPos position) {
        if (worldIn.func_180495_p(position = position.func_177977_b()).func_177230_c() == Blocks.field_150349_c) {
            worldIn.func_180501_a(position, ModBlocks.ANT_BLOCK.func_176223_P(), 2);
        }
        return true;
    }

    @SubscribeEvent
    public void decorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        Random rand = event.getRand();
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.GRASS) {
            if (rand.nextDouble() > 0.04) {
                return;
            }
            int x = rand.nextInt(16) + 8;
            int z = rand.nextInt(16) + 8;
            AntHillGenerator gen = new AntHillGenerator();
            gen.func_180709_b(world, rand, world.func_175645_m(event.getPos().func_177982_a(x, 0, z)));
        }
    }
}

