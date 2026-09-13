package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLunaMoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Luna Moth (the hook lanes, 2026-09-14): a consumer of the Butterfly rig. orig ClientProxyOreSpawn.java:407
 * registers {@code new RenderButterfly(new ModelButterfly(0.75f), 0.4f, 1.5f)} over the moth's sheets, so this descriptor
 * shares {@link ButterflyGeoReplacement}'s geo, clip file and hook under its own registry path ({@code luna_moth}; one
 * profile per registry path even for a shared rig) at the moth's own wingspeed 0.75f.
 *
 * <p>Scale and shadow follow {@link LunaMothRenderer}: 1.5 render scale and a 0.4 x 1.5 shadow (ENT-S-092); the texture
 * per {@code moth_type} exactly as {@link LunaMothRenderer#getTextureLocation} switches it (its sheets are private
 * there, so the switch is carried here).</p>
 */
public final class LunaMothGeoReplacement extends OreSpawnGeoReplacement<EntityLunaMoth> {
    /** orig ClientProxyOreSpawn.java:407 {@code new ModelButterfly(0.75f)}: the Luna Moth's wingspeed. */
    static final float WINGSPEED = 0.75F;
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/lunamoth.png");
    private static final GeoReplacementDescriptor<EntityLunaMoth> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_LUNA_MOTH.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityLunaMoth.class,
            // the Butterfly's shipped rig and clip file, one rig for four consumers
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/butterfly.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/butterfly.animation.json"),
            TEXTURE,
            LunaMothRenderer.SHADOW) {
        /** LunaMothRenderer.getTextureLocation: the moth type's sheet (1 / 2 / 3 -> eyemoth / darkmoth / firemoth, otherwise lunamoth). */
        @Override
        public ResourceLocation texture(EntityLunaMoth entity) {
            return switch (entity.moth_type) {
                case 1 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/eyemoth.png");
                case 2 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/darkmoth.png");
                case 3 -> ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/firemoth.png");
                default -> TEXTURE;
            };
        }

        @Override
        public void applyScale(EntityLunaMoth entity, PoseStack poseStack, float partialTick) {
            // orig RenderButterfly.java:26,42 preRenderScale: GL11.glScalef(scale, scale, scale) (LunaMothRenderer.scale)
            poseStack.scale(LunaMothRenderer.SCALE, LunaMothRenderer.SCALE, LunaMothRenderer.SCALE);
        }
    };

    public LunaMothGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        ButterflyGeoReplacement.pose(processor, inputs.ageInTicks(), WINGSPEED);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityLunaMoth, LunaMothGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new LunaMothGeoReplacement());
        }
    }
}
