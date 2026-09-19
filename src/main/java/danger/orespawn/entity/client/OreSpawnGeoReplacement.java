package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.client.animation.AttackingFlag;
import danger.orespawn.entity.client.animation.ContractLayers;
import danger.orespawn.entity.client.animation.KeyframeLayer;
import danger.orespawn.entity.client.animation.LocomotionKind;
import danger.orespawn.entity.client.animation.TriggerMask;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.object.BakedAnimations;

/**
 * One replaced animatable per registry entry. GeckoLib keys its per-entity
 * animation state by entity id through this singleton, so the entity class
 * carries no cache, controllers, or GeckoLib interface. That per-entity state
 * lives in {@link OreSpawnAnimatableInstanceCache} and is registered with
 * {@link GeoReplacementCaches}, so it can be dropped when the entity leaves
 * the client level (OPT-029; before that, every entity id ever drawn kept its
 * manager for the session).
 *
 * <p>The static helpers below let a code-driven pose be written in the
 * classic {@code ModelPart} vocabulary (vanilla sign conventions, pivot
 * positions in the parent's frame) and translate it onto GeckoLib's internal
 * bone basis in exactly one place - THE HOOKS' BASIS MAPPING. The basis facts,
 * derived from GeckoLib 4.8.4 bytecode, re-derived for the entity frame by
 * TEST-015 and proven geometrically by the Slice 4b harness fixture ({@code
 * fixture_runtime_basis_yz}, see) and by the animation
 * leg of every landed hook (0 rad against the classic model):</p>
 *
 * <ul>
 *   <li>the converter writes a bone pivot {@code (x, 24 - y, z)} of the
 *       ModelPart's absolute pivot (the Bedrock convention the Queen's native rig
 *       uses) and the baker negates JSON pivot X
 *       ({@code BakedModelFactory$Builtin.constructBone} 95-116), so an internal
 *       pivot is {@code (-x, 24 - y, z)}: INTERNAL SPACE IS CLASSIC SPACE
 *       REFLECTED IN X AND Y - the baker's x negation is the classic chain's
 *       {@code scale(-1, -1, 1)} ({@code LivingEntityRenderer.render} 395-400),
 *       which the converter no longer cancels;</li>
 *   <li>conjugating a rotation through that reflection ({@code S = diag(-1, -1, 1)},
 *       a half turn about Z) keeps the ZYX axis order, reverses the sense of the
 *       rotations about X and Y and keeps Z: internal rotation =
 *       {@code (-xRot, -yRot, zRot)} (before TEST-015, with the reflection in Y
 *       alone, it was {@code (-xRot, yRot, -zRot)}: a mirror in x reverses the
 *       sense of the rotations about Y and Z);</li>
 *   <li>{@code RenderUtil.translateMatrixToBone} (offsets 2-23) translates by
 *       {@code (-posX, posY, posZ)/16} in internal space, and a classic pivot
 *       move {@code (dx, dy, dz)} is the internal offset {@code (-dx, -dy, dz)},
 *       so {@code posX = dx, posY = -dy, posZ = dz} (before: {@code posX = -dx}:
 *       the mirror reverses the sign of an x position).</li>
 * </ul>
 * <p>No descriptor's hook statements change with the frame: every hook writes
 * classic terms through these helpers, and only this mapping carries the
 * frame.</p>
 */
public abstract class OreSpawnGeoReplacement<E extends Entity> implements GeoReplacedEntity {
    private final GeoReplacementDescriptor<E> descriptor;
    /**
     * OPT-029: the cache GeckoLib would pick for this animatable anyway — a
     * {@code GeoReplacedEntity} is a {@code SingletonGeoAnimatable}, whose
     * {@code animatableCacheOverride()} returns {@code new SingletonAnimatableInstanceCache(this)}
     * (4.8.4 bytecode, offsets 0-8), which {@code GeckoLibUtil.createInstanceCache} hands back
     * at offsets 1-12 before its Entity / BlockEntity test — made explicit as the evictable
     * subclass so the client evictor can drop a manager when its entity leaves
     * the level. NOT registered here: the headless s4 probe constructs the shipped
     * replacements in an un-bootstrapped JVM ({@code S4CandidateRuntime.instantiate}),
     * where the descriptor's entity-type supplier cannot be evaluated (it trips
     * {@code Bootstrap.checkBootstrapCalled} through {@code ModEntities}). The renderer
     * that owns this replacement registers it ({@link OreSpawnGeoReplacedEntityRenderer}),
     * on a bootstrapped client, through {@link #animatableCache()}.
     */
    private final OreSpawnAnimatableInstanceCache animCache = new OreSpawnAnimatableInstanceCache(this);

    protected OreSpawnGeoReplacement(GeoReplacementDescriptor<E> descriptor) {
        this.descriptor = descriptor;
    }

    public final GeoReplacementDescriptor<E> descriptor() {
        return this.descriptor;
    }

    /** OPT-029: the evictable cache, for the renderer that registers it with {@link GeoReplacementCaches}. */
    public final OreSpawnAnimatableInstanceCache animatableCache() {
        return this.animCache;
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
     * Two motion sources per species (Phase G slice (e) as decided
     * 2026-09-06, {@code phase_g_reports/animation_contract/contract_design.md}
     * section 6): the species' keyframe layers, one per frequency group of
     * the standard animation contract, or empty for a species animated by its
     * classic code only (every species but the Beaver, which declares its
     * three groups and, since item 15 landed on 2026-09-12, ships its
     * transcription - {@code idle}, {@code walk}, {@code walk_teeth}, {@code
     * walk_tail} - so its layers register under the default keys). A layer
     * registers only when the species' loaded clip file carries {@code idle} AND
     * {@code walk} (one without the other stays classic) and
     * {@code [modern] artistAnimations} says so ({@link
     * #registerKeyframeLayers}); the per-entity manager then decides the
     * source once: layers registered - the artist source, the classic hook stands down
     * ({@link OreSpawnGeoReplacementModel#setCustomAnimations}); none - the classic hook poses, as it always has.
     */
    public List<KeyframeLayer> keyframeLayers() {
        return List.of();
    }

    /**
     * THE WEIGHTS SLICE: the SPEC's {@code locomotion} word ({@code
     * tools/artist_specs/<registry>.json}), so the contract's {@code flying} input is what section 3 defines
     * - the SPEC's flyer AND {@code !onGround()}. {@link LocomotionKind#WALKER} unless a species' descriptor says
     * otherwise (the seven flyers, the Gold Fish and the T-shirt among the transcription species do).
     */
    public LocomotionKind locomotion() {
        return LocomotionKind.WALKER;
    }

    /**
     * How the species' synched attacking flag reads on the client, as the SPEC's trigger inventory classifies it
     * ({@link AttackingFlag}); {@link AttackingFlag#NONE} unless a descriptor wires its accessor ({@link #attacking}).
     */
    public AttackingFlag attackingFlag() {
        return AttackingFlag.NONE;
    }

    /** The species' attacking flag through its pose-style accessor (contract section 3); 0 where it has none. */
    protected int attacking(E entity) {
        return 0;
    }

    /** Contract section 3: a SPEC may narrow {@code inWater} from {@code isInWater()} to {@code isUnderWater()}. */
    protected boolean underwaterOnly() {
        return false;
    }

    /**
     * Contract section 5.3's proposed {@code boneResetTime}, 3 ticks ({@link TriggerMask#BONE_RESET_TICKS}): the
     * length of GeckoLib's lerp back to bind for a bone no controller wrote this frame - the blend-out of a
     * triggered clip's bones. Nothing else ever reaches that lerp: a transcription's layers and a classic hook
     * write their bones every frame, and a bone no source ever writes lerps from bind to bind.
     */
    @Override
    public double getBoneResetTime() {
        return TriggerMask.BONE_RESET_TICKS;
    }

    /**
     * The frame's locomotion state, read ONCE per frame from the drawn entity into the plain record
     * ({@link MotionInputs}; contract section 3): {@code flying} = the SPEC's flyer AND {@code !onGround()}, or
     * {@code FlyingAnimal.isFlying()} where the species implements it; {@code inWater} = {@code isInWater()}, or
     * {@code isUnderWater()} where {@link #underwaterOnly} says so; {@code hurtTime} and {@code deathTime} the
     * living entity's own fields (0 for a non-living species); {@code attacking} through {@link #attacking}.
     */
    public final MotionInputs motionInputs(AnimationState<?> state) {
        E entity = entity(state);
        boolean flying = entity instanceof FlyingAnimal flyer ? flyer.isFlying() : locomotion().flyer() && !entity.onGround();
        boolean inWater = underwaterOnly() ? entity.isUnderWater() : entity.isInWater();
        int hurtTime = 0;
        int deathTime = 0;
        if (entity instanceof LivingEntity living) {
            hurtTime = living.hurtTime;
            deathTime = living.deathTime;
        }
        return new MotionInputs(entity, ageInTicks(entity, state), limbSwingAmount(state), inWater, flying,
                attacking(entity), hurtTime, deathTime);
    }

    /**
     * The weighted contract over an ARTIST file for this species' declared groups ({@link ContractLayers#build}),
     * below the gates of {@link #registerKeyframeLayers}: the harness builds it on explicit readers.
     */
    public final ContractLayers contractLayers(BakedAnimations clips) {
        return ContractLayers.build(keyframeLayers(), clips, locomotion(), attackingFlag());
    }

    /**
     * Once per frame, before GeckoLib's controllers ({@link OreSpawnGeoReplacementModel#handleAnimations}): the
     * frame's {@link #motionInputs} handed to the manager's weighted contract - nothing for a manager on the
     * transcription's always-on layers or on the classic source.
     */
    public final void beginContractFrame(AnimatableManager<?> manager, AnimationState<?> state) {
        ContractLayers contract = ContractLayers.of(manager);
        if (contract != null) {
            contract.beginFrame(manager, motionInputs(state));
        }
    }

    /**
     * GeckoLib builds the per-entity {@code AnimatableManager} on the client's
     * render thread ({@code getManagerForId} -> the manager constructor ->
     * this, 4.8.4 offsets 27-55) and never rebuilds it while the entity stays
     * in the level (OPT-029 evicts it on leave), so the decision here is the
     * construction snapshot the config key documents.
     *
     * <p>FINAL: {@link #registerKeyframeLayers} over {@link #loadedClips()}
     * is the single self-gating path for every species; a species' only lever
     * is {@link #keyframeLayers()} - empty (every species but the Beaver today)
     * is the classic source and registers nothing. The thirteen empty
     * per-species overrides that predated the layers were deleted with the seal;
     * {@code KeyframeLegTests.kf_007} pins the presented state (every
     * replacement's layers empty but the Beaver's three).</p>
     */
    @Override
    public final void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        registerKeyframeLayers(controllers, loadedClips());
    }

    /**
     * GeckoLib's bake of this species' animation resource - what
     * {@code GeoModel.getAnimation} reads (4.8.4 offsets 6-18:
     * {@code GeckoLibCache.getBakedAnimations().get(resource)}) - or
     * {@code null} where no bake exists: before the first resource reload, or
     * on a dedicated server, whose cache map is the empty one the class
     * initialiser installs. {@code null} is the classic source.
     */
    protected final BakedAnimations loadedClips() {
        return GeckoLibCache.getBakedAnimations().get(this.descriptor.animationResource());
    }

    /**
     * Registers this species' {@link #keyframeLayers()} whose clips are present in
     * {@code clips}, and returns how many were registered (0 = the classic source).
     * Self-gated by clip presence (Q1 (a); the gate as decided 2026-09-12, item
     * 12): nothing registers unless the file carries BOTH {@link
     * KeyframeLayer#IDLE} and {@link KeyframeLayer#WALK} - the artist gate opens on
     * idle and walk delivered together; one without the other stays classic, and
     * the package checker says so - and then a layer whose own clip is missing is
     * skipped, its bones holding bind (contract section 2.4's fallback). Only
     * after that is the config asked ({@link
     * OreSpawnConfig#artistAnimations(EntityType)}: the modern master, the key,
     * the exclusion list). The entity readers handed to the
     * controllers are this replacement's own ({@link #ageInTicks} on the drawn
     * entity, {@link #limbSwingAmount} from the
     * renderer's state); the headless harness builds the same layers on explicit inputs through {@link KeyframeLayer#controller}.
     *
     * <p>THE WEIGHTS SLICE: behind the same gates the FILE decides the form ({@link
     * ContractLayers#isTranscription}) - the generator's transcription (an {@code
     * idle} keying no bone plus the declared groups' walk-family clips, or a
     * subset) registers the always-on layers below, exactly as before the slice,
     * bit-exact to the classic hook; any other file is an artist delivery and
     * registers the weighted contract ({@link #contractLayers}: the triggered clips'
     * controller first, then one weighted layer per group and present state,
     * {@link ContractLayers#register}) - the count returned is then the number of
     * controllers added.</p>
     */
    public final int registerKeyframeLayers(AnimatableManager.ControllerRegistrar controllers, BakedAnimations clips) {
        List<KeyframeLayer> layers = keyframeLayers();
        if (layers.isEmpty() || clips == null) {
            return 0;
        }
        if (clips.getAnimation(KeyframeLayer.IDLE) == null || clips.getAnimation(KeyframeLayer.WALK) == null) {
            return 0;
        }
        if (!OreSpawnConfig.artistAnimations(this.descriptor.entityType())) {
            return 0;
        }
        if (!ContractLayers.isTranscription(clips, layers)) {
            // THE WEIGHTS SLICE: an ARTIST file - an idle that keys a bone, a fly / swim / idle_<group> /
            // aggro_idle / calm_idle, a triggered clip - plays the weighted contract (ContractLayers); the
            // generator's transcription file, or a subset of it, takes the loop below unchanged: its groups
            // always-on, bit-exact to the classic hook (the keyframe leg is the proof).
            return contractLayers(clips).register(controllers, this, this::ageTicks);
        }
        int registered = 0;
        for (KeyframeLayer layer : layers) {
            if (!ContractLayers.playableLoop(layer.clip(), clips.getAnimation(layer.clip()))) {
                continue;
            }
            controllers.add(layer.controller(this, this::ageTicks, OreSpawnGeoReplacement::limbSwingAmount));
            registered++;
        }
        return registered;
    }

    /** The drawn entity's age for the phase-locked layers: vanilla {@code getBob}, as the classic hook reads it. */
    private float ageTicks(AnimationState<?> state) {
        return ageInTicks(entity(state), state);
    }

    /**
     * Code-driven pose hook, called by the shared model from
     * {@code GeoModel.setCustomAnimations} after keyframe controllers have
     * run - and only while the per-entity manager holds no controllers (the
     * classic source; {@link OreSpawnGeoReplacementModel#setCustomAnimations}).
     * The default adapts the renderer's state into {@link PoseInputs}
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

    /** {@code part.yRot = yRot} in classic terms (internal Y is the negated classic Y: the class javadoc's basis facts). */
    protected static void rotateY(AnimationProcessor<?> processor, String name, float yRot) {
        GeoBone bone = bone(processor, name);
        bone.setRotY(-yRot);
        bone.markRotationAsChanged();
    }

    /** {@code part.zRot = zRot} in classic terms (internal Z keeps the classic sense: the class javadoc's basis facts). */
    protected static void rotateZ(AnimationProcessor<?> processor, String name, float zRot) {
        GeoBone bone = bone(processor, name);
        bone.setRotZ(zRot);
        bone.markRotationAsChanged();
    }

    /**
     * The bone's bind pivot in classic terms, in its parent's frame (a
     * ModelPart's {@code x/y/z} is local to the parent pivot; GeckoLib pivots
     * are absolute, so the parent's internal pivot is subtracted). The internal
     * pivot is {@code (-x, 24 - y, z)} of the classic absolute pivot (the basis facts).
     */
    private static float[] classicBindPivot(GeoBone bone) {
        GeoBone parent = bone.getParent();
        if (parent == null) {
            return new float[] {-bone.getPivotX(), 24.0F - bone.getPivotY(), bone.getPivotZ()};
        }
        return new float[] {
                parent.getPivotX() - bone.getPivotX(),
                parent.getPivotY() - bone.getPivotY(),
                bone.getPivotZ() - parent.getPivotZ(),
        };
    }

    /** The bone's current classic position {@code (x, y, z)}: bind pivot plus any runtime move. */
    protected static float[] classicPosition(GeoBone bone) {
        float[] bind = classicBindPivot(bone);
        return new float[] {
                bind[0] + bone.getPosX(),
                bind[1] - bone.getPosY(),
                bind[2] + bone.getPosZ(),
        };
    }

    /**
     * {@code part.xRot} read back in classic terms for a part the classic never writes: the bind the bake gave the bone
     * (internal X is the negated classic X, {@link #rotateX}'s mapping; no controller runs on the classic source and
     * GeckoLib holds an untouched bone at its bake). Lifted into the base by the sixth Tier-2 slice (T2f, 2026-09-15) from
     * the Spit Bug's, Scorpion's and Sea Viper's descriptors, which each carried it privately (the design's rule: a bind
     * read other landed rigs share belongs to the base).
     */
    protected static float classicRotX(GeoBone bone) {
        return -bone.getRotX();
    }

    /**
     * {@code part.yRot} read back in classic terms (internal Y is the negated classic Y, {@link #rotateY}'s mapping): the
     * value the bone holds as this frame begins - the bind for a part the classic never writes, the PREVIOUS FRAME's write
     * for a part the classic accumulates on (GeckoLib's tickAnimation resets only a bone no hook wrote this tick, so a bone
     * written every frame keeps its value between frames as a classic ModelPart does). The remainder slice (2026-09-15):
     * the Princess's three power orbs, whose rotations accumulate per rendered frame on the model's own parts.
     */
    protected static float classicRotY(GeoBone bone) {
        return -bone.getRotY();
    }

    /** {@code part.zRot} read back in classic terms (internal Z keeps the classic sense, {@link #rotateZ}'s mapping); see {@link #classicRotY}. */
    protected static float classicRotZ(GeoBone bone) {
        return bone.getRotZ();
    }

    /** {@code part.x = x; part.y = y; part.z = z} in classic terms (the offset's x and z keep the classic sign, y is negated: the basis facts). */
    protected static void moveTo(AnimationProcessor<?> processor, String name, float x, float y, float z) {
        GeoBone bone = bone(processor, name);
        float[] bind = classicBindPivot(bone);
        bone.setPosX(x - bind[0]);
        bone.setPosY(bind[1] - y);
        bone.setPosZ(z - bind[2]);
        bone.markPositionAsChanged();
    }

    /** {@code part.visible = visible}. GeckoLib hides the bone's cubes and, like vanilla, its children. */
    protected static void setVisible(AnimationProcessor<?> processor, String name, boolean visible) {
        bone(processor, name).setHidden(!visible);
    }
}
