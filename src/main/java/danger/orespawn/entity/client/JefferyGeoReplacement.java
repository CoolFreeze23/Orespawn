package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Jeffery;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Jeffery (the hooks, owner 2026-09-14, addendum item 10): a consumer of the Giant Robot rig. The port registers
 * {@code GiantRobotRenderer::new} for the {@code jeffery} registry ({@code OreSpawnClient.java:66}; {@code Jeffery
 * extends GiantRobot} - a port-side registry with no 1.7.10 registration of its own), so this descriptor shares
 * {@link GiantRobotGeoReplacement}'s geo, clip file and hook ({@link GiantRobotGeoReplacement#poseRig}) under its own
 * registry path (the Ant precedent: one profile per registry path even for a shared rig), with the Giant Robot's texture,
 * shadow and scale: {@link GiantRobotRenderer#SHADOW} (0.99 x 1.0, ENT-S-092), {@code SCALE} 1.0 (identity, no scale
 * hook). The entity is read through {@code GiantRobotPose}, which the Jeffery satisfies through its parent.
 */
public final class JefferyGeoReplacement extends OreSpawnGeoReplacement<Jeffery> {
    private static final GeoReplacementDescriptor<Jeffery> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.JEFFERY.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Jeffery.class,
            // the Giant Robot's rig and clip file, one rig for two consumers
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/giantrobot.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/giantrobot.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/giantrobot.png"),
            GiantRobotRenderer.SHADOW) {
    };

    public JefferyGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        GiantRobotGeoReplacement.poseRig(processor, inputs);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Jeffery, JefferyGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new JefferyGeoReplacement());
        }
    }
}
