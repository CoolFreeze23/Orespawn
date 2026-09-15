package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Girlfriend;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Girlfriend (drafted by the fourth Tier-2 slice T2d, 2026-09-14, TEST-010 (b); landed by the remainder slice,
 * 2026-09-15, under the owner's closing set item 3 with the Boyfriend): the classic pose verbatim on the converted rig, ON
 * THE HOOK (Amendment 2: no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk}). {@code ModelGirlfriend} declares no {@code setupAnim} either: the pose its classic
 * {@code HumanoidMobRenderer} draws is vanilla {@code HumanoidModel.setupAnim} over the same seven bones (the arms 3 wide
 * where the Boyfriend's are 4: geometry, not pose), so this hook delegates to {@link BoyfriendGeoReplacement#poseRig}
 * (the Butterfly / Leon / Baby Dragon form: one hook, its own registry, texture, shadow and scale), the two per-frame lerps
 * at the seam's partial tick as the classic renderer's ({@code PoseInputs.partialTick()}); she swings (Girlfriend.java:441
 * the held-weapon melee, :521 after every shot or throw), so the swing is a reachable per-frame read the seam now carries.
 *
 * <p>THE CLASSIC LAYERS: {@link Renderer} attaches the vanilla layer classes {@code GirlfriendRenderer} carries - the
 * {@code HumanoidMobRenderer} constructor's three (CustomHeadLayer, ElytraLayer, ItemInHandLayer; 21.1.223 bytecode 7-67)
 * and its own {@code HumanoidArmorLayer} (GirlfriendRenderer.java:20-23) - through the seam's
 * {@link OreSpawnGeoReplacedEntityRenderer.VanillaLayersAdapter} over a classic {@code ModelGirlfriend}.</p>
 *
 * <p>Scale and shadow follow {@link GirlfriendRenderer}: 1.0 render scale, 5.0 while valentine-angry
 * ({@code GirlfriendRenderer.scale}, orig RenderGirlfriend.java:29-37), and the 0.5 shadow its constructor passes as a
 * literal (it declares no SHADOW constant; ENT-S-092); the texture per skin and the valentine state through
 * {@link GirlfriendRenderer#textureFor} (the classic renderer's own switch, lifted into it as the Fairy's was).</p>
 */
public final class GirlfriendGeoReplacement extends OreSpawnGeoReplacement<Girlfriend> {
    private static final GeoReplacementDescriptor<Girlfriend> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.GIRLFRIEND.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Girlfriend.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/girlfriend.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/girlfriend.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/girlfriend0.png"),
            // GirlfriendRenderer's constructor passes the literal 0.5f (no SHADOW constant): the equal literal
            0.5F) {
        /** GirlfriendRenderer.getTextureLocation: the valentine sheet while angry, else the numbered skin. */
        @Override
        public ResourceLocation texture(Girlfriend entity) {
            return GirlfriendRenderer.textureFor(entity);
        }

        @Override
        public void applyScale(Girlfriend entity, PoseStack poseStack, float partialTick) {
            // GirlfriendRenderer.scale (orig RenderGirlfriend.java:29-37): valentine-angry girlfriends render 5x
            if (entity.isValentineAngry()) {
                poseStack.scale(5.0f, 5.0f, 5.0f);
            }
        }
    };

    public GirlfriendGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        BoyfriendGeoReplacement.poseRig(processor, inputs);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Girlfriend, GirlfriendGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new GirlfriendGeoReplacement());
            // THE CLASSIC LAYERS (owner's closing set item 3 (3)): GirlfriendRenderer's, in its order - the HumanoidMobRenderer
            // constructor's CustomHeadLayer, ElytraLayer and ItemInHandLayer (21.1.223 bytecode 7-67), then its HumanoidArmorLayer
            // over the player armor layers (:20-23) - through the seam's adapter over a classic ModelGirlfriend posed from the bake.
            VanillaLayersAdapter<Girlfriend, ModelGirlfriend, GirlfriendGeoReplacement> layers = new VanillaLayersAdapter<>(
                    this, new ModelGirlfriend(context.bakeLayer(GirlfriendRenderer.MODEL_LAYER)));
            layers.addLayer(new CustomHeadLayer<>(layers, context.getModelSet(), context.getItemInHandRenderer()));
            layers.addLayer(new ElytraLayer<>(layers, context.getModelSet()));
            layers.addLayer(new ItemInHandLayer<>(layers, context.getItemInHandRenderer()));
            layers.addLayer(new HumanoidArmorLayer<>(layers,
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                    context.getModelManager()));
            addRenderLayer(layers);
        }
    }
}
