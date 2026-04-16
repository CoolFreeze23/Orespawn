/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  mcp.MethodsReturnNonnullByDefault
 *  net.minecraft.creativetab.CreativeTabs
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.NonNullList
 */
package danger.orespawn.tabs;

import danger.orespawn.init.ModItems;
import java.util.Comparator;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

@MethodsReturnNonnullByDefault
public class OrespawnTab
extends CreativeTabs {
    public OrespawnTab() {
        super("Orespawn");
    }

    public ItemStack func_78016_d() {
        return new ItemStack(ModItems.ULTIMATE_SWORD);
    }

    public void func_78018_a(NonNullList<ItemStack> list) {
        list.sort(Comparator.comparing(ItemStack::func_82833_r));
        super.func_78018_a(list);
    }
}

