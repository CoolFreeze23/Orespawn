package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Vanilla-mob parity guards (batch categories: parity).
 *
 * <p>BUG-036: the vendored MultiHitboxLib's upstream DEMO profile
 * ({@code data/minecraft/multihitboxlib/hitbox_profiles/creeper.json})
 * rode into the shipped jar, silently giving every vanilla creeper
 * multipart hitboxes: an UNPICKABLE main box (profile
 * {@code main-hitbox.canReceiveDamage=false} feeds
 * {@code IMultipartEntity.mhLibIsPickable}) plus feet/body/head part
 * surfaces with 0.5/1.0/2.0 damage modifiers — live in public beta.2/3.
 * The fix is deleting the stowaway file; this test pins the contract
 * that NO vanilla mob carries MHLib parts so a future vendoring update
 * cannot smuggle a demo profile back in.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class VanillaParityTests {

    /**
     * A vanilla creeper must have no multipart surfaces and must be
     * directly pickable — exactly vanilla behavior (BUG-036).
     */
    @GameTest(template = "empty")
    public void bug036_vanilla_creeper_has_no_mhlib_parts(GameTestHelper helper) {
        Creeper creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new BlockPos(2, 2, 2));
        helper.assertTrue(creeper.getParts() == null || creeper.getParts().length == 0,
                "vanilla creeper carries MHLib part entities (BUG-036 stowaway profile)");
        helper.assertFalse(creeper.isMultipartEntity(),
                "vanilla creeper reports itself multipart (BUG-036)");
        helper.assertTrue(creeper.isPickable(),
                "vanilla creeper is not directly pickable (BUG-036: profile main-hitbox "
                        + "canReceiveDamage=false leaked into mhLibIsPickable)");
        creeper.discard();
        helper.succeed();
    }
}
