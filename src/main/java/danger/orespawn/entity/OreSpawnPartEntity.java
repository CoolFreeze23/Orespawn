package danger.orespawn.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * A generic hittable sub-part for OreSpawn boss entities.
 * Each instance represents one region of the boss's body (head, wing, tail, etc.)
 * and forwards damage to the parent entity with part identity, allowing per-part
 * damage multipliers. Based on the vanilla {@code EnderDragonPart} pattern.
 *
 * @param <T> the parent boss entity type
 */
public class OreSpawnPartEntity<T extends Entity> extends PartEntity<T> {

    private final String name;
    private final EntityDimensions size;

    public OreSpawnPartEntity(T parent, String name, float width, float height) {
        super(parent);
        this.name = name;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    public String getPartName() {
        return this.name;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    /**
     * Forwards damage to the parent. If the parent implements {@link MultipartBoss},
     * the part identity is passed along so the parent can apply per-region damage
     * multipliers (e.g. head takes full damage, wings take reduced).
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        T parent = this.getParent();
        if (parent instanceof MultipartBoss boss) {
            return boss.hurtFromPart(this, source, amount);
        }
        return parent.hurt(source, amount);
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    /**
     * Interface for boss entities that support per-part damage routing.
     * Implementing this allows different body regions to have custom
     * damage multipliers (e.g. head = full damage, wings = 25%).
     */
    public interface MultipartBoss {
        boolean hurtFromPart(OreSpawnPartEntity<?> part, DamageSource source, float amount);
    }
}
