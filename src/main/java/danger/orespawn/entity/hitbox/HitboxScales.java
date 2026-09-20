package danger.orespawn.entity.hitbox;

import java.util.function.ToDoubleFunction;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.entity.Crab;
import danger.orespawn.entity.Girlfriend;
import danger.orespawn.entity.PitchBlack;
import de.dertoaster.multihitboxlib.api.MHLibEntitySizeScales;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * ENT-S-173: the size scale MultiHitboxLib applies to a profiled species' part boxes, registered per entity type
 * ({@link MHLibEntitySizeScales}) so that the boxes follow the model the way the renderer's descriptor scales it.
 * The library's own rule scales an {@code AgeableMob} baby's parts by 0.5 - right for the species whose descriptor
 * halves a baby's model (the Basilisk, the Frog, the Gazelle, the Whale and the others of that shape). This table covers
 * the rest:
 * <ul>
 * <li>{@link #FULL_SIZE}: ageable species whose renderer draws a baby at full size (a constant {@code applyScale}, or
 * none) - their parts stay full size too, 1.0 always;</li>
 * <li>the Crab: {@link Crab#getCrabScale()}, the same factor its box and its render scale follow;</li>
 * <li>Pitch Black: {@link PitchBlack#getPitchBlackScale()}, its size tier;</li>
 * <li>the Girlfriend: {@code 5.0} while valentine-angry (her descriptor's scale and her 2.5x8 box), else 1;</li>
 * <li>the CaterKiller: {@code 0.5} under PlayNicely (the descriptor's halved scale and the halved box), else 1.</li>
 * </ul>
 * The bosses that shrink under PlayNicely (the King, the Queen, Godzilla, the Kraken) implement
 * {@code IMHLibSizeCallback} themselves, which the library resolves first. Registered from
 * {@code OreSpawnMod.commonSetup}; the table is derived, with the profiles, from the descriptors' {@code applyScale}
 * bodies (tools/hitbox_species_table.py, column "rule").
 */
public final class HitboxScales {

    /** Ageable species drawn at full size as a baby: their parts do not shrink. */
    private static final DeferredHolder<?, ?>[] FULL_SIZE = {
            ModEntities.BABY_DRAGON, ModEntities.BOYFRIEND, ModEntities.COCKATEIL, ModEntities.DRAGON,
            ModEntities.ENTITY_ANT, ModEntities.ENTITY_CANNON_FODDER, ModEntities.ENTITY_CLIFF_RACER, ModEntities.ENTITY_CRICKET,
            ModEntities.ENTITY_DRAGONFLY, ModEntities.ENTITY_LEON, ModEntities.ENTITY_RAINBOW_ANT, ModEntities.ENTITY_RED_ANT,
            ModEntities.ENTITY_SPYRO, ModEntities.ENTITY_STINKY, ModEntities.ENTITY_STINK_BUG, ModEntities.ENTITY_TERMITE,
            ModEntities.ENTITY_UNSTABLE_ANT, ModEntities.GOLD_FISH, ModEntities.LEONOPTERYX, ModEntities.OSTRICH,
            ModEntities.RUBY_BIRD, ModEntities.THE_PRINCE, ModEntities.THE_PRINCESS, ModEntities.THE_PRINCE_ADULT,
            ModEntities.THE_PRINCE_TEEN,
    };

    private HitboxScales() {
    }

    public static void register() {
        ToDoubleFunction<Entity> one = entity -> 1.0D;
        for (DeferredHolder<?, ?> holder : FULL_SIZE) {
            MHLibEntitySizeScales.register((EntityType<?>) holder.get(), one);
        }
        MHLibEntitySizeScales.register(ModEntities.CRAB.get(), entity -> ((Crab) entity).getCrabScale());
        MHLibEntitySizeScales.register(ModEntities.PITCH_BLACK.get(), entity -> ((PitchBlack) entity).getPitchBlackScale());
        MHLibEntitySizeScales.register(ModEntities.GIRLFRIEND.get(),
                entity -> ((Girlfriend) entity).isValentineAngry() ? 5.0D : 1.0D);
        MHLibEntitySizeScales.register(ModEntities.ENTITY_CATER_KILLER.get(),
                entity -> OreSpawnConfig.PLAY_NICELY.get() ? 0.5D : 1.0D);
    }

    /** The number of types the table registers (pinned by the gametests). */
    public static int count() {
        return FULL_SIZE.length + 4;
    }
}
