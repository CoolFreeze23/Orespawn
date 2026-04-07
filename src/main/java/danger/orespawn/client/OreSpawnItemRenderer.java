package danger.orespawn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.client.model.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class OreSpawnItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static OreSpawnItemRenderer instance;

    private final Map<Item, WeaponRenderData> weaponModels = new HashMap<>();

    public OreSpawnItemRenderer() {
        super(null, null);
        registerWeapon(ModItems.BIG_BERTHA.get(),
                BerthaItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/berthatexture.png"),
                "big_bertha_flat", 0.7f);
        registerWeapon(ModItems.SLICE.get(),
                SliceItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/slicetexture.png"),
                "slice_flat", 0.65f);
        registerWeapon(ModItems.ROYAL_GUARDIAN_SWORD.get(),
                SliceItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/royaltexture.png"),
                "royal_guardian_sword_flat", 0.65f);
        registerWeapon(ModItems.BATTLE_AXE.get(),
                BattleAxeItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/battleaxetexture.png"),
                "battle_axe_flat", 0.6f);
        registerWeapon(ModItems.QUEEN_BATTLE_AXE.get(),
                QueenBattleAxeItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/queenbattleaxetexture.png"),
                "queen_battle_axe_flat", 0.6f);
        registerWeapon(ModItems.CHAINSAW.get(),
                ChainsawItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/chainsawtexture.png"),
                "chainsaw_flat", 0.55f);
        registerWeapon(ModItems.ATTITUDE_ADJUSTER.get(),
                HammyItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/attitudeadjustertexture.png"),
                "attitude_adjuster_flat", 0.45f);
        registerWeapon(ModItems.SQUID_ZOOKA.get(),
                SquidZookaItemModel.createLayerDefinition().bakeRoot(),
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/squidzookatexture.png"),
                "squid_zooka_flat", 0.6f);
    }

    private void registerWeapon(Item item, ModelPart root, ResourceLocation texture,
                                String flatModelId, float scale) {
        ModelResourceLocation flatLoc = ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "item/" + flatModelId));
        weaponModels.put(item, new WeaponRenderData(root, texture, flatLoc, scale));
    }

    public static OreSpawnItemRenderer getInstance() {
        if (instance == null) {
            instance = new OreSpawnItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        WeaponRenderData data = weaponModels.get(stack.getItem());
        if (data == null) return;

        if (context == ItemDisplayContext.GUI
                || context == ItemDisplayContext.GROUND
                || context == ItemDisplayContext.FIXED
                || context == ItemDisplayContext.NONE) {
            renderFlat(stack, context, poseStack, buffer, packedLight, packedOverlay, data);
            return;
        }

        render3D(stack, context, poseStack, buffer, packedLight, packedOverlay, data);
    }

    private void renderFlat(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                            MultiBufferSource buffer, int light, int overlay, WeaponRenderData data) {
        BakedModel flatModel = Minecraft.getInstance().getModelManager().getModel(data.flatModelLocation);
        boolean leftHand = (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        Minecraft.getInstance().getItemRenderer().render(
                stack, context, leftHand, poseStack, buffer, light, overlay, flatModel);
        poseStack.popPose();
    }

    private void render3D(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                          MultiBufferSource buffer, int light, int overlay, WeaponRenderData data) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(1.0f, -1.0f, -1.0f);

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer,
                RenderType.entityCutout(data.texture), false, stack.hasFoil());

        data.root.render(poseStack, vertexConsumer, light, overlay);
        poseStack.popPose();
    }

    private record WeaponRenderData(ModelPart root, ResourceLocation texture,
                                    ModelResourceLocation flatModelLocation, float scale) {}
}
