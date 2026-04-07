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

    public static final DeferredHolder<EntityType<?>, EntityType<Alosaurus>> ALOSAURUS =
            ENTITY_TYPES.register("alosaurus", () -> EntityType.Builder.of(Alosaurus::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.8f).clientTrackingRange(10).build("alosaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<AttackSquid>> ATTACK_SQUID =
            ENTITY_TYPES.register("attack_squid", () -> EntityType.Builder.of(AttackSquid::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).build("attack_squid"));

    public static final DeferredHolder<EntityType<?>, EntityType<BandP>> BAND_P =
            ENTITY_TYPES.register("band_p", () -> EntityType.Builder.of(BandP::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.0f).clientTrackingRange(10).build("band_p"));

    public static final DeferredHolder<EntityType<?>, EntityType<Basilisk>> BASILISK =
            ENTITY_TYPES.register("basilisk", () -> EntityType.Builder.of(Basilisk::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f).clientTrackingRange(10).build("basilisk"));

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
                    .sized(0.8f, 1.5f).clientTrackingRange(10).build("cryolophosaurus"));

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
                    .sized(3.0f, 6.0f).clientTrackingRange(16).build("giant_robot"));

    public static final DeferredHolder<EntityType<?>, EntityType<Hammerhead>> HAMMERHEAD =
            ENTITY_TYPES.register("hammerhead", () -> EntityType.Builder.of(Hammerhead::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.0f).clientTrackingRange(10).build("hammerhead"));

    public static final DeferredHolder<EntityType<?>, EntityType<Irukandji>> IRUKANDJI =
            ENTITY_TYPES.register("irukandji", () -> EntityType.Builder.of(Irukandji::new, MobCategory.MONSTER)
                    .sized(0.4f, 0.4f).clientTrackingRange(10).build("irukandji"));

    public static final DeferredHolder<EntityType<?>, EntityType<Nastysaurus>> NASTYSAURUS =
            ENTITY_TYPES.register("nastysaurus", () -> EntityType.Builder.of(Nastysaurus::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.0f).clientTrackingRange(10).build("nastysaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<PitchBlack>> PITCH_BLACK =
            ENTITY_TYPES.register("pitch_black", () -> EntityType.Builder.of(PitchBlack::new, MobCategory.MONSTER)
                    .sized(2.0f, 3.0f).clientTrackingRange(16).build("pitch_black"));

    public static final DeferredHolder<EntityType<?>, EntityType<Pointysaurus>> POINTYSAURUS =
            ENTITY_TYPES.register("pointysaurus", () -> EntityType.Builder.of(Pointysaurus::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.5f).clientTrackingRange(10).build("pointysaurus"));

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
                    .sized(2.0f, 2.0f).clientTrackingRange(10).build("sea_monster"));

    public static final DeferredHolder<EntityType<?>, EntityType<SeaViper>> SEA_VIPER =
            ENTITY_TYPES.register("sea_viper", () -> EntityType.Builder.of(SeaViper::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("sea_viper"));

    public static final DeferredHolder<EntityType<?>, EntityType<Skate>> SKATE =
            ENTITY_TYPES.register("skate", () -> EntityType.Builder.of(Skate::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.4f).clientTrackingRange(10).build("skate"));

    public static final DeferredHolder<EntityType<?>, EntityType<TRex>> TREX =
            ENTITY_TYPES.register("trex", () -> EntityType.Builder.of(TRex::new, MobCategory.MONSTER)
                    .sized(1.5f, 2.5f).clientTrackingRange(10).build("trex"));

    public static final DeferredHolder<EntityType<?>, EntityType<Urchin>> URCHIN =
            ENTITY_TYPES.register("urchin", () -> EntityType.Builder.of(Urchin::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("urchin"));

    public static final DeferredHolder<EntityType<?>, EntityType<Godzilla>> GODZILLA =
            ENTITY_TYPES.register("godzilla", () -> EntityType.Builder.of(Godzilla::new, MobCategory.MONSTER)
                    .sized(8.0f, 16.0f).clientTrackingRange(16).build("godzilla"));

    public static final DeferredHolder<EntityType<?>, EntityType<Kraken>> KRAKEN =
            ENTITY_TYPES.register("kraken", () -> EntityType.Builder.of(Kraken::new, MobCategory.MONSTER)
                    .sized(6.0f, 6.0f).clientTrackingRange(16).build("kraken"));

    public static final DeferredHolder<EntityType<?>, EntityType<TheKing>> THE_KING =
            ENTITY_TYPES.register("the_king", () -> EntityType.Builder.of(TheKing::new, MobCategory.MONSTER)
                    .sized(6.0f, 12.0f).clientTrackingRange(16).build("the_king"));

    public static final DeferredHolder<EntityType<?>, EntityType<TheQueen>> THE_QUEEN =
            ENTITY_TYPES.register("the_queen", () -> EntityType.Builder.of(TheQueen::new, MobCategory.MONSTER)
                    .sized(5.0f, 8.0f).clientTrackingRange(16).build("the_queen"));

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
                    .sized(0.6f, 1.5f).clientTrackingRange(10).build("cassowary"));

    public static final DeferredHolder<EntityType<?>, EntityType<Chipmunk>> CHIPMUNK =
            ENTITY_TYPES.register("chipmunk", () -> EntityType.Builder.of(Chipmunk::new, MobCategory.CREATURE)
                    .sized(0.35f, 0.35f).clientTrackingRange(8).build("chipmunk"));

    public static final DeferredHolder<EntityType<?>, EntityType<Cockateil>> COCKATEIL =
            ENTITY_TYPES.register("cockateil", () -> EntityType.Builder.of(Cockateil::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("cockateil"));

    public static final DeferredHolder<EntityType<?>, EntityType<Coin>> COIN =
            ENTITY_TYPES.register("coin", () -> EntityType.Builder.of(Coin::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("coin"));

    public static final DeferredHolder<EntityType<?>, EntityType<EasterBunny>> EASTER_BUNNY =
            ENTITY_TYPES.register("easter_bunny", () -> EntityType.Builder.of(EasterBunny::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.6f).clientTrackingRange(8).build("easter_bunny"));

    public static final DeferredHolder<EntityType<?>, EntityType<Flounder>> FLOUNDER =
            ENTITY_TYPES.register("flounder", () -> EntityType.Builder.of(Flounder::new, MobCategory.CREATURE)
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

    public static final DeferredHolder<EntityType<?>, EntityType<Island>> ISLAND =
            ENTITY_TYPES.register("island", () -> EntityType.Builder.of(Island::new, MobCategory.CREATURE)
                    .sized(3.0f, 3.0f).clientTrackingRange(10).build("island"));

    public static final DeferredHolder<EntityType<?>, EntityType<IslandToo>> ISLAND_TOO =
            ENTITY_TYPES.register("island_too", () -> EntityType.Builder.of(IslandToo::new, MobCategory.CREATURE)
                    .sized(3.0f, 3.0f).clientTrackingRange(10).build("island_too"));

    public static final DeferredHolder<EntityType<?>, EntityType<Peacock>> PEACOCK =
            ENTITY_TYPES.register("peacock", () -> EntityType.Builder.of(Peacock::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.0f).clientTrackingRange(10).build("peacock"));

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

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTshirt>> ENTITY_TSHIRT =
            ENTITY_TYPES.register("tshirt", () -> EntityType.Builder.of(EntityTshirt::new, MobCategory.CREATURE)
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
                    .sized(2.0f, 3.5f).clientTrackingRange(10).build("camarasaurus"));

    public static final DeferredHolder<EntityType<?>, EntityType<Dragon>> DRAGON =
            ENTITY_TYPES.register("dragon", () -> EntityType.Builder.of(Dragon::new, MobCategory.CREATURE)
                    .sized(1.5f, 2.0f).clientTrackingRange(10).build("dragon"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCannonFodder>> ENTITY_CANNON_FODDER =
            ENTITY_TYPES.register("cannon_fodder", () -> EntityType.Builder.of(EntityCannonFodder::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f).clientTrackingRange(10).build("cannon_fodder"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGammaMetroid>> ENTITY_GAMMA_METROID =
            ENTITY_TYPES.register("gamma_metroid", () -> EntityType.Builder.of(EntityGammaMetroid::new, MobCategory.CREATURE)
                    .sized(0.8f, 0.8f).clientTrackingRange(10).build("gamma_metroid"));

    public static final DeferredHolder<EntityType<?>, EntityType<Girlfriend>> GIRLFRIEND =
            ENTITY_TYPES.register("girlfriend", () -> EntityType.Builder.of(Girlfriend::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("girlfriend"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityHydrolisc>> ENTITY_HYDROLISC =
            ENTITY_TYPES.register("hydrolisc", () -> EntityType.Builder.of(EntityHydrolisc::new, MobCategory.CREATURE)
                    .sized(0.5f, 0.5f).clientTrackingRange(10).build("hydrolisc"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityLeon>> ENTITY_LEON =
            ENTITY_TYPES.register("leon", () -> EntityType.Builder.of(EntityLeon::new, MobCategory.CREATURE)
                    .sized(2.0f, 3.0f).clientTrackingRange(10).build("leon"));

    public static final DeferredHolder<EntityType<?>, EntityType<Lizard>> LIZARD =
            ENTITY_TYPES.register("lizard", () -> EntityType.Builder.of(Lizard::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.6f).clientTrackingRange(10).build("lizard"));

    public static final DeferredHolder<EntityType<?>, EntityType<Ostrich>> OSTRICH =
            ENTITY_TYPES.register("ostrich", () -> EntityType.Builder.of(Ostrich::new, MobCategory.CREATURE)
                    .sized(0.7f, 1.5f).clientTrackingRange(10).build("ostrich"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRubberDucky>> ENTITY_RUBBER_DUCKY =
            ENTITY_TYPES.register("rubber_ducky", () -> EntityType.Builder.of(EntityRubberDucky::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f).clientTrackingRange(8).build("rubber_ducky"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySpyro>> ENTITY_SPYRO =
            ENTITY_TYPES.register("spyro", () -> EntityType.Builder.of(EntitySpyro::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.8f).clientTrackingRange(10).build("spyro"));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityStinky>> ENTITY_STINKY =
            ENTITY_TYPES.register("stinky", () -> EntityType.Builder.of(EntityStinky::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.8f).clientTrackingRange(10).build("stinky"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrince>> THE_PRINCE =
            ENTITY_TYPES.register("the_prince", () -> EntityType.Builder.of(ThePrince::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.5f).clientTrackingRange(10).build("the_prince"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrinceAdult>> THE_PRINCE_ADULT =
            ENTITY_TYPES.register("the_prince_adult", () -> EntityType.Builder.of(ThePrinceAdult::new, MobCategory.CREATURE)
                    .sized(1.0f, 2.5f).clientTrackingRange(10).build("the_prince_adult"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrincess>> THE_PRINCESS =
            ENTITY_TYPES.register("the_princess", () -> EntityType.Builder.of(ThePrincess::new, MobCategory.CREATURE)
                    .sized(0.5f, 1.2f).clientTrackingRange(10).build("the_princess"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThePrinceTeen>> THE_PRINCE_TEEN =
            ENTITY_TYPES.register("the_prince_teen", () -> EntityType.Builder.of(ThePrinceTeen::new, MobCategory.CREATURE)
                    .sized(0.8f, 1.8f).clientTrackingRange(10).build("the_prince_teen"));

    public static final DeferredHolder<EntityType<?>, EntityType<VelocityRaptor>> VELOCITY_RAPTOR =
            ENTITY_TYPES.register("velocity_raptor", () -> EntityType.Builder.of(VelocityRaptor::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.2f).clientTrackingRange(10).build("velocity_raptor"));

    public static final DeferredHolder<EntityType<?>, EntityType<WaterDragon>> WATER_DRAGON =
            ENTITY_TYPES.register("water_dragon", () -> EntityType.Builder.of(WaterDragon::new, MobCategory.CREATURE)
                    .sized(1.5f, 2.0f).clientTrackingRange(10).build("water_dragon"));

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
                    .sized(0.3f, 0.5f).clientTrackingRange(8).build("fairy"));

    public static final DeferredHolder<EntityType<?>, EntityType<Firefly>> FIREFLY =
            ENTITY_TYPES.register("firefly", () -> EntityType.Builder.of(Firefly::new, MobCategory.AMBIENT)
                    .sized(0.2f, 0.2f).clientTrackingRange(8).build("firefly"));

    public static final DeferredHolder<EntityType<?>, EntityType<Ghost>> GHOST =
            ENTITY_TYPES.register("ghost", () -> EntityType.Builder.of(Ghost::new, MobCategory.AMBIENT)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("ghost"));

    public static final DeferredHolder<EntityType<?>, EntityType<GhostSkelly>> GHOST_SKELLY =
            ENTITY_TYPES.register("ghost_skelly", () -> EntityType.Builder.of(GhostSkelly::new, MobCategory.AMBIENT)
                    .sized(0.6f, 1.8f).clientTrackingRange(10).build("ghost_skelly"));

    public static final DeferredHolder<EntityType<?>, EntityType<Mothra>> MOTHRA =
            ENTITY_TYPES.register("mothra", () -> EntityType.Builder.of(Mothra::new, MobCategory.AMBIENT)
                    .sized(2.0f, 1.5f).clientTrackingRange(10).build("mothra"));

    // ==================== MISC (Mob) ====================

    public static final DeferredHolder<EntityType<?>, EntityType<AntRobot>> ANT_ROBOT =
            ENTITY_TYPES.register("ant_robot", () -> EntityType.Builder.of(AntRobot::new, MobCategory.MISC)
                    .sized(2.0f, 3.0f).clientTrackingRange(10).build("ant_robot"));

    public static final DeferredHolder<EntityType<?>, EntityType<Elevator>> ELEVATOR =
            ENTITY_TYPES.register("elevator", () -> EntityType.Builder.of(Elevator::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f).clientTrackingRange(10).build("elevator"));

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

    public static final DeferredHolder<EntityType<?>, EntityType<EnchantedCow>> ENCHANTED_COW =
            ENTITY_TYPES.register("enchanted_cow", () -> EntityType.Builder.of(EnchantedCow::new, MobCategory.CREATURE)
                    .sized(0.9f, 1.4f).clientTrackingRange(10).build("enchanted_cow"));

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

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
