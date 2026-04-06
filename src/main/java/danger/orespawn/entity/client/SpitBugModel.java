package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.EntitySpitBug;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class SpitBugModel extends EntityModel<EntitySpitBug> {
    private final ModelPart legintersection;
    private final ModelPart legintersectionpart2;
    private final ModelPart legintersectionpart3;
    private final ModelPart leg1start;
    private final ModelPart leg1startpart2;
    private final ModelPart leg1startpart3;
    private final ModelPart leg1;
    private final ModelPart leg1part2;
    private final ModelPart leg1part2b;
    private final ModelPart leg1part2c;
    private final ModelPart leg1part2d;
    private final ModelPart leg1part3;
    private final ModelPart leg1part3b;
    private final ModelPart leg1part3c;
    private final ModelPart leg2start;
    private final ModelPart leg2startpart2;
    private final ModelPart leg2startpart3;
    private final ModelPart leg2;
    private final ModelPart leg2part2;
    private final ModelPart leg2part2b;
    private final ModelPart leg2part2c;
    private final ModelPart leg2part2d;
    private final ModelPart leg2part3;
    private final ModelPart leg2part3b;
    private final ModelPart leg2part3c;
    private final ModelPart leg3start;
    private final ModelPart leg3startpart2;
    private final ModelPart leg3startpart3;
    private final ModelPart leg3;
    private final ModelPart leg3part2;
    private final ModelPart leg3part2b;
    private final ModelPart leg3part2c;
    private final ModelPart leg3part2d;
    private final ModelPart leg3part3;
    private final ModelPart leg3part3b;
    private final ModelPart leg3part3c;
    private final ModelPart leg4start;
    private final ModelPart leg4startpart2;
    private final ModelPart leg4startpart3;
    private final ModelPart leg4;
    private final ModelPart leg4part2;
    private final ModelPart leg4part2b;
    private final ModelPart leg4part2c;
    private final ModelPart leg4part2d;
    private final ModelPart leg4part3;
    private final ModelPart leg4part3b;
    private final ModelPart leg4part3c;
    private final ModelPart bodybase;
    private final ModelPart bodybasepart2;
    private final ModelPart bodybasepart3;
    private final ModelPart bodybasepart4;
    private final ModelPart bodybasepart5;
    private final ModelPart bodybasepart6;
    private final ModelPart bodybasepart7;
    private final ModelPart bodybasepart8;
    private final ModelPart bodybasepart9;
    private final ModelPart bodybasepart10;
    private final ModelPart bodybasepart11;
    private final ModelPart bodybasepart12;
    private final ModelPart bodybasepart13;
    private final ModelPart bodybasepart14;
    private final ModelPart bodybasepart15;
    private final ModelPart upperjawbase;
    private final ModelPart upperjawbasepart1;
    private final ModelPart upperjawbasepart2;
    private final ModelPart upperjawbasepart3;
    private final ModelPart tooth1;
    private final ModelPart tooth2;
    private final ModelPart tooth3;
    private final ModelPart tooth4;
    private final ModelPart tooth5;
    private final ModelPart lowerjawbase;
    private final ModelPart lowerjawbasepart1;
    private final ModelPart lowerjawbasepart2;
    private final ModelPart lowerjawbasepart3;
    private final ModelPart lowerjawbasepart4;
    private final ModelPart lowerjawbasepart5;
    private final ModelPart lowerjawbasepart6;
    private final ModelPart lowerjawbasepart7;
    private final ModelPart lowerjawbasepart8;
    private final ModelPart lowerjawbasepart9;
    private final ModelPart lowerjawbasepart10;
    private final ModelPart lowerjawbasepart11;
    private final ModelPart arm1start;
    private final ModelPart arm1;
    private final ModelPart arm1part2;
    private final ModelPart arm1end;
    private final ModelPart arm2start;
    private final ModelPart arm2;
    private final ModelPart arm2part2;
    private final ModelPart arm2end;
    private final ModelPart eye1;
    private final ModelPart eye2;

    public SpitBugModel(ModelPart root) {
        this.legintersection = root.getChild("legintersection");
        this.legintersectionpart2 = root.getChild("legintersectionpart2");
        this.legintersectionpart3 = root.getChild("legintersectionpart3");
        this.leg1start = root.getChild("leg1start");
        this.leg1startpart2 = root.getChild("leg1startpart2");
        this.leg1startpart3 = root.getChild("leg1startpart3");
        this.leg1 = root.getChild("leg1");
        this.leg1part2 = root.getChild("leg1part2");
        this.leg1part2b = root.getChild("leg1part2b");
        this.leg1part2c = root.getChild("leg1part2c");
        this.leg1part2d = root.getChild("leg1part2d");
        this.leg1part3 = root.getChild("leg1part3");
        this.leg1part3b = root.getChild("leg1part3b");
        this.leg1part3c = root.getChild("leg1part3c");
        this.leg2start = root.getChild("leg2start");
        this.leg2startpart2 = root.getChild("leg2startpart2");
        this.leg2startpart3 = root.getChild("leg2startpart3");
        this.leg2 = root.getChild("leg2");
        this.leg2part2 = root.getChild("leg2part2");
        this.leg2part2b = root.getChild("leg2part2b");
        this.leg2part2c = root.getChild("leg2part2c");
        this.leg2part2d = root.getChild("leg2part2d");
        this.leg2part3 = root.getChild("leg2part3");
        this.leg2part3b = root.getChild("leg2part3b");
        this.leg2part3c = root.getChild("leg2part3c");
        this.leg3start = root.getChild("leg3start");
        this.leg3startpart2 = root.getChild("leg3startpart2");
        this.leg3startpart3 = root.getChild("leg3startpart3");
        this.leg3 = root.getChild("leg3");
        this.leg3part2 = root.getChild("leg3part2");
        this.leg3part2b = root.getChild("leg3part2b");
        this.leg3part2c = root.getChild("leg3part2c");
        this.leg3part2d = root.getChild("leg3part2d");
        this.leg3part3 = root.getChild("leg3part3");
        this.leg3part3b = root.getChild("leg3part3b");
        this.leg3part3c = root.getChild("leg3part3c");
        this.leg4start = root.getChild("leg4start");
        this.leg4startpart2 = root.getChild("leg4startpart2");
        this.leg4startpart3 = root.getChild("leg4startpart3");
        this.leg4 = root.getChild("leg4");
        this.leg4part2 = root.getChild("leg4part2");
        this.leg4part2b = root.getChild("leg4part2b");
        this.leg4part2c = root.getChild("leg4part2c");
        this.leg4part2d = root.getChild("leg4part2d");
        this.leg4part3 = root.getChild("leg4part3");
        this.leg4part3b = root.getChild("leg4part3b");
        this.leg4part3c = root.getChild("leg4part3c");
        this.bodybase = root.getChild("bodybase");
        this.bodybasepart2 = root.getChild("bodybasepart2");
        this.bodybasepart3 = root.getChild("bodybasepart3");
        this.bodybasepart4 = root.getChild("bodybasepart4");
        this.bodybasepart5 = root.getChild("bodybasepart5");
        this.bodybasepart6 = root.getChild("bodybasepart6");
        this.bodybasepart7 = root.getChild("bodybasepart7");
        this.bodybasepart8 = root.getChild("bodybasepart8");
        this.bodybasepart9 = root.getChild("bodybasepart9");
        this.bodybasepart10 = root.getChild("bodybasepart10");
        this.bodybasepart11 = root.getChild("bodybasepart11");
        this.bodybasepart12 = root.getChild("bodybasepart12");
        this.bodybasepart13 = root.getChild("bodybasepart13");
        this.bodybasepart14 = root.getChild("bodybasepart14");
        this.bodybasepart15 = root.getChild("bodybasepart15");
        this.upperjawbase = root.getChild("upperjawbase");
        this.upperjawbasepart1 = root.getChild("upperjawbasepart1");
        this.upperjawbasepart2 = root.getChild("upperjawbasepart2");
        this.upperjawbasepart3 = root.getChild("upperjawbasepart3");
        this.tooth1 = root.getChild("tooth1");
        this.tooth2 = root.getChild("tooth2");
        this.tooth3 = root.getChild("tooth3");
        this.tooth4 = root.getChild("tooth4");
        this.tooth5 = root.getChild("tooth5");
        this.lowerjawbase = root.getChild("lowerjawbase");
        this.lowerjawbasepart1 = root.getChild("lowerjawbasepart1");
        this.lowerjawbasepart2 = root.getChild("lowerjawbasepart2");
        this.lowerjawbasepart3 = root.getChild("lowerjawbasepart3");
        this.lowerjawbasepart4 = root.getChild("lowerjawbasepart4");
        this.lowerjawbasepart5 = root.getChild("lowerjawbasepart5");
        this.lowerjawbasepart6 = root.getChild("lowerjawbasepart6");
        this.lowerjawbasepart7 = root.getChild("lowerjawbasepart7");
        this.lowerjawbasepart8 = root.getChild("lowerjawbasepart8");
        this.lowerjawbasepart9 = root.getChild("lowerjawbasepart9");
        this.lowerjawbasepart10 = root.getChild("lowerjawbasepart10");
        this.lowerjawbasepart11 = root.getChild("lowerjawbasepart11");
        this.arm1start = root.getChild("arm1start");
        this.arm1 = root.getChild("arm1");
        this.arm1part2 = root.getChild("arm1part2");
        this.arm1end = root.getChild("arm1end");
        this.arm2start = root.getChild("arm2start");
        this.arm2 = root.getChild("arm2");
        this.arm2part2 = root.getChild("arm2part2");
        this.arm2end = root.getChild("arm2end");
        this.eye1 = root.getChild("eye1");
        this.eye2 = root.getChild("eye2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("legintersection",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-6.0F, -2.0F, -7.0F, 12, 6, 14),
                PartPose.offset(0.0F, 3.0F, 1.0F));

        partdefinition.addOrReplaceChild("legintersectionpart2",
                CubeListBuilder.create().texOffs(0, 21).mirror()
                        .addBox(-8.0F, -2.0F, 0.0F, 8, 6, 8),
                PartPose.offsetAndRotation(0.0F, 3.0F, 2.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("legintersectionpart3",
                CubeListBuilder.create().texOffs(282, 18).mirror()
                        .addBox(-7.0F, 3.0F, -6.0F, 14, 2, 12),
                PartPose.offset(0.0F, 3.0F, 1.0F));

        partdefinition.addOrReplaceChild("leg1start",
                CubeListBuilder.create().texOffs(53, 0).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 4),
                PartPose.offsetAndRotation(4.0F, 3.0F, -4.0F, 0.0F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1startpart2",
                CubeListBuilder.create().texOffs(53, 19).mirror()
                        .addBox(-2.5F, -2.0F, -3.0F, 5, 5, 4),
                PartPose.offsetAndRotation(6.0F, 3.0F, -6.0F, 0.3717861F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1startpart3",
                CubeListBuilder.create().texOffs(53, 29).mirror()
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 2),
                PartPose.offsetAndRotation(6.0F, 3.0F, -6.0F, 0.669215F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(45, 36).mirror()
                        .addBox(-1.5F, -3.0F, -13.0F, 3, 3, 10),
                PartPose.offsetAndRotation(6.0F, 3.0F, -6.0F, 1.041001F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1part2",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, -1.5F, -13.0F, 4, 3, 15),
                PartPose.offsetAndRotation(12.0F, 13.0F, -12.0F, -1.152537F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1part2b",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, 0.5F, 1.0F, 4, 1, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, -12.0F, -0.7435722F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1part2c",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, -7.5F, -13.0F, 4, 1, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, -12.0F, -0.6320451F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1part2d",
                CubeListBuilder.create().texOffs(2, 50).mirror()
                        .addBox(-1.5F, -1.5F, -10.0F, 3, 3, 12),
                PartPose.offsetAndRotation(12.0F, 13.0F, -12.0F, -1.041001F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1part3",
                CubeListBuilder.create().texOffs(51, 69).mirror()
                        .addBox(-1.5F, -1.5F, -7.0F, 3, 3, 7),
                PartPose.offsetAndRotation(16.0F, 3.0F, -16.0F, 0.669215F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg1part3b",
                CubeListBuilder.create().texOffs(55, 80).mirror()
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 16, 4),
                PartPose.offsetAndRotation(20.0F, 8.0F, -20.0F, -0.4833219F, -0.7853982F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg1part3c",
                CubeListBuilder.create().texOffs(42, 80).mirror()
                        .addBox(-2.0F, 14.0F, 0.0F, 4, 3, 2),
                PartPose.offsetAndRotation(20.0F, 8.0F, -20.0F, -0.4833219F, -0.7853982F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg2start",
                CubeListBuilder.create().texOffs(52, 0).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 4),
                PartPose.offsetAndRotation(-4.0F, 3.0F, -4.0F, 0.0F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2startpart2",
                CubeListBuilder.create().texOffs(53, 19).mirror()
                        .addBox(-2.5F, -2.0F, -3.0F, 5, 5, 4),
                PartPose.offsetAndRotation(-6.0F, 3.0F, -6.0F, 0.3717861F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2startpart3",
                CubeListBuilder.create().texOffs(53, 29).mirror()
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 2),
                PartPose.offsetAndRotation(-6.0F, 3.0F, -6.0F, 0.669215F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create().texOffs(45, 36).mirror()
                        .addBox(-1.5F, -3.0F, -13.0F, 3, 3, 10),
                PartPose.offsetAndRotation(-6.0F, 3.0F, -6.0F, 1.041001F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2part2",
                CubeListBuilder.create().texOffs(72, 50).mirror()
                        .addBox(-2.0F, -1.5F, -13.0F, 4, 3, 15),
                PartPose.offsetAndRotation(-12.0F, 13.0F, -12.0F, -1.152537F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2part2b",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, 0.5F, 1.0F, 4, 1, 3),
                PartPose.offsetAndRotation(-12.0F, 13.0F, -12.0F, -0.7435722F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2part2c",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, -7.5F, -13.0F, 4, 1, 3),
                PartPose.offsetAndRotation(-12.0F, 13.0F, -12.0F, -0.6320451F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2part2d",
                CubeListBuilder.create().texOffs(2, 50).mirror()
                        .addBox(-1.5F, -1.5F, -10.0F, 3, 3, 12),
                PartPose.offsetAndRotation(-12.0F, 13.0F, -12.0F, -1.041001F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2part3",
                CubeListBuilder.create().texOffs(51, 69).mirror()
                        .addBox(-1.5F, -1.5F, -7.0F, 3, 3, 7),
                PartPose.offsetAndRotation(-16.0F, 3.0F, -16.0F, 0.669215F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leg2part3b",
                CubeListBuilder.create().texOffs(55, 80).mirror()
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 16, 4),
                PartPose.offsetAndRotation(-20.0F, 8.0F, -20.0F, -0.4833219F, 0.7853982F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg2part3c",
                CubeListBuilder.create().texOffs(42, 80).mirror()
                        .addBox(-2.0F, 14.0F, 0.0F, 4, 3, 2),
                PartPose.offsetAndRotation(-20.0F, 8.0F, -20.0F, -0.4833219F, 0.7853982F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg3start",
                CubeListBuilder.create().texOffs(52, 0).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 4),
                PartPose.offsetAndRotation(4.0F, 3.0F, 6.0F, 0.0F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3startpart2",
                CubeListBuilder.create().texOffs(72, 19).mirror()
                        .addBox(-2.5F, -2.0F, -3.0F, 5, 5, 4),
                PartPose.offsetAndRotation(6.0F, 3.0F, 8.0F, 0.3717861F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3startpart3",
                CubeListBuilder.create().texOffs(72, 29).mirror()
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 2),
                PartPose.offsetAndRotation(6.0F, 3.0F, 8.0F, 0.669215F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create().texOffs(72, 36).mirror()
                        .addBox(-1.5F, -3.0F, -13.0F, 3, 3, 10),
                PartPose.offsetAndRotation(6.0F, 3.0F, 8.0F, 1.041001F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3part2",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, -1.5F, -13.0F, 4, 3, 15),
                PartPose.offsetAndRotation(12.0F, 13.0F, 14.0F, -1.152537F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3part2b",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, 0.5F, 1.0F, 4, 1, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, 14.0F, -0.7435722F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3part2c",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, -7.5F, -13.0F, 4, 1, 3),
                PartPose.offsetAndRotation(12.0F, 13.0F, 14.0F, -0.6320451F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3part2d",
                CubeListBuilder.create().texOffs(111, 50).mirror()
                        .addBox(-1.5F, -1.5F, -10.0F, 3, 3, 12),
                PartPose.offsetAndRotation(12.0F, 13.0F, 14.0F, -1.041001F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3part3",
                CubeListBuilder.create().texOffs(72, 69).mirror()
                        .addBox(-1.5F, -1.5F, -7.0F, 3, 3, 7),
                PartPose.offsetAndRotation(16.0F, 3.0F, 18.0F, 0.669215F, -2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg3part3b",
                CubeListBuilder.create().texOffs(72, 80).mirror()
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 16, 4),
                PartPose.offsetAndRotation(20.0F, 8.0F, 22.0F, -0.4833219F, -2.356194F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg3part3c",
                CubeListBuilder.create().texOffs(89, 80).mirror()
                        .addBox(-2.0F, 14.0F, 0.0F, 4, 3, 2),
                PartPose.offsetAndRotation(20.0F, 8.0F, 22.0F, -0.4833219F, -2.356194F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg4start",
                CubeListBuilder.create().texOffs(52, 0).mirror()
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 4),
                PartPose.offsetAndRotation(-4.0F, 3.0F, 6.0F, 0.0F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4startpart2",
                CubeListBuilder.create().texOffs(72, 19).mirror()
                        .addBox(-2.5F, -2.0F, -3.0F, 5, 5, 4),
                PartPose.offsetAndRotation(-6.0F, 3.0F, 8.0F, 0.3717861F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4startpart3",
                CubeListBuilder.create().texOffs(72, 29).mirror()
                        .addBox(-2.0F, -2.0F, -4.0F, 4, 4, 2),
                PartPose.offsetAndRotation(-6.0F, 3.0F, 8.0F, 0.669215F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4",
                CubeListBuilder.create().texOffs(72, 36).mirror()
                        .addBox(-1.5F, -3.0F, -13.0F, 3, 3, 10),
                PartPose.offsetAndRotation(-6.0F, 3.0F, 8.0F, 1.041001F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4part2",
                CubeListBuilder.create().texOffs(72, 50).mirror()
                        .addBox(-2.0F, -1.5F, -13.0F, 4, 3, 15),
                PartPose.offsetAndRotation(-12.0F, 13.0F, 14.0F, -1.152537F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4part2b",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, 0.5F, 1.0F, 4, 1, 3),
                PartPose.offsetAndRotation(-12.0F, 13.0F, 14.0F, -0.7435722F, 2.363176F, 0.0F));

        partdefinition.addOrReplaceChild("leg4part2c",
                CubeListBuilder.create().texOffs(33, 50).mirror()
                        .addBox(-2.0F, -7.5F, -13.0F, 4, 1, 3),
                PartPose.offsetAndRotation(-12.0F, 13.0F, 14.0F, -0.6320451F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4part2d",
                CubeListBuilder.create().texOffs(111, 50).mirror()
                        .addBox(-1.5F, -1.5F, -10.0F, 3, 3, 12),
                PartPose.offsetAndRotation(-12.0F, 13.0F, 14.0F, -1.041001F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4part3",
                CubeListBuilder.create().texOffs(72, 69).mirror()
                        .addBox(-1.5F, -1.5F, -7.0F, 3, 3, 7),
                PartPose.offsetAndRotation(-16.0F, 3.0F, 18.0F, 0.669215F, 2.356194F, 0.0F));

        partdefinition.addOrReplaceChild("leg4part3b",
                CubeListBuilder.create().texOffs(72, 80).mirror()
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 16, 4),
                PartPose.offsetAndRotation(-20.0F, 8.0F, 22.0F, -0.4833219F, 2.356194F, -0.0349066F));

        partdefinition.addOrReplaceChild("leg4part3c",
                CubeListBuilder.create().texOffs(42, 80).mirror()
                        .addBox(-2.0F, 14.0F, 0.0F, 4, 3, 2),
                PartPose.offsetAndRotation(-20.0F, 8.0F, 22.0F, -0.4833219F, 2.356194F, -0.0349066F));

        partdefinition.addOrReplaceChild("bodybase",
                CubeListBuilder.create().texOffs(98, 0).mirror()
                        .addBox(-5.0F, -20.0F, -6.5F, 10, 20, 13),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart2",
                CubeListBuilder.create().texOffs(146, 0).mirror()
                        .addBox(-6.0F, -20.0F, -7.5F, 12, 12, 21),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart3",
                CubeListBuilder.create().texOffs(213, 0).mirror()
                        .addBox(-6.5F, -20.0F, -7.5F, 13, 12, 21),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart4",
                CubeListBuilder.create().texOffs(132, 34).mirror()
                        .addBox(-5.0F, -18.0F, -16.5F, 10, 8, 9),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart5",
                CubeListBuilder.create().texOffs(172, 36).mirror()
                        .addBox(-5.0F, -19.0F, 13.5F, 10, 10, 5),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart6",
                CubeListBuilder.create().texOffs(142, 53).mirror()
                        .addBox(-4.5F, -18.0F, 18.5F, 9, 4, 3),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart7",
                CubeListBuilder.create().texOffs(167, 53).mirror()
                        .addBox(-2.5F, -26.0F, -10.5F, 5, 2, 9),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -1.264073F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodybasepart8",
                CubeListBuilder.create().texOffs(111, 68).mirror()
                        .addBox(-6.0F, -13.0F, -18.5F, 12, 10, 11),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -0.4089647F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodybasepart9",
                CubeListBuilder.create().texOffs(157, 66).mirror()
                        .addBox(-7.5F, -14.0F, -11.5F, 15, 16, 7),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -1.412787F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodybasepart10",
                CubeListBuilder.create().texOffs(204, 35).mirror()
                        .addBox(-7.5F, -22.0F, -9.5F, 15, 5, 22),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart11",
                CubeListBuilder.create().texOffs(204, 63).mirror()
                        .addBox(-6.5F, -21.0F, -14.5F, 13, 4, 5),
                PartPose.offset(0.0F, 1.0F, 1.0F));

        partdefinition.addOrReplaceChild("bodybasepart12",
                CubeListBuilder.create().texOffs(282, 0).mirror()
                        .addBox(-5.0F, -3.0F, 2.5F, 10, 4, 12),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, 0.4833219F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodybasepart13",
                CubeListBuilder.create().texOffs(327, 0).mirror()
                        .addBox(4.0F, 1.0F, -3.5F, 4, 2, 10),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, 0.1858931F, 0.0F, -1.003822F));

        partdefinition.addOrReplaceChild("bodybasepart14",
                CubeListBuilder.create().texOffs(327, 0).mirror()
                        .addBox(-8.0F, 1.0F, -3.5F, 4, 2, 10),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, 0.185895F, 0.0F, 1.003826F));

        partdefinition.addOrReplaceChild("bodybasepart15",
                CubeListBuilder.create().texOffs(144, 91).mirror()
                        .addBox(-2.5F, -25.0F, 1.5F, 5, 3, 7),
                PartPose.offsetAndRotation(0.0F, 1.0F, 1.0F, -0.7504916F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("upperjawbase",
                CubeListBuilder.create().texOffs(0, 37).mirror()
                        .addBox(-3.5F, -2.0F, -6.0F, 7, 5, 6),
                PartPose.offset(0.0F, -13.0F, -15.0F));

        partdefinition.addOrReplaceChild("upperjawbasepart1",
                CubeListBuilder.create().texOffs(35, 28).mirror()
                        .addBox(-4.5F, -1.0F, -7.0F, 3, 3, 2),
                PartPose.offset(0.0F, -13.0F, -15.0F));

        partdefinition.addOrReplaceChild("upperjawbasepart2",
                CubeListBuilder.create().texOffs(35, 28).mirror()
                        .addBox(1.5F, -1.0F, -7.0F, 3, 3, 2),
                PartPose.offset(0.0F, -13.0F, -15.0F));

        partdefinition.addOrReplaceChild("upperjawbasepart3",
                CubeListBuilder.create().texOffs(27, 37).mirror()
                        .addBox(-1.0F, -1.0F, -7.0F, 2, 3, 2),
                PartPose.offset(0.0F, -13.0F, -15.0F));

        partdefinition.addOrReplaceChild("tooth1",
                CubeListBuilder.create().texOffs(116, 34).mirror()
                        .addBox(-1.5F, -2.0F, -14.0F, 2, 2, 7),
                PartPose.offsetAndRotation(0.0F, -13.0F, -15.0F, 0.2602503F, 0.3717861F, 0.0F));

        partdefinition.addOrReplaceChild("tooth2",
                CubeListBuilder.create().texOffs(116, 34).mirror()
                        .addBox(-0.5F, -2.0F, -14.0F, 2, 2, 7),
                PartPose.offsetAndRotation(0.0F, -13.0F, -15.0F, 0.2602503F, -0.3717861F, 0.0F));

        partdefinition.addOrReplaceChild("tooth3",
                CubeListBuilder.create().texOffs(116, 34).mirror()
                        .addBox(-1.0F, -2.0F, -14.0F, 2, 2, 7),
                PartPose.offsetAndRotation(0.0F, -13.0F, -15.0F, 0.2602503F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tooth4",
                CubeListBuilder.create().texOffs(90, 111).mirror()
                        .addBox(-5.5F, 1.5F, -23.5F, 3, 2, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tooth5",
                CubeListBuilder.create().texOffs(90, 111).mirror()
                        .addBox(2.5F, 1.5F, -23.5F, 3, 2, 5),
                PartPose.offsetAndRotation(0.0F, 0.0F, -6.0F, -0.2230717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lowerjawbase",
                CubeListBuilder.create().texOffs(90, 91).mirror()
                        .addBox(-5.0F, -1.0F, -15.5F, 10, 2, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart1",
                CubeListBuilder.create().texOffs(0, 69).mirror()
                        .addBox(-5.0F, -3.0F, -15.5F, 1, 2, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart2",
                CubeListBuilder.create().texOffs(0, 69).mirror()
                        .addBox(4.0F, -3.0F, -15.5F, 1, 2, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart3",
                CubeListBuilder.create().texOffs(0, 88).mirror()
                        .addBox(-4.0F, -2.0F, -15.5F, 1, 1, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart4",
                CubeListBuilder.create().texOffs(0, 88).mirror()
                        .addBox(3.0F, -2.0F, -15.5F, 1, 1, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart5",
                CubeListBuilder.create().texOffs(35, 134).mirror()
                        .addBox(5.0F, -5.0F, -16.5F, 2, 4, 20),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart6",
                CubeListBuilder.create().texOffs(35, 109).mirror()
                        .addBox(-7.0F, -5.0F, -16.5F, 2, 4, 20),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart7",
                CubeListBuilder.create().texOffs(73, 101).mirror()
                        .addBox(-6.0F, -3.0F, -19.5F, 4, 3, 4),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart8",
                CubeListBuilder.create().texOffs(73, 101).mirror()
                        .addBox(2.0F, -3.0F, -19.5F, 4, 3, 4),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart9",
                CubeListBuilder.create().texOffs(95, 72).mirror()
                        .addBox(-2.0F, -2.0F, -18.5F, 4, 2, 3),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart10",
                CubeListBuilder.create().texOffs(0, 106).mirror()
                        .addBox(-8.0F, -3.0F, -12.5F, 1, 2, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("lowerjawbasepart11",
                CubeListBuilder.create().texOffs(0, 106).mirror()
                        .addBox(7.0F, -3.0F, -12.5F, 1, 2, 16),
                PartPose.offset(0.0F, 0.0F, -6.0F));

        partdefinition.addOrReplaceChild("arm1start",
                CubeListBuilder.create().texOffs(0, 50).mirror()
                        .addBox(-0.5F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(5.0F, -11.0F, -14.0F));

        partdefinition.addOrReplaceChild("arm1",
                CubeListBuilder.create().texOffs(9, 125).mirror()
                        .addBox(-0.5F, -1.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(7.0F, -11.0F, -14.0F, -0.8922867F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm1part2",
                CubeListBuilder.create().texOffs(9, 133).mirror()
                        .addBox(-0.5F, -1.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(7.0F, -9.0F, -16.5F, 0.7435722F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm1end",
                CubeListBuilder.create().texOffs(9, 141).mirror()
                        .addBox(1.0F, 3.0F, 1.0F, 1, 1, 2),
                PartPose.offsetAndRotation(6.0F, -9.0F, -16.5F, 0.7435722F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm2start",
                CubeListBuilder.create().texOffs(0, 50).mirror()
                        .addBox(-2.5F, -1.0F, -1.0F, 3, 2, 2),
                PartPose.offset(-5.0F, -11.0F, -14.0F));

        partdefinition.addOrReplaceChild("arm2",
                CubeListBuilder.create().texOffs(0, 125).mirror()
                        .addBox(-1.5F, -1.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-7.0F, -11.0F, -14.0F, -0.8922867F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm2part2",
                CubeListBuilder.create().texOffs(0, 133).mirror()
                        .addBox(-1.5F, -1.0F, -1.0F, 2, 5, 2),
                PartPose.offsetAndRotation(-7.0F, -9.0F, -16.5F, 0.7435722F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("arm2end",
                CubeListBuilder.create().texOffs(0, 141).mirror()
                        .addBox(-1.0F, 3.0F, 1.0F, 1, 1, 2),
                PartPose.offsetAndRotation(-7.0F, -9.0F, -16.5F, 0.7435722F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("eye1",
                CubeListBuilder.create().texOffs(36, 37).mirror()
                        .addBox(-0.5F, -1.0F, -1.0F, 1, 2, 2),
                PartPose.offset(6.5F, -10.0F, -11.0F));

        partdefinition.addOrReplaceChild("eye2",
                CubeListBuilder.create().texOffs(36, 37).mirror()
                        .addBox(-0.5F, -1.0F, -1.0F, 1, 2, 2),
                PartPose.offset(-6.5F, -10.0F, -11.0F));

        return LayerDefinition.create(meshdefinition, 512, 256);
    }

    @Override
    public void setupAnim(EntitySpitBug entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        Object r = null;
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float pi4 = 1.570795f;
        newangle = Mth.sin((float)(ageInTicks * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.sin((float)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
        upangle = Math.abs(Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.12f * limbSwingAmount);
        }
        this.doLeftFrontLeg(newangle, upangle);
        this.doLeftRearLeg(-newangle, upangle);
        newangle = Mth.sin((float)((float)((double)(ageInTicks * 2.0f * limbSwingAmount) + Math.PI))) * (float)Math.PI * 0.12f * limbSwingAmount;
        nextangle = Mth.sin((float)((float)((double)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount) + Math.PI))) * (float)Math.PI * 0.12f * limbSwingAmount;
        upangle = 0.0f;
        if (nextangle > newangle) {
        upangle = Math.abs(Mth.cos((float)((float)((double)(ageInTicks * 2.0f * limbSwingAmount) + Math.PI))) * (float)Math.PI * 0.12f * limbSwingAmount);
        }
        this.doRightFrontLeg(-newangle, upangle);
        this.doRightRearLeg(newangle, upangle);
        newangle = entity.getAttacking() == 0 ? Mth.cos((float)(ageInTicks * 0.3f * limbSwingAmount)) * (float)Math.PI * 0.015f : Mth.cos((float)(ageInTicks * 2.6f * limbSwingAmount)) * (float)Math.PI * 0.1f;
        this.upperjawbasepart1.xRot = newangle = Math.abs(newangle);
        this.upperjawbasepart2.xRot = newangle;
        this.upperjawbasepart3.xRot = newangle;
        this.tooth1.xRot = 0.26f + newangle;
        this.tooth2.xRot = 0.26f + newangle;
        this.tooth3.xRot = 0.26f + newangle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.legintersection.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.legintersectionpart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.legintersectionpart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1start.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1startpart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1startpart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part2b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part2c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part2d.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part3b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg1part3c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2start.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2startpart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2startpart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part2b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part2c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part2d.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part3b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg2part3c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3start.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3startpart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3startpart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part2b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part2c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part2d.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part3b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg3part3c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4start.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4startpart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4startpart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part2b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part2c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part2d.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part3b.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leg4part3c.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodybasepart15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperjawbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperjawbasepart1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperjawbasepart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.upperjawbasepart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lowerjawbasepart11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm1start.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm1part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm1end.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm2start.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm2part2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.arm2end.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.eye2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    private void doRightFrontLeg(float angle, float upangle) {
        this.leg1part2.yRot = this.leg1.yRot = -1.2f + angle;
        this.leg1part2b.yRot = this.leg1.yRot;
        this.leg1part2c.yRot = this.leg1.yRot;
        this.leg1part2d.yRot = this.leg1.yRot;
        this.leg1part3.yRot = this.leg1.yRot;
        this.leg1part3b.yRot = this.leg1.yRot;
        this.leg1part3c.yRot = this.leg1.yRot;
        float dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg1.xRot));
        this.leg1part2c.z = this.leg1part2d.z = (float)((double)this.leg1.z - Math.cos(this.leg1.yRot) * (double)dist);
        this.leg1part2b.z = this.leg1part2d.z;
        this.leg1part2.z = this.leg1part2d.z;
        this.leg1part2c.x = this.leg1part2d.x = (float)((double)this.leg1.x - Math.sin(this.leg1.yRot) * (double)dist);
        this.leg1part2b.x = this.leg1part2d.x;
        this.leg1part2.x = this.leg1part2d.x;
        this.leg1part2.xRot = -1.152f + upangle;
        this.leg1part2b.xRot = -0.743f + upangle;
        this.leg1part2c.xRot = -0.632f + upangle;
        this.leg1part2d.xRot = -1.041f + upangle;
        dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg1part2.xRot));
        this.leg1part3.z = (float)((double)this.leg1part2.z - Math.cos(this.leg1part2.yRot) * (double)dist);
        this.leg1part3.x = (float)((double)this.leg1part2.x - Math.sin(this.leg1part2.yRot) * (double)dist);
        this.leg1part3.xRot = 0.669f - upangle;
        dist = 8.0f;
        dist = (float)Math.abs((double)dist * Math.cos(this.leg1part3.xRot));
        this.leg1part3b.z = this.leg1part3c.z = (float)((double)this.leg1part3.z - Math.cos(this.leg1part3.yRot) * (double)dist);
        this.leg1part3b.x = this.leg1part3c.x = (float)((double)this.leg1part3.x - Math.sin(this.leg1part3.yRot) * (double)dist);
        this.leg1part3b.xRot = -0.48f - upangle;
        this.leg1part3c.xRot = -0.48f - upangle;
    }

    private void doLeftFrontLeg(float angle, float upangle) {
        this.leg2part2.yRot = this.leg2.yRot = 1.2f + angle;
        this.leg2part2b.yRot = this.leg2.yRot;
        this.leg2part2c.yRot = this.leg2.yRot;
        this.leg2part2d.yRot = this.leg2.yRot;
        this.leg2part3.yRot = this.leg2.yRot;
        this.leg2part3b.yRot = this.leg2.yRot;
        this.leg2part3c.yRot = this.leg2.yRot;
        float dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg2.xRot));
        this.leg2part2c.z = this.leg2part2d.z = (float)((double)this.leg2.z - Math.cos(this.leg2.yRot) * (double)dist);
        this.leg2part2b.z = this.leg2part2d.z;
        this.leg2part2.z = this.leg2part2d.z;
        this.leg2part2c.x = this.leg2part2d.x = (float)((double)this.leg2.x - Math.sin(this.leg2.yRot) * (double)dist);
        this.leg2part2b.x = this.leg2part2d.x;
        this.leg2part2.x = this.leg2part2d.x;
        this.leg2part2.xRot = -1.152f + upangle;
        this.leg2part2b.xRot = -0.743f + upangle;
        this.leg2part2c.xRot = -0.632f + upangle;
        this.leg2part2d.xRot = -1.041f + upangle;
        dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg2part2.xRot));
        this.leg2part3.z = (float)((double)this.leg2part2.z - Math.cos(this.leg2part2.yRot) * (double)dist);
        this.leg2part3.x = (float)((double)this.leg2part2.x - Math.sin(this.leg2part2.yRot) * (double)dist);
        this.leg2part3.xRot = 0.669f - upangle;
        dist = 8.0f;
        dist = (float)Math.abs((double)dist * Math.cos(this.leg2part3.xRot));
        this.leg2part3b.z = this.leg2part3c.z = (float)((double)this.leg2part3.z - Math.cos(this.leg2part3.yRot) * (double)dist);
        this.leg2part3b.x = this.leg2part3c.x = (float)((double)this.leg2part3.x - Math.sin(this.leg2part3.yRot) * (double)dist);
        this.leg2part3b.xRot = -0.48f - upangle;
        this.leg2part3c.xRot = -0.48f - upangle;
    }

    private void doRightRearLeg(float angle, float upangle) {
        this.leg4part2.yRot = this.leg4.yRot = 2.1f + angle;
        this.leg4part2b.yRot = this.leg4.yRot;
        this.leg4part2c.yRot = this.leg4.yRot;
        this.leg4part2d.yRot = this.leg4.yRot;
        this.leg4part3.yRot = this.leg4.yRot;
        this.leg4part3b.yRot = this.leg4.yRot;
        this.leg4part3c.yRot = this.leg4.yRot;
        float dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg4.xRot));
        this.leg4part2c.z = this.leg4part2d.z = (float)((double)this.leg4.z - Math.cos(this.leg4.yRot) * (double)dist);
        this.leg4part2b.z = this.leg4part2d.z;
        this.leg4part2.z = this.leg4part2d.z;
        this.leg4part2c.x = this.leg4part2d.x = (float)((double)this.leg4.x - Math.sin(this.leg4.yRot) * (double)dist);
        this.leg4part2b.x = this.leg4part2d.x;
        this.leg4part2.x = this.leg4part2d.x;
        this.leg4part2.xRot = -1.152f + upangle;
        this.leg4part2b.xRot = -0.743f + upangle;
        this.leg4part2c.xRot = -0.632f + upangle;
        this.leg4part2d.xRot = -1.041f + upangle;
        dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg4part2.xRot));
        this.leg4part3.z = (float)((double)this.leg4part2.z - Math.cos(this.leg4part2.yRot) * (double)dist);
        this.leg4part3.x = (float)((double)this.leg4part2.x - Math.sin(this.leg4part2.yRot) * (double)dist);
        this.leg4part3.xRot = 0.669f - upangle;
        dist = 8.0f;
        dist = (float)Math.abs((double)dist * Math.cos(this.leg4part3.xRot));
        this.leg4part3b.z = this.leg4part3c.z = (float)((double)this.leg4part3.z - Math.cos(this.leg4part3.yRot) * (double)dist);
        this.leg4part3b.x = this.leg4part3c.x = (float)((double)this.leg4part3.x - Math.sin(this.leg4part3.yRot) * (double)dist);
        this.leg4part3b.xRot = -0.48f - upangle;
        this.leg4part3c.xRot = -0.48f - upangle;
    }

    private void doLeftRearLeg(float angle, float upangle) {
        this.leg3part2.yRot = this.leg3.yRot = -2.1f + angle;
        this.leg3part2b.yRot = this.leg3.yRot;
        this.leg3part2c.yRot = this.leg3.yRot;
        this.leg3part2d.yRot = this.leg3.yRot;
        this.leg3part3.yRot = this.leg3.yRot;
        this.leg3part3b.yRot = this.leg3.yRot;
        this.leg3part3c.yRot = this.leg3.yRot;
        float dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg3.xRot));
        this.leg3part2c.z = this.leg3part2d.z = (float)((double)this.leg3.z - Math.cos(this.leg3.yRot) * (double)dist);
        this.leg3part2b.z = this.leg3part2d.z;
        this.leg3part2.z = this.leg3part2d.z;
        this.leg3part2c.x = this.leg3part2d.x = (float)((double)this.leg3.x - Math.sin(this.leg3.yRot) * (double)dist);
        this.leg3part2b.x = this.leg3part2d.x;
        this.leg3part2.x = this.leg3part2d.x;
        this.leg3part2.xRot = -1.152f + upangle;
        this.leg3part2b.xRot = -0.743f + upangle;
        this.leg3part2c.xRot = -0.632f + upangle;
        this.leg3part2d.xRot = -1.041f + upangle;
        dist = 14.0f;
        dist = (float)((double)dist * Math.cos(this.leg3part2.xRot));
        this.leg3part3.z = (float)((double)this.leg3part2.z - Math.cos(this.leg3part2.yRot) * (double)dist);
        this.leg3part3.x = (float)((double)this.leg3part2.x - Math.sin(this.leg3part2.yRot) * (double)dist);
        this.leg3part3.xRot = 0.669f - upangle;
        dist = 8.0f;
        dist = (float)Math.abs((double)dist * Math.cos(this.leg3part3.xRot));
        this.leg3part3b.z = this.leg3part3c.z = (float)((double)this.leg3part3.z - Math.cos(this.leg3part3.yRot) * (double)dist);
        this.leg3part3b.x = this.leg3part3c.x = (float)((double)this.leg3part3.x - Math.sin(this.leg3part3.yRot) * (double)dist);
        this.leg3part3b.xRot = -0.48f - upangle;
        this.leg3part3c.xRot = -0.48f - upangle;
    }
}
