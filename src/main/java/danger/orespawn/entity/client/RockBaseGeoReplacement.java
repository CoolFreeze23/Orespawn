package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.RockBase;
import danger.orespawn.entity.pose.RockBasePose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib RockBase: {@link ModelRockBase#setupAnim} verbatim on the converted
 * rig (Tier 3 per ruling 2 of 2026-09-02). The pose is pure visibility: one
 * of five part sets is shown for the entity's rock type, and the texture is
 * the classic renderer's per-type table.
 */
public final class RockBaseGeoReplacement extends OreSpawnGeoReplacement<RockBase> {
    public static final String[] ALL_PARTS = {
            "RockShape1", "RockShape2", "RockShape3",
            "RockSmallShape2", "RockSmallShape1",
            "RockTNTShape1", "RockTNTShape2", "RockTNTShape3", "RockTNTShape4",
            "RockSpikeyShape1", "RockSpikeyShape2", "RockSpikeyShape3",
            "CrystalShape1", "CrystalShape2", "CrystalShape3a", "CrystalShape3b", "CrystalShape3c",
            "CrystalShape3d", "CrystalShape4a", "CrystalShape4b", "CrystalShape4c", "CrystalShape4d",
    };

    private static final GeoReplacementDescriptor<RockBase> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ROCK_BASE.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            RockBase.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/rockbase.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/rockbase.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/rocktexture.png"),
            0.3F) {
        @Override
        public ResourceLocation texture(RockBase entity) {
            return RockBaseRenderer.textureFor(entity.getRockType());
        }
    };

    public RockBaseGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        int rockType = inputs.subject(RockBasePose.class).getRockType();

        for (String part : ALL_PARTS) {
            setVisible(processor, part, false);
        }

        if (rockType < 1 || rockType > 12) {
            return;
        }

        if (rockType == 1) {
            setVisible(processor, "RockSmallShape1", true);
            setVisible(processor, "RockSmallShape2", true);
        } else if (rockType == 7) {
            setVisible(processor, "RockSpikeyShape1", true);
            setVisible(processor, "RockSpikeyShape2", true);
            setVisible(processor, "RockSpikeyShape3", true);
        } else if (rockType == 8) {
            setVisible(processor, "RockTNTShape1", true);
            setVisible(processor, "RockTNTShape2", true);
            setVisible(processor, "RockTNTShape3", true);
            setVisible(processor, "RockTNTShape4", true);
        } else if (rockType >= 9 && rockType <= 12) {
            setVisible(processor, "CrystalShape1", true);
            setVisible(processor, "CrystalShape2", true);
            setVisible(processor, "CrystalShape3a", true);
            setVisible(processor, "CrystalShape3b", true);
            setVisible(processor, "CrystalShape3c", true);
            setVisible(processor, "CrystalShape3d", true);
            setVisible(processor, "CrystalShape4a", true);
            setVisible(processor, "CrystalShape4b", true);
            setVisible(processor, "CrystalShape4c", true);
            setVisible(processor, "CrystalShape4d", true);
        } else {
            setVisible(processor, "RockShape1", true);
            setVisible(processor, "RockShape2", true);
            setVisible(processor, "RockShape3", true);
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<RockBase, RockBaseGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new RockBaseGeoReplacement());
        }
    }
}
