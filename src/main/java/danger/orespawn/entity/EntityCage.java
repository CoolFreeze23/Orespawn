package danger.orespawn.entity;

import danger.orespawn.ModItems;
import danger.orespawn.item.CagedMobItem;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import danger.orespawn.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityCage extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_CAGE_INDEX =
            SynchedEntityData.defineId(EntityCage.class, EntityDataSerializers.INT);

    private static final int DEFAULT_CAGE_INDEX = 160;
    private static final int HIT_PARTICLE_BURST_COUNT = 4;
    private static final double PARTICLE_OFFSET_Y = 0.25;
    private static final float EXPLODE_SOUND_PITCH = 1.5f;
    private static final float ROTATION_STEP_DEGREES = 30.0f;
    private static final float FULL_ROTATION_DEGREES = 360.0f;

    private int cageIndex = DEFAULT_CAGE_INDEX;
    private float visualRotationDegrees = 0.0f;

    /**
     * Per-species capture spec from the orig whitelist: {@code escapePercent} is the
     * extra escape dice (orig {@code nextInt(10) < N} = N×10%; Kraken used
     * {@code nextInt(100) < 95}), {@code count} the number of caged items dropped.
     */
    private record CaptureSpec(int escapePercent, int count) {}

    public EntityCage(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public EntityCage(Level level, Player thrower, int index) {
        super(ModEntities.ENTITY_CAGE.get(), thrower, level);
        this.cageIndex = index;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_CAGE_INDEX, DEFAULT_CAGE_INDEX);
    }

    public int getCageIndex() { return this.entityData.get(DATA_CAGE_INDEX); }

    /**
     * orig EntityCage.java:179-931 — the species whitelist, first matching branch wins.
     * Returns null for species the original cage could not capture (orig :932-937 —
     * the cage is then spent with no drop).
     */
    @Nullable
    private static CaptureSpec captureSpecFor(Entity t) {
        if (t instanceof SpiderDriver) return new CaptureSpec(0, 1);         // orig :179
        if (t instanceof CaveSpider) return new CaptureSpec(0, 1);           // orig :183
        if (t instanceof Spider) return new CaptureSpec(0, 1);               // orig :182
        if (t instanceof Crab) return new CaptureSpec(0, 1);                 // orig :191
        if (t instanceof Bat) return new CaptureSpec(0, 2);                  // orig :195-197 — ×2
        if (t instanceof Pig) return new CaptureSpec(0, 1);                  // orig :199
        if (t instanceof AttackSquid) return new CaptureSpec(0, 6);          // orig :507-509 — ×6
        if (t instanceof Squid) return new CaptureSpec(0, 1);                // orig :203
        if (t instanceof Chicken) return new CaptureSpec(0, 1);              // orig :207
        if (t instanceof Creeper) return new CaptureSpec(0, 1);              // orig :211
        if (t instanceof AbstractHorse) return new CaptureSpec(0, 1);        // orig :215
        if (t instanceof WitherSkeleton) return new CaptureSpec(0, 1);       // orig :221-222
        if (t instanceof Skeleton) return new CaptureSpec(0, 1);             // orig :219-226
        if (t instanceof Zombie) return new CaptureSpec(0, 1);               // orig :228-235 (incl. pigman)
        if (t instanceof Slime) return new CaptureSpec(0, 1);                // orig :237-244 (incl. magma cube)
        if (t instanceof Ghast) return new CaptureSpec(20, 1);               // orig :246-255
        if (t instanceof EnderMan) return new CaptureSpec(20, 1);            // orig :257-266
        if (t instanceof Silverfish) return new CaptureSpec(0, 2);           // orig :268-270 — ×2
        if (t instanceof Witch) return new CaptureSpec(0, 1);                // orig :272
        if (t instanceof Sheep) return new CaptureSpec(0, 1);                // orig :276
        if (t instanceof Wolf) return new CaptureSpec(0, 1);                 // orig :280
        if (t instanceof Ocelot) return new CaptureSpec(0, 1);               // orig :284
        if (t instanceof Blaze) return new CaptureSpec(0, 1);                // orig :288
        if (t instanceof Girlfriend) return new CaptureSpec(0, 1);           // orig :292 (untamed gate in caller)
        if (t instanceof Boyfriend) return new CaptureSpec(0, 1);            // orig :296 (untamed gate in caller)
        if (t instanceof EnderDragon) return new CaptureSpec(50, 1);         // orig :300-311
        if (t instanceof SnowGolem) return new CaptureSpec(0, 1);            // orig :325
        if (t instanceof IronGolem) return new CaptureSpec(0, 1);            // orig :329
        if (t instanceof WitherBoss) return new CaptureSpec(20, 1);          // orig :333-342
        if (t instanceof CrystalCow) return new CaptureSpec(0, 1);           // orig :344
        if (t instanceof EnchantedAppleCow) return new CaptureSpec(0, 1);    // orig :352 EnchantedCow
        if (t instanceof GoldCow) return new CaptureSpec(0, 1);              // orig :360
        if (t instanceof RedCow) return new CaptureSpec(0, 1);               // orig :368
        if (t instanceof MushroomCow) return new CaptureSpec(0, 1);          // orig :377-379
        if (t instanceof Cow) return new CaptureSpec(0, 1);                  // orig :376-387 (covers port cow splits)
        if (t instanceof Villager) return new CaptureSpec(0, 1);             // orig :389
        if (t instanceof Mothra) return new CaptureSpec(40, 1);              // orig :397-406
        if (t instanceof Alosaurus) return new CaptureSpec(40, 1);           // orig :408-417
        if (t instanceof Cryolophosaurus) return new CaptureSpec(0, 1);      // orig :419
        if (t instanceof Camarasaurus) return new CaptureSpec(0, 1);         // orig :423
        if (t instanceof VelocityRaptor) return new CaptureSpec(0, 1);       // orig :427
        if (t instanceof EntityHydrolisc) return new CaptureSpec(0, 1);      // orig :431
        if (t instanceof Basilisk) return new CaptureSpec(60, 1);            // orig :435-444
        if (t instanceof EntityDragonfly) return new CaptureSpec(0, 2);      // orig :446-448 — ×2
        if (t instanceof EntityEmperorScorpion) return new CaptureSpec(70, 1); // orig :450-459
        if (t instanceof Cephadrome) return new CaptureSpec(70, 1);          // orig :461-470
        if (t instanceof Dragon) return new CaptureSpec(70, 1);              // orig :472-481
        if (t instanceof EntityScorpion) return new CaptureSpec(0, 1);       // orig :483
        if (t instanceof CaveFisher) return new CaptureSpec(0, 1);           // orig :487
        if (t instanceof EntitySpyro) return new CaptureSpec(0, 1);          // orig :491
        if (t instanceof Baryonyx) return new CaptureSpec(0, 1);             // orig :495
        if (t instanceof EntityGammaMetroid) return new CaptureSpec(0, 1);   // orig :499
        if (t instanceof Cockateil) return new CaptureSpec(0, 4);            // orig :503-505 — ×4
        if (t instanceof EntityKyuubi) return new CaptureSpec(30, 1);        // orig :511-520
        if (t instanceof WaterDragon) return new CaptureSpec(60, 1);         // orig :522-531
        if (t instanceof Kraken) return new CaptureSpec(95, 1);              // orig :533-542 — nextInt(100)<95
        if (t instanceof Lizard) return new CaptureSpec(20, 1);              // orig :544-553
        if (t instanceof Alien) return new CaptureSpec(50, 1);               // orig :555-564
        if (t instanceof EntityBee) return new CaptureSpec(30, 1);           // orig :566-575
        if (t instanceof Firefly) return new CaptureSpec(0, 1);              // orig :577
        if (t instanceof Chipmunk) return new CaptureSpec(0, 1);             // orig :581
        if (t instanceof Gazelle) return new CaptureSpec(0, 1);              // orig :585
        if (t instanceof Ostrich) return new CaptureSpec(0, 1);              // orig :589
        if (t instanceof EntityTrooperBug) return new CaptureSpec(60, 1);    // orig :593-602
        if (t instanceof EntitySpitBug) return new CaptureSpec(30, 1);       // orig :604-613
        if (t instanceof EntityStinkBug) return new CaptureSpec(0, 1);       // orig :615
        if (t instanceof CreepingHorror) return new CaptureSpec(0, 1);       // orig :619
        if (t instanceof EntityTerribleTerror) return new CaptureSpec(0, 1); // orig :623
        if (t instanceof EntityCliffRacer) return new CaptureSpec(0, 1);     // orig :627
        if (t instanceof EntityTriffid) return new CaptureSpec(60, 1);       // orig :631-640
        if (t instanceof PitchBlack) return new CaptureSpec(70, 1);          // orig :642-651
        if (t instanceof EntityLurkingTerror) return new CaptureSpec(0, 1);  // orig :653
        if (t instanceof EntityWormLarge) return new CaptureSpec(50, 1);     // orig :681-690
        if (t instanceof EntityWormMedium) return new CaptureSpec(0, 1);     // orig :661
        if (t instanceof EntityWormSmall) return new CaptureSpec(0, 1);      // orig :657
        if (t instanceof Cassowary) return new CaptureSpec(0, 1);            // orig :665
        if (t instanceof CloudShark) return new CaptureSpec(0, 1);           // orig :669
        if (t instanceof GoldFish) return new CaptureSpec(0, 1);             // orig :673
        if (t instanceof EntityLeafMonster) return new CaptureSpec(0, 1);    // orig :677
        if (t instanceof EnderKnight) return new CaptureSpec(30, 1);         // orig :692-701
        if (t instanceof EnderReaper) return new CaptureSpec(20, 1);         // orig :703-712
        if (t instanceof Beaver) return new CaptureSpec(0, 1);               // orig :714
        if (t instanceof Urchin) return new CaptureSpec(0, 1);               // orig :718
        if (t instanceof Flounder) return new CaptureSpec(0, 1);             // orig :722
        if (t instanceof Skate) return new CaptureSpec(0, 1);                // orig :726
        if (t instanceof EntityRotator) return new CaptureSpec(0, 1);        // orig :730
        if (t instanceof Peacock) return new CaptureSpec(0, 1);              // orig :734
        if (t instanceof Fairy) return new CaptureSpec(0, 1);                // orig :738
        if (t instanceof DungeonBeast) return new CaptureSpec(0, 1);         // orig :742
        if (t instanceof EntityVortex) return new CaptureSpec(30, 1);        // orig :746-755
        if (t instanceof EntityRat) return new CaptureSpec(0, 1);            // orig :757
        if (t instanceof Whale) return new CaptureSpec(20, 1);               // orig :761-770
        if (t instanceof Irukandji) return new CaptureSpec(0, 1);            // orig :772
        if (t instanceof EntityStinky) return new CaptureSpec(0, 1);         // orig :776
        if (t instanceof EntityMantis) return new CaptureSpec(30, 1);        // orig :780-789
        if (t instanceof TRex) return new CaptureSpec(40, 1);                // orig :791-800
        if (t instanceof EntityHerculesBeetle) return new CaptureSpec(50, 1);// orig :802-811
        if (t instanceof EasterBunny) return new CaptureSpec(0, 1);          // orig :813
        if (t instanceof EntityCaterKiller) return new CaptureSpec(70, 1);   // orig :817-826
        if (t instanceof EntityMolenoid) return new CaptureSpec(50, 1);      // orig :828-837
        if (t instanceof SeaMonster) return new CaptureSpec(30, 1);          // orig :839-848
        if (t instanceof SeaViper) return new CaptureSpec(40, 1);            // orig :850-859
        if (t instanceof EntityRubberDucky) return new CaptureSpec(0, 1);    // orig :861
        if (t instanceof EntityLeon) return new CaptureSpec(70, 1);          // orig :865-874
        if (t instanceof Hammerhead) return new CaptureSpec(70, 1);          // orig :876-885
        if (t instanceof BandP) return new CaptureSpec(0, 1);                // orig :887
        if (t instanceof EntityCricket) return new CaptureSpec(0, 1);        // orig :891
        if (t instanceof Frog) return new CaptureSpec(0, 1);                 // orig :895
        if (t instanceof EntityBrutalfly) return new CaptureSpec(50, 1);     // orig :899-908
        if (t instanceof Nastysaurus) return new CaptureSpec(70, 1);         // orig :910-919
        if (t instanceof Pointysaurus) return new CaptureSpec(20, 1);        // orig :921-930
        return null;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        Entity target = result.getEntity();

        // orig EntityCage.java:160,935-937 — 20% base failure on an entity hit: the
        // whole capture block is skipped and the cage is simply lost (no empty cage —
        // the :932 CageEmpty else-branch only fires when NO entity was hit)
        if (this.random.nextInt(10) < 2) {
            this.discard();
            return;
        }

        this.spawnHitFx(target);

        // orig :172-178 — players always escape; the cage returns empty
        if (target instanceof Player) {
            this.dropEmptyCageAndDiscard();
            return;
        }

        // orig :292-299 — tamed Girlfriends/Boyfriends fall through the whole whitelist
        // (their branches require untamed), eating the cage with no drop
        if ((target instanceof Girlfriend gf && gf.isTame())
                || (target instanceof Boyfriend bf && bf.isTame())) {
            this.discard();
            return;
        }

        // orig :312-324 — hitting an Ender Dragon body part captures the parent dragon
        Entity captureTarget = target instanceof EnderDragonPart part ? part.parentMob : target;
        if (!(captureTarget instanceof Mob mob)) {
            this.discard(); // not a capturable living — cage spent (orig fall-through :935-937)
            return;
        }

        CaptureSpec spec = captureSpecFor(mob);
        if (spec == null) {
            // orig :932-937 — species not on the whitelist: mob stays, cage is lost, nothing drops
            this.discard();
            return;
        }
        if (spec.escapePercent() > 0 && this.random.nextInt(100) < spec.escapePercent()) {
            // per-species escape dice (e.g. Ghast orig :247, EnderDragon :301, Kraken :534)
            this.dropEmptyCageAndDiscard();
            return;
        }

        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        CompoundTag mobData = new CompoundTag();
        mob.saveWithoutId(mobData);
        mob.discard();
        // Multi-count species (Bat ×2, Silverfish ×2, Dragonfly ×2, Cockateil ×4,
        // AttackSquid ×6) drop several caged copies — the orig items each spawned a
        // fresh mob, which the cloned NBT reproduces.
        for (int i = 0; i < spec.count(); ++i) {
            ItemStack cageItem = CagedMobItem.createForEntity(entityId, mobData.copy());
            if (mob.hasCustomName()) {
                cageItem.set(DataComponents.CUSTOM_NAME, mob.getCustomName());
            }
            Block.popResource(this.level(), this.blockPosition(), cageItem);
        }
        this.discard();
    }

    private void spawnHitFx(Entity target) {
        for (int i = 0; i < HIT_PARTICLE_BURST_COUNT; ++i) {
            this.level().addParticle(ParticleTypes.SMOKE,
                    target.getX(), target.getY() + PARTICLE_OFFSET_Y, target.getZ(), 0, 0, 0);
            this.level().addParticle(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + PARTICLE_OFFSET_Y, target.getZ(), 0, 0, 0);
        }
        if (this.getOwner() != null) {
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, EXPLODE_SOUND_PITCH);
        }
    }

    private void dropEmptyCageAndDiscard() {
        Block.popResource(this.level(), this.blockPosition(), new ItemStack(ModItems.CAGE_EMPTY.get()));
        this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        // Block impact (entity hits all discard above) → empty cage back (orig :932-934)
        if (!this.level().isClientSide && !this.isRemoved()) {
            this.dropEmptyCageAndDiscard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.visualRotationDegrees += ROTATION_STEP_DEGREES;
        if (this.visualRotationDegrees > FULL_ROTATION_DEGREES) this.visualRotationDegrees -= FULL_ROTATION_DEGREES;
        this.setXRot(this.visualRotationDegrees);

        if (this.level().isClientSide) {
            this.entityData.set(DATA_CAGE_INDEX, this.cageIndex);
        }
    }
}
