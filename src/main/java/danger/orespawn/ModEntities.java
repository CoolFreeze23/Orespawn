package danger.orespawn;

import danger.orespawn.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OreSpawnMod.MOD_ID);

    // ==================== MONSTER (Hostile) ====================

    public static final DeferredHolder<EntityType<?>, EntityType<Alien>> ALIEN =
            ENTITY_TYPES.register("alien", () -> EntityType.Builder.of(Alien::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("alien"));

    // Phase 12 — Alien Boss is a buffed Alien sub-type registered as a
    // separate entity ID so its summon-aura AI and persistent flag don't
    // bleed into the regular Alien spawn pool. Hitbox is 1.4 × 3.5
    // (roughly twice the standard Alien) so the boss reads as the dungeon
    // mini-boss it is. Tracking range is bumped to 16 chunks so distant
    // players can see it inside the WTF-Alien lapis chamber from the
    // surface entry shaft.
    public static final DeferredHolder<EntityType<?>, EntityType<AlienBoss>> ALIEN_BOSS =
            ENTITY_TYPES.register("alien_boss", () -> EntityType.Builder.of(AlienBoss::new, MobCategory.MONSTER)
                    .sized(1.4f, 3.5f).clientTrackingRange(16).build("alien_boss"));

    public static final DeferredHolder<EntityType<?>, EntityType<Alosaurus>> ALOSAURUS =
            ENTITY_TYPES.register("alosaurus", () -> EntityType.Builder.of(Alosaurus::new, MobCategory.MONSTER)
                    .sized(1.9f, 3.6f).clientTrackingRange(12).build("alosaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<AttackSquid>> ATTACK_SQUID =
            ENTITY_TYPES.register("attack_squid", () -> EntityType.Builder.of(AttackSquid::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).build("attack_squid"));

    public static final DeferredHolder<EntityType<?>, EntityType<BandP>> BAND_P =
            ENTITY_TYPES.register("band_p", () -> EntityType.Builder.of(BandP::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.0f).clientTrackingRange(10).build("band_p"));

    public static final DeferredHolder<EntityType<?>, EntityType<Basilisk>> BASILISK =
            ENTITY_TYPES.register("basilisk", () -> EntityType.Builder.of(Basilisk::new, MobCategory.MONSTER)
                    .sized(1.6f, 3.5f).clientTrackingRange(12).build("basilisk"));

    public static final DeferredHolder<EntityType<?>, EntityType<CaveFisher>> CAVE_FISHER =
            ENTITY_TYPES.register("cave_fisher", () -> EntityType.Builder.of(CaveFisher::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).build("cave_fisher"));

    public static final DeferredHolder<EntityType<?>, EntityType<CloudShark>> CLOUD_SHARK =
            ENTITY_TYPES.register("cloud_shark", () -> EntityType.Builder.of(CloudShark::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.0f).clientTrackingRange(10).build("cloud_shark"));

    public static final DeferredHolder<EntityType<?>, EntityType<Crab>> CRAB =
            ENTITY_TYPES.register("crab", () -> EntityType.Builder.of(Crab::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.6f).clientTrackingRange(10).build("crab"));

    public static final DeferredHolder<EntityType<?>, EntityType<CreepingHorror>> CREEPING_HORROR =
            ENTITY_TYPES.register("creeping_horror", () -> EntityType.Builder.of(CreepingHorror::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.5f).clientTrackingRange(10).build("creeping_horror"));

    public static final DeferredHolder<EntityType<?>, EntityType<Cryolophosaurus>> CRYOLOPHOSAURUS =
            ENTITY_TYPES.register("cryolophosaurus", () -> EntityType.Builder.of(Cryolophosaurus::new, MobCategory.MONSTER)
                    .sized(0.75f, 0.75f).clientTrackingRange(10).build("cryolophosaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<DungeonBeast>> DUNGEON_BEAST =
            ENTITY_TYPES.register("dungeon_beast", () -> EntityType.Builder.of(DungeonBeast::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.5f).clientTrackingRange(10).build("dungeon_beast"));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderKnight>> ENDER_KNIGHT =
            ENTITY_TYPES.register("ender_knight", () -> EntityType.Builder.of(EnderKnight::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("ender_knight"));

    public static final DeferredHolder<EntityType<?>, EntityType<EnderReaper>> ENDER_REAPER =
            ENTITY_TYPES.register("ender_reaper", () -> EntityType.Builder.of(EnderReaper::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.5f).clientTrackingRange(10).build("ender_reaper"));

    public static final DeferredHolder<EntityType<?>, EntityType<GiantRobot>> GIANT_ROBOT =
            ENTITY_TYPES.register("giant_robot", () -> EntityType.Builder.of(GiantRobot::new, MobCategory.MONSTER)
                    .sized(4.0f, 10.0f).clientTrackingRange(16).build("giant_robot"));

    public static final DeferredHolder<EntityType<?>, EntityType<Jeffery>> JEFFERY =
            ENTITY_TYPES.register("jeffery", () -> EntityType.Builder.of(Jeffery::new, MobCategory.MONSTER)
                    .sized(4.0f, 10.0f).clientTrackingRange(16).build("jeffery"));

    public static final DeferredHolder<EntityType<?>, EntityType<Hammerhead>> HAMMERHEAD =
            ENTITY_TYPES.register("hammerhead", () -> EntityType.Builder.of(Hammerhead::new, MobCategory.MONSTER)
                    .sized(2.8f, 1.8f).clientTrackingRange(10).build("hammerhead"));

    public static final DeferredHolder<EntityType<?>, EntityType<Irukandji>> IRUKANDJI =
            ENTITY_TYPES.register("irukandji", () -> EntityType.Builder.of(Irukandji::new, MobCategory.MONSTER)
                    .sized(0.4f, 0.4f).clientTrackingRange(10).build("irukandji"));

    public static final DeferredHolder<EntityType<?>, EntityType<Nastysaurus>> NASTYSAURUS =
            ENTITY_TYPES.register("nastysaurus", () -> EntityType.Builder.of(Nastysaurus::new, MobCategory.MONSTER)
                    .sized(2.2f, 4.6f).clientTrackingRange(12).build("nastysaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<PitchBlack>> PITCH_BLACK =
            ENTITY_TYPES.register("pitch_black", () -> EntityType.Builder.of(PitchBlack::new, MobCategory.MONSTER)
                    .sized(2.0f, 3.0f).clientTrackingRange(16).build("pitch_black"));

    public static final DeferredHolder<EntityType<?>, EntityType<Pointysaurus>> POINTYSAURUS =
            ENTITY_TYPES.register("pointysaurus", () -> EntityType.Builder.of(Pointysaurus::new, MobCategory.MONSTER)
                    .sized(2.9f, 2.9f).clientTrackingRange(12).build("pointysaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<Robot1>> ROBOT_1 =
            ENTITY_TYPES.register("robot_1", () -> EntityType.Builder.of(Robot1::new, MobCategory.MONSTER)
                    .sized(1.0f, 2.0f).clientTrackingRange(10).build("robot_1"));

    public static final DeferredHolder<EntityType<?>, EntityType<Robot2>> ROBOT_2 =
            ENTITY_TYPES.register("robot_2", () -> EntityType.Builder.of(Robot2::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.5f).clientTrackingRange(10).build("robot_2"));

    public static final DeferredHolder<EntityType<?>, EntityType<Robot3>> ROBOT_3 =
            ENTITY_TYPES.register("robot_3", () -> EntityType.Builder.of(Robot3::new, MobCategory.MONSTER)
                    .sized(2.0f, 3.0f).clientTrackingRange(10).build("robot_3"));

    public static final DeferredHolder<EntityType<?>, EntityType<Robot4>> ROBOT_4 =
            ENTITY_TYPES.register("robot_4", () -> EntityType.Builder.of(Robot4::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.5f).clientTrackingRange(10).build("robot_4"));

    public static final DeferredHolder<EntityType<?>, EntityType<Robot5>> ROBOT_5 =
            ENTITY_TYPES.register("robot_5", () -> EntityType.Builder.of(Robot5::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.5f).clientTrackingRange(10).build("robot_5"));

    public static final DeferredHolder<EntityType<?>, EntityType<SeaMonster>> SEA_MONSTER =
            ENTITY_TYPES.register("sea_monster", () -> EntityType.Builder.of(SeaMonster::new, MobCategory.MONSTER)
                    .sized(5.0f, 5.0f).clientTrackingRange(10).build("sea_monster"));

    public static final DeferredHolder<EntityType<?>, EntityType<SeaViper>> SEA_VIPER =
            ENTITY_TYPES.register("sea_viper", () -> EntityType.Builder.of(SeaViper::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.5f).clientTrackingRange(10).build("sea_viper"));

    public static final DeferredHolder<EntityType<?>, EntityType<Skate>> SKATE =
            ENTITY_TYPES.register("skate", () -> EntityType.Builder.of(Skate::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.4f).clientTrackingRange(10).build("skate"));

    public static final DeferredHolder<EntityType<?>, EntityType<TRex>> TREX =
            ENTITY_TYPES.register("trex", () -> EntityType.Builder.of(TRex::new, MobCategory.MONSTER)
                    .sized(2.0f, 4.2f).clientTrackingRange(16).build("trex"));

    public static final DeferredHolder<EntityType<?>, EntityType<Urchin>> URCHIN =
            ENTITY_TYPES.register("urchin", () -> EntityType.Builder.of(Urchin::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("urchin"));

    // "Mobzilla" is the public-facing boss name (see Godzilla#bossEvent which
    // sets Component.literal("Mobzilla")). The class retains the 1.7.10
    // source name (Godzilla.java) for port fidelity, but all user-visible
    // strings, armor sets, blocks, loot tables, and recipes in this mod use
    // the "mobzilla_*" namespace. The registry ID "orespawn:godzilla" stays
    // stable so existing save files continue to resolve the entity type.
    // clientTrackingRange=16 chunks is required — Mobzilla is 14x24 blocks
    // and can fly well outside the normal 10-chunk visibility range; without
    // this the client pops the entity out of view during long-range fights.
    // 1.7.10 func_70105_a: Godzilla = 9.9 × 25.0 (real) / 2.475 × 6.25 (egg form).
    public static final DeferredHolder<EntityType<?>, EntityType<Godzilla>> GODZILLA =
            ENTITY_TYPES.register("godzilla", () -> EntityType.Builder.of(Godzilla::new, MobCategory.MONSTER)
                    .sized(10.0f, 25.0f).clientTrackingRange(16).build("godzilla"));

    // 1.7.10 func_70105_a: Kraken = 4.0 × 15.0 (real) / 1.33 × 5.0 (egg form).
    // The previous 12 × 10 made the squid wider than tall, which clipped its
    // tendrils through the floor when it surfaced.
    public static final DeferredHolder<EntityType<?>, EntityType<Kraken>> KRAKEN =
            ENTITY_TYPES.register("kraken", () -> EntityType.Builder.of(Kraken::new, MobCategory.MONSTER)
                    .sized(4.0f, 15.0f).clientTrackingRange(16).build("kraken"));

    // Multi-part bosses ─────────────────────────────────────────────────
    // The parent's own .sized() AABB is intentionally smaller than the
    // visual silhouette — player hits are handled by the PartEntity array
    // registered in TheKing / TheQueen's constructors, not by this root
    // hitbox. The root AABB is still used for spawn-fit tests and for the
    // client-side tracking range, so it must not be 0×0.
    //
    // clientTrackingRange=16 chunks is required: the bosses can fly
    // 120 blocks from their home point, and players need to see them
    // coming from that far away even before the fight starts.

    public static final DeferredHolder<EntityType<?>, EntityType<TheKing>> THE_KING =
            ENTITY_TYPES.register("the_king", () -> EntityType.Builder.of(TheKing::new, MobCategory.MONSTER)
                    .sized(6.0f, 12.0f).clientTrackingRange(16).build("the_king"));

    public static final DeferredHolder<EntityType<?>, EntityType<TheQueen>> THE_QUEEN =
            ENTITY_TYPES.register("the_queen", () -> EntityType.Builder.of(TheQueen::new, MobCategory.MONSTER)
                    .sized(12.0f, 16.0f).clientTrackingRange(16).build("the_queen"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBee>> ENTITY_BEE =
            ENTITY_TYPES.register("bee", () -> EntityType.Builder.of(EntityBee::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("bee"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBrutalfly>> ENTITY_BRUTALFLY =
            ENTITY_TYPES.register("brutalfly", () -> EntityType.Builder.of(EntityBrutalfly::new, MobCategory.MONSTER)
                    .sized(1.2f, 1.2f).clientTrackingRange(10).build("brutalfly"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCaterKiller>> ENTITY_CATER_KILLER =
            ENTITY_TYPES.register("cater_killer", () -> EntityType.Builder.of(EntityCaterKiller::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.0f).clientTrackingRange(10).build("cater_killer"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEmperorScorpion>> ENTITY_EMPEROR_SCORPION =
            ENTITY_TYPES.register("emperor_scorpion", () -> EntityType.Builder.of(EntityEmperorScorpion::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.5f).clientTrackingRange(10).build("emperor_scorpion"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityHerculesBeetle>> ENTITY_HERCULES_BEETLE =
            ENTITY_TYPES.register("hercules_beetle", () -> EntityType.Builder.of(EntityHerculesBeetle::new, MobCategory.MONSTER)
                    .sized(1.2f, 1.0f).clientTrackingRange(10).build("hercules_beetle"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityKyuubi>> ENTITY_KYUUBI =
            ENTITY_TYPES.register("kyuubi", () -> EntityType.Builder.of(EntityKyuubi::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.2f).clientTrackingRange(10).build("kyuubi"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityLeafMonster>> ENTITY_LEAF_MONSTER =
            ENTITY_TYPES.register("leaf_monster", () -> EntityType.Builder.of(EntityLeafMonster::new, MobCategory.MONSTER)
                    .sized(0.8f, 1.5f).clientTrackingRange(10).build("leaf_monster"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityLurkingTerror>> ENTITY_LURKING_TERROR =
            ENTITY_TYPES.register("lurking_terror", () -> EntityType.Builder.of(EntityLurkingTerror::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f).clientTrackingRange(10).build("lurking_terror"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMantis>> ENTITY_MANTIS =
            ENTITY_TYPES.register("mantis", () -> EntityType.Builder.of(EntityMantis::new, MobCategory.MONSTER)
                    .sized(0.8f, 1.8f).clientTrackingRange(10).build("mantis"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMolenoid>> ENTITY_MOLENOID =
            ENTITY_TYPES.register("molenoid", () -> EntityType.Builder.of(EntityMolenoid::new, MobCategory.MONSTER)
                    .sized(1.2f, 2.0f).clientTrackingRange(10).build("molenoid"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRat>> ENTITY_RAT =
            ENTITY_TYPES.register("rat", () -> EntityType.Builder.of(EntityRat::new, MobCategory.MONSTER)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("rat"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRotator>> ENTITY_ROTATOR =
            ENTITY_TYPES.register("rotator", () -> EntityType.Builder.of(EntityRotator::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.6f).clientTrackingRange(10).build("rotator"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityScorpion>> ENTITY_SCORPION =
            ENTITY_TYPES.register("scorpion", () -> EntityType.Builder.of(EntityScorpion::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.4f).clientTrackingRange(10).build("scorpion"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySpitBug>> ENTITY_SPIT_BUG =
            ENTITY_TYPES.register("spit_bug", () -> EntityType.Builder.of(EntitySpitBug::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).build("spit_bug"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTerribleTerror>> ENTITY_TERRIBLE_TERROR =
            ENTITY_TYPES.register("terrible_terror", () -> EntityType.Builder.of(EntityTerribleTerror::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("terrible_terror"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTriffid>> ENTITY_TRIFFID =
            ENTITY_TYPES.register("triffid", () -> EntityType.Builder.of(EntityTriffid::new, MobCategory.MONSTER)
                    .sized(1.0f, 2.0f).clientTrackingRange(10).build("triffid"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTrooperBug>> ENTITY_TROOPER_BUG =
            ENTITY_TYPES.register("trooper_bug", () -> EntityType.Builder.of(EntityTrooperBug::new, MobCategory.MONSTER)
                    .sized(1.2f, 1.5f).clientTrackingRange(10).build("trooper_bug"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityVortex>> ENTITY_VORTEX =
            ENTITY_TYPES.register("vortex", () -> EntityType.Builder.of(EntityVortex::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.5f).clientTrackingRange(10).build("vortex"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWormSmall>> ENTITY_WORM_SMALL =
            ENTITY_TYPES.register("worm_small", () -> EntityType.Builder.of(EntityWormSmall::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("worm_small"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWormMedium>> ENTITY_WORM_MEDIUM =
            ENTITY_TYPES.register("worm_medium", () -> EntityType.Builder.of(EntityWormMedium::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f).clientTrackingRange(10).build("worm_medium"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWormLarge>> ENTITY_WORM_LARGE =
            ENTITY_TYPES.register("worm_large", () -> EntityType.Builder.of(EntityWormLarge::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.5f).clientTrackingRange(10).build("worm_large"));

    // ==================== CREATURE (Passive) ====================

    public static final DeferredHolder<EntityType<?>, EntityType<Baryonyx>> BARYONYX =
            ENTITY_TYPES.register("baryonyx", () -> EntityType.Builder.of(Baryonyx::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.5f).clientTrackingRange(10).build("baryonyx"));

    public static final DeferredHolder<EntityType<?>, EntityType<Beaver>> BEAVER =
            ENTITY_TYPES.register("beaver", () -> EntityType.Builder.of(Beaver::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.8f).clientTrackingRange(8).build("beaver"));

    public static final DeferredHolder<EntityType<?>, EntityType<Cassowary>> CASSOWARY =
            ENTITY_TYPES.register("cassowary", () -> EntityType.Builder.of(Cassowary::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.2f).clientTrackingRange(10).build("cassowary"));

    public static final DeferredHolder<EntityType<?>, EntityType<Chipmunk>> CHIPMUNK =
            ENTITY_TYPES.register("chipmunk", () -> EntityType.Builder.of(Chipmunk::new, MobCategory.CREATURE)
                    .sized(0.35f, 0.35f).clientTrackingRange(8).build("chipmunk"));

    public static final DeferredHolder<EntityType<?>, EntityType<Cockateil>> COCKATEIL =
            ENTITY_TYPES.register("cockateil", () -> EntityType.Builder.of(Cockateil::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("cockateil"));

    // Coin is a dropped-currency pickup entity, not a natural-spawn creature.
    // Registered under MISC so the vanilla CREATURE spawn logic never tries
    // to place it in the world. The class still extends Animal for movement
    // physics, but the registry category controls natural-spawning gates.
    public static final DeferredHolder<EntityType<?>, EntityType<Coin>> COIN =
            ENTITY_TYPES.register("coin", () -> EntityType.Builder.of(Coin::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("coin"));

    public static final DeferredHolder<EntityType<?>, EntityType<EasterBunny>> EASTER_BUNNY =
            ENTITY_TYPES.register("easter_bunny", () -> EntityType.Builder.of(EasterBunny::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.6f).clientTrackingRange(8).build("easter_bunny"));

    public static final DeferredHolder<EntityType<?>, EntityType<Flounder>> FLOUNDER =
            ENTITY_TYPES.register("flounder", () -> EntityType.Builder.of(Flounder::new, MobCategory.WATER_CREATURE)
                    .sized(0.5f, 0.3f).clientTrackingRange(8).build("flounder"));

    public static final DeferredHolder<EntityType<?>, EntityType<Frog>> FROG =
            ENTITY_TYPES.register("frog", () -> EntityType.Builder.of(Frog::new, MobCategory.CREATURE)
                    .sized(0.75f, 0.75f).clientTrackingRange(8).build("frog"));

    public static final DeferredHolder<EntityType<?>, EntityType<Gazelle>> GAZELLE =
            ENTITY_TYPES.register("gazelle", () -> EntityType.Builder.of(Gazelle::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("gazelle"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoldFish>> GOLD_FISH =
            ENTITY_TYPES.register("gold_fish", () -> EntityType.Builder.of(GoldFish::new, MobCategory.CREATURE)
                    .sized(0.3f, 0.3f).clientTrackingRange(8).build("gold_fish"));

    // Island entities are 1.7.10-style: the entity itself is a tiny 0.5x0.5
    // marker that places sand/gravel/sandstone (Island) or grass/stone
    // (IslandToo) as real world blocks via its tick() method. The visible
    // "10x6 floating island" is those placed blocks, not the entity's AABB.
    // Keeping the hitbox tiny avoids a 10x6 collision/pathfinder probe on
    // every tick and faithfully matches 1.7.10's func_70105_a(0.5f, 0.5f).
    // clientTrackingRange stays at 16 chunks so the placed structure stays
    // visible at long distances.
    public static final DeferredHolder<EntityType<?>, EntityType<Island>> ISLAND =
            ENTITY_TYPES.register("island", () -> EntityType.Builder.of(Island::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f).clientTrackingRange(16).build("island"));

    public static final DeferredHolder<EntityType<?>, EntityType<IslandToo>> ISLAND_TOO =
            ENTITY_TYPES.register("island_too", () -> EntityType.Builder.of(IslandToo::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f).clientTrackingRange(16).build("island_too"));

    public static final DeferredHolder<EntityType<?>, EntityType<Peacock>> PEACOCK =
            ENTITY_TYPES.register("peacock", () -> EntityType.Builder.of(Peacock::new, MobCategory.CREATURE)
                    .sized(0.65f, 1.2f).clientTrackingRange(10).build("peacock"));

    public static final DeferredHolder<EntityType<?>, EntityType<Whale>> WHALE =
            ENTITY_TYPES.register("whale", () -> EntityType.Builder.of(Whale::new, MobCategory.CREATURE)
                    .sized(1.5f, 2.5f).clientTrackingRange(10).build("whale"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityAnt>> ENTITY_ANT =
            ENTITY_TYPES.register("ant", () -> EntityType.Builder.of(EntityAnt::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("ant"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCliffRacer>> ENTITY_CLIFF_RACER =
            ENTITY_TYPES.register("cliff_racer", () -> EntityType.Builder.of(EntityCliffRacer::new, MobCategory.CREATURE)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).build("cliff_racer"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCricket>> ENTITY_CRICKET =
            ENTITY_TYPES.register("cricket", () -> EntityType.Builder.of(EntityCricket::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("cricket"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityDragonfly>> ENTITY_DRAGONFLY =
            ENTITY_TYPES.register("dragonfly", () -> EntityType.Builder.of(EntityDragonfly::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("dragonfly"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRedAnt>> ENTITY_RED_ANT =
            ENTITY_TYPES.register("red_ant", () -> EntityType.Builder.of(EntityRedAnt::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("red_ant"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRainbowAnt>> ENTITY_RAINBOW_ANT =
            ENTITY_TYPES.register("rainbow_ant", () -> EntityType.Builder.of(EntityRainbowAnt::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("rainbow_ant"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityStinkBug>> ENTITY_STINK_BUG =
            ENTITY_TYPES.register("stink_bug", () -> EntityType.Builder.of(EntityStinkBug::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("stink_bug"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTermite>> ENTITY_TERMITE =
            ENTITY_TYPES.register("termite", () -> EntityType.Builder.of(EntityTermite::new, MobCategory.CREATURE)
                    .sized(0.3f, 0.3f).clientTrackingRange(8).build("termite"));

    // T-Shirt is a novelty gag entity (cosmetic prop) — not a naturally
    // spawning mob. Registered under MISC to exclude it from CREATURE spawn
    // passes; it is still summonable via /summon and spawn-eggs.
    public static final DeferredHolder<EntityType<?>, EntityType<EntityTshirt>> ENTITY_TSHIRT =
            ENTITY_TYPES.register("tshirt", () -> EntityType.Builder.of(EntityTshirt::new, MobCategory.MISC)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("tshirt"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityUnstableAnt>> ENTITY_UNSTABLE_ANT =
            ENTITY_TYPES.register("unstable_ant", () -> EntityType.Builder.of(EntityUnstableAnt::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("unstable_ant"));

    // ==================== CREATURE (Tameable) ====================

    public static final DeferredHolder<EntityType<?>, EntityType<Boyfriend>> BOYFRIEND =
            ENTITY_TYPES.register("boyfriend", () -> EntityType.Builder.of(Boyfriend::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("boyfriend"));

    public static final DeferredHolder<EntityType<?>, EntityType<Camarasaurus>> CAMARASAURUS =
            ENTITY_TYPES.register("camarasaurus", () -> EntityType.Builder.of(Camarasaurus::new, MobCategory.CREATURE)
                    .sized(1.4f, 2.6f).clientTrackingRange(10).build("camarasaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<Dragon>> DRAGON =
            ENTITY_TYPES.register("dragon", () -> EntityType.Builder.of(Dragon::new, MobCategory.CREATURE)
                    .sized(1.5f, 2.0f).clientTrackingRange(10).build("dragon"));

    // Baby Dragon — Wiki/1.7.10 lists Baby Dragon as a separate entity type
    // from the adult Dragon (with reduced HP and pre-fledged fire breath).
    // Hitbox is roughly 1.0 × 1.4 (about half the adult footprint). Subclass
    // of Dragon so it inherits flight/fire AI but overrides attribute pool.
    public static final DeferredHolder<EntityType<?>, EntityType<BabyDragon>> BABY_DRAGON =
            ENTITY_TYPES.register("baby_dragon", () -> EntityType.Builder.of(BabyDragon::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.4f).clientTrackingRange(10).build("baby_dragon"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCannonFodder>> ENTITY_CANNON_FODDER =
            ENTITY_TYPES.register("cannon_fodder", () -> EntityType.Builder.of(EntityCannonFodder::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f).clientTrackingRange(10).build("cannon_fodder"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGammaMetroid>> ENTITY_GAMMA_METROID =
            ENTITY_TYPES.register("gamma_metroid", () -> EntityType.Builder.of(EntityGammaMetroid::new, MobCategory.CREATURE)
                    .sized(1.5f, 1.5f).clientTrackingRange(10).build("gamma_metroid"));

    public static final DeferredHolder<EntityType<?>, EntityType<Girlfriend>> GIRLFRIEND =
            ENTITY_TYPES.register("girlfriend", () -> EntityType.Builder.of(Girlfriend::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("girlfriend"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityHydrolisc>> ENTITY_HYDROLISC =
            ENTITY_TYPES.register("hydrolisc", () -> EntityType.Builder.of(EntityHydrolisc::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("hydrolisc"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityLeon>> ENTITY_LEON =
            ENTITY_TYPES.register("leon", () -> EntityType.Builder.of(EntityLeon::new, MobCategory.CREATURE)
                    .sized(3.0f, 4.5f).clientTrackingRange(12).build("leon"));

    // Leonopteryx — 1.7.10 Mining-Dimension flying boss/mount. Hitbox is
    // 4 × 2 (the visible silhouette is much larger but a tight AABB lets
    // the player land a sword swing on the head/wing without invisible
    // collision walls). clientTrackingRange=16 chunks because the bird
    // hovers far above the player and must stay rendered during pursuit.
    public static final DeferredHolder<EntityType<?>, EntityType<Leonopteryx>> LEONOPTERYX =
            ENTITY_TYPES.register("leonopteryx", () -> EntityType.Builder.of(Leonopteryx::new, MobCategory.CREATURE)
                    .sized(4.0f, 2.0f).clientTrackingRange(16).build("leonopteryx"));

    public static final DeferredHolder<EntityType<?>, EntityType<Lizard>> LIZARD =
            ENTITY_TYPES.register("lizard", () -> EntityType.Builder.of(Lizard::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f).clientTrackingRange(10).build("lizard"));

    public static final DeferredHolder<EntityType<?>, EntityType<Ostrich>> OSTRICH =
            ENTITY_TYPES.register("ostrich", () -> EntityType.Builder.of(Ostrich::new, MobCategory.CREATURE)
                    .sized(0.85f, 2.1f).clientTrackingRange(10).build("ostrich"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRubberDucky>> ENTITY_RUBBER_DUCKY =
            ENTITY_TYPES.register("rubber_ducky", () -> EntityType.Builder.of(EntityRubberDucky::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("rubber_ducky"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySpyro>> ENTITY_SPYRO =
            ENTITY_TYPES.register("spyro", () -> EntityType.Builder.of(EntitySpyro::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("spyro"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityStinky>> ENTITY_STINKY =
            ENTITY_TYPES.register("stinky", () -> EntityType.Builder.of(EntityStinky::new, MobCategory.CREATURE)
                    .sized(0.75f, 0.75f).clientTrackingRange(10).build("stinky"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrince>> THE_PRINCE =
            ENTITY_TYPES.register("the_prince", () -> EntityType.Builder.of(ThePrince::new, MobCategory.CREATURE)
                    .sized(0.75f, 1.25f).clientTrackingRange(10).build("the_prince"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrinceAdult>> THE_PRINCE_ADULT =
            ENTITY_TYPES.register("the_prince_adult", () -> EntityType.Builder.of(ThePrinceAdult::new, MobCategory.CREATURE)
                    .sized(4.0f, 6.0f).clientTrackingRange(16).build("the_prince_adult"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrincess>> THE_PRINCESS =
            ENTITY_TYPES.register("the_princess", () -> EntityType.Builder.of(ThePrincess::new, MobCategory.CREATURE)
                    .sized(0.75f, 1.25f).clientTrackingRange(10).build("the_princess"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrinceTeen>> THE_PRINCE_TEEN =
            ENTITY_TYPES.register("the_prince_teen", () -> EntityType.Builder.of(ThePrinceTeen::new, MobCategory.CREATURE)
                    .sized(2.0f, 3.0f).clientTrackingRange(12).build("the_prince_teen"));

    public static final DeferredHolder<EntityType<?>, EntityType<VelocityRaptor>> VELOCITY_RAPTOR =
            ENTITY_TYPES.register("velocity_raptor", () -> EntityType.Builder.of(VelocityRaptor::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.6f).clientTrackingRange(10).build("velocity_raptor"));

    public static final DeferredHolder<EntityType<?>, EntityType<WaterDragon>> WATER_DRAGON =
            ENTITY_TYPES.register("water_dragon", () -> EntityType.Builder.of(WaterDragon::new, MobCategory.WATER_CREATURE)
                    .sized(1.25f, 1.9f).clientTrackingRange(12).build("water_dragon"));

    // ==================== AMBIENT ====================

    public static final DeferredHolder<EntityType<?>, EntityType<EntityButterfly>> ENTITY_BUTTERFLY =
            ENTITY_TYPES.register("butterfly", () -> EntityType.Builder.of(EntityButterfly::new, MobCategory.AMBIENT)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("butterfly"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityLunaMoth>> ENTITY_LUNA_MOTH =
            ENTITY_TYPES.register("luna_moth", () -> EntityType.Builder.of(EntityLunaMoth::new, MobCategory.AMBIENT)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("luna_moth"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMosquito>> ENTITY_MOSQUITO =
            ENTITY_TYPES.register("mosquito", () -> EntityType.Builder.of(EntityMosquito::new, MobCategory.AMBIENT)
                    .sized(0.3f, 0.3f).clientTrackingRange(8).build("mosquito"));

    public static final DeferredHolder<EntityType<?>, EntityType<Fairy>> FAIRY =
            ENTITY_TYPES.register("fairy", () -> EntityType.Builder.of(Fairy::new, MobCategory.AMBIENT)
                    .sized(0.4f, 0.8f).clientTrackingRange(8).build("fairy"));

    public static final DeferredHolder<EntityType<?>, EntityType<Firefly>> FIREFLY =
            ENTITY_TYPES.register("firefly", () -> EntityType.Builder.of(Firefly::new, MobCategory.AMBIENT)
                    .sized(0.2f, 0.2f).clientTrackingRange(8).build("firefly"));

    // Ghost / GhostSkelly were AMBIENT originally, but in 1.7.10 source they
    // are combat mobs that attack players (see reference_1_7_10_source
    // Ghost.java/GhostSkelly.java). AMBIENT caps out their spawn attempts at
    // a very low mob-cap and routes them through the AMBIENT (bat-style)
    // spawn rules — inappropriate for an aggressive mob. Moved to MONSTER
    // so future combat-AI goals (Phase 4B/C) hook into the proper target
    // selector + despawn rules, and spawning respects light-level gating.
    //
    // NOTE: the Java classes still extend AmbientCreature today. That's OK
    // at the registry level (MobCategory is independent of class hierarchy),
    // but Phase 4C will refactor them to extend Monster/PathfinderMob so
    // they can carry real target goals. Tracking issue: see inventory notes.
    // Ghost: 1.7.10 func_70105_a(0.5f, 1.5f).
    public static final DeferredHolder<EntityType<?>, EntityType<Ghost>> GHOST =
            ENTITY_TYPES.register("ghost", () -> EntityType.Builder.of(Ghost::new, MobCategory.MONSTER)
                    .sized(0.5f, 1.5f).clientTrackingRange(10).build("ghost"));

    // GhostSkelly: 1.7.10 func_70105_a(1.5f, 2.0f) — intentionally larger
    // than Ghost so its skeletal silhouette reads correctly at distance.
    public static final DeferredHolder<EntityType<?>, EntityType<GhostSkelly>> GHOST_SKELLY =
            ENTITY_TYPES.register("ghost_skelly", () -> EntityType.Builder.of(GhostSkelly::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.0f).clientTrackingRange(10).build("ghost_skelly"));

    // 1.7.10 func_70105_a: Mothra = 5.0 × 2.0. We bump to 6 × 3 so the
    // wing PartEntities (which extend ±6 sideways) read correctly against
    // the root hitbox during cross-biome target sweeps.
    public static final DeferredHolder<EntityType<?>, EntityType<Mothra>> MOTHRA =
            ENTITY_TYPES.register("mothra", () -> EntityType.Builder.of(Mothra::new, MobCategory.MONSTER)
                    .sized(6.0f, 3.0f).clientTrackingRange(16).build("mothra"));

    // Phase 14 — wiki-canon hostile butterfly variant for the Chaos
    // (a.k.a. "Danger") Dimension. Registered as MONSTER (not AMBIENT
    // like the standard butterfly) so it routes through the hostile
    // mob-cap and respects monster-spawn rules. Separate registration
    // keeps the ambient EntityButterfly's right-click → Chaos teleport
    // contract intact and avoids polluting its overworld spawn pool.
    public static final DeferredHolder<EntityType<?>, EntityType<VampireButterfly>> VAMPIRE_BUTTERFLY =
            ENTITY_TYPES.register("vampire_butterfly", () -> EntityType.Builder.of(VampireButterfly::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("vampire_butterfly"));

    // ==================== MISC (Mob) ====================

    public static final DeferredHolder<EntityType<?>, EntityType<AntRobot>> ANT_ROBOT =
            ENTITY_TYPES.register("ant_robot", () -> EntityType.Builder.of(AntRobot::new, MobCategory.MISC)
                    .sized(2.0f, 3.0f).clientTrackingRange(10).build("ant_robot"));

    public static final DeferredHolder<EntityType<?>, EntityType<Elevator>> ELEVATOR =
            ENTITY_TYPES.register("elevator", () -> EntityType.Builder.of(Elevator::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f).clientTrackingRange(10).build("elevator"));

    // Phase 10 — Hoverboard (1.7.10 Elevator.java port). Wide flat hitbox so
    // the rider's view sits where they expect; tracking range bumped to 128
    // (matches legacy Elevator.getTrackingRange()) so distant boards don't
    // snap to a stop on chunk-load.
    public static final DeferredHolder<EntityType<?>, EntityType<HoverboardEntity>> HOVERBOARD =
            ENTITY_TYPES.register("hoverboard", () -> EntityType.Builder.of(HoverboardEntity::new, MobCategory.MISC)
                    .sized(1.25f, 0.4f).clientTrackingRange(8).updateInterval(2).build("hoverboard"));

    // Legacy 1.7.10 sidecar heads ─ deprecated but kept for save compat.
    // In 1.7.10 each giant boss's "head" was a separate tracked entity that
    // teleported next to the parent every tick; see KingHead.java,
    // QueenHead.java, and GodzillaHead.java for the per-tick positioning
    // loops. In 1.21.1 this role is filled by OreSpawnPartEntity instances
    // owned by TheKing / TheQueen / Godzilla (Mobzilla), but the registry
    // entries must survive because:
    //   1. Existing saves contain instances of these entity types.
    //   2. TheKing / TheQueen / Godzilla still spawn one each to keep the
    //      1.7.10 flight-hook and AI-targeting parity.
    // Their client-side renderers (KingHeadRenderer, QueenHeadRenderer,
    // GodzillaHeadRenderer) all short-circuit shouldRender()=false so they
    // are invisible and have zero rendering cost — matching the 1.7.10
    // behaviour where those Render* classes were empty stubs.
    // Future removal: delete the spawn calls, bump the save-data version,
    // then drop these registrations.

    public static final DeferredHolder<EntityType<?>, EntityType<KingHead>> KING_HEAD =
            ENTITY_TYPES.register("king_head", () -> EntityType.Builder.of(KingHead::new, MobCategory.MISC)
                    .sized(3.0f, 3.0f).clientTrackingRange(10).build("king_head"));

    public static final DeferredHolder<EntityType<?>, EntityType<QueenHead>> QUEEN_HEAD =
            ENTITY_TYPES.register("queen_head", () -> EntityType.Builder.of(QueenHead::new, MobCategory.MISC)
                    .sized(2.0f, 2.0f).clientTrackingRange(10).build("queen_head"));

    public static final DeferredHolder<EntityType<?>, EntityType<GodzillaHead>> GODZILLA_HEAD =
            ENTITY_TYPES.register("godzilla_head", () -> EntityType.Builder.of(GodzillaHead::new, MobCategory.MISC)
                    .sized(3.0f, 3.0f).clientTrackingRange(10).build("godzilla_head"));

    public static final DeferredHolder<EntityType<?>, EntityType<PurplePower>> PURPLE_POWER =
            ENTITY_TYPES.register("purple_power", () -> EntityType.Builder.of(PurplePower::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("purple_power"));

    public static final DeferredHolder<EntityType<?>, EntityType<RockBase>> ROCK_BASE =
            ENTITY_TYPES.register("rock_base", () -> EntityType.Builder.of(RockBase::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("rock_base"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpiderRobot>> SPIDER_ROBOT =
            ENTITY_TYPES.register("spider_robot", () -> EntityType.Builder.of(SpiderRobot::new, MobCategory.MISC)
                    .sized(2.0f, 1.5f).clientTrackingRange(10).build("spider_robot"));

    public static final DeferredHolder<EntityType<?>, EntityType<Cephadrome>> CEPHADROME =
            ENTITY_TYPES.register("cephadrome", () -> EntityType.Builder.of(Cephadrome::new, MobCategory.MISC)
                    .sized(1.5f, 1.5f).clientTrackingRange(10).build("cephadrome"));

    public static final DeferredHolder<EntityType<?>, EntityType<RedCow>> RED_COW =
            ENTITY_TYPES.register("red_cow", () -> EntityType.Builder.of(RedCow::new, MobCategory.MISC)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("red_cow"));

    public static final DeferredHolder<EntityType<?>, EntityType<CrystalCow>> CRYSTAL_COW =
            ENTITY_TYPES.register("crystal_cow", () -> EntityType.Builder.of(CrystalCow::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("crystal_cow"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoldCow>> GOLD_COW =
            ENTITY_TYPES.register("gold_cow", () -> EntityType.Builder.of(GoldCow::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("gold_cow"));

    // Phase 14 follow-up — the previously separate `enchanted_cow`
    // entity was deleted and folded into ENCHANTED_APPLE_COW (below)
    // because they were mechanically and visually duplicates. The full
    // legacy loot table, dropCustomDeathLoot bonuses, and biome-modifier
    // spawn entries (dim_village_locals + dim_utopia_locals) all migrated
    // to the consolidated entity. Existing save instances of the old
    // registry ID will be silently dropped on world load via NeoForge's
    // missing-mapping handler — acceptable per the consolidation request.

    // Phase 14 — wiki-canon overworld cow variants. Not in 1.7.10
    // source (verified zero references in reference_1_7_10_source/)
    // but enumerated on the Wiki "Added Mobs" page; ship them so the
    // 56-entry hostile/neutral roster reaches 100% wiki coverage.
    // Hitbox + tracking range mirror the existing OreSpawn cow line so
    // they slot into pack-hunt AI ranges identically to RedCow.
    public static final DeferredHolder<EntityType<?>, EntityType<AppleCow>> APPLE_COW =
            ENTITY_TYPES.register("apple_cow", () -> EntityType.Builder.of(AppleCow::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("apple_cow"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoldenAppleCow>> GOLDEN_APPLE_COW =
            ENTITY_TYPES.register("golden_apple_cow", () -> EntityType.Builder.of(GoldenAppleCow::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("golden_apple_cow"));

    // Endgame-tier wiki-canon variant capping the apple → golden apple →
    // enchanted golden apple ladder. Same hitbox/tracking range as the
    // rest of the cow line so it pack-hunts identically; rarity is
    // controlled at the spawn-modifier weight rather than at the
    // entity level so the registry stays uniform with siblings.
    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedAppleCow>> ENCHANTED_APPLE_COW =
            ENTITY_TYPES.register("enchanted_apple_cow", () -> EntityType.Builder.of(EnchantedAppleCow::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("enchanted_apple_cow"));

    public static final DeferredHolder<EntityType<?>, EntityType<RubyBird>> RUBY_BIRD =
            ENTITY_TYPES.register("ruby_bird", () -> EntityType.Builder.of(RubyBird::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("ruby_bird"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpiderDriver>> SPIDER_DRIVER =
            ENTITY_TYPES.register("spider_driver", () -> EntityType.Builder.of(SpiderDriver::new, MobCategory.MISC)
                    .sized(1.4f, 0.9f).clientTrackingRange(10).build("spider_driver"));

    // ==================== MISC (Projectiles) ====================

    public static final DeferredHolder<EntityType<?>, EntityType<BetterFireball>> BETTER_FIREBALL =
            ENTITY_TYPES.register("better_fireball", () -> EntityType.Builder.<BetterFireball>of(BetterFireball::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("better_fireball"));

    public static final DeferredHolder<EntityType<?>, EntityType<BerthaHit>> BERTHA_HIT =
            ENTITY_TYPES.register("bertha_hit", () -> EntityType.Builder.<BerthaHit>of(BerthaHit::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("bertha_hit"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCage>> ENTITY_CAGE =
            ENTITY_TYPES.register("cage", () -> EntityType.Builder.<EntityCage>of(EntityCage::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("cage"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityThrownRock>> ENTITY_THROWN_ROCK =
            ENTITY_TYPES.register("thrown_rock", () -> EntityType.Builder.<EntityThrownRock>of(EntityThrownRock::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("thrown_rock"));

    public static final DeferredHolder<EntityType<?>, EntityType<InkSack>> INK_SACK =
            ENTITY_TYPES.register("ink_sack", () -> EntityType.Builder.<InkSack>of(InkSack::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("ink_sack"));

    public static final DeferredHolder<EntityType<?>, EntityType<LaserBall>> LASER_BALL =
            ENTITY_TYPES.register("laser_ball", () -> EntityType.Builder.<LaserBall>of(LaserBall::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(8).updateInterval(2).noSummon().build("laser_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<Shoes>> SHOES =
            ENTITY_TYPES.register("shoes", () -> EntityType.Builder.<Shoes>of(Shoes::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("shoes"));

    public static final DeferredHolder<EntityType<?>, EntityType<SunspotUrchin>> SUNSPOT_URCHIN =
            ENTITY_TYPES.register("sunspot_urchin", () -> EntityType.Builder.<SunspotUrchin>of(SunspotUrchin::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("sunspot_urchin"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThunderBolt>> THUNDER_BOLT =
            ENTITY_TYPES.register("thunder_bolt", () -> EntityType.Builder.<ThunderBolt>of(ThunderBolt::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("thunder_bolt"));

    public static final DeferredHolder<EntityType<?>, EntityType<WaterBall>> WATER_BALL =
            ENTITY_TYPES.register("water_ball", () -> EntityType.Builder.<WaterBall>of(WaterBall::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("water_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<IceBall>> ICE_BALL =
            ENTITY_TYPES.register("ice_ball", () -> EntityType.Builder.<IceBall>of(IceBall::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("ice_ball"));

    public static final DeferredHolder<EntityType<?>, EntityType<Acid>> ACID =
            ENTITY_TYPES.register("acid", () -> EntityType.Builder.<Acid>of(Acid::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("acid"));

    public static final DeferredHolder<EntityType<?>, EntityType<DeadIrukandji>> DEAD_IRUKANDJI =
            ENTITY_TYPES.register("dead_irukandji", () -> EntityType.Builder.<DeadIrukandji>of(DeadIrukandji::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).noSummon().build("dead_irukandji"));

    public static final DeferredHolder<EntityType<?>, EntityType<UltimateArrow>> ULTIMATE_ARROW =
            ENTITY_TYPES.register("ultimate_arrow", () -> EntityType.Builder.<UltimateArrow>of(UltimateArrow::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(8).updateInterval(2).noSummon().build("ultimate_arrow"));

    public static final DeferredHolder<EntityType<?>, EntityType<IrukandjiArrow>> IRUKANDJI_ARROW =
            ENTITY_TYPES.register("irukandji_arrow", () -> EntityType.Builder.<IrukandjiArrow>of(IrukandjiArrow::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).clientTrackingRange(8).updateInterval(2).noSummon().build("irukandji_arrow"));

    public static final DeferredHolder<EntityType<?>, EntityType<UltimateFishHook>> ULTIMATE_FISH_HOOK =
            ENTITY_TYPES.register("ultimate_fish_hook", () -> EntityType.Builder.<UltimateFishHook>of(UltimateFishHook::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(5).noSummon().build("ultimate_fish_hook"));

    // Fire-immune item entity for drops from lava-dwelling mobs
    public static final DeferredHolder<EntityType<?>, EntityType<EntityLavaLovingItem>> LAVA_LOVING_ITEM =
            ENTITY_TYPES.register("lava_loving_item", () -> EntityType.Builder.<EntityLavaLovingItem>of(EntityLavaLovingItem::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(6).updateInterval(20).fireImmune().build("lava_loving_item"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
