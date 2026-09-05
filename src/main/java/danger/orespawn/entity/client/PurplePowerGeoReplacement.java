package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.PurplePower;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib PurplePower (Slice 4c): {@link ModelPurplePower} on the
 * render-instance-expanded rig. The classic {@code renderToBuffer} (:59-78)
 * draws each spoke six times under {@code mulPose(Z, k * 60°)} on the pose
 * stack, outside the spoke's own animated rotation; the converted rig carries
 * that loop statically: draw {@code k} of {@code Shape1} is the clone bone
 * {@code Shape1__i<k>} (the spoke's pivot, no bind rotation) under its own
 * parent {@code Shape1__fan<k>} (pivot at the model origin, bind Z rotation
 * {@code k * 60°}). This hook writes the classic {@code setupAnim} angles
 * (:45-56, verbatim) onto the six clone children of each spoke; the static
 * steps never move. Texture by {@code getPurpleType()} through
 * {@link PurplePowerRenderer#textureFor}; shadow {@link PurplePowerRenderer#SHADOW}
 * (0.3 x 2.75, ENT-S-092).
 *
 * <p>ENT-S-146 (REPORT when this landed): the classic model is a re-authoring of
 * the 1.7.10 one (random per-frame tri-axis rolls, translucent, fullbright). This
 * candidate reproduces the classic AS IT STANDS, by the harness law
 * (candidate == classic); when the fix lands the hook is re-based onto it
 * (an {@code entity_state} pose through a {@code PurplePowerPose} carrying the
 * seeded RNG, the 60° step becoming the clone's own Z channel).</p>
 */
public final class PurplePowerGeoReplacement extends OreSpawnGeoReplacement<PurplePower> {
    /** ModelPurplePower.renderToBuffer:59-78 — six draws per spoke. */
    public static final int INSTANCES = 6;
    private static final String[] INNER = clones("Shape1");
    private static final String[] MIDDLE = clones("Shape2");
    private static final String[] OUTER = clones("Shape3");

    private static final GeoReplacementDescriptor<PurplePower> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.PURPLE_POWER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            PurplePower.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/purplepower.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/purplepower.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/purplepowertexture.png"),
            PurplePowerRenderer.SHADOW) {
        @Override
        public ResourceLocation texture(PurplePower entity) {
            return PurplePowerRenderer.textureFor(entity.getPurpleType());
        }
    };

    public PurplePowerGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** The converter's clone names for {@code render_instances} with {@code step_scope: stack}: {@code <part>__i<k>}. */
    private static String[] clones(String part) {
        String[] names = new String[INSTANCES];
        for (int k = 0; k < INSTANCES; k++) {
            names[k] = part + "__i" + k;
        }
        return names;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        float ageInTicks = inputs.ageInTicks();
        // ModelPurplePower.setupAnim:45-56, verbatim.
        float angle1 = (float) Math.toRadians(ageInTicks * 7.3F);
        float angle2 = (float) Math.toRadians(ageInTicks * 5.1F);
        float angle3 = (float) Math.toRadians(ageInTicks * 3.7F);

        for (int k = 0; k < INSTANCES; k++) {
            rotateZ(processor, INNER[k], angle1);
            rotateX(processor, INNER[k], angle2);
            rotateZ(processor, MIDDLE[k], angle2);
            rotateY(processor, MIDDLE[k], angle3);
            rotateZ(processor, OUTER[k], angle3);
            rotateX(processor, OUTER[k], angle1);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<PurplePower, PurplePowerGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new PurplePowerGeoReplacement());
        }
    }
}
