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

## model_elevator (Tier 3)

- Exact bones: 5; cubes: 5.
- Geometry maximum corner delta: 0 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 6 captures (30 draws).

- Static identity maximum rotation motion: 0 radians; no controller emitted.

## model_beaver (Tier 2)

- Exact bones: 9; cubes: 9.
- Geometry maximum corner delta: 2.00000000117e-07 blocks (epsilon 1e-05).
- Surface maximum UV delta: 0; normal delta: 0.
- Animation maximum rotation delta: 0 radians (epsilon 2e-06).
- Visual maximum changed fraction: 0; maximum mean absolute error: 0.
- Draw order: GeckoLib bone order equals the classic draw order over 21 captures (189 draws).

- Accepted path: exact Mth.cos GeoModel.setCustomAnimations legacy-parity exception.
- Dense actual-candidate maximum delta: 0 radians over 2380 samples; minimum authored-key/probe separation 0.000733999999994 age ticks, coincidences 0.
- Candidate gait proportionality maximum delta: 5.00000000292e-08 radians over amplitudes [0.0, 0.25, 0.5, 1.0]; candidate unscaled-channel delta 0.
- Reference JSON: `REFERENCE_ONLY_NOT_RUNTIME_ACCEPTANCE`; baked-keyframe runtime acceptance is false and artist-editable math-to-keyframes remains `OUTSTANDING_G3`.
- Keyframe reference leg: 2.5e-3 rad; Beaver reference leg 15 / 13 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included.
- Keyframe leg: `danger.orespawn.entity.client.animation.PhaseLockedKeyframeController` layers of `danger.orespawn.entity.client.BeaverGeoReplacement` over clip `src/main/resources/assets/orespawn/animations/entity/beaver.animation.json` (sha256 40f305d76c3f), spline repair ON; sample-grid max delta 0.00229524 radians over 17568 layer-bone samples (gait 0.00216249, teeth 0.00189214, tail 0.00229524); 264 wrap pairs, |v(T-eps) - v(0+eps)| max 3e-08; non-layer bones moved 0.
- Keyframe density search (an output): gait fewest 15 keys/bone at 0.00216275 over 1057088 comparisons, one fewer 0.00271118; teeth fewest 13 keys/bone at 0.00189218 over 264272 comparisons, one fewer 0.00254428; tail fewest 8 keys/bone at 0.00229527 over 264272 comparisons, one fewer 0.00398448; dense schedule of the shipped-candidate clip: gait 0.00216275, tail 0.00229527, teeth 0.00189218; reversed schedule max delta 0.

## fixture_nested_nonmirrored_rotated_inflate (non-production fixture)

- Coverage: nested_parent_bone, non_mirrored_uv, nonzero_bind_rotation, uniform_inflate.
- Geometry maximum corner delta: 1.41509716909e-07 blocks; surface UV maximum 0.
- Draw order: GeckoLib bone order equals the classic draw order over 1 captures (2 draws).

Reproduce with `gradlew.bat g1Parity`. Any mismatch exits nonzero before
proof evidence can be updated.
