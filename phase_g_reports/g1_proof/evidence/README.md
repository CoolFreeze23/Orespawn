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

## model_beaver (Tier 2)

- Exact bones: 9; cubes: 9.
- Geometry maximum corner delta: 2.00000000117e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 1.52587890625e-05; maximum mean absolute error: 0.00335184733073.

- Accepted path: exact Mth.cos GeoModel.setCustomAnimations legacy-parity exception.
- Dense actual-candidate maximum delta: 0 radians over 2380 samples; minimum authored-key/probe separation 0.000733999999994 age ticks, coincidences 0.
- Candidate gait proportionality maximum delta: 5.00000000292e-08 radians over amplitudes [0.0, 0.25, 0.5, 1.0]; candidate unscaled-channel delta 0.
- Reference JSON: `REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE`; baked-keyframe runtime acceptance is false and artist-editable math-to-keyframes remains `OUTSTANDING_G3`.

## fixture_nested_nonmirrored_rotated_inflate (non-production fixture)

- Coverage: nested_parent_bone, non_mirrored_uv, nonzero_bind_rotation, uniform_inflate.
- Geometry maximum corner delta: 1.41509716909e-07 blocks; surface UV maximum 0.

Reproduce with `gradlew.bat g1Parity`. Any mismatch exits nonzero before
proof evidence can be updated.
