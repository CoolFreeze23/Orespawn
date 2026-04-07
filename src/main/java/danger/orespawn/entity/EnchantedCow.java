package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EnchantedCow extends Cow {
    public EnchantedCow(EntityType<? extends EnchantedCow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int count = this.random.nextInt(2) + 1;
        for (int i = 0; i < count; i++) {
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
        return new EnchantedCow(ModEntities.ENCHANTED_COW.get(), this.level());
    }
}
