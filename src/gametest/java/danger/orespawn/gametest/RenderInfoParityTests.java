package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.client.RenderInfo;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-093: per-entity render scratch ({@link RenderInfo}) restored as the
 * originals had it. Each 1.7.10 entity owned one {@code renderdata = new
 * RenderInfo()} (e.g. orig Alien.java:42), zeroed all eight fields in
 * entityInit (orig Alien.java:79-89) and handed the same instance back from
 * {@code getRenderInfo()} (orig Alien.java:105-107); the model mutated that
 * instance client-side so the latch persisted per entity between frames.
 * The port had collapsed this into shared model-level state, so every
 * rendered individual of a species shared one claw/jaw/tail latch.
 *
 * <p>One method per species (the vanilla {@code @GameTest} idiom in this tree
 * is not parameterised); each delegates to {@link #pinRenderInfo}, which pins
 * (a) two spawned entities return distinct, non-null instances, and the same
 * instance across calls on one entity; (b) an int written on one instance
 * leaves the other at 0, and survives five ticks (nothing in the originals'
 * onLivingUpdate touches renderdata); (c) a fresh spawn is all-zero, mirroring
 * the entityInit reset. Kraken (the pattern precedent) is run as a control.
 *
 * <p>The models cannot be loaded in the dedicated gametest server (client
 * classes are stripped), so the formula divergences themselves are covered by
 * the two-refuter transcription review, not here. Render scale is ENT-S-092.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
// Own batch (TEST-003): adding tests to the default batch reshuffles its 50-test buckets and
// flips order-sensitive neighbours (bug003 rat despawn roll, dsb_item020 spawner outcome); an
// isolated batch keeps the default buckets exactly as they were before ENT-S-093.
public class RenderInfoParityTests {

    private static final BlockPos POS_A = new BlockPos(16, 8, 16);
    private static final BlockPos POS_B = new BlockPos(32, 8, 32);
    private static final int SENTINEL = 7;
    private static final int SURVIVE_TICKS = 5;

    /**
     * Shared checker. {@code accessor} is the species' installed
     * {@code getRenderInfo()}; {@code citation} names the orig field, entityInit
     * zeroing and accessor lines for the failure message.
     */
    private static <T extends Mob> void pinRenderInfo(GameTestHelper helper, EntityType<T> type,
            Function<T, RenderInfo> accessor, String species, String citation) {
        T a = helper.spawnWithNoFreeWill(type, POS_A);
        T b = helper.spawnWithNoFreeWill(type, POS_B);
        a.setNoAi(true);
        b.setNoAi(true);
        a.setPersistenceRequired();
        b.setPersistenceRequired();

        RenderInfo ra = accessor.apply(a);
        RenderInfo rb = accessor.apply(b);

        // (a) distinct, non-null, and stable per entity.
        helper.assertTrue(ra != null && rb != null,
                species + " getRenderInfo() returned null (" + citation + ", ENT-S-093)");
        helper.assertTrue(ra != rb,
                species + " two entities share one RenderInfo instance; the original owned one per entity ("
                        + citation + ", ENT-S-093)");
        helper.assertTrue(ra == accessor.apply(a) && rb == accessor.apply(b),
                species + " getRenderInfo() must return the same instance across calls so the model's latch persists ("
                        + citation + ", ENT-S-093)");

        // (c) fresh spawn is all-zero (mirrors the original entityInit reset).
        helper.assertTrue(isAllZero(ra) && isAllZero(rb),
                species + " fresh RenderInfo not all-zero: a=" + describe(ra) + " b=" + describe(rb) + " ("
                        + citation + ", ENT-S-093)");

        // (b) per-entity state is independent: write a's ri1, b stays 0.
        ra.ri1 = SENTINEL;
        helper.assertTrue(rb.ri1 == 0 && ra.ri1 == SENTINEL,
                species + " writing ri1 on one entity leaked to the other: a.ri1=" + ra.ri1 + " b.ri1=" + rb.ri1 + " ("
                        + citation + ", ENT-S-093)");

        helper.runAfterDelay(SURVIVE_TICKS, () -> {
            RenderInfo ra2 = accessor.apply(a);
            RenderInfo rb2 = accessor.apply(b);
            helper.assertTrue(ra2 == ra && rb2 == rb,
                    species + " RenderInfo instance replaced by ticking (" + citation + ", ENT-S-093)");
            helper.assertTrue(ra2.ri1 == SENTINEL && rb2.ri1 == 0,
                    species + " per-entity ri1 did not survive " + SURVIVE_TICKS + " ticks: a.ri1=" + ra2.ri1
                            + " b.ri1=" + rb2.ri1 + " (" + citation + ", ENT-S-093)");
            helper.succeed();
        });
    }

    private static boolean isAllZero(RenderInfo r) {
        return r.ri1 == 0 && r.ri2 == 0 && r.ri3 == 0 && r.ri4 == 0
                && r.rf1 == 0.0f && r.rf2 == 0.0f && r.rf3 == 0.0f && r.rf4 == 0.0f;
    }

    private static String describe(RenderInfo r) {
        return "[ri " + r.ri1 + "," + r.ri2 + "," + r.ri3 + "," + r.ri4
                + " rf " + r.rf1 + "," + r.rf2 + "," + r.rf3 + "," + r.rf4 + "]";
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_alien_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.ALIEN.get(), danger.orespawn.entity.Alien::getRenderInfo,
                "Alien", "orig Alien.java:42 field, :79-89 entityInit zero, :105-107 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_cephadrome_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.CEPHADROME.get(), danger.orespawn.entity.Cephadrome::getRenderInfo,
                "Cephadrome", "orig Cephadrome.java:62 field, :139-149 entityInit zero, :156-158 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_emperor_scorpion_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.ENTITY_EMPEROR_SCORPION.get(),
                danger.orespawn.entity.EntityEmperorScorpion::getRenderInfo,
                "EmperorScorpion", "orig EmperorScorpion.java:53 field, :77-87 entityInit zero, :110-112 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_ghost_skelly_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.GHOST_SKELLY.get(), danger.orespawn.entity.GhostSkelly::getRenderInfo,
                "GhostSkelly", "orig GhostSkelly.java:22 field, :42-52 entityInit zero, :55-57 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_leon_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.ENTITY_LEON.get(), danger.orespawn.entity.EntityLeon::getRenderInfo,
                "Leon", "orig Leon.java:64 field, :155-165 entityInit zero, :176-178 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_lurking_terror_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.ENTITY_LURKING_TERROR.get(),
                danger.orespawn.entity.EntityLurkingTerror::getRenderInfo,
                "LurkingTerror", "orig LurkingTerror.java:49 field, :71-81 entityInit zero, :99-101 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_cave_fisher_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.CAVE_FISHER.get(), danger.orespawn.entity.CaveFisher::getRenderInfo,
                "CaveFisher", "orig CaveFisher.java:39 field, :68-78 entityInit zero, :94-96 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_dragon_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.DRAGON.get(), danger.orespawn.entity.Dragon::getRenderInfo,
                "Dragon", "orig Dragon.java:80 field, :178-188 entityInit zero, :199-201 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_dungeon_beast_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.DUNGEON_BEAST.get(), danger.orespawn.entity.DungeonBeast::getRenderInfo,
                "DungeonBeast", "orig DungeonBeast.java:43 field, :72-82 entityInit zero, :98-100 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_nastysaurus_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.NASTYSAURUS.get(), danger.orespawn.entity.Nastysaurus::getRenderInfo,
                "Nastysaurus", "orig Nastysaurus.java:43 field, :71-81 entityInit zero, :84-86 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_pitch_black_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.PITCH_BLACK.get(), danger.orespawn.entity.PitchBlack::getRenderInfo,
                "PitchBlack", "orig PitchBlack.java:55 field, :88-98 entityInit zero, :193-195 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_prince_teen_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.THE_PRINCE_TEEN.get(), danger.orespawn.entity.ThePrinceTeen::getRenderInfo,
                "ThePrinceTeen", "orig ThePrinceTeen.java:80 field, :216-226 entityInit zero, :237-239 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_ostrich_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.OSTRICH.get(), danger.orespawn.entity.Ostrich::getRenderInfo,
                "Ostrich", "orig Ostrich.java:45 field, :92-102 entityInit zero, :105-107 getRenderInfo");
    }

    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_scorpion_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.ENTITY_SCORPION.get(), danger.orespawn.entity.EntityScorpion::getRenderInfo,
                "Scorpion", "orig Scorpion.java:45 field, :75-85 entityInit zero, :101-103 getRenderInfo");
    }

    /** Control: the Kraken precedent the fourteen species were ported against. */
    @GameTest(template = "empty_large", batch = "renderInfoParity")
    public static void s093_kraken_control_renderinfo_per_entity(GameTestHelper helper) {
        pinRenderInfo(helper, ModEntities.KRAKEN.get(), danger.orespawn.entity.Kraken::getRenderInfo,
                "Kraken", "orig Kraken.java:58 field, :123-125 getRenderInfo (precedent)");
    }
}
