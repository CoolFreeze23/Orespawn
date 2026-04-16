/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.material.Material
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.world.World
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class OreBasicStone
extends Block {
    public OreBasicStone(int par1, float f1, float f2) {
        super(Material.field_151576_e);
        this.func_149711_c(f1);
        this.func_149752_b(f2);
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        this.func_149675_a(false);
    }

    public static Entity spawnCreature(World par0World, int par1, String name, double par2, double par4, double par6) {
        Entity var8 = null;
        var8 = name == null ? EntityList.func_75616_a((int)par1, (World)par0World) : EntityList.func_188429_b((ResourceLocation)new ResourceLocation(name), (World)par0World);
        if (var8 != null) {
            var8.func_70012_b(par2, par4, par6, par0World.field_73012_v.nextFloat() * 360.0f, 0.0f);
            par0World.func_72838_d(var8);
            ((EntityLiving)var8).func_70642_aH();
        }
        return var8;
    }
}

