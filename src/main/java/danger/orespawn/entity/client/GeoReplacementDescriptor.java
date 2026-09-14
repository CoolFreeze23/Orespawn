package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.joml.Matrix4f;

/**
 * Per-registry description of a client-only GeckoLib replacement renderer.
 *
 * <p>The registry identity and the geo/animation/texture triple are fixed.
 * Anything that legitimately varies by species (texture variant, render
 * scale) is a hook over the actual entity being drawn. The entity class
 * itself is never touched: this is what lets a species move to GeckoLib
 * without inheriting {@code GeoEntity}.</p>
 */
public abstract class GeoReplacementDescriptor<E extends Entity> {
    private final Supplier<? extends EntityType<? extends E>> entityType;
    private final Class<E> entityClass;
    private final ResourceLocation modelResource;
    private final ResourceLocation animationResource;
    private final ResourceLocation textureResource;
    private final float shadowRadius;

    protected GeoReplacementDescriptor(Supplier<? extends EntityType<? extends E>> entityType,
                                       Class<E> entityClass,
                                       ResourceLocation modelResource,
                                       ResourceLocation animationResource,
                                       ResourceLocation textureResource,
                                       float shadowRadius) {
        this.entityType = Objects.requireNonNull(entityType, "entityType");
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
        this.modelResource = Objects.requireNonNull(modelResource, "modelResource");
        this.animationResource = Objects.requireNonNull(animationResource, "animationResource");
        this.textureResource = Objects.requireNonNull(textureResource, "textureResource");
        this.shadowRadius = shadowRadius;
    }

    public final EntityType<?> entityType() {
        return Objects.requireNonNull(this.entityType.get(), "replacement entity type");
    }

    public final ResourceLocation modelResource() {
        return this.modelResource;
    }

    public final ResourceLocation animationResource() {
        return this.animationResource;
    }

    public final ResourceLocation textureResource() {
        return this.textureResource;
    }

    public final float shadowRadius() {
        return this.shadowRadius;
    }

    /** The replaced renderer only ever sees its own registry's entities; anything else is a wiring bug. */
    public final E requireEntity(Object candidate) {
        if (!(candidate instanceof Entity entity)
                || entity.getType() != entityType()
                || !this.entityClass.isInstance(entity)) {
            throw new IllegalArgumentException(
                    "Replacement for " + entityType() + " received " + candidate);
        }
        return this.entityClass.cast(entity);
    }

    /** Texture for the entity being drawn; the descriptor texture by default. */
    public ResourceLocation texture(E entity) {
        return this.textureResource;
    }

    /** Applied before GeckoLib's own scaling, i.e. where the classic renderer scaled its pose stack. */
    public void applyScale(E entity, PoseStack poseStack, float partialTick) {
    }

    /** Applied after GeckoLib's yaw/death rotations, i.e. where a classic {@code setupRotations} override added its own. */
    public void applyRotations(E entity, PoseStack poseStack, float ageInTicks, float partialTick) {
    }

    /** GeckoLib's own default colour: {@code GeoRenderer.getRenderColor} returns {@code Color.WHITE}, {@code new Color(-1)} (4.8.4 bytecode). */
    public static final int WHITE = -1;

    /**
     * ENT-S-146: the light level a fullbright species ({@link #fullBright}) answers for BOTH
     * {@code getBlockLightLevel} and {@code getSkyLightLevel}, so {@code EntityRenderer.getPackedLightCoords}
     * (21.1.223 bytecode 0-24) packs {@code LightTexture.pack(15, 15)} = 15728880 = {@code LightTexture
     * .FULL_BRIGHT}, whose lightmap texel is (240, 240). The seam's own constant (on ENT-S-146, D4): the
     * shared renderer reads it here, never from a species model; a species' classic renderer carries
     * its own transcription of the same level (e.g. {@code ModelPurplePower.LIGHT_LEVEL}, pinned equal
     * by the game-test row).
     */
    public static final int FULL_BRIGHT_LEVEL = 15;

    /**
     * ENT-S-146: the render-type FUNCTION the candidate draws its visible body with, or {@code null} for
     * GeckoLib's own ({@code GeoReplacedEntityRenderer.getRenderType} 47-61 falls through to
     * {@code GeoModel.getRenderType} = {@code RenderType.entityCutoutNoCull}, 4.8.4 bytecode offset 1).
     * The classic side holds its render type the same way - {@code Model.renderType}, a
     * {@code Function<ResourceLocation, RenderType>} stored by {@code EntityModel(Function)} and applied
     * by {@code Model.renderType(ResourceLocation)}, which {@code LivingEntityRenderer.getRenderType}
     * returns for a visible body (offsets 17-30) - so a species hands over the classic model's OWN
     * function object and the two renderers cannot drift apart. Returned as the function rather than
     * the {@code RenderType} (on ENT-S-146, D3) because the headless parity probe cannot
     * initialise {@code RenderType} at all ({@code RenderType.<clinit>} reaches {@code Items} through
     * {@code ItemRenderer.<clinit>} and trips {@code Bootstrap.checkBootstrapCalled}; OPT-029 R0,
     * measured 2026-09-06) but can observe a function object: its identity against the classic model's
     * field and the {@code RenderType} factory its owner class references. {@link
     * OreSpawnGeoReplacedEntityRenderer#getRenderType} applies it to the texture on the same condition
     * as vanilla (the entity not invisible) and leaves GeckoLib's invisible / glowing branches alone.
     */
    public Function<ResourceLocation, RenderType> renderType(E entity) {
        return null;
    }

    /**
     * ENT-S-146: the ARGB colour every vertex of the candidate is multiplied by - the colour int
     * {@code GeoRenderer.defaultRender} takes from {@code getRenderColor(...).argbInt()} (offsets 4-18)
     * and hands to every {@code addVertex} ({@code createVerticesOfQuad}, offset 81). {@link #WHITE}
     * (the default) keeps GeckoLib's own. The classic side's counterpart is the colour argument of
     * {@code ModelPart.render}.
     */
    public int renderColor(E entity, float partialTick) {
        return WHITE;
    }

    /**
     * ENT-S-146: {@code true} to draw the candidate fullbright - both light levels 15, so
     * {@code EntityRenderer.getPackedLightCoords} (0-24) packs {@code LightTexture.FULL_BRIGHT} into the
     * light the dispatcher hands {@code render}; the {@code MagmaCubeRenderer} idiom, the same the classic
     * renderer of such a species uses. {@code false} (the default) leaves the world's light.
     */
    public boolean fullBright(E entity) {
        return false;
    }

    /**
     * ENT-S-146: {@code true} when the rig's within-cube face order is visible - a blending render type -
     * so the seam expects the shipped geo to carry {@link FaceOrder#KEY} and warns once when a resource
     * pack dropped it; {@code false} (the default) for an opaque rig, where an absent key is the norm.
     */
    public boolean cubeFaceOrderRequired() {
        return false;
    }

    /**
     * The CONSTANT RENDER TRANSFORM (TEST-013): what the classic model's {@code renderToBuffer} applies to the pose
     * stack before every part, in the CLASSIC renderer's own terms - the Dungeon Beast's {@code
     * mulPose(Axis.YP.rotationDegrees(90))} (ModelDungeonBeast.java:535, orig :574), the Kraken's {@code
     * Axis.XP.rotationDegrees(90)} (ModelKraken.java:733, orig :1137) - which no {@code setupAnim} statement
     * carries and no bone of a converted rig can pose. {@link RenderTransform#IDENTITY}, the default, for every other
     * rig. Read WITHOUT an entity (the ENT-S-146 form of {@link #renderType}): the headless parity probe reads it
     * from the descriptor alone and applies it on both of its sides, so the geometry, draw-order and visual legs
     * measure it; {@link OreSpawnGeoReplacedEntityRenderer#applyRotations} applies its slot form in-game.
     */
    public RenderTransform renderTransform() {
        return RenderTransform.IDENTITY;
    }

    /**
     * A constant render transform in the classic renderer's own terms: a translation, then rotations about X, Y and Z in
     * that order ({@code translate(t); mulPose(XP); mulPose(YP); mulPose(ZP)} on the classic pose stack - the calls a
     * classic {@code renderToBuffer} makes; both rigs that declare one rotate about a single axis, so the order is theirs
     * by construction).
     *
     * <p>THE ORDER ANALYSIS (TEST-013; the frame from the bytecode of NeoForge 21.1.223 and GeckoLib 4.8.4, of the
     * harness-and-seam landing): the classic path applies its rotation INSIDE {@code renderToBuffer}, after
     * {@code LivingEntityRenderer.render}'s {@code scale(-1, -1, 1)} flip and {@code translate(0, -1.501, 0)} lift - in
     * ModelPart space, Y down, the origin 1.501 blocks above the feet. The replaced renderer's descriptor slot
     * ({@link OreSpawnGeoReplacedEntityRenderer#applyRotations}, after the entity yaw) runs in ENTITY space, before
     * GeckoLib's own chain: its 0.01 lift ({@code actuallyRender} 727) and then the bake, which flips NOTHING in x -
     * GeckoLib's baker negates the Bedrock cube x the converter had negated, so the bake's vertices land on ModelPart x
     * directly, and the probe's normalisation ({@code translate(0, 1.5, 0) scale(1, -1, 1)}) is the whole frame between
     * the two spaces. So the frame between the slot and ModelPart space is the SEAM's, {@code F = scale(1, -1, 1)
     * translate(0, -1.501, 0)} ({@link #seamFrame}: the bake's Y flip about the datum plus the classic lift; GeckoLib's
     * 0.01 lift outside it), and the slot carries the transform CONJUGATED through it, {@code F C F^-1}: for a rotation
     * about Y, {@code S_y R_y(t) S_y = R_y(t)}, so the Dungeon Beast's YP 90 is YP 90 in the slot; the Kraken's XP 90
     * becomes translate(0, 1.501, 1.501) R_x(-90), the same rotation about the classic origin 1.501 up (the mirror in y
     * reverses the sense of a rotation about X). The two renderers' chains differ by an x-mirror for every rig (the
     * classic's {@code scale(-1, -1, 1)} against the bake's y-only flip): the register's TEST-015 line. {@link #slotMatrix}
     * is the product, computed rather than hand-derived; {@code T2SeamTests.t2_009} measures it on matrices against the
     * closed form and against the classic chain on sample points. The parity probe wraps the SAME slot form in
     * {@code F^-1 ... F} around its ModelPart-space capture, which measures that the renderer's slot is the F-conjugate of
     * the declared form (an unconjugated slot fails the geometry leg at every sample) - not the frame itself, which no
     * headless leg can see: the bytecode above is the frame's confirmation.</p>
     */
    public record RenderTransform(float xDegrees, float yDegrees, float zDegrees, float x, float y, float z) {
        public static final RenderTransform IDENTITY = new RenderTransform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        /** M: LivingEntityRenderer.render's {@code scale(-1, -1, 1)} then {@code translate(0, -1.501, 0)} (21.1.223), the frame the slot's conjugation runs through. */
        private static final Matrix4f SEAM_FRAME = new Matrix4f().scale(1.0F, -1.0F, 1.0F).translate(0.0F, -1.501F, 0.0F);

        /** A pure rotation in classic terms: degrees about X, Y and Z as {@code Axis.XP / YP / ZP.rotationDegrees}. */
        public static RenderTransform rotationDegrees(float xDegrees, float yDegrees, float zDegrees) {
            return new RenderTransform(xDegrees, yDegrees, zDegrees, 0.0F, 0.0F, 0.0F);
        }

        public boolean isIdentity() {
            return this.xDegrees == 0.0F && this.yDegrees == 0.0F && this.zDegrees == 0.0F
                    && this.x == 0.0F && this.y == 0.0F && this.z == 0.0F;
        }

        /** The classic form: the calls the classic {@code renderToBuffer} makes, on a classic (ModelPart-space) pose stack. */
        public void applyClassic(PoseStack poseStack) {
            if (this.x != 0.0F || this.y != 0.0F || this.z != 0.0F) {
                poseStack.translate(this.x, this.y, this.z);
            }
            if (this.xDegrees != 0.0F) {
                poseStack.mulPose(Axis.XP.rotationDegrees(this.xDegrees));
            }
            if (this.yDegrees != 0.0F) {
                poseStack.mulPose(Axis.YP.rotationDegrees(this.yDegrees));
            }
            if (this.zDegrees != 0.0F) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(this.zDegrees));
            }
        }

        /**
         * The classic form as a matrix C: the same right-multiplied translate, X, Y, Z rotations {@link #applyClassic}
         * makes on a pose stack, built on the matrix alone so the probe and the dedicated-server gametests can read it
         * without a client class ({@code PoseStack.mulPose} and {@code Matrix4f.rotateX/Y/Z} both post-multiply).
         */
        public Matrix4f classicMatrix() {
            Matrix4f matrix = new Matrix4f();
            if (this.x != 0.0F || this.y != 0.0F || this.z != 0.0F) {
                matrix.translate(this.x, this.y, this.z);
            }
            if (this.xDegrees != 0.0F) {
                matrix.rotateX((float) Math.toRadians(this.xDegrees));
            }
            if (this.yDegrees != 0.0F) {
                matrix.rotateY((float) Math.toRadians(this.yDegrees));
            }
            if (this.zDegrees != 0.0F) {
                matrix.rotateZ((float) Math.toRadians(this.zDegrees));
            }
            return matrix;
        }

        /** A copy of the seam frame F (the record javadoc): the bake's Y flip about the datum plus the classic lift. */
        public static Matrix4f seamFrame() {
            return new Matrix4f(SEAM_FRAME);
        }

        /** The slot form F C F^-1: what the replaced renderer multiplies onto the pose stack in its descriptor slot. */
        public Matrix4f slotMatrix() {
            Matrix4f frame = seamFrame();
            return new Matrix4f(frame).mul(classicMatrix()).mul(frame.invert());
        }

        /** Applies the slot form to the pose stack (nothing for the identity). */
        public void applySlot(PoseStack poseStack) {
            if (!isIdentity()) {
                poseStack.mulPose(slotMatrix());
            }
        }
    }
}
