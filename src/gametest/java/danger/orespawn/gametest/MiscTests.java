package danger.orespawn.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;
import danger.orespawn.block.BlockDuctTape;
import danger.orespawn.block.BlockPizza;
import danger.orespawn.block.OreGenericEgg;
import danger.orespawn.block.RTPBlock;
import danger.orespawn.entity.AntRobot;
import danger.orespawn.gui.CrystalFurnaceBlockEntity;
import danger.orespawn.world.feature.ModFeatures;
import danger.orespawn.world.feature.SpawnOresPoolFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Misc item/block behavior tests (checklist items i003, i004, i005, i006,
 * i009, i011, i013, i015, i017, i073, i132, i135).
 *
 * <p>Far-region convention (batch 9, K base 900): big/messy scenes build at
 * X = K*512, Z = 50000, Y = 100 via {@code helper.absolutePos(...)} offsets.
 * This file's K values: 900 (RTP platform), 901 (crystal furnace), 902
 * (ore/troll breaks), 903 (SpawnOres pool sampling). Assertions at those
 * absolute positions read {@code helper.getLevel().getBlockState} directly.</p>
 */
@GameTestHolder(OreSpawnMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MiscTests {

    private static BlockPos region(GameTestHelper helper, int k) {
        return helper.absolutePos(BlockPos.ZERO).offset(k * 512, 0, 50_000).atY(100);
    }

    private static Holder<Enchantment> enchant(ServerLevel level,
                                               net.minecraft.resources.ResourceKey<Enchantment> key) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }

    /** Lang lookup with a bundled-JSON fallback for headless servers. */
    private static Map<String, String> bundledLang;

    private static String resolveLang(String key) {
        String viaLanguage = Language.getInstance().getOrDefault(key);
        if (!viaLanguage.equals(key)) return viaLanguage;
        if (bundledLang == null) {
            bundledLang = new HashMap<>();
            try (InputStream in = MiscTests.class.getResourceAsStream("/assets/orespawn/lang/en_us.json")) {
                JsonObject json = JsonParser.parseReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                json.entrySet().forEach(e -> bundledLang.put(e.getKey(), e.getValue().getAsString()));
            } catch (Exception e) {
                throw new IllegalStateException("cannot load bundled en_us.json", e);
            }
        }
        return bundledLang.getOrDefault(key, key);
    }

    // ---------------------------------------------------------------

    /**
     * i003 — ITEM-011/012: LEFT-clicking a Pizza block eats a slice; LEFT-
     * clicking Duct Tape repairs the held damaged item. Both route through
     * {@code Block.attack} (port BlockPizza.java:61-66 / BlockDuctTape.java:
     * 62-67, restoring orig BlockPizza.java:91-93 / BlockDuctTape.java:92-94
     * onBlockClicked; FIX_LOG "ITEM-011/012"). Pizza: +4 nutrition / +1 BITES
     * (BlockPizza FOOD_NUTRITION=4, orig slice values); Duct Tape: repairs
     * max(maxDamage/6, 1) and +1 USES (BlockDuctTape REPAIR_FRACTION_DIVISOR).
     */
    @GameTest(template = "empty")
    public void item011_012_pizza_ducttape_left_click(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        BlockPos pizzaRel = new BlockPos(2, 2, 2);
        helper.setBlock(pizzaRel, ModBlocks.PIZZA.get());
        BlockPos pizzaPos = helper.absolutePos(pizzaRel);
        player.getFoodData().setFoodLevel(10);
        level.getBlockState(pizzaPos).attack(level, pizzaPos, player);
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 14,
                "pizza left-click: +4 nutrition (orig BlockPizza slice food value)");
        helper.assertValueEqual(level.getBlockState(pizzaPos).getValue(BlockPizza.BITES), 1,
                "pizza left-click: BITES increments");

        BlockPos tapeRel = new BlockPos(4, 2, 2);
        helper.setBlock(tapeRel, ModBlocks.DUCT_TAPE.get());
        BlockPos tapePos = helper.absolutePos(tapeRel);
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        pick.setDamageValue(100);
        player.setItemInHand(InteractionHand.MAIN_HAND, pick);
        level.getBlockState(tapePos).attack(level, tapePos, player);
        int repair = Math.max(pick.getMaxDamage() / 6, 1);
        helper.assertValueEqual(pick.getDamageValue(), 100 - repair,
                "duct tape left-click: repairs maxDamage/6");
        helper.assertValueEqual(level.getBlockState(tapePos).getValue(BlockDuctTape.USES), 1,
                "duct tape left-click: USES increments");
        helper.succeed();
    }

    /**
     * i004 — ITEM-013/014: stepping on an RTP block random-teleports the
     * player (port RTPBlock.java:41-90, restoring orig RTPBlock.java:25
     * onEntityWalking; target = ±16 with ±nextInt(8)−nextInt(8) jitter per
     * axis, ±4 Y search over solid-ground/air/air columns); Mole Dirt sinks
     * feet 0.125 via its lowered collision box {@code Block.box(0,0,0,16,14,16)}
     * (port MoleDirtBlock.java:24-27, orig MoleDirtBlock.java:33-36).
     * FIX_LOG "ITEM-013/014". K=900.
     */
    @GameTest(template = "empty")
    public void item013_014_rtp_mole_dirt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = region(helper, 900);

        // 61x61 stone platform one below the RTP block: every candidate target
        // column (|dx|,|dz| <= 23) has solid ground + 2 air above.
        for (int x = -30; x <= 30; x++) {
            for (int z = -30; z <= 30; z++) {
                level.setBlock(base.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 2);
            }
        }
        level.setBlock(base, ModBlocks.BLOCK_TELEPORT.get().defaultBlockState(), 2);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(base.getX() + 0.5, base.getY() + 1, base.getZ() + 0.5);
        BlockState state = level.getBlockState(base);
        ((RTPBlock) state.getBlock()).stepOn(level, base, state, player);

        double dx = player.getX() - (base.getX() + 0.5);
        double dz = player.getZ() - (base.getZ() + 0.5);
        // orig RTPBlock: |offset| = 16 + nextInt(8) - nextInt(8) ∈ [9, 23].
        helper.assertTrue(Math.abs(dx) >= 9 - 0.001 && Math.abs(dx) <= 23 + 0.001,
                "RTP: |dx| in [9,23], got " + dx);
        helper.assertTrue(Math.abs(dz) >= 9 - 0.001 && Math.abs(dz) <= 23 + 0.001,
                "RTP: |dz| in [9,23], got " + dz);
        helper.assertTrue(Math.abs(player.getY() - base.getY()) < 0.001,
                "RTP: lands on the platform level (Y search ±4)");

        BlockPos molePos = base.offset(0, 0, 30);
        level.setBlock(molePos, ModBlocks.MOLE_DIRT.get().defaultBlockState(), 2);
        double maxY = level.getBlockState(molePos)
                .getCollisionShape(level, molePos).max(Direction.Axis.Y);
        helper.assertTrue(Math.abs(maxY - 0.875) < 1.0e-9,
                "mole dirt: collision top at 14/16 = 0.875 (feet sink 0.125), got " + maxY);
        helper.succeed();
    }

    /**
     * i005 — ITEM-016 + C8 furnace: Crystal Furnace cooks in exactly 150 ticks
     * (7.5 s — orig TileEntityCrystalFurnace.java:175); crystal coal burns
     * 20000 ticks (orig :267-275) = floor(20000/150) = 133 completed smelts;
     * a lava-bucket fuel leaves the EMPTY BUCKET in the fuel slot (orig
     * :165-170). FIX_LOG "ITEM-016". Driven deterministically by calling
     * {@code CrystalFurnaceBlockEntity.serverTick} in a far non-ticking
     * region, so no double ticking can skew the counts. K=901.
     */
    @GameTest(template = "empty")
    public void item016_crystal_furnace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = region(helper, 901);
        level.setBlock(pos, ModBlocks.CRYSTAL_FURNACE.get().defaultBlockState(), 2);
        CrystalFurnaceBlockEntity be = (CrystalFurnaceBlockEntity) level.getBlockEntity(pos);
        be.setItem(0, new ItemStack(Blocks.SAND));               // input: sand → glass
        be.setItem(1, new ItemStack(ModBlocks.CRYSTAL_COAL.get()));

        // 149 ticks: not done yet; the 150th completes the smelt.
        for (int t = 0; t < 149; t++) {
            CrystalFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), be);
        }
        helper.assertTrue(be.getItem(2).isEmpty(), "cook must not finish before tick 150");
        CrystalFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), be);
        helper.assertTrue(be.getItem(2).is(Blocks.GLASS.asItem()),
                "cook completes exactly at 150 ticks (orig :175)");
        helper.assertTrue(be.getItem(1).isEmpty(),
                "the single crystal coal was consumed on ignition");

        // Crystal coal burn budget: burnTime=20000 set at tick 1, decremented
        // each following tick → burning through tick 20000; cookTime increments
        // once per burning tick → completed smelts = floor(20000/150) = 133
        // (the 134th would need tick 20100 > 20000). Drive to tick 20100 and
        // count outputs, refilling the input so fuel is the only limit.
        int outputs = be.getItem(2).getCount(); // = 1 from the first smelt
        be.setItem(2, ItemStack.EMPTY);
        for (int t = 150; t < 20_100; t++) {
            if (be.getItem(0).isEmpty()) {
                be.setItem(0, new ItemStack(Blocks.SAND, 64));
            }
            CrystalFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), be);
            ItemStack out = be.getItem(2);
            if (!out.isEmpty()) {
                outputs += out.getCount();
                be.setItem(2, ItemStack.EMPTY);
            }
        }
        helper.assertValueEqual(outputs, 133,
                "crystal coal (20000t) yields exactly 133 smelts at 150t each");
        helper.assertFalse(level.getBlockState(pos)
                        .getValue(danger.orespawn.block.CrystalFurnace.LIT),
                "furnace unlit after the coal's 20000 ticks");

        // Lava bucket container-item behavior (orig :165-170).
        BlockPos pos2 = pos.offset(4, 0, 0);
        level.setBlock(pos2, ModBlocks.CRYSTAL_FURNACE.get().defaultBlockState(), 2);
        CrystalFurnaceBlockEntity be2 = (CrystalFurnaceBlockEntity) level.getBlockEntity(pos2);
        be2.setItem(0, new ItemStack(Blocks.SAND));
        be2.setItem(1, new ItemStack(Items.LAVA_BUCKET));
        CrystalFurnaceBlockEntity.serverTick(level, pos2, level.getBlockState(pos2), be2);
        helper.assertTrue(be2.getItem(1).is(Items.BUCKET),
                "lava bucket fuel leaves the empty bucket in the fuel slot");
        helper.succeed();
    }

    /**
     * i006 — ITEM-019: the Creeper Repellent pushes matching entities every 10
     * game ticks (0.5 s) within radius 20 — scheduled-tick cadence restored
     * from orig CreeperRepellent.java:59-76 (tickRate 10) with force
     * (20−dist)×0.4 (orig :100-107); port RepellentBlock.java:33-77,120-133.
     * FIX_LOG "ITEM-019". A no-AI creeper 15 blocks out is re-pinned each tick
     * and the ~2.0-strength velocity impulses are counted: the old randomTick
     * port (~68 s average) would produce zero pushes in this window.
     */
    @GameTest(template = "empty_large", timeoutTicks = 200)
    public void item019_repellent_cadence(GameTestHelper helper) {
        BlockPos repellentRel = new BlockPos(24, 2, 24);
        helper.setBlock(repellentRel, ModBlocks.CREEPER_REPELLENT.get());
        Creeper creeper = helper.spawnWithNoFreeWill(EntityType.CREEPER, new BlockPos(9, 2, 24));
        Vec3 pin = creeper.position();

        AtomicInteger pushes = new AtomicInteger();
        double[] lastSpeed = {0.0};
        helper.onEachTick(() -> {
            double h = creeper.getDeltaMovement().horizontalDistance();
            // Each push injects (20-15)*0.4 = 2.0 horizontal speed; between
            // pushes friction only decays it, so a +0.5 jump is a push.
            if (h > lastSpeed[0] + 0.5) {
                pushes.incrementAndGet();
            }
            lastSpeed[0] = h;
            creeper.setPos(pin.x, pin.y, pin.z); // keep distance/force constant
        });
        helper.runAfterDelay(65, () -> {
            helper.assertTrue(pushes.get() >= 4,
                    "repellent must push ~every 10 ticks: expected >=4 impulses in 65 ticks, got "
                            + pushes.get());
            creeper.discard();
            helper.succeed();
        });
    }

    /**
     * i009 — ITEM-040 + ITEM-057: with an Experience Sword ticking in a
     * player's inventory, each worn Experience armor piece occasionally grants
     * +1 XP — outer 1-in-60 roll per tick, chest piece 1-in-20 (orig
     * ExperienceSword.java:63-103; port ExperienceSword.java:40-78; FIX_LOG
     * "ITEM-040", AUDIT_FINDINGS ITEM-040/ITEM-057). Also asserts the ITEM-040
     * identity fixes: durability 1400 (orig :35) and baked Sharpness 2 +
     * Unbreaking 3 (orig :40-41, Looting-3 bug removed).
     *
     * <p>Statistics: P(+1 XP per call, chest only) = (1/60)(1/20) = 1/1200.
     * With n = 60000 calls, P(zero XP) = (1199/1200)^60000 = e^{−50} ≈ 2e−22
     * &lt; 1e−9. Negative control: no armor → the per-piece branch can never
     * fire, so XP must stay exactly 0 over the same n.</p>
     */
    @GameTest(template = "empty")
    public void item040_057_experience_sword_armor_xp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack sword = new ItemStack(ModItems.EXPERIENCE_SWORD.get());
        helper.assertValueEqual(sword.getMaxDamage(), 1400,
                "ITEM-040: durability 1400 overrides the Emerald tier");

        Player armored = helper.makeMockPlayer(GameType.SURVIVAL);
        armored.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.EXPERIENCE_CHESTPLATE.get()));
        for (int i = 0; i < 60_000; i++) {
            sword.getItem().inventoryTick(sword, level, armored, 0, true);
        }
        helper.assertTrue(armored.totalExperience > 0,
                "armor-XP tick: wearing the experience chestplate must generate XP "
                        + "(p=1/1200 per tick, n=60000, false-fail ~2e-22)");
        net.minecraft.world.item.enchantment.ItemEnchantments enchants = sword.getOrDefault(
                DataComponents.ENCHANTMENTS,
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        helper.assertValueEqual(enchants.getLevel(enchant(level, Enchantments.SHARPNESS)), 2,
                "ITEM-040: baked Sharpness 2");
        helper.assertValueEqual(enchants.getLevel(enchant(level, Enchantments.UNBREAKING)), 3,
                "ITEM-040: baked Unbreaking 3 (not Looting 3)");

        Player bare = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack sword2 = new ItemStack(ModItems.EXPERIENCE_SWORD.get());
        for (int i = 0; i < 60_000; i++) {
            sword2.getItem().inventoryTick(sword2, level, bare, 0, true);
        }
        helper.assertValueEqual(bare.totalExperience, 0,
                "armor-XP tick: no experience armor worn → no XP ever");
        helper.succeed();
    }

    /**
     * i011 (part 1) — ITEM-050/052: ZooKeeper makes a mob persistent and
     * breaks after ONE use (2 damage on a 1-durability item — orig
     * ItemZooKeeper.java:44-45; port ItemZooKeeper.java:41-49, ModItems
     * durability(1)); the Wrench refuses a healthy unowned AntRobot (orig
     * ItemWrench.java:50-57), disassembles one below half health into an
     * ant_robot_kit carrying missing HP as item damage (orig :86-94), and the
     * kit re-spawns the robot with carried-over health, custom name and owned
     * flag (orig ItemSpiderRobotKit.java:45-55). FIX_LOG "ITEM-050/051/052".
     */
    @GameTest(template = "empty")
    public void item050_052_zookeeper_wrench_kit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));

        // -- ZooKeeper: 1-use persistence ---------------------------------
        Pig pig = EntityType.PIG.create(level);
        pig.setPos(center.getX(), center.getY(), center.getZ());
        level.addFreshEntity(pig);
        ItemStack zoo = new ItemStack(ModItems.ZOO_KEEPER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, zoo);
        zoo.getItem().onLeftClickEntity(zoo, player, pig);
        helper.assertTrue(pig.isPersistenceRequired(),
                "ZooKeeper left-click sets persistence (orig :44)");
        helper.assertTrue(zoo.isEmpty(),
                "ZooKeeper breaks after a single use (2 dmg vs durability 1)");
        pig.discard();

        // -- Wrench refusal: healthy + unowned ----------------------------
        AntRobot healthy = ModEntities.ANT_ROBOT.get().create(level);
        healthy.setPos(center.getX(), center.getY(), center.getZ());
        level.addFreshEntity(healthy);
        ItemStack wrench = new ItemStack(ModItems.WRENCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        boolean took = wrench.getItem().onLeftClickEntity(wrench, player, healthy);
        helper.assertFalse(took, "Wrench must refuse a healthy unowned AntRobot (orig :50-53)");
        helper.assertFalse(healthy.isRemoved(), "refused robot survives");
        helper.assertValueEqual(wrench.getDamageValue(), 0, "refusal costs no durability");
        healthy.discard();

        // -- Wrench success: unowned below half health --------------------
        AntRobot hurt = ModEntities.ANT_ROBOT.get().create(level);
        hurt.setPos(center.getX(), center.getY(), center.getZ());
        level.addFreshEntity(hurt);
        hurt.setHealth(100.0f); // max 300 (MobStats.ANT_ROBOT) → below half
        took = wrench.getItem().onLeftClickEntity(wrench, player, hurt);
        helper.assertTrue(took, "Wrench disassembles an unowned robot below half health");
        helper.assertTrue(hurt.isRemoved(), "disassembled robot is discarded");
        List<ItemEntity> kits = level.getEntitiesOfClass(ItemEntity.class,
                new AABB(center).inflate(4),
                e -> e.getItem().is(ModItems.ANT_ROBOT_KIT.get()));
        helper.assertTrue(kits.size() == 1, "exactly one ant_robot_kit drops");
        helper.assertValueEqual(kits.get(0).getItem().getDamageValue(), 200,
                "kit damage = missing HP (300 − 100), orig ItemWrench.java:86-94");
        kits.forEach(ItemEntity::discard);

        // -- Kit re-spawn with carried-over health + name -----------------
        BlockPos ground = helper.absolutePos(new BlockPos(6, 1, 6));
        helper.setBlock(new BlockPos(6, 1, 6), Blocks.STONE);
        ItemStack kit = new ItemStack(ModItems.ANT_ROBOT_KIT.get());
        kit.setDamageValue(200);
        kit.set(DataComponents.CUSTOM_NAME, Component.literal("Clanky"));
        player.setItemInHand(InteractionHand.MAIN_HAND, kit);
        kit.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
        List<AntRobot> robots = level.getEntitiesOfClass(AntRobot.class,
                new AABB(ground).inflate(3), AntRobot::isAlive);
        helper.assertTrue(robots.size() == 1, "kit use spawns the robot");
        AntRobot spawned = robots.get(0);
        helper.assertTrue(Math.abs(spawned.getHealth() - 100.0f) < 0.01,
                "kit-spawned robot health = maxDamage − kit damage = 100 (orig :47)");
        helper.assertTrue(spawned.hasCustomName()
                        && spawned.getCustomName().getString().equals("Clanky"),
                "kit carries the custom name onto the robot (orig :48-50)");
        helper.assertValueEqual(spawned.getOwned(), 1,
                "kit-spawned ant robots are owned (orig :52-55)");
        spawned.discard();
        helper.succeed();
    }

    /**
     * i011 (part 2) — ITEM-051: the Sifter rolls the original weighted tables
     * on water/sand/gravel/dirt/grass and costs 1 durability per use (orig
     * ItemSifter.java:35-474; port ItemSifter.java:42-188 — water nextInt(160)
     * with the six mod fish at cases 1-6, solids nextInt(60); FIX_LOG
     * "ITEM-050/051/052": "mod fish from water!").
     *
     * <p>Statistics: mod-fish chance per water use = 6/160; n = 600 uses →
     * P(no fish) = (154/160)^600 = e^{−22.9} ≈ 1.1e−10 &lt; 1e−9. Solid
     * substrates win ≥ 12/60 per use; n = 200 → P(no drop) ≤ (48/60)^200 =
     * e^{−44.6}.</p>
     */
    @GameTest(template = "empty_large")
    public void item051_sifter_tables(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        // Basin so the water source cannot flow away once ticks resume.
        BlockPos waterRel = new BlockPos(10, 2, 10);
        for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH,
                Direction.EAST, Direction.WEST, Direction.DOWN}) {
            helper.setBlock(waterRel.relative(d), Blocks.STONE);
        }
        helper.setBlock(waterRel, Blocks.WATER);
        BlockPos waterPos = helper.absolutePos(waterRel);

        // durability: one use costs exactly 1 (orig :472); sifter maxDamage 600.
        ItemStack first = new ItemStack(ModItems.SIFTER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, first);
        first.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(waterPos), Direction.UP, waterPos, false)));
        helper.assertValueEqual(first.getDamageValue(), 1, "sifter costs 1 durability per use");

        Set<Item> modFish = Set.of(ModItems.GREEN_FISH.get(), ModItems.BLUE_FISH.get(),
                ModItems.PINK_FISH.get(), ModItems.ROCK_FISH.get(),
                ModItems.WOOD_FISH.get(), ModItems.GREY_FISH.get());
        for (int i = 0; i < 600; i++) {
            ItemStack sifter = new ItemStack(ModItems.SIFTER.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, sifter);
            sifter.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(waterPos), Direction.UP, waterPos, false)));
        }
        boolean fish = !level.getEntitiesOfClass(ItemEntity.class,
                new AABB(waterPos).inflate(6),
                e -> modFish.contains(e.getItem().getItem())).isEmpty();
        helper.assertTrue(fish,
                "sifting water 600x must roll a mod fish (p=6/160 each, false-fail ~1e-10)");
        level.getEntitiesOfClass(ItemEntity.class, new AABB(waterPos).inflate(12))
                .forEach(ItemEntity::discard);

        Block[] solids = {Blocks.SAND, Blocks.GRAVEL, Blocks.DIRT, Blocks.GRASS_BLOCK};
        for (int s = 0; s < solids.length; s++) {
            BlockPos rel = new BlockPos(20 + s * 4, 2, 10);
            helper.setBlock(rel, solids[s]);
            BlockPos abs = helper.absolutePos(rel);
            for (int i = 0; i < 200; i++) {
                ItemStack sifter = new ItemStack(ModItems.SIFTER.get());
                player.setItemInHand(InteractionHand.MAIN_HAND, sifter);
                sifter.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
            }
            boolean dropped = !level.getEntitiesOfClass(ItemEntity.class,
                    new AABB(abs).inflate(5)).isEmpty();
            helper.assertTrue(dropped, "sifting " + solids[s] + " 200x must roll its table "
                    + "(win >= 12/60 per use, false-fail < e^-44)");
            level.getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(8))
                    .forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    /**
     * i013 — ITEM-001/005: breaking overworld ruby/amethyst ore NEVER explodes
     * and pops 5..13 bonus XP (orig OreRuby.java:26-30 / OreAmethyst.java:
     * 26-30 — {@code 5 + nextInt(5) + nextInt(5)}; the port has no explosion
     * path at all, OreRuby.java Javadoc); breaking a red-ant/termite troll
     * block as a player erupts 15..20 mobs even WITH Silk Touch (orig
     * OreBasicStone.java:47-58 — {@code 15 + nextInt(6)}, no Silk Touch
     * escape; port OreBasicStone.java:82-120). FIX_LOG "ITEM-001/005". K=902.
     */
    @GameTest(template = "empty")
    public void item001_005_gem_ores_troll_blocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = region(helper, 902);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        Block[] ores = {ModBlocks.ORE_RUBY.get(), ModBlocks.ORE_AMETHYST.get()};
        for (int o = 0; o < ores.length; o++) {
            BlockPos orePos = base.offset(o * 12, 0, 0);
            // Marker shell that an explosion would destroy.
            for (Direction d : Direction.values()) {
                level.setBlock(orePos.relative(d), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
            }
            for (int trial = 0; trial < 20; trial++) {
                level.setBlock(orePos, ores[o].defaultBlockState(), 2);
                level.destroyBlock(orePos, true);
                for (Direction d : Direction.values()) {
                    helper.assertTrue(level.getBlockState(orePos.relative(d))
                                    .is(Blocks.DIAMOND_BLOCK),
                            "ITEM-001: ore break must never explode (shell intact)");
                }
                // Orbs spawned by one award() call can merge (value stays,
                // "Count" increments — ExperienceOrb.java:163,180), so total
                // XP = Σ value × Count read from the orb's saved NBT.
                int xp = level.getEntitiesOfClass(ExperienceOrb.class,
                                new AABB(orePos).inflate(5)).stream()
                        .mapToInt(orb -> {
                            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                            orb.saveWithoutId(tag);
                            return orb.getValue() * Math.max(tag.getInt("Count"), 1);
                        }).sum();
                helper.assertTrue(xp >= 5 && xp <= 13,
                        "gem ore pops 5..13 XP (5+d5+d5, orig OreRuby.java:28), got " + xp);
                level.getEntitiesOfClass(ExperienceOrb.class, new AABB(orePos).inflate(5))
                        .forEach(ExperienceOrb::discard);
                level.getEntitiesOfClass(ItemEntity.class, new AABB(orePos).inflate(5))
                        .forEach(ItemEntity::discard);
            }
        }

        ItemStack silkPick = new ItemStack(Items.DIAMOND_PICKAXE);
        silkPick.enchant(enchant(level, Enchantments.SILK_TOUCH), 1);
        record Troll(Block block, EntityType<?> mob) {}
        List<Troll> trolls = List.of(
                new Troll(ModBlocks.RED_ANT_TROLL.get(), ModEntities.ENTITY_RED_ANT.get()),
                new Troll(ModBlocks.TERMITE_TROLL.get(), ModEntities.ENTITY_TERMITE.get()));
        for (int t = 0; t < trolls.size(); t++) {
            Troll troll = trolls.get(t);
            BlockPos pos = base.offset(40 + t * 20, 0, 0);
            // ground pad for the swarm
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    level.setBlock(pos.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 2);
                }
            }
            level.setBlock(pos, troll.block().defaultBlockState(), 2);
            BlockState state = level.getBlockState(pos);
            level.removeBlock(pos, false);
            troll.block().playerDestroy(level, player, pos, state, null, silkPick);
            int count = level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(8),
                    e -> e.getType() == troll.mob()).size();
            helper.assertTrue(count >= 15 && count <= 20,
                    "troll block erupts 15..20 mobs even with Silk Touch (orig :48,54), got "
                            + count);
            level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(12),
                    e -> e.getType() == troll.mob()).forEach(Mob::discard);
        }
        helper.succeed();
    }

    /**
     * i015 — ITEM-029 (D4): special-food effects per orig ItemSunFish.java:
     * 29-48 (FIX_LOG D4 "ITEM-029 — FIXED"): Butter Candy Speed+Jump 2000t
     * (100 s); Cooked Bacon Regen+Strength 2000t; Crystal Apple Regen+Strength
     * 3000t (150 s); Heart ("Love") Regen IV + Strength III + Fire Res III +
     * Resistance II 6000t (300 s) plus Speed+Jump 5000t (250 s). Port:
     * ModItems.java BUTTER_CANDY/COOKED_BACON/CRYSTAL_APPLE/HEART food
     * definitions. Amplifiers are 0-based (Regen IV = amp 3).
     */
    @GameTest(template = "empty")
    public void item029_special_food_effects(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        record Expected(Holder<MobEffect> effect, int amplifier, int duration) {}
        record Food(Item item, List<Expected> effects) {}
        List<Food> foods = List.of(
                new Food(ModItems.BUTTER_CANDY.get(), List.of(
                        new Expected(MobEffects.MOVEMENT_SPEED, 0, 2000),
                        new Expected(MobEffects.JUMP, 0, 2000))),
                new Food(ModItems.COOKED_BACON.get(), List.of(
                        new Expected(MobEffects.REGENERATION, 0, 2000),
                        new Expected(MobEffects.DAMAGE_BOOST, 0, 2000))),
                new Food(ModItems.CRYSTAL_APPLE.get(), List.of(
                        new Expected(MobEffects.REGENERATION, 0, 3000),
                        new Expected(MobEffects.DAMAGE_BOOST, 0, 3000))),
                new Food(ModItems.HEART.get(), List.of(
                        new Expected(MobEffects.REGENERATION, 3, 6000),
                        new Expected(MobEffects.DAMAGE_BOOST, 2, 6000),
                        new Expected(MobEffects.FIRE_RESISTANCE, 2, 6000),
                        new Expected(MobEffects.DAMAGE_RESISTANCE, 1, 6000),
                        new Expected(MobEffects.MOVEMENT_SPEED, 0, 5000),
                        new Expected(MobEffects.JUMP, 0, 5000))));

        for (Food food : foods) {
            player.removeAllEffects();
            player.eat(level, new ItemStack(food.item()));
            String name = BuiltInRegistries.ITEM.getKey(food.item()).toString();
            helper.assertValueEqual(player.getActiveEffects().size(), food.effects().size(),
                    name + ": exact effect count");
            for (Expected e : food.effects()) {
                MobEffectInstance inst = player.getEffect(e.effect());
                helper.assertTrue(inst != null, name + ": missing effect " + e.effect());
                helper.assertValueEqual(inst.getAmplifier(), e.amplifier(),
                        name + ": amplifier of " + e.effect());
                helper.assertValueEqual(inst.getDuration(), e.duration(),
                        name + ": duration of " + e.effect());
            }
        }
        player.removeAllEffects();
        helper.succeed();
    }

    /**
     * i017 — C8 ExperienceCatcher: clicking under an XP orb worth ≥3 catches
     * it ~80% of the time (miss = nextInt(5)==1, orig ExperienceCatcher.java:
     * 37), converting orb → Bottle o' Enchanting + string + stick with the
     * catcher consumed (orig :38-53); a miss (or an orb worth &lt;3) drops the
     * catcher back at the click point, still consuming one from the stack
     * (orig :56-62). Port item/ExperienceCatcher.java:34-81.
     *
     * <p>Statistics: n = 1000 trials at p = 0.8; bounds [680, 920] give
     * Hoeffding false-fail ≤ 2·exp(−2·1000·0.12²) = 2e^{−28.8} ≈ 6e−13
     * &lt; 1e−9.</p>
     */
    @GameTest(template = "empty")
    public void c8_experience_catcher(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos groundRel = new BlockPos(4, 1, 4);
        helper.setBlock(groundRel, Blocks.STONE);
        BlockPos ground = helper.absolutePos(groundRel);
        Vec3 click = Vec3.atCenterOf(ground).add(0, 0.5, 0);

        // Deterministic: an orb worth < 3 is never caught (orig :37 value gate)
        // and the catcher drops back at the click point (orig :56-62).
        ExperienceOrb smallOrb = new ExperienceOrb(level, click.x, ground.getY() + 1.2, click.z, 2);
        level.addFreshEntity(smallOrb);
        ItemStack catcher = new ItemStack(ModItems.EXPERIENCE_CATCHER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, catcher);
        catcher.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(click, Direction.UP, ground, false)));
        helper.assertFalse(smallOrb.isRemoved(), "orbs worth <3 are never caught");
        helper.assertTrue(catcher.isEmpty(), "the catcher is always consumed from the stack");
        helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class,
                        new AABB(ground).inflate(3),
                        e -> e.getItem().is(ModItems.EXPERIENCE_CATCHER.get())).isEmpty(),
                "on a miss the catcher drops back at the click point");
        smallOrb.discard();
        level.getEntitiesOfClass(ItemEntity.class, new AABB(ground).inflate(4))
                .forEach(ItemEntity::discard);

        int successes = 0;
        boolean successDetailChecked = false;
        for (int i = 0; i < 1000; i++) {
            ExperienceOrb orb = new ExperienceOrb(level, click.x, ground.getY() + 1.2, click.z, 5);
            level.addFreshEntity(orb);
            ItemStack stack = new ItemStack(ModItems.EXPERIENCE_CATCHER.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(click, Direction.UP, ground, false)));
            if (orb.isRemoved()) {
                successes++;
                if (!successDetailChecked) {
                    successDetailChecked = true;
                    AABB box = new AABB(ground).inflate(3);
                    helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, box,
                                    e -> e.getItem().is(Items.EXPERIENCE_BOTTLE)).isEmpty(),
                            "catch yields a Bottle o' Enchanting (orig :40-44)");
                    helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, box,
                                    e -> e.getItem().is(Items.STRING)).isEmpty(),
                            "catch yields string (orig :45-47)");
                    helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, box,
                                    e -> e.getItem().is(Items.STICK)).isEmpty(),
                            "catch yields a stick (orig :48-50)");
                    helper.assertTrue(stack.isEmpty(), "catch consumes the catcher (orig :51-53)");
                }
            } else {
                orb.discard();
            }
            level.getEntitiesOfClass(ItemEntity.class, new AABB(ground).inflate(4))
                    .forEach(ItemEntity::discard);
        }
        helper.assertTrue(successes >= 680 && successes <= 920,
                "catch rate ~80% (orig :37): got " + successes + "/1000");
        helper.succeed();
    }

    /**
     * i073 — D2 registry: the entity id is {@code orespawn:elevator} displayed
     * as "Hoverboard" (intentional — ModItems.java "elevator" comment citing
     * orig OreSpawnMain.java:1904/5174), so {@code /summon orespawn:hoverboard}
     * fails, and the creative tabs contain exactly ONE Hoverboard entry.
     */
    @GameTest(template = "empty")
    public void d2_hoverboard_registry(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.containsKey(
                        ResourceLocation.fromNamespaceAndPath("orespawn", "elevator")),
                "entity id orespawn:elevator must exist");
        helper.assertFalse(BuiltInRegistries.ENTITY_TYPE.containsKey(
                        ResourceLocation.fromNamespaceAndPath("orespawn", "hoverboard")),
                "orespawn:hoverboard must NOT exist (why /summon fails — intentional)");
        helper.assertFalse(BuiltInRegistries.ITEM.containsKey(
                        ResourceLocation.fromNamespaceAndPath("orespawn", "hoverboard")),
                "no orespawn:hoverboard item either");

        CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), false, level.registryAccess());
        int elevatorEntries = 0;
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            for (ItemStack stack : tab.getDisplayItems()) {
                if (stack.is(ModItems.ELEVATOR.get())) elevatorEntries++;
            }
        }
        helper.assertValueEqual(elevatorEntries, 1,
                "creative tabs hold exactly ONE Hoverboard (elevator) entry");

        int named = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (key.getNamespace().equals("orespawn")
                    && resolveLang(item.getDescriptionId()).equals("Hoverboard")) {
                named++;
            }
        }
        helper.assertValueEqual(named, 1,
                "exactly one orespawn item is named 'Hoverboard' (item.orespawn.elevator)");
        helper.succeed();
    }

    /**
     * i132 — D5c lang: every spawn ore reads "Ancient Dried &lt;Mob&gt; Spawn
     * Egg" (part blocks read "... Spawn Egg Part"), including the 11 renamed
     * crystal egg ores (ore_urchin.., ModBlocks.java:166-193) and kraken/
     * dragon (ModBlocks.java:161-164). The full set is the 119-entry
     * OreGenericEgg registry (ModBlocks.java:195-196 comment; 119 matching
     * entries in assets/orespawn/lang/en_us.json); the only OreGenericEgg
     * blocks EXCLUDED are the ender-pearl/eye-of-ender storage blocks
     * (ModBlocks.java:55-60, ITEM-010 — not spawn ores).
     */
    @GameTest(template = "empty")
    public void d5c_spawn_ore_lang(GameTestHelper helper) {
        int checked = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (!key.getNamespace().equals("orespawn")) continue;
            if (!(block instanceof OreGenericEgg)) continue;
            if (block == ModBlocks.BLOCK_ENDER_PEARL.get()
                    || block == ModBlocks.BLOCK_EYE_OF_ENDER.get()) continue;
            checked++;
            String name = resolveLang(block.getDescriptionId());
            helper.assertTrue(name.startsWith("Ancient Dried ") && name.contains(" Spawn Egg"),
                    "spawn ore " + key + " must read 'Ancient Dried <Mob> Spawn Egg', got '"
                            + name + "'");
            helper.assertFalse(name.equals(block.getDescriptionId()),
                    "spawn ore " + key + " must have a lang entry");
        }
        helper.assertValueEqual(checked, 119,
                "the full 119-entry spawn-ore (OreGenericEgg) registry is present");
        helper.succeed();
    }

    /**
     * i135 — D5c interim content GONE: no {@code orespawn:ancient_dried_egg}
     * block/item exists anywhere (retired with the SpawnOres pool restoration,
     * ModItems.java:236-239, PN-010 closed), no registered ore feature places
     * the dragon/kraken boss blocks as deep single-block veins any more, and
     * the boss blocks now appear via the restored pool inside its documented
     * Y50..128 window (SpawnOresPoolFeature.java:62-66 RATE/MIN_DEPTH/
     * MAX_DEPTH per orig OreSpawnMain.java:1579; kraken c50 / dragon c53 pool
     * rows per d5_extraction/spawn_ores_spec.md §2). K=903.
     *
     * <p>Statistics: per place() pass ≈ 28 + d20 (+30 at 1/20) vein rolls;
     * each roll passes the Y gate w.p. 79/128 and picks kraken w.p.
     * (97/104)·(1/98) → ~0.23 kraken veins per pass. n = 200 passes → λ ≈ 46,
     * P(zero) = e^{−46}; same for dragon.</p>
     */
    @GameTest(template = "empty")
    public void d5c_interim_content_gone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.assertFalse(BuiltInRegistries.BLOCK.containsKey(
                        ResourceLocation.fromNamespaceAndPath("orespawn", "ancient_dried_egg")),
                "ancient_dried_egg block must be gone (PN-010 retired)");
        helper.assertFalse(BuiltInRegistries.ITEM.containsKey(
                        ResourceLocation.fromNamespaceAndPath("orespawn", "ancient_dried_egg")),
                "ancient_dried_egg item must be gone");

        // The retired interim design placed the boss blocks as their own deep
        // ore features; assert no registered configured feature still targets
        // them (the pool builds its vein configs at runtime instead).
        Set<Block> bossBlocks = Set.of(ModBlocks.KRAKEN_SPAWN_BLOCK.get(),
                ModBlocks.DRAGON_SPAWN_BLOCK.get(),
                ModBlocks.ENDER_DRAGON_SPAWN_BLOCK.get(),
                ModBlocks.WATER_DRAGON_SPAWN_BLOCK.get());
        for (ConfiguredFeature<?, ?> cf
                : level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)) {
            if (cf.config() instanceof OreConfiguration ore) {
                for (OreConfiguration.TargetBlockState target : ore.targetStates) {
                    helper.assertFalse(bossBlocks.contains(target.state.getBlock()),
                            "no registered ore feature may place boss spawn blocks directly "
                                    + "(interim deep single-block veins retired)");
                }
            }
        }

        // Pool sampling: fill a far chunk with stone across the pool's Y
        // window and run the real feature; boss blocks must appear via the
        // pool and only within the documented band.
        BlockPos raw = region(helper, 903);
        BlockPos chunkOrigin = new BlockPos(raw.getX() & ~15, 0, raw.getZ() & ~15);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 45; y <= 132; y++) {
                    level.setBlock(chunkOrigin.offset(x, y, z),
                            Blocks.STONE.defaultBlockState(), 2);
                }
            }
        }
        SpawnOresPoolFeature feature = ModFeatures.SPAWN_ORES_POOL.get();
        SpawnOresPoolFeature.Configuration cfg = new SpawnOresPoolFeature.Configuration(20, 1, 1);
        FeaturePlaceContext<SpawnOresPoolFeature.Configuration> ctx = new FeaturePlaceContext<>(
                Optional.empty(), level, level.getChunkSource().getGenerator(), level.random,
                chunkOrigin, cfg);
        for (int i = 0; i < 200; i++) {
            feature.place(ctx);
        }
        int total = 0;
        boolean kraken = false;
        boolean dragon = false;
        for (BlockPos p : BlockPos.betweenClosed(chunkOrigin.offset(-4, 40, -4),
                chunkOrigin.offset(20, 135, 20))) {
            BlockState state = level.getBlockState(p);
            if (!(state.getBlock() instanceof OreGenericEgg)) continue;
            total++;
            // Roll gate is y in [50,128] (OSW:369); vanilla ore spread can
            // smear a vein a couple of blocks, hence the >=48 bound.
            helper.assertTrue(p.getY() >= 48,
                    "pool veins only in the Y50+ window (MIN_DEPTH=50), found egg at y="
                            + p.getY());
            if (state.is(ModBlocks.KRAKEN_SPAWN_BLOCK.get())) kraken = true;
            if (state.is(ModBlocks.DRAGON_SPAWN_BLOCK.get())) dragon = true;
        }
        helper.assertTrue(total > 0, "the pool must place spawn-ore veins");
        helper.assertTrue(kraken,
                "kraken_spawn_block appears via the pool (c50; λ≈46 over 200 passes)");
        helper.assertTrue(dragon,
                "dragon_spawn_block appears via the pool (c53; λ≈46 over 200 passes)");
        helper.succeed();
    }
}
