/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.block.state.IBlockState
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

import danger.orespawn.blocks.BlockCornPlant;
import danger.orespawn.blocks.TileEntityPlant;
import danger.orespawn.init.ModBlocks;
import java.util.Random;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CornPlantGenerator
extends WorldGenerator {
    private BlockCornPlant plant = (BlockCornPlant)ModBlocks.CORN_PLANT;
    private IBlockState state = this.plant.func_176223_P();

    public boolean func_180709_b(World worldIn, Random rand, BlockPos position) {
        for (int i = 0; i < 32; ++i) {
            int heightContribution;
            BlockPos blockpos = position.func_177982_a(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            if (worldIn.field_73011_w.func_177495_o() || !worldIn.func_175623_d(blockpos) || this.plant.isBlockUnderCorn(worldIn, blockpos) || this.plant.isBlockUnderAir(worldIn, blockpos) || !this.plant.isBlockUnderGrass(worldIn, blockpos)) continue;
            int maxHeight = 21;
            for (int height = 0; height < maxHeight && (worldIn.func_175623_d(blockpos) || worldIn.func_180495_p(blockpos).func_177230_c() == Blocks.field_150329_H) && blockpos.func_177956_o() < 255 && this.plant.canBlockStay(worldIn, blockpos); height += heightContribution) {
                worldIn.func_180501_a(blockpos, this.state.func_177226_a((IProperty)BlockCornPlant.STAGE, (Comparable)Integer.valueOf(3)), 2);
                TileEntityPlant tileEntityPlant = (TileEntityPlant)worldIn.func_175625_s(blockpos);
                heightContribution = tileEntityPlant.getHeightContribution();
                blockpos = new BlockPos(blockpos.func_177958_n(), blockpos.func_177956_o() + 1, blockpos.func_177952_p());
            }
            worldIn.func_180501_a(blockpos, this.state, 2);
        }
        return true;
    }

    @SubscribeEvent
    public void decorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        Biome biome = world.getBiomeForCoordsBody(event.getPos());
        Random rand = event.getRand();
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.GRASS) {
            if (rand.nextDouble() > 0.01) {
                return;
            }
            int x = rand.nextInt(16) + 8;
            int y = rand.nextInt(16) + 8;
            CornPlantGenerator gen = new CornPlantGenerator();
            gen.func_180709_b(world, rand, world.func_175645_m(event.getPos().func_177982_a(x, 0, y)));
        }
    }
}

