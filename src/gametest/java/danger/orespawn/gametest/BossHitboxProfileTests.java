package danger.orespawn.gametest;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.GodzillaHead;
import danger.orespawn.entity.KingHead;
import danger.orespawn.entity.QueenHead;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * BOSS-047 — the King, the Kraken and Godzilla carry MultiHitboxLib bone-synced hitbox profiles fitted to their drawn
 * rigs (the Queen's ENT-S-092 form), in place of the King's and Godzilla's hand-placed {@code OreSpawnPartEntity}
 * layouts and the three head sidecars. Server-side pins only: the profile resolves and builds its parts, the parts sit
 * where the rest pose puts them (the fallback offsets, before any client bone packet), their union reaches where the
 * drawn body reaches, damage through a part reaches the body with the profile's modifier, the sidecars are gone, and
 * PlayNicely scales the parts with the box. The drawn alignment itself is the writer's arithmetic
 * ({@code danger.orespawn.tools.hitbox.BossPartProfileWriter}, GeckoLib's own bake and matrix chain) and an in-game look.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BossHitboxProfileTests {

    private static final float EPS = 1e-4f;
    private static final BlockPos POS_A = new BlockPos(24, 8, 24);
    private static final BlockPos POS_B = new BlockPos(12, 8, 12);
    private static final int ALIGN_TICKS = 3;

    /** One profiled boss: its type, full and PlayNicely boxes, the PlayNicely entity scale, the profile's part count. */
    private record Boss(String label, EntityType<? extends Mob> type, float fullW, float fullH, float niceW, float niceH,
                        double niceScale, int parts, String headPart, String glancingPart,
                        double minSpanX, double minSpanY, double minSpanZ) {
    }

    private static List<Boss> bosses() {
        return List.of(
                // orig TheKing.java:86/:88; render scale 2.1 (ClientProxyOreSpawn.java:492); heads 25+ blocks ahead, wings 52 out
                new Boss("TheKing", ModEntities.THE_KING.get(), 22.0f, 24.0f, 5.5f, 6.0f, 0.25D, 26, "CHead1", "Lwing3", 80.0D, 15.0D, 60.0D),
                // orig Kraken.java:73/:75; render scale 1.0 (ClientProxyOreSpawn.java:444) under the classic whole-model XP 90 turn:
                // upright, tentacles 14 blocks below the feet, tail 18 above
                new Boss("Kraken", ModEntities.KRAKEN.get(), 4.0f, 15.0f, 1.3333334f, 5.0f, 1.0D / 3.0D, 25, "Head", null, 6.0D, 25.0D, 4.0D), // no glancing part: one full-damage box in 1.7.10
                // orig Godzilla.java:72/:74; render scale 2.0 (ClientProxyOreSpawn.java:462); 25 tall, arms 17 out, tail 23 behind
                new Boss("Godzilla", ModEntities.GODZILLA.get(), 9.9f, 25.0f, 2.475f, 6.25f, 0.25D, 16, "Head", "Tail5", 25.0D, 20.0D, 30.0D));
    }

    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        return mob;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IMultipartEntity multipart(GameTestHelper helper, Mob mob, String label) {
        Object asObject = mob;
        helper.assertTrue(asObject instanceof IMultipartEntity<?>, label + " is not an IMultipartEntity (MHLib LivingEntity mixin missing)");
        return (IMultipartEntity) asObject;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static HitboxProfile profile(GameTestHelper helper, Mob mob, String label) {
        Optional<HitboxProfile> profile = multipart(helper, mob, label).getHitboxProfile();
        helper.assertTrue(profile != null && profile.isPresent(), label + ": no hitbox profile resolved for " + EntityType.getKey(mob.getType()));
        return profile.get();
    }

    private static Map<String, MHLibPartEntity<?>> partsByName(GameTestHelper helper, Mob mob, Boss boss) {
        PartEntity<?>[] parts = mob.getParts();
        helper.assertTrue(parts != null && parts.length == boss.parts,
                boss.label + " must carry the profile's " + boss.parts + " MHLib parts, got " + (parts == null ? 0 : parts.length));
        Map<String, MHLibPartEntity<?>> byName = new LinkedHashMap<>();
        for (PartEntity<?> part : parts) {
            helper.assertTrue(part instanceof MHLibPartEntity<?>, boss.label + " part is not an MHLibPartEntity: " + part);
            helper.assertTrue(part.getParent() == mob, boss.label + " part " + part + " does not name the boss as its parent");
            MHLibPartEntity<?> mhlibPart = (MHLibPartEntity<?>) part;
            byName.put(mhlibPart.getConfigName(), mhlibPart);
        }
        helper.assertTrue(byName.size() == boss.parts, boss.label + " part names are not distinct: " + byName.keySet());
        return byName;
    }

    /** The profile resolves, every synched bone is a part named for it, the envelope is the 1.7.10 box and is not pickable. */
    @GameTest(template = "empty_large", batch = "bossHitboxProfiles")
    public static void s047a_the_king_the_kraken_and_godzilla_carry_their_profiles(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        try {
            OreSpawnConfig.PLAY_NICELY.set(false);
            for (Boss boss : bosses()) {
                Mob mob = spawnFrozen(helper, boss.type, POS_A);
                HitboxProfile profile = profile(helper, mob, boss.label);
                helper.assertTrue(mob.isMultipartEntity(), boss.label + " does not report multipart");
                Map<String, MHLibPartEntity<?>> parts = partsByName(helper, mob, boss);
                Set<String> synched = new LinkedHashSet<>(profile.synchedBones());
                helper.assertTrue(synched.equals(parts.keySet()),
                        boss.label + " parts " + parts.keySet() + " are not the profile's synched bones " + synched);
                helper.assertTrue(profile.partConfigs().size() == boss.parts,
                        boss.label + " profile lists " + profile.partConfigs().size() + " parts, expected " + boss.parts);
                helper.assertTrue(Math.abs(mob.getBbWidth() - boss.fullW) < EPS && Math.abs(mob.getBbHeight() - boss.fullH) < EPS,
                        boss.label + " envelope must be " + boss.fullW + "x" + boss.fullH + ", got " + mob.getBbWidth() + "x" + mob.getBbHeight());
                helper.assertTrue(!profile.mainHitboxConfig().canReceiveDamage() && !mob.isPickable(),
                        boss.label + " envelope must take no damage and be unpickable (every hit through a part)");
                for (MHLibPartEntity<?> part : parts.values()) {
                    helper.assertTrue(part.isPickable(), boss.label + " part " + part.getConfigName() + " is not pickable");
                    helper.assertTrue(part.getConfig().canReceiveDamage(), boss.label + " part " + part.getConfigName() + " takes no damage");
                }
                mob.discard();
            }
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
        }
        helper.succeed();
    }

    /**
     * After the alignment ticks, with no client bone packet, every part sits at its fallback: its centre within its own
     * size of the anchor bone's rest world position, and the parts' union spans the drawn body - the King's wings and
     * heads, the Kraken's tentacles and tail, Godzilla's height and tail - which the old layouts never did.
     */
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "bossHitboxProfiles")
    public static void s047b_parts_sit_on_their_bones_at_rest(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        OreSpawnConfig.PLAY_NICELY.set(false);
        Map<Boss, Mob> mobs = new LinkedHashMap<>();
        try {
            for (Boss boss : bosses()) {
                mobs.put(boss, spawnFrozen(helper, boss.type, POS_A));
            }
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
        }
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                for (Map.Entry<Boss, Mob> entry : mobs.entrySet()) {
                    Boss boss = entry.getKey();
                    Mob mob = entry.getValue();
                    Map<String, MHLibPartEntity<?>> parts = partsByName(helper, mob, boss);
                    Vec3 origin = mob.position();
                    AABB union = null;
                    for (MHLibPartEntity<?> part : parts.values()) {
                        AABB box = part.getBoundingBox();
                        helper.assertTrue(box.getXsize() > 0.05D && box.getYsize() > 0.05D,
                                boss.label + " part " + part.getConfigName() + " has no size: " + box);
                        Vec3 anchor = origin.add(part.getConfigPositionOffset());
                        double reach = Math.max(box.getXsize(), box.getYsize()) + 1.0D;
                        helper.assertTrue(box.getCenter().distanceTo(anchor) <= reach,
                                boss.label + " part " + part.getConfigName() + " centre " + box.getCenter() + " is "
                                        + box.getCenter().distanceTo(anchor) + " from its bone's rest position " + anchor
                                        + " (> " + reach + "): the fallback alignment did not place it");
                        union = union == null ? box : union.minmax(box);
                    }
                    helper.assertTrue(union != null && union.getXsize() >= boss.minSpanX && union.getYsize() >= boss.minSpanY
                                    && union.getZsize() >= boss.minSpanZ,
                            boss.label + " parts span " + (union == null ? "nothing" : union.getXsize() + "x" + union.getYsize() + "x" + union.getZsize())
                                    + ", less than the drawn body's " + boss.minSpanX + "x" + boss.minSpanY + "x" + boss.minSpanZ);
                }
            } finally {
                mobs.values().forEach(Mob::discard);
            }
            helper.succeed();
        });
    }

    /**
     * A hit on the head part reaches the body in full; one on a glancing part at the profile's modifier. Two frozen
     * bosses each: the King's hurtCooldown and Godzilla's and the Kraken's hurtTimer count down only in
     * customServerAiStep, which setNoAi skips, so a frozen boss takes exactly one hit. Magic: in bypasses_armor (the
     * King's getArmorValue 21, Godzilla's 21 and the Kraken's 10 would leave 33.28 / 33.28 / 36.8 of a generic 40) and
     * not in bypasses_invulnerability (the ENT-S-172 clause).
     */
    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "bossHitboxProfiles")
    public static void s047c_part_damage_reaches_the_body_with_the_modifier(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        OreSpawnConfig.PLAY_NICELY.set(false);
        Map<Boss, Mob[]> mobs = new LinkedHashMap<>();
        try {
            for (Boss boss : bosses()) {
                mobs.put(boss, new Mob[]{spawnFrozen(helper, boss.type, POS_A), spawnFrozen(helper, boss.type, POS_B)});
            }
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
        }
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                DamageSource source = helper.getLevel().damageSources().magic();
                for (Map.Entry<Boss, Mob[]> entry : mobs.entrySet()) {
                    Boss boss = entry.getKey();
                    LivingEntity viaHead = entry.getValue()[0];
                    float before = viaHead.getHealth();
                    boolean hit = partsByName(helper, entry.getValue()[0], boss).get(boss.headPart).hurt(source, 40.0F);
                    helper.assertTrue(hit && Math.abs((before - viaHead.getHealth()) - 40.0F) < 0.01F,
                            boss.label + ": 40 through the head part " + boss.headPart + " took " + (before - viaHead.getHealth()) + " (hit=" + hit + ")");
                    if (boss.glancingPart == null) {
                        continue; // every part of this boss takes full damage, as its single 1.7.10 box did
                    }
                    LivingEntity viaGlancing = entry.getValue()[1];
                    MHLibPartEntity<?> glancing = partsByName(helper, entry.getValue()[1], boss).get(boss.glancingPart);
                    float modifier = glancing.getConfig().damageModifier();
                    helper.assertTrue(modifier < 1.0F, boss.label + " glancing part " + boss.glancingPart + " has modifier " + modifier);
                    before = viaGlancing.getHealth();
                    hit = glancing.hurt(source, 40.0F);
                    helper.assertTrue(hit && Math.abs((before - viaGlancing.getHealth()) - 40.0F * modifier) < 0.01F,
                            boss.label + ": 40 through " + boss.glancingPart + " (x" + modifier + ") took " + (before - viaGlancing.getHealth()) + " (hit=" + hit + ")");
                }
            } finally {
                for (Mob[] pair : mobs.values()) {
                    pair[0].discard();
                    pair[1].discard();
                }
            }
            helper.succeed();
        });
    }

    /** The 1.7.10 head sidecars are retired: one that loads discards itself on its first server tick. */
    @GameTest(template = "empty_large", timeoutTicks = 40, batch = "bossHitboxProfiles")
    public static void s047d_the_head_sidecars_discard_themselves(GameTestHelper helper) {
        KingHead king = helper.spawnWithNoFreeWill(ModEntities.KING_HEAD.get(), POS_A);
        QueenHead queen = helper.spawnWithNoFreeWill(ModEntities.QUEEN_HEAD.get(), POS_B);
        GodzillaHead godzilla = helper.spawnWithNoFreeWill(ModEntities.GODZILLA_HEAD.get(), new BlockPos(36, 8, 36));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(king.isRemoved(), "a KingHead sidecar survived its first server ticks (BOSS-047)");
            helper.assertTrue(queen.isRemoved(), "a QueenHead sidecar survived its first server ticks (BOSS-047)");
            helper.assertTrue(godzilla.isRemoved(), "a GodzillaHead sidecar survived its first server ticks (BOSS-047)");
            helper.succeed();
        });
    }

    /** PlayNicely (a constructor-time snapshot): the envelope and every part shrink by the same factor as the draw. */
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "bossHitboxProfilesNice")
    public static void s047e_play_nicely_scales_the_parts_with_the_box(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        Map<Boss, Mob[]> pairs = new LinkedHashMap<>();
        try {
            for (Boss boss : bosses()) {
                OreSpawnConfig.PLAY_NICELY.set(false);
                Mob full = spawnFrozen(helper, boss.type, POS_A);
                OreSpawnConfig.PLAY_NICELY.set(true);
                Mob nice = spawnFrozen(helper, boss.type, POS_B);
                pairs.put(boss, new Mob[]{full, nice});
            }
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
        }
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                for (Map.Entry<Boss, Mob[]> entry : pairs.entrySet()) {
                    Boss boss = entry.getKey();
                    Mob full = entry.getValue()[0];
                    Mob nice = entry.getValue()[1];
                    helper.assertTrue(Math.abs(nice.getBbWidth() - boss.niceW) < EPS && Math.abs(nice.getBbHeight() - boss.niceH) < EPS,
                            boss.label + " PlayNicely envelope must be " + boss.niceW + "x" + boss.niceH + ", got " + nice.getBbWidth() + "x" + nice.getBbHeight());
                    double internal = multipart(helper, nice, boss.label).mhlibGetEntitySizeInternally(nice);
                    helper.assertTrue(Math.abs(internal - boss.niceScale) < EPS,
                            boss.label + " PlayNicely MHLib entity scale must be " + boss.niceScale + ", got " + internal);
                    Map<String, MHLibPartEntity<?>> fullParts = partsByName(helper, full, boss);
                    Map<String, MHLibPartEntity<?>> niceParts = partsByName(helper, nice, boss);
                    for (Map.Entry<String, MHLibPartEntity<?>> part : fullParts.entrySet()) {
                        EntityDimensions fullDims = part.getValue().getDimensions(Pose.STANDING);
                        EntityDimensions niceDims = niceParts.get(part.getKey()).getDimensions(Pose.STANDING);
                        helper.assertTrue(Math.abs(niceDims.width() - fullDims.width() * boss.niceScale) < EPS
                                        && Math.abs(niceDims.height() - fullDims.height() * boss.niceScale) < EPS,
                                boss.label + " PlayNicely part " + part.getKey() + " must be " + boss.niceScale + "x the hostile part ("
                                        + fullDims.width() + "x" + fullDims.height() + "), got " + niceDims.width() + "x" + niceDims.height());
                    }
                }
            } finally {
                for (Mob[] pair : pairs.values()) {
                    pair[0].discard();
                    pair[1].discard();
                }
            }
            helper.succeed();
        });
    }
}
