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
 * Endgame-tier surface variant of the OreSpawn cow lineage that caps
 * the Apple → Golden Apple → Enchanted Golden Apple ladder. This entity
 * is the post-consolidation single source of truth for the wiki's
 * "Enchanted (Golden Apple) Cow" — the previously separate
 * {@code EnchantedCow} class was deleted and its loot table, AI, spawn
 * placements, and bonus drops were folded into this class so the world
 * doesn't ship two visually-identical glinting cows.
 *
 * <p><b>Loot</b> — base table is data-driven via
 * {@code loot_table/entities/enchanted_apple_cow.json} (leather 1–4,
 * golden_apple 1–2, enchanted_golden_apple 1). On top of the JSON pool,
 * {@link #dropCustomDeathLoot} adds the legacy {@code EnchantedCow}
 * bonus: 1–2 XP bottles always, plus a 20% chance of an enchanted book
 * — so the consolidated entity is strictly a superset of every drop the
 * old enchanted cow used to give.
 *
 * <p><b>Spawning</b> — {@code add_overworld_creatures.json} (weight 1,
 * the rare overworld sighting) plus {@code dim_village_locals.json}
 * (weight 4, packs of 2–4) and {@code dim_utopia_locals.json}
 * (weight 5, packs of 2–4) — the latter two preserve the legacy
 * EnchantedCow spawn niche unchanged.
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
        // Legacy EnchantedCow bonus drops, ported verbatim so existing
        // farms/players retain the same yield expectations after the
        // class consolidation. The base leather + apple drops are
        // resolved by the loot-table JSON, not here, to keep the
        // data-driven path as the source of truth.
        int bottles = this.random.nextInt(2) + 1;
        for (int i = 0; i < bottles; i++) {
            this.spawnAtLocation(Items.EXPERIENCE_BOTTLE);
        }
        if (this.random.nextInt(5) == 0) {
            this.spawnAtLocation(Items.ENCHANTED_BOOK);
        }
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
