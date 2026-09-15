# Phase G1 proof — compiled LayerDefinition to GeckoLib geo

Status: **PASS**

Ground truth is each executed, compiled `createBodyLayer()` and its baked
`ModelPart` tree. The generated side is parsed and baked by pinned GeckoLib
4.8.4, then captured through `GeoRenderer`.

The independent gates are:

- geometry: baked ModelPart world-space cube corners versus GeckoLib-rendered geo corners;
- surface mapping: position/normal/UV tuple parity through the two pinned renderer paths;
- animation: independently executed compiled `setupAnim` versus the actual fresh-baked candidate path;
  Beaver uses the approved exact `Mth.cos` custom-hook legacy-parity exception;
  its emitted clip is reference-only, not runtime acceptance, and editable keyframes remain G3 work;
- visual: independent software rasterization of concrete `EntityModel.renderToBuffer` and `GeoRenderer` streams using the shipped texture;
  every pixel is compared (G2 root-order contract, 2026-09-06) and the z-fight contested fraction is reported as a diagnostic only;
  a changed pixel whose two front fragments on both sides are the same pair of faces within 1e-5 blocks, the shown
  fragments differing inside the pair, is pair-contested (the tightening adopted):
  reported per sample, never a mismatch, capped at 1 percent of the image;
- draw order: per full capture, the sequence of parts the classic `renderToBuffer` drew equals the sequence of bones `GeoRenderer` emitted,
  and the order shipped in each geo (`orespawn:bone_draw_order`) equals the converter's, the probe's and the fresh bake's traversal;
  a hierarchy entry (the FK slice) draws parent-first, its key the tree's pre-order: GeoRenderer's
  sequence equals that pre-order, the classic order's deviation is recorded and the visual leg judges it; its surface leg's normal
  epsilon is the named HIERARCHY_NORMAL_EPSILON 1e-5 with the chain's accumulation recorded, and its chain-link leg compares
  every link's world transform against the classic within the geometry epsilon.

## model_tshirt (Tier 2)

- Exact bones: 2; cubes: 2.
- Geometry maximum corner delta: 0 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (12 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.TshirtGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 48 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Tshirt reference leg 19 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.TshirtGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/tshirt.animation.json` (sha256 28dec493cc7f), spline repair ON; sample-grid max delta 0 radians over 14 layer-bone samples (turn 0); 1 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): turn fewest 19 keys/bone at 0.00221294 over 524584 comparisons, one fewer 0.00263029; dense schedule of the shipped-candidate clip: turn 0.00221294; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: turn declared 20 ticks, 0.0350141 clip ticks per age tick, LUT index 15875, clip tick 4.84467 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_mosquito (Tier 2)

- Exact bones: 5; cubes: 5.
- Geometry maximum corner delta: 7.28010989079e-08 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (30 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.MosquitoGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 360 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Mosquito reference leg 13 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.MosquitoGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/mosquito.animation.json` (sha256 5731ef23d0d7), spline repair ON; sample-grid max delta 0.001755335 radians over 92 layer-bone samples (wings 0.00175533); 9 wrap pairs, |v(T-eps) - v(0+eps)| max 3e-08; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189218 over 1052288 comparisons, one fewer 0.0025443; dense schedule of the shipped-candidate clip: wings 0.00189218; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 9.5493 clip ticks per age tick, LUT index 4409, clip tick 1.34552 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_cliffracer (Tier 2)

- Exact bones: 8; cubes: 8.
- Geometry maximum corner delta: 7.00000000187e-08 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (48 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CliffRacerGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 336 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Cliff Racer reference leg 13 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.CliffRacerGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/cliffracer.animation.json` (sha256 0fc3ebdd3b0a), spline repair ON; sample-grid max delta 0.0017441 radians over 26 layer-bone samples (wings 0.0017441); 4 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189218 over 525112 comparisons, one fewer 0.00254427; dense schedule of the shipped-candidate clip: wings 0.00189218; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 4.13803 clip ticks per age tick, LUT index 41232, clip tick 12.583 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_brutalfly (Tier 2)

- Exact bones: 14; cubes: 14.
- Geometry maximum corner delta: 1.00000000058e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (84 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.BrutalflyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 420 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Brutalfly reference leg 13 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.BrutalflyGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/brutalfly.animation.json` (sha256 6cbe553a8e24), spline repair ON; sample-grid max delta 0.00148505 radians over 108 layer-bone samples (wings 0.00148505); 2 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189218 over 3146784 comparisons, one fewer 0.0025443; dense schedule of the shipped-candidate clip: wings 0.00189218; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 0.827606 clip ticks per age tick, LUT index 47568, clip tick 14.5166 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_dragonfly (Tier 2)

- Exact bones: 26; cubes: 26.
- Geometry maximum corner delta: 1.91049731764e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.40000000037e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (156 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.DragonflyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 3432 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Dragonfly reference leg 13 / 10 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.DragonflyGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/dragonfly.animation.json` (sha256 9e6a0c411be6), spline repair ON; sample-grid max delta 0.00177852 radians over 258 layer-bone samples (wings 0.0017444, jaws 0.00177852); 19 wrap pairs, |v(T-eps) - v(0+eps)| max 3e-07; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189233 over 1052528 comparisons, one fewer 0.0025444; jaws fewest 10 keys/bone at 0.00194411 over 526264 comparisons, one fewer 0.00280526; dense schedule of the shipped-candidate clip: jaws 0.00194411, wings 0.00189233; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 8.27606 clip ticks per age tick, LUT index 16928, clip tick 5.16602; jaws declared 20 ticks, 1.90986 clip ticks per age tick, LUT index 13989, clip tick 4.2691 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_cockateil (Tier 2)

- Exact bones: 16; cubes: 16.
- Geometry maximum corner delta: 1.20415945835e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.16619037864e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (96 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CockateilGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 4992 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Cockateil reference leg 14 / 10 / 9 / 9 / 9 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.CockateilGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/cockateil.animation.json` (sha256 b6315568c9f6), spline repair ON; sample-grid max delta 0.0022395 radians over 1030 layer-bone samples (wings 0.0019695, tail 0.00194092, feather1 0.00223942, feather2 0.00222948, feather3 0.0022395); 49 wrap pairs, |v(T-eps) - v(0+eps)| max 1.2e-07; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 14 keys/bone at 0.00210881 over 1054880 comparisons, one fewer 0.00264919; tail fewest 10 keys/bone at 0.00194409 over 791160 comparisons, one fewer 0.00280523; feather1 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather2 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather3 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367239; dense schedule of the shipped-candidate clip: feather1 0.00224417, feather2 0.00224417, feather3 0.00224417, tail 0.00194409, wings 0.00210881; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 4.77465 clip ticks per age tick, LUT index 2204, clip tick 0.672607; tail declared 20 ticks, 0.95493 clip ticks per age tick, LUT index 39762, clip tick 12.1344; feather1 declared 20 ticks, 3.50141 clip ticks per age tick, LUT index 14724, clip tick 4.49341; feather2 declared 20 ticks, 3.81972 clip ticks per age tick, LUT index 27978, clip tick 8.53821; feather3 declared 20 ticks, 4.13803 clip ticks per age tick, LUT index 41232, clip tick 12.583 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_ruby_bird (Tier 2)

- Exact bones: 16; cubes: 16.
- Geometry maximum corner delta: 1.20415945835e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.16619037864e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (96 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.RubyBirdGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 4992 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Ruby Bird reference leg 14 / 10 / 9 / 9 / 9 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.RubyBirdGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/cockateil.animation.json` (sha256 b6315568c9f6), spline repair ON; sample-grid max delta 0.0022395 radians over 1030 layer-bone samples (wings 0.0019695, tail 0.00194092, feather1 0.00223942, feather2 0.00222948, feather3 0.0022395); 49 wrap pairs, |v(T-eps) - v(0+eps)| max 1.2e-07; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 14 keys/bone at 0.00210881 over 1054880 comparisons, one fewer 0.00264919; tail fewest 10 keys/bone at 0.00194409 over 791160 comparisons, one fewer 0.00280523; feather1 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather2 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather3 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367239; dense schedule of the shipped-candidate clip: feather1 0.00224417, feather2 0.00224417, feather3 0.00224417, tail 0.00194409, wings 0.00210881; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 4.77465 clip ticks per age tick, LUT index 2204, clip tick 0.672607; tail declared 20 ticks, 0.95493 clip ticks per age tick, LUT index 39762, clip tick 12.1344; feather1 declared 20 ticks, 3.50141 clip ticks per age tick, LUT index 14724, clip tick 4.49341; feather2 declared 20 ticks, 3.81972 clip ticks per age tick, LUT index 27978, clip tick 8.53821; feather3 declared 20 ticks, 4.13803 clip ticks per age tick, LUT index 41232, clip tick 12.583 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_firefly (Tier 2)

- Exact bones: 12; cubes: 12.
- Geometry maximum corner delta: 2.03960780436e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.07721864019e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (72 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.FireflyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 720 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Firefly reference leg 14 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.FireflyGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/firefly.animation.json` (sha256 8dd40950d882), spline repair ON; sample-grid max delta 0.0014405 radians over 38 layer-bone samples (wings 0.0014405); 7 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 14 keys/bone at 0.00210881 over 525856 comparisons, one fewer 0.00264907; dense schedule of the shipped-candidate clip: wings 0.00210881; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 7.95775 clip ticks per age tick, LUT index 3674, clip tick 1.12122 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_goldfish (Tier 2)

- Exact bones: 16; cubes: 16.
- Geometry maximum corner delta: 1.64012194626e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.59999999916e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (96 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.GoldFishGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 4608 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0102233886719, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Gold Fish reference leg 11 / 11 / 11 / 11 / 13 / 10 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.GoldFishGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/goldfish.animation.json` (sha256 2556436c0a06), spline repair ON; sample-grid max delta 0.00208834 radians over 665 layer-bone samples (pectoral1 0.00208834, pectoral2 0.00207969, pectoral3 0.00207986, pectoral4 0.00207986, bottomfins 0.00189148, jaw 0.001941); 45 wrap pairs, |v(T-eps) - v(0+eps)| max 3e-08; non-layer bones moved 0.
- Keyframe density search (an output): pectoral1 fewest 11 keys/bone at 0.0020889 over 263672 comparisons, one fewer 0.00291613; pectoral2 fewest 11 keys/bone at 0.0020889 over 263672 comparisons, one fewer 0.00291622; pectoral3 fewest 11 keys/bone at 0.0020889 over 263672 comparisons, one fewer 0.00291613; pectoral4 fewest 11 keys/bone at 0.0020889 over 263672 comparisons, one fewer 0.00291622; bottomfins fewest 13 keys/bone at 0.00189218 over 527344 comparisons, one fewer 0.0025443; jaw fewest 10 keys/bone at 0.00194412 over 263672 comparisons, one fewer 0.00280523; dense schedule of the shipped-candidate clip: bottomfins 0.00189218, jaw 0.00194412, pectoral1 0.0020889, pectoral2 0.0020889, pectoral3 0.0020889, pectoral4 0.0020889; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: pectoral1 declared 20 ticks, 2.89662 clip ticks per age tick, LUT index 2648, clip tick 0.808105; pectoral2 declared 20 ticks, 2.6738 clip ticks per age tick, LUT index 32692, clip tick 9.97681; pectoral3 declared 20 ticks, 2.45099 clip ticks per age tick, LUT index 62735, clip tick 19.1452; pectoral4 declared 20 ticks, 2.22817 clip ticks per age tick, LUT index 27243, clip tick 8.3139; bottomfins declared 20 ticks, 3.78789 clip ticks per age tick, LUT index 13545, clip tick 4.13361; jaw declared 20 ticks, 1.55972 clip ticks per age tick, LUT index 51838, clip tick 15.8197 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_ant (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 1.49999999999e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.44948974303e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (420 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.AntGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 15180 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Ant reference leg 15 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.AntGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/ant.animation.json` (sha256 c0651139648c), spline repair ON; sample-grid max delta 0.00216644 radians over 3528 layer-bone samples (gait 6.13921e-05, jaws 0.00216644); 116 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): gait fewest 15 keys/bone at 0.00216275 over 3157296 comparisons, one fewer 0.00271118; jaws fewest 8 keys/bone at 0.00229527 over 526216 comparisons, one fewer 0.00398448; dense schedule of the shipped-candidate clip: gait 0.00216275, jaws 0.00229527; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: gait declared 20 ticks, 8.59437 clip ticks per age tick, LUT index 30183, clip tick 9.21112; jaws declared 20 ticks, 1.27324 clip ticks per age tick, LUT index 53016, clip tick 16.1792 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_rainbow_ant (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 1.49999999999e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.44948974303e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (420 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.RainbowAntGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 15180 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Rainbow Ant reference leg 15 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.RainbowAntGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/ant.animation.json` (sha256 c0651139648c), spline repair ON; sample-grid max delta 0.00216644 radians over 3528 layer-bone samples (gait 6.13921e-05, jaws 0.00216644); 116 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): gait fewest 15 keys/bone at 0.00216275 over 3157296 comparisons, one fewer 0.00271118; jaws fewest 8 keys/bone at 0.00229527 over 526216 comparisons, one fewer 0.00398448; dense schedule of the shipped-candidate clip: gait 0.00216275, jaws 0.00229527; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: gait declared 20 ticks, 8.59437 clip ticks per age tick, LUT index 30183, clip tick 9.21112; jaws declared 20 ticks, 1.27324 clip ticks per age tick, LUT index 53016, clip tick 16.1792 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_red_ant (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 1.49999999999e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.44948974303e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (420 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.RedAntGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 15180 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Red Ant reference leg 15 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.RedAntGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/ant.animation.json` (sha256 c0651139648c), spline repair ON; sample-grid max delta 0.00216644 radians over 3528 layer-bone samples (gait 6.13921e-05, jaws 0.00216644); 116 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): gait fewest 15 keys/bone at 0.00216275 over 3157296 comparisons, one fewer 0.00271118; jaws fewest 8 keys/bone at 0.00229527 over 526216 comparisons, one fewer 0.00398448; dense schedule of the shipped-candidate clip: gait 0.00216275, jaws 0.00229527; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: gait declared 20 ticks, 8.59437 clip ticks per age tick, LUT index 30183, clip tick 9.21112; jaws declared 20 ticks, 1.27324 clip ticks per age tick, LUT index 53016, clip tick 16.1792 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_termite (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 1.49999999999e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.44948974303e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (420 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.TermiteGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 15180 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Termite reference leg 15 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.TermiteGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/ant.animation.json` (sha256 c0651139648c), spline repair ON; sample-grid max delta 0.00216644 radians over 3528 layer-bone samples (gait 6.13921e-05, jaws 0.00216644); 116 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): gait fewest 15 keys/bone at 0.00216275 over 3157296 comparisons, one fewer 0.00271118; jaws fewest 8 keys/bone at 0.00229527 over 526216 comparisons, one fewer 0.00398448; dense schedule of the shipped-candidate clip: gait 0.00216275, jaws 0.00229527; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: gait declared 20 ticks, 8.59437 clip ticks per age tick, LUT index 30183, clip tick 9.21112; jaws declared 20 ticks, 1.27324 clip ticks per age tick, LUT index 53016, clip tick 16.1792 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_unstable_ant (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 1.49999999999e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.44948974303e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (420 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.UnstableAntGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 15180 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00430297851562, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Keyframe reference leg: 2.5e-3 rad; Unstable Ant reference leg 15 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.UnstableAntGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/ant.animation.json` (sha256 c0651139648c), spline repair ON; sample-grid max delta 0.00216644 radians over 3528 layer-bone samples (gait 6.13921e-05, jaws 0.00216644); 116 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): gait fewest 15 keys/bone at 0.00216275 over 3157296 comparisons, one fewer 0.00271118; jaws fewest 8 keys/bone at 0.00229527 over 526216 comparisons, one fewer 0.00398448; dense schedule of the shipped-candidate clip: gait 0.00216275, jaws 0.00229527; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: gait declared 20 ticks, 8.59437 clip ticks per age tick, LUT index 30183, clip tick 9.21112; jaws declared 20 ticks, 1.27324 clip ticks per age tick, LUT index 53016, clip tick 16.1792 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_cloudshark (Tier 2)

- Exact bones: 8; cubes: 8.
- Geometry maximum corner delta: 2.11896201121e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.16619037897e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (48 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CloudSharkGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 144 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0490264892578, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_bee (Tier 2)

- Exact bones: 23; cubes: 23.
- Geometry maximum corner delta: 5.95315042679e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.51865043267e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 1.01816922991e-05; maximum pair-contested fraction: 0.000900268554688 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 11 captures (253 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.BeeGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 2.00000000117e-07 model units; hidden-bone checks 10.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0132598876953, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000900268554688 under the cap 0.01.

## model_fairy (Tier 2)

- Exact bones: 15; cubes: 15.
- Geometry maximum corner delta: 3.00000000027e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.20016665446e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (90 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.FairyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 270 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 75.0, 'head_pitch_degrees': 12.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00724792480469, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Render mode: entity_translucent (vertex colour (255, 255, 255, 255), light world): blend SRC_ALPHA / ONE_MINUS_SRC_ALPHA / ONE / ONE_MINUS_SRC_ALPHA over the background in emission order, LEQUAL depth test with the depth written (fragments within 1e-05 blocks are one plane and all pass), texel alpha < 0.1 discarded; the same emulation on both sides.
- Render state observed: both sides request entity_translucent (RenderType.entityTranslucent, the classic model's own render-type function - the same object on the candidate), vertex colour (255, 255, 255, 255) and packed light 0 at every captured vertex (2160 classic + 2160 candidate).

## model_gammametroid (Tier 2)

- Exact bones: 21; cubes: 21.
- Geometry maximum corner delta: 3.16227765991e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.0037215376e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (126 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.GammaMetroidGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 378 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000732421875, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_irukandji (Tier 2)

- Exact bones: 9; cubes: 9.
- Geometry maximum corner delta: 1.40000000037e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.51327459483e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (54 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.IrukandjiGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 162 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_skate (Tier 2)

- Exact bones: 3; cubes: 3.
- Geometry maximum corner delta: 1.04403065148e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (63 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.SkateGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 189 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00450134277344, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_rubberducky (Tier 2)

- Exact bones: 8; cubes: 8.
- Geometry maximum corner delta: 1.99999999895e-08 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (48 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.RubberDuckyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 144 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 30.0, 'head_pitch_degrees': 10.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_terribleterror (Tier 2)

- Exact bones: 21; cubes: 21.
- Geometry maximum corner delta: 1.99999999895e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.50083310175e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0.00018310546875 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (126 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.TerribleTerrorGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 378 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0770263671875, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.00018310546875 under the cap 0.01.

## model_wormlarge (Tier 2)

- Exact bones: 23; cubes: 23.
- Geometry maximum corner delta: 5.13419906199e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.88679622695e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (138 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.WormLargeGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 2.99999999953e-06 model units over 414 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 1.52587890625e-05, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_wormmedium (Tier 2)

- Exact bones: 8; cubes: 8.
- Geometry maximum corner delta: 3.17647603458e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.49999999977e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (48 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.WormMediumGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 4.99999999626e-07 model units over 144 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_wormsmall (Tier 2)

- Exact bones: 3; cubes: 3.
- Geometry maximum corner delta: 1.49999999977e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.51380315737e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 1.52587890625e-05 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (18 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.WormSmallGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 54 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 1.52587890625e-05, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 1.52587890625e-05 under the cap 0.01.

## model_cannonfodder (Tier 2)

- Exact bones: 6; cubes: 6.
- Geometry maximum corner delta: 1.4080127833e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 9.99999999474e-08.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (126 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CannonFodderGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 378 position channels; inputs {'limb_swing': 3.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 30.0, 'head_pitch_degrees': 10.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_caterkiller (Tier 2)

- Exact bones: 80; cubes: 80.
- Geometry maximum corner delta: 1.22065556252e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.62788205984e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (3280 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.CaterKillerGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00871276855469, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_cricket (Tier 2)

- Exact bones: 11; cubes: 11.
- Geometry maximum corner delta: 1.44568322872e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.03960780545e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (231 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CricketGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 693 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 7.62939453125e-05, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_herculesbeetle (Tier 2)

- Exact bones: 37; cubes: 37.
- Geometry maximum corner delta: 6.11882341538e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.57480157531e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1517 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.HerculesBeetleGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_crab (Tier 2)

- Exact bones: 46; cubes: 46.
- Geometry maximum corner delta: 1.06301458059e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.72592521244e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0.000137329101562 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1886 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.CrabGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000381469726562, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000137329101562 under the cap 0.01.
- Render instances: leg1 x8 (explicit, per-draw transforms), leg2 x8 (explicit, per-draw transforms), leg3 x8 (explicit, per-draw transforms); 24 clone and 0 group bones (24 explicit clones, 984 of the draws proven by draw pose alone: no group bone); 984 measured draws over 41 captures: instance pose linear delta 0, translation 0 model units; draw pose linear delta 1.49999999977e-07, translation 9.4899999965e-06 model units.

## model_kyuubi (Tier 2)

- Exact bones: 42; cubes: 42.
- Geometry maximum corner delta: 4.09351927059e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.72207755708e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (882 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.KyuubiGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 1.00000000103e-06 model units over 2646 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 30.0, 'head_pitch_degrees': 10.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0010986328125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_leafmonster (Tier 2)

- Exact bones: 5; cubes: 5.
- Geometry maximum corner delta: 3.49999999966e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.21655250542e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (205 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.LeafMonsterGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00669860839844, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_alosaurus (Tier 2)

- Exact bones: 21; cubes: 21.
- Geometry maximum corner delta: 5.38516480409e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 7.61577310696e-08.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (861 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.AlosaurusGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 1.99999999673e-07 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_attacksquid (Tier 2)

- Exact bones: 9; cubes: 9.
- Geometry maximum corner delta: 2.99999999953e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.5709920267e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (189 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.AttackSquidGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 567 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 30.0, 'head_pitch_degrees': 10.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_bandp (Tier 2)

- Exact bones: 7; cubes: 7.
- Geometry maximum corner delta: 3.01496268591e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.23707192424e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (147 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.BandPGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 441 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 30.0, 'head_pitch_degrees': 10.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_baryonyx (Tier 2)

- Exact bones: 52; cubes: 52.
- Geometry maximum corner delta: 4.12310562587e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (1092 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.BaryonyxGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 3276 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000152587890625, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_camarasaurus (Tier 2)

- Exact bones: 21; cubes: 21.
- Geometry maximum corner delta: 3.17647603877e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.48663604743e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (441 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.CamarasaurusGeoReplacement`).
- Entity states: ['full_health']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 20.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0048828125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_cassowary (Tier 2)

- Exact bones: 12; cubes: 12.
- Geometry maximum corner delta: 2.44131112368e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (252 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CassowaryGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 756 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00042724609375, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_creepinghorror (Tier 2)

- Exact bones: 26; cubes: 26.
- Geometry maximum corner delta: 2.15406592193e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.068816087e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0.000152587890625 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (546 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CreepingHorrorGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 1638 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0020751953125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000152587890625 under the cap 0.01.

## model_cryolophosaurus (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 2.41867732545e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (420 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CryolophosaurusGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 1260 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00230407714844, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_easterbunny (Tier 2)

- Exact bones: 13; cubes: 13.
- Geometry maximum corner delta: 2.02237484057e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (273 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.EasterBunnyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 819 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_flounder (Tier 2)

- Exact bones: 6; cubes: 6.
- Geometry maximum corner delta: 1.04403065034e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (126 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.FlounderGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 378 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_pointysaurus (Tier 2)

- Exact bones: 30; cubes: 30.
- Geometry maximum corner delta: 3.23882694758e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.52970585382e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1230 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.PointysaurusGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_whale (Tier 2)

- Exact bones: 14; cubes: 14.
- Geometry maximum corner delta: 1.01980390283e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.40370937201e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (294 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.WhaleGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 882 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_molenoid (Tier 2)

- Exact bones: 37; cubes: 37.
- Geometry maximum corner delta: 7.28010989018e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1517 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.MolenoidGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 1.00000000103e-06 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000717163085938, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_rat (Tier 2)

- Exact bones: 12; cubes: 12.
- Geometry maximum corner delta: 1.99999999895e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (492 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.RatGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000518798828125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_spitbug (Tier 2)

- Exact bones: 93; cubes: 93.
- Geometry maximum corner delta: 6.20000000347e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.59615099647e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 6.103515625e-05 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (3813 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.SpitBugGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 5.99999999906e-07 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000839233398438, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 6.103515625e-05 under the cap 0.01.

## model_stinkbug (Tier 2)

- Exact bones: 50; cubes: 50.
- Geometry maximum corner delta: 1.06301458075e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.20000000048e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 1.52587890625e-05 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (1050 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.StinkBugGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 3150 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0, 0.25, 0.5, 1.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00178527832031, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 1.52587890625e-05 under the cap 0.01.

## model_trooperbug (Tier 2)

- Exact bones: 134; cubes: 134.
- Geometry maximum corner delta: 9.43398113309e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.01246117979e-07.
- Animation maximum rotation delta: 5.99999999635e-13 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 3.0517578125e-05; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (5494 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.TrooperBugGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 5.99999999635e-13 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00308227539062, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_velocityraptor (Tier 2)

- Exact bones: 34; cubes: 34.
- Geometry maximum corner delta: 3.08706980768e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.00042163966e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1394 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.VelocityRaptorGeoReplacement`).
- Entity states: ['full_health', 'sitting']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00462341308594, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_ghostskelly (Tier 2)

- Exact bones: 10; cubes: 10.
- Geometry maximum corner delta: 3.01522801969e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.50030177641e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 1.52587890625e-05; maximum mean absolute error: 0.002685546875; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (60 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.GhostSkellyGeoReplacement`).
- Entity states: ['idle']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 5.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00148010253906, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Render mode: entity_translucent (vertex colour (255, 255, 255, 255), light world): blend SRC_ALPHA / ONE_MINUS_SRC_ALPHA / ONE / ONE_MINUS_SRC_ALPHA over the background in emission order, LEQUAL depth test with the depth written (fragments within 1e-05 blocks are one plane and all pass), texel alpha < 0.1 discarded; the same emulation on both sides.
- Render state observed: both sides request entity_translucent (RenderType.entityTranslucent, the classic model's own render-type function - the same object on the candidate), vertex colour (255, 255, 255, 255) and packed light 0 at every captured vertex (1440 classic + 1440 candidate).

## model_hydrolisc (Tier 2)

- Exact bones: 40; cubes: 40.
- Geometry maximum corner delta: 2.15406592399e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.41774468823e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1640 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.HydroliscGeoReplacement`).
- Entity states: ['full_health', 'sitting']; rotation maximum delta 0 radians; position maximum delta 5.99999999962e-08 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0103302001953, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_lizard (Tier 2)

- Exact bones: 71; cubes: 71.
- Geometry maximum corner delta: 3.02654919187e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.00000000006e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (2911 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.LizardGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00973510742188, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_mantis (Tier 2)

- Exact bones: 36; cubes: 36.
- Geometry maximum corner delta: 1.01980390286e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.41421356167e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 1.52587890625e-05; maximum mean absolute error: 0.0029042561849; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 11 captures (396 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.MantisGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 9.99999999252e-07 model units; hidden-bone checks 10.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00218200683594, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_cavefisher (Tier 2)

- Exact bones: 75; cubes: 75.
- Geometry maximum corner delta: 2.0615528132e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.71172427651e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (4575 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.CaveFisherGeoReplacement`).
- Entity states: ['idle', 'claws_snapping', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00289916992188, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_chipmunk (Tier 2)

- Exact bones: 18; cubes: 18.
- Geometry maximum corner delta: 1.16619037836e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 3.5891200566e-08.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (658 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.ChipmunkGeoReplacement`).
- Entity states: ['idle', 'sitting']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00701904296875, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_enderknight (Tier 2)

- Exact bones: 40; cubes: 40.
- Geometry maximum corner delta: 4.60977222905e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.43136921886e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1640 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.EnderKnightGeoReplacement`).
- Entity states: ['idle', 'screaming']; rotation maximum delta 0 radians; position maximum delta 2.00000000117e-07 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0020751953125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_enderreaper (Tier 2)

- Exact bones: 66; cubes: 66.
- Geometry maximum corner delta: 8.06473805819e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.4177446887e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (2706 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.EnderReaperGeoReplacement`).
- Entity states: ['idle', 'screaming']; rotation maximum delta 0 radians; position maximum delta 2.00000000206e-06 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00157165527344, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_frog (Tier 2)

- Exact bones: 10; cubes: 10.
- Geometry maximum corner delta: 1.26822710955e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.84434270158e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (410 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.FrogGeoReplacement`).
- Entity states: ['idle', 'singing']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.006103515625, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_gazelle (Tier 2)

- Exact bones: 34; cubes: 34.
- Geometry maximum corner delta: 3.66196668434e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.60000052356e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1394 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.GazelleGeoReplacement`).
- Entity states: ['idle', 'crouching']; rotation maximum delta 0 radians; position maximum delta 1.99999999673e-07 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00816345214844, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_nastysaurus (Tier 2)

- Exact bones: 59; cubes: 59.
- Geometry maximum corner delta: 6.32551183999e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.38924439945e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 1.52587890625e-05 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (3599 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.NastysaurusGeoReplacement`).
- Entity states: ['idle', 'chewing', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00668334960938, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 1.52587890625e-05 under the cap 0.01.

## model_peacock (Tier 2)

- Exact bones: 16; cubes: 16.
- Geometry maximum corner delta: 3.74165738599e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.08627804957e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (656 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.PeacockGeoReplacement`).
- Entity states: ['idle', 'display']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000518798828125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_seaviper (Tier 2)

- Exact bones: 34; cubes: 34.
- Geometry maximum corner delta: 2.00034593036e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.00102473753e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1394 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.SeaViperGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 1.00000000103e-06 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00595092773438, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_urchin (Tier 2)

- Exact bones: 17; cubes: 17.
- Geometry maximum corner delta: 3.10644491296e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.82482875922e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (697 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.UrchinGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000503540039062, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_ostrich (Tier 2)

- Exact bones: 38; cubes: 38.
- Geometry maximum corner delta: 2.25610283539e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.11803398841e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (768 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.OstrichGeoReplacement`).
- Entity states: ['idle', 'wings_flapping', 'sitting', 'activated']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 20.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0219573974609, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_spyro (Tier 2)

- Exact bones: 37; cubes: 37.
- Geometry maximum corner delta: 2.12132034472e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.00997512427e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 81 captures (2997 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.SpyroGeoReplacement`).
- Entity states: ['idle', 'flying', 'legs_folded', 'sitting']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 80.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00314331054688, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_stinky (Tier 2)

- Exact bones: 20; cubes: 20.
- Geometry maximum corner delta: 2.00249844061e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.65697046301e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (1220 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.StinkyGeoReplacement`).
- Entity states: ['idle', 'legs_folded', 'sitting']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00344848632812, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_triffid (Tier 2)

- Exact bones: 178; cubes: 178.
- Geometry maximum corner delta: 8.12165383183e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.8287099534e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 16 captures (2848 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.TriffidGeoReplacement`).
- Entity states: ['idle', 'open', 'attacking']; rotation maximum delta 0 radians; position maximum delta 2.99999999953e-06 model units; hidden-bone checks 15.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00341796875, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_emperorscorpion (Tier 1)

- Exact bones: 78; cubes: 78.
- Geometry maximum corner delta: 1.28062484761e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 8.35463942937e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (4758 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.EmperorScorpionGeoReplacement`).
- Entity states: ['idle', 'claws_swinging', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0078125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Chain-link leg (the hierarchy form): 52 links under roots ['LeftShoulder', 'Leg1Seg1', 'Leg2Seg1', 'Leg3Seg1', 'Leg4Seg1', 'Leg5Seg1', 'Leg6Seg1', 'Leg7Seg1', 'Leg8Seg1', 'RightShoulder', 'Tailseg1']; world transforms of 4758 bone-samples over 61 samples within 1e-05 - maximum linear delta 4.6764352335e-07, maximum translation delta 8.09817934488e-07 blocks (worst s_idle_a0_25_t0:Leg3Seg5); the worst link Tailseg8 under Tailseg7: 6.96866e-08 linear / 8.09818e-07 blocks at s_idle_a0_t0.
- Surface leg of a hierarchy entry: normal epsilon 1e-05 (HIERARCHY_NORMAL_EPSILON); the chain's accumulation - the worst link Tailseg6 under Tailseg5 at depth 5: 8.35464e-07 at s_idle_a0_t0:Tailseg6#0; the maxima by depth {'0': 1.299999999870849e-07, '1': 2.213594362181519e-07, '2': 2.402082429737773e-07, '3': 2.8191529831357717e-07, '4': 5.811196089540885e-07, '5': 8.354639429370931e-07, '6': 7.810249673572553e-08, '7': 7.810249673572553e-08, '8': 1.4000000003733248e-07, '9': 3.444198019913622e-07}.
- Draw order of a hierarchy entry: GeoRenderer draws the key's pre-order (parent-first) on every capture; the classic renderToBuffer order deviates from it at up to 7 units on 61 of 61 captures (4 recorded findings) - judged by the visual leg above.

## model_alien (Tier 1)

- Exact bones: 55; cubes: 55.
- Geometry maximum corner delta: 1.15758369044e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.45201236583e-06.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0.000244140625 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (3355 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.AlienGeoReplacement`).
- Entity states: ['idle', 'claws_swinging', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000244140625, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000244140625 under the cap 0.01.
- Chain-link leg (the hierarchy form): 26 links under roots ['arml1', 'armr1', 'neck', 'tail1']; world transforms of 3355 bone-samples over 61 samples within 1e-05 - maximum linear delta 5.89538622281e-07, maximum translation delta 5.93968893181e-07 blocks (worst s_idle_a0_t_quarter:spike4); the worst link spike4 under tail4: 3.96026e-07 linear / 5.93969e-07 blocks at s_idle_a0_t_half.
- Surface leg of a hierarchy entry: normal epsilon 1e-05 (HIERARCHY_NORMAL_EPSILON); the chain's accumulation - the worst link clawl1 under arml2 at depth 2: 2.45201e-06 at s_idle_a0_t0:clawl1#0; the maxima by depth {'0': 2.0000000000575113e-07, '1': 3.1382826257612083e-07, '2': 2.4520123658282206e-06, '3': 2.766427751275831e-07, '4': 4.4130563958280783e-07}.
- Draw order of a hierarchy entry: GeoRenderer draws the key's pre-order (parent-first) on every capture; the classic renderToBuffer order deviates from it at up to 26 units on 61 of 61 captures (11 recorded findings) - judged by the visual leg above.

## model_alien_boss (Tier 1)

- Exact bones: 55; cubes: 55.
- Geometry maximum corner delta: 1.15758369044e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.45201236583e-06.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0.000244140625 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (3355 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.AlienBossGeoReplacement`).
- Entity states: ['idle', 'claws_swinging', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000244140625, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000244140625 under the cap 0.01.
- Chain-link leg (the hierarchy form): 26 links under roots ['arml1', 'armr1', 'neck', 'tail1']; world transforms of 3355 bone-samples over 61 samples within 1e-05 - maximum linear delta 5.89538622281e-07, maximum translation delta 5.93968893181e-07 blocks (worst s_idle_a0_t_quarter:spike4); the worst link spike4 under tail4: 3.96026e-07 linear / 5.93969e-07 blocks at s_idle_a0_t_half.
- Surface leg of a hierarchy entry: normal epsilon 1e-05 (HIERARCHY_NORMAL_EPSILON); the chain's accumulation - the worst link clawl1 under arml2 at depth 2: 2.45201e-06 at s_idle_a0_t0:clawl1#0; the maxima by depth {'0': 2.0000000000575113e-07, '1': 3.1382826257612083e-07, '2': 2.4520123658282206e-06, '3': 2.766427751275831e-07, '4': 4.4130563958280783e-07}.
- Draw order of a hierarchy entry: GeoRenderer draws the key's pre-order (parent-first) on every capture; the classic renderToBuffer order deviates from it at up to 26 units on 61 of 61 captures (11 recorded findings) - judged by the visual leg above.

## model_kraken (Tier 1)

- Exact bones: 111; cubes: 111.
- Geometry maximum corner delta: 4.01367661933e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 3.20420036858e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 16 captures (1776 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.KrakenGeoReplacement`).
- Entity states: ['idle', 'twitching', 'attacking']; rotation maximum delta 0 radians; position maximum delta 4.00000000056e-06 model units; hidden-bone checks 15.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_dungeonbeast (Tier 2)

- Exact bones: 60; cubes: 60.
- Geometry maximum corner delta: 5.28266230293e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 3.32489097586e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 7.62939453125e-05 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 61 captures (3660 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.DungeonBeastGeoReplacement`).
- Entity states: ['idle', 'jaws_still', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 60.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00241088867188, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 7.62939453125e-05 under the cap 0.01.

## model_basilisk (Tier 1)

- Exact bones: 21; cubes: 21.
- Geometry maximum corner delta: 1.08166538278e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.70293863625e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (861 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.BasiliskGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 4.00000000411e-06 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00395202636719, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_godzilla (Tier 1)

- Exact bones: 71; cubes: 71.
- Geometry maximum corner delta: 2.39999999962e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.03960780545e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 3.0517578125e-05; maximum mean absolute error: 0.000152587890625; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (2911 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.GodzillaGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.0023193359375, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_hammerhead (Tier 1)

- Exact bones: 37; cubes: 37.
- Geometry maximum corner delta: 6.24179461265e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.5132745959e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1517 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.HammerheadGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_trex (Tier 1)

- Exact bones: 27; cubes: 27.
- Geometry maximum corner delta: 5.38516480409e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 7.61577310696e-08.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1107 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.TRexGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 1.99999999673e-07 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_leon (Tier 1)

- Exact bones: 98; cubes: 98.
- Geometry maximum corner delta: 1.06644268413e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.50199920055e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 81 captures (4018 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.LeonGeoReplacement`).
- Entity states: ['idle', 'sitting', 'flying', 'flying_attacking']; rotation maximum delta 0 radians; position maximum delta 9.99999999696e-07 model units; hidden-bone checks 80.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_leonopteryx (Tier 1)

- Exact bones: 98; cubes: 98.
- Geometry maximum corner delta: 1.06644268413e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.50199920055e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 81 captures (4018 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.LeonopteryxGeoReplacement`).
- Entity states: ['idle', 'sitting', 'flying', 'flying_attacking']; rotation maximum delta 0 radians; position maximum delta 9.99999999696e-07 model units; hidden-bone checks 80.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_cephadrome (Tier 1)

- Exact bones: 50; cubes: 50.
- Geometry maximum corner delta: 1.41548578253e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.24053565011e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 16 captures (800 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.CephadromeGeoReplacement`).
- Entity states: ['idle', 'attacking', 'flying']; rotation maximum delta 0 radians; position maximum delta 5.00000000292e-08 model units; hidden-bone checks 15.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00396728515625, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_dragon (Tier 1)

- Exact bones: 55; cubes: 55.
- Geometry maximum corner delta: 8.56796358232e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.20227155466e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.000198446015051; maximum pair-contested fraction: 0.000411987304688 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (1155 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.DragonGeoReplacement`).
- Entity states: ['idle', 'attacking', 'flying', 'sitting']; rotation maximum delta 0 radians; position maximum delta 2.99999999953e-07 model units; hidden-bone checks 20.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00700378417969, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000411987304688 under the cap 0.01.

## model_baby_dragon (Tier 1)

- Exact bones: 55; cubes: 55.
- Geometry maximum corner delta: 8.56796358232e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.20227155466e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.000198446015051; maximum pair-contested fraction: 0.000411987304688 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (1155 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.BabyDragonGeoReplacement`).
- Entity states: ['idle', 'attacking', 'flying', 'sitting']; rotation maximum delta 0 radians; position maximum delta 2.99999999953e-07 model units; hidden-bone checks 20.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00700378417969, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0.000411987304688 under the cap 0.01.

## model_giantrobot (Tier 1)

- Exact bones: 29; cubes: 29.
- Geometry maximum corner delta: 2.00499376408e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.40000000037e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1189 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.GiantRobotGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Render instances: arm1 x2 (explicit, per-draw transforms), arm2 x2 (explicit, per-draw transforms), arm3 x2 (explicit, per-draw transforms), foot1 x2 (explicit, per-draw transforms), foot2 x2 (explicit, per-draw transforms), foot3 x2 (explicit, per-draw transforms), knuckles x2 (explicit, per-draw transforms), shin x2 (explicit, per-draw transforms), thigh x2 (explicit, per-draw transforms), thigh2 x2 (explicit, per-draw transforms), thigh3 x2 (explicit, per-draw transforms); 22 clone and 0 group bones (22 explicit clones, 902 of the draws proven by draw pose alone: no group bone); 902 measured draws over 41 captures: instance pose linear delta 0, translation 0 model units; draw pose linear delta 0, translation 1.52200000088e-05 model units.

## model_jeffery (Tier 1)

- Exact bones: 29; cubes: 29.
- Geometry maximum corner delta: 2.00499376408e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.40000000037e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (1189 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.JefferyGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.
- Render instances: arm1 x2 (explicit, per-draw transforms), arm2 x2 (explicit, per-draw transforms), arm3 x2 (explicit, per-draw transforms), foot1 x2 (explicit, per-draw transforms), foot2 x2 (explicit, per-draw transforms), foot3 x2 (explicit, per-draw transforms), knuckles x2 (explicit, per-draw transforms), shin x2 (explicit, per-draw transforms), thigh x2 (explicit, per-draw transforms), thigh2 x2 (explicit, per-draw transforms), thigh3 x2 (explicit, per-draw transforms); 22 clone and 0 group bones (22 explicit clones, 902 of the draws proven by draw pose alone: no group bone); 902 measured draws over 41 captures: instance pose linear delta 0, translation 0 model units; draw pose linear delta 0, translation 1.52200000088e-05 model units.

## model_pitchblack (Tier 1)

- Exact bones: 101; cubes: 101.
- Geometry maximum corner delta: 1.02610915611e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.02484567211e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 81 captures (8181 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.PitchBlackGeoReplacement`).
- Entity states: ['idle', 'chomping', 'attacking', 'flying']; rotation maximum delta 0 radians; position maximum delta 1.00000000103e-06 model units; hidden-bone checks 80.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000946044921875, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_seamonster (Tier 1)

- Exact bones: 23; cubes: 23.
- Geometry maximum corner delta: 6.04069532772e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 2.00000000006e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 1.52587890625e-05; maximum mean absolute error: 0.000162760416667; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 41 captures (943 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.SeaMonsterGeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 2.00000000206e-06 model units; hidden-bone checks 40.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000747680664062, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_theprince (Tier 1)

- Exact bones: 35; cubes: 35.
- Geometry maximum corner delta: 2.50000000146e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.48672355968e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0; maximum pair-contested fraction: 0 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 81 captures (2835 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.ThePrinceGeoReplacement`).
- Entity states: ['idle', 'attacking', 'legs_folded', 'sitting']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 80.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.00177001953125, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 0 under the cap 0.01.

## model_theprinceadult (Tier 1)

- Exact bones: 119; cubes: 119.
- Geometry maximum corner delta: 5.09901951187e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.5556349194e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 1.52587890625e-05; maximum mean absolute error: 9.1552734375e-05; maximum pair-contested fraction: 1.52587890625e-05 (never a mismatch; cap 0.01).
- Draw order: GeckoLib bone order equals the classic draw order over 81 captures (9639 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.ThePrinceAdultGeoReplacement`).
- Entity states: ['idle', 'attacking', 'flying', 'sitting']; rotation maximum delta 0 radians; position maximum delta 1.9999999985e-06 model units; hidden-bone checks 80.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0.000259399414062, a diagnostic. Pair-contested pixels (the same two front faces on both sides within 1e-05 blocks) never a mismatch: maximum fraction 1.52587890625e-05 under the cap 0.01.

Reproduce with `gradlew.bat g1Parity`. Any mismatch exits nonzero before
proof evidence can be updated.
