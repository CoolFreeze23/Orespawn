### Beaver  (original rule: Beaver.java:273-282)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.getY() > 100.0) return false;
        BlockState below = level.getBlockState(this.blockPosition().below());
        return below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SHORT_GRASS) || below.is(Blocks.OAK_LEAVES);
    }
ORIG:
  273|     public boolean func_70601_bi() {
  274|         if (this.field_70163_u < 50.0) {
  275|             return false;
  276|         }
  277|         if (this.field_70163_u > 100.0) {
  278|             return false;
  279|         }
  280|         Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v);
  281|         return bid == Blocks.field_150346_d || bid == Blocks.field_150349_c || bid == Blocks.field_150329_H || bid == Blocks.field_150362_t;
  282|     }


### Chipmunk  (original rule: Chipmunk.java:248-253)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(Chipmunk.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= 2;
    }
ORIG:
  248|     public boolean func_70601_bi() {
  249|         if (this.field_70163_u < 50.0) {
  250|             return false;
  251|         }
  252|         return this.findBuddies() <= 2;
  253|     }


### CloudShark  (original rule: CloudShark.java:198-200)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return true;
    }
ORIG:
  198|     public boolean func_70601_bi() {
  199|         return true;
  200|     }


### CreepingHorror  (original rule: CreepingHorror.java:220-228)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        // orig CreepingHorror.java:220-228 ΓÇö only spawns in darkness, at
        // night, and either in the Chaos dimension (DimensionID6) or at y<=15.
        if (level instanceof ServerLevelAccessor server) {
            if (!Monster.isDarkEnoughToSpawn(server, this.blockPosition(), this.getRandom())) {
                return false;
            }
            if (server.getLevel().isDay()) return false;
            return server.getLevel().dimension() == CHAOS_DIM || this.getY() <= 15.0;
        }
        return super.checkSpawnRules(level, spawnType);
    }
ORIG:
  220|     public boolean func_70601_bi() {
  221|         if (!this.func_70814_o()) {
  222|             return false;
  223|         }
  224|         if (this.field_70170_p.func_72935_r()) {
  225|             return false;
  226|         }
  227|         return this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 || !(this.field_70163_u > 15.0);
  228|     }


### EntityAnt  (original rule: EntityAnt.java:140-145)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(EntityAnt.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= 4;
    }
ORIG:
  140|     public boolean func_70601_bi() {
  141|         if (this.field_70163_u < 50.0) {
  142|             return false;
  143|         }
  144|         return this.findBuddies() <= 4;
  145|     }


### EntityButterfly  (original rule: EntityButterfly.java:283-310)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        BlockState state = level.getBlockState(this.blockPosition());
        if (!state.isAir()) return false;
        if (!level.canSeeSky(this.blockPosition())) return false;
        return this.getY() >= 50.0;
    }
ORIG:
  283|     public boolean func_70601_bi() {
  284|         Block bid;
  285|         for (int k = -3; k < 3; ++k) {
  286|             for (int j = -3; j < 3; ++j) {
  287|                 for (int i = 0; i < 5; ++i) {
  288|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  289|                     if (bid != Blocks.field_150474_ac) continue;
  290|                     TileEntityMobSpawner tileentitymobspawner = null;
  291|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  292|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  293|                     if (s == null || !s.equals("Butterfly")) continue;
  294|                     this.butterfly_type = 1;
  295|                     return true;
  296|                 }
  297|             }
  298|         }
  299|         bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
  300|         if (bid != Blocks.field_150350_a) {
  301|             return false;
  302|         }
  303|         if (!this.field_70170_p.func_72935_r()) {
  304|             return false;
  305|         }
  306|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  307|             return true;
  308|         }
  309|         return !(this.field_70163_u < 50.0);
  310|     }


### EntityCliffRacer  (original rule: CliffRacer.java:145-147)
PORT:
@Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                    net.minecraft.world.entity.MobSpawnType spawnType) {
        return this.getY() >= 50.0;
    }
ORIG:
  145|     public boolean func_70601_bi() {
  146|         return !(this.field_70163_u < 50.0);
  147|     }


### EntityCricket  (original rule: Cricket.java:137-142)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 30.0) return false;
        return level.getEntitiesOfClass(EntityCricket.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= 5;
    }
ORIG:
  137|     public boolean func_70601_bi() {
  138|         if (this.field_70163_u < 30.0) {
  139|             return false;
  140|         }
  141|         return this.findBuddies() <= 5;
  142|     }


### EntityDragonfly  (original rule: Dragonfly.java:187-192)
PORT:
@Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
                                    net.minecraft.world.entity.MobSpawnType spawnType) {
        return this.getY() >= 50.0 && level.dayTime() % 24000L < 13000L;
    }
ORIG:
  187|     public boolean func_70601_bi() {
  188|         if (this.field_70163_u < 50.0) {
  189|             return false;
  190|         }
  191|         return this.field_70170_p.func_72935_r();
  192|     }


### EntityLunaMoth  (original rule: EntityLunaMoth.java:168-180)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        BlockState state = level.getBlockState(this.blockPosition());
        if (!state.isAir()) return false;
        if (level.canSeeSky(this.blockPosition())) return false;
        return this.getY() >= MIN_SPAWN_Y;
    }
ORIG:
  168|     public boolean func_70601_bi() {
  169|         Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
  170|         if (bid != Blocks.field_150350_a) {
  171|             return false;
  172|         }
  173|         if (this.field_70170_p.func_72935_r()) {
  174|             return false;
  175|         }
  176|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  177|             return true;
  178|         }
  179|         return !(this.field_70163_u < 50.0);
  180|     }


### EntityMosquito  (original rule: EntityMosquito.java:145-147)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return true;
    }
ORIG:
  145|     public boolean func_70601_bi() {
  146|         return true;
  147|     }


### EntityStinkBug  (original rule: StinkBug.java:136-151)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return this.getY() >= 50.0;
    }
ORIG:
  136|     public boolean func_70601_bi() {
  137|         for (int k = -3; k < 3; ++k) {
  138|             for (int j = -3; j < 3; ++j) {
  139|                 for (int i = 0; i < 5; ++i) {
  140|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  141|                     if (bid != Blocks.field_150474_ac) continue;
  142|                     TileEntityMobSpawner tileentitymobspawner = null;
  143|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  144|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  145|                     if (s == null || !s.equals("Stink Bug")) continue;
  146|                     return true;
  147|                 }
  148|             }
  149|         }
  150|         return !(this.field_70163_u < 50.0);
  151|     }


### EntityTermite  (original rule: NONE)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < MIN_SPAWN_Y) return false;
        return level.getEntitiesOfClass(EntityTermite.class,
                this.getBoundingBox().inflate(20.0, 10.0, 20.0)).size() <= MAX_NEARBY_CLUSTER;
    }

### Fairy  (original rule: Fairy.java:334-347)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        int airCount = 0;
        for (int k = -1; k <= 1; k++) {
            for (int j = -1; j <= 1; j++) {
                if (level.getBlockState(new BlockPos((int) this.getX() + j, (int) this.getY(), (int) this.getZ() + k)).isAir())
                    airCount++;
            }
        }
        if (airCount < 6) return false;
        return this.getY() >= 50.0;
    }
ORIG:
  334|     public boolean func_70601_bi() {
  335|         int sc = 0;
  336|         for (int k = -1; k <= 1; ++k) {
  337|             for (int j = -1; j <= 1; ++j) {
  338|                 Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u, (int)this.field_70161_v + k);
  339|                 if (bid != Blocks.field_150350_a) continue;
  340|                 ++sc;
  341|             }
  342|         }
  343|         if (sc < 6) {
  344|             return false;
  345|         }
  346|         return !(this.field_70163_u < 50.0);
  347|     }


### Firefly  (original rule: Firefly.java:161-176)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (!level.getBlockState(this.blockPosition()).isAir()) return false;
        if (level.canSeeSky(this.blockPosition())) return false;
        int buddies = level.getEntitiesOfClass(Firefly.class,
                this.getBoundingBox().inflate(20.0, 8.0, 20.0)).size();
        if (buddies > 10) return false;
        return this.getY() >= 50.0;
    }
ORIG:
  161|     public boolean func_70601_bi() {
  162|         Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u, (int)this.field_70161_v);
  163|         if (bid != Blocks.field_150350_a) {
  164|             return false;
  165|         }
  166|         if (this.field_70170_p.func_72935_r()) {
  167|             return false;
  168|         }
  169|         if (this.findBuddies() > 10) {
  170|             return false;
  171|         }
  172|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  173|             return true;
  174|         }
  175|         return !(this.field_70163_u < 50.0);
  176|     }


### Flounder  (original rule: Flounder.java:219-230)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.random.nextInt(20) != 1) return false;
        return level.getEntitiesOfClass(Flounder.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0)).size() <= 10;
    }
ORIG:
  219|     public boolean func_70601_bi() {
  220|         if (this.field_70163_u < 50.0) {
  221|             return false;
  222|         }
  223|         if (!this.field_70170_p.func_72935_r()) {
  224|             return false;
  225|         }
  226|         if (this.field_70170_p.field_73012_v.nextInt(20) != 1) {
  227|             return false;
  228|         }
  229|         return this.findBuddies() <= 10;
  230|     }


### Frog  (original rule: Frog.java:240-251)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        long dayTime = level.dayTime() % 24000L;
        if (dayTime >= 13000L) return false;
        return level.getEntitiesOfClass(Frog.class,
                this.getBoundingBox().inflate(20.0, 8.0, 20.0)).size() <= 5;
    }
ORIG:
  240|     public boolean func_70601_bi() {
  241|         if (this.field_70163_u < 50.0) {
  242|             return false;
  243|         }
  244|         if (!this.field_70170_p.func_72935_r()) {
  245|             return false;
  246|         }
  247|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5 && this.field_70170_p.field_73012_v.nextInt(20) != 1) {
  248|             return false;
  249|         }
  250|         return this.findBuddies() <= 5;
  251|     }


### Gazelle  (original rule: Gazelle.java:368-377)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.getY() > 100.0) return false;
        BlockState below = level.getBlockState(this.blockPosition().below());
        return below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.SHORT_GRASS);
    }
ORIG:
  368|     public boolean func_70601_bi() {
  369|         if (this.field_70163_u < 50.0) {
  370|             return false;
  371|         }
  372|         if (this.field_70163_u > 100.0) {
  373|             return false;
  374|         }
  375|         Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t, (int)this.field_70163_u - 1, (int)this.field_70161_v);
  376|         return bid == Blocks.field_150346_d || bid == Blocks.field_150349_c || bid == Blocks.field_150329_H;
  377|     }


### Ghost  (original rule: Ghost.java:145-160)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return !level.canSeeSky(this.blockPosition());
    }
ORIG:
  145|     public boolean func_70601_bi() {
  146|         for (int k = -2; k < 2; ++k) {
  147|             for (int j = -2; j < 2; ++j) {
  148|                 for (int i = 0; i < 5; ++i) {
  149|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  150|                     if (bid != Blocks.field_150474_ac) continue;
  151|                     TileEntityMobSpawner tileentitymobspawner = null;
  152|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  153|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  154|                     if (s == null || !s.equals("Ghost")) continue;
  155|                     return true;
  156|                 }
  157|             }
  158|         }
  159|         return !this.field_70170_p.func_72935_r();
  160|     }


### GhostSkelly  (original rule: GhostSkelly.java:173-188)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return !level.canSeeSky(this.blockPosition());
    }
ORIG:
  173|     public boolean func_70601_bi() {
  174|         for (int k = -2; k < 2; ++k) {
  175|             for (int j = -2; j < 2; ++j) {
  176|                 for (int i = 0; i < 5; ++i) {
  177|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  178|                     if (bid != Blocks.field_150474_ac) continue;
  179|                     TileEntityMobSpawner tileentitymobspawner = null;
  180|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  181|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  182|                     if (s == null || !s.equals("Ghost Pumpkin Skelly")) continue;
  183|                     return true;
  184|                 }
  185|             }
  186|         }
  187|         return !this.field_70170_p.func_72935_r();
  188|     }


### Godzilla  (original rule: Godzilla.java:557-591)
PORT:
/**
     * 1.7.10 fidelity: ambient Mobzilla spawning required clear sky, night,
     * y >= 50, a roughly 16├âΓÇö10├âΓÇö16 air pocket, a 1/40 dice roll, and the
     * global {@code OreSpawnMain.godzilla_has_spawned} flag being unset.
     * Player-summoned Mobzillas (via the 9 Ancient Dried Egg Parts) bypass
     * this entire chain because the spawn item calls {@code spawn()} with
     * {@link MobSpawnType#SPAWN_EGG}, not {@link MobSpawnType#NATURAL}.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION
                && spawnType != MobSpawnType.SPAWNER) {
            return super.checkSpawnRules(level, spawnType);
        }
        if (this.getY() < 50.0) return false;
        if (!level.canSeeSky(this.blockPosition())) return false;

        ServerLevel serverLevel = (level instanceof ServerLevel sl) ? sl : null;
        if (serverLevel != null && !serverLevel.dimensionType().hasFixedTime() && serverLevel.isDay()) {
            return false;
        }
        if (this.getRandom().nextInt(40) != 1) return false;

        BlockPos minPos = BlockPos.containing(this.getX() - 8.0, this.getY(), this.getZ() - 8.0);
        BlockPos maxPos = BlockPos.containing(this.getX() + 8.0, this.getY() + 10.0, this.getZ() + 8.0);
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (!level.getBlockState(pos).isAir()) return false;
        }

        AABB siblingBox = this.getBoundingBox().inflate(64.0, 16.0, 64.0);
        if (!level.getEntitiesOfClass(Godzilla.class, siblingBox, g -> g != this).isEmpty()) {
            return false;
        }

        if (OreSpawnConfig.MOBZILLA_SINGLE_SPAWN.get() && serverLevel != null) {
            if (MobzillaSpawnTracker.get(serverLevel).hasSpawned()) return false;
        }
        return true;
    }
ORIG:
  557|     public boolean func_70601_bi() {
  558|         if (!this.func_70814_o()) {
  559|             return false;
  560|         }
  561|         if (this.field_70170_p.func_72935_r()) {
  562|             return false;
  563|         }
  564|         if (this.field_70163_u < 50.0) {
  565|             return false;
  566|         }
  567|         if (OreSpawnMain.godzilla_has_spawned != 0) {
  568|             return false;
  569|         }
  570|         if (this.field_70170_p.field_73012_v.nextInt(40) != 1) {
  571|             return false;
  572|         }
  573|         for (int k = -8; k <= 8; ++k) {
  574|             for (int j = -8; j <= 8; ++j) {
  575|                 for (int i = 5; i < 15; ++i) {
  576|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  577|                     if (bid == Blocks.field_150350_a) continue;
  578|                     return false;
  579|                 }
  580|             }
  581|         }
  582|         Godzilla target = null;
  583|         target = (Godzilla)this.field_70170_p.func_72857_a(Godzilla.class, this.field_70121_D.func_72314_b(64.0, 16.0, 64.0), (Entity)this);
  584|         if (target != null) {
  585|             return false;
  586|         }
  587|         if (!this.field_70170_p.field_72995_K) {
  588|             OreSpawnMain.godzilla_has_spawned = 1;
  589|         }
  590|         return true;
  591|     }


### GoldFish  (original rule: GoldFish.java:153-155)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return true;
    }
ORIG:
  153|     public boolean func_70601_bi() {
  154|         return true;
  155|     }


### Hammerhead  (original rule: Hammerhead.java:277-316)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(Hammerhead.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0)).isEmpty();
    }
ORIG:
  277|     public boolean func_70601_bi() {
  278|         Block bid;
  279|         int i;
  280|         int j;
  281|         int k;
  282|         for (k = -3; k < 3; ++k) {
  283|             for (j = -3; j < 3; ++j) {
  284|                 for (i = 0; i < 5; ++i) {
  285|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  286|                     if (bid != Blocks.field_150474_ac) continue;
  287|                     TileEntityMobSpawner tileentitymobspawner = null;
  288|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  289|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  290|                     if (s == null || !s.equals("Hammerhead")) continue;
  291|                     return true;
  292|                 }
  293|             }
  294|         }
  295|         if (!this.func_70814_o()) {
  296|             return false;
  297|         }
  298|         if (this.field_70163_u < 50.0) {
  299|             return false;
  300|         }
  301|         if (this.field_70170_p.func_72935_r()) {
  302|             return false;
  303|         }
  304|         for (k = -1; k < 1; ++k) {
  305|             for (j = -1; j < 1; ++j) {
  306|                 for (i = 1; i < 6; ++i) {
  307|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  308|                     if (bid == Blocks.field_150350_a) continue;
  309|                     return false;
  310|                 }
  311|             }
  312|         }
  313|         Hammerhead target = null;
  314|         target = (Hammerhead)this.field_70170_p.func_72857_a(Hammerhead.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
  315|         return target == null;
  316|     }


### Irukandji  (original rule: Irukandji.java:326-337)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.random.nextInt(60) != 1) return false;
        return level.getEntitiesOfClass(Irukandji.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0)).size() <= 2;
    }
ORIG:
  326|     public boolean func_70601_bi() {
  327|         if (this.field_70163_u < 50.0) {
  328|             return false;
  329|         }
  330|         if (!this.field_70170_p.func_72935_r()) {
  331|             return false;
  332|         }
  333|         if (this.field_70170_p.field_73012_v.nextInt(60) != 1) {
  334|             return false;
  335|         }
  336|         return this.findBuddies() <= 2;
  337|     }


### Lizard  (original rule: Lizard.java:368-370)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return this.getY() >= 50.0;
    }
ORIG:
  368|     public boolean func_70601_bi() {
  369|         return !(this.field_70163_u < 50.0);
  370|     }


### Mothra  (original rule: Mothra.java:295-331)
PORT:
/**
     * 1.7.10 fidelity: Mothra spawned only when a vanilla mob spawner block
     * tagged {@code EntityId="Mothra"} sat within ┬▒2 X/Z and +1..+3 Y of the
     * spawn point ΓÇö even though her {@code addSpawn} entries listed Nether and
     * Mesa biomes. We mirror that gating here, but relax the NBT requirement
     * to "any spawner block" because in 1.21.1 spawner contents are stored as
     * weighted spawn potentials, and we don't ship a Mothra-specific spawner
     * block. The {@code MOTHRA_REQUIRES_SPAWNER} config lets servers disable
     * the gate if they want unconditional biome spawning.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (spawnType == MobSpawnType.SPAWN_EGG || spawnType == MobSpawnType.MOB_SUMMONED
                || spawnType == MobSpawnType.COMMAND || spawnType == MobSpawnType.EVENT) {
            return super.checkSpawnRules(level, spawnType);
        }
        List<Mothra> nearby = level.getEntitiesOfClass(Mothra.class,
                this.getBoundingBox().inflate(64.0, 32.0, 64.0));
        if (!nearby.isEmpty()) return false;

        if (OreSpawnConfig.MOTHRA_REQUIRES_SPAWNER.get()) {
            BlockPos here = this.blockPosition();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 1; dy <= 3; dy++) {
                        if (level.getBlockState(here.offset(dx, dy, dz)).is(Blocks.SPAWNER)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        if (this.getY() < 70.0) return false;
        return level.canSeeSky(this.blockPosition());
    }
ORIG:
  295|     public boolean func_70601_bi() {
  296|         Block bid;
  297|         int i;
  298|         int j;
  299|         int k;
  300|         for (k = -2; k <= 2; ++k) {
  301|             for (j = -2; j <= 2; ++j) {
  302|                 for (i = 1; i < 4; ++i) {
  303|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  304|                     if (bid != Blocks.field_150474_ac) continue;
  305|                     TileEntityMobSpawner tileentitymobspawner = null;
  306|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  307|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  308|                     if (s == null || !s.equals("Mothra")) continue;
  309|                     return true;
  310|                 }
  311|             }
  312|         }
  313|         if (this.field_70163_u < 70.0) {
  314|             return false;
  315|         }
  316|         if (this.field_70170_p.func_72935_r()) {
  317|             return false;
  318|         }
  319|         for (k = -4; k < 4; ++k) {
  320|             for (j = -3; j < 3; ++j) {
  321|                 for (i = 1; i < 10; ++i) {
  322|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  323|                     if (bid == Blocks.field_150350_a) continue;
  324|                     return false;
  325|                 }
  326|             }
  327|         }
  328|         Mothra target = null;
  329|         target = (Mothra)this.field_70170_p.func_72857_a(Mothra.class, this.field_70121_D.func_72314_b(64.0, 32.0, 64.0), (Entity)this);
  330|         return target == null;
  331|     }


### Ostrich  (original rule: Ostrich.java:325-338)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!level.canSeeSky(this.blockPosition())) return false;
        if (this.random.nextInt(4) != 1) return false;
        List<Ostrich> nearby = level.getEntitiesOfClass(Ostrich.class,
                this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        return nearby.isEmpty();
    }
ORIG:
  325|     public boolean func_70601_bi() {
  326|         if (this.field_70163_u < 50.0) {
  327|             return false;
  328|         }
  329|         if (!this.field_70170_p.func_72935_r()) {
  330|             return false;
  331|         }
  332|         if (this.field_70170_p.field_73012_v.nextInt(4) != 1) {
  333|             return false;
  334|         }
  335|         Ostrich target = null;
  336|         target = (Ostrich)this.field_70170_p.func_72857_a(Ostrich.class, this.field_70121_D.func_72314_b(16.0, 6.0, 16.0), (Entity)this);
  337|         return target == null;
  338|     }


### SeaMonster  (original rule: SeaMonster.java:544-570)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(SeaMonster.class,
                this.getBoundingBox().inflate(16.0, 5.0, 16.0)).size() <= 1;
    }
ORIG:
  544|     public boolean func_70601_bi() {
  545|         SeaMonster target = null;
  546|         for (int k = -3; k < 3; ++k) {
  547|             for (int j = -3; j < 3; ++j) {
  548|                 for (int i = 0; i < 5; ++i) {
  549|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  550|                     if (bid != Blocks.field_150474_ac) continue;
  551|                     TileEntityMobSpawner tileentitymobspawner = null;
  552|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  553|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  554|                     if (s == null || !s.equals("Sea Monster")) continue;
  555|                     return true;
  556|                 }
  557|             }
  558|         }
  559|         if (this.field_70163_u < 50.0) {
  560|             return false;
  561|         }
  562|         if (this.field_70170_p.func_72935_r()) {
  563|             return false;
  564|         }
  565|         if (!this.func_70814_o()) {
  566|             return false;
  567|         }
  568|         target = (SeaMonster)this.field_70170_p.func_72857_a(SeaMonster.class, this.field_70121_D.func_72314_b(16.0, 5.0, 16.0), (Entity)this);
  569|         return target == null;
  570|     }


### SeaViper  (original rule: SeaViper.java:561-584)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (!level.getFluidState(this.blockPosition()).is(FluidTags.WATER)) return false;
        return level.getEntitiesOfClass(SeaViper.class,
                this.getBoundingBox().inflate(16.0, 5.0, 16.0)).size() <= 1;
    }
ORIG:
  561|     public boolean func_70601_bi() {
  562|         SeaViper target = null;
  563|         for (int k = -3; k < 3; ++k) {
  564|             for (int j = -3; j < 3; ++j) {
  565|                 for (int i = 0; i < 5; ++i) {
  566|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  567|                     if (bid != Blocks.field_150474_ac) continue;
  568|                     TileEntityMobSpawner tileentitymobspawner = null;
  569|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  570|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  571|                     if (s == null || !s.equals("Sea Viper")) continue;
  572|                     return true;
  573|                 }
  574|             }
  575|         }
  576|         if (this.field_70163_u < 50.0) {
  577|             return false;
  578|         }
  579|         if (!this.field_70170_p.func_72935_r()) {
  580|             return false;
  581|         }
  582|         target = (SeaViper)this.field_70170_p.func_72857_a(SeaViper.class, this.field_70121_D.func_72314_b(16.0, 5.0, 16.0), (Entity)this);
  583|         return target == null;
  584|     }


### Skate  (original rule: Skate.java:318-329)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        if (this.random.nextInt(30) != 1) return false;
        return level.getEntitiesOfClass(Skate.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0)).size() <= 6;
    }
ORIG:
  318|     public boolean func_70601_bi() {
  319|         if (this.field_70163_u < 50.0) {
  320|             return false;
  321|         }
  322|         if (!this.field_70170_p.func_72935_r()) {
  323|             return false;
  324|         }
  325|         if (this.field_70170_p.field_73012_v.nextInt(30) != 1) {
  326|             return false;
  327|         }
  328|         return this.findBuddies() <= 6;
  329|     }


### SpiderDriver  (original rule: SpiderDriver.java:177-184)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        List<SpiderRobot> nearby = level.getEntitiesOfClass(SpiderRobot.class,
                this.getBoundingBox().inflate(24.0, 12.0, 24.0));
        if (!nearby.isEmpty()) return true;
        return super.checkSpawnRules(level, spawnType);
    }
ORIG:
  177|     public boolean func_70601_bi() {
  178|         SpiderRobot target = null;
  179|         target = (SpiderRobot)this.field_70170_p.func_72857_a(SpiderRobot.class, this.field_70121_D.func_72314_b(24.0, 12.0, 24.0), (Entity)this);
  180|         if (target != null) {
  181|             return true;
  182|         }
  183|         return super.func_70601_bi();
  184|     }


### Urchin  (original rule: Urchin.java:298-332)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        long timeOfDay = level.dayTime() % 24000L;
        return timeOfDay >= 13000L;
    }
ORIG:
  298|     public boolean func_70601_bi() {
  299|         Block bid;
  300|         int j;
  301|         int k;
  302|         int sc = 0;
  303|         for (k = -2; k <= 2; ++k) {
  304|             for (j = -2; j <= 2; ++j) {
  305|                 for (int i = 1; i < 4; ++i) {
  306|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  307|                     if (bid != Blocks.field_150474_ac) continue;
  308|                     TileEntityMobSpawner tileentitymobspawner = null;
  309|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  310|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  311|                     if (s == null || !s.equals("Crystal Urchin")) continue;
  312|                     this.was_spawnered = 1;
  313|                     return true;
  314|                 }
  315|             }
  316|         }
  317|         for (k = -1; k <= 1; ++k) {
  318|             for (j = -1; j <= 1; ++j) {
  319|                 bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + 1, (int)this.field_70161_v + k);
  320|                 if (bid != Blocks.field_150350_a) continue;
  321|                 ++sc;
  322|             }
  323|         }
  324|         if (sc < 6) {
  325|             return false;
  326|         }
  327|         if (!this.func_70814_o()) {
  328|             return false;
  329|         }
  330|         long t = this.field_70170_p.func_72820_D();
  331|         return (t %= 24000L) >= 13000L;
  332|     }


### VampireButterfly  (original rule: NONE)
PORT:
??

### VelocityRaptor  (original rule: VelocityRaptor.java:78-83)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.canSeeSky(this.blockPosition());
    }
ORIG:
   78|     public boolean func_70601_bi() {
   79|         if (this.field_70163_u < 50.0) {
   80|             return false;
   81|         }
   82|         return this.field_70170_p.func_72935_r();
   83|     }


### WaterDragon  (original rule: WaterDragon.java:716-739)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        return level.getEntitiesOfClass(WaterDragon.class,
                this.getBoundingBox().inflate(16.0, 5.0, 16.0)).size() <= 1;
    }
ORIG:
  716|     public boolean func_70601_bi() {
  717|         for (int k = -3; k < 3; ++k) {
  718|             for (int j = -3; j < 3; ++j) {
  719|                 for (int i = 0; i < 5; ++i) {
  720|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  721|                     if (bid != Blocks.field_150474_ac) continue;
  722|                     TileEntityMobSpawner tileentitymobspawner = null;
  723|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  724|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  725|                     if (s == null || !s.equals("Water Dragon")) continue;
  726|                     return true;
  727|                 }
  728|             }
  729|         }
  730|         WaterDragon target = null;
  731|         if (this.field_70163_u < 50.0) {
  732|             return false;
  733|         }
  734|         if (!this.field_70170_p.func_72935_r()) {
  735|             return false;
  736|         }
  737|         target = (WaterDragon)this.field_70170_p.func_72857_a(WaterDragon.class, this.field_70121_D.func_72314_b(16.0, 5.0, 16.0), (Entity)this);
  738|         return target == null;
  739|     }


### Whale  (original rule: Whale.java:260-271)
PORT:
@Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        if (this.getY() < 50.0) return false;
        long dayTime = level.dayTime() % 24000L;
        if (dayTime >= 13000L) return false;
        if (this.random.nextInt(50) != 1) return false;
        return level.getEntitiesOfClass(Whale.class,
                this.getBoundingBox().inflate(32.0, 8.0, 32.0)).size() <= 0;
    }
ORIG:
  260|     public boolean func_70601_bi() {
  261|         if (this.field_70163_u < 50.0) {
  262|             return false;
  263|         }
  264|         if (!this.field_70170_p.func_72935_r()) {
  265|             return false;
  266|         }
  267|         if (this.field_70170_p.field_73012_v.nextInt(50) != 1) {
  268|             return false;
  269|         }
  270|         return this.findBuddies() <= 0;
  271|     }


