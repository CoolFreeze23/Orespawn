#!/usr/bin/env python3
"""Static asset/renderer completeness auditor for the OreSpawn NeoForge 1.21.1 port.

Replaces human eyeballing of ~600 registered items / 200+ blocks / 140+ entities.
Pure static analysis: parses the Java registry classes with regexes and walks the
JSON asset tree. No Minecraft runtime, stdlib only.

Checks
  1. ITEM MODELS      every registered item has models/item/<name>.json, it parses,
                      and every texture (following mod-namespace parent chains)
                      resolves to a real .png. Vanilla (minecraft:) refs assumed OK.
  2. BLOCKSTATES      every registered block has blockstates/<name>.json, it parses,
                      every referenced model exists, and those models' textures resolve.
  3. ENTITY RENDERERS every ENTITY_TYPES.register(...) name has a renderer
                      registration in client code (the invisible-entity class).
  4. HANDHELD/DISPLAY heuristics for tool-named items using item/generated or
                      lacking display transforms (ADVISORY).
  5. GUI TEXTURES     every menu type has a screen; every orespawn ResourceLocation
                      texture referenced from Java exists; legacy >=176 U-coordinate
                      blits from vanilla container textures flagged (the
                      crystal-furnace-missing-progress-arrow class, ADVISORY).
  6. SOUNDS           every sounds.json entry references .ogg files that exist;
                      registered SoundEvents missing from sounds.json (ADVISORY).
  7. INDEX CASE       every resource file is tracked by git under a lowercase,
                      ResourceLocation-valid path, and every Java asset literal
                      matches its git-tracked name exactly. Reads the git INDEX,
                      not the disk: a case-insensitive checkout hides uppercase
                      tracked names that break every other clone (BUG-038).

Findings whose (category, name) pair is listed in ACKNOWLEDGED below are
reported under a separate ACKNOWLEDGED section and never affect the exit code
(the verified-faithful / known-false-positive whitelist).

Exit code 1 if any non-acknowledged ERROR-level findings, else 0.
--json (alias: --write) refreshes tools/asset_audit_report.json.
"""

import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src" / "main" / "java" / "danger" / "orespawn"
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "orespawn"
MOD_ID = "orespawn"

TOOL_KEYWORDS = ("sword", "pickaxe", "axe", "shovel", "hoe", "chainsaw", "saw",
                 "rod", "staff", "bow", "hammer", "claw")
HANDHELD_PARENTS = {"item/handheld", "item/handheld_rod", "item/handheld_mace"}
FULL_CUBE_PARENTS = {
    "block/cube_all", "block/cube", "block/cube_bottom_top", "block/cube_column",
    "block/cube_column_horizontal", "block/cube_directional", "block/cube_top",
    "block/cube_mirrored_all", "block/orientable", "block/orientable_with_bottom",
}
# Blocks whose original shape is torch-like / cake-like; a full-cube model is suspect.
NON_CUBE_NAME_HINTS = ("torch", "repellent", "pizza", "cake")

# --------------------------------------------------------------------------
# Acknowledged findings
# --------------------------------------------------------------------------
# (category, name) pairs — exactly as emitted by add() — that were verified
# against the 1.7.10 ground truth and signed off. They print under an
# ACKNOWLEDGED section and never affect the exit code, so the audit gates on
# genuinely new findings only. Every entry MUST carry a justification comment.
ACKNOWLEDGED = {
    # Faithfully invisible: orig RenderItemUrchin.java:21-23 draws nothing when
    # the entity is a BerthaHit, so the port's deliberate NoopProjectileRenderer
    # reproduces the original exactly.
    ("ENTITY_NOOP_RENDERER", "bertha_hit"),
    # Thrown-projectile flat icon is faithful: orig ItemShoes/RenderShoe render
    # a flat static sprite, so item/generated with no handheld transforms is
    # correct. The tool-keyword hit is the "hoe" substring inside "shoes".
    ("TOOL_GENERATED_PARENT", "boots_shoes"),
    ("TOOL_NO_TRANSFORMS", "boots_shoes"),
    # Keyword false-positives: "hammer" inside "hammerhead" and "bow" inside
    # "rainbow" — both are plain cube spawn-block block-items, not tools.
    ("TOOL_NO_TRANSFORMS", "hammerhead_spawn_block"),
    ("TOOL_NO_TRANSFORMS", "rainbow_ant_block"),
}

findings = []      # list of dicts: level, category, name, detail, path
skipped = []       # things the static parser could not verify


def rel(p):
    try:
        return str(Path(p).relative_to(ROOT)).replace("\\", "/")
    except (ValueError, TypeError):
        return str(p)


def add(level, category, name, detail, path=""):
    findings.append({"level": level, "category": category, "name": name,
                     "detail": detail, "path": rel(path) if path else ""})


def err(category, name, detail, path=""):
    add("ERROR", category, name, detail, path)


def adv(category, name, detail, path=""):
    add("ADVISORY", category, name, detail, path)


def skip(what, detail):
    skipped.append({"what": what, "detail": detail})


def read(path):
    return Path(path).read_text(encoding="utf-8", errors="replace")


def load_json(path):
    """Returns (data, error_string)."""
    try:
        return json.loads(read(path)), None
    except (OSError, json.JSONDecodeError) as e:
        return None, str(e)


def split_ref(ref):
    """'orespawn:block/x' -> ('orespawn','block/x'); 'item/generated' -> ('minecraft','item/generated')."""
    if ":" in ref:
        ns, _, path = ref.partition(":")
        return ns, path
    return "minecraft", ref


# --------------------------------------------------------------------------
# Java registry parsing
# --------------------------------------------------------------------------

def detect_unparsed(text, register_call_re, file_label):
    """Flag register(...) calls whose first arg is not a string literal (dynamic names)."""
    for m in re.finditer(register_call_re, text):
        first = text[m.end():m.end() + 60].lstrip()
        if first.startswith('"'):
            continue
        if first.startswith(("eventBus", "modEventBus", "bus")):
            continue  # the DeferredRegister.register(IEventBus) hookup call
        skip(file_label, "non-literal registration name near: "
             + text[m.start():m.start() + 80].replace("\n", " "))


def parse_items():
    """Returns (all_item_names, block_item_names, spawn_egg_names)."""
    path = JAVA / "ModItems.java"
    text = read(path)
    items, block_items, spawn_eggs = [], set(), set()

    for m in re.finditer(r'ITEMS\.registerSimpleBlockItem\(\s*"([^"]+)"', text):
        items.append(m.group(1))
        block_items.add(m.group(1))
    for m in re.finditer(r'ITEMS\.registerSimpleItem\(\s*"([^"]+)"', text):
        items.append(m.group(1))
    for m in re.finditer(r'ITEMS\.register\(\s*"([^"]+)"', text):
        name = m.group(1)
        items.append(name)
        body = text[m.end():m.end() + 400]
        if "BlockItem(" in body:
            block_items.add(name)
        if "SpawnEggItem" in body:
            spawn_eggs.add(name)

    detect_unparsed(text, r'ITEMS\.register(?:SimpleBlockItem|SimpleItem)?\(', "ModItems.java")
    return items, block_items, spawn_eggs


def parse_blocks():
    path = JAVA / "ModBlocks.java"
    text = read(path)
    blocks = [m.group(1) for m in re.finditer(r'BLOCKS\.register\(\s*"([^"]+)"', text)]
    detect_unparsed(text, r'BLOCKS\.register\(', "ModBlocks.java")
    return blocks


def parse_entities():
    """Returns {CONST_NAME: (registry_name, mob_category)}."""
    path = JAVA / "ModEntities.java"
    text = read(path)
    entities = {}
    # (?://...\n)* skips javadoc-style // comment lines between '=' and the call
    for m in re.finditer(r'(\w+)\s*=\s*(?://[^\n]*\n\s*)*ENTITY_TYPES\.register\(\s*"([^"]+)"', text):
        const, name = m.group(1), m.group(2)
        tail = text[m.end():m.end() + 600]
        cat = re.search(r'MobCategory\.(\w+)', tail)
        entities[const] = (name, cat.group(1) if cat else "?")
    detect_unparsed(text, r'ENTITY_TYPES\.register\(', "ModEntities.java")
    return entities


def parse_menus():
    text = read(JAVA / "ModMenuTypes.java")
    menus = {m.group(1): m.group(2) for m in
             re.finditer(r'(\w+)\s*=\s*\n?\s*MENUS\.register\(\s*"([^"]+)"', text)}
    detect_unparsed(text, r'MENUS\.register\(', "ModMenuTypes.java")
    return menus


def parse_sound_events():
    """Registered SoundEvent ids from ModSounds.java (registration name and, when
    present, the explicit ResourceLocation id inside the supplier)."""
    text = read(JAVA / "ModSounds.java")
    ids = []
    for m in re.finditer(r'SOUND_EVENTS\.register\(\s*"([^"]+)"', text):
        reg_name = m.group(1)
        body = text[m.end():m.end() + 300]
        rl = re.search(r'fromNamespaceAndPath\(\s*[^,]+,\s*"([^"]+)"', body)
        ids.append(rl.group(1) if rl else reg_name)
    return ids


def all_java_files():
    return sorted(JAVA.rglob("*.java"))


# --------------------------------------------------------------------------
# Model chain resolution
# --------------------------------------------------------------------------

_model_cache = {}


def model_json_path(ref_path):
    return ASSETS / "models" / (ref_path + ".json")


def resolve_chain(ref, _seen=None):
    """Resolve a mod-namespace model reference and its parent chain.

    Returns dict:
      textures        merged texture map (child overrides parent)
      has_display     any model in the mod chain defines "display"
      display_keys    union of display slot names defined in the mod chain
      terminal        the first non-mod parent ref ('item/generated', 'builtin/entity', ...)
                      or None if the chain ends without a parent
      overrides       model refs from "overrides" of the leaf model
      errors          list of (category, detail, path) accumulated while walking
    """
    if ref in _model_cache:
        return _model_cache[ref]
    if _seen is None:
        _seen = set()
    result = {"textures": {}, "has_display": False, "display_keys": set(),
              "terminal": None, "overrides": [], "errors": []}
    ns, path = split_ref(ref)
    if ns != MOD_ID:
        result["terminal"] = path
        return result
    if ref in _seen:
        result["errors"].append(("MODEL_CYCLE", "parent chain cycle at " + ref, ""))
        return result
    _seen.add(ref)

    file = model_json_path(path)
    if not file.is_file():
        result["errors"].append(("MODEL_MISSING", "model file does not exist: " + ref, file))
        _model_cache[ref] = result
        return result
    data, jerr = load_json(file)
    if jerr:
        result["errors"].append(("MODEL_PARSE", "JSON parse error: " + jerr, file))
        _model_cache[ref] = result
        return result
    if not isinstance(data, dict):
        result["errors"].append(("MODEL_PARSE", "model JSON is not an object", file))
        _model_cache[ref] = result
        return result

    parent = data.get("parent")
    if isinstance(parent, str):
        pns, ppath = split_ref(parent)
        if pns == MOD_ID:
            up = resolve_chain(parent, _seen)
            result["textures"].update(up["textures"])
            result["has_display"] = up["has_display"]
            result["display_keys"] = set(up["display_keys"])
            result["terminal"] = up["terminal"]
            result["errors"].extend(up["errors"])
        else:
            result["terminal"] = ppath  # vanilla / builtin parent: assumed OK

    tex = data.get("textures")
    if isinstance(tex, dict):
        result["textures"].update({k: v for k, v in tex.items() if isinstance(v, str)})
    disp = data.get("display")
    if isinstance(disp, dict):
        result["has_display"] = True
        result["display_keys"] |= set(disp.keys())
    for ov in data.get("overrides", []) if isinstance(data.get("overrides"), list) else []:
        if isinstance(ov, dict) and isinstance(ov.get("model"), str):
            result["overrides"].append(ov["model"])

    _model_cache[ref] = result
    return result


def check_model_textures(ref, owner, category_prefix, blockstate_path=""):
    """Walk a top-level model ref's merged textures; report missing pngs / unresolved vars."""
    chain = resolve_chain(ref)
    for cat, detail, path in chain["errors"]:
        err(category_prefix + "_" + ("PARSE" if "PARSE" in cat else
            "CYCLE" if "CYCLE" in cat else "MODEL_MISSING"),
            owner, detail, path or blockstate_path)
    merged = chain["textures"]
    for slot, value in merged.items():
        v, hops = value, set()
        while v.startswith("#"):
            var = v[1:]
            if var in hops or var not in merged:
                break
            hops.add(var)
            v = merged[var]
        if v.startswith("#"):
            adv("TEXTURE_VAR_UNRESOLVED", owner,
                'model %s texture slot "%s" resolves to unbound variable "%s"' % (ref, slot, v),
                model_json_path(split_ref(ref)[1]))
            continue
        ns, tpath = split_ref(v)
        if ns != MOD_ID:
            continue  # vanilla texture assumed OK
        png = ASSETS / "textures" / (tpath + ".png")
        if not png.is_file():
            err(category_prefix + "_TEXTURE_MISSING", owner,
                'model %s slot "%s" references missing texture %s:%s' % (ref, slot, ns, tpath),
                png)
    # bow-style overrides: referenced models must exist too
    for oref in chain["overrides"]:
        ons, opath = split_ref(oref)
        if ons == MOD_ID and not model_json_path(opath).is_file():
            err(category_prefix + "_MODEL_MISSING", owner,
                "override model does not exist: " + oref, model_json_path(opath))
    return chain


# --------------------------------------------------------------------------
# Check 1 + 4: item models, handheld/display heuristics
# --------------------------------------------------------------------------

def check_items(items, block_items, spawn_eggs, block_terminal_parents):
    for name in items:
        model_file = ASSETS / "models" / "item" / (name + ".json")
        if not model_file.is_file():
            err("ITEM_MODEL_MISSING", name, "no item model for registered item", model_file)
            continue
        data, jerr = load_json(model_file)
        if jerr:
            err("ITEM_MODEL_PARSE", name, "JSON parse error: " + jerr, model_file)
            continue
        ref = "%s:item/%s" % (MOD_ID, name)
        chain = check_model_textures(ref, name, "ITEM")

        terminal = chain["terminal"] or ""
        # 4a: tool-named item on item/generated
        is_tool = any(k in name for k in TOOL_KEYWORDS) and name not in spawn_eggs
        if is_tool and terminal == "item/generated":
            adv("TOOL_GENERATED_PARENT", name,
                "tool/weapon-named item uses item/generated; likely needs "
                "item/handheld or explicit display transforms", model_file)
        # 4b: tool-named item with no display transforms anywhere in mod chain
        if is_tool and not chain["has_display"] and terminal not in HANDHELD_PARENTS:
            adv("TOOL_NO_TRANSFORMS", name,
                'tool/weapon-named item model has no "display" key and no handheld '
                "parent (terminal parent: %s)" % (terminal or "none"), model_file)
        # 4c: builtin/entity (BEWLR) models missing standard display slots ->
        #     held-sideways / giant-ground-item class
        if terminal == "builtin/entity":
            expected = {"gui", "ground", "fixed", "thirdperson_righthand", "firstperson_righthand"}
            missing = sorted(expected - chain["display_keys"])
            if missing:
                adv("BEWLR_DISPLAY_INCOMPLETE", name,
                    "builtin/entity item model missing display transforms: "
                    + ", ".join(missing), model_file)
        # pizza-class: 3D cube block whose item renders as a flat 16x16 sprite
        if name in block_items and terminal == "item/generated":
            bterm = block_terminal_parents.get(name, set())
            if bterm & FULL_CUBE_PARENTS:
                adv("BLOCKITEM_FLAT_SPRITE", name,
                    "block item uses item/generated (flat sprite) while its block "
                    "model is a full cube (%s); original likely rendered as a 3D "
                    "block/cake shape" % ", ".join(sorted(bterm & FULL_CUBE_PARENTS)),
                    model_file)


# --------------------------------------------------------------------------
# Check 2: blockstates
# --------------------------------------------------------------------------

def blockstate_model_refs(data):
    refs = []
    variants = data.get("variants")
    if isinstance(variants, dict):
        for v in variants.values():
            entries = v if isinstance(v, list) else [v]
            for e in entries:
                if isinstance(e, dict) and isinstance(e.get("model"), str):
                    refs.append(e["model"])
    multipart = data.get("multipart")
    if isinstance(multipart, list):
        for part in multipart:
            if not isinstance(part, dict):
                continue
            apply_ = part.get("apply")
            entries = apply_ if isinstance(apply_, list) else [apply_]
            for e in entries:
                if isinstance(e, dict) and isinstance(e.get("model"), str):
                    refs.append(e["model"])
    return refs


def check_blocks(blocks):
    """Returns {block_name: set(terminal_parents_of_its_models)} for later heuristics."""
    terminal_parents = {}
    for name in blocks:
        bs_file = ASSETS / "blockstates" / (name + ".json")
        if not bs_file.is_file():
            err("BLOCKSTATE_MISSING", name, "no blockstate for registered block", bs_file)
            continue
        data, jerr = load_json(bs_file)
        if jerr:
            err("BLOCKSTATE_PARSE", name, "JSON parse error: " + jerr, bs_file)
            continue
        refs = blockstate_model_refs(data)
        if not refs:
            err("BLOCKSTATE_NO_MODELS", name,
                "blockstate parses but references no models", bs_file)
            continue
        terms = set()
        for ref in dict.fromkeys(refs):  # dedupe, keep order
            ns, path = split_ref(ref)
            if ns != MOD_ID:
                continue  # vanilla model assumed OK
            if not model_json_path(path).is_file():
                err("BLOCK_MODEL_MISSING", name,
                    "blockstate references missing model " + ref, model_json_path(path))
                continue
            chain = check_model_textures(ref, name, "BLOCK", bs_file)
            if chain["terminal"]:
                terms.add(chain["terminal"])
        terminal_parents[name] = terms
        # torch/repellent/pizza-class: name says non-cube shape, model is a full cube
        if any(h in name for h in NON_CUBE_NAME_HINTS) and terms & FULL_CUBE_PARENTS:
            adv("SHAPE_SUSPECT_FULL_CUBE", name,
                "block name suggests a torch/cake-style shape but its model chain "
                "terminates in a full-cube template (%s) - renders as a full block"
                % ", ".join(sorted(terms & FULL_CUBE_PARENTS)), bs_file)
    return terminal_parents


# --------------------------------------------------------------------------
# Check 3: entity renderer registrations (+ texture-ref sweep)
# --------------------------------------------------------------------------

NOOP_RENDERER_RE = re.compile(r'(?i)noop|no_?op|empty|invisible|dummy|blank')


def check_entity_renderers(entities, java_texts):
    registered = set()
    renderer_class = {}  # CONST -> renderer class name (when statically visible)
    for path, text in java_texts:
        for m in re.finditer(
                r'registerEntityRenderer\(\s*ModEntities\.(\w+)\s*\.get\(\)\s*,\s*([\w.]+)::new',
                text):
            registered.add(m.group(1))
            renderer_class[m.group(1)] = (m.group(2), path)
        for m in re.finditer(r'registerEntityRenderer\(\s*ModEntities\.(\w+)\s*\.get\(\)', text):
            registered.add(m.group(1))
        for m in re.finditer(r'EntityRenderers\.register\(\s*ModEntities\.(\w+)', text):
            registered.add(m.group(1))
    for const, (name, cat) in sorted(entities.items(), key=lambda kv: kv[1][0]):
        if const not in registered:
            hint = " [MobCategory.MISC - projectile/technical entity: renders INVISIBLE]" \
                if cat == "MISC" else ""
            err("ENTITY_NO_RENDERER", name,
                "ModEntities.%s (MobCategory.%s) has no renderer registration in client code%s"
                % (const, cat, hint), JAVA / "ModEntities.java")
        elif const in renderer_class and NOOP_RENDERER_RE.search(renderer_class[const][0]):
            cls, path = renderer_class[const]
            adv("ENTITY_NOOP_RENDERER", name,
                "ModEntities.%s (MobCategory.%s) is bound to %s - a deliberate "
                "do-nothing renderer, so the entity is INVISIBLE in-game (the "
                "thrown-rock/shoe/ball projectile class)" % (const, cat, cls), path)


def check_texture_refs(java_texts):
    """Every literal orespawn ResourceLocation asset path in Java must exist on disk."""
    lit = re.compile(r'fromNamespaceAndPath\(\s*(?:OreSpawnMod\.MOD_ID|"orespawn")\s*,\s*'
                     r'"((?:textures|geo|animations)/[^"]+)"\s*\)')
    concat = re.compile(r'fromNamespaceAndPath\(\s*(?:OreSpawnMod\.MOD_ID|"orespawn")\s*,\s*'
                        r'"((?:textures|geo|animations)/[^"]+)"\s*\+')
    for path, text in java_texts:
        is_screen = "AbstractContainerScreen" in text
        for m in lit.finditer(text):
            asset = m.group(1)
            if not (ASSETS / asset).is_file():
                cat = "GUI_TEXTURE_MISSING" if asset.startswith("textures/gui/") \
                    else "TEXTURE_REF_MISSING"
                err(cat, Path(path).stem,
                    "Java references missing asset orespawn:%s" % asset, path)
        for m in concat.finditer(text):
            prefix = m.group(1)
            base = ASSETS / prefix
            matches = list(base.parent.glob(base.name + "*")) if base.parent.is_dir() else []
            skip(rel(path), 'concatenated asset path "%s" + <expr> - cannot fully verify '
                 "statically (%d file(s) match prefix on disk)" % (prefix, len(matches)))
            if not matches:
                err("TEXTURE_REF_MISSING", Path(path).stem,
                    'no files on disk match concatenated asset prefix "%s*"' % prefix, path)
        # crystal-furnace class: blitting sub-sprites from a vanilla container
        # texture at U>=176. Since 1.20.2 the progress/flame sprites moved to
        # textures/gui/sprites/**, so the >=176 region of container textures is
        # empty -> the widget silently renders nothing.
        if is_screen and 'withDefaultNamespace("textures/gui/container/' in text:
            if re.search(r'(?:_U|_V|TEXTURE_U|TEXTURE_V)\s*=\s*(1[7-9]\d|2\d\d)\b', text) or \
               re.search(r'blit\([^;]*,\s*(1[7-9]\d|2\d\d)\s*,', text, re.S):
                adv("GUI_LEGACY_ATLAS_BLIT", Path(path).stem,
                    "screen blits sub-sprites (arrow/flame) from a vanilla container "
                    "texture at U>=176; in 1.21.1 those sprites live in "
                    "textures/gui/sprites/** and this region is empty - progress "
                    "indicators render invisible", path)


# --------------------------------------------------------------------------
# Check 5 extra: menu screens
# --------------------------------------------------------------------------

def check_menu_screens(menus, java_texts):
    screen_regs = set()
    for _, text in java_texts:
        for m in re.finditer(r'\bevent\.register\(\s*ModMenuTypes\.(\w+)\s*\.get\(\)', text):
            screen_regs.add(m.group(1))
        for m in re.finditer(r'MenuScreens\.register\(\s*ModMenuTypes\.(\w+)', text):
            screen_regs.add(m.group(1))
    for const, name in menus.items():
        if const not in screen_regs:
            err("MENU_NO_SCREEN", name,
                "MenuType ModMenuTypes.%s has no screen registration in client code" % const,
                JAVA / "ModMenuTypes.java")


# --------------------------------------------------------------------------
# Additional standalone models (ModelEvent.RegisterAdditional)
# --------------------------------------------------------------------------

def check_additional_models(java_texts):
    for path, text in java_texts:
        if "ModelEvent.RegisterAdditional" not in text:
            continue
        arrays = re.findall(r'String\[\]\s+\w+\s*=\s*\{([^}]*)\}', text)
        templates = re.findall(r'"([\w./]+/)"\s*\+\s*\w+\s*\+\s*"([\w.]*)"', text)
        if not arrays or not templates:
            skip(rel(path), "ModelEvent.RegisterAdditional present but the name "
                 "array / path template could not be parsed statically")
            continue
        names = []
        for arr in arrays:
            names += re.findall(r'"([^"]+)"', arr)
        for prefix, suffix in templates:
            for n in names:
                mref = "%s:%s%s%s" % (MOD_ID, prefix, n, suffix)
                _, mpath = split_ref(mref)
                if not model_json_path(mpath).is_file():
                    err("ADDITIONAL_MODEL_MISSING", n,
                        "RegisterAdditional references missing model " + mref,
                        model_json_path(mpath))
                else:
                    check_model_textures(mref, n + suffix, "ADDITIONAL")


# --------------------------------------------------------------------------
# Check 6: sounds
# --------------------------------------------------------------------------

def check_sounds():
    sounds_file = ASSETS / "sounds.json"
    if not sounds_file.is_file():
        err("SOUNDS_JSON_MISSING", "sounds.json", "assets/orespawn/sounds.json missing",
            sounds_file)
        return set()
    data, jerr = load_json(sounds_file)
    if jerr:
        err("SOUNDS_JSON_PARSE", "sounds.json", "JSON parse error: " + jerr, sounds_file)
        return set()
    keys = set(data.keys())
    for event, spec in data.items():
        entries = spec.get("sounds", []) if isinstance(spec, dict) else []
        if not entries:
            adv("SOUND_EVENT_EMPTY", event, "sound event defines no sounds", sounds_file)
        for entry in entries:
            if isinstance(entry, dict):
                if entry.get("type") == "event":
                    ref = entry.get("name", "")
                    ns, p = split_ref(ref)
                    if ns == MOD_ID and p not in keys:
                        err("SOUND_FILE_MISSING", event,
                            "references undefined sound event " + ref, sounds_file)
                    continue
                name = entry.get("name", "")
            else:
                name = entry
            if not isinstance(name, str) or not name:
                continue
            ns, p = split_ref(name)
            if ns != MOD_ID:
                continue  # vanilla sound assumed OK
            ogg = ASSETS / "sounds" / (p + ".ogg")
            if not ogg.is_file():
                err("SOUND_FILE_MISSING", event,
                    "sound entry %s -> missing file" % name, ogg)
    return keys


def check_sound_events(sound_keys):
    for sid in parse_sound_events():
        if sid not in sound_keys:
            adv("SOUND_EVENT_UNDEFINED", sid,
                "SoundEvent registered in ModSounds.java but not defined in "
                "sounds.json - plays silently", JAVA / "ModSounds.java")


# --------------------------------------------------------------------------
# Check 7: resource paths as git tracks them (index names, not disk names)
# --------------------------------------------------------------------------
# BUG-038: on a case-insensitive checkout the working tree can be lowercase
# while git still tracks the file under an uppercase name. Every other clone
# writes the tracked name into the jar, and the case-sensitive jar filesystem
# then misses the (lowercase-only) ResourceLocation lookup. No disk-based
# check can see that, so this one asks git for the index.

RESOURCE_PATH_OK = re.compile(r"^[a-z0-9/._-]+$")


def check_index_case(java_texts):
    import subprocess
    try:
        out = subprocess.run(["git", "ls-files", "-z", "--", "src/main/resources"],
                             cwd=ROOT, capture_output=True, check=True).stdout
    except (OSError, subprocess.CalledProcessError) as e:
        adv("INDEX_CASE_UNCHECKED", "git",
            "could not list the git index (%s); resource-path case not verified" % e)
        return
    tracked = {p for p in out.decode("utf-8", "replace").split("\0") if p}
    for p in sorted(tracked):
        inner = p[len("src/main/resources/"):]
        if not (inner.startswith("assets/") or inner.startswith("data/")):
            continue
        if not RESOURCE_PATH_OK.match(inner):
            err("RESOURCE_PATH_CASE", Path(p).name,
                "tracked in git as %s - ResourceLocation paths are lowercase-only, "
                "so this file is unreachable from any clone whose filesystem keeps "
                "the tracked name" % inner, ROOT / p)
    lit = re.compile(r'fromNamespaceAndPath\(\s*(?:OreSpawnMod\.MOD_ID|"orespawn")\s*,\s*'
                     r'"((?:textures|geo|animations|sounds)/[^"]+)"\s*\)')
    for path, text in java_texts:
        for m in lit.finditer(text):
            asset = m.group(1)
            if "src/main/resources/assets/orespawn/" + asset not in tracked \
                    and (ASSETS / asset).is_file():
                err("TEXTURE_REF_CASE", Path(path).stem,
                    "Java references orespawn:%s, which exists on disk but git does "
                    "not track that exact name (case mismatch or untracked file)"
                    % asset, path)


# --------------------------------------------------------------------------
# Reporting
# --------------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser(description="OreSpawn static asset/renderer auditor")
    ap.add_argument("--json", "--write", dest="json", action="store_true",
                    help="also refresh tools/asset_audit_report.json "
                         "(--write is an alias)")
    args = ap.parse_args()

    try:
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    except AttributeError:
        pass

    for p in (JAVA, ASSETS):
        if not p.is_dir():
            print("FATAL: expected directory missing: %s" % p)
            return 2

    items, block_items, spawn_eggs = parse_items()
    blocks = parse_blocks()
    entities = parse_entities()
    menus = parse_menus()
    java_texts = [(str(p), read(p)) for p in all_java_files()]

    block_terminals = check_blocks(blocks)
    check_items(items, block_items, spawn_eggs, block_terminals)
    check_entity_renderers(entities, java_texts)
    check_menu_screens(menus, java_texts)
    check_texture_refs(java_texts)
    check_additional_models(java_texts)
    sound_keys = check_sounds()
    check_sound_events(sound_keys)
    check_index_case(java_texts)

    # ---- report ----
    acknowledged = [f for f in findings
                    if (f["category"], f["name"]) in ACKNOWLEDGED]
    active = [f for f in findings
              if (f["category"], f["name"]) not in ACKNOWLEDGED]
    errors = [f for f in active if f["level"] == "ERROR"]
    advisories = [f for f in active if f["level"] == "ADVISORY"]

    print("OreSpawn static asset audit")
    print("  root: %s" % ROOT)
    print("  parsed: %d items (%d block items, %d spawn eggs), %d blocks, "
          "%d entities, %d menu types" % (len(items), len(block_items),
          len(spawn_eggs), len(blocks), len(entities), len(menus)))
    print()

    def summarize(label, rows):
        counts = {}
        for f in rows:
            counts[f["category"]] = counts.get(f["category"], 0) + 1
        print("%s: %d finding(s)" % (label, len(rows)))
        for cat in sorted(counts):
            print("  %-28s %d" % (cat, counts[cat]))
        print()

    summarize("ERROR", errors)
    summarize("ADVISORY", advisories)
    if acknowledged:
        summarize("ACKNOWLEDGED (whitelisted, no exit-code effect)", acknowledged)

    for f in errors + advisories:
        print("%s %s: %s — %s — %s" % (
            "[E]" if f["level"] == "ERROR" else "[A]",
            f["category"], f["name"], f["detail"], f["path"]))

    if acknowledged:
        print()
        print("ACKNOWLEDGED (see ACKNOWLEDGED set in %s for justifications):"
              % rel(__file__))
        for f in acknowledged:
            print("[K] %s: %s — %s — %s" % (
                f["category"], f["name"], f["detail"], f["path"]))

    if skipped:
        print()
        print("STATIC-ANALYSIS LIMITS (%d item(s) not fully verifiable):" % len(skipped))
        for s in skipped:
            print("  ~ %s — %s" % (s["what"], s["detail"]))

    if args.json:
        out = Path(__file__).resolve().parent / "asset_audit_report.json"
        out.write_text(json.dumps({
            "root": str(ROOT),
            "parsed": {"items": len(items), "block_items": len(block_items),
                       "spawn_eggs": len(spawn_eggs), "blocks": len(blocks),
                       "entities": len(entities), "menus": len(menus)},
            "error_count": len(errors), "advisory_count": len(advisories),
            "acknowledged_count": len(acknowledged),
            "errors": errors, "advisories": advisories,
            "acknowledged": acknowledged, "skipped": skipped,
        }, indent=2), encoding="utf-8")
        print()
        print("JSON report written: %s" % out)

    print()
    print("RESULT: %d error(s), %d advisory(ies), %d acknowledged -> exit %d"
          % (len(errors), len(advisories), len(acknowledged), 1 if errors else 0))
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
