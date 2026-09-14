package danger.orespawn.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Nastysaurus;
import danger.orespawn.entity.pose.NastysaurusPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Nastysaurus (the hooks, owner 2026-09-14, addendum item 10; landed by the sixth Tier-2 slice T2f,
 * 2026-09-15, the owner's closing set item 4): {@link ModelNastysaurus#poseFrom} verbatim on the converted rig, ON THE
 * HOOK (no keyframe layer, no transcription - the self-gate stays closed until an artist delivers {@code idle} and
 * {@code walk} ; the geo, the wiring and the proofs landed with T2f). Wingspeed 0.65f (orig ModelNastysaurus.java:15,77
 * / ClientProxyOreSpawn.java:508): the HEAD-LOOK idiom (yaw {@code netHeadYaw % 360 x 0.35} in radians on the head, the
 * third neck ring, the jaws and the twenty-three teeth, half of it on the second ring); the ATTACKING branch ({@code
 * getAttacking() != 0}: the jaw and its teeth on a 0.85 ws cosine x PI x 0.16 + 0.5) over the CHEW LATCH (the Robot2
 * precedent; orig ModelNastysaurus.java:438,450): the phase of the 0.7 ws idle rhythm wraps against the per-entity
 * {@code RenderInfo.rf1} and on each wrap {@code ri1} is re-rolled from the level's RNG ({@code nextInt(20) == 1}), a
 * set bit chewing on the 0.85 ws sine + 0.5, a clear one holding 0.196 rad; the THRESHOLD idiom on each leg ({@code
 * (double) limbSwingAmount > 0.001} selects the {@code age x ws / 2} cosine / sine, else 0), the right leg a half turn
 * behind: the seven claws and the lower leg lifted {@code sin x 4 x amount} while the sine is positive and swept
 * {@code 15 + 10 x cos x amount} in z (POSITION writes through {@link #moveTo} ), the thigh and the upper leg FOLLOWING
 * the lower leg by 17 units along its pitch, the fourteen claw pitches reset to 0; and the tail's yaw at 0.76 / 0.25
 * attacking over 0.26 / 0.08 (tail3 at half, tail4 FOLLOWING it by 11 units). The entity is read through
 * {@link NastysaurusPose} (the Slice 4b doctrine). Every value the classic reads back from a part it just wrote is held
 * in a local; the coordinates the classic never writes (the claws' and legs' x, tail3's pivot) are read through
 * {@link #classicPosition} (the bind).
 * <p>Scale and shadow follow {@link NastysaurusRenderer} : 1.5 render scale ({@link NastysaurusRenderer#SCALE}, applied
 * around {@code super.render} - made public by the landing slice T2f, the hook lanes' equal literal dropped; the T2d
 * form) and the {@code 1.0f * 1.5f} shadow literal its constructor passes (no SHADOW constant). No zero-thickness
 * cube.</p>
 */
public final class NastysaurusGeoReplacement extends OreSpawnGeoReplacement<Nastysaurus> {
    /** orig ModelNastysaurus.java:15,77 {@code wingspeed} = 0.65f (ClientProxyOreSpawn.java:508): the chain's frequency multiplier. */
    static final float WINGSPEED = 0.65f;
    private static final GeoReplacementDescriptor<Nastysaurus> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.NASTYSAURUS.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Nastysaurus.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/nastysaurus.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/nastysaurus.animation.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/nastysaurus.png"),
            // NastysaurusRenderer's constructor passes the literal 1.0f * 1.5f (no SHADOW constant): the equal literal
            1.0F * 1.5F) {
        @Override
        public void applyScale(Nastysaurus entity, PoseStack poseStack, float partialTick) {
            // NastysaurusRenderer.render: poseStack.scale(SCALE, SCALE, SCALE) around super.render (the renderer's constant,
            // public since T2f: the T2d form)
            poseStack.scale(NastysaurusRenderer.SCALE, NastysaurusRenderer.SCALE, NastysaurusRenderer.SCALE);
        }
    };

    private static final String[] L_CLAWS = {"lclaw6", "lclaw7", "lclaw1", "lclaw5", "lclaw4", "lclaw3", "lclaw2"};
    private static final String[] R_CLAWS = {"rclaw6", "rclaw7", "rclaw1", "rclaw5", "rclaw4", "rclaw3", "rclaw2"};

    public NastysaurusGeoReplacement() {
        super(DESCRIPTOR);
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        NastysaurusPose entity = inputs.subject(NastysaurusPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        // ModelNastysaurus.poseFrom verbatim.
        float newangle;
        float pscale = 2.0F;
        float tailspeed;
        float tailamp;
        float clawZ = 15.0F;
        float clawY = 21.0F;
        float clawZamp = 5.0F * pscale;
        float clawYamp = 2.0F * pscale;
        float pi4 = 0.7853982F;

        // orig ModelNastysaurus.java:438,450 - per-entity latch, not a model field (ENT-S-093)
        RenderInfo r = entity.getRenderInfo();

        float headYaw = netHeadYaw % 360.0F;
        headYaw *= 0.35F;

        rotateY(processor, "neck2", (float) Math.toRadians(headYaw) * 0.5F);
        float head3YRot = (float) Math.toRadians(headYaw);   // neck3.yRot = head7.yRot = (head3.yRot = ...)
        rotateY(processor, "head3", head3YRot);
        rotateY(processor, "head7", head3YRot);
        rotateY(processor, "neck3", head3YRot);
        rotateY(processor, "jaw5", head3YRot);               // jaw1.yRot = jaw5.yRot = head3.yRot
        rotateY(processor, "jaw1", head3YRot);
        rotateY(processor, "tooth5", head3YRot);             // tooth4.yRot = tooth5.yRot = head3.yRot
        rotateY(processor, "tooth4", head3YRot);
        rotateY(processor, "tooth3", head3YRot);
        rotateY(processor, "tooth2", head3YRot);
        rotateY(processor, "tooth1", head3YRot);
        rotateY(processor, "tooth10", head3YRot);            // tooth9.yRot = tooth10.yRot = head3.yRot
        rotateY(processor, "tooth9", head3YRot);
        rotateY(processor, "tooth8", head3YRot);
        rotateY(processor, "tooth7", head3YRot);
        rotateY(processor, "tooth6", head3YRot);
        rotateY(processor, "tooth15", head3YRot);            // tooth14.yRot = tooth15.yRot = head3.yRot
        rotateY(processor, "tooth14", head3YRot);
        rotateY(processor, "tooth13", head3YRot);
        rotateY(processor, "tooth12", head3YRot);
        rotateY(processor, "tooth11", head3YRot);
        rotateY(processor, "tooth20", head3YRot);            // tooth19.yRot = tooth20.yRot = head3.yRot
        rotateY(processor, "tooth19", head3YRot);
        rotateY(processor, "tooth18", head3YRot);
        rotateY(processor, "tooth17", head3YRot);
        rotateY(processor, "tooth16", head3YRot);
        rotateY(processor, "tooth23", head3YRot);            // tooth22.yRot = tooth23.yRot = head3.yRot
        rotateY(processor, "tooth22", head3YRot);
        rotateY(processor, "tooth21", head3YRot);

        if (entity.getAttacking() != 0) {
            newangle = Mth.cos(ageInTicks * 0.85F * WINGSPEED) * (float) Math.PI * 0.16F;
            newangle += 0.5F;
        } else {
            newangle = ageInTicks * 0.7F * WINGSPEED % ((float) Math.PI * 2);
            newangle = Math.abs(newangle);
            if (newangle < r.rf1) {
                r.ri1 = 0;
                if (entity.getLevelRandom().nextInt(20) == 1) {
                    r.ri1 |= 1;
                }
            }
            r.rf1 = newangle;
            if (r.ri1 != 0) {
                newangle = Mth.sin(ageInTicks * 0.85F * WINGSPEED) * (float) Math.PI * 0.16F;
                newangle += 0.5F;
            } else {
                newangle = pi4 / 4.0F;
            }
        }

        rotateX(processor, "jaw5", newangle);        // jaw1.xRot = jaw5.xRot = newangle
        rotateX(processor, "jaw1", newangle);
        rotateX(processor, "tooth15", newangle);     // tooth14.xRot = tooth15.xRot = newangle
        rotateX(processor, "tooth14", newangle);
        rotateX(processor, "tooth20", newangle);     // tooth19.xRot = tooth20.xRot = newangle
        rotateX(processor, "tooth19", newangle);
        rotateX(processor, "tooth18", newangle);
        rotateX(processor, "tooth17", newangle);
        rotateX(processor, "tooth16", newangle);
        rotateX(processor, "tooth23", newangle);     // tooth22.xRot = tooth23.xRot = newangle
        rotateX(processor, "tooth22", newangle);
        rotateX(processor, "tooth21", newangle);

        float t1;
        float t2;
        if ((double) limbSwingAmount > 0.001) {
            newangle = Mth.cos(ageInTicks * WINGSPEED / pscale);
            t1 = Mth.sin(ageInTicks * WINGSPEED / pscale);
        } else {
            newangle = 0.0F;
            t1 = 0.0F;
        }
        float lclaw1Y;
        if (t1 > 0.0F) {
            t2 = t1 * clawYamp * limbSwingAmount;
            lclaw1Y = clawY - t2;
        } else {
            lclaw1Y = clawY;
        }
        float lclaw1Z = clawZ + clawZamp * newangle * limbSwingAmount;
        // lclaw6.z = lclaw7.z = (lclaw1.z = ...); lclaw5..2.z = lclaw7.z; the same for y (x never written: the bind)
        for (String claw : L_CLAWS) {
            moveYZ(processor, claw, lclaw1Y, lclaw1Z);
        }
        float leftleg3Z = lclaw1Z;
        float leftleg3Y = lclaw1Y;
        moveYZ(processor, "leftleg3", leftleg3Y, leftleg3Z);
        float leftleg3XRot = -0.523F + newangle * (float) Math.PI * 0.15F * limbSwingAmount;
        rotateX(processor, "leftleg3", leftleg3XRot);
        rotateX(processor, "leftleg1", -0.576F + newangle * (float) Math.PI * 0.06F * limbSwingAmount);
        rotateX(processor, "leftleg2", 0.977F + newangle * (float) Math.PI * 0.06F * limbSwingAmount);
        float leftleg1Y = leftleg3Y - (float) Math.cos(leftleg3XRot) * 17.0F;   // leftleg1.y = leftleg2.y = ...
        float leftleg1Z = leftleg3Z - (float) Math.sin(leftleg3XRot) * 17.0F;   // leftleg1.z = leftleg2.z = ...
        moveYZ(processor, "leftleg2", leftleg1Y, leftleg1Z);
        moveYZ(processor, "leftleg1", leftleg1Y, leftleg1Z);

        if ((double) limbSwingAmount > 0.001) {
            newangle = Mth.cos(ageInTicks * WINGSPEED / pscale + pi4 * 4.0F);
            t1 = Mth.sin(ageInTicks * WINGSPEED / pscale + pi4 * 4.0F);
        } else {
            newangle = 0.0F;
            t1 = 0.0F;
        }
        float rclaw1Y;
        if (t1 > 0.0F) {
            t2 = t1 * clawYamp * limbSwingAmount;
            rclaw1Y = clawY - t2;
        } else {
            rclaw1Y = clawY;
        }
        float rclaw1Z = clawZ + clawZamp * newangle * limbSwingAmount;
        for (String claw : R_CLAWS) {
            moveYZ(processor, claw, rclaw1Y, rclaw1Z);
        }
        float rightleg3Z = rclaw1Z;
        float rightleg3Y = rclaw1Y;
        moveYZ(processor, "rightleg3", rightleg3Y, rightleg3Z);
        float rightleg3XRot = -0.523F + newangle * (float) Math.PI * 0.15F * limbSwingAmount;
        rotateX(processor, "rightleg3", rightleg3XRot);
        rotateX(processor, "rightleg1", -0.576F + newangle * (float) Math.PI * 0.06F * limbSwingAmount);
        rotateX(processor, "rightleg2", 0.977F + newangle * (float) Math.PI * 0.06F * limbSwingAmount);
        float rightleg1Y = rightleg3Y - (float) Math.cos(rightleg3XRot) * 17.0F;
        float rightleg1Z = rightleg3Z - (float) Math.sin(rightleg3XRot) * 17.0F;
        moveYZ(processor, "rightleg2", rightleg1Y, rightleg1Z);
        moveYZ(processor, "rightleg1", rightleg1Y, rightleg1Z);

        rotateX(processor, "lclaw1", 0.0F);
        rotateX(processor, "lclaw7", 0.0F);
        rotateX(processor, "lclaw6", 0.0F);
        rotateX(processor, "lclaw5", 0.0F);
        rotateX(processor, "lclaw4", 0.0F);
        rotateX(processor, "lclaw3", 0.0F);
        rotateX(processor, "lclaw2", 0.0F);
        rotateX(processor, "rclaw1", 0.0F);
        rotateX(processor, "rclaw7", 0.0F);
        rotateX(processor, "rclaw6", 0.0F);
        rotateX(processor, "rclaw5", 0.0F);
        rotateX(processor, "rclaw4", 0.0F);
        rotateX(processor, "rclaw3", 0.0F);
        rotateX(processor, "rclaw2", 0.0F);

        if (entity.getAttacking() != 0) {
            tailspeed = 0.76F;
            tailamp = 0.25F;
        } else {
            tailspeed = 0.26F;
            tailamp = 0.08F;
        }

        float tail3YRot = Mth.cos(ageInTicks * tailspeed * WINGSPEED) * (float) Math.PI * tailamp / 2.0F;
        rotateY(processor, "tail3", tail3YRot);
        float[] tail3 = classicPosition(bone(processor, "tail3"));  // never written: the bind pivot
        float tail4Z = tail3[2] + (float) Math.cos(tail3YRot) * 11.0F;
        float tail4X = tail3[0] + (float) Math.sin(tail3YRot) * 11.0F;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        rotateY(processor, "tail4", Mth.cos(ageInTicks * tailspeed * WINGSPEED) * (float) Math.PI * tailamp);
        // orig ModelNastysaurus.java:592 e.setRenderInfo(r) copied r into itself (same instance); nothing to write back.
    }

    /** {@code part.y = y; part.z = z} on a part whose x the classic never writes (the bind). */
    private static void moveYZ(AnimationProcessor<?> processor, String name, float y, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, position[0], y, z);
    }

    /** {@code part.x = x; part.z = z} on a part whose y the classic never writes (the bind). */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] position = classicPosition(bone(processor, name));
        moveTo(processor, name, x, position[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Nastysaurus, NastysaurusGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new NastysaurusGeoReplacement());
        }
    }
}
