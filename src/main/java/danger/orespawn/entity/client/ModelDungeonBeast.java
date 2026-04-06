package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.DungeonBeast;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelDungeonBeast extends EntityModel<DungeonBeast> {
    private int ri1, ri2;
    private final ModelPart tail7;
    private final ModelPart head3;
    private final ModelPart neck;
    private final ModelPart lhornbase;
    private final ModelPart leye;
    private final ModelPart ljaw3;
    private final ModelPart ljaw1;
    private final ModelPart ljaw2;
    private final ModelPart rjaw1;
    private final ModelPart rjaw2;
    private final ModelPart rjaw3;
    private final ModelPart t1s3;
    private final ModelPart rshoulder;
    private final ModelPart rheel;
    private final ModelPart lshoulder;
    private final ModelPart rleg1;
    private final ModelPart rleg2;
    private final ModelPart lleg1;
    private final ModelPart lleg2;
    private final ModelPart rfoot;
    private final ModelPart ltoe3;
    private final ModelPart ltoe2;
    private final ModelPart ltoe1;
    private final ModelPart head1;
    private final ModelPart horn2;
    private final ModelPart rhornbase;
    private final ModelPart rh1;
    private final ModelPart lh1;
    private final ModelPart lh2;
    private final ModelPart rh2;
    private final ModelPart rh3;
    private final ModelPart lh3;
    private final ModelPart lh4;
    private final ModelPart rh4;
    private final ModelPart horn1;
    private final ModelPart t2s3;
    private final ModelPart tail3;
    private final ModelPart t4s1;
    private final ModelPart t6s1;
    private final ModelPart tail6;
    private final ModelPart body;
    private final ModelPart bodys1;
    private final ModelPart bodys2;
    private final ModelPart tail1;
    private final ModelPart bodys3;
    private final ModelPart t1s1;
    private final ModelPart t1s2;
    private final ModelPart tail2;
    private final ModelPart t3s2;
    private final ModelPart t2s2;
    private final ModelPart t2s1;
    private final ModelPart t3s1;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart t5s1;
    private final ModelPart head2;
    private final ModelPart reye;
    private final ModelPart lfoot;
    private final ModelPart rfoot2;
    private final ModelPart lfoot2;
    private final ModelPart lheel;
    private final ModelPart rtoe3;
    private final ModelPart rtoe2;
    private final ModelPart rtoe1;

    public ModelDungeonBeast(ModelPart root) {
        this.tail7 = root.getChild("tail7");
        this.head3 = root.getChild("head3");
        this.neck = root.getChild("neck");
        this.lhornbase = root.getChild("lhornbase");
        this.leye = root.getChild("leye");
        this.ljaw3 = root.getChild("ljaw3");
        this.ljaw1 = root.getChild("ljaw1");
        this.ljaw2 = root.getChild("ljaw2");
        this.rjaw1 = root.getChild("rjaw1");
        this.rjaw2 = root.getChild("rjaw2");
        this.rjaw3 = root.getChild("rjaw3");
        this.t1s3 = root.getChild("t1s3");
        this.rshoulder = root.getChild("rshoulder");
        this.rheel = root.getChild("rheel");
        this.lshoulder = root.getChild("lshoulder");
        this.rleg1 = root.getChild("rleg1");
        this.rleg2 = root.getChild("rleg2");
        this.lleg1 = root.getChild("lleg1");
        this.lleg2 = root.getChild("lleg2");
        this.rfoot = root.getChild("rfoot");
        this.ltoe3 = root.getChild("ltoe3");
        this.ltoe2 = root.getChild("ltoe2");
        this.ltoe1 = root.getChild("ltoe1");
        this.head1 = root.getChild("head1");
        this.horn2 = root.getChild("horn2");
        this.rhornbase = root.getChild("rhornbase");
        this.rh1 = root.getChild("rh1");
        this.lh1 = root.getChild("lh1");
        this.lh2 = root.getChild("lh2");
        this.rh2 = root.getChild("rh2");
        this.rh3 = root.getChild("rh3");
        this.lh3 = root.getChild("lh3");
        this.lh4 = root.getChild("lh4");
        this.rh4 = root.getChild("rh4");
        this.horn1 = root.getChild("horn1");
        this.t2s3 = root.getChild("t2s3");
        this.tail3 = root.getChild("tail3");
        this.t4s1 = root.getChild("t4s1");
        this.t6s1 = root.getChild("t6s1");
        this.tail6 = root.getChild("tail6");
        this.body = root.getChild("body");
        this.bodys1 = root.getChild("bodys1");
        this.bodys2 = root.getChild("bodys2");
        this.tail1 = root.getChild("tail1");
        this.bodys3 = root.getChild("bodys3");
        this.t1s1 = root.getChild("t1s1");
        this.t1s2 = root.getChild("t1s2");
        this.tail2 = root.getChild("tail2");
        this.t3s2 = root.getChild("t3s2");
        this.t2s2 = root.getChild("t2s2");
        this.t2s1 = root.getChild("t2s1");
        this.t3s1 = root.getChild("t3s1");
        this.tail4 = root.getChild("tail4");
        this.tail5 = root.getChild("tail5");
        this.t5s1 = root.getChild("t5s1");
        this.head2 = root.getChild("head2");
        this.reye = root.getChild("reye");
        this.lfoot = root.getChild("lfoot");
        this.rfoot2 = root.getChild("rfoot2");
        this.lfoot2 = root.getChild("lfoot2");
        this.lheel = root.getChild("lheel");
        this.rtoe3 = root.getChild("rtoe3");
        this.rtoe2 = root.getChild("rtoe2");
        this.rtoe1 = root.getChild("rtoe1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("tail7",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -0.5F, -0.5333334F, 3, 1, 1),
                PartPose.offsetAndRotation(-24.0F, 23.5F, 0.0F, 0.0F, 0.0F, 3.141593F));

        partdefinition.addOrReplaceChild("head3",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.0F, -2.466667F, 4.3F, 4, 4, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.8028515F, 0.0F));

        partdefinition.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -1.5F, -2.533333F, 3, 3, 5),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.1745329F));

        partdefinition.addOrReplaceChild("lhornbase",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.5F, -3.0F, 0.5F, 3, 1, 2),
                PartPose.offset(5.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("leye",
                CubeListBuilder.create().texOffs(14, 15).mirror().addBox(3.0F, -1.466667F, 3.3F, 2, 1, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw3",
                CubeListBuilder.create().texOffs(10, 28).mirror().addBox(3.5F, 0.0F, 1.5F, 1, 1, 1),
                PartPose.offsetAndRotation(10.0F, 16.0F, 2.0F, 0.0F, 0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw1",
                CubeListBuilder.create().texOffs(10, 20).mirror().addBox(0.0F, 0.0F, -1.466667F, 3, 1, 2),
                PartPose.offsetAndRotation(10.0F, 16.0F, 2.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("ljaw2",
                CubeListBuilder.create().texOffs(10, 24).mirror().addBox(2.0F, 0.0F, 0.3F, 2, 1, 2),
                PartPose.offsetAndRotation(10.0F, 16.0F, 2.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("rjaw1",
                CubeListBuilder.create().texOffs(10, 20).mirror().addBox(0.0F, 0.0F, -0.4666667F, 3, 1, 2),
                PartPose.offsetAndRotation(10.0F, 16.0F, -2.0F, 0.0F, 0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("rjaw2",
                CubeListBuilder.create().texOffs(10, 24).mirror().addBox(2.0F, 0.0F, -2.3F, 2, 1, 2),
                PartPose.offsetAndRotation(10.0F, 16.0F, -2.0F, 0.0F, -0.3490659F, 0.0F));

        partdefinition.addOrReplaceChild("rjaw3",
                CubeListBuilder.create().texOffs(10, 28).mirror().addBox(3.5F, 0.0F, -2.5F, 1, 1, 1),
                PartPose.offsetAndRotation(10.0F, 16.0F, -2.0F, 0.0F, -0.5235988F, 0.0F));

        partdefinition.addOrReplaceChild("t1s3",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(-3.0F, -7.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.8726646F));

        partdefinition.addOrReplaceChild("rshoulder",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.0F, -2.2F, -5.0F, 4, 4, 2),
                PartPose.offset(-1.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("rheel",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.3F, 0.3F, 6.0F, 1, 1, 1),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lshoulder",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.0F, -2.2F, 3.0F, 4, 4, 2),
                PartPose.offset(-1.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("rleg1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.466667F, -2.0F, -5.0F, 3, 3, 6),
                PartPose.offsetAndRotation(3.0F, 15.0F, -4.0F, 0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rleg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -0.2F, 0.0F, 2, 2, 6),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lleg1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.466667F, -2.0F, -1.0F, 3, 3, 6),
                PartPose.offsetAndRotation(3.0F, 15.0F, 4.0F, -0.6981317F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lleg2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -1.8F, 0.0F, 2, 2, 6),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rfoot",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, -0.7F, 5.0F, 3, 3, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("ltoe3",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.7F, -1.5F, 4.5F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, 0.7853982F, -0.7853982F));

        partdefinition.addOrReplaceChild("ltoe2",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.0F, -1.3F, 5.2F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, 0.0F, -0.7853982F));

        partdefinition.addOrReplaceChild("ltoe1",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.0F, -0.6F, 5.2F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, -0.7853982F, -0.7853982F));

        partdefinition.addOrReplaceChild("head1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.0F, -2.466667F, -3.0F, 4, 4, 6),
                PartPose.offset(5.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("horn2",
                CubeListBuilder.create().texOffs(75, 6).mirror().addBox(-7.0F, -4.0F, -0.5F, 1, 2, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, 2.181662F));

        partdefinition.addOrReplaceChild("rhornbase",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.5F, -3.0F, -2.5F, 3, 1, 2),
                PartPose.offset(5.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("rh1",
                CubeListBuilder.create().texOffs(0, 28).mirror().addBox(4.0F, -3.0F, -2.5F, 2, 3, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("lh1",
                CubeListBuilder.create().texOffs(0, 28).mirror().addBox(4.0F, -3.0F, 0.5F, 2, 3, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("lh2",
                CubeListBuilder.create().texOffs(0, 23).mirror().addBox(5.0F, -4.0F, 1.0F, 1, 3, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.8726646F));

        partdefinition.addOrReplaceChild("rh2",
                CubeListBuilder.create().texOffs(0, 23).mirror().addBox(5.0F, -4.0F, -2.0F, 1, 3, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.8726646F));

        partdefinition.addOrReplaceChild("rh3",
                CubeListBuilder.create().texOffs(0, 19).mirror().addBox(6.1F, -2.4F, -2.0F, 1, 2, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -1.396263F));

        partdefinition.addOrReplaceChild("lh3",
                CubeListBuilder.create().texOffs(0, 19).mirror().addBox(6.1F, -2.4F, 1.0F, 1, 2, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -1.396263F));

        partdefinition.addOrReplaceChild("lh4",
                CubeListBuilder.create().texOffs(0, 15).mirror().addBox(6.5F, -1.8F, 1.0F, 1, 2, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -1.745329F));

        partdefinition.addOrReplaceChild("rh4",
                CubeListBuilder.create().texOffs(0, 15).mirror().addBox(6.5F, -1.8F, -2.0F, 1, 2, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, -1.745329F));

        partdefinition.addOrReplaceChild("horn1",
                CubeListBuilder.create().texOffs(75, 6).mirror().addBox(-8.0F, -2.5F, -0.5F, 1, 2, 1),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, 0.0F, 2.617994F));

        partdefinition.addOrReplaceChild("t2s3",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(3.0F, 3.466667F, -0.5333334F, 1, 3, 1),
                PartPose.offsetAndRotation(-6.0F, 17.0F, 0.0F, 0.0F, 0.0F, 2.007129F));

        partdefinition.addOrReplaceChild("tail3",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -1.5F, -2.5F, 6, 3, 5),
                PartPose.offsetAndRotation(-10.0F, 20.0F, 0.0F, 0.0F, 0.0F, 2.530727F));

        partdefinition.addOrReplaceChild("t4s1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(0.5333334F, 1.533333F, -0.4666667F, 1, 2, 1),
                PartPose.offsetAndRotation(-14.0F, 22.8F, 0.0F, 0.0F, 0.0F, 2.356194F));

        partdefinition.addOrReplaceChild("t6s1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(0.0F, 0.5F, -0.5F, 1, 1, 1),
                PartPose.offsetAndRotation(-21.0F, 23.5F, 0.0F, 0.0F, 0.0F, 2.356194F));

        partdefinition.addOrReplaceChild("tail6",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -0.5F, -1.0F, 4, 1, 2),
                PartPose.offsetAndRotation(-21.0F, 23.5F, 0.0F, 0.0F, 0.0F, 3.141593F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -3.0F, -4.0F, 7, 6, 8),
                PartPose.offset(-1.0F, 15.0F, 0.0F));

        partdefinition.addOrReplaceChild("bodys1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(6.0F, -3.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("bodys2",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(4.0F, -4.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("tail1",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -2.533333F, -3.5F, 7, 5, 7),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, 2.792527F));

        partdefinition.addOrReplaceChild("bodys3",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(2.0F, -5.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));

        partdefinition.addOrReplaceChild("t1s1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(1.0F, -5.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.8726646F));

        partdefinition.addOrReplaceChild("t1s2",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(-1.0F, -6.0F, -0.5F, 1, 4, 1),
                PartPose.offsetAndRotation(-1.0F, 15.0F, 0.0F, 0.0F, 0.0F, -0.8726646F));

        partdefinition.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -2.0F, -3.0F, 7, 4, 6),
                PartPose.offsetAndRotation(-6.0F, 17.0F, 0.0F, 0.0F, 0.0F, 2.530727F));

        partdefinition.addOrReplaceChild("t3s2",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(2.5F, 2.466667F, -0.5333334F, 1, 3, 1),
                PartPose.offsetAndRotation(-10.0F, 20.0F, 0.0F, 0.0F, 0.0F, 2.007129F));

        partdefinition.addOrReplaceChild("t2s2",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(1.0F, 2.466667F, -0.5333334F, 1, 3, 1),
                PartPose.offsetAndRotation(-6.0F, 17.0F, 0.0F, 0.0F, 0.0F, 2.007129F));

        partdefinition.addOrReplaceChild("t2s1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(-1.0F, 1.466667F, -0.5333334F, 1, 3, 1),
                PartPose.offsetAndRotation(-6.0F, 17.0F, 0.0F, 0.0F, 0.0F, 2.007129F));

        partdefinition.addOrReplaceChild("t3s1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(0.5F, 1.466667F, -0.5333334F, 1, 3, 1),
                PartPose.offsetAndRotation(-10.0F, 20.0F, 0.0F, 0.0F, 0.0F, 2.007129F));

        partdefinition.addOrReplaceChild("tail4",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -1.0F, -2.0F, 5, 2, 4),
                PartPose.offsetAndRotation(-14.0F, 22.8F, 0.0F, 0.0F, 0.0F, 3.054326F));

        partdefinition.addOrReplaceChild("tail5",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -0.5F, -1.5F, 4, 1, 3),
                PartPose.offsetAndRotation(-18.0F, 23.2F, 0.0F, 0.0F, 0.0F, 3.054326F));

        partdefinition.addOrReplaceChild("t5s1",
                CubeListBuilder.create().texOffs(75, 0).mirror().addBox(0.0F, 0.5F, -0.5F, 1, 2, 1),
                PartPose.offsetAndRotation(-18.0F, 23.2F, 0.0F, 0.0F, 0.0F, 2.356194F));

        partdefinition.addOrReplaceChild("head2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(2.0F, -2.466667F, -6.3F, 4, 4, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, -0.8028515F, 0.0F));

        partdefinition.addOrReplaceChild("reye",
                CubeListBuilder.create().texOffs(5, 15).mirror().addBox(3.0F, -1.466667F, -5.3F, 2, 1, 2),
                PartPose.offsetAndRotation(5.0F, 15.0F, 0.0F, 0.0F, -0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("lfoot",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, -2.3F, 5.0F, 3, 3, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rfoot2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.6F, -1.5F, 5.0F, 2, 2, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, 0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("lfoot2",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.7F, -0.5F, 5.0F, 2, 2, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("lheel",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.3F, -1.3F, 6.0F, 1, 1, 1),
                PartPose.offsetAndRotation(3.0F, 17.0F, 7.0F, -1.570796F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rtoe3",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.7F, 0.6F, 4.5F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, -0.7853982F, -0.7853982F));

        partdefinition.addOrReplaceChild("rtoe2",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.0F, 0.3F, 5.2F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, 0.0F, -0.7853982F));

        partdefinition.addOrReplaceChild("rtoe1",
                CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-3.0F, -0.6F, 5.2F, 1, 1, 2),
                PartPose.offsetAndRotation(3.0F, 17.0F, -7.0F, -1.570796F, 0.7853982F, -0.7853982F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(DungeonBeast entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float newangle = 0.0f;
        float upangle = 0.0f;
        float nextangle = 0.0f;
        float tailamp = 0.0f;
        float pi4 = 0.39269876f;
        this.rfoot2.zRot = this.rheel.zRot = (newangle = Mth.cos((float)(ageInTicks * 1.4f * limbSwingAmount)) * (float)Math.PI * 0.22f * limbSwingAmount);
        this.rfoot.zRot = this.rheel.zRot;
        this.rleg2.zRot = this.rheel.zRot;
        this.rleg1.zRot = this.rheel.zRot;
        this.rtoe2.zRot = -0.785f + newangle;
        this.lfoot2.zRot = this.lheel.zRot = -newangle;
        this.lfoot.zRot = this.lheel.zRot;
        this.lleg2.zRot = this.lheel.zRot;
        this.lleg1.zRot = this.lheel.zRot;
        this.ltoe2.zRot = -0.785f - newangle;
        this.bodys1.xRot = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount)) * (float)Math.PI * 0.07f;
        this.bodys2.xRot = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + pi4)) * (float)Math.PI * 0.07f;
        this.bodys3.xRot = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 2.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t1s1.xRot = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 3.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t1s2.xRot = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 4.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t1s3.xRot = Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 5.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t2s1.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 6.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t2s2.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 7.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t2s3.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 8.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t3s1.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 9.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t3s2.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 10.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t4s1.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 11.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t5s1.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 12.0f * pi4)) * (float)Math.PI * 0.07f;
        this.t6s1.xRot = -Mth.cos((float)(ageInTicks * 0.5f * limbSwingAmount + 13.0f * pi4)) * (float)Math.PI * 0.07f;
        tailamp = entity.getAttacking() == 0 ? limbSwingAmount : 1.25f;
        newangle = Mth.cos((float)(ageInTicks * 0.75f * limbSwingAmount)) * (float)Math.PI * 0.25f * tailamp;
        this.t1s2.yRot = this.t1s3.yRot = (this.tail1.yRot = newangle * 0.25f);
        this.t1s1.yRot = this.t1s3.yRot;
        this.tail2.yRot = newangle * 0.5f;
        this.tail2.x = this.tail1.x - (float)Math.cos(this.tail1.yRot) * 6.0f;
        this.tail2.z = this.tail1.z - (float)Math.sin(this.tail1.yRot) * 6.0f;
        this.t2s2.yRot = this.t2s3.yRot = this.tail2.yRot;
        this.t2s1.yRot = this.t2s3.yRot;
        this.t2s2.z = this.t2s3.z = this.tail2.z;
        this.t2s1.z = this.t2s3.z;
        this.t2s2.x = this.t2s3.x = this.tail2.x;
        this.t2s1.x = this.t2s3.x;
        this.tail3.yRot = newangle * 0.75f;
        this.tail3.x = this.tail2.x - (float)Math.cos(this.tail2.yRot) * 5.0f;
        this.tail3.z = this.tail2.z - (float)Math.sin(this.tail2.yRot) * 5.0f;
        this.t3s1.yRot = this.t3s2.yRot = this.tail3.yRot;
        this.t3s1.z = this.t3s2.z = this.tail3.z;
        this.t3s1.x = this.t3s2.x = this.tail3.x;
        this.tail4.yRot = newangle;
        this.tail4.x = this.tail3.x - (float)Math.cos(this.tail3.yRot) * 4.5f;
        this.tail4.z = this.tail3.z - (float)Math.sin(this.tail3.yRot) * 4.5f;
        this.t4s1.yRot = this.tail4.yRot;
        this.t4s1.z = this.tail4.z;
        this.t4s1.x = this.tail4.x;
        this.tail5.yRot = newangle * 1.25f;
        this.tail5.x = this.tail4.x - (float)Math.cos(this.tail4.yRot) * 4.0f;
        this.tail5.z = this.tail4.z - (float)Math.sin(this.tail4.yRot) * 4.0f;
        this.t5s1.yRot = this.tail5.yRot;
        this.t5s1.z = this.tail5.z;
        this.t5s1.x = this.tail5.x;
        this.tail6.yRot = newangle * 1.5f;
        this.tail6.x = this.tail5.x - (float)Math.cos(this.tail5.yRot) * 3.0f;
        this.tail6.z = this.tail5.z - (float)Math.sin(this.tail5.yRot) * 3.0f;
        this.t6s1.yRot = this.tail6.yRot;
        this.t6s1.z = this.tail6.z;
        this.t6s1.x = this.tail6.x;
        this.tail7.yRot = newangle * 1.75f;
        this.tail7.x = this.tail6.x - (float)Math.cos(this.tail6.yRot) * 3.0f;
        this.tail7.z = this.tail6.z - (float)Math.sin(this.tail6.yRot) * 3.0f;
        newangle = Mth.cos((float)(ageInTicks * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        nextangle = Mth.cos((float)((ageInTicks + 0.1f) * 2.0f * limbSwingAmount)) * (float)Math.PI * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
        if (entity.getAttacking() == 0) {
        ri1 = entity.getRandom().nextInt(15);
        ri2 = entity.getRandom().nextInt(15);
        } else {
        ri1 = 0;
        ri2 = 0;
        }
        }
        if (ri1 == 0) {
        this.ljaw1.yRot = -0.349f + newangle;
        this.ljaw2.yRot = 0.349f + newangle;
        this.ljaw3.yRot = 0.523f + newangle;
        this.rjaw1.yRot = 0.349f - newangle;
        this.rjaw2.yRot = -0.349f - newangle;
        this.rjaw3.yRot = -0.523f - newangle;
        } else {
        this.ljaw1.yRot = -0.349f;
        this.ljaw2.yRot = 0.349f;
        this.ljaw3.yRot = 0.523f;
        this.rjaw1.yRot = 0.349f;
        this.rjaw2.yRot = -0.349f;
        this.rjaw3.yRot = -0.523f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.tail7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lhornbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ljaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ljaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ljaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rjaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rjaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rjaw3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t1s3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rshoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lshoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.ltoe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rhornbase.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rh1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lh1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lh2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rh2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rh3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lh3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lh4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rh4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.horn1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t2s3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t4s1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t6s1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodys1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodys2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bodys3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t1s1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t1s2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t3s2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t2s2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t2s1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t3s1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.t5s1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.reye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rfoot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lfoot2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rtoe2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
