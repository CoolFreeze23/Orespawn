package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Coin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Coin: {@link ModelCoin#setupAnim} verbatim on the converted rig
 * (Tier 3, code-driven per Amendment 1), with {@link CoinRenderer}'s 0.125
 * render scale and its 0.75 x 0.125 shadow (BUG-040 fix, proven against the
 * 1.7.10 source by the reference-geometry leg).
 */
public final class CoinGeoReplacement extends OreSpawnGeoReplacement<Coin> {
    private static final GeoReplacementDescriptor<Coin> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.COIN.get(),
            Coin.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/coin.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/coin.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/coin.png"),
            CoinRenderer.SHADOW) {
        @Override
        public void applyScale(Coin entity, PoseStack poseStack, float partialTick) {
            poseStack.scale(CoinRenderer.SCALE, CoinRenderer.SCALE, CoinRenderer.SCALE);
        }
    };

    public CoinGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Intentionally none: the pose is code-driven below.
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        // ModelCoin.setupAnim: coin.yRot = Mth.cos(ageInTicks * 0.05F * WINGSPEED) * PI
        rotateY(processor, "coin", Mth.cos(inputs.ageInTicks() * 0.05F * 0.22F) * (float) Math.PI);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Coin, CoinGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new CoinGeoReplacement());
        }
    }
}
