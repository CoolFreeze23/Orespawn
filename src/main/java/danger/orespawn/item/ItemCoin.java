package danger.orespawn.item;

import danger.orespawn.OreSpawnMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

/**
 * Coin item. Right-click consumes the coin and dispenses one random reward
 * from the {@code orespawn:gameplay/coin_reward} loot table.
 *
 * <p>Legacy 1.7.10 reference: the {@code Coin} entity dropped a 10-slot
 * pool on death (emerald sword/axe/shovel/pickaxe/hoe, diamond, uranium
 * nugget, titanium nugget, emerald, coin egg). We expose that same pool as
 * a data-driven loot table so server admins can rebalance via datapack
 * without recompiling.</p>
 */
public class ItemCoin extends Item {
    public static final ResourceKey<LootTable> COIN_REWARD_TABLE = ResourceKey.create(
            net.minecraft.core.registries.Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "gameplay/coin_reward"));

    public ItemCoin(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(COIN_REWARD_TABLE);
        if (table == LootTable.EMPTY) {
            // Loot table missing — bail without consuming so admins can debug.
            return InteractionResultHolder.pass(stack);
        }

        LootParams params = new LootParams.Builder(serverLevel)
                .create(LootContextParamSets.EMPTY);
        List<ItemStack> rewards = table.getRandomItems(params);

        BlockPos pos = player.blockPosition();
        for (ItemStack reward : rewards) {
            if (reward.isEmpty()) continue;
            if (!player.getInventory().add(reward)) {
                ItemEntity drop = new ItemEntity(serverLevel,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, reward);
                drop.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(drop);
            }
        }

        serverLevel.playSound(null, pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.6f);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.consume(stack);
    }
}
