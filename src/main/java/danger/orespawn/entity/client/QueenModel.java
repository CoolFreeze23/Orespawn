package danger.orespawn.entity.client;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.TheQueen;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Geckolib model wrapper for {@link TheQueen}. Replaces the legacy
 * procedural {@code ModelTheQueen} class.
 *
 * <h2>Asset paths</h2>
 *
 * <ul>
 *   <li><b>Geometry:</b> {@code assets/orespawn/geo/entity/ModelTheQueen.geo.json}
 *       — exported from Blockbench in bedrock format 1.12.0
 *       (geometry identifier {@code geometry.ModelTheQueen}).</li>
 *   <li><b>Animations:</b>
 *       {@code assets/orespawn/animations/entity/ModelTheQueen.animation.json}
 *       — bedrock format 1.8.0, eight clips total: idle / idle_to_attack /
 *       attack / bite / tail_whip_left / tail_whip_right / roar / death.</li>
 * </ul>
 *
 * <h2>Dynamic phase-shift texture swap</h2>
 *
 * <p>{@link #getTextureResource(TheQueen)} branches on
 * {@link TheQueen#isAwake()} so the model renders with the
 * <b>blue</b> (dormant) texture by default and the <b>red</b>
 * (aggro) texture once she's woken up via the hurt-gate phase
 * shift. This is the canonical Geckolib way to do per-entity
 * texture variants — re-evaluated every frame, so the swap is
 * instant the moment {@code IS_AWAKE} flips.</p>
 */
public class QueenModel extends GeoModel<TheQueen> {

    private static final ResourceLocation GEO =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/ModelTheQueen.geo.json");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/ModelTheQueen.animation.json");
    private static final ResourceLocation TEXTURE_DORMANT =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/blue_queen.png");
    private static final ResourceLocation TEXTURE_AGGRO =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/red_queen.png");

    @Override
    public ResourceLocation getModelResource(TheQueen animatable) {
        return GEO;
    }

    @Override
    public ResourceLocation getAnimationResource(TheQueen animatable) {
        return ANIMATIONS;
    }

    @Override
    public ResourceLocation getTextureResource(TheQueen animatable) {
        return animatable.isAwake() ? TEXTURE_AGGRO : TEXTURE_DORMANT;
    }
}
