package danger.orespawn.gametest;

import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MOD-042: the dog texture. The armour's texture hook decides on the client (ArmourModelProbe checks its rule at build
 * time, where no config is loaded); here the master-and-key reading the hook asks for, as every [modern] key's.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DogArmourTests {

    /** dogArmour() is true only while modern.enabled and modern.dogArmour are both on; both are on by default. */
    @GameTest(template = "empty")
    public static void mod042a_the_dog_texture_reads_master_and_key(GameTestHelper helper) {
        final boolean master = OreSpawnConfig.MODERN_ENABLED.get();
        final boolean key = OreSpawnConfig.MODERN_DOG_ARMOUR.get();
        boolean on, keyOff, masterOff;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_DOG_ARMOUR.set(true);
            on = OreSpawnConfig.dogArmour();
            OreSpawnConfig.MODERN_DOG_ARMOUR.set(false);
            keyOff = OreSpawnConfig.dogArmour();
            OreSpawnConfig.MODERN_DOG_ARMOUR.set(true);
            OreSpawnConfig.MODERN_ENABLED.set(false);
            masterOff = OreSpawnConfig.dogArmour();
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.MODERN_DOG_ARMOUR.set(key);
        }
        helper.assertTrue(on && !keyOff && !masterOff,
                "dogArmour() reads master and key: on " + on + ", key off " + keyOff + ", master off " + masterOff);
        helper.assertTrue(master && key, "the defaults are expected on: modern.enabled " + master + ", modern.dogArmour " + key);
        helper.succeed();
    }
}
