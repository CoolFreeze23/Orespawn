package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.client.DevRendererSwitch;
import danger.orespawn.client.DevRendererSwitch.Variant;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Pins the Phase G renderer switch contract: classic unless the all-species token or the species is listed. */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PhaseGDevSwitchTests {

    @GameTest(template = "empty")
    public static void phaseg001_beaver_renderer_switch_defaults_to_classic(GameTestHelper helper) {
        helper.assertTrue(DevRendererSwitch.resolve(null, "beaver") == Variant.CLASSIC, "no property must select classic");
        helper.assertTrue(DevRendererSwitch.resolve("", "beaver") == Variant.CLASSIC, "empty property must select classic");
        helper.assertTrue(DevRendererSwitch.resolve("true", "beaver") == Variant.CLASSIC, "'true' is not the candidate token");
        helper.assertTrue(DevRendererSwitch.resolve("Candidate", "beaver") == Variant.CLASSIC, "the all-species token is case-exact");
        helper.assertTrue(DevRendererSwitch.resolve("candidate", "beaver") == Variant.CANDIDATE, "exact token selects every species");
        helper.assertTrue(DevRendererSwitch.resolve("candidate", "elevator") == Variant.CANDIDATE, "exact token selects every species");
        helper.assertTrue(DevRendererSwitch.resolve("beaver,elevator", "elevator") == Variant.CANDIDATE, "listed species is selected");
        helper.assertTrue(DevRendererSwitch.resolve(" Beaver , elevator ", "beaver") == Variant.CANDIDATE, "list tokens are trimmed and case-insensitive");
        helper.assertTrue(DevRendererSwitch.resolve("beaver", "elevator") == Variant.CLASSIC, "unlisted species stays classic");
        helper.assertTrue(DevRendererSwitch.resolve(null, null, "beaver") == Variant.CLASSIC, "neither property: classic");
        helper.assertTrue(DevRendererSwitch.resolve(null, "candidate", "elevator") == Variant.CANDIDATE, "the original Beaver property still works, for every species");
        helper.assertTrue(DevRendererSwitch.resolve("elevator", "beaver", "beaver") == Variant.CANDIDATE, "either property may list the species");
        helper.succeed();
    }
}
