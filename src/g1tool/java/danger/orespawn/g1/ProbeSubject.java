package danger.orespawn.g1;

import com.google.gson.JsonElement;
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
import danger.orespawn.entity.pose.HumanoidPose;
import danger.orespawn.entity.pose.ThePrincessPose;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
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
 *
 * <p>THE GETTERS' VALUES: every int and boolean pose-interface getter answers its REST value (0 / false: the
 * defaults every landed dump was pinned against) unless the state JSON's {@code getters} object declares
 * another - {@code {"getters": {"getActivity": 1, "isInSittingPose": true}}} - the reference-clip sampler's way of
 * raising one value alone; the Slice 4b preset fields ({@code attacking}, {@code rock_type}, {@code ri1},
 * {@code rf1}, {@code seed}) keep their meaning and are read first. Every getter records that it was read
 * ({@link #readGetters()}; {@link #attackingRead()} kept for the attacking cross-check), so the sampler can report which
 * reads a hook reaches at the sampled inputs.</p>
 */
final class ProbeSubject implements Robot2Pose, Robot3Pose, Robot4Pose, RockBasePose, RotatorPose, PurplePowerPose,
        BeePose, CaterKillerPose, HerculesBeetlePose,
        EmperorScorpionPose, GhostSkellyPose, HydroliscPose, LeafMonsterPose, LeonPose, LizardPose, LurkingTerrorPose, MantisPose, AlienPose, AlosaurusPose, BasiliskPose, CamarasaurusPose, CephadromePose, ChipmunkPose, CrabPose, DragonPose, DungeonBeastPose, EnderKnightPose, EnderReaperPose, FrogPose, GazellePose, CaveFisherPose, GiantRobotPose, GodzillaPose, HammerheadPose, KrakenPose, NastysaurusPose, PeacockPose, PitchBlackPose, PointysaurusPose, SeaMonsterPose, SeaViperPose, TRexPose, TheKingPose, ThePrincePose, ThePrinceAdultPose, ThePrinceTeenPose, UrchinPose, WaterDragonPose, MolenoidPose, RatPose, ScorpionPose, SpitBugPose, SpyroPose, StinkyPose, TriffidPose, TrooperBugPose, VelocityRaptorPose, OstrichPose, HumanoidPose, ThePrincessPose {
    /** The state JSON's object of getter name -> value (an int or a boolean), each getter's declared value. */
    static final String GETTERS_KEY = "getters";
    private final RenderInfo renderInfo = new RenderInfo();
    private final int attacking;
    private final int rockType;
    private final RandomSource random;
    /** Slice 4c: an {@code rf1} preset was declared, so {@link #after()} reports the advanced angle. */
    private final boolean fanSpinDeclared;
    private int shielding = -1;
    /** The declared getter values (item 32 (2)); a getter absent here answers its rest value. */
    private final Map<String, JsonElement> values = new TreeMap<>();
    /** Every getter a pose call read on this subject, by name (the sampler's cross-check). */
    private final TreeSet<String> readGetters = new TreeSet<>();
    /**
     * The reference-clip sampler's cross-check: whether any pose call read {@link #getAttacking()} on this
     * subject - every pose interface's attacking getter lands here, so the sampler can report which hooks actually
     * read the flag at the sampled inputs beside the pose interfaces they declare. The attacking VALUE itself is
     * declared through the state ({@code attacking}, the Slice 4b presets' field, or the {@code getters} object's
     * {@code getAttacking}): the sampler's attack state declares 1, its walk and idle states 0.
     */
    private boolean attackingRead;

    ProbeSubject(JsonObject state) {
        if (state.has(GETTERS_KEY) && state.get(GETTERS_KEY).isJsonObject()) {
            state.getAsJsonObject(GETTERS_KEY).entrySet().forEach(entry -> this.values.put(entry.getKey(), entry.getValue()));
        }
        this.attacking = state.has("attacking") ? state.get("attacking").getAsInt() : intValue("getAttacking", 0);
        this.rockType = state.has("rock_type") ? state.get("rock_type").getAsInt() : intValue("getRockType", 0);
        this.renderInfo.ri1 = state.has("ri1") ? state.get("ri1").getAsInt() : 0;
        this.fanSpinDeclared = state.has("rf1");
        if (this.fanSpinDeclared) {
            this.renderInfo.rf1 = state.get("rf1").getAsFloat();
        }
        this.random = RandomSource.create(state.has("seed") ? state.get("seed").getAsLong() : 0L);
    }

    /** The declared value of an int getter, or its rest value. */
    private int intValue(String getter, int rest) {
        JsonElement value = this.values.get(getter);
        if (value == null) {
            return rest;
        }
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean() ? 1 : 0;
        }
        return value.getAsInt();
    }

    /** The declared value of a boolean getter, or its rest value. */
    private boolean booleanValue(String getter, boolean rest) {
        JsonElement value = this.values.get(getter);
        if (value == null) {
            return rest;
        }
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            return value.getAsBoolean();
        }
        return value.getAsInt() != 0;
    }

    private void read(String getter) {
        this.readGetters.add(getter);
    }

    /** Every getter a pose call has read on this subject, by name (the sampler's per-state cross-check). */
    TreeSet<String> readGetters() {
        return new TreeSet<>(this.readGetters);
    }

    @Override
    public RenderInfo getRenderInfo() {
        read("getRenderInfo");
        return this.renderInfo;
    }

    @Override
    public int getAttacking() {
        read("getAttacking");
        this.attackingRead = true;
        return this.attacking;
    }

    /** Whether a pose call has read {@link #getAttacking()} on this subject (the sampler's cross-check; see the field). */
    boolean attackingRead() {
        return this.attackingRead;
    }

    @Override
    public RandomSource getRandom() {
        read("getRandom");
        return this.random;
    }

    /** ENT-S-146: the seeded source as the level's random (orig ModelPurplePower.java:57 {@code worldObj.rand}). */
    @Override
    public RandomSource getLevelRandom() {
        read("getLevelRandom");
        return this.random;
    }

    @Override
    public void setShielding(int shielding) {
        this.shielding = shielding;
    }

    @Override
    public int getRockType() {
        read("getRockType");
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
        read("isInSittingPose");
        return booleanValue("isInSittingPose", false);
    }

    /** HydroliscPose: the rest value (HydroliscModel.poseFrom: hf = entity.getHealth() / entity.getMaxHealth() (hf = 1 at rest: health equal to the max)). */
    @Override
    public float getHealth() {
        read("getHealth");
        return 20.0F;
    }

    /** HydroliscPose: the rest value (HydroliscModel.poseFrom: hf = entity.getHealth() / entity.getMaxHealth()). */
    @Override
    public float getMaxHealth() {
        read("getMaxHealth");
        return 20.0F;
    }

    /** LeonPose: the rest value (LeonModel.poseFrom: boolean flying = entity.getActivity() != 0; if (entity.getActivity() == 0) { standing } else { flying } (orig ModelLeon.java:729/852)). */
    @Override
    public int getActivity() {
        read("getActivity");
        return intValue("getActivity", 0);
    }

    /** LeonPose: the rest value (LeonModel.poseFrom (flying): fchest.y = entity.getBeingRidden() == 0 ? ... : -2.0f; if (entity.getBeingRidden() == 0) { head yaw from netHeadYaw } else { the rf1 accumulator }). */
    @Override
    public int getBeingRidden() {
        read("getBeingRidden");
        return intValue("getBeingRidden", 0);
    }

    /** LeonPose: the rest value (LeonModel.poseFrom (flying, ridden): netHeadYaw = (entity.getYRotO() - entity.getYRot()) * 8.0f (orig ModelLeon.java:1014 rotationYaw) - Entity.getYRot() already satisfies it). */
    @Override
    public float getYRot() {
        read("getYRot");
        return 0.0F;
    }

    /** LeonPose: the rest value (LeonModel.poseFrom (flying, ridden): netHeadYaw = (entity.getYRotO() - entity.getYRot()) * 8.0f (orig ModelLeon.java:1014 prevRotationYaw) - EntityLeon.getYRotO() is a new one-line delegate to Entity.yRotO). */
    @Override
    public float getYRotO() {
        read("getYRotO");
        return 0.0F;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70165_t (posX)). */
    @Override
    public double getX() {
        read("getX");
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70161_v (posZ)). */
    @Override
    public double getZ() {
        read("getZ");
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70169_q (prevPosX)). */
    @Override
    public double xOld() {
        read("xOld");
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:385 e.field_70166_s (prevPosZ)). */
    @Override
    public double zOld() {
        read("zOld");
        return 0.0D;
    }

    /** CephadromePose: the rest value (orig ModelCephadrome.java:470 e.field_70126_B (prevRotationYaw)). */
    @Override
    public float yRotO() {
        read("yRotO");
        return 0.0F;
    }

    /** EnderKnightPose: the rest value (orig ModelEnderKnight.java:332 if (e.isScreaming())). */
    @Override
    public boolean isScreaming() {
        read("isScreaming");
        return booleanValue("isScreaming", false);
    }

    /** FrogPose: the rest value (orig ModelFrog.java:102 c.getSinging() != 0). */
    @Override
    public int getSinging() {
        read("getSinging");
        return intValue("getSinging", 0);
    }

    /** FrogPose: the rest value (orig ModelFrog.java:104 c.field_70181_x (motionY) beyond +-0.1; the port's Entity.getDeltaMovement().y - rest Vec3.ZERO (the crouch); the fully qualified type / rest so the merged getter compiles without an import). */
    @Override
    public net.minecraft.world.phys.Vec3 getDeltaMovement() {
        read("getDeltaMovement");
        return net.minecraft.world.phys.Vec3.ZERO;
    }

    /** GazellePose: the rest value (orig ModelGazelle.java:297 if (!g.func_70906_o()) as the port's classic reads it: Entity.isCrouching()). */
    @Override
    public boolean isCrouching() {
        read("isCrouching");
        return booleanValue("isCrouching", false);
    }

    /** PeacockPose: the rest value (ModelPeacock.poseFrom: if (entity.getBlink() > 0) { the head feathers 0.401 / -0.174 / -0.698 and the tail fanned 1.047 / -+0.4, 0.8, 1.2 } else { folded }; Peacock :74). */
    @Override
    public int getBlink() {
        read("getBlink");
        return intValue("getBlink", 0);
    }

    /** PitchBlackPose: the rest value (ModelPitchBlack.poseFrom: float pscale = entity.getPitchBlackScale(); every rhythm / pscale, the claw amplitudes x pscale (orig :39 size tier)). */
    @Override
    public float getPitchBlackScale() {
        read("getPitchBlackScale");
        return 1.0F;
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: if (entity.isOrderedToSit()) newangle = 0.0f (the tail sway stilled) - TamableAnimal.isOrderedToSit() already satisfies it). */
    @Override
    public boolean isOrderedToSit() {
        read("isOrderedToSit");
        return booleanValue("isOrderedToSit", false);
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: d1 = entity.getHead1Ext(); Lneck.xRot = toRadians(d1) (degrees); ThePrince :174). */
    @Override
    public int getHead1Ext() {
        read("getHead1Ext");
        return intValue("getHead1Ext", 0);
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: d2 = entity.getHead2Ext(); neck.xRot = toRadians(d2); ThePrince :175). */
    @Override
    public int getHead2Ext() {
        read("getHead2Ext");
        return intValue("getHead2Ext", 0);
    }

    /** ThePrincePose: the rest value (ModelThePrince.poseFrom: d3 = entity.getHead3Ext(); Rneck.xRot = toRadians(d3); ThePrince :176). */
    @Override
    public int getHead3Ext() {
        read("getHead3Ext");
        return intValue("getHead3Ext", 0);
    }

    /** TriffidPose: the rest value (TriffidModel.poseFrom: newangle = entity.getOpenClosed() == 0 ? 0.122522116f : ... (orig ModelTriffid.java:1275)). */
    @Override
    public int getOpenClosed() {
        read("getOpenClosed");
        return intValue("getOpenClosed", 0);
    }

    /** OstrichPose: the rest value (OstrichModel.poseFrom: entity.getIsActivated() == 0 / != 0 / > 1 (orig ModelOstrich.java:349, :420-425)). */
    @Override
    public int getIsActivated() {
        read("getIsActivated");
        return intValue("getIsActivated", 0);
    }

    /** OstrichPose: the rest value (OstrichModel.poseFrom: if (entity.isVehicle()) (orig ModelOstrich.java:335)). */
    @Override
    public boolean isVehicle() {
        read("isVehicle");
        return booleanValue("isVehicle", false);
    }

    // ---- HumanoidPose (the remainder slice, 2026-09-15; drafted by T2d): the vanilla HumanoidModel.setupAnim reads of the
    // Boyfriend / Girlfriend hooks, at rest unless the state's getters object raises one; the two per-frame lerps take the
    // seam's partial tick over the declared previous-tick / current-tick values, vanilla's own forms
    // (LivingEntity.getAttackAnim :3057-3062, getSwimAmount :387).

    /** The declared value of a float getter (a JSON number), or its rest value. */
    private float floatValue(String getter, float rest) {
        JsonElement value = this.values.get(getter);
        return value == null ? rest : value.getAsFloat();
    }

    /** HumanoidPose: the rest value (HumanoidModel.setupAnim :137 entity.getFallFlyingTicks() > 4; a tameable never fall-flies). */
    @Override
    public int getFallFlyingTicks() {
        read("getFallFlyingTicks");
        return intValue("getFallFlyingTicks", 0);
    }

    /** HumanoidPose: the rest value (HumanoidModel.setupAnim :138 entity.isVisuallySwimming(); no mob takes the SWIMMING pose). */
    @Override
    public boolean isVisuallySwimming() {
        read("isVisuallySwimming");
        return booleanValue("isVisuallySwimming", false);
    }

    /** HumanoidPose: the rest value (LivingEntityRenderer.render offsets 52-89: the model's riding; a boat or minecart seats the rider). */
    @Override
    public boolean isSeatedOnVehicle() {
        read("isSeatedOnVehicle");
        return booleanValue("isSeatedOnVehicle", false);
    }

    /** HumanoidPose: the rest value (HumanoidModel.setupAnim :192 entity.getMainArm() == HumanoidArm.RIGHT; vanilla's default main arm). */
    @Override
    public net.minecraft.world.entity.HumanoidArm getMainArm() {
        read("getMainArm");
        return net.minecraft.world.entity.HumanoidArm.RIGHT;
    }

    /** HumanoidPose: the rest value (HumanoidModel.setupAnim :193, :248 entity.isUsingItem()). */
    @Override
    public boolean isUsingItem() {
        read("isUsingItem");
        return booleanValue("isUsingItem", false);
    }

    /** HumanoidPose: the rest value (HumanoidModel.setupAnim :194 entity.getUsedItemHand() == InteractionHand.MAIN_HAND). */
    @Override
    public net.minecraft.world.InteractionHand getUsedItemHand() {
        read("getUsedItemHand");
        return net.minecraft.world.InteractionHand.MAIN_HAND;
    }

    /** HumanoidPose: the rest value (HumanoidModel.getAttackArm :466-467: entity.swingingArm == InteractionHand.MAIN_HAND; the field's default). */
    @Override
    public net.minecraft.world.InteractionHand getSwingingArm() {
        read("getSwingingArm");
        return net.minecraft.world.InteractionHand.MAIN_HAND;
    }

    /**
     * HumanoidPose: vanilla LivingEntity.getAttackAnim(partialTick) (21.1.223 :3057-3062: {@code f = attackAnim - oAttackAnim;
     * if (f < 0) f++; return oAttackAnim + f * partialTick}) over the declared {@code oAttackAnim} / {@code attackAnim} (rest 0
     * / 0: no swing), so a state that declares them proves the hook reads the seam's partial tick verbatim.
     */
    @Override
    public float getAttackAnim(float partialTick) {
        read("getAttackAnim");
        float attackAnim = floatValue("attackAnim", 0.0F);
        float oAttackAnim = floatValue("oAttackAnim", 0.0F);
        float f = attackAnim - oAttackAnim;
        if (f < 0.0F) {
            f++;
        }
        return oAttackAnim + f * partialTick;
    }

    /** HumanoidPose: vanilla LivingEntity.getSwimAmount(partialTick) (:387: Mth.lerp(partialTick, swimAmountO, swimAmount)) over the declared values (rest 0 / 0). */
    @Override
    public float getSwimAmount(float partialTick) {
        read("getSwimAmount");
        return net.minecraft.util.Mth.lerp(partialTick, floatValue("swimAmountO", 0.0F), floatValue("swimAmount", 0.0F));
    }

    /** HumanoidPose: the rest value (LivingEntityRenderer.render offsets 92-100: model.young = entity.isBaby(); a tameable never bred a baby). */
    @Override
    public boolean isBaby() {
        read("isBaby");
        return booleanValue("isBaby", false);
    }
}
