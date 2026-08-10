package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Scaffolding smoke test (2026-08-10): proves the gametest source set,
 * empty structure template, and the {@code runGameTestServer} task work
 * before any real tests are written. Asserts nothing about the mod.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SmokeTests {

    // Template names here are paths within the orespawn namespace — the
    // @GameTestHolder value supplies the namespace ("empty" ->
    // orespawn:empty -> data/orespawn/structure/empty.nbt);
    // @PrefixGameTestTemplate(false) removes only the class-name prefix.
    @GameTest(template = "empty")
    public void scaffolding_smoke(GameTestHelper helper) {
        helper.succeed();
    }
}
