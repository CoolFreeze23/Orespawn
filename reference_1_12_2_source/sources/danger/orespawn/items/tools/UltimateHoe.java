/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.ParametersAreNonnullByDefault
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockFarmland
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Enchantments
 *  net.minecraft.init.SoundEvents
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemHoe
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.EnumActionResult
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.EnumHand
 *  net.minecraft.util.SoundCategory
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.World
 *  net.minecraftforge.event.ForgeEventFactory
 */
package danger.orespawn.items.tools;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.init.ModItems;
import danger.orespawn.items.tools.OrespawnToolMaterial;
import java.util.ArrayList;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class UltimateHoe
extends ItemHoe {
    public UltimateHoe() {
        super(OrespawnToolMaterial.UltimateTools.Material);
        this.func_77655_b("ultimate_hoe");
        this.setRegistryName("ultimate_hoe");
        this.func_77637_a(OreSpawnMain.OreSpawnTab);
        ModItems.ITEMS.add((Item)this);
    }

    @ParametersAreNonnullByDefault
    public void func_77663_a(ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) {
        if (!stack.func_77948_v()) {
            stack.func_77966_a(Enchantments.field_185305_q, 2);
        }
        super.func_77663_a(stack, world, player, itemSlot, isSelected);
    }

    public EnumActionResult func_180614_a(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.func_184586_b(hand);
        if (!player.func_175151_a(pos.func_177972_a(facing), facing, itemstack)) {
            return EnumActionResult.FAIL;
        }
        int hook = ForgeEventFactory.onHoeUse((ItemStack)itemstack, (EntityPlayer)player, (World)worldIn, (BlockPos)pos);
        if (hook != 0) {
            return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
        IBlockState iblockstate = worldIn.func_180495_p(pos);
        Block original = iblockstate.func_177230_c();
        if (facing == EnumFacing.DOWN || !worldIn.func_175623_d(pos.func_177984_a())) {
            return EnumActionResult.PASS;
        }
        if (original != Blocks.field_150346_d && original != Blocks.field_150349_c) {
            return EnumActionResult.PASS;
        }
        worldIn.func_175656_a(pos, Blocks.field_150458_ak.func_176223_P().func_177226_a((IProperty)BlockFarmland.field_176531_a, (Comparable)Integer.valueOf(7)));
        ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>();
        blockPositions.add(new BlockPos(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p()));
        blockPositions.add(new BlockPos(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p() + 1));
        blockPositions.add(new BlockPos(pos.func_177958_n() + 1, pos.func_177956_o(), pos.func_177952_p() - 1));
        blockPositions.add(new BlockPos(pos.func_177958_n() - 1, pos.func_177956_o(), pos.func_177952_p()));
        blockPositions.add(new BlockPos(pos.func_177958_n() - 1, pos.func_177956_o(), pos.func_177952_p() + 1));
        blockPositions.add(new BlockPos(pos.func_177958_n() - 1, pos.func_177956_o(), pos.func_177952_p() - 1));
        blockPositions.add(new BlockPos(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p() + 1));
        blockPositions.add(new BlockPos(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p() - 1));
        for (BlockPos blockPosition : blockPositions) {
            if (worldIn.func_180495_p(blockPosition).func_177230_c() == Blocks.field_150349_c || worldIn.func_180495_p(blockPosition).func_177230_c() == Blocks.field_150346_d) {
                BlockPos abovePos2;
                Block blockAbove2;
                BlockPos abovePos = new BlockPos(blockPosition.func_177958_n(), blockPosition.func_177956_o() + 1, blockPosition.func_177952_p());
                Block blockAbove = worldIn.func_180495_p(abovePos).func_177230_c();
                if (blockAbove == Blocks.field_150350_a) {
                    worldIn.func_175656_a(blockPosition, Blocks.field_150458_ak.func_176223_P().func_177226_a((IProperty)BlockFarmland.field_176531_a, (Comparable)Integer.valueOf(7)));
                    continue;
                }
                if (blockAbove != Blocks.field_150349_c && blockAbove != Blocks.field_150346_d || (blockAbove2 = worldIn.func_180495_p(abovePos2 = new BlockPos(abovePos.func_177958_n(), abovePos.func_177956_o() + 1, abovePos.func_177952_p())).func_177230_c()) != Blocks.field_150350_a) continue;
                worldIn.func_175656_a(abovePos, Blocks.field_150458_ak.func_176223_P().func_177226_a((IProperty)BlockFarmland.field_176531_a, (Comparable)Integer.valueOf(7)));
                continue;
            }
            if (worldIn.func_180495_p(blockPosition).func_177230_c() != Blocks.field_150350_a) continue;
            BlockPos underPos = new BlockPos(blockPosition.func_177958_n(), blockPosition.func_177956_o() - 1, blockPosition.func_177952_p());
            Block blockUnder = worldIn.func_180495_p(underPos).func_177230_c();
            if (worldIn.func_180495_p(underPos).func_177230_c() != Blocks.field_150346_d && worldIn.func_180495_p(underPos).func_177230_c() != Blocks.field_150349_c) continue;
            worldIn.func_175656_a(underPos, Blocks.field_150458_ak.func_176223_P().func_177226_a((IProperty)BlockFarmland.field_176531_a, (Comparable)Integer.valueOf(7)));
        }
        worldIn.func_184133_a(player, pos, SoundEvents.field_187693_cj, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return EnumActionResult.SUCCESS;
    }
}

