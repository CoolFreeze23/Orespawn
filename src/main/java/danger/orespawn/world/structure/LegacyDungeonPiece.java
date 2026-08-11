package danger.orespawn.world.structure;

import danger.orespawn.ModBlocks;
import danger.orespawn.ModEntities;
import danger.orespawn.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Audit Part 2 &mdash; unified {@link StructurePiece} for the four legacy
 * Tech &amp; Danger dungeons (Shadow, Greenhouse, Robot Lab, White House).
 *
 * <p>All four use the canonical Mansion / Stronghold multi-pass
 * {@code chunkBox.isInside} stitching pattern (see {@link RoyalTreePiece}
 * for the reference implementation), so they can faithfully reproduce
 * the legacy structures' full footprints &mdash; up to ~50 blocks across
 * for the Robot Lab and White House &mdash; without being clipped at
 * chunk borders by the {@link WorldGenLevel} 24-block write window.</p>
 *
 * <p>The legacy generators in {@code GenericDungeon} are ported
 * byte-for-byte: same loops in the same nesting order, same block
 * selections by index, same chest fill counts. Where a legacy item has
 * no 1.21.1 counterpart (e.g. {@code BeeEgg}, {@code MantisEgg}), the
 * closest functional analog is substituted &mdash; documented inline in
 * each chest fill helper.</p>
 */
public class LegacyDungeonPiece extends StructurePiece {

    /**
     * {@code Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE}: no neighbour
     * cascade, no lighting recompute, and — TF-021 fix (2026-08-10) — no
     * SHAPE updates on already-placed neighbours. On the live buildNow path
     * a plain flag-2 {@code ServerLevel.setBlock} still ran
     * {@code updateShape} on neighbours, which let {@code BushBlock}
     * self-erase fragile plants the generator had just placed (greenhouse
     * farmland mushrooms died in ~86% of placements — 13.65% empty plots vs
     * the documented 1-in-20, orig GenericDungeon.java:5075-5080). The
     * 1.7.10 {@code setBlockFast} flag 2 fired no neighbour updates at all
     * (orig GenericDungeon.java:187-189); UPDATE_KNOWN_SHAPE restores that
     * semantic for every piece write (also protects lily pads, nether wart,
     * cocoa on their structures' live paths).
     */
    private static final int FLAG_PIECE_WRITE = 2 | 16;

    /**
     * Per-dungeon bounding box envelope. Each tuple is sized to the legacy
     * footprint plus a safety margin so {@code postProcess} fires for
     * every chunk the algorithm can touch &mdash; matching the
     * {@link RoyalTreePiece} telemetry-locked pattern (worst-observed
     * extent + ~12-block ceiling).
     *
     * <p>Footprint reference (verified 1.7.10
     * {@code GenericDungeon.java}):</p>
     * <ul>
     *   <li><b>Shadow</b> &mdash; line 1453: 19 wide, double pyramid going
     *       down 10 + up 10 along Y; ±20 horizontal × +20/-12 vertical.</li>
     *   <li><b>Greenhouse</b> &mdash; line 5030: 23 long × 15 wide × 7
     *       tall + 3 above for spawners; ±24 horizontal × +14/-2 vertical.</li>
     *   <li><b>Robot Lab</b> &mdash; line 4044: 10×20 entry, then 30×30
     *       hangar shifted south + 35-tall sniper tower; ±48 horizontal
     *       × +50/-2 vertical.</li>
     *   <li><b>White House</b> &mdash; line 5423: 25×25 base + walls (23×6
     *       tall) + 13-tier stepped roof + 12-tall flagpole + fountains
     *       extending z-15; ±48 horizontal × +25/-2 vertical.</li>
     * </ul>
     */
    public enum DungeonType {
        SHADOW(20, 12, 20),
        GREENHOUSE(24, 2, 14),
        // Phase D6a reconciliation: anchor switched from the chunk-centre
        // heightmap probe to the faithful Islands D4 grass anchor
        // (addD4RobotLab: LessLag gate + nextInt(8) jitter + grass scan
        // Y20→5, OSW:2368-2389; audit spec d6_extraction/robot_lab_audit_spec.md).
        ROBOT_LAB(-48, 48, 2, 50, -48, 48, PlacementMode.ISLANDS_GRASS),
        WHITE_HOUSE(48, 2, 25),
        // Audit Part 3 — buried 5x5 lapis surface antenna + 17-block descending
        // shaft + 4 cardinal "Part" rooms (W=15 wide max). Down -25, up +6.
        // D6b batch-4 F8 fix (dsb_sweep_spec.md): the south Part room writes
        // to z = origin-21 (orig GD:1674 — makeAlienPart at sz-7 spanning 15),
        // outside the old symmetric ±20 box, so its far Z wall plane was
        // ALWAYS dropped on the buildNow path and on ~1/16 worldgen chunk
        // alignments. Widened to the true footprint (X -19..+17, Z -21..+15)
        // +1 margin. Documented delta: the piece RandomSource seeds from the
        // bounding box, so worldgen layouts reseed for existing seeds.
        ALIEN_WTF(-20, 20, 25, 6, -22, 20, PlacementMode.SURFACE_CENTER),
        // Audit Part 3 — hollow rad=10 sphere with surface decoration shell
        // (legacy line 4677). Down -10, up +5 (hollowed sky cap).
        LEONOPTERYX_NEST(12, 10, 5),
        // Audit Part 3 — 51x51x48 grand altar (legacy line 4353/5697). Origin
        // is the SW corner of the pad, so positive X/Z reach is +56 with the
        // 5-block clear margin; we centre by passing the centre as origin
        // and use ±32 extents to span the full footprint.
        // D6b batch-4 F3 fix (dsb_sweep_spec.md): down 4 clipped the v=1..9
        // dirt skirt (orig GD:4377-4382/5721-5726, writes to origin-9) and
        // up 56 clipped the top 2 rows of the j<=height+10 air clear (orig
        // GD:4364, writes to origin+58) in BOTH worldgen and buildNow.
        // Widened to down 10 / up 59 (+1 margin). Documented delta: piece
        // RNG seeds from the box, so layouts reseed for existing seeds.
        KING_ALTAR(32, 10, 59),
        QUEEN_ALTAR(32, 10, 59),
        // Audit Part 4 + Phase D5 reconciliation — King's / Queen's Challenge
        // Tower (legacy GenericDungeon.makeEnormousCastle line 191 /
        // makeEnormousCastleQ line 6393). 28x28 base + up to 6 stacked floors
        // (10/10/9/9/8/16 tall), 4-block foundation skirt, western
        // platform-arm + descending stair to x=-37 (GD:340-361), and — on
        // level-6 towers — the buried "Large Worm" spawner ring scattered
        // over x,z -28..+55 at y-1 (GD:362-374). The pre-D5 symmetric ±40
        // box CLIPPED that ring's outer band at chunk borders (WGEN-055);
        // the asymmetric box now covers stair + skirt + ring with margin:
        // x -39..+57, z -30..+57, down 4, up 85 (Nightmare-cap spawners top
        // out at +80, GD:495-514). Placement is the faithful Islands D4
        // anchor (grass level + nextInt(8) jitter + LessLag gate,
        // OSW:2203-2228) instead of the old chunk-centre heightmap probe.
        KING_TOWER(-39, 57, 4, 85, -30, 57, PlacementMode.ISLANDS_GRASS),
        QUEEN_TOWER(-39, 57, 4, 85, -30, 57, PlacementMode.ISLANDS_GRASS),
        // Phase D5 (WGEN-037) — BasiliskMaze (orig BasiliskMaze.java). The
        // footprint is strongly asymmetric around the build origin (X0,Y0,Z0):
        // X −8 (pyramid ground ring, BM:421) .. +64 (east bedrock shell Bx+61
        // = X0+64, BM:328); Y −(D+4) with D ∈ [20,29] → −33 (castle floor,
        // BM:305) .. +8 (pyramid apex, BM:419); Z −22 (castle north shell
        // Bz−2, BM:335) .. +11 (south shell Bz+31, BM:342). One margin block
        // each side. Placement reproduces OreSpawnWorld.addBasiliskMaze's
        // lowest-of-36-columns surface scan (OSW:2573-2597).
        BASILISK_MAZE(-9, 65, 34, 9, -23, 12, PlacementMode.LOWEST_SURFACE_36),
        // Phase D5 (WGEN-042, Nightmare Rookery) — two 26-column spike-ridge
        // passes (orig GenericDungeon.java:5242-5312). X core −5..+20 ±1 side
        // bulge; Z is a cumulative 52-step drunkard's walk from 0, hard bound
        // ±52 ±1 bulge (unclamped in the original, GenericDungeon.java:5253,
        // 5283); Y pillars 0..18 + chest 19 + spawner 20. One margin block.
        NIGHTMARE_ROOKERY(-7, 22, 1, 22, -54, 54, PlacementMode.ISLANDS_GRASS),
        // Phase D6a (WGEN-042, Ender Castle) — orig GenericDungeon.java:
        // 3207-3624. 29×29 obsidian plate (−3..+25) + four corner towers
        // reaching −4..+26, 17 tall (spec d6_extraction/ender_castle_spec.md
        // §10). Generates in TWO dimensions: the End (default END_SURFACE —
        // air-on-end-stone anchor, OSW:1557-1570) and Islands D4 i==7
        // (ISLANDS_GRASS via the structure JSON's placement_mode override,
        // OSW:2322-2343).
        ENDER_CASTLE(-5, 27, 1, 17, -5, 27, PlacementMode.END_SURFACE),
        // Phase D6a (WGEN-042, Inca Pyramid) — orig GenericDungeon.java:
        // 3735-4044. 41×31 stepped base with four ramps reaching X −10..+50,
        // Z −10..+40, 20 tall (spec d6_extraction/inca_pyramid_spec.md §7).
        // Islands D4 i==8 (OSW:2345-2366).
        INCA_PYRAMID(-11, 51, 1, 20, -11, 41, PlacementMode.ISLANDS_GRASS),
        // Phase D6a (WGEN-042, Kyuubi Dungeon) — orig GenericDungeon.java:
        // 1095-1363. Surface hut + 22-deep shaft into the 20×30 boss room
        // with altar/ziggurat; X −0..+34, Y −22..+5, Z −15..+14 (spec
        // d6_extraction/kyuubi_dungeon_spec.md, suggested entry adopted).
        // Mining rotation i==1 slot (1/665, set 26/13 like BasiliskMaze).
        KYUUBI_DUNGEON(-1, 35, 23, 6, -16, 15, PlacementMode.LOWEST_SURFACE_36),
        // Phase D6a (WGEN-042, Ender Dragon Hospital) — orig GenericDungeon
        // .java:2815-2991. 10×10 iron-bar cage with 4 End Crystals on bedrock
        // caps (NO dragon — spec section A2), ramp to X −6; End-exclusive
        // worldgen (1/4 × 1/25, OSW:1542-1555).
        HOSPITAL(-7, 10, 1, 12, -1, 10, PlacementMode.END_SURFACE),
        // Phase D6a (WGEN-042, Monster Island) — orig GenericDungeon.java:
        // 5170-5240. Floating lens island on the OVERWORLD OCEAN surface
        // (biome "Ocean", 1/6 × 1/300, anchor = the water-surface block,
        // OSW:1398-1412). X ±5, Y −1..+4, Z ±3.
        MONSTER_ISLAND(-6, 6, 2, 5, -4, 4, PlacementMode.OCEAN_SURFACE),
        // Phase D6b batch 1 — specs in phase_d_reports/d6_extraction/.
        // Play Pool (GD:1934-1957): sky platform +16..+18 above the ocean
        // air-anchor (OSW:1136-1154; anchor = the AIR block, not the water —
        // hence OCEAN_SURFACE_AIR).
        PLAY_POOL(-2, 5, 1, 19, -1, 1, PlacementMode.OCEAN_SURFACE_AIR),
        // Cloud Shark Dungeon (GD:2059-2091): 3×3×3 floating sky cluster,
        // Islands Y 150..159 with no scan at all (OSW:2423-2428).
        CLOUD_SHARK_DUNGEON(-2, 2, 2, 2, -2, 2, PlacementMode.SKY_BAND_150),
        // Gold Fish Bowl (GD:2408-2488): glass bowl on the ocean surface
        // (OSW:1176-1194, water-block anchor like Monster Island).
        GOLD_FISH_BOWL(-2, 6, 0, 9, -2, 6, PlacementMode.OCEAN_SURFACE),
        // Spit Bug Lair (GD:2638-2696): overworld Swampland platform
        // (OSW:1236-1256; exact-name "Swampland" → minecraft:swamp only).
        SPIT_BUG_LAIR(-9, 9, 1, 13, -9, 9, PlacementMode.SWAMP_GRASS_SURFACE),
        // Phase D6b batch 2 — specs in phase_d_reports/d6_extraction/.
        // Igloo (GD:2698-2813): worldgen placement wired by WGEN-071 per the
        // igloo_spec.md §7.3 ruling (both original gates reproduced
        // mechanically — snowy_plains-only biome tag + a generation-time
        // TRUE-surface re-verification; no invented frequency). The anchor
        // is SNOW_SURFACE_MINUS2; the piece check may RELOCATE the build to
        // one of the original's remaining jittered attempt columns anywhere
        // in the anchor chunk (orig OreSpawnWorld.java:1265-1275), so the
        // box covers the shell's −6..+7 drift union (spec §6) around ANY
        // in-chunk column: ±15 jitter → −(15+6+1)..+(15+7+1) = −22..+23 on
        // both axes. DSB type 20 (buildNow) still bypasses biome, scan and
        // the −2 sink (spec §9) — the wider write window is harmless there.
        IGLOO(-22, 23, 1, 6, -22, 23, PlacementMode.SNOW_SURFACE_MINUS2),
        // Ender Reaper Graveyard (GD:2490-2563 + makeAGrave :2565-2576):
        // End-exclusive (addEndReapers OSW:1527-1540, quickSpaceCheck 12×12).
        ENDER_REAPER_GRAVEYARD(-1, 11, 5, 5, -1, 13, PlacementMode.END_SURFACE),
        // Water Dragon Lair (GD:1959-2057): ocean-surface structure.
        WATER_DRAGON_LAIR(-11, 12, 2, 8, -11, 12, PlacementMode.OCEAN_SURFACE),
        // Leaf Monster Dungeon (GD:2093-2226): overworld exact-"Plains"
        // (biome via tag; the SWAMP_GRASS_SURFACE anchor is biome-agnostic —
        // 4 jitter attempts, Y 41..100 dry grass surface, same scan shape).
        LEAF_MONSTER_DUNGEON(-4, 7, 5, 17, -4, 7, PlacementMode.SWAMP_GRASS_SURFACE),
        // Mini Dungeon (GD:2229-2406): Islands D4 i==10 (addD4Mini).
        MINI_DUNGEON(-7, 10, 1, 12, -1, 10, PlacementMode.ISLANDS_GRASS),
        // Cephadrome Altar (GD:4731-4829): Islands D4 i==12.
        CEPHADROME_ALTAR(-5, 5, 1, 5, -5, 5, PlacementMode.ISLANDS_GRASS),
        // Phase D6b batch 3 — specs in phase_d_reports/d6_extraction/.
        // Bouncy Castle (GD:3106-3205): overworld exact-"Desert" scan
        // (OSW:1280-1299; anchor = the SAND block, posY−1 at OSW:1292).
        BOUNCY_CASTLE(-5, 5, 1, 5, -5, 5, PlacementMode.SAND_SURFACE_MINUS1),
        // Damsel In Distress (GD:3625-3733): Village-dimension cottage
        // (OSW:1301-1317; anchor = the GRASS block, posY−1 at OSW:1311,
        // plus the quickSpaceCheck 12×12 clearance probe OSW:2625-2633).
        DAMSEL_IN_DISTRESS(-5, 5, 1, 10, -5, 5, PlacementMode.VILLAGE_GRASS_SURFACE),
        // Girlfriend Island (GD:4962-5028): Monster Island's near-twin —
        // same ocean scan shape (OSW:1378-1396), same box.
        GIRLFRIEND_ISLAND(-6, 6, 2, 5, -4, 4, PlacementMode.OCEAN_SURFACE),
        // Stinky House (GD:5314-5381): Islands D4 i==15 (addD4StinkyHouse
        // OSW:2276-2297, grass anchor like the other D4 adds).
        STINKY_HOUSE(-6, 20, 1, 4, -5, 13, PlacementMode.ISLANDS_GRASS),
        // Pumpkin (GD:6041-6182): Islands D4 i==17 (addPumpkin); the
        // builder receives the AIR block above grass — hence _AIR.
        PUMPKIN(-1, 14, 1, 18, -1, 12, PlacementMode.ISLANDS_GRASS_AIR),
        // Rainbow (GD:6260-6393): Islands D4 i==18 (addD4Rainbow
        // OSW:2430-2436) — unconditional sky placement, Y 70..89.
        RAINBOW(-15, 14, 0, 41, -4, 4, PlacementMode.SKY_BAND_70),
        // Phase D6b batch 4 — specs in phase_d_reports/d6_extraction/.
        // Spider Hangout (GD:6989-7043): Village-dimension gravel pad
        // (addSpiderHangout OSW:1319-1338, grass-block anchor posY-1 +
        // quickSpaceCheck; SpiderDriverEnable gate honored in
        // findGenerationPoint, orig OSW:1323-1325).
        SPIDER_HANGOUT(-1, 20, 2, 20, -1, 20, PlacementMode.VILLAGE_GRASS_SURFACE),
        // Red Ant Hangout (GD:7045-7069, file-final method): Village-dim
        // twin of the spider pad (addRedAntHangout OSW:1340-1356 — no
        // config gate in the original, verified).
        RED_ANT_HANGOUT(-1, 16, 2, 16, -1, 16, PlacementMode.VILLAGE_GRASS_SURFACE),
        // Frog Pond (GD:6018-6039): overworld exact-"Plains" water sheet
        // (addFrogPond OSW:1156-1174; anchor = the GRASS block, posY-1 at
        // OSW:1168 — SAND_SURFACE_MINUS1 is the biome-agnostic -1-anchor
        // mode; plains identity via the biome tag).
        FROG_POND(-4, 4, 1, 3, -4, 4, PlacementMode.SAND_SURFACE_MINUS1),
        // Rubber Ducky Pond (GD:5383-5421): overworld exact-"Plains"
        // perched pond (addRubberDuckyPond OSW:1217-1236; anchor = the AIR
        // block above grass, same shape as the swamp scan).
        RUBBER_DUCKY_POND(-6, 7, 1, 7, -6, 6, PlacementMode.SWAMP_GRASS_SURFACE),
        // Haunted House (GD:891-1010, overworld — distinct from the
        // Crystal variant): addHauntedHouse OSW:979-997, 3-biome gate,
        // air-block anchor; the original's 5 jitter attempts vs the
        // mode's 4 is a documented delta (haunted_house_spec.md §7).
        HAUNTED_HOUSE(-4, 4, 1, 5, -4, 4, PlacementMode.SWAMP_GRASS_SURFACE),
        // Ender Knight Dungeon (GD:1794-1932): End (addEndKnights
        // OSW:1512-1525, END_SURFACE default) + Mining dimension via the
        // per-JSON placement_mode override LOWEST_GRASS_36 (addEnderKnight
        // OSW:2087-2113 — lowest-grass 6x6 scan, NO -2 sink).
        ENDER_KNIGHT_DUNGEON(-1, 13, 1, 6, -3, 7, PlacementMode.END_SURFACE);

        /** How {@link LegacyDungeonStructure#findGenerationPoint} anchors this type. */
        public enum PlacementMode {
            /** Chunk-centre heightmap probe (the original Audit Part 2-4 behaviour). */
            SURFACE_CENTER,
            /**
             * OreSpawnWorld.addBasiliskMaze (orig OreSpawnWorld.java:2573-2597):
             * sample a 6×6 grid of columns at chunk offsets {0,3,6,9,12,15},
             * keep the LOWEST surface within the original's Y 31..128 scan
             * window, refuse unless that surface is above Y40, and sink the
             * origin 2 blocks into the terrain.
             */
            LOWEST_SURFACE_36,
            /**
             * OreSpawnWorld.addD4NightmareRookery (orig OreSpawnWorld.java:
             * 2253-2274): LessLag 50% skip, corner + nextInt(8) jitter, and a
             * grass-level anchor found by the original's Y 20→5 downward scan
             * (the Islands plane is flat, grass at Y7 via the orespawn:islands
             * noise settings, so the predicted heightmap is exact).
             */
            ISLANDS_GRASS,
            /**
             * The End-dimension anchor shared by addEnderCastle
             * (orig OreSpawnWorld.java:1557-1570) and addHospital (:1542-1555):
             * up to 3 attempts of chunk + nextInt(16) jitter, accepting the
             * first column whose surface — air directly on end stone — lies in
             * the original's Y 90→11 scan window. Structure starts resolve
             * before blocks exist, so the block probes map to the noise
             * heightmap (surface = getBaseHeight − 1; void columns report
             * min-build and are rejected), and the originals' flat air-plane
             * clearance probes (30×30 at +8 / 12×12 at +4) are approximated by
             * requiring the sampled neighbourhood surfaces not to rise above
             * the anchor (documented mapping delta, D6a report).
             */
            END_SURFACE,
            /**
             * OreSpawnWorld.addMonsterIsland (orig OreSpawnWorld.java:
             * 1398-1412): corner-biome gate "Ocean", up to 4 attempts of
             * in-chunk jitter, scan Y 100→41 for air directly above STILL
             * WATER; the anchor is the water-surface block itself (posY − 1).
             * Modern mapping: ocean biomes via the structure's biome tag, the
             * water surface from the noise heightmap (first-free − 1), and a
             * water-exists check via the ocean-floor heightmap sitting below
             * sea level.
             */
            OCEAN_SURFACE,
            /**
             * Play Pool's variant of {@link #OCEAN_SURFACE}
             * (orig OreSpawnWorld.java:1146-1148 — byte-identical scan to
             * Monster Island's, but the builder receives {@code posY}, the
             * AIR block above the water, not {@code posY - 1}).
             */
            OCEAN_SURFACE_AIR,
            /**
             * addD4CloudShark (orig OreSpawnWorld.java:2423-2428): no scan
             * at all — X/Z = chunk + 4 + nextInt(8), Y = 150 + nextInt(10),
             * unconditional. Frequency lives entirely in the structure set.
             */
            SKY_BAND_150,
            /**
             * addSpitBugLair (orig OreSpawnWorld.java:1236-1256): overworld
             * "Swampland" corner-biome gate (→ biome tag), 4 attempts of
             * chunk + nextInt(16) jitter, air-over-GRASS scan Y 100→41.
             * Grass identity is not predictable pre-terrain; approximated as
             * "dry column" (WORLD_SURFACE == OCEAN_FLOOR) within the Y
             * window — documented mapping delta, same style as END_SURFACE's
             * clearance approximation.
             */
            SWAMP_GRASS_SURFACE,
            /**
             * addBouncyCastle (orig OreSpawnWorld.java:1280-1299): the
             * {@link #SWAMP_GRASS_SURFACE} scan shape (exact-"Desert"
             * corner-biome gate → biome list, 4 attempts of chunk +
             * nextInt(16) jitter, air-over-SAND scan Y 100→41 with the
             * dry-column approximation standing in for the sand identity
             * test) — but the anchor is the SAND block itself
             * ({@code posY - 1}, orig :1292), one below the air block
             * SWAMP_GRASS_SURFACE returns. The mode is biome-agnostic
             * (surface identity rides on the structure's biomes): the Frog
             * Pond (addFrogPond OSW:1156-1174, grass/Plains, anchor
             * posY − 1 at :1168) is its second exact user.
             */
            SAND_SURFACE_MINUS1,
            /**
             * addDamselInDistress (orig OreSpawnWorld.java:1301-1317):
             * Village-dimension grass scan with the same 4-attempt
             * Y 100→41 shape; the anchor is the GRASS block
             * ({@code posY - 1}, orig :1311), and the original's
             * {@code quickSpaceCheck} 12×12 all-air plane at anchor+4
             * (orig :2625-2633) is approximated by the END_SURFACE-style
             * footprint clearance probe.
             */
            VILLAGE_GRASS_SURFACE,
            /**
             * Pumpkin's variant of {@link #ISLANDS_GRASS}: the D4 i-roll
             * hands {@code makePumpkin} the AIR block above the grass
             * anchor (orig OreSpawnWorld.java:2407-2421 chain → :2416),
             * so this mode is ISLANDS_GRASS plus one on Y.
             */
            ISLANDS_GRASS_AIR,
            /**
             * addD4Rainbow (orig OreSpawnWorld.java:2430-2436): no scan
             * at all — X/Z = chunk + 4 + nextInt(8), Y = 70 + nextInt(20),
             * unconditional; {@link #SKY_BAND_150} in a lower band. The
             * original drew Y from the WORLD RNG while X/Z came from the
             * chunk random (:2434); that mixed-stream quirk collapses into
             * the single seeded structure random (documented delta, same
             * as the Cloud Shark's).
             */
            SKY_BAND_70,
            /**
             * addEnderKnight's Mining-dimension anchor (orig
             * OreSpawnWorld.java:2087-2113): the {@link #LOWEST_SURFACE_36}
             * 6×6 lowest-surface scan (offsets {0,3,6,9,12,15}, Y 31..128
             * window, strictly-lowest first-seen-wins, {@code lowestY > 40}
             * gate) with two deltas — the accept condition is a GRASS block
             * (:2097, not any-solid; grass identity collapses into the same
             * noise-surface probe, the documented SWAMP_GRASS_SURFACE-style
             * approximation) and the anchor is {@code lowestY} with NO −2
             * sink (:2108 vs addBasiliskMaze's :2594). Future users with the
             * identical scan: addAlienWTF (OSW:2059-2085 — ALIEN_WTF still
             * anchors SURFACE_CENTER, a pre-D5 reconciliation candidate; not
             * rewired this slice) and addBeeHive (OSW:2031-2057, at
             * lowestY + 3).
             */
            LOWEST_GRASS_36,
            /**
             * addIgloo (WGEN-071; orig OreSpawnWorld.java:1265-1275): the
             * {@link #SWAMP_GRASS_SURFACE} scan shape — 4 attempts of chunk +
             * nextInt(16) jitter (:1265-1267), Y 100→41 accept window
             * (:1269), dry-column approximation — anchored TWO below the
             * first free block ({@code posY - 2}, :1271; cposy = one below
             * the surface block). The original double-gated: the corner biome
             * name had to be EXACTLY "Ice Plains" (:1263-1264 — excluding
             * "Ice Plains Spikes"), mapped to a minecraft:snowy_plains-ONLY
             * biome tag; AND the scanned column had to be air directly over a
             * SNOW BLOCK ({@code field_150433_aE} — the full block, NOT the
             * snow layer, :1270). Snow-block identity cannot ride on the
             * biome tag (plain snowy-plains surfaces are grass under a snow
             * LAYER, which the original scan rejected — igloo_spec.md §1.3),
             * so per the §7.3 ruling BOTH gates are reproduced mechanically
             * instead of inventing a frequency: this mode carries the scan
             * shape/window, and the piece re-verifies the TRUE snow-block
             * surface against real blocks at generation time, generating
             * NOTHING when every attempted column fails
             * ({@link LegacyDungeonPiece#resolveIglooWorldgenSite}). §1.3
             * caveat: the underlying 1.7.10 vanilla-terrain claim (plain Ice
             * Plains never exposing snow blocks) is unverified against
             * 1.7.10 vanilla itself — vanilla sources are not in the
             * reference tree.
             */
            SNOW_SURFACE_MINUS2
        }

        public final int minXOff;
        public final int maxXOff;
        public final int downExtent;
        public final int upExtent;
        public final int minZOff;
        public final int maxZOff;
        public final PlacementMode placement;

        /** Symmetric envelope (all pre-D5 dungeon types). */
        DungeonType(int h, int d, int u) {
            this(-h, h, d, u, -h, h, PlacementMode.SURFACE_CENTER);
        }

        /** Asymmetric envelope for algorithms whose reach differs per side. */
        DungeonType(int minXOff, int maxXOff, int down, int up, int minZOff, int maxZOff,
                    PlacementMode placement) {
            this.minXOff = minXOff;
            this.maxXOff = maxXOff;
            this.downExtent = down;
            this.upExtent = up;
            this.minZOff = minZOff;
            this.maxZOff = maxZOff;
            this.placement = placement;
        }
    }

    private final DungeonType dungeonType;
    private final BlockPos origin;

    /**
     * BUG-033 (beta.3): per-pass state for the gated helpers below, held in
     * a ThreadLocal instead of plain instance fields. One structure piece is
     * shared by EVERY chunk it spans, and postProcess for different chunks
     * of the same piece runs concurrently (vanilla worker threads; constantly
     * under c2me's parallel chunk system, and Distant Horizons' distant
     * generation drives this generator from its own threads too). The
     * previous "hot-loop cache" fields raced two ways: a finishing pass
     * nulled the scratch state out from under a live one (the beta.2
     * Mining-dimension freeze — NPE in {@link #place}, dead chunk, wedged
     * chunk system), and two live passes could adopt each other's chunk-clip
     * boxes, silently writing into the wrong chunk's window. A ThreadLocal
     * keeps the fix inside this class: the ~35 per-structure generator
     * classes call back through these helpers, and their signatures must not
     * change in a hotfix.
     */
    private record PassCtx(WorldGenLevel level, BlockPos.MutableBlockPos mut,
                           int cbMinX, int cbMaxX, int cbMinY, int cbMaxY,
                           int cbMinZ, int cbMaxZ) {}

    private final transient ThreadLocal<PassCtx> passCtx = new ThreadLocal<>();

    /** The current pass's context; non-null exactly inside postProcess. */
    private PassCtx ctx() {
        return passCtx.get();
    }

    public LegacyDungeonPiece(BlockPos origin, DungeonType dungeonType) {
        super(ModStructureTypes.LEGACY_DUNGEON_PIECE.get(), 0,
                new BoundingBox(
                        origin.getX() + dungeonType.minXOff,
                        origin.getY() - dungeonType.downExtent,
                        origin.getZ() + dungeonType.minZOff,
                        origin.getX() + dungeonType.maxXOff,
                        origin.getY() + dungeonType.upExtent,
                        origin.getZ() + dungeonType.maxZOff));
        this.origin = origin.immutable();
        this.dungeonType = dungeonType;
    }

    public LegacyDungeonPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModStructureTypes.LEGACY_DUNGEON_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
        this.dungeonType = DungeonType.valueOf(tag.getString("dt"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("ox", origin.getX());
        tag.putInt("oy", origin.getY());
        tag.putInt("oz", origin.getZ());
        tag.putString("dt", dungeonType.name());
    }

    /**
     * Non-null only on the {@link #buildNow} live-game path: the original
     * Dungeon Spawner Block built with the world RNG
     * (orig DungeonSpawnerBlock.java:52 {@code world.rand}), so repeated
     * builds must not repeat a layout. Worldgen passes leave this null and
     * use the deterministic per-piece seed below (required so every chunk's
     * replay paints the same slice).
     */
    private transient RandomSource runtimeRandomOverride;

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager,
                            ChunkGenerator chunkGenerator, RandomSource random,
                            BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
        // RoyalTreePiece pattern: deterministic per-piece RNG seeded from the
        // bounding box corners so every chunk pass paints the same slice.
        RandomSource rng = runtimeRandomOverride != null ? runtimeRandomOverride
                : RandomSource.create(
                (long) this.boundingBox.minX() * 341873128712L
                        + (long) this.boundingBox.minZ() * 132897987541L);

        this.passCtx.set(new PassCtx(level, new BlockPos.MutableBlockPos(),
                chunkBox.minX(), chunkBox.maxX(), chunkBox.minY(),
                chunkBox.maxY(), chunkBox.minZ(), chunkBox.maxZ()));

        try {
            switch (dungeonType) {
                case SHADOW -> generateShadowDungeon(rng);
                case GREENHOUSE -> generateGreenhouse(rng);
                case ROBOT_LAB -> generateRobotLab(rng);
                case WHITE_HOUSE -> generateWhiteHouse(rng);
                case ALIEN_WTF -> generateAlienWtfDungeon(rng);
                case LEONOPTERYX_NEST -> generateLeonopteryxNest(rng);
                case KING_ALTAR -> generateRoyalAltar(rng, true);
                case QUEEN_ALTAR -> generateRoyalAltar(rng, false);
                case KING_TOWER -> generateChallengeTower(rng, true);
                case QUEEN_TOWER -> generateChallengeTower(rng, false);
                case BASILISK_MAZE -> BasiliskMazeGenerator.generate(this, origin, rng);
                case NIGHTMARE_ROOKERY -> NightmareRookeryGenerator.generate(this, origin, rng);
                case ENDER_CASTLE -> EnderCastleGenerator.generate(this, origin, rng);
                case INCA_PYRAMID -> IncaPyramidGenerator.generate(this, origin, rng);
                case KYUUBI_DUNGEON -> KyuubiDungeonGenerator.generate(this, origin, rng);
                case HOSPITAL -> HospitalGenerator.generate(this, origin, rng);
                case MONSTER_ISLAND -> MonsterIslandGenerator.generate(this, origin, rng);
                case PLAY_POOL -> PlayPoolGenerator.generate(this, origin, rng);
                case CLOUD_SHARK_DUNGEON -> CloudSharkDungeonGenerator.generate(this, origin, rng);
                case GOLD_FISH_BOWL -> GoldFishBowlGenerator.generate(this, origin, rng);
                case SPIT_BUG_LAIR -> SpitBugLairGenerator.generate(this, origin, rng);
                case IGLOO -> {
                    // WGEN-071: worldgen passes re-verify the TRUE snow-block
                    // surface (and may relocate within the anchor chunk)
                    // before building; buildNow bypasses the check (spec §9).
                    BlockPos iglooSite = resolveIglooWorldgenSite(rng, chunkPos);
                    if (iglooSite != null) IglooGenerator.generate(this, iglooSite, rng);
                }
                case ENDER_REAPER_GRAVEYARD -> EnderReaperGraveyardGenerator.generate(this, origin, rng);
                case WATER_DRAGON_LAIR -> WaterDragonLairGenerator.generate(this, origin, rng);
                case LEAF_MONSTER_DUNGEON -> LeafMonsterDungeonGenerator.generate(this, origin, rng);
                case MINI_DUNGEON -> MiniDungeonGenerator.generate(this, origin, rng);
                case CEPHADROME_ALTAR -> CephadromeAltarGenerator.generate(this, origin, rng);
                case BOUNCY_CASTLE -> BouncyCastleGenerator.generate(this, origin, rng);
                case DAMSEL_IN_DISTRESS -> DamselInDistressGenerator.generate(this, origin, rng);
                case GIRLFRIEND_ISLAND -> GirlfriendIslandGenerator.generate(this, origin, rng);
                case STINKY_HOUSE -> StinkyHouseGenerator.generate(this, origin, rng);
                case PUMPKIN -> PumpkinGenerator.generate(this, origin, rng);
                case RAINBOW -> RainbowGenerator.generate(this, origin, rng);
                case SPIDER_HANGOUT -> SpiderHangoutGenerator.generate(this, origin, rng);
                case RED_ANT_HANGOUT -> RedAntHangoutGenerator.generate(this, origin, rng);
                case FROG_POND -> FrogPondGenerator.generate(this, origin, rng);
                case RUBBER_DUCKY_POND -> RubberDuckyPondGenerator.generate(this, origin, rng);
                case HAUNTED_HOUSE -> HauntedHouseGenerator.generate(this, origin, rng);
                case ENDER_KNIGHT_DUNGEON -> EnderKnightDungeonGenerator.generate(this, origin, rng);
            }
        } finally {
            // BUG-033: scoped to this pass only — concurrent passes on other
            // threads keep their own context.
            this.passCtx.remove();
        }
    }

    // ---- Per-cell helpers ----------------------------------------------
    // Visibility note: place/placeSpawner/placeLootChest/spawnPersistent are
    // package-private so per-structure generator classes (BasiliskMazeGenerator,
    // NightmareRookeryGenerator, ...) can build through the same gated writers
    // instead of growing this file by ~500 lines per D6 structure. The
    // cross-chunk stitching contract for those generators: consume RNG
    // unconditionally in every pass, gate only the WRITES — a draw skipped in
    // one chunk pass but taken in another desynchronises the replay.

    /**
     * Live-game entry for the Dungeon Spawner Block outcome pool
     * (orig DungeonSpawnerBlock.java:52-202): builds {@code type} immediately
     * at {@code origin} on a real {@link ServerLevel}, bypassing the
     * structure-start machinery. The piece's whole bounding box is the write
     * window, so nothing is chunk-clipped, and the level RNG replaces the
     * deterministic per-position seed — the original built this path with
     * {@code world.rand}, so rebuilding at the same spot must not repeat a
     * layout.
     */
    public static void buildNow(ServerLevel level, BlockPos origin, DungeonType type) {
        LegacyDungeonPiece piece = new LegacyDungeonPiece(origin, type);
        piece.runtimeRandomOverride = level.random;
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
                level.random, piece.getBoundingBox(), new ChunkPos(origin), origin);
    }

    /** Returns true iff the target cell is inside the per-chunk write window. */
    boolean inChunk(int x, int y, int z) {
        PassCtx c = ctx();
        return x >= c.cbMinX() && x <= c.cbMaxX()
                && y >= c.cbMinY() && y <= c.cbMaxY()
                && z >= c.cbMinZ() && z <= c.cbMaxZ();
    }

    /**
     * Gated PRE-BUILD terrain probe for the one sanctioned read pattern:
     * originals that read the world at the SAME cell they are about to write
     * (royal-altar dirt skirt; Ender Reaper graveyard air-only skirt,
     * orig GenericDungeon.java:2500; Leaf Monster foundation roots, :2113).
     * Legal ONLY for read-cell == write-cell probes — each cell's outcome
     * depends solely on pre-build terrain inside the current pass, so chunk
     * replays stay consistent. Returns {@code null} outside the current
     * write window: treat as "unknown, skip", keeping read and write gated
     * on the same cell. Any other read shape (neighbouring cells, own prior
     * writes) must use an in-memory model instead (pattern doc §1 step 3).
     */
    BlockState terrainStateIfInChunk(int x, int y, int z) {
        if (!inChunk(x, y, z)) return null;
        PassCtx c = ctx();
        c.mut().set(x, y, z);
        return c.level().getBlockState(c.mut());
    }

    /**
     * WGEN-071 &mdash; the Igloo's generation-time TRUE-surface
     * re-verification (igloo_spec.md &sect;7.3 ruling; orig
     * OreSpawnWorld.java:1265-1275). The original double-gated worldgen
     * igloos: corner biome named EXACTLY "Ice Plains" (:1263-1264, excludes
     * "Ice Plains Spikes") AND, per attempted column, air directly over a
     * SNOW BLOCK &mdash; {@code field_150433_aE}, the full block, NOT the
     * snow layer (:1270). The biome half rides on the structure's
     * snowy_plains-only tag; the snow-block half cannot (plain snowy-plains
     * surfaces are grass under a snow LAYER, which the original scan
     * rejected &mdash; spec &sect;1.3), so per the &sect;7.3 ruling BOTH
     * gates are reproduced mechanically, with NO invented frequency: this
     * check probes the real chunk at generation time and, when the anchored
     * column fails, tries the original's remaining jittered attempts within
     * the anchor chunk (4 total, :1265-1267), RELOCATING the build to the
     * first column that passes. When none passes the igloo generates
     * NOTHING (silent no-op) &mdash; the faithful border-artifact filter:
     * in modern snowy_plains, snow-block surfaces occur almost exclusively
     * where ice_spikes floor noise bleeds across the biome border,
     * reproducing the original's ~zero frequency inside plain Ice Plains.
     * &sect;1.3 caveat: the 1.7.10 vanilla-terrain claim behind that story
     * is unverified against 1.7.10 vanilla itself (vanilla sources are not
     * in the reference tree).
     *
     * <p>Stitching-contract notes (why this read shape is legal even though
     * it is not read-cell == write-cell):</p>
     * <ul>
     * <li><b>Reads are region-safe.</b> Every candidate column lies in the
     *     ANCHOR chunk, and every chunk pass that can reach the probe is
     *     within 1 chunk of it (the early exit below), so the reads stay
     *     inside the features-stage 3&times;3 read neighbourhood.</li>
     * <li><b>Verdicts are pass-stable against self-writes.</b> The check
     *     reads exactly two fixed-Y cells per column ({@code anchorY + 2}
     *     air, {@code anchorY + 1} snow block). The igloo's own writes can
     *     never flip a verdict: wall cells pair snow at +1 with ICE at +2
     *     (fails the air test), the doorway punch pairs air at +2 with a
     *     door at +1, spawners/chest replace +1 with non-snow, and the
     *     build column's own two probe cells are never written (spec
     *     &sect;2). Columns are consulted in a fixed order and the first
     *     pass wins, so every chunk replay reaches the same verdict.</li>
     * <li><b>Snow LAYERS count as the air cell, never the block below.</b>
     *     At surface_structures time in a pre-decoration chunk no layer can
     *     exist yet, so this never admits a column the strict air test
     *     would reject; it exists solely so passes that re-run the check
     *     AFTER the anchor chunk's freeze_top_layer step (which drops a
     *     layer onto open snow surfaces &mdash; including through the
     *     dome's apex skylight) still reach the same verdict instead of
     *     shearing the neighbour slices. Later-step vegetal features
     *     overwriting a probe cell between passes remain a residual,
     *     accepted hazard (same family as every terrain probe's).</li>
     * <li><b>RNG:</b> the 3 remaining jitter attempts are drawn up front,
     *     unconditionally, from the deterministic piece random &mdash;
     *     identical in every pass. (findGenerationPoint's own 4 noise-level
     *     attempts ran on the structure-seed stream and are not replayable
     *     here; the piece re-draws its retries &mdash; documented delta.)
     *     The far-chunk early exit skips the draws, which is safe because
     *     the igloo generator itself consumes ZERO draws (spec &sect;11);
     *     revisit if that ever changes.</li>
     * </ul>
     *
     * @return the build origin for this igloo (the anchored column or a
     *         relocated in-chunk attempt column at the same Y), or
     *         {@code null} to generate nothing. The buildNow path (DSB type
     *         20, orig DungeonSpawnerBlock.java:113-115) returns the origin
     *         unconditionally &mdash; it bypassed biome, scan and the
     *         &minus;2 sink in the original too (spec &sect;9).
     */
    private BlockPos resolveIglooWorldgenSite(RandomSource rng, ChunkPos chunkPos) {
        if (runtimeRandomOverride != null) return origin;   // DSB type 20 — no gates (spec §9)
        ChunkPos anchorChunk = new ChunkPos(origin);
        // Chunks more than 1 chunk from the anchor chunk can never contain
        // igloo writes (candidate columns span the anchor chunk; the shell
        // reaches at most −6..+7 from its column) — nothing to do there.
        if (Math.abs(chunkPos.x - anchorChunk.x) > 1
                || Math.abs(chunkPos.z - anchorChunk.z) > 1) {
            return null;
        }
        // The original's remaining jitter attempts (orig :1265-1267; attempt
        // #1 is the column findGenerationPoint anchored). Drawn up front,
        // unconditionally — see the RNG note in the Javadoc.
        int[] jitterX = new int[3];
        int[] jitterZ = new int[3];
        for (int i = 0; i < 3; i++) {
            jitterX[i] = anchorChunk.getMinBlockX() + rng.nextInt(16);
            jitterZ[i] = anchorChunk.getMinBlockZ() + rng.nextInt(16);
        }
        if (iglooColumnHasSnowBlockSurface(origin.getX(), origin.getZ())) {
            return origin;
        }
        for (int i = 0; i < 3; i++) {
            if (iglooColumnHasSnowBlockSurface(jitterX[i], jitterZ[i])) {
                return new BlockPos(jitterX[i], origin.getY(), jitterZ[i]);
            }
        }
        return null;   // silent no-op — the faithful border-artifact filter
    }

    /**
     * One column of {@link #resolveIglooWorldgenSite}'s probe, mirroring
     * orig OreSpawnWorld.java:1270-1272 at the anchored Y: air (or a
     * post-decoration snow LAYER &mdash; see the stability note above) at
     * {@code firstFree = anchorY + 2}, and {@code minecraft:snow_block}
     * &mdash; the FULL block, never the layer &mdash; at {@code firstFree
     * - 1}. Retry columns are held to the anchor's Y rather than re-scanned
     * Y 100&rarr;41: the piece box fixes the build Y, and a full re-scan
     * would read cells the igloo itself writes (air above wall snow),
     * breaking pass stability &mdash; documented mechanical delta.
     */
    private boolean iglooColumnHasSnowBlockSurface(int x, int z) {
        int firstFree = origin.getY() + 2;   // anchor = firstFree − 2 (orig OSW:1271)
        PassCtx c = ctx();
        c.mut().set(x, firstFree, z);
        BlockState above = c.level().getBlockState(c.mut());
        if (!above.isAir() && !above.is(Blocks.SNOW)) return false;   // orig :1270 air test
        c.mut().set(x, firstFree - 1, z);
        return c.level().getBlockState(c.mut()).is(Blocks.SNOW_BLOCK); // field_150433_aE, orig :1270
    }

    /** Gated {@code level.setBlock} ({@link #FLAG_PIECE_WRITE}). */
    void place(int x, int y, int z, BlockState state) {
        if (!inChunk(x, y, z)) return;
        PassCtx c = ctx();
        c.mut().set(x, y, z);
        c.level().setBlock(c.mut(), state, FLAG_PIECE_WRITE);
    }

    /**
     * Gated chest placement bound to a data-driven loot table — the Phase C
     * treatment approved for the generic/ruby dungeon chest lists (roll counts
     * and weights live in the JSON; the table rolls when a player first opens
     * the chest). Used by the D5+ structure generators in place of the older
     * code-side {@link ChestFiller} fills.
     */
    void placeLootChest(int x, int y, int z, ResourceKey<LootTable> lootTable) {
        placeLootChest(x, y, z, null, lootTable);
    }

    /**
     * {@link #placeLootChest(int, int, int, ResourceKey)} with an explicit
     * facing. The originals stamped chest facing metadata after placement
     * (e.g. GD:744 meta 5 = faces east, so each Challenge Tower chest faces
     * the room centre); a default chest state loses that (WGEN-056).
     * {@code facing == null} keeps the default state.
     */
    void placeLootChest(int x, int y, int z, Direction facing, ResourceKey<LootTable> lootTable) {
        if (!inChunk(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        WorldGenLevel level = ctx().level();
        level.setBlock(pos, chestState(facing), FLAG_PIECE_WRITE);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(lootTable);
        }
    }

    private static BlockState chestState(Direction facing) {
        BlockState state = Blocks.CHEST.defaultBlockState();
        return facing == null ? state
                : state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    /**
     * Gated direct entity spawn, porting the original's {@code spawnCreature}
     * pattern (orig BasiliskMaze.java:243-252: createEntityByName +
     * setLocationAndAngles with a random yaw + spawnEntityInWorld +
     * playLivingSound) plus the callers' {@code func_110163_bv()} =
     * {@link Mob#setPersistenceRequired()}. The yaw is drawn by the CALLER so
     * the RNG stream stays identical across chunk passes even when the spawn
     * position falls outside the current write window. The ambient-sound call
     * is inaudible during worldgen (no players track an ungenerated chunk) but
     * matters on the live Dungeon Spawner Block path, which builds through
     * this same code.
     */
    void spawnPersistent(EntityType<? extends Mob> type, double x, double y, double z, float yawDegrees) {
        spawnPersistent(type, x, y, z, yawDegrees, true);
    }

    /**
     * {@link #spawnPersistent(EntityType, double, double, double, float)}
     * with the ambient sound controllable: originals that spawned via the
     * {@code spawnCreature} helper played the mob's living sound
     * (orig BasiliskMaze.java:243-252), but the bare
     * {@code createEntity + setLocationAndAngles + spawnEntityInWorld}
     * spawns (Spider/Red Ant Hangouts, orig GD:7038-7042 / :7064-7068) did
     * not — those callers pass {@code false} so the live Dungeon Spawner
     * Block path stays silent like the original (D6b batch-4 verify fix).
     */
    void spawnPersistent(EntityType<? extends Mob> type, double x, double y, double z,
                         float yawDegrees, boolean ambientSound) {
        if (!inChunk(Mth.floor(x), Mth.floor(y), Mth.floor(z))) return;
        WorldGenLevel level = ctx().level();
        Mob mob = type.create(level.getLevel());
        if (mob == null) return;
        mob.moveTo(x, y, z, yawDegrees, 0.0f);
        mob.setPersistenceRequired();
        level.addFreshEntityWithPassengers(mob);
        if (ambientSound) mob.playAmbientSound();
    }

    /**
     * Gated direct spawn for NON-mob entities (Phase D6a: the Hospital's four
     * {@code EntityEnderCrystal}s, orig GenericDungeon.java:2906-2921 — spawned
     * with a random yaw and no persistence flag; end crystals never despawn).
     * Same stitching contract as {@link #spawnPersistent}: the caller draws the
     * yaw so the RNG stream is identical in every chunk pass.
     */
    void spawnEntity(EntityType<? extends net.minecraft.world.entity.Entity> type,
                     double x, double y, double z, float yawDegrees) {
        if (!inChunk(Mth.floor(x), Mth.floor(y), Mth.floor(z))) return;
        WorldGenLevel level = ctx().level();
        net.minecraft.world.entity.Entity entity = type.create(level.getLevel());
        if (entity == null) return;
        entity.moveTo(x, y, z, yawDegrees, 0.0f);
        level.addFreshEntityWithPassengers(entity);
    }

    /** Gated chest placement + immediate fill (within the same postProcess pass). */
    private void placeChest(int x, int y, int z, ChestFiller filler, RandomSource random) {
        placeChest(x, y, z, null, filler, random);
    }

    /** {@link #placeChest(int, int, int, ChestFiller, RandomSource)} with an
     *  explicit facing (see {@link #placeLootChest(int, int, int, Direction,
     *  ResourceKey)} for why facing matters). */
    private void placeChest(int x, int y, int z, Direction facing, ChestFiller filler, RandomSource random) {
        if (!inChunk(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        WorldGenLevel level = ctx().level();
        level.setBlock(pos, chestState(facing), FLAG_PIECE_WRITE);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            filler.fill(chest, random);
        }
    }

    /** Gated spawner placement + immediate {@link EntityType} bind. */
    void placeSpawner(int x, int y, int z, EntityType<?> mob) {
        if (!inChunk(x, y, z)) return;
        BlockPos pos = new BlockPos(x, y, z);
        WorldGenLevel level = ctx().level();
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), FLAG_PIECE_WRITE);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(mob, null, level.getRandom(), pos);
        }
    }

    @FunctionalInterface
    private interface ChestFiller {
        void fill(ChestBlockEntity chest, RandomSource random);
    }

    // ---- Shadow Dungeon ------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeShadowDungeon} (1.7.10
     * source line 1453&ndash;1537). Two stacked pyramids: the lower
     * 19&rarr;1 stepped pyramid descends 10 blocks below origin with
     * obsidian/bedrock walls (alternating by Y parity), soul-sand cross-
     * pattern at midpoint, four corner Nightmare/Ender Reaper spawners
     * on every odd-Y ring (width 9&ndash;15), and four chest fills filled
     * from {@link #shadowContentsList}. The upper pyramid is the same
     * 19&rarr;1 stepped layout going up 10 blocks (no spawners or
     * chests &mdash; pure visual cap, legacy line 1519).
     */
    private void generateShadowDungeon(RandomSource random) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int totalWidth = 19;
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();

        // Phase 1 — lower pyramid descending below origin (legacy line 1467-1515).
        int yoff = 0;
        int xoff = 0;
        int zoff = 0;
        for (int width = totalWidth; width > 0; width -= 2) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < width; k++) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        BlockState blk = obsidian;
                        if ((yoff & 1) != 0) blk = bedrock;
                        // Soul-sand cross at midpoint windows (line 1475).
                        if (k >= width / 2 - 1 && k <= width / 2 + 1
                                || i >= width / 2 - 1 && i <= width / 2 + 1) {
                            blk = soulSand;
                        }
                        place(cposx + i + xoff, cposy - yoff, cposz + k + zoff, blk);
                    } else {
                        place(cposx + i + xoff, cposy - yoff, cposz + k + zoff, air);
                    }
                }
            }
            // Spawner + chest tier (legacy line 1484-1511).
            if (width <= 15 && width >= 9) {
                EntityType<?> mob;
                if ((yoff & 1) != 0) {
                    fillShadowChests(cposx + xoff, cposy - yoff, cposz + zoff, width, 0, random);
                    mob = ModEntities.ENDER_REAPER.get();
                } else {
                    mob = ModEntities.PITCH_BLACK.get();
                }
                placeSpawner(cposx + xoff + 1, cposy - yoff, cposz + zoff + 1, mob);
                placeSpawner(cposx + xoff + width - 2, cposy - yoff, cposz + zoff + 1, mob);
                placeSpawner(cposx + xoff + 1, cposy - yoff, cposz + zoff + width - 2, mob);
                placeSpawner(cposx + xoff + width - 2, cposy - yoff, cposz + zoff + width - 2, mob);
            }
            xoff++;
            zoff++;
            yoff++;
        }

        // Phase 2 — upper pyramid rising above origin (legacy line 1519-1535).
        yoff = 0;
        xoff = 0;
        zoff = 0;
        for (int width = totalWidth; width > 0; width -= 2) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < width; k++) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        BlockState blk = obsidian;
                        if ((yoff & 1) != 0) blk = bedrock;
                        place(cposx + i + xoff, cposy + yoff, cposz + k + zoff, blk);
                    } else {
                        place(cposx + i + xoff, cposy + yoff, cposz + k + zoff, air);
                    }
                }
            }
            xoff++;
            zoff++;
            yoff++;
        }
    }

    /** Direct port of {@code fill_shadow_chests} (1.7.10 line 1539-1568). */
    private void fillShadowChests(int cposx, int cposy, int cposz, int width, int height, RandomSource random) {
        placeChest(cposx + 1, cposy + height, cposz + width / 2, this::fillShadowChest, random);
        placeChest(cposx + width - 2, cposy + height, cposz + width / 2, this::fillShadowChest, random);
        placeChest(cposx + width / 2, cposy + height, cposz + 1, this::fillShadowChest, random);
        placeChest(cposx + width / 2, cposy + height, cposz + width - 2, this::fillShadowChest, random);
    }

    /**
     * Authentic {@code shadowContentsList} (1.7.10 line 52). Each entry
     * preserves the legacy weight by frequency: 13 entries with weights
     * 20/20/15/15/15/25/25/15/15/15/15/15/10. The fill loop runs
     * {@code 3 + nextInt(7)} insertions per legacy {@code
     * WeightedRandomChestContent.func_76293_a} call.
     */
    private void fillShadowChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(Items.GLOWSTONE_DUST, 2 + random.nextInt(7)),     // weight 20
                new ItemStack(Items.GLOWSTONE_DUST, 2 + random.nextInt(7)),     //   (dup for 20 weight)
                new ItemStack(Items.BLAZE_ROD, 4 + random.nextInt(5)),          // weight 20
                new ItemStack(Items.BLAZE_ROD, 4 + random.nextInt(5)),          //   (dup)
                new ItemStack(Items.MAGMA_CREAM, 2 + random.nextInt(7)),        // weight 15
                new ItemStack(Items.BLAZE_POWDER, 2 + random.nextInt(7)),       // weight 15
                new ItemStack(Items.FIRE_CHARGE, 4 + random.nextInt(5)),        // weight 15
                new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)),      // weight 25
                new ItemStack(Items.ROTTEN_FLESH, 6 + random.nextInt(11)),      //   (dup)
                new ItemStack(Items.RED_DYE, 6 + random.nextInt(11)),           // weight 25
                new ItemStack(Items.RED_DYE, 6 + random.nextInt(11)),           //   (dup)
                new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(7)),      // weight 15
                new ItemStack(ModItems.EXPERIENCE_TREE_SEED.get(), 2 + random.nextInt(3)), // weight 15
                new ItemStack(ModItems.ELEVATOR.get(), 1),                      // weight 15
                new ItemStack(ModItems.NIGHTMARE_SWORD.get(), 1),               // weight 15
                new ItemStack(ModItems.POISON_SWORD.get(), 1),                  // weight 15
                new ItemStack(ModItems.RAT_SWORD.get(), 1),                     // weight 10
                new ItemStack(ModItems.RUBY_SWORD.get(), 1),                    // weight 10
                new ItemStack(ModItems.BIG_HAMMER.get(), 1),                    // weight 15
                new ItemStack(ModItems.INGOT_TITANIUM.get(), 1),                // weight 5
                new ItemStack(ModItems.INGOT_URANIUM.get(), 1),                 // weight 5
                new ItemStack(ModItems.ULTIMATE_SWORD.get(), 1),                // weight 10
                new ItemStack(ModItems.ULTIMATE_BOW.get(), 1),                  // weight 10
                new ItemStack(ModItems.ENDER_REAPER_SPAWN_EGG.get(), 2 + random.nextInt(7)), // weight 15
                new ItemStack(ModItems.PITCH_BLACK_SPAWN_EGG.get(), 2 + random.nextInt(7))   // weight 15
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(7);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- Greenhouse ----------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeGreenhouseDungeon} (1.7.10
     * source line 5030&ndash;5168). 23&times;15&times;7 glass-walled box
     * with iron-block ceiling stripes, glass-pane skylight strip every
     * 4 columns, glowstone every 4&times;4 grid intersection, grass
     * floor with water-irrigation strips every 3 rows, randomized crop
     * planting on farmland, two stacked Triffid spawners on the centre
     * of the roof, and a {@link #fillGreenhouseChest} chest beneath them.
     */
    private void generateGreenhouse(RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        int height = 7;
        int width = 15;
        int length = 23;

        // Centre the box on origin so /locate puts the player inside the
        // greenhouse, not at the SW corner.
        int ox = cposx - length / 2;
        int oz = cposz - width / 2;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7);

        for (int i = 0; i < length; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = 0; j < height; j++) {
                    BlockState blk = air;
                    if (i == 0 || k == 0 || i == length - 1 || k == width - 1) {
                        blk = glass;
                    }
                    if (j == height - 1) {
                        blk = iron;
                        if (i % 4 == 3 && k % 4 == 3) blk = glowstone;
                        if (k % 4 == 1) blk = glass;
                    }
                    if (j == 0) {
                        blk = grass;
                        if (i != 0 && k != 0 && i != length - 1 && k != width - 1 && i % 3 == 2) {
                            blk = water;
                        }
                    }
                    if (j == 1 && i != 0 && k != 0 && i != length - 1 && k != width - 1
                            && i % 3 != 2 && random.nextInt(3) != 1) {
                        // Lay farmland one block below + plant on top
                        // (legacy line 5064-5126).
                        place(ox + i, cposy + j - 1, oz + k, farmland);
                        BlockState plant = pickGreenhousePlant(random.nextInt(20));
                        place(ox + i, cposy + j, oz + k, plant);
                        continue;
                    }
                    place(ox + i, cposy + j, oz + k, blk);
                }
            }
        }
        // Hollow out the headroom above the greenhouse (legacy line 5131-5137).
        for (int i = 0; i < length; i++) {
            for (int k = 0; k < width; k++) {
                for (int j = height; j <= height + 6; j++) {
                    place(ox + i, cposy + j, oz + k, air);
                }
            }
        }
        // Double-iron-door entry with stone lintels + buttons (legacy line
        // 5138-5147 — the identical entry pattern as the Robot Lab hangar's
        // GD:4076-4083: two ItemDoor.func_150924_a(dir=3) doors at width/2
        // and width/2-1 on the z=0 wall, lintel stones, meta-4 buttons).
        // D6b close-out fix (dsb_sweep_spec.md F1): the port previously
        // placed a single door column at the WRONG x (ox + length/2) and
        // omitted the second door, both lintels, and both buttons. States
        // per the D6a-verified robot-lab trace: dir 3 = FACING NORTH
        // (1.7.10 door meta 0=east/1=south/2=west/3=north), east leaf
        // HINGE=LEFT, west leaf HINGE=RIGHT.
        place(ox + width / 2, cposy + 1, oz, air);                   // orig :5138
        place(ox + width / 2, cposy + 2, oz, air);                   // orig :5139
        place(ox + width / 2 - 1, cposy + 1, oz, air);               // orig :5140
        place(ox + width / 2 - 1, cposy + 2, oz, air);               // orig :5141
        BlockState ghDoorEast = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT);
        BlockState ghDoorWest = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.RIGHT);
        place(ox + width / 2, cposy + 1, oz, ghDoorEast);            // orig :5142
        place(ox + width / 2, cposy + 2, oz,
                ghDoorEast.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        place(ox + width / 2 - 1, cposy + 1, oz, ghDoorWest);        // orig :5143
        place(ox + width / 2 - 1, cposy + 2, oz,
                ghDoorWest.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        place(ox + width / 2 - 2, cposy + 2, oz, Blocks.STONE.defaultBlockState()); // orig :5144 (lintel)
        place(ox + width / 2 + 1, cposy + 2, oz, Blocks.STONE.defaultBlockState()); // orig :5145 (lintel)
        BlockState ghButton = Blocks.STONE_BUTTON.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);  // meta 4 (robot-lab trace)
        place(ox + width / 2 - 2, cposy + 2, oz - 1, ghButton);      // orig :5146
        place(ox + width / 2 + 1, cposy + 2, oz - 1, ghButton);      // orig :5147

        // Roof Triffid spawners (legacy line 5148-5161).
        int i = length / 2;
        int k = width / 2;
        placeSpawner(ox + i, cposy + height + 1, oz + k, ModEntities.ENTITY_TRIFFID.get());
        placeSpawner(ox + i, cposy + height + 2, oz + k, ModEntities.ENTITY_TRIFFID.get());

        // Loot chest at roof base (legacy line 5162-5167).
        placeChest(ox + i, cposy + height, oz + k, this::fillGreenhouseChest, random);
    }

    /**
     * Authentic per-tile RNG (legacy line 5067-5125). Index 8 is the one
     * intentional gap in the legacy table (no plant rolled), so the call
     * must return air on that roll to match drop frequencies. D6b batch-4
     * verify fix: case 7 is REEDS/sugar cane (orig GD:5090-5092,
     * field_150436_aH — not pumpkin) and case 19 is MyRicePlant
     * (GD:5123-5125 → ModBlocks.RICE_PLANT); both had silently drifted.
     */
    private BlockState pickGreenhousePlant(int t) {
        return switch (t) {
            case 0 -> Blocks.DANDELION.defaultBlockState();
            case 1 -> Blocks.POPPY.defaultBlockState();
            case 2 -> Blocks.BROWN_MUSHROOM.defaultBlockState();
            case 3 -> Blocks.RED_MUSHROOM.defaultBlockState();
            case 4 -> Blocks.WHEAT.defaultBlockState();
            case 5 -> Blocks.CARROTS.defaultBlockState();
            case 6 -> Blocks.POTATOES.defaultBlockState();
            case 7 -> Blocks.SUGAR_CANE.defaultBlockState();     // orig GD:5090-5092 (reeds)
            case 9 -> ModBlocks.CORN_3.get().defaultBlockState();
            case 10 -> ModBlocks.TOMATO_3.get().defaultBlockState();
            case 11 -> ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState();
            case 12 -> ModBlocks.BUTTERFLY_PLANT.get().defaultBlockState();
            case 13 -> ModBlocks.MOTH_PLANT.get().defaultBlockState();
            case 14 -> ModBlocks.RADISH_PLANT.get().defaultBlockState();
            case 15 -> ModBlocks.LETTUCE_3.get().defaultBlockState();
            case 16 -> ModBlocks.FLOWER_PINK.get().defaultBlockState();
            case 17 -> ModBlocks.FLOWER_BLUE.get().defaultBlockState();
            case 18 -> ModBlocks.QUINOA_3.get().defaultBlockState();
            case 19 -> ModBlocks.RICE_PLANT.get().defaultBlockState(); // orig GD:5123-5125
            default -> Blocks.AIR.defaultBlockState(); // t == 8 (the one legacy gap)
        };
    }

    /**
     * Authentic {@code GreenhouseContentsList} (1.7.10 line 31). Weights
     * preserved by entry duplication: 35/35/35/35/25/25/25.
     */
    private void fillGreenhouseChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(ModItems.GREEN_GOO.get(), 4 + random.nextInt(7)),
                new ItemStack(ModBlocks.CREEPER_REPELLENT.get(), 4 + random.nextInt(7)),
                new ItemStack(Items.APPLE, 6 + random.nextInt(11)),
                new ItemStack(Items.OAK_SAPLING, 6 + random.nextInt(11)),
                new ItemStack(Items.OAK_LEAVES, 6 + random.nextInt(11)),
                new ItemStack(Items.DIRT, 6 + random.nextInt(11)),
                new ItemStack(Items.OAK_LOG, 6 + random.nextInt(11))
        };
        int slots = chest.getContainerSize();
        int count = 5 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- Robot Lab -----------------------------------------------------

    /**
     * Loot for the two treasure-room chests: full transcription of
     * {@code RobotContentsList} (1.7.10 GenericDungeon.java line 37 — 23
     * entries, total weight 755, incl. the intentional duplicate rail and
     * redstone-torch entries) at the original fill count {@code 10 +
     * nextInt(5)} (GD:4344/4349 → rolls uniform 10..14).
     */
    private static final ResourceKey<LootTable> ROBOT_LAB_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath("orespawn", "chests/robot_lab"));

    /**
     * Direct port of {@code GenericDungeon.makeRobotLab} (1.7.10 source
     * line 4044&ndash;4091) and its sub-methods. Builds, in original order:
     * <ul>
     *   <li>Entry hall: 10&times;20&times;5 quartz box with iron-block
     *       floor stripe (line 4053-4075), 2&times;2 doorway carve + two
     *       full iron doors + two stone buttons (line 4076-4083).</li>
     *   <li>Main hangar 30&times;30&times;9 &mdash; built FIRST, before the
     *       entry pillars (line 4084; south of entry, x-shifted &minus;10):
     *       quartz walls, iron-block floor stripe, 10-wide door opening
     *       back into the entry hall (line 4127-4158), plus its five annex
     *       calls (line 4159-4163): Robo-Pounder altar (line 4223-4258),
     *       redstone railway (line 4260-4293), assembly line with
     *       sticky-piston crushers (line 4295-4308), Robo-Warrior treasure
     *       room with iron bars + 2 loot chests (line 4310-4351), and the
     *       rooftop sniper tower: 12&times;12 pad + iron-bar railing +
     *       4 inner Robo-Sniper pillars + 30-tall central spire
     *       (line 4166-4221).</li>
     *   <li>Six Robo-Sniper entry pillars, AFTER the hangar: three on each
     *       wall at z = length/3, 2*length/3, length&minus;1
     *       (line 4085-4090).</li>
     * </ul>
     *
     * <p><b>Build order matters</b>: the hangar's k=0 door carve
     * (line 4152-4154) blows air through the shared wall row (z rel 19);
     * the two rear pillars (line 4087/4090) then repair those columns and
     * re-seat their spawners. A pillars-first order silently deletes both
     * rear Robo-Sniper spawners (robot_lab_audit_spec.md §18 item 2).</p>
     *
     * <p>This generator draws NO randomness: the original's only
     * {@code world.rand} use was the chest fills (GD:4344/4349), which are
     * data-driven here ({@link #ROBOT_LAB_LOOT}), so the RNG stream is
     * trivially identical in every chunk pass.</p>
     */
    private void generateRobotLab(RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        // Centre on origin so /locate matches the visual centre of the lab.
        // (Invented recentring, kept + documented: the original built
        // NE-ward from its anchor; this is a constant -5/-25 shift of the
        // whole build, geometry unchanged — audit §18 item 10.)
        int ox = cposx - 5;
        int oz = cposz - 25;

        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // Entry hall (legacy line 4053-4075).
        int width = 10;
        int length = 20;
        int height = 5;
        for (int j = 0; j <= height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = quartz;
                    if (j == 0) {
                        bid = quartz;
                        if (i == width / 2 || i == width / 2 - 1) bid = iron;
                    }
                    if (j == height) {
                        bid = quartz;
                        if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = air;
                    }
                    place(ox + i, cposy + j, oz + k, bid);
                }
            }
        }
        // Carve the 2x2 doorway in the k=0 wall (legacy line 4076-4079).
        place(ox + width / 2, cposy + 1, oz, air);
        place(ox + width / 2, cposy + 2, oz, air);
        place(ox + width / 2 - 1, cposy + 1, oz, air);
        place(ox + width / 2 - 1, cposy + 2, oz, air);
        // Two FULL iron doors (lower + upper halves, legacy line 4080-4081).
        // The original used vanilla ItemDoor.func_150924_a(dir=3), whose
        // hinge pick and double-door pairing READ neighbouring world blocks
        // — forbidden inside a generator (chunk-pass divergence, audit §17)
        // — so both halves are placed with static states. 1.7.10
        // ItemDoor.func_150924_a dir=3 (GD:4080-4081) is the NORTH-facing
        // placement (1.7.10 door meta 0=east/1=south/2=west/3=north, per the
        // 1.13 DataFixer table): the panel sits flush with the interior/south
        // face of the k=0 wall, i.e. modern FACING=NORTH. Hinges follow the
        // vanilla adjacent-door helper's trace for that facing: east leaf
        // HINGE=LEFT, west leaf HINGE=RIGHT, so the pair opens away from the
        // centre (D6a verification pass corrected an earlier 180° mirror).
        BlockState doorEast = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT);
        BlockState doorWest = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.RIGHT);
        place(ox + width / 2, cposy + 1, oz, doorEast);
        place(ox + width / 2, cposy + 2, oz,
                doorEast.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        place(ox + width / 2 - 1, cposy + 1, oz, doorWest);
        place(ox + width / 2 - 1, cposy + 2, oz,
                doorWest.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        // Two stone buttons at z-1 flanking the doors (legacy line 4082-4083,
        // meta 4 = wall-mounted pointing north, i.e. attached to the outside
        // of the entry wall) — the only legitimate way to open the iron doors.
        BlockState button = Blocks.STONE_BUTTON.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        place(ox + width / 2 - 2, cposy + 2, oz - 1, button);
        place(ox + width / 2 + 1, cposy + 2, oz - 1, button);

        // Main hangar FIRST (legacy line 4084; shifted south + x-shifted -10)
        // — see the build-order note in the method Javadoc.
        makeRoboMain(ox, cposy, oz + length - 1);

        // 6 Robo-Sniper entry pillars AFTER the hangar (legacy line
        // 4085-4090); the two z = length-1 pillars rebuild the shared-wall
        // columns the hangar's door carve just opened, restoring their
        // spawners (legacy overwrite-order dependency, audit §19.3).
        makeRoboPillar(ox, cposy, oz + length / 3, 0);
        makeRoboPillar(ox, cposy, oz + length * 2 / 3, 0);
        makeRoboPillar(ox, cposy, oz + (length - 1), 0);
        makeRoboPillar(ox + width - 1, cposy, oz + length / 3, 1);
        makeRoboPillar(ox + width - 1, cposy, oz + length * 2 / 3, 1);
        makeRoboPillar(ox + width - 1, cposy, oz + (length - 1), 1);
    }

    /** Direct port of {@code makerobopillar} (1.7.10 line 4093-4125). */
    private void makeRoboPillar(int cposx, int cposy, int cposz, int dir) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        for (int j = 0; j < 5; j++) {
            for (int i = -1; i < 2; i++) {
                for (int k = -1; k < 2; k++) {
                    BlockState bid = quartz;
                    if (j == 2 || j == 3) {
                        if (k == 0 && (i == -1 || i == 1)) bid = redstone;
                        if (i == 0 && (k == -1 || k == 1)) bid = redstone;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // "Robo-Sniper" spawner = Robot5 (OreSpawnMain.java line 3719):
        // dir 0 = east face (+1), dir 1 = west face (-1) — legacy line
        // 4111-4124. 10 pillars total (6 entry + 4 tower) = 10 snipers.
        if (dir == 0) {
            placeSpawner(cposx + 1, cposy + 1, cposz, ModEntities.ROBOT_5.get());
        } else {
            placeSpawner(cposx - 1, cposy + 1, cposz, ModEntities.ROBOT_5.get());
        }
    }

    /**
     * Direct port of {@code makerobomain} (1.7.10 line 4127-4164). Takes no
     * {@link RandomSource}: the hangar and all five annexes are fully
     * deterministic (the only legacy RNG, the chest fills at GD:4344/4349,
     * lives in {@link #ROBOT_LAB_LOOT}).
     */
    private void makeRoboMain(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int width = 30;
        int length = 30;
        int height = 9;
        cposx -= 10; // legacy line 4132 — x-shift
        for (int j = 0; j <= height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = quartz;
                    if (j == 0) {
                        bid = quartz;
                        if (i == width / 2 || i == width / 2 - 1) bid = iron;
                    }
                    if (j == height) {
                        bid = quartz;
                        if (i == 0 || k == 0 || i == width - 1 || k == length - 1) bid = air;
                    }
                    // Open south face for the entry hall connection (line 4152).
                    if ((j == 1 || j == 2 || j == 3) && k == 0
                            && i >= width / 3 && i < width * 2 / 3) {
                        bid = air;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // Annex calls in original order (legacy line 4159-4163).
        makeRoboAltar(cposx + width / 2 - 4, cposy, cposz + 6);
        makeRoboRailway(cposx + 3, cposy, cposz + 10);
        makeRoboAssemblyLine(cposx + width - 4, cposy, cposz + 4);
        makeRoboTreasureRoom(cposx + 9, cposy, cposz + 18);
        makeRoboTower(cposx + width / 2 - 6, cposy + height, cposz + length / 2 - 6);
    }

    /** Direct port of {@code makeroboaltar} (1.7.10 line 4223-4258). */
    private void makeRoboAltar(int cposx, int cposy, int cposz) {
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 8; k++) {
                place(cposx + i, cposy, cposz + k, iron);
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int k = 0; k < 6; k++) {
                place(cposx + i + 1, cposy + 1, cposz + k + 1, quartz);
            }
        }
        place(cposx + 2, cposy + 1, cposz + 2, redstone);
        place(cposx + 2, cposy + 2, cposz + 2, torch);
        place(cposx + 5, cposy + 1, cposz + 5, redstone);
        place(cposx + 5, cposy + 2, cposz + 5, torch);
        place(cposx + 5, cposy + 1, cposz + 2, redstone);
        place(cposx + 5, cposy + 2, cposz + 2, torch);
        place(cposx + 2, cposy + 1, cposz + 5, redstone);
        place(cposx + 2, cposy + 2, cposz + 5, torch);
        // Two "Robo-Pounder" spawners (legacy line 4248-4257). Robo-Pounder is
        // registered as Robot2 (OreSpawnMain.java line 3695) — the class
        // numbers do NOT follow difficulty order; ROBOT_4 here was a port bug
        // (robot_lab_audit_spec.md sect. 18 item 1).
        placeSpawner(cposx + 3, cposy + 2, cposz + 3, ModEntities.ROBOT_2.get());
        placeSpawner(cposx + 4, cposy + 2, cposz + 4, ModEntities.ROBOT_2.get());
    }

    /**
     * Direct port of {@code makeroborailway} (1.7.10 line 4260-4293): two
     * parallel 13-long rail lines at x+0 and x+3, all at y+1, z+0..+12. The
     * original's 32 unrolled {@code setBlockFast} calls are compressed into
     * one loop, write-for-write identical: plain rail on rows z+0/1/3/4/5/
     * 7/8/9/11/12, and on rows z+2/6/10 a golden (powered) rail
     * ({@code field_150318_D}, GD:4265/4268/4275/4278/4285/4288) on each
     * line flanked by two floor levers between the tracks (meta 5 = lever
     * on ground, handle on the north-south axis, unpowered; GD:4266-4267,
     * 4276-4277, 4286-4287). The prior port's DETECTOR_RAIL + default wall
     * lever were wrong blocks (robot_lab_audit_spec.md sect. 18 item 6).
     */
    private void makeRoboRailway(int cposx, int cposy, int cposz) {
        BlockState rail = Blocks.RAIL.defaultBlockState();
        BlockState golden = Blocks.POWERED_RAIL.defaultBlockState();
        BlockState floorLever = Blocks.LEVER.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        for (int dz = 0; dz <= 12; dz++) {
            // z%4==2 rows (2, 6, 10) are the boost rows (GD:4265-4288).
            boolean boost = (dz == 2 || dz == 6 || dz == 10);
            BlockState here = boost ? golden : rail;
            place(cposx + 0, cposy + 1, cposz + dz, here);
            place(cposx + 3, cposy + 1, cposz + dz, here);
            if (boost) {
                place(cposx + 1, cposy + 1, cposz + dz, floorLever);
                place(cposx + 2, cposy + 1, cposz + dz, floorLever);
            }
        }
    }

    /**
     * Direct port of {@code makeroboassemblyline} (1.7.10 line 4295-4308):
     * a 2-wide, 24-long quartz belt (GD:4305-4306) with, every third row
     * ({@code k%3==1}), a quartz stair step at x&minus;2 (meta 1 =
     * ascending west, GD:4298), a sticky piston at y+2 (meta 3 = facing
     * south, GD:4299) and a white carpet above it (meta 0, GD:4300); the
     * {@code k%3==0} rows carry floor levers placed PRE-POWERED (meta 13 =
     * 5+8: ground lever, north-south axis, ON — GD:4302-4304), so the
     * piston "crushers" are extended and active from the moment of
     * generation. The prior port's RED_CARPET / RED_WOOL / default-facing
     * piston / unpowered wall lever were all wrong states and killed the
     * animated tableau (robot_lab_audit_spec.md sect. 18 item 7).
     */
    private void makeRoboAssemblyLine(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState stairsWest = Blocks.QUARTZ_STAIRS.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
        BlockState pistonSouth = Blocks.STICKY_PISTON.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.SOUTH);
        BlockState whiteCarpet = Blocks.WHITE_CARPET.defaultBlockState();
        BlockState leverOn = Blocks.LEVER.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.POWERED, true);
        for (int k = 0; k < 24; k++) { // legacy line 4296
            place(cposx, cposy + 1, cposz + k, quartz);
            place(cposx + 1, cposy + 1, cposz + k, quartz);
            if (k % 3 == 1) { // legacy line 4297-4301
                place(cposx - 2, cposy + 1, cposz + k, stairsWest);
                place(cposx, cposy + 2, cposz + k, pistonSouth);
                place(cposx, cposy + 3, cposz + k, whiteCarpet);
            }
            if (k % 3 == 0) { // legacy line 4302-4304
                place(cposx, cposy + 2, cposz + k, leverOn);
            }
        }
    }

    /** Direct port of {@code makerobotreasureroom} (1.7.10 line 4310-4351). */
    private void makeRoboTreasureRoom(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int j = 1; j < 7; j++) {
            for (int i = 0; i < 12; i++) {
                for (int k = 0; k < 8; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == 11 || k == 7) bid = quartz;
                    if (j == 2 && i == 11) bid = bars;
                    if (j == 3 && bid != air) bid = bars;
                    if (!(j != 1 && j != 2 && j != 3 || k != 0 || i != 1 && i != 2)) bid = air;
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // "Robo-Warrior" spawner (legacy line 4336-4340). Robo-Warrior is
        // registered as Robot4 (OreSpawnMain.java line 3711) — the prior
        // port's ROBOT_2 was the inverse of the altar's swap bug
        // (robot_lab_audit_spec.md sect. 18 item 1).
        placeSpawner(cposx + 10, cposy + 1, cposz + 1, ModEntities.ROBOT_4.get());
        // Two RobotContentsList chests, meta 2 = faces north (legacy line
        // 4341-4350); fills are data-driven (rolls 10 + nextInt(5) live in
        // the JSON), replacing the old in-code palette fill.
        placeLootChest(cposx + 8, cposy + 1, cposz + 1, Direction.NORTH, ROBOT_LAB_LOOT);
        placeLootChest(cposx + 6, cposy + 1, cposz + 1, Direction.NORTH, ROBOT_LAB_LOOT);
    }

    /** Direct port of {@code makerobotower} (1.7.10 line 4166-4221). */
    private void makeRoboTower(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        // Two-layer 12x12 base + corner redstone markers.
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 12; i++) {
                for (int k = 0; k < 12; k++) {
                    BlockState bid = air;
                    if (j == 1) {
                        if (i == 0 || k == 0 || i == 11 || k == 11) bid = bars;
                        if (i == 0 && (k == 0 || k == 11)) bid = redstone;
                        if (i == 11 && (k == 0 || k == 11)) bid = redstone;
                    }
                    if (j == 0) bid = quartz;
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // 4 corner Robo-Sniper pillars (legacy line 4193-4196).
        makeRoboPillar(cposx + 4, cposy + 1, cposz + 4, 1);
        makeRoboPillar(cposx + 7, cposy + 1, cposz + 7, 0);
        makeRoboPillar(cposx + 4, cposy + 1, cposz + 7, 1);
        makeRoboPillar(cposx + 7, cposy + 1, cposz + 4, 0);
        // 30-tall iron-bar central spire (legacy line 4197-4220).
        for (int j = 5; j < 35; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 3; k++) {
                    BlockState bid;
                    if (j < 15) {
                        bid = quartz;
                    } else if (j < 25) {
                        bid = (k == 2) ? bars : quartz;
                    } else {
                        if (k == 1) bid = bars;
                        else if (k == 2) bid = air;
                        else bid = quartz;
                    }
                    place(cposx + i + 5, cposy + j, cposz + k + 5, bid);
                }
            }
        }
    }

    // ---- White House ---------------------------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeWhiteHouse} (1.7.10 source
     * line 5423&ndash;5694). Composes 7 sub-builds:
     * <ul>
     *   <li>Two fountains (legacy {@code makefountain}, line 5436).</li>
     *   <li>Quartz walkway (legacy {@code makewalkway}, line 5466).</li>
     *   <li>25&times;25 quartz base with crystal-torch corners
     *       (legacy {@code makewhbase}, line 5487).</li>
     *   <li>23&times;6 quartz walls with checkerboard glass-pane windows
     *       and an iron-door entry (legacy {@code makewhwalls}, line 5507).</li>
     *   <li>13-tier stepped quartz roof + 12-tall cobblestone-wall
     *       flagpole + crystal-torch corners (legacy {@code makewhroof},
     *       line 5554).</li>
     *   <li>Carpet/bed interior (legacy {@code makewhinterior}, line 5599).</li>
     *   <li>4 Criminal spawners + 4 chests at the rear back row
     *       (legacy line 5646-5694).</li>
     * </ul>
     */
    private void generateWhiteHouse(RandomSource random) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        // Centre the structure on the origin so /locate matches the
        // visual centre (legacy code laid the structure between
        // [-5..+22] X and [-15..+19] Z relative to its passed cposx/cposz;
        // the natural pivot is +12,+9 from the SW corner).
        int ox = cposx - 12;
        int oz = cposz - 9;

        makeFountain(ox - 5, cposy, oz - 15);
        makeFountain(ox + 15, cposy, oz - 15);
        makeWalkway(ox + 7, cposy, oz - 15);
        makeWhBase(ox - 4, cposy, oz - 6);
        makeWhWalls(ox - 3, cposy + 2, oz - 5);
        makeWhRoof(ox - 4, cposy, oz - 6);
        makeWhInterior(ox - 1, cposy + 2, oz - 3);
        makeWhSpawnersAndChests(ox - 1, cposy + 2, oz - 3, random);
    }

    /** Direct port of {@code makefountain} (1.7.10 line 5436-5464). */
    private void makeFountain(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 7; i++) {
            for (int k = 0; k < 5; k++) {
                for (int j = 0; j < 15; j++) {
                    BlockState bid = water;
                    if (i == 0 || k == 0 || i == 6 || k == 4) bid = quartz;
                    if (j == 0) bid = quartz;
                    if (j == 1 && i == 3 && k == 2) bid = glowstone;
                    if (j > 1) {
                        bid = air;
                        if (j <= 4 && i == 3 && k == 2) bid = quartz;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // Centre water column at j=5 (legacy line 5461-5463).
        place(cposx + 3, cposy + 5, cposz + 2, Blocks.WATER.defaultBlockState());
        place(cposx + 2, cposy + 5, cposz + 2, Blocks.WATER.defaultBlockState());
        place(cposx + 4, cposy + 5, cposz + 2, Blocks.WATER.defaultBlockState());
    }

    /** Direct port of {@code makewalkway} (1.7.10 line 5466-5485). */
    private void makeWalkway(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 10; k++) {
                for (int j = 0; j < 15; j++) {
                    BlockState bid = quartz;
                    if (j == 1) {
                        bid = air;
                        if (k > 6) bid = quartz;
                    }
                    if (j > 1) bid = air;
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
    }

    /** Direct port of {@code makewhbase} (1.7.10 line 5487-5505). */
    private void makeWhBase(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();
        for (int i = 0; i < 25; i++) {
            for (int k = 0; k < 25; k++) {
                place(cposx + i, cposy + 1, cposz + k, quartz);
                if (i != 0 && i != 24 || k != 0 && k != 24) continue;
                place(cposx + i, cposy + 2, cposz + k, torch);
            }
        }
        for (int i = 1; i < 24; i++) {
            for (int k = 1; k < 24; k++) {
                place(cposx + i, cposy + 2, cposz + k, quartz);
            }
        }
    }

    /** Direct port of {@code makewhwalls} (1.7.10 line 5507-5552). */
    private void makeWhWalls(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 23; i++) {
            for (int k = 0; k < 23; k++) {
                for (int j = 0; j < 6; j++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == 22 || k == 22) bid = quartz;
                    if (j != 0 && bid != air) {
                        if (k == 22) {
                            if ((j & 1) == 1) {
                                if ((i & 1) == 0 || (k & 1) == 0) bid = pane;
                            } else if ((i & 1) == 1 || (k & 1) == 1) bid = pane;
                        } else if (k != 0) {
                            if ((j & 1) == 1) {
                                if (i == 2 || k == 2 || i == 20 || k == 20) bid = pane;
                            } else if (i == 1 || k == 1 || i == 21 || k == 21) bid = pane;
                            if (j > 0 && j < 5 && k > 7 && k < 15) bid = pane;
                        } else if ((j & 1) == 1) {
                            if (i == 2 || k == 2 || i == 20 || k == 20) bid = pane;
                        } else if (i == 1 || k == 1 || i == 21 || k == 21) bid = pane;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        // Full 2-tall iron door entry (legacy line 5548-5551:
        // ItemDoor.func_150924_a dir=3 = FACING NORTH, default hinge — a
        // single door with symmetric wall neighbours keeps vanilla's LEFT).
        // D6b close-out fix (dsb_sweep_spec.md F2): the port previously
        // placed only the LOWER half; the upper half is restored, and the
        // button's facing is corrected SOUTH -> NORTH (1.7.10 button meta 4
        // = north per the D6a-verified robot-lab trace — the button hangs on
        // the z=0 wall block south of it and protrudes north).
        place(cposx + 11, cposy, cposz, air);
        place(cposx + 11, cposy + 1, cposz, air);
        BlockState whDoor = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        place(cposx + 11, cposy, cposz, whDoor);
        place(cposx + 11, cposy + 1, cposz,
                whDoor.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        place(cposx + 12, cposy + 1, cposz - 1, Blocks.STONE_BUTTON.defaultBlockState()
                .setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    /** Direct port of {@code makewhroof} (1.7.10 line 5554-5597). */
    private void makeWhRoof(int cposx, int cposy, int cposz) {
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState stairs = Blocks.QUARTZ_STAIRS.defaultBlockState();
        BlockState wall = Blocks.COBBLESTONE_WALL.defaultBlockState();
        BlockState torch = Blocks.TORCH.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int j = 0; j < 13; j++) {
            for (int i = 0; i < 25 - 2 * j; i++) {
                for (int k = 0; k < 25 - 2 * j; k++) {
                    BlockState bid = air;
                    if (i == 0 || k == 0 || i == 24 - 2 * j || k == 24 - 2 * j) bid = quartz;
                    if (j == 0 && bid != air && (i + k & 1) == 1) bid = stairs;
                    if (j == 12) bid = stairs;
                    place(cposx + i + j, cposy + 8 + j, cposz + k + j, bid);
                    if (i != 0 && i != 24 - 2 * j || k != 0 && k != 24 - 2 * j) continue;
                    place(cposx + i + j, cposy + 8 + j + 1, cposz + k + j, torch);
                }
            }
        }
        // 12-tall flagpole + base cross (legacy line 5575-5591).
        for (int dy = 0; dy <= 11; dy++) {
            place(cposx + 12, cposy + 8 + dy, cposz + 12, wall);
        }
        place(cposx + 11, cposy + 8 + 0, cposz + 12, wall);
        place(cposx + 13, cposy + 8 + 0, cposz + 12, wall);
        place(cposx + 12, cposy + 8 + 0, cposz + 11, wall);
        place(cposx + 12, cposy + 8 + 0, cposz + 13, wall);
        // 4 ground-tier torches (legacy line 5593-5596).
        place(cposx + 11, cposy + 8 + 1, cposz + 12, torch);
        place(cposx + 13, cposy + 8 + 1, cposz + 12, torch);
        place(cposx + 12, cposy + 8 + 1, cposz + 11, torch);
        place(cposx + 12, cposy + 8 + 1, cposz + 13, torch);
    }

    /**
     * Direct port of {@code makewhinterior} (1.7.10 line 5599-5645). The
     * legacy code placed alternating red carpet (legacy {@code field_150370_cb}
     * meta=3) and red bed strips (legacy {@code field_150326_M}) at four
     * carpet-bed pair locations: zoff = 1/7/13 with xoff = 0/11.
     */
    private void makeWhInterior(int cposx, int cposy, int cposz) {
        BlockState carpet = Blocks.RED_CARPET.defaultBlockState();
        BlockState bedFoot = Blocks.RED_BED.defaultBlockState()
                .setValue(BlockStateProperties.BED_PART,
                        net.minecraft.world.level.block.state.properties.BedPart.FOOT);
        BlockState bedHead = Blocks.RED_BED.defaultBlockState()
                .setValue(BlockStateProperties.BED_PART,
                        net.minecraft.world.level.block.state.properties.BedPart.HEAD);
        int[] zoffs = {1, 7, 13};
        int[] xoffs = {0, 11};
        for (int zoff : zoffs) {
            for (int xoff : xoffs) {
                for (int i = 0; i < 8; i++) {
                    place(cposx + xoff + i, cposy, cposz + zoff, carpet);
                    place(cposx + xoff + i, cposy, cposz + zoff + 1, bedFoot);
                    place(cposx + xoff + i, cposy, cposz + zoff + 2, bedHead);
                    place(cposx + xoff + i, cposy, cposz + zoff + 3, carpet);
                }
            }
        }
    }

    /** Direct port of {@code makewhinterior} spawner block (1.7.10 line 5646-5694). */
    private void makeWhSpawnersAndChests(int cposx, int cposy, int cposz, RandomSource random) {
        EntityType<?> criminal = ModEntities.BAND_P.get();
        int zoff = 18;
        for (int xoff : new int[]{2, 6, 12, 16}) {
            placeSpawner(cposx + xoff, cposy + 1, cposz + zoff, criminal);
            placeChest(cposx + xoff, cposy, cposz + zoff, this::fillWhiteHouseChest, random);
        }
    }

    /**
     * Authentic {@code WhiteHouseContentsList} (1.7.10 line 26). Weights:
     * 35/10/10/35/25/35/35/35/35/35/35; preserved by entry duplication.
     */
    private void fillWhiteHouseChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(ModItems.CORN_DOG.get(), 6 + random.nextInt(7)),  // 35
                new ItemStack(ModItems.URANIUM_NUGGET.get(), 2 + random.nextInt(5)), // 10
                new ItemStack(ModItems.TITANIUM_NUGGET.get(), 2 + random.nextInt(5)), // 10
                new ItemStack(ModItems.AMETHYST_GEM.get(), 2 + random.nextInt(5)),    // 35
                new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(5)),      // 25
                new ItemStack(ModItems.BAND_P_SPAWN_EGG.get(), 4 + random.nextInt(7)), // 35
                new ItemStack(Items.EMERALD, 6 + random.nextInt(11)),           // 35
                new ItemStack(Items.PORKCHOP, 6 + random.nextInt(11)),          // 35
                new ItemStack(Items.COOKED_PORKCHOP, 6 + random.nextInt(11)),   // 35
                new ItemStack(Items.DIAMOND, 6 + random.nextInt(11)),           // 35
                new ItemStack(Items.GOLD_INGOT, 6 + random.nextInt(11))         // 35
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- WTF-Alien Dungeon (Audit Part 3) -----------------------------

    /**
     * Direct port of {@code GenericDungeon.makeAlienWTFDungeon} (1.7.10
     * source line 1570&ndash;1691). Three composite phases:
     *
     * <ol>
     *   <li><b>Surface antenna</b> &mdash; 5&times;5&times;5 hollow lapis
     *       block on the surface (legacy line 1581-1591), shell built of
     *       {@code Blocks.LAPIS_BLOCK} ({@code field_150369_x}). The
     *       {@code cposy -= depth - 3} on line 1580 anchors the antenna's
     *       top three blocks below the legacy surface anchor.</li>
     *   <li><b>Descending shaft</b> &mdash; 4&times;4 lapis-walled access
     *       shaft from the antenna down 17 blocks (legacy line 1595-1624)
     *       with a single stone "step" inscribed in a rotating corner per
     *       Y (the legacy {@code switch (s)} on line 1605). The rotating
     *       step block is {@code Blocks.STONE} ({@code field_150348_b}).</li>
     *   <li><b>Four cardinal "Part" rooms</b> &mdash; 9/11/13/15 wide cube
     *       rooms (difficulty 1/2/3/4) opening N/E/W/S of the shaft floor
     *       via 1-tall connecting tubes (legacy {@code makePart}, line
     *       1693-1791). Each room has a quartz floor with central obsidian
     *       cross + obsidian walls/ceiling, {@code difficulty} central
     *       Alien/WTF spawners (50/50 random, line 1740-1762), and 1-4
     *       chests filled from {@link #fillAlienWtfChest}.</li>
     * </ol>
     */
    private void generateAlienWtfDungeon(RandomSource random) {
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();

        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();

        int width = 5;
        int height = 5;
        int depth = 20;
        // Legacy line 1580: cposy -= depth - 3 (antenna sinks below grass).
        int antennaY = cposy - (depth - 3);

        // Phase 1: 5x5x5 hollow lapis antenna (legacy line 1581-1591).
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    boolean shell = i == 0 || j == 0 || k == 0
                            || i == width - 1 || j == height - 1 || k == width - 1;
                    place(cposx + i - 2, antennaY + j, cposz + k - 2, shell ? lapis : air);
                }
            }
        }

        // Phase 2: descending 4x4 shaft (legacy line 1592-1624).
        int s = 0;
        int sx = cposx - 1;
        int sz = cposz - 1;
        for (int j = 3; j < depth; j++) {
            for (int i = 0; i < 4; i++) {
                for (int k = 0; k < 4; k++) {
                    boolean shell = i == 0 || k == 0 || i == 3 || k == 3;
                    place(sx + i, antennaY + j, sz + k, shell ? lapis : air);
                }
            }
            switch (s) {
                case 0 -> place(sx + 1, antennaY + j, sz + 1, stone);
                case 1 -> place(sx + 2, antennaY + j, sz + 1, stone);
                case 2 -> place(sx + 2, antennaY + j, sz + 2, stone);
                default -> place(sx + 1, antennaY + j, sz + 2, stone);
            }
            if (++s > 3) s = 0;
        }

        // Phase 3: four cardinal "Part" rooms (legacy line 1625-1690).
        // Each call centres on the shaft floor and steps further outward.
        sx++;
        sz++;
        // North part — width 9, difficulty 1, dx=+1, dz=+1.
        makeAlienPart(sx, antennaY, sz + 7, 9, 5, 1, 1, 1, random);
        carveAlienConnector(sx, antennaY, sz, 3, 6, 0, 1);
        // East part — width 11, difficulty 2, dx=+1, dz=-1.
        makeAlienPart(sx + 7, antennaY, sz, 11, 6, 1, -1, 2, random);
        carveAlienConnector(sx, antennaY, sz, 6, 3, 1, 0);
        // West part — width 13, difficulty 3, dx=-1, dz=+1.
        makeAlienPart(sx - 7, antennaY, sz, 13, 7, -1, 1, 3, random);
        carveAlienConnector(sx, antennaY, sz, 6, 3, -1, 0);
        // South part — width 15, difficulty 4, dx=-1, dz=-1.
        makeAlienPart(sx, antennaY, sz - 7, 15, 8, -1, -1, 4, random);
        carveAlienConnector(sx, antennaY, sz, 3, 6, 0, -1);
    }

    /** Direct port of {@code makePart} (1.7.10 line 1693-1791). */
    private void makeAlienPart(int cposx, int cposy, int cposz,
                               int width, int height, int dx, int dz,
                               int difficulty, RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();

        // Hollow box (legacy line 1699-1705).
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < width; k++) {
                    place(cposx + i * dx, cposy + j, cposz + k * dz, air);
                }
            }
        }
        // Floor — quartz with central obsidian cross (legacy line 1706-1715).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                BlockState floor = quartz;
                if (i == width / 2 || k == width / 2) floor = obsidian;
                place(cposx + i * dx, cposy, cposz + k * dz, floor);
            }
        }
        // Ceiling — pure obsidian (legacy line 1716-1722).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i * dx, cposy + height, cposz + k * dz, obsidian);
            }
        }
        // X-walls (legacy line 1723-1731).
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                place(cposx + i * dx, cposy + j, cposz + 0 * dz, obsidian);
                place(cposx + i * dx, cposy + j, cposz + (width - 1) * dz, obsidian);
            }
        }
        // Z-walls (legacy line 1732-1739).
        for (int k = 0; k < width; k++) {
            for (int j = 0; j < height; j++) {
                place(cposx + 0 * dx, cposy + j, cposz + k * dz, obsidian);
                place(cposx + (width - 1) * dx, cposy + j, cposz + k * dz, obsidian);
            }
        }
        // Spawners (legacy line 1740-1762). Difficulty controls vertical
        // count; each placement randomly picks Alien (mob index 0) or
        // "WTF?" (mob index 1) — both resolve to ALIEN in 1.21.1 since
        // the legacy mod registered them under the same EntityType.
        EntityType<?> alien = ModEntities.ALIEN.get();
        for (int j = 0; j < difficulty; j++) {
            placeSpawner(cposx + dx * width / 2, cposy + j + 2,
                    cposz + dz * width / 2, alien);
            placeSpawner(cposx + dx * width / 2 + dx, cposy + j + 2,
                    cposz + dz * width / 2 + dz, alien);
        }
        // Tiered chests (legacy line 1763-1791).
        placeChest(cposx + width * dx / 2, cposy + 1, cposz + dz,
                this::fillAlienWtfChest, random);
        if (difficulty > 1) {
            placeChest(cposx + width * dx / 2, cposy + 1, cposz + (width - 2) * dz,
                    this::fillAlienWtfChest, random);
        }
        if (difficulty > 2) {
            placeChest(cposx + dx, cposy + 1, cposz + width / 2 * dz,
                    this::fillAlienWtfChest, random);
        }
        if (difficulty > 3) {
            placeChest(cposx + (width - 2) * dx, cposy + 1, cposz + width / 2 * dz,
                    this::fillAlienWtfChest, random);
        }
    }

    /** Carve the 1-tall connecting tube from shaft floor to a Part room. */
    private void carveAlienConnector(int sx, int sy, int sz, int dx, int dz, int rx, int rz) {
        BlockState air = Blocks.AIR.defaultBlockState();
        // 1-tall tube shaft-side -> Part-side, hugging the centerline.
        for (int n = 0; n < 8; n++) {
            int x = sx + (rx == 0 ? dx / 2 : rx * (1 + n));
            int z = sz + (rz == 0 ? dz / 2 : rz * (1 + n));
            place(x, sy + 1, z, air);
            place(x, sy + 2, z, air);
        }
    }

    /**
     * Authentic {@code AlienWTFContentsList} (1.7.10 line 51). 18 entries:
     * Diamond-Block(15), Ruby(20), Amethyst(20), Uranium-Ingot(5),
     * Titanium-Ingot(5), Ultimate-{Helmet,Body,Legs,Boots}(10 each),
     * Ultimate-Bow(15), Nightmare-Sword(15), Experience-Catcher(15),
     * Ray-Gun(10), Cage-Empty(20), Corn-Dog(20), Bacon(20),
     * Popcorn-Bag(20), Fire-Fish(15). Fill loop is
     * {@code 3 + nextInt(5)} per legacy {@code func_76293_a} call.
     */
    private void fillAlienWtfChest(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] palette = new ItemStack[]{
                new ItemStack(Items.DIAMOND_BLOCK, 1 + random.nextInt(2)),                  // 15
                new ItemStack(ModItems.RUBY.get(), 1),                                      // 20
                new ItemStack(ModItems.RUBY.get(), 1),                                      //   (dup for 20 weight)
                new ItemStack(ModItems.AMETHYST_GEM.get(), 1),                              // 20
                new ItemStack(ModItems.AMETHYST_GEM.get(), 1),                              //   (dup)
                new ItemStack(ModItems.INGOT_URANIUM.get(), 1 + random.nextInt(2)),         // 5
                new ItemStack(ModItems.INGOT_TITANIUM.get(), 1 + random.nextInt(2)),        // 5
                new ItemStack(ModItems.ULTIMATE_HELMET.get(), 1),                           // 10
                new ItemStack(ModItems.ULTIMATE_CHESTPLATE.get(), 1),                       // 10
                new ItemStack(ModItems.ULTIMATE_LEGGINGS.get(), 1),                         // 10
                new ItemStack(ModItems.ULTIMATE_BOOTS_ARMOR.get(), 1),                      // 10
                new ItemStack(ModItems.ULTIMATE_BOW.get(), 1),                              // 15
                new ItemStack(ModItems.NIGHTMARE_SWORD.get(), 1),                           // 15
                new ItemStack(ModItems.EXPERIENCE_CATCHER.get(), 4 + random.nextInt(7)),    // 15
                new ItemStack(ModItems.RAY_GUN.get(), 1),                                   // 10
                new ItemStack(ModItems.CAGE_EMPTY.get(), 1 + random.nextInt(10)),           // 20
                new ItemStack(ModItems.CORN_DOG.get(), 1 + random.nextInt(10)),             // 20
                new ItemStack(ModItems.COOKED_BACON.get(), 1 + random.nextInt(5)),          // 20
                new ItemStack(ModItems.POPCORN_BAG.get(), 2 + random.nextInt(7)),           // 20
                new ItemStack(ModItems.FIRE_FISH.get(), 2 + random.nextInt(7))              // 15
        };
        int slots = chest.getContainerSize();
        int count = 3 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            chest.setItem(random.nextInt(slots), palette[random.nextInt(palette.length)].copy());
        }
    }

    // ---- Leonopteryx Nest (Audit Part 3) ------------------------------

    /**
     * Direct port of {@code GenericDungeon.makeLeonNest} (1.7.10 source
     * line 4677&ndash;4729). Hollow rad=10 dome with a randomized
     * decoration shell on the outer 2 layers (oak-leaves / oak-log /
     * oak-planks / dirt / cobblestone / mossy-cobblestone — legacy
     * {@code which = nextInt(6)} on line 4692). Hollow interior carved
     * to rad-2. Five-block air pocket above the dome (legacy line
     * 4716-4723). Single Leonopteryx spawner placed at
     * {@code cposy - (rad - 4)} (line 4724-4728) so the boss surfaces
     * out of the centre of the dome when triggered.
     */
    private void generateLeonopteryxNest(RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int rad = 10;
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();

        // Phase 1: hollow dome (legacy line 4685-4715).
        for (int j = 0; j <= rad; j++) {
            for (int i = -rad; i <= rad; i++) {
                for (int k = -rad; k <= rad; k++) {
                    int dist = (int) Math.sqrt(j * j + i * i + k * k);
                    if (dist > rad) continue;
                    BlockState bid = air;
                    if (dist >= rad - 2) {
                        // Per-cell RNG palette pick: 6-way uniform random.
                        int which = random.nextInt(6);
                        bid = switch (which) {
                            case 0 -> Blocks.OAK_LEAVES.defaultBlockState();
                            case 1 -> Blocks.OAK_LOG.defaultBlockState();
                            case 2 -> Blocks.OAK_PLANKS.defaultBlockState();
                            case 3 -> Blocks.DIRT.defaultBlockState();
                            case 4 -> Blocks.COBBLESTONE.defaultBlockState();
                            default -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        };
                    }
                    place(cposx + i, cposy - j, cposz + k, bid);
                }
            }
        }

        // Phase 2: 5-block air pocket above the dome (legacy line 4716-4723).
        for (int j = 1; j <= 5; j++) {
            for (int i = -rad; i <= rad; i++) {
                for (int k = -rad; k <= rad; k++) {
                    place(cposx + i, cposy + j, cposz + k, air);
                }
            }
        }

        // Phase 3: central Leonopteryx spawner (legacy line 4724-4728).
        placeSpawner(cposx, cposy - (rad - 4), cposz, ModEntities.LEONOPTERYX.get());
    }

    // ---- Royal Altars (King + Queen, Audit Part 3) --------------------

    /**
     * Unified port of {@code GenericDungeon.makeKingAltar} (line 4353)
     * and {@code makeQueenAltar} (line 5697). Both share the same
     * 51&times;51&times;48 envelope, four corner columns, ceiling
     * plate, portrait wall, and tapered centre altar; only the palette
     * differs. King uses Quartz/Gold/Emerald (legacy {@code field_150371_ca}
     * / {@code field_150340_R} / {@code field_150475_bE}). Queen uses
     * Obsidian/Redstone/Amethyst.
     *
     * <p><b>King portrait wall &mdash;</b> the 33&times;33 pixel sprite
     * encoded in {@code GenericDungeon.king[]} (line 63). Each
     * non-negative entry runs {@code v} consecutive blocks of the
     * current color and toggles the palette; each {@code -1} fills the
     * remaining columns to width 33 with stone and advances the row.
     * The Queen wall uses the identical sprite data but swaps the
     * {@code field_150371_ca} (quartz) palette half for Block-Ruby.</p>
     */
    private void generateRoyalAltar(RandomSource random, boolean king) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        // The legacy generator anchored its SW corner at (cposx, cposz);
        // for a centred /locate hit we shift so the centre of the 51x51
        // pad lands on origin.
        int ox = cposx - 25;
        int oz = cposz - 25;

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState slab = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();

        int width = 51;
        int length = 51;
        int height = 48;

        // Phase 1: clear the build envelope (legacy line 4364-4371).
        for (int j = 0; j <= height + 10; j++) {
            for (int i = -5; i < width + 5; i++) {
                for (int k = -5; k < length + 5; k++) {
                    place(ox + i, cposy + j, oz + k, air);
                }
            }
        }
        // Phase 2: 51x51 grass pad with up-to-10-block dirt skirt
        // (legacy line 4372-4384).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < length; k++) {
                place(ox + i, cposy, oz + k, grass);
                for (int v = 1; v < 10; v++) {
                    if (!inChunk(ox + i, cposy - v, oz + k)) continue;
                    BlockState here = ctx().level().getBlockState(
                            new BlockPos(ox + i, cposy - v, oz + k));
                    if (here.isAir() || here.is(Blocks.SHORT_GRASS) || here.is(Blocks.WATER)) {
                        place(ox + i, cposy - v, oz + k, dirt);
                    }
                }
            }
        }
        // Phase 3: four 5x5x44 corner columns (legacy line 4385-4388 / 5729-5732).
        buildRoyalColumn(ox + 1, cposy + 1, oz + 1, king);
        buildRoyalColumn(ox + width - 8, cposy + 1, oz + length - 8, king);
        buildRoyalColumn(ox + 1, cposy + 1, oz + length - 8, king);
        buildRoyalColumn(ox + width - 8, cposy + 1, oz + 1, king);
        // Phase 4: ceiling plate (legacy line 4389-4402 / 5733-5746).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < length; k++) {
                place(ox + i, cposy + height - 1, oz + k, slab);
            }
        }
        for (int i = -1; i <= width; i++) {
            for (int k = -1; k <= length; k++) {
                place(ox + i, cposy + height, oz + k, slab);
            }
        }
        // Phase 5: 33x33 portrait wall (legacy line 4403 -> makekingbackground
        // at 4476 / 5747 -> makequeenbackground at 5817).
        buildRoyalPortraitWall(ox + 4, cposy + 10, oz + 9, king);
        // Phase 6: centre altar pyramid (legacy line 4404 / 5748).
        buildRoyalCenterAltar(ox + width / 2, cposy, oz + length / 2, king);
    }

    /** Direct port of {@code makekingcolumn} / {@code makequeencolumn}. */
    private void buildRoyalColumn(int cposx, int cposy, int cposz, boolean king) {
        BlockState slab = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState band1 = king ? Blocks.GOLD_BLOCK.defaultBlockState()
                : Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState band2 = king ? Blocks.EMERALD_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int width = 5;
        int length = 5;
        int height = 44;

        // 7x7 cap top + bottom (legacy line 4419-4425 / 5763-5769).
        for (int i = 0; i < width + 2; i++) {
            for (int k = 0; k < length + 2; k++) {
                place(cposx + i, cposy, cposz + k, slab);
                place(cposx + i, cposy + height + 1, cposz + k, slab);
            }
        }
        cposx++; cposz++; cposy++;

        // 5x5x44 hollow column with banded inlays
        // (legacy line 4429-4473 / 5773-5814).
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                for (int k = 0; k < length; k++) {
                    BlockState bid = air;
                    boolean wall = i == 0 || k == 0 || i == width - 1 || k == length - 1;
                    if (wall) bid = slab;
                    if (wall && j % 4 == 0 && (i == 2 || k == 2)) bid = band1;
                    if (wall && j % 4 == 1) {
                        if (i == 1 || k == 1) bid = band1;
                        if (i == 3 || k == 3) bid = band1;
                    }
                    if (wall && j % 4 == 2) {
                        if (i == 1 || k == 1) bid = band1;
                        if (i == 3 || k == 3) bid = band1;
                        if (i == 2 || k == 2) bid = band2;
                    }
                    if (wall && j % 4 == 3) {
                        if (i == 1 || k == 1) bid = band1;
                        if (i == 3 || k == 3) bid = band1;
                    }
                    place(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
    }

    /**
     * Direct port of {@code makekingbackground} / {@code makequeenbackground}.
     * 33&times;33 single-X-slab portrait sprite encoded in
     * {@link #ROYAL_PORTRAIT_DATA}.
     */
    private void buildRoyalPortraitWall(int cposx, int cposy, int cposz, boolean king) {
        BlockState band = king ? Blocks.GOLD_BLOCK.defaultBlockState()
                : Blocks.REDSTONE_BLOCK.defaultBlockState();
        BlockState diamond = Blocks.DIAMOND_BLOCK.defaultBlockState();
        BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState foreground = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_RUBY.get().defaultBlockState();

        int height = 33;
        int width = 33;
        int curz = 0;
        int cury = 0;
        BlockState bid = stone;

        for (int v : ROYAL_PORTRAIT_DATA) {
            if (v < 0) {
                // Newline marker — fill remaining row with stone, advance Y.
                bid = stone;
                while (curz < width) {
                    place(cposx, cposy + cury, cposz + curz, bid);
                    curz++;
                }
                cury++;
                curz = 0;
                continue;
            }
            for (int n = 0; n < v; n++) {
                place(cposx, cposy + cury, cposz + curz, bid);
                curz++;
            }
            bid = (bid == stone) ? foreground : stone;
        }

        // Portrait frame: gold/redstone trim (legacy line 4503-4513).
        for (int i = 0; i < width; i++) {
            place(cposx, cposy - 1, cposz + i, band);
            place(cposx, cposy + height, cposz + i, band);
        }
        for (int i = -1; i <= height; i++) {
            place(cposx, cposy + i, cposz - 1, band);
            place(cposx, cposy + i, cposz + width, band);
        }
        // Portrait corner gems + crystal-torch sconces (legacy line 4515-4522).
        place(cposx, cposy - 2, cposz - 2, diamond);
        place(cposx, cposy + height + 1, cposz + width + 1, diamond);
        place(cposx, cposy - 2, cposz + width + 1, diamond);
        place(cposx, cposy + height + 1, cposz - 2, diamond);
        place(cposx, cposy - 1, cposz - 2, torch);
        place(cposx, cposy + height + 2, cposz + width + 1, torch);
        place(cposx, cposy - 1, cposz + width + 1, torch);
        place(cposx, cposy + height + 2, cposz - 2, torch);
    }

    /** Direct port of {@code makekingcenteraltar} / {@code makequeencenteraltar}. */
    private void buildRoyalCenterAltar(int cposx, int cposy, int cposz, boolean king) {
        BlockState slab = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        BlockState amethyst = ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
        BlockState corner = king ? lapis : amethyst;
        BlockState torch = ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();

        // Layer 0: 21x21 centre + 13x41 cross arms (legacy line 4534-4556).
        layRoyalRect(cposx, cposy, cposz, 10, 10, slab);
        layRoyalRect(cposx, cposy, cposz, 6, 20, slab);
        layRoyalRect(cposx, cposy, cposz, 20, 6, slab);

        // Layer 1: 17x17 centre + 9x37 cross with corner gems (legacy line 4557-4595).
        layRoyalRect(cposx, cposy + 1, cposz, 8, 8, slab);
        layRoyalCrossWithCorners(cposx, cposy + 1, cposz, 4, 18, slab, corner);
        layRoyalCrossWithCorners(cposx, cposy + 1, cposz, 18, 4, slab, corner);

        // Layer 2: 15x15 + 7x35 cross with corner torches (legacy line 4596-4627).
        for (int i = -7; i <= 7; i++) {
            for (int k = -7; k <= 7; k++) {
                place(cposx + i, cposy + 2, cposz + k, slab);
                if (i == 7 && (k == -7 || k == 7)) {
                    place(cposx + i, cposy + 3, cposz + k, torch);
                }
                if (i == -7 && (k == -7 || k == 7)) {
                    place(cposx + i, cposy + 3, cposz + k, torch);
                }
            }
        }
        layRoyalRect(cposx, cposy + 2, cposz, 3, 17, slab);
        layRoyalRect(cposx, cposy + 2, cposz, 17, 3, slab);

        // Layer 3: 13x13 + 5x33 cross (legacy line 4628-4654).
        layRoyalRect(cposx, cposy + 3, cposz, 6, 6, slab);
        layRoyalRect(cposx, cposy + 3, cposz, 2, 16, slab);
        layRoyalRect(cposx, cposy + 3, cposz, 16, 2, slab);

        // Layer 4: 5x5 cap + corner torches (legacy line 4655-4668).
        for (int i = -2; i <= 2; i++) {
            for (int k = -2; k <= 2; k++) {
                place(cposx + i, cposy + 4, cposz + k, slab);
                if (i == 2 && (k == -2 || k == 2)) {
                    place(cposx + i, cposy + 5, cposz + k, torch);
                }
                if (i == -2 && (k == -2 || k == 2)) {
                    place(cposx + i, cposy + 5, cposz + k, torch);
                }
            }
        }

        // Apex chest with the boss spawn egg in slot 13 (legacy line 4669-4674).
        if (inChunk(cposx, cposy + 4, cposz)) {
            BlockPos chestPos = new BlockPos(cposx, cposy + 4, cposz);
            // Use full update flag so the chest tile entity initialises.
            WorldGenLevel level = ctx().level();
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                ItemStack egg = king
                        ? new ItemStack(ModItems.THE_KING_SPAWN_EGG.get())
                        : new ItemStack(ModItems.THE_QUEEN_SPAWN_EGG.get());
                chest.setItem(13, egg);
            }
        }
    }

    private void layRoyalRect(int cx, int cy, int cz,
                              int halfX, int halfZ, BlockState slab) {
        for (int i = -halfX; i <= halfX; i++) {
            for (int k = -halfZ; k <= halfZ; k++) {
                place(cx + i, cy, cz + k, slab);
            }
        }
    }

    private void layRoyalCrossWithCorners(int cx, int cy, int cz,
                                          int halfX, int halfZ,
                                          BlockState slab, BlockState corner) {
        for (int i = -halfX; i <= halfX; i++) {
            for (int k = -halfZ; k <= halfZ; k++) {
                boolean isCorner = (i == halfX || i == -halfX) && (k == -halfZ || k == halfZ);
                place(cx + i, cy, cz + k, isCorner ? corner : slab);
            }
        }
    }

    /**
     * 33&times;33 portrait sprite data shared by the King and Queen
     * altars, copied verbatim from {@code GenericDungeon.king[]}
     * (1.7.10 line 63). The Queen variant in the legacy source uses
     * the identical array (line 64), so a single literal serves both.
     * Each {@code -1} is a row terminator; non-negative entries
     * specify a run length and toggle the palette colour.
     */
    private static final int[] ROYAL_PORTRAIT_DATA = new int[]{
            -1, -1, 24, 3, -1, 24, 5, -1, 17, 12, -1, 16, 15, -1, 15, 14, -1,
            15, 6, 3, 5, -1, 14, 6, 4, 3, -1, 14, 5, -1, 14, 5, -1, 12, 9, -1,
            11, 11, -1, 8, 17, -1, 5, 23, -1, 3, 27, -1, 2, 29, -1, 1, 31, -1,
            0, 33, -1, 13, 6, -1, 12, 9, -1, 11, 3, 1, 2, 1, 4, -1, 10, 3, 2,
            2, 3, 2, -1, 10, 2, 4, 2, 3, 2, -1, 9, 2, 5, 2, 4, 6, -1, 9, 2, 5,
            2, 6, 4, -1, 8, 2, 6, 1, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 8, 2,
            5, 2, -1, 15, 2, -1, -1, -1
    };

    // ---- Audit Part 4 — King's / Queen's Challenge Tower ---------------

    /**
     * Direct port of {@code GenericDungeon.makeEnormousCastle} (King, line
     * 191&ndash;375) and {@code makeEnormousCastleQ} (Queen, line
     * 6393&ndash;6577). The two share the same scaffolding — 28&times;28
     * base, four corner exterior spawner stacks, central Emperor Scorpion
     * column, six stacked floors with shrinking footprint
     * (26&times;10 / 26&times;10 / 24&times;9 / 24&times;9 / 22&times;8 /
     * 22&times;16), western platform-spire arm with a long descending
     * stair, and a level-6 Large Worm scatter — but with three palette
     * differences:
     * <ul>
     *   <li>King floor: {@code stone}; Queen floor: {@code obsidian}.</li>
     *   <li>King exterior corner spawners: Terrible Terror; Queen: Lurking Terror.</li>
     *   <li>King floor mob ladder: Cloud Shark / Lurking Terror / Rotator /
     *       Bee / Mantis / Mothra. Queen ladder: Rotator / Bee / Mantis /
     *       Mothra / Brutalfly / Vortex.</li>
     *   <li>King spire: {@code quartz_block} (legacy {@code field_150371_ca});
     *       Queen spire: {@link ModBlocks#BLOCK_AMETHYST}.</li>
     *   <li>King foundation skirt: {@code stone}; Queen: {@code obsidian}.</li>
     * </ul>
     *
     * <p>The legacy code rolled {@code level = 1 + nextInt(6)} with a
     * weighted reroll favouring level 4&ndash;6, then suppressed any floor
     * whose index exceeded {@code level}. We reproduce the same roll using
     * the deterministic per-piece {@link RandomSource} so the visible tower
     * shape stays seed-stable (and so the prize-floor that places the
     * Royal Guardian Sword + Prince/Princess Egg is gated on the same
     * roll the legacy expected). The final-prize chest only appears when
     * the level=6 floor's {@code addLevelDecorations(decor=1, difficulty=6)}
     * resolves {@code reward=6}, exactly as the legacy.</p>
     */
    private void generateChallengeTower(RandomSource random, boolean king) {
        int cposx = origin.getX();
        int cposy = origin.getY();
        int cposz = origin.getZ();
        int width = 28;
        int height = 16;
        int platformwidth = 11;

        // orig GD:202-205 / 6404-6407 — the tower's difficulty roll:
        // uniform 1-6, then levels 1-3 are rerolled up by 3 two thirds of
        // the time, giving P(1)=P(2)=P(3)=1/18 and P(4)=P(5)=P(6)=5/18.
        // Only a level-6 tower (~27.8%) builds all six floors and awards
        // the reward-6 Royal chests. A pre-D5 "QA fix" locked this to 6 so
        // every tower guaranteed the prize — invented behavior, removed
        // per WGEN-051 (the guaranteed-prize idea is archived as a 2.0
        // candidate, MODERNIZATION_NOTES MOD-012).
        int level = 1 + random.nextInt(6);
        if (level <= 3 && random.nextInt(3) != 1) {
            level += 3;
        }

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState ironBars = Blocks.IRON_BARS.defaultBlockState();
        BlockState netherFence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
        BlockState floorBlk = king ? Blocks.STONE.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState skirtBlk = king ? Blocks.STONE.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState extremeTorch = ModBlocks.EXTREME_TORCH.get().defaultBlockState();
        EntityType<?> baseCornerMob = king ? ModEntities.ENTITY_TERRIBLE_TERROR.get()
                : ModEntities.ENTITY_LURKING_TERROR.get();

        // Phase 1 — clear interior + west spire envelope (legacy line 206-212 / 6408-6414).
        for (int i = -20; i < width + 4; i++) {
            for (int j = 1; j < height + 10; j++) {
                for (int k = -4; k < width + 4; k++) {
                    place(cposx + i, cposy + j, cposz + k, air);
                }
            }
        }
        // Phase 2 — base floor (line 213-218 / 6415-6420). King = stone, Queen = obsidian.
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy, cposz + k, floorBlk);
            }
        }
        // Phase 3 — base ceiling at j=height (line 219-224 / 6421-6426). Bedrock cap.
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy + height, cposz + k, bedrock);
            }
        }
        // Phase 4 — N/S iron-bar walls (line 225-232 / 6427-6434).
        for (int i = 0; i < width; i++) {
            for (int j = 1; j < height; j++) {
                place(cposx + i, cposy + j, cposz, ironBars);
                place(cposx + i, cposy + j, cposz + width - 1, ironBars);
            }
        }
        // Phase 5 — E/W iron-bar walls (line 233-240 / 6435-6442).
        for (int k = 0; k < width; k++) {
            for (int j = 1; j < height; j++) {
                place(cposx, cposy + j, cposz + k, ironBars);
                place(cposx + width - 1, cposy + j, cposz + k, ironBars);
            }
        }
        // Phase 6 — 4 corner Extreme Torches (line 241-244 / 6443-6446).
        place(cposx + 1, cposy + 1, cposz + 1, extremeTorch);
        place(cposx + 1, cposy + 1, cposz + width - 2, extremeTorch);
        place(cposx + width - 2, cposy + 1, cposz + 1, extremeTorch);
        place(cposx + width - 2, cposy + 1, cposz + width - 2, extremeTorch);
        // Phase 7 — foundation skirt + outer fence trim (line 245-253 / 6447-6455).
        for (int i = -4; i < width + 4; i++) {
            for (int k = -4; k < width + 4; k++) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    place(cposx + i, cposy, cposz + k, skirtBlk);
                }
                if (i == -4 || k == -4 || i == width + 3 || k == width + 3) {
                    place(cposx + i, cposy + 1, cposz + k, netherFence);
                }
            }
        }
        // Phase 8 — 4 corner exterior spawner stacks j=0..3 (line 254-275 / 6456-6477).
        // King = Terrible Terror; Queen = Lurking Terror.
        for (int j = 0; j < 4; j++) {
            placeSpawner(cposx - 3, cposy + 1 + j, cposz - 3, baseCornerMob);
            placeSpawner(cposx - 3, cposy + 1 + j, cposz + width + 2, baseCornerMob);
            placeSpawner(cposx + width + 2, cposy + 1 + j, cposz - 3, baseCornerMob);
            placeSpawner(cposx + width + 2, cposy + 1 + j, cposz + width + 2, baseCornerMob);
        }
        // Phase 9 — central Emperor Scorpion column j=2,3,4 (line 276-290 / 6478-6492).
        for (int j = 2; j <= 4; j++) {
            placeSpawner(cposx + width / 2, cposy + j, cposz + width / 2,
                    ModEntities.ENTITY_EMPEROR_SCORPION.get());
        }

        // Phase 10 — stacked floors. The (cposx, cposz) corner shifts inward as
        // the floor footprint shrinks; widths and heights match legacy exactly.
        EntityType<?>[] cornerMobs = king ? kingFloorCornerMobs() : queenFloorCornerMobs();
        int j = height;
        // Floor 1 — width-2, h=10, pw=4, decor=1.
        buildChallengeFloor(king, cposx + 1, cposy + j, cposz + 1,
                width - 2, 10, 4, cornerMobs[0], 1, -1, 5, 1, level, random);
        j += 10;
        if (level >= 2) {
            buildChallengeFloor(king, cposx + 1, cposy + j, cposz + 1,
                    width - 2, 10, 4, cornerMobs[1], 0, 0, 4, 2, level, random);
        }
        j += 10;
        if (level >= 3) {
            buildChallengeFloor(king, cposx + 2, cposy + j, cposz + 2,
                    width - 4, 9, 4, cornerMobs[2], 1, 1, 4, 3, level, random);
        }
        j += 9;
        if (level >= 4) {
            buildChallengeFloor(king, cposx + 2, cposy + j, cposz + 2,
                    width - 4, 9, 3, cornerMobs[3], 0, 0, 4, 4, level, random);
        }
        j += 9;
        if (level >= 5) {
            buildChallengeFloor(king, cposx + 3, cposy + j, cposz + 3,
                    width - 6, 8, 3, cornerMobs[4], 1, 1, 4, 5, level, random);
        }
        j += 8;
        if (level >= 6) {
            buildChallengeFloor(king, cposx + 3, cposy + j, cposz + 3,
                    width - 6, 16, 3, cornerMobs[5], 0, 0, 3, 6, level, random);
        }

        // Phase 11 — western platform (line 314-321 / 6516-6523).
        BlockState spireBlk = king ? Blocks.QUARTZ_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_AMETHYST.get().defaultBlockState();
        for (int i = 0; i < platformwidth; i++) {
            int yj = height;
            for (int k = -(platformwidth / 2); k <= platformwidth / 2; k++) {
                place(cposx + i - 20, cposy + yj, cposz + k + width / 2, spireBlk);
                if ((i != 0 && i != platformwidth - 1 && k != -(platformwidth / 2) && k != platformwidth / 2)
                        || (i == 0 && k >= -1 && k <= 1)) continue;
                place(cposx + i - 20, cposy + yj + 1, cposz + k + width / 2, netherFence);
            }
        }
        // Phase 12 — connector arm (line 322-339 / 6524-6541).
        for (int i = -10; i <= -3; i++) {
            int yj = height;
            for (int k = -2; k < 3; k++) {
                if (i == -3 || i == -10) {
                    if (k != -2 && k != 2) {
                        place(cposx + i, cposy + yj + 1, cposz + k + width / 2, air);
                        continue;
                    }
                    place(cposx + i, cposy + yj + 1, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + i, cposy + yj + 2, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + i, cposy + yj + 3, cposz + k + width / 2, Blocks.FIRE.defaultBlockState());
                    continue;
                }
                place(cposx + i, cposy + yj, cposz + k + width / 2, spireBlk);
                if (k != -2 && k != 2) continue;
                place(cposx + i, cposy + yj + 1, cposz + k + width / 2, netherFence);
            }
        }
        // Phase 13 — descending stair (line 340-361 / 6542-6563).
        int xi = -21;
        for (int yj = height; yj >= 0; yj--) {
            for (int k = -2; k < 3; k++) {
                for (int t = 0; t < 6; t++) {
                    place(cposx + xi, cposy + yj + t + 1, cposz + k + width / 2, air);
                }
                if (yj == 0) {
                    if (k != -2 && k != 2) {
                        place(cposx + xi, cposy + yj + 1, cposz + k + width / 2, air);
                        continue;
                    }
                    place(cposx + xi, cposy + yj + 1, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + xi, cposy + yj + 2, cposz + k + width / 2, Blocks.NETHERRACK.defaultBlockState());
                    place(cposx + xi, cposy + yj + 3, cposz + k + width / 2, Blocks.FIRE.defaultBlockState());
                    continue;
                }
                place(cposx + xi, cposy + yj, cposz + k + width / 2, spireBlk);
                if (k != -2 && k != 2) continue;
                place(cposx + xi, cposy + yj + 1, cposz + k + width / 2, netherFence);
            }
            xi--;
        }
        // Phase 14 — level=6 Large Worm scatter (line 362-374 / 6564-6576).
        if (level >= 6) {
            int span = width * 3;
            for (int tries = 0; tries < 100; tries++) {
                int yj2 = -1;
                int xi2 = random.nextInt(span);
                int zk2 = random.nextInt(span);
                if (xi2 >= span / 4 && xi2 <= span * 3 / 4
                        && zk2 >= span / 4 && zk2 <= span * 3 / 4) continue;
                xi2 -= span / 2;
                zk2 -= span / 2;
                placeSpawner(cposx + xi2 + width / 2, cposy + yj2,
                        cposz + zk2 + width / 2, ModEntities.ENTITY_WORM_LARGE.get());
            }
        }
    }

    /**
     * Direct port of {@code GenericDungeon.buildLevel} (King, line 377&ndash;478)
     * / {@code buildLevelQ} (Queen, line 6579&ndash;6680). Each floor is a
     * bedrock-walled cube with a vein-block accent on its NS faces (gold
     * for King, ruby for Queen), a bedrock floor + ceiling, an outer
     * fence skirt, a diagonal stair connector to the floor above, a
     * 4-corner spawner column j=1..4 for the floor's exterior mob, and a
     * centred decoration room dispatched via
     * {@link #addChallengeFloorDecor}.
     */
    private void buildChallengeFloor(boolean king, int cposx, int cposy, int cposz,
                                     int width, int height, int pw, EntityType<?> critter,
                                     int stepside, int stepoff, int holelen, int decor, int level,
                                     RandomSource random) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState veinBlk = king ? Blocks.GOLD_BLOCK.defaultBlockState()
                : ModBlocks.BLOCK_RUBY.get().defaultBlockState();
        BlockState skirtBlk = king ? Blocks.STONE.defaultBlockState()
                : Blocks.OBSIDIAN.defaultBlockState();
        BlockState fence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();

        // Phase A — hollow the floor's footprint + 1-block fence margin (line 381-387 / 6583-6589).
        for (int i = -pw; i < width + pw; i++) {
            for (int yj = 1; yj < height; yj++) {
                for (int k = -pw; k < width + pw; k++) {
                    place(cposx + i, cposy + yj, cposz + k, air);
                }
            }
        }
        // Phase B — bedrock floor (line 388-393 / 6590-6595).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy, cposz + k, bedrock);
            }
        }
        // Phase C — bedrock ceiling at y=height (line 394-399 / 6596-6601).
        for (int i = 0; i < width; i++) {
            for (int k = 0; k < width; k++) {
                place(cposx + i, cposy + height, cposz + k, bedrock);
            }
        }
        // Phase D — N/S bedrock walls (line 400-407 / 6602-6609).
        for (int i = 0; i < width; i++) {
            for (int yj = 1; yj < height; yj++) {
                place(cposx + i, cposy + yj, cposz, bedrock);
                place(cposx + i, cposy + yj, cposz + width - 1, bedrock);
            }
        }
        // Phase E — E/W walls with vein-block accent on the corner columns
        // (line 408-419 / 6610-6621). King vein = gold; Queen vein = ruby.
        for (int k = 0; k < width; k++) {
            for (int yj = 1; yj < height; yj++) {
                BlockState blk = (k == 0 || k == width - 1) ? veinBlk : bedrock;
                place(cposx, cposy + yj, cposz + k, blk);
                place(cposx + width - 1, cposy + yj, cposz + k, blk);
            }
        }
        // Phase F — outer foundation skirt + outer fence (line 420-428 / 6622-6630).
        for (int i = -pw; i < width + pw; i++) {
            for (int k = -pw; k < width + pw; k++) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    place(cposx + i, cposy, cposz + k, skirtBlk);
                }
                if (i != -pw && k != -pw && i != width + (pw - 1) && k != width + (pw - 1)) continue;
                place(cposx + i, cposy + 1, cposz + k, fence);
            }
        }
        // Phase G — diagonal stair to the next floor (line 429-440 / 6631-6642).
        int si = -(height / 2) + width / 2;
        for (int yj = 1; yj < height; yj++) {
            int sk;
            if (stepside != 0) {
                sk = -1;
            } else {
                sk = width;
            }
            place(cposx + si, cposy + yj, cposz + sk, skirtBlk);
            si++;
        }
        // Phase H — hole through the floor for the stair to land in (line 441-454 / 6643-6656).
        if (stepoff >= 0) {
            int sk;
            if (stepside == 0) {
                sk = -1 - stepoff;
            } else {
                sk = width + stepoff;
            }
            int hi = width / 2;
            for (int l = 0; l < holelen; l++) {
                place(cposx + hi + l, cposy, cposz + sk, air);
            }
        }
        // Phase I — 4 corner spawner stacks j=1..4 with the floor's "outside" mob
        // (line 455-476 / 6657-6678). This is the wiki "Outside" gauntlet ladder.
        for (int yj = 0; yj < 4; yj++) {
            placeSpawner(cposx - (pw - 1), cposy + yj + 1, cposz - (pw - 1), critter);
            placeSpawner(cposx - (pw - 1), cposy + yj + 1, cposz + width + (pw - 2), critter);
            placeSpawner(cposx + width + (pw - 2), cposy + yj + 1, cposz - (pw - 1), critter);
            placeSpawner(cposx + width + (pw - 2), cposy + yj + 1, cposz + width + (pw - 2), critter);
        }
        // Phase J — centred decoration room (the wiki "Inside" gauntlet ladder).
        addChallengeFloorDecor(king, cposx, cposy, cposz, width, height, decor, level, random);
    }

    /**
     * Direct port of {@code GenericDungeon.addLevelDecorations} (King, line
     * 480&ndash;725) / {@code addLevelDecorationsQ} (Queen, line
     * 6682&ndash;6927). Each "decor" tier picks a different "Inside" mob
     * for the centre 1&times;1 spawner column gated by an iron-bar shaft,
     * and sets {@code reward} that drives the chest-fill table. Decor=6
     * is the Nightmare cap (4 Nightmare spawners around a central Large
     * Worm column with a dirt-filled void). The bottom floor (decor=1)
     * with difficulty=6 sets {@code reward=6}, which is what triggers the
     * Royal Guardian Sword + Prince/Princess Egg chest layout in
     * {@link #fillChallengeChests}.
     */
    private void addChallengeFloorDecor(boolean king, int cposx, int cposy, int cposz,
                                        int width, int height, int decor, int difficulty,
                                        RandomSource random) {
        int reward = 1;
        EntityType<?> critter;

        if (decor == 6) {
            // Nightmare cap (line 485-541 / 6687-6743). Same for King and Queen.
            BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
            BlockState fire = Blocks.FIRE.defaultBlockState();
            BlockState dirt = Blocks.DIRT.defaultBlockState();
            place(cposx, cposy + height, cposz, netherrack);
            place(cposx, cposy + height + 1, cposz, fire);
            place(cposx, cposy + height, cposz + width - 1, netherrack);
            place(cposx, cposy + height + 1, cposz + width - 1, fire);
            place(cposx + width - 1, cposy + height, cposz, netherrack);
            place(cposx + width - 1, cposy + height + 1, cposz, fire);
            place(cposx + width - 1, cposy + height, cposz + width - 1, netherrack);
            place(cposx + width - 1, cposy + height + 1, cposz + width - 1, fire);
            place(cposx + width / 2, cposy + height, cposz + width / 2, Blocks.AIR.defaultBlockState());
            placeSpawner(cposx + width / 2 - 1, cposy + height + 2, cposz + width / 2,
                    ModEntities.PITCH_BLACK.get());
            placeSpawner(cposx + width / 2 + 1, cposy + height + 2, cposz + width / 2,
                    ModEntities.PITCH_BLACK.get());
            placeSpawner(cposx + width / 2, cposy + height + 2, cposz + width / 2 - 1,
                    ModEntities.PITCH_BLACK.get());
            placeSpawner(cposx + width / 2, cposy + height + 2, cposz + width / 2 + 1,
                    ModEntities.PITCH_BLACK.get());
            for (int i = 1; i < width - 1; i++) {
                for (int yj = 1; yj < 5; yj++) {
                    for (int k = 1; k < width - 1; k++) {
                        place(cposx + i, cposy + yj, cposz + k, dirt);
                    }
                }
            }
            placeSpawner(cposx + width / 2, cposy + 2, cposz + width / 2,
                    ModEntities.ENTITY_WORM_LARGE.get());
            placeSpawner(cposx + width / 2, cposy + 3, cposz + width / 2,
                    ModEntities.ENTITY_WORM_LARGE.get());
            placeSpawner(cposx + width / 2, cposy + 4, cposz + width / 2,
                    ModEntities.ENTITY_WORM_LARGE.get());
            // orig GD:537-539 — the bare 1x1 air shaft through the dirt fill
            // and this floor's slab. The original places NO climbable block
            // here (or anywhere in the tower); players brought their own. A
            // pre-D5 "QA fix" added a scaffolding column — invented behavior,
            // removed per WGEN-052 (archived as MOD-012).
            for (int yj = 0; yj < 10; yj++) {
                place(cposx + 1, cposy + yj, cposz + 1, Blocks.AIR.defaultBlockState());
            }
            fillChallengeChests(king, cposx, cposy + 4, cposz, width, decor, reward, random);
            return;
        }
        // Decor 5..1 — central spawner column with iron-bar shaft + chest fill.
        // Mob ladder differs between King and Queen per legacy.
        if (king) {
            critter = pickKingDecorMob(decor, difficulty);
            reward = pickDecorReward(decor, difficulty);
        } else {
            critter = pickQueenDecorMob(decor, difficulty);
            reward = pickDecorReward(decor, difficulty);
        }
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        // Two stacked spawners j=2,3 in the centre (line 551-560 etc / 6753-6762 etc).
        placeSpawner(cposx + width / 2, cposy + 2, cposz + width / 2, critter);
        placeSpawner(cposx + width / 2, cposy + 3, cposz + width / 2, critter);
        // 4-cell bedrock shaft around the spawner column at y=1..4 (line 561-566 etc / 6763-6768 etc).
        for (int yj = 1; yj < 5; yj++) {
            place(cposx + width / 2 - 1, cposy + yj, cposz + width / 2, bedrock);
            place(cposx + width / 2 + 1, cposy + yj, cposz + width / 2, bedrock);
            place(cposx + width / 2, cposy + yj, cposz + width / 2 - 1, bedrock);
            place(cposx + width / 2, cposy + yj, cposz + width / 2 + 1, bedrock);
        }
        // Up/down ladder access cut. Exact corners alternate per decor tier
        // — match legacy parity precisely.
        //   decor=5: floor (w-2, w-2), ceiling (1, 1)   [legacy line 567-568]
        //   decor=4: floor (1, 1),     ceiling (w-2, w-2) [line 600-601]
        //   decor=3: floor (w-2, w-2), ceiling (1, 1)   [line 637-638]
        //   decor=2: floor (1, 1),     ceiling (w-2, w-2) [line 678-679]
        //   decor=1: NO floor hole; ceiling (1, 1) only [line 722]
        // The previous port collapsed decor=1 into the generic else branch,
        // which carved a bogus extra ceiling hole at (w-2, w-2) and an
        // extraneous floor hole at (1, 1) exposing the bedrock base. Match
        // the legacy exactly per-decor below.
        BlockState air = Blocks.AIR.defaultBlockState();
        int floorHoleX, floorHoleZ, ceilHoleX, ceilHoleZ;
        if (decor == 5 || decor == 3) {
            floorHoleX = width - 2; floorHoleZ = width - 2;
            ceilHoleX = 1;          ceilHoleZ = 1;
        } else if (decor == 4 || decor == 2) {
            floorHoleX = 1;         floorHoleZ = 1;
            ceilHoleX = width - 2;  ceilHoleZ = width - 2;
        } else { // decor == 1: bottom floor, no floor hole; ceiling at (1, 1)
            floorHoleX = -1; floorHoleZ = -1; // sentinel: skip floor carve
            ceilHoleX = 1;   ceilHoleZ = 1;
        }
        if (floorHoleX >= 0) {
            place(cposx + floorHoleX, cposy, cposz + floorHoleZ, air);
        }
        place(cposx + ceilHoleX, cposy + height, cposz + ceilHoleZ, air);
        // NOTE: the legacy places NO ladders or climbable blocks anywhere in
        // the tower (verified: zero ladder references in GD:191-786 /
        // 6393-6987) — the 1x1 bedrock holes are the only route and players
        // bring their own blocks. A pre-D5 "QA fix" added scaffolding
        // columns under every ceiling hole — invented behavior, removed per
        // WGEN-052 (archived as MOD-012).

        // Decor 1 also lays 4 RTP teleport blocks at the central spawner base
        // (line 718-721 / 6920-6923) so the player can warp out after looting.
        if (decor == 1) {
            BlockState rtp = ModBlocks.BLOCK_TELEPORT.get().defaultBlockState();
            place(cposx + width / 2 - 1, cposy + 1, cposz + width / 2 - 1, rtp);
            place(cposx + width / 2 + 1, cposy + 1, cposz + width / 2 + 1, rtp);
            place(cposx + width / 2 + 1, cposy + 1, cposz + width / 2 - 1, rtp);
            place(cposx + width / 2 - 1, cposy + 1, cposz + width / 2 + 1, rtp);
        }
        fillChallengeChests(king, cposx, cposy, cposz, width, decor, reward, random);
    }

    /**
     * King's "Inside" mob ladder. Direct port of {@code addLevelDecorations}
     * decor 1..5 critter switches (legacy lines 484-700). The wiki's
     * "Worm &rarr; T-Rex &rarr; Basilisk &rarr; Hercules Beetle &rarr;
     * Jumpy Bug &rarr; Hammerhead &rarr; Emperor Scorpion" combined ladder
     * collapses across all six floors; the per-(decor, difficulty) lookup
     * here reproduces the exact source mapping.
     */
    private static EntityType<?> pickKingDecorMob(int decor, int difficulty) {
        // Layout matches the legacy if/else cascade exactly.
        switch (decor) {
            case 1:
                if (difficulty >= 6) return ModEntities.HAMMERHEAD.get();
                if (difficulty == 5) return ModEntities.ENTITY_TROOPER_BUG.get(); // "Jumpy Bug" = TrooperBug (orig OreSpawnMain.java:3943), WGEN-054
                if (difficulty == 4) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 3) return ModEntities.BASILISK.get();
                if (difficulty == 2) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 2:
                if (difficulty >= 6) return ModEntities.ENTITY_TROOPER_BUG.get(); // "Jumpy Bug" = TrooperBug (orig OreSpawnMain.java:3943), WGEN-054
                if (difficulty == 5) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 4) return ModEntities.BASILISK.get();
                if (difficulty == 3) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 3:
                if (difficulty >= 6) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 5) return ModEntities.BASILISK.get();
                if (difficulty == 4) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 4:
                if (difficulty >= 6) return ModEntities.BASILISK.get();
                if (difficulty == 5) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
            case 5:
            default:
                if (difficulty >= 6) return ModEntities.TREX.get();
                return ModEntities.ALOSAURUS.get();
        }
    }

    /**
     * Queen's "Inside" mob ladder. Direct port of
     * {@code addLevelDecorationsQ} decor 1..5 critter switches (legacy
     * lines 6686-6902). Replaces the King's Alosaurus/T.Rex bottom with
     * T.Rex/Nastysaurus and tops out at CaterKiller for decor=1
     * difficulty=6 (line 6900-6902).
     */
    private static EntityType<?> pickQueenDecorMob(int decor, int difficulty) {
        switch (decor) {
            case 1:
                if (difficulty >= 6) return ModEntities.ENTITY_CATER_KILLER.get();
                if (difficulty == 5) return ModEntities.ENTITY_TROOPER_BUG.get(); // "Jumpy Bug" = TrooperBug (orig OreSpawnMain.java:3943), WGEN-054
                if (difficulty == 4) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 3) return ModEntities.BASILISK.get();
                if (difficulty == 2) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 2:
                if (difficulty >= 6) return ModEntities.ENTITY_TROOPER_BUG.get(); // "Jumpy Bug" = TrooperBug (orig OreSpawnMain.java:3943), WGEN-054
                if (difficulty == 5) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 4) return ModEntities.BASILISK.get();
                if (difficulty == 3) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 3:
                if (difficulty >= 6) return ModEntities.ENTITY_HERCULES_BEETLE.get();
                if (difficulty == 5) return ModEntities.BASILISK.get();
                if (difficulty == 4) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 4:
                if (difficulty >= 6) return ModEntities.BASILISK.get();
                if (difficulty == 5) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
            case 5:
            default:
                if (difficulty >= 6) return ModEntities.NASTYSAURUS.get();
                return ModEntities.TREX.get();
        }
    }

    /**
     * Direct port of the legacy decor-vs-difficulty reward table. Each
     * decor tier shifts the difficulty&rarr;reward mapping by one so a
     * level-6 player ends with reward=6 only on decor=1 (the bottom
     * floor), matching the legacy cascade exactly. Decor=6 sets
     * reward=1 unconditionally (the Nightmare cap drops level-1 loot).
     */
    private static int pickDecorReward(int decor, int difficulty) {
        switch (decor) {
            case 1: return difficulty;          // 1..6 -> 1..6 (line 701 / 6903)
            case 2: return Math.max(1, difficulty - 1); // 2..6 -> 1..5
            case 3: return Math.max(1, difficulty - 2); // 3..6 -> 1..4
            case 4: return Math.max(1, difficulty - 3); // 4..6 -> 1..3
            case 5: return Math.max(1, difficulty - 4); // 5..6 -> 1..2
            default: return 1;
        }
    }

    /**
     * Direct port of {@code GenericDungeon.fill_chests} (King, GD:727-785) /
     * {@code fill_chestsQ} (Queen, GD:6929-6987). Four chests at the cardinal
     * mid-edges of the floor, each stamped with the original's facing
     * metadata (GD:744/754/765/776 — meta 5/4/3/2, every chest faces the
     * room centre; restored per WGEN-056). When {@code reward == 6} — only
     * the bottom floor of a level-6 tower — they hold the fixed Royal prize
     * layout in the exact slots the legacy used; the prize eggs are the
     * FUNCTIONAL Prince/Princess spawn eggs, matching the original
     * {@code ThePrinceEgg}/{@code ThePrincessEgg} {@code ItemSpawnEgg}s
     * (OSM:5616/5630), not the invented trophy items (ITEM-066). Otherwise
     * each chest binds the level-N loot table transcribing the original
     * {@code levelNContentsList} (GD:57-61) with the original
     * {@code 5 + nextInt(7)} stack rolls of {@code func_76293_a} (GD:750) —
     * faithful lists restored per WGEN-053. The Queen variant's decor 2-6
     * floors called the King's {@code fill_chests} with the same lists
     * (GD:6742/6771/6804/6841/6882); sharing one table set per reward tier
     * reproduces that call graph's outcome exactly.
     */
    private void fillChallengeChests(boolean king, int cposx, int cposy, int cposz,
                                     int width, int decor, int reward, RandomSource random) {
        if (reward == 6) {
            // West chest (GD:743-752 / 6945-6954, faces east): Prince/Princess egg, slot 1.
            placeChest(cposx + 1, cposy + 1, cposz + width / 2, Direction.EAST, (chest, rng) ->
                    chest.setItem(1, new ItemStack(king
                            ? ModItems.THE_PRINCE_SPAWN_EGG.get()
                            : ModItems.THE_PRINCESS_SPAWN_EGG.get())), random);
            // East chest (GD:753-763 / 6955-6965, faces west): helmet slot 1 + chestplate slot 2.
            placeChest(cposx + width - 2, cposy + 1, cposz + width / 2, Direction.WEST, (chest, rng) -> {
                chest.setItem(1, new ItemStack(king
                        ? ModItems.ROYAL_HELMET.get() : ModItems.QUEEN_HELMET.get()));
                chest.setItem(2, new ItemStack(king
                        ? ModItems.ROYAL_CHESTPLATE.get() : ModItems.QUEEN_CHESTPLATE.get()));
            }, random);
            // North chest (GD:764-774 / 6966-6976, faces south): leggings slot 1 + boots slot 2.
            placeChest(cposx + width / 2, cposy + 1, cposz + 1, Direction.SOUTH, (chest, rng) -> {
                chest.setItem(1, new ItemStack(king
                        ? ModItems.ROYAL_LEGGINGS.get() : ModItems.QUEEN_LEGGINGS.get()));
                chest.setItem(2, new ItemStack(king
                        ? ModItems.ROYAL_BOOTS.get() : ModItems.QUEEN_BOOTS.get()));
            }, random);
            // South chest (GD:775-784 / 6977-6986, faces north): Royal Guardian Sword, slot 1.
            placeChest(cposx + width / 2, cposy + 1, cposz + width - 2, Direction.NORTH, (chest, rng) ->
                    chest.setItem(1, new ItemStack(ModItems.ROYAL_GUARDIAN_SWORD.get())), random);
            return;
        }
        // Rewards 1-5: the original weighted lists as data (GD:729-742 list select).
        ResourceKey<LootTable> loot = challengeLootTable(reward);
        placeLootChest(cposx + 1, cposy + 1, cposz + width / 2, Direction.EAST, loot);
        placeLootChest(cposx + width - 2, cposy + 1, cposz + width / 2, Direction.WEST, loot);
        placeLootChest(cposx + width / 2, cposy + 1, cposz + 1, Direction.SOUTH, loot);
        placeLootChest(cposx + width / 2, cposy + 1, cposz + width - 2, Direction.NORTH, loot);
    }

    /**
     * The {@code chestContents} list select of {@code fill_chests}
     * (GD:729-742): reward 1-5 → {@code chests/challenge_tower_level<N>},
     * each a full transcription of {@code levelNContentsList} (GD:57-61).
     */
    private static ResourceKey<LootTable> challengeLootTable(int reward) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(
                "orespawn", "chests/challenge_tower_level" + reward));
    }

    /**
     * King's per-floor "Outside" corner-spawner ladder. Direct port of
     * the {@code makeEnormousCastle.buildLevel(critter=...)} string
     * arguments at legacy lines 292 / 295 / 299 / 303 / 307 / 311.
     *
     * <pre>
     *   Floor 1 (decor=1): "Cloud Shark"     -&gt; CLOUD_SHARK
     *   Floor 2 (decor=2): "Lurking Terror"  -&gt; ENTITY_LURKING_TERROR
     *   Floor 3 (decor=3): "Rotator"         -&gt; ENTITY_ROTATOR
     *   Floor 4 (decor=4): "Bee"             -&gt; ENTITY_BEE
     *   Floor 5 (decor=5): "Mantis"          -&gt; ENTITY_MANTIS
     *   Floor 6 (decor=6): "Mothra"          -&gt; MOTHRA
     * </pre>
     *
     * <p>Resolved at call-time (i.e. inside {@code postProcess}) so the
     * NeoForge {@code DeferredHolder} registry-attach has guaranteed to
     * have run; lazy-resolving statically would NPE if this class loaded
     * before {@code FMLClientSetupEvent} fired.</p>
     */
    private static EntityType<?>[] kingFloorCornerMobs() {
        return new EntityType<?>[]{
                ModEntities.CLOUD_SHARK.get(),
                ModEntities.ENTITY_LURKING_TERROR.get(),
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_BEE.get(),
                ModEntities.ENTITY_MANTIS.get(),
                ModEntities.MOTHRA.get()
        };
    }

    /**
     * Queen's per-floor "Outside" corner-spawner ladder. Direct port of
     * the {@code makeEnormousCastleQ.buildLevelQ(critter=...)} string
     * arguments at legacy lines 6494 / 6497 / 6501 / 6505 / 6509 / 6513.
     *
     * <pre>
     *   Floor 1: "Rotator"    -&gt; ENTITY_ROTATOR
     *   Floor 2: "Bee"        -&gt; ENTITY_BEE
     *   Floor 3: "Mantis"     -&gt; ENTITY_MANTIS
     *   Floor 4: "Mothra"     -&gt; MOTHRA
     *   Floor 5: "Brutalfly"  -&gt; ENTITY_BRUTALFLY
     *   Floor 6: "Vortex"     -&gt; ENTITY_VORTEX
     * </pre>
     */
    private static EntityType<?>[] queenFloorCornerMobs() {
        return new EntityType<?>[]{
                ModEntities.ENTITY_ROTATOR.get(),
                ModEntities.ENTITY_BEE.get(),
                ModEntities.ENTITY_MANTIS.get(),
                ModEntities.MOTHRA.get(),
                ModEntities.ENTITY_BRUTALFLY.get(),
                ModEntities.ENTITY_VORTEX.get()
        };
    }
}
