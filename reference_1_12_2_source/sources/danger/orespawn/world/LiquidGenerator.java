/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.init.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.biome.Biome
 *  net.minecraft.world.gen.feature.WorldGenerator
 *  net.minecraftforge.event.terraingen.DecorateBiomeEvent$Decorate
 *  net.minecraftforge.event.terraingen.DecorateBiomeEvent$Decorate$EventType
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 */
package danger.orespawn.world;

import danger.orespawn.init.ModBiomes;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LiquidGenerator
extends WorldGenerator {
    public boolean func_180709_b(World worldIn, Random rand, BlockPos position) {
        if (!worldIn.field_73011_w.func_177495_o() && worldIn.func_175623_d(position)) {
            if (Math.random() < 0.5) {
                worldIn.func_175656_a(position, Blocks.field_150358_i.func_176223_P());
            } else {
                worldIn.func_175656_a(position, Blocks.field_150356_k.func_176223_P());
            }
        }
        return true;
    }

    @SubscribeEvent
    public void decorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        Biome biome = world.getBiomeForCoordsBody(event.getPos());
        Random rand = event.getRand();
        if (biome == ModBiomes.MINING_BIOME && event.getType() == DecorateBiomeEvent.Decorate.EventType.GRASS) {
            if (rand.nextDouble() > 0.1) {
                return;
            }
            int x = rand.nextInt(16) + 8;
            int y = rand.nextInt(16) + 8;
            LiquidGenerator gen = new LiquidGenerator();
            gen.func_180709_b(world, rand, world.func_175645_m(event.getPos().func_177982_a(x, 0, y)));
        }
    }
}

