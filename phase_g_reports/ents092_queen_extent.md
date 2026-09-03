# ENT-S-092 — The Queen: world extent of the port rig at 1.0 versus the 1.7.10 model at 2.0 (2026-09-03)

Owner's instruction: "compare the world extent of the current rig at 1.0 against the 1.7.10 model at 2.0.
If they match, record a MOD decision that the re-authored rig preserves her size and close the item. If
not, restore 2.0 and re-tune the profile part sizes under the main-size law. Report before either."

## Result: they do not match. The port rig is the 1.7.10 model at 1:1 pixel scale, drawn at half size.

Rest pose, blocks (1.7.10: ModelTheQueen constructor pose, RendererLivingEntity pipeline, render scale
2.0 from `new RenderTheQueen(new ModelTheQueen(0.65f), 1.9f, 2.0f)`, ClientProxyOreSpawn.java:493;
port: `the_queen.geo.json` bind pose through the GeckoLib 4.8.4 bake and render, proven from bytecode,
pose-stack scale 1.0):

| axis | 1.7.10 at 2.0 | port at 1.0 | ratio | port if drawn at 2.0 | ratio |
|---|---|---|---|---|---|
| width | 99.00 | 49.94 | 0.50 | 99.9 | 1.009 |
| depth | 91.02 | 45.35 | 0.50 | 90.7 | 0.997 |
| height | 24.96 | 12.20 | 0.49 | 24.4 | 0.977 |
| wingspan | 99.00 | 49.94 | 0.50 | 99.9 | 1.009 |

PlayNicely (1.7.10 scale/4 = 0.5; port 0.25): 24.75 x 22.76 x 6.24 versus 12.49 x 11.34 x 3.05, the same
one-half ratio.

Why the ratio is exactly one half: the geo has 130 cubes with the identical size multiset as the 130
1.7.10 boxes (one typo apart: RREye [2,5,11] in the original, [2,6,11] in the geo); 35 of the 56
shared-name bones keep the exact constructor pivot and 36 of 50 single-cube bones the exact cube
placement, and the remaining differences are re-rig moves of the leg, claw and neck chains, never a
rescale. Cross-validation: 34 bones present in both files with matching pivot, rotation and unrotated
ancestors give the same world box in both pipelines with zero residual, so the translation, the 1/16
scaling, the Z-Y-X order and the sign conventions agree. The residuals at equal scale (+0.9% width,
-0.4% depth, -2.3% height) come from the re-authored rest pose, not from size.

The 1.7.10 constructor pose is never drawn as-is (the render method rewrites wing, claw, leg, tail and
neck rotations every frame): idle envelope at 2.0 is wingspan 71 to 99 blocks and height 21 to 50; the
comparison is rest rig against rest rig, as instructed.

## Hitbox and profile, side by side

| | 1.7.10 | port |
|---|---|---|
| normal hitbox | 22 x 24 (TheQueen.java:78-79) | 22 x 24 (ModEntities .sized; MHLib main size [22, 24]) |
| PlayNicely hitbox | 5.5 x 6 (TheQueen.java:80-82) | 22 x 24 effective: `getDefaultDimensions` returns 5.5 x 6 but MHLib's EntityEvent.Size hook replaces it with the profile main size in both modes (ENT-S-095 batch 3) |
| MHLib main size | n/a | [22, 24], equal to `.sized` and to 1.7.10, as the main-size law requires |

## What "restore 2.0 and re-tune under the main-size law" entails (not done; awaiting the owner)

1. `QueenRenderer`: apply the 1.7.10 factor AFTER `super.preRender` (GeckoLib captures
   `entityRenderTranslations` before `scaleModelForRender`; today's pre-capture scale is cancelled out of
   every bone's world matrix, which is why the MHLib parts stay at the 1.0 positions). SCALE 2.0 and
   SCALE / 4 while PlayNicely; shadow 3.0 -> 3.8 (1.9 x 2.0). Drawn size after: 99.9 x 90.7 x 24.4.
2. Main hitbox: NO change. The law fixes the profile main size to the EntityType dims, 22 x 24, and it
   has no render-scale input.
3. Part sizes (`the_queen.json` parts[].box.size): factor 2.0 on both axes if today's coverage of the
   drawn cubes is to be kept: Body1 [8,8] -> [16,16]; LHead/LHead4/LHead12 [4,3] -> [8,6]; Lwing1 [16,3]
   -> [32,6]; Tail1 [4,4] -> [8,8]; Tail4 [3,3] -> [6,6]; Tail7 [2,2] -> [4,4]; leftLeg/rightLeg [3,8] ->
   [6,16]. At 1.0 today the body, head, leg and tail parts are 1.6 to 2.1 times larger than their cubes;
   at 2.0 they are 0.8 to 1.0 times, so the sizes read as authored to 2.0 proportions.
4. Part pivots are all [0,0,0] (boxes extend up from the bone); fallback offsets match neither scale
   today and can be re-derived from the 2.0 rest pivots (values in the comparison output).
5. PlayNicely parts: a 0.5 pose scale pulls positions in but not sizes; the Q9 ruling (no authoritative
   hitboxes from client pose scale) points at the server-side part scaling hook, a separate decision.
6. Tooling that moves with the edit: the renderer pin manifest entry (expected 3.8 / 2.0, pending), a
   pin-leg exception for a post-preRender scale site, the phase_g_inventory marker, and stale comments
   claiming the /4 scale shrinks the MHLib parts.

Only the right wing root is unsynced (pre-existing: Lwing1 is the only wing part). Verification of the
part placement is in-game (F3+B) because the parts are client-fed.

Method and citations: scratch `queen_extent/` (parse_ref.py, extents_core.py, run_all.py, results.json,
fit_table.json, javap dumps of RenderUtil, BakedModelFactory$Builtin, GeoEntityRenderer, GeoRenderer,
GeoBone). The 1.7.10 pipeline constants (the (-1,-1,1) flip, the 24/16 lift, the Z-Y-X order) were proven
from the Mojang 1.7.10 client jar earlier in this audit (BUG-041 law-11 record); the extents are
differences and are lift-independent.
