package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityStinkBug;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Stink Bug (the hooks, landed by the fifth Tier-2 slice T2e): {@link StinkBugModel#setupAnim} verbatim on the converted rig, ON THE HOOK (no
 * keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.75f
 * (orig ModelStinkBug.java:13,66 / ClientProxyOreSpawn.java:453), every channel a {@code Mth.sin}: the
 * GAIT-scaled legs about X at {@code sin(age x 3.1 x ws) x PI x 0.3 x limbSwingAmount} (f1 and f3 positive; f2, f4 and
 * f6 the negative; f5 untouched; no threshold); the two antennae about Z on 0.4 x ws at 0.2 x PI, mirrored; the jaw
 * about X on 0.2 x ws at 0.04 x PI about 0.18 rad; the two horns about X (0.4 / 0.46 x ws) and Y (0.43 / 0.49 x ws) at
 * 0.15 x PI about 0.52 and -+0.3 rad; and the tail's twenty-three parts about X on one 0.1 x ws sine at 0.1 x PI
 * about -0.2 rad.
 *
 * <p>Scale and shadow follow {@link StinkBugRenderer}: 0.85 render scale and a 0.35 x 0.85 shadow (0.2975, ENT-S-092).</p>
 */
public final class StinkBugGeoReplacement extends OreSpawnGeoReplacement<EntityStinkBug> {
    /** orig ModelStinkBug.java:13,66 {@code wingspeed} = 0.75f (ClientProxyOreSpawn.java:453): the chain's third multiply. */
    static final float WINGSPEED = 0.75F;
    private static final GeoReplacementDescriptor<EntityStinkBug> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_STINK_BUG.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityStinkBug.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/stinkbug.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/stinkbug.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/stinkbug.png"),
            StinkBugRenderer.SHADOW) {
        @Override
        public void applyScale(EntityStinkBug entity, PoseStack poseStack, float partialTick) {
            // orig RenderStinkBug.preRenderScale: GL11.glScalef(scale, scale, scale) (StinkBugRenderer.scale)
            poseStack.scale(StinkBugRenderer.SCALE, StinkBugRenderer.SCALE, StinkBugRenderer.SCALE);
        }
    };

    public StinkBugGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // StinkBugModel.setupAnim verbatim, the float chain left to right (the classic's chained assignments unrolled
        // in its order; its second write of f3 stores the same value and is written once).
        float newangle = 0.0f;
        newangle = Mth.sin(ageInTicks * 3.1f * WINGSPEED) * (float) Math.PI * 0.3f * limbSwingAmount;
        rotateX(processor, "f3", newangle);
        rotateX(processor, "f1", newangle);
        rotateX(processor, "f6", -newangle);
        rotateX(processor, "f4", -newangle);
        rotateX(processor, "f2", -newangle);
        newangle = Mth.sin(ageInTicks * 0.4f * WINGSPEED) * (float) Math.PI * 0.2f;
        rotateZ(processor, "b9", newangle);
        rotateZ(processor, "b10", -newangle);
        newangle = Mth.sin(ageInTicks * 0.2f * WINGSPEED) * (float) Math.PI * 0.04f;
        rotateX(processor, "jaw", 0.18f + newangle);
        rotateX(processor, "h1", 0.52f + Mth.sin(ageInTicks * 0.4f * WINGSPEED) * (float) Math.PI * 0.15f);
        rotateY(processor, "h1", -0.3f + Mth.sin(ageInTicks * 0.43f * WINGSPEED) * (float) Math.PI * 0.15f);
        rotateX(processor, "h2", 0.52f + Mth.sin(ageInTicks * 0.46f * WINGSPEED) * (float) Math.PI * 0.15f);
        rotateY(processor, "h2", 0.3f + Mth.sin(ageInTicks * 0.49f * WINGSPEED) * (float) Math.PI * 0.15f);
        float tailX = -0.2f + Mth.sin(ageInTicks * 0.1f * WINGSPEED) * (float) Math.PI * 0.1f;
        rotateX(processor, "tail", tailX);
        rotateX(processor, "t5", tailX);
        rotateX(processor, "t4", tailX);
        rotateX(processor, "t3", tailX);
        rotateX(processor, "t2", tailX);
        rotateX(processor, "t1", tailX);
        rotateX(processor, "t10", tailX);
        rotateX(processor, "t9", tailX);
        rotateX(processor, "t8", tailX);
        rotateX(processor, "t7", tailX);
        rotateX(processor, "t6", tailX);
        rotateX(processor, "t15", tailX);
        rotateX(processor, "t14", tailX);
        rotateX(processor, "t13", tailX);
        rotateX(processor, "t12", tailX);
        rotateX(processor, "t11", tailX);
        rotateX(processor, "t20", tailX);
        rotateX(processor, "t19", tailX);
        rotateX(processor, "t18", tailX);
        rotateX(processor, "t17", tailX);
        rotateX(processor, "t16", tailX);
        rotateX(processor, "t22", tailX);
        rotateX(processor, "t21", tailX);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityStinkBug, StinkBugGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new StinkBugGeoReplacement());
        }
    }
}
