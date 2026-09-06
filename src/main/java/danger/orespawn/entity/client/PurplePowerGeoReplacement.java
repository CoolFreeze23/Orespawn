package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PurplePower;
import danger.orespawn.entity.pose.PurplePowerPose;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib PurplePower (Slice 4c, re-based onto the ENT-S-146 classic 2026-09-06):
 * {@link ModelPurplePower}'s three random spoke fans on the render-instance-expanded rig.
 * The classic {@code renderToBuffer} (orig ModelPurplePower.java:44-86) rolls the level's
 * random three times per rendered frame and draws each spoke six times at its own stepped
 * {@code zRot} inside the roll's pose-stack rotation, the rotations accumulating: Shape1's fan
 * under X(r1); Shape2's under X(r1), X(r1) again (orig :64, not negated), then Y(r2); Shape3's
 * under those, Y(r2) again (:73) and then Z(r3). The converted rig carries the loop statically
 * ({@code render_instances}, {@code step_scope: part}): draw {@code k} of a spoke is the clone bone
 * {@code <spoke>__i<k>} (its own Z rotation {@code k} float32-accumulated steps, bind) under the fan
 * group {@code <spoke>__fan} at the model origin; every carried rotation is its own group above
 * the fan ({@code group_chain}: {@code Shape2__carry_x1}, {@code Shape2__carry_x2};
 * {@code Shape3__carry_x1}, {@code Shape3__carry_x2}, {@code Shape3__carry_y1},
 * {@code Shape3__carry_y2}) - one bone per classic {@code glRotatef}, nested, because a pose stack
 * rotated about X and then about Y is parent X over child Y, whereas one GeckoLib bone rotates
 * Z, then Y, then X ({@code RenderUtil.rotateMatrixAroundBone} 4.8.4 bytecode: {@code mulPose(ZP)}
 * 10-22, {@code mulPose(YP)} 35-47, {@code mulPose(XP)} 60-72) and cannot express X-then-Y in one
 * bone; and one bone per rotation rather than one bone per doubled angle because the matrix
 * products then run in the classic's own sequence: a single X(2 r1) rounds differently from
 * X(r1) - X(r1) and left Shape3's normals 1.5e-6 off the classic's, over the surface leg's 1e-6
 * (measured 2026-09-06; the tolerance stands, the rig became more literal). So this hook only
 * rolls the group bones and never touches a clone; the rolls come from the same
 * {@link PurplePowerPose#roll} the classic loop reads, three per frame in X, Y, Z order, from
 * {@link PurplePowerPose#getLevelRandom()}.
 *
 * <p>The render state (orig :53-56: translucent 0.55 alpha at 0.75 colour, fullbright) rides the
 * descriptor's hooks below, read by {@link OreSpawnGeoReplacedEntityRenderer}: the render type,
 * colour and light levels are the classic model's own constants, so the two renderers cannot
 * drift apart; and the shipped rig carries the classic within-cube face order ({@link FaceOrder},
 * {@code orespawn:cube_face_order}), which decides what a translucent spoke's back face shows
 * through its front face. Texture by {@code getPurpleType()} through
 * {@link PurplePowerRenderer#textureFor}; shadow {@link PurplePowerRenderer#SHADOW} (0.3 x 2.75,
 * ENT-S-092).</p>
 *
 * <p>Once per rendered frame, as the classic: GeckoLib 4.8.4 {@code GeoReplacedEntityRenderer
 * .actuallyRender} runs {@code GeoModel.handleAnimations} (offsets 709-718) and so this hook only
 * when {@code isReRender} is false; ENT-S-147 (REPORT) records the per-frame animation dedup that
 * can skip the hook behind the pause screen with one instance in view - the rolls then hold that
 * frame where the classic would roll again; the same record as the Rotator's.</p>
 */
public final class PurplePowerGeoReplacement extends OreSpawnGeoReplacement<PurplePower> {
    /** The fan group bones the converter emits for {@code render_instances} with {@code step_scope: part} (each chain's last). */
    public static final String SHAPE1_FAN = "Shape1__fan";
    public static final String SHAPE2_FAN = "Shape2__fan";
    public static final String SHAPE3_FAN = "Shape3__fan";
    /** orig :58 and :64 - X by r1, twice, above Shape2's and Shape3's fans. */
    public static final String SHAPE2_CARRY_X1 = "Shape2__carry_x1";
    public static final String SHAPE2_CARRY_X2 = "Shape2__carry_x2";
    public static final String SHAPE3_CARRY_X1 = "Shape3__carry_x1";
    public static final String SHAPE3_CARRY_X2 = "Shape3__carry_x2";
    /** orig :67 and :73 - Y by r2, twice, above Shape3's fan. */
    public static final String SHAPE3_CARRY_Y1 = "Shape3__carry_y1";
    public static final String SHAPE3_CARRY_Y2 = "Shape3__carry_y2";

    private static final GeoReplacementDescriptor<PurplePower> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.PURPLE_POWER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            PurplePower.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/purplepower.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/purplepower.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture.png"),
            PurplePowerRenderer.SHADOW) {
        @Override
        public ResourceLocation texture(PurplePower entity) {
            return PurplePowerRenderer.textureFor(entity.getPurpleType());
        }

        /**
         * orig :53-54 - the classic model's own render-type function object ({@link ModelPurplePower#RENDER_TYPE},
         * {@code RenderType::entityTranslucent}): the SAME {@code Function} the classic renderer applies, so the
         * harness proves the two sides equal by identity.
         */
        @Override
        public Function<ResourceLocation, RenderType> renderType(PurplePower entity) {
            return ModelPurplePower.RENDER_TYPE;
        }

        /** orig :55 - the classic model's packed (0.75, 0.75, 0.75, 0.55). */
        @Override
        public int renderColor(PurplePower entity, float partialTick) {
            return ModelPurplePower.COLOR;
        }

        /** orig :56 - lightmap 240 / 240, the classic renderer's light-level overrides. */
        @Override
        public boolean fullBright(PurplePower entity) {
            return true;
        }

        /** A blending rig: the seam expects {@link FaceOrder#KEY} in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public PurplePowerGeoReplacement() {
        super(DESCRIPTOR);
    }

    /**
     * orig ModelPurplePower.java:57-82 on the group bones, one bone per {@code glRotatef}. The three
     * rolls are consumed in the classic's order (X, Y, Z: :57, :66, :75), each converted with the same
     * {@link ModelPurplePower#rollRadians} the classic's {@code Axis.rotationDegrees} applies; the repeated
     * rotations (:64, :73 - never negated) are the carry groups, each turned by the very roll its fan
     * was, so the accumulating pose stack is reproduced multiply for multiply.
     */
    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        RandomSource random = inputs.subject(PurplePowerPose.class).getLevelRandom();
        float x = ModelPurplePower.rollRadians(PurplePowerPose.roll(random));   // :57
        rotateX(processor, SHAPE1_FAN, x);                                       // :58 (the fan of :59-63)
        float y = ModelPurplePower.rollRadians(PurplePowerPose.roll(random));   // :66
        rotateX(processor, SHAPE2_CARRY_X1, x);                                  // :58, carried
        rotateX(processor, SHAPE2_CARRY_X2, x);                                  // :64, the repeat
        rotateY(processor, SHAPE2_FAN, y);                                       // :67 (the fan of :68-72)
        float z = ModelPurplePower.rollRadians(PurplePowerPose.roll(random));   // :75
        rotateX(processor, SHAPE3_CARRY_X1, x);                                  // :58, carried
        rotateX(processor, SHAPE3_CARRY_X2, x);                                  // :64
        rotateY(processor, SHAPE3_CARRY_Y1, y);                                  // :67, carried
        rotateY(processor, SHAPE3_CARRY_Y2, y);                                  // :73, the repeat
        rotateZ(processor, SHAPE3_FAN, z);                                       // :76 (the fan of :77-81)
        // :82 rotates Z once more and :85 pops: nothing after Shape3's fan reads it.
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<PurplePower, PurplePowerGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new PurplePowerGeoReplacement());
        }
    }
}
