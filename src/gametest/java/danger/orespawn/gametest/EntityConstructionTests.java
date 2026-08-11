package danger.orespawn.gametest;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * BUG-034 regression net (beta.3): constructs every registered orespawn
 * entity type once and discards it. A throw in a constructor or in
 * registerGoals (beta.2's DungeonBeast: the TF-026 Params guard tripping on
 * a bad constant) doesn't fail loudly in play — NaturalSpawner catches it,
 * logs "Failed to create mob" forever, and the mob is silently absent from
 * the game. Nothing else in the suite instantiates ALL types, which is how
 * that shipped; this test makes the next one a red gate instead.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EntityConstructionTests {

    @GameTest(template = "empty")
    public void every_entity_type_constructs(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        int constructed = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!OreSpawnMod.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            try {
                Entity entity = type.create(helper.getLevel());
                if (entity == null) {
                    // create() returns null only for factory-less types
                    // (vanilla PLAYER/FISHING_BOBBER pattern); we register
                    // none, so null is a failure until proven otherwise.
                    failures.add(id + " (create returned null)");
                } else {
                    entity.discard();
                    ++constructed;
                }
            } catch (Throwable t) {
                failures.add(id + " (" + t + ")");
            }
        }
        if (!failures.isEmpty()) {
            helper.fail("entity types failed to construct: " + String.join("; ", failures));
            return;
        }
        if (constructed == 0) {
            helper.fail("no orespawn entity types found in the registry");
            return;
        }
        helper.succeed();
    }
}
