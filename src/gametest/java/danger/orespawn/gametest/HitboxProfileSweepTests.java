package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.Dragon;
import danger.orespawn.entity.Mothra;
import danger.orespawn.entity.ThePrinceAdult;
import danger.orespawn.entity.ThePrinceTeen;
import danger.orespawn.entity.OreSpawnPartEntity;
import danger.orespawn.entity.hitbox.HitboxScales;
import danger.orespawn.gametest.HitboxProfileExpectations.Expected;
import de.dertoaster.multihitboxlib.api.IMultipartEntity;
import de.dertoaster.multihitboxlib.api.MHLibEntitySizeScales;
import de.dertoaster.multihitboxlib.entity.MHLibPartEntity;
import de.dertoaster.multihitboxlib.entity.hitbox.HitboxProfile;
import de.dertoaster.multihitboxlib.init.MHLibDatapackLoaders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-173: every living OreSpawn species drawn through a GeckoLib rig carries a MultiHitboxLib profile whose
 * parts are boxes fitted to the drawn rig (the bosses' BOSS-047 treatment, generated for the rest by
 * tools/hitbox_profiles.sh). The sweep pins, over the whole {@link HitboxProfileExpectations}
 * list: the profile resolves with the generated part count and no main size (the entity keeps its own dimensions);
 * the body is not pickable and every part is, taking full damage and not solid; the server-side fallback places every
 * part exactly where the profile says, scaled by the entity's size; a hit through a part reaches the body in full; a
 * baby's parts follow the renderer's rule (halved where the descriptor halves the model, full size where it does
 * not); Mothra's hand-placed parts are gone; the size table is registered.
 *
 * <p>The species are taken one at a time (spawned alone, checked after the alignment ticks, discarded): a hundred
 * frozen creatures on one block hit each other through their own tick logic, and a hit within a mob's own
 * invulnerability window no larger than the last is refused by vanilla's {@code LivingEntity.hurt}.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HitboxProfileSweepTests {

    private static final float EPS = 1e-4f;
    private static final double PLACE_EPS = 0.05D;
    private static final BlockPos POS = new BlockPos(24, 8, 24);
    private static final int ALIGN_TICKS = 3;
    /** The profiled species the sweep does not own: the four bosses (BOSS-047, ENT-S-092) and the two gait-fed robots (S4/S5b). */
    private static final Set<String> OTHER_PROFILED = Set.of("orespawn:the_king", "orespawn:the_queen", "orespawn:kraken",
            "orespawn:godzilla", "orespawn:ant_robot", "orespawn:spider_robot");

    private interface Check {
        void run(Expected expected, Mob mob);
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends Mob> type(GameTestHelper helper, String id) {
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(id));
        helper.assertTrue(type.isPresent(), "no entity type " + id);
        return (EntityType<? extends Mob>) type.get();
    }

    /** {@link #spawnFrozen} at a position of the caller's. */
    private static Mob spawnFrozenAt(GameTestHelper helper, EntityType<? extends Mob> type, BlockPos pos) {
        Mob mob = helper.spawnWithNoFreeWill(type, pos);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setYRot(0.0F);
        mob.setXRot(0.0F);
        mob.yBodyRot = 0.0F;
        mob.yHeadRot = 0.0F;
        return mob;
    }

    /** A frozen mob facing +z with no pitch: the fallback alignment rotates the part offsets by the entity's yaw and pitch. */
    private static Mob spawnFrozen(GameTestHelper helper, EntityType<? extends Mob> type) {
        return spawnFrozenAt(helper, type, POS);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IMultipartEntity multipart(GameTestHelper helper, Mob mob, String label) {
        Object asObject = mob;
        helper.assertTrue(asObject instanceof IMultipartEntity<?>, label + " is not an IMultipartEntity (MHLib LivingEntity mixin missing)");
        return (IMultipartEntity) asObject;
    }

    @SuppressWarnings("unchecked")
    private static HitboxProfile profile(GameTestHelper helper, Mob mob, String label) {
        Optional<HitboxProfile> profile = multipart(helper, mob, label).getHitboxProfile();
        helper.assertTrue(profile != null && profile.isPresent(), label + ": no hitbox profile resolved");
        return profile.get();
    }

    private static List<MHLibPartEntity<?>> parts(GameTestHelper helper, Mob mob, Expected expected) {
        PartEntity<?>[] parts = mob.getParts();
        helper.assertTrue(parts != null && parts.length == expected.parts(),
                expected.id() + " must carry the profile's " + expected.parts() + " MHLib parts, got " + (parts == null ? 0 : parts.length));
        List<MHLibPartEntity<?>> out = new ArrayList<>();
        Set<String> names = new LinkedHashSet<>();
        for (PartEntity<?> part : parts) {
            helper.assertTrue(part instanceof MHLibPartEntity<?>, expected.id() + " part is not an MHLibPartEntity: " + part);
            helper.assertTrue(part.getParent() == mob, expected.id() + " part " + part + " does not name the mob as its parent");
            MHLibPartEntity<?> mhlibPart = (MHLibPartEntity<?>) part;
            names.add(mhlibPart.getConfigName());
            out.add(mhlibPart);
        }
        helper.assertTrue(names.size() == expected.parts(), expected.id() + " part names are not distinct: " + names);
        return out;
    }

    /** Spawns the species one after another, runs the check on each after the alignment ticks, collects the failures. */
    private static void sweep(GameTestHelper helper, int index, List<String> failures, Check check) {
        if (index >= HitboxProfileExpectations.ALL.size()) {
            helper.assertTrue(failures.isEmpty(), failures.size() + " failure(s): " + String.join("; ", failures));
            helper.succeed();
            return;
        }
        Expected expected = HitboxProfileExpectations.ALL.get(index);
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        OreSpawnConfig.PLAY_NICELY.set(false);
        Mob mob;
        try {
            mob = spawnFrozen(helper, type(helper, expected.id()));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
        }
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                check.run(expected, mob);
            } catch (GameTestAssertException e) {
                failures.add(e.getMessage());
            } finally {
                mob.discard();
            }
            sweep(helper, index + 1, failures, check);
        });
    }

    /**
     * Every species in the list resolves its profile: the generated part count, every synched bone a part named for
     * it, no main size (the body box is what the entity's own dimensions say - the registered 1.7.10 box, Mothra's
     * MOD-029 form, the Crab's growth - not a profile value), the body not pickable and taking no damage itself, every
     * part pickable, taking full damage, not solid. And the coverage: the profiled OreSpawn types are exactly this list
     * plus the six the sweep does not own.
     */
    @GameTest(template = "empty_large", batch = "hitboxProfilesAll")
    public static void s173a_every_species_carries_its_generated_profile(GameTestHelper helper) {
        final boolean prior = OreSpawnConfig.PLAY_NICELY.get();
        OreSpawnConfig.PLAY_NICELY.set(false);
        try {
            Set<String> listed = new LinkedHashSet<>();
            for (Expected expected : HitboxProfileExpectations.ALL) {
                listed.add(expected.id());
                EntityType<? extends Mob> type = type(helper, expected.id());
                Mob mob = spawnFrozen(helper, type);
                try {
                    HitboxProfile profile = profile(helper, mob, expected.id());
                    helper.assertTrue(mob.isMultipartEntity(), expected.id() + " does not report multipart");
                    helper.assertTrue(profile.partConfigs().size() == expected.parts(),
                            expected.id() + " profile has " + profile.partConfigs().size() + " parts, expected " + expected.parts());
                    helper.assertTrue(profile.syncToModel(), expected.id() + " profile does not sync with the model");
                    helper.assertTrue(profile.mainHitboxConfig().baseSize().equals(Vec2.ZERO),
                            expected.id() + " profile overrides the main size: " + profile.mainHitboxConfig().baseSize());
                    EntityDimensions own = mob.getDimensions(Pose.STANDING);
                    helper.assertTrue(Math.abs(mob.getBbWidth() - own.width()) < EPS && Math.abs(mob.getBbHeight() - own.height()) < EPS,
                            expected.id() + " body box " + mob.getBbWidth() + "x" + mob.getBbHeight() + " is not the entity's own "
                                    + own.width() + "x" + own.height() + " (MHLib's size handler replaced it)");
                    if (expected.rule().equals("constant") || expected.rule().equals("baby")) {
                        EntityDimensions registered = type.getDimensions();
                        boolean mothra = expected.id().equals("orespawn:mothra"); // MOD-029: its own 6x3 root in modern mode
                        helper.assertTrue(mothra || (Math.abs(own.width() - registered.width()) < EPS && Math.abs(own.height() - registered.height()) < EPS),
                                expected.id() + " own dimensions " + own.width() + "x" + own.height() + " are not the registered "
                                        + registered.width() + "x" + registered.height());
                    }
                    helper.assertTrue(!profile.mainHitboxConfig().canReceiveDamage() && !mob.isPickable(),
                            expected.id() + " body still takes hits itself (canReceiveDamage=" + profile.mainHitboxConfig().canReceiveDamage()
                                    + ", isPickable=" + mob.isPickable() + ")");
                    List<MHLibPartEntity<?>> parts = parts(helper, mob, expected);
                    Set<String> synched = new LinkedHashSet<>(profile.synchedBones());
                    Set<String> names = new LinkedHashSet<>();
                    for (MHLibPartEntity<?> part : parts) {
                        names.add(part.getConfigName());
                        helper.assertTrue(part.isPickable(), expected.id() + " part " + part.getConfigName() + " is not pickable");
                        helper.assertTrue(part.getConfig().canReceiveDamage(), expected.id() + " part " + part.getConfigName() + " takes no damage");
                        helper.assertTrue(Math.abs(part.getConfig().damageModifier() - 1.0F) < EPS,
                                expected.id() + " part " + part.getConfigName() + " has modifier " + part.getConfig().damageModifier() + ", not 1.0");
                        helper.assertTrue(!part.getConfig().collidable() && !part.canBeCollidedWith(),
                                expected.id() + " part " + part.getConfigName() + " is solid");
                    }
                    helper.assertTrue(synched.equals(names), expected.id() + " synched bones " + synched + " are not its parts " + names);
                } finally {
                    mob.discard();
                }
            }
            // coverage: nothing profiled beyond the list and the six, nothing in the list without a profile
            Set<String> profiled = new LinkedHashSet<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                ResourceLocation key = EntityType.getKey(type);
                if (!key.getNamespace().equals(OreSpawnMod.MOD_ID)) {
                    continue;
                }
                if (MHLibDatapackLoaders.getHitboxProfile(type, helper.getLevel().registryAccess()).isPresent()) {
                    profiled.add(key.toString());
                }
            }
            Set<String> expectedProfiled = new LinkedHashSet<>(listed);
            expectedProfiled.addAll(OTHER_PROFILED);
            helper.assertTrue(profiled.equals(expectedProfiled), "profiled OreSpawn types " + profiled.size() + " differ from the expected "
                    + expectedProfiled.size() + ": extra " + difference(profiled, expectedProfiled) + ", missing " + difference(expectedProfiled, profiled));
        } finally {
            OreSpawnConfig.PLAY_NICELY.set(prior);
        }
        helper.succeed();
    }

    private static Set<String> difference(Set<String> a, Set<String> b) {
        Set<String> out = new LinkedHashSet<>(a);
        out.removeAll(b);
        return out;
    }

    /**
     * After the alignment ticks (server-side, no client) the fallback of {@code alignSynchedSubParts} has placed every
     * part on its drawn rest segment: the box's bottom-centre at the entity's position plus (position - the pivot
     * rotated by the anchor bone's rest rotation, the profile's "rotation") scaled by the entity's size (the Crab spawns
     * at a quarter of its full size, Pitch Black at its tier), at body yaw 0 - the writer's own derivation (pivot =
     * anchor - segment bottom-centre, taken back through that rotation), so the pinned place is the drawn segment's
     * bottom-centre. And every box has a size.
     */
    @GameTest(template = "empty_large", timeoutTicks = 900, batch = "hitboxProfilesAll")
    public static void s173b_the_fallback_places_every_part_on_its_drawn_rest_segment(GameTestHelper helper) {
        sweep(helper, 0, new ArrayList<>(), (expected, mob) -> {
            @SuppressWarnings("unchecked")
            double scale = multipart(helper, mob, expected.id()).mhlibGetEntitySizeInternally(mob);
            helper.assertTrue(scale > 0.0D, expected.id() + " has entity size scale " + scale);
            Vec3 origin = mob.position();
            for (MHLibPartEntity<?> part : parts(helper, mob, expected)) {
                AABB box = part.getBoundingBox();
                helper.assertTrue(box.getXsize() > 0.02D && box.getYsize() > 0.02D && box.getZsize() > 0.02D,
                        expected.id() + " part " + part.getConfigName() + " has no size: " + box);
                EntityDimensions dims = part.getDimensions(Pose.STANDING);
                Vec3 rest = part.getConfig().hitboxType().getBaseRotation();
                Vec3 rotatedPivot = de.dertoaster.multihitboxlib.util.BodyYawFold.rotateLikeApplyInformation(part.getPivot(), rest);
                Vec3 expectedCentre = origin.add(part.getConfigPositionOffset().subtract(rotatedPivot).scale(scale))
                        .add(0.0D, dims.height() / 2.0D, 0.0D);
                double off = box.getCenter().distanceTo(expectedCentre);
                helper.assertTrue(off <= PLACE_EPS, expected.id() + " part " + part.getConfigName() + " centre " + box.getCenter()
                        + " is " + off + " from its drawn rest place " + expectedCentre + " (scale " + scale + ", rest rotation " + rest + ")");
            }
        });
    }

    /**
     * TEST-020: a flier without AI keeps still. The sweep spawns its mobs frozen (no AI) and checks their parts three
     * ticks later; the Dragons' and the Princes' own behaviours ran whatever the NoAI tag said, so a frozen Dragon or
     * Baby Dragon that took off (its passive behaviours switch it to flying on one tick in 750) flew, and the parts the
     * library had placed before that flight moved it sat a move behind: s173b's red row at 9886127, -60, 10773056. Set
     * flying, all four stay where they were spawned, facing as they were.
     */
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "hitboxProfilesAll")
    public static void s173i_a_flier_without_ai_stays_where_it_was_spawned(GameTestHelper helper) {
        List<String> ids = List.of("orespawn:dragon", "orespawn:baby_dragon", "orespawn:the_prince_adult", "orespawn:the_prince_teen");
        List<Mob> fliers = new ArrayList<>();
        List<Vec3> spawned = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Mob mob = spawnFrozenAt(helper, type(helper, ids.get(i)), POS.offset(i * 12 - 18, 0, 0));
            setActivity(helper, mob, ids.get(i), 1);
            fliers.add(mob);
            spawned.add(mob.position());
        }
        helper.runAfterDelay(ALIGN_TICKS + 7, () -> {
            List<String> failures = new ArrayList<>();
            try {
                for (int i = 0; i < fliers.size(); i++) {
                    Mob mob = fliers.get(i);
                    double moved = mob.position().distanceTo(spawned.get(i));
                    int activity = activity(mob);
                    if (activity != 1) {
                        failures.add(ids.get(i) + " is no longer set flying (activity " + activity + "): the check would be vacuous");
                    }
                    if (moved > 1.0E-9) {
                        failures.add(ids.get(i) + " without AI moved " + moved + " from where it was spawned (" + spawned.get(i)
                                + " to " + mob.position() + ")");
                    }
                    if (mob.getYRot() != 0.0F) {
                        failures.add(ids.get(i) + " without AI turned to yaw " + mob.getYRot());
                    }
                }
            } finally {
                fliers.forEach(Entity::discard);
            }
            helper.assertTrue(failures.isEmpty(), failures.size() + " failure(s): " + String.join("; ", failures));
            helper.succeed();
        });
    }

    /** The activity state of a Dragon or a Prince (0 on the ground, 1 flying). */
    private static int activity(Mob mob) {
        if (mob instanceof Dragon dragon) {
            return dragon.getActivity();
        }
        if (mob instanceof ThePrinceAdult adult) {
            return adult.getActivity();
        }
        return mob instanceof ThePrinceTeen teen ? teen.getActivity() : -1;
    }

    private static void setActivity(GameTestHelper helper, Mob mob, String id, int value) {
        if (mob instanceof Dragon dragon) {
            dragon.setActivity(value);
        } else if (mob instanceof ThePrinceAdult adult) {
            adult.setActivity(value);
        } else if (mob instanceof ThePrinceTeen teen) {
            teen.setActivity(value);
        } else {
            helper.fail(id + " is neither a Dragon nor a Prince");
        }
    }

    /**
     * TEST-020: a burrower or a hurt frog without AI stays where it was spawned. The three worms, buried (stone two and
     * three blocks over each, the cells their burrow cycles read), rise through it on their own, 0.05 a tick; the frog
     * jumps 0.35 when hurt. Without AI none of them moves: twenty ticks on, the frog hurt for no damage as it was
     * spawned, every one stands where it was spawned.
     */
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "hitboxProfilesAll")
    public static void s173j_a_burrower_or_a_hurt_frog_without_ai_stays_where_it_was_spawned(GameTestHelper helper) {
        helper.assertFalse(OreSpawnConfig.PLAY_NICELY.get(),
                "playNicely is on: the small worm would read only its own cell for its first fifty ticks");
        List<String> worms = List.of("orespawn:worm_small", "orespawn:worm_medium", "orespawn:worm_large");
        List<String> ids = new ArrayList<>();
        List<Mob> mobs = new ArrayList<>();
        List<Vec3> spawned = new ArrayList<>();
        for (int i = 0; i < worms.size(); i++) {
            BlockPos at = POS.offset(i * 12 - 18, 0, 0);
            helper.setBlock(at.above(2), Blocks.STONE);
            helper.setBlock(at.above(3), Blocks.STONE);
            Mob worm = spawnFrozenAt(helper, type(helper, worms.get(i)), at);
            ids.add(worms.get(i));
            mobs.add(worm);
            spawned.add(worm.position());
        }
        Mob frog = spawnFrozenAt(helper, type(helper, "orespawn:frog"), POS.offset(18, 0, 0));
        ids.add("orespawn:frog");
        mobs.add(frog);
        spawned.add(frog.position());
        frog.hurt(helper.getLevel().damageSources().generic(), 0.0F);
        helper.runAfterDelay(20, () -> {
            List<String> failures = new ArrayList<>();
            try {
                for (int i = 0; i < mobs.size(); i++) {
                    double moved = mobs.get(i).position().distanceTo(spawned.get(i));
                    if (moved > 1.0E-9) {
                        failures.add(ids.get(i) + " without AI moved " + moved + " from where it was spawned (" + spawned.get(i)
                                + " to " + mobs.get(i).position() + ")");
                    }
                }
            } finally {
                mobs.forEach(Entity::discard);
            }
            helper.assertTrue(failures.isEmpty(), failures.size() + " failure(s): " + String.join("; ", failures));
            helper.succeed();
        });
    }

    /**
     * TEST-020: a tame flier without AI does not take off for its owner. A tame Dragon or Baby Dragon takes off when its
     * owner flies or stands more than 12 blocks off (its passive behaviours at 20), a tame adult Prince at 30 and a teen
     * at 20. Without AI none of them does: with the owner flying 48 to 57 blocks away, all four are still on the ground,
     * where they were spawned, ten ticks on. A batch of its own: the owner, a player in the level, stays away from the
     * sweep's mobs, and it leaves the level however the test ends.
     */
    @SuppressWarnings({"removal", "deprecation"})
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "hitboxNoAiOwner")
    public static void s173l_a_tame_flier_without_ai_does_not_take_off_for_its_owner(GameTestHelper helper) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        List<Mob> mobs = new ArrayList<>();
        EntityLogicTestsA.onTestExit(helper, () -> {
            mobs.forEach(Entity::discard);
            helper.getLevel().getServer().getPlayerList().remove(owner);
        });
        Vec3 ownerAt = helper.absoluteVec(new Vec3(44.5D, 8.0D, 44.5D));
        owner.teleportTo(helper.getLevel(), ownerAt.x, ownerAt.y, ownerAt.z, 0.0F, 0.0F);
        owner.getAbilities().mayfly = true;
        owner.getAbilities().flying = true;
        owner.onUpdateAbilities();
        List<String> ids = List.of("orespawn:dragon", "orespawn:baby_dragon", "orespawn:the_prince_adult", "orespawn:the_prince_teen");
        List<BlockPos> at = List.of(new BlockPos(4, 8, 4), new BlockPos(4, 8, 16), new BlockPos(16, 8, 4), new BlockPos(10, 8, 10));
        List<Vec3> spawned = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Mob mob = spawnFrozenAt(helper, type(helper, ids.get(i)), at.get(i));
            TamableAnimal pet = (TamableAnimal) mob;
            pet.setTame(true, false);
            pet.setOwnerUUID(owner.getUUID());
            pet.setOrderedToSit(false);
            mobs.add(mob);
            spawned.add(mob.position());
        }
        helper.runAfterDelay(10, () -> {
            List<String> failures = new ArrayList<>();
            for (int i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                double far = Math.sqrt(mob.distanceToSqr(owner));
                if (((TamableAnimal) mob).getOwner() != owner || far <= 30.0D || !owner.getAbilities().flying) {
                    failures.add(ids.get(i) + " does not see its owner flying more than 30 blocks off (" + far + "): the check would be vacuous");
                }
                if (activity(mob) != 0) {
                    failures.add(ids.get(i) + " without AI took off for its owner (activity " + activity(mob) + ")");
                }
                double moved = mob.position().distanceTo(spawned.get(i));
                if (moved > 1.0E-9) {
                    failures.add(ids.get(i) + " without AI moved " + moved + " from where it was spawned");
                }
            }
            helper.assertTrue(failures.isEmpty(), failures.size() + " failure(s): " + String.join("; ", failures));
            helper.succeed();
        });
    }

    /**
     * TEST-020: a jumper without AI stays where it was spawned. The frog jumps 0.35 on its own one tick in 70 and the
     * cricket 0.25 one tick in 50, each after the library has placed its parts; s173b caught a frog that had jumped on
     * its last tick (0.35 against 0.05). Without AI neither jumps: after 600 ticks both stand where they were spawned.
     * Without the hold the frog would stay put that long one time in about 5,600 and the cricket one in about 180,000.
     */
    @GameTest(template = "empty_large", timeoutTicks = 660, batch = "hitboxProfilesAll")
    public static void s173k_a_jumper_without_ai_stays_where_it_was_spawned(GameTestHelper helper) {
        List<String> ids = List.of("orespawn:frog", "orespawn:cricket");
        List<Mob> mobs = new ArrayList<>();
        List<Vec3> spawned = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Mob mob = spawnFrozenAt(helper, type(helper, ids.get(i)), POS.offset(i * 12 - 6, 0, 0));
            mobs.add(mob);
            spawned.add(mob.position());
        }
        helper.runAfterDelay(600, () -> {
            List<String> failures = new ArrayList<>();
            try {
                for (int i = 0; i < mobs.size(); i++) {
                    double moved = mobs.get(i).position().distanceTo(spawned.get(i));
                    if (moved > 1.0E-9) {
                        failures.add(ids.get(i) + " without AI moved " + moved + " from where it was spawned (" + spawned.get(i)
                                + " to " + mobs.get(i).position() + ")");
                    }
                }
            } finally {
                mobs.forEach(Entity::discard);
            }
            helper.assertTrue(failures.isEmpty(), failures.size() + " failure(s): " + String.join("; ", failures));
            helper.succeed();
        });
    }

    /**
     * The fallback turns with the body: the same Alosaurus at body yaw 90 carries its parts where the yaw-0 layout,
     * turned by the collector's own term (-90 degrees about y: the yaw-0 frame's +z front to -x, west, as the entity
     * faces), puts them - not mirrored (the old +yaw) and not left in the yaw-0 arrangement (the old unrotated pivot).
     */
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "hitboxProfilesAll")
    public static void s173g_the_fallback_turns_with_the_body(GameTestHelper helper) {
        Mob facingSouth = spawnFrozen(helper, type(helper, "orespawn:alosaurus"));
        Mob facingWest = spawnFrozen(helper, type(helper, "orespawn:alosaurus"));
        facingWest.setYRot(90.0F);
        facingWest.yBodyRot = 90.0F;
        facingWest.yHeadRot = 90.0F;
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                PartEntity<?>[] south = facingSouth.getParts();
                PartEntity<?>[] west = facingWest.getParts();
                helper.assertTrue(south != null && west != null && south.length == west.length && south.length > 1, "the two Alosauruses carry different part counts");
                double yawTerm = de.dertoaster.multihitboxlib.util.BodyYawFold.bodyYawRotationTerm(90.0F);
                Vec3 originSouth = facingSouth.position();
                Vec3 originWest = facingWest.position();
                double spread = 0.0D;
                for (int i = 0; i < south.length; i++) {
                    Vec3 localSouth = south[i].getBoundingBox().getCenter().subtract(originSouth);
                    Vec3 expectedWest = localSouth.yRot((float) yawTerm).add(originWest);
                    Vec3 actualWest = west[i].getBoundingBox().getCenter();
                    double off = actualWest.distanceTo(expectedWest);
                    helper.assertTrue(off <= PLACE_EPS, "Alosaurus part " + ((MHLibPartEntity<?>) west[i]).getConfigName() + " at yaw 90 sits " + off
                            + " from the turned yaw-0 place (expected " + expectedWest + ", actual " + actualWest + ")");
                    spread = Math.max(spread, Math.abs(localSouth.x) + Math.abs(localSouth.z));
                }
                helper.assertTrue(spread > 1.0D, "the Alosaurus' parts do not reach out of its origin (spread " + spread + "): the turn test would be vacuous");
            } finally {
                facingSouth.discard();
                facingWest.discard();
            }
            helper.succeed();
        });
    }

    /**
     * A hit through a part is the hit on the body (modifier 1.0 on every part): 8 points of magic (in bypasses_armor,
     * not in bypasses_invulnerability - the ENT-S-172 clause does not apply) through each species' first part take
     * exactly what the same hit takes from a twin struck on the body - the species' own rule included (the Irukandji's
     * and the ants' caps of 1, the Rat's 5, the Triffid's closed shell refusing it, the Ostrich's false with the health
     * gone). One hit per mob: a frozen mob's own hit window never counts down.
     */
    @GameTest(template = "empty_large", timeoutTicks = 900, batch = "hitboxProfilesAll")
    public static void s173c_a_hit_through_a_part_is_the_hit_on_the_body(GameTestHelper helper) {
        sweep(helper, 0, new ArrayList<>(), (expected, mob) -> {
            DamageSource source = helper.getLevel().damageSources().magic();
            Mob twin = spawnFrozen(helper, type(helper, expected.id()));
            try {
                MHLibPartEntity<?> part = parts(helper, mob, expected).get(0);
                float before = mob.getHealth();
                boolean hit = part.hurt(source, 8.0F);
                float taken = before - mob.getHealth();
                float twinBefore = twin.getHealth();
                boolean twinHit = twin.hurt(source, 8.0F);
                float twinTaken = twinBefore - twin.getHealth();
                helper.assertTrue(hit == twinHit && Math.abs(taken - twinTaken) < 0.01F,
                        expected.id() + ": 8 through " + part.getConfigName() + " took " + taken + " (hit=" + hit + "), 8 on the body took "
                                + twinTaken + " (hit=" + twinHit + ")");
                helper.assertTrue(twinTaken > 0.0F || !twinHit || expected.id().equals("orespawn:triffid"),
                        expected.id() + ": the body hit itself took nothing (" + twinTaken + ", hit=" + twinHit + ")");
            } finally {
                twin.discard();
            }
        });
    }

    /**
     * A baby's parts follow the renderer's rule. The Frog's descriptor halves a baby's model: the library's own rule
     * (an AgeableMob baby is 0.5) halves its body box and its part boxes. The Cockateil's draws a baby at full size:
     * the size table registers 1.0, so its part boxes stay full size while vanilla's age scale still halves the body
     * box (the box the model ignores). The Crab's registered scale is its growth factor, and the table is registered.
     */
    @GameTest(template = "empty_large", timeoutTicks = 80, batch = "hitboxProfilesAll")
    public static void s173d_a_babys_parts_follow_the_renderers_rule(GameTestHelper helper) {
        Mob frogAdult = spawnFrozen(helper, ModEntities.FROG.get());
        Mob frogBaby = spawnFrozen(helper, ModEntities.FROG.get());
        ((AgeableMob) frogBaby).setBaby(true);
        Mob birdAdult = spawnFrozen(helper, ModEntities.COCKATEIL.get());
        Mob birdBaby = spawnFrozen(helper, ModEntities.COCKATEIL.get());
        ((AgeableMob) birdBaby).setBaby(true);
        Crab crab = (Crab) spawnFrozen(helper, ModEntities.CRAB.get());
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                helper.assertTrue(HitboxScales.count() == MHLibEntitySizeScales.size(),
                        "size table registered " + MHLibEntitySizeScales.size() + " types, the table has " + HitboxScales.count());
                // the frog: body halves (vanilla), parts halve (the library's baby rule; no registration)
                helper.assertTrue(MHLibEntitySizeScales.get(ModEntities.FROG.get()) == null, "the Frog has a registered scale; the library's baby rule was expected");
                helper.assertTrue(Math.abs(frogBaby.getBbWidth() - 0.5F * frogAdult.getBbWidth()) < EPS, "baby frog body " + frogBaby.getBbWidth() + " is not half of " + frogAdult.getBbWidth());
                EntityDimensions adultPart = ((MHLibPartEntity<?>) frogAdult.getParts()[0]).getDimensions(Pose.STANDING);
                EntityDimensions babyPart = ((MHLibPartEntity<?>) frogBaby.getParts()[0]).getDimensions(Pose.STANDING);
                helper.assertTrue(Math.abs(babyPart.width() - 0.5F * adultPart.width()) < EPS && Math.abs(babyPart.height() - 0.5F * adultPart.height()) < EPS,
                        "baby frog part " + babyPart.width() + "x" + babyPart.height() + " is not half of " + adultPart.width() + "x" + adultPart.height());
                // the cockateil: body halves (vanilla), parts stay (the table's 1.0)
                ToDoubleFunction<Entity> birdScale = MHLibEntitySizeScales.get(ModEntities.COCKATEIL.get());
                helper.assertTrue(birdScale != null && birdScale.applyAsDouble(birdBaby) == 1.0D, "the Cockateil's registered scale is not 1.0 for a baby");
                helper.assertTrue(Math.abs(birdBaby.getBbWidth() - 0.5F * birdAdult.getBbWidth()) < EPS, "baby cockateil body " + birdBaby.getBbWidth() + " is not half of " + birdAdult.getBbWidth());
                adultPart = ((MHLibPartEntity<?>) birdAdult.getParts()[0]).getDimensions(Pose.STANDING);
                babyPart = ((MHLibPartEntity<?>) birdBaby.getParts()[0]).getDimensions(Pose.STANDING);
                helper.assertTrue(Math.abs(babyPart.width() - adultPart.width()) < EPS && Math.abs(babyPart.height() - adultPart.height()) < EPS,
                        "baby cockateil part " + babyPart.width() + "x" + babyPart.height() + " is not the adult's " + adultPart.width() + "x" + adultPart.height());
                // the crab: the registered scale is its growth factor
                ToDoubleFunction<Entity> crabScale = MHLibEntitySizeScales.get(ModEntities.CRAB.get());
                helper.assertTrue(crabScale != null && Math.abs(crabScale.applyAsDouble(crab) - crab.getCrabScale()) < EPS,
                        "the Crab's registered scale is not its growth factor " + crab.getCrabScale());
                MHLibPartEntity<?> crabPart = (MHLibPartEntity<?>) crab.getParts()[0];
                helper.assertTrue(crabPart.getBoundingBox().getXsize() > 0.02D, "the crab's first part has no box");
            } finally {
                frogAdult.discard();
                frogBaby.discard();
                birdAdult.discard();
                birdBaby.discard();
                crab.discard();
            }
            helper.succeed();
        });
    }

    /** Mothra's four hand-placed OreSpawnPartEntity boxes are gone; its MHLib parts take full damage through a wing. */
    @GameTest(template = "empty_large", timeoutTicks = 80, batch = "hitboxProfilesAll")
    public static void s173e_mothra_has_no_hand_placed_parts(GameTestHelper helper) {
        Mothra mothra = (Mothra) spawnFrozen(helper, ModEntities.MOTHRA.get());
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                PartEntity<?>[] parts = mothra.getParts();
                helper.assertTrue(parts != null && parts.length > 0, "Mothra has no parts");
                MHLibPartEntity<?> wing = null;
                for (PartEntity<?> part : parts) {
                    helper.assertTrue(!(part instanceof OreSpawnPartEntity<?>), "Mothra still carries a hand-placed part: " + part);
                    helper.assertTrue(part instanceof MHLibPartEntity<?>, "Mothra part is not an MHLibPartEntity: " + part);
                    if (((MHLibPartEntity<?>) part).getConfigName().toLowerCase().contains("wing")) {
                        wing = (MHLibPartEntity<?>) part;
                    }
                }
                helper.assertTrue(wing != null, "Mothra has no wing part");
                helper.assertTrue(!mothra.isPickable(), "Mothra's root box is still pickable");
                float before = mothra.getHealth();
                boolean hit = wing.hurt(helper.getLevel().damageSources().magic(), 8.0F);
                helper.assertTrue(hit && Math.abs((before - mothra.getHealth()) - 8.0F) < 0.01F,
                        "8 through Mothra's wing " + wing.getConfigName() + " took " + (before - mothra.getHealth()) + " (hit=" + hit + "); the port's old 0.25x+1 wing scheme is retired");
            } finally {
                mothra.discard();
            }
            helper.succeed();
        });
    }

    /**
     * A vanilla arrow that strikes a part lands on the creature for everything but the damage (the vendored
     * MixinAbstractArrow, the MixinPlayer pattern): a burning arrow dropped onto a Frog hits its one part, the Frog
     * takes the damage, catches fire and counts the arrow; without the mixin the part would burn and count it.
     */
    @GameTest(template = "empty_large", timeoutTicks = 100, batch = "hitboxProfilesAll")
    public static void s173f_an_arrow_through_a_part_lands_on_the_creature(GameTestHelper helper) {
        Mob frog = spawnFrozen(helper, ModEntities.FROG.get());
        frog.setNoGravity(true);
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            // the arrow can only meet the frog through its part: the body box is not pickable, the one part is
            helper.assertTrue(!frog.isPickable() && !frog.canBeHitByProjectile(), "the frog's body box is pickable: the arrow could land on it, not on a part");
            helper.assertTrue(frog.getParts() != null && frog.getParts().length == 1 && frog.getParts()[0].canBeHitByProjectile(),
                    "the frog does not offer exactly one pickable part");
            float before = frog.getHealth();
            net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(helper.getLevel(),
                    frog.getX(), frog.getY() + 3.0D, frog.getZ(), net.minecraft.world.item.ItemStack.EMPTY, null);
            arrow.setBaseDamage(2.0D);
            arrow.igniteForSeconds(100.0F);
            arrow.setNoGravity(true);
            arrow.setDeltaMovement(0.0D, -0.5D, 0.0D);
            helper.getLevel().addFreshEntity(arrow);
            helper.runAfterDelay(30, () -> {
                try {
                    helper.assertTrue(arrow.isRemoved() || arrow.getY() < frog.getY() - 1.0D || frog.getHealth() < before,
                            "the arrow never met the frog (arrow y " + arrow.getY() + ", frog y " + frog.getY() + ")");
                    helper.assertTrue(frog.getHealth() < before, "the frog took no damage from the arrow (health " + frog.getHealth() + " of " + before + ")");
                    helper.assertTrue(frog.getRemainingFireTicks() > 0, "the burning arrow did not set the frog on fire: the fire landed on the part");
                    helper.assertTrue(frog.getArrowCount() == 1, "the frog counts " + frog.getArrowCount() + " arrows, expected 1: the count landed on the part");
                } finally {
                    frog.discard();
                    arrow.discard();
                }
                helper.succeed();
            });
        });
    }

    /**
     * A part stands for its creature where the game asks the entity itself: {@code MyUtils.behindPart} and the
     * predicates read the creature; the part's root vehicle is the creature's (a rider is a passenger of the same
     * vehicle as its mount's wing, so the rider's arrows spare it; a creature is its own parts' root, so its own volleys
     * do); pick-block on a part answers the creature's spawn egg; fire set on a part burns the creature, never the part.
     */
    @GameTest(template = "empty_large", timeoutTicks = 60, batch = "hitboxProfilesAll")
    public static void s173h_a_part_stands_for_its_creature(GameTestHelper helper) {
        Mob dragon = spawnFrozen(helper, ModEntities.DRAGON.get());
        Mob king = spawnFrozen(helper, ModEntities.THE_KING.get());
        net.minecraft.world.entity.player.Player rider = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.runAfterDelay(ALIGN_TICKS, () -> {
            try {
                PartEntity<?> wing = dragon.getParts()[0];
                helper.assertTrue(danger.orespawn.util.MyUtils.behindPart(wing) == dragon, "behindPart does not return the dragon for its part");
                helper.assertTrue(danger.orespawn.util.MyUtils.isRoyalty(king.getParts()[0]) && !danger.orespawn.util.MyUtils.isRoyalty(wing),
                        "the royalty predicate does not read the creature behind a part");
                helper.assertTrue(wing.getRootVehicle() == dragon, "a part's root vehicle is not its creature: " + wing.getRootVehicle());
                helper.assertTrue(dragon.isPassengerOfSameVehicle(wing), "a creature does not count as the same vehicle as its own part");
                rider.startRiding(dragon, true);
                helper.assertTrue(rider.getVehicle() == dragon, "the rider did not mount the dragon");
                helper.assertTrue(rider.isPassengerOfSameVehicle(wing), "the rider is not a passenger of the same vehicle as the mount's part: its arrows would hit the mount");
                rider.stopRiding();
                net.minecraft.world.item.ItemStack pick = wing.getPickResult();
                helper.assertTrue(pick != null && !pick.isEmpty() && pick.getItem() instanceof net.minecraft.world.item.SpawnEggItem,
                        "pick-block on a part does not answer the creature's spawn egg: " + pick);
                helper.assertTrue(dragon.getRemainingFireTicks() <= 0, "the dragon burns before the test");
                wing.igniteForSeconds(4.0F);
                helper.assertTrue(dragon.getRemainingFireTicks() >= 60 && wing.getRemainingFireTicks() <= 0,
                        "fire set on a part did not land on the creature (creature " + dragon.getRemainingFireTicks() + ", part " + wing.getRemainingFireTicks() + ")");
                dragon.clearFire();
            } finally {
                rider.discard();
                dragon.discard();
                king.discard();
            }
            helper.succeed();
        });
    }
}
