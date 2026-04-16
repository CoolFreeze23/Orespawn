/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraft.world.gen.feature.WorldGenerator
 *  net.minecraft.world.gen.structure.template.Template
 *  net.minecraft.world.gen.structure.template.TemplateManager
 */
package danger.orespawn.world.gen.generators;

import danger.orespawn.util.interfaces.IStructure;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class WorldGenStructure
extends WorldGenerator
implements IStructure {
    public static String structureName;

    public WorldGenStructure(String name) {
        structureName = name;
    }

    public boolean func_180709_b(World worldIn, Random rand, BlockPos position) {
        WorldGenStructure.generateStructure(worldIn, position);
        return true;
    }

    public static void generateStructure(World world, BlockPos pos) {
        ResourceLocation location;
        MinecraftServer mcServer = world.func_73046_m();
        TemplateManager manager = worldServer.func_184163_y();
        Template template = manager.func_189942_b(mcServer, location = new ResourceLocation("orespawn", structureName));
        if (template != null) {
            IBlockState state = world.func_180495_p(pos);
            world.func_184138_a(pos, state, state, 3);
            template.func_186253_b(world, pos, settings);
        }
    }
}

