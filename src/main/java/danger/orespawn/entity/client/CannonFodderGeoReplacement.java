package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityCannonFodder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Cannon Fodder (the third Tier-2 slice, 2026-09-13): {@link CannonFodderModel#setupAnim} verbatim on the
 * converted rig, ON THE HOOK (owner 2026-09-13, Amendment 2 to Amendment 1: no keyframe layer, no transcription - the
 * self-gate stays closed until an artist delivers {@code idle} and {@code walk}). A port-authored six-part quadruped
 * (1.7.10's {@code EntityCannonFodder} was an unregistered tameable base class with no model or renderer of its own,
 * so this rig has no 1.7.10 pair and no reference leg): the HEAD-LOOK idiom (yaw and pitch in radians) and a
 * vanilla-style gait on the walk DISTANCE - {@code cos(limbSwing * 0.6662f) * 1.2f * limbSwingAmount}, the front-left
 * and back-right legs positive, the other two the negative (the Amendment 1 code-driven form: a {@code limbSwing}
 * gait is not a transcription-form channel, so it runs on the hook).
 *
 * <p>Shadow follows {@link CannonFodderRenderer}'s constructor ({@code super(context, model, 0.4f)}: the literal it
 * passes - it declares no SHADOW constant and no scale override, and no reference renderer pins it).</p>
 */
public final class CannonFodderGeoReplacement extends OreSpawnGeoReplacement<EntityCannonFodder> {
    private static final GeoReplacementDescriptor<EntityCannonFodder> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_CANNON_FODDER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityCannonFodder.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/cannonfodder.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/cannonfodder.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/cannon_fodder.png"),
            // CannonFodderRenderer's constructor passes the literal 0.4f (no SHADOW constant): the equal literal
            0.4F) {
    };

    public CannonFodderGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float limbSwing = inputs.limbSwing();
        float limbSwingAmount = inputs.limbSwingAmount();
        float netHeadYaw = inputs.netHeadYaw();
        float headPitch = inputs.headPitch();
        // CannonFodderModel.setupAnim verbatim.
        rotateY(processor, "head", netHeadYaw * ((float) Math.PI / 180F));
        rotateX(processor, "head", headPitch * ((float) Math.PI / 180F));

        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        rotateX(processor, "leg_front_left", legSwing);
        rotateX(processor, "leg_front_right", -legSwing);
        rotateX(processor, "leg_back_left", -legSwing);
        rotateX(processor, "leg_back_right", legSwing);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityCannonFodder, CannonFodderGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CannonFodderGeoReplacement());
        }
    }
}
