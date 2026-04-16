/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  javax.annotation.ParametersAreNonnullByDefault
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityList
 *  net.minecraft.entity.EntityLiving
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.item.EntityItem
 *  net.minecraft.entity.passive.EntityHorse
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.entity.projectile.EntityThrowable
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.EnumParticleTypes
 *  net.minecraft.util.SoundCategory
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.RayTraceResult
 *  net.minecraft.world.World
 */
package danger.orespawn.entity;

import danger.orespawn.init.ModItems;
import danger.orespawn.items.CritterCage;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCage
extends EntityThrowable {
    public Class<? extends Entity> my_class;
    public String customName;
    private World throwerWorld = null;
    private EntityPlayer thrower = null;

    public EntityCage(World world) {
        super(world);
        this.throwerWorld = world;
    }

    public EntityCage(World par1World, EntityPlayer par2EntityLiving, Class<? extends Entity> my_class, @Nullable String customName) {
        super(par1World, (EntityLivingBase)par2EntityLiving);
        this.throwerWorld = par1World;
        this.thrower = par2EntityLiving;
        this.my_class = my_class;
        this.customName = customName;
        if (this.thrower.field_70170_p != null) {
            this.throwerWorld = this.thrower.field_70170_p;
        }
    }

    @ParametersAreNonnullByDefault
    protected void func_70184_a(RayTraceResult result) {
        if (this.my_class == null) {
            if (result.field_72308_g != null && this.field_70146_Z.nextInt(10) >= 2) {
                Entity hit;
                if (this.throwerWorld != null) {
                    for (int var3 = 0; var3 < 4; ++var3) {
                        this.throwerWorld.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, result.field_72308_g.field_70165_t, result.field_72308_g.field_70163_u + 0.25, result.field_72308_g.field_70161_v, 0.0, 0.0, 0.0, new int[0]);
                        this.throwerWorld.func_175688_a(EnumParticleTypes.EXPLOSION_NORMAL, result.field_72308_g.field_70165_t, result.field_72308_g.field_70163_u + 0.25, result.field_72308_g.field_70161_v, 0.0, 0.0, 0.0, new int[0]);
                        this.throwerWorld.func_175688_a(EnumParticleTypes.REDSTONE, result.field_72308_g.field_70165_t, result.field_72308_g.field_70163_u + 0.25, result.field_72308_g.field_70161_v, 0.0, 0.0, 0.0, new int[0]);
                    }
                    if (this.thrower != null) {
                        this.thrower.func_184185_a(SoundEvents.field_187539_bB, 1.0f, 1.5f);
                    }
                }
                if ((hit = result.field_72308_g) instanceof EntityLiving) {
                    CritterCage cc = CritterCage.getCageFromEntity(hit);
                    if (cc == null) {
                        return;
                    }
                    if (this.field_70146_Z.nextFloat() < cc.getChance() && !this.field_70170_p.field_72995_K) {
                        this.func_145779_a(cc, 1);
                        ((EntityLiving)hit).func_184174_b(false);
                        ((EntityLiving)hit).func_70106_y();
                    }
                }
            }
        } else {
            result.func_178782_a();
            result.func_178782_a();
            BlockPos position = result.func_178782_a();
            if (result.field_72308_g != null) {
                position = result.field_72308_g.func_180425_c();
            }
            for (int i = 0; i < 6; ++i) {
                this.field_70170_p.func_175688_a(EnumParticleTypes.SMOKE_LARGE, (double)((float)position.func_177958_n() + 0.5f), (double)((float)position.func_177956_o() + 1.25f), (double)((float)position.func_177952_p() + 0.5f), 0.0, 0.0, 0.0, new int[]{0});
                this.field_70170_p.func_175688_a(EnumParticleTypes.EXPLOSION_LARGE, (double)((float)position.func_177958_n() + 0.5f), (double)((float)position.func_177956_o() + 1.25f), (double)((float)position.func_177952_p() + 0.5f), 0.0, 0.0, 0.0, new int[]{0});
                this.field_70170_p.func_175688_a(EnumParticleTypes.REDSTONE, (double)((float)position.func_177958_n() + 0.5f), (double)((float)position.func_177956_o() + 1.25f), (double)((float)position.func_177952_p() + 0.5f), 0.0, 0.0, 0.0, new int[]{0});
            }
            this.field_70170_p.func_184134_a(this.field_70165_t, this.field_70163_u, this.field_70161_v, SoundEvents.field_187539_bB, SoundCategory.PLAYERS, 1.0f, 1.5f, true);
            Entity summon = EntityList.func_191304_a(this.my_class, (World)this.field_70170_p);
            System.out.println(summon);
            summon.func_70107_b((double)position.func_177958_n(), (double)(position.func_177956_o() + 1), (double)position.func_177952_p());
            if (this.my_class == EntityHorse.class) {
                ((EntityHorse)summon).func_110235_q(this.field_70170_p.field_73012_v.nextInt());
            }
            this.field_70170_p.func_72838_d(summon);
            EntityItem empty = new EntityItem(this.field_70170_p);
            empty.func_92058_a(new ItemStack(ModItems.EMPTY_CAGE));
            empty.func_70107_b((double)position.func_177958_n(), (double)position.func_177956_o(), (double)position.func_177952_p());
            this.field_70170_p.func_72838_d((Entity)empty);
            if (summon instanceof EntityLiving && this.customName != null) {
                summon.func_96094_a(this.customName);
            }
        }
        if (!this.field_70170_p.field_72995_K) {
            this.func_70106_y();
        }
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        this.throwerWorld.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, this.field_70165_t, this.field_70163_u, this.field_70161_v, 0.0, 0.0, 0.0, new int[0]);
    }
}

