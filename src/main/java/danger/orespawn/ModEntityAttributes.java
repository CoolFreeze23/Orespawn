package danger.orespawn;

import danger.orespawn.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Monster (hostile)
        event.put(ModEntities.ALIEN.get(), Alien.createAttributes().build());
        event.put(ModEntities.ALIEN_BOSS.get(), AlienBoss.createAttributes().build());
        event.put(ModEntities.ALOSAURUS.get(), Alosaurus.createAttributes().build());
        event.put(ModEntities.ATTACK_SQUID.get(), AttackSquid.createAttributes().build());
        event.put(ModEntities.BAND_P.get(), BandP.createAttributes().build());
        event.put(ModEntities.BASILISK.get(), Basilisk.createAttributes().build());
        event.put(ModEntities.CAVE_FISHER.get(), CaveFisher.createAttributes().build());
        event.put(ModEntities.CLOUD_SHARK.get(), CloudShark.createAttributes().build());
        event.put(ModEntities.CRAB.get(), Crab.createAttributes().build());
        event.put(ModEntities.CREEPING_HORROR.get(), CreepingHorror.createAttributes().build());
        event.put(ModEntities.CRYOLOPHOSAURUS.get(), Cryolophosaurus.createAttributes().build());
        event.put(ModEntities.DUNGEON_BEAST.get(), DungeonBeast.createAttributes().build());
        event.put(ModEntities.ENDER_KNIGHT.get(), EnderKnight.createAttributes().build());
        event.put(ModEntities.ENDER_REAPER.get(), EnderReaper.createAttributes().build());
        event.put(ModEntities.GIANT_ROBOT.get(), GiantRobot.createAttributes().build());
        event.put(ModEntities.JEFFERY.get(), Jeffery.createAttributes().build());
        event.put(ModEntities.HAMMERHEAD.get(), Hammerhead.createAttributes().build());
        event.put(ModEntities.IRUKANDJI.get(), Irukandji.createAttributes().build());
        event.put(ModEntities.NASTYSAURUS.get(), Nastysaurus.createAttributes().build());
        event.put(ModEntities.PITCH_BLACK.get(), PitchBlack.createAttributes().build());
        event.put(ModEntities.POINTYSAURUS.get(), Pointysaurus.createAttributes().build());
        event.put(ModEntities.ROBOT_1.get(), Robot1.createAttributes().build());
        event.put(ModEntities.ROBOT_2.get(), Robot2.createAttributes().build());
        event.put(ModEntities.ROBOT_3.get(), Robot3.createAttributes().build());
        event.put(ModEntities.ROBOT_4.get(), Robot4.createAttributes().build());
        event.put(ModEntities.ROBOT_5.get(), Robot5.createAttributes().build());
        event.put(ModEntities.SEA_MONSTER.get(), SeaMonster.createAttributes().build());
        event.put(ModEntities.SEA_VIPER.get(), SeaViper.createAttributes().build());
        event.put(ModEntities.SKATE.get(), Skate.createAttributes().build());
        event.put(ModEntities.TREX.get(), TRex.createAttributes().build());
        event.put(ModEntities.URCHIN.get(), Urchin.createAttributes().build());
        event.put(ModEntities.GODZILLA.get(), Godzilla.createAttributes().build());
        event.put(ModEntities.KRAKEN.get(), Kraken.createAttributes().build());
        event.put(ModEntities.THE_KING.get(), TheKing.createAttributes().build());
        event.put(ModEntities.THE_QUEEN.get(), TheQueen.createAttributes().build());
        event.put(ModEntities.ENTITY_BEE.get(), EntityBee.createAttributes().build());
        event.put(ModEntities.ENTITY_BRUTALFLY.get(), EntityBrutalfly.createAttributes().build());
        event.put(ModEntities.ENTITY_CATER_KILLER.get(), EntityCaterKiller.createAttributes().build());
        event.put(ModEntities.ENTITY_EMPEROR_SCORPION.get(), EntityEmperorScorpion.createAttributes().build());
        event.put(ModEntities.ENTITY_HERCULES_BEETLE.get(), EntityHerculesBeetle.createAttributes().build());
        event.put(ModEntities.ENTITY_KYUUBI.get(), EntityKyuubi.createAttributes().build());
        event.put(ModEntities.ENTITY_LEAF_MONSTER.get(), EntityLeafMonster.createAttributes().build());
        event.put(ModEntities.ENTITY_LURKING_TERROR.get(), EntityLurkingTerror.createAttributes().build());
        event.put(ModEntities.ENTITY_MANTIS.get(), EntityMantis.createAttributes().build());
        event.put(ModEntities.ENTITY_MOLENOID.get(), EntityMolenoid.createAttributes().build());
        event.put(ModEntities.ENTITY_RAT.get(), EntityRat.createAttributes().build());
        event.put(ModEntities.ENTITY_ROTATOR.get(), EntityRotator.createAttributes().build());
        event.put(ModEntities.ENTITY_SCORPION.get(), EntityScorpion.createAttributes().build());
        event.put(ModEntities.ENTITY_SPIT_BUG.get(), EntitySpitBug.createAttributes().build());
        event.put(ModEntities.ENTITY_TERRIBLE_TERROR.get(), EntityTerribleTerror.createAttributes().build());
        event.put(ModEntities.ENTITY_TRIFFID.get(), EntityTriffid.createAttributes().build());
        event.put(ModEntities.ENTITY_TROOPER_BUG.get(), EntityTrooperBug.createAttributes().build());
        event.put(ModEntities.ENTITY_VORTEX.get(), EntityVortex.createAttributes().build());
        event.put(ModEntities.ENTITY_WORM_SMALL.get(), EntityWormSmall.createAttributes().build());
        event.put(ModEntities.ENTITY_WORM_MEDIUM.get(), EntityWormMedium.createAttributes().build());
        event.put(ModEntities.ENTITY_WORM_LARGE.get(), EntityWormLarge.createAttributes().build());

        // Creature (passive)
        event.put(ModEntities.BARYONYX.get(), Baryonyx.createAttributes().build());
        event.put(ModEntities.BEAVER.get(), Beaver.createAttributes().build());
        event.put(ModEntities.CASSOWARY.get(), Cassowary.createAttributes().build());
        event.put(ModEntities.CHIPMUNK.get(), Chipmunk.createAttributes().build());
        event.put(ModEntities.COCKATEIL.get(), Cockateil.createAttributes().build());
        event.put(ModEntities.COIN.get(), Coin.createAttributes().build());
        event.put(ModEntities.EASTER_BUNNY.get(), EasterBunny.createAttributes().build());
        event.put(ModEntities.FLOUNDER.get(), Flounder.createAttributes().build());
        event.put(ModEntities.FROG.get(), Frog.createAttributes().build());
        event.put(ModEntities.GAZELLE.get(), Gazelle.createAttributes().build());
        event.put(ModEntities.GOLD_FISH.get(), GoldFish.createAttributes().build());
        event.put(ModEntities.ISLAND.get(), Island.createAttributes().build());
        event.put(ModEntities.ISLAND_TOO.get(), IslandToo.createAttributes().build());
        event.put(ModEntities.PEACOCK.get(), Peacock.createAttributes().build());
        event.put(ModEntities.WHALE.get(), Whale.createAttributes().build());
        event.put(ModEntities.ENTITY_ANT.get(), EntityAnt.createAttributes().build());
        event.put(ModEntities.ENTITY_CLIFF_RACER.get(), EntityCliffRacer.createAttributes().build());
        event.put(ModEntities.ENTITY_CRICKET.get(), EntityCricket.createAttributes().build());
        event.put(ModEntities.ENTITY_DRAGONFLY.get(), EntityDragonfly.createAttributes().build());
        event.put(ModEntities.ENTITY_RED_ANT.get(), EntityRedAnt.createAttributes().build());
        event.put(ModEntities.ENTITY_RAINBOW_ANT.get(), EntityRainbowAnt.createAttributes().build());
        event.put(ModEntities.ENTITY_STINK_BUG.get(), EntityStinkBug.createAttributes().build());
        event.put(ModEntities.ENTITY_TERMITE.get(), EntityTermite.createAttributes().build());
        event.put(ModEntities.ENTITY_TSHIRT.get(), EntityTshirt.createAttributes().build());
        event.put(ModEntities.ENTITY_UNSTABLE_ANT.get(), EntityUnstableAnt.createAttributes().build());

        // Creature (tameable)
        event.put(ModEntities.BOYFRIEND.get(), Boyfriend.createAttributes().build());
        event.put(ModEntities.CAMARASAURUS.get(), Camarasaurus.createAttributes().build());
        event.put(ModEntities.DRAGON.get(), Dragon.createAttributes().build());
        event.put(ModEntities.BABY_DRAGON.get(), BabyDragon.createAttributes().build());
        event.put(ModEntities.ENTITY_CANNON_FODDER.get(), EntityCannonFodder.createAttributes().build());
        event.put(ModEntities.ENTITY_GAMMA_METROID.get(), EntityGammaMetroid.createAttributes().build());
        event.put(ModEntities.GIRLFRIEND.get(), Girlfriend.createAttributes().build());
        event.put(ModEntities.ENTITY_HYDROLISC.get(), EntityHydrolisc.createAttributes().build());
        // TF-030: both ids are the one consolidated EntityLeon (orig Leon.java
        // registered "Leonopteryx", OreSpawnMain.java:4377/4381).
        event.put(ModEntities.ENTITY_LEON.get(), EntityLeon.createAttributes().build());
        event.put(ModEntities.LEONOPTERYX.get(), EntityLeon.createAttributes().build());
        event.put(ModEntities.LIZARD.get(), Lizard.createAttributes().build());
        event.put(ModEntities.OSTRICH.get(), Ostrich.createAttributes().build());
        event.put(ModEntities.ENTITY_RUBBER_DUCKY.get(), EntityRubberDucky.createAttributes().build());
        event.put(ModEntities.ENTITY_SPYRO.get(), EntitySpyro.createAttributes().build());
        event.put(ModEntities.ENTITY_STINKY.get(), EntityStinky.createAttributes().build());
        event.put(ModEntities.THE_PRINCE.get(), ThePrince.createAttributes().build());
        event.put(ModEntities.THE_PRINCE_ADULT.get(), ThePrinceAdult.createAttributes().build());
        event.put(ModEntities.THE_PRINCESS.get(), ThePrincess.createAttributes().build());
        event.put(ModEntities.THE_PRINCE_TEEN.get(), ThePrinceTeen.createAttributes().build());
        event.put(ModEntities.VELOCITY_RAPTOR.get(), VelocityRaptor.createAttributes().build());
        event.put(ModEntities.WATER_DRAGON.get(), WaterDragon.createAttributes().build());

        // Ambient
        event.put(ModEntities.ENTITY_BUTTERFLY.get(), EntityButterfly.createAttributes().build());
        event.put(ModEntities.ENTITY_LUNA_MOTH.get(), EntityLunaMoth.createAttributes().build());
        event.put(ModEntities.ENTITY_MOSQUITO.get(), EntityMosquito.createAttributes().build());
        event.put(ModEntities.FAIRY.get(), Fairy.createAttributes().build());
        event.put(ModEntities.FIREFLY.get(), Firefly.createAttributes().build());
        event.put(ModEntities.GHOST.get(), Ghost.createAttributes().build());
        event.put(ModEntities.GHOST_SKELLY.get(), GhostSkelly.createAttributes().build());
        event.put(ModEntities.MOTHRA.get(), Mothra.createAttributes().build());

        // Misc (mob)
        event.put(ModEntities.ANT_ROBOT.get(), AntRobot.createAttributes().build());
        event.put(ModEntities.ELEVATOR.get(), Elevator.createAttributes().build());
        event.put(ModEntities.KING_HEAD.get(), KingHead.createAttributes().build());
        event.put(ModEntities.QUEEN_HEAD.get(), QueenHead.createAttributes().build());
        event.put(ModEntities.GODZILLA_HEAD.get(), GodzillaHead.createAttributes().build());
        event.put(ModEntities.PURPLE_POWER.get(), PurplePower.createAttributes().build());
        event.put(ModEntities.ROCK_BASE.get(), RockBase.createAttributes().build());
        event.put(ModEntities.SPIDER_ROBOT.get(), SpiderRobot.createAttributes().build());
        event.put(ModEntities.CEPHADROME.get(), Cephadrome.createAttributes().build());
        event.put(ModEntities.RED_COW.get(), RedCow.createAttributes().build());
        event.put(ModEntities.SPIDER_DRIVER.get(), SpiderDriver.createAttributes().build());
        event.put(ModEntities.CRYSTAL_COW.get(), CrystalCow.createAttributes().build());
        event.put(ModEntities.GOLD_COW.get(), GoldCow.createAttributes().build());
        event.put(ModEntities.APPLE_COW.get(), AppleCow.createAttributes().build());
        event.put(ModEntities.GOLDEN_APPLE_COW.get(), GoldenAppleCow.createAttributes().build());
        event.put(ModEntities.ENCHANTED_APPLE_COW.get(), EnchantedAppleCow.createAttributes().build());
        event.put(ModEntities.RUBY_BIRD.get(), RubyBird.createAttributes().build());
        event.put(ModEntities.VAMPIRE_BUTTERFLY.get(), VampireButterfly.createAttributes().build());
    }

    /**
     * The original's cows' rule, for the Red Cow, the Gold Cow and the Enchanted Cow: EntityAnimal's (grass below,
     * light above 8) for the spawns while the world runs; with the chunk, any ground the ON_GROUND placement accepts,
     * because 1.7.10's chunk-generation spawner (SpawnerAnimals.performWorldGenSpawning) asked only for a solid block
     * below and never called getCanSpawnHere. That is how the Enchanted Cow's 15/3/6 herds stood on the mushroom
     * islands' mycelium (orig OreSpawnMain.java:4623), as vanilla's mooshrooms did.
     */
    public static boolean originalCowSpawnRules(EntityType<? extends Animal> type, LevelAccessor level, MobSpawnType spawnType,
                                                BlockPos pos, RandomSource random) {
        return spawnType == MobSpawnType.CHUNK_GENERATION || Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // WGEN-086: every species the port's lists carry, placed by the list the biome carries it in, as the 1.7.10
        // spawner placed it (ListedSpawnPlacement), and judged by its own 1.7.10 rule alone (OriginalSpawnRules):
        // the transcription in its checkSpawnRules where the original overrode getCanSpawnHere; the animal rule
        // where it inherited EntityAnimal's (the cows, the Hydrolisc) or its override returned it (the Girlfriend
        // and the Boyfriend); the Spider Driver's robot bypass, else the spider's darkness. No predicate adds a rule
        // the original lacked.
        listed(event, ModEntities.ALIEN.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ALOSAURUS.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ATTACK_SQUID.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.BAND_P.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.BARYONYX.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.BASILISK.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.BEAVER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.BOYFRIEND.get(), Animal::checkAnimalSpawnRules);
        listed(event, ModEntities.CAMARASAURUS.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CASSOWARY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CAVE_FISHER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CEPHADROME.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CHIPMUNK.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CLOUD_SHARK.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.COCKATEIL.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.COIN.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CRAB.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CREEPING_HORROR.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CRYOLOPHOSAURUS.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.CRYSTAL_COW.get(), ModEntityAttributes::originalCowSpawnRules);
        listed(event, ModEntities.DRAGON.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.DUNGEON_BEAST.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.EASTER_BUNNY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENCHANTED_APPLE_COW.get(), ModEntityAttributes::originalCowSpawnRules);
        listed(event, ModEntities.ENDER_KNIGHT.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENDER_REAPER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_BEE.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_BRUTALFLY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_BUTTERFLY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_CATER_KILLER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_CLIFF_RACER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_CRICKET.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_DRAGONFLY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_EMPEROR_SCORPION.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_GAMMA_METROID.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_HERCULES_BEETLE.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_HYDROLISC.get(), Animal::checkAnimalSpawnRules);
        listed(event, ModEntities.ENTITY_KYUUBI.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_LEAF_MONSTER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_LEON.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_LUNA_MOTH.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_LURKING_TERROR.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_MANTIS.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_MOLENOID.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_MOSQUITO.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_RAT.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_ROTATOR.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_RUBBER_DUCKY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_SCORPION.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_SPIT_BUG.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_SPYRO.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_STINKY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_STINK_BUG.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_TERRIBLE_TERROR.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_TROOPER_BUG.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_TSHIRT.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_VORTEX.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ENTITY_WORM_LARGE.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.FAIRY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.FIREFLY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.FLOUNDER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.FROG.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.GAZELLE.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.GHOST.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.GHOST_SKELLY.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.GIANT_ROBOT.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.GIRLFRIEND.get(), Animal::checkAnimalSpawnRules);
        listed(event, ModEntities.GODZILLA.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.GOLD_COW.get(), ModEntityAttributes::originalCowSpawnRules);
        listed(event, ModEntities.GOLD_FISH.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.HAMMERHEAD.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.IRUKANDJI.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.LIZARD.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.MOTHRA.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.NASTYSAURUS.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.OSTRICH.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.PEACOCK.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.PITCH_BLACK.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.POINTYSAURUS.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.RED_COW.get(), ModEntityAttributes::originalCowSpawnRules);
        listed(event, ModEntities.ROBOT_1.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ROBOT_2.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ROBOT_3.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ROBOT_4.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.ROBOT_5.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.SEA_MONSTER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.SEA_VIPER.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.SKATE.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.SPIDER_DRIVER.get(), SpiderDriver::checkSpiderDriverSpawnRules);
        listed(event, ModEntities.TREX.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.URCHIN.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.VELOCITY_RAPTOR.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.WATER_DRAGON.get(), ModEntityAttributes::originalRuleAlone);
        listed(event, ModEntities.WHALE.get(), ModEntityAttributes::originalRuleAlone);
        // Water mobs
        event.register(ModEntities.KRAKEN.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        event.register(ModEntities.ENTITY_WORM_SMALL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_MEDIUM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_TRIFFID.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase 14 — overworld surface cow variants. Animal spawn rules
        // (grass + sky + brightness > 8) match the existing RedCow gate
        // so they only appear in the same green biomes as the others.
        event.register(ModEntities.APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase 14 — Vampire Butterfly is a flying hostile, gets the
        // NO_RESTRICTIONS placement (matches CloudShark / Mothra) and
        // checkAnyLightMonsterSpawnRules so it can spawn in the
        // permanently dim Chaos dimension regardless of light level.
        event.register(ModEntities.VAMPIRE_BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, VampireButterfly::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // TEST-002 (E6, 2026-08-11) — entities the startup ServerLifecycleHooks error named, no list carries them now:
        // the ground check (ON_GROUND); the per-entity gates live in each entity's checkSpawnRules with orig citations.
        // ThePrincess/ThePrince extend TamableAnimal and RedAnt/Ant bypassed the 1.7.10 animal grass/light default
        // (orig func_70601_bi overrides without super), so they take Mob::checkMobSpawnRules; IslandToo has no orig
        // override at all, so the faithful mapping is the 1.7.10 EntityAnimal default = Animal::checkAnimalSpawnRules.
        event.register(ModEntities.ISLAND_TOO.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_RED_ANT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_ANT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.THE_PRINCESS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.THE_PRINCE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // orig Island.java — no func_70601_bi override; 1.7.10 EntityAnimal default, same mapping as the IslandToo sibling
        event.register(ModEntities.ISLAND.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // leonopteryx — same class + rules as leon (orig Leon.java:452-478); supersedes the old Animal-predicate line in the "Flying mobs" block (removed via sharedEdits)
        event.register(ModEntities.LEONOPTERYX.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    /** A listed species: placed by the list the biome carries it in, its predicate as given. */
    private static <T extends Mob> void listed(RegisterSpawnPlacementsEvent event, EntityType<T> type,
                                               SpawnPlacements.SpawnPredicate<T> predicate) {
        event.register(type, ListedSpawnPlacement.INSTANCE, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate,
                RegisterSpawnPlacementsEvent.Operation.AND);
    }

    /**
     * WGEN-086: the predicate of a species whose original overrode getCanSpawnHere without calling its parent's: it
     * adds nothing, the override's transcription in the entity's checkSpawnRules being the whole rule.
     */
    public static <T extends Mob> boolean originalRuleAlone(EntityType<T> type, ServerLevelAccessor level,
                                                            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return true;
    }
}
