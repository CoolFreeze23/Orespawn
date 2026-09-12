# Phase G1 proof — compiled LayerDefinition to GeckoLib geo

Status: **PASS**

Ground truth is each executed, compiled `createBodyLayer()` and its baked
`ModelPart` tree. The generated side is parsed and baked by pinned GeckoLib
4.8.4, then captured through `GeoRenderer`.

The independent gates are:

- geometry: baked ModelPart world-space cube corners versus GeckoLib-rendered geo corners;
- surface mapping: position/normal/UV tuple parity through the two pinned renderer paths;
- animation: independently executed compiled `setupAnim` versus the actual fresh-baked candidate path;
  Beaver uses the owner-approved exact `Mth.cos` custom-hook legacy-parity exception;
  its emitted clip is reference-only, not runtime acceptance, and editable keyframes remain G3 work;
- visual: independent software rasterization of concrete `EntityModel.renderToBuffer` and `GeoRenderer` streams using the shipped texture;
  every pixel is compared (G2 root-order contract, 2026-09-06) and the z-fight contested fraction is reported as a diagnostic only;
- draw order: per full capture, the sequence of parts the classic `renderToBuffer` drew equals the sequence of bones `GeoRenderer` emitted,
  and the order shipped in each geo (`orespawn:bone_draw_order`) equals the converter's, the probe's and the fresh bake's traversal.

## model_tshirt (Tier 2)

- Exact bones: 2; cubes: 2.
- Geometry maximum corner delta: 0 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (12 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.TshirtGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 48 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Tshirt reference leg 19 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.TshirtGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/tshirt.animation.json` (sha256 4185b567e3e5), spline repair ON; sample-grid max delta 0 radians over 14 layer-bone samples (turn 0); 1 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): turn fewest 19 keys/bone at 0.00221294 over 524584 comparisons, one fewer 0.00263029; dense schedule of the shipped-candidate clip: turn 0.00221294; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: turn declared 20 ticks, 0.0350141 clip ticks per age tick, LUT index 15875, clip tick 4.84467 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_mosquito (Tier 2)

- Exact bones: 5; cubes: 5.
- Geometry maximum corner delta: 2.0024984384e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (30 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.MosquitoGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 360 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Mosquito reference leg 13 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.MosquitoGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/mosquito.animation.json` (sha256 0f6871224fbc), spline repair ON; sample-grid max delta 0.001755335 radians over 92 layer-bone samples (wings 0.00175533); 9 wrap pairs, |v(T-eps) - v(0+eps)| max 3e-08; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189218 over 1052288 comparisons, one fewer 0.0025443; dense schedule of the shipped-candidate clip: wings 0.00189218; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 9.5493 clip ticks per age tick, LUT index 4409, clip tick 1.34552 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_cliffracer (Tier 2)

- Exact bones: 8; cubes: 8.
- Geometry maximum corner delta: 2.99999999953e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (48 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CliffRacerGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 336 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Cliff Racer reference leg 13 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.CliffRacerGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/cliffracer.animation.json` (sha256 96243b5dd44a), spline repair ON; sample-grid max delta 0.0017441 radians over 26 layer-bone samples (wings 0.0017441); 4 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189218 over 525112 comparisons, one fewer 0.00254427; dense schedule of the shipped-candidate clip: wings 0.00189218; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 4.13803 clip ticks per age tick, LUT index 41232, clip tick 12.583 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_brutalfly (Tier 2)

- Exact bones: 14; cubes: 14.
- Geometry maximum corner delta: 2.00997512317e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (84 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.BrutalflyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 420 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Brutalfly reference leg 13 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.BrutalflyGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/brutalfly.animation.json` (sha256 dd8850763250), spline repair ON; sample-grid max delta 0.00148505 radians over 108 layer-bone samples (wings 0.00148505); 2 wrap pairs, |v(T-eps) - v(0+eps)| max 0; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189218 over 3146784 comparisons, one fewer 0.0025443; dense schedule of the shipped-candidate clip: wings 0.00189218; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 0.827606 clip ticks per age tick, LUT index 47568, clip tick 14.5166 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_dragonfly (Tier 2)

- Exact bones: 26; cubes: 26.
- Geometry maximum corner delta: 3.0066592752e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.40000000037e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (156 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.DragonflyGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 3432 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Dragonfly reference leg 13 / 10 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.DragonflyGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/dragonfly.animation.json` (sha256 bd2c04161e49), spline repair ON; sample-grid max delta 0.00177852 radians over 258 layer-bone samples (wings 0.0017444, jaws 0.00177852); 19 wrap pairs, |v(T-eps) - v(0+eps)| max 3e-07; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 13 keys/bone at 0.00189233 over 1052528 comparisons, one fewer 0.0025444; jaws fewest 10 keys/bone at 0.00194411 over 526264 comparisons, one fewer 0.00280526; dense schedule of the shipped-candidate clip: jaws 0.00194411, wings 0.00189233; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 8.27606 clip ticks per age tick, LUT index 16928, clip tick 5.16602; jaws declared 20 ticks, 1.90986 clip ticks per age tick, LUT index 13989, clip tick 4.2691 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_cockateil (Tier 2)

- Exact bones: 16; cubes: 16.
- Geometry maximum corner delta: 2.03960780676e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.16619037864e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (96 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.CockateilGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 4992 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Cockateil reference leg 14 / 10 / 9 / 9 / 9 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.CockateilGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/cockateil.animation.json` (sha256 238f28f6f660), spline repair ON; sample-grid max delta 0.0022395 radians over 1030 layer-bone samples (wings 0.0019695, tail 0.00194092, feather1 0.00223942, feather2 0.00222948, feather3 0.0022395); 49 wrap pairs, |v(T-eps) - v(0+eps)| max 1.2e-07; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 14 keys/bone at 0.00210881 over 1054880 comparisons, one fewer 0.00264919; tail fewest 10 keys/bone at 0.00194409 over 791160 comparisons, one fewer 0.00280523; feather1 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather2 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather3 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367239; dense schedule of the shipped-candidate clip: feather1 0.00224417, feather2 0.00224417, feather3 0.00224417, tail 0.00194409, wings 0.00210881; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 4.77465 clip ticks per age tick, LUT index 2204, clip tick 0.672607; tail declared 20 ticks, 0.95493 clip ticks per age tick, LUT index 39762, clip tick 12.1344; feather1 declared 20 ticks, 3.50141 clip ticks per age tick, LUT index 14724, clip tick 4.49341; feather2 declared 20 ticks, 3.81972 clip ticks per age tick, LUT index 27978, clip tick 8.53821; feather3 declared 20 ticks, 4.13803 clip ticks per age tick, LUT index 41232, clip tick 12.583 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

## model_ruby_bird (Tier 2)

- Exact bones: 16; cubes: 16.
- Geometry maximum corner delta: 2.03960780676e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.16619037864e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (96 draws).

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.RubyBirdGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 4992 position channels; inputs {'limb_swing': 0.0, 'limb_swing_amounts': [0.0], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels compared, none excluded (G2 root-order contract): maximum contested fraction 0, a diagnostic.
- Keyframe reference leg: 2.5e-3 rad; Ruby Bird reference leg 14 / 10 / 9 / 9 / 9 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.RubyBirdGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/cockateil.animation.json` (sha256 238f28f6f660), spline repair ON; sample-grid max delta 0.0022395 radians over 1030 layer-bone samples (wings 0.0019695, tail 0.00194092, feather1 0.00223942, feather2 0.00222948, feather3 0.0022395); 49 wrap pairs, |v(T-eps) - v(0+eps)| max 1.2e-07; non-layer bones moved 0.
- Keyframe density search (an output): wings fewest 14 keys/bone at 0.00210881 over 1054880 comparisons, one fewer 0.00264919; tail fewest 10 keys/bone at 0.00194409 over 791160 comparisons, one fewer 0.00280523; feather1 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather2 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367241; feather3 fewest 9 keys/bone at 0.00224417 over 263720 comparisons, one fewer 0.00367239; dense schedule of the shipped-candidate clip: feather1 0.00224417, feather2 0.00224417, feather3 0.00224417, tail 0.00194409, wings 0.00210881; reversed schedule max delta 0.
- Keyframe late-prime twin (TEST-005): a manager built before its clips were served (unserved through the slot at age 137.371, no tick, bone motion 0), primed at age 138.371: wings declared 20 ticks, 4.77465 clip ticks per age tick, LUT index 2204, clip tick 0.672607; tail declared 20 ticks, 0.95493 clip ticks per age tick, LUT index 39762, clip tick 12.1344; feather1 declared 20 ticks, 3.50141 clip ticks per age tick, LUT index 14724, clip tick 4.49341; feather2 declared 20 ticks, 3.81972 clip ticks per age tick, LUT index 27978, clip tick 8.53821; feather3 declared 20 ticks, 4.13803 clip ticks per age tick, LUT index 41232, clip tick 12.583 - every fact the from-zero prime's; pose delta 0 at the prime, 0 over the follow-ups.

Reproduce with `gradlew.bat g1Parity`. Any mismatch exits nonzero before
proof evidence can be updated.
