# ENT-S-092 recheck list — largest visible world-scale changes (2026-09-03)

Renderer scale restored to the 1.7.10 `preRenderCallback` value (`par3` of the registration). Ratio = new size / old
port size, linear. Rows marked `batch 1b` are the five bosses whose part entities or MHLib bones follow the rendered
scale; they are NOT landed yet (consequences presented separately). Everything else landed in batch 1a.

| entity | old port scale | 1.7.10 scale | ratio | shadow old -> new | batch |
|---|---|---|---|---|---|
| Brutalfly | 1 | 9 | x9 | 1.5 -> 6.75 | 1a |
| Irukandji | 1 | 0.25 | x0.25 | 0.2 -> 0.025 | 1a |
| Kraken | 3 | 1 | x0.333 | 3 -> 1 | 1b (held) |
| SeaMonster | 3 | 1 | x0.333 | 1.5 -> 1 | 1b (held) |
| Fairy | 1 | 0.35 | x0.35 | 0.15 -> 0.035 | 1a |
| TheKing | 1 | 2.1 | x2.1 | 5 -> 3.99 | 1b (held) |
| Robot3 | 1 | 0.5 | x0.5 | 2 -> 0.5 | 1a |
| TheQueen | 1 | 2 | x2 | 3 -> 3.8 | 1b (held) |
| Cricket | 1 | 0.5 | x0.5 | 0.1 -> 0.075 | 1a |
| Hydrolisc | 1 | 0.65 | x0.65 | 0.25 -> 0.4225 | 1a |
| Godzilla | 3 | 2 | x0.667 | 5 -> 2 | 1b (held) |
| Dragonfly | 1 | 1.5 | x1.5 | 0.5 -> 0.45 | 1a |
| EmperorScorpion | 1 | 1.5 | x1.5 | 1.2 -> 1.425 | 1a |
| ThePrinceTeen | 0.85 | 1.25 | x1.47 | 0.9 -> 1.25 | 1a |
| Firefly | 1 | 0.75 | x0.75 | 0.1 -> 0.15 | 1a |
| VelocityRaptor | 1 | 0.75 | x0.75 | 0.5 -> 0.4125 | 1a |
| Spyro | 1 | 0.75 | x0.75 | 0.5 -> 0.4875 | 1a |
| Cockateil | 1 | 0.75 | x0.75 | 0.3 -> 0.225 | 1a |
| RubyBird | 1 | 0.75 | x0.75 | 0.3 -> 0.225 | 1a |
| Scorpion | 1 | 0.75 | x0.75 | 0.3 -> 0.2625 | 1a |
| CaveFisher | 1 | 0.75 | x0.75 | 0.5 -> 0.2625 | 1a |
| SpitBug | 1 | 0.75 | x0.75 | 0.6 -> 0.4125 | 1a |
| CreepingHorror | 1 | 0.75 | x0.75 | 0.3 -> 0.3375 | 1a |
| TerribleTerror | 1 | 0.75 | x0.75 | 0.5 -> 0.3375 | 1a |
| Beaver | 1 | 0.75 | x0.75 | 0.5 -> 0.1125 | 1a |
| Rat | 1 | 0.75 | x0.75 | 0.25 -> 0.075 | 1a |
| RubberDucky | 1 | 0.75 | x0.75 | 0.25 -> 0.1125 | 1a |
| Urchin | 1 | 1.25 | x1.25 | 0.3 -> 0.4375 | 1a |
| CaterKiller | 1 | 1.25 | x1.25 | 1.2 -> 1.25 | 1a |
| Hammerhead | 2 | 2.5 | x1.25 | 1 -> 2.5 | 1a |
| LurkingTerror | 1 | 0.85 | x0.85 | 1 -> 0.3825 | 1a |
| GammaMetroid | 1 | 0.9 | x0.9 | 1 -> 0.675 | 1a |
| AttackSquid | 1 | 0.9 | x0.9 | 0.5 -> 0.225 | 1a |
| Chipmunk | 1 | 0.9 | x0.9 | 0.3 -> 0.135 | 1a |
| Bee | 1 | 1.1 | x1.1 | 0.5 -> 0.99 | 1a |
| Alien | 1 | 1.1 | x1.1 | 0.7 -> 0.385 | 1a |
| TrooperBug | 1 | 1.1 | x1.1 | 1 -> 1.045 | 1a |
| Mantis | 1 | 1.1 | x1.1 | 0.8 -> 0.99 | 1a |
| HerculesBeetle | 1 | 1.1 | x1.1 | 1 -> 1.089 | 1a |
| GhostSkelly | 1 | 1.05 | x1.05 | 0.5 -> 0 | 1a |
