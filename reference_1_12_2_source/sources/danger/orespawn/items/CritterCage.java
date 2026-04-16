/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.passive.EntityHorse
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.EnumActionResult
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.EnumParticleTypes
 *  net.minecraft.util.SoundCategory
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 */
package danger.orespawn.items;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.entity.EntityCage;
import danger.orespawn.init.ModItems;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CritterCage
extends Item {
    private static final ArrayList<CritterCage> critterCages = new ArrayList();
    private final float chance;
    Class<? extends Entity> entityClass;

    public CritterCage(Class<? extends Entity> entityClass, String unlocalizedName) {
        this(entityClass, unlocalizedName, 1.0f);
    }

    public CritterCage(Class<? extends Entity> entityClass, String unlocalizedName, float chance) {
        this.func_77655_b(unlocalizedName);
        this.setRegistryName(unlocalizedName);
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        this.field_77777_bU = 16;
        this.entityClass = entityClass;
        this.chance = chance;
        ModItems.ITEMS.add(this);
        critterCages.add(this);
    }

    public float getChance() {
        return this.chance;
    }

    public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.func_184586_b(hand);
        EnumActionResult result = this.handleRightClick(world, player, hand, player.func_180425_c(), false);
        if (result == EnumActionResult.SUCCESS && !player.func_184812_l_()) {
            stack.func_190918_g(1);
        }
        return new ActionResult(result, (Object)stack);
    }

    public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing blockside, float p_180614_6_, float p_180614_7_, float p_180614_8_) {
        EnumActionResult result = this.handleRightClick(world, player, hand, pos, true);
        if (result != EnumActionResult.FAIL && !player.func_184812_l_()) {
            player.func_184586_b(hand).func_190918_g(1);
        }
        return result;
    }

    private EnumActionResult handleRightClick(World world, EntityPlayer entityPlayer, EnumHand hand, BlockPos position, boolean clickingOnBlock) {
        if (this.entityClass == null) {
            if (!world.field_72995_K) {
                EntityCage ec = new EntityCage(world, entityPlayer, this.entityClass, null);
                ec.func_184538_a((Entity)entityPlayer, entityPlayer.field_70125_A, entityPlayer.field_70177_z, 0.0f, 1.5f, 1.0f);
                world.func_72838_d((Entity)ec);
            }
            world.func_184133_a(entityPlayer, entityPlayer.func_180425_c(), SoundEvents.field_187511_aA, SoundCategory.PLAYERS, 1.0f, 1.5f);
            return EnumActionResult.SUCCESS;
        }
        if (world.field_72995_K) {
            return EnumActionResult.SUCCESS;
        }
        if (clickingOnBlock) {
            for (int i = 0; i < 6; ++i) {
                world.func_175688_a(EnumParticleTypes.SMOKE_LARGE, (double)((float)position.func_177958_n() + 0.5f), (double)((float)position.func_177956_o() + 1.25f), (double)((float)position.func_177952_p() + 0.5f), 0.0, 0.0, 0.0, new int[]{0});
                world.func_175688_a(EnumParticleTypes.EXPLOSION_LARGE, (double)((float)position.func_177958_n() + 0.5f), (double)((float)position.func_177956_o() + 1.25f), (double)((float)position.func_177952_p() + 0.5f), 0.0, 0.0, 0.0, new int[]{0});
                world.func_175688_a(EnumParticleTypes.REDSTONE, (double)((float)position.func_177958_n() + 0.5f), (double)((float)position.func_177956_o() + 1.25f), (double)((float)position.func_177952_p() + 0.5f), 0.0, 0.0, 0.0, new int[]{0});
            }
            world.func_184133_a(entityPlayer, position, SoundEvents.field_187539_bB, SoundCategory.PLAYERS, 1.0f, 1.5f);
            Entity summon = EntityList.func_191304_a(this.entityClass, (World)world);
            summon.func_70107_b((double)((float)position.func_177958_n() + 0.5f), (double)(position.func_177956_o() + 1), (double)((float)position.func_177952_p() + 0.5f));
            if (this.entityClass == EntityHorse.class) {
                ((EntityHorse)summon).func_110235_q(world.field_73012_v.nextInt());
            }
            world.func_72838_d(summon);
            EntityItem empty = new EntityItem(world);
            empty.func_92058_a(new ItemStack(ModItems.EMPTY_CAGE));
            empty.func_70107_b((double)position.func_177958_n(), (double)position.func_177956_o(), (double)position.func_177952_p());
            world.func_72838_d((Entity)empty);
            ItemStack stack = entityPlayer.func_184586_b(hand);
            if (summon instanceof EntityLiving && stack.func_82837_s()) {
                summon.func_96094_a(stack.func_82833_r());
            }
            return EnumActionResult.SUCCESS;
        }
        if (!world.field_72995_K) {
            String name = null;
            if (entityPlayer.func_184586_b(hand).func_82837_s()) {
                name = entityPlayer.func_184586_b(hand).func_82833_r();
            }
            EntityCage ec = new EntityCage(world, entityPlayer, this.entityClass, name);
            ec.func_184538_a((Entity)entityPlayer, entityPlayer.field_70125_A, entityPlayer.field_70177_z, 0.0f, 1.5f, 1.0f);
            world.func_72838_d((Entity)ec);
        }
        world.func_184133_a(entityPlayer, entityPlayer.func_180425_c(), SoundEvents.field_187511_aA, SoundCategory.PLAYERS, 1.0f, 1.5f);
        return EnumActionResult.SUCCESS;
    }

    public static CritterCage getCageFromEntity(Entity e) {
        for (CritterCage cc : critterCages) {
            if (cc.entityClass != e.getClass()) continue;
            return cc;
        }
        return null;
    }
}

