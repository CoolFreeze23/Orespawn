package danger.orespawn.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * Baby Dragon — distinct entity from the adult {@link Dragon}, matching the
 * 1.7.10 / Namu Wiki separation where Baby Dragon and Dragon are catalogued
 * as two entity types rather than one age-scaled mob.
 *
 * Per Namu Wiki / 1.7.10 reference:
 *   - Reduced HP (~50 vs adult 200)
 *   - Reduced attack damage (~5 vs adult 35)
 *   - Pre-fledged fire-breath: DragonFire defaults to 1 from spawn (so the
 *     baby can already shoot small fireballs without needing flint+steel),
 *     inherited from Dragon.defineSynchedData() which initialises the data
 *     accessor to 1.
 *   - Smaller hitbox (1.0 × 1.4 — registered in ModEntities) and visual
 *     scale (0.45×) — see BabyDragonRenderer.
 *
 * Inherits the full Dragon AI / flight / interaction stack so the baby still
 * grows up via the apple interaction (via Dragon#mobInteract) but does not
 * have the full adult HP/attack pool. The shared SynchedEntityData accessors
 * defined on Dragon resolve correctly because BabyDragon is a Dragon subclass.
 */
public class BabyDragon extends Dragon {

    private static final int BABY_MAX_HEALTH = 50;
    private static final double BABY_ATTACK = 5.0;
    private static final double BABY_SPEED = 0.30;
    private static final double BABY_FOLLOW_RANGE = 24.0;

    public BabyDragon(EntityType<? extends BabyDragon> type, Level level) {
        super(type, level);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BABY_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BABY_SPEED)
                .add(Attributes.ATTACK_DAMAGE, BABY_ATTACK)
                .add(Attributes.FOLLOW_RANGE, BABY_FOLLOW_RANGE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2);
    }

    @Override
    public int mygetMaxHealth() {
        return BABY_MAX_HEALTH;
    }
}
