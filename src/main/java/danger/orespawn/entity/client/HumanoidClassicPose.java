package danger.orespawn.entity.client;

import danger.orespawn.entity.pose.HumanoidPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

/**
 * THE CLASSIC SIDE OF THE BIPEDS (the remainder slice - TEST-010 (b)): vanilla {@code
 * net.minecraft.client.model.HumanoidModel.setupAnim} (NeoForge 21.1.223, HumanoidModel.java:137-286 by the jar's
 * LineNumberTable; the repository's sources jar is a stub, so the body was read from the bytecode with javap, every float
 * literal as the constant pool holds it) transcribed STATEMENT BY STATEMENT over a {@link HumanoidModel}'s own parts and
 * fields, the entity read through {@link HumanoidPose} - the {@code poseFrom} entry of {@code ModelBoyfriend} /
 * {@code ModelGirlfriend} (which declare no {@code setupAnim} of their own) for the parity probe, which drives the classic
 * model from a declared state without a live entity. IN-GAME THE CLASSIC PATH IS UNCHANGED: {@code HumanoidMobRenderer}
 * calls vanilla's own {@code setupAnim}, never this; the probe cannot run vanilla's method because it takes the entity
 * ({@code LivingEntity}'s class initialiser trips {@code Bootstrap.checkBootstrapCalled} through the game-event registry in
 * the un-bootstrapped JVM, measured 2026-09-15), so the classic side of the animation leg is this transcription and the
 * hook ({@code BoyfriendGeoReplacement.poseRig}) another of the same bytecode - the leg proves the two agree at 0 rad on
 * every sample; the transcriptions' fidelity to the bytecode is by the reading, cited line by line below.
 *
 * <p>{@link #prepare} sets the three model fields the classic renderer sets per frame before {@code setupAnim} -
 * {@code attackTime} ({@code LivingEntityRenderer.render} bytecode offsets 39-49: {@code getAttackAnim(entity, partialTick)}),
 * {@code riding} (52-89) and, through {@code HumanoidModel.prepareMobModel} (:129-131), {@code swimAmount =
 * entity.getSwimAmount(partialTick)} - from the pose interface at the FRAME's partial tick; {@code young} (92-100) is read
 * by {@code AgeableListModel.renderToBuffer}, not by {@code setupAnim}. {@code crouching} and the two arm poses are never
 * written for a {@code HumanoidMobRenderer} (21.1.223's class is its two constructors and their layers): the fields'
 * defaults, false and EMPTY, as the model was constructed.</p>
 */
final class HumanoidClassicPose {
    private HumanoidClassicPose() {
    }

    /**
     * The renderer-set fields (LivingEntityRenderer.render 39-49 / 52-89 / 92-100; HumanoidModel.prepareMobModel :129-131) at the
     * frame's partial tick - {@code young} included: {@code EntityModel.young} defaults to true, and {@code AgeableListModel
     * .renderToBuffer} draws the baby form (the head scaled and offset) while it stands, so the probe's capture sets it exactly as
     * the renderer does before every draw.
     */
    static void prepare(HumanoidModel<?> model, HumanoidPose entity, float partialTick) {
        model.attackTime = entity.getAttackAnim(partialTick);
        model.riding = entity.isSeatedOnVehicle();
        model.young = entity.isBaby();
        model.swimAmount = entity.getSwimAmount(partialTick);
    }

    /** HumanoidModel.setupAnim :137-286 over the model's parts, reading the fields {@link #prepare} set. */
    static void setupAnim(HumanoidModel<?> model, HumanoidPose entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean flag = entity.getFallFlyingTicks() > 4;                                                    // :137
        boolean flag1 = entity.isVisuallySwimming();                                                       // :138
        model.head.yRot = netHeadYaw * 0.017453292F;                                                       // :139
        if (flag) {                                                                                        // :140
            model.head.xRot = -0.7853982F;                                                                 // :141
        } else if (model.swimAmount > 0.0F) {                                                              // :142
            if (flag1) {                                                                                   // :143
                model.head.xRot = rotlerpRad(model.swimAmount, model.head.xRot, -0.7853982F);              // :144
            } else {
                model.head.xRot = rotlerpRad(model.swimAmount, model.head.xRot, headPitch * 0.017453292F); // :146
            }
        } else {
            model.head.xRot = headPitch * 0.017453292F;                                                    // :149
        }
        model.body.yRot = 0.0F;                                                                            // :152
        model.rightArm.z = 0.0F;                                                                           // :153
        model.rightArm.x = -5.0F;                                                                          // :154
        model.leftArm.z = 0.0F;                                                                            // :155
        model.leftArm.x = 5.0F;                                                                            // :156
        float f = 1.0F;                                                                                    // :157
        if (flag) {                                                                                        // :158
            f = (float) entity.getDeltaMovement().lengthSqr();                                             // :159
            f /= 0.2F;                                                                                     // :160
            f *= f * f;                                                                                    // :161
        }
        if (f < 1.0F) {                                                                                    // :163
            f = 1.0F;                                                                                      // :164
        }
        model.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F / f;  // :168
        model.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;                 // :169
        model.rightArm.zRot = 0.0F;                                                                        // :170
        model.leftArm.zRot = 0.0F;                                                                         // :171
        model.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;                   // :172
        model.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount / f;       // :173
        model.rightLeg.yRot = 0.005F;                                                                      // :174
        model.leftLeg.yRot = -0.005F;                                                                      // :175
        model.rightLeg.zRot = 0.005F;                                                                      // :176
        model.leftLeg.zRot = -0.005F;                                                                      // :177
        if (model.riding) {                                                                                // :178
            model.rightArm.xRot += -0.62831855F;                                                           // :179
            model.leftArm.xRot += -0.62831855F;                                                            // :180
            model.rightLeg.xRot = -1.4137167F;                                                             // :181
            model.rightLeg.yRot = 0.31415927F;                                                             // :182
            model.rightLeg.zRot = 0.07853982F;                                                             // :183
            model.leftLeg.xRot = -1.4137167F;                                                              // :184
            model.leftLeg.yRot = -0.31415927F;                                                             // :185
            model.leftLeg.zRot = -0.07853982F;                                                             // :186
        }
        model.rightArm.yRot = 0.0F;                                                                        // :189
        model.leftArm.yRot = 0.0F;                                                                         // :190
        boolean flag2 = entity.getMainArm() == HumanoidArm.RIGHT;                                          // :191
        if (entity.isUsingItem()) {                                                                        // :192
            boolean flag3 = entity.getUsedItemHand() == InteractionHand.MAIN_HAND;                         // :193
            if (flag3 == flag2) {                                                                          // :194
                poseRightArm(model);                                                                       // :195
            } else {
                poseLeftArm(model);                                                                        // :197
            }
        } else {
            boolean flag4 = flag2 ? model.leftArmPose.isTwoHanded() : model.rightArmPose.isTwoHanded();    // :200
            if (flag2 != flag4) {                                                                          // :201
                poseLeftArm(model);                                                                        // :202
                poseRightArm(model);                                                                       // :203
            } else {
                poseRightArm(model);                                                                       // :205
                poseLeftArm(model);                                                                        // :206
            }
        }
        setupAttackAnimation(model, entity, ageInTicks);                                                   // :210
        if (model.crouching) {                                                                             // :211
            model.body.xRot = 0.5F;                                                                        // :212
            model.rightArm.xRot += 0.4F;                                                                   // :213
            model.leftArm.xRot += 0.4F;                                                                    // :214
            model.rightLeg.z = 4.0F;                                                                       // :215
            model.leftLeg.z = 4.0F;                                                                        // :216
            model.rightLeg.y = 12.2F;                                                                      // :217
            model.leftLeg.y = 12.2F;                                                                       // :218
            model.head.y = 4.2F;                                                                           // :219
            model.body.y = 3.2F;                                                                           // :220
            model.leftArm.y = 5.2F;                                                                        // :221
            model.rightArm.y = 5.2F;                                                                       // :222
        } else {
            model.body.xRot = 0.0F;                                                                        // :224
            model.rightLeg.z = 0.0F;                                                                       // :225
            model.leftLeg.z = 0.0F;                                                                        // :226
            model.rightLeg.y = 12.0F;                                                                      // :227
            model.leftLeg.y = 12.0F;                                                                       // :228
            model.head.y = 0.0F;                                                                           // :229
            model.body.y = 0.0F;                                                                           // :230
            model.leftArm.y = 2.0F;                                                                        // :231
            model.rightArm.y = 2.0F;                                                                       // :232
        }
        if (model.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {                                        // :235
            bobModelPart(model, true, ageInTicks, 1.0F);                                                   // :236 AnimationUtils.bobModelPart(rightArm, ageInTicks, 1.0F)
        }
        if (model.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {                                         // :239
            bobModelPart(model, false, ageInTicks, -1.0F);                                                 // :240 AnimationUtils.bobModelPart(leftArm, ageInTicks, -1.0F)
        }
        if (model.swimAmount > 0.0F) {                                                                     // :243
            float f5 = limbSwing % 26.0F;                                                                  // :244
            HumanoidArm humanoidarm = getAttackArm(entity);                                                // :245
            float f1 = humanoidarm == HumanoidArm.RIGHT && model.attackTime > 0.0F ? 0.0F : model.swimAmount;  // :246
            float f2 = humanoidarm == HumanoidArm.LEFT && model.attackTime > 0.0F ? 0.0F : model.swimAmount;   // :247
            if (!entity.isUsingItem()) {                                                                   // :248
                if (f5 < 14.0F) {                                                                          // :249
                    model.leftArm.xRot = rotlerpRad(f2, model.leftArm.xRot, 0.0F);                         // :250
                    model.rightArm.xRot = Mth.lerp(f1, model.rightArm.xRot, 0.0F);                         // :251
                    model.leftArm.yRot = rotlerpRad(f2, model.leftArm.yRot, 3.1415927F);                   // :252
                    model.rightArm.yRot = Mth.lerp(f1, model.rightArm.yRot, 3.1415927F);                   // :253
                    model.leftArm.zRot = rotlerpRad(f2, model.leftArm.zRot, 3.1415927F + 1.8707964F * quadraticArmUpdate(f5) / quadraticArmUpdate(14.0F));   // :254
                    model.rightArm.zRot = Mth.lerp(f1, model.rightArm.zRot, 3.1415927F - 1.8707964F * quadraticArmUpdate(f5) / quadraticArmUpdate(14.0F));   // :257
                } else if (f5 >= 14.0F && f5 < 22.0F) {                                                    // :260
                    float f6 = (f5 - 14.0F) / 8.0F;                                                        // :261
                    model.leftArm.xRot = rotlerpRad(f2, model.leftArm.xRot, 1.5707964F * f6);              // :262
                    model.rightArm.xRot = Mth.lerp(f1, model.rightArm.xRot, 1.5707964F * f6);              // :263
                    model.leftArm.yRot = rotlerpRad(f2, model.leftArm.yRot, 3.1415927F);                   // :264
                    model.rightArm.yRot = Mth.lerp(f1, model.rightArm.yRot, 3.1415927F);                   // :265
                    model.leftArm.zRot = rotlerpRad(f2, model.leftArm.zRot, 5.012389F - 1.8707964F * f6);  // :266
                    model.rightArm.zRot = Mth.lerp(f1, model.rightArm.zRot, 1.2707963F + 1.8707964F * f6); // :267
                } else if (f5 >= 22.0F && f5 < 26.0F) {                                                    // :268
                    float f3 = (f5 - 22.0F) / 4.0F;                                                        // :269
                    model.leftArm.xRot = rotlerpRad(f2, model.leftArm.xRot, 1.5707964F - 1.5707964F * f3); // :270
                    model.rightArm.xRot = Mth.lerp(f1, model.rightArm.xRot, 1.5707964F - 1.5707964F * f3); // :271
                    model.leftArm.yRot = rotlerpRad(f2, model.leftArm.yRot, 3.1415927F);                   // :272
                    model.rightArm.yRot = Mth.lerp(f1, model.rightArm.yRot, 3.1415927F);                   // :273
                    model.leftArm.zRot = rotlerpRad(f2, model.leftArm.zRot, 3.1415927F);                   // :274
                    model.rightArm.zRot = Mth.lerp(f1, model.rightArm.zRot, 3.1415927F);                   // :275
                }
            }
            model.leftLeg.xRot = Mth.lerp(model.swimAmount, model.leftLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F + 3.1415927F));  // :281
            model.rightLeg.xRot = Mth.lerp(model.swimAmount, model.rightLeg.xRot, 0.3F * Mth.cos(limbSwing * 0.33333334F));            // :282
        }
        model.hat.copyFrom(model.head);                                                                    // :285
    }

    /** HumanoidModel.poseRightArm (:289-330): the EMPTY pose alone is reachable for a HumanoidMobRenderer - {@code rightArm.yRot = 0} (:291). */
    private static void poseRightArm(HumanoidModel<?> model) {
        if (model.rightArmPose != HumanoidModel.ArmPose.EMPTY) {
            throw new IllegalStateException("the classic side transcribes the EMPTY arm pose only; a HumanoidMobRenderer never sets another");
        }
        model.rightArm.yRot = 0.0F;
    }

    /** HumanoidModel.poseLeftArm (:333-374): the EMPTY pose alone - {@code leftArm.yRot = 0} (:335). */
    private static void poseLeftArm(HumanoidModel<?> model) {
        if (model.leftArmPose != HumanoidModel.ArmPose.EMPTY) {
            throw new IllegalStateException("the classic side transcribes the EMPTY arm pose only; a HumanoidMobRenderer never sets another");
        }
        model.leftArm.yRot = 0.0F;
    }

    /** HumanoidModel.setupAttackAnimation (:382-408): the swing on the attack arm. */
    private static void setupAttackAnimation(HumanoidModel<?> model, HumanoidPose entity, float ageInTicks) {
        if (!(model.attackTime <= 0.0F)) {                                                                 // :382
            HumanoidArm humanoidarm = getAttackArm(entity);                                                // :383
            float f = model.attackTime;                                                                    // :384
            model.body.yRot = Mth.sin(Mth.sqrt(f) * 6.2831855F) * 0.2F;                                    // :385
            if (humanoidarm == HumanoidArm.LEFT) {                                                         // :386
                model.body.yRot *= -1.0F;                                                                  // :387
            }
            model.rightArm.z = Mth.sin(model.body.yRot) * 5.0F;                                            // :390
            model.rightArm.x = -Mth.cos(model.body.yRot) * 5.0F;                                           // :391
            model.leftArm.z = -Mth.sin(model.body.yRot) * 5.0F;                                            // :392
            model.leftArm.x = Mth.cos(model.body.yRot) * 5.0F;                                             // :393
            model.rightArm.yRot = model.rightArm.yRot + model.body.yRot;                                   // :394
            model.leftArm.yRot = model.leftArm.yRot + model.body.yRot;                                     // :395
            model.leftArm.xRot = model.leftArm.xRot + model.body.yRot;                                     // :396
            f = 1.0F - model.attackTime;                                                                   // :397
            f *= f;                                                                                        // :398
            f *= f;                                                                                        // :399
            f = 1.0F - f;                                                                                  // :400
            float f1 = Mth.sin(f * 3.1415927F);                                                            // :401
            float f2 = Mth.sin(model.attackTime * 3.1415927F) * -(model.head.xRot - 0.7F) * 0.75F;         // :402
            getArm(model, humanoidarm).xRot -= f1 * 1.2F + f2;                                             // :403
            getArm(model, humanoidarm).yRot = getArm(model, humanoidarm).yRot + model.body.yRot * 2.0F;    // :404
            getArm(model, humanoidarm).zRot = getArm(model, humanoidarm).zRot + Mth.sin(model.attackTime * 3.1415927F) * -0.4F;  // :405
        }
    }

    /** HumanoidModel.getArm (:457, the bytecode's line table). */
    private static net.minecraft.client.model.geom.ModelPart getArm(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
    }

    /** HumanoidModel.getAttackArm (:466-467): the main arm while the main hand swings, else its opposite. */
    private static HumanoidArm getAttackArm(HumanoidPose entity) {
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

    /** AnimationUtils.bobModelPart (:59-60): {@code zRot += multiplier * (cos(age * 0.09) * 0.05 + 0.05); xRot += multiplier * sin(age * 0.067) * 0.05}. */
    private static void bobModelPart(HumanoidModel<?> model, boolean right, float ageInTicks, float multiplier) {
        net.minecraft.client.model.geom.ModelPart part = right ? model.rightArm : model.leftArm;
        part.zRot = part.zRot + multiplier * (Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        part.xRot = part.xRot + multiplier * Mth.sin(ageInTicks * 0.067F) * 0.05F;
    }
}
