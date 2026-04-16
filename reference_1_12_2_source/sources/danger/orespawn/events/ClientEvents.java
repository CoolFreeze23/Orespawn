/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.block.Block
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.client.renderer.color.IBlockColor
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.text.ITextComponent
 *  net.minecraft.util.text.TextComponentString
 *  net.minecraft.util.text.TextFormatting
 *  net.minecraft.world.ColorizerGrass
 *  net.minecraft.world.IBlockAccess
 *  net.minecraftforge.client.event.ColorHandlerEvent$Block
 *  net.minecraftforge.client.event.ColorHandlerEvent$Item
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.fml.relauncher.Side
 */
package danger.orespawn.events;

import danger.orespawn.init.ModBlocks;
import danger.orespawn.util.premium.PremiumChecker;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid="orespawn", value={Side.CLIENT})
public class ClientEvents {
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        boolean premium = PremiumChecker.CheckUser(event.player);
        if (premium) {
            TextComponentString message = new TextComponentString("Welcome premium user!");
            message.func_150256_b().func_150238_a(TextFormatting.GOLD);
            event.player.func_145747_a((ITextComponent)message);
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().func_186722_a(new IBlockColor(){

            public int func_186720_a(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
                double temperature = worldIn.func_180494_b(pos).func_180626_a(pos);
                double humidity = worldIn.func_180494_b(pos).func_76727_i();
                if (temperature < 0.0) {
                    temperature = 0.0;
                } else if (temperature > 1.0) {
                    temperature = 1.0;
                }
                if (humidity < 0.0) {
                    humidity = 0.0;
                } else if (humidity > 1.0) {
                    humidity = 1.0;
                }
                return ColorizerGrass.func_77480_a((double)temperature, (double)humidity);
            }
        }, new Block[]{ModBlocks.ANT_BLOCK});
    }

    @SubscribeEvent
    public static void registerItemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().func_186731_a((stack, tintIndex) -> ColorizerGrass.func_77480_a((double)0.5, (double)0.5), new Block[]{ModBlocks.ANT_BLOCK});
    }
}

