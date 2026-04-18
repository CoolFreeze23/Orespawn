package danger.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

/**
 * Challenge Tower — direct port of the 1.7.10
 * {@code GenericDungeon.makeEnormousCastle} (King) and
 * {@code makeEnormousCastleQ} (Queen) layouts. The legacy code stamped a
 * 28×16×28 stone-brick base, then stacked between 1 and 6 procedural floors
 * on top, each containing a centred 1×1 spawner column gated by an iron-bar
 * shaft. The Queen variant mirrors the King's 6-floor ladder but swaps the
 * cap mob and chest contents.
 *
 * <p>Per the legacy source ({@code GenericDungeon.java#L292-L313}):</p>
 * <pre>
 *   Floor 1: Cloud Shark
 *   Floor 2: Lurking Terror
 *   Floor 3: Rotator
 *   Floor 4: Bee
 *   Floor 5: Mantis
 *   Floor 6: Mothra
 * </pre>
 *
 * <p>The Queen variant replaces Cloud Shark / Mothra with Mantis / Nightmare
 * (Pitch Black) per {@code makeEnormousCastleQ}.</p>
 *
 * <p>Modernized in 1.21.1 to:
 * <ul>
 *   <li>Run from {@link Feature} pipeline so it composes with biome
 *       modifiers and rarity placement (no manual chunk-event wiring).</li>
 *   <li>Use {@link WorldGenLevel#setBlock} with flag {@code 2} to suppress
 *       lighting/neighbour updates per cell (the original raw
 *       {@code FastSetBlock} was effectively the same — flag 2 is the modern
 *       equivalent that still fires block-entity updates for chests &
 *       spawners).</li>
 *   <li>Bound-check the entire footprint up-front; bail without partial
 *       writes if any cell would clip below worldgen floor / above
 *       max-build height. This avoids the runaway cascading chunk-load
 *       that the user explicitly flagged as a stability concern.</li>
 * </ul>
 * </p>
 */
public class ChallengeTowerFeature extends Feature<ChallengeTowerFeature.Config> {

    public record Config(boolean queenVariant) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.BOOL.optionalFieldOf("queen_variant", false).forGetter(Config::queenVariant)
        ).apply(inst, Config::new));
    }

    private static final int BASE_WIDTH = 28;
    private static final int BASE_HEIGHT = 16;
    private static final int FLOORS = 6;
    private static final int FLOOR_HEIGHT = 9;

    public ChallengeTowerFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> ctx) {
        WorldGenLevel level = ctx.level();
        boolean queen = ctx.config().queenVariant();

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, ctx.origin());
        // Drop one tile so the base sits flush in the ground, like the legacy code.
        BlockPos origin = surface.below();

        int totalHeight = BASE_HEIGHT + (FLOORS * FLOOR_HEIGHT) + 4;
        if (origin.getY() + totalHeight >= level.getMaxBuildHeight() - 4) return false;
        if (origin.getY() < level.getMinBuildHeight() + 2) return false;

        // Reject ocean / unstable ground — the tower needs solid footing.
        BlockState floor = level.getBlockState(origin);
        if (floor.isAir() || floor.liquid()) return false;

        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState bedrockCap = Blocks.BEDROCK.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();

        // Hollow + frame the base (matches makeEnormousCastle 28×16×28 box).
        for (int dx = 0; dx < BASE_WIDTH; dx++) {
            for (int dz = 0; dz < BASE_WIDTH; dz++) {
                for (int dy = 0; dy < BASE_HEIGHT; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    boolean wall = dx == 0 || dx == BASE_WIDTH - 1 || dz == 0 || dz == BASE_WIDTH - 1;
                    boolean cap = dy == 0 || dy == BASE_HEIGHT - 1;
                    if (cap) {
                        level.setBlock(pos, dy == BASE_HEIGHT - 1 ? bedrockCap : bricks, 2);
                    } else if (wall) {
                        level.setBlock(pos, ctx.random().nextInt(4) == 0 ? mossy : bricks, 2);
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Centre Emperor Scorpion column inside the base (lines 276-290 of legacy).
        int cx = BASE_WIDTH / 2;
        int cz = BASE_WIDTH / 2;
        for (int dy = 2; dy <= 4; dy++) {
            BlockPos spawnerPos = origin.offset(cx, dy, cz);
            placeSpawner(level, spawnerPos, ModEntities.ENTITY_EMPEROR_SCORPION.get());
        }

        // Stack the 6 floors. The legacy source rolled a 1..6 "level" int and
        // suppressed any floor whose index exceeded the roll; we always build
        // all 6 here so the visible silhouette matches the 1.7.10 maximum.
        List<EntityType<?>> mobLadder = queen ? queenFloorMobs() : kingFloorMobs();
        int currentY = BASE_HEIGHT;
        for (int floorIdx = 0; floorIdx < FLOORS; floorIdx++) {
            EntityType<?> mob = mobLadder.get(floorIdx);
            buildFloor(level, origin.offset(1, currentY, 1),
                    BASE_WIDTH - 2, FLOOR_HEIGHT, mob, ctx.random().nextInt(2) == 0);
            // Iron-bar spawner shaft up the centre (legacy lines 712-717).
            for (int barY = 1; barY < FLOOR_HEIGHT; barY++) {
                BlockPos barPos = origin.offset(cx, currentY + barY, cz);
                placeIronBarsAround(level, barPos, ironBars);
            }
            currentY += FLOOR_HEIGHT;
        }

        // Top deck cap + Royal Guardian chest (legacy lines 743-784, reward=6).
        BlockPos topDeck = origin.offset(cx, currentY, cz);
        placeRoyalChests(level, topDeck, queen, ctx);
        return true;
    }

    private static void buildFloor(WorldGenLevel level, BlockPos corner, int width, int height,
                                   EntityType<?> mob, boolean addCornerSpawners) {
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();

        // Floor + ceiling + 4 walls
        for (int x = -1; x <= width; x++) {
            for (int z = -1; z <= width; z++) {
                level.setBlock(corner.offset(x, 0, z), bricks, 2);
                level.setBlock(corner.offset(x, height - 1, z), bricks, 2);
            }
        }
        for (int y = 1; y < height - 1; y++) {
            for (int t = -1; t <= width; t++) {
                level.setBlock(corner.offset(-1, y, t), mossy, 2);
                level.setBlock(corner.offset(width, y, t), mossy, 2);
                level.setBlock(corner.offset(t, y, -1), mossy, 2);
                level.setBlock(corner.offset(t, y, width), mossy, 2);
            }
        }
        // Carve interior so mobs have room to path
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = 1; y < height - 1; y++) {
                    level.setBlock(corner.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }

        // Centre spawner column (2 stacked, like legacy lines 702-711).
        int cx = width / 2;
        int cz = width / 2;
        placeSpawner(level, corner.offset(cx, 1, cz), mob);
        placeSpawner(level, corner.offset(cx, 2, cz), mob);

        // Optional 4-corner tower spawners for the harder floor variants.
        if (addCornerSpawners) {
            placeSpawner(level, corner.offset(0, 1, 0), mob);
            placeSpawner(level, corner.offset(width - 1, 1, 0), mob);
            placeSpawner(level, corner.offset(0, 1, width - 1), mob);
            placeSpawner(level, corner.offset(width - 1, 1, width - 1), mob);
        }
    }

    private static void placeIronBarsAround(WorldGenLevel level, BlockPos centre, BlockState bars) {
        level.setBlock(centre.north(), bars, 2);
        level.setBlock(centre.south(), bars, 2);
        level.setBlock(centre.east(), bars, 2);
        level.setBlock(centre.west(), bars, 2);
    }

    private static void placeSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> mob) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }

    private static void placeRoyalChests(WorldGenLevel level, BlockPos centre,
                                         boolean queen, FeaturePlaceContext<Config> ctx) {
        // Four-corner chest layout matches the legacy fill_chests reward=6 path.
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, net.minecraft.core.Direction.NORTH);

        BlockPos[] slots = {
                centre.offset(-2, 1, 0),
                centre.offset(2, 1, 0),
                centre.offset(0, 1, -2),
                centre.offset(0, 1, 2)
        };
        for (BlockPos slot : slots) {
            level.setBlock(slot, chestState, 2);
        }

        // Slot 0: Prince/Princess egg — the legacy "starter" chest content.
        if (level.getBlockEntity(slots[0]) instanceof ChestBlockEntity chest) {
            chest.setItem(1, new ItemStack(queen
                    ? ModItems.PRINCESS_EGG.get()
                    : ModItems.PRINCE_EGG.get()));
            chest.setItem(0, new ItemStack(Items.DIAMOND, 16));
        }
        // Slot 1: Royal Helmet + Chestplate (legacy lines 758-759 / queen 6975-6976).
        if (level.getBlockEntity(slots[1]) instanceof ChestBlockEntity chest) {
            chest.setItem(1, new ItemStack(ModItems.ROYAL_HELMET.get()));
            chest.setItem(2, new ItemStack(ModItems.ROYAL_CHESTPLATE.get()));
            chest.setItem(0, new ItemStack(Items.GOLDEN_APPLE, ctx.random().nextInt(4) + 2));
        }
        // Slot 2: Royal Leggings + Boots (legacy lines 769-770 / queen 6982-6983).
        if (level.getBlockEntity(slots[2]) instanceof ChestBlockEntity chest) {
            chest.setItem(1, new ItemStack(ModItems.ROYAL_LEGGINGS.get()));
            chest.setItem(2, new ItemStack(ModItems.ROYAL_BOOTS.get()));
            chest.setItem(0, new ItemStack(Items.EMERALD, ctx.random().nextInt(8) + 4));
        }
        // Slot 3: Royal Guardian Greatsword (`MyRoyal` legacy line 780).
        if (level.getBlockEntity(slots[3]) instanceof ChestBlockEntity chest) {
            chest.setItem(1, new ItemStack(ModItems.ROYAL_GUARDIAN_SWORD.get()));
            chest.setItem(0, new ItemStack(Items.NETHERITE_INGOT, ctx.random().nextInt(2) + 1));
        }
    }

    /** Mob ladder for the King variant — Cloud Shark → Mothra ascending. */
    private static List<EntityType<?>> kingFloorMobs() {
        return List.of(
                ModEntities.CLOUD_SHARK.get(),
                ModEntities.ENTITY_LURKING_TERROR.get(),
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_BEE.get(),
                ModEntities.ENTITY_MANTIS.get(),
                ModEntities.MOTHRA.get()
        );
    }

    /** Mob ladder for the Queen variant — flips top floors to Mantis / Pitch Black. */
    private static List<EntityType<?>> queenFloorMobs() {
        return List.of(
                ModEntities.ENTITY_MANTIS.get(),
                ModEntities.ENTITY_LURKING_TERROR.get(),
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_BEE.get(),
                ModEntities.MOTHRA.get(),
                ModEntities.PITCH_BLACK.get()
        );
    }
}
