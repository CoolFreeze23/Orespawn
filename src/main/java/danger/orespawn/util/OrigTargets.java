package danger.orespawn.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;

/**
 * ENT-S-127 — the prey rule of 1.7.10's vanilla target tasks, port-wide (owner's ruling, 2026-09-04 night: an engine
 * convention, one helper).
 *
 * <p>1.7.10's {@code EntityAITarget.isSuitableTarget} asked {@code EntityLiving.canAttackClass(Class)} of every candidate
 * ahead of the task's own selector, and that method's only body — client jar sha1 e80d9b3b…, {@code sw.a(Class)}; no
 * OreSpawn class overrides it — is {@code cls != EntityCreeper.class && cls != EntityGhast.class}. So every vanilla
 * {@code EntityAINearestAttackableTarget} an OreSpawn tameable registered with {@code IMob.mobSelector} (orig
 * Dragon.java:116, Leon.java:93, ThePrinceAdult.java:113, ThePrinceTeen.java:117) never took a Creeper or a Ghast,
 * whatever the selector said. In 1.21.1 the Ghast is still refused by the engine ({@code Mob.canAttackType}, applied in
 * {@code TargetingConditions.test}); the Creeper is not, so the selector of every port goal that maps one of those tasks
 * reads {@link #vanillaTaskPrey} — the IMob convention's {@code Enemy} test (ENT-S-124) less the Creeper, vanilla's own
 * IronGolem idiom. Not this helper's: the species' custom scans, which took Creepers in both trees (orig Dragon.java:561,
 * Leon.java:412, ThePrinceAdult.java:495 accept any EntityMob), and the Boyfriend / Girlfriend goals, whose orig
 * {@code MyEntityAITarget.isSuitableTarget} never called {@code canAttackClass} and granted the Creeper
 * (MyEntityAITarget.java:111) and the Ghast (:114) explicitly.</p>
 */
public final class OrigTargets {

    private OrigTargets() {
    }

    /**
     * orig {@code EntityLiving.canAttackClass} composed over {@code IMob.mobSelector}: a hostile ({@code Enemy}, ENT-S-124)
     * that is no Creeper. The Ghast half of {@code canAttackClass} is vanilla 1.21.1's own {@code Mob.canAttackType}.
     */
    public static boolean vanillaTaskPrey(LivingEntity candidate) {
        return candidate instanceof Enemy && !(candidate instanceof Creeper);
    }
}
