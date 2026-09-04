package danger.orespawn.entity.ai;

import danger.orespawn.entity.Mothra;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;

/**
 * Port of the original {@code MyEntityAINearestAttackableTarget} (orig MyEntityAINearestAttackableTarget.java) — the
 * Boyfriend's and Girlfriend's own nearest-attackable task (orig Boyfriend.java:138 / :141, Girlfriend.java:164 / :167:
 * the EntityCreeper hunt at 20 and the IMob hunt at 15) and, through {@code MyValentineTarget}'s copy of the same scan
 * (orig MyValentineTarget.java:60), the Girlfriend's two Valentine tasks (Girlfriend.java:161-162, 16). Built on vanilla's
 * {@link NearestAttackableTargetGoal} for its hold ({@code TargetGoal.canContinueToUse} at {@link #getFollowDistance},
 * ENT-S-129) and start; three things are the original's and not vanilla's (ENT-S-135, the targeting ledger's
 * MyEntityAINearestAttackableTarget / MyValentineTarget scan-set rows):
 * <ul>
 *   <li>the scan set is a BOX alone — {@code selectEntitiesWithinAABB(targetClass, boundingBox.expand(targetDistance, 4,
 *       targetDistance), selector)} (orig :56) — where vanilla's goal intersects that box with a {@code range(followDistance)}
 *       sphere scaled by the target's visibility (sneaking 0.8), and for {@code Player.class} scans all the level's players
 *       through the sphere with no box at all; the conditions here carry no range;</li>
 *   <li>the cadence is every pass — orig :53 rolls only when {@code targetChance > 0}, and every registration passes 0 —
 *       where vanilla's 3-arg forms draw a 1-in-5 acquisition roll per goal pass ({@code randomInterval} 10);</li>
 *   <li>the per-task {@code targetDistance} (orig :36) is the box's half-width and the hold's reach (orig
 *       MyEntityAITarget.java:52), not the FOLLOW_RANGE attribute (the Dragon's ENT-S-117 idiom, ENT-S-129).</li>
 * </ul>
 * The pick is the first candidate, in distance order, that {@link #canAttack} accepts: the conditions' selector (the
 * task's species rules, orig MyEntityAITarget.java:88-116 — the Boyfriend's / Girlfriend's {@code isMonsterPrey}, the
 * Valentine goal's owner / tamed-pet rule), {@code forCombat}'s screens (kept as at HEAD: the owner through
 * {@code TamableAnimal.canAttack}, the engine's Ghast refusal, MOD-036's Valentine safety gates), the goal's line of sight
 * (orig :108, {@code mustSee} — dropped for a {@code Player.class} task, whose candidates orig :96 answered ahead of that
 * step), then orig's grants ahead of the reach block — a Player (:96), Mothra (:105), a Creeper (:111), a Ghast (:114) —
 * and, for everything else with {@code nearbyOnly}, vanilla's reach cache: {@code TargetGoal.canReach}'s 1.5-block end-node
 * test (orig MyEntityAITarget.java:131-144; the path search range is the FOLLOW_RANGE attribute in both trees) behind a
 * {@code reducedTickDelay(10 + nextInt(5))} cache — 5-7 goal passes: the same in ticks for one candidate as orig's
 * {@code 10 + nextInt(5)} ticks, halved in candidate evaluations ({@code --reachCacheTime} runs per candidate). The order
 * is orig's {@link MyEntityAINearestAttackableTargetSorter} (orig :38, :57 — a creeper's distance² halved, no silhouette
 * term; {@link #targetOrder}); the Girlfriend's Valentine subclass sorts plain, as orig MyValentineTarget.java:41 / :61
 * did with {@code MyValentineTargetSorter} (ENT-S-139, the ledger's T4 §(v) row).
 */
public class MyEntityAINearestAttackableTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    /** orig MyEntityAINearestAttackableTarget.java:36 {@code targetDistance} — the box's half-width and the hold's reach. */
    private final double targetDistance;
    /** orig MyEntityAINearestAttackableTarget.java:23 / :38 {@code theNearestAttackableTargetSorter} — the task's own sorter, held as orig held it (ENT-S-139). */
    private final MyEntityAINearestAttackableTargetSorter targetSorter;

    /**
     * @param targetDistance orig's {@code par3} (:36): 20 for the Creeper hunt, 15 for the IMob hunt, 16 for the Valentine tasks
     * @param mustSee        orig's {@code par5} (the sight step of MyEntityAITarget.java:108)
     * @param nearbyOnly     orig's {@code par6} (the reachability test of MyEntityAITarget.java:117-127)
     * @param selector       orig's {@code par7IEntitySelector} composed with the task's own {@code isSuitableTarget} rules
     */
    public MyEntityAINearestAttackableTargetGoal(Mob owner, Class<T> targetClass, double targetDistance, boolean mustSee,
                                                 boolean nearbyOnly, @Nullable Predicate<LivingEntity> selector) {
        super(owner, targetClass, 0, mustSee, nearbyOnly, selector); // orig :36 targetChance 0: no roll (:53 gates only a chance > 0) — every pass
        this.targetDistance = targetDistance;
        this.targetSorter = new MyEntityAINearestAttackableTargetSorter(owner); // orig :38 — new MyEntityAINearestAttackableTargetSorter(this, par1) (ENT-S-139)
        // orig :56 — the box is the task's only bound: vanilla's ctor adds a range(getFollowDistance()) sphere, scaled by the
        // target's visibility percent, that the original never had; rebuilt without it, the selector and forCombat's screens kept
        TargetingConditions conditions = TargetingConditions.forCombat().selector(selector);
        if (!mustSee) conditions.ignoreLineOfSight(); // vanilla's own `!mustSee → ignoreLineOfSight`, which the rebuild had dropped (T3b refuter B, D2); every registration passes true, so inert today
        if (this.targetType == Player.class) conditions.ignoreLineOfSight(); // orig MyEntityAITarget.java:96 — a Player is answered BEFORE the sight step :108 (and the reach block :117, see canAttack): the Valentine Player task takes a player it cannot see, as orig did (T3b refuter A, D2)
        this.targetConditions = conditions;
    }

    @Override
    protected double getFollowDistance() {
        return this.targetDistance; // orig :36 the box (:56) and MyEntityAITarget.java:52 the hold beyond targetDistance², not the FOLLOW_RANGE attribute (ENT-S-129)
    }

    /**
     * orig MyEntityAITarget.java:78-129 {@code isSuitableTarget}: the candidates it answers BEFORE the nearbyOnly reach block
     * (:117-127) — a Player (:96-98; {@code valentines_day != 0} is the Valentine task's own {@code canUse} gate, orig
     * MyValentineTarget.java:48, and no Creeper- or Mob-class candidate is a player), Mothra (:105-107), an EntityCreeper
     * (:111-113), an EntityGhast (:114-116) — are taken once the conditions pass: the selector, {@code forCombat}'s screens
     * (which refuse a Ghast through the engine's {@code Mob.canAttackType} first, ENT-S-127) and, unless the ctor dropped it,
     * the line of sight — orig's :108 precedes the Creeper and Ghast grants as here, follows the Player grant (hence the
     * {@code Player.class} drop) and follows the Mothra grant (the ENT-S-128 deferral disclosed at {@code isMonsterPrey}).
     * Every other candidate goes through vanilla's {@code TargetGoal.canAttack}, whose reach cache is the nearbyOnly test
     * (:117-127 / :131-144; the conditions are tested again inside it — pure within a tick, the sensing cache). Vanilla's
     * reach-tests every candidate: at T3b's draft a fence-ringed creeper inside the Creeper task's box was refused where
     * orig :111 returned before :117 (T3b refuter A, D1).
     */
    @Override
    protected boolean canAttack(@Nullable LivingEntity candidate, TargetingConditions conditions) {
        if (candidate == null || !conditions.test(this.mob, candidate)) return false;
        if (candidate instanceof Player) return true;  // orig MyEntityAITarget.java:96-98
        if (candidate instanceof Mothra) return true;  // orig :105-107
        if (candidate instanceof Creeper) return true; // orig :111-113
        if (candidate instanceof Ghast) return true;   // orig :114-116
        return super.canAttack(candidate, conditions); // orig :117-127 — the nearbyOnly reach cache (:131-144), then :128 true
    }

    /**
     * orig MyEntityAINearestAttackableTarget.java:57 {@code Collections.sort(var5, this.theNearestAttackableTargetSorter)} — the
     * order the box scan is walked in: {@link MyEntityAINearestAttackableTargetSorter}, a creeper's distance² halved and no
     * silhouette term (orig MyEntityAINearestAttackableTargetSorter.java:21-31 — not GenericTargetSorter's :24-26 division).
     * {@code MyValentineTarget.java:61} sorted its copy of the scan with {@code MyValentineTargetSorter} (plain distance², :20-24):
     * the Girlfriend's Valentine subclass overrides this. Stable in both trees — {@code TargetSelection.firstMatch} keeps the
     * list's order on ties as {@code Collections.sort} did. ENT-S-139 (the targeting ledger's T4 §(v) row).
     */
    protected Comparator<? super T> targetOrder() {
        return this.targetSorter;
    }

    /**
     * orig MyEntityAINearestAttackableTarget.java:56-64 (and MyValentineTarget.java:60-68): the box scan, sorted, the first
     * candidate {@code isSuitableTarget} accepts — here {@link #canAttack} with the goal's conditions, which carries the
     * selector, {@code forCombat}'s screens and the line of sight, then orig's pre-reach grants and the {@code nearbyOnly}
     * reach cache. Vanilla's {@code findTarget} took the nearest candidate the conditions ADMIT, with the range sphere and,
     * for players, no box. The 3-arg {@code getEntitiesOfClass} applies no NO_SPECTATORS filter of its own; a spectator is
     * refused by {@code TargetingConditions.test}'s {@code canBeSeenByAnyone}.
     */
    @Override
    protected void findTarget() {
        List<T> candidates = this.mob.level().getEntitiesOfClass(this.targetType,
                this.getTargetSearchArea(this.targetDistance), e -> true);          // orig :56 — expand(targetDistance, 4, targetDistance)
        this.target = TargetSelection.firstMatch(candidates, this.targetOrder(),
                candidate -> this.canAttack(candidate, this.targetConditions));    // orig :57-63 — sorted (:57, the task's sorter — targetOrder), the first suitable (OPT-021)
    }
}
