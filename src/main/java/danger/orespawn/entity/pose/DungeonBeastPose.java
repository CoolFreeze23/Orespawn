package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelDungeonBeast.poseFrom} reads from its entity, as an interface the entity already satisfies (the hooks,
 * owner 2026-09-14, addendum item 10; the {@link Robot2Pose} shape): the per-entity render scratch whose {@code ri1} /
 * {@code ri2} are the jaw latch re-rolled at each falling zero-crossing (orig ModelDungeonBeast.java:546-573), the attacking
 * flag that fixes the tail amplitude and the re-roll (orig :506, :550), and the entity's RNG the re-roll draws from (orig
 * :551-552). The classic model and the GeckoLib hook both pose from this, so the parity harness can drive them headlessly
 * with a declared state and a seeded source instead of a live entity.
 */
public interface DungeonBeastPose {
    /** orig ModelDungeonBeast.java:546 {@code e.getRenderInfo()} (orig DungeonBeast.java:98-100); ri1 / ri2 rest at 0 (the jaws chew). */
    RenderInfo getRenderInfo();

    /** orig ModelDungeonBeast.java:506, :550 {@code e.getAttacking()}; rest 0. */
    int getAttacking();

    /** orig ModelDungeonBeast.java:551-552 {@code rand.nextInt(15)} (the entity's source, the Kraken convention). */
    RandomSource getRandom();
}
