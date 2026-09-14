package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Peacock;
import danger.orespawn.entity.pose.PeacockPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Peacock (the hooks, landed by the sixth Tier-2 slice T2f): {@link ModelPeacock#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk} ; the geo, the wiring and the proofs landed with T2f). Wingspeed 0.75f (orig
 * ModelPeacock.java:14,33 / ClientProxyOreSpawn.java:478): the THRESHOLD idiom on the two legs' pitch -
 * above a walking speed of a tenth ({@code (double) limbSwingAmount > 0.1}) {@code cos(age x 1.3 ws) x PI x 0.15 x
 * amount} , mirrored, 0 at or below it - and the DISPLAY branch ({@code getBlink() > 0}): the three head feathers
 * pitched 0.401 / -0.174 / -0.698 and the seven tail feathers raised 1.047 rad and fanned -+0.4 / 0.8 / 1.2 about Z,
 * else the head feathers folded at -1.06 and every tail feather at 0 - constants written every frame. The entity is
 * read through {@link PeacockPose} (the Slice 4b doctrine).
 *
 * <p>Scale and shadow follow {@link PeacockRenderer} : {@code SCALE} 1.0 (identity) with a baby drawn at 0.5 through
 * its {@code render} wrapper (orig RenderPeacock.preRenderScale :39-45, children at scale / 2), and a 0.25 x 1.0 shadow
 * (ENT-S-092). The three head feathers and the seven tail feathers are zero-thickness cubes (the seam draws every cube
 * with its true transformed normal and its two coplanar faces in the classic order, ENT-S-161 / TEST-007 - below).</p>
 */
public final class PeacockGeoReplacement extends OreSpawnGeoReplacement<Peacock> {
    /** orig ModelPeacock.java:14,33 {@code wingspeed} = 0.75f (ClientProxyOreSpawn.java:478): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.75f;
    private static final GeoReplacementDescriptor<Peacock> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.PEACOCK.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Peacock.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/peacock.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/peacock.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/peacock.png"),
            PeacockRenderer.SHADOW) {
        @Override
        public void applyScale(Peacock entity, PoseStack poseStack, float partialTick) {
            // PeacockRenderer.render (orig RenderPeacock.preRenderScale :39-45): a child gets poseStack.scale(0.5f),
            // otherwise nothing (SCALE 1.0, never applied)
            if (entity.isBaby()) {
                poseStack.scale(0.5f, 0.5f, 0.5f);
            }
        }

        /**
         * A rig with zero-thickness cubes (the three head feathers 0 x 7 x 3, the seven tail feathers 8 x 0 x 30): the
         * within-cube face order decides a flat cube's z-fight, so the shipped geo carries the classic order
         * ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.
         */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public PeacockGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        PeacockPose entity = inputs.subject(PeacockPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelPeacock.poseFrom verbatim.
        float newangle = 0.0f;
        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos((float) (ageInTicks * 1.3f * WINGSPEED)) * (float) Math.PI * 0.15f * limbSwingAmount : 0.0f;
        rotateX(processor, "lleg", newangle);
        rotateX(processor, "rleg", -newangle);
        if (entity.getBlink() > 0) {
            rotateX(processor, "hf1", 0.401f);
            rotateX(processor, "hf2", -0.174f);
            rotateX(processor, "hf3", -0.698f);
            rotateX(processor, "tailf1", 1.047f);
            rotateX(processor, "tailf2", 1.047f);
            rotateX(processor, "tailf3", 1.047f);
            rotateX(processor, "tailf4", 1.047f);
            rotateX(processor, "tailf5", 1.047f);
            rotateX(processor, "tailf6", 1.047f);
            rotateX(processor, "tailf7", 1.047f);
            rotateZ(processor, "tailf1", -0.4f);
            rotateZ(processor, "tailf2", -0.8f);
            rotateZ(processor, "tailf3", -1.2f);
            rotateZ(processor, "tailf4", 0.4f);
            rotateZ(processor, "tailf5", 0.8f);
            rotateZ(processor, "tailf6", 1.2f);
        } else {
            rotateX(processor, "hf1", -1.06f);
            rotateX(processor, "hf2", -1.06f);
            rotateX(processor, "hf3", -1.06f);
            rotateX(processor, "tailf1", 0.0f);
            rotateX(processor, "tailf2", 0.0f);
            rotateX(processor, "tailf3", 0.0f);
            rotateX(processor, "tailf4", 0.0f);
            rotateX(processor, "tailf5", 0.0f);
            rotateX(processor, "tailf6", 0.0f);
            rotateX(processor, "tailf7", 0.0f);
            rotateZ(processor, "tailf1", 0.0f);
            rotateZ(processor, "tailf2", 0.0f);
            rotateZ(processor, "tailf3", 0.0f);
            rotateZ(processor, "tailf4", 0.0f);
            rotateZ(processor, "tailf5", 0.0f);
            rotateZ(processor, "tailf6", 0.0f);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Peacock, PeacockGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new PeacockGeoReplacement());
        }
    }
}
