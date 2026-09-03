package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Elevator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Hoverboard renderer (orig RenderElevator.java). Texture follows the
 * board's synced paint color 1..10 (orig Elevator.java:45-54, 73-107 —
 * cycled by Ultimate Sword clicks); a recent hit rocks the board around X
 * by {@code sin(t) * t * damage / 10} in the synced forward direction
 * (orig RenderElevator.java:31-38, the vanilla boat wobble).
 */
public class ElevatorRenderer extends MobRenderer<Elevator, ModelElevator> {
    /** Index 0 unused; 1..10 match the original's texture1..texture10. */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[11];

    static {
        for (int i = 1; i <= 10; ++i) {
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID,
                    "textures/entity/elevator" + i + ".png");
        }
    }

    public static final ModelLayerLocation MODEL_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "elevator"), "main");

    public ElevatorRenderer(EntityRendererProvider.Context context) {
        // orig RenderElevator.java:23 — shadow 0.25.
        super(context, new ModelElevator(context.bakeLayer(MODEL_LAYER)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(Elevator entity) {
        int color = entity.getColor();
        if (color < 1 || color > 10) color = 1;
        return TEXTURES[color];
    }

    @Override
    protected void scale(Elevator entity, PoseStack poseStack, float partialTick) {
        // LivingEntityRenderer.render: scale(-1,-1,1); this.scale(...); translate(0,-1.501,0).
        // orig RenderElevator.java:43-44 flips and renders with NO lift, so cancel vanilla's in the
        // already-flipped frame: local +1.501 == world -1.501 (ENT-S-091, exact cancellation).
        poseStack.translate(0.0F, 1.501F, 0.0F);
    }

    /**
     * ENT-S-094: orig RenderElevator.java:19 extends the plain {@code Render}, not
     * RendererLivingEntity, so the 1.7.10 board had none of the living rotations.
     * {@code super.setupRotations} is deliberately NOT called: LivingEntityRenderer
     * .setupRotations (1.21.1 bytecode) is offsets 0-32 isShaking yaw jitter,
     * 34-59 the yaw, 62-126 the deathTime Z-flip (× getFlipDegrees 90), 129-180
     * the riptide spin, 183-262 the SLEEPING bed rotations and 265-300 the
     * Dinnerbone/Grumm upside-down flip (public static isEntityUpsideDown, so it
     * cannot be overridden). Only the yaw of offsets 44-59 is re-applied, and
     * unconditionally — orig RenderElevator.java:30 has no SLEEPING gate.
     */
    @Override
    protected void setupRotations(Elevator entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks, float scale) {
        // ENT-S-094: rotate by the ENTITY yaw, not the body yaw handed in as rotationYaw.
        // LivingEntityRenderer.render (1.21.1 bytecode) computes fload 8 = Mth.rotLerp(partialTicks,
        // yBodyRotO, yBodyRot) at 103-115 and passes it here at 379-390; the 1.7.10 RenderManager
        // .renderEntityStatic (bnn.a(sa,F,Z) offsets 88-104: getfield prevRotationYaw, getfield
        // rotationYaw, getfield prevRotationYaw, fsub, fload partial, fmul, fadd) handed
        // RenderElevator.doRender prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks:
        // the entity yaw, a plain lerp with no wrap. Mth.lerp(delta, start, end) (bytecode 0-7:
        // fload start, fload delta, fload end, fload start, fsub, fmul, fadd) is start + delta *
        // (end - start): the same three operations, bit-identical since IEEE fmul is commutative.
        // Mth.rotLerp (bytecode 0-10) would first pass (end - start) through wrapDegrees; it cannot
        // differ here, because LivingEntity.tick (offsets 411-466, after the aiStep yaw writes at
        // 171 where tickRidden/lerpPositionAndRotationStep run) leaves yRot - yRotO within
        // [-180, 180), on which wrapDegrees is the identity, and Entity.absRotateTo (27-32) resets
        // yRotO = yRot; the plain lerp is used because it is the exact 1.7.10 formula.
        float entityYaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        // orig RenderElevator.java:30 — glRotatef(180 - yaw, 0, 1, 0).
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        // orig RenderElevator.java:31-38 — boat-style hit wobble.
        float hitTime = (float) entity.getTimeSinceHit() - partialTicks;
        float damage = entity.getDamageTaken() - partialTicks;
        if (damage < 0.0f) {
            damage = 0.0f;
        }
        if (hitTime > 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    Mth.sin(hitTime) * hitTime * damage / 10.0f * (float) entity.getForwardDirection()));
        }
    }

    /**
     * ENT-S-094: no name tag. In 1.7.10 the label (Render.renderLivingLabel) was
     * reached only from RendererLivingEntity.passSpecialRender, which the plain
     * Render never ran, and orig RenderElevator.java:48-50 calls nothing but
     * renderElevator. MobRenderer.shouldShowName (bytecode 0-38) would otherwise
     * gate a custom-named board through the living distance/team rules.
     */
    @Override
    protected boolean shouldShowName(Elevator entity) {
        return false;
    }

    /*
     * ENT-S-094: the leash line is NOT suppressed on this path, deliberately. The 1.7.10 plain
     * Render drew no leash (bno's method list is doRender/bindTexture/renderShadow/
     * doRenderShadowAndFire/renderLivingLabel/renderEntityOnFire; the line lived in
     * RendererLivingEntity), but in 1.21.1 the only leash draw for a Mob is EntityRenderer
     * .renderLeash, which is PRIVATE (javap: private <E extends Entity> void renderLeash(T, float,
     * PoseStack, MultiBufferSource, E)) and is reached only from EntityRenderer.render offsets
     * 0-36 (instanceof Leashable -> Leashable.getLeashHolder -> renderLeash), which
     * LivingEntityRenderer.render invokes by invokespecial at 695-705; MobRenderer (1.21.1)
     * overrides nothing but shouldShowName and getShadowRadius, and NeoForge 21.1 has no leash
     * event. No renderer hook stops the line short of copying LivingEntityRenderer.render, so it
     * is left; the seam (OreSpawnGeoReplacedEntityRenderer.renderLeash) leaves it for the same
     * reason, so both paths draw it exactly once through the same vanilla code.
     */
}
