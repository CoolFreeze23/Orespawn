package danger.orespawn.g1;

import com.google.gson.JsonObject;
import danger.orespawn.entity.client.RenderInfo;
import danger.orespawn.entity.pose.Robot2Pose;
import danger.orespawn.entity.pose.Robot3Pose;
import danger.orespawn.entity.pose.Robot4Pose;
import danger.orespawn.entity.pose.RockBasePose;
import net.minecraft.util.RandomSource;

/**
 * A declared entity state standing in for the live entity on both sides of
 * the comparison: the compiled model's {@code poseFrom} and the production
 * hook each get a FRESH subject built from the same manifest entry, so a
 * latch or RNG re-roll evolves identically. {@link #after()} records what the
 * pose wrote back (RenderInfo latch, Robot4 shielding) for the parity tool.
 */
final class ProbeSubject implements Robot2Pose, Robot3Pose, Robot4Pose, RockBasePose {
    private final RenderInfo renderInfo = new RenderInfo();
    private final int attacking;
    private final int rockType;
    private final RandomSource random;
    private int shielding = -1;

    ProbeSubject(JsonObject state) {
        this.attacking = state.has("attacking") ? state.get("attacking").getAsInt() : 0;
        this.rockType = state.has("rock_type") ? state.get("rock_type").getAsInt() : 0;
        this.renderInfo.ri1 = state.has("ri1") ? state.get("ri1").getAsInt() : 0;
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
        return out;
    }
}
