# Known Issues — OreSpawn Port (BETA)

**This release is a beta.** The game logic underneath has been through a
1023-test automated suite (all green, in both robot modes; the count grows
with every remediation batch — the gate's last literal) plus hands-on
play sessions, but a lot
of the *visual and audio* polish has deliberately been left open for
community feedback. You will find rough edges — and we want to hear about
every one of them.

---

## Known visual rough edges

None of these are confirmed broken — they simply haven't been hand-checked
against the 1.7.10 original yet. If something below looks or sounds wrong in
your game, that's exactly the report we need.

- Some mob animations and model scales have not been hand-verified against the 1.7.10 originals (idle wing flaps, Mothra's giant size, Nightmares growing with their hitbox) — screenshots welcome. *(i043, i074)*
- Custom mob and boss sounds (Basilisk, Kraken, T-Rex, Godzilla, Prince wing flaps, Stinky's burps, Girlfriend/Boyfriend fight taunts) have not been verified by ear — some may be missing or fall back to vanilla audio. *(i048, i081, i087, i091, i104)*
- Riding the big mounts (Dragon, Leon, Leonopteryx, Cephadrome, Ostrich, the Prince mounts) has not been feel-tested — rides could feel floaty or misaligned, and the Left-Alt fly/sprint keybind is unverified. *(i066, i068, i102)*
- Hoverboard tricks are unverified: the wall-crash shatter, the rare high-speed malfunction, skin cycling with the Ultimate Sword, and the ride-only hum. *(i070, i071, i072)*
- Boss-fight presentation is unverified: the King/Queen models inside their huge hitboxes, boss-spawner sounds, the "Prepare to die!" transformation, and the death screen when a boss finishes you off. *(i096, i098, i099, i103)*
- Smaller effects like the Princess's firework-spark aura and the Krakens' individual mouth-twitch cycles are unverified. *(i076, i093)*
- Structures in the far dimensions have not been sightseen in a live client — Nightmare Rookery spires, Challenge Tower heights, the Ender Castle, the Islands rainbow, dungeon ground-anchoring — a structure could generate floating, sunken, or with missing decorations. *(i124, i125, i128, i136, i162, i170)*
- Terrain and spawn sweeps (Islands/Chaos terrain, ore-vein rates, which mobs spawn in which dimension) passed automated checks but still await a full manual fly-through. *(i106–i120, i130, i144)*
- The Village dimension loading on a live server, and its structure config gates, are unverified. *(i158, i164)*
- The giant Valentine's-Day Girlfriend (Feb 14) has not been seen in a real client. *(i178)*

---

## Fixed this cycle — please confirm

These came straight out of hand-testing and are fixed in code in this build;
most still need a second pair of eyes in a real game. If one still looks
wrong for you, please say so.

**post-beta.3 (in the next build):**

- The Princess and The Prince no longer spawn wild in the overworld (they
  could even appear right at world spawn on a brand-new world). This was
  leftover invented content — in the original they only come from spawn
  eggs, the Queen's death, and structures. Girlfriends and Boyfriends
  still roam wild exactly like 1.7.10. *(BUG-037)*

- The Queen no longer freezes mid-air (or endlessly repeats one attack
  swing) after her first melee — her attack animations now finish and
  blend back into her flying stance, and she stays animated through
  combat lulls like the original always did. Her death pose still holds.
  *(BUG-035)*

- Queen follow-ups from the same review: she flies and attacks at the
  original's full cadence (was half rate), her wake-up animation plays
  to the end, only hits that actually damage her wake her from the
  dormant blue phase, and two theoretical freeze edges are guarded.
  Watch for: her fight feeling noticeably more aggressive than the
  last build — that's the original's pace, not a bug. *(BUG-035
  follow-up)*

- The Chaos dimension is no longer a flat stone slab: it's back to the
  original's floating grassy islands over open void. The first fix
  attempt translated the original noise math faithfully but missed that
  the 1.7.10 generator INVERTS the Nether density field (stone where
  the Nether has air, air where it has rock — a photographic negative);
  a second field report ("I remember grass and floating islands")
  caught it. A third report ("more verticalness, hills and mountains")
  then caught two unit-conversion errors against the modern engine:
  noise sampled per-block instead of per-cell (terrain 4x/8x too fine)
  and the modern noise's /128 output normalization uncompensated
  (noise 128x too weak, so the banding curve flattened everything into
  plates). Both are now verified against the decompiled engine source,
  and a side-by-side render of the original math vs the fixed router
  is numerically identical. Also fixed: the port's invented water sea
  under Y64 is gone (the original places no fluid), grass+dirt is the
  default surface on every island top (was wrongly confined to the
  Y60-65 band, which is the one place the original makes it patchy),
  beds work, and dimension arrivals hunt for an island to land on like
  the original teleporter instead of blind-dropping into the void.
  Note: Chaos chunks you already generated keep their old shape —
  explore new areas or delete the dimension's region folder to
  regenerate. *(field reports)*

**beta.3 (first field reports — thank you!):**

- The mod no longer needs any other mod installed to launch: beta.2 crashed
  on startup unless something else provided the `databuddy` library; it's
  bundled now. *(BUG-032)*
- Fixed the game freezing (chunks stop loading, blocks stop breaking) when
  exploring near Basilisk Mazes or royal trees, most often in the Mining
  dimension with performance mods like c2me or Distant Horizons' distant
  generation installed. Frozen worlds are safe — affected chunks
  regenerate cleanly. *(BUG-033)*
- The Dungeon Beast actually spawns now (it never could in beta.2, and it
  spammed "Failed to create mob" into the log while trying). *(BUG-034)*

**beta.2 cycle:**

- Pizza looks like pizza now — a cake-style block you eat slice by slice, not a flat filled-in square. *(i003)*
- Duct tape actually works now, and it works like cake: right-click the ground to **place** the tape, then click the placed tape with the damaged item (a single one, main hand) to repair. Six uses per tape. *(i003 / TF-027)*
- Thrown rocks, shoes, water balls, and the other throwables are now visible in flight instead of invisible. *(i018, i019, i020)*
- The hoverboard now sits at your feet instead of hovering through the middle of your body. *(i069 / TF-029)*
- The rat's model is fixed (its texture was scrambled across the wrong body parts). *(i080)*
- The adult Prince's texture is fixed (it was a copy of the wrong skin). *(i002)*
- Instant Garden crops render as proper plants now (corn, quinoa, lettuce, tomato, radish, strawberry). *(i010)*
- The Crystal Furnace's progress arrow moves again (it always smelted fine — it just didn't show it). *(i005)*
- Kraken and Creeper repellents look like torches now, not full solid blocks. *(i006)*
- The chainsaw is no longer held sideways in first- and third-person. *(i008)*
- The WaterDragon no longer crashes on spawn — it can actually appear in your world now. *(TEST-005 / TF-001, TF-026)*
- Ruby and amethyst ores drop gems (and XP) when mined without Silk Touch. *(i013 / TF-017, TF-022)*
- The lava fishing bobber floats properly on the lava surface instead of sinking and drifting oddly. *(i085 / TF-028)*

---

## Faithful 1.7.10 quirks that may look like bugs

All of these reproduce the original 1.7.10 behavior **on purpose** — please
don't report them as bugs. Configurable modern behavior for each is on the
2.0 wishlist.

- Taming a baby Prince with a diamond block transforms it to a teen instantly — the tame maxes its growth in the original too. *(TF-024)*
- The Duplicator tree grows one block at a time and takes about 12 minutes to finish (longer if you sleep through nights or wander out of range) — that's the original pacing. *(MOD-015)*
- The chainsaw fells everything woody in an 11×16×11 box around the broken log — neighboring trees included, exactly like 1.7.10. *(MOD-016)*
- The Instant Garden digs in at **your feet**, not at the block you clicked — clicking uphill puts the plot one block lower. *(MOD-017)*
- Rocks **place** a pet rock when you click a block within reach (even into tight spaces); aim at open air to actually throw one. *(MOD-018)*
- Mole dirt sinks your feet and slows you down like soul sand — intended, original values. *(i004)*
- Experience armor never repairs itself and the Experience Sword never drains — the set quietly trickles XP instead (about 4 XP/min with the full set; invisible in creative mode). *(MOD-019)*
- The Cephadrome can't be permanently tamed — feed it raw beef, chicken, **or** porkchop to calm and heal it, then mount empty-handed; each ride needs a fresh meal. An earlier beta build had a porkchop "tame" that stuck — that was not in 1.7.10 and has been removed (any stuck tames reset on load). *(TF-032)*

---

## Optional non-source content (off by default)

The Vampire Butterfly, Apple Cow, and Golden Apple Cow appear on the classic
OreSpawn wiki but never existed in the 1.7.10 mod's code, so a source-faithful
build can't ship them enabled. They're still in the mod — set
`phase14ContentEnable = true` in the config to get their spawns and creative
spawn eggs back. That one line is enough unless you have set `modern.enabled =
false`, the master switch that forces every 2.0 feature (this content included)
to its classic/off value; it defaults to true and defers to the per-feature
keys. (The Enchanted Golden Apple Cow IS original content and is
always on.) *(MOD-021)*

---

## Open items

Known, on the radar, not yet resolved:

- ~~The Leonopteryx may look or animate oddly (stiff pose, smaller than it should be)~~ **Fixed in this build** — the Leonopteryx and Leon are now one creature under the hood (as in 1.7.10), rendered at the correct 1.75× size with full animation; the stiff interim pose and the double-drawn wing sets are gone. Existing saved Leons and Leonopteryxes both keep working. *(TF-030 — fixed 2026-08-11)*
- In the **Crystal dimension**, the big Fairy Castle Trees can generate with sheared-off flat edges where they cross a chunk boundary — the tree's arms simply stop mid-air. Roughly 1 in 25 Crystal chunks rolls a castle tree, and most of them clip at least one arm; the ordinary small fairy trees are fine (at worst a block or two on rare max-size ones), and every other Crystal structure is unaffected. When it happens, the game log notes a "Crystal structure write dropped" warning. **This is the designated first post-beta patch** — the fix (rebuilding the castle tree on the multi-chunk structure pipeline) is scoped and scheduled, it just doesn't block the beta. *(BUG-021 — deferred, owner-approved 2026-08-11)*
- ~~Kraken and Creeper repellents can only be placed on the floor for now; wall-mounting (which 1.7.10 supported) is a planned follow-up.~~ **Fixed in this build** — repellents now place on walls exactly like torches (vanilla torch/wall-torch split under the hood), pop off and drop themselves if the wall is removed, and keep their full repel behavior in either orientation. Existing floor-placed repellents are untouched. *(fixed 2026-08-11)*
- ~~The Extractor block is pending review — it never actually existed in 1.7.10, so it will either be removed or properly adopted as new content.~~ **Removed in this build** — it was a port invention with no 1.7.10 counterpart and its processing recipes were already gone; the design is archived (with the kyanite branch) for a possible 2.0 return. Player-placed Extractors will disappear from existing worlds on load. *(MOD-020 — ruling applied 2026-08-11, TF-031)*
- ~~Your **first** ant-teleport into a freshly generated dimension can bury you inside terrain~~ **Fixed in this build** — arrivals now land on the surface even on the very first visit (the destination terrain is generated before the landing spot is chosen). Please confirm on a fresh world. *(TEST-004 — fixed 2026-08-11, GameTest-covered)*

---

## Help us squash the rest

This beta lives on your feedback — bug reports and "is this supposed to look
like that?" questions are equally welcome. When you report something, please
include: the mod version, what you did (exact commands or steps help a lot),
what you expected versus what you saw, a screenshot or short clip for
anything visual, the log file for crashes (`latest.log` or the crash report),
and whether the world was fresh or upgraded. Side-by-side comparisons with
1.7.10 screenshots are gold — a lot of this beta's remaining work is exactly
that kind of visual verification, and you can settle an item for everyone
with one picture.

- **Fixed for the next release:** vanilla creepers in beta.2/beta.3
  carried invisible extra hitbox surfaces from a bundled-library demo
  file (BUG-036) — direct hits sometimes registered oddly (head hits
  did double damage, point-blank body shots could feel off). Purely
  server-side; no world data affected.

## The 2.0 robot overhaul (new in 2.0.0-beta.1)

**Tuning feedback welcome — beta players are the tuners now.** Two
feel items shipped as-built on purpose, and your reports decide their
final tune: (1) at sustained full sprint the spider's legs churn —
stepping faster and scrappier than a clean walk cycle, because the
body never slows down and inhibited steps convert to quick forced
lifts; it disappears at normal speeds. (2) The Robot Ant's step tempo
and re-step eagerness are a first-pass tune — if its six-legged walk
reads mincing or twitchy to you, say so. Neither affects damage,
mounting, or hover behavior.

- **Modern robot spiders** (the `spiderMovement` config, default MODERN — what a
  default config runs, together with the riding camera, because the `modern.enabled`
  master defaults to true and defers to the per-feature keys):
  legs are now real hittable surfaces dealing body-identical damage, the
  crosshair health bar works on legs (and now also on The King's giant
  body parts), and a mounted player can steer. Two things that are
  intended, not bugs: a mid-swing (airborne) leg is a moving target that
  tracks the server's swing — at high ping, lead it or hit the body,
  which pays the same; and legs dipping in lava or fire never hurt the
  spider — only its body touching a hazard does, exactly as in 1.0.
- **Classic is one config line away**: `spiderMovement = "CLASSIC"` gives the exact
  1.0/1.7.10 spider (visual-only legs, body-only hitbox, unsteerable), and
  `modern.enabled = false` is the one-line switch to the exact 1.7.10 experience for
  every 2.0 feature at once (robots, riding camera, Mothra's wider hitbox, the optional
  wiki mobs, fireball fire respecting `mobGriefing`, companions defending their owner, Mobzilla
  sparing its boss peers, the Pointysaurus stare, the Cryolophosaurus revenge chase). The master defaults to true and defers to the per-feature keys; it only
  forces classic when set false.
- **Boss fireballs respect `mobGriefing`** for the fire they place (MOD-031, new in 2.0, on by
  default through `[modern] fireRespectsMobGriefing`): while the rule is off there is no fire
  beside the block a fireball hits and no blast fire, the way ghast and blaze shots behave; damage,
  the ignite of whatever it hits and the explosion itself are unchanged. Set the key to `false`, or
  `modern.enabled = false`, for the 1.7.10 fire-always behaviour.
- **Smarter targeting, four keys** (MOD-032..035, new in 2.0, on by default under `[modern]`):
  `petsDefendOwner` — a tamed Leon, Prince Teen, Prince Adult, Boyfriend or Girlfriend avenges whoever hurts its
  owner and joins its owner's fights, and a tamed Leon that already has a target does not drop it for the nearest
  monster (the Gamma Metroid, Spyro, Stinky, the Prince, the Princess, the Hydrolisc and the Velocity Raptor carry the
  same goals but their own combat, where they have one, does not read them yet); `godzillaSparesBossPeers` — Mobzilla will
  not target the Nightmare, the Kraken, the King and Queen, the Prince line or the Purple Power;
  `pointysaurusStareAggro` — a survival player who looks straight at a Pointysaurus within 32 blocks
  is attacked; `cryolophosaurusRevengeChase` — a Cryolophosaurus chases whoever hurt it. Each key
  `false`, or `modern.enabled = false`, is the 1.7.10 behaviour (pets fight only what their own scan
  picks, Mobzilla hunts everything but its eight 1.7.10 exclusions, the Pointysaurus only reacts to
  proximity and hits, the Cryolophosaurus remembers its attacker but never chases). The three goal
  keys apply to newly spawned or loaded mobs, not live ones. Kept in BOTH modes on a safety ruling
  (MOD-036): on Valentine's Day the giant angry Girlfriend leaves Peaceful and creative players alone,
  where 1.7.10 hunted them.
- While a player is actively steering a modern spider, two vanilla
  rider-physics rules kick in that unridden (or NPC-ridden) spiders do
  not get: step height rises from 0.6 to a full block, and mid-air
  steering is stronger. This is the same treatment vanilla gives ridden
  horses and is kept deliberately — it is what makes stairs feel right
  from the saddle.
- **Mounting the spider**: the clickable body is now the full-size
  3.25×2.25 box of the 1.7.10 original — 2.0.0 restored it after
  port 1.0 shipped a shrunken, never-audited 2.0×1.5 box
  (ENT-S-088). An earlier version of this note claimed the small
  ground-level core was the hitbox "in 1.0 too" and that the big
  body "was never the hitbox" — that was true of port 1.0's small
  box, but NOT of 1.7.10, which always had the big box. Aim
  anywhere in the box between/under the visual body (it spans
  ground level to 2.25 high and 3.25 wide, so it is a much easier
  target than before; the visual body overhead can still sit partly
  above it). As a modern improvement, the LEGS are also clickable
  mount surfaces (a leg click mounts you, something classic's click
  geometry never offered).
- Robot ants only obey (heal, mount) their OWNER, exactly as in 1.0 —
  a wild or command-summoned ant ignores you unless summoned with
  `{AntRobotOwned:1}`.
- Robot **ants** get the same modern treatment (six real leg hitboxes,
  terrain-planting feet, same damage rules). The ant's hover-ride is
  untouched — and hovering means its legs can lose the ground: they
  fold into a dangle under the body while airborne and re-plant on
  landing. That dangling-flight look is the intended design, not a
  glitch.
