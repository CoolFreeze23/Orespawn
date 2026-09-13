package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.EntityLeafMonster;
import danger.orespawn.entity.pose.LeafMonsterPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Leaf Monster (the hook lanes, 2026-09-14, addendum item 10): {@link LeafMonsterModel#poseFrom} verbatim on
 * the converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist
 * delivers {@code idle} and {@code walk}). No wingspeed: the ATTACKING branch is the whole pose - at rest the body sits
 * at y 16 and both arms at y 8 with every rotation 0 (the bush); attacking, the body rises to y 0 and the arms to y -8
 * (POSITION writes through {@link #moveTo}), the legs take the THRESHOLD gait ({@code cos(age * 0.95) * PI * 0.25 *
 * limbSwingAmount} above a walking speed of a tenth, mirrored) and the arms wave on the {@code |cos|} idiom
 * ({@code |cos(age * 0.7) * PI * 0.55|}: the right arm's yaw and both pitches the negative, the left arm's yaw
 * positive). The entity is read through {@link LeafMonsterPose}.
 *
 * <p>Scale and shadow follow {@link LeafMonsterRenderer}: 1.0 render scale (identity - no scale override, so no scale
 * hook) and a 0.65 x 1.0 shadow (ENT-S-092).</p>
 */
public final class LeafMonsterGeoReplacement extends OreSpawnGeoReplacement<EntityLeafMonster> {
    private static final GeoReplacementDescriptor<EntityLeafMonster> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.ENTITY_LEAF_MONSTER.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            EntityLeafMonster.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/leafmonster.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/leafmonster.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/leafmonster.png"),
            LeafMonsterRenderer.SHADOW) {
    };

    public LeafMonsterGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        LeafMonsterPose entity = inputs.subject(LeafMonsterPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        // LeafMonsterModel.poseFrom (the body of the classic setupAnim) verbatim: only y is written on the body and
        // the arms, their x / z stay the bind.
        float[] body = classicPosition(bone(processor, "body"));
        float[] rarm = classicPosition(bone(processor, "rarm"));
        float[] larm = classicPosition(bone(processor, "larm"));
        int attacking = entity.getAttacking();
        if (attacking == 0) {
            moveTo(processor, "body", body[0], 16.0F, body[2]);
            moveTo(processor, "rarm", rarm[0], 8.0F, rarm[2]);
            moveTo(processor, "larm", larm[0], 8.0F, larm[2]);
            rotateY(processor, "rarm", 0.0F);
            rotateY(processor, "larm", 0.0F);
            rotateX(processor, "rarm", 0.0F);
            rotateX(processor, "larm", 0.0F);
            rotateX(processor, "lleg", 0.0F);
            rotateX(processor, "rleg", 0.0F);
        } else {
            moveTo(processor, "body", body[0], 0.0F, body[2]);
            moveTo(processor, "rarm", rarm[0], -8.0F, rarm[2]);
            moveTo(processor, "larm", larm[0], -8.0F, larm[2]);
            float legAngle = limbSwingAmount > 0.1f ? Mth.cos(ageInTicks * 0.95f) * (float) Math.PI * 0.25f * limbSwingAmount : 0.0f;
            rotateX(processor, "lleg", legAngle);
            rotateX(processor, "rleg", -legAngle);
            float armAngle = Mth.cos(ageInTicks * 0.7f) * (float) Math.PI * 0.55f;
            rotateY(processor, "rarm", -Math.abs(armAngle));
            rotateY(processor, "larm", Math.abs(armAngle));
            rotateX(processor, "rarm", -Math.abs(armAngle));
            rotateX(processor, "larm", -Math.abs(armAngle));
        }
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<EntityLeafMonster, LeafMonsterGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new LeafMonsterGeoReplacement());
        }
    }
}
