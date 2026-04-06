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

public class RedCow extends Cow {
    public RedCow(EntityType<? extends RedCow> type, Level level) {
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
            this.spawnAtLocation(Items.WHEAT);
        }
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
