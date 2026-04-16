/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.util.EnumActionResult
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.items;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemGenericEgg
extends Item {
    Class<? extends Entity> entityClass;

    public ItemGenericEgg(String name, Class<? extends Entity> entityClass) {
        this.func_77655_b(name);
        this.setRegistryName(name);
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        this.entityClass = entityClass;
        ModItems.ITEMS.add(this);
    }

    public EnumActionResult func_180614_a(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.field_72995_K) {
            return EnumActionResult.SUCCESS;
        }
        Entity entity = EntityList.func_191304_a(this.entityClass, (World)worldIn);
        assert (entity != null);
        entity.func_70107_b((double)pos.func_177958_n(), (double)(pos.func_177956_o() + 1), (double)pos.func_177952_p());
        worldIn.func_72838_d(entity);
        ((EntityLiving)entity).func_70642_aH();
        if (!player.func_184812_l_()) {
            player.func_184586_b(hand).func_190918_g(1);
        }
        return EnumActionResult.SUCCESS;
    }
}

