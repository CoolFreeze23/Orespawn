package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntityLurkingTerror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LurkingTerrorModel extends EntityModel<EntityLurkingTerror> {
    private int ri1, ri2;
    private float rf1, rf2;
    private final ModelPart body;
    private final ModelPart leg1;
    private final ModelPart leg1part2;
    private final ModelPart leg1part3;
    private final ModelPart leg2;
    private final ModelPart leg2part2;
    private final ModelPart leg2part3;
    private final ModelPart leg3;
    private final ModelPart leg3part2;
    private final ModelPart leg3part3;
    private final ModelPart leg4;
    private final ModelPart leg4part2;
    private final ModelPart leg4part3;
    private final ModelPart leg5;
    private final ModelPart leg5part2;
    private final ModelPart leg6;
    private final ModelPart leg6part2;
    private final ModelPart thorax;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart jaw1;
    private final ModelPart jaw1part2;
    private final ModelPart jaw1tooth1;
    private final ModelPart jaw1tooth2;
    private final ModelPart jaw1tooth3;
    private final ModelPart jaw1tooth4;
    private final ModelPart jaw1tooth5;
    private final ModelPart jaw1tooth6;
    private final ModelPart jaw2;
    private final ModelPart jaw2part2;
    private final ModelPart jaw2tooth1;
    private final ModelPart jaw2tooth2;
    private final ModelPart jaw2tooth3;
    private final ModelPart jaw2tooth4;
    private final ModelPart jaw2tooth5;
    private final ModelPart jaw2tooth6;
    private final ModelPart jaw3;
    private final ModelPart jaw3part2;
    private final ModelPart jaw3tooth1;
    private final ModelPart jaw3tooth2;
    private final ModelPart jaw3tooth3;
    private final ModelPart jaw3tooth4;
    private final ModelPart jaw3tooth5;
    private final ModelPart jaw3tooth6;
    private final ModelPart jaw4;
    private final ModelPart jaw4part2;
    private final ModelPart jaw4tooth1;
    private final ModelPart jaw4tooth2;
    private final ModelPart jaw4tooth3;
    private final ModelPart jaw4tooth4;
    private final ModelPart jaw4tooth5;
    private final ModelPart jaw4tooth6;
    private final ModelPart tonguepart1;
    private final ModelPart tonguepart2;
    private final ModelPart tonguepart3;
    private final ModelPart wing_1;
    private final ModelPart wing_2;
    private final ModelPart wing_3;
    private final ModelPart wing_4;

    public LurkingTerrorModel(ModelPart root) {
        this.body = root.getChild("body");
        this.leg1 = root.getChild("leg1");
        this.leg1part2 = root.getChild("leg1part2");
        this.leg1part3 = root.getChild("leg1part3");
        this.leg2 = root.getChild("leg2");
        this.leg2part2 = root.getChild("leg2part2");
        this.leg2part3 = root.getChild("leg2part3");
        this.leg3 = root.getChild("leg3");
        this.leg3part2 = root.getChild("leg3part2");
        this.leg3part3 = root.getChild("leg3part3");
        this.leg4 = root.getChild("leg4");
        this.leg4part2 = root.getChild("leg4part2");
        this.leg4part3 = root.getChild("leg4part3");
        this.leg5 = root.getChild("leg5");
        this.leg5part2 = root.getChild("leg5part2");
        this.leg6 = root.getChild("leg6");
        this.leg6part2 = root.getChild("leg6part2");
        this.thorax = root.getChild("thorax");
        this.abdomen = root.getChild("abdomen");
        this.head = root.getChild("head");
        this.jaw1 = root.getChild("jaw1");
        this.jaw1part2 = root.getChild("jaw1part2");
        this.jaw1tooth1 = root.getChild("jaw1tooth1");
        this.jaw1tooth2 = root.getChild("jaw1tooth2");
        this.jaw1tooth3 = root.getChild("jaw1tooth3");
        this.jaw1tooth4 = root.getChild("jaw1tooth4");
        this.jaw1tooth5 = root.getChild("jaw1tooth5");
        this.jaw1tooth6 = root.getChild("jaw1tooth6");
        this.jaw2 = root.getChild("jaw2");
        this.jaw2part2 = root.getChild("jaw2part2");
        this.jaw2tooth1 = root.getChild("jaw2tooth1");
        this.jaw2tooth2 = root.getChild("jaw2tooth2");
        this.jaw2tooth3 = root.getChild("jaw2tooth3");
        this.jaw2tooth4 = root.getChild("jaw2tooth4");
        this.jaw2tooth5 = root.getChild("jaw2tooth5");
        this.jaw2tooth6 = root.getChild("jaw2tooth6");
        this.jaw3 = root.getChild("jaw3");
        this.jaw3part2 = root.getChild("jaw3part2");
        this.jaw3tooth1 = root.getChild("jaw3tooth1");
        this.jaw3tooth2 = root.getChild("jaw3tooth2");
        this.jaw3tooth3 = root.getChild("jaw3tooth3");
        this.jaw3tooth4 = root.getChild("jaw3tooth4");
        this.jaw3tooth5 = root.getChild("jaw3tooth5");
        this.jaw3tooth6 = root.getChild("jaw3tooth6");
        this.jaw4 = root.getChild("jaw4");
        this.jaw4part2 = root.getChild("jaw4part2");
        this.jaw4tooth1 = root.getChild("jaw4tooth1");
        this.jaw4tooth2 = root.getChild("jaw4tooth2");
        this.jaw4tooth3 = root.getChild("jaw4tooth3");
        this.jaw4tooth4 = root.getChild("jaw4tooth4");
        this.jaw4tooth5 = root.getChild("jaw4tooth5");
        this.jaw4tooth6 = root.getChild("jaw4tooth6");
        this.tonguepart1 = root.getChild("tonguepart1");
        this.tonguepart2 = root.getChild("tonguepart2");
        this.tonguepart3 = root.getChild("tonguepart3");
        this.wing_1 = root.getChild("wing_1");
        this.wing_2 = root.getChild("wing_2");
        this.wing_3 = root.getChild("wing_3");
        this.wing_4 = root.getChild("wing_4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(-4.0F, -4.0F, -8.0F, 8, 8, 12),
                PartPose.offset(0.0F, 10.0F, 0.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(18, 0).mirror()
                        .addBox(-15.0F, -1.5F, -1.5F, 16, 3, 3),
                PartPose.offsetAndRotation(-4.0F, 10.0F, -1.0F, 0.0F, -0.5759587F, -0.1919862F));

        partdefinition.addOrReplaceChild("leg1part2",
                CubeListBuilder.create().texOffs(58, 0).mirror()
                        .addBox(-15.0F, -1.5F, -1.5F, 3, 8, 3),
                PartPose.offsetAndRotation(-4.0F, 10.0F, -1.0F, 0.0F, -0.5759587F, -0.1919862F));

        partdefinition.addOrReplaceChild("leg1part3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-15.0F, -1.0F, -1.0F, 2, 8, 2),
                PartPose.offsetAndRotation(-4.0F, 10.0F, -1.0F, 0.0F, -0.5759587F, -0.6753082F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(18, 0).mirror()
                        .addBox(-1.0F, -1.5F, -1.5F, 16, 3, 3),
                PartPose.offsetAndRotation(4.0F, 10.0F, -1.0F, 0.0F, 0.5759587F, 0.1919862F));

        partdefinition.addOrReplaceChild("leg2part2",
                CubeListBuilder.create().texOffs(58, 0).mirror()
                        .addBox(12.0F, -1.5F, -1.5F, 3, 8, 3),
                PartPose.offsetAndRotation(4.0F, 10.0F, -1.0F, 0.0F, 0.5759587F, 0.1919862F));

        partdefinition.addOrReplaceChild("leg2part3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(13.0F, -1.0F, -1.0F, 2, 8, 2),
                PartPose.offsetAndRotation(4.0F, 10.0F, -1.0F, 0.0F, 0.5759587F, 0.6753028F));

        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(18, 0).mirror()
                        .addBox(-15.0F, -1.5F, -1.5F, 16, 3, 3),
                PartPose.offsetAndRotation(-4.0F, 10.0F, 1.0F, 0.0F, 0.2792527F, -0.1919862F));

        partdefinition.addOrReplaceChild("leg3part2",
                CubeListBuilder.create().texOffs(58, 0).mirror()
                        .addBox(-15.0F, -1.5F, -1.5F, 3, 8, 3),
                PartPose.offsetAndRotation(-4.0F, 10.0F, 1.0F, 0.0F, 0.2792527F, -0.1919862F));

        partdefinition.addOrReplaceChild("leg3part3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-15.0F, -1.0F, -1.0F, 2, 8, 2),
                PartPose.offsetAndRotation(-4.0F, 10.0F, 1.0F, 0.0F, 0.2792527F, -0.6753028F));

        partdefinition.addOrReplaceChild("leg4",
                CubeListBuilder.create().texOffs(18, 0).mirror()
                        .addBox(-1.0F, -1.5F, -1.5F, 16, 3, 3),
                PartPose.offsetAndRotation(4.0F, 10.0F, 1.0F, 0.0F, -0.2792527F, 0.1919862F));

        partdefinition.addOrReplaceChild("leg4part2",
                CubeListBuilder.create().texOffs(58, 0).mirror()
                        .addBox(12.0F, -1.5F, -1.5F, 3, 8, 3),
                PartPose.offsetAndRotation(4.0F, 10.0F, 1.0F, 0.0F, -0.2792527F, 0.1919862F));

        partdefinition.addOrReplaceChild("leg4part3",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(13.0F, -1.0F, -1.0F, 2, 8, 2),
                PartPose.offsetAndRotation(4.0F, 10.0F, 1.0F, 0.0F, -0.2792527F, 0.6753028F));

        partdefinition.addOrReplaceChild("leg5",
                CubeListBuilder.create().texOffs(119, 0).mirror()
                        .addBox(-4.0F, -1.5F, -1.5F, 25, 3, 3),
                PartPose.offsetAndRotation(4.0F, 10.0F, 4.0F, 0.0F, -1.134359F, 0.3407057F));

        partdefinition.addOrReplaceChild("leg5part2",
                CubeListBuilder.create().texOffs(18, 9).mirror()
                        .addBox(18.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offsetAndRotation(4.0F, 10.0F, 4.0F, 0.0F, -1.134359F, 0.3407057F));

        partdefinition.addOrReplaceChild("leg6",
                CubeListBuilder.create().texOffs(119, 0).mirror()
                        .addBox(-21.0F, -1.5F, -1.5F, 25, 3, 3),
                PartPose.offsetAndRotation(-4.0F, 10.0F, 4.0F, 0.0F, 1.134359F, -0.3407057F));

        partdefinition.addOrReplaceChild("leg6part2",
                CubeListBuilder.create().texOffs(18, 9).mirror()
                        .addBox(-21.0F, -1.5F, -1.5F, 3, 10, 3),
                PartPose.offsetAndRotation(-4.0F, 10.0F, 4.0F, 0.0F, 1.134359F, -0.3407057F));

        partdefinition.addOrReplaceChild("thorax",
                CubeListBuilder.create().texOffs(0, 42).mirror()
                        .addBox(-2.0F, -2.0F, -6.0F, 4, 4, 18),
                PartPose.offsetAndRotation(0.0F, 10.0F, 9.0F, -0.2602503F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("abdomen",
                CubeListBuilder.create().texOffs(118, 18).mirror()
                        .addBox(-3.0F, -3.0F, 0.0F, 6, 6, 16),
                PartPose.offset(0.0F, 13.0F, 20.0F));

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(27, 48).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 5),
                PartPose.offset(0.0F, 10.0F, -8.0F));

        partdefinition.addOrReplaceChild("jaw1",
                CubeListBuilder.create().texOffs(96, 31).mirror()
                        .addBox(-1.0F, -1.0F, -13.0F, 1, 2, 14),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1part2",
                CubeListBuilder.create().texOffs(39, 17).mirror()
                        .addBox(-1.1F, -2.0F, -5.0F, 1, 4, 5),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1tooth1",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(0.0F, -0.5F, -13.0F, 1, 1, 1),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1tooth2",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(0.0F, -0.5F, -11.0F, 1, 1, 1),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1tooth3",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(0.0F, -0.5F, -9.0F, 1, 1, 1),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1tooth4",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(0.0F, -0.5F, -7.0F, 1, 1, 1),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1tooth5",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(0.0F, -1.5F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw1tooth6",
                CubeListBuilder.create().texOffs(39, 27).mirror()
                        .addBox(0.0F, 0.5F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(-2.0F, 10.0F, -8.0F, 0.0F, 0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(0.0F, -1.0F, -13.0F, 1, 2, 14),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2part2",
                CubeListBuilder.create().texOffs(39, 7).mirror()
                        .addBox(0.1F, -2.0F, -5.0F, 1, 4, 5),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2tooth1",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(-1.0F, -0.5F, -13.0F, 1, 1, 1),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2tooth2",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(-1.0F, -0.5F, -11.0F, 1, 1, 1),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2tooth3",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(-1.0F, -0.5F, -9.0F, 1, 1, 1),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2tooth4",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(-1.0F, -0.5F, -7.0F, 1, 1, 1),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2tooth5",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(-1.0F, -1.5F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw2tooth6",
                CubeListBuilder.create().texOffs(96, 48).mirror()
                        .addBox(-1.0F, 0.5F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(2.0F, 10.0F, -8.0F, 0.0F, -0.4089656F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(-1.0F, -1.0F, -13.0F, 2, 1, 14),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3part2",
                CubeListBuilder.create().texOffs(0, 27).mirror()
                        .addBox(-2.0F, -1.0F, -5.0F, 4, 1, 5),
                PartPose.offsetAndRotation(0.0F, 7.9F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3tooth1",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(-0.5F, 0.0F, -13.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3tooth2",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(-0.5F, 0.0F, -11.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3tooth3",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(-0.5F, 0.0F, -9.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3tooth4",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(-0.5F, 0.0F, -7.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3tooth5",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(-1.5F, 0.0F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw3tooth6",
                CubeListBuilder.create().texOffs(95, 16).mirror()
                        .addBox(0.5F, 0.0F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 8.0F, -8.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(-1.0F, 0.0F, -13.0F, 2, 1, 14),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4part2",
                CubeListBuilder.create().texOffs(0, 20).mirror()
                        .addBox(-2.0F, 0.0F, -5.0F, 4, 1, 5),
                PartPose.offsetAndRotation(0.0F, 12.1F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4tooth1",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(-0.5F, -1.0F, -13.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4tooth2",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(-0.5F, -1.0F, -11.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4tooth3",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(-0.5F, -1.0F, -9.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4tooth4",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(-0.5F, -1.0F, -7.0F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4tooth5",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(-1.5F, -1.0F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("jaw4tooth6",
                CubeListBuilder.create().texOffs(95, 0).mirror()
                        .addBox(0.5F, -1.0F, -4.5F, 1, 1, 1),
                PartPose.offsetAndRotation(0.0F, 12.0F, -8.0F, 0.4089656F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tonguepart1",
                CubeListBuilder.create().texOffs(24, 34).mirror()
                        .addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5),
                PartPose.offsetAndRotation(1.6F, 9.3F, -15.0F, 1.041001F, 1.264073F, -1.07818F));

        partdefinition.addOrReplaceChild("tonguepart2",
                CubeListBuilder.create().texOffs(0, 46).mirror()
                        .addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.0F, 10.0F, -11.0F, -0.1858931F, -0.2230717F, 0.669215F));

        partdefinition.addOrReplaceChild("tonguepart3",
                CubeListBuilder.create().texOffs(24, 27).mirror()
                        .addBox(-0.5F, -0.5F, -5.0F, 1, 1, 5),
                PartPose.offsetAndRotation(0.2F, 11.3F, -19.0F, -0.2602503F, 0.3717861F, -1.07818F));

        partdefinition.addOrReplaceChild("wing_1",
                CubeListBuilder.create().texOffs(108, 42).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 0, 22),
                PartPose.offsetAndRotation(-2.0F, 6.0F, -5.0F, 0.5948578F, -0.9294653F, 0.0F));

        partdefinition.addOrReplaceChild("wing_2",
                CubeListBuilder.create().texOffs(141, 42).mirror()
                        .addBox(-4.0F, 0.0F, 0.0F, 8, 0, 22),
                PartPose.offsetAndRotation(2.0F, 6.0F, -5.0F, 0.5948606F, 0.9294576F, 0.0F));

        partdefinition.addOrReplaceChild("wing_3",
                CubeListBuilder.create().texOffs(64, 27).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 0, 18),
                PartPose.offsetAndRotation(-2.0F, 6.0F, -1.0F, 0.3346075F, -0.4089647F, 0.0F));

        partdefinition.addOrReplaceChild("wing_4",
                CubeListBuilder.create().texOffs(153, 17).mirror()
                        .addBox(-2.0F, 0.0F, 0.0F, 4, 0, 18),
                PartPose.offsetAndRotation(2.0F, 6.0F, -1.0F, 0.3346075F, 0.4089656F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 64);
    }

    @Override
    public void setupAnim(EntityLurkingTerror entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float legspeed = 0.7f;
        float mouthspeed = 0.9f;
        newangle = ageInTicks * legspeed * limbSwingAmount % ((float)Math.PI * 2);
        newangle = Math.abs(newangle);
        if (newangle < rf1) {
        ri1 = 0;
        if (entity.getRandom().nextInt(3) == 1) {
        ri1 |= 1;
        }
        if (entity.getRandom().nextInt(3) == 1) {
        ri1 |= 2;
        }
        if (entity.getRandom().nextInt(4) == 1) {
        ri1 |= 4;
        }
        if (entity.getRandom().nextInt(4) == 1) {
        ri1 |= 8;
        }
        if (entity.getRandom().nextInt(6) == 1) {
        ri1 |= 0x10;
        }
        if (entity.getRandom().nextInt(6) == 1) {
        ri1 |= 0x20;
        }
        }
        rf1 = newangle;
        newangle = ageInTicks * mouthspeed * limbSwingAmount % ((float)Math.PI * 2);
        if ((newangle = Math.abs(newangle)) < rf2) {
        ri2 = 0;
        if (entity.getRandom().nextInt(20) == 1) {
        ri2 |= 1;
        }
        if (entity.getAttacking() != 0) {
        ri2 = 1;
        }
        }
        rf2 = newangle;
        newangle = 0.0f;
        if ((ri1 & 1) != 0) {
        newangle = Mth.sin((float)(ageInTicks * legspeed * limbSwingAmount)) * (float)Math.PI * 0.25f;
        }
        this.leg2.zRot = this.leg2part2.zRot = 0.191f + newangle;
        this.leg2part3.zRot = 0.675f + newangle;
        newangle = 0.0f;
        if ((ri1 & 2) != 0) {
        newangle = Mth.sin((float)(ageInTicks * legspeed * limbSwingAmount)) * (float)Math.PI * 0.25f;
        }
        this.leg1.zRot = this.leg1part2.zRot = -0.191f + newangle;
        this.leg1part3.zRot = -0.675f + newangle;
        newangle = 0.0f;
        if ((ri1 & 4) != 0) {
        newangle = Mth.sin((float)(ageInTicks * legspeed * limbSwingAmount)) * (float)Math.PI * 0.15f;
        }
        this.leg4.zRot = this.leg4part2.zRot = 0.191f + newangle;
        this.leg4part3.zRot = 0.675f + newangle;
        newangle = 0.0f;
        if ((ri1 & 8) != 0) {
        newangle = Mth.sin((float)(ageInTicks * legspeed * limbSwingAmount)) * (float)Math.PI * 0.15f;
        }
        this.leg3.zRot = this.leg3part2.zRot = -0.191f + newangle;
        this.leg3part3.zRot = -0.675f + newangle;
        newangle = 0.0f;
        if ((ri1 & 0x10) != 0) {
        newangle = Mth.sin((float)(ageInTicks * legspeed * limbSwingAmount)) * (float)Math.PI * 0.1f;
        }
        this.leg6.zRot = this.leg6part2.zRot = -0.34f + newangle;
        newangle = 0.0f;
        if ((ri1 & 0x20) != 0) {
        newangle = Mth.sin((float)(ageInTicks * legspeed * limbSwingAmount)) * (float)Math.PI * 0.1f;
        }
        this.leg5.zRot = this.leg5part2.zRot = 0.34f + newangle;
        newangle = 0.0f;
        if ((ri2 & 1) != 0) {
        newangle = Mth.sin((float)(ageInTicks * mouthspeed * limbSwingAmount)) * (float)Math.PI * 0.35f;
        newangle = Math.abs(newangle);
        }
        this.jaw1.yRot = newangle;
        this.jaw1part2.yRot = newangle;
        this.jaw1tooth3.yRot = this.jaw1tooth5.yRot = newangle;
        this.jaw1tooth1.yRot = this.jaw1tooth5.yRot;
        this.jaw1tooth4.yRot = this.jaw1tooth6.yRot = newangle;
        this.jaw1tooth2.yRot = this.jaw1tooth6.yRot;
        this.jaw2.yRot = -newangle;
        this.jaw2part2.yRot = -newangle;
        this.jaw2tooth3.yRot = this.jaw2tooth5.yRot = -newangle;
        this.jaw2tooth1.yRot = this.jaw2tooth5.yRot;
        this.jaw2tooth4.yRot = this.jaw2tooth6.yRot = -newangle;
        this.jaw2tooth2.yRot = this.jaw2tooth6.yRot;
        this.jaw3.xRot = -newangle;
        this.jaw3part2.xRot = -newangle;
        this.jaw3tooth3.xRot = this.jaw3tooth5.xRot = -newangle;
        this.jaw3tooth1.xRot = this.jaw3tooth5.xRot;
        this.jaw3tooth4.xRot = this.jaw3tooth6.xRot = -newangle;
        this.jaw3tooth2.xRot = this.jaw3tooth6.xRot;
        this.jaw4.xRot = newangle;
        this.jaw4part2.xRot = newangle;
        this.jaw4tooth3.xRot = this.jaw4tooth5.xRot = newangle;
        this.jaw4tooth1.xRot = this.jaw4tooth5.xRot;
        this.jaw4tooth4.xRot = this.jaw4tooth6.xRot = newangle;
        this.jaw4tooth2.xRot = this.jaw4tooth6.xRot;
        this.tonguepart3.xRot = 0.0f;
        this.tonguepart2.xRot = 0.0f;
        this.tonguepart1.xRot = 0.0f;
        this.tonguepart3.yRot = 0.0f;
        this.tonguepart2.yRot = 0.0f;
        this.tonguepart1.yRot = 0.0f;
        this.tonguepart3.zRot = 0.0f;
        this.tonguepart2.zRot = 0.0f;
        this.tonguepart1.zRot = 0.0f;
        this.tonguepart1.x = this.tonguepart3.x = this.tonguepart2.x;
        this.tonguepart1.y = this.tonguepart3.y = this.tonguepart2.y;
        this.tonguepart1.z = this.tonguepart2.z - newangle * 5.0f;
        this.tonguepart3.z = this.tonguepart2.z - newangle * 10.0f;
        this.thorax.xRot = newangle = Mth.sin((float)(ageInTicks * 0.1f * limbSwingAmount)) * (float)Math.PI * 0.06f;
        this.abdomen.y = (float)((double)this.thorax.y - Math.sin(newangle) * 14.0);
        newangle = Mth.cos((float)(ageInTicks * 1.4f * limbSwingAmount)) * (float)Math.PI * 0.2f;
        this.wing_1.xRot = 0.455f + newangle;
        this.wing_2.xRot = 0.455f + newangle;
        this.wing_3.xRot = 0.455f - newangle;
        this.wing_4.xRot = 0.455f - newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg5part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg6part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.thorax.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.abdomen.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw1tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw2tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw3tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.jaw4tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tonguepart1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tonguepart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tonguepart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.wing_4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
