/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.block.ITileEntityProvider
 *  net.minecraft.block.SoundType
 *  net.minecraft.block.material.Material
 *  net.minecraft.block.properties.IProperty
 *  net.minecraft.block.properties.PropertyInteger
 *  net.minecraft.block.state.BlockFaceShape
 *  net.minecraft.block.state.BlockStateContainer
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.init.Blocks
 *  net.minecraft.init.Items
 *  net.minecraft.item.Item
 *  net.minecraft.tileentity.TileEntity
 *  net.minecraft.util.BlockRenderLayer
 *  net.minecraft.util.EnumFacing
 *  net.minecraft.util.math.AxisAlignedBB
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.IBlockAccess
 *  net.minecraft.world.World
 *  net.minecraftforge.common.EnumPlantType
 *  net.minecraftforge.common.ForgeHooks
 *  net.minecraftforge.common.IPlantable
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package danger.orespawn.blocks;

import danger.orespawn.OreSpawnMain;
import danger.orespawn.blocks.TileEntityPlant;
import danger.orespawn.init.ModBlocks;
import danger.orespawn.init.ModItems;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCornPlant
extends Block
implements IPlantable,
ITileEntityProvider {
    public static final PropertyInteger STAGE = PropertyInteger.func_177719_a((String)"stage", (int)0, (int)15);
    protected static final AxisAlignedBB CORN_AABB = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 1.0, 0.875);

    public BlockCornPlant() {
        super(Material.field_151585_k);
        this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a((IProperty)STAGE, (Comparable)Integer.valueOf(0)));
        this.func_149663_c("corn_plant");
        this.setRegistryName("corn_plant");
        this.func_149647_a(OreSpawnMain.OreSpawnTab);
        this.func_149675_a(true);
        this.func_149672_a(SoundType.field_185850_c);
        ModBlocks.BLOCKS.add(this);
    }

    public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
        return CORN_AABB;
    }

    public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.func_180495_p(pos.func_177984_a()).func_177230_c() == Blocks.field_150350_a && this.checkForDrop(worldIn, pos, state)) {
            int height = 0;
            int maxHeight = 21;
            int i = 1;
            while (worldIn.func_180495_p(pos.func_177979_c(i)).func_177230_c() == this) {
                BlockPos position = pos.func_177979_c(i);
                BlockCornPlant plant = (BlockCornPlant)worldIn.func_180495_p(position).func_177230_c();
                TileEntityPlant tileEntity = (TileEntityPlant)worldIn.func_175625_s(pos);
                height += tileEntity.getHeightContribution();
                ++i;
            }
            if (ForgeHooks.onCropsGrowPre((World)worldIn, (BlockPos)pos, (IBlockState)state, (boolean)true)) {
                if (height < maxHeight) {
                    TileEntityPlant tileEntity = (TileEntityPlant)worldIn.func_175625_s(pos);
                    if (tileEntity.getAge() == 15) {
                        tileEntity.setAge(0);
                        IBlockState currentState = worldIn.func_180495_p(pos);
                        worldIn.func_175656_a(pos.func_177984_a(), this.func_176223_P());
                        worldIn.func_175656_a(pos, (Integer)currentState.func_177229_b((IProperty)STAGE) != 0 ? currentState : state.func_177226_a((IProperty)STAGE, (Comparable)Integer.valueOf(1)));
                    } else {
                        tileEntity.setAge(tileEntity.getAge() + 1);
                    }
                } else {
                    TileEntityPlant tileEntity = (TileEntityPlant)worldIn.func_175625_s(pos);
                    if (tileEntity.getAge() == 15) {
                        int phase = tileEntity.getPhase();
                        if (phase != 4) {
                            int stage;
                            if (phase <= 1) {
                                tileEntity.setPhase(2);
                                phase = tileEntity.getPhase();
                            }
                            int i2 = 1;
                            while (worldIn.func_180495_p(pos.func_177979_c(i2)).func_177230_c() == this) {
                                ++i2;
                            }
                            --i2;
                            while (i2 != 0 && (stage = ((Integer)worldIn.func_180495_p(pos.func_177979_c(i2)).func_177229_b((IProperty)STAGE)).intValue()) >= phase) {
                                --i2;
                            }
                            if (i2 >= 1) {
                                worldIn.func_175656_a(pos.func_177979_c(i2), this.func_176223_P().func_177226_a((IProperty)STAGE, (Comparable)((Integer)worldIn.func_180495_p(pos.func_177979_c(i2)).func_177229_b((IProperty)STAGE) < 3 ? Integer.valueOf((Integer)worldIn.func_180495_p(pos.func_177979_c(i2)).func_177229_b((IProperty)STAGE) + 1) : (Integer)worldIn.func_180495_p(pos.func_177979_c(i2)).func_177229_b((IProperty)STAGE))));
                            } else {
                                tileEntity.setPhase(tileEntity.getPhase() + 1);
                            }
                        }
                    } else {
                        tileEntity.setAge(tileEntity.getAge() + 1);
                    }
                }
                ForgeHooks.onCropsGrowPost((World)worldIn, (BlockPos)pos, (IBlockState)state, (IBlockState)worldIn.func_180495_p(pos));
            }
        }
    }

    public boolean func_176196_c(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.func_180495_p(pos.func_177977_b());
        Block block = state.func_177230_c();
        if (block.canSustainPlant(state, (IBlockAccess)worldIn, pos.func_177977_b(), EnumFacing.UP, (IPlantable)this)) {
            return true;
        }
        return block == this || block == Blocks.field_150349_c || block == Blocks.field_150346_d || block == Blocks.field_150458_ak;
    }

    public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        this.checkForDrop(worldIn, pos, state);
    }

    protected final boolean checkForDrop(World worldIn, BlockPos pos, IBlockState state) {
        if (this.canBlockStay(worldIn, pos)) {
            return true;
        }
        this.func_176226_b(worldIn, pos, state, 0);
        worldIn.func_175698_g(pos);
        return false;
    }

    public boolean canBlockStay(World worldIn, BlockPos pos) {
        return this.func_176196_c(worldIn, pos);
    }

    public Item func_180660_a(IBlockState state, Random rand, int fortune) {
        if ((Integer)state.func_177229_b((IProperty)STAGE) == 3) {
            return ModItems.CORN;
        }
        return Items.field_190931_a;
    }

    public int func_149745_a(Random rand) {
        return 1 + rand.nextInt(2);
    }

    public boolean func_149662_c(IBlockState state) {
        return false;
    }

    public boolean func_149686_d(IBlockState state) {
        return false;
    }

    public BlockFaceShape func_193383_a(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Nullable
    public AxisAlignedBB func_180646_a(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return field_185506_k;
    }

    @SideOnly(value=Side.CLIENT)
    public BlockRenderLayer func_180664_k() {
        return BlockRenderLayer.CUTOUT;
    }

    public int func_176201_c(IBlockState state) {
        return (Integer)state.func_177229_b((IProperty)STAGE);
    }

    public IBlockState func_176203_a(int meta) {
        return this.func_176223_P().func_177226_a((IProperty)STAGE, (Comparable)Integer.valueOf(meta));
    }

    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Beach;
    }

    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return this.func_176223_P();
    }

    protected BlockStateContainer func_180661_e() {
        return new BlockStateContainer((Block)this, new IProperty[]{STAGE});
    }

    @Nullable
    public TileEntity func_149915_a(World worldIn, int meta) {
        return new TileEntityPlant();
    }

    public boolean isBlockUnderCorn(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.func_180495_p(pos.func_177977_b());
        Block block = state.func_177230_c();
        return block == ModBlocks.CORN_PLANT;
    }

    public boolean isBlockUnderAir(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.func_180495_p(pos.func_177977_b());
        Block block = state.func_177230_c();
        return block == Blocks.field_150350_a;
    }

    public boolean isBlockUnderGrass(World worldIn, BlockPos pos) {
        IBlockState state = worldIn.func_180495_p(pos.func_177977_b());
        Block block = state.func_177230_c();
        return block == Blocks.field_150349_c;
    }
}

