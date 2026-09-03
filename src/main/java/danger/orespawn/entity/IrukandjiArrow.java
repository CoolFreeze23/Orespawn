package danger.orespawn.entity;

import danger.orespawn.ModEntities;
import danger.orespawn.OreSpawnConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of the original IrukandjiArrow (orig IrukandjiArrow.java). Unlike the
 * velocity-scaled UltimateArrow, this arrow deals a FLAT 100 on every hit:
 * orig :157 seeds {@code var23 = 100.0f} with no velocity term, the
 * {@code func_70239_b} damage setter is an empty override (:269-270) so no
 * caller can reseed it, and {@code func_70242_d} returns the constant 100
 * (:272-273). Crit adds {@code nextInt(damage/2 + 2)} = 0..51 (:172-173).
 * The original applies NO potion effects on hit — the sting debuffs belong
 * to the Irukandji jellyfish entity, not the arrow (ENT-D-064).
 */
public class IrukandjiArrow extends AbstractArrow {
    /** orig IrukandjiArrow.java:157 — flat, never velocity-scaled. */
    private static final float FLAT_DAMAGE = 100.0f;

    public IrukandjiArrow(EntityType<? extends IrukandjiArrow> type, Level level) {
        super(type, level);
    }

    public IrukandjiArrow(Level level, LivingEntity shooter, ItemStack weapon) {
        super(ModEntities.IRUKANDJI_ARROW.get(), shooter, level, new ItemStack(Items.ARROW), weapon);
    }

    // orig MyDispenserBehaviorArrow.java:18-22 — dispensers spawn the arrow at a bare position
    public IrukandjiArrow(Level level, double x, double y, double z) {
        super(ModEntities.IRUKANDJI_ARROW.get(), x, y, z, level, new ItemStack(Items.ARROW), null);
    }

    /**
     * orig IrukandjiArrow.java:155-200, replacing the vanilla hit so the
     * damage stays flat. Flow, in original order: the
     * {@code ultimate_sword_pvp}=off guard no-sells players / Girlfriend /
     * Boyfriend / tamed pets with just the bow-hit sound (:158-170); crit
     * adds 0..51 (:172-173); a burning arrow ignites the victim 5s (:176-177);
     * on a successful hurt the victim's arrow count increments, Punch knockback
     * pushes a Mob (orig :181 EntityLiving, never a player; ENT-S-111) 0.6/blk (:187-188), an arrow-hit-player
     * ding reaches the shooter (:190-192), and the arrow despawns with the
     * bow-hit sound; a no-sold hurt deflects the arrow backwards (:195-199).
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();

        if (!OreSpawnConfig.ULTIMATE_SWORD_PVP.get()
                && (hit instanceof Player || hit instanceof Girlfriend || hit instanceof Boyfriend
                        || (hit instanceof TamableAnimal pet && pet.isTame()))) {
            this.playBowHitSound();
            this.discard();
            return;
        }

        float damage = FLAT_DAMAGE;
        if (this.isCritArrow()) {
            damage += this.random.nextInt((int) FLAT_DAMAGE / 2 + 2);
        }
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().arrow(this, owner == null ? this : owner);

        if (this.isOnFire()) {
            hit.igniteForSeconds(5.0f);
        }

        if (hit.hurt(source, damage)) {
            // orig :181 `instanceof EntityLiving` (1.21.1 Mob) wrapped the arrow count, the push AND the ding
            // together, so a player got none of them (ENT-S-111; the ding was unreachable in 1.7.10 too).
            if (hit instanceof Mob living) {
                if (!this.level().isClientSide) {
                    living.setArrowCount(living.getArrowCount() + 1);
                }
                int punch = this.punchLevel();
                if (punch > 0) { // inside the orig :181 gate: a player is never pushed (ENT-S-111)
                    Vec3 flat = this.getDeltaMovement().multiply(1.0, 0.0, 1.0);
                    if (flat.lengthSqr() > 0.0) {
                        // orig :187-188 — 0.6 * punch along the flight line, +0.1 lift
                        Vec3 push = flat.normalize().scale(punch * 0.6);
                        living.push(push.x, 0.1, push.z);
                    }
                }
                if (owner instanceof ServerPlayer shooter && hit != owner && hit instanceof Player) {
                    shooter.connection.send(new ClientboundGameEventPacket(
                            ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0f));
                }
            }
            this.playBowHitSound();
            this.discard();
        } else {
            // orig :195-199 — bounce off: reverse at 10% and flip yaw
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0f);
            this.yRotO += 180.0f;
        }
    }

    /** orig :162/:193 — 1.2 / (rand*0.2 + 0.9) pitch on every outcome. */
    private void playBowHitSound() {
        this.playSound(SoundEvents.ARROW_HIT, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
    }

    /** Punch level read from the firing weapon (orig SkateBow.java:53-55 seeds knockbackStrength). */
    private int punchLevel() {
        ItemStack weapon = this.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(
                this.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.PUNCH),
                weapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
