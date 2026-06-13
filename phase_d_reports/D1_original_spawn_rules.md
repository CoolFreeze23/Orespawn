# D1 reference — every original `func_70601_bi` (getCanSpawnHere) override

Auto-extracted from `reference_1_7_10_source/sources/danger/orespawn/` by
`tools/extract_spawn_rules.py`. Line numbers are original-file lines, cite as
`orig <File>.java:<line>`.

## Alien.java (lines 397-434)

```java
  397|     public boolean func_70601_bi() {
  398|         Block bid;
  399|         int i;
  400|         int j;
  401|         int k;
  402|         for (k = -3; k < 3; ++k) {
  403|             for (j = -3; j < 3; ++j) {
  404|                 for (i = 0; i < 5; ++i) {
  405|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  406|                     if (bid != Blocks.field_150474_ac) continue;
  407|                     TileEntityMobSpawner tileentitymobspawner = null;
  408|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  409|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  410|                     if (s == null || !s.equals("Alien")) continue;
  411|                     return true;
  412|                 }
  413|             }
  414|         }
  415|         if (!this.func_70814_o()) {
  416|             return false;
  417|         }
  418|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  419|             return true;
  420|         }
  421|         if (this.field_70163_u > 50.0) {
  422|             return false;
  423|         }
  424|         for (k = -1; k < 2; ++k) {
  425|             for (j = -1; j < 2; ++j) {
  426|                 for (i = 1; i < 4; ++i) {
  427|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  428|                     if (bid == Blocks.field_150350_a) continue;
  429|                     return false;
  430|                 }
  431|             }
  432|         }
  433|         return true;
  434|     }
```

## Alosaurus.java (lines 240-279)

```java
  240|     public boolean func_70601_bi() {
  241|         Block bid;
  242|         int i;
  243|         int j;
  244|         int k;
  245|         for (k = -3; k < 3; ++k) {
  246|             for (j = -3; j < 3; ++j) {
  247|                 for (i = 0; i < 5; ++i) {
  248|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  249|                     if (bid != Blocks.field_150474_ac) continue;
  250|                     TileEntityMobSpawner tileentitymobspawner = null;
  251|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  252|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  253|                     if (s == null || !s.equals("Alosaurus")) continue;
  254|                     return true;
  255|                 }
  256|             }
  257|         }
  258|         if (!this.func_70814_o()) {
  259|             return false;
  260|         }
  261|         if (this.field_70163_u < 50.0) {
  262|             return false;
  263|         }
  264|         if (this.field_70170_p.func_72935_r()) {
  265|             return false;
  266|         }
  267|         for (k = -1; k < 1; ++k) {
  268|             for (j = -1; j < 1; ++j) {
  269|                 for (i = 1; i < 6; ++i) {
  270|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  271|                     if (bid == Blocks.field_150350_a) continue;
  272|                     return false;
  273|                 }
  274|             }
  275|         }
  276|         Alosaurus target = null;
  277|         target = (Alosaurus)this.field_70170_p.func_72857_a(Alosaurus.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
  278|         return target == null;
  279|     }
```

## AttackSquid.java (lines 645-651)

```java
  645|     public boolean func_70601_bi() {
  646|         super.func_70601_bi();
  647|         if (this.field_70163_u < 50.0) {
  648|             return false;
  649|         }
  650|         return this.field_70170_p.func_72935_r();
  651|     }
```

## BandP.java (lines 278-309)

```java
  278|     public boolean func_70601_bi() {
  279|         for (int k = -3; k < 3; ++k) {
  280|             for (int j = -3; j < 3; ++j) {
  281|                 for (int i = 0; i < 5; ++i) {
  282|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  283|                     if (bid != Blocks.field_150474_ac) continue;
  284|                     TileEntityMobSpawner tileentitymobspawner = null;
  285|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  286|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  287|                     if (s == null || !s.equals("Criminal")) continue;
  288|                     return true;
  289|                 }
  290|             }
  291|         }
  292|         if (!this.field_70170_p.func_72935_r()) {
  293|             return false;
  294|         }
  295|         if (this.field_70163_u < 50.0) {
  296|             return false;
  297|         }
  298|         if (this.field_70163_u < 100.0) {
  299|             return false;
  300|         }
  301|         BandP target = null;
  302|         target = (BandP)this.field_70170_p.func_72857_a(BandP.class, this.field_70121_D.func_72314_b(32.0, 12.0, 32.0), (Entity)this);
  303|         if (target != null) {
  304|             return false;
  305|         }
  306|         EntityVillager target2 = null;
  307|         target2 = (EntityVillager)this.field_70170_p.func_72857_a(EntityVillager.class, this.field_70121_D.func_72314_b(36.0, 12.0, 36.0), (Entity)this);
  308|         return target2 != null;
  309|     }
```

## Baryonyx.java (lines 66-74)

```java
   66|     public boolean func_70601_bi() {
   67|         if (this.field_70163_u < 50.0) {
   68|             return false;
   69|         }
   70|         if (!this.field_70170_p.func_72935_r()) {
   71|             return false;
   72|         }
   73|         return this.findBuddies() <= 8;
   74|     }
```

## Basilisk.java (lines 441-477)

```java
  441|     public boolean func_70601_bi() {
  442|         Block bid;
  443|         int i;
  444|         int j;
  445|         int k;
  446|         for (k = -3; k < 3; ++k) {
  447|             for (j = -3; j < 3; ++j) {
  448|                 for (i = 0; i < 5; ++i) {
  449|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  450|                     if (bid != Blocks.field_150474_ac) continue;
  451|                     TileEntityMobSpawner tileentitymobspawner = null;
  452|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  453|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  454|                     if (s == null || !s.equals("Basilisk")) continue;
  455|                     return true;
  456|                 }
  457|             }
  458|         }
  459|         if (!this.func_70814_o()) {
  460|             return false;
  461|         }
  462|         if (this.field_70170_p.func_72935_r()) {
  463|             return false;
  464|         }
  465|         for (k = -1; k < 2; ++k) {
  466|             for (j = -1; j < 2; ++j) {
  467|                 for (i = 1; i < 5; ++i) {
  468|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  469|                     if (bid == Blocks.field_150350_a) continue;
  470|                     return false;
  471|                 }
  472|             }
  473|         }
  474|         Basilisk target = null;
  475|         target = (Basilisk)this.field_70170_p.func_72857_a(Basilisk.class, this.field_70121_D.func_72314_b(20.0, 6.0, 20.0), (Entity)this);
  476|         return target == null;
  477|     }
```

## Beaver.java (lines 273-282)

```java
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
```

## Bee.java (lines 253-287)

```java
  253|     public boolean func_70601_bi() {
  254|         Block bid;
  255|         int i;
  256|         int j;
  257|         int k;
  258|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  259|             return true;
  260|         }
  261|         for (k = -2; k < 2; ++k) {
  262|             for (j = -2; j < 2; ++j) {
  263|                 for (i = 0; i < 5; ++i) {
  264|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  265|                     if (bid != Blocks.field_150474_ac) continue;
  266|                     TileEntityMobSpawner tileentitymobspawner = null;
  267|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  268|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  269|                     if (s == null || !s.equals("Bee")) continue;
  270|                     return true;
  271|                 }
  272|             }
  273|         }
  274|         for (k = -1; k < 2; ++k) {
  275|             for (j = -1; j < 2; ++j) {
  276|                 for (i = 1; i < 5; ++i) {
  277|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  278|                     if (bid == Blocks.field_150350_a) continue;
  279|                     return false;
  280|                 }
  281|             }
  282|         }
  283|         if (this.field_70163_u < 50.0) {
  284|             return false;
  285|         }
  286|         return this.field_70170_p.func_72935_r();
  287|     }
```

## Boyfriend.java (lines 978-993)

```java
  978|     public boolean func_70601_bi() {
  979|         for (int k = -3; k < 3; ++k) {
  980|             for (int j = -3; j < 3; ++j) {
  981|                 for (int i = 0; i < 5; ++i) {
  982|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  983|                     if (bid != Blocks.field_150474_ac) continue;
  984|                     TileEntityMobSpawner tileentitymobspawner = null;
  985|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  986|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  987|                     if (s == null || !s.equals("Boyfriend")) continue;
  988|                     return true;
  989|                 }
  990|             }
  991|         }
  992|         return super.func_70601_bi();
  993|     }
```

## Brutalfly.java (lines 290-329)

```java
  290|     public boolean func_70601_bi() {
  291|         Block bid;
  292|         int i;
  293|         int j;
  294|         int k;
  295|         for (k = -2; k <= 2; ++k) {
  296|             for (j = -2; j <= 2; ++j) {
  297|                 for (i = 1; i < 4; ++i) {
  298|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  299|                     if (bid != Blocks.field_150474_ac) continue;
  300|                     TileEntityMobSpawner tileentitymobspawner = null;
  301|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  302|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  303|                     if (s == null || !s.equals("Brutalfly")) continue;
  304|                     return true;
  305|                 }
  306|             }
  307|         }
  308|         if (this.field_70163_u < 70.0) {
  309|             return false;
  310|         }
  311|         if (!this.func_70814_o()) {
  312|             return false;
  313|         }
  314|         if (this.field_70170_p.func_72935_r()) {
  315|             return false;
  316|         }
  317|         for (k = -4; k < 4; ++k) {
  318|             for (j = -3; j < 3; ++j) {
  319|                 for (i = 1; i < 10; ++i) {
  320|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  321|                     if (bid == Blocks.field_150350_a) continue;
  322|                     return false;
  323|                 }
  324|             }
  325|         }
  326|         Brutalfly target = null;
  327|         target = (Brutalfly)this.field_70170_p.func_72857_a(Brutalfly.class, this.field_70121_D.func_72314_b(64.0, 32.0, 64.0), (Entity)this);
  328|         return target == null;
  329|     }
```

## Camarasaurus.java (lines 78-83)

```java
   78|     public boolean func_70601_bi() {
   79|         if (this.field_70163_u < 50.0) {
   80|             return false;
   81|         }
   82|         return this.field_70170_p.func_72935_r();
   83|     }
```

## Cassowary.java (lines 113-115)

```java
  113|     public boolean func_70601_bi() {
  114|         return this.field_70170_p.func_72935_r();
  115|     }
```

## CaterKiller.java (lines 585-624)

```java
  585|     public boolean func_70601_bi() {
  586|         Block bid;
  587|         int i;
  588|         int j;
  589|         int k;
  590|         for (k = -3; k < 3; ++k) {
  591|             for (j = -3; j < 3; ++j) {
  592|                 for (i = 0; i < 5; ++i) {
  593|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  594|                     if (bid != Blocks.field_150474_ac) continue;
  595|                     TileEntityMobSpawner tileentitymobspawner = null;
  596|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  597|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  598|                     if (s == null || !s.equals("CaterKiller")) continue;
  599|                     return true;
  600|                 }
  601|             }
  602|         }
  603|         if (this.field_70163_u < 50.0) {
  604|             return false;
  605|         }
  606|         if (this.field_70170_p.field_73012_v.nextInt(10) != 0) {
  607|             return false;
  608|         }
  609|         if (!this.field_70170_p.func_72935_r()) {
  610|             return false;
  611|         }
  612|         for (k = -1; k < 2; ++k) {
  613|             for (j = -1; j < 2; ++j) {
  614|                 for (i = 1; i < 5; ++i) {
  615|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  616|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150362_t || bid == Blocks.field_150361_u || bid == Blocks.field_150364_r || bid == Blocks.field_150363_s) continue;
  617|                     return false;
  618|                 }
  619|             }
  620|         }
  621|         CaterKiller target = null;
  622|         target = (CaterKiller)this.field_70170_p.func_72857_a(CaterKiller.class, this.field_70121_D.func_72314_b(48.0, 16.0, 48.0), (Entity)this);
  623|         return target == null;
  624|     }
```

## CaveFisher.java (lines 256-275)

```java
  256|     public boolean func_70601_bi() {
  257|         boolean sc = false;
  258|         for (int k = -2; k < 2; ++k) {
  259|             for (int j = -2; j < 2; ++j) {
  260|                 for (int i = 0; i < 5; ++i) {
  261|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  262|                     if (bid != Blocks.field_150474_ac) continue;
  263|                     TileEntityMobSpawner tileentitymobspawner = null;
  264|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  265|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  266|                     if (s == null || !s.equals("CaveFisher")) continue;
  267|                     return true;
  268|                 }
  269|             }
  270|         }
  271|         if (!this.func_70814_o()) {
  272|             return false;
  273|         }
  274|         return !(this.field_70163_u > 50.0);
  275|     }
```

## Cephadrome.java (lines 593-630)

```java
  593|     public boolean func_70601_bi() {
  594|         Block bid;
  595|         int i;
  596|         int j;
  597|         int k;
  598|         for (k = -3; k < 3; ++k) {
  599|             for (j = -3; j < 3; ++j) {
  600|                 for (i = 0; i < 5; ++i) {
  601|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  602|                     if (bid != Blocks.field_150474_ac) continue;
  603|                     TileEntityMobSpawner tileentitymobspawner = null;
  604|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  605|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  606|                     if (s == null || !s.equals("Cephadrome")) continue;
  607|                     this.badmood = 1;
  608|                     return true;
  609|                 }
  610|             }
  611|         }
  612|         if (!this.field_70170_p.func_72935_r()) {
  613|             return false;
  614|         }
  615|         if (this.field_70163_u < 50.0) {
  616|             return false;
  617|         }
  618|         for (k = -2; k < 2; ++k) {
  619|             for (j = -2; j < 2; ++j) {
  620|                 for (i = 1; i < 5; ++i) {
  621|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  622|                     if (bid == Blocks.field_150350_a) continue;
  623|                     return false;
  624|                 }
  625|             }
  626|         }
  627|         Cephadrome target = null;
  628|         target = (Cephadrome)this.field_70170_p.func_72857_a(Cephadrome.class, this.field_70121_D.func_72314_b(16.0, 6.0, 16.0), (Entity)this);
  629|         return target == null;
  630|     }
```

## Chipmunk.java (lines 248-253)

```java
  248|     public boolean func_70601_bi() {
  249|         if (this.field_70163_u < 50.0) {
  250|             return false;
  251|         }
  252|         return this.findBuddies() <= 2;
  253|     }
```

## CliffRacer.java (lines 145-147)

```java
  145|     public boolean func_70601_bi() {
  146|         return !(this.field_70163_u < 50.0);
  147|     }
```

## CloudShark.java (lines 198-200)

```java
  198|     public boolean func_70601_bi() {
  199|         return true;
  200|     }
```

## Cockateil.java (lines 232-240)

```java
  232|     public boolean func_70601_bi() {
  233|         if (!this.field_70170_p.func_72935_r()) {
  234|             return false;
  235|         }
  236|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  237|             return true;
  238|         }
  239|         return !(this.field_70163_u < 50.0);
  240|     }
```

## Coin.java (lines 138-148)

```java
  138|     public boolean func_70601_bi() {
  139|         if (!this.field_70170_p.func_72935_r()) {
  140|             return false;
  141|         }
  142|         if (this.field_70163_u < 50.0) {
  143|             return false;
  144|         }
  145|         Coin target = null;
  146|         target = (Coin)this.field_70170_p.func_72857_a(Coin.class, this.field_70121_D.func_72314_b(20.0, 8.0, 20.0), (Entity)this);
  147|         return target == null;
  148|     }
```

## Crab.java (lines 456-486)

```java
  456|     public boolean func_70601_bi() {
  457|         for (int k = -3; k < 3; ++k) {
  458|             for (int j = -3; j < 3; ++j) {
  459|                 for (int i = 0; i < 5; ++i) {
  460|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  461|                     if (bid != Blocks.field_150474_ac) continue;
  462|                     TileEntityMobSpawner tileentitymobspawner = null;
  463|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  464|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  465|                     if (s == null || !s.equals("Crab")) continue;
  466|                     this.setCrabScale(0.35f);
  467|                     return true;
  468|                 }
  469|             }
  470|         }
  471|         if (this.field_70163_u < 50.0) {
  472|             return false;
  473|         }
  474|         if (!this.field_70170_p.func_72935_r()) {
  475|             return false;
  476|         }
  477|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5) {
  478|             if (this.field_70170_p.field_73012_v.nextInt(40) != 1) {
  479|                 return false;
  480|             }
  481|             if (this.findBuddies() > 3) {
  482|                 return false;
  483|             }
  484|         }
  485|         return true;
  486|     }
```

## CreepingHorror.java (lines 220-228)

```java
  220|     public boolean func_70601_bi() {
  221|         if (!this.func_70814_o()) {
  222|             return false;
  223|         }
  224|         if (this.field_70170_p.func_72935_r()) {
  225|             return false;
  226|         }
  227|         return this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 || !(this.field_70163_u > 15.0);
  228|     }
```

## Cricket.java (lines 137-142)

```java
  137|     public boolean func_70601_bi() {
  138|         if (this.field_70163_u < 30.0) {
  139|             return false;
  140|         }
  141|         return this.findBuddies() <= 5;
  142|     }
```

## Cryolophosaurus.java (lines 231-236)

```java
  231|     public boolean func_70601_bi() {
  232|         if (!this.func_70814_o()) {
  233|             return false;
  234|         }
  235|         return !this.field_70170_p.func_72935_r() || !(this.field_70163_u > 50.0);
  236|     }
```

## Dragon.java (lines 598-611)

```java
  598|     public boolean func_70601_bi() {
  599|         Dragon target = null;
  600|         if (!this.field_70170_p.func_72935_r()) {
  601|             return false;
  602|         }
  603|         target = (Dragon)this.field_70170_p.func_72857_a(Dragon.class, this.field_70121_D.func_72314_b(16.0, 6.0, 16.0), (Entity)this);
  604|         if (target != null) {
  605|             return false;
  606|         }
  607|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  608|             return true;
  609|         }
  610|         return !(this.field_70163_u < 50.0);
  611|     }
```

## Dragonfly.java (lines 187-192)

```java
  187|     public boolean func_70601_bi() {
  188|         if (this.field_70163_u < 50.0) {
  189|             return false;
  190|         }
  191|         return this.field_70170_p.func_72935_r();
  192|     }
```

## DungeonBeast.java (lines 275-312)

```java
  275|     public boolean func_70601_bi() {
  276|         Block bid;
  277|         int j;
  278|         int k;
  279|         int sc = 0;
  280|         for (k = -3; k < 3; ++k) {
  281|             for (j = -3; j < 3; ++j) {
  282|                 for (int i = 0; i < 5; ++i) {
  283|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  284|                     if (bid != Blocks.field_150474_ac) continue;
  285|                     TileEntityMobSpawner tileentitymobspawner = null;
  286|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  287|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  288|                     if (s == null || !s.equals("Dungeon Beast")) continue;
  289|                     return true;
  290|                 }
  291|             }
  292|         }
  293|         if (!this.func_70814_o()) {
  294|             return false;
  295|         }
  296|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5) {
  297|             if (this.field_70163_u > 28.0 || this.field_70163_u < 25.0) {
  298|                 return false;
  299|             }
  300|             for (k = -1; k <= 1; ++k) {
  301|                 for (j = -1; j <= 1; ++j) {
  302|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + 1, (int)this.field_70161_v + k);
  303|                     if (bid != Blocks.field_150350_a) continue;
  304|                     ++sc;
  305|                 }
  306|             }
  307|             if (sc < 6) {
  308|                 return false;
  309|             }
  310|         }
  311|         return true;
  312|     }
```

## EasterBunny.java (lines 67-77)

```java
   67|     public boolean func_70601_bi() {
   68|         if (this.field_70163_u < 50.0) {
   69|             return false;
   70|         }
   71|         if (!this.field_70170_p.func_72935_r()) {
   72|             return false;
   73|         }
   74|         EasterBunny target = null;
   75|         target = (EasterBunny)this.field_70170_p.func_72857_a(EasterBunny.class, this.field_70121_D.func_72314_b(32.0, 8.0, 32.0), (Entity)this);
   76|         return target == null;
   77|     }
```

## EmperorScorpion.java (lines 529-559)

```java
  529|     public boolean func_70601_bi() {
  530|         for (int k = -2; k < 2; ++k) {
  531|             for (int j = -2; j < 2; ++j) {
  532|                 for (int i = 2; i < 5; ++i) {
  533|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  534|                     if (bid == Blocks.field_150474_ac) {
  535|                         TileEntityMobSpawner tileentitymobspawner = null;
  536|                         tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  537|                         String s = tileentitymobspawner.func_145881_a().func_98276_e();
  538|                         if (s != null && s.equals("Emperor Scorpion")) {
  539|                             return true;
  540|                         }
  541|                     }
  542|                     if (bid == Blocks.field_150350_a) continue;
  543|                     return false;
  544|                 }
  545|             }
  546|         }
  547|         if (!this.func_70814_o()) {
  548|             return false;
  549|         }
  550|         if (this.field_70170_p.func_72935_r()) {
  551|             return false;
  552|         }
  553|         if (this.field_70163_u < 50.0) {
  554|             return false;
  555|         }
  556|         EmperorScorpion target = null;
  557|         target = (EmperorScorpion)this.field_70170_p.func_72857_a(EmperorScorpion.class, this.field_70121_D.func_72314_b(20.0, 6.0, 20.0), (Entity)this);
  558|         return target == null;
  559|     }
```

## EnderKnight.java (lines 256-277)

```java
  256|     public boolean func_70601_bi() {
  257|         for (int k = -3; k < 3; ++k) {
  258|             for (int j = -3; j < 3; ++j) {
  259|                 for (int i = 0; i < 5; ++i) {
  260|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  261|                     if (bid != Blocks.field_150474_ac) continue;
  262|                     TileEntityMobSpawner tileentitymobspawner = null;
  263|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  264|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  265|                     if (s == null || !s.equals("Ender Knight")) continue;
  266|                     return true;
  267|                 }
  268|             }
  269|         }
  270|         if (!this.func_70814_o()) {
  271|             return false;
  272|         }
  273|         if (this.field_70170_p.func_72935_r()) {
  274|             return false;
  275|         }
  276|         return !(this.field_70163_u < 30.0);
  277|     }
```

## EnderReaper.java (lines 253-279)

```java
  253|     public boolean func_70601_bi() {
  254|         for (int k = -3; k < 3; ++k) {
  255|             for (int j = -3; j < 3; ++j) {
  256|                 for (int i = 0; i < 5; ++i) {
  257|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  258|                     if (bid != Blocks.field_150474_ac) continue;
  259|                     TileEntityMobSpawner tileentitymobspawner = null;
  260|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  261|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  262|                     if (s == null || !s.equals("Ender Reaper")) continue;
  263|                     return true;
  264|                 }
  265|             }
  266|         }
  267|         if (!this.func_70814_o()) {
  268|             return false;
  269|         }
  270|         if (this.field_70170_p.func_72935_r()) {
  271|             return false;
  272|         }
  273|         if (this.field_70163_u < 30.0) {
  274|             return false;
  275|         }
  276|         EnderReaper target = null;
  277|         target = (EnderReaper)this.field_70170_p.func_72857_a(EnderReaper.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
  278|         return target == null;
  279|     }
```

## EntityAnt.java (lines 140-145)

```java
  140|     public boolean func_70601_bi() {
  141|         if (this.field_70163_u < 50.0) {
  142|             return false;
  143|         }
  144|         return this.findBuddies() <= 4;
  145|     }
```

## EntityButterfly.java (lines 283-310)

```java
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
```

## EntityLunaMoth.java (lines 168-180)

```java
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
```

## EntityMosquito.java (lines 145-147)

```java
  145|     public boolean func_70601_bi() {
  146|         return true;
  147|     }
```

## Fairy.java (lines 334-347)

```java
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
```

## Firefly.java (lines 161-176)

```java
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
```

## Flounder.java (lines 219-230)

```java
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
```

## Frog.java (lines 240-251)

```java
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
```

## GammaMetroid.java (lines 328-365)

```java
  328|     public boolean func_70601_bi() {
  329|         Block bid;
  330|         int i;
  331|         int j;
  332|         int k;
  333|         for (k = -3; k < 3; ++k) {
  334|             for (j = -3; j < 3; ++j) {
  335|                 for (i = 0; i < 5; ++i) {
  336|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  337|                     if (bid != Blocks.field_150474_ac) continue;
  338|                     TileEntityMobSpawner tileentitymobspawner = null;
  339|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  340|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  341|                     if (s == null || !s.equals("WTF?")) continue;
  342|                     return true;
  343|                 }
  344|             }
  345|         }
  346|         if (!this.isValidLightLevel()) {
  347|             return false;
  348|         }
  349|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4) {
  350|             return true;
  351|         }
  352|         if (this.field_70163_u > 50.0) {
  353|             return false;
  354|         }
  355|         for (k = -1; k < 1; ++k) {
  356|             for (j = -1; j < 1; ++j) {
  357|                 for (i = 1; i < 4; ++i) {
  358|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  359|                     if (bid == Blocks.field_150350_a) continue;
  360|                     return false;
  361|                 }
  362|             }
  363|         }
  364|         return true;
  365|     }
```

## Gazelle.java (lines 368-377)

```java
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
```

## Ghost.java (lines 145-160)

```java
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
```

## GhostSkelly.java (lines 173-188)

```java
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
```

## GiantRobot.java (lines 364-381)

```java
  364|     public boolean func_70601_bi() {
  365|         if (this.field_70163_u < 50.0) {
  366|             return false;
  367|         }
  368|         if (this.field_70170_p.func_72935_r()) {
  369|             return false;
  370|         }
  371|         for (int k = -1; k < 1; ++k) {
  372|             for (int j = -1; j <= 1; ++j) {
  373|                 for (int i = 1; i < 6; ++i) {
  374|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  375|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
  376|                     return false;
  377|                 }
  378|             }
  379|         }
  380|         return this.func_70814_o();
  381|     }
```

## Girlfriend.java (lines 1100-1115)

```java
 1100|     public boolean func_70601_bi() {
 1101|         for (int k = -3; k < 3; ++k) {
 1102|             for (int j = -3; j < 3; ++j) {
 1103|                 for (int i = 0; i < 5; ++i) {
 1104|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
 1105|                     if (bid != Blocks.field_150474_ac) continue;
 1106|                     TileEntityMobSpawner tileentitymobspawner = null;
 1107|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
 1108|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
 1109|                     if (s == null || !s.equals("Girlfriend")) continue;
 1110|                     return true;
 1111|                 }
 1112|             }
 1113|         }
 1114|         return super.func_70601_bi();
 1115|     }
```

## Godzilla.java (lines 557-591)

```java
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
```

## GoldFish.java (lines 153-155)

```java
  153|     public boolean func_70601_bi() {
  154|         return true;
  155|     }
```

## Hammerhead.java (lines 277-316)

```java
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
```

## HerculesBeetle.java (lines 442-481)

```java
  442|     public boolean func_70601_bi() {
  443|         Block bid;
  444|         int i;
  445|         int j;
  446|         int k;
  447|         for (k = -3; k < 3; ++k) {
  448|             for (j = -3; j < 3; ++j) {
  449|                 for (i = 0; i < 5; ++i) {
  450|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  451|                     if (bid != Blocks.field_150474_ac) continue;
  452|                     TileEntityMobSpawner tileentitymobspawner = null;
  453|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  454|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  455|                     if (s == null || !s.equals("Hercules Beetle")) continue;
  456|                     return true;
  457|                 }
  458|             }
  459|         }
  460|         if (!this.func_70814_o()) {
  461|             return false;
  462|         }
  463|         if (this.field_70170_p.func_72935_r()) {
  464|             return false;
  465|         }
  466|         if (this.field_70163_u < 50.0) {
  467|             return false;
  468|         }
  469|         for (k = -2; k < 2; ++k) {
  470|             for (j = -2; j < 2; ++j) {
  471|                 for (i = 2; i < 5; ++i) {
  472|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  473|                     if (bid == Blocks.field_150350_a) continue;
  474|                     return false;
  475|                 }
  476|             }
  477|         }
  478|         HerculesBeetle target = null;
  479|         target = (HerculesBeetle)this.field_70170_p.func_72857_a(HerculesBeetle.class, this.field_70121_D.func_72314_b(16.0, 6.0, 16.0), (Entity)this);
  480|         return target == null;
  481|     }
```

## Irukandji.java (lines 326-337)

```java
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
```

## Kraken.java (lines 1183-1197)

```java
 1183|     public boolean func_70601_bi() {
 1184|         if (this.field_70163_u < 50.0) {
 1185|             return false;
 1186|         }
 1187|         for (int k = -1; k < 2; ++k) {
 1188|             for (int j = -1; j < 1; ++j) {
 1189|                 for (int i = 1; i < 6; ++i) {
 1190|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
 1191|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
 1192|                     return false;
 1193|                 }
 1194|             }
 1195|         }
 1196|         return true;
 1197|     }
```

## Kyuubi.java (lines 222-224)

```java
  222|     public boolean func_70601_bi() {
  223|         return true;
  224|     }
```

## LeafMonster.java (lines 227-251)

```java
  227|     public boolean func_70601_bi() {
  228|         for (int k = -3; k < 3; ++k) {
  229|             for (int j = -3; j < 3; ++j) {
  230|                 for (int i = 0; i < 5; ++i) {
  231|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  232|                     if (bid != Blocks.field_150474_ac) continue;
  233|                     TileEntityMobSpawner tileentitymobspawner = null;
  234|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  235|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  236|                     if (s == null || !s.equals("Leaf Monster")) continue;
  237|                     return true;
  238|                 }
  239|             }
  240|         }
  241|         if (!this.func_70814_o()) {
  242|             return false;
  243|         }
  244|         if (this.field_70170_p.func_72935_r()) {
  245|             return false;
  246|         }
  247|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID4 ? this.field_70163_u > 20.0 : this.field_70163_u < 50.0) {
  248|             return false;
  249|         }
  250|         return this.findBuddies() <= 4;
  251|     }
```

## Leon.java (lines 452-478)

```java
  452|     public boolean func_70601_bi() {
  453|         for (int k = -3; k < 3; ++k) {
  454|             for (int j = -3; j < 3; ++j) {
  455|                 for (int i = 0; i < 5; ++i) {
  456|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  457|                     if (bid != Blocks.field_150474_ac) continue;
  458|                     TileEntityMobSpawner tileentitymobspawner = null;
  459|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  460|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  461|                     if (s == null || !s.equals("Leonopteryx")) continue;
  462|                     return true;
  463|                 }
  464|             }
  465|         }
  466|         if (this.field_70170_p.field_73012_v.nextInt(16) != 0) {
  467|             return false;
  468|         }
  469|         Leon target = null;
  470|         if (!this.field_70170_p.func_72935_r()) {
  471|             return false;
  472|         }
  473|         target = (Leon)this.field_70170_p.func_72857_a(Leon.class, this.field_70121_D.func_72314_b(48.0, 16.0, 48.0), (Entity)this);
  474|         if (target != null) {
  475|             return false;
  476|         }
  477|         return !(this.field_70163_u < 50.0);
  478|     }
```

## Lizard.java (lines 368-370)

```java
  368|     public boolean func_70601_bi() {
  369|         return !(this.field_70163_u < 50.0);
  370|     }
```

## LurkingTerror.java (lines 237-269)

```java
  237|     public boolean func_70601_bi() {
  238|         LurkingTerror target = null;
  239|         for (int k = -2; k < 2; ++k) {
  240|             for (int j = -2; j < 2; ++j) {
  241|                 for (int i = 0; i < 5; ++i) {
  242|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  243|                     if (bid != Blocks.field_150474_ac) continue;
  244|                     TileEntityMobSpawner tileentitymobspawner = null;
  245|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  246|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  247|                     if (s == null || !s.equals("Lurking Terror")) continue;
  248|                     return true;
  249|                 }
  250|             }
  251|         }
  252|         if (!this.func_70814_o()) {
  253|             return false;
  254|         }
  255|         if (!this.field_70170_p.func_72935_r()) {
  256|             return false;
  257|         }
  258|         if (this.field_70170_p.field_73012_v.nextInt(2) != 1) {
  259|             return false;
  260|         }
  261|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 && this.field_70170_p.field_73012_v.nextInt(6) != 0) {
  262|             return false;
  263|         }
  264|         target = (LurkingTerror)this.field_70170_p.func_72857_a(LurkingTerror.class, this.field_70121_D.func_72314_b(32.0, 16.0, 32.0), (Entity)this);
  265|         if (target != null) {
  266|             return false;
  267|         }
  268|         return !(this.field_70163_u < 10.0);
  269|     }
```

## Mantis.java (lines 263-302)

```java
  263|     public boolean func_70601_bi() {
  264|         Block bid;
  265|         int i;
  266|         int j;
  267|         int k;
  268|         for (k = -2; k <= 2; ++k) {
  269|             for (j = -2; j <= 2; ++j) {
  270|                 for (i = 1; i < 4; ++i) {
  271|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  272|                     if (bid != Blocks.field_150474_ac) continue;
  273|                     TileEntityMobSpawner tileentitymobspawner = null;
  274|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  275|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  276|                     if (s == null || !s.equals("Mantis")) continue;
  277|                     return true;
  278|                 }
  279|             }
  280|         }
  281|         for (k = -2; k < 2; ++k) {
  282|             for (j = -2; j < 2; ++j) {
  283|                 for (i = 1; i < 6; ++i) {
  284|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  285|                     if (bid == Blocks.field_150350_a) continue;
  286|                     return false;
  287|                 }
  288|             }
  289|         }
  290|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 && this.field_70170_p.field_73012_v.nextInt(6) != 0) {
  291|             return false;
  292|         }
  293|         if (this.field_70163_u < 50.0) {
  294|             return false;
  295|         }
  296|         if (!this.field_70170_p.func_72935_r()) {
  297|             return false;
  298|         }
  299|         Mantis target = null;
  300|         target = (Mantis)this.field_70170_p.func_72857_a(Mantis.class, this.field_70121_D.func_72314_b(32.0, 16.0, 32.0), (Entity)this);
  301|         return target == null;
  302|     }
```

## Molenoid.java (lines 303-342)

```java
  303|     public boolean func_70601_bi() {
  304|         Block bid;
  305|         int i;
  306|         int j;
  307|         int k;
  308|         for (k = -3; k < 3; ++k) {
  309|             for (j = -3; j < 3; ++j) {
  310|                 for (i = 0; i < 5; ++i) {
  311|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  312|                     if (bid != Blocks.field_150474_ac) continue;
  313|                     TileEntityMobSpawner tileentitymobspawner = null;
  314|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  315|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  316|                     if (s == null || !s.equals("Molenoid")) continue;
  317|                     return true;
  318|                 }
  319|             }
  320|         }
  321|         if (!this.func_70814_o()) {
  322|             return false;
  323|         }
  324|         if (this.field_70163_u < 50.0) {
  325|             return false;
  326|         }
  327|         if (this.field_70170_p.func_72935_r()) {
  328|             return false;
  329|         }
  330|         for (k = -1; k < 1; ++k) {
  331|             for (j = -1; j < 1; ++j) {
  332|                 for (i = 1; i < 4; ++i) {
  333|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  334|                     if (bid == Blocks.field_150350_a) continue;
  335|                     return false;
  336|                 }
  337|             }
  338|         }
  339|         Molenoid target = null;
  340|         target = (Molenoid)this.field_70170_p.func_72857_a(Molenoid.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
  341|         return target == null;
  342|     }
```

## Mothra.java (lines 295-331)

```java
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
```

## Nastysaurus.java (lines 304-343)

```java
  304|     public boolean func_70601_bi() {
  305|         Block bid;
  306|         int i;
  307|         int j;
  308|         int k;
  309|         for (k = -3; k < 3; ++k) {
  310|             for (j = -3; j < 3; ++j) {
  311|                 for (i = 0; i < 5; ++i) {
  312|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  313|                     if (bid != Blocks.field_150474_ac) continue;
  314|                     TileEntityMobSpawner tileentitymobspawner = null;
  315|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  316|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  317|                     if (s == null || !s.equals("Nastysaurus")) continue;
  318|                     return true;
  319|                 }
  320|             }
  321|         }
  322|         if (!this.func_70814_o()) {
  323|             return false;
  324|         }
  325|         if (this.field_70163_u < 50.0) {
  326|             return false;
  327|         }
  328|         if (this.field_70170_p.func_72935_r()) {
  329|             return false;
  330|         }
  331|         for (k = -1; k < 1; ++k) {
  332|             for (j = -1; j < 1; ++j) {
  333|                 for (i = 1; i < 6; ++i) {
  334|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  335|                     if (bid == Blocks.field_150350_a) continue;
  336|                     return false;
  337|                 }
  338|             }
  339|         }
  340|         Nastysaurus target = null;
  341|         target = (Nastysaurus)this.field_70170_p.func_72857_a(Nastysaurus.class, this.field_70121_D.func_72314_b(16.0, 8.0, 16.0), (Entity)this);
  342|         return target == null;
  343|     }
```

## Ostrich.java (lines 325-338)

```java
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
```

## Peacock.java (lines 101-119)

```java
  101|     public boolean func_70601_bi() {
  102|         for (int k = -1; k < 1; ++k) {
  103|             for (int j = -1; j < 1; ++j) {
  104|                 for (int i = 1; i < 3; ++i) {
  105|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  106|                     if (bid == Blocks.field_150350_a) continue;
  107|                     return false;
  108|                 }
  109|             }
  110|         }
  111|         long t = this.field_70170_p.func_72820_D();
  112|         if ((t %= 24000L) > 12000L) {
  113|             return false;
  114|         }
  115|         if (this.field_70163_u < 50.0 || this.field_70163_u > 100.0) {
  116|             return false;
  117|         }
  118|         return this.findBuddies() <= 2;
  119|     }
```

## PitchBlack.java (lines 429-483)

```java
  429|     public boolean func_70601_bi() {
  430|         Block bid;
  431|         int i;
  432|         int j;
  433|         int k;
  434|         for (k = -3; k < 3; ++k) {
  435|             for (j = -3; j < 3; ++j) {
  436|                 for (i = 0; i < 5; ++i) {
  437|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  438|                     if (bid != Blocks.field_150474_ac) continue;
  439|                     TileEntityMobSpawner tileentitymobspawner = null;
  440|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  441|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  442|                     if (s == null || !s.equals("Nightmare")) continue;
  443|                     Float t = Float.valueOf(this.getPitchBlackScale());
  444|                     if (t.floatValue() > 1.0f) {
  445|                         t = Float.valueOf(1.0f);
  446|                     }
  447|                     this.setPitchBlackScale(t.floatValue());
  448|                     return true;
  449|                 }
  450|             }
  451|         }
  452|         if (!this.func_70814_o()) {
  453|             return false;
  454|         }
  455|         if (this.field_70170_p.func_72935_r()) {
  456|             return false;
  457|         }
  458|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6) {
  459|             PitchBlack target = null;
  460|             target = (PitchBlack)this.field_70170_p.func_72857_a(PitchBlack.class, this.field_70121_D.func_72314_b(16.0, 16.0, 16.0), (Entity)this);
  461|             if (target != null) {
  462|                 return false;
  463|             }
  464|         }
  465|         if (this.getPitchBlackScale() < 1.1f) {
  466|             return true;
  467|         }
  468|         int ix = 1;
  469|         if (this.getPitchBlackScale() > 3.1f) {
  470|             ix = 2;
  471|         }
  472|         int iy = ix * 3;
  473|         for (k = -ix; k <= ix; ++k) {
  474|             for (j = -ix; j <= ix; ++j) {
  475|                 for (i = 1; i <= iy; ++i) {
  476|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  477|                     if (bid == Blocks.field_150350_a) continue;
  478|                     return false;
  479|                 }
  480|             }
  481|         }
  482|         return true;
  483|     }
```

## Pointysaurus.java (lines 275-312)

```java
  275|     public boolean func_70601_bi() {
  276|         Block bid;
  277|         int i;
  278|         int j;
  279|         int k;
  280|         for (k = -3; k < 3; ++k) {
  281|             for (j = -3; j < 3; ++j) {
  282|                 for (i = 0; i < 5; ++i) {
  283|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  284|                     if (bid != Blocks.field_150474_ac) continue;
  285|                     TileEntityMobSpawner tileentitymobspawner = null;
  286|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  287|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  288|                     if (s == null || !s.equals("Pointysaurus")) continue;
  289|                     return true;
  290|                 }
  291|             }
  292|         }
  293|         if (!this.func_70814_o()) {
  294|             return false;
  295|         }
  296|         if (this.field_70163_u < 50.0) {
  297|             return false;
  298|         }
  299|         if (this.field_70170_p.func_72935_r()) {
  300|             return false;
  301|         }
  302|         for (k = -1; k < 1; ++k) {
  303|             for (j = -1; j < 1; ++j) {
  304|                 for (i = 1; i < 6; ++i) {
  305|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  306|                     if (bid == Blocks.field_150350_a) continue;
  307|                     return false;
  308|                 }
  309|             }
  310|         }
  311|         return true;
  312|     }
```

## PurplePower.java (lines 226-228)

```java
  226|     public boolean func_70601_bi() {
  227|         return true;
  228|     }
```

## Rat.java (lines 302-339)

```java
  302|     public boolean func_70601_bi() {
  303|         Block bid;
  304|         int j;
  305|         int k;
  306|         int sc = 0;
  307|         for (k = -2; k < 2; ++k) {
  308|             for (j = -2; j < 2; ++j) {
  309|                 for (int i = 0; i < 5; ++i) {
  310|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  311|                     if (bid != Blocks.field_150474_ac) continue;
  312|                     TileEntityMobSpawner tileentitymobspawner = null;
  313|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  314|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  315|                     if (s == null || !s.equals("Rat")) continue;
  316|                     return true;
  317|                 }
  318|             }
  319|         }
  320|         if (!this.func_70814_o()) {
  321|             return false;
  322|         }
  323|         if (this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID5) {
  324|             if (this.field_70163_u > 50.0) {
  325|                 return false;
  326|             }
  327|             for (k = -1; k <= 1; ++k) {
  328|                 for (j = -1; j <= 1; ++j) {
  329|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + 1, (int)this.field_70161_v + k);
  330|                     if (bid != Blocks.field_150350_a) continue;
  331|                     ++sc;
  332|                 }
  333|             }
  334|             if (sc < 4) {
  335|                 return false;
  336|             }
  337|         }
  338|         return this.findBuddies() <= 8;
  339|     }
```

## Robot1.java (lines 226-234)

```java
  226|     public boolean func_70601_bi() {
  227|         if (this.field_70163_u < 50.0) {
  228|             return false;
  229|         }
  230|         if (!this.func_70814_o()) {
  231|             return false;
  232|         }
  233|         return !this.field_70170_p.func_72935_r();
  234|     }
```

## Robot2.java (lines 403-437)

```java
  403|     public boolean func_70601_bi() {
  404|         Block bid;
  405|         int i;
  406|         int j;
  407|         int k;
  408|         for (k = -3; k < 3; ++k) {
  409|             for (j = -3; j < 3; ++j) {
  410|                 for (i = 0; i < 5; ++i) {
  411|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  412|                     if (bid != Blocks.field_150474_ac) continue;
  413|                     TileEntityMobSpawner tileentitymobspawner = null;
  414|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  415|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  416|                     if (s == null || !s.equals("Robo-Pounder")) continue;
  417|                     return true;
  418|                 }
  419|             }
  420|         }
  421|         if (this.field_70163_u < 50.0) {
  422|             return false;
  423|         }
  424|         if (this.field_70170_p.func_72935_r()) {
  425|             return false;
  426|         }
  427|         for (k = -1; k < 1; ++k) {
  428|             for (j = -1; j <= 1; ++j) {
  429|                 for (i = 1; i < 6; ++i) {
  430|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  431|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
  432|                     return false;
  433|                 }
  434|             }
  435|         }
  436|         return this.func_70814_o();
  437|     }
```

## Robot3.java (lines 343-360)

```java
  343|     public boolean func_70601_bi() {
  344|         if (this.field_70163_u < 50.0) {
  345|             return false;
  346|         }
  347|         if (this.field_70170_p.func_72935_r()) {
  348|             return false;
  349|         }
  350|         for (int k = -1; k < 1; ++k) {
  351|             for (int j = -1; j <= 1; ++j) {
  352|                 for (int i = 1; i < 6; ++i) {
  353|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  354|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
  355|                     return false;
  356|                 }
  357|             }
  358|         }
  359|         return this.func_70814_o();
  360|     }
```

## Robot4.java (lines 415-449)

```java
  415|     public boolean func_70601_bi() {
  416|         Block bid;
  417|         int i;
  418|         int j;
  419|         int k;
  420|         for (k = -3; k < 3; ++k) {
  421|             for (j = -3; j < 3; ++j) {
  422|                 for (i = 0; i < 5; ++i) {
  423|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  424|                     if (bid != Blocks.field_150474_ac) continue;
  425|                     TileEntityMobSpawner tileentitymobspawner = null;
  426|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  427|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  428|                     if (s == null || !s.equals("Robo-Warrior")) continue;
  429|                     return true;
  430|                 }
  431|             }
  432|         }
  433|         if (this.field_70163_u < 50.0) {
  434|             return false;
  435|         }
  436|         if (this.field_70170_p.func_72935_r()) {
  437|             return false;
  438|         }
  439|         for (k = -1; k < 1; ++k) {
  440|             for (j = -1; j <= 1; ++j) {
  441|                 for (i = 1; i < 6; ++i) {
  442|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  443|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
  444|                     return false;
  445|                 }
  446|             }
  447|         }
  448|         return this.func_70814_o();
  449|     }
```

## Robot5.java (lines 317-351)

```java
  317|     public boolean func_70601_bi() {
  318|         Block bid;
  319|         int i;
  320|         int j;
  321|         int k;
  322|         for (k = -3; k < 3; ++k) {
  323|             for (j = -3; j < 3; ++j) {
  324|                 for (i = 0; i < 5; ++i) {
  325|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  326|                     if (bid != Blocks.field_150474_ac) continue;
  327|                     TileEntityMobSpawner tileentitymobspawner = null;
  328|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  329|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  330|                     if (s == null || !s.equals("Robo-Sniper")) continue;
  331|                     return true;
  332|                 }
  333|             }
  334|         }
  335|         if (this.field_70163_u < 50.0) {
  336|             return false;
  337|         }
  338|         if (this.field_70170_p.func_72935_r()) {
  339|             return false;
  340|         }
  341|         for (k = -1; k < 1; ++k) {
  342|             for (j = -1; j <= 1; ++j) {
  343|                 for (i = 1; i < 3; ++i) {
  344|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  345|                     if (bid == Blocks.field_150350_a || bid == Blocks.field_150329_H) continue;
  346|                     return false;
  347|                 }
  348|             }
  349|         }
  350|         return this.func_70814_o();
  351|     }
```

## RockBase.java (lines 191-193)

```java
  191|     public boolean func_70601_bi() {
  192|         return !(this.field_70163_u < 50.0);
  193|     }
```

## Rotator.java (lines 255-288)

```java
  255|     public boolean func_70601_bi() {
  256|         Block bid;
  257|         int i;
  258|         int j;
  259|         int k;
  260|         for (k = -2; k <= 2; ++k) {
  261|             for (j = -2; j <= 2; ++j) {
  262|                 for (i = 1; i < 4; ++i) {
  263|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  264|                     if (bid != Blocks.field_150474_ac) continue;
  265|                     TileEntityMobSpawner tileentitymobspawner = null;
  266|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  267|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  268|                     if (s == null || !s.equals("Rotator")) continue;
  269|                     this.was_spawnered = 1;
  270|                     return true;
  271|                 }
  272|             }
  273|         }
  274|         if (!this.func_70814_o()) {
  275|             return false;
  276|         }
  277|         for (k = -1; k <= 1; ++k) {
  278|             for (j = -1; j <= 1; ++j) {
  279|                 for (i = 1; i < 3; ++i) {
  280|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  281|                     if (bid == Blocks.field_150350_a) continue;
  282|                     return false;
  283|                 }
  284|             }
  285|         }
  286|         long t = this.field_70170_p.func_72820_D();
  287|         return (t %= 24000L) >= 12000L;
  288|     }
```

## RubberDucky.java (lines 508-526)

```java
  508|     public boolean func_70601_bi() {
  509|         for (int k = -3; k < 3; ++k) {
  510|             for (int j = -3; j < 3; ++j) {
  511|                 for (int i = 0; i < 5; ++i) {
  512|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  513|                     if (bid != Blocks.field_150474_ac) continue;
  514|                     TileEntityMobSpawner tileentitymobspawner = null;
  515|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  516|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  517|                     if (s == null || !s.equals("Rubber Ducky")) continue;
  518|                     return true;
  519|                 }
  520|             }
  521|         }
  522|         if (this.field_70163_u < 50.0) {
  523|             return false;
  524|         }
  525|         return this.field_70170_p.func_72935_r();
  526|     }
```

## RubyBird.java (lines 29-31)

```java
   29|     public boolean func_70601_bi() {
   30|         return true;
   31|     }
```

## Scorpion.java (lines 281-299)

```java
  281|     public boolean func_70601_bi() {
  282|         for (int k = -3; k < 3; ++k) {
  283|             for (int j = -3; j < 3; ++j) {
  284|                 for (int i = 0; i < 5; ++i) {
  285|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  286|                     if (bid != Blocks.field_150474_ac) continue;
  287|                     TileEntityMobSpawner tileentitymobspawner = null;
  288|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  289|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  290|                     if (s == null || !s.equals("Scorpion")) continue;
  291|                     return true;
  292|                 }
  293|             }
  294|         }
  295|         if (!this.func_70814_o()) {
  296|             return false;
  297|         }
  298|         return !this.field_70170_p.func_72935_r() || !(this.field_70163_u > 50.0);
  299|     }
```

## SeaMonster.java (lines 544-570)

```java
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
```

## SeaViper.java (lines 561-584)

```java
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
```

## Skate.java (lines 318-329)

```java
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
```

## SpiderDriver.java (lines 177-184)

```java
  177|     public boolean func_70601_bi() {
  178|         SpiderRobot target = null;
  179|         target = (SpiderRobot)this.field_70170_p.func_72857_a(SpiderRobot.class, this.field_70121_D.func_72314_b(24.0, 12.0, 24.0), (Entity)this);
  180|         if (target != null) {
  181|             return true;
  182|         }
  183|         return super.func_70601_bi();
  184|     }
```

## SpitBug.java (lines 396-430)

```java
  396|     public boolean func_70601_bi() {
  397|         Block bid;
  398|         int i;
  399|         int j;
  400|         int k;
  401|         for (k = -3; k < 3; ++k) {
  402|             for (j = -3; j < 3; ++j) {
  403|                 for (i = 0; i < 5; ++i) {
  404|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  405|                     if (bid != Blocks.field_150474_ac) continue;
  406|                     TileEntityMobSpawner tileentitymobspawner = null;
  407|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  408|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  409|                     if (s == null || !s.equals("Spit Bug")) continue;
  410|                     return true;
  411|                 }
  412|             }
  413|         }
  414|         if (this.field_70170_p.func_72935_r() && this.field_70170_p.field_73012_v.nextInt(20) > 1) {
  415|             return false;
  416|         }
  417|         if (!this.func_70814_o()) {
  418|             return false;
  419|         }
  420|         for (k = -2; k < 2; ++k) {
  421|             for (j = -2; j < 2; ++j) {
  422|                 for (i = 1; i < 4; ++i) {
  423|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  424|                     if (bid == Blocks.field_150350_a) continue;
  425|                     return false;
  426|                 }
  427|             }
  428|         }
  429|         return true;
  430|     }
```

## Spyro.java (lines 407-412)

```java
  407|     public boolean func_70601_bi() {
  408|         if (!this.field_70170_p.func_72935_r()) {
  409|             return false;
  410|         }
  411|         return !(this.field_70163_u < 50.0);
  412|     }
```

## StinkBug.java (lines 136-151)

```java
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
```

## Stinky.java (lines 286-291)

```java
  286|     public boolean func_70601_bi() {
  287|         if (!this.field_70170_p.func_72935_r()) {
  288|             return false;
  289|         }
  290|         return this.findBuddies() <= 2;
  291|     }
```

## TerribleTerror.java (lines 193-214)

```java
  193|     public boolean func_70601_bi() {
  194|         for (int k = -2; k < 2; ++k) {
  195|             for (int j = -2; j < 2; ++j) {
  196|                 for (int i = 0; i < 5; ++i) {
  197|                     Block bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  198|                     if (bid != Blocks.field_150474_ac) continue;
  199|                     TileEntityMobSpawner tileentitymobspawner = null;
  200|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  201|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  202|                     if (s == null || !s.equals("Terrible Terror")) continue;
  203|                     return true;
  204|                 }
  205|             }
  206|         }
  207|         if (!this.func_70814_o()) {
  208|             return false;
  209|         }
  210|         if (this.field_70170_p.func_72935_r()) {
  211|             return false;
  212|         }
  213|         return this.field_70170_p.field_73011_w.field_76574_g == OreSpawnMain.DimensionID6 || !(this.field_70163_u > 40.0);
  214|     }
```

## TheKing.java (lines 847-849)

```java
  847|     public boolean func_70601_bi() {
  848|         return true;
  849|     }
```

## ThePrince.java (lines 381-383)

```java
  381|     public boolean func_70601_bi() {
  382|         return true;
  383|     }
```

## ThePrinceAdult.java (lines 541-543)

```java
  541|     public boolean func_70601_bi() {
  542|         return false;
  543|     }
```

## ThePrincess.java (lines 369-371)

```java
  369|     public boolean func_70601_bi() {
  370|         return true;
  371|     }
```

## ThePrinceTeen.java (lines 561-563)

```java
  561|     public boolean func_70601_bi() {
  562|         return false;
  563|     }
```

## TheQueen.java (lines 813-815)

```java
  813|     public boolean func_70601_bi() {
  814|         return true;
  815|     }
```

## TRex.java (lines 276-315)

```java
  276|     public boolean func_70601_bi() {
  277|         Block bid;
  278|         int i;
  279|         int j;
  280|         int k;
  281|         for (k = -3; k < 3; ++k) {
  282|             for (j = -3; j < 3; ++j) {
  283|                 for (i = 0; i < 5; ++i) {
  284|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  285|                     if (bid != Blocks.field_150474_ac) continue;
  286|                     TileEntityMobSpawner tileentitymobspawner = null;
  287|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  288|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  289|                     if (s == null || !s.equals("T. Rex")) continue;
  290|                     return true;
  291|                 }
  292|             }
  293|         }
  294|         if (!this.func_70814_o()) {
  295|             return false;
  296|         }
  297|         if (this.field_70163_u < 50.0) {
  298|             return false;
  299|         }
  300|         if (this.field_70170_p.func_72935_r()) {
  301|             return false;
  302|         }
  303|         for (k = -1; k <= 1; ++k) {
  304|             for (j = -1; j <= 1; ++j) {
  305|                 for (i = 1; i < 6; ++i) {
  306|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  307|                     if (bid == Blocks.field_150350_a) continue;
  308|                     return false;
  309|                 }
  310|             }
  311|         }
  312|         TRex target = null;
  313|         target = (TRex)this.field_70170_p.func_72857_a(TRex.class, this.field_70121_D.func_72314_b(24.0, 12.0, 24.0), (Entity)this);
  314|         return target == null;
  315|     }
```

## Triffid.java (lines 355-357)

```java
  355|     public boolean func_70601_bi() {
  356|         return true;
  357|     }
```

## TrooperBug.java (lines 536-570)

```java
  536|     public boolean func_70601_bi() {
  537|         Block bid;
  538|         int i;
  539|         int j;
  540|         int k;
  541|         for (k = -3; k < 3; ++k) {
  542|             for (j = -3; j < 3; ++j) {
  543|                 for (i = 0; i < 5; ++i) {
  544|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  545|                     if (bid != Blocks.field_150474_ac) continue;
  546|                     TileEntityMobSpawner tileentitymobspawner = null;
  547|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  548|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  549|                     if (s == null || !s.equals("Jumpy Bug")) continue;
  550|                     return true;
  551|                 }
  552|             }
  553|         }
  554|         if (!this.func_70814_o()) {
  555|             return false;
  556|         }
  557|         if (this.field_70170_p.func_72935_r() && this.field_70170_p.field_73012_v.nextInt(20) > 1) {
  558|             return false;
  559|         }
  560|         for (k = -2; k < 2; ++k) {
  561|             for (j = -2; j < 2; ++j) {
  562|                 for (i = 1; i < 5; ++i) {
  563|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  564|                     if (bid == Blocks.field_150350_a) continue;
  565|                     return false;
  566|                 }
  567|             }
  568|         }
  569|         return true;
  570|     }
```

## Tshirt.java (lines 93-103)

```java
   93|     public boolean func_70601_bi() {
   94|         if (!this.field_70170_p.func_72935_r()) {
   95|             return false;
   96|         }
   97|         if (this.field_70163_u < 50.0) {
   98|             return false;
   99|         }
  100|         Tshirt target = null;
  101|         target = (Tshirt)this.field_70170_p.func_72857_a(Tshirt.class, this.field_70121_D.func_72314_b(20.0, 8.0, 20.0), (Entity)this);
  102|         return target == null;
  103|     }
```

## Urchin.java (lines 298-332)

```java
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
```

## VelocityRaptor.java (lines 78-83)

```java
   78|     public boolean func_70601_bi() {
   79|         if (this.field_70163_u < 50.0) {
   80|             return false;
   81|         }
   82|         return this.field_70170_p.func_72935_r();
   83|     }
```

## Vortex.java (lines 240-284)

```java
  240|     public boolean func_70601_bi() {
  241|         Block bid;
  242|         int i;
  243|         int j;
  244|         int k;
  245|         for (k = -3; k < 3; ++k) {
  246|             for (j = -3; j < 3; ++j) {
  247|                 for (i = 0; i < 5; ++i) {
  248|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  249|                     if (bid != Blocks.field_150474_ac) continue;
  250|                     TileEntityMobSpawner tileentitymobspawner = null;
  251|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  252|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  253|                     if (s == null || !s.equals("Vortex")) continue;
  254|                     this.was_spawnered = 1;
  255|                     return true;
  256|                 }
  257|             }
  258|         }
  259|         for (k = -2; k <= 2; ++k) {
  260|             for (j = -2; j <= 2; ++j) {
  261|                 for (i = 1; i < 4; ++i) {
  262|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  263|                     if (bid == Blocks.field_150350_a) continue;
  264|                     return false;
  265|                 }
  266|             }
  267|         }
  268|         if (!this.func_70814_o()) {
  269|             return false;
  270|         }
  271|         if (this.field_70163_u < 50.0) {
  272|             return false;
  273|         }
  274|         long t = this.field_70170_p.func_72820_D();
  275|         if ((t %= 24000L) < 12000L) {
  276|             return false;
  277|         }
  278|         if (this.field_70170_p.field_73012_v.nextInt(2) != 1) {
  279|             return false;
  280|         }
  281|         Vortex target = null;
  282|         target = (Vortex)this.field_70170_p.func_72857_a(Vortex.class, this.field_70121_D.func_72314_b(20.0, 16.0, 20.0), (Entity)this);
  283|         return target == null;
  284|     }
```

## WaterDragon.java (lines 716-739)

```java
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
```

## Whale.java (lines 260-271)

```java
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
```

## WormLarge.java (lines 263-309)

```java
  263|     public boolean func_70601_bi() {
  264|         Block bid;
  265|         int i;
  266|         int j;
  267|         int k;
  268|         for (k = -3; k < 3; ++k) {
  269|             for (j = -3; j < 3; ++j) {
  270|                 for (i = 0; i < 5; ++i) {
  271|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  272|                     if (bid != Blocks.field_150474_ac) continue;
  273|                     TileEntityMobSpawner tileentitymobspawner = null;
  274|                     tileentitymobspawner = (TileEntityMobSpawner)this.field_70170_p.func_147438_o((int)this.field_70165_t + j, (int)this.field_70163_u + i, (int)this.field_70161_v + k);
  275|                     String s = tileentitymobspawner.func_145881_a().func_98276_e();
  276|                     if (s == null || !s.equals("Large Worm")) continue;
  277|                     this.wormsSpawned = 1;
  278|                     return true;
  279|                 }
  280|             }
  281|         }
  282|         if (this.field_70163_u < 50.0) {
  283|             return false;
  284|         }
  285|         WormLarge target = null;
  286|         target = (WormLarge)this.field_70170_p.func_72857_a(WormLarge.class, this.field_70121_D.func_72314_b(32.0, 8.0, 32.0), (Entity)this);
  287|         if (target != null) {
  288|             return false;
  289|         }
  290|         for (i = -6; i <= 6; ++i) {
  291|             for (j = -6; j <= 6; ++j) {
  292|                 for (k = -2; k >= -8; --k) {
  293|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + k, (int)this.field_70161_v + j);
  294|                     if (bid != Blocks.field_150350_a) continue;
  295|                     return false;
  296|                 }
  297|             }
  298|         }
  299|         for (i = -6; i <= 6; ++i) {
  300|             for (j = -6; j <= 6; ++j) {
  301|                 for (k = 2; k <= 8; ++k) {
  302|                     bid = this.field_70170_p.func_147439_a((int)this.field_70165_t + i, (int)this.field_70163_u + k, (int)this.field_70161_v + j);
  303|                     if (bid == Blocks.field_150350_a) continue;
  304|                     return false;
  305|                 }
  306|             }
  307|         }
  308|         return true;
  309|     }
```

## WormMedium.java (lines 240-242)

```java
  240|     public boolean func_70601_bi() {
  241|         return !this.field_70170_p.func_72935_r();
  242|     }
```

## WormSmall.java (lines 214-216)

```java
  214|     public boolean func_70601_bi() {
  215|         return !this.field_70170_p.func_72935_r();
  216|     }
```
