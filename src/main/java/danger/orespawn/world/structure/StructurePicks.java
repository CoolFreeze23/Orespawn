package danger.orespawn.world.structure;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * OreSpawn's one-pick structure sets (WGEN-080).
 *
 * <p>1.7.10 placed OreSpawn's structures from rolls its world generator made in every chunk as the chunk populated
 * (orig OreSpawnWorld.java:30-328): Mining's {@code nextInt(95) == 1} and then {@code nextInt(7)} for the builder, the
 * Islands' {@code nextInt(100) == 0} and {@code nextInt(19)}, the overworld's six-way pick and its chain of seven, the
 * Village's three, the End's four. One roll picked one builder, and a builder that found no site built nothing; no
 * other builder stepped in. Behind the rolls stood the {@code recently_placed} cooldown: every build blocked them for
 * the next 49 chunks the populator ran, so the dimension's structures kept apart from each other whatever their kind,
 * while two of the same kind could still stand close.
 *
 * <p>A 1.21 structure set of one structure each (the port's placement until beta.12) spaced every kind on a grid of its
 * own: no two of a kind ever close, any two kinds as close as chance put them, and each kind's grid lined up against
 * the next. Each dimension's rolls are therefore one set now, as the original's rolls were one roll: one spot for
 * every few chunks, the spacing chosen so that the spots keep the dimension's structures apart about as the cooldown
 * did, and each spot's structure picked by weight at the original's odds, the cooldown's share of the roll counted.
 * The weight of {@link NothingStructure} is the share of spots the original's roll left empty.
 *
 * <p>Vanilla's pick is not final: when the picked structure finds no site, {@code ChunkGenerator.createStructures}
 * tries the set's other structures in turn, which the original never did. For the sets listed here the structure pass
 * therefore skips every structure but the first pick ({@code StructurePickMixin}); a picked structure that finds no
 * site leaves the spot empty, as the original's builder did. Commands and structure searches are left alone: a search
 * that stops at a spot where another structure was picked finds no start there when the chunk loads and moves on.
 */
public final class StructurePicks {

    /** The sets whose first pick is final. */
    public static final List<ResourceKey<StructureSet>> ONE_PICK_SETS = List.of(
            key("overworld_pool"), key("overworld_chain"), key("mining_structures"), key("village_structures"),
            key("end_structures"), key("islands_structures"), key("cloud_shark_dungeon"));

    private StructurePicks() {
    }

    private static ResourceKey<StructureSet> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath("orespawn", path));
    }

    /**
     * The structure vanilla tries first in {@code chunk} for a set of several structures, drawn as
     * {@code ChunkGenerator.createStructures} draws it: a {@code LegacyRandomSource(0)} given
     * {@code setLargeFeatureSeed(seed, x, z)}, then {@code nextInt} of the total weight, walked through the entries in
     * the set's order.
     */
    public static StructureSet.StructureSelectionEntry firstPick(StructureSet set, long seed, ChunkPos chunk) {
        List<StructureSet.StructureSelectionEntry> entries = set.structures();
        int total = 0;
        for (StructureSet.StructureSelectionEntry entry : entries) {
            total += entry.weight();
        }
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureSeed(seed, chunk.x, chunk.z);
        int draw = random.nextInt(total);
        for (StructureSet.StructureSelectionEntry entry : entries) {
            draw -= entry.weight();
            if (draw < 0) return entry;
        }
        return entries.get(entries.size() - 1);
    }

    /**
     * Whether the structure pass passes {@code entry} over in {@code chunk}: it belongs to one of the
     * {@link #ONE_PICK_SETS} and is not that set's first pick there.
     */
    public static boolean passedOver(RegistryAccess access, StructureSet.StructureSelectionEntry entry, long seed,
                                     ChunkPos chunk) {
        Registry<StructureSet> sets = access.registryOrThrow(Registries.STRUCTURE_SET);
        for (ResourceKey<StructureSet> key : ONE_PICK_SETS) {
            StructureSet set = sets.get(key);
            if (set == null || !set.structures().contains(entry)) continue;
            return !firstPick(set, seed, chunk).equals(entry);
        }
        return false;
    }
}
