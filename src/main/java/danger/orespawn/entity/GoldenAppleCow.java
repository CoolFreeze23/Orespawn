package danger.orespawn.entity;

import javax.annotation.Nullable;
import danger.orespawn.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

/**
 * Rare endgame surface variant of the cow lineage that drops golden
 * apples on death. Wiki "Added Mobs" entry; not in the 1.7.10 source
 * (verified Phase 14). Implementation mirrors {@link AppleCow} with a
 * tighter 1–2 golden-apple drop window (data-driven via
 * {@code loot_table/entities/golden_apple_cow.json}) so a single kill
 * is meaningful but doesn't trivialise the natural-cave golden apple
 * economy.
 *
 * <p>Spawn weight in {@code add_overworld_creatures.json} is
 * intentionally one-third of {@link AppleCow} so they read as
 * "rare cousins" rather than reliable apple farms.
 */
public class GoldenAppleCow extends Cow {
    public GoldenAppleCow(EntityType<? extends GoldenAppleCow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new GoldenAppleCow(ModEntities.GOLDEN_APPLE_COW.get(), this.level());
    }
}
