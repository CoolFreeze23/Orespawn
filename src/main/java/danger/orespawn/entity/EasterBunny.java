package danger.orespawn.entity;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import danger.orespawn.OreSpawnMod;

public class EasterBunny extends Animal {
    // OPT-011: cached SoundEvents — identical createVariableRangeEvent ids,
    // allocated once per class instead of on every sound query.
    private static final SoundEvent SND_DUCK_HURT = SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(OreSpawnMod.MOD_ID, "duck_hurt"));
    private static final float MOVE_SPEED = 0.45f;
    private static final int MAX_HEALTH = 10;

    public EasterBunny(EntityType<? extends EasterBunny> type, Level level) {
        super(type, level);
        // OPT-009: constant speed - assert the attribute base once here instead
        // of re-writing it every tick (same value the removed per-tick call set).
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(MOVE_SPEED);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.0, 1.4));
        this.goalSelector.addGoal(4, new PanicGoal(this, 1.5));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.ATTACK_DAMAGE, 8.0);
    }

    @Override
    public boolean removeWhenFarAway(double dist) {
        return !this.isPersistenceRequired();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SND_DUCK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SND_DUCK_HURT;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** orig EasterBunny.java:609-611 - breeding item is the Crystal Apple (no taming; the audit's "carrot taming" claim was wrong). */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /**
     * orig EasterBunny.java:120-128 - 1-in-200 revenge clear, then a 1-in-600
     * roll lays a stack of 1-3 random spawn eggs from the 115-slot table
     * (:130-587). Criminal maps to the port's band_p (see the C7 village-roster
     * audit correction).
     */
    @Override
    protected void customServerAiStep() {
        if (this.random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
        if (this.random.nextInt(600) == 1) {
            layAnEgg(1 + this.random.nextInt(3));
        }
    }

    /** orig EasterBunny.java:130-587 - one nextInt(115) roll picks the egg type; the whole 1-3 count drops as a single stack at plus/minus 0-1 x/z, y+1. */
    private void layAnEgg(int count) {
        net.minecraft.world.item.Item egg = switch (this.random.nextInt(115)) {
            case 5 -> ModItems.GIRLFRIEND_SPAWN_EGG.get(); // orig GirlfriendEgg
            case 6 -> ModItems.RED_COW_SPAWN_EGG.get(); // orig RedCowEgg
            case 7 -> ModItems.GOLD_COW_SPAWN_EGG.get(); // orig GoldCowEgg
            case 8 -> ModItems.ENCHANTED_APPLE_COW_SPAWN_EGG.get(); // orig EnchantedCowEgg
            case 9 -> ModItems.MOTHRA_SPAWN_EGG.get(); // orig MOTHRAEgg
            case 10 -> ModItems.ALOSAURUS_SPAWN_EGG.get(); // orig AloEgg
            case 11 -> ModItems.CRYOLOPHOSAURUS_SPAWN_EGG.get(); // orig CryoEgg
            case 12 -> ModItems.CAMARASAURUS_SPAWN_EGG.get(); // orig CamaEgg
            case 13 -> ModItems.VELOCITY_RAPTOR_SPAWN_EGG.get(); // orig VeloEgg
            case 14 -> ModItems.HYDROLISC_SPAWN_EGG.get(); // orig HydroEgg
            case 15 -> ModItems.BASILISK_SPAWN_EGG.get(); // orig BasilEgg
            case 16 -> ModItems.DRAGONFLY_SPAWN_EGG.get(); // orig DragonflyEgg
            case 17 -> ModItems.EMPEROR_SCORPION_SPAWN_EGG.get(); // orig EmperorScorpionEgg
            case 18 -> ModItems.SCORPION_SPAWN_EGG.get(); // orig ScorpionEgg
            case 19 -> ModItems.CAVE_FISHER_SPAWN_EGG.get(); // orig CaveFisherEgg
            case 20 -> ModItems.SPYRO_SPAWN_EGG.get(); // orig SpyroEgg
            case 21 -> ModItems.BARYONYX_SPAWN_EGG.get(); // orig BaryonyxEgg
            case 22 -> ModItems.GAMMA_METROID_SPAWN_EGG.get(); // orig GammaMetroidEgg
            case 23 -> ModItems.COCKATEIL_SPAWN_EGG.get(); // orig CockateilEgg
            case 24 -> ModItems.KYUUBI_SPAWN_EGG.get(); // orig KyuubiEgg
            case 25 -> ModItems.ALIEN_SPAWN_EGG.get(); // orig AlienEgg
            case 26 -> ModItems.ATTACK_SQUID_SPAWN_EGG.get(); // orig AttackSquidEgg
            case 27 -> ModItems.WATER_DRAGON_SPAWN_EGG.get(); // orig WaterDragonEgg
            case 28 -> ModItems.CEPHADROME_SPAWN_EGG.get(); // orig CephadromeEgg
            case 29 -> ModItems.DRAGON_SPAWN_EGG.get(); // orig DragonEgg
            case 30 -> ModItems.KRAKEN_SPAWN_EGG.get(); // orig KrakenEgg
            case 31 -> ModItems.LIZARD_SPAWN_EGG.get(); // orig LizardEgg
            case 32 -> ModItems.BEE_SPAWN_EGG.get(); // orig BeeEgg
            case 33 -> ModItems.TROOPER_BUG_SPAWN_EGG.get(); // orig TrooperBugEgg
            case 34 -> ModItems.SPIT_BUG_SPAWN_EGG.get(); // orig SpitBugEgg
            case 35 -> ModItems.STINK_BUG_SPAWN_EGG.get(); // orig StinkBugEgg
            case 36 -> ModItems.OSTRICH_SPAWN_EGG.get(); // orig OstrichEgg
            case 37 -> ModItems.GAZELLE_SPAWN_EGG.get(); // orig GazelleEgg
            case 38 -> ModItems.CHIPMUNK_SPAWN_EGG.get(); // orig ChipmunkEgg
            case 39 -> ModItems.CREEPING_HORROR_SPAWN_EGG.get(); // orig CreepingHorrorEgg
            case 40 -> ModItems.TERRIBLE_TERROR_SPAWN_EGG.get(); // orig TerribleTerrorEgg
            case 41 -> ModItems.CLIFF_RACER_SPAWN_EGG.get(); // orig CliffRacerEgg
            case 42 -> ModItems.TRIFFID_SPAWN_EGG.get(); // orig TriffidEgg
            case 43 -> ModItems.PITCH_BLACK_SPAWN_EGG.get(); // orig PitchBlackEgg
            case 44 -> ModItems.LURKING_TERROR_SPAWN_EGG.get(); // orig LurkingTerrorEgg
            case 45 -> ModItems.GODZILLA_SPAWN_EGG.get(); // orig GodzillaEgg
            case 46 -> ModItems.WORM_SMALL_SPAWN_EGG.get(); // orig SmallWormEgg
            case 47 -> ModItems.WORM_MEDIUM_SPAWN_EGG.get(); // orig MediumWormEgg
            case 48 -> ModItems.WORM_LARGE_SPAWN_EGG.get(); // orig LargeWormEgg
            case 49 -> ModItems.CASSOWARY_SPAWN_EGG.get(); // orig CassowaryEgg
            case 50 -> ModItems.CLOUD_SHARK_SPAWN_EGG.get(); // orig CloudSharkEgg
            case 51 -> ModItems.GOLD_FISH_SPAWN_EGG.get(); // orig GoldFishEgg
            case 52 -> ModItems.LEAF_MONSTER_SPAWN_EGG.get(); // orig LeafMonsterEgg
            case 53 -> ModItems.TSHIRT_SPAWN_EGG.get(); // orig TshirtEgg
            case 54 -> ModItems.ENDER_KNIGHT_SPAWN_EGG.get(); // orig EnderKnightEgg
            case 55 -> ModItems.ENDER_REAPER_SPAWN_EGG.get(); // orig EnderReaperEgg
            case 56 -> ModItems.BEAVER_SPAWN_EGG.get(); // orig BeaverEgg
            case 57 -> ModItems.ROTATOR_SPAWN_EGG.get(); // orig RotatorEgg
            case 58 -> ModItems.VORTEX_SPAWN_EGG.get(); // orig VortexEgg
            case 59 -> ModItems.PEACOCK_SPAWN_EGG.get(); // orig PeacockEgg
            case 60 -> ModItems.FAIRY_SPAWN_EGG.get(); // orig FairyEgg
            case 61 -> ModItems.DUNGEON_BEAST_SPAWN_EGG.get(); // orig DungeonBeastEgg
            case 62 -> ModItems.RAT_SPAWN_EGG.get(); // orig RatEgg
            case 63 -> ModItems.FLOUNDER_SPAWN_EGG.get(); // orig FlounderEgg
            case 64 -> ModItems.WHALE_SPAWN_EGG.get(); // orig WhaleEgg
            case 65 -> ModItems.IRUKANDJI_SPAWN_EGG.get(); // orig IrukandjiEgg
            case 66 -> ModItems.SKATE_SPAWN_EGG.get(); // orig SkateEgg
            case 67 -> ModItems.URCHIN_SPAWN_EGG.get(); // orig UrchinEgg
            case 68 -> ModItems.ROBOT_1_SPAWN_EGG.get(); // orig Robot1Egg
            case 69 -> ModItems.ROBOT_2_SPAWN_EGG.get(); // orig Robot2Egg
            case 70 -> ModItems.ROBOT_3_SPAWN_EGG.get(); // orig Robot3Egg
            case 71 -> ModItems.ROBOT_4_SPAWN_EGG.get(); // orig Robot4Egg
            case 72 -> ModItems.GHOST_SPAWN_EGG.get(); // orig GhostEgg
            case 73 -> ModItems.GHOST_SKELLY_SPAWN_EGG.get(); // orig GhostSkellyEgg
            case 74 -> ModItems.ANT_SPAWN_EGG.get(); // orig BrownAntEgg
            case 75 -> ModItems.RED_ANT_SPAWN_EGG.get(); // orig RedAntEgg
            case 76 -> ModItems.RAINBOW_ANT_SPAWN_EGG.get(); // orig RainbowAntEgg
            case 77 -> ModItems.UNSTABLE_ANT_SPAWN_EGG.get(); // orig UnstableAntEgg
            case 78 -> ModItems.TERMITE_SPAWN_EGG.get(); // orig TermiteEgg
            case 79 -> ModItems.BUTTERFLY_SPAWN_EGG.get(); // orig ButterflyEgg
            case 80 -> ModItems.LUNA_MOTH_SPAWN_EGG.get(); // orig MothEgg
            case 81 -> ModItems.MOSQUITO_SPAWN_EGG.get(); // orig MosquitoEgg
            case 82 -> ModItems.FIREFLY_SPAWN_EGG.get(); // orig FireflyEgg
            case 83 -> ModItems.TREX_SPAWN_EGG.get(); // orig TRexEgg
            case 84 -> ModItems.HERCULES_BEETLE_SPAWN_EGG.get(); // orig HerculesEgg
            case 85 -> ModItems.MANTIS_SPAWN_EGG.get(); // orig MantisEgg
            case 86 -> ModItems.STINKY_SPAWN_EGG.get(); // orig StinkyEgg
            case 87 -> ModItems.ROBOT_5_SPAWN_EGG.get(); // orig Robot5Egg
            case 88 -> ModItems.COIN_SPAWN_EGG.get(); // orig CoinEgg
            case 89 -> ModItems.BOYFRIEND_SPAWN_EGG.get(); // orig BoyfriendEgg
            case 90 -> ModItems.THE_KING_SPAWN_EGG.get(); // orig TheKingEgg
            case 91 -> ModItems.THE_PRINCE_SPAWN_EGG.get(); // orig ThePrinceEgg
            case 92 -> ModItems.EASTER_BUNNY_SPAWN_EGG.get(); // orig EasterBunnyEgg
            case 93 -> ModItems.MOLENOID_SPAWN_EGG.get(); // orig MolenoidEgg
            case 94 -> ModItems.SEA_MONSTER_SPAWN_EGG.get(); // orig SeaMonsterEgg
            case 95 -> ModItems.SEA_VIPER_SPAWN_EGG.get(); // orig SeaViperEgg
            case 96 -> ModItems.CATER_KILLER_SPAWN_EGG.get(); // orig CaterKillerEgg
            case 97 -> ModItems.LEON_SPAWN_EGG.get(); // orig LeonEgg
            case 98 -> ModItems.HAMMERHEAD_SPAWN_EGG.get(); // orig HammerheadEgg
            case 99 -> ModItems.RUBBER_DUCKY_SPAWN_EGG.get(); // orig RubberDuckyEgg
            case 100 -> ModItems.CRYSTAL_COW_SPAWN_EGG.get(); // orig CrystalCowEgg
            case 101 -> ModItems.BAND_P_SPAWN_EGG.get(); // orig CriminalEgg
            case 102 -> ModItems.THE_QUEEN_SPAWN_EGG.get(); // orig TheQueenEgg
            case 103 -> ModItems.BRUTALFLY_SPAWN_EGG.get(); // orig BrutalflyEgg
            case 104 -> ModItems.NASTYSAURUS_SPAWN_EGG.get(); // orig NastysaurusEgg
            case 105 -> ModItems.POINTYSAURUS_SPAWN_EGG.get(); // orig PointysaurusEgg
            case 106 -> ModItems.CRICKET_SPAWN_EGG.get(); // orig CricketEgg
            case 107 -> ModItems.THE_PRINCESS_SPAWN_EGG.get(); // orig ThePrincessEgg
            case 108 -> ModItems.FROG_SPAWN_EGG.get(); // orig FrogEgg
            case 109 -> ModItems.JEFFERY_SPAWN_EGG.get(); // orig JefferyEgg
            case 110 -> ModItems.ANT_ROBOT_SPAWN_EGG.get(); // orig AntRobotEgg
            case 111 -> ModItems.SPIDER_ROBOT_SPAWN_EGG.get(); // orig SpiderRobotEgg
            case 112 -> ModItems.SPIDER_DRIVER_SPAWN_EGG.get(); // orig SpiderDriverEgg
            case 113 -> ModItems.CRAB_SPAWN_EGG.get(); // orig CrabEgg
            default -> null; // orig :574-579 - rolls 0-4 and 114 lay nothing
        };
        if (egg == null) return;
        net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(
                this.level(),
                this.getX() + this.random.nextInt(2) - this.random.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + this.random.nextInt(2) - this.random.nextInt(2),
                new ItemStack(egg, count));
        this.level().addFreshEntity(drop);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return new EasterBunny(ModEntities.EASTER_BUNNY.get(), level);
    }

    /** orig EasterBunny.java:67-77 — y>=50; daytime; no other EasterBunny within 32/8/32. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                   net.minecraft.world.entity.MobSpawnType spawnType) {
        // orig OreSpawnMain.java:4570-4571,4681 — spawns only registered on
        // April 20 ("Easter"); the port's biome modifiers are static, so the
        // date gate lives here (closes the ENT-D-011 remainder).
        if (!danger.orespawn.util.SeasonalDates.isEaster()) return false;
        if (this.getY() < 50.0) return false;
        if (!OriginalSpawnGates.isDaytime(level)) return false;
        return !OriginalSpawnGates.anyOtherNearby(this, level, EasterBunny.class, 32.0, 8.0, 32.0);
    }
}
