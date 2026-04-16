/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.EntityAnt;
import danger.orespawn.MyEntityAIWanderALot;
import danger.orespawn.OreSpawnMain;
import danger.orespawn.OreSpawnTeleporter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;

public class EntityRedAnt
extends EntityAnt {
    int attack_delay = 20;

    public EntityRedAnt(World par1World) {
        super(par1World);
        this.func_70105_a(0.2f, 0.2f);
        this.moveSpeed = 0.2f;
        this.field_70728_aV = 1;
        this.func_70661_as().func_75491_a(true);
        this.field_70714_bg.func_75776_a(0, (EntityAIBase)new EntityAIPanic((EntityCreature)this, (double)1.4f));
        this.field_70714_bg.func_75776_a(1, (EntityAIBase)new EntityAIAttackOnCollide((EntityCreature)this, EntityPlayer.class, 1.0, false));
        this.field_70714_bg.func_75776_a(2, (EntityAIBase)new MyEntityAIWanderALot((EntityCreature)this, 10, 1.0));
        if (OreSpawnMain.PlayNicely == 0) {
            this.field_70715_bh.func_75776_a(1, (EntityAIBase)new EntityAINearestAttackableTarget((EntityCreature)this, EntityPlayer.class, 4, true));
        }
    }

    protected void func_110147_ax() {
        super.func_110147_ax();
        this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)this.mygetMaxHealth());
        this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(this.moveSpeed);
        this.func_110148_a(SharedMonsterAttributes.field_111264_e).func_111128_a(1.0);
    }

    public int mygetMaxHealth() {
        return 2;
    }

    public boolean func_70652_k(Entity par1Entity) {
        if (OreSpawnMain.OreSpawnRand.nextInt(15) != 0) {
            return false;
        }
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return false;
        }
        boolean var4 = par1Entity.func_70097_a(DamageSource.func_76358_a((EntityLivingBase)this), 1.0f);
        return var4;
    }

    public boolean func_70085_c(EntityPlayer par1EntityPlayer) {
        if (par1EntityPlayer == null) {
            return false;
        }
        if (!(par1EntityPlayer instanceof EntityPlayerMP)) {
            return false;
        }
        ItemStack var2 = par1EntityPlayer.field_71071_by.func_70448_g();
        if (var2 != null && var2.field_77994_a <= 0) {
            par1EntityPlayer.field_71071_by.func_70299_a(par1EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
            var2 = null;
        }
        if (var2 != null) {
            return false;
        }
        if (par1EntityPlayer.field_71093_bK != OreSpawnMain.DimensionID2) {
            MinecraftServer.func_71276_C().func_71203_ab().transferPlayerToDimension((EntityPlayerMP)par1EntityPlayer, OreSpawnMain.DimensionID2, (Teleporter)new OreSpawnTeleporter(MinecraftServer.func_71276_C().func_71218_a(OreSpawnMain.DimensionID2), OreSpawnMain.DimensionID2, this.field_70170_p));
        } else {
            MinecraftServer.func_71276_C().func_71203_ab().transferPlayerToDimension((EntityPlayerMP)par1EntityPlayer, 0, (Teleporter)new OreSpawnTeleporter(MinecraftServer.func_71276_C().func_71218_a(0), 0, this.field_70170_p));
        }
        return true;
    }

    public void func_70071_h_() {
        super.func_70071_h_();
        if (this.field_70128_L) {
            return;
        }
        if (this.attack_delay > 0) {
            --this.attack_delay;
        }
        if (this.attack_delay > 0) {
            return;
        }
        this.attack_delay = 20;
        if (this.field_70170_p.field_73013_u == EnumDifficulty.PEACEFUL) {
            return;
        }
        if (OreSpawnMain.PlayNicely != 0) {
            return;
        }
        EntityPlayer e = this.field_70170_p.func_72856_b((Entity)this, 1.5);
        if (e != null) {
            this.func_70652_k((Entity)e);
        }
    }
}

