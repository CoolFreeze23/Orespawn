package danger.orespawn.entity.client;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Dragon;
import danger.orespawn.entity.pose.DragonPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationProcessor;

/**
 * GeckoLib Dragon (the hooks, owner 2026-09-14, addendum item 10; Tier 1): {@link ModelDragon#poseFrom} verbatim on the
 * converted rig, ON THE HOOK (Amendment 2 to Amendment 1: no keyframe layer, no transcription - the self-gate stays closed
 * until an artist delivers {@code idle} and {@code walk}). ANIM_SPEED 1.0f (ModelDragon.java:20 - the port's classic
 * frequency, where 1.7.10 registered the model with wingspeed 0.65f, orig ModelDragon.java:15,73 / ClientProxyOreSpawn.java:
 * 447; the port's model is what this transcribes). The idioms, all read through {@link DragonPose}: the walk amplitude
 * from the MOVEMENT DELTA - above a walking speed of a thousandth {@code cos(age * 1.25f) * PI * lspeed * 0.6f} with
 * {@code lspeed} the horizontal distance moved this tick (orig :418-424; no clamp); the ACTIVITY branch on the fourteen leg
 * parts about X (flying: a constant 1.0 tuck; else the gait, the pairs opposed; orig :425-456); the ATTACKING x ACTIVITY
 * ternaries on the wings' Z (0.75 x 0.28 flying; -0.45 + 0.85 x 0.2 attacking on the ground; -0.85 + 0.2 x 0.028 at rest;
 * orig :457) with the POSITION-write idiom (through {@link #moveTo}) - the three wing segments a side FOLLOW each other 7 and
 * 6 units along (cos, sin) of the previous segment's roll, at 4/3 and 3/2 of the angle, the inner membranes copying the
 * segment pivots (orig :458-491); the tail rhythm by the flags (0.76 / 0.45; 0.96 / 0.75 attacking; 0.22 / 0.22 idle; 0 / 0
 * sitting; orig :492-502) and the tail chain's pivots each 6 units along (sin, cos) of the previous yaw with the classic's x
 * nudges, the spikes copying their segments (orig :503-537); the RIDDEN-FLIGHT NECK LATCH (orig :538-551, the Rotator's
 * precedent): while activity is 1 the head yaw is the negated body-yaw delta x 8 eased into the per-entity {@link RenderInfo}'s
 * {@code rf1} by a sixtieth and clamped to +-50, else {@code netHeadYaw / 2}; the HEAD-LOOK idiom about Y at 0.25 / 0.5 /
 * 0.75 of the radians on the neck and head, each pivot 6 units back, the mouth / horns copying the head (the horns +-0.26
 * rad), the lower jaw 9 units on (orig :552-574); and the ATTACKING branch on the jaw's pitch (orig :575). Wing1's, wing4's,
 * tail1's and neck2's pivots are never written (the bind), read through {@link #classicPosition}; every value the classic
 * reads back from a part it just wrote is held in a local; a part whose x / y (wings) or x / z (tail, neck) the classic
 * writes keeps its bind third axis. Orig :576's {@code e.setRenderInfo(r)} is the port's omitted self-copy (ENT-S-093).
 *
 * <p>One rig, two registries (orig ClientProxyOreSpawn.java:447 {@code new RenderDragon(new ModelDragon(0.65f), 1.25f,
 * 1.0f)}; the port's Baby Dragon is a distinct entity drawn by {@link BabyDragonRenderer} with the same model):
 * {@link BabyDragonGeoReplacement} shares this class's geo, clip file and hook ({@link #poseDragon}; named apart from the base's final harness entry {@code pose}) under its own descriptor -
 * one profile per registry path (design Q9; the Ant precedent) - with its own texture rule, shadow and scale.</p>
 *
 * <p>Shadow follows {@link DragonRenderer}: a 1.25 x 1.0 shadow (ENT-S-092); the classic renderer scales by 1.0 (its
 * private SCALE), so no scale hook; the texture is the dragon type's sheet as {@link DragonRenderer#getTextureLocation}
 * resolves it (type 0 {@code dragon.png}, any other {@code white_dragon.png} - the renderer's two constants are private, so
 * the equal literals here). The rig has a zero-thickness cube (tail5, a fin), so the shipped geo carries the classic
 * within-cube face order ({@link FaceOrder#KEY}; TEST-007) and the seam expects it.</p>
 */
public final class DragonGeoReplacement extends OreSpawnGeoReplacement<Dragon> {
    /** ModelDragon.ANIM_SPEED = 1.0f: the port's classic frequency (1.7.10's wingspeed was 0.65f, ClientProxyOreSpawn.java:447). */
    static final float ANIM_SPEED = 1.0F;
    /** DragonRenderer.TEXTURE (private): orig RenderDragon.getEntityTexture, the type-0 sheet. */
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/dragon.png");
    /** DragonRenderer.TEXTURE_WHITE (private): any other dragon type. */
    static final ResourceLocation TEXTURE_WHITE = ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "textures/entity/white_dragon.png");
    private static final GeoReplacementDescriptor<Dragon> DESCRIPTOR = new GeoReplacementDescriptor<>(
            () -> ModEntities.DRAGON.get(),  // lambda: a bound method ref would initialise ModEntities eagerly
            Dragon.class,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "geo/entity/dragon.geo.json"),
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "animations/entity/dragon.animation.json"),
            TEXTURE,
            DragonRenderer.SHADOW) {
        @Override
        public ResourceLocation texture(Dragon entity) {
            return textureFor(entity.getDragonType());
        }

        /** One zero-thickness cube (tail5): the seam expects {@link FaceOrder#KEY} in the shipped geo. */
        @Override
        public boolean cubeFaceOrderRequired() {
            return true;
        }
    };

    public DragonGeoReplacement() {
        super(DESCRIPTOR);
    }

    /** DragonRenderer.getTextureLocation / BabyDragonRenderer.getTextureLocation: type 0 the dragon sheet, any other the white one. */
    static ResourceLocation textureFor(int dragonType) {
        if (dragonType != 0) {
            return TEXTURE_WHITE;
        }
        return TEXTURE;
    }

    @Override
    protected void applyCustomAnimations(AnimationProcessor<?> processor, PoseInputs inputs) {
        poseDragon(processor, inputs);
    }

    /** ModelDragon.poseFrom (the body of the classic setupAnim) verbatim, the float chain left to right; shared by the two consumers of the rig (the Ant's {@code pose} form, named apart from {@link #pose(AnimationProcessor, PoseInputs)}, the base's final harness entry). */
    static void poseDragon(AnimationProcessor<?> processor, PoseInputs inputs) {
        DragonPose entity = inputs.subject(DragonPose.class);
        float limbSwingAmount = inputs.limbSwingAmount();
        float ageInTicks = inputs.ageInTicks();
        float netHeadYaw = inputs.netHeadYaw();
        float newangle;
        float lspeed = 0.0F;
        float tailspeed = 0.76F;
        float tailamp = 0.45F;

        if (limbSwingAmount > 0.001F) {
            lspeed = (float) ((entity.xOld() - entity.getX()) * (entity.xOld() - entity.getX())
                    + (entity.zOld() - entity.getZ()) * (entity.zOld() - entity.getZ()));
            lspeed = (float) Math.sqrt(lspeed);
            newangle = Mth.cos(ageInTicks * 1.25F * ANIM_SPEED) * (float) Math.PI * lspeed * 0.6F;
        } else {
            newangle = 0.0F;
        }

        // Leg animation
        if (entity.getActivity() != 0) {
            float a = 1.0F;
            rotateX(processor, "leg4", 0.557F - a);
            rotateX(processor, "leg5", -0.557F - a);
            rotateX(processor, "foot2", -a);
            rotateX(processor, "leg3", 0.557F - a);
            rotateX(processor, "leg6", -0.557F - a);
            rotateX(processor, "foot1", -a);
            rotateX(processor, "leg2", -0.632F + a);
            rotateX(processor, "leg7", 0.89F + a);
            rotateX(processor, "leg10", -0.557F + a);
            rotateX(processor, "foot3", a);
            rotateX(processor, "leg1", -0.632F + a);
            rotateX(processor, "leg9", 0.89F + a);
            rotateX(processor, "leg11", -0.557F + a);
            rotateX(processor, "foot4", a);
        } else {
            rotateX(processor, "leg4", 0.557F + newangle);
            rotateX(processor, "leg5", -0.557F + newangle);
            rotateX(processor, "foot2", newangle);
            rotateX(processor, "leg3", 0.557F - newangle);
            rotateX(processor, "leg6", -0.557F - newangle);
            rotateX(processor, "foot1", -newangle);
            rotateX(processor, "leg2", -0.632F - newangle);
            rotateX(processor, "leg7", 0.89F - newangle);
            rotateX(processor, "leg10", -0.557F - newangle);
            rotateX(processor, "foot3", -newangle);
            rotateX(processor, "leg1", -0.632F + newangle);
            rotateX(processor, "leg9", 0.89F + newangle);
            rotateX(processor, "leg11", -0.557F + newangle);
            rotateX(processor, "foot4", newangle);
        }

        // Wing animation
        if (entity.getAttacking() != 0) {
            newangle = entity.getActivity() != 0
                    ? Mth.cos(ageInTicks * 0.75F * ANIM_SPEED) * (float) Math.PI * 0.28F
                    : -0.45F + Mth.cos(ageInTicks * 0.85F * ANIM_SPEED) * (float) Math.PI * 0.2F;
        } else {
            newangle = entity.getActivity() != 0
                    ? Mth.cos(ageInTicks * 0.75F * ANIM_SPEED) * (float) Math.PI * 0.28F
                    : -0.85F + Mth.cos(ageInTicks * 0.2F * ANIM_SPEED) * (float) Math.PI * 0.028F;
        }

        float wing1ZRot = newangle;
        rotateZ(processor, "wing1", wing1ZRot);
        rotateZ(processor, "wing15", newangle);
        float wing3ZRot = newangle * 4.0F / 3.0F;
        rotateZ(processor, "wing3", wing3ZRot);
        float[] wing1 = classicPosition(bone(processor, "wing1"));  // never written: the bind pivot
        float wing3Y = wing1[1] + (float) Math.sin(wing1ZRot) * 7.0F;
        float wing3X = wing1[0] + (float) Math.cos(wing1ZRot) * 7.0F;
        moveXY(processor, "wing3", wing3X, wing3Y);
        rotateZ(processor, "wing8", newangle * 4.0F / 3.0F);
        moveXY(processor, "wing8", wing3X, wing3Y);
        float wing2ZRot = newangle * 3.0F / 2.0F;
        rotateZ(processor, "wing2", wing2ZRot);
        float wing2Y = wing3Y + (float) Math.sin(wing3ZRot) * 6.0F;
        float wing2X = wing3X + (float) Math.cos(wing3ZRot) * 6.0F;
        moveXY(processor, "wing2", wing2X, wing2Y);
        rotateZ(processor, "wing7", newangle * 3.0F / 2.0F);
        moveXY(processor, "wing7", wing2X, wing2Y);
        rotateZ(processor, "wing9", newangle * 3.0F / 2.0F);
        moveXY(processor, "wing9", wing2X, wing2Y);

        float wing4ZRot = -newangle;
        rotateZ(processor, "wing4", wing4ZRot);
        rotateZ(processor, "wing14", -newangle);
        float wing5ZRot = -newangle * 4.0F / 3.0F;
        rotateZ(processor, "wing5", wing5ZRot);
        float[] wing4 = classicPosition(bone(processor, "wing4"));  // never written: the bind pivot
        float wing5Y = wing4[1] - (float) Math.sin(wing4ZRot) * 7.0F;
        float wing5X = wing4[0] - (float) Math.cos(wing4ZRot) * 7.0F;
        moveXY(processor, "wing5", wing5X, wing5Y);
        rotateZ(processor, "wing10", -newangle * 4.0F / 3.0F);
        moveXY(processor, "wing10", wing5X, wing5Y);
        float wing6ZRot = -newangle * 3.0F / 2.0F;
        rotateZ(processor, "wing6", wing6ZRot);
        float wing6Y = wing5Y - (float) Math.sin(wing5ZRot) * 6.0F;
        float wing6X = wing5X - (float) Math.cos(wing5ZRot) * 6.0F;
        moveXY(processor, "wing6", wing6X, wing6Y);
        rotateZ(processor, "wing11", -newangle * 3.0F / 2.0F);
        moveXY(processor, "wing11", wing6X, wing6Y);
        rotateZ(processor, "wing12", -newangle * 3.0F / 2.0F);
        moveXY(processor, "wing12", wing6X, wing6Y);

        // Tail animation
        if (entity.getAttacking() != 0) {
            tailspeed = 0.96F;
            tailamp = 0.75F;
        }
        if (entity.getActivity() == 0 && entity.getAttacking() == 0) {
            tailspeed = 0.22F;
            tailamp = 0.22F;
        }
        // orig ModelDragon.java:500 e.func_70906_o() = EntityTameable.isSitting(); the port's isInSittingPose(). ENT-S-093.
        if (entity.isInSittingPose()) {
            tailspeed = 0.0F;
            tailamp = 0.0F;
        }

        float tail1YRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * 0.04F;
        rotateY(processor, "tail1", tail1YRot);
        float[] tail1 = classicPosition(bone(processor, "tail1"));  // never written: the bind pivot
        moveXZ(processor, "spike10", tail1[0], tail1[2]);
        rotateY(processor, "spike10", tail1YRot);

        float tail2Z = tail1[2] + (float) Math.cos(tail1YRot) * 6.0F;
        float tail2X = tail1[0] + (float) Math.sin(tail1YRot) * 6.0F;
        moveXZ(processor, "tail2", tail2X, tail2Z);
        float tail2YRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.125F;
        rotateY(processor, "tail2", tail2YRot);
        moveXZ(processor, "spike7", tail2X, tail2Z);
        rotateY(processor, "spike7", tail2YRot);

        float neck1Z = tail2Z + (float) Math.cos(tail2YRot) * 6.0F;
        float neck1X = tail2X + (float) Math.sin(tail2YRot) * 6.0F;
        moveXZ(processor, "neck1", neck1X, neck1Z);
        float neck1YRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.25F;
        rotateY(processor, "neck1", neck1YRot);
        moveXZ(processor, "spike8", neck1X, neck1Z);
        rotateY(processor, "spike8", neck1YRot);

        float tail3Z = neck1Z + (float) Math.cos(neck1YRot) * 6.0F;
        float tail3X = neck1X + 1.0F + (float) Math.sin(neck1YRot) * 6.0F;
        moveXZ(processor, "tail3", tail3X, tail3Z);
        float tail3YRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.375F;
        rotateY(processor, "tail3", tail3YRot);
        moveXZ(processor, "spike3", tail3X - 1.0F, tail3Z);
        rotateY(processor, "spike3", tail3YRot);

        float tail4Z = tail3Z + (float) Math.cos(tail3YRot) * 6.0F;
        float tail4X = tail3X + (float) Math.sin(tail3YRot) * 6.0F;
        moveXZ(processor, "tail4", tail4X, tail4Z);
        float tail4YRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.5F;
        rotateY(processor, "tail4", tail4YRot);
        moveXZ(processor, "spike9", tail4X - 1.0F, tail4Z);
        rotateY(processor, "spike9", tail4YRot);

        float tail6Z = tail4Z + (float) Math.cos(tail4YRot) * 6.0F;
        float tail6X = tail4X - 1.0F + (float) Math.sin(tail4YRot) * 6.0F;
        moveXZ(processor, "tail6", tail6X, tail6Z);
        float tail6YRot = Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.625F;
        rotateY(processor, "tail6", tail6YRot);

        float tail5Z = tail6Z + (float) Math.cos(tail6YRot) * 6.0F;
        float tail5X = tail6X - 0.5F + (float) Math.sin(tail6YRot) * 6.0F;
        moveXZ(processor, "tail5", tail5X, tail5Z);
        rotateY(processor, "tail5", Mth.cos(ageInTicks * tailspeed * ANIM_SPEED) * (float) Math.PI * tailamp * 0.75F);

        // Head/neck tracking - orig ModelDragon.java:538-551; latch lives on the entity (orig Dragon.java:80,
        // ModelDragon.java:417/:541-548) so each dragon keeps its own. ENT-S-093.
        RenderInfo r = entity.getRenderInfo();
        float headYaw = netHeadYaw;
        if (entity.getActivity() == 1) {
            headYaw = (entity.yRotO() - entity.getYRot()) * 8.0F;   // orig :539 (field_70126_B - field_70177_z)
            headYaw = -headYaw;                                      // orig :540
            r.rf1 += (headYaw - r.rf1) / 60.0F;                      // orig :541
            if (r.rf1 > 50.0F) {                                     // orig :542-544
                r.rf1 = 50.0F;
            }
            if (r.rf1 < -50.0F) {                                    // orig :545-547
                r.rf1 = -50.0F;
            }
            headYaw = r.rf1;                                         // orig :548
        } else {
            headYaw /= 2.0F;                                         // orig :550
        }

        float spike2YRot = (float) Math.toRadians(headYaw) * 0.25F;
        rotateY(processor, "spike2", spike2YRot);
        float neck2YRot = spike2YRot;
        rotateY(processor, "neck2", neck2YRot);

        float[] neck2 = classicPosition(bone(processor, "neck2"));  // never written: the bind pivot
        float neck3Z = neck2[2] - (float) Math.cos(neck2YRot) * 6.0F;
        float neck3X = neck2[0] - (float) Math.sin(neck2YRot) * 6.0F;
        moveXZ(processor, "neck3", neck3X, neck3Z);
        float neck3YRot = (float) Math.toRadians(headYaw) * 0.5F;
        rotateY(processor, "neck3", neck3YRot);
        moveXZ(processor, "spike1", neck3X, neck3Z);
        rotateY(processor, "spike1", neck3YRot);

        float headZ = neck3Z - (float) Math.cos(neck3YRot) * 6.0F;
        float headX = neck3X - (float) Math.sin(neck3YRot) * 6.0F;
        moveXZ(processor, "head", headX, headZ);
        float headYRot = (float) Math.toRadians(headYaw) * 0.75F;
        rotateY(processor, "head", headYRot);
        moveXZ(processor, "mouth1", headX, headZ);
        rotateY(processor, "mouth1", headYRot);
        moveXZ(processor, "horn1", headX, headZ);
        rotateY(processor, "horn1", headYRot + 0.26F);
        moveXZ(processor, "horn2", headX, headZ);
        rotateY(processor, "horn2", headYRot - 0.26F);
        float mouth2Z = headZ - (float) Math.cos(headYRot) * 9.0F;
        float mouth2X = headX - (float) Math.sin(headYRot) * 9.0F;
        moveXZ(processor, "mouth2", mouth2X, mouth2Z);
        rotateY(processor, "mouth2", headYRot);

        newangle = Mth.cos(ageInTicks * 1.5F * ANIM_SPEED) * (float) Math.PI * 0.14F;
        rotateX(processor, "mouth2", entity.getAttacking() != 0 ? 0.4F + newangle : 0.07F);
    }

    /** {@code part.x = x; part.y = y} in classic terms: the classic writes x and y only (the wings), so z keeps the part's bind. */
    private static void moveXY(AnimationProcessor<?> processor, String name, float x, float y) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, y, current[2]);
    }

    /** {@code part.x = x; part.z = z} in classic terms: the classic writes x and z only (the tail and neck), so y keeps the part's bind. */
    private static void moveXZ(AnimationProcessor<?> processor, String name, float x, float z) {
        float[] current = classicPosition(bone(processor, name));
        moveTo(processor, name, x, current[1], z);
    }

    public static final class Renderer extends OreSpawnGeoReplacedEntityRenderer<Dragon, DragonGeoReplacement> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context, new DragonGeoReplacement());
        }
    }
}
