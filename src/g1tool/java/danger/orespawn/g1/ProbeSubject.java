package danger.orespawn.g1;

import com.google.gson.JsonObject;
import danger.orespawn.entity.client.RenderInfo;
import danger.orespawn.entity.pose.EmperorScorpionPose;
import danger.orespawn.entity.pose.GhostSkellyPose;
import danger.orespawn.entity.pose.HydroliscPose;
import danger.orespawn.entity.pose.LeafMonsterPose;
import danger.orespawn.entity.pose.LeonPose;
import danger.orespawn.entity.pose.LizardPose;
import danger.orespawn.entity.pose.LurkingTerrorPose;
import danger.orespawn.entity.pose.MantisPose;
import danger.orespawn.entity.pose.AlienPose;
import danger.orespawn.entity.pose.AlosaurusPose;
import danger.orespawn.entity.pose.BasiliskPose;
import danger.orespawn.entity.pose.CamarasaurusPose;
import danger.orespawn.entity.pose.CephadromePose;
import danger.orespawn.entity.pose.ChipmunkPose;
import danger.orespawn.entity.pose.CrabPose;
import danger.orespawn.entity.pose.DragonPose;
import danger.orespawn.entity.pose.DungeonBeastPose;
import danger.orespawn.entity.pose.EnderKnightPose;
import danger.orespawn.entity.pose.EnderReaperPose;
import danger.orespawn.entity.pose.FrogPose;
import danger.orespawn.entity.pose.GazellePose;
import danger.orespawn.entity.pose.CaveFisherPose;
import danger.orespawn.entity.pose.GiantRobotPose;
import danger.orespawn.entity.pose.GodzillaPose;
import danger.orespawn.entity.pose.HammerheadPose;
import danger.orespawn.entity.pose.KrakenPose;
import danger.orespawn.entity.pose.NastysaurusPose;
import danger.orespawn.entity.pose.PeacockPose;
import danger.orespawn.entity.pose.PitchBlackPose;
import danger.orespawn.entity.pose.PointysaurusPose;
import danger.orespawn.entity.pose.SeaMonsterPose;
import danger.orespawn.entity.pose.SeaViperPose;
import danger.orespawn.entity.pose.TRexPose;
import danger.orespawn.entity.pose.TheKingPose;
import danger.orespawn.entity.pose.ThePrincePose;
import danger.orespawn.entity.pose.ThePrinceAdultPose;
import danger.orespawn.entity.pose.ThePrinceTeenPose;
import danger.orespawn.entity.pose.UrchinPose;
import danger.orespawn.entity.pose.WaterDragonPose;
import danger.orespawn.entity.pose.MolenoidPose;
import danger.orespawn.entity.pose.RatPose;
import danger.orespawn.entity.pose.ScorpionPose;
import danger.orespawn.entity.pose.SpitBugPose;
import danger.orespawn.entity.pose.SpyroPose;
import danger.orespawn.entity.pose.StinkyPose;
import danger.orespawn.entity.pose.TriffidPose;
import danger.orespawn.entity.pose.TrooperBugPose;
import danger.orespawn.entity.pose.VelocityRaptorPose;
import danger.orespawn.entity.pose.OstrichPose;
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
        BeePose, CaterKillerPose, HerculesBeetlePose,
        EmperorScorpionPose, GhostSkellyPose, HydroliscPose, LeafMonsterPose, LeonPose, LizardPose, LurkingTerrorPose, MantisPose, AlienPose, AlosaurusPose, BasiliskPose, CamarasaurusPose, CephadromePose, ChipmunkPose, CrabPose, DragonPose, DungeonBeastPose, EnderKnightPose, EnderReaperPose, FrogPose, GazellePose, CaveFisherPose, GiantRobotPose, GodzillaPose, HammerheadPose, KrakenPose, NastysaurusPose, PeacockPose, PitchBlackPose, PointysaurusPose, SeaMonsterPose, SeaViperPose, TRexPose, TheKingPose, ThePrincePose, ThePrinceAdultPose, ThePrinceTeenPose, UrchinPose, WaterDragonPose, MolenoidPose, RatPose, ScorpionPose, SpitBugPose, SpyroPose, StinkyPose, TriffidPose, TrooperBugPose, VelocityRaptorPose, OstrichPose {
    private final RenderInfo renderInfo = new RenderInfo();
    private final int attacking;
    private final int rockType;
    private final RandomSource random;
    /** Slice 4c: an {@code rf1} preset was declared, so {@link #after()} reports the advanced angle. */
    private final boolean fanSpinDeclared;
    private int shielding = -1;
    /**
     * The reference-clip sampler's cross-check (owner 2026-09-14, addendum item 11): whether any pose call read
     * {@link #getAttacking()} on this subject - every pose interface's attacking getter lands here, so the sampler can
     * report which hooks actually read the flag at the sampled inputs beside the pose interfaces they declare. The
     * attacking VALUE itself is declared through the state ({@code attacking}, the Slice 4b presets' field): the
     * sampler's attack state declares 1, its walk and idle states 0.
     */
    private boolean attackingRead;

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
        this.attackingRead = true;
        return this.attacking;
    }

    /** Whether a pose call has read {@link #getAttacking()} on this subject (the sampler's cross-check; see the field). */
    boolean attackingRead() {
        return this.attackingRead;
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

    /** HydroliscPose: the rest value (HydroliscModel.poseFrom: if (entity.isInSittingPose()) newangle = 0.0f (the tail sway stilled)). */
    @Override
    public boolean isInSittingPose() {
        return false;
    }

    /** HydroliscPose: the rest value (HydroliscModel.poseFrom: hf = entity.getHealth() / entity.getMaxHealth() (hf = 1 at rest: health equal to the max)). */
    @Override
    public float getHealth() {
        return 20.0F;
    }

    /** HydroliscPose: the rest value (HydroliscModel.poseFrom: hf = entity.getHealth() / entity.getMaxHealth()). */
    @Override
    public float getMaxHealth() {
        return 20.0F;
    }

    /** LeonPose: the rest value (LeonModel.poseFrom: boolean flying = entity.getActivity() != 0; if (entity.getActivity() == 0) { standing } else { flying } (orig ModelLeon.java:729/852)). */
    @Override
    public int getActivity() {
        return 0;
    }

    /** LeonPose: the rest value (LeonModel.poseFrom (flying): fchest.y = entity.getBeingRidden() == 0 ? ... : -2.0f; if (entity.getBeingRidden() == 0) { head yaw from netHeadYaw } else { the rf1 accumulator }). */
    @Override
    public int getBeingRidden() {
        return 0;
    }

    /** LeonPose: the rest value (LeonModel.poseFrom (flying, ridden): netHeadYaw = (entity.getYRotO() - entity.getYRot()) * 8.0f (orig ModelLeon.java:1014 rotationYaw) - Entity.getYRot() already satisfies it). */
    @Override
    public float getYRot() {
        return 0.0F;
    }

    /** LeonPose: the rest value (LeonModel.poseFrom (flying, ridden): netHeadYaw = (entity.getYRotO() - entity.getYRot()) * 8.0f (orig ModelLeon.java:1014 prevRotationYaw) - EntityLeon.getYRotO() is a new one-line delegate to Entity.yRotO). */
    @Override
    public float getYRotO() {
        return 0.0F;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70165_t (posX)). */
    @Override
    public double getX() {
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70161_v (posZ)). */
    @Override
    public double getZ() {
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70169_q (prevPosX)). */
    @Override
    public double xOld() {
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70166_s (prevPosZ)). */
    @Override
    public double zOld() {
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:470 e.field_70126_B (prevRotationYaw)). */
    @Override
    public float yRotO() {
        return 0.0F;
    }

    /** EnderKnightPose: the rest value (orig ModelEnderKnight.java:332 if (e.isScreaming())). */
    @Override
    public boolean isScreaming() {
        return false;
    }

    /** FrogPose: the rest value (orig ModelFrog.java:102 c.getSinging() != 0). */
    @Override
    public int getSinging() {
        return 0;
    }

    /** FrogPose: the rest value (orig ModelFrog.java:104 c.field_70181_x (motionY) beyond +-0.1; the port's Entity.getDeltaMovement().y - rest Vec3.ZERO (the crouch); the fully qualified type / rest so the merged getter compiles without an import). */
    @Override
    public net.minecraft.world.phys.Vec3 getDeltaMovement() {
        return net.minecraft.world.phys.Vec3.ZERO;
    }

    /** GazellePose: the rest value (orig ModelGazelle.java:297 if (!g.func_70906_o()) as the port's classic reads it: Entity.isCrouching()). */
    @Override
    public boolean isCrouching() {
        return false;
    }

    /** PeacockPose: the rest value (ModelPeacock.poseFrom: if (entity.getBlink() > 0) { the head feathers 0.401 / -0.174 / -0.698 and the tail fanned 1.047 / -+0.4, 0.8, 1.2 } else { folded }; Peacock :74). */
    @Override
    public int getBlink() {
        return 0;
    }

    /** PitchBlackPose: the rest value (ModelPitchBlack.poseFrom: float pscale = entity.getPitchBlackScale(); every rhythm / pscale, the claw amplitudes x pscale (orig :39 size tier)). */
    @Override
    public float getPitchBlackScale() {
        return 1.0F;
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: if (entity.isOrderedToSit()) newangle = 0.0f (the tail sway stilled) - TamableAnimal.isOrderedToSit() already satisfies it). */
    @Override
    public boolean isOrderedToSit() {
        return false;
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: d1 = entity.getHead1Ext(); Lneck.xRot = toRadians(d1) (degrees); ThePrince :174). */
    @Override
    public int getHead1Ext() {
        return 0;
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: d2 = entity.getHead2Ext(); neck.xRot = toRadians(d2); ThePrince :175). */
    @Override
    public int getHead2Ext() {
        return 0;
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: d3 = entity.getHead3Ext(); Rneck.xRot = toRadians(d3); ThePrince :176). */
    @Override
    public int getHead3Ext() {
        return 0;
    }

    /** TriffidPose: the rest value (TriffidModel.poseFrom: newangle = entity.getOpenClosed() == 0 ? 0.122522116f : ... (orig ModelTriffid.java:1275)). */
    @Override
    public int getOpenClosed() {
        return 0;
    }

    /** OstrichPose: the rest value (OstrichModel.poseFrom: entity.getIsActivated() == 0 / != 0 / > 1 (orig ModelOstrich.java:349, :420-425)). */
    @Override
    public int getIsActivated() {
        return 0;
    }

    /** OstrichPose: the rest value (OstrichModel.poseFrom: if (entity.isVehicle()) (orig ModelOstrich.java:335)). */
    @Override
    public boolean isVehicle() {
        return false;
    }
}
