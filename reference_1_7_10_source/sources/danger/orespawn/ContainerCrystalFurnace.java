/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.TileEntityCrystalFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class ContainerCrystalFurnace
extends Container {
    private TileEntityCrystalFurnace furnace;
    private int lastCookTime;
    private int lastBurnTime;
    private int lastItemBurnTime;

    public ContainerCrystalFurnace(InventoryPlayer par1InventoryPlayer, TileEntityCrystalFurnace par2TileEntityCrystalFurnace) {
        int i;
        this.furnace = par2TileEntityCrystalFurnace;
        this.func_75146_a(new Slot((IInventory)par2TileEntityCrystalFurnace, 0, 56, 17));
        this.func_75146_a(new Slot((IInventory)par2TileEntityCrystalFurnace, 1, 56, 53));
        this.func_75146_a((Slot)new SlotFurnace(par1InventoryPlayer.field_70458_d, (IInventory)par2TileEntityCrystalFurnace, 2, 116, 35));
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.func_75146_a(new Slot((IInventory)par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.func_75146_a(new Slot((IInventory)par1InventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    public void func_75132_a(ICrafting par1ICrafting) {
        super.func_75132_a(par1ICrafting);
        par1ICrafting.func_71112_a((Container)this, 0, this.furnace.furnaceCookTime);
        par1ICrafting.func_71112_a((Container)this, 1, this.furnace.furnaceBurnTime);
        par1ICrafting.func_71112_a((Container)this, 2, this.furnace.currentItemBurnTime);
    }

    public void func_75142_b() {
        super.func_75142_b();
        for (int i = 0; i < this.field_75149_d.size(); ++i) {
            ICrafting icrafting = (ICrafting)this.field_75149_d.get(i);
            if (this.lastCookTime != this.furnace.furnaceCookTime) {
                icrafting.func_71112_a((Container)this, 0, this.furnace.furnaceCookTime);
            }
            if (this.lastBurnTime != this.furnace.furnaceBurnTime) {
                icrafting.func_71112_a((Container)this, 1, this.furnace.furnaceBurnTime);
            }
            if (this.lastItemBurnTime == this.furnace.currentItemBurnTime) continue;
            icrafting.func_71112_a((Container)this, 2, this.furnace.currentItemBurnTime);
        }
        this.lastCookTime = this.furnace.furnaceCookTime;
        this.lastBurnTime = this.furnace.furnaceBurnTime;
        this.lastItemBurnTime = this.furnace.currentItemBurnTime;
    }

    @SideOnly(value=Side.CLIENT)
    public void func_75137_b(int par1, int par2) {
        if (par1 == 0) {
            this.furnace.furnaceCookTime = par2;
        }
        if (par1 == 1) {
            this.furnace.furnaceBurnTime = par2;
        }
        if (par1 == 2) {
            this.furnace.currentItemBurnTime = par2;
        }
    }

    public boolean func_75145_c(EntityPlayer par1EntityPlayer) {
        return this.furnace.func_70300_a(par1EntityPlayer);
    }

    public ItemStack func_82846_b(EntityPlayer par1EntityPlayer, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.field_75151_b.get(par2);
        if (slot != null && slot.func_75216_d()) {
            ItemStack itemstack1 = slot.func_75211_c();
            itemstack = itemstack1.func_77946_l();
            if (par2 == 2) {
                if (!this.func_75135_a(itemstack1, 3, 39, true)) {
                    return null;
                }
                slot.func_75220_a(itemstack1, itemstack);
            } else if (par2 != 1 && par2 != 0 ? (FurnaceRecipes.func_77602_a().func_151395_a(itemstack1) != null ? !this.func_75135_a(itemstack1, 0, 1, false) : (TileEntityCrystalFurnace.isItemFuel((ItemStack)itemstack1) ? !this.func_75135_a(itemstack1, 1, 2, false) : (par2 >= 3 && par2 < 30 ? !this.func_75135_a(itemstack1, 30, 39, false) : par2 >= 30 && par2 < 39 && !this.func_75135_a(itemstack1, 3, 30, false)))) : !this.func_75135_a(itemstack1, 3, 39, false)) {
                return null;
            }
            if (itemstack1.field_77994_a == 0) {
                slot.func_75215_d((ItemStack)null);
            } else {
                slot.func_75218_e();
            }
            if (itemstack1.field_77994_a == itemstack.field_77994_a) {
                return null;
            }
            slot.func_82870_a(par1EntityPlayer, itemstack1);
        }
        return itemstack;
    }
}

