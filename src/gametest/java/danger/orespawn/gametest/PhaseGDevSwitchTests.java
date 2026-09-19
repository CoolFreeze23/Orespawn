package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import danger.orespawn.client.DevRendererSwitch;
import danger.orespawn.client.DevRendererSwitch.Variant;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Pins the Phase G renderer switch contract ON THE BRANCH {@code default-flip}: the candidate unless the all-species
 * token {@code classic} or the species is listed under {@code -Dorespawn.dev.classicRenderers} (or the original
 * Beaver property, the same grammar). Master's row pinned the classic default with these twelve
 * assertions; this row is its inversion, assertion for assertion - re-pinned because the default it pins is the
 * decision's, not a tolerance.
 *
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class PhaseGDevSwitchTests {

    @GameTest(template = "empty")
    public static void phaseg001_renderer_switch_defaults_to_the_candidate_on_the_branch(GameTestHelper helper) {
        helper.assertTrue(DevRendererSwitch.resolve(null, "beaver") == Variant.CANDIDATE, "no property must select the candidate (the branch default)");
        helper.assertTrue(DevRendererSwitch.resolve("", "beaver") == Variant.CANDIDATE, "empty property must select the candidate");
        helper.assertTrue(DevRendererSwitch.resolve("true", "beaver") == Variant.CANDIDATE, "'true' is not the classic token");
        helper.assertTrue(DevRendererSwitch.resolve("Classic", "beaver") == Variant.CANDIDATE, "the all-species token is case-exact");
        helper.assertTrue(DevRendererSwitch.resolve("classic", "beaver") == Variant.CLASSIC, "exact token keeps every species classic");
        helper.assertTrue(DevRendererSwitch.resolve("classic", "elevator") == Variant.CLASSIC, "exact token keeps every species classic");
        helper.assertTrue(DevRendererSwitch.resolve("beaver,elevator", "elevator") == Variant.CLASSIC, "listed species keeps its classic renderer");
        helper.assertTrue(DevRendererSwitch.resolve(" Beaver , elevator ", "beaver") == Variant.CLASSIC, "list tokens are trimmed and case-insensitive");
        helper.assertTrue(DevRendererSwitch.resolve("beaver", "elevator") == Variant.CANDIDATE, "unlisted species draws with its candidate");
        helper.assertTrue(DevRendererSwitch.resolve(null, null, "beaver") == Variant.CANDIDATE, "neither property: the candidate");
        helper.assertTrue(DevRendererSwitch.resolve(null, "classic", "elevator") == Variant.CLASSIC, "the original Beaver property still works, for every species");
        helper.assertTrue(DevRendererSwitch.resolve("elevator", "beaver", "beaver") == Variant.CLASSIC, "either property may list the species");
        helper.succeed();
    }
}
