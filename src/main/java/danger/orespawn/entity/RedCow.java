package danger.orespawn.entity;

import javax.annotation.Nullable;
import danger.orespawn.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

// Death drops are fully data-driven via loot_table/entities/red_cow.json
// (orig RedCow.java:17-23: apples + vanilla cow drops).
public class RedCow extends Cow {
    public RedCow(EntityType<? extends RedCow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    protected void customServerAiStep() {
        if (this.random.nextInt(200) == 1) {
            this.setTarget(null);
        }
        super.customServerAiStep();
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return false;
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new RedCow(ModEntities.RED_COW.get(), this.level());
    }
}
