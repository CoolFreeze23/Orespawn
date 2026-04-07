package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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
        return Cockateil.createAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.random.nextInt(3) == 0) {
            this.spawnAtLocation(ModItems.RUBY.get());
        }
    }

    @Nullable
    @Override
    public Animal getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new RubyBird(ModEntities.RUBY_BIRD.get(), this.level());
    }
}
