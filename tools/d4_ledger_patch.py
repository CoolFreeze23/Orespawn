# One-shot ledger patch for slice D4 (2026-07-02): appends Resolution lines to
# the 26 findings closed/deferred this slice and rewrites the 7 pre-existing
# PARTIAL resolutions that D4 finished (or, for ITEM-062, narrowed).
import io
import re
import sys

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")

PATH = "AUDIT_FINDINGS.md"
with open(PATH, encoding="utf-8") as fh:
    text = fh.read()

D4 = "2026-07-02, Phase D4"

APPEND = {
    "ITEM-063": f"FIXED ({D4} — `ModDispenserBehaviors` registers all 8 original behaviors (IrukandjiArrow with pickup-allowed, WaterBall, SunspotUrchin, Acid, IceBall, DeadIrukandji, LaserBall, plus the shared rock behavior stamped onto all 12 rock items with types 1-12; orig OreSpawnMain.java:5755-5773 + MyDispenserBehavior*.java) using the original BehaviorProjectileDispense numbers — velocity 1.1, inaccuracy 6.0, +0.1 vertical bias, aux effect 1002; see FIX_LOG.md)",
    "ITEM-029": f"FIXED ({D4} — orig ItemSunFish.java:29-48 effects restored via FoodProperties: Butter Candy Speed+Jump Boost 2000t, Cooked Bacon Regen+Strength 2000t, Crystal Apple Regen+Strength 3000t, Heart Regen IV/Strength III/Fire Res III/Resistance II 6000t + Speed/Jump 5000t; item renamed \"Love\" per orig lang; see FIX_LOG.md)",
    "ITEM-057": f"FIXED ({D4} — the armor-set XP effect was already restored alongside ITEM-040 in C6 (sword and armor share the XP-bottle set-effect handler); D4 closed the item half: ItemExperienceTreeSeed placement/consumption ported faithfully and the invented leaf-harvest mechanic removed from BlockExperienceLeaves; the tree worldgen body itself is WGEN-045 (Phase D5); see FIX_LOG.md)",
    "ITEM-022": f"VERIFIED-CORRECT ({D4} — RockBlock is dead code in 1.7.10: the class exists in the source but is never instantiated or registered anywhere (grep across OreSpawnMain and the full tree), so no block form ever existed in-game and there is nothing to port; see FIX_LOG.md)",
    "ENT-K-076": f"FIXED ({D4} — RockBase death drop restored (one rock item matching the mob's type) and the type indexing realigned to the original 1-based 1-12 scheme across ItemRock/EntityThrownRock/RockBase, fixing a 0-vs-1-based mismatch that made placed rocks lose their type; see FIX_LOG.md)",
    "ITEM-060": f"FIXED ({D4} — `skate_bow.json` added per the orig registration (crystal-stick + string bow shape); see FIX_LOG.md)",
    "ITEM-061": f"FIXED ({D4} — `chest_from_crystal_planks.json` added (orig OreSpawnMain.java:3083, duplicated at :3209). The audit's \"piston\" reading was a misidentification: field_151135_aq at :3084-3085 is the 1.7.10 wooden door, crafted in the 2x3 plank shape — the port's existing `oak_door_from_crystal_planks.json` already matches it faithfully, so the door conversion flagged as divergent is in fact correct; see FIX_LOG.md)",
    "ENT-K-007": f"FIXED ({D4} — fire immunity restored per orig Kyuubi.java (field_70178_ae = true), so its own fire attacks no longer self-damage; see FIX_LOG.md)",
    "ENT-S-025": f"FIXED ({D4} — SpitBug cactus + fall immunity ported from the orig damage-source filter; see FIX_LOG.md)",
    "ENT-S-047": f"FIXED ({D4} — Triffid cactus + fall immunity ported from the orig damage-source filter; see FIX_LOG.md)",
    "ENT-A-088": f"FIXED ({D4} — `chipmunk.json` rebuilt from the orig drop table, including the tamed-only poppy drop (orig Chipmunk.java:231-242, handled in-code per the established tamed-gate convention); see FIX_LOG.md)",
    "ENT-D-052": f"FIXED ({D4} — `gold_fish.json` loot added per the orig GoldFish drop table; see FIX_LOG.md)",
    "ENT-K-084": f"FIXED ({D4} — `rubber_ducky.json` loot added per the orig RubberDucky drop table; see FIX_LOG.md)",
    "ENT-S-034": f"FIXED ({D4} — `stinky.json` death drop added per the orig Stinky drop; see FIX_LOG.md)",
    "ENT-K-011": f"FIXED ({D4} — Nether bonus restored: breaking lavafoam in the Nether grants 5 + nextInt(5) + nextInt(5) = 5-13 XP via getExpDrop (orig Lavafoam.java:110-116); see FIX_LOG.md)",
    "ENT-S-085": f"FIXED ({D4} — theft ported line-by-line: 1-in-4 helmet-else-chestplate steal (orig WormLarge.java:210-230) and independent 1-in-4 held-item steal (:231-238) with the stolen stack zeroed and scattered as an item entity, PlayNicely gate (:192-198), nearest non-creative player within 8 (:199-202); death drops (worm tooth/painting/rotten flesh/leather, :352-377) and the \"Large Worm\" spawner bypass (:263-309) also restored; see FIX_LOG.md)",
    "ENT-S-078": f"FIXED ({D4} — the surface-block check ported at every burrow-cycle step (orig WormSmall.java:107-110/124-127/139-142) with tall grass counting as air (:104-106); the 1-in-6 boots theft (:188-195) and night-only spawn gate (:214-216) restored with it; see FIX_LOG.md)",
    "ENT-K-047": f"FIXED ({D4} — termite hunting ported: nearest living, visible Termite targeting (orig Peacock.java:202-237), flat 6.0 mob-attack damage (:166-169), 1-in-200 revenge clear / peaceful gate (:181-200); see FIX_LOG.md)",
    "ENT-K-048": f"FIXED ({D4} — egg laying ported: clear-air / first-half-of-day / 50<=y<=100 / at-most-2-buddies-within-16 gate (orig Peacock.java:101-119, restoring the never-called findBuddies()), 1-3 eggs at ±0-1 x/z y+1 (:171-179,197-199); Crystal Apple confirmed as the breeding item (:259-261); see FIX_LOG.md)",
    "ENT-D-010": f"FIXED ({D4} — mob-egg laying ported with the full 115-entry mob→spawn-egg lookup (script-extracted from the orig table and mapped to the port's spawn-egg items) and carrot taming restored; natural spawns additionally gated to Easter via checkSpawnRules (ANIM-016); see FIX_LOG.md)",
    "ENT-S-059": f"FIXED ({D4} — rebuilt on the vanilla FishingHook using access transformers for nibble/currentState/catchingFish/shouldStopFishing: the orig weighted junk/treasure/vanilla-fish/OreSpawn-water-fish/lava-fish pools ported into getCatch with Luck-of-the-Sea/Lure scaling, lava fishing (buoyancy + bite state machine + lava-appropriate particles), fire-immune hook spawning EntityLavaLovingItem for lava catches, XP orb on retrieve, random durability damage + level-30 enchant on caught gear; the invented +3 luck/+2 lure-speed constructor bonuses were removed and the renderer switched to the vanilla FishingHookRenderer; see FIX_LOG.md)",
    "ENT-S-036": f"FIXED ({D4} — verified already restored in Phase C6 as part of ITEM-053's projectile pass (\"urchin fire restored\"); the ledger entry was simply never updated, no D4 code change needed; see FIX_LOG.md)",
    "ENT-K-080": f"FIXED ({D4} — verified already implemented in slice D1's spawn-architecture work: `wasSpawnered` is set during checkSpawnRules, persisted to NBT, and consumed by the despawn exemption; the ledger entry was never updated, no D4 code change needed; see FIX_LOG.md)",
    "ANIM-016": f"FIXED ({D4} — seasonal gates ported and made live: `SeasonalDates` evaluates isHalloween/isValentines/isEaster from LocalDate at check time instead of the orig's once-at-init GregorianCalendar snapshot (deviation logged as PN-014). Halloween: the 22-biome Ghost/GhostSkelly block added as `halloween_ghosts.json`, runtime-gated in checkSpawnRules with the 5 year-round biomes exempt (closes ENT-D-039/041). Easter: EasterBunny spawn gate (closes ENT-D-011). Valentine's: Girlfriend 2.5x8.0 dimensions + 800 HP + girlfriendv texture + MyValentineTarget goal (players/Boyfriends while angry) + Rose Sword 1-in-4 cure with Love drops, persisted via feelingBetter NBT; see FIX_LOG.md)",
    "ENT-A-052": f"FIXED ({D4} — pass-through immunities restored in canHitEntity (other BetterFireballs, Mothra, GodzillaHead, Royalty, plus Player/Dragon when notme is set) and the HP-halving exemption list restored in onHitEntity (Royalty, Godzilla, GodzillaHead, PitchBlack, Kraken) per orig BetterFireball; see FIX_LOG.md)",
    "ITEM-065": f"DEFERRED ({D4} — the orig config-file per-tier weapon/armor/ore stat overrides cannot be replicated against NeoForge's frozen static item registries without registry mutation; the orig default values stay hardcoded (verified number-by-number in earlier slices), the platform decision is documented as PARITY_NOTES PN-013 and the config system as 2.0 modernization candidate MODERNIZATION_NOTES MOD-011; see FIX_LOG.md)",
}

# fid -> (old resolution state+opening, replacement full line suffix appended)
REWRITE = {
    "ENT-A-098": " — CLOSED (2026-07-02, Phase D4 — the CoinEgg slot filled: coin.json's remaining jackpot slot now yields the ported coin spawn egg, completing the orig 10-slot table)",
    "ITEM-053": " — CLOSED (2026-07-02, Phase D4 — Shoes & GameController throwables ported: ItemShoes drives all 5 shoe/controller items, the full per-target damage table restored incl. Girlfriend/Boyfriend 1.0f and the Valentine's-Day 10.0f override, reddust + snowballpoof impact particles)",
    "ENT-A-001": " — CLOSED (2026-07-02, Phase D4 — TrooperBug/SpitBug acid immunity restored in LaserBall.onHitEntity: when isAcid, the projectile discards on impact with either bug)",
    "ENT-D-011": " — CLOSED (2026-07-02, Phase D4 — the Easter-day gate is now live via SeasonalDates.isEaster() in EasterBunny.checkSpawnRules; ANIM-016)",
    "ENT-D-039": " — CLOSED (2026-07-02, Phase D4 — the Halloween 22-biome block added as halloween_ghosts.json, runtime-gated by SeasonalDates.isHalloween() in Ghost.checkSpawnRules with the 5 ungated biomes exempt; ANIM-016)",
    "ENT-D-041": " — CLOSED (2026-07-02, Phase D4 — same as ENT-D-039 for GhostSkelly: halloween_ghosts.json + SeasonalDates gate in checkSpawnRules; ANIM-016)",
}

ITEM_062_NEW = (
    "- **Resolution:** PARTIAL (2026-06-12, Phase C — all 381 original registrations diffed by script "
    "(phase_c_reports/C6_recipe_diff.md): 201 logical recipes verified/fixed, 16 invented recipe JSONs removed. "
    "2026-07-02, Phase D4 — the six diffed-absent standalone recipes added (skate bow, chest from crystal planks, "
    "red bed from crystal planks, raw corn dog, bucket from pink-tourmaline ingots, cobweb from string). "
    "Remainder: the 116 water-bucket spawn-block→egg conversions (OreSpawnMain.java:2667-3000s), blocked on the "
    "~105-type SpawnOres block pool owned by WGEN-005 → Phase D5 structure/spawn-block slice; see FIX_LOG.md)"
)

changed = 0

for fid, res in APPEND.items():
    start = text.find(f"### {fid} ")
    if start < 0:
        start = text.find(f"### {fid}\n")
    assert start >= 0, f"finding {fid} not found"
    end = text.find("\n### ", start + 1)
    if end < 0:
        end = len(text)
    block = text[start:end]
    assert "**Resolution:**" not in block, f"{fid} already has a resolution"
    trimmed = block.rstrip("\n")
    new_block = trimmed + f"\n- **Resolution:** {res}\n"
    text = text[:start] + new_block + text[end:]
    changed += 1

for fid, suffix in REWRITE.items():
    start = text.find(f"### {fid} ")
    end = text.find("\n### ", start + 1)
    block = text[start:end]
    m = re.search(r"- \*\*Resolution:\*\* PARTIAL (.*?)(?=\n)", block, re.DOTALL)
    assert m, f"{fid}: PARTIAL resolution line not found"
    old_line = m.group(0)
    new_line = old_line.replace("**Resolution:** PARTIAL", "**Resolution:** FIXED", 1) + suffix
    text = text[:start] + block.replace(old_line, new_line, 1) + text[end:]
    changed += 1

# ITEM-062 stays PARTIAL but the resolution text is replaced wholesale.
start = text.find("### ITEM-062 ")
end = text.find("\n### ", start + 1)
block = text[start:end]
m = re.search(r"- \*\*Resolution:\*\* PARTIAL .*?(?=\n)", block, re.DOTALL)
assert m, "ITEM-062 resolution not found"
text = text[:start] + block.replace(m.group(0), ITEM_062_NEW, 1) + text[end:]
changed += 1

with open(PATH, "w", encoding="utf-8", newline="") as fh:
    fh.write(text)
print(f"patched {changed} findings")
