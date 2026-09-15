package danger.orespawn.entity.pose;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

/**
 * What vanilla {@code HumanoidModel.setupAnim} and the classic renderer that feeds it read from a humanoid entity, as an
 * interface the entity already satisfies (drafted by the fourth Tier-2 slice T2d, 2026-09-14, TEST-010 (b); landed by the
 * remainder slice, 2026-09-15, under the owner's closing set item 3 - "the seam carries the renderer's partial tick in
 * PoseInputs; the biped hooks read getAttackAnim(partialTick) verbatim"; the Slice 4b form of {@link CrabPose}).
 * {@code ModelBoyfriend} / {@code ModelGirlfriend} declare no {@code setupAnim}: the pose the classic
 * {@code HumanoidMobRenderer} draws is vanilla {@code net.minecraft.client.model.HumanoidModel.setupAnim} (NeoForge
 * 21.1.223, HumanoidModel.java:137-286 by the jar's LineNumberTable) over the six floats plus these entity reads, and over
 * the model fields {@code LivingEntityRenderer.render} sets per frame - {@code attackTime} (bytecode offsets 39-49:
 * {@code getAttackAnim(partialTick)}), {@code riding} (52-89), {@code young} (92-100; not read by {@code setupAnim}) - and
 * {@code HumanoidModel.prepareMobModel} sets ({@code swimAmount}, :129-131: {@code getSwimAmount(partialTick)}). The GeckoLib
 * hook reads the same values through this interface at the seam's partial tick ({@code PoseInputs.partialTick()}), the
 * classic model's {@code poseFrom} the same way, and the harness's {@code ProbeSubject} answers every one at its rest value
 * unless a declared state raises it.
 *
 * <p>THE TWO PER-FRAME LERPS - {@link #getAttackAnim(float)} and {@link #getSwimAmount(float)} - are declared here as the
 * classic renderer calls them, {@code LivingEntity}'s own public methods (getAttackAnim: {@code f = attackAnim -
 * oAttackAnim; if (f < 0) f++; return oAttackAnim + f * partialTick}, LivingEntity.java:3057-3062; getSwimAmount:
 * {@code Mth.lerp(partialTick, swimAmountO, swimAmount)}, :387), which the entity satisfies without a delegate.</p>
 */
public interface HumanoidPose {
    /** HumanoidModel.setupAnim :137 {@code entity.getFallFlyingTicks() > 4} (the elytra glide; rest 0 - a tameable never fall-flies). */
    int getFallFlyingTicks();

    /** HumanoidModel.setupAnim :138 {@code entity.isVisuallySwimming()} (rest false: no mob takes the SWIMMING pose). */
    boolean isVisuallySwimming();

    /** HumanoidModel.setupAnim :159 {@code entity.getDeltaMovement().lengthSqr()} (the fall-flying speed factor; rest zero). */
    Vec3 getDeltaMovement();

    /**
     * LivingEntityRenderer.render offsets 52-89: {@code entity.isPassenger() && entity.getVehicle() != null &&
     * entity.getVehicle().shouldRiderSit()} - the model's {@code riding} (the seated pose in a boat or minecart; rest false).
     * A one-line delegate on the entity (the hook's read has no single accessor).
     */
    boolean isSeatedOnVehicle();

    /** HumanoidModel.setupAnim :192 {@code entity.getMainArm() == HumanoidArm.RIGHT} (rest RIGHT). */
    HumanoidArm getMainArm();

    /** HumanoidModel.setupAnim :193 and :248 {@code entity.isUsingItem()} (rest false). */
    boolean isUsingItem();

    /** HumanoidModel.setupAnim :194 {@code entity.getUsedItemHand() == InteractionHand.MAIN_HAND}. */
    InteractionHand getUsedItemHand();

    /**
     * HumanoidModel.getAttackArm (:466-467): {@code entity.swingingArm == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite()} -
     * the public field {@code LivingEntity.swingingArm} through a one-line delegate on the entity (rest MAIN_HAND).
     */
    InteractionHand getSwingingArm();

    /** LivingEntityRenderer.render offsets 39-49: {@code getAttackAnim(entity, partialTick)} = {@code entity.getAttackAnim(partialTick)} - the swing (rest 0). */
    float getAttackAnim(float partialTick);

    /** HumanoidModel.prepareMobModel :129-131: {@code entity.getSwimAmount(partialTick)} (rest 0). */
    float getSwimAmount(float partialTick);

    /**
     * LivingEntityRenderer.render offsets 92-100: {@code model.young = entity.isBaby()} - read by {@code AgeableListModel.renderToBuffer}
     * (the baby head and body scaling), not by {@code setupAnim}; {@code EntityModel.young} defaults to TRUE, so the classic side sets
     * it before every capture as the renderer does (rest false: a tameable whose {@code getBreedOffspring} returns null is never bred a
     * baby; only a summoned negative age reaches the baby form, which the seam does not carry - the register's draft line).
     */
    boolean isBaby();
}
