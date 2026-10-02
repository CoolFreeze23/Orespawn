package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.item.ItemOreSpawnWolfArmor;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Wolf;

/**
 * MOD-041: the sets' wolf armour on a wolf. Vanilla's wolf armour layer draws only while Wolf.hasArmor(), which asks
 * for vanilla's own wolf armour, so this layer, added to the wolf's renderer, draws ours the same way: the wolf's model
 * at the armour's size (the WOLF_ARMOR layer), posed as the wolf, with the item's texture. Without a dye overlay or
 * cracks, as the armour takes neither.
 */
public class WolfArmourLayer extends RenderLayer<Wolf, WolfModel<Wolf>> {
    private final WolfModel<Wolf> model;

    public WolfArmourLayer(RenderLayerParent<Wolf, WolfModel<Wolf>> renderer, EntityModelSet models) {
        super(renderer);
        this.model = new WolfModel<>(models.bakeLayer(ModelLayers.WOLF_ARMOR));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Wolf wolf, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(wolf.getBodyArmorItem().getItem() instanceof ItemOreSpawnWolfArmor armour)) return;
        this.getParentModel().copyPropertiesTo(this.model);
        this.model.prepareMobModel(wolf, limbSwing, limbSwingAmount, partialTick);
        this.model.setupAnim(wolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(armour.getTexture()));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
