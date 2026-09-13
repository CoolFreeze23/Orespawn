package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.pose.PurplePowerPose;
import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * orig ModelPurplePower.java (ENT-S-146, transcribed 2026-09-06). Three spoke boxes (:24-41) drawn as
 * three six-spoke fans thrown into a fresh random orientation on every rendered frame (:44-86): the
 * orb is translucent (0.55 alpha at 0.75 colour), fullbright, and shimmers.
 *
 * <p>Where the 1.7.10 GL state went in 1.21.1 (all bytecode-cited against NeoForge 21.1.223):</p>
 * <ul>
 *   <li>:52 {@code glEnable(GL_NORMALIZE)}: no counterpart needed, {@code ModelPart.Polygon} normals
 *       are unit vectors already;</li>
 *   <li>:53-54 {@code glEnable(GL_BLEND); glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)}: the render type,
 *       {@link #RENDER_TYPE} = {@code RenderType.entityTranslucent} (the {@code SlimeModel} idiom:
 *       {@code EntityModel(Function)} stores it and {@code LivingEntityRenderer.getRenderType}'s
 *       bodyVisible branch, offsets 17-30, returns {@code model.renderType(texture)} to
 *       {@code render}, which draws through {@code bufferSource.getBuffer(renderType)} at 579).
 *       {@code RenderType.lambda$static$7} builds it with {@code TRANSLUCENT_TRANSPARENCY} (25),
 *       {@code NO_CULL} (31), {@code LIGHTMAP}, {@code OVERLAY} and the builder defaults
 *       {@code LEQUAL_DEPTH_TEST} / {@code COLOR_DEPTH_WRITE} ({@code CompositeStateBuilder.<init>}
 *       26-29 / 75-78); {@code RenderStateShard.lambda$static$10} is
 *       {@code blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ONE_MINUS_SRC_ALPHA)};</li>
 *   <li>:55 {@code glColor4f(0.75, 0.75, 0.75, 0.55)}: {@link #COLOR}, the packed vertex colour every
 *       part is drawn with ({@code ModelPart.Cube.compile} hands its colour argument to every
 *       {@code addVertex}, offset 176; the shader multiplies the texel by it and the blend uses its
 *       alpha). Set absolutely, as {@code glColor4f} was: the colour the renderer passes in (white, or
 *       the 0.15-alpha {@code 654311423} for a mob invisible to the viewer, {@code LivingEntityRenderer
 *       .render} 610-621) is disregarded exactly as 1.7.10 overrode it;</li>
 *   <li>:56 {@code setLightmapTextureCoords(240, 240)}: the block-15 / sky-15 lightmap texel, i.e.
 *       {@code LightTexture.pack(15, 15)} = {@code FULL_BRIGHT}; the packed light is the RENDERER's in
 *       1.21.1 ({@code EntityRenderer.getPackedLightCoords} 0-24 packs {@code getBlockLightLevel} /
 *       {@code getSkyLightLevel}), so {@link PurplePowerRenderer} and the GeckoLib candidate answer
 *       both with {@link #LIGHT_LEVEL}; the model passes the light it is handed through;</li>
 *   <li>:83-85 the colour reset, {@code glDisable(GL_BLEND)} and the pop: the colour is a per-call
 *       argument and the blend state belongs to the render type, so only the pose-stack pop remains.</li>
 * </ul>
 *
 * <p>The rolls (:57, :66, :75) come from the WORLD's random ({@code p.worldObj.rand}), read through
 * {@link PurplePowerPose#getLevelRandom()} and captured by {@link #poseFrom} because
 * {@code renderToBuffer} has no entity argument in 1.21.1. The accumulating rotations are kept
 * bug-for-bug: :58 and :64 both rotate about X by the same {@code rf1} (the second is NOT negated), so
 * Shape2's fan is drawn under X(2 rf1) - Y(rf1'), and Shape3's under X(2 rf1) - Y(2 rf1') - Z(rf1''); the
 * matrix is popped at :85, so nothing persists past the frame. The 60-degree step is the spoke's own
 * {@code zRot} (:60, {@code newangle += 1.0471976f} at :62, float32 accumulation) inside the fan's
 * rotation - not a pose-stack step outside it. Nothing reads the age: the spin is the rolls'.</p>
 *
 * <p>One thing the port does NOT transcribe: the order in which a box's six faces are emitted. Here it is
 * vanilla's own {@code ModelPart.Cube} order (DOWN, UP, WEST, NORTH, EAST, SOUTH; NeoForge 21.1.223
 * {@code Cube.<init>} 365-785), which the GeckoLib candidate is permuted into ({@link FaceOrder}); 1.7.10's
 * {@code ModelBox} drew +X, -X, -Y, +Y, -Z, +Z (verified against the 1.7.10 client jar, {@code bis.<init>}
 * 365-772 and {@code render} 0-28). Under blending with the depth written that order decides which of a
 * spoke's own faces shows through the others - disclosed in the ENT-S-146 record, not reproducible with
 * vanilla's cube.</p>
 *
 * <p>The GeckoLib candidate ({@link PurplePowerGeoReplacement}) reproduces the same frame on the
 * render-instance-expanded rig ({@code tools/s4_model_proofs.json}): the clones carry the step, the
 * fan groups the rolls and the carried doublings.</p>
 *
 * BUG-041 stage 2 (2026-09-13): the 1.7.10 export sets {@code mirror = true} AFTER {@code addBox} (orig ModelPurplePower.java:
 * 3 stores, all inert - 1.7.10's ModelBox reads the flag in its constructor, law 11 from Mojang's 1.7.10 jar),
 * so the original rendered UNMIRRORED. The port's 3 {@code .mirror()} calls preceded {@code addBox} and flipped
 * every face's U: dropped port-wide as the EnderReaper precedent was (5354420); geometry unchanged; proven by the
 * reference-geometry leg.
 */
public class ModelPurplePower extends EntityModel<PurplePower> {
    /** orig :53-54 - the blend state, as the model's render type (see the class comment). */
    public static final Function<ResourceLocation, RenderType> RENDER_TYPE = RenderType::entityTranslucent;
    /**
     * orig :55 {@code glColor4f(0.75f, 0.75f, 0.75f, 0.55f)} packed as ARGB: {@code 0x8CBFBFBF} = (A 140, R 191,
     * G 191, B 191), exactly what {@code FastColor.ARGB32.colorFromFloat(0.55F, 0.75F, 0.75F, 0.75F)} returns
     * ({@code as8BitChannel} = {@code Mth.floor(f * 255)}: 140.25 -> 140, 191.25 -> 191) - the bytes the shader
     * multiplies the texel by. A compile-time constant on purpose: the GeckoLib descriptor's {@code renderColor}
     * and the game-test row read it inlined, without loading this client-only class on the dedicated server
     * (the 4b finding), and the row pins the literal against {@code colorFromFloat} there; the parity harness
     * observes it at every vertex on both sides.
     */
    public static final int COLOR = 0x8CBFBFBF;
    /**
     * orig :56 - both lightmap coordinates at 240 = light level 15 in the 1.21.1 lightmap's 16-per-level
     * texels; {@code LightTexture.pack(15, 15)} = {@code 15 << 4 | 15 << 20} = 15728880 =
     * {@code LightTexture.FULL_BRIGHT}. Both renderers answer {@code getBlockLightLevel} and
     * {@code getSkyLightLevel} with it (the {@code MagmaCubeRenderer} idiom, bytecode 0-2).
     */
    public static final int LIGHT_LEVEL = 15;
    /** orig :59 / :68 / :77 - six draws per spoke. */
    public static final int INSTANCES = 6;
    /** orig :62 / :71 / :80 - the spoke-to-spoke Z step, accumulated in float32 ({@code newangle += 1.0471976f}). */
    public static final float SPOKE_STEP = 1.0471976F;

    private final ModelPart innerSpoke;
    private final ModelPart middleSpoke;
    private final ModelPart outerSpoke;

    /** orig :57 {@code p.worldObj.rand}: the level's random, captured by {@link #poseFrom} for {@link #renderToBuffer}. */
    private RandomSource random;

    public ModelPurplePower(ModelPart root) {
        super(RENDER_TYPE);
        this.innerSpoke = root.getChild("Shape1");
        this.middleSpoke = root.getChild("Shape2");
        this.outerSpoke = root.getChild("Shape3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("Shape1",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-2.0F, -0.5F, -0.5F, 4, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape2",
                CubeListBuilder.create().texOffs(0, 7)
                        .addBox(-4.0F, -0.5F, -0.5F, 8, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("Shape3",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-7.0F, -0.5F, -0.5F, 14, 1, 1),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(PurplePower entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        poseFrom(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    /**
     * orig :50 {@code setRotationAngles} writes no angle; the pose is the three rolls of
     * {@link #renderToBuffer}. This entity-free entry captures the source they are rolled from (orig :57
     * {@code p.worldObj.rand}, the entity's level) - the Slice 4c parity harness drives the classic model
     * and the GeckoLib hook from the same seeded {@link PurplePowerPose}.
     */
    public void poseFrom(PurplePowerPose entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                         float netHeadYaw, float headPitch) {
        this.random = entity.getLevelRandom();
    }

    /**
     * The degrees-to-radians conversion {@code Axis.rotationDegrees} applies to a roll before
     * {@code mulPose} (1.21.1 bytecode: {@code ldc 0.017453292f; fmul}; the constant is
     * {@code (float) (Math.PI / 180.0)}), so the GeckoLib hook feeds its fan group bones the identical float.
     */
    public static float rollRadians(float degrees) {
        return degrees * ((float) (Math.PI / 180.0));
    }

    /**
     * orig :44-86, statement for statement (the GL state of :52-56 and :83-84 lives in the constants and
     * the renderer, see the class comment). Three fans: a fresh roll, the pose stack rotated by it about
     * the fan's axis, six draws of the spoke at its own stepped {@code zRot}, then the SAME rotation
     * applied again (:64, :73, :82 - not negated: it doubles and carries into the next fans), popped at
     * the end.
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        RandomSource random = this.random;
        poseStack.pushPose();                                          // :51 glPushMatrix
        // :52-56 - see RENDER_TYPE, COLOR, LIGHT_LEVEL.
        float rf1 = roll(random);                                      // :57 rf1 = worldObj.rand.nextFloat() * 360
        poseStack.mulPose(Axis.XP.rotationDegrees(rf1));               // :58 glRotatef(rf1, 1, 0, 0)
        renderFan(this.innerSpoke, poseStack, vertexConsumer, packedLight, packedOverlay);   // :59-63
        poseStack.mulPose(Axis.XP.rotationDegrees(rf1));               // :64 glRotatef(rf1, 1, 0, 0) - again, NOT negated
        rf1 = roll(random);                                            // :66 (:65 newangle = 0 is the loop's own reset)
        poseStack.mulPose(Axis.YP.rotationDegrees(rf1));               // :67 glRotatef(rf1, 0, 1, 0)
        renderFan(this.middleSpoke, poseStack, vertexConsumer, packedLight, packedOverlay);  // :68-72
        poseStack.mulPose(Axis.YP.rotationDegrees(rf1));               // :73 again
        rf1 = roll(random);                                            // :75
        poseStack.mulPose(Axis.ZP.rotationDegrees(rf1));               // :76 glRotatef(rf1, 0, 0, 1)
        renderFan(this.outerSpoke, poseStack, vertexConsumer, packedLight, packedOverlay);   // :77-81
        poseStack.mulPose(Axis.ZP.rotationDegrees(rf1));               // :82 again
        // :83 glColor4f(1, 1, 1, 1) and :84 glDisable(GL_BLEND): per-call colour, render-type-scoped blend - nothing to reset.
        poseStack.popPose();                                           // :85 glPopMatrix
    }

    /**
     * orig :57 / :66 / :75 through {@link PurplePowerPose#roll}. {@code random} is null only when
     * {@code renderToBuffer} runs without a preceding pose - never in the game ({@code LivingEntityRenderer
     * .render} calls {@code setupAnim} at 510 before {@code renderToBuffer} at 621), only the parity
     * harness's static / reference-geometry captures, which then draw the unrolled bind frame.
     */
    private static float roll(RandomSource random) {
        return random == null ? 0.0F : PurplePowerPose.roll(random);
    }

    /**
     * orig :59-63 (and :68-72, :77-81): six draws of one spoke, its own {@code rotateAngleZ} stepping
     * 0, 60, ... 300 degrees inside the fan's pose-stack rotation, the step accumulated in float32 as
     * the original did ({@code newangle += 1.0471976f}). The colour is :55's, absolute.
     *
     * <p>Slice 4c: the GeckoLib rig carries this loop statically - draw {@code k} of a spoke is the
     * clone bone {@code <spoke>__i<k>} (its own Z rotation {@code k} steps, bind) under the fan group
     * {@code <spoke>__fan} the hook rolls; see {@code tools/s4_model_proofs.json} ({@code
     * render_instances}, {@code step_scope: part}).</p>
     */
    private static void renderFan(ModelPart spoke, PoseStack poseStack, VertexConsumer vertexConsumer,
                                  int packedLight, int packedOverlay) {
        float newangle = 0.0F;                                         // :48 / :65 / :74
        for (int i = 0; i < INSTANCES; ++i) {                          // :59
            spoke.zRot = newangle;                                     // :60 Shape1.rotateAngleZ = newangle
            spoke.render(poseStack, vertexConsumer, packedLight, packedOverlay, COLOR);   // :61 Shape1.render(f5) under :55's colour
            newangle += SPOKE_STEP;                                    // :62
        }
    }
}
