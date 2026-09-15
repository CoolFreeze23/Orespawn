package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.BabyDragon;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Baby Dragon (the hooks, owner 2026-09-14, addendum item 10; Tier 1; landed by the first Tier-1 slice T1a,
 * 2026-09-15, the owner's closing set item 4): a consumer of the Dragon rig. The port's {@link BabyDragonRenderer}
 * draws the distinct {@link BabyDragon} entity with {@link ModelDragon} at 0.45 scale over the same two type textures,
 * so this descriptor shares {@link DragonGeoReplacement} 's geo, clip file and hook ({@link
 * DragonGeoReplacement#poseDragon}) under its own registry path ({@code baby_dragon}; design Q9, one profile per
 * registry path even for a shared rig - the Ant precedent), and the harness proves it on its own manifest entry. The
 * entity reads {@link danger.orespawn.entity.pose.DragonPose} through its parent.
 *
 * <p>Scale and shadow follow {@link BabyDragonRenderer} : 0.45 render scale ({@link BabyDragonRenderer#SCALE}, made
 * public by the landing slice T1a - the T2d form) and a 0.6 shadow (its constructor literal - no 1.7.10 pair pins it -
 * so the equal literal here); the texture by {@code getDragonType()} as {@link BabyDragonRenderer#getTextureLocation}
 * resolves it. The rig's zero-thickness cube (tail5) is the Dragon's, so the seam expects the classic face order in the
 * shared geo.</p>
 */
public final class BabyDragonGeoReplacement extends OreSpawnGeoReplacement<BabyDragon> {
    private static final GeoReplacementDescriptor<BabyDragon> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.BABY_DRAGON.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            BabyDragon.class,
            // the Dragon's rig and clip file, one rig for two consumers (the literals are what the asset audit and the
            // package tool attribute the rig by)
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/dragon.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/dragon.animation.json"),
            DragonGeoReplacement.TEXTURE,
            // BabyDragonRenderer's constructor passes the literal 0.6f (no SHADOW constant): the equal literal
            0.6F) {
        @Override
        public ResourceLocation texture(BabyDragon entity) {
            return DragonGeoReplacement.textureFor(entity.getDragonType());
        }

        @Override
        public void applyScale(BabyDragon entity, PoseStack poseStack, float partialTick) {
            // BabyDragonRenderer.render: poseStack.scale(SCALE, SCALE, SCALE), 0.45 unconditionally (the renderer's constant, public since T1a)
            poseStack.scale(BabyDragonRenderer.SCALE, BabyDragonRenderer.SCALE, BabyDragonRenderer.SCALE);
        }

        /** The Dragon rig's one zero-thickness cube (tail5): the seam expects {@link FaceOrder#KEY} in the shared geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public BabyDragonGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        DragonGeoReplacement.poseDragon(processor, inputs);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<BabyDragon, BabyDragonGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new BabyDragonGeoReplacement());
        }
    }
}
