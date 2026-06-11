package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

// Death drops are fully data-driven via loot_table/entities/gold_cow.json
// (orig GoldCow.java:18-25: apples + 1 golden apple + RedCow/vanilla cow drops).
public class GoldCow extends Cow {
    public GoldCow(EntityType<? extends GoldCow> type, Level level) {
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
        return new GoldCow(ModEntities.GOLD_COW.get(), this.level());
    }
}
