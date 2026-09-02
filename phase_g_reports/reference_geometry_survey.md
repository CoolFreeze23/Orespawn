# Reference-geometry survey — every port entity model against its 1.7.10 source (2026-09-02)

Tool: `tools/reference_geometry_leg.py` in `--survey` mode over a generated manifest of every port model
with a `createBodyLayer()` (90), paired by name with `reference_1_7_10_source/**/Model*.java` and with
the texture its renderer bakes; 87 pairs (skipped: ModelGodzillaHead, ModelKingHead, ModelQueenHead —
port-only head rigs with no 1.7.10 model). The port side is the compiled LayerDefinition dump written
by `G1ModelProbe vanilla`; the reference side is parsed from the decompiled constructor (all 109
reference model files parse). Renderer-level transforms and `setRotationAngles` motion are NOT compared.

Result: PASS 2, texture-mapping-only divergence 78,
geometry-moving divergence 7.

## The port-wide finding: mirror

82 of 87 port models set `.mirror()` on cubes whose 1.7.10 original set
`mirror = true` AFTER `addBox`. In 1.7.10, `ModelBox` reads the part's mirror flag in its constructor,
so a flag set afterwards never reaches the box: those originals rendered UNMIRRORED. The port's
`CubeListBuilder.mirror()` precedes `addBox` and is effective, so every such face samples its texture
flipped horizontally relative to 1.7.10 (invisible on symmetric texels, visible on asymmetric ones).
78 models differ in nothing else. AUDIT BUG-041 (report only; the fix is one flag per cube
plus a proof rerun, but it changes every proven species and the owner rules).

The same construction-time rule makes the exports' trailing `setTextureSize(64, 32)` inert; the parser
applies both rules, so no UV-normalisation divergence remains once mirror is accounted for.

Law-11 status of the 1.7.10 ordering claim: taken from the 1.7.10 `ModelRenderer`/`ModelBox` sources
(MCP), not verified against bytecode here — no 1.7.10 client jar is on this machine; downloading
Mojang's `1.7.10/client.jar` needs the owner's go.

## Geometry-moving models (7)

| model | matched | port parts | categories | reading |
|---|---|---|---|---|
| caterkiller | 31/31 | 80 | EXTRA_IN_PORT=49, MIRROR=31 | port carries 49 extra parts (seg1_1, seg1_2, ... duplicated segments): the port unrolled or rebuilt the segment chain |
| elevator | 5/5 | 5 | PIVOT=5, MIRROR=5 | all five shapes: reference rotation point y 0, port pivot y 24 (a 1.5-block lift at model level; whether RenderElevator compensated is a renderer question this leg does not see) |
| island | 0/3 | 6 | MISSING_IN_PORT=3, EXTRA_IN_PORT=6 | port model is not the original: three 8x8x8 shapes in 1.7.10 versus body/head/four legs in the port |
| islandtoo | 0/3 | 6 | MISSING_IN_PORT=3, EXTRA_IN_PORT=6 | same as Island (IslandToo drew ModelIsland in 1.7.10) |
| seaviper | 34/34 | 34 | PIVOT=34, ROTATION=13, MIRROR=34 | rebuilt: every pivot differs and 13 rotations are on different axes (reference X, port Y); the port renamed segments t10.. |
| skate | 0/3 | 4 | MISSING_IN_PORT=3, EXTRA_IN_PORT=4 | port model is not the original (3 reference parts unmatched, 4 port parts extra) |
| stinkbug | 50/50 | 50 | PIVOT=50, MIRROR=50 | all 50 pivots differ by the reference's trailing `+= 6.0f` on rotation-point Y (f6: reference 16 + 6 = 22, port 16): the port transcribed setRotationPoint and dropped the adjustments |

## Exact (2): coin, kyuubi

## Mirror-only (78)

alien, alosaurus, antrobot, attacksquid, bandp, baryonyx, basilisk, beaver, bee, brutalfly, camarasaurus, cassowary, cavefisher, cephadrome, chipmunk, cloudshark, cockateil, crab, creepinghorror, cryolophosaurus, dragon, dragonfly, dungeonbeast, easterbunny, emperorscorpion, enderknight, enderreaper, fairy, firefly, flounder, frog, gazelle, ghostskelly, giantrobot, godzilla, goldfish, hammerhead, herculesbeetle, hydrolisc, irukandji, kraken, leon, lizard, lurkingterror, mantis, molenoid, nastysaurus, ostrich, peacock, pitchblack, pointysaurus, purplepower, robot1, robot2, robot3, robot4, robot5, rockbase, scorpion, seamonster, spiderrobot, spitbug, spyro, stinky, theking, theprince, theprinceadult, theprincess, theprinceteen, trex, triffid, trooperbug, urchin, velocityraptor, waterdragon, whale, wormlarge, wormmedium

## Cross-check: the independent reader survey (26 agents, 24 sampled pairs)

Run before the mechanical leg existed in this form, it reached the same conclusions from the other
direction: 21 of 24 sampled pairs DIVERGE on geometry, every one for the mirror flag, with all numeric
fields byte-identical; its population scan counted 3,542 `mirror = true` stores in the reference, all
after `addBox` (zero live mirrors), and 99 of 110 port model files with `.mirror()` before `addBox`.
Its three sampled MATCHES (EnderReaper, LurkingTerror, SpitBug) compared the mirror flag by intent, not
by effect; the leg reports them as mirror-only. Its three prescribed parser fixes (record texture size at
addBox time, report mirror as its own class, pair twins by placement) are the three made in this commit.
Its box-count check flagged Ghost (3 -> 2), Mosquito (5 -> 4) and Tshirt (2 -> 3, sheet 512x256 ->
320x160) as re-authored; those three were outside the 87 pairs here (renderer texture or class-form not
matched by the generator) and join ENT-S-091's list for a read.

## Renderers: shadow and world scale (mechanical sweep, `renderer_sweep.json`)

1.7.10 registrations read `new RenderX(new ModelX(f), par2, par3)`; every `RenderX` passes `par2 * par3`
to RenderLiving as the shadow radius and scales the pose by `par3` in `preRenderCallback`. 109
registrations parsed; 2 have no port renderer (RubyBird, and one alias). Shadow radius differs in
85 of the 97 resolvable pairs (matches: 12; unresolved literals: EntityAnt, EntityRedAnt, EntityRainbowAnt, EntityUnstableAnt, Cryolophosaurus, Camarasaurus, Termite, ThePrince, Nastysaurus, ThePrincess).
World scale differs in 48 (of which 7 carry a non-literal scale expression the sweep
could not evaluate: Basilisk, Camarasaurus, Chipmunk, Godzilla, Beaver, TheKing, PurplePower). The readers' independent scan put the same
numbers at 84/100 and 43-46/100. Shadow is cosmetic; world scale changes the mob's rendered size and is
a visible parity divergence (largest, reference -> port): Brutalfly 9 -> 1.0 (no override); Kraken 1 -> 3.0; SeaMonster 1 -> 3.0; Irukandji 0.25 -> 1.0 (no override); Tshirt 0.33 -> 1.0 (no override); Fairy 0.35 -> 1.0 (no override); EntityMosquito 0.5 -> 1.0 (no override); Dragonfly 1.5 -> 1.0 (no override); EmperorScorpion 1.5 -> 1.0 (no override); Robot3 0.5 -> 1.0 (no override); Peacock 1 -> 0.5; EasterBunny 1 -> 0.5.
AUDIT ENT-S-092.

## Motion (readers' structural scan plus 24 sampled reads; not mechanically comparable)

99 of 109 reference models animate, all inside `render` (none in `setRotationAngles`); 47 rewrite
rotation points every frame, 15 use per-entity `RenderInfo` or the world RNG, 17 issue GL calls inside
render, 5 pose-and-draw in loops. Sampled motion: 9 of 24 DIVERGE - Alien (per-entity selector state
collapsed into model-instance fields shared by every alien on screen; RNG source changed), Cephadrome
(`rf1` low-pass filter re-zeroed every frame; yaw source swapped body -> head), Gazelle (one state
predicate), Dragon/BabyDragon, RubberDucky (head/beak damping 0.45 lost), Mosquito (hand-authored rig),
PurplePower (random-per-frame GL pose), SeaViper, LurkingTerror. Structural floor: 14 port models hold
selector/filter state as model-instance fields (EmperorScorpion, GhostSkelly, Leon, LurkingTerror, Alien,
CaveFisher, Cephadrome, Dragon, DungeonBeast, Nastysaurus, PitchBlack, ThePrinceTeen, Ostrich, Scorpion) -
the defect class Slice 4b avoided for Robot2/Robot3 by keeping `RenderInfo` per entity; only Kraken,
Robot2, Robot3 and Rotator kept it. AUDIT ENT-S-093. Honest range for motion divergence: 15-45 of the 99
animated models; the 14 above first.

## Feasibility of a standing reference-geometry leg

Feasible and already wired: any manifest model may declare `reference_source`; `s4ReferenceGeometry`
runs before `s4Parity`, which refuses the model without a PASS and checks the leg's JSON into the proof
(Coin is the first). For the mirror-only 78 the leg would go green the moment the port flags are
corrected (or the manifest records an owner ruling that the port keeps the flipped mapping). The seven
geometry-moving models need reads and rulings first. Motion (`setRotationAngles`) is not mechanically
comparable: the 1.7.10 classes cannot be compiled or executed here; a formula-by-formula read per
model is the only route, and the survey workflow's sampled reads are the start of that.
