/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import danger.orespawn.CrystalFurnace;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/*
 * Exception performing whole class analysis ignored.
 */
public class TileEntityCrystalFurnace
extends TileEntity
implements ISidedInventory {
    private static final int[] slots_top = new int[]{0};
    private static final int[] slots_bottom = new int[]{2, 1};
    private static final int[] slots_sides = new int[]{1};
    private ItemStack[] furnaceItemStacks = new ItemStack[3];
    public int furnaceBurnTime;
    public int currentItemBurnTime;
    public int furnaceCookTime;
    private String field_94130_e;

    public int func_70302_i_() {
        return this.furnaceItemStacks.length;
    }

    public ItemStack func_70301_a(int par1) {
        return this.furnaceItemStacks[par1];
    }

    public ItemStack func_70298_a(int par1, int par2) {
        if (this.furnaceItemStacks[par1] != null) {
            if (this.furnaceItemStacks[par1].field_77994_a <= par2) {
                ItemStack itemstack = this.furnaceItemStacks[par1];
                this.furnaceItemStacks[par1] = null;
                return itemstack;
            }
            ItemStack itemstack = this.furnaceItemStacks[par1].func_77979_a(par2);
            if (this.furnaceItemStacks[par1].field_77994_a == 0) {
                this.furnaceItemStacks[par1] = null;
            }
            return itemstack;
        }
        return null;
    }

    public ItemStack func_70304_b(int par1) {
        if (this.furnaceItemStacks[par1] != null) {
            ItemStack itemstack = this.furnaceItemStacks[par1];
            this.furnaceItemStacks[par1] = null;
            return itemstack;
        }
        return null;
    }

    public void func_70299_a(int par1, ItemStack par2ItemStack) {
        this.furnaceItemStacks[par1] = par2ItemStack;
        if (par2ItemStack != null && par2ItemStack.field_77994_a > this.func_70297_j_()) {
            par2ItemStack.field_77994_a = this.func_70297_j_();
        }
    }

    public String func_145825_b() {
        return this.func_145818_k_() ? this.field_94130_e : "container.furnace";
    }

    public boolean func_145818_k_() {
        return this.field_94130_e != null && this.field_94130_e.length() > 0;
    }

    public void func_145951_a(String par1Str) {
        this.field_94130_e = par1Str;
    }

    public void func_145839_a(NBTTagCompound par1NBTTagCompound) {
        super.func_145839_a(par1NBTTagCompound);
        NBTTagList nbttaglist = par1NBTTagCompound.func_150295_c("Items", 10);
        this.furnaceItemStacks = new ItemStack[this.func_70302_i_()];
        for (int i = 0; i < nbttaglist.func_74745_c(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.func_150305_b(i);
            byte b0 = nbttagcompound1.func_74771_c("Slot");
            if (b0 < 0 || b0 >= this.furnaceItemStacks.length) continue;
            this.furnaceItemStacks[b0] = ItemStack.func_77949_a((NBTTagCompound)nbttagcompound1);
        }
        this.furnaceBurnTime = par1NBTTagCompound.func_74765_d("BurnTime");
        this.furnaceCookTime = par1NBTTagCompound.func_74765_d("CookTime");
        this.currentItemBurnTime = TileEntityCrystalFurnace.getItemBurnTime((ItemStack)this.furnaceItemStacks[1]);
        if (par1NBTTagCompound.func_74764_b("CustomName")) {
            this.field_94130_e = par1NBTTagCompound.func_74779_i("CustomName");
        }
    }

    public void func_145841_b(NBTTagCompound par1NBTTagCompound) {
        super.func_145841_b(par1NBTTagCompound);
        par1NBTTagCompound.func_74777_a("BurnTime", (short)this.furnaceBurnTime);
        par1NBTTagCompound.func_74777_a("CookTime", (short)this.furnaceCookTime);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.furnaceItemStacks.length; ++i) {
            if (this.furnaceItemStacks[i] == null) continue;
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.func_74774_a("Slot", (byte)i);
            this.furnaceItemStacks[i].func_77955_b(nbttagcompound1);
            nbttaglist.func_74742_a((NBTBase)nbttagcompound1);
        }
        par1NBTTagCompound.func_74782_a("Items", (NBTBase)nbttaglist);
        if (this.func_145818_k_()) {
            par1NBTTagCompound.func_74778_a("CustomName", this.field_94130_e);
        }
    }

    public int func_70297_j_() {
        return 64;
    }

    @SideOnly(value=Side.CLIENT)
    public int getCookProgressScaled(int par1) {
        return this.furnaceCookTime * par1 / 150;
    }

    @SideOnly(value=Side.CLIENT)
    public int getBurnTimeRemainingScaled(int par1) {
        if (this.currentItemBurnTime == 0) {
            this.currentItemBurnTime = 150;
        }
        return this.furnaceBurnTime * par1 / this.currentItemBurnTime;
    }

    public boolean isBurning() {
        return this.furnaceBurnTime > 0;
    }

    public void func_145845_h() {
        boolean flag = this.furnaceBurnTime > 0;
        boolean flag1 = false;
        if (this.furnaceBurnTime > 0) {
            --this.furnaceBurnTime;
        }
        if (!this.field_145850_b.field_72995_K) {
            if (this.furnaceBurnTime == 0 && this.canSmelt()) {
                this.currentItemBurnTime = this.furnaceBurnTime = TileEntityCrystalFurnace.getItemBurnTime((ItemStack)this.furnaceItemStacks[1]);
                if (this.furnaceBurnTime > 0) {
                    flag1 = true;
                    if (this.furnaceItemStacks[1] != null) {
                        --this.furnaceItemStacks[1].field_77994_a;
                        if (this.furnaceItemStacks[1].field_77994_a == 0) {
                            this.furnaceItemStacks[1] = this.furnaceItemStacks[1].func_77973_b().getContainerItem(this.furnaceItemStacks[1]);
                        }
                    }
                }
            }
            if (this.isBurning() && this.canSmelt()) {
                ++this.furnaceCookTime;
                if (this.furnaceCookTime == 150) {
                    this.furnaceCookTime = 0;
                    this.smeltItem();
                    flag1 = true;
                }
            } else {
                this.furnaceCookTime = 0;
            }
            if (flag != this.furnaceBurnTime > 0) {
                flag1 = true;
                CrystalFurnace.updateFurnaceBlockState((this.furnaceBurnTime > 0 ? 1 : 0) != 0, (World)this.field_145850_b, (int)this.field_145851_c, (int)this.field_145848_d, (int)this.field_145849_e);
            }
        }
        if (flag1) {
            this.func_70296_d();
        }
    }

    private boolean canSmelt() {
        if (this.furnaceItemStacks[0] == null) {
            return false;
        }
        ItemStack itemstack = FurnaceRecipes.func_77602_a().func_151395_a(this.furnaceItemStacks[0]);
        if (itemstack == null) {
            return false;
        }
        if (this.furnaceItemStacks[2] == null) {
            return true;
        }
        if (!this.furnaceItemStacks[2].func_77969_a(itemstack)) {
            return false;
        }
        int result = this.furnaceItemStacks[2].field_77994_a + itemstack.field_77994_a;
        return result <= this.func_70297_j_() && result <= itemstack.func_77976_d();
    }

    public void smeltItem() {
        if (this.canSmelt()) {
            ItemStack itemstack = FurnaceRecipes.func_77602_a().func_151395_a(this.furnaceItemStacks[0]);
            if (this.furnaceItemStacks[2] == null) {
                this.furnaceItemStacks[2] = itemstack.func_77946_l();
            } else if (this.furnaceItemStacks[2].func_77969_a(itemstack)) {
                this.furnaceItemStacks[2].field_77994_a += itemstack.field_77994_a;
            }
            --this.furnaceItemStacks[0].field_77994_a;
            if (this.furnaceItemStacks[0].field_77994_a <= 0) {
                this.furnaceItemStacks[0] = null;
            }
        }
    }

    public static int getItemBurnTime(ItemStack par0ItemStack) {
        if (par0ItemStack == null) {
            return 0;
        }
        Item item = par0ItemStack.func_77973_b();
        if (item instanceof ItemBlock && Block.func_149634_a((Item)item) != Blocks.field_150350_a) {
            Block block = Block.func_149634_a((Item)item);
            if (block == Blocks.field_150376_bx) {
                return 150;
            }
            if (block.func_149688_o() == Material.field_151575_d) {
                return 300;
            }
            if (block == Blocks.field_150402_ci) {
                return 16000;
            }
        }
        if (item instanceof ItemTool && ((ItemTool)item).func_77861_e().equals("WOOD")) {
            return 200;
        }
        if (item instanceof ItemSword && ((ItemSword)item).func_150932_j().equals("WOOD")) {
            return 200;
        }
        if (item instanceof ItemHoe && ((ItemHoe)item).func_77842_f().equals("WOOD")) {
            return 200;
        }
        if (item == Items.field_151055_y) {
            return 100;
        }
        if (item == Items.field_151044_h) {
            return 1600;
        }
        if (item == Items.field_151129_at) {
            return 20000;
        }
        if (item == Item.func_150898_a((Block)Blocks.field_150345_g)) {
            return 100;
        }
        if (item == Items.field_151072_bj) {
            return 2400;
        }
        if (item == Item.func_150898_a((Block)OreSpawnMain.CrystalCoal)) {
            return 20000;
        }
        if (item == Item.func_150898_a((Block)OreSpawnMain.MyCrystalTreeLog)) {
            return 800;
        }
        if (item == Item.func_150898_a((Block)OreSpawnMain.CrystalPlanksBlock)) {
            return 400;
        }
        return GameRegistry.getFuelValue((ItemStack)par0ItemStack);
    }

    public static boolean isItemFuel(ItemStack par0ItemStack) {
        return TileEntityCrystalFurnace.getItemBurnTime((ItemStack)par0ItemStack) > 0;
    }

    public boolean func_70300_a(EntityPlayer par1EntityPlayer) {
        return this.field_145850_b.func_147438_o(this.field_145851_c, this.field_145848_d, this.field_145849_e) != this ? false : par1EntityPlayer.func_70092_e((double)this.field_145851_c + 0.5, (double)this.field_145848_d + 0.5, (double)this.field_145849_e + 0.5) <= 64.0;
    }

    public void openChest() {
    }

    public void closeChest() {
    }

    public boolean func_94041_b(int par1, ItemStack par2ItemStack) {
        return par1 == 2 ? false : (par1 == 1 ? TileEntityCrystalFurnace.isItemFuel((ItemStack)par2ItemStack) : true);
    }

    public int[] func_94128_d(int par1) {
        return par1 == 0 ? slots_bottom : (par1 == 1 ? slots_top : slots_sides);
    }

    public boolean func_102007_a(int par1, ItemStack par2ItemStack, int par3) {
        return this.func_94041_b(par1, par2ItemStack);
    }

    public boolean func_102008_b(int par1, ItemStack par2ItemStack, int par3) {
        return par3 != 0 || par1 != 1 || par2ItemStack.func_77973_b() == Items.field_151133_ar;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }
}

