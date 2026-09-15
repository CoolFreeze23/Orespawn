package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Kraken;
import danger.orespawn.entity.pose.KrakenPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Kraken (the hooks, landed by the first Tier-1 slice T1a): {@link ModelKraken#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers
 * {@code idle} and {@code walk} ; the geo, the wiring and the proofs landed with T1a). The port's classic model as it is,
 * {@code ANIM_SPEED} 1.0f (orig ClientProxyOreSpawn.java:444 {@code new ModelKraken(1.0f)} ): the two fins' roll on 0.43 /
 * 0.32 cosines (Z, x PI x 0.15 / 0.14); the six eight-ring tentacles through {@link #dangleTentacle} (the classic
 * helper, the same name): the root ring pitched and yawed on the tentacle's own {@code differ} / {@code ydiffer}
 * cosines around its {@code xoff} / {@code yoff} , each following ring's pivot FOLLOWING the last 30 units along its
 * pitch and yaw (POSITION writes through {@link #moveTo} ) and lagging a further 0.314159 rad, the two front tentacles' sign
 * flipped for the right one and the ATTACKING branch ({@code getAttacking() != 0}) stiffening them (0.5 / 0.03 / no offset
 * over 0.2 / 0.1 / -0.25); the two suction cups riding 30 units past the eighth ring and copying its angles; the mouth
 * TWITCH latch (the Robot2 precedent, orig ModelKraken.java:1045-1057): on the falling zero crossing of a 0.66 cosine (the
 * 0.1-tick look-ahead) the per-entity {@code RenderInfo} 's {@code ri1} / {@code ri2} are re-rolled from the entity's own RNG
 * ({@code nextInt(10)} / {@code nextInt(15)} idle, {@code nextInt(4)} / {@code nextInt(3)} attacking), and
 * while {@code ri1} is 1 or 3 the eight mouth parts twitch on a 0.5 cosine x PI x 0.015 (orig :1058) and the forty-one
 * teeth on seven times that, around their -+0.3 .. 0.39 rests. The entity is read through {@link KrakenPose} (the Slice
 * 4b doctrine). Every value the classic reads back from a part it just wrote is held in a local; the six root rings'
 * pivots, never written, are read through {@link #classicPosition} (the bind).
 * <p>THE WHOLE-MODEL RENDER TRANSFORM (TEST-013): {@code ModelKraken.renderToBuffer} draws every part under {@code
 * mulPose(Axis.XP.rotationDegrees(90))} (ModelKraken.java:733, orig :1137), which no bone of the converted rig can pose;
 * {@code DESCRIPTOR.renderTransform()} declares it in the classic's terms and the seam applies its slot form. Scale and
 * shadow follow {@link KrakenRenderer} : 1.0, a third of it while {@code getPlayNicely() != 0} , a 1.0 x 1.0 shadow
 * (ENT-S-092). No zero-thickness cube.</p>
 */
public final class KrakenGeoReplacement extends OreSpawnGeoReplacement<Kraken> {
    /** The port's {@code ModelKraken.ANIM_SPEED} = 1.0f (orig ClientProxyOreSpawn.java:444 {@code new ModelKraken(1.0f)}). */
    static final float ANIM_SPEED = 1.0F;
    private static final GeoReplacementDescriptor<Kraken> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.KRAKEN.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Kraken.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/kraken.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/kraken.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/kraken.png"),
            KrakenRenderer.SHADOW) {
        @Override
        public void applyScale(Kraken entity, PoseStack poseStack, float partialTick) {
            // orig RenderKraken.preRenderScale (:39-45): PlayNicely gets glScalef(scale / 3), otherwise glScalef(scale)
            // (KrakenRenderer.scale)
            float effectiveScale = entity.getPlayNicely() != 0 ? KrakenRenderer.SCALE / 3.0F : KrakenRenderer.SCALE;
            poseStack.scale(effectiveScale, effectiveScale, effectiveScale);
        }

        @Override
        public RenderTransform renderTransform() { return RenderTransform.rotationDegrees(90.0F, 0.0F, 0.0F); }  // renderToBuffer:733
    };

    public KrakenGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        KrakenPose entity = inputs.subject(KrakenPose.class);
        float ageInTicks = inputs.ageInTicks();
        // ModelKraken.poseFrom verbatim.
        rotateZ(processor, "Finright", Mth.cos(ageInTicks * 0.43F * ANIM_SPEED) * (float) Math.PI * 0.15F);
        rotateZ(processor, "Finleft", Mth.cos(ageInTicks * 0.32F * ANIM_SPEED) * (float) Math.PI * 0.14F);

        int attacking = entity.getAttacking();

        float[] tent58 = dangleTentacle(processor, ageInTicks, 5, attacking);
        float[] tent68 = dangleTentacle(processor, ageInTicks, 6, attacking);

        float sucktioncupleftY = tent58[Y] + (float) Math.sin(tent58[X_ROT]) * 30.0F * (float) Math.cos(tent58[Y_ROT]);
        float sucktioncupleftZ = tent58[Z] - (float) Math.cos(tent58[X_ROT]) * 30.0F * (float) Math.cos(tent58[Y_ROT]);
        float sucktioncupleftX = tent58[X] - (float) Math.sin(tent58[Y_ROT]) * 30.0F * (float) Math.cos(tent58[X_ROT]);
        moveTo(processor, "Sucktioncupleft", sucktioncupleftX, sucktioncupleftY, sucktioncupleftZ);
        rotateX(processor, "Sucktioncupleft", tent58[X_ROT]);
        rotateY(processor, "Sucktioncupleft", tent58[Y_ROT]);

        float sucktioncuprightY = tent68[Y] + (float) Math.sin(tent68[X_ROT]) * 30.0F * (float) Math.cos(tent68[Y_ROT]);
        float sucktioncuprightZ = tent68[Z] - (float) Math.cos(tent68[X_ROT]) * 30.0F * (float) Math.cos(tent68[Y_ROT]);
        float sucktioncuprightX = tent68[X] - (float) Math.sin(tent68[Y_ROT]) * 30.0F * (float) Math.cos(tent68[X_ROT]);
        moveTo(processor, "Sucktioncupright", sucktioncuprightX, sucktioncuprightY, sucktioncuprightZ);
        rotateX(processor, "Sucktioncupright", tent68[X_ROT]);
        rotateY(processor, "Sucktioncupright", tent68[Y_ROT]);

        // Per-entity scratch as in the original (orig Kraken.java:58, orig ModelKraken.java:1045-1057): each Kraken
        // keeps its own twitch state instead of sharing one field on the model singleton.
        RenderInfo r = entity.getRenderInfo();
        float newangle = Mth.cos(ageInTicks * 0.66F) * (float) Math.PI * 0.15F;
        float nextangle = Mth.cos((ageInTicks + 0.1F) * 0.66F) * (float) Math.PI * 0.15F;
        if (nextangle > 0.0F && newangle < 0.0F) {
            // orig ModelKraken.java:1048-1057 - re-roll at the cosine zero crossing;
            // idle nextInt(10)/nextInt(15), attacking nextInt(4)/nextInt(3).
            if (attacking == 0) {
                r.ri1 = entity.getRandom().nextInt(10);
                r.ri2 = entity.getRandom().nextInt(15);
            } else {
                r.ri1 = entity.getRandom().nextInt(4);
                r.ri2 = entity.getRandom().nextInt(3);
            }
        }

        // orig ModelKraken.java:1058 - twitch only while ri1 is 1 or 3.
        newangle = (r.ri1 == 1 || r.ri1 == 3)
                ? Mth.cos(ageInTicks * 0.5F * ANIM_SPEED) * (float) Math.PI * 0.015F
                : 0.0F;

        rotateX(processor, "Mouth1", -0.38F + newangle);
        rotateY(processor, "Mouth1", -0.38F + newangle);
        rotateX(processor, "Mouth2", -0.38F + newangle);
        rotateY(processor, "Mouth2", 0.38F - newangle);
        rotateX(processor, "Mouth3", 0.38F - newangle);
        rotateY(processor, "Mouth3", 0.38F - newangle);
        rotateX(processor, "Mouth5", 0.38F - newangle);
        rotateY(processor, "Mouth5", -0.38F + newangle);
        rotateX(processor, "Mouth4", 0.38F - newangle);
        rotateY(processor, "Mouth6", -0.38F + newangle);
        rotateY(processor, "Mouth7", 0.38F - newangle);
        rotateX(processor, "Mouth8", -0.38F + newangle);

        newangle *= 7.0F;

        rotateX(processor, "Tooth2", -0.35F - newangle);
        rotateX(processor, "Tooth3", -0.34F - newangle);
        rotateX(processor, "Tooth4", -0.33F - newangle);
        rotateX(processor, "Tooth5", -0.36F - newangle);
        rotateX(processor, "Tooth6", -0.32F - newangle);
        rotateY(processor, "Tooth11", 0.35F + newangle);
        rotateY(processor, "Tooth12", 0.37F + newangle);
        rotateY(processor, "Tooth13", 0.33F + newangle);
        rotateY(processor, "Tooth14", 0.34F + newangle);
        rotateY(processor, "Tooth15", 0.36F + newangle);
        rotateY(processor, "Tooth16", 0.35F + newangle);
        rotateY(processor, "Tooth17", 0.32F + newangle);
        rotateX(processor, "Tooth22", 0.31F + newangle);
        rotateX(processor, "Tooth23", 0.37F + newangle);
        rotateX(processor, "Tooth24", 0.33F + newangle);
        rotateX(processor, "Tooth25", 0.34F + newangle);
        rotateX(processor, "Tooth26", 0.36F + newangle);
        rotateX(processor, "Tooth27", 0.35F + newangle);
        rotateY(processor, "Tooth32", -0.37F - newangle);
        rotateY(processor, "Tooth33", -0.33F - newangle);
        rotateY(processor, "Tooth34", -0.34F - newangle);
        rotateY(processor, "Tooth35", -0.36F - newangle);
        rotateY(processor, "Tooth36", -0.35F - newangle);
        rotateY(processor, "Tooth37", -0.32F - newangle);
        rotateX(processor, "Tooth7", -0.35F - newangle);
        rotateY(processor, "Tooth7", 0.33F + newangle);
        rotateX(processor, "Tooth8", -0.31F - newangle);
        rotateY(processor, "Tooth8", 0.37F + newangle);
        rotateX(processor, "Tooth9", -0.32F - newangle);
        rotateY(processor, "Tooth9", 0.3F + newangle);
        rotateX(processor, "Tooth10", -0.33F - newangle);
        rotateY(processor, "Tooth10", 0.33F + newangle);
        rotateX(processor, "Tooth18", 0.35F + newangle);
        rotateY(processor, "Tooth18", 0.33F + newangle);
        rotateX(processor, "Tooth19", 0.31F + newangle);
        rotateY(processor, "Tooth19", 0.37F + newangle);
        rotateX(processor, "Tooth20", 0.37F + newangle);
        rotateY(processor, "Tooth20", 0.37F + newangle);
        rotateX(processor, "Tooth21", 0.3F + newangle);
        rotateY(processor, "Tooth21", 0.3F + newangle);
        rotateX(processor, "Tooth28", 0.37F + newangle);
        rotateY(processor, "Tooth28", -0.3F - newangle);
        rotateX(processor, "Tooth29", 0.33F + newangle);
        rotateY(processor, "Tooth29", -0.32F - newangle);
        rotateX(processor, "Tooth30", 0.3F + newangle);
        rotateY(processor, "Tooth30", -0.37F - newangle);
        rotateX(processor, "Tooth31", 0.37F + newangle);
        rotateY(processor, "Tooth31", -0.3F - newangle);
        rotateX(processor, "Tooth38", -0.34F - newangle);
        rotateY(processor, "Tooth38", -0.33F - newangle);
        rotateX(processor, "Tooth39", -0.35F - newangle);
        rotateY(processor, "Tooth39", -0.37F - newangle);
        rotateX(processor, "Tooth40", -0.39F - newangle);
        rotateY(processor, "Tooth40", -0.33F - newangle);
        rotateX(processor, "Tooth41", -0.34F - newangle);
        rotateY(processor, "Tooth41", -0.36F - newangle);
        rotateX(processor, "Tooth1", -0.35F - newangle);
        rotateY(processor, "Tooth1", -0.32F - newangle);

        dangleTentacle(processor, ageInTicks, 1, 0);
        dangleTentacle(processor, ageInTicks, 2, 0);
        dangleTentacle(processor, ageInTicks, 3, 0);
        dangleTentacle(processor, ageInTicks, 4, 0);
    }

    /** Indices into the last ring's state {@link #dangleTentacle} returns: the classic reads {@code p8.x/y/z/xRot/yRot} back. */
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int X_ROT = 3;
    private static final int Y_ROT = 4;

    /**
     * ModelKraken.dangleTentacle verbatim on tentacle {@code dir}'s rings {@code Tent<dir>1..8}: the root ring's pivot is
     * never written (the bind), each following ring's is the classic's chain; returns the eighth ring's
     * {@code x, y, z, xRot, yRot} for the suction cups.
     */
    private static float[] dangleTentacle(AnimationProcessor<?> processor, float ageInTicks, int dir, int att) {
        String p1 = "Tent" + dir + "1";
        String p2 = "Tent" + dir + "2";
        String p3 = "Tent" + dir + "3";
        String p4 = "Tent" + dir + "4";
        String p5 = "Tent" + dir + "5";
        String p6 = "Tent" + dir + "6";
        String p7 = "Tent" + dir + "7";
        String p8 = "Tent" + dir + "8";
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

        float p1XRot = xoff + s * Mth.cos(ageInTicks * differ * ANIM_SPEED) * (float) Math.PI * amp;
        float p1YRot = yoff - Mth.cos(ageInTicks * ydiffer * ANIM_SPEED) * (float) Math.PI * amp;
        rotateX(processor, p1, p1XRot);
        rotateY(processor, p1, p1YRot);
        float[] p1Pos = classicPosition(bone(processor, p1));  // never written: the bind pivot

        float p2Y = p1Pos[1] + (float) Math.sin(p1XRot) * dist * (float) Math.cos(p1YRot);
        float p2Z = p1Pos[2] - (float) Math.cos(p1XRot) * dist * (float) Math.cos(p1YRot);
        float p2X = p1Pos[0] - (float) Math.sin(p1YRot) * dist * (float) Math.cos(p1XRot);
        moveTo(processor, p2, p2X, p2Y, p2Z);
        float p2XRot = xoff / 2.0F + s * Mth.cos(ageInTicks * differ * ANIM_SPEED - pi4) * (float) Math.PI * amp;
        float p2YRot = yoff / 2.0F - Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - pi4) * (float) Math.PI * amp;
        rotateX(processor, p2, p2XRot);
        rotateY(processor, p2, p2YRot);

        float p3Y = p2Y + (float) Math.sin(p2XRot) * dist * (float) Math.cos(p2YRot);
        float p3Z = p2Z - (float) Math.cos(p2XRot) * dist * (float) Math.cos(p2YRot);
        float p3X = p2X - (float) Math.sin(p2YRot) * dist * (float) Math.cos(p2XRot);
        moveTo(processor, p3, p3X, p3Y, p3Z);
        float p3XRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 2.0F * pi4) * (float) Math.PI * amp;
        float p3YRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 2.0F * pi4) * (float) Math.PI * amp;
        rotateX(processor, p3, p3XRot);
        rotateY(processor, p3, p3YRot);

        float p4Y = p3Y + (float) Math.sin(p3XRot) * dist * (float) Math.cos(p3YRot);
        float p4Z = p3Z - (float) Math.cos(p3XRot) * dist * (float) Math.cos(p3YRot);
        float p4X = p3X - (float) Math.sin(p3YRot) * dist * (float) Math.cos(p3XRot);
        moveTo(processor, p4, p4X, p4Y, p4Z);
        float p4XRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 3.0F * pi4) * (float) Math.PI * amp;
        float p4YRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 3.0F * pi4) * (float) Math.PI * amp;
        rotateX(processor, p4, p4XRot);
        rotateY(processor, p4, p4YRot);

        float p5Y = p4Y + (float) Math.sin(p4XRot) * dist * (float) Math.cos(p4YRot);
        float p5Z = p4Z - (float) Math.cos(p4XRot) * dist * (float) Math.cos(p4YRot);
        float p5X = p4X - (float) Math.sin(p4YRot) * dist * (float) Math.cos(p4XRot);
        moveTo(processor, p5, p5X, p5Y, p5Z);
        float p5XRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 4.0F * pi4) * (float) Math.PI * amp;
        float p5YRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 4.0F * pi4) * (float) Math.PI * amp;
        rotateX(processor, p5, p5XRot);
        rotateY(processor, p5, p5YRot);

        float p6Y = p5Y + (float) Math.sin(p5XRot) * dist * (float) Math.cos(p5YRot);
        float p6Z = p5Z - (float) Math.cos(p5XRot) * dist * (float) Math.cos(p5YRot);
        float p6X = p5X - (float) Math.sin(p5YRot) * dist * (float) Math.cos(p5XRot);
        moveTo(processor, p6, p6X, p6Y, p6Z);
        float p6XRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 5.0F * pi4) * (float) Math.PI * amp;
        float p6YRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 5.0F * pi4) * (float) Math.PI * amp;
        rotateX(processor, p6, p6XRot);
        rotateY(processor, p6, p6YRot);

        float p7Y = p6Y + (float) Math.sin(p6XRot) * dist * (float) Math.cos(p6YRot);
        float p7Z = p6Z - (float) Math.cos(p6XRot) * dist * (float) Math.cos(p6YRot);
        float p7X = p6X - (float) Math.sin(p6YRot) * dist * (float) Math.cos(p6XRot);
        moveTo(processor, p7, p7X, p7Y, p7Z);
        float p7XRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 6.0F * pi4) * (float) Math.PI * amp;
        float p7YRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 6.0F * pi4) * (float) Math.PI * amp;
        rotateX(processor, p7, p7XRot);
        rotateY(processor, p7, p7YRot);

        float p8Y = p7Y + (float) Math.sin(p7XRot) * dist * (float) Math.cos(p7YRot);
        float p8Z = p7Z - (float) Math.cos(p7XRot) * dist * (float) Math.cos(p7YRot);
        float p8X = p7X - (float) Math.sin(p7YRot) * dist * (float) Math.cos(p7XRot);
        moveTo(processor, p8, p8X, p8Y, p8Z);
        float p8XRot = s * Mth.cos(ageInTicks * differ * ANIM_SPEED - 7.0F * pi4) * (float) Math.PI * amp;
        float p8YRot = -Mth.cos(ageInTicks * ydiffer * ANIM_SPEED - 7.0F * pi4) * (float) Math.PI * amp;
        rotateX(processor, p8, p8XRot);
        rotateY(processor, p8, p8YRot);
        return new float[] {p8X, p8Y, p8Z, p8XRot, p8YRot};
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Kraken, KrakenGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new KrakenGeoReplacement());
        }

        /** The classic {@link KrakenRenderer#shouldRender} (OPT-013): unconditionally drawn, never frustum-culled by its hitbox (T1a). */
        @Override
        public boolean shouldRender(Kraken entity, net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
            return true;
        }
    }
}
