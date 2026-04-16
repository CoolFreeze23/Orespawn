/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.enchantment.Enchantment
 */
package danger.orespawn.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;

public class ItemEnchantments {
    private Map<Enchantment, Integer> enchantmentLevels = new HashMap<Enchantment, Integer>();

    public ItemEnchantments addEnchantment(Enchantment e, int level) {
        this.enchantmentLevels.put(e, level);
        return this;
    }

    public Map getMap() {
        return this.enchantmentLevels;
    }
}

