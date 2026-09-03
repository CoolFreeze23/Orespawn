package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.TheQueen;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Geckolib renderer for {@link TheQueen}. Replaces the legacy
 * four-pass {@code TheQueenRenderer} (opaque body / translucent
 * wing membranes / fullbright power cubes / fullbright eyes) with
 * a single Geckolib-driven render call.
 *
 * <p>The dynamic blue→red texture swap on phase-shift is owned by
 * {@link QueenModel#getTextureResource}, so this class only needs
 * to wire up the model, the 1.7.10 shadow and the 1.7.10 render
 * scale.</p>
 *
 * <p>orig ClientProxyOreSpawn.java:493
 * {@code new RenderTheQueen(new ModelTheQueen(0.65f), 1.9f, 2.0f)}:
 * RenderTheQueen.java:23-26 {@code super(model, par2 * par3)} makes the
 * shadow 1.9 * 2.0 = 3.8 and keeps {@code this.scale = par3} = 2.0;
 * RenderTheQueen.java:40-46 preRenderScale scales by {@code scale / 4}
 * (0.5) while {@code getPlayNicely() != 0} and by {@code scale} (2.0)
 * otherwise, invoked from func_77041_b :48-49. The ModelTheQueen(0.65f)
 * argument is only wingspeed, not a size (ENT-S-092).</p>
 *
 * <p>Frustum culling is forced off via {@link #shouldRender}: the
 * Queen's part-entity hitboxes can extend ~50 blocks from her
 * root AABB on a fully-extended wing or tail swing, and a missed
 * cull would let her invisibly clip through the player's screen
 * edge during the boss fight.</p>
 *
 * <h2>MultiHitBoxLib bone tracking</h2>
 *
 * <p>This class stays a plain {@link GeoEntityRenderer}. MHLib's
 * {@code MixinGeoEntityRenderer} (priority MAX_VALUE) injects at the
 * TAIL of every {@code GeoEntityRenderer} constructor and calls
 * {@code addRenderLayer(new GeckolibBoneInformationCollectorLayer(self))},
 * so the bone-tracking layer is attached automatically once super(...)
 * returns. The same mixin also wraps {@code renderRecursively} HEAD/TAIL
 * to invoke the layer's {@code onRenderRecursivelyStart/End} hooks,
 * which read each synced bone's world position via
 * {@code GeoBone#getWorldPosition} and ship a {@code CPacketBoneInformation}
 * server-ward where {@code MHLibPartEntity} positions are reconciled.</p>
 *
 * <p>Two renderer-side facts make that position meaningful (ENT-S-092,
 * proven from the GeckoLib 4.8.4 bytecode):</p>
 * <ol>
 *   <li>{@code GeoEntityRenderer.renderRecursively} only fills a bone's
 *       local/world matrices when {@code GeoBone.isTrackingMatrices()} is
 *       true (offsets 24-28 gate the block at 31-115); the flag defaults to
 *       false ({@code GeoBone.<init>} offset 139-140) and nothing in GeckoLib
 *       or the vendored MHLib ever sets it, so {@code getWorldPosition()}
 *       read the constructor's identity matrix — world (0, 0, 0) — for every
 *       synced bone. {@link #preRender} therefore enables tracking on the
 *       profile's synched bones once per baked model.</li>
 *   <li>{@code GeoEntityRenderer.preRender} captures
 *       {@code entityRenderTranslations = new Matrix4f(poseStack.last().pose())}
 *       at offsets 0-15 and only THEN (offset 38) calls
 *       {@code scaleModelForRender}; every bone's local matrix is
 *       {@code entityRenderTranslations^-1 * poseState}
 *       ({@code RenderUtil.invertAndMultiplyMatrices}, renderRecursively
 *       offsets 47-56), so a pose scale pushed BEFORE super.preRender is
 *       captured and cancelled out of every bone position, while one applied
 *       in {@code scaleModelForRender} ({@code GeoRenderer} default, offsets
 *       0-24: {@code if (!isReRender && (w != 1 || h != 1)) poseStack.scale(w, h, w)})
 *       multiplies every bone world offset. The 1.7.10 factor is applied in
 *       {@link #scaleModelForRender} for that reason: the drawn body and the
 *       MHLib part positions scale together (verified headlessly by
 *       {@code danger.orespawn.g1.QueenPartPlacementProbe}: positions at 2.0
 *       and 0.5 are exactly 2.0x / 0.5x the 1.0 positions, and the old
 *       pre-capture order leaves them at 1.0x — a half-size ghost).</li>
 * </ol>
 *
 * <p>Part SIZES never come from the pose scale: they are the profile sizes
 * times the bone animation scale times {@code TheQueen#mhlibGetEntitySizeScale}
 * (1.0 hostile, 0.25 PlayNicely), which is what makes the PlayNicely parts
 * quarter-size to match the 0.5 draw of a profile authored for the 2.0 draw.</p>
 */
public class QueenRenderer extends GeoEntityRenderer<TheQueen> {

    /** orig RenderTheQueen.java:25 {@code this.scale = par3} with ClientProxyOreSpawn.java:493 par3 = 2.0f. */
    public static final float SCALE = 2.0F;
    /** orig RenderTheQueen.java:23-26 {@code super(model, par2 * par3)} with ClientProxyOreSpawn.java:493 (1.9f, 2.0f) = 3.8. */
    public static final float SHADOW = 1.9F * 2.0F;

    /** The baked model whose synched bones already have matrix tracking enabled (see {@link #preRender}). */
    private BakedGeoModel trackedModel;

    public QueenRenderer(EntityRendererProvider.Context context) {
        super(context, new QueenModel());
        this.shadowRadius = SHADOW;
    }

    // OPT-013: evaluated for replacement with a finite inflated cull box and
    // intentionally left as-is. The animated model envelope (GeckoLib/MHLib
    // bone-driven parts, code-model limb rotations) is not statically provable
    // from any constant in this codebase, and an under-sized box would visibly
    // pop the boss out at the screen edge — a behavior change. Keeping
    // unconditional true is the strictly-neutral choice.
    @Override
    public boolean shouldRender(TheQueen entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    /**
     * Enables {@code GeoBone#setTrackingMatrices(true)} on the profile's synched
     * bones before GeckoLib captures the entity translation, so
     * {@code renderRecursively} fills the world matrices MHLib's collector reads
     * (see the class comment). Baked models are cached per resource by GeckoLib,
     * so this runs once per model instance (again after a resource reload).
     * The scale itself is NOT applied here: anything pushed onto the pose stack
     * before {@code super.preRender} lands inside the capture and would leave the
     * parts on a half-size ghost of the drawn body.
     */
    @Override
    public void preRender(PoseStack poseStack, TheQueen animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        if (model != this.trackedModel && trackSynchedBones(animatable, model)) {
            this.trackedModel = model;
        }
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, colour);
    }

    /**
     * BOSS-017 / ENT-S-092: orig RenderTheQueen.java:40-46 preRenderScale — a
     * PlayNicely Queen ({@code getPlayNicely() != 0}, the live synced flag)
     * renders at {@code scale / 4} = 0.5, otherwise at {@code scale} = 2.0.
     * GeckoLib pathway: this is the post-capture slot GeoEntityRenderer.preRender
     * calls at offset 38, so the factor reaches every bone's local/world matrix
     * and the MHLib part positions follow the drawn geometry; part sizes stay
     * at the profile values (times the entity scale, see TheQueen). The
     * GeckoLib native {@code withScale} factors (1.0 unless set) compose with it,
     * and the inherited {@code !isReRender} guard keeps layer re-renders from
     * scaling twice.
     */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, TheQueen animatable,
                                    BakedGeoModel model, boolean isReRender, float partialTick, int packedLight,
                                    int packedOverlay) {
        float effectiveScale = animatable.getPlayNicely() != 0 ? SCALE / 4.0F : SCALE;
        super.scaleModelForRender(widthScale * effectiveScale, heightScale * effectiveScale, poseStack, animatable,
                model, isReRender, partialTick, packedLight, packedOverlay);
    }

    /**
     * @return true only when the profile resolved and every synched bone was found and
     *         switched to matrix tracking; the caller latches the model only then, so a
     *         profile that is not resolved yet (or a bone list that does not match the
     *         baked model) is retried on the next frame instead of being latched as done.
     */
    private static boolean trackSynchedBones(TheQueen animatable, BakedGeoModel model) {
        if (!((Object) animatable instanceof IMultipartEntity<?> multipart)) {
            return false;
        }
        Optional<HitboxProfile> profile = multipart.getHitboxProfile();
        if (profile == null || profile.isEmpty()) {
            return false;
        }
        boolean allTracked = true;
        for (String bone : profile.get().synchedBones()) {
            Optional<GeoBone> geoBone = model.getBone(bone);
            if (geoBone.isPresent()) {
                geoBone.get().setTrackingMatrices(true);
            } else {
                allTracked = false;
            }
        }
        return allTracked;
    }
}
