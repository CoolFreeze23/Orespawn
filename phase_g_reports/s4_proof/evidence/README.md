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
- visual: independent software rasterization of concrete `EntityModel.renderToBuffer` and `GeoRenderer` streams using the shipped texture.

## model_elevator (Tier 3)

- Exact bones: 5; cubes: 5.
- Geometry maximum corner delta: 0 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Static identity maximum rotation motion: 0 radians; no controller emitted.

## model_vortex (Tier 3)

- Exact bones: 1; cubes: 1.
- Geometry maximum corner delta: 0 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0.000137329101562; maximum mean absolute error: 0.00422668457031.

- Static identity maximum rotation motion: 0 radians; no controller emitted.

## model_island (Tier 3)

- Exact bones: 6; cubes: 6.
- Geometry maximum corner delta: 2.0124611809e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.04469506549e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.IslandGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 126 position channels; inputs {'limb_swing': 3.7, 'limb_swing_amounts': [0.0, 0.5, 1.0], 'net_head_yaw_degrees': 25.0, 'head_pitch_degrees': -12.0}.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.

## model_islandtoo (Tier 3)

- Exact bones: 6; cubes: 6.
- Geometry maximum corner delta: 2.0124611809e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.04469506549e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.IslandTooGeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 126 position channels; inputs {'limb_swing': 3.7, 'limb_swing_amounts': [0.0, 0.5, 1.0], 'net_head_yaw_degrees': 25.0, 'head_pitch_degrees': -12.0}.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.

## model_robot1 (Tier 3)

- Exact bones: 27; cubes: 27.
- Geometry maximum corner delta: 3.00026665435e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.Robot1GeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 891 position channels; inputs {'limb_swing': 6.1, 'limb_swing_amounts': [0.05, 0.5], 'net_head_yaw_degrees': 0.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.00054931640625.

## model_robot5 (Tier 3)

- Exact bones: 11; cubes: 11.
- Geometry maximum corner delta: 2.00000000117e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations through OreSpawnGeoReplacementModel.setCustomAnimations (`danger.orespawn.entity.client.Robot5GeoReplacement`).
- Rotation maximum delta 0 radians; position maximum delta 0 model units over 165 position channels; inputs {'limb_swing': 37.3, 'limb_swing_amounts': [0.05, 1.0], 'net_head_yaw_degrees': 40.0, 'head_pitch_degrees': 0.0}.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.0116119384766.

## model_robot2 (Tier 3)

- Exact bones: 15; cubes: 15.
- Geometry maximum corner delta: 1.00498756226e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.Robot2GeoReplacement`).
- Entity states: ['idle_ri0', 'idle_ri1', 'idle_ri2', 'idle_ri3', 'attacking_seeded']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 20.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.

## model_robot3 (Tier 3)

- Exact bones: 19; cubes: 19.
- Geometry maximum corner delta: 1.00498756225e-06 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.43178210569e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.Robot3GeoReplacement`).
- Entity states: ['idle_ri0', 'idle_ri1', 'attacking_ri0']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 12.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.00250244140625.

## model_robot4 (Tier 3)

- Exact bones: 56; cubes: 56.
- Geometry maximum corner delta: 3.70471321441e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 1.06301458092e-07.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.Robot4GeoReplacement`).
- Entity states: ['idle', 'attacking']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 12.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.000411987304688.

## model_rockbase (Tier 3)

- Exact bones: 22; cubes: 22.
- Geometry maximum corner delta: 2.00000000117e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.

- Accepted path: production OreSpawnGeoReplacement.applyCustomAnimations posed from declared entity states through the entity's pose interface; compiled poseFrom on the same states (`danger.orespawn.entity.client.RockBaseGeoReplacement`).
- Entity states: ['type_0', 'type_1', 'type_2', 'type_3', 'type_4', 'type_5', 'type_6', 'type_7', 'type_8', 'type_9', 'type_10', 'type_11', 'type_12', 'type_13']; rotation maximum delta 0 radians; position maximum delta 0 model units; hidden-bone checks 14.
- Visual z-fight pixels excluded (ruling 2): maximum contested fraction 0.

## fixture_runtime_basis_yz (non-production fixture)

- Coverage: nested_parent_bone, non_mirrored_uv, nonzero_bind_rotation, uniform_inflate.
- Geometry maximum corner delta: 2.76767050085e-07 blocks; surface UV maximum 0.
- Runtime basis proof: `danger.orespawn.g1.G1RuntimeBasisFixtureReplacement` rotation maximum delta 0 radians, position maximum delta 0 model units, surface mapping exact over 336 posed vertex samples.

Reproduce with `gradlew.bat g1Parity`. Any mismatch exits nonzero before
proof evidence can be updated.
