# Renderer scale and shadow findings (ENT-S-092), 2026-09-02

Method, as the owner required, verified on the largest discrepancies against every scale path: renderer
`scale()`/`render()`/`poseStack.scale`, `Attributes.SCALE` (suppliers and modifiers, repo-wide), EntityType
`.sized`/`.scale`, `getScale`/`getAgeScale`/baby branches, model-level box sizes versus the reference
`addBox` sizes, MHLib profiles, config gates and mixins; each verdict refuted once (twice for the first
fourteen). 49 renderers verified by reading (63 refutations: 61 failed, 2 corrected citations or a label).
Reference: 1.7.10 `RenderX(model, par2, par3)` passes `par2 * par3` to RenderLiving as the shadow radius and
scales the pose by `par3` in `preRenderCallback`.

Verified: world scale DIVERGES in 44 of 49; matches via another path or outright in 5
(Basilisk, Camarasaurus, EasterBunny, Mothra, Peacock); shadow DIVERGES in 48 of 49 (Camarasaurus matches).
The sweep's shadow column held on every verified row; its scale column had 5 false positives (baby-scale
branches, a shared renderer, a compensating hook), so the remaining scale flags are read before they count.

## Verified renderers

| entity | scale (ref -> port) | verdict | shadow (ref -> port) | verdict |
|---|---|---|---|---|
| Alien | 1.1 -> 1 | DIVERGES | 0.385 -> 0.7 | DIVERGES |
| AttackSquid | 0.9 -> 1 | DIVERGES | 0.225 -> 0.5 | DIVERGES |
| Basilisk | 1.25 -> 1.25 | MATCHES | 0.625 -> 1.5 | DIVERGES |
| Beaver | 0.75 -> 1 | DIVERGES | 0.1125 -> 0.5 | DIVERGES |
| Bee | 1.1 -> 1 | DIVERGES | 0.99 -> 0.5 | DIVERGES |
| Brutalfly | 9 -> 1 | DIVERGES | 6.75 -> 1.5 | DIVERGES |
| Camarasaurus | 0.65 -> 0.65 | MATCHES_VIA_OTHER_PATH | 0.4225 -> 0.4225 | MATCHES |
| CaterKiller | 1.25 -> 1 | DIVERGES | 1.25 -> 1.2 | DIVERGES |
| CaveFisher | 0.75 -> 1 | DIVERGES | 0.2625 -> 0.5 | DIVERGES |
| Chipmunk | 0.9 -> 1 | DIVERGES | 0.135 -> 0.3 | DIVERGES |
| Cockateil | 0.75 -> 1 | DIVERGES | 0.225 -> 0.3 | DIVERGES |
| CreepingHorror | 0.75 -> 1 | DIVERGES | 0.3375 -> 0.3 | DIVERGES |
| Cricket | 0.5 -> 1 | DIVERGES | 0.075 -> 0.1 | DIVERGES |
| Dragonfly | 1.5 -> 1 | DIVERGES | 0.45 -> 0.5 | DIVERGES |
| EasterBunny | 1 -> 1 | MATCHES_VIA_OTHER_PATH | 0.5 -> 0.3 | DIVERGES |
| EmperorScorpion | 1.5 -> 1 | DIVERGES | 1.425 -> 1.2 | DIVERGES |
| EntityMosquito | 0.5 -> 1 | DIVERGES | 0.15 -> 0.05 | DIVERGES |
| Fairy | 0.35 -> 1 | DIVERGES | 0.035 -> 0.15 | DIVERGES |
| Firefly | 0.75 -> 1 | DIVERGES | 0.15 -> 0.1 | DIVERGES |
| GammaMetroid | 0.9 -> 1 | DIVERGES | 0.675 -> 1 | DIVERGES |
| Ghost | 0.65 -> 1 | DIVERGES | 0 -> 0.3 | DIVERGES |
| GhostSkelly | 1.05 -> 1 | DIVERGES | 0 -> 0.5 | DIVERGES |
| Godzilla | 2 -> 3 | DIVERGES | 2 -> 5 | DIVERGES |
| Hammerhead | 2.5 -> 2 | DIVERGES | 2.5 -> 1 | DIVERGES |
| HerculesBeetle | 1.1 -> 1 | DIVERGES | 1.089 -> 1 | DIVERGES |
| Hydrolisc | 0.65 -> 1 | DIVERGES | 0.4225 -> 0.25 | DIVERGES |
| Irukandji | 0.25 -> 1 | DIVERGES | 0.025 -> 0.2 | DIVERGES |
| Kraken | 1 -> 3 | DIVERGES | 1 -> 3 | DIVERGES |
| LurkingTerror | 0.85 -> 1 | DIVERGES | 0.3825 -> 1 | DIVERGES |
| Mantis | 1.1 -> 1 | DIVERGES | 0.99 -> 0.8 | DIVERGES |
| Mothra | 10 -> 10 | MATCHES_VIA_OTHER_PATH | 7.5 -> 1.5 | DIVERGES |
| Peacock | 1 -> 1 | MATCHES_VIA_OTHER_PATH | 0.25 -> 0.4 | DIVERGES |
| PurplePower | 1 -> 0.55 | DIVERGES | 0.825 -> 0.5 | DIVERGES |
| Rat | 0.75 -> 1 | DIVERGES | 0.075 -> 0.25 | DIVERGES |
| Robot3 | 0.5 -> 1 | DIVERGES | 0.5 -> 2 | DIVERGES |
| RubberDucky | 0.75 -> 1 | DIVERGES | 0.1125 -> 0.25 | DIVERGES |
| Scorpion | 0.75 -> 1 | DIVERGES | 0.2625 -> 0.3 | DIVERGES |
| SeaMonster | 1 -> 3 | DIVERGES | 1 -> 1.5 | DIVERGES |
| Skate | 0.75 -> 1 | DIVERGES | 0.075 -> 0.4 | DIVERGES |
| SpitBug | 0.75 -> 1 | DIVERGES | 0.4125 -> 0.6 | DIVERGES |
| Spyro | 0.75 -> 1 | DIVERGES | 0.4875 -> 0.5 | DIVERGES |
| StinkBug | 0.85 -> 1 | DIVERGES | 0.2975 -> 0.2 | DIVERGES |
| TerribleTerror | 0.75 -> 1 | DIVERGES | 0.3375 -> 0.5 | DIVERGES |
| TheKing | 2.1 -> 1 | DIVERGES | 3.99 -> 5 | DIVERGES |
| ThePrinceTeen | 1.25 -> 0.85 | DIVERGES | 1.25 -> 0.9 | DIVERGES |
| TrooperBug | 1.1 -> 1 | DIVERGES | 1.045 -> 1 | DIVERGES |
| Tshirt | 0.33 -> 1 | DIVERGES | 0.33 -> 2 | DIVERGES |
| Urchin | 1.25 -> 1 | DIVERGES | 0.4375 -> 0.3 | DIVERGES |
| VelocityRaptor | 0.75 -> 1 | DIVERGES | 0.4125 -> 0.5 | DIVERGES |

## Shadow-only by the sweep, not yet read (46)

The sweep's shadow verdict is `par2 * par3` versus the port's `super(context, model, <literal>)`; it held on
49 of 49 verified rows. These carry a shadow difference and no scale flag:

EntityButterfly (0.3 -> 0.15), EntityLunaMoth (0.6 -> 0.15), EntityAnt (0.025 -> None), EntityRedAnt (0.0525 -> None), EntityRainbowAnt (0.025 -> None), EntityUnstableAnt (0.025 -> None), TRex (1.2 -> 1.0), Cryolophosaurus (0.375 -> None), Kyuubi (0.1 -> 0.5), WaterDragon (0.935 -> 0.85), Robot1 (0.3 -> 0.5), Robot2 (1 -> 1.5), Robot4 (1 -> 1.5), Lizard (0.75 -> 0.5), Dragon (1.25 -> 1.5), Gazelle (0.45 -> 0.5), Ostrich (0.55 -> 0.5), CliffRacer (0.3 -> 0.5), Triffid (0.3 -> 1.0), PitchBlack (1.25 -> 2.0), WormSmall (0.1 -> 0.25), WormMedium (0.25 -> 0.5), WormLarge (0.9 -> 1.0), GoldFish (0.2 -> 0.3), LeafMonster (0.65 -> 0.5), EnderKnight (0.3 -> 0.5), EnderReaper (0.2 -> 0.5), Termite (0.0525 -> None), Rotator (0.1 -> 0.5), Vortex (0.1 -> 1.5), DungeonBeast (0.25 -> 0.5), Flounder (0.1 -> 0.3), Whale (0.1 -> 1.5), Stinky (0.75 -> 0.5), ThePrince (0.5625 -> None), Molenoid (1 -> 2.0), SeaViper (1 -> 0.4), BandP (1 -> 0.5), RockBase (0 -> 0.3), Nastysaurus (1.5 -> None), ThePrincess (0.49 -> None), Frog (0.35 -> 0.3), SpiderRobot (0.99 -> 3.0), GiantRobot (0.99 -> 4.0), AntRobot (0.99 -> 2.0), Crab (0.99 -> 0.8)

## Unresolved by the sweep (9): EntityAnt, EntityRedAnt, EntityRainbowAnt, EntityUnstableAnt, Cryolophosaurus, Termite, ThePrince, Nastysaurus, ThePrincess

Their port shadow is an expression the sweep does not evaluate (shared AntRenderer, computed constants); read
them with the fix slice.

## Per-renderer fix and pin

House style is `CoinRenderer`: `public static final float SCALE`/`SHADOW` constants with the
ClientProxyOreSpawn citation, `super(context, model, SHADOW)`, and a `scale()` override applying `SCALE`
(the 1.7.10 `preRenderCallback` slot). Pinning test: a gametest asserting the constants (compile-time
literals, no client class load) and, for the shadow, the renderer's `shadowRadius` through the same
constant. The verified findings' full text (exact lines, visible effect, fix) is in
`phase_g_reports/renderer_findings.json`.
