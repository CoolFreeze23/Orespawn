package danger.orespawn.item;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

/**
 * Sifter, ported from 1.7.10 ItemSifter.java:35-474. Right-clicking water,
 * sand, gravel, dirt or grass rolls that substrate's weighted table and
 * tosses the result on the ground near the clicked block; most rolls yield
 * nothing. Water uses a 160-roll table (41 winning entries, incl. the six
 * mod fish, the four shoes and gated emerald/ruby/amethyst/diamond rolls);
 * the solid substrates use 60-roll tables with 12-15 winning entries each.
 * Costs 1 durability per use.
 */
public class ItemSifter extends Item {
    public ItemSifter(Item.Properties properties) {
        super(properties);
    }

    /** orig ItemSifter.java:30-33 — drop at clicked pos ± nextInt(2) on x/z, y + 1.1. */
    private static void dropItemRand(ItemLike item, int count, Level level, BlockPos pos) {
        RandomSource rand = level.random;
        double x = pos.getX() + rand.nextInt(2) - rand.nextInt(2) + 0.5;
        double z = pos.getZ() + rand.nextInt(2) - rand.nextInt(2) + 0.5;
        level.addFreshEntity(new ItemEntity(level, x, pos.getY() + 1.1, z, new ItemStack(item, count)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        RandomSource rand = level.random;

        // orig ItemSifter.java:40-47 — water above the clicked block counts as
        // sifting water (covers clicking the bottom of a lake/river)
        BlockState above = level.getBlockState(pos.above());
        boolean water = state.is(Blocks.WATER) || above.is(Blocks.WATER);

        if (water) {
            // orig ItemSifter.java:49-231 — nextInt(160), cases 0..40 win
            switch (rand.nextInt(160)) {
                case 0, 34 -> dropItemRand(Items.COD, 1, level, pos);                              // orig :52,204 — raw fish
                case 1 -> dropItemRand(ModItems.GREEN_FISH.get(), 1, level, pos);                  // orig :56
                case 2 -> dropItemRand(ModItems.BLUE_FISH.get(), 1, level, pos);                   // orig :60
                case 3 -> dropItemRand(ModItems.PINK_FISH.get(), 1, level, pos);                   // orig :64
                case 4 -> dropItemRand(ModItems.ROCK_FISH.get(), 1, level, pos);                   // orig :68
                case 5 -> dropItemRand(ModItems.WOOD_FISH.get(), 1, level, pos);                   // orig :72
                case 6 -> dropItemRand(ModItems.GREY_FISH.get(), 1, level, pos);                   // orig :76
                case 7, 14, 35 -> dropItemRand(Items.GLASS_BOTTLE, 1, level, pos);                 // orig :80,108,208
                case 8, 26 -> dropItemRand(Items.IRON_INGOT, 1, level, pos);                       // orig :84,172
                case 9, 27 -> dropItemRand(Items.GOLD_NUGGET, 1, level, pos);                      // orig :88,176
                case 10, 30 -> dropItemRand(ModItems.RED_HEELS.get(), 1, level, pos);              // orig :92,188
                case 11, 31 -> dropItemRand(ModItems.BLACK_HEELS.get(), 1, level, pos);            // orig :96,192
                case 12, 32 -> dropItemRand(ModItems.SLIPPERS.get(), 1, level, pos);               // orig :100,196
                case 13, 33 -> dropItemRand(ModItems.BOOTS_SHOES.get(), 1, level, pos);            // orig :104,200
                case 15, 36 -> dropItemRand(Items.BONE, 1, level, pos);                            // orig :112,212
                case 16, 37 -> dropItemRand(Blocks.STONE, 1, level, pos);                          // orig :116,216
                case 17, 39 -> dropItemRand(Items.BUCKET, 1, level, pos);                          // orig :120,224
                case 18, 40 -> dropItemRand(Items.WATER_BUCKET, 1, level, pos);                    // orig :124,228
                case 19 -> {                                                                       // orig :127-134 — 1/3 emerald else gravel
                    if (rand.nextInt(3) == 1) dropItemRand(Items.EMERALD, 1, level, pos);
                    else dropItemRand(Blocks.GRAVEL, 1, level, pos);
                }
                case 20 -> {                                                                       // orig :135-142 — 1/3 ruby else gravel
                    if (rand.nextInt(3) == 1) dropItemRand(ModItems.RUBY.get(), 1, level, pos);
                    else dropItemRand(Blocks.GRAVEL, 1, level, pos);
                }
                case 21 -> {                                                                       // orig :143-150 — 1/3 amethyst else gravel
                    if (rand.nextInt(3) == 1) dropItemRand(ModItems.AMETHYST_GEM.get(), 1, level, pos);
                    else dropItemRand(Blocks.GRAVEL, 1, level, pos);
                }
                case 22 -> dropItemRand(ModItems.MOTH_SCALE.get(), 1, level, pos);                 // orig :152
                case 23 -> dropItemRand(ModItems.URANIUM_NUGGET.get(), 1, level, pos);             // orig :156
                case 24 -> dropItemRand(ModItems.TITANIUM_NUGGET.get(), 1, level, pos);            // orig :160
                case 25 -> {                                                                       // orig :163-170 — 1/2 diamond else gravel
                    if (rand.nextInt(2) == 1) dropItemRand(Items.DIAMOND, 1, level, pos);
                    else dropItemRand(Blocks.GRAVEL, 1, level, pos);
                }
                case 28 -> dropItemRand(Items.REDSTONE, 1, level, pos);                            // orig :180
                case 29 -> dropItemRand(Items.COAL, 1, level, pos);                                // orig :184
                case 38 -> dropItemRand(Blocks.STONE_BUTTON, 1, level, pos);                       // orig :220
                default -> { }
            }
        } else if (state.is(Blocks.SAND)) {
            // orig ItemSifter.java:233-293 — nextInt(60), cases 0..13 win
            switch (rand.nextInt(60)) {
                case 0 -> dropItemRand(Items.IRON_HORSE_ARMOR, 1, level, pos);                     // orig :237
                case 1 -> dropItemRand(Items.SHEARS, 1, level, pos);                               // orig :241
                case 2 -> dropItemRand(Items.CARROT_ON_A_STICK, 1, level, pos);                    // orig :245
                case 3 -> dropItemRand(Items.POISONOUS_POTATO, 1, level, pos);                     // orig :249
                case 4 -> dropItemRand(Items.ITEM_FRAME, 1, level, pos);                           // orig :253
                case 5 -> dropItemRand(Items.BONE, 1, level, pos);                                 // orig :257
                case 6 -> dropItemRand(Items.COMPASS, 1, level, pos);                              // orig :261
                case 7 -> dropItemRand(Items.GLASS_BOTTLE, 1, level, pos);                         // orig :265
                case 8 -> dropItemRand(Items.SADDLE, 1, level, pos);                               // orig :269
                case 9 -> dropItemRand(Items.IRON_HELMET, 1, level, pos);                          // orig :273
                case 10 -> dropItemRand(Items.IRON_CHESTPLATE, 1, level, pos);                     // orig :277
                case 11 -> dropItemRand(Items.IRON_LEGGINGS, 1, level, pos);                       // orig :281
                case 12 -> dropItemRand(Items.IRON_BOOTS, 1, level, pos);                          // orig :285
                case 13 -> dropItemRand(Blocks.SAND, 1, level, pos);                               // orig :289
                default -> { }
            }
        } else if (state.is(Blocks.GRAVEL)) {
            // orig ItemSifter.java:294-346 — nextInt(60), cases 0..11 win
            switch (rand.nextInt(60)) {
                case 0 -> dropItemRand(Items.FLINT, 1, level, pos);                                // orig :298
                case 1 -> dropItemRand(ModItems.SALT.get(), 1, level, pos);                        // orig :302
                case 2 -> dropItemRand(Items.FLINT_AND_STEEL, 1, level, pos);                      // orig :306
                case 3 -> dropItemRand(Items.SPIDER_EYE, 1, level, pos);                           // orig :310
                case 4 -> dropItemRand(Items.ITEM_FRAME, 1, level, pos);                           // orig :314
                case 5 -> dropItemRand(Items.FEATHER, 1, level, pos);                              // orig :318
                case 6 -> dropItemRand(Items.STRING, 1, level, pos);                               // orig :322
                case 7 -> dropItemRand(Items.GLASS_BOTTLE, 1, level, pos);                         // orig :326
                case 8 -> dropItemRand(Items.LEAD, 1, level, pos);                                 // orig :330
                case 9 -> dropItemRand(Items.NAME_TAG, 1, level, pos);                             // orig :334
                case 10 -> dropItemRand(Blocks.SAND, 1, level, pos);                               // orig :338
                case 11 -> dropItemRand(Blocks.GRAVEL, 1, level, pos);                             // orig :342
                default -> { }
            }
        } else if (state.is(Blocks.DIRT)) {
            // orig ItemSifter.java:347-407 — nextInt(60), cases 0..13 win
            switch (rand.nextInt(60)) {
                case 0 -> dropItemRand(Items.STRING, 1, level, pos);                               // orig :351
                case 1 -> dropItemRand(ModItems.SALT.get(), 1, level, pos);                        // orig :355
                case 2 -> dropItemRand(Items.SHEARS, 1, level, pos);                               // orig :359
                case 3 -> dropItemRand(Items.STICK, 1, level, pos);                                // orig :363
                case 4 -> dropItemRand(Items.BOWL, 1, level, pos);                                 // orig :367
                case 5 -> dropItemRand(Items.FLOWER_POT, 1, level, pos);                           // orig :371
                case 6 -> dropItemRand(Items.OAK_SIGN, 1, level, pos);                             // orig :375
                case 7 -> dropItemRand(Items.BRICK, 1, level, pos);                                // orig :379
                case 8 -> dropItemRand(Items.PAPER, 1, level, pos);                                // orig :383
                case 9 -> dropItemRand(Items.BONE, 1, level, pos);                                 // orig :387
                case 10 -> dropItemRand(Items.GLASS_BOTTLE, 1, level, pos);                        // orig :391
                case 11 -> dropItemRand(Blocks.SAND, 1, level, pos);                               // orig :395
                case 12 -> dropItemRand(Blocks.GRAVEL, 1, level, pos);                             // orig :399
                case 13 -> dropItemRand(Blocks.DIRT, 1, level, pos);                               // orig :403
                default -> { }
            }
        } else if (state.is(Blocks.GRASS_BLOCK)) {
            // orig ItemSifter.java:408-471 — nextInt(60), cases 0..14 win
            int roll = rand.nextInt(60);
            switch (roll) {
                case 0 -> dropItemRand(Blocks.DANDELION, 1, level, pos);                           // orig :412
                case 1 -> dropItemRand(Blocks.POPPY, 1, level, pos);                               // orig :416
                case 2 -> dropItemRand(ModBlocks.FLOWER_PINK.get(), 1, level, pos);                // orig :420
                case 3 -> dropItemRand(ModBlocks.FLOWER_BLUE.get(), 1, level, pos);                // orig :424
                case 4 -> dropItemRand(ModBlocks.FLOWER_BLACK.get(), 1, level, pos);               // orig :428
                case 5, 6 -> {
                    // orig :431-437 — case 5 has no break, so a scary-flower
                    // roll also drops wheat (decompiler-confirmed fallthrough)
                    if (roll == 5) dropItemRand(ModBlocks.FLOWER_SCARY.get(), 1, level, pos);
                    dropItemRand(Items.WHEAT, 1, level, pos);
                }
                case 7 -> dropItemRand(Items.PUMPKIN_SEEDS, 1, level, pos);                        // orig :439
                case 8 -> dropItemRand(Items.MELON_SEEDS, 1, level, pos);                          // orig :443
                case 9 -> dropItemRand(Items.CARROT, 1, level, pos);                               // orig :447
                case 10 -> dropItemRand(Items.POTATO, 1, level, pos);                              // orig :451
                case 11 -> dropItemRand(Blocks.DEAD_BUSH, 1, level, pos);                          // orig :455
                case 12 -> dropItemRand(Blocks.GRAVEL, 1, level, pos);                             // orig :459
                case 13 -> dropItemRand(Blocks.DIRT, 1, level, pos);                               // orig :463
                case 14 -> dropItemRand(Blocks.GRASS_BLOCK, 1, level, pos);                        // orig :467
                default -> { }
            }
        }

        // orig ItemSifter.java:472 — 1 durability per use regardless of substrate
        context.getItemInHand().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return InteractionResult.SUCCESS;
    }
}
