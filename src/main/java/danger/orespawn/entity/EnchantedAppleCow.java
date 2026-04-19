package danger.orespawn.entity;

import javax.annotation.Nullable;
import danger.orespawn.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Endgame-tier surface variant of the OreSpawn cow lineage that drops
 * {@link Items#ENCHANTED_GOLDEN_APPLE} on death. Wiki-canon completion
 * entity (not in 1.7.10 source — verified zero references in
 * {@code reference_1_7_10_source/}); rounds out the
 * Apple → Golden Apple → Enchanted Golden Apple cow ladder so the
 * variant family mirrors the vanilla apple progression players already
 * recognise.
 *
 * <p>Drop window is intentionally a flat 1 (versus
 * {@link AppleCow}'s 1–3 and {@link GoldenAppleCow}'s 1–2) so a single
 * kill yields a single notch-apple — the most valuable consumable in
 * vanilla — without trivialising the long-form god-apple economy.
 *
 * <p>Spawn weight in {@code add_overworld_creatures.json} is
 * intentionally one-sixth of {@link AppleCow} (one-half of
 * {@link GoldenAppleCow}) so they read as a true "shiny" sighting.
 */
public class EnchantedAppleCow extends Cow {
    public EnchantedAppleCow(EntityType<? extends EnchantedAppleCow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(Items.ENCHANTED_GOLDEN_APPLE);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new EnchantedAppleCow(ModEntities.ENCHANTED_APPLE_COW.get(), this.level());
    }
}
