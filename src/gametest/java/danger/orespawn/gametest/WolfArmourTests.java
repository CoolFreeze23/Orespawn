package danger.orespawn.gametest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import danger.orespawn.ModCreativeTabs;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnConfig;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.WolfArmourEvents;
import danger.orespawn.data.ModernCondition;
import danger.orespawn.item.ItemOreSpawnWolfArmor;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * MOD-041: wolf armour for the 14 armour sets. Each is vanilla's wolf armour (a canine AnimalArmorItem) on its set's
 * armour material, with the horse armour's values: the chestplate's armour value as its own (the material's body
 * value), the material's toughness and knockback resistance, no durability, one to a stack, drawn from
 * textures/entity/wolf/armor/wolf_armor_<material>. A tamed wolf's owner puts it on with a click and takes it off with
 * shears (WolfArmourEvents, on vanilla's terms; the player's interaction runs the event as the game does, and the
 * players are on the server's player list, where a wolf looks its owner up); the wolf's damage is cut by it and none
 * taken into it. modern.wolfArmour (with modern.enabled) loads the 13 recipes and the
 * King's drop of the Royal Guardian one and fills the creative tab; the King's challenge chest is checked in
 * StructureTestsA's King tower row.
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WolfArmourTests {

    /** The sets in the armour's order, with each one's armour material name. */
    private static final String[][] SETS = {
            {"ultimate", "ultimate"}, {"royal", "royal"}, {"queen", "queen"}, {"mobzilla", "mobzilla"},
            {"experience", "experience"}, {"ruby", "ruby"}, {"amethyst", "amethyst"}, {"emerald", "emerald"},
            {"lavaeel", "lava_eel"}, {"mothscale", "moth_scale"}, {"peacock", "peacock"}, {"pink", "pink"},
            {"tigerseye", "tigers_eye"}, {"lapis", "lapis"}};

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, path);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(id(path));
    }

    /**
     * A plain player on the server's player list in the game mode given (a wolf finds its owner among the level's
     * players), taken off the list when the test ends, passed or failed.
     */
    private static ServerPlayer player(GameTestHelper helper, GameType mode) {
        MinecraftServer server = helper.getLevel().getServer();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.randomUUID(), "test-wolf-player"), false);
        ServerPlayer player = new ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(mode);
        EntityLogicTestsA.onTestExit(helper, () -> server.getPlayerList().remove(player));
        return player;
    }

    /** A grown wolf tamed by the player, without free will so it stays put. */
    private static Wolf tamedWolf(GameTestHelper helper, ServerPlayer owner, BlockPos pos) {
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, pos);
        wolf.tame(owner);
        return wolf;
    }

    /** The player's click on the wolf with the stack in hand, as the game runs it (its interaction event first). */
    private static InteractionResult click(ServerPlayer player, Wolf wolf, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player.interactOn(wolf, InteractionHand.MAIN_HAND);
    }

    /**
     * Each wolf armour is a canine AnimalArmorItem on its set's material: the chestplate's armour value, toughness and
     * knockback resistance, no durability, one to a stack, its set's own texture; the list holds the 14 in the
     * armour's order.
     */
    @GameTest(template = "empty")
    public static void mod041a_each_wolf_armour_is_its_sets_material_on_a_wolf(GameTestHelper helper) {
        helper.assertValueEqual(ModItems.WOLF_ARMOR.size(), SETS.length, "wolf armours listed");
        for (int i = 0; i < SETS.length; i++) {
            String set = SETS[i][0];
            Item item = item(set + "_wolf_armor");
            helper.assertTrue(item == ModItems.WOLF_ARMOR.get(i).get(), set + "_wolf_armor is not the list's entry " + i);
            helper.assertTrue(item instanceof ItemOreSpawnWolfArmor animal
                            && animal.getBodyType() == AnimalArmorItem.BodyType.CANINE,
                    set + "_wolf_armor is not a canine wolf armour of the sets'");
            AnimalArmorItem armour = (AnimalArmorItem) item;
            ArmorItem chest = (ArmorItem) item(set + "_chestplate");
            helper.assertTrue(armour.getMaterial().is(chest.getMaterial().unwrapKey().orElseThrow()),
                    set + ": the wolf armour's material is not the chestplate's");
            helper.assertValueEqual(armour.getMaterial().unwrapKey().orElseThrow().location(), id(SETS[i][1]),
                    set + "'s armour material");
            helper.assertValueEqual(armour.getDefense(), chest.getDefense(), set + ": armour value against the chestplate's");
            helper.assertTrue(armour.getToughness() == chest.getToughness(),
                    set + ": toughness " + armour.getToughness() + " against the chestplate's " + chest.getToughness());
            helper.assertTrue(armour.getMaterial().value().knockbackResistance() == chest.getMaterial().value().knockbackResistance(),
                    set + ": knockback resistance against the chestplate's");
            ItemStack stack = new ItemStack(item);
            helper.assertValueEqual(stack.getMaxStackSize(), 1, set + ": stack size");
            helper.assertFalse(stack.isDamageableItem(), set + ": wolf armour takes no wear");
            helper.assertValueEqual(armour.getTexture(), id("textures/entity/wolf/armor/wolf_armor_" + SETS[i][1] + ".png"),
                    set + ": texture");
        }
        helper.succeed();
    }

    /**
     * A tamed wolf's owner puts each one on with a click (the piece leaves the hand, the wolf does not sit down) and,
     * a tick on, the wolf carries its armour value, toughness and knockback resistance; shears take it off again,
     * dropping it and wearing a point.
     */
    @GameTest(template = "empty_large", timeoutTicks = 60)
    public static void mod041b_an_owner_puts_each_on_and_shears_take_it_off(GameTestHelper helper) {
        ServerPlayer owner = player(helper, GameType.SURVIVAL);
        List<Wolf> wolves = new ArrayList<>();
        for (int i = 0; i < SETS.length; i++) {
            Wolf wolf = tamedWolf(helper, owner, new BlockPos(3 + (i % 7) * 5, 2, 8 + (i / 7) * 10));
            boolean sitting = wolf.isOrderedToSit();
            InteractionResult result = click(owner, wolf, new ItemStack(item(SETS[i][0] + "_wolf_armor")));
            helper.assertTrue(result.consumesAction(), SETS[i][0] + ": the owner's click was not taken (" + result + ")");
            helper.assertTrue(wolf.getBodyArmorItem().is(item(SETS[i][0] + "_wolf_armor")),
                    SETS[i][0] + ": the wolf does not wear it after the owner's click");
            helper.assertTrue(owner.getMainHandItem().isEmpty(), SETS[i][0] + ": the piece is still in the owner's hand");
            helper.assertTrue(wolf.isOrderedToSit() == sitting, SETS[i][0] + ": the click also made the wolf sit or stand");
            wolves.add(wolf);
        }
        helper.runAfterDelay(3, () -> {
            for (int i = 0; i < SETS.length; i++) {
                Wolf wolf = wolves.get(i);
                AnimalArmorItem armour = (AnimalArmorItem) item(SETS[i][0] + "_wolf_armor");
                helper.assertTrue(wolf.getAttributeValue(Attributes.ARMOR) == armour.getDefense(),
                        SETS[i][0] + ": the wolf's armour " + wolf.getAttributeValue(Attributes.ARMOR) + ", not " + armour.getDefense());
                helper.assertTrue(Math.abs(wolf.getAttributeValue(Attributes.ARMOR_TOUGHNESS) - armour.getToughness()) < 1.0E-6,
                        SETS[i][0] + ": the wolf's toughness " + wolf.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
                helper.assertTrue(Math.abs(wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)
                                - armour.getMaterial().value().knockbackResistance()) < 1.0E-6,
                        SETS[i][0] + ": the wolf's knockback resistance " + wolf.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            }
            Wolf first = wolves.get(0);
            ItemStack shears = new ItemStack(Items.SHEARS);
            InteractionResult result = click(owner, first, shears);
            helper.assertTrue(result.consumesAction(), "the owner's shears were not taken (" + result + ")");
            helper.assertTrue(first.getBodyArmorItem().isEmpty(), "the shears left the wolf armour on");
            helper.assertValueEqual(shears.getDamageValue(), 1, "the shears' wear");
            List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, first.getBoundingBox().inflate(3),
                    e -> e.getItem().is(item(SETS[0][0] + "_wolf_armor")));
            helper.assertValueEqual(dropped.size(), 1, "the shorn wolf armour dropped");
            dropped.forEach(ItemEntity::discard);
            wolves.forEach(Wolf::discard);
            helper.succeed();
        });
    }

    /**
     * No one else puts it on or takes it off: not a stranger, not on an untamed wolf or a pup, not over a piece already
     * worn; a curse of binding keeps the shears off it, but not in creative.
     */
    @GameTest(template = "empty_large", timeoutTicks = 40)
    public static void mod041c_only_the_owner_on_a_grown_wolf_and_not_through_a_curse(GameTestHelper helper) {
        ServerPlayer owner = player(helper, GameType.SURVIVAL);
        ServerPlayer stranger = player(helper, GameType.SURVIVAL);
        Item ruby = item("ruby_wolf_armor");
        Wolf owned = tamedWolf(helper, owner, new BlockPos(3, 2, 3));
        click(stranger, owned, new ItemStack(ruby));
        helper.assertTrue(owned.getBodyArmorItem().isEmpty(), "a stranger put wolf armour on another's wolf");
        helper.assertTrue(stranger.getMainHandItem().is(ruby), "the stranger's piece left the hand");

        Wolf wild = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(9, 2, 3));
        click(owner, wild, new ItemStack(ruby));
        helper.assertTrue(wild.getBodyArmorItem().isEmpty(), "wolf armour went on an untamed wolf");

        Wolf pup = tamedWolf(helper, owner, new BlockPos(15, 2, 3));
        pup.setAge(-24000);
        click(owner, pup, new ItemStack(ruby));
        helper.assertTrue(pup.getBodyArmorItem().isEmpty(), "wolf armour went on a pup");

        click(owner, owned, new ItemStack(ruby));
        ItemStack second = new ItemStack(item("emerald_wolf_armor"));
        click(owner, owned, second);
        helper.assertTrue(owned.getBodyArmorItem().is(ruby), "a second piece replaced the one worn");
        helper.assertValueEqual(owner.getMainHandItem().getCount(), 1, "the second piece left the hand");

        click(stranger, owned, new ItemStack(Items.SHEARS));
        helper.assertTrue(owned.getBodyArmorItem().is(ruby), "a stranger's shears took the wolf armour off");

        Wolf bound = tamedWolf(helper, owner, new BlockPos(21, 2, 3));
        ItemStack cursed = new ItemStack(ruby);
        cursed.enchant(helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.BINDING_CURSE), 1);
        bound.setBodyArmorItem(cursed);
        click(owner, bound, new ItemStack(Items.SHEARS));
        helper.assertTrue(bound.getBodyArmorItem().is(ruby), "shears took a cursed wolf armour off in survival");
        ServerPlayer creative = player(helper, GameType.CREATIVE);
        bound.tame(creative);
        click(creative, bound, new ItemStack(Items.SHEARS));
        helper.assertTrue(bound.getBodyArmorItem().isEmpty(), "shears in creative left a cursed wolf armour on");
        AABB area = helper.getBounds().inflate(1.0);
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).forEach(ItemEntity::discard);
        for (Wolf wolf : List.of(owned, wild, pup, bound)) {
            wolf.discard();
        }
        helper.succeed();
    }

    /**
     * The wolf's damage is cut by the armour and none of it taken into the armour: a hit costs the armoured wolf less
     * than the same hit costs a bare one, and the piece is still worn, unworn.
     */
    @GameTest(template = "empty_large", timeoutTicks = 40)
    public static void mod041d_the_armour_cuts_the_damage_and_takes_none(GameTestHelper helper) {
        ServerPlayer owner = player(helper, GameType.SURVIVAL);
        Wolf bare = tamedWolf(helper, owner, new BlockPos(3, 2, 3));
        Wolf armoured = tamedWolf(helper, owner, new BlockPos(9, 2, 3));
        click(owner, armoured, new ItemStack(item("ruby_wolf_armor")));
        helper.runAfterDelay(2, () -> {
            float bareBefore = bare.getHealth();
            float armouredBefore = armoured.getHealth();
            Pig attacker = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(6, 2, 8));
            bare.hurt(helper.getLevel().damageSources().mobAttack(attacker), 10.0F);
            armoured.hurt(helper.getLevel().damageSources().mobAttack(attacker), 10.0F);
            float bareLost = bareBefore - bare.getHealth();
            float armouredLost = armouredBefore - armoured.getHealth();
            helper.assertTrue(bareLost > 9.99F, "the bare wolf lost " + bareLost + " of 10");
            helper.assertTrue(armouredLost > 0.0F && armouredLost < bareLost,
                    "the armoured wolf lost " + armouredLost + ", the bare one " + bareLost);
            ItemStack worn = armoured.getBodyArmorItem();
            helper.assertTrue(worn.is(item("ruby_wolf_armor")) && worn.getDamageValue() == 0,
                    "the wolf armour after the hit: " + worn + ", damage " + worn.getDamageValue());
            attacker.discard();
            bare.discard();
            armoured.discard();
            helper.succeed();
        });
    }

    /**
     * The armour is never lost: a dying wolf takes none (it has already dropped what it wore), a click on a wolf the
     * owner leads lets it go first, as vanilla's click does, and Doggy Talents Next's training treat takes it off and
     * drops it before that mod could turn the wolf into a dog (by the treat's id: the mod is not here) wherever the
     * treat would train it, the owner's wolf or an untamed one, where a stranger's treat on another's wolf or another
     * item does nothing.
     */
    @GameTest(template = "empty_large", timeoutTicks = 40)
    public static void mod041h_no_armour_lost_to_a_dying_wolf_a_lead_or_training(GameTestHelper helper) {
        ServerPlayer owner = player(helper, GameType.SURVIVAL);
        ServerPlayer stranger = player(helper, GameType.SURVIVAL);
        Item ruby = item("ruby_wolf_armor");

        Wolf dying = tamedWolf(helper, owner, new BlockPos(3, 2, 3));
        dying.setHealth(0.0F);
        click(owner, dying, new ItemStack(ruby));
        helper.assertTrue(dying.getBodyArmorItem().isEmpty(), "wolf armour went on a dying wolf");
        helper.assertTrue(owner.getMainHandItem().is(ruby), "the piece left the hand for a dying wolf");

        Wolf led = tamedWolf(helper, owner, new BlockPos(9, 2, 3));
        led.setLeashedTo(owner, true);
        click(owner, led, new ItemStack(ruby));
        helper.assertTrue(led.getBodyArmorItem().isEmpty(), "the click on a led wolf put wolf armour on instead of letting it go");
        helper.assertFalse(led.isLeashed(), "the click did not let the led wolf go");

        Wolf trained = tamedWolf(helper, owner, new BlockPos(15, 2, 3));
        click(owner, trained, new ItemStack(ruby));
        helper.assertTrue(trained.getBodyArmorItem().is(ruby), "the wolf to be trained does not wear the armour");
        ResourceLocation treat = ResourceLocation.fromNamespaceAndPath("doggytalents", "training_treat");
        helper.assertFalse(WolfArmourEvents.dropBeforeTraining(trained, stranger, treat),
                "a stranger's training treat took the wolf armour off");
        helper.assertFalse(WolfArmourEvents.dropBeforeTraining(trained, owner, ResourceLocation.withDefaultNamespace("bone")),
                "another item took the wolf armour off");
        helper.assertTrue(WolfArmourEvents.dropBeforeTraining(trained, owner, treat) && trained.getBodyArmorItem().isEmpty(),
                "the owner's training treat left the wolf armour on");
        List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                trained.getBoundingBox().inflate(3), e -> e.getItem().is(ruby));
        helper.assertValueEqual(dropped.size(), 1, "the wolf armour dropped before the training");

        Wolf wild = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(21, 2, 3));
        wild.setBodyArmorItem(new ItemStack(ruby));
        helper.assertTrue(WolfArmourEvents.trainingFindsOurs(wild, stranger, treat)
                        && WolfArmourEvents.dropBeforeTraining(wild, stranger, treat) && wild.getBodyArmorItem().isEmpty(),
                "a treat that would train an untamed wolf left our armour on it");

        AABB area = helper.getBounds().inflate(1.0);
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, area).forEach(ItemEntity::discard);
        for (Wolf wolf : List.of(dying, led, trained, wild)) {
            wolf.discard();
        }
        helper.succeed();
    }

    /**
     * The load condition reads modern.enabled and modern.wolfArmour together; with the defaults the 13 recipes are
     * loaded (none for the Royal Guardian, which is loot only), each making its wolf armour, and the King's table
     * drops the Royal Guardian wolf armour once a kill, beside his chestplate.
     */
    @GameTest(template = "empty")
    public static void mod041e_the_recipes_and_the_kings_drop_load_with_the_feature(GameTestHelper helper) {
        ICondition condition = new ModernCondition("wolfArmour");
        final boolean master = OreSpawnConfig.MODERN_ENABLED.get();
        final boolean key = OreSpawnConfig.MODERN_WOLF_ARMOUR.get();
        boolean on, keyOff, masterOff;
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(true);
            on = condition.test(ICondition.IContext.EMPTY);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(false);
            keyOff = condition.test(ICondition.IContext.EMPTY);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(true);
            OreSpawnConfig.MODERN_ENABLED.set(false);
            masterOff = condition.test(ICondition.IContext.EMPTY);
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(key);
        }
        helper.assertTrue(on && !keyOff && !masterOff,
                "the condition reads master and key: on " + on + ", key off " + keyOff + ", master off " + masterOff);

        helper.assertTrue(master && key, "the defaults are expected on for the loaded data: modern.enabled " + master
                + ", modern.wolfArmour " + key);
        ServerLevel level = helper.getLevel();
        for (String[] set : SETS) {
            var recipe = level.getRecipeManager().byKey(id(set[0] + "_wolf_armor"));
            if (set[0].equals("royal")) {
                helper.assertTrue(recipe.isEmpty(), "the Royal Guardian wolf armour has a recipe");
                continue;
            }
            helper.assertTrue(recipe.isPresent(), set[0] + "_wolf_armor has no recipe");
            RecipeHolder<?> holder = recipe.get();
            ItemStack result = holder.value().getResultItem(level.registryAccess());
            helper.assertTrue(result.is(item(set[0] + "_wolf_armor")) && result.getCount() == 1,
                    set[0] + "_wolf_armor's recipe makes " + result);
            helper.assertTrue(holder.value() instanceof ShapedRecipe shaped && shaped.getWidth() == 3 && shaped.getHeight() == 3,
                    set[0] + "_wolf_armor's recipe is not a 3 x 3 shaped one");
            List<Ingredient> grid = holder.value().getIngredients();
            Item[] expected = grid(set[0]);
            for (int slot = 0; slot < 9; slot++) {
                Ingredient ingredient = grid.get(slot);
                boolean right = expected[slot] == null ? ingredient.isEmpty()
                        : !ingredient.isEmpty() && ingredient.test(new ItemStack(expected[slot]));
                helper.assertTrue(right, set[0] + "_wolf_armor's recipe, slot " + slot + ": expected "
                        + (expected[slot] == null ? "nothing" : BuiltInRegistries.ITEM.getKey(expected[slot])));
            }
        }

        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(1, 2, 1));
        LootTable king = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id("entities/the_king")));
        for (int roll = 0; roll < 8; roll++) {
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.THIS_ENTITY, pig)
                    .withParameter(LootContextParams.ORIGIN, pig.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().generic())
                    .create(LootContextParamSets.ENTITY);
            int wolf = 0;
            int chest = 0;
            for (ItemStack stack : king.getRandomItems(params)) {
                if (stack.is(ModItems.ROYAL_WOLF_ARMOR.get())) {
                    wolf += stack.getCount();
                }
                if (stack.is(ModItems.ROYAL_CHESTPLATE.get())) {
                    chest += stack.getCount();
                }
            }
            helper.assertTrue(wolf == 1 && chest == 1,
                    "the King's drop, roll " + roll + ": " + wolf + " wolf armour and " + chest + " chestplate, not one each");
        }
        pig.discard();
        helper.succeed();
    }

    /**
     * A recipe's 3 x 3 grid, row by row (null where it is empty): vanilla's wolf armour shape with the set's own
     * material, as its chestplate takes; Ultimate's rows as its chestplate's (iron, titanium, uranium); Experience as
     * its armour, eight bottles o' enchanting around the Emerald one.
     */
    private static Item[] grid(String set) {
        if (set.equals("ultimate")) {
            Item iron = Items.IRON_INGOT;
            Item titanium = item("ingot_titanium");
            Item uranium = item("ingot_uranium");
            return new Item[]{iron, null, null, titanium, titanium, titanium, uranium, null, uranium};
        }
        if (set.equals("experience")) {
            Item bottle = Items.EXPERIENCE_BOTTLE;
            return new Item[]{bottle, bottle, bottle, bottle, item("emerald_wolf_armor"), bottle, bottle, bottle, bottle};
        }
        Item m = switch (set) {
            case "queen" -> item("queen_scale");
            case "mobzilla" -> item("godzilla_scale");
            case "ruby" -> item("ruby");
            case "amethyst" -> item("amethyst_gem");
            case "emerald" -> Items.EMERALD;
            case "lavaeel" -> item("lava_eel");
            case "mothscale" -> item("moth_scale");
            case "peacock" -> item("peacock_feather");
            case "pink" -> item("crystal_pink_ingot");
            case "tigerseye" -> item("tigers_eye_ingot");
            case "lapis" -> Items.LAPIS_BLOCK;
            default -> throw new IllegalArgumentException(set);
        };
        return new Item[]{m, null, null, m, m, m, m, null, m};
    }

    /**
     * The equipment tab lists the 14 wolf armours right after the 14 horse armours while the feature is on (the horse
     * armour held on around it), none while it is off.
     */
    @GameTest(template = "empty")
    public static void mod041f_the_creative_tab_follows_the_feature(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CreativeModeTab tab = ModCreativeTabs.EQUIPMENT_TAB.get();
        CreativeModeTab.ItemDisplayParameters parameters =
                new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), false, level.registryAccess());
        final boolean master = OreSpawnConfig.MODERN_ENABLED.get();
        final boolean key = OreSpawnConfig.MODERN_WOLF_ARMOUR.get();
        final boolean horses = OreSpawnConfig.MODERN_HORSE_ARMOUR.get();
        List<Item> shownOn;
        List<Item> shownOff;
        List<Item> afterHorses = new ArrayList<>();
        try {
            OreSpawnConfig.MODERN_ENABLED.set(true);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(true);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(true);
            tab.buildContents(parameters);
            shownOn = wolfArmourIn(tab);
            List<ItemStack> all = new ArrayList<>(tab.getDisplayItems());
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).is(ModItems.LAPIS_HORSE_ARMOR.get())) {
                    for (int j = i + 1; j < Math.min(all.size(), i + 1 + SETS.length); j++) {
                        afterHorses.add(all.get(j).getItem());
                    }
                    break;
                }
            }
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(false);
            tab.buildContents(parameters);
            shownOff = wolfArmourIn(tab);
        } finally {
            OreSpawnConfig.MODERN_ENABLED.set(master);
            OreSpawnConfig.MODERN_WOLF_ARMOUR.set(key);
            OreSpawnConfig.MODERN_HORSE_ARMOUR.set(horses);
            tab.buildContents(parameters);
        }
        List<Item> expected = new ArrayList<>();
        ModItems.WOLF_ARMOR.forEach(w -> expected.add(w.get()));
        helper.assertTrue(shownOn.equals(expected), "with the feature on the tab lists " + shownOn.size()
                + " wolf armours, not the 14 in the armour's order");
        helper.assertTrue(afterHorses.equals(expected), "the wolf armours do not follow the Lapis horse armour, the horse "
                + "armour's last");
        helper.assertTrue(shownOff.isEmpty(), "with the feature off the tab still lists " + shownOff.size() + " wolf armours");
        helper.succeed();
    }

    private static List<Item> wolfArmourIn(CreativeModeTab tab) {
        List<Item> out = new ArrayList<>();
        for (ItemStack stack : tab.getDisplayItems()) {
            if (stack.getItem() instanceof ItemOreSpawnWolfArmor) {
                out.add(stack.getItem());
            }
        }
        return out;
    }
}
