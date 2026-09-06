package danger.orespawn.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Phase G slice (d): spawns a {@link BenchScene} as a WEDGE relative to an origin and a look yaw
 * (the commanding player's position and yaw), on the surface, in a fixed state. Common code with no
 * client import; the game tests call {@link #layout} and {@link #spawn} directly with their own
 * origin.
 *
 * <p>Layout (refuter B, 2026-09-06 -- replaces the 10x10 grid whose far corners lay 260-269 blocks
 * out, past the 256-block tracking cap and the 192-block simulation distance): rows along the look
 * axis from {@link BenchScene#baseDistance()} out at the scene's pitch {@link BenchScene#spacing()};
 * each row holds the columns at multiples of the pitch whose lateral offset stays within
 * {@code d * tan(35 degrees)} of the axis ({@value #WEDGE_HALF_ANGLE_DEGREES} degrees is half the
 * default 70-degree FOV -- the vertical one; the horizontal FOV is wider on a 16:9 screen, so the
 * wedge is conservative) and whose Euclidean x/z distance from the origin is below
 * {@value #MAX_DISTANCE_BLOCKS} blocks (inside the default 12-chunk simulation distance of 192
 * blocks, where entities tick, and the Queen's 256-block client tracking range). Rows fill in
 * order and each row fills centre-out (0, -1, +1, -2, +2 ... pitches), so one mob stands on the
 * axis at the base distance, and the fill stops at the count. {@link #capacity} is how many slots
 * the wedge holds for a scene; {@link #layout} refuses a larger count. A hundred Queens at the
 * 12-block pitch take ten rows (40 to 148 blocks out; the farthest stands in the ninth row, 136
 * blocks out and 84 aside: 159.8 blocks); their 22-block boxes overlap at that pitch -- the Queen
 * is {@code noPhysics}, and an overlapping draw is still a
 * full draw (the collector walks every bone of every Queen), which is what the scene measures.
 * Minecraft's yaw: 0 faces +z, 90 faces -x, so {@code forward = (-sin yaw, 0, cos yaw)} and the
 * player's right is {@code (-cos yaw, 0, -sin yaw)}. Scene B negates the forward vector (the same
 * wedge behind the camera).</p>
 *
 * <p>Every mob's y is the heightmap top at its column ({@code MOTION_BLOCKING_NO_LEAVES}: the first
 * air block above the ground), so it stands on the ground. A no-AI mob stays there: its
 * {@code travel} IS called every tick ({@code LivingEntity.aiStep} calls it unconditionally; only
 * {@code serverAiStep} is gated on {@code isEffectiveAi}), but the body of
 * {@code LivingEntity.travel} -- gravity and the {@code move} -- runs only when
 * {@code isControlledByLocalInstance()}, which for a mob without a player passenger is
 * {@code Mob.isEffectiveAi()} = server side AND not no-AI. So gravity is never applied to a no-AI
 * mob: it keeps the position it was placed at (floating, had it been placed in mid-air), and the
 * heightmap-top spawn is what puts it on the ground. Every mob faces the origin, is persistent,
 * carries the {@value #TAG} tag (what {@link #discardTagged} sweeps), and is no-AI when the state
 * says so.</p>
 */
public final class BenchSceneSpawner {

    /** The entity tag every benchmark mob carries. */
    public static final String TAG = "orespawn_bench";
    /** Half the default 70-degree FOV: every scene A slot lies within this angle of the look axis. */
    public static final double WEDGE_HALF_ANGLE_DEGREES = 35.0D;
    /**
     * Every slot's x/z distance from the origin stays below this: inside the default simulation
     * distance (12 chunks = 192 blocks, the entity-ticking range) and the Queen's client tracking
     * range (clientTrackingRange 16 = 256 blocks, capped by the view distance).
     */
    public static final double MAX_DISTANCE_BLOCKS = 180.0D;

    private static final double EPS = 1.0E-9D;

    private BenchSceneSpawner() {
    }

    /**
     * The wedge's slots in fill order as {@code {along, lateral}} pairs (blocks, along the look
     * axis and to the right of it), at most {@code limit} of them.
     */
    private static List<double[]> wedge(BenchScene scene, int limit) {
        List<double[]> out = new ArrayList<>();
        if (limit <= 0) {
            return out;
        }
        final double tanHalf = Math.tan(Math.toRadians(WEDGE_HALF_ANGLE_DEGREES));
        final double pitch = scene.spacing();
        for (int row = 0; ; row++) {
            final double along = scene.baseDistance() + row * pitch;
            if (along >= MAX_DISTANCE_BLOCKS) {
                return out;
            }
            final int maxColumns = (int) Math.floor(along * tanHalf / pitch + EPS);
            for (int k = 0; k <= 2 * maxColumns; k++) {
                // centre-out: 0, -1, +1, -2, +2 ...
                final int column = (k + 1) / 2 * (k % 2 == 0 ? 1 : -1);
                final double lateral = column * pitch;
                if (Math.hypot(along, lateral) >= MAX_DISTANCE_BLOCKS) {
                    continue;
                }
                out.add(new double[]{along, lateral});
                if (out.size() >= limit) {
                    return out;
                }
            }
        }
    }

    /** How many mobs the wedge holds for {@code scene} (every slot inside the FOV wedge and under {@value #MAX_DISTANCE_BLOCKS} blocks). */
    public static int capacity(BenchScene scene) {
        return wedge(scene, Integer.MAX_VALUE).size();
    }

    /**
     * The slot positions (x, 0, z) for {@code count} mobs of {@code scene}; y is resolved at spawn.
     *
     * @throws IllegalArgumentException when {@code count} exceeds {@link #capacity}
     */
    public static List<Vec3> layout(Vec3 origin, float yawDegrees, BenchScene scene, int count) {
        List<Vec3> out = new ArrayList<>(Math.max(count, 0));
        if (count <= 0) {
            return out;
        }
        List<double[]> slots = wedge(scene, count);
        if (slots.size() < count) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "scene %s holds at most %d mobs within %.0f blocks of the camera (a %.0f-degree wedge at a %.0f-block pitch from %.0f blocks out); asked for %d",
                    scene.name(), slots.size(), MAX_DISTANCE_BLOCKS, 2.0D * WEDGE_HALF_ANGLE_DEGREES, scene.spacing(), scene.baseDistance(), count));
        }
        final double yaw = Math.toRadians(yawDegrees);
        final double fx = -Math.sin(yaw);
        final double fz = Math.cos(yaw);
        final double rx = -Math.cos(yaw);
        final double rz = -Math.sin(yaw);
        final double sign = scene.behindPlayer() ? -1.0D : 1.0D;
        for (double[] slot : slots) {
            final double along = sign * slot[0];
            final double lateral = slot[1];
            Vec3 position = new Vec3(origin.x + fx * along + rx * lateral, 0.0D, origin.z + fz * along + rz * lateral);
            final double distance = Math.hypot(position.x - origin.x, position.z - origin.z);
            if (distance >= MAX_DISTANCE_BLOCKS) {
                throw new IllegalStateException("layout invariant broken: a slot lies " + distance + " blocks from the origin (limit " + MAX_DISTANCE_BLOCKS + ")");
            }
            out.add(position);
        }
        return out;
    }

    /** The farthest slot's x/z distance from the origin (what the report records as {@code coverage.max_distance_blocks}); 0 for no slots. */
    public static double maxDistanceBlocks(Vec3 origin, List<Vec3> slots) {
        double max = 0.0D;
        for (Vec3 slot : slots) {
            max = Math.max(max, Math.hypot(slot.x - origin.x, slot.z - origin.z));
        }
        return max;
    }

    /** The yaw a spawned mob faces: towards the origin. */
    public static float facingYaw(float yawDegrees, BenchScene scene) {
        return Mth.wrapDegrees(scene.behindPlayer() ? yawDegrees : yawDegrees + 180.0F);
    }

    /** Spawns the scene; returns the mobs in layout order. */
    public static List<Mob> spawn(ServerLevel level, Vec3 origin, float yawDegrees, BenchScene scene, int count,
                                  BenchState state) {
        List<Mob> mobs = new ArrayList<>(Math.max(count, 0));
        float facing = facingYaw(yawDegrees, scene);
        for (Vec3 slot : layout(origin, yawDegrees, scene, count)) {
            Mob mob = scene.type().create(level);
            if (mob == null) {
                continue;
            }
            int floorX = Mth.floor(slot.x);
            int floorZ = Mth.floor(slot.z);
            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, floorX, floorZ);
            mob.moveTo(slot.x, y, slot.z, facing, 0.0F);
            mob.setYBodyRot(facing);
            mob.setYHeadRot(facing);
            mob.yRotO = facing;
            mob.yBodyRotO = facing;
            mob.yHeadRotO = facing;
            mob.setNoAi(state.noAi());
            mob.setPersistenceRequired();
            mob.addTag(TAG);
            if (level.addFreshEntity(mob)) {
                mobs.add(mob);
            }
        }
        return mobs;
    }

    /** Discards every entity of the level carrying {@value #TAG}; returns how many. */
    public static int discardTagged(ServerLevel level) {
        List<Entity> tagged = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity.getTags().contains(TAG)) {
                tagged.add(entity);
            }
        }
        for (Entity entity : tagged) {
            if (!entity.isRemoved()) {
                entity.discard();
            }
        }
        return tagged.size();
    }

    /** Parts across the mobs (the MHLib part count of the scene). */
    public static int partCount(List<? extends Entity> mobs) {
        int parts = 0;
        for (Entity mob : mobs) {
            if (mob.isRemoved()) {
                continue;
            }
            var array = mob.getParts();
            if (array != null) {
                parts += array.length;
            }
        }
        return parts;
    }

    /** How many of {@code mobs} are alive and stand in an entity-ticking chunk of {@code level} (what the report's per-entity server figures divide by). */
    public static int countTicking(ServerLevel level, List<? extends Entity> mobs) {
        int ticking = 0;
        for (Entity mob : mobs) {
            if (!mob.isRemoved() && level.isPositionEntityTicking(mob.blockPosition())) {
                ticking++;
            }
        }
        return ticking;
    }
}
