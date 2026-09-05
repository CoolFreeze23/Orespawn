<!-- Phase G slice (c), presented 2026-09-06 for the owner's look BEFORE its gate (ruling 2026-09-05, addendum item 23 (8)(c)).
The code sits on branch g2-root-order (c5e9af1), refuted twice, ungated; it gates and lands on the owner's word together with
the pin removals below. Open for the owner: (1) the missing-key policy — DrawOrder fails loudly when a rig ships without the
order (a Blockbench re-export in a resource pack would crash the client on the mob's first frame); recommended: loud when the
key is present and wrong, a logged fallback to GeckoLib's own order when it is absent; (2) the pin removals — the fifteen
max_contested_fraction_pin entries and the four in_game_acceptance PENDING_OWNER strings become vacuous once nothing is
excluded; the two gradle parity tasks gain --no-contested-exclusion (or the tool's default flips); both proofs regenerate and
the benchmark re-pins; (3) the harness rasteriser's tie rule (first-wins) against the game's LEQUAL (last-wins) — parity is
unaffected, a fidelity note; (4) the in-game pass with the dev switch that gives the production seam its executed evidence. -->

# G2 root-order contract — BEFORE / AFTER, every species (2026-09-06)

What changes for a player: nothing, unless a rig self-overlaps. Where two faces of one
mob sit in the same plane, the game shows the face drawn LAST (the depth test is
LEQUAL, so a later coplanar fragment overwrites; the harness rasteriser's own tie rule is
first-wins — a fidelity note for the owner, parity unaffected since both sides share the rule
and now the order), and until now GeckoLib drew the same faces in its own
hash order, so the candidate could show the other face. With the contract the
candidate draws in the classic order, so Island / IslandToo (46% of every capture
contested), PurplePower (10.6% at bind), Robot5 (1.16%), Robot3 (0.25%), Robot1
(0.055%), Robot4 (0.04%) and Beaver (0.0015%) now resolve every contested pixel
exactly as the classic renderer does. Twelve of the fourteen rigs had a GeckoLib
order different from the classic one (Vortex and Coin are single-bone rigs); for the
five whose contested fraction was already 0 the image was already identical and only
the vertex stream order changed.

Columns: BEFORE = the last gradle run's dumps under build/{s4,g1} (GeckoLib's own order; they
predate G2 and reproduce the checked-in proofs' exclusion-on numbers exactly); AFTER = the bake sorted
into the classic order through the production `DrawOrder.apply`. "excl on" is the
ruling-2 z-fight exclusion as ratified; "excl OFF" compares every pixel. All
numbers from the parity tool's own `render_capture` / `pixel_diff` (scratch script
`before_after.py`, no re-implementation); changed = fraction of 65536 pixels,
MAE = mean absolute channel error over the compared pixels.

```
proof  model              pin(excluded)  BEFORE excl-on chg/MAE   BEFORE excl-OFF chg/MAE      AFTER excl-on chg/MAE    AFTER excl-OFF chg/MAE     worst-after-off
s4     model_elevator     0.000000      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_vortex       0.000000      0.000137  0.0042   0.000137  0.0042        0.000137  0.0042   0.000137  0.0042   bind.front
s4     model_coin         0.000000      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_island       0.464233      0.000000  0.0000   0.225342 37.7072        0.000000  0.0000   0.000000  0.0000   -
s4     model_islandtoo    0.464233      0.000000  0.0000   0.225342 15.6237        0.000000  0.0000   0.000000  0.0000   -
s4     model_robot1       0.000549      0.000000  0.0000   0.000549  0.0822        0.000000  0.0000   0.000000  0.0000   -
s4     model_robot5       0.011612      0.000000  0.0000   0.005615  0.5896        0.000000  0.0000   0.000000  0.0000   -
s4     model_robot2       0.000000      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_robot3       0.002502      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_robot4       0.000412      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_rockbase     0.000000      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_rotator      0.000000      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
s4     model_purplepower  0.105850      0.000000  0.0000   0.052643  3.1211        0.000000  0.0000   0.000000  0.0000   -
g1     model_elevator     0.000000      0.000000  0.0000   0.000000  0.0000        0.000000  0.0000   0.000000  0.0000   -
g1     model_beaver       0.000015      0.000000  0.0000   0.000015  0.0034        0.000000  0.0000   0.000000  0.0000   -
```

Reading the table:

- The "BEFORE excl-OFF" column is what ruling 2 had been excluding: Island 22.5% of
  the image at MAE 37.7 (IslandToo 15.6), PurplePower 5.3%, Robot5 0.56%, Robot1
  0.055%, Beaver 0.0015% - every one a coplanar face pair drawn in a different order
  by the two renderers. Robot3 / Robot4 had contested pixels whose two faces carried
  the same texel in the end (0 changed even without the exclusion).
- "AFTER excl-OFF" is 0 changed / 0 MAE for every species: with the bake sorted into
  the classic order, every previously contested pixel resolves identically on both
  sides. No species is left over - no FINDING to stop on.
- Vortex's 0.000137 / 0.0042 is its accepted boundary residual (4a-2), identical in
  every column: contested 0, unrelated to draw order.
- The contested fractions themselves are unchanged by the reorder (the mask records
  where two different texels meet the front, whichever wins), so every pin is met
  exactly in both runs; the `pixel_changed_fraction` (0.001) and MAE (0.25)
  thresholds are the ones the AFTER excl-OFF column passes under.
- The fixture `fixture_runtime_basis_yz` and the g1 fixture have no visual leg; their
  draw-order leg passes (7 / 1 captures).

Also measured, the draw-order leg itself (the direct proof, independent of pixels):
in every one of the 170 full captures across the 17 manifest entries the sequence
of parts the classic `renderToBuffer` drew equals the sequence of bones
`GeoRenderer` emitted cubes for (2,528 draws), and the traversal of a fresh GeckoLib
bake after `DrawOrder.apply` equals the order shipped in each geo.
