package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Camarasaurus;
import danger.orespawn.entity.pose.CamarasaurusPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Camarasaurus (the hooks, owner 2026-09-14, addendum item 10; landed by the fourth Tier-2 slice T2d,
 * 2026-09-14, the owner's item 9): {@link ModelCamarasaurus#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays
 * closed until an artist delivers {@code idle} and {@code walk}). Wingspeed 0.65f (orig ModelCamarasaurus.java:14,38 /
 * ClientProxyOreSpawn.java:421): the THRESHOLD idiom on the eight leg parts about X - above a walking speed of a tenth
 * {@code cos(age * 1.3f * ws) * PI * 0.25f * limbSwingAmount} around 0 / -0.15 rad rests, 0 at or below it (orig :175-183);
 * the HEALTH-FREQUENCY idiom on the four tail parts about Y - {@code cos(age * 1.5f * ws * hf) * PI * 0.25f * hf} with
 * {@code hf = health / maxHealth} (orig :184-185, read through {@link CamarasaurusPose}; orig :186's sitting check is the
 * port's {@code if (false)}, kept), scaled 0.25 / 0.5 / 0.75 / 1 down the chain; the POSITION-write idiom (through
 * {@link #moveTo}) - each tail part's pivot FOLLOWS the previous 5 / 8 / 7 units along (sin, cos) of its yaw and each neck
 * / head part the previous 6 / 7 / 5 units back (orig :189-217); and the HEAD-LOOK idiom about Y at 0.125 / 0.25 / 0.38
 * / 1 / 1 of {@code toRadians(netHeadYaw)} on the three necks and two heads (orig :198-217; no pitch). Tail0's and Neck1's
 * pivots are never written (the bind), read through {@link #classicPosition}; every value the classic reads back from a
 * part it just wrote is held in a local; a part whose x / z the classic writes keeps its bind y.
 *
 * <p>Scale and shadow follow {@link CamarasaurusRenderer}: {@link CamarasaurusRenderer#SCALE} (0.65, lifted to public by
 * the landing slice so both renderers read the one constant), halved for a baby, and a 0.65 x 0.65 shadow (ENT-S-092;
 * orig RenderCamarasaurus.java:23-24, 39-44) - the shadow a constructor literal there, so the equal literal here.</p>
 */
public final class CamarasaurusGeoReplacement extends OreSpawnGeoReplacement<Camarasaurus> {
    /** orig ModelCamarasaurus.java:14,38 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:421): the chain's third multiply. */
    static final float WINGSPEED = 0.65F;
    private static final GeoReplacementDescriptor<Camarasaurus> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.CAMARASAURUS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Camarasaurus.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/camarasaurus.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/camarasaurus.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/camarasaurus.png"),
            // orig RenderCamarasaurus.java:23 super(model, par2 * par3) with 0.65f x 0.65f: the product CamarasaurusRenderer's
            // constructor passes (it declares no SHADOW constant)
            0.65F * 0.65F) {
        @Override
        public void applyScale(Camarasaurus entity, PoseStack poseStack, float partialTick) {
            // orig RenderCamarasaurus.preRenderScale (:39-44): a child gets glScalef(scale / 2), otherwise glScalef(scale)
            // (CamarasaurusRenderer.render: isBaby() ? SCALE / 2 : SCALE)
            if (entity.isBaby()) {
                poseStack.scale(CamarasaurusRenderer.SCALE / 2.0F, CamarasaurusRenderer.SCALE / 2.0F, CamarasaurusRenderer.SCALE / 2.0F);
                return;
            }
            poseStack.scale(CamarasaurusRenderer.SCALE, CamarasaurusRenderer.SCALE, CamarasaurusRenderer.SCALE);
        }
    };

    public CamarasaurusGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        CamarasaurusPose entity = inputs.subject(CamarasaurusPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelCamarasaurus.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right.
        float hf = 0.0f;
        float newangle = 0.0f;

        newangle = (double) limbSwingAmount > 0.1 ? Mth.cos(ageInTicks * 1.3f * WINGSPEED) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
        rotateX(processor, "FLegupleft", newangle);
        rotateX(processor, "FLegdownleft", newangle);
        rotateX(processor, "FLegupright", -newangle);
        rotateX(processor, "FLegdownright", -newangle);
        rotateX(processor, "BLegupleft", -0.15f - newangle);
        rotateX(processor, "BLegdownleft", -newangle);
        rotateX(processor, "BLegupright", -0.15f + newangle);
        rotateX(processor, "BLegdownright", newangle);

        hf = (float) entity.getHealth() / entity.getMaxHealth();
        newangle = Mth.cos(ageInTicks * 1.5f * WINGSPEED * hf) * (float) Math.PI * 0.25f * hf;
        if (false) {
            newangle = 0.0f;
        }
        float tail0YRot = newangle * 0.25f;
        rotateY(processor, "Tail0", tail0YRot);
        float[] tail0 = classicPosition(bone(processor, "Tail0"));  // never written: the bind pivot
        float tail1Z = tail0[2] + (float) Math.cos(tail0YRot) * 5.0f;
        float tail1X = tail0[0] + (float) Math.sin(tail0YRot) * 5.0f;
        moveXZ(processor, "Tail1", tail1X, tail1Z);
        float tail1YRot = newangle * 0.5f;
        rotateY(processor, "Tail1", tail1YRot);
        float tail2Z = tail1Z + (float) Math.cos(tail1YRot) * 8.0f;
        float tail2X = tail1X + (float) Math.sin(tail1YRot) * 8.0f;
        moveXZ(processor, "Tail2", tail2X, tail2Z);
        float tail2YRot = newangle * 0.75f;
        rotateY(processor, "Tail2", tail2YRot);
        float tail3Z = tail2Z + (float) Math.cos(tail2YRot) * 7.0f;
        float tail3X = tail2X + (float) Math.sin(tail2YRot) * 7.0f;
        moveXZ(processor, "Tail3", tail3X, tail3Z);
        rotateY(processor, "Tail3", newangle * 1.0f);

        float neck1YRot = (float) Math.toRadians(netHeadYaw) * 0.125f;
        rotateY(processor, "Neck1", neck1YRot);
        float[] neck1 = classicPosition(bone(processor, "Neck1"));  // never written: the bind pivot
        float neck2Z = neck1[2];
        float neck2X = neck1[0];
        moveXZ(processor, "Neck2", neck2X, neck2Z);
        float neck2YRot = (float) Math.toRadians(netHeadYaw) * 0.25f;
        rotateY(processor, "Neck2", neck2YRot);
        float neck3Z = neck2Z - (float) Math.cos(neck2YRot) * 6.0f;
        float neck3X = neck2X - (float) Math.sin(neck2YRot) * 6.0f;
        moveXZ(processor, "Neck3", neck3X, neck3Z);
        float neck3YRot = (float) Math.toRadians(netHeadYaw) * 0.38f;
        rotateY(processor, "Neck3", neck3YRot);
        float head1Z = neck3Z - (float) Math.cos(neck3YRot) * 7.0f;
        float head1X = neck3X - (float) Math.sin(neck3YRot) * 7.0f;
        moveXZ(processor, "Head1", head1X, head1Z);
        float head1YRot = (float) Math.toRadians(netHeadYaw);
        rotateY(processor, "Head1", head1YRot);
        float head2Z = head1Z - (float) Math.cos(head1YRot) * 5.0f;
        float head2X = head1X - (float) Math.sin(head1YRot) * 5.0f;
        moveXZ(processor, "Head2", head2X, head2Z);
        rotateY(processor, "Head2", (float) Math.toRadians(netHeadYaw));
    }

    /** {@code part.x = x; part.z = z} in classic terms: the classic writes x and z only, so y keeps the part's bind. */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Camarasaurus, CamarasaurusGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CamarasaurusGeoReplacement());
        }
    }
}
