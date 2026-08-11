package danger.orespawn;

import danger.orespawn.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Water mobs
        event.register(ModEntities.KRAKEN.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.SEA_VIPER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.SEA_MONSTER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.IRUKANDJI.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.WHALE.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.FLOUNDER.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.SKATE.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // Phase D1 — natural water spawns restored (ENT-A-021 / ENT-K-085); both
        // were waterCreature spawns in 1.7.10 (orig OreSpawnMain.java:4863-4865, 4873-4874).
        event.register(ModEntities.ATTACK_SQUID.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_RUBBER_DUCKY.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Cave mobs
        event.register(ModEntities.CAVE_FISHER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.DUNGEON_BEAST.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_SCORPION.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_EMPEROR_SCORPION.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Overworld surface hostile mobs
        event.register(ModEntities.ALOSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.NASTYSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.POINTYSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.TREX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CRYOLOPHOSAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.BASILISK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.HAMMERHEAD.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_CATER_KILLER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_TROOPER_BUG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_MANTIS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_SMALL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_MEDIUM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_WORM_LARGE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_LEAF_MONSTER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_TRIFFID.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENDER_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENDER_REAPER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Overworld surface passive/neutral mobs
        event.register(ModEntities.BARYONYX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CAMARASAURUS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.VELOCITY_RAPTOR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CHIPMUNK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GAZELLE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.OSTRICH.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CASSOWARY.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.BEAVER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.EASTER_BUNNY.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.LIZARD.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Crystal dimension passive mobs - use expanded check that also allows CrystalGrass
        event.register(ModEntities.PEACOCK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.COCKATEIL.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.FROG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CRYSTAL_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntityAttributes::checkAnimalOrCrystalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Ambient/flying mobs
        event.register(ModEntities.ENTITY_BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_DRAGONFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.FIREFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_LUNA_MOTH.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_MOSQUITO.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GHOST.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GHOST_SKELLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Flying mobs
        event.register(ModEntities.CLOUD_SHARK.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.MOTHRA.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Cave mobs
        event.register(ModEntities.ENTITY_MOLENOID.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_STINK_BUG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ALIEN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.CREEPING_HORROR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_LURKING_TERROR.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase D1 — ambient overworld spawns restored (ENT-A-085 Cephadrome,
        // ENT-A-099 Coin); both were 1.7.10 ambient ground spawns.
        event.register(ModEntities.CEPHADROME.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.COIN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase 14 — overworld surface cow variants. Animal spawn rules
        // (grass + sky + brightness > 8) match the existing RedCow gate
        // so they only appear in the same green biomes as the others.
        event.register(ModEntities.APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.GOLDEN_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENCHANTED_APPLE_COW.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Phase 14 — Vampire Butterfly is a flying hostile, gets the
        // NO_RESTRICTIONS placement (matches CloudShark / Mothra) and
        // checkAnyLightMonsterSpawnRules so it can spawn in the
        // permanently dim Chaos dimension regardless of light level.
        event.register(ModEntities.VAMPIRE_BUTTERFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, VampireButterfly::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // TEST-002 (E6, 2026-08-11) — the six unregistered-but-spawning
        // entities from the startup ServerLifecycleHooks error, plus the two
        // free-rider siblings sharing identical rules (ant with red_ant via
        // EntityAnt's inherited gate; the_prince with the_princess via
        // companion_royalty.json). Placement layer = the 1.7.10 creature/
        // monster-type solid-ground check (ON_GROUND); the per-entity gates
        // (darkness/y-level/buddy/day-dice) already live in each entity's
        // checkSpawnRules instance override with orig citations. SpitBug is
        // the sole Monster subclass (Monster::checkMonsterSpawnRules per
        // file convention); GammaMetroid/ThePrincess/ThePrince extend
        // TamableAnimal and CliffRacer/RedAnt/Ant bypassed the 1.7.10
        // animal grass/light default (orig func_70601_bi overrides without
        // super), so they take Mob::checkMobSpawnRules; IslandToo has no
        // orig override at all, so the faithful mapping is the 1.7.10
        // EntityAnimal default = Animal::checkAnimalSpawnRules.
        event.register(ModEntities.ENTITY_SPIT_BUG.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_GAMMA_METROID.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ISLAND_TOO.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_CLIFF_RACER.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_RED_ANT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.ENTITY_ANT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.THE_PRINCESS.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(ModEntities.THE_PRINCE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        // Pre-F sweep (2026-08-11) — the remaining unregistered-but-spawning
        // ids from the startup ServerLifecycleHooks error, each with its orig
        // func_70601_bi evidence (see FIX_LOG pre-F entry). Predicate choice
        // follows FUNCTION over convention: daytime-required Monster
        // subclasses (BandP/Bee/Crab/...) take Mob::checkMobSpawnRules because
        // Monster::checkMonsterSpawnRules would dead-gate them; super-calling
        // overrides (Boyfriend/Girlfriend/...) keep the 1.7.10 animal default
        // via Animal::checkAnimalSpawnRules; flyers/water use the
        // NO_RESTRICTIONS / IN_WATER precedents.
        // orig BandP.java:278-309 — daytime mob (no super, monster darkness default dropped): gates live in BandP.checkSpawnRules
        event.register(ModEntities.BAND_P.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Bee.java:253-287 — daytime flyer (no super, monster darkness default dropped): gates in EntityBee.checkSpawnRules; NO_RESTRICTIONS per CloudShark/Mothra flyer precedent
        event.register(ModEntities.ENTITY_BEE.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Boyfriend.java:978-993 — spawner bypass then super.func_70601_bi(): EntityAnimal grass/light default retained, so Animal rules
        event.register(ModEntities.BOYFRIEND.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Girlfriend.java:1100-1115 — spawner bypass then super.func_70601_bi(): EntityAnimal grass/light default retained, so Animal rules
        event.register(ModEntities.GIRLFRIEND.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Brutalfly.java:290-329 — night flyer keeping the monster darkness check (func_70814_o): gates in EntityBrutalfly.checkSpawnRules; NO_RESTRICTIONS per CloudShark precedent
        event.register(ModEntities.ENTITY_BRUTALFLY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Crab.java:456-486 — daytime mob (no super, monster darkness default dropped): gates live in Crab.checkSpawnRules
        event.register(ModEntities.CRAB.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Cricket.java:137-142 — override bypassed the animal grass/light default (no super): gates live in EntityCricket.checkSpawnRules
        event.register(ModEntities.ENTITY_CRICKET.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Fairy.java:334-347 — ambient flyer (EntityAmbientCreature): open-air/y>=50 gates in Fairy.checkSpawnRules; NO_RESTRICTIONS per Firefly/Ghost precedent
        event.register(ModEntities.FAIRY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Dragon.java:598-611 — TamableAnimal whose override bypassed the animal grass/light default (day/buddy/Islands-or-y>=50 gates live in Dragon.checkSpawnRules)
        event.register(ModEntities.DRAGON.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig GiantRobot.java:364-381 — EntityMob keeping the darkness default (y>=50/night/air-or-grass clearance gates live in GiantRobot.checkSpawnRules)
        event.register(ModEntities.GIANT_ROBOT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Godzilla.java:557-591 — EntityMob; orig checked func_70814_o darkness first, supplied here by the monster default (night/y/dice/air-pocket/sibling/one-shot gates live in Godzilla.checkSpawnRules)
        event.register(ModEntities.GODZILLA.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig GoldFish.java:153-155 — EntityAnimal whose override returned true (bypassed the animal default); creature-category ground spawn in all four port biomes, not a water spawn
        event.register(ModEntities.GOLD_FISH.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig HerculesBeetle.java:442-481 — EntityMob keeping the darkness default (spawner-bypass/night/y>=50/air-box/buddy gates live in EntityHerculesBeetle.checkSpawnRules)
        event.register(ModEntities.ENTITY_HERCULES_BEETLE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Hydrolisc.java — no func_70601_bi override; faithful mapping is the 1.7.10 EntityAnimal default (grass + light)
        event.register(ModEntities.ENTITY_HYDROLISC.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Island.java — no func_70601_bi override; 1.7.10 EntityAnimal default, same mapping as the IslandToo sibling
        event.register(ModEntities.ISLAND.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Kyuubi.java:222-224 — func_70601_bi returns unconditional true
        // (no darkness/difficulty gate). STRICT PARITY over file convention:
        // Mob::checkMobSpawnRules, because the Monster predicate would layer a
        // darkness default the 1.7.10 code lacked (suppressing Nether spawns
        // near lava/glowstone and darkening its dungeon-spawner requirement).
        event.register(ModEntities.ENTITY_KYUUBI.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // leon — orig Leon.java:452-478 (flying tameable; func_70601_bi override w/o super bypasses the animal default -> Mob predicate; TF-030: one class, two save-compat ids, both registered identically)
        event.register(ModEntities.ENTITY_LEON.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // leonopteryx — same class + rules as leon (orig Leon.java:452-478); supersedes the old Animal-predicate line in the "Flying mobs" block (removed via sharedEdits)
        event.register(ModEntities.LEONOPTERYX.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // pitch_black — orig PitchBlack.java:429-483 (EntityMob ground monster; per-entity gates live in PitchBlack.checkSpawnRules)
        event.register(ModEntities.PITCH_BLACK.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // rat — orig Rat.java:302-339 (EntityMob ground monster; per-entity gates live in EntityRat.checkSpawnRules)
        event.register(ModEntities.ENTITY_RAT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // robot_1 — orig Robot1.java:226-234 (EntityMob ground monster; y/darkness/night gates live in Robot1.checkSpawnRules)
        event.register(ModEntities.ROBOT_1.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // robot_2 — orig Robot2.java:403-437 (EntityMob ground monster; spawner/y/night/clearance/darkness gates live in Robot2.checkSpawnRules)
        event.register(ModEntities.ROBOT_2.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // robot_3 — orig Robot3.java:343-360 (EntityMob ground monster, no spawner bypass; gates live in Robot3.checkSpawnRules)
        event.register(ModEntities.ROBOT_3.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // robot_4 — orig Robot4.java:415-449 (EntityMob ground monster; spawner/y/night/clearance/darkness gates live in Robot4.checkSpawnRules)
        event.register(ModEntities.ROBOT_4.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // robot_5 — orig Robot5.java:317-351 (EntityMob ground monster; spawner/y/night/short-clearance/darkness gates live in Robot5.checkSpawnRules)
        event.register(ModEntities.ROBOT_5.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Rotator.java:255-288 — spawner-bypass/darkness/air-box/night gates live in EntityRotator#checkSpawnRules; flying Monster => CloudShark precedent
        event.register(ModEntities.ENTITY_ROTATOR.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Spyro.java:407-412 — daytime + y>=50 gates live in EntitySpyro#checkSpawnRules; flying TamableAnimal whose orig override bypassed the animal default => Mothra precedent
        event.register(ModEntities.ENTITY_SPYRO.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Stinky.java:286-291 — daytime + buddy-cap (findBuddies :705-708) gates live in EntityStinky#checkSpawnRules; flying TamableAnimal whose orig override bypassed the animal default => Mothra precedent
        event.register(ModEntities.ENTITY_STINKY.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig TerribleTerror.java:193-214 — spawner-bypass/darkness/night/(Chaos or y<=40) gates live in EntityTerribleTerror#checkSpawnRules; flying Monster => CloudShark precedent
        event.register(ModEntities.ENTITY_TERRIBLE_TERROR.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Urchin.java:298-332 — spawner-bypass/headroom/darkness/late-night gates live in Urchin#checkSpawnRules; walking ground Monster
        event.register(ModEntities.URCHIN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig Vortex.java:240-284 — spawner-bypass/air-box/darkness/y>=50/night/dice/solo gates live in EntityVortex#checkSpawnRules; flying Monster => CloudShark precedent
        event.register(ModEntities.ENTITY_VORTEX.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING, Monster::checkMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        // orig OreSpawnMain.java:4844-4847 — waterCreature spawn lists; y>=50/daytime/solo gates live in WaterDragon#checkSpawnRules (orig WaterDragon.java:716-739)
        event.register(ModEntities.WATER_DRAGON.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    /**
     * Like Animal::checkAnimalSpawnRules but also allows spawning on CrystalGrass.
     * Used for passive mobs that appear in both the Overworld and Crystal dimension.
     */
    private static boolean checkAnimalOrCrystalSpawnRules(
            EntityType<? extends Animal> type, LevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean validGround = below.is(Blocks.GRASS_BLOCK)
                || below.is(ModBlocks.CRYSTAL_GRASS.get());
        return validGround && level.getRawBrightness(pos, 0) > 8;
    }
}
