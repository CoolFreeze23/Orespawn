/*
 * Decompiled with CFR 0.152.
 */
package danger.orespawn;

import danger.orespawn.EntityThrownRock;
import danger.orespawn.OreSpawnMain;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

final class MyDispenserBehaviorRock
extends BehaviorProjectileDispense {
    MyDispenserBehaviorRock() {
    }

    public ItemStack func_82487_b(IBlockSource par1IBlockSource, ItemStack par2ItemStack) {
        World world = par1IBlockSource.func_82618_k();
        IPosition iposition = BlockDispenser.func_149939_a((IBlockSource)par1IBlockSource);
        EnumFacing enumfacing = BlockDispenser.func_149937_b((int)par1IBlockSource.func_82620_h());
        IProjectile iprojectile = this.func_82499_a(world, iposition);
        iprojectile.func_70186_c((double)enumfacing.func_82601_c(), (double)((float)enumfacing.func_96559_d() + 0.1f), (double)enumfacing.func_82599_e(), this.func_82500_b(), this.func_82498_a());
        EntityThrownRock r = (EntityThrownRock)iprojectile;
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MySmallRock) {
            r.setRockType(1);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyRock) {
            r.setRockType(2);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyRedRock) {
            r.setRockType(3);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyGreenRock) {
            r.setRockType(4);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyBlueRock) {
            r.setRockType(5);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyPurpleRock) {
            r.setRockType(6);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MySpikeyRock) {
            r.setRockType(7);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyTNTRock) {
            r.setRockType(8);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyCrystalRedRock) {
            r.setRockType(9);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyCrystalGreenRock) {
            r.setRockType(10);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyCrystalBlueRock) {
            r.setRockType(11);
        }
        if (par2ItemStack.func_77973_b() == OreSpawnMain.MyCrystalTNTRock) {
            r.setRockType(12);
        }
        world.func_72838_d((Entity)iprojectile);
        par2ItemStack.func_77979_a(1);
        return par2ItemStack;
    }

    protected IProjectile func_82499_a(World par1World, IPosition par2IPosition) {
        EntityThrownRock entityarrow = new EntityThrownRock(par1World, par2IPosition.func_82615_a(), par2IPosition.func_82617_b(), par2IPosition.func_82616_c());
        return entityarrow;
    }
}

