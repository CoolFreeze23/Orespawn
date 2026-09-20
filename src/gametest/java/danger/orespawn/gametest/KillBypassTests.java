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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-172 — {@code /kill} kills everything of OreSpawn's.
 *
 * <p>{@code /kill} is {@code Entity.kill()}: for a living entity {@code hurt(genericKill, Float.MAX_VALUE)}, a source in
 * {@code DamageTypeTags.BYPASSES_INVULNERABILITY}. Twenty-three of the port's damage overrides capped the amount (the
 * King and the Queen at 750, Godzilla at 750, the Boyfriend, the Girlfriend, the Purple Power, the Velocity Raptor and
 * the Hydrolisc at 10, the tamed Gazelle at 10), refused it inside a hit cooldown (the King, the Queen, Godzilla, the
 * Kraken, the Basilisk, the Cephadrome, the Emperor Scorpion, the Hercules Beetle, the Leon, the Spit Bug, the Trooper
 * Bug, the Prince's teen and adult forms, the Dragon) or forwarded it to a body that capped it (the three head
 * sidecars), so the command left them standing. Each of them now lets a bypassing source through to
 * {@code super.hurt} first, the contract every vanilla boss keeps (EnderDragon, Wither).</p>
 *
 * <p>Everything is built with {@code EntityType.create} and added a block above the floor at the centre of
 * {@code empty_large} (48x16x48), so a death spawn (the King's Prince at +10) stays inside the bounds for the
 * cleanup.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class KillBypassTests {

    private static final BlockPos CENTRE = new BlockPos(24, 1, 24);

    /** Every registered OreSpawn entity type, living or not: created, added, killed, gone. */
    @GameTest(template = "empty_large", timeoutTicks = 200)
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
            Entity entity = type.create(level);
            helper.assertTrue(entity != null, key + " could not be created");
            entity.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            }
            level.addFreshEntity(entity);
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
     * The gate order: a boss hit a moment ago is inside its own hit cooldown (the King's 20 ticks, orig TheKing.java
     * attackEntityFrom; the Kraken's 30, ENT-K-002), which refused every source before the fix - {@code /kill} during a
     * fight did nothing. The bypass is read before the cooldown and before the cap.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public static void s172b_a_boss_inside_its_hit_cooldown_still_dies_to_kill(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(CENTRE));
        List<EntityType<? extends LivingEntity>> bosses = List.of(
                ModEntities.THE_KING.get(), ModEntities.THE_QUEEN.get(), ModEntities.GODZILLA.get(), ModEntities.KRAKEN.get());
        for (EntityType<? extends LivingEntity> type : bosses) {
            LivingEntity boss = type.create(level);
            helper.assertTrue(boss != null, type.toShortString() + " could not be created");
            boss.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
            if (boss instanceof Mob mob) {
                mob.setNoAi(true);
            }
            level.addFreshEntity(boss);
            boss.hurt(level.damageSources().generic(), 1.0F);
            helper.assertTrue(!boss.isDeadOrDying(), type.toShortString() + " died of a 1-point hit");
            boss.kill();
            helper.assertTrue(gone(boss), type.toShortString() + " inside its hit cooldown survives /kill (ENT-S-172)");
            if (!boss.isRemoved()) {
                boss.discard();
            }
        }
        helper.killAllEntities();
        helper.succeed();
    }

    private static boolean gone(Entity entity) {
        return entity.isRemoved() || (entity instanceof LivingEntity living && living.isDeadOrDying());
    }
}
