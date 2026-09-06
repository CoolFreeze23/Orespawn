# PN draft (ENT-S-146, refuter A item D3) — the hurt / death flash on the orb: 1.7.10 drew no red; 1.21.1 tints red on both renderers

**Status of the premise: VERIFIED against the 1.7.10 client jar (law-11 check, 2026-09-06, fix lane).** Nothing here is recalled.

## What 1.7.10 did (the jar, class `boh` = `RendererLivingEntity`, method `a(sv, double, double, double, float, float)` = `doRender`)

Located by shape (`boh super=bno`, `bno` = `Render`; the only class whose `doRender` references both `GL11.glDepthFunc`
and `GL11.glColor4f` and calls `bhr.a(Lsa;FFFFFF)V` = `ModelBase.render` more than once). After the normal
`renderModel` pass (`a(sv, FFFFFF)`, offset 725 of this method) and `renderEquippedItems` (`c(sv, F)`, 802-804), the
method reads `getBrightness` (`sv.d(F)`, 808-813) and `getColorMultiplier` (`a(sv, FF)I`, 815-824), then:

| offsets | bytecode | what it is |
|---|---|---|
| 826-841 | `buu.j(buu.c)`; `glDisable(3553)`; `buu.j(buu.b)` | the LIGHTMAP texture unit disabled (`OpenGlHelper.setActiveTexture(lightmapTexUnit)`, `GL_TEXTURE_2D` off, back to the default unit) |
| 844-867 | `if ((colorMultiplier >>> 24 & 255) > 0 \|\| entity.hurtTime (sv.ax) > 0 \|\| entity.deathTime (sv.aA) > 0)` | the pass runs only when hurt / dying / colour-multiplied |
| 870-873 | `glDisable(3553)` | `GL_TEXTURE_2D` off: texturing OFF for the pass |
| 876-879 | `glDisable(3008)` | `GL_ALPHA_TEST` off |
| 882-894 | `glEnable(3042)`; `glBlendFunc(770, 771)` | `GL_BLEND`, `SRC_ALPHA / ONE_MINUS_SRC_ALPHA` |
| 897-900 | `sipush 514`; `glDepthFunc` | **`GL_EQUAL`**: only fragments at exactly the depth already written pass |
| 903-914 | `if (hurtTime > 0 \|\| deathTime > 0)` | the hurt / death branch |
| 917-923 | `glColor4f(brightness, 0, 0, 0.4f)` | **the red is set here, BEFORE the model draws** |
| 926-946 | `this.i.a(sa, FFFFFF)` | **a SECOND `mainModel.render`** with the same six floats as the normal pass |
| 949-1005 | four `inheritRenderPass` passes with the same red on `renderPassModel` | not applicable: PurplePower declares no render pass model |
| 1008-1169 | the colour-multiplier branch (`glColor4f(r, g, b, a)` from `getColorMultiplier`, a third render) | not taken for the orb (`getColorMultiplier` returns 0 for it) |
| 1172-1193 | `glDepthFunc(515 = GL_LEQUAL)`; `glDisable(GL_BLEND)`; `glEnable(GL_ALPHA_TEST)`; `glEnable(GL_TEXTURE_2D)` | the state restored |

And the orb's own model (orig `ModelPurplePower.java:55`) calls `glColor4f(0.75f, 0.75f, 0.75f, 0.55f)` at the top of
`render`, i.e. INSIDE that second pass, AFTER the renderer's red at 917-923 and BEFORE any quad is drawn. The red never
reaches a vertex of the orb. What the second pass actually drew for a hurt or dying PurplePower:

- every spoke again, untextured (texturing off: the flat colour (0.75, 0.75, 0.75) at alpha 0.55, no texel), blended
  SRC_ALPHA / ONE_MINUS_SRC_ALPHA over the frame the normal pass left;
- under `GL_EQUAL`, so a fragment passes only where the depth buffer holds exactly its depth — the depth the NORMAL pass
  wrote last at that pixel: with LEQUAL and the depth mask on, that is the nearest fragment that passed there (a
  farther fragment drawn earlier was blended but its depth overwritten by the nearer one). So the second pass re-blends
  ONLY the front-most layer of each pixel, once, with flat grey at 0.55 (plus the model's own `glColor4f(1, 1, 1, 1)`
  reset at :83 and `glDisable(GL_BLEND)` at :84 — the latter also switching the renderer's blend OFF for the rest of the
  pass, which is only the not-taken render-pass loop and the restore).

Player-visible in 1.7.10: on being hit or while dying the orb did not flash red; its front-most surfaces brightened
toward the flat 0.75 grey for the hurt / death frames (one more 0.55-alpha layer of untextured grey over the front layer
— the texel's purple washed ~55% toward grey there), with no lightmap on that layer (the lightmap unit was disabled at
826-841 for the pass). Note `glColor4f`'s alpha at 917-923 was 0.4, but the model overrode it to 0.55.

## What 1.21.1 does (both renderers of the port)

There is no second pass. `LivingEntityRenderer.render` (21.1.223) packs ONE overlay coordinate per draw —
`getOverlayCoords(entity, getWhiteOverlayProgress(entity, partialTick))` at 593; `getOverlayCoords` (430-444) =
`OverlayTexture.pack(OverlayTexture.u(whiteOverlay), OverlayTexture.v(hurtTime > 0 || deathTime > 0))` —
and hands it to `renderToBuffer` as `packedOverlay`, which every vertex carries. `OverlayTexture.v(hurt)` is 3 when hurt
or dying, else 10 (`v(Z)I`, offsets 0-10); the overlay texture's rows 0-7 are filled with the constant `-1308622593`
(alpha `0xB2` = 178/255 ≈ 0.7; the red hurt tint seen in-game) and rows 8-15 with the white-to-grey ramp
(`OverlayTexture.<init>`, the `setPixelRGBA` at 53-55 / 85-88). The entity shader then mixes it into the ONE draw's
colour: `rendertype_entity_translucent.fsh` line 26, `color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a)` —
30% of the overlay's red into every fragment of the orb (after `color *= vertexColor` at 25, before the lightmap at 27).
The GeckoLib candidate takes the same coordinate: `GeoReplacedEntityRenderer.getPackedOverlay` (4.8.4, 0-59) packs
`OverlayTexture.v(hurtTime > 0 || deathTime > 0)` for a `LivingEntity` and `GeoRenderer.defaultRender` forwards it to
every bone (the ENT-S-094 record); the orb is a `Mob`, so both renderers tint the single translucent draw ~30% red
while hurt or dying.

Player-visible in the port: the orb flashes red when hit and while dying, on both renderers alike — where 1.7.10
brightened its front layer toward grey and showed no red.

## Why the port does not reproduce 1.7.10 here

The 1.7.10 behaviour is an accident of GL state order (the model's `glColor4f` clobbering the renderer's red inside a
second, untextured, `GL_EQUAL` pass); 1.21.1 has no per-model colour call, no second pass and no `GL_EQUAL` re-draw —
the overlay is a per-vertex coordinate the shader mixes, decided by the renderer (`getOverlayCoords`) for the one draw.
Reproducing "no red, one extra flat-grey layer on the front-most surface" would mean (a) overriding `getOverlayCoords`
/ `getPackedOverlay` to `NO_OVERLAY` on both renderers for the species (the ENT-S-094 non-living mode already does the
former for non-living species — a one-line per-species hook is possible) AND (b) drawing a second untextured pass of the
model under an `equal`-depth render type that vanilla does not define (`RenderStateShard.EQUAL_DEPTH_TEST` exists
("==", 514), but no entity render type uses it, and a mod-defined one would need its own shader without texturing).
Part (a) alone would remove the red and give a plain translucent orb with no flash at all — closer to 1.7.10 than the
red, but not it. The harness emulates no overlay on either side (`NO_OVERLAY`), so neither the red nor a grey pass is
in the proof; the record lists the hurt overlay only as "not emulated".

## What is pinned

- Nothing in code: the record discloses it (this PN); `OreSpawnGeoReplacedEntityRenderer.getPackedOverlay` keeps
  GeckoLib's living behaviour for the orb (ENT-S-094 left the red pass to living species on purpose).
- Evidence: `pp146\fix\law11\mc1710_boh.txt` (`javap -c -p` of `boh`; the block quoted above at listing lines 462-655),
  `scan1710.py` / `scan1710.log` (how `boh` was located), `pp146\fix\javap\NF_OverlayTexture.txt`,
  `pp146\javap\NF_LivingEntityRenderer.txt` (`getOverlayCoords` 430-444, the call at 593),
  `pp146\refB\rendertype_entity_translucent.fsh` (line 26).

**For the owner — three readings, none chosen here:** (1) keep 1.21.1's red flash on both renderers (the port's
default for every living species; disclosed); (2) `NO_OVERLAY` for the orb on both renderers (no flash at all; a
per-species hook in the ENT-S-094 shape, one line each side, harness-neutral); (3) the 1.7.10 grey second pass
(a mod-defined render type and shader; out of proportion for a hurt frame of one species).
