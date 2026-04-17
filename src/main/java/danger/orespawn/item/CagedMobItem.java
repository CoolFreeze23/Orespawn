package danger.orespawn.item;

import danger.orespawn.ModDataComponents;
import danger.orespawn.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Filled cage item — carries a captured mob's full state via two
 * {@link net.minecraft.core.component.DataComponentType DataComponents}:
 *
 * <ul>
 *   <li>{@link ModDataComponents#CAGED_ENTITY} — the entity-type ResourceLocation
 *       (cheap, always present, drives the tooltip name).</li>
 *   <li>{@link ModDataComponents#CAGED_ENTITY_DATA} — the mob's serialized
 *       {@link CompoundTag} from {@link Mob#saveWithoutId(CompoundTag)} when
 *       captured via the {@link EmptyCageItem#interactLivingEntity} path. May
 *       be absent for cages produced by the projectile / type-only paths, in
 *       which case the mob is re-spawned in vanilla-default state.</li>
 * </ul>
 *
 * <p>{@link #useOn(UseOnContext)} deserializes the stored NBT through
 * {@link EntityType#loadEntityRecursive}, restoring health / custom name / age
 * / equipment / taming exactly as captured. Returning an Empty Cage on
 * release matches 1.7.10 {@code CritterCage.func_77648_a}'s
 * {@code ent.func_145779_a(OreSpawnMain.CageEmpty, 1)} behavior.</p>
 */
public class CagedMobItem extends Item {
    public CagedMobItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack stack = context.getItemInHand();
        ResourceLocation entityId = stack.get(ModDataComponents.CAGED_ENTITY.get());
        if (entityId == null) return InteractionResult.PASS;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (type == null || !BuiltInRegistries.ENTITY_TYPE.getKey(type).equals(entityId)) {
            return InteractionResult.FAIL;
        }

        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clicked.relative(face);

        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        CompoundTag savedNbt = stack.get(ModDataComponents.CAGED_ENTITY_DATA.get());
        Entity spawned;
        if (savedNbt != null && !savedNbt.isEmpty()) {
            CompoundTag tag = savedNbt.copy();
            tag.putString("id", entityId.toString());
            spawned = EntityType.loadEntityRecursive(tag, serverLevel, e -> {
                e.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                        level.getRandom().nextFloat() * 360.0F, 0.0F);
                return e;
            });
            if (spawned != null) {
                serverLevel.addFreshEntity(spawned);
            }
        } else {
            spawned = type.create(serverLevel, null, spawnPos, MobSpawnType.MOB_SUMMONED, true, false);
            if (spawned != null) {
                spawned.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                        level.getRandom().nextFloat() * 360.0F, 0.0F);
                if (spawned instanceof Mob mob && stack.has(DataComponents.CUSTOM_NAME)) {
                    mob.setCustomName(stack.getHoverName());
                }
                serverLevel.addFreshEntity(spawned);
                if (spawned instanceof Mob mob) {
                    mob.playAmbientSound();
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 1, 0, 0, 0, 0);
        }
        level.playSound(null, spawnPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);

        if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack empty = new ItemStack(ModItems.CAGE_EMPTY.get());
            if (!context.getPlayer().getInventory().add(empty)) {
                context.getPlayer().drop(empty, false);
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation entityId = stack.get(ModDataComponents.CAGED_ENTITY.get());
        if (entityId != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityId);
            if (type != null) {
                return Component.translatable("item.orespawn.caged_mob",
                        Component.translatable(type.getDescriptionId()));
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation entityId = stack.get(ModDataComponents.CAGED_ENTITY.get());
        if (entityId != null) {
            tooltip.add(Component.literal(entityId.toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
        CompoundTag nbt = stack.get(ModDataComponents.CAGED_ENTITY_DATA.get());
        if (nbt != null && !nbt.isEmpty()) {
            if (nbt.contains("Health")) {
                tooltip.add(Component.translatable("tooltip.orespawn.caged_health",
                        String.format("%.1f", nbt.getFloat("Health"))).withStyle(ChatFormatting.GREEN));
            }
            if (nbt.getBoolean("PersistenceRequired") || nbt.getBoolean("Tamed")) {
                tooltip.add(Component.translatable("tooltip.orespawn.caged_persistent")
                        .withStyle(ChatFormatting.AQUA));
            }
        }
    }

    /** Cage with type-only metadata (used by the projectile path, where we don't
     *  have an ItemStack-side reference at hit time and just snapshot type). */
    public static ItemStack createForEntity(ResourceLocation entityId) {
        ItemStack stack = new ItemStack(ModItems.CAGED_MOB.get());
        stack.set(ModDataComponents.CAGED_ENTITY.get(), entityId);
        return stack;
    }

    /** Cage with full live-state preserved — the {@link EmptyCageItem} direct-capture path. */
    public static ItemStack createForEntity(ResourceLocation entityId, CompoundTag mobData) {
        ItemStack stack = createForEntity(entityId);
        if (mobData != null && !mobData.isEmpty()) {
            stack.set(ModDataComponents.CAGED_ENTITY_DATA.get(), mobData.copy());
        }
        return stack;
    }
}
