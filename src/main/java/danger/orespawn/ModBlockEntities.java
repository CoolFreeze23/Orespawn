package danger.orespawn;

import danger.orespawn.block.entity.ExtractorBlockEntity;
import danger.orespawn.block.entity.RandomDungeonSpawnerBlockEntity;
import danger.orespawn.gui.CrystalFurnaceBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OreSpawnMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalFurnaceBlockEntity>> CRYSTAL_FURNACE_BE =
            BLOCK_ENTITIES.register("crystal_furnace",
                    () -> BlockEntityType.Builder.of(CrystalFurnaceBlockEntity::new,
                            ModBlocks.CRYSTAL_FURNACE.get()).build(null));

    // Phase 11 — Extractor BE: hopper-driven processor for orespawn:extracting recipes.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_BE =
            BLOCK_ENTITIES.register("extractor",
                    () -> BlockEntityType.Builder.of(ExtractorBlockEntity::new,
                            ModBlocks.EXTRACTOR.get()).build(null));

    // Phase 11 — Random Dungeon delayed spawner BE.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RandomDungeonSpawnerBlockEntity>> RANDOM_DUNGEON_SPAWNER_BE =
            BLOCK_ENTITIES.register("random_dungeon_spawner",
                    () -> BlockEntityType.Builder.of(RandomDungeonSpawnerBlockEntity::new,
                            ModBlocks.RANDOM_DUNGEON_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
