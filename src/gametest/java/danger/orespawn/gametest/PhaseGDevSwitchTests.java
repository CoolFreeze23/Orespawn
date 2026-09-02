package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.client.DevRendererSwitch;
import danger.orespawn.client.DevRendererSwitch.Variant;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Pins the Phase G renderer switch contract: classic unless the exact candidate token is present. */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PhaseGDevSwitchTests {

    @GameTest(template = "empty")
    public static void phaseg001_beaver_renderer_switch_defaults_to_classic(GameTestHelper helper) {
        helper.assertTrue(DevRendererSwitch.resolve(null) == Variant.CLASSIC, "no property must select classic");
        helper.assertTrue(DevRendererSwitch.resolve("") == Variant.CLASSIC, "empty property must select classic");
        helper.assertTrue(DevRendererSwitch.resolve("true") == Variant.CLASSIC, "'true' is not the candidate token");
        helper.assertTrue(DevRendererSwitch.resolve("Candidate") == Variant.CLASSIC, "token is case-exact");
        helper.assertTrue(DevRendererSwitch.resolve("candidate") == Variant.CANDIDATE, "exact token selects the candidate");
        helper.succeed();
    }
}
