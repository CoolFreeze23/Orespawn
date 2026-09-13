package danger.orespawn.g1;

import com.google.gson.JsonObject;
import danger.orespawn.entity.client.RenderInfo;
import danger.orespawn.entity.pose.BeePose;
import danger.orespawn.entity.pose.CaterKillerPose;
import danger.orespawn.entity.pose.HerculesBeetlePose;
import danger.orespawn.entity.pose.PurplePowerPose;
import danger.orespawn.entity.pose.Robot2Pose;
import danger.orespawn.entity.pose.Robot3Pose;
import danger.orespawn.entity.pose.Robot4Pose;
import danger.orespawn.entity.pose.RockBasePose;
import danger.orespawn.entity.pose.RotatorPose;
import net.minecraft.util.RandomSource;

/**
 * A declared entity state standing in for the live entity on both sides of
 * the comparison: the compiled model's {@code poseFrom} and the production
 * hook each get a FRESH subject built from the same manifest entry, so a
 * latch or RNG re-roll evolves identically. {@link #after()} records what the
 * pose wrote back (RenderInfo latch, Robot4 shielding, the Rotator's fan
 * angle) for the parity tool.
 *
 * <p>ENT-S-146: the one seeded source stands for both the entity's random
 * ({@link Robot2Pose#getRandom()}) and the level's ({@link PurplePowerPose#getLevelRandom()});
 * no pose reads both. PurplePower's rolls are consumed three per frame on each side and
 * proven through the resulting pose (a roll drawn out of order or in excess moves a fan);
 * {@link #after()} is unchanged.</p>
 */
final class ProbeSubject implements Robot2Pose, Robot3Pose, Robot4Pose, RockBasePose, RotatorPose, PurplePowerPose,
        BeePose, CaterKillerPose, HerculesBeetlePose {
    private final RenderInfo renderInfo = new RenderInfo();
    private final int attacking;
    private final int rockType;
    private final RandomSource random;
    /** Slice 4c: an {@code rf1} preset was declared, so {@link #after()} reports the advanced angle. */
    private final boolean fanSpinDeclared;
    private int shielding = -1;

    ProbeSubject(JsonObject state) {
        this.attacking = state.has("attacking") ? state.get("attacking").getAsInt() : 0;
        this.rockType = state.has("rock_type") ? state.get("rock_type").getAsInt() : 0;
        this.renderInfo.ri1 = state.has("ri1") ? state.get("ri1").getAsInt() : 0;
        this.fanSpinDeclared = state.has("rf1");
        if (this.fanSpinDeclared) {
            this.renderInfo.rf1 = state.get("rf1").getAsFloat();
        }
        this.random = RandomSource.create(state.has("seed") ? state.get("seed").getAsLong() : 0L);
    }

    @Override
    public RenderInfo getRenderInfo() {
        return this.renderInfo;
    }

    @Override
    public int getAttacking() {
        return this.attacking;
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    /** ENT-S-146: the seeded source as the level's random (orig ModelPurplePower.java:57 {@code worldObj.rand}). */
    @Override
    public RandomSource getLevelRandom() {
        return this.random;
    }

    @Override
    public void setShielding(int shielding) {
        this.shielding = shielding;
    }

    @Override
    public int getRockType() {
        return this.rockType;
    }

    /** Observable writes made by the pose; compared between the compiled and candidate sides. */
    JsonObject after() {
        JsonObject out = new JsonObject();
        out.addProperty("ri1", this.renderInfo.ri1);
        out.addProperty("shielding", this.shielding);
        if (this.fanSpinDeclared) {
            // Only when declared: the landed entity_state dumps stay byte-identical.
            out.addProperty("rf1", this.renderInfo.rf1);
        }
        return out;
    }
}
