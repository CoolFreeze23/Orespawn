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
 * Surface variant of the OreSpawn cow lineage that drops apples on
 * death. The Wiki "Added Mobs" catalog enumerates Apple Cow alongside
 * Red / Crystal / Gold / Enchanted cows; the 1.7.10 source we ship
 * does not contain an {@code AppleCow} class (Phase 14 audit verified
 * zero references in {@code reference_1_7_10_source/}), so this is a
 * wiki-canon completion implementation modelled on {@link RedCow}'s
 * {@code dropCustomDeathLoot} pattern.
 *
 * <p>Drops 1–3 apples per kill (matching {@link RedCow}'s wheat drop
 * cadence) on top of the vanilla beef/leather base loot inherited
 * from {@link Cow}. Spawns in any overworld biome via
 * {@code add_overworld_creatures.json}.
 */
public class AppleCow extends Cow {
    public AppleCow(EntityType<? extends AppleCow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = this.random.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            this.spawnAtLocation(Items.APPLE);
        }
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new AppleCow(ModEntities.APPLE_COW.get(), this.level());
    }
}
