# PN draft (ENT-S-146, refuter A item D2) — the within-cube face order is 1.21.1's, not 1.7.10's

**Status of the premise: VERIFIED against the 1.7.10 client jar (law-11 check, 2026-09-06, fix lane).** Nothing here is recalled.

## The divergence

Both renderers of the port emit a box's six faces in the order NeoForge 21.1.223's `ModelPart.Cube.<init>` stores its
polygons — DOWN, UP, WEST, NORTH, EAST, SOUTH (offsets 365, 436, 507, 578, 649, 720; each slot ends in its
`Polygon.<init>` and `aastore`, the last at 782-785; `compile` emits the array in order). The GeckoLib candidate is
permuted into that order at bake time (`FaceOrder.apply`, the `orespawn:cube_face_order` key) and the harness proves
the two sides' per-face normal sequences equal on every capture (972 faces over 9 captures). So the candidate matches
the CLASSIC RENDERER OF THIS PORT — which is vanilla 1.21.1's cube, not the original mod's.

The original drew a different order. In the 1.7.10 client jar the `ModelBox` is the class `bis` (located by shape, not
by name: the constructor `<init>(bix, int, int, float, float, float, int, int, int, float)` — ten arguments, `bix` being
`ModelRenderer` — with the fields `h [Lbii;` (eight `PositionTextureVertex`) and `i [Lbhv;` (six `TexturedQuad`; `bhv`'s
constructor takes a `bii[]`)). Its constructor builds the eight corners at offsets 155-297 (locals 14-21: (x1,y1,z1),
(x2,y1,z1), (x2,y2,z1), (x1,y2,z1), (x1,y1,z2), (x2,y1,z2), (x2,y2,z2), (x1,y2,z2)) and stores the six quads into `i[]` at:

| slot | offsets | corners (locals) | face |
|---|---|---|---|
| 0 | 365-437 | 19, 15, 16, 20 | x2 (+X) |
| 1 | 439-498 | 14, 18, 21, 17 | x1 (−X) |
| 2 | 500-559 | 19, 18, 14, 15 | y1 (−Y) |
| 3 | 561-626 | 16, 17, 21, 20 | y2 (+Y) |
| 4 | 628-693 | 15, 14, 17, 16 | z1 (−Z) |
| 5 | 695-772 | 18, 19, 20, 21 | z2 (+Z) |

`render` (`a(bmh, float)`, offsets 0-28) walks `i[0..5]` in order, calling `TexturedQuad.draw` (`bhv.a(Lbmh;F)V`) on each.
A mirrored box (`bix.i`, the `ModelRenderer.mirror` flag) swaps x1 and x2 BEFORE the corners are built (offsets 136-153,
exactly the swap 1.21.1's `Cube.<init>` performs at 121-135) and then reverses every quad's vertex order (774-806,
`bhv.a()V` = `flipFace`, which copies the array back to front). So for the PurplePower's three boxes — all mirrored
(`.mirror()` in `createBodyLayer`, orig `mirror = true`) — 1.7.10 drew: the min-x face, the max-x face, the min-y face,
the max-y face, the min-z face, the max-z face; 1.21.1 (and therefore both renderers of the port) draw: min-y, max-y,
min-x (WEST, at the swapped coordinate), min-z, max-x, max-z.

In vanilla's own terms: 1.7.10 = +X, −X, −Y, +Y, −Z, +Z (x swapped under mirror); 1.21.1 = DOWN, UP, WEST, NORTH, EAST,
SOUTH. The two orders agree on nothing but "the two Z faces come last, −Z before +Z".

## The player-visible effect (translucent only)

For an opaque (cutout) rig the order of a cube's own faces is invisible — the depth test decides, whatever the emission
order (only a zero-thickness box ties). For the PurplePower orb, drawn with `GL_BLEND` and the depth mask on (orig
`ModelPurplePower.java:53-54`; 1.21.1 `RenderType.entityTranslucent`, `TRANSLUCENT_TRANSPARENCY` + `COLOR_DEPTH_WRITE`,
`LEQUAL`), the order decides which of a SPOKE'S OWN faces show through the others: a back face emitted BEFORE the front
face is blended and then overdrawn (it shows through the front face's 0.55 alpha); one emitted AFTER the front face is
depth-rejected and shows nothing. Each spoke is a 1 px × 1 px thick, 4 / 8 / 14 px long box in its own frame (X the long axis; ±X the two 1×1 END
faces, ±Y and ±Z the four long SIDE faces), so at a pixel where two of ITS OWN faces overlap — an end face and a side
face, at the spoke's tips seen obliquely — the pair that is drawn first is blended and written, and the second passes
(blends over, two layers) only if it lies in FRONT of the first:

- 1.7.10 draws the two end faces FIRST (+X, −X), every side face after. An end face behind a side face therefore shows
  through it (end blended, then the nearer side blended over: two layers); an end face in front of a side face hides it
  (the later, farther side face is depth-rejected: one layer).
- 1.21.1 draws DOWN and UP (the ±Y sides) first, then WEST (an end), NORTH (a Z side), EAST (the other end), SOUTH. Against
  the ±Y sides the end faces come SECOND, so the outcome flips at exactly those pixels: an end face behind a ±Y side is
  hidden (one layer) where 1.7.10 showed it through, and an end face in front of a ±Y side shows the side through it
  (two layers) where 1.7.10 hid it. Against the ±Z sides the relative order is 1.7.10's (ends before sides), unchanged.

Across a whole ring (six spokes, 36 faces, three rings blended over each other in emission order) this moves one
0.55-alpha layer of the 0.75-grey texel on or off the pixels at each spoke's tips where an end face overlaps a ±Y side
face — one layer is roughly the difference between (110, 32, 123) and (162, 96, 171) on the seeded captures (see
`before_after.md`): a slightly different shimmer at the spokes' tips, a few pixels per spoke at the harness's scale.
The rings' overall look — three translucent, glowing, randomly re-thrown fans — is unchanged; nothing else in the game
draws through this path.

## Why the port cannot follow 1.7.10 here

The classic renderer of the port IS vanilla's `ModelPart` / `ModelPart.Cube`: the order is `Cube.<init>`'s and
`compile`'s, not the mod's to set. Reproducing 1.7.10's order would require replacing vanilla's cube with a mod-owned
copy of `ModelPart.Cube` for one species (the whole `LayerDefinition` / `CubeListBuilder` bake path is `final` /
package-private around it), and the GeckoLib candidate would then have to be permuted into THAT order instead — a
fork of vanilla's model classes for a shimmer difference at the spokes' rims. The port matches the two renderers to
each other exactly (the face-order contract, proven per face), and to 1.7.10 in everything the render state controls
(blend, colour, light, the rolls, the accumulating rotations); the face order inside a cube is the one place the port
follows 1.21.1's vanilla over 1.7.10, disclosed here.

## What is pinned

- `FaceOrder` (the contract; the 1.7.10 divergence in its class comment, offsets cited); `ModelPurplePower` (the class
  comment names it); `tools/layer_definition_to_geo.py` (the evidence rule, offsets 365-785 per face).
- The harness proves the two renderers' face orders equal (972 faces / 9 captures) — it does not, and cannot, prove
  either equal to 1.7.10's.
- Evidence: `pp146\fix\law11\mc1710_bis.txt` (`javap -c -p` of `bis`), `mc1710_bhv.txt` (`bhv`, `flipFace` at `a()V`
  offsets 0-48), `mc1710_bii.txt`, the scanner `scan1710.py` and its log (how the classes were located: 1803 classes,
  exactly one constructor of the ten-argument shape).

**For the owner:** a disclosure, not a choice — unless the owner wants the vanilla cube forked for one species.
