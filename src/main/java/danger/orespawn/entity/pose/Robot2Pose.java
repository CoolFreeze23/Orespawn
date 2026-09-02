package danger.orespawn.entity.pose;

import danger.orespawn.entity.client.RenderInfo;
import net.minecraft.util.RandomSource;

/**
 * What {@code ModelRobot2.setupAnim} reads from its entity, as an interface the
 * entity already satisfies. The classic model and the GeckoLib hook both pose
 * from this, so the parity harness can drive them headlessly with a declared
 * state instead of a live entity (a dedicated gametest server strips client
 * model classes, so the comparison cannot run there).
 */
public interface Robot2Pose {
    RenderInfo getRenderInfo();

    int getAttacking();

    RandomSource getRandom();
}
