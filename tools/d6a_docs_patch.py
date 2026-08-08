"""D6a docs patch: PARITY PN-017/018/019, FIX_LOG D6a section, TESTING_CHECKLIST D6a."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

pn = ROOT / "PARITY_NOTES.md"
pn.write_text(pn.read_text(encoding="utf-8") + """
## PN-017 — End-dimension placement mapping (EnderCastle + Hospital, Phase D6a)

- **Original:** both structures fire dimension-wide in the End (OSW:219-241 —
dimension-id gate, biome never inspected; 1.7.10 had one End biome and no outer
islands), with block scans: 3 attempts, air-on-end-stone in Y 90→11
(EnderCastle adds a 30×30 air plane at +8, Hospital a 12×12 at +4).
- **Port:** biome tag `#minecraft:is_end` (the faithful-to-CODE choice — the
dimension-wide gate + end-stone scan self-selects island terrain, which now
includes the 1.9+ outer-ring biomes; outer islands top out ~Y60-75, inside the
scan window) + the END_SURFACE placement mode (noise-heightmap anchor; void
columns rejected; clearance planes approximated by footprint corner/centre
surface sampling ≤ anchor+3 — conservative vs the castle's looser +8 plane).
Frequencies map to structure-set spacing (castle 14/7 ≈ 1/200; hospital 10/5 ≈
1/100); the End path's "recently_placed never set" quirk maps to independent
sets. Player-visible: castles/hospitals can also appear on outer End islands —
terrain that did not exist in 1.7.10; central-island behavior matches.

## PN-018 — Inca Pyramid ramps: pre-build terrain reads (Phase D6a)

- **Original:** ramp rails/treads/support pillars condition every write on
world air-reads (GD:3791-3873): pillars stop at terrain, treads skip occupied
cells, and docking into the pyramid's own steps is a read-after-write.
- **Port:** own-structure reads reproduced exactly via an in-memory write-set
model; pre-build terrain is unreadable under chunk stitching, so unrecorded
cells read as air — support pillars always fill to relative y 0 (replacing the
surface grass under ~200 outside-footprint ramp-lane cells even on the flat
Islands plane, where the original stopped atop the grass) and treads place
unconditionally. Worst case on the live Dungeon Spawner Block path over rough
terrain: ramps punch through instead of yielding. Sanctioned by the spec
(section 10); the flat-plane grass delta is cosmetic (under the ramp lanes).

## PN-019 — Monster Island ocean-biome mapping (Phase D6a)

- **Original:** corner-biome name check EXACTLY "Ocean" (OSW:1402-1403) —
excludes Deep Ocean, Frozen Ocean, beaches; 1.7.10 had no other ocean variants.
- **Port:** biome filter `minecraft:ocean` only. Modern lukewarm/cold/warm
variants (no 1.7.10 counterpart) are excluded — the narrow faithful reading;
the deep/frozen exclusions carry over exactly. Set 42/21 ≈ the 1/6 × 1/300 odds.
""", encoding="utf-8")
print("PARITY_NOTES: PN-017/018/019 appended")

fl = ROOT / "FIX_LOG.md"
t = fl.read_text(encoding="utf-8")
anchor = "## Phase D — slice D5: representative structures + SpawnOres pool (2026-08-08)"
assert anchor in t
d6a = """## Phase D — slice D6a: strong-model structures (2026-08-08)

- **Specs:** `phase_d_reports/d6_extraction/` — six extraction/audit specs, each
  independently verified against the originals before implementation.
- **WGEN-042 — PARTIAL (advanced):** EnderCastle (GD:3207-3623; End
  END_SURFACE placement + Islands i==7 via the new per-JSON placement_mode
  override; DSB 27; 8-entry loot 270), IncaPyramid (GD:3735-4042; write-set
  model for ramp self-reads, PN-018 deviation; DSB 29; 14-entry loot 480),
  KyuubiDungeon (GD:1095-1361; Mining set 26/13; DSB 7; five loot tables,
  totals 110/130, four blaze fill formulas kept distinct), EnderDragonHospital
  (GD:2815-2991; 4 End Crystals via the new spawnEntity helper — NO dragon
  exists in the original; End-only 10/5; DSB 24), MonsterIsland (GD:5170-5240;
  overworld OCEAN_SURFACE, minecraft:ocean only, 42/21; DSB 37), FairyTree/
  FairyCastleTree live path (DSB 0/1 + LessLag shrinks restored). Salts
  84340-84345. Remaining ~16 mechanical structures -> D6b.
- **Robot Lab reconciliation — WGEN-058..061 (new) FIXED:** invented chest
  palette (dropper/dispenser/clock/comparator) -> faithful 23-entry
  `chests/robot_lab.json` (total 755); Robo-Pounder/Robo-Warrior bindings
  unswapped; hangar-before-pillars order restored (the port erased both rear
  sniper spawners every generation); railway/assembly/altar/door hardware
  restored (powered rails, powered crusher lever, quartz stairs/white carpet,
  piston facing, NORTH-facing door pair). Anchor switched to the faithful
  ISLANDS_GRASS; the /locate recentring retained per the audit's own
  recommendation.
- **WGEN-044 — FIXED (audit corrected):** BlockDuplicatorLog was already
  faithful; the missing half was the worldgen seed log (VeggiePatchFeature
  what==5, orig OSW:1915-1916) — restored.
- **WGEN-045 — FIXED:** BlockExperiencePlant's placeholder grower replaced
  with the faithful Trees.ExperienceTree port (live-tick reads legal, kept).
- **WGEN-062 (new) — FIXED:** fairy-tree dispatch scan-exhaustion return
  restored to the original TRUE (suppresses the chunk's follow-ups).
- **Infrastructure:** END_SURFACE + OCEAN_SURFACE placement modes, per-JSON
  placement_mode override, generic spawnEntity helper; the pattern doc gained
  the tree-generator addendum and the "port the FULL return contract" trap.
- **Verification:** four-verifier independent pass over all new code:
  0 critical / 0 major / 4 minor, all resolved pre-commit (ocean anchor
  off-by-one at Y40, robot door 180-degree mirror, Inca Javadoc overclaim,
  WGEN-062). EnderCastle/Kyuubi verifiers: zero findings. Verifiers also
  corrected two D5-assessment miscounts: EnderCastle has THREE loot chests
  (not 4), and DSB types 24/37 are single-call (the "type-24 pair" premise
  was a truncated-read artifact).
- **Notes:** PN-017 (End placement), PN-018 (Inca ramps), PN-019 (ocean
  biome). No new MOD entries (removed inventions were duplicative).
- **Ledger:** 618 IDs (613 + WGEN-058..062), 461 terminal / 157 open,
  reconcile green. **Build:** full `./gradlew build` — see commit.
- **Pending manual tests:** TESTING_CHECKLIST.md section D6a.

---

"""
fl.write_text(t.replace(anchor, d6a + anchor), encoding="utf-8")
print("FIX_LOG: D6a section inserted")

tc = ROOT / "TESTING_CHECKLIST.md"
t = tc.read_text(encoding="utf-8")
anchor = "## Failure log"
assert anchor in t
d6t = """## D6a — strong-model structures (added 2026-08-08)

- **EnderCastle (End)** — `/execute in minecraft:the_end run tp @s 100 70 0`, `/locate structure orespawn:ender_castle_end`. Expect: 29x29 obsidian-plate castle on end stone (central OR outer islands — PN-017), 4 spiral-stair corner towers, rooftop lava pool + dragon-egg pedestal, Ender Knight/Reaper rooftop spawner pairs + pit + CaveFisher alcoves, 3 alcove chests (facing inward) rolling the ender/experience-catcher table (6-10 stacks), trophy ender chest EMPTY (plain block).
- **EnderCastle (Islands)** — `/locate structure orespawn:ender_castle_islands` in orespawn:islands: same castle at grass level.
- **IncaPyramid (Islands)** — `/locate structure orespawn:inca_pyramid`: 41x31 stepped pyramid, 4 torch-ended ramps (support pillars reach the ground — PN-018), lit-lamp temple checkerboard, 5 water altars, 4 Creeper Repellents, Molenoid spawner, trapdoor + ladder shaft, 24-grave graveyard (~1/3 with Ghost spawners, poppy/dandelion/poppy beds), all chests roll 10-14 stacks of the 480-weight table.
- **KyuubiDungeon (Mining)** — `/locate structure orespawn:kyuubi_dungeon`: sealed surface hut (enter via the 1x1 roof hole), 22-deep shaft with water brake, lava-walled corridor, boss room with altar + 4-tier ziggurat, 8-Blaze spawner ring, kyuubi chest (7-13 stacks) + 4 wall chests with DIFFERENT fill counts (4-8/3-7/5-9/6-10) incl. blaze-egg loot entries.
- **Robot Lab (Islands)** — freshly generated lab: BOTH rear sniper spawners exist behind the hangar wall; altar spawns Robo-Pounder, treasure room Robo-Warrior, pillars Robo-Sniper; railway has powered (golden) rails + unpowered floor levers; assembly-line sticky pistons face south under white carpet and are CRUSHING (their lever generates powered); entry doors face NORTH and open with the wall buttons; chests roll 10-14 of the 755-weight table (minecarts, kits, Ray Gun — no droppers/dispensers/clocks).
- **Hospital (End)** — `/locate structure orespawn:hospital`: 10x10 iron-bar cage, 4 End Crystals on bedrock caps (NO dragon), 8 spawners, chest at the corner (6-10 of the 210 table).
- **MonsterIsland (overworld)** — `/locate structure orespawn:monster_island` over PLAIN ocean only (not deep/frozen/warm — PN-019): floating lens island in the water surface, canopy tree, 4 spawners all of ONE randomly picked mob, 2 chests (4-8 of the 450 table).
- **DSB outcomes** — Random Dungeon Spawner can now also produce: fairy tree (0), fairy castle tree (1), kyuubi dungeon (7), hospital (24), ender castle (27), inca pyramid (29), robot lab (30), monster island (37) at the block.
- **Trees** — a lone Duplicator Log appears rarely in veggie patches (duplicatorTreeEnable on); the experience sapling grows the REAL experience tree (2x2 oak trunk, drooping branch crown); crystal fairy trees shrink with lessLag=1; fairy-tree chunks that fail the site scan still suppress termites (WGEN-062 quirk).

---

"""
tc.write_text(t.replace(anchor, d6t + anchor), encoding="utf-8")
print("TESTING_CHECKLIST: D6a section inserted")
