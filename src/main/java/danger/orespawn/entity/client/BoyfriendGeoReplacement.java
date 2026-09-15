package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Boyfriend;
import danger.orespawn.entity.pose.HumanoidPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * GeckoLib Boyfriend (drafted by the fourth Tier-2 slice T2d, TEST-010 (b); landed by the remainder slice, under -
 * "the seam carries the renderer's partial tick in PoseInputs; the biped hooks read getAttackAnim(partialTick) verbatim;
 * the classic item, armor, head and elytra layers are drawn by the seam's renderer for those species as the classic
 * renderer draws them"): the classic pose verbatim on the converted rig, ON THE HOOK (no keyframe layer, no transcription
 * - the self-gate stays closed until an artist delivers {@code idle} and {@code walk}). {@code ModelBoyfriend} declares no
 * {@code setupAnim}: the pose its classic renderer draws is vanilla {@code
 * net.minecraft.client.model.HumanoidModel.setupAnim} (NeoForge 21.1.223, HumanoidModel.java:137-286 by the jar's
 * LineNumberTable - the repository's sources jar is a stub, so the body was read from the bytecode with javap, every float
 * literal as the constant pool holds it) over the six floats, the entity reads {@link HumanoidPose} carries, and the model
 * fields the classic renderer sets per frame: {@code attackTime = entity.getAttackAnim(partialTick)}
 * (LivingEntityRenderer.render bytecode offsets 39-49), {@code riding} (52-89), {@code young} (92-100; read by
 * AgeableListModel.renderToBuffer, not by setupAnim - the baby head and body scaling this entity never takes by breeding,
 * getBreedOffspring returns null; a summoned negative age would, and the seam does not carry that renderToBuffer form - the
 * register's draft line),
 * {@code swimAmount = entity.getSwimAmount(partialTick)} (HumanoidModel.prepareMobModel :129-131); {@code crouching} and the
 * two arm poses are never written for a {@code HumanoidMobRenderer} (21.1.223's class is its two constructors and their
 * layers) - false and EMPTY. The whole body is transcribed in {@link #poseRig}: the head look, the fall-flying head and
 * speed factor, the distance-phased walk (the legs 1.4 rad and the arms 2 x 0.5 rad times the walking speed, the legs'
 * 0.005 yaw / roll splay), the riding pose, the EMPTY arm poses, the attack swing (:382-408), the crouch branch (never
 * taken), the arms' idle bob (AnimationUtils.bobModelPart :59-60) and the swim branch (:243-282, unreachable for a mob that
 * never takes the SWIMMING pose), the hat copying the head (:285). The two per-frame lerps read the seam's partial tick
 * ({@code PoseInputs.partialTick()}) exactly as the classic renderer hands the frame's to {@code getAttackAnim} /
 * {@code getSwimAmount}. The Girlfriend's hook delegates here (the Butterfly / Leon form). The classic side of the animation
 * leg is {@link HumanoidClassicPose}, the same bytecode transcribed over the classic model's parts.
 *
 * <p>THE CLASSIC LAYERS: {@link Renderer} attaches the vanilla layer classes {@code BoyfriendRenderer} carries - the
 * {@code CustomHeadLayer}, {@code ElytraLayer} and {@code ItemInHandLayer} a {@code HumanoidMobRenderer} adds in its
 * constructor (21.1.223 bytecode 7-67, in that order) and the {@code HumanoidArmorLayer} BoyfriendRenderer adds after them
 * (BoyfriendRenderer.java:30-33) - through the seam's {@link OreSpawnGeoReplacedEntityRenderer.VanillaLayersAdapter}, which
 * hands them a classic {@code ModelBoyfriend} posed from the bake's bones in the classic frame.</p>
 *
 * <p>Scale and shadow follow {@link BoyfriendRenderer}: 1.0 render scale (identity - no scale override, so no scale
 * hook) and a 0.55 shadow (ENT-S-092); the texture per skin, wetness and prince state through
 * {@link BoyfriendRenderer#textureFor} (the classic renderer's own switch, lifted into it as the Fairy's was).</p>
 */
public final class BoyfriendGeoReplacement extends OreSpawnGeoReplacement<Boyfriend> {
    private static final GeoReplacementDescriptor<Boyfriend> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.BOYFRIEND.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Boyfriend.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/boyfriend.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/boyfriend.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/boyfriend0.png"),
            BoyfriendRenderer.SHADOW) {
        /** BoyfriendRenderer.getTextureLocation: the numbered dry skin, the swimshorts skin while wet, the frog-prince skins. */
        @Override
        public ResourceLocation texture(Boyfriend entity) {
            return BoyfriendRenderer.textureFor(entity);
        }
    };

    public BoyfriendGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        poseRig(processor, inputs);
    }

    /**
     * Vanilla {@code HumanoidModel.setupAnim} (21.1.223, HumanoidModel.java:137-286) over the seven bones {@code head},
     * {@code hat}, {@code body}, {@code right_arm}, {@code left_arm}, {@code right_leg}, {@code left_leg}: every classic
     * field write mirrored in a local (the fields are all written before they are read within one frame - the swim branch's
     * {@code this.head.xRot} at :144 / :146 excepted, a previous-frame read taken from the bone), the classic's own float
     * chains, the writes landing once per bone and axis at the end. The renderer-set model fields come first, from the
     * subject at the seam's partial tick, as {@code LivingEntityRenderer.render} and {@code HumanoidModel.prepareMobModel} set them.
     */
    static void poseRig(AnimationProcessor<?> processor, PoseInputs inputs) {
        HumanoidPose entity = inputs.subject(HumanoidPose.class);
        float limbSwing = inputs.limbSwing();
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // LivingEntityRenderer.render offsets 39-49: model.attackTime = getAttackAnim(entity, partialTick); 52-89: model.riding;
        // HumanoidModel.prepareMobModel :129-131: swimAmount = entity.getSwimAmount(partialTick) - at the frame's partial tick.
        float attackTime = entity.getAttackAnim(inputs.partialTick());
        boolean riding = entity.isSeatedOnVehicle();
        float swimAmount = entity.getSwimAmount(inputs.partialTick());
        // crouching / rightArmPose / leftArmPose: never written for a HumanoidMobRenderer - the fields' defaults (false, EMPTY).
        final boolean crouching = false;
        // HumanoidModel.setupAnim :137-138
        boolean flag = entity.getFallFlyingTicks() > 4;
        boolean flag1 = entity.isVisuallySwimming();
        // :139 the head yaw; :140-149 the head pitch (the elytra dive, the swim lerp against the previous frame, the look)
        float headYRot = netHeadYaw * 0.017453292F;
        float headXRot;
        if (flag) {
            headXRot = -0.7853982F;
        } else if (swimAmount > 0.0F) {
            float headXRotBefore = classicRotX(bone(processor, "head"));  // :144 / :146 read the field before this frame's write
            if (flag1) {
                headXRot = rotlerpRad(swimAmount, headXRotBefore, -0.7853982F);
            } else {
                headXRot = rotlerpRad(swimAmount, headXRotBefore, headPitch * 0.017453292F);
            }
        } else {
            headXRot = headPitch * 0.017453292F;
        }
        // :152-156
        float bodyYRot = 0.0F;
        float rightArmZ = 0.0F;
        float rightArmX = -5.0F;
        float leftArmZ = 0.0F;
        float leftArmX = 5.0F;
        // :157-165 the fall-flying speed factor
        float f = 1.0F;
        if (flag) {
            f = (float) entity.getDeltaMovement().lengthSqr();
            f /= 0.2F;
            f *= f * f;
        }
        if (f < 1.0F) {
            f = 1.0F;
        }
        // :168-177 the walk: the arms against the legs, phased by the distance walked; the legs' 0.005 splay
        float rightArmXRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F / f;
        float leftArmXRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        float rightArmZRot = 0.0F;
        float leftArmZRot = 0.0F;
        float rightLegXRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        float leftLegXRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount / f;
        float rightLegYRot = 0.005F;
        float leftLegYRot = -0.005F;
        float rightLegZRot = 0.005F;
        float leftLegZRot = -0.005F;
        // :178-186 the riding pose
        if (riding) {
            rightArmXRot += -0.62831855F;
            leftArmXRot += -0.62831855F;
            rightLegXRot = -1.4137167F;
            rightLegYRot = 0.31415927F;
            rightLegZRot = 0.07853982F;
            leftLegXRot = -1.4137167F;
            leftLegYRot = -0.31415927F;
            leftLegZRot = -0.07853982F;
        }
        // :189-190
        float rightArmYRot = 0.0F;
        float leftArmYRot = 0.0F;
        // :191-206 the arm poses - EMPTY for a HumanoidMobRenderer: poseRightArm (:289-291) and poseLeftArm (:333-335) each
        // set their arm's yaw to 0 in whichever order the main arm and the used hand select (isTwoHanded() of EMPTY is false)
        boolean flag2 = entity.getMainArm() == HumanoidArm.RIGHT;
        if (entity.isUsingItem()) {
            boolean flag3 = entity.getUsedItemHand() == InteractionHand.MAIN_HAND;
            if (flag3 == flag2) {
                rightArmYRot = 0.0F;
            } else {
                leftArmYRot = 0.0F;
            }
        } else {
            boolean flag4 = flag2 ? false : false;  // leftArmPose.isTwoHanded() : rightArmPose.isTwoHanded(), both EMPTY
            if (flag2 != flag4) {
                leftArmYRot = 0.0F;
                rightArmYRot = 0.0F;
            } else {
                rightArmYRot = 0.0F;
                leftArmYRot = 0.0F;
            }
        }
        // :210 setupAttackAnimation (:382-408): the swing on the attack arm (getAttackArm :466-467)
        if (!(attackTime <= 0.0F)) {
            HumanoidArm humanoidarm = attackArm(entity);
            float f0 = attackTime;
            bodyYRot = Mth.sin(Mth.sqrt(f0) * 6.2831855F) * 0.2F;
            if (humanoidarm == HumanoidArm.LEFT) {
                bodyYRot *= -1.0F;
            }
            rightArmZ = Mth.sin(bodyYRot) * 5.0F;
            rightArmX = -Mth.cos(bodyYRot) * 5.0F;
            leftArmZ = -Mth.sin(bodyYRot) * 5.0F;
            leftArmX = Mth.cos(bodyYRot) * 5.0F;
            rightArmYRot = rightArmYRot + bodyYRot;
            leftArmYRot = leftArmYRot + bodyYRot;
            leftArmXRot = leftArmXRot + bodyYRot;
            f0 = 1.0F - attackTime;
            f0 *= f0;
            f0 *= f0;
            f0 = 1.0F - f0;
            float f1 = Mth.sin(f0 * 3.1415927F);
            float f2 = Mth.sin(attackTime * 3.1415927F) * -(headXRot - 0.7F) * 0.75F;
            if (humanoidarm == HumanoidArm.RIGHT) {
                rightArmXRot -= f1 * 1.2F + f2;
                rightArmYRot = rightArmYRot + bodyYRot * 2.0F;
                rightArmZRot = rightArmZRot + Mth.sin(attackTime * 3.1415927F) * -0.4F;
            } else {
                leftArmXRot -= f1 * 1.2F + f2;
                leftArmYRot = leftArmYRot + bodyYRot * 2.0F;
                leftArmZRot = leftArmZRot + Mth.sin(attackTime * 3.1415927F) * -0.4F;
            }
        }
        // :211-232 the crouch: never for a HumanoidMobRenderer, so the standing branch (:224-232)
        float bodyXRot;
        float rightLegZ;
        float leftLegZ;
        float rightLegY;
        float leftLegY;
        float headY;
        float bodyY;
        float leftArmY;
        float rightArmY;
        if (crouching) {
            bodyXRot = 0.5F;
            rightArmXRot += 0.4F;
            leftArmXRot += 0.4F;
            rightLegZ = 4.0F;
            leftLegZ = 4.0F;
            rightLegY = 12.2F;
            leftLegY = 12.2F;
            headY = 4.2F;
            bodyY = 3.2F;
            leftArmY = 5.2F;
            rightArmY = 5.2F;
        } else {
            bodyXRot = 0.0F;
            rightLegZ = 0.0F;
            leftLegZ = 0.0F;
            rightLegY = 12.0F;
            leftLegY = 12.0F;
            headY = 0.0F;
            bodyY = 0.0F;
            leftArmY = 2.0F;
            rightArmY = 2.0F;
        }
        // :235-240 the arms' idle bob (AnimationUtils.bobModelPart :59-60), unless a SPYGLASS pose (never: EMPTY)
        rightArmZRot = rightArmZRot + 1.0F * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        rightArmXRot = rightArmXRot + 1.0F * Mth.sin(ageInTicks * 0.067F) * 0.05F;
        leftArmZRot = leftArmZRot + -1.0F * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        leftArmXRot = leftArmXRot + -1.0F * Mth.sin(ageInTicks * 0.067F) * 0.05F;
        // :243-282 the swim (the arms :249-275, the legs :281-282): unreachable for a mob that never takes the SWIMMING pose
        // (swimAmount stays 0), as the code has it
        if (swimAmount > 0.0F) {
            float f5 = limbSwing % 26.0F;
            HumanoidArm humanoidarm1 = attackArm(entity);
            float f6 = humanoidarm1 == HumanoidArm.RIGHT && attackTime > 0.0F ? 0.0F : swimAmount;
            float f7 = humanoidarm1 == HumanoidArm.LEFT && attackTime > 0.0F ? 0.0F : swimAmount;
            if (!entity.isUsingItem()) {
                if (f5 < 14.0F) {
                    leftArmXRot = rotlerpRad(f7, leftArmXRot, 0.0F);
                    rightArmXRot = Mth.lerp(f6, rightArmXRot, 0.0F);
                    leftArmYRot = rotlerpRad(f7, leftArmYRot, 3.1415927F);
                    rightArmYRot = Mth.lerp(f6, rightArmYRot, 3.1415927F);
                    leftArmZRot = rotlerpRad(f7, leftArmZRot, 3.1415927F + 1.8707964F * quadraticArmUpdate(f5) / quadraticArmUpdate(14.0F));
                    rightArmZRot = Mth.lerp(f6, rightArmZRot, 3.1415927F - 1.8707964F * quadraticArmUpdate(f5) / quadraticArmUpdate(14.0F));
                } else if (f5 >= 14.0F && f5 < 22.0F) {
                    float f10 = (f5 - 14.0F) / 8.0F;
                    leftArmXRot = rotlerpRad(f7, leftArmXRot, 1.5707964F * f10);
                    rightArmXRot = Mth.lerp(f6, rightArmXRot, 1.5707964F * f10);
                    leftArmYRot = rotlerpRad(f7, leftArmYRot, 3.1415927F);
                    rightArmYRot = Mth.lerp(f6, rightArmYRot, 3.1415927F);
                    leftArmZRot = rotlerpRad(f7, leftArmZRot, 5.012389F - 1.8707964F * f10);
                    rightArmZRot = Mth.lerp(f6, rightArmZRot, 1.2707963F + 1.8707964F * f10);
                } else if (f5 >= 22.0F && f5 < 26.0F) {
                    float f3 = (f5 - 22.0F) / 4.0F;
                    leftArmXRot = rotlerpRad(f7, leftArmXRot, 1.5707964F - 1.5707964F * f3);
                    rightArmXRot = Mth.lerp(f6, rightArmXRot, 1.5707964F - 1.5707964F * f3);
                    leftArmYRot = rotlerpRad(f7, leftArmYRot, 3.1415927F);
                    rightArmYRot = Mth.lerp(f6, rightArmYRot, 3.1415927F);
                    leftArmZRot = rotlerpRad(f7, leftArmZRot, 3.1415927F);
                    rightArmZRot = Mth.lerp(f6, rightArmZRot, 3.1415927F);
                }
            }
            leftLegXRot = Mth.lerp(swimAmount, leftLegXRot, 0.3F * Mth.cos(limbSwing * 0.33333334F + 3.1415927F));
            rightLegXRot = Mth.lerp(swimAmount, rightLegXRot, 0.3F * Mth.cos(limbSwing * 0.33333334F));
        }
        // The writes, once per bone and axis (the classic's final field values); :285 hat.copyFrom(head) - the hat takes the
        // head's rotation and position (the head's roll and x / z are never written: both stay at the bind, 0).
        rotateY(processor, "head", headYRot);
        rotateX(processor, "head", headXRot);
        rotateY(processor, "hat", headYRot);
        rotateX(processor, "hat", headXRot);
        rotateX(processor, "body", bodyXRot);
        rotateY(processor, "body", bodyYRot);
        rotateX(processor, "right_arm", rightArmXRot);
        rotateY(processor, "right_arm", rightArmYRot);
        rotateZ(processor, "right_arm", rightArmZRot);
        rotateX(processor, "left_arm", leftArmXRot);
        rotateY(processor, "left_arm", leftArmYRot);
        rotateZ(processor, "left_arm", leftArmZRot);
        rotateX(processor, "right_leg", rightLegXRot);
        rotateY(processor, "right_leg", rightLegYRot);
        rotateZ(processor, "right_leg", rightLegZRot);
        rotateX(processor, "left_leg", leftLegXRot);
        rotateY(processor, "left_leg", leftLegYRot);
        rotateZ(processor, "left_leg", leftLegZRot);
        float[] head = classicPosition(bone(processor, "head"));      // x / z never written: the bind
        float[] body = classicPosition(bone(processor, "body"));
        float[] rightLeg = classicPosition(bone(processor, "right_leg"));
        float[] leftLeg = classicPosition(bone(processor, "left_leg"));
        moveTo(processor, "head", head[0], headY, head[2]);
        moveTo(processor, "hat", head[0], headY, head[2]);
        moveTo(processor, "body", body[0], bodyY, body[2]);
        moveTo(processor, "right_arm", rightArmX, rightArmY, rightArmZ);
        moveTo(processor, "left_arm", leftArmX, leftArmY, leftArmZ);
        moveTo(processor, "right_leg", rightLeg[0], rightLegY, rightLegZ);
        moveTo(processor, "left_leg", leftLeg[0], leftLegY, leftLegZ);
    }

    /** HumanoidModel.getAttackArm (:466-467): the main arm while the main hand swings, else its opposite. */
    private static HumanoidArm attackArm(HumanoidPose entity) {
        HumanoidArm humanoidarm = entity.getMainArm();
        return entity.getSwingingArm() == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
    }

    /** HumanoidModel.rotlerpRad (:411-420): the shortest-way lerp of an angle toward a target, by a fraction. */
    private static float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % 6.2831855F;
        if (f < -3.1415927F) {
            f += 6.2831855F;
        }
        if (f >= 3.1415927F) {
            f -= 6.2831855F;
        }
        return maxAngle + angle * f;
    }

    /** HumanoidModel.quadraticArmUpdate (:424). */
    private static float quadraticArmUpdate(float limbSwing) {
        return -65.0F * limbSwing + limbSwing * limbSwing;
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Boyfriend, BoyfriendGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BoyfriendGeoReplacement());
            // THE CLASSIC LAYERS: the vanilla layer classes BoyfriendRenderer carries, in its order - HumanoidMobRenderer(Context, M,
            // float) adds the CustomHeadLayer, the ElytraLayer and the ItemInHandLayer (21.1.223 bytecode 7-67), then
            // BoyfriendRenderer adds the HumanoidArmorLayer over the player armor layers (:30-33) - attached to the seam's
            // renderer through the adapter that hands them a classic ModelBoyfriend posed from the bake's bones.
            VanillaLayersAdapter<Boyfriend, ModelBoyfriend, BoyfriendGeoReplacement> layers = new VanillaLayersAdapter<>(
                    this, new ModelBoyfriend(context.bakeLayer(BoyfriendRenderer.MODEL_LAYER)));
            layers.addLayer(new CustomHeadLayer<>(layers, context.getModelSet(), context.getItemInHandRenderer()));
            layers.addLayer(new ElytraLayer<>(layers, context.getModelSet()));
            layers.addLayer(new ItemInHandLayer<>(layers, context.getItemInHandRenderer()));
            layers.addLayer(new HumanoidArmorLayer<>(layers,
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                    context.getModelManager()));
            addRenderLayer(layers);
        }
    }
}
