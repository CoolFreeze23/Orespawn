/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.EnumActionResult
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.World
 *  net.minecraftforge.common.EnumPlantType
 *  net.minecraftforge.common.IPlantable
 */
package danger.orespawn.items;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemButterflySeed
extends Item
implements IPlantable {
    public ItemButterflySeed() {
        this.func_77655_b("butterfly_seed");
        this.setRegistryName("butterfly_seed");
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add(this);
    }

    public EnumActionResult func_180614_a(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.func_184586_b(hand);
        IBlockState state = worldIn.func_180495_p(pos);
        if (facing == EnumFacing.UP && player.func_175151_a(pos.func_177972_a(facing), facing, stack) && state.func_177230_c().canSustainPlant(state, (IBlockAccess)worldIn, pos, EnumFacing.UP, (IPlantable)this) && worldIn.func_175623_d(pos.func_177984_a())) {
            worldIn.func_175656_a(pos.func_177984_a(), ModBlocks.BUTTERFLY_PLANT.func_176223_P());
            if (!player.field_71075_bZ.field_75098_d && !worldIn.field_72995_K) {
                stack.func_190918_g(1);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return ModBlocks.BUTTERFLY_PLANT.func_176223_P();
    }
}

