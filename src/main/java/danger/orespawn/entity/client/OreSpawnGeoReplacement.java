package danger.orespawn.entity.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * One replaced animatable per registry entry. GeckoLib keys its per-entity
 * animation state by entity id through this singleton, so the entity class
 * carries no cache, controllers, or GeckoLib interface.
 *
 * <p>The static helpers below let a code-driven pose be written in the
 * classic {@code ModelPart} vocabulary (vanilla sign conventions, pivot
 * positions in the parent's frame) and translate it onto GeckoLib's internal
 * bone basis in exactly one place. The basis facts, derived from GeckoLib
 * 4.8.4 bytecode and proven geometrically by the Slice 4b harness fixture
 * ({@code fixture_runtime_basis_yz}, see FIX_LOG "PHASE G SLICE 4b"):</p>
 * <ul>
 *   <li>the converter writes bone pivot {@code (-x, 24 - y, z)} and the baker
 *       negates JSON pivot X, so an internal pivot is {@code (x, 24 - y, z)}
 *       of the ModelPart's absolute pivot: internal space is classic space
 *       reflected in Y;</li>
 *   <li>conjugating a rotation through that reflection negates the X and Z
 *       angles and keeps Y: internal rotation = {@code (-xRot, yRot, -zRot)};</li>
 *   <li>{@code RenderUtil.translateMatrixToBone} translates by
 *       {@code (-posX, posY, posZ)/16} in internal space, so a classic pivot
 *       move {@code (dx, dy, dz)} is {@code posX = -dx, posY = -dy, posZ = dz}.</li>
 * </ul>
 */
public abstract class OreSpawnGeoReplacement<E extends Entity> implements GeoReplacedEntity {
    private final GeoReplacementDescriptor<E> descriptor;
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    protected OreSpawnGeoReplacement(GeoReplacementDescriptor<E> descriptor) {
        this.descriptor = descriptor;
    }

    public final GeoReplacementDescriptor<E> descriptor() {
        return this.descriptor;
    }

    @Override
    public final EntityType<?> getReplacingEntityType() {
        return this.descriptor.entityType();
    }

    @Override
    public final AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    /**
     * ENT-S-094: per-species non-living render mode. {@code true} for a species
     * whose 1.7.10 renderer extended the plain {@code Render} even though the
     * entity was living (the Elevator: orig RenderElevator.java:19), so it never
     * had RendererLivingEntity's death flip, shaking/sleeping/upside-down
     * rotations, hurt red pass or name label. {@link OreSpawnGeoReplacedEntityRenderer}
     * reads it to skip the matching GeoReplacedEntityRenderer branches (the
     * living-only parts of applyRotations, getPackedOverlay, shouldShowName) so
     * the candidate matches the classic renderer. Default {@code false}: every
     * other species keeps GeckoLib's living behaviour.
     */
    public boolean nonLivingRender() {
        return false;
    }

    /**
     * Code-driven pose hook, called by the shared model from
     * {@code GeoModel.setCustomAnimations} after keyframe controllers have
     * run. The default adapts the renderer's state into {@link PoseInputs}
     * and calls {@link #applyCustomAnimations(AnimationProcessor, PoseInputs)};
     * the G1 Beaver overrides this form directly.
     */
    protected void applyCustomAnimations(AnimationProcessor<?> processor, AnimationState<?> state) {
        applyCustomAnimations(processor, PoseInputs.fromState(state));
    }

    /**
     * Code-driven pose in classic terms. Species that keep their classic trig
     * animation implement it here with the original formulas; species animated
     * purely by clips leave it. Consumes only plain inputs so the harness can
     * run the identical production code headlessly.
     */
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
    }

    /** Harness entry: the production hook on explicit inputs, bypassing GeckoLib's state. */
    public final void pose(AnimationProcessor<?> processor, PoseInputs inputs) {
        applyCustomAnimations(processor, inputs);
    }

    /** The entity being drawn, installed by the replaced renderer before animations run. */
    protected final E entity(AnimationState<?> state) {
        return this.descriptor.requireEntity(state.getData(DataTickets.ENTITY));
    }

    /** The entity being drawn; fails for a harness input that carries none. */
    protected final E entity(PoseInputs inputs) {
        return this.descriptor.requireEntity(inputs.subject());
    }

    /** Vanilla {@code LivingEntityRenderer#getBob}: integer tick count widened to float, then the partial tick added. */
    protected static float ageInTicks(Entity entity, AnimationState<?> state) {
        return (float) entity.tickCount + state.getPartialTick();
    }

    /**
     * The replaced renderer computes {@code limbSwingAmount} exactly as vanilla
     * {@code LivingEntityRenderer#render} does (walk speed clamped to 1, zero when
     * dead or seated on a vehicle), so it is read from the state, not re-derived.
     */
    protected static float limbSwingAmount(AnimationState<?> state) {
        return state.getLimbSwingAmount();
    }

    /** The named bone, or a wiring failure: a missing bone means the shipped geo drifted from the classic rig. */
    protected static GeoBone bone(AnimationProcessor<?> processor, String name) {
        GeoBone bone = processor.getBone(name);
        if (bone == null) {
            throw new IllegalStateException("GeckoLib rig is missing bone " + name);
        }
        return bone;
    }

    /** {@code part.xRot = xRot} in classic terms. */
    protected static void rotateX(AnimationProcessor<?> processor, String name, float xRot) {
        GeoBone bone = bone(processor, name);
        bone.setRotX(-xRot);
        bone.markRotationAsChanged();
    }

    /** {@code part.yRot = yRot} in classic terms. */
    protected static void rotateY(AnimationProcessor<?> processor, String name, float yRot) {
        GeoBone bone = bone(processor, name);
        bone.setRotY(yRot);
        bone.markRotationAsChanged();
    }

    /** {@code part.zRot = zRot} in classic terms. */
    protected static void rotateZ(AnimationProcessor<?> processor, String name, float zRot) {
        GeoBone bone = bone(processor, name);
        bone.setRotZ(-zRot);
        bone.markRotationAsChanged();
    }

    /**
     * The bone's bind pivot in classic terms, in its parent's frame (a
     * ModelPart's {@code x/y/z} is local to the parent pivot; GeckoLib pivots
     * are absolute, so the parent's internal pivot is subtracted).
     */
    private static float[] classicBindPivot(GeoBone bone) {
        GeoBone parent = bone.getParent();
        if (parent == null) {
            return new float[] {bone.getPivotX(), 24.0F - bone.getPivotY(), bone.getPivotZ()};
        }
        return new float[] {
                bone.getPivotX() - parent.getPivotX(),
                parent.getPivotY() - bone.getPivotY(),
                bone.getPivotZ() - parent.getPivotZ(),
        };
    }

    /** The bone's current classic position {@code (x, y, z)}: bind pivot plus any runtime move. */
    protected static float[] classicPosition(GeoBone bone) {
        float[] bind = classicBindPivot(bone);
        return new float[] {
                bind[0] - bone.getPosX(),
                bind[1] - bone.getPosY(),
                bind[2] + bone.getPosZ(),
        };
    }

    /** {@code part.x = x; part.y = y; part.z = z} in classic terms. */
    protected static void moveTo(AnimationProcessor<?> processor, String name, float x, float y, float z) {
        GeoBone bone = bone(processor, name);
        float[] bind = classicBindPivot(bone);
        bone.setPosX(bind[0] - x);
        bone.setPosY(bind[1] - y);
        bone.setPosZ(z - bind[2]);
        bone.markPositionAsChanged();
    }

    /** {@code part.visible = visible}. GeckoLib hides the bone's cubes and, like vanilla, its children. */
    protected static void setVisible(AnimationProcessor<?> processor, String name, boolean visible) {
        bone(processor, name).setHidden(!visible);
    }
}
