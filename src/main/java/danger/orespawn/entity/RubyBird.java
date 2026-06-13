package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class RubyBird extends Cockateil {
    public RubyBird(EntityType<? extends RubyBird> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // orig RubyBird.java defines no attribute overrides — it inherits
        // Cockateil's stats (HP 2 / speed 0.33 / ATK 1, orig Cockateil.java:51-54,128).
        return Cockateil.createAttributes();
    }

    // Death drops are fully data-driven via loot_table/entities/ruby_bird.json
    // (orig Cockateil.java:242-248: 0-2 of ruby [1/3, killed by player] or feather).

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        // orig RubyBird.java:22-27 — bespoke "orespawn:rubybird" when day and not raining, else silent
        if (this.level().isDay() && !this.level().isRaining()) {
            return SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "rubybird"));
        }
        return null;
    }

    @Nullable
    @Override
    public Animal getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new RubyBird(ModEntities.RUBY_BIRD.get(), this.level());
    }

    /** orig RubyBird.java:29-31 — always allowed. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        return true;
    }
}
