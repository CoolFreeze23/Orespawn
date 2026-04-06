package danger.orespawn.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityRedAnt extends EntityAnt {
    private int attackDelay = 20;

    public EntityRedAnt(EntityType<? extends EntityRedAnt> type, Level level) {
        super(type, level);
        this.moveSpeed = 0.2;
        this.xpReward = 1;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 10, 1.0));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.random.nextInt(15) != 0) return false;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        return target.hurt(this.damageSources().mobAttack(this), 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) return;
        if (this.attackDelay > 0) this.attackDelay--;
        if (this.attackDelay > 0) return;
        this.attackDelay = 20;
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) return;
        Player nearby = this.level().getNearestPlayer(this, 1.5);
        if (nearby != null) this.doHurtTarget(nearby);
    }
}
