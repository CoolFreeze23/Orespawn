package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityRotator;
import danger.orespawn.entity.pose.RotatorPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Rotator (Slice 4c): {@link RotatorModel}'s gyroscope on the
 * render-instance-expanded rig. The classic draw loop renders each blade part
 * eight times at 45° Z steps inside a pose-stack rotation of the per-entity
 * angle {@code ri.rf1} about X, Y or Z ({@link RotatorModel#renderFan}, orig
 * ModelRotator.java:52-73). The converted rig carries that loop statically:
 * draw {@code k} of {@code shape1} is the clone bone {@code shape1__i<k>} (its
 * own Z rotation {@code k * FAN_STEP}, bind) under the fan group bone
 * {@code shape1__fan} (pivot at the model origin), so this hook only spins the
 * three group bones by {@code rf1} — X for shape1, Y for shape2, Z for shape3 —
 * and then advances {@code rf1} exactly where the classic model does (once per
 * rendered frame: GeckoLib 4.8.4's {@code GeoReplacedEntityRenderer.actuallyRender}
 * runs {@code GeoModel.handleAnimations} → {@code setCustomAnimations} only when
 * {@code isReRender} is false, bytecode offsets 579-718; {@code GeoRenderer.reRender}
 * passes {@code true}, offsets 12/36). Shadow is {@link RotatorRenderer#SHADOW}
 * (0.1 x 1.0, ENT-S-092).
 *
 * <p>Known edge: GeckoLib runs the hook even when the entity is invisible (the
 * draw is skipped at offset 748 only), whereas vanilla's LivingEntityRenderer
 * skips {@code renderToBuffer} — and with it the classic advance — for an
 * invisible mob. The fan angle is arbitrary accumulated state, never visible in
 * that case; recorded, not reproduced.</p>
  ENT-S-147 (REPORT, 2026-09-06): the advance below rides GeckoLib's per-frame animation dedup — with one
 * Rotator in view and the game paused, {@code handleAnimations} returns before {@code setCustomAnimations} and the
 * gyroscope freezes where the classic keeps spinning 2° per frame; the fix shape (a per-render-pass descriptor
 * hook for the advance, the pose kept pure) waits for the owner's ruling.
 */
public final class RotatorGeoReplacement extends OreSpawnGeoReplacement<EntityRotator> {
    /** The fan group bones the converter emits for {@code render_instances} with {@code step_scope: part}. */
    public static final String SHAPE1_FAN = "shape1__fan";
    public static final String SHAPE2_FAN = "shape2__fan";
    public static final String SHAPE3_FAN = "shape3__fan";

    private static final GeoReplacementDescriptor<EntityRotator> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_ROTATOR.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityRotator.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/rotator.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/rotator.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rotator.png"),
            RotatorRenderer.SHADOW) {
    };

    public RotatorGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        RenderInfo ri = inputs.subject(RotatorPose.class).getRenderInfo();
        // orig ModelRotator.java:52, 60, 68 — the same rf1 spins the three fans about X, Y and Z.
        float spin = RotatorModel.fanSpinRadians(ri.rf1);
        rotateX(processor, SHAPE1_FAN, spin);
        rotateY(processor, SHAPE2_FAN, spin);
        rotateZ(processor, SHAPE3_FAN, spin);
        // orig ModelRotator.java:75-78 — after the draw, advance 2° per rendered frame, wrap at 359°.
        RotatorModel.advanceFanSpin(ri);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityRotator, RotatorGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RotatorGeoReplacement());
        }
    }
}
