/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.entity.ai.EntityAIBase
 *  net.minecraft.entity.ai.RandomPositionGenerator
 *  net.minecraft.entity.monster.IMob
 *  net.minecraft.entity.passive.EntityTameable
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.pathfinding.PathEntity
 *  net.minecraft.pathfinding.PathNavigate
 *  net.minecraft.util.Vec3
 */
package danger.orespawn;

import danger.orespawn.EntityCannonFodder;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.Vec3;

public class MyEntityAIAvoidEntity
extends EntityAIBase {
    private EntityCreature theEntity;
    private double farSpeed;
    private double nearSpeed;
    private Entity closestLivingEntity;
    private float distanceFromEntity;
    private PathEntity entityPathEntity;
    private PathNavigate entityPathNavigate;
    private Class targetEntityClass;

    public MyEntityAIAvoidEntity(EntityCreature par1EntityCreature, Class par2Class, float par3, double par4, double par6) {
        this.theEntity = par1EntityCreature;
        this.targetEntityClass = par2Class;
        this.distanceFromEntity = par3;
        this.farSpeed = par4;
        this.nearSpeed = par6;
        this.entityPathNavigate = par1EntityCreature.func_70661_as();
        this.func_75248_a(1);
    }

    public boolean func_75250_a() {
        Vec3 vec3;
        EntityCannonFodder cf;
        if (this.theEntity != null && this.theEntity instanceof EntityCannonFodder && (cf = (EntityCannonFodder)this.theEntity).get_is_activated() != 0) {
            return false;
        }
        if (this.targetEntityClass == EntityPlayer.class) {
            if (this.theEntity instanceof EntityTameable && ((EntityTameable)this.theEntity).func_70909_n()) {
                return false;
            }
            this.closestLivingEntity = this.theEntity.field_70170_p.func_72890_a((Entity)this.theEntity, (double)this.distanceFromEntity);
            if (this.closestLivingEntity == null) {
                return false;
            }
        } else {
            List list = this.theEntity.field_70170_p.func_82733_a(this.targetEntityClass, this.theEntity.field_70121_D.func_72314_b((double)this.distanceFromEntity, 3.0, (double)this.distanceFromEntity), IMob.field_82192_a);
            if (list.isEmpty()) {
                return false;
            }
            this.closestLivingEntity = (Entity)list.get(0);
        }
        if ((vec3 = RandomPositionGenerator.func_75461_b((EntityCreature)this.theEntity, (int)16, (int)7, (Vec3)Vec3.func_72443_a((double)this.closestLivingEntity.field_70165_t, (double)this.closestLivingEntity.field_70163_u, (double)this.closestLivingEntity.field_70161_v))) == null) {
            return false;
        }
        if (this.closestLivingEntity.func_70092_e(vec3.field_72450_a, vec3.field_72448_b, vec3.field_72449_c) < this.closestLivingEntity.func_70068_e((Entity)this.theEntity)) {
            return false;
        }
        this.entityPathEntity = this.entityPathNavigate.func_75488_a(vec3.field_72450_a, vec3.field_72448_b, vec3.field_72449_c);
        return this.entityPathEntity == null ? false : this.entityPathEntity.func_75880_b(vec3);
    }

    public boolean func_75253_b() {
        return !this.entityPathNavigate.func_75500_f();
    }

    public void func_75249_e() {
        this.entityPathNavigate.func_75484_a(this.entityPathEntity, this.farSpeed);
    }

    public void func_75251_c() {
        this.closestLivingEntity = null;
    }

    public void func_75246_d() {
        if (this.theEntity.func_70068_e(this.closestLivingEntity) < 49.0) {
            this.theEntity.func_70661_as().func_75489_a(this.nearSpeed);
        } else {
            this.theEntity.func_70661_as().func_75489_a(this.farSpeed);
        }
    }

    static EntityCreature func_98217_a(MyEntityAIAvoidEntity par0EntityAIAvoidEntity) {
        return par0EntityAIAvoidEntity.theEntity;
    }
}

