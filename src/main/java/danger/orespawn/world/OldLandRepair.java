package danger.orespawn.world;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Land generated in Utopia, the Village, Crystal and Mining when they still reached down to Y-64, made safe now that
 * they start at Y0 as the original's did. Such a chunk's blocks below Y0 can no longer be read (the game leaves them
 * out when it saves the chunk again), its floor at Y0 is whatever stood there, open to the void wherever a cave or water
 * crossed it, and its stored heights count from Y-64, 64 blocks too high. As it loads it gets the original's bedrock:
 * a floor at Y0 under every column, and the ragged layers up to Y4 in its stone; its heights are measured again, and it
 * is saved that way. A chunk saved since holds nothing below Y0 and is left alone, as is every other dimension.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public final class OldLandRepair {
    private static final Logger LOG = LoggerFactory.getLogger(OldLandRepair.class);
    private static final Set<ResourceKey<Level>> DIMENSIONS = Set.of(dimension("utopia"), dimension("village"),
            dimension("crystal"), dimension("mining"));
    private static final Map<ResourceKey<Level>, Boolean> REPORTED = new ConcurrentHashMap<>();

    private OldLandRepair() {
    }

    private static ResourceKey<Level> dimension(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, name));
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        ChunkAccess chunk = event.getChunk();
        if (!holdsBlocksOutside(event.getData(), chunk)) return;
        Level level = levelOf(chunk);
        if (level == null || !DIMENSIONS.contains(level.dimension())) return;
        repair(chunk);
        if (REPORTED.putIfAbsent(level.dimension(), Boolean.TRUE) == null) {
            LOG.info("{}: land saved when the dimension reached below Y0 (the first at chunk {}, {}) gets a bedrock "
                    + "floor and its heights measured again as it loads; its blocks below Y0 are gone",
                    level.dimension().location(), chunk.getPos().x, chunk.getPos().z);
        }
    }

    /** The level a chunk loads into: a whole chunk names it; a chunk still being generated was made with it. */
    private static Level levelOf(ChunkAccess chunk) {
        Level level = chunk.getLevel();
        if (level != null) return level;
        try {
            LevelHeightAccessor made = ObfuscationReflectionHelper.getPrivateValue(ChunkAccess.class, chunk,
                    "levelHeightAccessor");
            return made instanceof Level found ? found : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Whether a chunk's save holds blocks outside the height of the dimension it loads into. */
    public static boolean holdsBlocksOutside(CompoundTag saved, LevelHeightAccessor height) {
        ListTag sections = saved.getList("sections", Tag.TAG_COMPOUND);
        for (int i = 0; i < sections.size(); i++) {
            CompoundTag section = sections.getCompound(i);
            int y = section.getByte("Y");
            if (y >= height.getMinSection() && y < height.getMaxSection()) continue;
            ListTag palette = section.getCompound("block_states").getList("palette", Tag.TAG_COMPOUND);
            for (int j = 0; j < palette.size(); j++) {
                String name = palette.getCompound(j).getString("Name");
                if (!name.equals("minecraft:air") && !name.equals("minecraft:cave_air")
                        && !name.equals("minecraft:void_air")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The repair: a chunk whose surface is built gets bedrock at the bottom of every column, whatever stood there (an
     * ore of any mod, air, water) but a block holding a block entity (a chest, a spawner) or a point of interest (a
     * portal, a job site), and above it to four blocks up the original's ragged bedrock where natural stone stands (a
     * block {@code y} up turns with the original's chance, {@code y <= nextInt(5)}); a chunk not yet surfaced gets its
     * bedrock from the surface pass to come. Then every height it keeps is measured from its blocks, its light is
     * worked out again as it loads, and the chunk is marked to be saved.
     */
    public static void repair(ChunkAccess chunk) {
        int bottom = chunk.getMinBuildHeight();
        if (chunk.getPersistedStatus().isOrAfter(ChunkStatus.SURFACE)) {
            BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
            Random random = new Random(chunk.getPos().x * 341873128712L + chunk.getPos().z * 132897987541L);
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    for (int up = 0; up <= 4; up++) {
                        int y = bottom + up;
                        boolean turns = up == 0 || up <= random.nextInt(5);
                        BlockState here = chunk.getBlockState(at.set(chunk.getPos().getMinBlockX() + x, y,
                                chunk.getPos().getMinBlockZ() + z));
                        boolean floor = up == 0 && !here.is(Blocks.BEDROCK) && !here.hasBlockEntity()
                                && PoiTypes.forState(here).isEmpty();
                        if (!floor && !(up > 0 && turns && naturalStone(here))) continue;
                        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
                        section.setBlockState(x, y & 15, z, bedrock, false);
                    }
                }
            }
        }
        EnumSet<Heightmap.Types> kept = EnumSet.noneOf(Heightmap.Types.class);
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {
            kept.add(entry.getKey());
            chunk.setHeightmap(entry.getKey(), new Heightmap(chunk, entry.getKey()).getRawData());
        }
        Heightmap.primeHeightmaps(chunk, kept);
        chunk.setLightCorrect(false);
        chunk.setUnsaved(true);
    }

    /** The stone a cave or the bottom of the world cuts: the overworld's stones and the ores in them, soil, gravel. */
    private static boolean naturalStone(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Tags.Blocks.ORES) || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.SAND) || state.is(Blocks.CLAY)
                || state.is(Blocks.SANDSTONE) || state.is(danger.orespawn.ModBlocks.CRYSTAL_STONE.get());
    }
}
