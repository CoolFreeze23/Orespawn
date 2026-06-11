package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

// Death drops are fully data-driven via loot_table/entities/crystal_cow.json
// (orig CrystalCow.java:19-26: crystal apples + 1 apple + RedCow/vanilla cow drops).
// orig CrystalCow.java:13-14 — extends RedCow, inheriting its 1-in-200
// revenge-forgiveness tick and never-despawn behavior.
public class CrystalCow extends RedCow {
    public CrystalCow(EntityType<? extends CrystalCow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return RedCow.createAttributes();
    }

    @Nullable
    @Override
    public Cow getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new CrystalCow(ModEntities.CRYSTAL_COW.get(), this.level());
    }
}
