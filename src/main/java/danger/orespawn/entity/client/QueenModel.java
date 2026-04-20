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
 *   <li><b>Geometry:</b> {@code assets/orespawn/geo/entity/the_queen.geo.json}
 *       — exported from Blockbench in bedrock format 1.12.0. The Blockbench
 *       file was named {@code ModelTheQueen.geo.json} originally, but Mojang's
 *       1.21.1 {@code ResourceLocation} validator only accepts {@code [a-z0-9/._-]}
 *       in path segments and rejects any uppercase letters — so we rename
 *       the asset on disk to {@code the_queen.geo.json}. The internal Bedrock
 *       geometry identifier ({@code geometry.ModelTheQueen}) is unaffected
 *       and remains valid because Geckolib resolves it from the JSON content,
 *       not the filename.</li>
 *   <li><b>Animations:</b>
 *       {@code assets/orespawn/animations/entity/the_queen.animation.json}
 *       — bedrock format 1.8.0, eight clips total: idle / idle_to_attack /
 *       attack / bite / tail_whip_left / tail_whip_right / roar / death.
 *       Same lowercase-rename rationale as the geometry file.</li>
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
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/the_queen.geo.json");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/the_queen.animation.json");
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
