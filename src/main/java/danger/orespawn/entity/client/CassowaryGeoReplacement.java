package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Cassowary;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cassowary (the hooks, owner 2026-09-14, addendum item 10; landed by the fourth Tier-2 slice T2d, 2026-09-14,
 * the owner's item 9): {@link ModelCassowary#setupAnim} verbatim on the
 * converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays closed
 * until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.55f (orig ModelCassowary.java:14,29 /
 * ClientProxyOreSpawn.java:469): the THRESHOLD idiom - above a walking speed of a tenth the two legs and two feet pitch on
 * {@code cos(age * 1.3f * ws) * PI * 0.15f * limbSwingAmount} (one pair the negative) and the neck / gobbler on
 * {@code cos(age * 2.6f * ws) * PI * 0.1f * limbSwingAmount} around -2.827 / 0 rad, both 0 at or below it (orig :147-167);
 * and the POSITION-write idiom (through {@link #moveTo}) - the crest, beak and head pivots FOLLOW the neck 7 units along
 * (sin, cos) of its pitch (orig :168-173). The neck's pivot is never written (the bind), read through
 * {@link #classicPosition}; the neck's pitch the classic reads back is held in a local; the three followers keep their bind x.
 *
 * <p>Scale and shadow follow {@link CassowaryRenderer}: {@link CassowaryRenderer#SCALE} (1.0, lifted to public by the
 * landing slice so both renderers read the one constant), halved for a baby, and a 0.5 x 1.0 shadow (ENT-S-092; orig
 * RenderCassowary.java:23-24, 39-44) - the shadow a constructor literal there, so the equal literal here.</p>
 */
public final class CassowaryGeoReplacement extends OreSpawnGeoReplacement<Cassowary> {
    /** orig ModelCassowary.java:14,29 {@code wingspeed} = 0.55f (ClientProxyOreSpawn.java:469): the chain's third multiply. */
    static final float WINGSPEED = 0.55F;
    private static final GeoReplacementDescriptor<Cassowary> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CASSOWARY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Cassowary.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cassowary.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cassowary.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cassowary.png"),
            // orig RenderCassowary.java:23 super(model, par2 * par3) with 0.5f x 1.0f: CassowaryRenderer's constructor passes
            // the literal 0.5f (it declares no SHADOW constant)
            0.5F) {
        @Override
        public void applyScale(Cassowary entity, PoseStack poseStack, float partialTick) {
            // orig RenderCassowary.preRenderScale (:39-44): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            // (CassowaryRenderer.render: isBaby() ? SCALE / 2 : SCALE)
            if (entity.isBaby()) {
                poseStack.scale(CassowaryRenderer.SCALE / 2.0F, CassowaryRenderer.SCALE / 2.0F, CassowaryRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(CassowaryRenderer.SCALE, CassowaryRenderer.SCALE, CassowaryRenderer.SCALE);
        }
    };

    public CassowaryGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // ModelCassowary.setupAnim verbatim, the float chain left to right.
        float newangle = 0.0F;
        float newangle2 = 0.0F;
        if (limbSwingAmount > 0.1F) {
            newangle = Mth.cos(ageInTicks * 1.3F * WINGSPEED) * (float) Math.PI * 0.15F * limbSwingAmount;
            newangle2 = Mth.cos(ageInTicks * 2.6F * WINGSPEED) * (float) Math.PI * 0.1F * limbSwingAmount;
        } else {
            newangle2 = 0.0F;
            newangle = 0.0F;
        }
        rotateX(processor, "leg1", newangle);
        rotateX(processor, "foot2", newangle);
        rotateX(processor, "leg2", -newangle);
        rotateX(processor, "foot1", -newangle);
        float neckXRot = -2.827F + newangle2;
        rotateX(processor, "neck", neckXRot);
        rotateX(processor, "gobbler", newangle2);
        float[] neck = classicPosition(bone(processor, "neck"));  // never written: the bind pivot
        float crestZ = neck[2] + Mth.sin(neckXRot) * 7.0F;
        float beakZ = crestZ;
        float headZ = beakZ;
        float crestY = neck[1] + Mth.cos(neckXRot) * 7.0F;
        float beakY = crestY;
        float headY = beakY;
        moveYZ(processor, "crest", crestY, crestZ);
        moveYZ(processor, "beak", beakY, beakZ);
        moveYZ(processor, "head", headY, headZ);
    }

    /** {@code part.y = y; part.z = z} in classic terms: the classic writes y and z only, so x keeps the part's bind. */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, current[0], y, z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Cassowary, CassowaryGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CassowaryGeoReplacement());
        }
    }
}
