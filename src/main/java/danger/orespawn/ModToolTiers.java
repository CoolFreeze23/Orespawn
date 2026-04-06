package danger.orespawn;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {
    public static final Tier RUBY = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1500, 11.0f, 16.0f, 85, () -> Ingredient.of(ModItems.RUBY.get()));
    public static final Tier AMETHYST = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            2000, 11.0f, 11.0f, 70, () -> Ingredient.of(ModItems.AMETHYST_GEM.get()));
    public static final Tier EMERALD = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1300, 10.0f, 6.0f, 75, () -> Ingredient.EMPTY);
    public static final Tier ULTIMATE = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            3000, 15.0f, 36.0f, 100, () -> Ingredient.EMPTY);
    public static final Tier NIGHTMARE = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            1800, 12.0f, 26.0f, 60, () -> Ingredient.EMPTY);
    public static final Tier BERTHA = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            9000, 15.0f, 496.0f, 100, () -> Ingredient.EMPTY);
    public static final Tier ROYAL = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            10000, 15.0f, 746.0f, 150, () -> Ingredient.EMPTY);
    public static final Tier CRYSTAL_WOOD = new SimpleTier(BlockTags.INCORRECT_FOR_WOODEN_TOOL,
            300, 3.0f, 2.0f, 15, () -> Ingredient.EMPTY);
    public static final Tier CRYSTAL_STONE = new SimpleTier(BlockTags.INCORRECT_FOR_STONE_TOOL,
            800, 6.0f, 5.0f, 45, () -> Ingredient.EMPTY);
    public static final Tier CRYSTAL_PINK = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,
            1100, 10.0f, 7.0f, 65, () -> Ingredient.EMPTY);
    public static final Tier TIGERS_EYE = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,
            1600, 12.0f, 8.0f, 75, () -> Ingredient.EMPTY);
    public static final Tier HAMMY = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2000, 15.0f, 82.0f, 100, () -> Ingredient.EMPTY);
    public static final Tier BATTLE = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1500, 15.0f, 46.0f, 75, () -> Ingredient.EMPTY);
    public static final Tier CHAINSAW = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1500, 10.0f, 56.0f, 75, () -> Ingredient.EMPTY);
    public static final Tier QUEEN_BATTLE = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2200, 15.0f, 662.0f, 100, () -> Ingredient.EMPTY);
}
