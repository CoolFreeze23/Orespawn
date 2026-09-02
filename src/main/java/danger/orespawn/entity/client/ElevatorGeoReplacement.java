package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Elevator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;

/**
 * GeckoLib hoverboard. The rig is static (the G1 Tier-3 proof); the classic
 * renderer's two behaviors — paint-colour texture and the boat-style hit
 * wobble from {@code ElevatorRenderer.setupRotations} — live in the descriptor.
 */
public final class ElevatorGeoReplacement extends OreSpawnGeoReplacement<Elevator> {
    /** Index 0 unused; 1..10 match the original's texture1..texture10. */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[11];

    static {
        for (int i = 1; i <= 10; ++i) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                    "textures/entity/elevator" + i + ".png");
        }
    }

    private static final GeoReplacementDescriptor<Elevator> DESCRIPTOR = new GeoReplacementDescriptor<>(
            ModEntities.ELEVATOR::get,
            Elevator.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/elevator.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/elevator.animation.json"),
            TEXTURES[1],
            0.25F) {
        @Override
        public ResourceLocation texture(Elevator entity) {
            int color = entity.getColor();
            if (color < 1 || color > 10) color = 1;
            return TEXTURES[color];
        }

        @Override
        public void applyRotations(Elevator entity, PoseStack poseStack, float ageInTicks, float partialTick) {
            // orig RenderElevator.java:31-38 — boat-style hit wobble.
            float hitTime = (float) entity.getTimeSinceHit() - partialTick;
            float damage = entity.getDamageTaken() - partialTick;
            if (damage < 0.0f) {
                damage = 0.0f;
            }
            if (hitTime > 0.0f) {
                poseStack.mulPose(Axis.XP.rotationDegrees(
                        Mth.sin(hitTime) * hitTime * damage / 10.0f * (float) entity.getForwardDirection()));
            }
        }
    };

    public ElevatorGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Elevator, ElevatorGeoReplacement> {
        public Renderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context) {
            super(context, new ElevatorGeoReplacement());
        }
    }
}
