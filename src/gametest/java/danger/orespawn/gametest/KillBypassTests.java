package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.List;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-172 — {@code /kill} kills everything of OreSpawn's.
 *
 * <p>{@code /kill} is {@code Entity.kill()}: {@code remove(KILLED)} for a non-living entity, for a living one
 * {@code hurt(genericKill, Float.MAX_VALUE)} - a source in {@code DamageTypeTags.BYPASSES_INVULNERABILITY}, the tag
 * (with {@code out_of_world}, the void) that vanilla's own gates yield to: {@code Entity.isInvulnerableTo}'s
 * Invulnerable flag, the totem's {@code checkTotemDeathProtection}, {@code WitherBoss.hurt}'s spawn-armour gate (the
 * Ender Dragon answers {@code /kill} in its own {@code kill()} override). Thirty of the port's damage overrides,
 * transcribed from 1.7.10 {@code attackEntityFrom} bodies (whose {@code /kill} could not name a mob, and whose void
 * source ran through the same timers), capped the amount (the King, the Queen and Godzilla at 750; the Boyfriend, the
 * Girlfriend, the Purple Power, the Velocity Raptor and the Hydrolisc at 10, the tamed Gazelle at 10), refused it
 * inside their own hit window (the King, the Queen, Godzilla, the Kraken, the Basilisk, the Cephadrome, the Emperor
 * Scorpion, the Hercules Beetle, the Leon, the Spit Bug, the Trooper Bug, the Prince's teen and adult forms, the
 * Dragon, the Nightmare, the Robot 4, the Water Dragon, the Crab, the Sea Monster, the Sea Viper), forwarded it to a
 * body that capped it (the three head sidecars) or refused an attacker-less hit while ridden (the hoverboard). Each
 * now lets a bypassing source through first.</p>
 *
 * <p>Everything is built with {@code EntityType.create} and added a block above the floor at the centre of
 * {@code empty_large} (48x16x48), so a death spawn (the King's Prince at +10) stays inside the bounds for the
 * cleanup.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KillBypassTests {

    private static final BlockPos CENTRE = new BlockPos(24, 1, 24);

    /** Every registered OreSpawn entity type, living or not: created, added, killed, gone (the caps and the forwarders). */
    @GameTest(template = "empty_large")
    public static void s172a_kill_kills_every_orespawn_entity(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(CENTRE));
        List<String> survivors = new ArrayList<>();
        int checked = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = EntityType.getKey(type);
            if (!OreSpawnMod.MOD_ID.equals(key.getNamespace())) {
                continue;
            }
            Entity entity = place(level, type, at);
            entity.kill();
            checked++;
            if (!gone(entity)) {
                survivors.add(key.getPath());
            }
            if (!entity.isRemoved()) {
                entity.discard();
            }
        }
        helper.assertTrue(checked > 100, "only " + checked + " OreSpawn entity types were checked");
        helper.assertTrue(survivors.isEmpty(), survivors.size() + " of OreSpawn's entities survive /kill: " + survivors);
        helper.killAllEntities();
        helper.succeed();
    }

    /**
     * The gate order, over every living type: a 1-point generic hit first, so any species with a hit window (the
     * King's 20 ticks, the Kraken's 30, the Sea Viper's 5 ...) sits inside it - before the fix that window refused
     * every source, so {@code /kill} during a fight did nothing. The bypass is read before the window and the cap.
     */
    @GameTest(template = "empty_large")
    public static void s172b_every_living_type_inside_its_hit_window_still_dies_to_kill(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(CENTRE));
        List<String> survivors = new ArrayList<>();
        int checked = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = EntityType.getKey(type);
            if (!OreSpawnMod.MOD_ID.equals(key.getNamespace())) {
                continue;
            }
            Entity entity = place(level, type, at);
            if (!(entity instanceof LivingEntity living)) {
                entity.discard();
                continue;
            }
            living.hurt(level.damageSources().generic(), 1.0F);
            living.kill();
            checked++;
            if (!gone(living)) {
                survivors.add(key.getPath());
            }
            if (!living.isRemoved()) {
                living.discard();
            }
        }
        helper.assertTrue(checked > 100, "only " + checked + " living OreSpawn types were checked");
        helper.assertTrue(survivors.isEmpty(),
                survivors.size() + " of OreSpawn's living types survive /kill inside their hit window (ENT-S-172): " + survivors);
        helper.killAllEntities();
        helper.succeed();
    }

    /**
     * The conditional caps and the ridden board: a tamed Gazelle and a tamed Velocity Raptor cap a hit at 10 only
     * while tamed (orig Gazelle.java / VelocityRaptor.java attackEntityFrom); the hoverboard refused an attacker-less
     * hit while ridden (orig Elevator.java attackEntityFrom) - {@code generic_kill} carries no attacker.
     */
    @GameTest(template = "empty_large")
    public static void s172c_the_tamed_caps_and_the_ridden_hoverboard_die_to_kill(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(CENTRE));
        List<EntityType<? extends LivingEntity>> tameables = List.of(ModEntities.GAZELLE.get(), ModEntities.VELOCITY_RAPTOR.get());
        for (EntityType<? extends LivingEntity> type : tameables) {
            LivingEntity pet = (LivingEntity) place(level, type, at);
            helper.assertTrue(pet instanceof TamableAnimal, type.toShortString() + " is not tameable");
            ((TamableAnimal) pet).setTame(true, false);
            pet.setHealth(pet.getMaxHealth());
            pet.kill();
            helper.assertTrue(gone(pet), "a tamed " + type.toShortString() + " survives /kill (its tamed 10-point cap, ENT-S-172)");
            if (!pet.isRemoved()) {
                pet.discard();
            }
        }
        Entity board = place(level, ModEntities.ELEVATOR.get(), at);
        Pig rider = place(level, EntityType.PIG, at) instanceof Pig pig ? pig : null;
        helper.assertTrue(rider != null && rider.startRiding(board, true), "the pig could not board the hoverboard");
        helper.assertTrue(board.getFirstPassenger() == rider, "the hoverboard reports no rider");
        board.kill();
        helper.assertTrue(gone(board), "a ridden hoverboard survives /kill (ENT-S-172)");
        if (rider != null && !rider.isRemoved()) {
            rider.discard();
        }
        if (!board.isRemoved()) {
            board.discard();
        }
        helper.killAllEntities();
        helper.succeed();
    }

    private static Entity place(ServerLevel level, EntityType<?> type, Vec3 at) {
        Entity entity = type.create(level);
        if (entity == null) {
            throw new IllegalStateException(EntityType.getKey(type) + " could not be created");
        }
        entity.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        level.addFreshEntity(entity);
        return entity;
    }

    private static boolean gone(Entity entity) {
        return entity.isRemoved() || (entity instanceof LivingEntity living && living.isDeadOrDying());
    }
}
