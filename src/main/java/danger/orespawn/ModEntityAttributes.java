package danger.orespawn;

import danger.orespawn.entity.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = OreSpawnMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Monster (hostile)
        event.put(ModEntities.ALIEN.get(), Alien.createAttributes().build());
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
        event.put(ModEntities.ENTITY_CANNON_FODDER.get(), EntityCannonFodder.createAttributes().build());
        event.put(ModEntities.ENTITY_GAMMA_METROID.get(), EntityGammaMetroid.createAttributes().build());
        event.put(ModEntities.GIRLFRIEND.get(), Girlfriend.createAttributes().build());
        event.put(ModEntities.ENTITY_HYDROLISC.get(), EntityHydrolisc.createAttributes().build());
        event.put(ModEntities.ENTITY_LEON.get(), EntityLeon.createAttributes().build());
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
        event.put(ModEntities.ENCHANTED_COW.get(), EnchantedCow.createAttributes().build());
        event.put(ModEntities.RUBY_BIRD.get(), RubyBird.createAttributes().build());
    }
}
