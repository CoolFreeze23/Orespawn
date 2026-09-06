package danger.orespawn.gametest;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.entity.ThePrinceTeen;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * ENT-S-156 (2026-09-06, wave 5): ThePrinceTeen's movement-speed base after construction is the registered 0.32
 * (orig ThePrinceTeen.java:87 {@code moveSpeed = 0.32f}, :139 {@code setBaseValue(moveSpeed)}; BOSS-026's
 * createAttributes 0.32). HEAD's constructor re-applied a pre-fix {@code 0.35f} field over it (the OPT-009
 * conversion of the old per-tick write): 0.3499999940395355 read back, 1.094x the pace on every goal. The field and
 * the write are gone; nothing else wrote the attribute. Own batch (TEST-003: a new default-batch method would shift
 * the bucket order).
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PrinceTeenSpeedParityTests {

    @GameTest(template = "empty", batch = "princeTeenSpeedParity")
    public void s156_01_prince_teen_movement_speed_base_is_0_32(GameTestHelper helper) {
        ThePrinceTeen teen = helper.spawnWithNoFreeWill(ModEntities.THE_PRINCE_TEEN.get(), new BlockPos(2, 1, 2));
        try {
            double base = teen.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
            helper.assertTrue(base == 0.32D, "ENT-S-156: ThePrinceTeen's MOVEMENT_SPEED base after construction must be the registered"
                    + " 0.32 (orig ThePrinceTeen.java:87; BOSS-026), not the pre-fix 0.35f the OPT-009 constructor assert re-applied"
                    + " (0.3499999940395355); actual " + base);
        } finally {
            teen.discard();
        }
        helper.succeed();
    }
}
