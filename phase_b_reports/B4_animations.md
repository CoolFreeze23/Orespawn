# Phase B4 — Animation frequency mistranslation fix (wingspeed → limbSwingAmount)

**Systemic bug** (audit: `audit_sections/08_animations_events_gui.md`): the original 1.7.10 models multiply trig
FREQUENCY by `this.wingspeed`, a constructor constant set per entity in `ClientProxyOreSpawn.java`. The port
multiplied frequency by `limbSwingAmount` (runtime movement, ~0 at idle), freezing idle/hover animations and making
frequency jitter with speed. Original amplitude terms that use `f1` (limbSwingAmount) were left as-is.

**Fix applied to every affected model**: added a `wingspeed` constant (value = original constructor argument from
`ClientProxyOreSpawn.java`, cited per file) and replaced the frequency factor `limbSwingAmount` with
`this.wingspeed`, preserving every coefficient exactly. `ButterflyModel` (shared by several entities with different
original wingspeeds) instead takes `wingspeed` as a constructor parameter supplied by each renderer.

All searches for remaining misuse (`ageInTicks * … * limbSwingAmount` and variants: bare `ageInTicks *
limbSwingAmount`, trailing-coefficient `ageInTicks * limbSwingAmount * k`, phase-shifted `(ageInTicks + k) * k *
limbSwingAmount`, variable multipliers `legspeed`/`mouthspeed`/`tailspeed`/`fanspeed`, `/ pscale`, `% 2π`)
return zero hits after the fix.

## Fixed files

| Port file | # expressions fixed | Orig file:line(s) (wingspeed field, ctor assign) | wingspeed value | Notes |
|---|---|---|---|---|
| BeeModel.java | 8 | ModelBee.java:14,40 | 2.0f (proxy:425) | |
| BrutalflyModel.java | 1 | ModelBrutalfly.java:27,32 | 0.2f (proxy:507) | |
| ButterflyModel.java | 1 | ModelButterfly.java:23,28,96 | ctor param (see below) | shared model; param added |
| DragonflyModel.java | 2 | ModelDragonfly.java:13,42 | 2.0f (proxy:424) | |
| EmperorScorpionModel.java | 12 | ModelEmperorScorpion.java:15,96 | 0.22f (proxy:426) | incl. `(ageInTicks + k)` phase forms |
| FairyModel.java | 8 | ModelFairy.java:16,34 | 1.5f (proxy:477) | bare and trailing-coefficient forms (`f2 * wingspeed [* k]`) |
| FireflyModel.java | 2 | ModelFirefly.java:16,33 | 2.5f (proxy:406) | bare form `f2 * wingspeed` |
| HerculesBeetleModel.java | 3 | ModelHerculesBeetle.java:14,54 | 1.0f (proxy:489) | trailing-coefficient form `f2 * wingspeed * 0.45f` |
| HydroliscModel.java | 4 | ModelHydrolisc.java:14,57 | 0.65f (proxy:422) | `* hf` post-factor preserved |
| KyuubiModel.java | 31 | ModelKyuubi.java:15,60 | 0.5f (proxy:432) | |
| LeonModel.java | 8 | ModelLeon.java:15,116 | 0.22f (proxy:500) | `* spd` / `* amp` post-factors preserved |
| LizardModel.java | 3 | ModelLizard.java:15,89 | 0.65f (proxy:445) | |
| LurkingTerrorModel.java | 11 | ModelLurkingTerror.java:15 | 1.0f (proxy:461; orig ctor takes no wingspeed arg — field default) | `legspeed`/`mouthspeed` multipliers preserved |
| MantisModel.java | 4 | ModelMantis.java:14,53 | 2.0f (proxy:488) | |
| ModelAlien.java | 24 | ModelAlien.java:15,73 | 0.22f (proxy:435) | bare, `fanspeed`, and `(ageInTicks + 0.2f)` forms |
| ModelAttackSquid.java | 20 | ModelAttackSquid.java:14,26 | 1.0f (proxy:437) | amplitude `* f1` terms preserved |
| ModelBandP.java | 4 | ModelBandP.java:14,24 | 0.4f (proxy:504) | |
| ModelCaveFisher.java | 5 | ModelCaveFisher.java:15,93 | 0.62f (proxy:434) | incl. `(ageInTicks + 0.1f)` form |
| ModelCloudShark.java | 4 | ModelCloudShark.java:13,24 | 1.0f (proxy:471) | |
| ModelCockateil.java | 5 | ModelCockateil.java:13,32 | 1.0f (proxy:430-431) | Cockateil and RubyBird both use 1.0f |
| ModelDungeonBeast.java | 18 | ModelDungeonBeast.java:16,83 | 0.62f (proxy:481) | |
| ModelEasterBunny.java | 3 | ModelEasterBunny.java:14,30 | 0.55f (proxy:498) | |
| ModelEnderKnight.java | 3 | ModelEnderKnight.java:54,57 | 0.21f (proxy:473) | |
| ModelEnderReaper.java | 4 | ModelEnderReaper.java:80,83 | 0.23f (proxy:474) | |
| ModelGoldFish.java | 6 | ModelGoldFish.java:13,32 | 0.7f (proxy:470) | |
| ModelHammerhead.java | 4 | ModelHammerhead.java:14,54 | 0.33f (proxy:501) | |
| ModelPeacock.java | 1 | ModelPeacock.java:14,33 | 0.75f (proxy:478) | |
| ModelPitchBlack.java | 15 | ModelPitchBlack.java:15,119 | 0.65f (proxy:460) | `/ pscale`, `tailspeed`, `% 2π` forms preserved |
| ModelSeaMonster.java | 7 | ModelSeaMonster.java:14,40 | 0.5f (proxy:496) | |
| ModelSeaViper.java | 7 | ModelSeaViper.java:14,51 | 0.5f (proxy:497) | pre-existing field had wrong value 1.0f → corrected to 0.5f |
| ModelUrchin.java | 21 | ModelUrchin.java:14,34 (expr 155-194) | 1.0f (proxy:487) | audit special case: frequency now `wingspeed`, amplitude `* limbSwingAmount` kept (orig `* f1`) — matches orig exactly |
| ModelWaterDragon.java | 10 | ModelWaterDragon.java:14,41 | 0.5f (proxy:436) | |
| MolenoidModel.java | 4 | ModelMolenoid.java:14,54 | 0.5f (proxy:495) | |
| OstrichModel.java | 3 | ModelOstrich.java:16,57 | 0.65f (proxy:450) | |
| ScorpionModel.java | 6 | ModelScorpion.java:15,40 | 0.62f (proxy:433) | incl. `(ageInTicks + 0.1f)` form |
| SpitBugModel.java | 8 | ModelSpitBug.java:14,110 | 0.55f (proxy:452) | |
| StinkBugModel.java | 8 | ModelStinkBug.java:13,66 | 0.75f (proxy:453) | orig uses `ff2`/`ff1` param names |
| TriffidModel.java | 2 | ModelTriffid.java:15,196 | 1.0f (proxy:459) | |
| TrooperBugModel.java | 16 | ModelTrooperBug.java:14,151 | 0.22f (proxy:451) | |
| TshirtModel.java | 1 | ModelTshirt.java:13,18 | 0.22f (proxy:418) | |
| VelocityRaptorModel.java | 4 | ModelVelocityRaptor.java:15,52 | 1.25f (proxy:423) | `* hf` post-factor preserved |

**Totals: 41 files, 342 expressions fixed** (`proxy:` = orig `ClientProxyOreSpawn.java` line).
Including the follow-up wingspeed-value corrections below: **53 model files + 6 renderer files touched in total.**

## Files where wingspeed ≠ 1.0 supplied via constructor

`ButterflyModel` is the only model shared by entities with differing original wingspeeds, so it now takes
`wingspeed` as a constructor parameter (field documented citing orig ModelButterfly.java:23,28). Suppliers:

| Renderer | Value passed | Source |
|---|---|---|
| ButterflyRenderer.java:18 | 1.0f | orig ClientProxyOreSpawn.java:405 `new ModelButterfly(1.0f)` |
| LunaMothRenderer.java:18 | 0.75f | orig ClientProxyOreSpawn.java:407 `new ModelButterfly(0.75f)` |
| MothraRenderer.java:20 | 0.2f | orig ClientProxyOreSpawn.java:411 `new ModelButterfly(0.2f)` |
| LeonopteryxRenderer.java:34 | 1.0f | no orig RenderButterfly registration for this entity — orig field default (ModelButterfly.java:23) |
| VampireButterflyRenderer.java:29 | 1.0f | no orig RenderButterfly registration for this entity — orig field default (ModelButterfly.java:23) |

All other fixed models are instantiated by exactly one renderer (or multiple renderers with the same original
value — Cockateil/RubyBird both 1.0f, orig proxy:430-431), so the constant is a `private final float wingspeed`
field in the model with a citation comment.

## Mothra scale fix

Verified: orig `RenderButterfly.java:24-26` stores the third constructor arg as `scale` and applies it via
`glScalef` (RenderButterfly.java:42); orig registration `ClientProxyOreSpawn.java:411` passes **10.0f** for Mothra.
Port `MothraRenderer.scale()` used 5.0f → corrected to **10.0f** (MothraRenderer.java:23-27, cited).

## LunaMoth scale fix — FIXED (follow-up)

Orig LunaMoth registration (proxy:407) passes scale **1.5f** via `RenderButterfly` (stored RenderButterfly.java:26,
applied via glScalef RenderButterfly.java:42). The port `LunaMothRenderer` had no `scale()` override (rendered at
1.0). Added `scale()` override mirroring `MothraRenderer` — `LunaMothRenderer.java:22-26`, cited.

## UNVERIFIED

None — every replaced expression was matched against the corresponding original model source, with identical
coefficients and identical placement of the `wingspeed` (frequency) and `f1`/`limbSwingAmount` (amplitude) factors.

## Wingspeed-value corrections in already-structured models — FIXED (follow-up)

14 port models already used `this.wingspeed` as the frequency source (correct structure, not flagged by the
audit's mistranslation finding), but 12 of them hardcoded `wingspeed = 1.0` where the original registration passes
a different value. Each was verified against the orig model ctor (`this.wingspeed = f1`) and the orig
`ClientProxyOreSpawn.java` registration, then corrected with a citation comment (no expression changes needed):

| Port file | Value 1.0 → | Orig model file:line(s) | Orig ClientProxyOreSpawn.java |
|---|---|---|---|
| ModelBasilisk.java | 0.3F | ModelBasilisk.java:14,38 | :420 |
| ModelBaryonyx.java | 0.25f | ModelBaryonyx.java:14,69 | :428 |
| ModelCamarasaurus.java | 0.65f | ModelCamarasaurus.java:14,38 | :421 |
| ModelCassowary.java | 0.55f | ModelCassowary.java:14,29 | :469 |
| ModelCephadrome.java | 0.55f | ModelCephadrome.java:15,68 | :446 |
| ModelCryolophosaurus.java | 0.75f | ModelCryolophosaurus.java:13,38 | :419 |
| ModelGazelle.java | 0.65F | ModelGazelle.java:14,51 | :449 |
| ModelNastysaurus.java | 0.65f | ModelNastysaurus.java:15,77 | :508 |
| ModelThePrince.java | 0.65f | ModelThePrince.java:15,53 | :494 |
| ModelThePrinceAdult.java | 0.65F | ModelThePrinceAdult.java:15,137 | :513 |
| ModelThePrinceTeen.java | 0.65f | ModelThePrinceTeen.java:16,90 | :503 |
| ModelThePrincess.java | 0.65f | ModelThePrincess.java:16,56 | :511 |

(ModelPointysaurus and ModelFrog already matched their original value of 1.0f — unchanged.)

## Build status

`.\gradlew.bat compileJava` — **BUILD SUCCESSFUL** after both the initial B4 pass and the follow-up pass (only
pre-existing deprecation warnings in `ModEntityAttributes.java` / `OreSpawnClient.java` / `ModItems.java`,
unrelated to B4). The `FileAlreadyExistsException` on `build\reports\problems\problems-report.html` was hit once;
the locked directory was renamed aside (`build\reports\problems_locked`) and the build then succeeded.
