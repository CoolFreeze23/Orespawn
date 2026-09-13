package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TRex;
import danger.orespawn.entity.pose.TRexPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib T-Rex (the hooks, owner 2026-09-14, addendum item 10): {@link ModelTRex#poseFrom} verbatim on the converted
 * rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk}; the landing slice adds the geo, the wiring and the proofs). The port's classic model as
 * it is, {@code ANIM_SPEED} 1.0f (orig ClientProxyOreSpawn.java:417 passes {@code new ModelTRex(0.2f)}; the port's model
 * is what the hook transcribes): the THRESHOLD idiom on the eight leg parts' pitch - above a walking speed of a tenth
 * ({@code limbSwingAmount > 0.1f}, the float compare) {@code cos(age x 1.3) x PI x 0.25 x amount}, 0 at or below it -
 * around the rest pitches -0.174 / 0.506 / -0.401 / 0, the left leg mirrored; the ATTACKING branch on the jaw
 * ({@code getAttacking() != 0}: 0.52 + a 0.45 cosine x PI x 0.18, else 0.1); and the arms' 0.1 cosine x PI x 0.05
 * around -0.523 (the classic's {@code leftArm} / {@code rightArm} fields are the rig's {@code shape17} / {@code shape11}
 * parts). The entity is read through {@link TRexPose} (the Slice 4b doctrine).
 *
 * <p>Scale and shadow follow {@link TRexRenderer}: 1.2 render scale (its private {@code SCALE}, applied around
 * {@code super.render} - the literal) and a 1.0 x 1.2 shadow (ENT-S-092). No zero-thickness cube.</p>
 */
public final class TRexGeoReplacement extends OreSpawnGeoReplacement<TRex> {
    /** The port's {@code ModelTRex.ANIM_SPEED} = 1.0f: the chain's frequency multiplier. */
    static final float ANIM_SPEED = 1.0F;
    /** TRexRenderer's private {@code SCALE} = 1.2f (orig RenderTRex.scale, ClientProxyOreSpawn.java:417): the equal literal. */
    static final float SCALE = 1.2f;
    private static final GeoReplacementDescriptor<TRex> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.TREX.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            TRex.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/trex.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/trex.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/trex.png"),
            TRexRenderer.SHADOW) {
        @Override
        public void applyScale(TRex entity, PoseStack poseStack, float partialTick) {
            // TRexRenderer.render (orig RenderTRex.preRenderScale :39-45): poseStack.scale(SCALE, SCALE, SCALE)
            poseStack.scale(SCALE, SCALE, SCALE);
        }
    };

    public TRexGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        TRexPose entity = inputs.subject(TRexPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelTRex.poseFrom verbatim.
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * 1.3F * ANIM_SPEED) * (float) Math.PI * 0.25F * limbSwingAmount
                : 0.0F;

        rotateX(processor, "rightleg", -0.174F + newangle);
        rotateX(processor, "rightleg2", 0.506F + newangle);
        rotateX(processor, "rightleg3", -0.401F + newangle);
        rotateX(processor, "rightleg4", newangle);

        rotateX(processor, "leftleg", -0.174F - newangle);
        rotateX(processor, "leftleg2", 0.506F - newangle);
        rotateX(processor, "leftleg3", -0.401F - newangle);
        rotateX(processor, "leftleg4", -newangle);

        rotateX(processor, "jaw", entity.getAttacking() != 0
                ? 0.52F + Mth.cos(ageInTicks * 0.45F) * (float) Math.PI * 0.18F
                : 0.1F);

        rotateX(processor, "shape17", -0.523F + Mth.cos(ageInTicks * 0.1F) * (float) Math.PI * 0.05F);   // leftArm
        rotateX(processor, "shape11", -0.523F + Mth.cos(ageInTicks * 0.1F) * (float) Math.PI * 0.05F);   // rightArm
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<TRex, TRexGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new TRexGeoReplacement());
        }
    }
}
