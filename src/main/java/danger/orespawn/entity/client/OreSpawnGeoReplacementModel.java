package danger.orespawn.entity.client;

import com.google.gson.JsonObject;
import danger.orespawn.entity.client.animation.SplineRepair;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;

/** The one GeoModel shared by every replacement: resources and the code-driven pose hook come from the descriptor and animatable. */
public final class OreSpawnGeoReplacementModel<E extends Entity, A extends OreSpawnGeoReplacement<E>>
        extends GeoModel<A> {
    private final GeoReplacementDescriptor<E> descriptor;
    /** G2: the bake the seam last decided on (applied, or fell back to GeckoLib's own order); a resource reload replaces it. */
    private BakedGeoModel ordered;
    /** Q9: the loaded animation file the repaired copy below was made from; a resource reload replaces it. */
    private BakedAnimations repairedSource;
    /** Q9: {@link #repairedSource} with every catmullrom keyframe's spline arguments repaired ({@link SplineRepair}). */
    private BakedAnimations repaired;

    public OreSpawnGeoReplacementModel(GeoReplacementDescriptor<E> descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public ResourceLocation getModelResource(A animatable) {
        return this.descriptor.modelResource();
    }

    @Override
    public ResourceLocation getAnimationResource(A animatable) {
        return this.descriptor.animationResource();
    }

    @Override
    public ResourceLocation getTextureResource(A animatable) {
        return this.descriptor.textureResource();
    }

    /**
     * G2 root-order contract: the cached bake is sorted into the classic draw order once
     * per bake, before its first draw. GeckoLib 4.8.4 {@code GeoModel.getBakedModel}
     * (bytecode) fetches {@code GeckoLibCache.getBakedModels().get(location)} (offsets
     * 0-9), throws when absent (13-42) and, when the instance differs from
     * {@code currentModel}, re-registers the processor's bones and remembers it (43-61);
     * {@code GeoRenderer.defaultRender} calls it on every draw (46-49).
     * {@code GeckoLibCache.reload} bakes every model again into fresh
     * {@code Object2ObjectOpenHashMap}s (0-16; {@code lambda$loadModels$5} constructs each
     * {@code BakedGeoModel} anew at 94-110) and {@code lambda$reload$0} swaps
     * {@code MODELS} (4-5), so after a resource reload the instance changes and the
     * identity test below runs the reorder again. The order is read from the geo
     * resource itself ({@link DrawOrder#KEY}, written by the converter next to the bones
     * it orders), through the same resource manager GeckoLib loaded the bake from. The
     * reorder is idempotent (sorting a sorted list), so a second model instance over the
     * same cached bake is harmless.
     *
     * <p>The missing-key policy (owner ruling 2026-09-06): this call runs inside
     * {@code GeoRenderer.defaultRender}, under {@code EntityRenderDispatcher.render}, so a
     * throw here is a client crash on the mob's first frame. A rig WITHOUT the key is what
     * a resource pack produces (Blockbench's exporter rewrites {@code description} and
     * drops it), and a resource pack must never crash the client: {@link
     * DrawOrder#applyOrFallback} reads the resource itself and leaves GeckoLib's own order
     * in place for such a rig, WARN once. A key that is present and wrong does not crash
     * the client either - "never" is absolute - it is loud as an ERROR log once per resource
     * (the geo, the exact reason, the pack author's fix) and then the same fallback, the
     * bake untouched; the reading presented to the owner with the landing. The shipped
     * rigs never take either fallback - the asset audit fails the build for a seam rig
     * whose key is absent or wrong - and the identity test above means the decision, like
     * the reorder, is made once per bake. ENT-S-146 adds the within-cube face order
     * ({@link FaceOrder#KEY}, written for a translucent rig, where the order of a cube's faces
     * decides what its blending shows) under the same policy and the same identity test.</p>
     */
    @Override
    public BakedGeoModel getBakedModel(ResourceLocation location) {
        BakedGeoModel baked = super.getBakedModel(location);
        if (baked != this.ordered) {
            ResourceManager resources = Minecraft.getInstance().getResourceManager();
            boolean faceOrderRequired = this.descriptor.cubeFaceOrderRequired();
            // ENT-S-146 (refuter A, D5): the geo resource is parsed ONCE per bake and the document handed to
            // both keys - not one parse per key for every seam rig on every bake and reload.
            JsonObject geoJson;
            try {
                geoJson = DrawOrder.geoJson(resources, location);
            } catch (IllegalStateException unreadable) {
                // The resource GeckoLib baked from moments ago cannot be read: both keys take their
                // ERROR-logged fallback (once per resource), the bake left exactly as the factory built it.
                DrawOrder.unreadable(location, unreadable);
                FaceOrder.unreadable(location, unreadable);
                this.ordered = baked;
                return baked;
            }
            DrawOrder.applyOrFallback(baked, geoJson, location);
            // ENT-S-146: the within-cube face order, the same policy (a present key applied, a wrong one
            // ERROR-logged once and left alone); absent is silent unless the species draws translucent.
            FaceOrder.applyOrFallback(baked, geoJson, location, faceOrderRequired);
            this.ordered = baked;
        }
        return baked;
    }

    /**
     * The Q9 repair (owner ruling 2026-09-06, scope addendum item 24, Q9 (a) SCOPED to this seam):
     * every clip a replacement's controller asks for is served from a copy of the loaded animation
     * file whose {@code catmullrom} keyframes carry the textbook spline arguments
     * ({@link SplineRepair}: GeckoLib 4.8.4 feeds each segment its own start value as P0 - a kinked
     * tangent at every key; the bytecode is cited there). GeckoLib 4.8.4 {@code GeoModel.getAnimation}
     * (bytecode) fetches {@code GeckoLibCache.getBakedAnimations().get(getAnimationResource(animatable))}
     * (offsets 0-18) and returns {@code BakedAnimations.getAnimation(name)} when the file is there
     * (20-44; {@code null} for a clip name the file lacks), and only then walks the fallback resources
     * and throws for a missing file (45-152); {@code AnimationController} asks through it on every
     * stage poll ({@code AnimationProcessor.buildAnimationQueue}). The repair is made ONCE per loaded
     * file, by identity: {@code GeckoLibCache.reload} bakes every animation file again into a fresh map
     * ({@code loadAnimations} -> {@code FileLoader.loadAnimationsFile} -> new {@code BakedAnimations}
     * records) and swaps {@code ANIMATIONS}, so after a resource reload the instance changes and the
     * identity test below repairs the new one; the same instance on every later call is served from
     * the copy. Idempotent by construction (the pass reads only values it never changes). The
     * replacement seam only: the Queen's native {@code QueenModel extends GeoModel} keeps stock
     * semantics until her own ruling; a file that is absent takes GeckoLib's own resolution and error,
     * untouched. A file that is present but lacks the clip answers {@code null} here without walking
     * {@code getAnimationResourceFallbacks} - GeckoLib's own order today (the walk starts at 45, only
     * after a present file has answered; no replacement overrides the fallbacks), so a future fallback
     * declaration must make this override walk it too (item 15 refuter A, D8). What this model can
     * retain is one pair ({@link #repairedSource} / {@link #repaired}, replaced, never accumulated),
     * and the model instance itself is replaced on F3+T (D6).
     */
    @Override
    public Animation getAnimation(A animatable, String name) {
        BakedAnimations loaded = GeckoLibCache.getBakedAnimations().get(getAnimationResource(animatable));
        if (loaded == null) {
            return super.getAnimation(animatable, name);
        }
        if (loaded != this.repairedSource) {
            this.repaired = SplineRepair.repair(loaded);
            this.repairedSource = loaded;
        }
        return this.repaired.getAnimation(name);
    }

    /**
     * One motion source per species ({@code contract_design.md} section 6.1): the classic code-driven
     * hook runs only while the per-entity manager holds no controllers. GeckoLib 4.8.4
     * {@code GeoModel.handleAnimations} runs {@code AnimationProcessor.tickAnimation} - every registered
     * controller, its bones written - at offsets 268-284 and THEN this method at 287-292, so a hook that
     * posed here would overwrite the layers' pose (measured in the (e) demonstration, E: the hook wins);
     * the gate is the manager the same {@code handleAnimations} fetched moments ago
     * ({@code getManagerForId(instanceId)}, 5-15: one map lookup on the singleton cache), decided once
     * when that manager was built ({@link OreSpawnGeoReplacement#registerControllers}). No flag can
     * drift from it.
     */
    @Override
    public void setCustomAnimations(A animatable, long instanceId, AnimationState<A> animationState) {
        if (!animatable.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().isEmpty()) {
            return;
        }
        animatable.applyCustomAnimations(getAnimationProcessor(), animationState);
    }
}
