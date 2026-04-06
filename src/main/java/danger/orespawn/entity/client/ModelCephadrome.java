package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import danger.orespawn.entity.Cephadrome;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelCephadrome extends EntityModel<Cephadrome> {
    private final float wingspeed = 1.0f;
    private final ModelPart leftfoot;
    private final ModelPart butt;
    private final ModelPart rightfoot;
    private final ModelPart topfin1;
    private final ModelPart topfin2;
    private final ModelPart topfin3;
    private final ModelPart topfin4;
    private final ModelPart leftshoulder;
    private final ModelPart lefwingfin1;
    private final ModelPart tailfin1;
    private final ModelPart tailmembrane2;
    private final ModelPart tailfin2;
    private final ModelPart tailfin4;
    private final ModelPart tailfin3;
    private final ModelPart tailmembrane1;
    private final ModelPart topmem1;
    private final ModelPart topmem2;
    private final ModelPart topmem3;
    private final ModelPart topmem4;
    private final ModelPart neck1;
    private final ModelPart body;
    private final ModelPart chest1;
    private final ModelPart leftleg1;
    private final ModelPart mouth;
    private final ModelPart neck2;
    private final ModelPart head;
    private final ModelPart hammerhead;
    private final ModelPart chest;
    private final ModelPart neck3;
    private final ModelPart tail1;
    private final ModelPart rightleg1;
    private final ModelPart leftleg2;
    private final ModelPart rightleg2;
    private final ModelPart body2;
    private final ModelPart leftleg3;
    private final ModelPart rightleg3;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tailmembrane3;
    private final ModelPart leftwingfin2;
    private final ModelPart leftwingfin3;
    private final ModelPart leftwingfin4;
    private final ModelPart leftwingmembrane;
    private final ModelPart rightshoulder;
    private final ModelPart rightwingfin1;
    private final ModelPart rightwingfin2;
    private final ModelPart rightwingfin3;
    private final ModelPart rightwingfin4;
    private final ModelPart rightwingmembrane;
    private final ModelPart hammerhead2;

    public ModelCephadrome(ModelPart root) {
        this.leftfoot = root.getChild("leftfoot");
        this.butt = root.getChild("butt");
        this.rightfoot = root.getChild("rightfoot");
        this.topfin1 = root.getChild("topfin1");
        this.topfin2 = root.getChild("topfin2");
        this.topfin3 = root.getChild("topfin3");
        this.topfin4 = root.getChild("topfin4");
        this.leftshoulder = root.getChild("leftshoulder");
        this.lefwingfin1 = root.getChild("lefwingfin1");
        this.tailfin1 = root.getChild("tailfin1");
        this.tailmembrane2 = root.getChild("tailmembrane2");
        this.tailfin2 = root.getChild("tailfin2");
        this.tailfin4 = root.getChild("tailfin4");
        this.tailfin3 = root.getChild("tailfin3");
        this.tailmembrane1 = root.getChild("tailmembrane1");
        this.topmem1 = root.getChild("topmem1");
        this.topmem2 = root.getChild("topmem2");
        this.topmem3 = root.getChild("topmem3");
        this.topmem4 = root.getChild("topmem4");
        this.neck1 = root.getChild("neck1");
        this.body = root.getChild("body");
        this.chest1 = root.getChild("chest1");
        this.leftleg1 = root.getChild("leftleg1");
        this.mouth = root.getChild("mouth");
        this.neck2 = root.getChild("neck2");
        this.head = root.getChild("head");
        this.hammerhead = root.getChild("hammerhead");
        this.chest = root.getChild("chest");
        this.neck3 = root.getChild("neck3");
        this.tail1 = root.getChild("tail1");
        this.rightleg1 = root.getChild("rightleg1");
        this.leftleg2 = root.getChild("leftleg2");
        this.rightleg2 = root.getChild("rightleg2");
        this.body2 = root.getChild("body2");
        this.leftleg3 = root.getChild("leftleg3");
        this.rightleg3 = root.getChild("rightleg3");
        this.tail2 = root.getChild("tail2");
        this.tail3 = root.getChild("tail3");
        this.tailmembrane3 = root.getChild("tailmembrane3");
        this.leftwingfin2 = root.getChild("leftwingfin2");
        this.leftwingfin3 = root.getChild("leftwingfin3");
        this.leftwingfin4 = root.getChild("leftwingfin4");
        this.leftwingmembrane = root.getChild("leftwingmembrane");
        this.rightshoulder = root.getChild("rightshoulder");
        this.rightwingfin1 = root.getChild("rightwingfin1");
        this.rightwingfin2 = root.getChild("rightwingfin2");
        this.rightwingfin3 = root.getChild("rightwingfin3");
        this.rightwingfin4 = root.getChild("rightwingfin4");
        this.rightwingmembrane = root.getChild("rightwingmembrane");
        this.hammerhead2 = root.getChild("hammerhead2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("leftfoot", CubeListBuilder.create().texOffs(41, 194).mirror()
            .addBox(-2.0F, 34.0F, -12.0F, 9, 4, 10), PartPose.offset(7.0F, -14.0F, 17.0F));

        partdefinition.addOrReplaceChild("butt", CubeListBuilder.create().texOffs(367, 235).mirror()
            .addBox(0.0F, 0.0F, -2.0F, 9, 14, 6), PartPose.offsetAndRotation(-4.5F, -8.0F, 29.0F, -0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightfoot", CubeListBuilder.create().texOffs(41, 170).mirror()
            .addBox(-7.0F, 34.0F, -12.0F, 9, 4, 10), PartPose.offset(-7.0F, -14.0F, 17.0F));

        partdefinition.addOrReplaceChild("topfin1", CubeListBuilder.create().texOffs(64, 112).mirror()
            .addBox(-3.0F, -2.0F, -30.0F, 6, 3, 30), PartPose.offsetAndRotation(0.0F, -15.0F, -7.0F, -1.850049F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topfin2", CubeListBuilder.create().texOffs(69, 81).mirror()
            .addBox(-3.0F, -2.0F, -25.0F, 6, 3, 25), PartPose.offsetAndRotation(0.0F, -15.0F, -2.0F, -2.076942F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topfin3", CubeListBuilder.create().texOffs(-1, 140).mirror()
            .addBox(-3.0F, -2.0F, -20.0F, 6, 3, 20), PartPose.offsetAndRotation(0.0F, -16.0F, 3.0F, -2.426008F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topfin4", CubeListBuilder.create().texOffs(148, 148).mirror()
            .addBox(-3.0F, -2.0F, -10.0F, 6, 3, 10), PartPose.offsetAndRotation(0.0F, -17.0F, 13.0F, -2.635447F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftshoulder", CubeListBuilder.create().texOffs(144, 236).mirror()
            .addBox(0.0F, 0.0F, 1.0F, 6, 8, 11), PartPose.offsetAndRotation(6.0F, -16.0F, -14.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("lefwingfin1", CubeListBuilder.create().texOffs(147, 96).mirror()
            .addBox(0.0F, -2.0F, -2.0F, 70, 5, 3), PartPose.offsetAndRotation(9.0F, -12.0F, -11.0F, -0.2617994F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("tailfin1", CubeListBuilder.create().texOffs(168, 0).mirror()
            .addBox(-6.0F, -1.0F, 0.0F, 12, 3, 30), PartPose.offsetAndRotation(0.0F, -9.0F, 56.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailmembrane2", CubeListBuilder.create().texOffs(201, 38).mirror()
            .addBox(0.0F, -8.0F, 3.0F, 0, 10, 19), PartPose.offsetAndRotation(0.0F, 0.0F, 56.0F, -0.296706F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailfin2", CubeListBuilder.create().texOffs(186, 184).mirror()
            .addBox(-4.0F, 0.0F, 0.0F, 8, 2, 27), PartPose.offsetAndRotation(0.0F, -7.0F, 56.0F, -0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailfin4", CubeListBuilder.create().texOffs(186, 137).mirror()
            .addBox(-4.0F, 1.0F, 1.0F, 8, 3, 22), PartPose.offsetAndRotation(0.0F, -3.0F, 56.0F, -0.837758F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailfin3", CubeListBuilder.create().texOffs(185, 216).mirror()
            .addBox(-4.0F, 0.0F, 1.0F, 8, 2, 23), PartPose.offsetAndRotation(0.0F, -5.0F, 57.0F, -0.5759587F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailmembrane1", CubeListBuilder.create().texOffs(245, 38).mirror()
            .addBox(0.0F, 0.0F, 4.0F, 0, 11, 21), PartPose.offsetAndRotation(0.0F, -9.0F, 56.0F, 0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topmem1", CubeListBuilder.create().texOffs(25, 0).mirror()
            .addBox(0.0F, -25.0F, 0.0F, 0, 24, 10), PartPose.offsetAndRotation(0.0F, -15.0F, -6.0F, -0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topmem2", CubeListBuilder.create().texOffs(135, 0).mirror()
            .addBox(1.0F, -22.0F, 0.0F, 0, 20, 10), PartPose.offsetAndRotation(-1.0F, -15.0F, -2.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topmem3", CubeListBuilder.create().texOffs(258, 0).mirror()
            .addBox(0.0F, -18.0F, 0.0F, 0, 18, 8), PartPose.offsetAndRotation(0.0F, -16.0F, 3.0F, -0.8901179F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("topmem4", CubeListBuilder.create().texOffs(282, 0).mirror()
            .addBox(0.0F, -9.0F, 0.0F, 0, 9, 10), PartPose.offsetAndRotation(0.0F, -17.0F, 13.0F, -1.117011F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(404, 235).mirror()
            .addBox(-6.0F, -5.0F, -10.0F, 10, 9, 10), PartPose.offsetAndRotation(1.0F, -6.0F, -33.0F, 0.3665191F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 220).mirror()
            .addBox(-6.0F, -11.0F, -10.0F, 12, 15, 19), PartPose.offsetAndRotation(0.0F, -7.0F, 3.0F, 0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("chest1", CubeListBuilder.create().texOffs(98, 210).mirror()
            .addBox(-3.0F, -4.0F, -2.0F, 10, 11, 5), PartPose.offsetAndRotation(-2.0F, -6.0F, -13.0F, 1.029744F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg1", CubeListBuilder.create().texOffs(135, 183).mirror()
            .addBox(-1.0F, 0.0F, -4.0F, 7, 18, 10), PartPose.offsetAndRotation(7.0F, -14.0F, 17.0F, -0.5759587F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(92, 150).mirror()
            .addBox(-7.0F, 1.0F, 3.0F, 14, 15, 4), PartPose.offsetAndRotation(0.0F, -6.0F, -43.0F, -0.8726646F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(152, 110).mirror()
            .addBox(-6.0F, -5.0F, -17.0F, 11, 10, 17), PartPose.offsetAndRotation(0.5F, -10.0F, -19.0F, 0.2617994F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(275, 219).mirror()
            .addBox(-10.0F, -3.0F, -16.0F, 20, 7, 16), PartPose.offsetAndRotation(0.0F, -6.0F, -43.0F, 0.5061455F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hammerhead", CubeListBuilder.create().texOffs(258, 134).mirror()
            .addBox(-18.0F, -2.0F, -15.0F, 36, 6, 14), PartPose.offsetAndRotation(0.0F, -6.0F, -43.0F, 0.4537856F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(100, 15).mirror()
            .addBox(-3.0F, -3.0F, 0.0F, 9, 29, 7), PartPose.offsetAndRotation(-1.5F, 0.0F, -5.0F, 1.413717F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("neck3", CubeListBuilder.create().texOffs(264, 173).mirror()
            .addBox(-6.0F, -5.0F, -16.0F, 12, 11, 16), PartPose.offsetAndRotation(0.0F, -11.0F, -6.0F, 0.0872665F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(51, 5).mirror()
            .addBox(-5.0F, -6.0F, 0.0F, 10, 13, 14), PartPose.offsetAndRotation(0.0F, -10.0F, 22.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg1", CubeListBuilder.create().texOffs(94, 175).mirror()
            .addBox(-6.0F, 0.0F, -4.0F, 7, 18, 10), PartPose.offsetAndRotation(-7.0F, -14.0F, 17.0F, -0.5759587F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg2", CubeListBuilder.create().texOffs(28, 112).mirror()
            .addBox(-1.0F, 6.0F, -17.0F, 7, 12, 7), PartPose.offsetAndRotation(7.0F, -14.0F, 17.0F, 0.9773844F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg2", CubeListBuilder.create().texOffs(32, 90).mirror()
            .addBox(-6.0F, 6.0F, -17.0F, 7, 12, 7), PartPose.offsetAndRotation(-7.0F, -14.0F, 17.0F, 0.9773844F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(400, 179).mirror()
            .addBox(0.0F, 3.0F, 3.0F, 12, 16, 16), PartPose.offsetAndRotation(-6.0F, -23.0F, 6.0F, -0.1919862F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftleg3", CubeListBuilder.create().texOffs(351, 192).mirror()
            .addBox(-1.0F, 17.0F, 10.0F, 7, 17, 6), PartPose.offsetAndRotation(7.0F, -14.0F, 17.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightleg3", CubeListBuilder.create().texOffs(323, 192).mirror()
            .addBox(-6.0F, 17.0F, 10.0F, 7, 17, 6), PartPose.offsetAndRotation(-7.0F, -14.0F, 17.0F, -0.5235988F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(51, 55).mirror()
            .addBox(-6.0F, -6.0F, 0.0F, 9, 12, 14), PartPose.offsetAndRotation(1.5F, -7.0F, 35.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(105, 52).mirror()
            .addBox(-5.0F, -6.0F, 0.0F, 8, 11, 14), PartPose.offsetAndRotation(1.0F, -5.0F, 48.0F, -0.1396263F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("tailmembrane3", CubeListBuilder.create().texOffs(155, 38).mirror()
            .addBox(0.0F, -10.0F, 0.0F, 0, 10, 18), PartPose.offsetAndRotation(0.0F, 2.0F, 56.0F, -0.837758F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("leftwingfin2", CubeListBuilder.create().texOffs(160, 83).mirror()
            .addBox(0.0F, -2.0F, 0.0F, 64, 4, 2), PartPose.offsetAndRotation(9.0F, -12.0F, -11.0F, -0.2617994F, -0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("leftwingfin3", CubeListBuilder.create().texOffs(209, 106).mirror()
            .addBox(0.0F, -2.0F, 0.0F, 48, 4, 2), PartPose.offsetAndRotation(9.0F, -11.0F, -10.0F, -0.2617994F, -0.7853982F, 0.0F));

        partdefinition.addOrReplaceChild("leftwingfin4", CubeListBuilder.create().texOffs(233, 120).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 37, 4, 2), PartPose.offsetAndRotation(9.0F, -13.0F, -6.0F, -0.2617994F, -1.186824F, 0.0F));

        partdefinition.addOrReplaceChild("leftwingmembrane", CubeListBuilder.create().texOffs(300, 27).mirror()
            .addBox(3.0F, 0.0F, 0.0F, 64, 0, 34), PartPose.offsetAndRotation(9.0F, -13.0F, -10.0F, -0.0872665F, -0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rightshoulder", CubeListBuilder.create().texOffs(0, 193).mirror()
            .addBox(0.0F, 0.0F, 0.0F, 6, 8, 11), PartPose.offsetAndRotation(-12.0F, -16.0F, -13.0F, -0.1745329F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("rightwingfin1", CubeListBuilder.create().texOffs(344, 109).mirror()
            .addBox(-69.0F, -2.0F, 0.0F, 69, 5, 3), PartPose.offsetAndRotation(-10.0F, -12.0F, -13.0F, -0.2617994F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("rightwingfin2", CubeListBuilder.create().texOffs(349, 119).mirror()
            .addBox(-63.0F, -2.0F, 0.0F, 64, 4, 2), PartPose.offsetAndRotation(-9.0F, -12.0F, -11.0F, -0.2617994F, 0.4363323F, 0.0F));

        partdefinition.addOrReplaceChild("rightwingfin3", CubeListBuilder.create().texOffs(368, 128).mirror()
            .addBox(-49.0F, 0.0F, 0.0F, 48, 4, 2), PartPose.offsetAndRotation(-9.0F, -13.0F, -9.0F, -0.2617994F, 0.7679449F, 0.0F));

        partdefinition.addOrReplaceChild("rightwingfin4", CubeListBuilder.create().texOffs(379, 137).mirror()
            .addBox(-35.0F, 0.0F, 0.0F, 37, 4, 2), PartPose.offsetAndRotation(-9.0F, -13.0F, -6.0F, -0.2617994F, 1.186824F, 0.0F));

        partdefinition.addOrReplaceChild("rightwingmembrane", CubeListBuilder.create().texOffs(300, 67).mirror()
            .addBox(-67.0F, -1.0F, 0.0F, 64, 0, 34), PartPose.offsetAndRotation(-9.0F, -12.0F, -12.0F, -0.0872665F, 0.1745329F, 0.0F));

        partdefinition.addOrReplaceChild("hammerhead2", CubeListBuilder.create().texOffs(258, 157).mirror()
            .addBox(-25.0F, 0.0F, -14.0F, 50, 4, 7), PartPose.offsetAndRotation(0.0F, -7.0F, -43.0F, 0.4537856F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 256);
    }

    @Override
    public void setupAnim(Cephadrome entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float rf1 = 0.0f;
        float newangle = 0.0f;
        float lspeed = 0.0f;
        float pi4 = 0.7853982f;
        float tailspeed = 0.76f;
        float tailamp = 0.1f;

        if ((double)limbSwingAmount > 0.001) {
            lspeed = (float)((entity.xOld - entity.getX()) * (entity.xOld - entity.getX()) + (entity.zOld - entity.getZ()) * (entity.zOld - entity.getZ()));
            lspeed = (float)Math.sqrt(lspeed);
            newangle = Mth.cos(ageInTicks * 0.75f * this.wingspeed) * (float)Math.PI * lspeed * 0.4f;
            if ((double)newangle > 0.5) {
                newangle = 0.75f;
            }
            if ((double)newangle < -0.5) {
                newangle = -0.75f;
            }
        } else {
            newangle = 0.0f;
        }

        if (entity.getActivity() != 0) {
            newangle = 1.0f;
            this.rightleg1.xRot = -0.58f + newangle;
            this.rightleg2.xRot = 0.98f + newangle;
            this.rightleg3.xRot = -0.52f + newangle;
            this.rightfoot.xRot = newangle;
            this.leftleg1.xRot = -0.58f + newangle;
            this.leftleg2.xRot = 0.98f + newangle;
            this.leftleg3.xRot = -0.52f + newangle;
            this.leftfoot.xRot = newangle;
        } else {
            this.rightleg1.xRot = -0.58f + newangle;
            this.rightleg2.xRot = 0.98f + newangle;
            this.rightleg3.xRot = -0.52f + newangle;
            this.rightfoot.xRot = newangle;
            this.leftleg1.xRot = -0.58f - newangle;
            this.leftleg2.xRot = 0.98f - newangle;
            this.leftleg3.xRot = -0.52f - newangle;
            this.leftfoot.xRot = -newangle;
        }

        newangle = entity.getActivity() != 0 ? Mth.cos(ageInTicks * 0.55f * this.wingspeed) * (float)Math.PI * 0.28f : (entity.getAttacking() == 0 ? -0.85f + Mth.cos(ageInTicks * 0.2f * this.wingspeed) * (float)Math.PI * 0.028f : -0.65f + Mth.cos(ageInTicks * 0.9f * this.wingspeed) * (float)Math.PI * 0.068f);
        this.lefwingfin1.zRot = newangle;
        this.leftwingfin2.zRot = newangle;
        this.leftwingfin3.zRot = newangle;
        this.leftwingfin4.zRot = newangle;
        this.leftwingmembrane.zRot = newangle;
        this.rightwingfin1.zRot = -newangle;
        this.rightwingfin2.zRot = -newangle;
        this.rightwingfin3.zRot = -newangle;
        this.rightwingfin4.zRot = -newangle;
        this.rightwingmembrane.zRot = -newangle;

        newangle = Mth.cos(ageInTicks * 0.15f * this.wingspeed) * (float)Math.PI * 0.05f;
        this.topfin1.xRot = -1.85f - Math.abs(newangle);
        this.topmem1.xRot = -0.26f - Math.abs(newangle);
        this.topfin2.xRot = -2.07f - Math.abs(newangle / 2.0f);
        this.topmem2.xRot = -0.52f - Math.abs(newangle / 2.0f);
        this.topfin3.xRot = -2.42f - Math.abs(newangle / 4.0f);
        this.topmem3.xRot = -0.89f - Math.abs(newangle / 4.0f);
        this.topfin4.xRot = -2.63f - Math.abs(newangle / 8.0f);
        this.topmem4.xRot = -1.11f - Math.abs(newangle / 8.0f);

        if (entity.getActivity() == 0 && entity.getAttacking() == 0) {
            tailspeed = 0.22f;
            tailamp = 0.03f;
        }

        this.tail1.yRot = Mth.cos(ageInTicks * tailspeed * this.wingspeed) * (float)Math.PI * 0.04f;
        this.tail2.z = this.tail1.z + (float)Math.cos(this.tail1.yRot) * 13.0f;
        this.tail2.x = this.tail1.x + 1.5f + (float)Math.sin(this.tail1.yRot) * 13.0f;
        this.tail2.yRot = Mth.cos(ageInTicks * tailspeed * this.wingspeed - pi4) * (float)Math.PI * tailamp;
        this.tail3.z = this.tail2.z + (float)Math.cos(this.tail2.yRot) * 13.0f;
        this.tail3.x = this.tail2.x - 0.5f + (float)Math.sin(this.tail2.yRot) * 13.0f;
        this.tail3.yRot = Mth.cos(ageInTicks * tailspeed * this.wingspeed - 2.0f * pi4) * (float)Math.PI * tailamp;

        this.tailfin1.z = this.tail3.z + (float)Math.cos(this.tail3.yRot) * 10.0f;
        this.tailfin1.x = this.tail3.x - 1.0f + (float)Math.sin(this.tail3.yRot) * 10.0f;
        this.tailfin1.yRot = Mth.cos(ageInTicks * tailspeed * this.wingspeed - 3.0f * pi4) * (float)Math.PI * tailamp;

        this.tailfin2.z = this.tailfin1.z;
        this.tailfin2.x = this.tailfin1.x;
        this.tailfin2.yRot = this.tailfin1.yRot;
        this.tailfin3.z = this.tailfin1.z;
        this.tailfin3.x = this.tailfin1.x;
        this.tailfin3.yRot = this.tailfin1.yRot;
        this.tailfin4.z = this.tailfin1.z;
        this.tailfin4.x = this.tailfin1.x;
        this.tailfin4.yRot = this.tailfin1.yRot;
        this.tailmembrane1.z = this.tailfin1.z;
        this.tailmembrane1.x = this.tailfin1.x;
        this.tailmembrane1.yRot = this.tailfin1.yRot;
        this.tailmembrane2.z = this.tailfin1.z;
        this.tailmembrane2.x = this.tailfin1.x;
        this.tailmembrane2.yRot = this.tailfin1.yRot;
        this.tailmembrane3.z = this.tailfin1.z;
        this.tailmembrane3.x = this.tailfin1.x;
        this.tailmembrane3.yRot = this.tailfin1.yRot;

        float headYaw = netHeadYaw;
        if (entity.getActivity() == 1) {
            headYaw = (entity.yHeadRotO - entity.yHeadRot) * 10.0f;
            headYaw = -headYaw;
            rf1 += (headYaw - rf1) / 50.0f;
            if (rf1 > 50.0f) {
                rf1 = 50.0f;
            }
            if (rf1 < -50.0f) {
                rf1 = -50.0f;
            }
            headYaw = rf1;
        } else {
            headYaw /= 2.0f;
        }

        this.neck3.yRot = (float)Math.toRadians(headYaw) * 0.125f;
        this.neck2.z = this.neck3.z - (float)Math.cos(this.neck3.yRot) * 14.0f;
        this.neck2.x = this.neck3.x + 0.5f - (float)Math.sin(this.neck3.yRot) * 14.0f;
        this.neck2.yRot = (float)Math.toRadians(headYaw) * 0.25f;
        this.neck1.z = this.neck2.z - (float)Math.cos(this.neck2.yRot) * 14.0f;
        this.neck1.x = this.neck2.x + 0.5f - (float)Math.sin(this.neck2.yRot) * 14.0f;
        this.neck1.yRot = (float)Math.toRadians(headYaw) * 0.5f;
        this.head.z = this.neck1.z - (float)Math.cos(this.neck1.yRot) * 8.0f;
        this.head.x = this.neck1.x - (float)Math.sin(this.neck1.yRot) * 8.0f;
        this.head.yRot = (float)Math.toRadians(headYaw) * 0.75f;

        this.hammerhead.z = this.head.z;
        this.hammerhead.x = this.head.x;
        this.hammerhead.yRot = this.head.yRot;
        this.hammerhead2.z = this.head.z;
        this.hammerhead2.x = this.head.x;
        this.hammerhead2.yRot = this.head.yRot;
        this.mouth.z = this.head.z;
        this.mouth.x = this.head.x;
        this.mouth.yRot = this.head.yRot;

        newangle = Mth.cos(ageInTicks * 0.5f * this.wingspeed) * (float)Math.PI * 0.14f;
        this.mouth.xRot = entity.getAttacking() != 0 ? -0.61f + newangle : -0.87f;

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.leftfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.butt.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightfoot.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topfin3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topfin4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftshoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.lefwingfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailmembrane2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfin4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailfin3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailmembrane1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topmem1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topmem2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topmem3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.topmem4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hammerhead.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.neck3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightleg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.tailmembrane3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwingfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwingfin3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwingfin4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.leftwingmembrane.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightshoulder.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwingfin1.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwingfin2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwingfin3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwingfin4.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.rightwingmembrane.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.hammerhead2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
