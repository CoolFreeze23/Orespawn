# Known Issues — OreSpawn Port (BETA)

**This release is a beta.** The game logic underneath has been through a
1422-test automated suite (all green, in both robot modes; the count grows
with every remediation batch) plus hands-on play sessions, but a lot
of the *visual and
audio* polish has deliberately been left open for community
feedback. You will find rough edges — and we want to hear about every one
of them.

---

## Known visual rough edges

None of these are confirmed broken — they simply haven't been hand-checked
against the 1.7.10 original yet. If something below looks or sounds wrong in
your game, that's exactly the report we need.

- Some mob animations and model scales have not been hand-verified against the 1.7.10 originals (idle wing flaps, Mothra's giant size, Nightmares growing with their hitbox) — screenshots welcome. *(i043, i074)* With `-Dorespawn.dev.classicRenderers=<species>` the same creature can be drawn through its classic renderer for a side-by-side (see "The GeckoLib rigs" below).
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

**2.0.0-beta.13 (this build):**

- Utopia, the Village, Crystal and Mining are built on the original's 1.7.10 terrain again (no mountains far out); Utopia's trees keep apart and off the Royal Altars, and Chaos has its trees. *(WGEN-087, WGEN-081 to WGEN-084)*
- Their water, surface and depth are the original's and their caves are made by its rules (still water below Y63, the original's surface, caves as common and as deep as its, bedrock at Y0, Mining's bare stone and blue sky), checked against a real 1.7.10 server; the Crystal Fairy Trees are as rare as the original's. *(WGEN-093, WGEN-092)*
- Land generated there by an earlier beta keeps everything, below Y0 and above Y256 too: the dimensions keep their height range, with the original's world built inside it. Their ores are the original's. *(WGEN-096, WGEN-095)*
- Their springs and lava lakes, OreSpawn's own ores in them, and Utopia's and Crystal's colours are the original's; Mining's trees, flowers, grass and falls are back. *(WGEN-099, WGEN-097, WGEN-101, WGEN-102)*
- The apple, cherry, peach, scary and experience leaves take the biome's colour; Mining's and the Village's structures spawn as often as the original's on the new terrain. *(BUG-047, WGEN-091)*
- New, a modern option: the Royal Altars fitted to the land round them (`[modern] altarTerrain`, default on). *(MOD-044)*
- The Crystal dimension's tree platforms are whole across chunk edges; the dungeons of Utopia, Mining and the Village too. *(BUG-021, WGEN-088)*
- The Apple Cow, the Spider Driver and the T-Shirt spawn; Utopia's, the Village's and Chaos's spawn lists are the original's; disabled mobs stay out of every list. *(ENT-S-190, WGEN-085, ENT-S-191)*
- The Apple Cow and the Golden Apple Cow have the original's names; the optional wiki cows are the Orchard Cow (Wiki) and the Golden Orchard Cow (Wiki). *(ENT-S-192)*
- The Kyuubi, the Gamma Metroid, the Princes' and the Princess's wings and the Ghosts are drawn see-through as in the original; the Kyuubi and the Triffid face the original's way. *(ENT-S-182 to ENT-S-188, ENT-S-162)*
- OreSpawn's swords, tools and armour take enchantments at the enchanting table and the anvil, each what its vanilla
  counterpart takes; the Ultimate Bow, the Skate Bow and the gadgets that wear out take Unbreaking and Mending.
  *(ITEM-073)*
- OreSpawn gear dropped by Godzilla, the Kraken, the Basilisk, the Cater Killer, the Cephadrome and the Trooper Bug
  rolls the original's enchantments with its chances and levels, and the gear that enchants itself checks for its own
  enchantments as the original did. The Emerald Pickaxe gives itself Silk Touch. *(ITEM-075, ITEM-076)*
- A creature's hit boxes keep up with it when it flies, jumps, burrows or turns, and when a rider gets on; a flying
  Prince can be hit where it is. *(the multipart creatures' hit boxes)*
- A Dragon, Baby Dragon, Prince, Frog, Cricket or worm with the NoAI tag no longer takes off, flies, jumps or burrows
  on its own. *(TEST-020)*
- A name-tagged Urchin stays in daylight; a tamed Girlfriend refuses horse and wolf armour. *(ENT-S-178, ENT-S-179)*

**2.0.0-beta.12:**

- Utopia's tall Wind and Sky trees, apple trees and vegetable patches no longer grow out of the base of a King or
  Queen tree, or of Utopia's big square, round and circular trees: as in 1.7.10, none of them grows in the chunk where
  a big tree is rooted, and where a tree from a neighbouring chunk runs into a big tree, the big tree wins. A tree
  rooted next door can still reach under a big tree's branches, as in the original. New chunks only. *(WGEN-075,
  GitHub #4)*
- Fences, iron bars, glass panes and walls in OreSpawn's structures join their neighbours, as they did in 1.7.10,
  instead of standing as separate posts. New chunks only: a structure already generated keeps its loose posts.
  *(WGEN-076, GitHub #5)*
- The Islands' challenge towers and its other big structures turn up about a third less often, as in 1.7.10: the
  original skipped its structure roll for 49 chunks after every build, which the port had left out. The King's and
  Queen's towers now share one placement, one tower at a time, instead of standing in pairs, and the other structures
  no longer bunch together. The Islands' ruby dungeon, missing until now, generates. New chunks only; a new structure
  at the edge of explored land can be cut off where it meets the old chunks. *(WGEN-078, GitHub #5)*
- Apple trees and the Wind and Sky trees no longer share a Utopia chunk, and the King and Queen altars stay out of a
  chunk that grew trees of its own, as in 1.7.10 (the chunk's rolls now run in the original's order). New chunks
  only. *(WGEN-077)*
- OreSpawn's structures in the overworld and in the Mining and Village dimensions turn up as often as in 1.7.10:
  most came a fifth to three quarters more often before, Mining's Leonopteryx nests about four times as often. King
  and Queen trees turn up about half again as often as before, as in 1.7.10. Mining's shadow dungeon, alien dungeon,
  bee hive and Leonopteryx nest, the Islands' greenhouse and White House, and Utopia's royal trees and altars take the
  sites the original's builders chose. The Village's villages stand in exactly the original's chunks. The small bee
  hives and mantis hives grow only in forests, birch forests and jungles, and `DisableOverworldDungeons` turns them
  off. New chunks only; a new structure at the edge of explored land can be cut off where it meets the old chunks.
  *(WGEN-079)*
- Each dimension's structures share one roll again, as in 1.7.10, so they spread out the way the original spread
  them: no clumps or rows, no more of them side by side than in the original, and two of the same kind can again turn
  up near each other. The King and Queen trees grow from Utopia's big-tree roll and the King and Queen altars come only
  to chunks that grow no trees, at the original's odds (about half as many altars as before). New chunks only.
  *(WGEN-080)*

**2.0.0-beta.11:**

- The option descriptions in `config/orespawn-common.toml` read in plain words (the fireball fire rule, the
  Chainsaw's sight check, the artist animations switch). Nothing plays differently; an existing config file picks up
  the new descriptions the first time the game loads it, and your settings stay as they are.

**2.0.0-beta.10:**

- The Girlfriend and the Boyfriend keep throwing shoes through a fight and swing their arms when they hit, throw
  or dance. After their first swing it never ended, which stopped every later throw and hid the arm swing; they
  tick their own swing as they did in 1.7.10. A hit right after a throw lands without a second arm swing, as every
  mob's swing works. *(ENT-S-175)*

**2.0.0-beta.9:**

- Every creature drawn through a rig has hit boxes that follow its model: several boxes along a long or winged
  creature's body, one box the size of a small one, 666 in all over 103 species, every one at full damage as the
  single old box was. Mothra's hand-placed boxes are gone (she takes full damage anywhere). A Crab or a Nightmare no
  longer starts inside a box of the wrong size, and a flame or tipped arrow that hits a boss's box now reaches the
  boss. Known limits: square-in-plan boxes, wings and fins padded rather than tilted, rest-pose boxes on a server
  with no player in range. *(ENT-S-173)*
- The boxes' cost is kept small: the server checks only the boxes of creatures near whatever it is testing, and the
  player who sends a creature's pose to the server sends it every second tick for ordinary creatures (the bosses
  every tick); the other players no longer send empty updates. *(ENT-S-174)*

**2.0.0-beta.8:**

- `/kill @e` kills every OreSpawn creature again, the bosses included and in the middle of a fight. Thirty species
  had capped, cooldown-gated, forwarded or refused the command's hit. *(ENT-S-172)*
- The King, the Kraken and Godzilla have hit boxes that follow their models (26, 25 and 16 bone-synced parts, like
  the Queen's); the hand-placed boxes and the floating head boxes are gone, and the 1.7.10 head creatures are no
  longer spawned (one in an old world disappears when it loads). The King and Godzilla take damage by part as the
  Queen does; the Kraken takes full damage anywhere. Known limits: square-in-plan boxes on long thin parts, the
  Kraken's tentacle boxes lagging up to three blocks mid-wave, solid parts, the Kraken's tall swim box, rest-pose
  parts on a server with no player in range. *(BOSS-047)*

**2.0.0-beta.7:**

- Creatures you leave behind despawn as they did in 1.7.10: frogs, crickets and T-shirts (Utopia no longer fills with
  every frog that ever spawned near you), wild grown Baryonyxes, Cassowaries, Flounders, Stink Bugs and Whales (their
  young stay, for good), wild adult Camarasauruses, Chipmunks and Water Dragons (tamed ones stay) and a wild, unridden
  Prince teen or adult — tame or name-tag the ones you mean to keep. In the other direction the worms, the Ant and Spider
  Robots, the hoverboard, the Purple Power and the Rock Base no longer vanish when you walk away; a Creeping Horror and a
  Firefly go only by day, a Gold Fish only by night. *(ENT-S-171)*

**2.0.0-beta.6:**

- Utopia's Sky trees and Wind trees no longer lose their branches at chunk borders — they generate as structures now,
  written chunk by chunk. The Sky trees stand at their original height again and the Wind trees all lean east, as in
  1.7.10. *(WGEN-072)*
- Utopia's giant square-trunked and round-trunked trees — the hollow towers with spiral steps, platforms, chests and
  Iron Golems on the branches — generate again, at the original's one-in-fifty-chunks rate; the platform tree's
  branches are the original's leaf-rimmed discs. *(WGEN-073, WGEN-074)*
- Frogs no longer overrun Utopia: a frog drawn from a water list needs two-deep water, as in 1.7.10. *(ENT-S-170)*

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

## Known limits of the new features (2.0.0-beta.13)

- **The Royal Altars' fitted ground:** a hillside more than 16 blocks over the pad keeps a lower wall at the ring's edge; ground more than 59 over the pad stays as an overhang; water or lava standing against a cut (a spring, a pond beyond the ring) runs into it when a block next to it changes; a tree standing on a cut keeps its column of ground.
- **Chance in the original's dimensions:** in Utopia, the Village and Mining the caves and ravines are made by the original's rules (as many, as deep, lava near the bottom) but fall in other places than the original's, and trees, plants, ponds and other things placed by chance land elsewhere too. Where one of them crosses a chunk edge, which lands last follows the order the game generates the chunks in, so two worlds of one seed can differ slightly there. *(WGEN-100, WGEN-098)*
- **Trees in the original's dimensions:** Mining has more trees than the original (41 against 25 in the 400 x 400 blocks round the spawn), since 1.21's trees grow in spots where 1.7.10's needed more room; Utopia has about a fifth less tree cover than the original (0.77 of it round the spawn, 0.82 over four places out to 8,000 blocks east), its cause not yet found. *(WGEN-102, WGEN-108)*
- **The Village's water:** its 1.21 jigsaw villages reshape the ground round them, so there is less water in and round them than in the original. *(WGEN-094)*
- **The 1.7.10 terrain in existing worlds:** Utopia, the Village, Crystal and Mining generate new land with the original's terrain; where it meets land generated by beta.12 or earlier there is a visible step or cliff. For the original terrain everywhere in one of them, take out anything worth keeping, close the world and delete that dimension's folder under the world's `dimensions/orespawn/` (`utopia`, `village`, `crystal` or `mining`). *(WGEN-087)*
- **The modern armour style:** mobs whose armour GeckoLib draws (Iron's Spells' wizards, for one) wear the classic look
  in both styles. On a zombie villager the leggings' details show through the robe. A piece whose geometry fails to
  load (a broken resource pack) shows the classic model and texture but keeps its modern icon. On a mob whose armour
  model has boxes of another size than the player's, the pieces fit as on the player.
- **Horse armour:** the texture leaves the belly, the ears and the mane bare, as vanilla's horse armour does. A change
  of `[modern] horseArmour` reaches the recipes and the King's drop on `/reload` or a restart, the creative tab when
  the world is joined again.
- **Wolf armour:** Doggy Talents Next dogs can't wear it (they take only vanilla's); a training treat on a wolf
  wearing one drops the armour first, and the next treat trains it. A summoned wolf (Ars Nouveau's) never takes it.
- **The dogs' modern look:** Doggy Talents Next keeps each piece's texture for the session, so a change of style or of
  `[modern] dogArmour` reaches dogs after a restart; its legacy armour render draws its own textures; a heavy helmet's
  closed visor covers a dog's face.
- **Spears:** they need Mounts of Mayhem (tested with 1.9.8) on the server and every client, and a world keeps its
  OreSpawn spears only while it is installed. They have no left-click jab; the mod's Lunge enchantment does not go on
  them.

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

The Vampire Butterfly and two cows from the classic OreSpawn wiki, shown in game
as the Orchard Cow (Wiki) and the Golden Orchard Cow (Wiki), never existed in the
1.7.10 mod's code, so a source-faithful build can't ship them enabled. They're
still in the mod — set `phase14ContentEnable = true` in the config to get their
spawns and creative spawn eggs back. That one line is enough unless you have set
`modern.enabled = false`, the master switch that forces every 2.0 feature (this
content included) to its classic/off value; it defaults to true and defers to the
per-feature keys. (The original's own cows, the Apple Cow, the Golden Apple Cow
and the Enchanted Golden Apple Cow, are always on.) *(MOD-021)*

---

## Open items

Known, on the radar, not yet resolved:

- ~~The Leonopteryx may look or animate oddly (stiff pose, smaller than it should be)~~ **Fixed in this build** — the Leonopteryx and Leon are now one creature under the hood (as in 1.7.10), rendered at the correct 1.75× size with full animation; the stiff interim pose and the double-drawn wing sets are gone. Existing saved Leons and Leonopteryxes both keep working. *(TF-030 — fixed 2026-08-11)*
- In the **Crystal dimension**, the big Fairy Castle Trees can generate with sheared-off flat edges where they cross a chunk boundary — the tree's arms simply stop mid-air. Roughly 1 in 25 Crystal chunks rolls a castle tree, and most of them clip at least one arm; the ordinary small fairy trees are fine (at worst a block or two on rare max-size ones), and every other Crystal structure is unaffected. When it happens, the game log notes a "Crystal structure write dropped" warning. **This is the designated first post-beta patch** — the fix (rebuilding the castle tree on the multi-chunk structure pipeline) is scoped and scheduled, it just doesn't block the beta. The Utopia trees received exactly this fix on 2026-09-20 (WGEN-072); the castle tree is next. *(BUG-021 — deferred 2026-08-11)*
- ~~Kraken and Creeper repellents can only be placed on the floor for now; wall-mounting (which 1.7.10 supported) is a planned follow-up.~~ **Fixed in this build** — repellents now place on walls exactly like torches (vanilla torch/wall-torch split under the hood), pop off and drop themselves if the wall is removed, and keep their full repel behavior in either orientation. Existing floor-placed repellents are untouched. *(fixed 2026-08-11)*
- ~~The Extractor block is pending review — it never actually existed in 1.7.10, so it will either be removed or properly adopted as new content.~~ **Removed in this build** — it was a port invention with no 1.7.10 counterpart and its processing recipes were already gone; the design is archived (with the kyanite branch) for a possible 2.0 return. Player-placed Extractors will disappear from existing worlds on load. *(MOD-020 — applied 2026-08-11, TF-031)*
- ~~Your **first** ant-teleport into a freshly generated dimension can bury you inside terrain~~ **Fixed in this build** — arrivals now land on the surface even on the very first visit (the destination terrain is generated before the landing spot is chosen). Please confirm on a fresh world. *(TEST-004 — fixed 2026-08-11, GameTest-covered)*

---

## The GeckoLib rigs — now the default renderers (new in 2.0.0-beta.5)

Every creature but the two solver robots (the Ant Robot and the Spider Robot) now draws through a GeckoLib rig converted from
its classic model and proven against it bone for bone and pixel for pixel; the poses are the classic motion, bit for bit, so
nothing should look different from beta.4. If a species does look different — a limb on the other side, a part missing, a
texture facing the wrong way — put it back on its classic renderer with one JVM argument and tell us:
`-Dorespawn.dev.classicRenderers=<registry names, comma-separated>` (or `classic` for every species; the start-up log names each species kept
classic). That side-by-side screenshot, the species and the argument are exactly the report we need. The Queen keeps her
hand-made rig either way.

- **The four butterflies came last.** Butterfly, Luna Moth, Mothra and Vampire Butterfly share one rig; the Mothra's flat-wing
  pose ties 1.2 % of the image between two overlapping wing slabs, and the rig landed under a declared, pinned allowance for
  exactly that sample (the comparison's default rule of 1 % stands for everyone else). *(TEST-019, landed 2026-09-19)*
- **The mirror correction, accepted.** Every converted rig had been written mirrored left for right, in a frame where the
  comparison could not see it; the converter now writes rigs in the same convention as the Queen's hand-made rig, everything
  was regenerated, the comparison reproduces the real in-game render chains, and an in-game look at the Ender Knight's
  one sword (the same hand, the same height, through both renderers) accepted it on 2026-09-19. For a comparison of your own,
  `-Dorespawn.dev.classicRenderers=ender_knight` puts the knight back on its classic renderer. *(TEST-015, closed 2026-09-19)*
- **Three classic-renderer quirks the GeckoLib rigs copy on purpose** (a converted rig draws exactly what the port's classic
  renderer draws; these are divergences of the classic renderer from 1.7.10, recorded for a later parity pass,
  not fixed here): the Triffid stands a quarter-turn from where 1.7.10 turned it *(ENT-S-162)*; Godzilla bites and
  swings its arms on every cycle where 1.7.10 did so on about half of them, re-rolled per cycle *(ENT-S-163)*; the Prince
  Teen's wing membranes are opaque where 1.7.10 drew them translucent grey *(ENT-S-164)*.
- **Boss hitboxes follow the rigs** (BOSS-047): the King, the Kraken and Godzilla now carry bone-synced hitbox parts
  fitted to their drawn models (26, 25 and 16 parts), like the Queen; the old floating head boxes are gone, and every
  hit goes through a part (heads full damage, body and limbs half, wings and tail a quarter on the King and Godzilla;
  the Kraken full everywhere, as in 1.7.10). The Princess keeps her classic single hitbox. Known limits: the boxes are
  square in plan, so long thin parts (necks, tentacles, wing membranes) read wider than they look; the Kraken's
  tentacle boxes can lag the drawn tentacle by up to three blocks at the extremes of its wave; the parts are solid
  (you can stand on Godzilla's tail); the Kraken's tall 1.7.10 swim box is still its collision box; on a server with
  no player in render range the parts sit at their rest positions. Tell us what looks wrong.
- **Every creature's hit boxes follow its model** (ENT-S-173): the 103 species drawn through a rig now carry
  bone-synced hitbox parts fitted to the drawn model (one box for a small creature, up to 26 for the adult Prince; 666
  in all), like the bosses; every part takes full damage, as the single 1.7.10 box did, and the old box stays the
  movement and collision box (a baby's half, the Crab's growth). Mothra's four hand-placed boxes are gone with them.
  Known limits, the bosses' too: the boxes are square in plan, so long thin parts read wider than they look; a wing or a
  fin is padded for the flap rather than tilting with it; on a server with no player in render range the parts sit at
  their rest pose (on the model and turned with it, but not animated); the player the server picks for a creature
  sends its bone positions every second tick (the bosses every tick). *(ENT-S-173, ENT-S-174)* The groupings
  were made from the rigs, not judged in play — tell us which creature's boxes look wrong and we will hand-edit its spec.

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
  keys apply to newly spawned or loaded mobs, not live ones. Kept in BOTH modes for safety
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
