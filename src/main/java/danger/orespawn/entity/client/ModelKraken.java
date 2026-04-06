package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import danger.orespawn.entity.Kraken;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelKraken extends EntityModel<Kraken> {
    private static final float ANIM_SPEED = 1.0F;

    private final ModelPart Lefteye;
    private final ModelPart Backbody;
    private final ModelPart Centerbody;
    private final ModelPart Head;
    private final ModelPart Sucktioncupleft;
    private final ModelPart Finright;
    private final ModelPart Tailbase1;
    private final ModelPart Tail2;
    private final ModelPart Tailtip;
    private final ModelPart Finleft;
    private final ModelPart Frontbody;
    private final ModelPart Mouth1;
    private final ModelPart Tent54;
    private final ModelPart Tent62;
    private final ModelPart Tent63;
    private final ModelPart Tent64;
    private final ModelPart Tent58;
    private final ModelPart Tent66;
    private final ModelPart Tent67;
    private final ModelPart Tent68;
    private final ModelPart Tent28;
    private final ModelPart Tent51;
    private final ModelPart Tent52;
    private final ModelPart Tent53;
    private final ModelPart Tent65;
    private final ModelPart Tent55;
    private final ModelPart Tent56;
    private final ModelPart Tent57;
    private final ModelPart Sucktioncupright;
    private final ModelPart Righteye;
    private final ModelPart Mouth2;
    private final ModelPart Mouth3;
    private final ModelPart Mouth4;
    private final ModelPart Mouth5;
    private final ModelPart Mouth6;
    private final ModelPart Mouth7;
    private final ModelPart Mouth8;
    private final ModelPart Tent61;
    private final ModelPart Tent38;
    private final ModelPart Tent22;
    private final ModelPart Tent23;
    private final ModelPart Tent24;
    private final ModelPart Tent25;
    private final ModelPart Tent26;
    private final ModelPart Tent27;
    private final ModelPart Tooth1;
    private final ModelPart Tent48;
    private final ModelPart Tent32;
    private final ModelPart Tent33;
    private final ModelPart Tent34;
    private final ModelPart Tent35;
    private final ModelPart Tent36;
    private final ModelPart Tent37;
    private final ModelPart Jet;
    private final ModelPart Tent41;
    private final ModelPart Tent42;
    private final ModelPart Tent43;
    private final ModelPart Tent44;
    private final ModelPart Tent45;
    private final ModelPart Tent46;
    private final ModelPart Tent47;
    private final ModelPart Tent21;
    private final ModelPart Tent11;
    private final ModelPart Tent12;
    private final ModelPart Tent13;
    private final ModelPart Tent14;
    private final ModelPart Tent15;
    private final ModelPart Tent16;
    private final ModelPart Tent31;
    private final ModelPart Tent18;
    private final ModelPart Tooth2;
    private final ModelPart Tooth3;
    private final ModelPart Tooth4;
    private final ModelPart Tooth5;
    private final ModelPart Tooth6;
    private final ModelPart Tooth7;
    private final ModelPart Tooth8;
    private final ModelPart Tooth9;
    private final ModelPart Tooth10;
    private final ModelPart Tooth11;
    private final ModelPart Tooth12;
    private final ModelPart Tooth13;
    private final ModelPart Tooth14;
    private final ModelPart Tooth15;
    private final ModelPart Tooth16;
    private final ModelPart Tooth17;
    private final ModelPart Tooth18;
    private final ModelPart Tooth19;
    private final ModelPart Tooth20;
    private final ModelPart Tooth21;
    private final ModelPart Tooth22;
    private final ModelPart Tooth23;
    private final ModelPart Tooth24;
    private final ModelPart Tooth25;
    private final ModelPart Tooth26;
    private final ModelPart Tooth27;
    private final ModelPart Tooth28;
    private final ModelPart Tooth29;
    private final ModelPart Tooth30;
    private final ModelPart Tooth31;
    private final ModelPart Tooth32;
    private final ModelPart Tooth33;
    private final ModelPart Tooth34;
    private final ModelPart Tooth35;
    private final ModelPart Tooth36;
    private final ModelPart Tooth37;
    private final ModelPart Tooth38;
    private final ModelPart Tooth39;
    private final ModelPart Tooth40;
    private final ModelPart Tooth41;
    private final ModelPart Tent17;

    private int ri1 = 0;
    private int ri2 = 0;

    public ModelKraken(ModelPart root) {
        this.Lefteye = root.getChild("Lefteye");
        this.Backbody = root.getChild("Backbody");
        this.Centerbody = root.getChild("Centerbody");
        this.Head = root.getChild("Head");
        this.Sucktioncupleft = root.getChild("Sucktioncupleft");
        this.Finright = root.getChild("Finright");
        this.Tailbase1 = root.getChild("Tailbase1");
        this.Tail2 = root.getChild("Tail2");
        this.Tailtip = root.getChild("Tailtip");
        this.Finleft = root.getChild("Finleft");
        this.Frontbody = root.getChild("Frontbody");
        this.Mouth1 = root.getChild("Mouth1");
        this.Tent54 = root.getChild("Tent54");
        this.Tent62 = root.getChild("Tent62");
        this.Tent63 = root.getChild("Tent63");
        this.Tent64 = root.getChild("Tent64");
        this.Tent58 = root.getChild("Tent58");
        this.Tent66 = root.getChild("Tent66");
        this.Tent67 = root.getChild("Tent67");
        this.Tent68 = root.getChild("Tent68");
        this.Tent28 = root.getChild("Tent28");
        this.Tent51 = root.getChild("Tent51");
        this.Tent52 = root.getChild("Tent52");
        this.Tent53 = root.getChild("Tent53");
        this.Tent65 = root.getChild("Tent65");
        this.Tent55 = root.getChild("Tent55");
        this.Tent56 = root.getChild("Tent56");
        this.Tent57 = root.getChild("Tent57");
        this.Sucktioncupright = root.getChild("Sucktioncupright");
        this.Righteye = root.getChild("Righteye");
        this.Mouth2 = root.getChild("Mouth2");
        this.Mouth3 = root.getChild("Mouth3");
        this.Mouth4 = root.getChild("Mouth4");
        this.Mouth5 = root.getChild("Mouth5");
        this.Mouth6 = root.getChild("Mouth6");
        this.Mouth7 = root.getChild("Mouth7");
        this.Mouth8 = root.getChild("Mouth8");
        this.Tent61 = root.getChild("Tent61");
        this.Tent38 = root.getChild("Tent38");
        this.Tent22 = root.getChild("Tent22");
        this.Tent23 = root.getChild("Tent23");
        this.Tent24 = root.getChild("Tent24");
        this.Tent25 = root.getChild("Tent25");
        this.Tent26 = root.getChild("Tent26");
        this.Tent27 = root.getChild("Tent27");
        this.Tooth1 = root.getChild("Tooth1");
        this.Tent48 = root.getChild("Tent48");
        this.Tent32 = root.getChild("Tent32");
        this.Tent33 = root.getChild("Tent33");
        this.Tent34 = root.getChild("Tent34");
        this.Tent35 = root.getChild("Tent35");
        this.Tent36 = root.getChild("Tent36");
        this.Tent37 = root.getChild("Tent37");
        this.Jet = root.getChild("Jet");
        this.Tent41 = root.getChild("Tent41");
        this.Tent42 = root.getChild("Tent42");
        this.Tent43 = root.getChild("Tent43");
        this.Tent44 = root.getChild("Tent44");
        this.Tent45 = root.getChild("Tent45");
        this.Tent46 = root.getChild("Tent46");
        this.Tent47 = root.getChild("Tent47");
        this.Tent21 = root.getChild("Tent21");
        this.Tent11 = root.getChild("Tent11");
        this.Tent12 = root.getChild("Tent12");
        this.Tent13 = root.getChild("Tent13");
        this.Tent14 = root.getChild("Tent14");
        this.Tent15 = root.getChild("Tent15");
        this.Tent16 = root.getChild("Tent16");
        this.Tent31 = root.getChild("Tent31");
        this.Tent18 = root.getChild("Tent18");
        this.Tooth2 = root.getChild("Tooth2");
        this.Tooth3 = root.getChild("Tooth3");
        this.Tooth4 = root.getChild("Tooth4");
        this.Tooth5 = root.getChild("Tooth5");
        this.Tooth6 = root.getChild("Tooth6");
        this.Tooth7 = root.getChild("Tooth7");
        this.Tooth8 = root.getChild("Tooth8");
        this.Tooth9 = root.getChild("Tooth9");
        this.Tooth10 = root.getChild("Tooth10");
        this.Tooth11 = root.getChild("Tooth11");
        this.Tooth12 = root.getChild("Tooth12");
        this.Tooth13 = root.getChild("Tooth13");
        this.Tooth14 = root.getChild("Tooth14");
        this.Tooth15 = root.getChild("Tooth15");
        this.Tooth16 = root.getChild("Tooth16");
        this.Tooth17 = root.getChild("Tooth17");
        this.Tooth18 = root.getChild("Tooth18");
        this.Tooth19 = root.getChild("Tooth19");
        this.Tooth20 = root.getChild("Tooth20");
        this.Tooth21 = root.getChild("Tooth21");
        this.Tooth22 = root.getChild("Tooth22");
        this.Tooth23 = root.getChild("Tooth23");
        this.Tooth24 = root.getChild("Tooth24");
        this.Tooth25 = root.getChild("Tooth25");
        this.Tooth26 = root.getChild("Tooth26");
        this.Tooth27 = root.getChild("Tooth27");
        this.Tooth28 = root.getChild("Tooth28");
        this.Tooth29 = root.getChild("Tooth29");
        this.Tooth30 = root.getChild("Tooth30");
        this.Tooth31 = root.getChild("Tooth31");
        this.Tooth32 = root.getChild("Tooth32");
        this.Tooth33 = root.getChild("Tooth33");
        this.Tooth34 = root.getChild("Tooth34");
        this.Tooth35 = root.getChild("Tooth35");
        this.Tooth36 = root.getChild("Tooth36");
        this.Tooth37 = root.getChild("Tooth37");
        this.Tooth38 = root.getChild("Tooth38");
        this.Tooth39 = root.getChild("Tooth39");
        this.Tooth40 = root.getChild("Tooth40");
        this.Tooth41 = root.getChild("Tooth41");
        this.Tent17 = root.getChild("Tent17");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition p = meshdefinition.getRoot();

        // Body parts (pivots have +90 X, +30 Y baked in from original constructor)
        p.addOrReplaceChild("Lefteye", CubeListBuilder.create().texOffs(56, 458).mirror()
                .addBox(0.0F, -8.0F, -12.0F, 4, 16, 24), PartPose.offset(20F, 6F, 0F));
        p.addOrReplaceChild("Backbody", CubeListBuilder.create().texOffs(320, 272).mirror()
                .addBox(-24.0F, -24.0F, 0.0F, 48, 48, 48), PartPose.offset(0F, 6F, 118F));
        p.addOrReplaceChild("Centerbody", CubeListBuilder.create().texOffs(320, 176).mirror()
                .addBox(-24.0F, -24.0F, -24.0F, 48, 48, 48), PartPose.offset(0F, 6F, 94F));
        p.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(336, 0).mirror()
                .addBox(-20.0F, -20.0F, -40.0F, 40, 40, 40), PartPose.offset(0F, 6F, 22F));
        p.addOrReplaceChild("Frontbody", CubeListBuilder.create().texOffs(320, 80).mirror()
                .addBox(-20.0F, -20.0F, -47.0F, 48, 48, 48), PartPose.offset(-4F, 2F, 69F));
        p.addOrReplaceChild("Finright", CubeListBuilder.create().texOffs(0, 329).mirror()
                .addBox(-40.0F, -8.0F, -32.0F, 40, 12, 104), PartPose.offset(-12F, 9F, 173F));
        p.addOrReplaceChild("Finleft", CubeListBuilder.create().texOffs(0, 201).mirror()
                .addBox(0.0F, -8.0F, -32.0F, 40, 12, 104), PartPose.offset(12F, 9F, 173F));
        p.addOrReplaceChild("Tailbase1", CubeListBuilder.create().texOffs(368, 368).mirror()
                .addBox(-20.0F, -20.0F, 0.0F, 40, 40, 32), PartPose.offset(0F, 6F, 165F));
        p.addOrReplaceChild("Tail2", CubeListBuilder.create().texOffs(384, 440).mirror()
                .addBox(-16.0F, -16.0F, 0.0F, 32, 32, 32), PartPose.offset(0F, 6F, 197F));
        p.addOrReplaceChild("Tailtip", CubeListBuilder.create().texOffs(272, 457).mirror()
                .addBox(-12.0F, -12.0F, 0.0F, 24, 24, 32), PartPose.offset(0F, 6F, 229F));
        p.addOrReplaceChild("Jet", CubeListBuilder.create().texOffs(80, 42).mirror()
                .addBox(-5.0F, -5.0F, -32.0F, 10, 10, 32),
                PartPose.offsetAndRotation(0F, 23F, 26F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Righteye", CubeListBuilder.create().texOffs(0, 458).mirror()
                .addBox(-4.0F, -8.0F, -12.0F, 4, 16, 24), PartPose.offset(-20F, 6F, 0F));

        // Mouth parts
        p.addOrReplaceChild("Mouth1", CubeListBuilder.create().texOffs(232, 160).mirror()
                .addBox(-2.0F, -8.0F, -24.0F, 4, 16, 24),
                PartPose.offsetAndRotation(11F, 2F, -14F, -0.3839724F, -0.3839724F, -0.7853982F));
        p.addOrReplaceChild("Mouth2", CubeListBuilder.create().texOffs(232, 92).mirror()
                .addBox(-2.0F, -8.0F, -24.0F, 4, 16, 24),
                PartPose.offsetAndRotation(-12F, 2F, -14F, -0.3839724F, 0.3839724F, 0.7853982F));
        p.addOrReplaceChild("Mouth3", CubeListBuilder.create().texOffs(232, 12).mirror()
                .addBox(-2.0F, -8.0F, -24.0F, 4, 16, 24),
                PartPose.offsetAndRotation(-10F, 18F, -14F, 0.3839724F, 0.3839724F, -0.8004762F));
        p.addOrReplaceChild("Mouth4", CubeListBuilder.create().texOffs(288, 427).mirror()
                .addBox(-8.0F, -2.0F, -24.0F, 16, 4, 24),
                PartPose.offsetAndRotation(0F, 21F, -14F, 0.3839724F, 0.0F, 0.0F));
        p.addOrReplaceChild("Mouth5", CubeListBuilder.create().texOffs(288, 387).mirror()
                .addBox(-2.0F, -8.0F, -24.0F, 4, 16, 24),
                PartPose.offsetAndRotation(11F, 18F, -14F, 0.3839724F, -0.3839724F, 0.7853982F));
        p.addOrReplaceChild("Mouth6", CubeListBuilder.create().texOffs(175, 160).mirror()
                .addBox(-2.0F, -8.0F, -24.0F, 4, 16, 24),
                PartPose.offsetAndRotation(10F, 9F, -14F, 0.0F, -0.3839724F, 0.0F));
        p.addOrReplaceChild("Mouth7", CubeListBuilder.create().texOffs(232, 52).mirror()
                .addBox(-2.0F, -8.0F, -24.0F, 4, 16, 24),
                PartPose.offsetAndRotation(-10F, 9F, -14F, 0.0F, 0.3839724F, 0.0F));
        p.addOrReplaceChild("Mouth8", CubeListBuilder.create().texOffs(232, 132).mirror()
                .addBox(-8.0F, -2.0F, -24.0F, 16, 4, 24),
                PartPose.offsetAndRotation(0F, -1F, -14F, -0.3839724F, 0.0F, 0.0F));

        // Suction cups
        p.addOrReplaceChild("Sucktioncupleft", CubeListBuilder.create().texOffs(80, 84).mirror()
                .addBox(-8.0F, -4.0F, -32.0F, 16, 8, 32),
                PartPose.offsetAndRotation(32F, 4F, -246F, 0.3490659F, 0.2617994F, 0.0F));
        p.addOrReplaceChild("Sucktioncupright", CubeListBuilder.create().texOffs(80, 84).mirror()
                .addBox(-8.0F, -4.0F, -32.0F, 16, 8, 32),
                PartPose.offsetAndRotation(-14F, 0F, -246F, 0.3490659F, -0.2617994F, 0.0F));

        // Tentacle 1: Tent11-Tent18
        p.addOrReplaceChild("Tent11", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(-14F, 7F, -15F, 0.0872665F, 0.4363323F, 0.0F));
        p.addOrReplaceChild("Tent12", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(-26F, 10F, -42F, 0.0872665F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tent13", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(-36F, 13F, -70F, 0.0872665F, 0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent14", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(-44F, 15F, -98F, 0.0872665F, 0.4363323F, 0.0F));
        p.addOrReplaceChild("Tent15", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(-57F, 18F, -127F, 0.0872665F, 0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent16", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(-65F, 21F, -156F, 0.0872665F, 0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent17", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(-68F, 24F, -187F, 0.0872665F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent18", CubeListBuilder.create().texOffs(0, 57).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 1, 1, 32),
                PartPose.offsetAndRotation(-62F, 27F, -217F, 0.0872665F, -0.1745329F, 0.0F));

        // Tentacle 2: Tent21-Tent28
        p.addOrReplaceChild("Tent21", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(-14F, 22F, -15F, 0.2617994F, 0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent22", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(-21F, 30F, -43F, 0.2617994F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent23", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(-26F, 38F, -71F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tent24", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-3.0F, -3.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(-26F, 49F, -101F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tent25", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-3.0F, -3.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(-26F, 59F, -129F, 0.2617994F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent26", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(-24F, 67F, -158F, 0.2617994F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent27", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(-22F, 75F, -187F, 0.1745329F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent28", CubeListBuilder.create().texOffs(0, 57).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 1, 1, 32),
                PartPose.offsetAndRotation(-17F, 80F, -217F, 0.0872665F, -0.1745329F, 0.0F));

        // Tentacle 3: Tent31-Tent38
        p.addOrReplaceChild("Tent31", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(16F, 22F, -15F, 0.2617994F, -0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent32", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(24F, 30F, -44F, 0.2617994F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tent33", CubeListBuilder.create().texOffs(0, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(34F, 38F, -72F, 0.3490659F, -0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent34", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(41F, 48F, -100F, 0.2617994F, -0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent35", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(49F, 56F, -128F, 0.1745329F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent36", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(54F, 61F, -157F, 0.1745329F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent37", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(57F, 66F, -186F, 0.0872665F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tent38", CubeListBuilder.create().texOffs(0, 57).mirror()
                .addBox(0.0F, 0.0F, -32.0F, 1, 1, 32), PartPose.offset(57F, 69F, -217F));

        // Tentacle 4: Tent41-Tent48
        p.addOrReplaceChild("Tent41", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(16F, 7F, -15F, 0.0872665F, -0.4363323F, 0.0F));
        p.addOrReplaceChild("Tent42", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(28F, 9F, -42F, 0.0872665F, -0.5235988F, 0.0F));
        p.addOrReplaceChild("Tent43", CubeListBuilder.create().texOffs(0, 162).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 7, 7, 32),
                PartPose.offsetAndRotation(43F, 12F, -69F, 0.0872665F, -0.6108652F, 0.0F));
        p.addOrReplaceChild("Tent44", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(60F, 14F, -95F, 0.0872665F, -0.4363323F, 0.0F));
        p.addOrReplaceChild("Tent45", CubeListBuilder.create().texOffs(0, 125).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 5, 5, 32),
                PartPose.offsetAndRotation(73F, 17F, -123F, 0.0872665F, -0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent46", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(81F, 20F, -152F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent47", CubeListBuilder.create().texOffs(0, 90).mirror()
                .addBox(-1.0F, -1.0F, -32.0F, 3, 3, 32),
                PartPose.offsetAndRotation(84F, 23F, -182F, 0.0872665F, 0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent48", CubeListBuilder.create().texOffs(0, 57).mirror()
                .addBox(0.0F, 0.0F, -32.0F, 1, 1, 32),
                PartPose.offsetAndRotation(82F, 25F, -212F, 0.0872665F, 0.2617994F, 0.0F));

        // Tentacle 5: Tent51-Tent58
        p.addOrReplaceChild("Tent51", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(14F, -8F, -15F, -0.2617994F, -0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent52", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(22F, -16F, -44F, -0.2617994F, -0.3490659F, -0.0523599F));
        p.addOrReplaceChild("Tent53", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(32F, -24F, -71F, 0.0872665F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent54", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(37F, -21F, -101F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent55", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(40F, -19F, -129F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent56", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(42F, -16F, -159F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent57", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(45F, -14F, -189F, 0.2617994F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent58", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(40F, -6F, -218F, 0.3490659F, 0.2617994F, 0.0F));

        // Tentacle 6: Tent61-Tent68
        p.addOrReplaceChild("Tent61", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(-14F, -8F, -15F, -0.2617994F, 0.2617994F, 0.0F));
        p.addOrReplaceChild("Tent62", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(-21F, -16F, -43F, -0.2617994F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tent63", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(-31F, -24F, -69.5332946777344F, -0.0872665F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent64", CubeListBuilder.create().texOffs(80, 161).mirror()
                .addBox(-4.0F, -4.0F, -32.0F, 8, 8, 32),
                PartPose.offsetAndRotation(-36F, -27F, -98F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent65", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(-34F, -24F, -129F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent66", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(-31F, -21F, -160F, 0.0872665F, -0.0872665F, 0.0F));
        p.addOrReplaceChild("Tent67", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(-28F, -18F, -191F, 0.2617994F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tent68", CubeListBuilder.create().texOffs(80, 124).mirror()
                .addBox(-2.0F, -2.0F, -32.0F, 4, 4, 32),
                PartPose.offsetAndRotation(-23F, -10F, -219F, 0.3490659F, -0.2617994F, 0.0F));

        // Teeth
        p.addOrReplaceChild("Tooth1", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-7F, 23F, -29F, -0.1745329F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth2", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-3F, 24F, -27F, -0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth3", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-1F, 24F, -27F, -0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth4", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(1F, 24F, -27F, -0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth5", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(3F, 24F, -27F, -0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth6", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(5F, 24F, -27F, -0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth7", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(9F, 23F, -28F, -0.3490659F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth8", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(10F, 21F, -29F, -0.3490659F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth9", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(12F, 20F, -28F, -0.3490659F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth10", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(14F, 19F, -28F, -0.3490659F, 0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth11", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 14F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth12", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 12F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth13", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 10F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth14", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 8F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth15", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 6F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth16", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 4F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth17", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(15F, 2F, -30F, 0.0F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth18", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(13F, -1F, -32F, 0.1745329F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth19", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(11F, -3F, -32F, 0.1745329F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth20", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(9F, -5F, -32F, 0.1745329F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth21", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(9F, -5F, -32F, 0.1745329F, 0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth22", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(5F, -7F, -32F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth23", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(3F, -7F, -32F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth24", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(1F, -7F, -32F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth25", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-1F, -7F, -32F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth26", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-3F, -7F, -32F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth27", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-5F, -7F, -32F, 0.3490659F, 0.0F, 0.0F));
        p.addOrReplaceChild("Tooth28", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-1.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-8F, -6F, -32F, 0.3490659F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth29", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-11F, -4F, -32F, 0.3490659F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth30", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-13F, -2F, -32F, 0.3490659F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth31", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-14F, 0F, -32F, 0.3490659F, -0.1745329F, 0.0F));
        p.addOrReplaceChild("Tooth32", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-16F, 4F, -32F, 0.0F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth33", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-16F, 6F, -32F, 0.0F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth34", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-16F, 8F, -32F, 0.0F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth35", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-16F, 10F, -32F, 0.0F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth36", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-16F, 12F, -32F, 0.0F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth37", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-16F, 14F, -32F, 0.0F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth38", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-15F, 18F, -30F, -0.1745329F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth39", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-13F, 20F, -30F, -0.1745329F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth40", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-11F, 22F, -30F, -0.1745329F, -0.3490659F, 0.0F));
        p.addOrReplaceChild("Tooth41", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(0.0F, 0.0F, -8.0F, 1, 1, 8),
                PartPose.offsetAndRotation(-9F, 23F, -29F, -0.1745329F, -0.3490659F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(Kraken entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.Finright.zRot = Mth.cos(ageInTicks * 0.43F * ANIM_SPEED) * (float) Math.PI * 0.15F;
        this.Finleft.zRot = Mth.cos(ageInTicks * 0.32F * ANIM_SPEED) * (float) Math.PI * 0.14F;

        int attacking = entity.getAttacking();

        dangleTentacle(ageInTicks, 5, attacking, Tent51, Tent52, Tent53, Tent54, Tent55, Tent56, Tent57, Tent58);
        dangleTentacle(ageInTicks, 6, attacking, Tent61, Tent62, Tent63, Tent64, Tent65, Tent66, Tent67, Tent68);

        Sucktioncupleft.y = Tent58.y + (float) Math.sin(Tent58.xRot) * 30.0F * (float) Math.cos(Tent58.yRot);
        Sucktioncupleft.z = Tent58.z - (float) Math.cos(Tent58.xRot) * 30.0F * (float) Math.cos(Tent58.yRot);
        Sucktioncupleft.x = Tent58.x - (float) Math.sin(Tent58.yRot) * 30.0F * (float) Math.cos(Tent58.xRot);
        Sucktioncupleft.xRot = Tent58.xRot;
        Sucktioncupleft.yRot = Tent58.yRot;

        Sucktioncupright.y = Tent68.y + (float) Math.sin(Tent68.xRot) * 30.0F * (float) Math.cos(Tent68.yRot);
        Sucktioncupright.z = Tent68.z - (float) Math.cos(Tent68.xRot) * 30.0F * (float) Math.cos(Tent68.yRot);
        Sucktioncupright.x = Tent68.x - (float) Math.sin(Tent68.yRot) * 30.0F * (float) Math.cos(Tent68.xRot);
        Sucktioncupright.xRot = Tent68.xRot;
        Sucktioncupright.yRot = Tent68.yRot;

        float newangle = Mth.cos(ageInTicks * 0.66F) * (float) Math.PI * 0.15F;
        float nextangle = Mth.cos((ageInTicks + 0.1F) * 0.66F) * (float) Math.PI * 0.15F;
        if (nextangle > 0.0F && newangle < 0.0F) {
            if (attacking == 0) {
                ri1 = entity.getRandom().nextInt(10);
                ri2 = entity.getRandom().nextInt(15);
            } else {
                ri1 = entity.getRandom().nextInt(4);
                ri2 = entity.getRandom().nextInt(3);
            }
        }

        newangle = (ri1 == 1 || ri1 == 3)
                ? Mth.cos(ageInTicks * 0.5F * ANIM_SPEED) * (float) Math.PI * 0.015F
                : 0.0F;

        Mouth1.xRot = -0.38F + newangle;
        Mouth1.yRot = -0.38F + newangle;
        Mouth2.xRot = -0.38F + newangle;
        Mouth2.yRot = 0.38F - newangle;
        Mouth3.xRot = 0.38F - newangle;
        Mouth3.yRot = 0.38F - newangle;
        Mouth5.xRot = 0.38F - newangle;
        Mouth5.yRot = -0.38F + newangle;
        Mouth4.xRot = 0.38F - newangle;
        Mouth6.yRot = -0.38F + newangle;
        Mouth7.yRot = 0.38F - newangle;
        Mouth8.xRot = -0.38F + newangle;

        newangle *= 7.0F;

        Tooth2.xRot = -0.35F - newangle;
        Tooth3.xRot = -0.34F - newangle;
        Tooth4.xRot = -0.33F - newangle;
        Tooth5.xRot = -0.36F - newangle;
        Tooth6.xRot = -0.32F - newangle;
        Tooth11.yRot = 0.35F + newangle;
        Tooth12.yRot = 0.37F + newangle;
        Tooth13.yRot = 0.33F + newangle;
        Tooth14.yRot = 0.34F + newangle;
        Tooth15.yRot = 0.36F + newangle;
        Tooth16.yRot = 0.35F + newangle;
        Tooth17.yRot = 0.32F + newangle;
        Tooth22.xRot = 0.31F + newangle;
        Tooth23.xRot = 0.37F + newangle;
        Tooth24.xRot = 0.33F + newangle;
        Tooth25.xRot = 0.34F + newangle;
        Tooth26.xRot = 0.36F + newangle;
        Tooth27.xRot = 0.35F + newangle;
        Tooth32.yRot = -0.37F - newangle;
        Tooth33.yRot = -0.33F - newangle;
        Tooth34.yRot = -0.34F - newangle;
        Tooth35.yRot = -0.36F - newangle;
        Tooth36.yRot = -0.35F - newangle;
        Tooth37.yRot = -0.32F - newangle;
        Tooth7.xRot = -0.35F - newangle;
        Tooth7.yRot = 0.33F + newangle;
        Tooth8.xRot = -0.31F - newangle;
        Tooth8.yRot = 0.37F + newangle;
        Tooth9.xRot = -0.32F - newangle;
        Tooth9.yRot = 0.3F + newangle;
        Tooth10.xRot = -0.33F - newangle;
        Tooth10.yRot = 0.33F + newangle;
        Tooth18.xRot = 0.35F + newangle;
        Tooth18.yRot = 0.33F + newangle;
        Tooth19.xRot = 0.31F + newangle;
        Tooth19.yRot = 0.37F + newangle;
        Tooth20.xRot = 0.37F + newangle;
        Tooth20.yRot = 0.37F + newangle;
        Tooth21.xRot = 0.3F + newangle;
        Tooth21.yRot = 0.3F + newangle;
        Tooth28.xRot = 0.37F + newangle;
        Tooth28.yRot = -0.3F - newangle;
        Tooth29.xRot = 0.33F + newangle;
        Tooth29.yRot = -0.32F - newangle;
        Tooth30.xRot = 0.3F + newangle;
        Tooth30.yRot = -0.37F - newangle;
        Tooth31.xRot = 0.37F + newangle;
        Tooth31.yRot = -0.3F - newangle;
        Tooth38.xRot = -0.34F - newangle;
        Tooth38.yRot = -0.33F - newangle;
        Tooth39.xRot = -0.35F - newangle;
        Tooth39.yRot = -0.37F - newangle;
        Tooth40.xRot = -0.39F - newangle;
        Tooth40.yRot = -0.33F - newangle;
        Tooth41.xRot = -0.34F - newangle;
        Tooth41.yRot = -0.36F - newangle;
        Tooth1.xRot = -0.35F - newangle;
        Tooth1.yRot = -0.32F - newangle;

        dangleTentacle(ageInTicks, 1, 0, Tent11, Tent12, Tent13, Tent14, Tent15, Tent16, Tent17, Tent18);
        dangleTentacle(ageInTicks, 2, 0, Tent21, Tent22, Tent23, Tent24, Tent25, Tent26, Tent27, Tent28);
        dangleTentacle(ageInTicks, 3, 0, Tent31, Tent32, Tent33, Tent34, Tent35, Tent36, Tent37, Tent38);
        dangleTentacle(ageInTicks, 4, 0, Tent41, Tent42, Tent43, Tent44, Tent45, Tent46, Tent47, Tent48);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        Lefteye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Backbody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Centerbody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Sucktioncupleft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Finright.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tailbase1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tailtip.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Finleft.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Frontbody.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent54.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent62.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent63.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent64.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent58.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent66.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent67.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent68.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent28.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent51.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent52.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent53.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent65.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent55.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent56.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent57.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Sucktioncupright.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Righteye.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Mouth8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent61.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent38.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent22.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent23.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent24.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent25.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent26.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent27.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent48.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent32.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent33.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent34.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent35.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent36.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent37.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Jet.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent41.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent42.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent43.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent44.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent45.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent46.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent47.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent31.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth5.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth7.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth8.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth9.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth10.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth11.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth12.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth13.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth14.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth15.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth16.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth18.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth19.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth20.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth21.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth22.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth23.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth24.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth25.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth26.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth27.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth28.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth29.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth30.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth31.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth32.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth33.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth34.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth35.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth36.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth37.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth38.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth39.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth40.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tooth41.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        Tent17.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);

        poseStack.popPose();
    }

    private void dangleTentacle(float ageInTicks, int dir, int att,
                                ModelPart p1, ModelPart p2, ModelPart p3, ModelPart p4,
                                ModelPart p5, ModelPart p6, ModelPart p7, ModelPart p8) {
        float pi4 = 0.314159F;
        int dist = 30;
        float differ = 0.1F;
        float xoff = 0.0F;
        float ydiffer = 0.1F;
        float yoff = 0.0F;
        float s = -1.0F;
        float amp = 0.1F;

        if (dir == 1) differ = 0.101F;
        if (dir == 2) differ = 0.097F;
        if (dir == 3) differ = 0.093F;
        if (dir == 4) differ = 0.087F;
        if (dir == 1) ydiffer = 0.102F;
        if (dir == 2) ydiffer = 0.098F;
        if (dir == 3) ydiffer = 0.092F;
        if (dir == 4) ydiffer = 0.088F;
        if (dir == 2) xoff = 0.26F;
        if (dir == 3) xoff = 0.26F;
        if (dir == 1) yoff = 0.44F;
        if (dir == 4) yoff = -0.44F;
        if (dir == 5) differ = 0.2F;
        if (dir == 6) differ = 0.2F;
        if (dir == 5) xoff = -0.25F;
        if (dir == 6) xoff = -0.25F;
        if (dir == 6) s = 1.0F;

        if (att != 0) {
            if (dir == 5) { differ = 0.5F; amp = 0.03F; xoff = 0.0F; }
            if (dir == 6) { differ = 0.5F; amp = 0.03F; xoff = 0.0F; }
        }

        p1.xRot = xoff + s * Mth.cos(ageInTicks * differ * ANIM_SPEED) * (float) Math.PI * amp;
        p1.yRot = yoff - Mth.cos(ageInTicks * ydiffer * ANIM_SPEED) * (float) Math.PI * amp;

        p2.y = p1.y + (float) Math.sin(p1.xRot) * dist * (float) Math.cos(p1.yRot);
        p2.z = p1.z - (float) Math.cos(p1.xRot) * dist * (float) Math.cos(p1.yRot);
        p2.x = p1.x - (float) Math.sin(p1.yRot) * dist * (float) Math.cos(p1.xRot);
        p2.xRot = xoff / 2.0F + s * Mth.cos(ageInTicks * differ * ANIM_SPEED - pi4) * (float) Math.PI * amp;
        p2.yRot = yoff / 2.0F - Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - pi4) * (float) Math.PI * amp;

        p3.y = p2.y + (float) Math.sin(p2.xRot) * dist * (float) Math.cos(p2.yRot);
        p3.z = p2.z - (float) Math.cos(p2.xRot) * dist * (float) Math.cos(p2.yRot);
        p3.x = p2.x - (float) Math.sin(p2.yRot) * dist * (float) Math.cos(p2.xRot);
        p3.xRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 2.0F * pi4) * (float) Math.PI * amp;
        p3.yRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 2.0F * pi4) * (float) Math.PI * amp;

        p4.y = p3.y + (float) Math.sin(p3.xRot) * dist * (float) Math.cos(p3.yRot);
        p4.z = p3.z - (float) Math.cos(p3.xRot) * dist * (float) Math.cos(p3.yRot);
        p4.x = p3.x - (float) Math.sin(p3.yRot) * dist * (float) Math.cos(p3.xRot);
        p4.xRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 3.0F * pi4) * (float) Math.PI * amp;
        p4.yRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 3.0F * pi4) * (float) Math.PI * amp;

        p5.y = p4.y + (float) Math.sin(p4.xRot) * dist * (float) Math.cos(p4.yRot);
        p5.z = p4.z - (float) Math.cos(p4.xRot) * dist * (float) Math.cos(p4.yRot);
        p5.x = p4.x - (float) Math.sin(p4.yRot) * dist * (float) Math.cos(p4.xRot);
        p5.xRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 4.0F * pi4) * (float) Math.PI * amp;
        p5.yRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 4.0F * pi4) * (float) Math.PI * amp;

        p6.y = p5.y + (float) Math.sin(p5.xRot) * dist * (float) Math.cos(p5.yRot);
        p6.z = p5.z - (float) Math.cos(p5.xRot) * dist * (float) Math.cos(p5.yRot);
        p6.x = p5.x - (float) Math.sin(p5.yRot) * dist * (float) Math.cos(p5.xRot);
        p6.xRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 5.0F * pi4) * (float) Math.PI * amp;
        p6.yRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 5.0F * pi4) * (float) Math.PI * amp;

        p7.y = p6.y + (float) Math.sin(p6.xRot) * dist * (float) Math.cos(p6.yRot);
        p7.z = p6.z - (float) Math.cos(p6.xRot) * dist * (float) Math.cos(p6.yRot);
        p7.x = p6.x - (float) Math.sin(p6.yRot) * dist * (float) Math.cos(p6.xRot);
        p7.xRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 6.0F * pi4) * (float) Math.PI * amp;
        p7.yRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 6.0F * pi4) * (float) Math.PI * amp;

        p8.y = p7.y + (float) Math.sin(p7.xRot) * dist * (float) Math.cos(p7.yRot);
        p8.z = p7.z - (float) Math.cos(p7.xRot) * dist * (float) Math.cos(p7.yRot);
        p8.x = p7.x - (float) Math.sin(p7.yRot) * dist * (float) Math.cos(p7.xRot);
        p8.xRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 7.0F * pi4) * (float) Math.PI * amp;
        p8.yRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 7.0F * pi4) * (float) Math.PI * amp;
    }
}
