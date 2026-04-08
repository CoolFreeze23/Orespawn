package danger.orespawn;

import danger.orespawn.entity.EntityLavaLovingItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts item drops from fire-immune OreSpawn mobs into
 * EntityLavaLovingItem instances so they don't burn in lava.
 *
 * Listens to LivingDropsEvent on the GAME bus and replaces each
 * vanilla ItemEntity with a fire-immune equivalent when the dying
 * mob is fire-immune and belongs to the OreSpawn namespace.
 */
@EventBusSubscriber(modid = OreSpawnMod.MOD_ID)
public class ModLavaDropHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!event.getEntity().fireImmune()) return;

        // Only process OreSpawn entities to avoid interfering with other mods
        var entityId = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (!OreSpawnMod.MOD_ID.equals(entityId.getNamespace())) return;

        List<ItemEntity> replacements = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            if (drop instanceof EntityLavaLovingItem) {
                replacements.add(drop);
                continue;
            }
            EntityLavaLovingItem lavaDrop = new EntityLavaLovingItem(
                    drop.level(), drop.getX(), drop.getY(), drop.getZ(), drop.getItem());
            lavaDrop.setPickUpDelay(40);
            replacements.add(lavaDrop);
        }

        event.getDrops().clear();
        event.getDrops().addAll(replacements);
    }
}
