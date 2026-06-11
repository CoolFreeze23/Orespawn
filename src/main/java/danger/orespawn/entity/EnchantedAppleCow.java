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
 * Endgame-tier surface variant of the OreSpawn cow lineage that caps
 * the Apple → Golden Apple → Enchanted Golden Apple ladder. This entity
 * is the post-consolidation single source of truth for the wiki's
 * "Enchanted (Golden Apple) Cow" — the previously separate
 * {@code EnchantedCow} class was deleted and its loot table, AI, spawn
 * placements, and bonus drops were folded into this class so the world
 * doesn't ship two visually-identical glinting cows.
 *
 * <p><b>Loot</b> — fully data-driven via
 * {@code loot_table/entities/enchanted_apple_cow.json}, which carries
 * exactly the original EnchantedCow drop list (orig EnchantedCow.java:26-34:
 * apples, golden_apple x2, enchanted_golden_apple x1, plus the inherited
 * RedCow apples and vanilla cow leather/beef).
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
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new EnchantedAppleCow(ModEntities.ENCHANTED_APPLE_COW.get(), this.level());
    }
}
