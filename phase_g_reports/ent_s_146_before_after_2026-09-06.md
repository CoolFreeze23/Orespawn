# ENT-S-146 — the visual leg's translucent mode: before / after, every species (presented BEFORE the gate)

The harness-semantics change. `tools/g1_render_parity.py` gains a per-model visual mode, declared in the manifest
(`visual_mode`), beside the default it has always run: `entity_cutout_no_cull`, the verbatim `render_capture` path
(no blending, alpha < 0.1 discarded, depth written, first-wins at an exact tie). The new mode, `entity_translucent`,
emulates what the GPU does for ONE model's quads in emission order under `RenderType.entityTranslucent`'s states
(NeoForge 21.1.223 bytecode: `RenderType.lambda$static$7` sets `TRANSLUCENT_TRANSPARENCY` (22-25) and `NO_CULL`
(28-31) with the builder defaults `LEQUAL_DEPTH_TEST` / `COLOR_DEPTH_WRITE` (`CompositeStateBuilder.<init>` 26-29 /
75-78); `RenderStateShard.lambda$static$10` = `enableBlend; blendFuncSeparate(SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE,
ONE_MINUS_SRC_ALPHA)`; `COLOR_DEPTH_WRITE = new WriteMaskStateShard(true, true)`, `<clinit>` 1179-1188;
`LEQUAL_DEPTH_TEST = new DepthTestStateShard("<=", 515)`, 1147-1160; `rendertype_entity_translucent.fsh`: `if
(color.a < 0.1) discard;` on the texture alpha, then `color *= vertexColor`):

- the texel is discarded at alpha < 0.1 (the texture alpha, before the vertex colour — the same line in both entity
  shaders, so the vertex alpha never discards);
- the fragment colour is the texel times the vertex colour the manifest declares as RGBA bytes — PurplePower's
  (191, 191, 191, 140), what `FastColor.ARGB32.colorFromFloat(0.55, 0.75, 0.75, 0.75)` packs (`as8BitChannel` =
  `Mth.floor(f * 255)`) and every vertex carries on both renderers;
- depth test LEQUAL with the depth WRITTEN: a passing fragment writes its depth; a later fragment behind it is
  rejected; a later fragment on the same plane passes and blends again. The harness holds "same plane" to a window
  that is its OWN named tolerance — the manifest's `thresholds.coplanar_depth_epsilon_blocks`, 1e-5 blocks, an owner
  ruling presented with its number below, no default in the tool (a blended model in a manifest without the field
  fails loudly; refuter B, D2a) — two fragments closer than that are one plane and all pass, in emission order, on
  both sides. What the data shows (refuter B's sweep, `refB\sweep_output.txt` / `followup_output.txt`): on the classic
  side alone the six spokes' coplanar depths agree to <= 1e-9 at most contested pixels (155 / 202 / 237 / 175 of the
  sampled between-quad gaps per capture), so a ring's shared planes are genuine ties there, not noise; the CROSS-SIDE
  difference for the SAME fragment is ~1e-7 genuine (vertex deltas <= 1.8e-7 blocks; median 2.8e-17 to 8.4e-8, p99
  <= 1.3e-6) plus up to ~1e-6 from the harness's own `Camera.project`, which rounds every vertex to 6 decimals before
  projecting — a pre-existing quirk of BOTH modes (the cutout leg's first-wins ties see the same rounding). A window
  below that noise therefore flips passes between the two sides; the sweep and the options are in the section below.
  The test is against the LAST written depth (a chain of near-coplanar fragments can walk the front back: measured
  maximum 9.9e-6 blocks, no chained pass beyond one window; refuter B, D5). Faces a spoke's thickness apart (1/16
  block and more) are decided exactly as the GPU decides them;
- blending SRC_ALPHA / ONE_MINUS_SRC_ALPHA over what the pixel holds (the background first), quantised to 8 bits
  after every fragment as an RGBA8 framebuffer is; the destination alpha stays opaque;
- the contested diagnostic in this mode: a pixel where ANOTHER quad's fragment passed within the plane tolerance,
  whatever its texel (under blending a second layer changes the pixel even when its colour is the same), never
  cleared by a nearer fragment (every earlier layer still contributes). A diagnostic only; every pixel is compared.
- Not emulated on either side, in either mode: the lightmap multiply (PurplePower's `full_bright` texel is white, so
  its flat texel colour is the game's; a world-lit model's is the game's before its lightmap), the directional
  `minecraft_mix_light` shading (the same normals on both sides, proven by the surface leg), fog, the hurt overlay
  (`NO_OVERLAY`), the GPU's top-left fill rule (a pixel centre exactly on an edge shared by two DIFFERENT quads is
  covered by both here — identical on both sides except within ~2e-5 px of an edge).

The default path is untouched code: every model without a `visual_mode` runs `render_capture` as before, and the
report gains no field for it.

## Per species — the checked-in proofs (before) against the scratch run of the same tool on the same tree (after)

`changed` = max changed pixel fraction, `MAE` = max mean absolute error, `contested` = max contested fraction (a
diagnostic). "identical" = the model's whole report entry and every one of its PNGs byte-identical to the checked-in
proof.

| suite | model | mode before -> after | captures | changed before -> after | MAE before -> after | contested before -> after | report entry + PNGs |
|---|---|---|---|---|---|---|---|
| s4 | model_elevator | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0 -> 0 | identical |
| s4 | model_vortex | entity_cutout_no_cull -> entity_cutout_no_cull | 10 -> 10 | 0.000137329 -> 0.000137329 | 0.00422668 -> 0.00422668 | 0 -> 0 | identical |
| s4 | model_coin | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0 -> 0 | identical |
| s4 | model_island | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0.464233 -> 0.464233 | identical |
| s4 | model_islandtoo | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0.464233 -> 0.464233 | identical |
| s4 | model_robot1 | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0.000549316 -> 0.000549316 | identical |
| s4 | model_robot5 | entity_cutout_no_cull -> entity_cutout_no_cull | 2 -> 2 | 0 -> 0 | 0 -> 0 | 0.0116119 -> 0.0116119 | identical |
| s4 | model_robot2 | entity_cutout_no_cull -> entity_cutout_no_cull | 3 -> 3 | 0 -> 0 | 0 -> 0 | 0 -> 0 | identical |
| s4 | model_robot3 | entity_cutout_no_cull -> entity_cutout_no_cull | 2 -> 2 | 0 -> 0 | 0 -> 0 | 0.00250244 -> 0.00250244 | identical |
| s4 | model_robot4 | entity_cutout_no_cull -> entity_cutout_no_cull | 3 -> 3 | 0 -> 0 | 0 -> 0 | 0.000411987 -> 0.000411987 | identical |
| s4 | model_rockbase | entity_cutout_no_cull -> entity_cutout_no_cull | 12 -> 12 | 0 -> 0 | 0 -> 0 | 0 -> 0 | identical |
| s4 | model_rotator | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0 -> 0 | identical |
| s4 | model_purplepower | entity_cutout_no_cull -> **entity_translucent** | 5 -> 4 | 0 -> 3.05176e-05 | 0 -> 0.000457764 | 0.10585 -> 0.160751 | changed (the fixed classic, new states, the new mode) |
| s4 | fixture_runtime_basis_yz | no visual leg | — | — | — | — | identical |
| g1 | model_elevator | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 0 -> 0 | identical |
| g1 | model_beaver | entity_cutout_no_cull -> entity_cutout_no_cull | 5 -> 5 | 0 -> 0 | 0 -> 0 | 1.52588e-05 -> 1.52588e-05 | identical |
| g1 | fixture_nested_nonmirrored_rotated_inflate | no visual leg | — | — | — | — | identical |

Byte identity (scratch proof vs checked-in, `compare_proofs.py`): s4 — 240 files identical, among them all 186 PNGs
of the twelve cutout models and the fixture, every `generated/*` of every other model (the Rotator's
`conversion.json` included), every reference-leg file; changed: `evidence/report.json`, `evidence/README.md`,
`generated/model_purplepower.{geo,conversion,animation-contract}.json` (`model_purplepower.animation.json` is
byte-identical: the same empty clip); the 15 old PurplePower PNGs go, 12 new come (four captures). The report's
top-level fields differ only in `visual_artifact_count` (201 -> 198) and, after the refuters' items, `thresholds`
(the named `coplanar_depth_epsilon_blocks`). g1 — 44 of 44 files identical, every report entry and top-level field
identical. The fix lane's run against the implementation lane's (`fix\compare_proofs_fix.py`): s4 254 of 257 files
identical — all 198 PNGs, the four PurplePower captures included, so nothing the refuters' items changed moved a
pixel — and only `evidence/report.json`, `evidence/README.md` (the observed render state, the named tolerance's
wording) and `generated/model_purplepower.conversion.json` (the evidence string's per-face offsets, the compiled
dump's sha — `ModelPurplePower.java` changed) differ; g1 44 of 44.

## PurplePower under the new mode — the captures

Before (the HEAD classic, `code_driven` ages, cutout): bind 0 / 0 / 0.10585; t_0_001250 0 / 0 / 0; t_0_005000 0 / 0 /
0; t_0_030000 0 / 0 / 0; t_quarter 0 / 0 / 0.0305939 (changed / MAE / contested).

After (the fixed classic, `entity_state` seeds, translucent), changed / MAE / contested / foreground (vanilla = geo):

| capture | changed | MAE | contested (blend diagnostic) | foreground |
|---|---|---|---|---|
| s_seed_1_t0 | 0 | 0 | 0.126633 | 0.1619 / 0.1619 |
| s_seed_12345_t0 | 3.05176e-05 (2 pixels of 65,536) | 0.000457764 | 0.160751 | 0.2034 / 0.2034 |
| s_seed_2026_t_half | 0 | 0 | 0.14183 | 0.1681 / 0.1681 |
| s_seed_777_t0 | 0 | 0 | 0.137787 | 0.1920 / 0.1920 |

The two pixels (137, 126) and (135, 131) of `s_seed_12345_t0` (refuter B's fragment logs, `refB\sweep_output.txt`):
the same 36 fragments on both sides, and the flip is quads 6 and 25 of one spoke — a 1/16-block face seen almost
edge-on, projecting to 3.8 px² with a 0.256-block depth span across ~0.3 px, so a 2e-7-block vertex difference between
the sides becomes 1.6e-5 / 2.5e-5 blocks of interpolated depth at the pixel centre; on the classic side three layers
blend there, on the candidate two. The two pixels are present at EVERY window of the sweep below (1e-6 through 1e-5;
gone only from 3e-5 up, where the window itself exceeds the flip) and both are flagged by the contested diagnostic on
the classic side — the residual is the face's depth gradient, not the tie window. The same mechanism sits behind the
Vortex's accepted 0.000137 (silhouette pixels under cutout). The 0.001 changed-fraction threshold is the existing
ruling; nothing tuned.

## The depth-tie window — the sweep, the residual and the options (refuter B, D2; presented, not chosen)

Changed pixels per capture (`s_seed_1_t0` / `s_seed_12345_t0` / `s_seed_2026_t_half` / `s_seed_777_t0`) as the
harness's window is swept (the tool's own `render_capture_blended`, both sides; refuter B's `sweep.py`, verbatim):

| window (blocks) | s_seed_1_t0 | s_seed_12345_t0 | s_seed_2026_t_half | s_seed_777_t0 |
|---|---|---|---|---|
| 0 | 2935 | 4582 | 5380 | 3757 |
| 1e-7 | 153 | 1016 | 948 | 511 |
| 1e-6 | 0 | 94 | 158 | 35 |
| 3e-6 | 0 | 2 | 0 | 12 |
| 1e-5 (the named tolerance) | 0 | 2 | 0 | 0 |
| 3e-5, 1e-4, 1e-3 | 0 | 0 | 0 | 0 |
| 1e-6 with an UNROUNDED projector | 0 | 2 | 4 | 7 |

(At 1e-6 every one of the 94 / 158 changed pixels of the two middle captures is contested on at least one side; at
1e-5 the two residual pixels are the edge-on face above.) The last row is the same sweep with `Camera.project`'s
6-decimal rounding removed: the cross-side noise then sits at ~1e-7 and a 1e-6 window leaves 0 / 2 / 4 / 7 — the
rounding, not the geometry, is most of what a 1e-6 window trips over.

The options for the owner (none chosen here; the tolerance is a ruling, presented with its number):

1. keep 1e-5 as the named tolerance `coplanar_depth_epsilon_blocks` (the manifest as landed: 0 / 2 / 0 / 0 changed,
   both residual pixels explained above, the projector untouched);
2. 1e-6 with the projector's rounding removed (0 / 2 / 4 / 7 at 1e-6 unrounded) — a harness-semantics change for EVERY
   species, since the cutout leg's first-wins ties see the same rounding, to be presented before/after on its own
   before any gate; not done here.

Two more notes on the same leg (refuter B, D6 / D5): `pixel_channel_tolerance: 2` is a pre-existing threshold and is
NOT changed, but under a single 0.55-alpha layer one texel level moves the output channel by only ~0.55 of a level
(the alpha; ~0.41 with the 0.75 colour multiply as well), where an opaque capture moves it by a full level — so the
same 2-level tolerance spans ~1.8× (refuter B's figure, through the alpha alone; up to ~2.4× with the colour) the
texel levels it spans on an opaque capture: looser per texel level, disclosed, not tuned; and the window tests against
the LAST written depth (max walk-back 9.9e-6 blocks measured, chained passes 0).

## The render state — observed on both sides, not asserted (refuter B, D3)

The probe now records, beside every dump (`<id>.render-state.json`, both modes; the dumps themselves byte-identical —
the compiled dump's sha256 is the conversion's provenance), what each side actually requested, and the parity tool
requires the two sides equal to each other and to the model's visual mode for EVERY model (`G1 RENDER STATE PASS`, 13
s4 + 2 g1 lines): PurplePower `entity_translucent` / vertex colour (191, 191, 191, 140) / packed light 15728880 on both
sides over 3888 + 3888 observed vertices — the classic side's render type read from the `Model.renderType` function
the model holds (its owner `ModelPurplePower`, the one `RenderType` factory that class references: `entityTranslucent`;
`RenderType` itself cannot be initialised in the probe JVM, OPT-029 R0, measured), the colour observed at every
`addVertex`, the light the classic RENDERER's own overrides answer (`PurplePowerRenderer`, evaluated registry-free:
15 / 15 → `LightTexture.pack` = 15728880); the candidate side's from the descriptor hooks as the renderer applies them
(`renderType(entity)` = the SAME function object as the classic model's, `renderColor`, `fullBright` → the same packed
light), the colour and light handed to `actuallyRender` and observed at every `addVertex`. Every cutout model:
`entity_cutout_no_cull` (the `EntityModel` default on the classic side, `GeoModel.getRenderType` on the candidate's) /
white / the probe's light, and a cutout rig ever reporting anything else fails the leg loudly; their report entries
carry no field for it (byte-identical), PurplePower's carries `visual.render_state`.

What the mode changes, on the classic side alone — the same capture rasterised cutout and translucent (the
translucent run is what the leg compared):

| capture | foreground (both modes) | pixels differing cutout vs translucent | mean foreground RGB cutout -> translucent | contested (blend diagnostic) |
|---|---|---|---|---|
| s_seed_1_t0 | 0.1619 | 0.1619 (every foreground pixel) | (165.0, 41.3, 183.2) -> (110.4, 31.7, 122.9) | 0.1266 |
| s_seed_12345_t0 | 0.2034 | 0.2034 | (161.9, 36.6, 180.3) -> (105.8, 27.5, 118.4) | 0.1608 |
| s_seed_2026_t_half | 0.1681 | 0.1681 | (170.4, 46.9, 188.6) -> (113.0, 34.9, 125.3) | 0.1418 |
| s_seed_777_t0 | 0.1920 | 0.1920 | (162.8, 38.7, 181.0) -> (105.5, 29.8, 117.7) | 0.1378 |

(The dimming is the 0.75 colour times the 0.55 alpha over the dark background; where spokes overlap the layers
compound — the shimmer the player sees.)

Contested rose from 0.106 (bind, the HEAD classic's flat rings) to 0.13-0.16 because the blend-mode diagnostic
counts every same-plane other-quad layer whatever its texel: a ring's six spokes cross at the origin on two shared
planes, so a sixth of the image is "contested" and blends identically on both sides — every pixel compared, changed
0 on three captures.

## What the face-order contract changes (the other half of the mode)

Nothing for a cutout rig (no key written, no pass applied, the captures byte-identical above). For PurplePower the
candidate's quads come out of GeckoLib's `buildQuads` order (WEST, EAST, NORTH, SOUTH, UP, DOWN) and are permuted at
bake time into the classic `ModelPart.Cube` order (DOWN, UP, WEST, NORTH, EAST, SOUTH; a mirrored cube's X faces
swapped) — under blending with the depth written that decides whether a spoke's back face shows through its front
face. The draw-order leg compares the per-quad normal sequences of every cube on every capture: 972 faces over 9
captures equal. Without it the leg cannot pass (the back faces would blend on one side and be depth-rejected on the
other).
