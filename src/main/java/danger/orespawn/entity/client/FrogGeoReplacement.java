package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Frog;
import danger.orespawn.entity.pose.FrogPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Frog (the hooks, owner 2026-09-14, addendum item 10; landed by the sixth Tier-2 slice T2f, 2026-09-15, the
 * owner's closing set item 4): {@link ModelFrog#poseFrom} verbatim on the converted rig, ON THE HOOK (Amendment 2 to
 * Amendment 1: no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk} ). Wingspeed 1.0f (orig ModelFrog.java:14,27 / ClientProxyOreSpawn.java:512): the THRESHOLD idiom on the
 * front legs and lower hind legs about Y - above a walking speed of a tenth
 * {@code cos(age * ws * 1.4f) * PI * 0.55f * limbSwingAmount} , the right side the negative, the hind pair at half, 0 at
 * or below it (orig :133-141); the SINGING branch on the jaw - a 0.85 ws cosine x 0.15 around 1.22 rad while singing, 1.22
 * otherwise (orig :102-103, read through {@link FrogPose} ); the JUMP branch on the upper hind legs' roll - +-2.44 rad
 * while the vertical motion exceeds a tenth either way, +-0.227 otherwise (orig :104-110, {@code getDeltaMovement().y} );
 * and the POSITION-write idiom (through {@link #moveTo} ) - each lower hind leg's pivot FOLLOWS its upper leg 9 units
 * along (cos, sin) of the roll (orig :111-114). The upper legs' pivots are never written (the bind), read through
 * {@link #classicPosition} ; the rolls the classic reads back are held in locals; the followers keep their bind x.
 * <p>Scale and shadow follow {@link FrogRenderer} : 1.0 render scale, halved for a baby, and a 0.35 x 1.0 shadow
 * (ENT-S-092; orig RenderFrog.java:23-24, 39-40 - the 1.7.10 renderer scaled by 1.0 unconditionally; the port's renderer
 * halves a baby, and the port's renderer is what this follows). The descriptor scales by {@link FrogRenderer#SCALE} (made
 * public by the landing slice T2f; the hook lanes passed an equal literal while it was private - the T2d form).</p>
 */
public final class FrogGeoReplacement extends OreSpawnGeoReplacement<Frog> {
    /** orig ModelFrog.java:14,27 {@code wingspeed} = 1.0f (ClientProxyOreSpawn.java:512): the chain's second multiply. */
    static final float WINGSPEED = 1.0F;
    private static final GeoReplacementDescriptor<Frog> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.FROG.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Frog.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/frog.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/frog.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/frogtexture.png"),
            FrogRenderer.SHADOW) {
        @Override
        public void applyScale(Frog entity, PoseStack poseStack, float partialTick) {
            if (entity.isBaby()) {
                poseStack.scale(FrogRenderer.SCALE / 2.0F, FrogRenderer.SCALE / 2.0F, FrogRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(FrogRenderer.SCALE, FrogRenderer.SCALE, FrogRenderer.SCALE);
        }
    };

    public FrogGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        FrogPose entity = inputs.subject(FrogPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelFrog.poseFrom (the body of the classic setupAnim) verbatim.
        float newangle = limbSwingAmount > 0.1F
                ? Mth.cos(ageInTicks * WINGSPEED * 1.4F) * (float) Math.PI * 0.55F * limbSwingAmount
                : 0.0F;
        rotateY(processor, "lfleg", newangle);
        rotateY(processor, "rfleg", -newangle);
        rotateY(processor, "lleg2", -newangle / 2.0F);
        rotateY(processor, "rleg2", newangle / 2.0F);

        newangle = entity.getSinging() != 0
                ? Mth.cos(ageInTicks * 0.85F * WINGSPEED) * (float) Math.PI * 0.15F
                : 0.0F;
        rotateX(processor, "jaw", newangle + 1.22F);

        float lleg1ZRot;
        float rleg1ZRot;
        if (entity.getDeltaMovement().y > 0.1 || entity.getDeltaMovement().y < -0.1) {
            lleg1ZRot = 2.44F;
            rleg1ZRot = -2.44F;
        } else {
            lleg1ZRot = 0.227F;
            rleg1ZRot = -0.227F;
        }
        rotateZ(processor, "lleg1", lleg1ZRot);
        rotateZ(processor, "rleg1", rleg1ZRot);

        float[] lleg1 = classicPosition(bone(processor, "lleg1"));  // never written: the bind pivot
        float lleg2Y = lleg1[1] - (float) Math.cos(lleg1ZRot) * 9.0F;
        float lleg2Z = lleg1[2] + (float) Math.sin(lleg1ZRot) * 9.0F;
        moveYZ(processor, "lleg2", lleg2Y, lleg2Z);
        float[] rleg1 = classicPosition(bone(processor, "rleg1"));  // never written: the bind pivot
        float rleg2Y = rleg1[1] - (float) Math.cos(rleg1ZRot) * 9.0F;
        float rleg2Z = rleg1[2] + (float) Math.sin(rleg1ZRot) * 9.0F;
        moveYZ(processor, "rleg2", rleg2Y, rleg2Z);
    }

    /** {@code part.y = y; part.z = z} in classic terms: the classic writes y and z only, so x keeps the part's bind. */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Frog, FrogGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new FrogGeoReplacement());
        }
    }
}
