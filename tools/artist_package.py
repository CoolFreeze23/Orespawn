#!/usr/bin/env python3
"""Artist handoff package generator — Phase G slice (f).

Builds the `artist_handoff/` package (PHASE_G_PROMPT.md G2/G5; scope addendum A.3;
the animation contract's SPEC additions, `phase_g_reports/animation_contract/contract_design.md` §7)
from the repository's own sources, mechanically wherever the data exists and from an
AUTHORED seed (`tools/artist_specs/<registry>.json`) where a human sentence is needed.

The repository's `artist_handoff/` is written by `package --out artist_handoff --entities ...` since
the mirror drop landed (d51f06f; owner 2026-09-13, second set, item 10: the pilot pair first - the Queen's
`idle` and `attack`, the Beaver); until then (owner 2026-09-05, addendum item 23 (8)(f)) nothing under
`artist_handoff/` was committed and the generator refused the path. The `package` subcommand writes
wherever `--out` points.

Subcommands (standard library only; Python 3.11+):

    inventory    --out DIR            INVENTORY.csv, one row per ModEntities registration
    texture-map  --out DIR            TEXTURE_MAP.csv, every shipped textures/entity png
    spec ENTITY  --out DIR            entities/<registry>/SPEC.md + spec.manifest.json
    readme       --out DIR            README_FIRST.md (the G5 artist contract)
    bbmodel ENTITY --out DIR          entities/<registry>/<name>.bbmodel (Blockbench project)
    roundtrip ENTITY [--out DIR]      .bbmodel -> geo + animation JSON -> semantic diff vs shipped
    package      --out DIR            the whole tree for the landed species + dryrun_summary
    check FOLDER [--manifest FILE] [--lock-mode warn|reject]
                                      validate a returned artist folder against its manifest

Every generated text file is written with LF endings and UTF-8.
"""

from __future__ import annotations

import argparse
import base64
import csv
import hashlib
import io
import json
import math
import os
import re
import struct
import sys
import uuid
from collections import Counter, OrderedDict, defaultdict
from pathlib import Path
from typing import Any, Iterable

TOOL_VERSION = ("0.2.7 (the third-set tooling commit, 2026-09-13: the pilot's second clip is bite; every contract marker resolved to the "
                "2026-09-06 rulings, README rule 5 split by controller kind; the priority table lists the packaged folders only; the reference "
                "clip's span rule - a period multiple closing within 5 degrees, capped at 6 s)")

ROOT = Path(__file__).resolve().parent.parent
BB_NAMESPACE = uuid.UUID("6f0b4b2e-9d1c-4a7e-8f3a-2c5e1d7b9a10")  # deterministic .bbmodel uuids

CONTRACT_CLIPS_LOOP = ("idle", "walk", "swim", "fly", "aggro_idle", "calm_idle")
CONTRACT_CLIPS_TRIGGERED = OrderedDict((("attack", "false"), ("hurt", "false"), ("death", "hold_on_last_frame")))
CONTRACT_CLIP_NAMES = CONTRACT_CLIPS_LOOP + tuple(CONTRACT_CLIPS_TRIGGERED)

# ONE locked-bone policy (owner 2026-09-06, scope addendum item 24 (18)), stated identically in README_FIRST rule 6,
# SPEC §3 / §5 / §7 / §11 and `check`'s summary line: keying a locked bone is allowed and WARNED — the SPEC states the
# consequence (the hitbox part follows the bone in-game); renaming, re-parenting or deleting a locked bone is REFUSED.
# The reject mode for keys (`lock_mode: reject` in the manifest, or `check --lock-mode reject`) stays available for the
# day the server-side hitbox evaluator lands; it is not the policy today. (Contract §8.1 / P7 wrote REJECT for a key on a
# locked bone; the pilot boss's shipped clips key 26 of her 27 locked bones — allowed and warned under the ruling.)
LOCK_MODE_DEFAULT = "warn"
LOCK_POLICY_ID = "warn-keyed, refuse-structural"  # the manifest's `lock_policy` field; `check` prints it beside the sentence
LOCK_POLICY = ("Keying a locked bone is allowed; the checker warns, and the hitbox part follows the bone in-game "
               "(the consequence, so keep such keys deliberate). Renaming, re-parenting or deleting a locked bone is refused.")
LOCK_REJECT_MODE = ("A reject mode for keys on locked bones stays available for the day the server-side hitbox evaluator lands "
                    "(`check --lock-mode reject`, or `lock_mode: reject` in the manifest); it is not today's policy — the one open item "
                    "in this package: whether and when that mode becomes the policy is the owner's later ruling; everything else here "
                    "rests on the rulings of 2026-09-06.")

# `check`'s rule table (README_FIRST "what check enforces" and SPEC §11 quote this list; check_folder implements it).
ROTATION_BOUND_DEG = 3600.0
POSITION_BOUND = 1024.0
CHECK_REJECTS = [
    "a missing or empty folder, or a missing manifest",
    "no `.animation.json`, one under the wrong name (the sheet names the file), or a second one",
    "a clip not in the sheet (renamed or added; `idle_alt_N` only where the sheet allows it), or a clip delivered for a Tier-3 creature",
    "a wrong `loop` value; a missing `idle` or `walk` (they open the game's switch only TOGETHER — one without the other leaves the creature on its code-driven motion); a missing clip the game already triggers by name",
    "a key on a bone the rig does not have (renamed), or a channel other than rotation / position / scale",
    "a Molang string, a non-numeric or non-finite value, a custom-instruction (`timeline`) key",
    "an event keyframe (`sound_effects` / `particle_effects`) on a LOOPING clip of a phase-locked species — no event keyframes on loops; code-fired events come from the trigger inventory (one-shot clips may carry them; a creature whose controllers are its own GeckoLib controllers — the SPEC's controller kind `native`, the Queen — is exempt: native controllers are not phase-locked, event keys fire per loop)",
    f"|rotation| > {ROTATION_BOUND_DEG:.0f} degrees or |position| > {POSITION_BOUND:.0f} units",
    "a key later than the clip's `animation_length` (a declared length shorter than the last key)",
    "a returned `.geo.json` whose bones, parents, pivots, rotations, cubes, UVs or canvas differ from the shipped rig",
    "a `locked` bone renamed, re-parented or deleted in a returned `.geo.json` (the finding names the bone)",
    "a texture that is not this creature's, or whose canvas size changed",
    "a `*_reference.animation.json` returned that is not the package's own untouched copy (the reference-only clip the package carries beside the sheet: the classic code sampled at fixed inputs, never edited, never delivered, never shipped — the finding names the file; the untouched copy coming back is warned, not a delivery)",
]
# The reference-only clip (owner 2026-09-13, second set, addendum item 27 (3)): `tools/reference_clips/<registry>_reference.animation.json`,
# the classic hook sampled at fixed inputs by the g1 harness (ReferenceClipSampler; gradle referenceClips), copied beside each
# species' sheet by `package`, marked reference-only in SPEC §4.3, refused by `check` (a REJECT naming the file) and by the
# asset audit under src/main/resources (the jar never carries one). `_preview` stays a WARN (ruled 2026-09-06, Q15 (a): a
# Blockbench-only export, never in the jar); this is a REJECT.
REFERENCE_CLIP_SUFFIX = "_reference.animation.json"
REFERENCE_CLIP_INDEX = "reference_clips.json"
REFERENCE_CLIP_NAME = "reference"
README_REFERENCE_SENTENCE = ("The `<registry>_reference.animation.json` beside each sheet is REFERENCE-ONLY: the creature's classic code sampled at "
                             "fixed inputs so you can see today's motion in Blockbench — never edit it into a delivery, never return it (the "
                             "checker REJECTS it by name) and never ship it (the game's jar never carries one).")
CHECK_WARNS = [
    "a key on a `locked` bone (the clip and the bones are named) — " + LOCK_POLICY,
    "a `_preview` file delivered (a Blockbench-only aid, never in the jar — ruled 2026-09-06, Q15 (a); leave it out of the delivery)",
    "a returned `.geo.json` at all (the shipped rig is used regardless — do not re-export it)",
    "a clip length far from the one the sheet states (rule 5: a phase-locked creature's loops at 1.0 s — ruled 2026-09-06, Q14 (a); "
    "a native creature's clips at their shipped length; a one-shot at the length its row states); an optional clip not delivered",
    "an alias-named texture (the canonical name is preferred); a `format_version` other than 1.8.0",
    "a number written as a string; an unknown keyframe key; a clip without `animation_length`; an unreadable `.bbmodel`",
]
LAW_SERIES = {  # migration design Appendix A.5 — contiguous variant series (the girlfriend0..40 law)
    "girlfriend": (0, 40),
    "boyfriend": (0, 27),
    "swimshorts": (0, 17),
    "elevator": (1, 10),
}
GUI_STRAY_NAMES = {"items.png", "textures.png", "logo.png", "spinners.png", "girlfriendgui.png"}
TICKS_PER_SECOND = 20.0
# README rule 5, the phase-locked kind (owner 2026-09-06, Q14 (a); split by controller kind 2026-09-13, third set, item 28 (3)):
# every loop of a phase-locked creature is authored at 1.0 s — in-game the length is free (P6) and the sheet's tempo table
# gives the rate; a native creature (controller kind `native`, the Queen) keeps each clip's shipped length instead.
LOOP_AUTHORING_LENGTH_SECONDS = 1.0


# ---------------------------------------------------------------------------
# paths
# ---------------------------------------------------------------------------

class Paths:
    def __init__(self, root: Path):
        self.root = root
        self.assets = root / "src/main/resources/assets/orespawn"
        self.geo_dir = self.assets / "geo/entity"
        self.anim_dir = self.assets / "animations/entity"
        self.tex_root = self.assets / "textures"
        self.tex_dir = self.tex_root / "entity"
        self.java = root / "src/main/java/danger/orespawn"
        self.mod_entities = self.java / "ModEntities.java"
        self.client_dir = self.java / "entity/client"
        self.entity_dir = self.java / "entity"
        self.ai_dir = self.java / "entity/ai"
        self.pose_dir = self.java / "entity/pose"
        self.client_class = self.java / "OreSpawnClient.java"
        self.dev_renderers = self.java / "client/PhaseGDevRenderers.java"
        self.profiles = root / "src/main/resources/data/orespawn/multihitboxlib/hitbox_profiles"
        self.specs_dir = root / "tools/artist_specs"
        self.keyframe_clips = root / "tools/keyframe_clips"      # the exact transcriptions' clip manifests (one per registry)
        self.reference_clips = root / "tools/reference_clips"    # the sampler's reference-only clips and their index
        self.pins = root / "tools/reference_renderer_pins.json"
        self.design = root / "phase_g_reports/geckolib_migration_design.md"
        self.provenance = root / "provenance_byte_identical_assets.txt"
        self.sidecar_dirs = [root / "phase_g_reports/s4_proof/generated", root / "phase_g_reports/g1_proof/generated"]
        self.proofs = [root / "tools/s4_model_proofs.json", root / "tools/g1_model_proofs.json"]
        self.contract = root / "phase_g_reports/animation_contract/contract_design.md"
        self.open_questions = root / "phase_g_reports/animation_contract/open_questions.md"


# ---------------------------------------------------------------------------
# small utilities
# ---------------------------------------------------------------------------

class Warnings:
    """Collected generator warnings: (scope, code, message)."""

    def __init__(self) -> None:
        self.items: list[tuple[str, str, str]] = []

    def add(self, scope: str, code: str, message: str) -> None:
        self.items.append((scope, code, message))

    def for_scope(self, scope: str) -> list[tuple[str, str, str]]:
        return [w for w in self.items if w[0] == scope]


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def load_json(path: Path) -> Any:
    with open(path, "r", encoding="utf-8") as fh:
        return json.load(fh)


def write_text(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)


def write_json(path: Path, data: Any, indent: int = 2) -> None:
    write_text(path, json.dumps(data, indent=indent, ensure_ascii=False) + "\n")


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as fh:
        for chunk in iter(lambda: fh.read(1 << 16), b""):
            h.update(chunk)
    return h.hexdigest()


def png_size(path: Path) -> tuple[int, int]:
    """Width/height from the PNG IHDR chunk (no image library)."""
    with open(path, "rb") as fh:
        head = fh.read(33)
    if len(head) < 24 or head[:8] != b"\x89PNG\r\n\x1a\n" or head[12:16] != b"IHDR":
        raise ValueError(f"not a PNG: {path}")
    width, height = struct.unpack(">II", head[16:24])
    return width, height


def fmt(value: float, digits: int = 4) -> str:
    if isinstance(value, bool):
        return str(value)
    if isinstance(value, int):
        return str(value)
    text = f"{value:.{digits}f}"
    if "." in text:  # trailing zeros go only after a decimal point: fmt(40.0, 0) is "40", not "4" (0.2.7)
        text = text.rstrip("0").rstrip(".")
    return text if text not in ("", "-0") else "0"


def vec(v: Iterable[float] | None, n: int = 3) -> list[float]:
    if v is None:
        return [0.0] * n
    out = [float(x) for x in v]
    while len(out) < n:
        out.append(0.0)
    return out[:n]


def csv_write(path: Path, header: list[str], rows: list[list[Any]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="") as fh:
        writer = csv.writer(fh, lineterminator="\n")
        writer.writerow(header)
        for row in rows:
            writer.writerow(["" if c is None else c for c in row])


def md_table(header: list[str], rows: list[list[Any]]) -> str:
    def cell(c: Any) -> str:
        return str(c).replace("|", "\\|").replace("\n", " ")

    lines = ["| " + " | ".join(header) + " |", "|" + "|".join("---" for _ in header) + "|"]
    for row in rows:
        lines.append("| " + " | ".join(cell(c) for c in row) + " |")
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# repository facts (mechanical)
# ---------------------------------------------------------------------------

# Both registration forms: `EntityType.Builder.of(X::new, ...)` and the explicitly typed
# `EntityType.Builder.<X>of(X::new, ...)` (the projectiles, item entities and the fish hook use the second).
ENTITY_RE = re.compile(
    r"(\w+)\s*=\s*(?://[^\n]*\n\s*)*ENTITY_TYPES\.register\(\s*\"([a-z0-9_]+)\"\s*,\s*\(\)\s*->\s*EntityType\.Builder\.(?:<\w+>)?of\(\s*(\w+)::new\s*,\s*MobCategory\.(\w+)\s*\)(.*?)\.build\(",
    re.S,
)


def parse_mod_entities(paths: Paths) -> "OrderedDict[str, dict[str, Any]]":
    """registry -> {const, java_class, category, width, height, line}."""
    text = read_text(paths.mod_entities)
    out: OrderedDict[str, dict[str, Any]] = OrderedDict()
    for m in ENTITY_RE.finditer(text):
        const, registry, cls, cat, body = m.groups()
        sized = re.search(r"\.sized\(\s*([-0-9.eE]+)[fF]?\s*,\s*([-0-9.eE]+)[fF]?\s*\)", body)
        out[registry] = {
            "const": const,
            "java_class": cls,
            "category": cat,
            "width": float(sized.group(1)) if sized else None,
            "height": float(sized.group(2)) if sized else None,
            "line": text[: m.start()].count("\n") + 1,
        }
    return out


TIER_ROW_RE = re.compile(r"^\|\s*(\d+)\s*\|")


def parse_tier_table(paths: Paths, warnings: Warnings) -> dict[str, dict[str, Any]]:
    """model class -> {served: [registry...], tier: int|None, animation_class: str, scale: str} from the G0 design table."""
    out: dict[str, dict[str, Any]] = {}
    if not paths.design.exists():
        warnings.add("global", "DESIGN_MISSING", f"tier table source missing: {paths.design}")
        return out
    for line in read_text(paths.design).splitlines():
        if not TIER_ROW_RE.match(line):
            continue
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) < 8:
            continue
        model = cells[1].strip("`").split("`")[0].strip()
        served = re.findall(r"([a-z0-9_]+) \[", cells[2])
        tier_text = cells[7].strip()
        tier = int(tier_text) if tier_text.isdigit() else None
        out[model] = {"served": served, "tier": tier, "animation_class": cells[4], "scale": cells[5]}
    if len(out) != 109:
        warnings.add("global", "TIER_TABLE_COUNT", f"tier table parsed {len(out)} model rows (the G0 inventory has 109)")
    return out


VANILLA_ROW_RE = re.compile(r"^\|\s*`(\w+)`\s*\|\s*`(\w+)`\s*\|\s*([a-z0-9_]+)\s*\[[^\]]*\]\s*\|(.*)\|\s*(\d+)\s*\|\s*$")


def parse_vanilla_reuse_table(paths: Paths) -> dict[str, dict[str, Any]]:
    """registry -> {model, renderer, tier} from the design's "Vanilla-model reuse (proposed Tier 0)" table
    (`geckolib_migration_design.md`, the six cow renderers and SpiderDriverRenderer): consumers with no
    OreSpawn model class to convert. They are not counted among the 109 model rows."""
    out: dict[str, dict[str, Any]] = {}
    if not paths.design.exists():
        return out
    inside = False
    for line in read_text(paths.design).splitlines():
        if line.startswith("#### Vanilla-model reuse"):
            inside = True
            continue
        if inside and (line.startswith("<!-- END") or line.startswith("## ")):
            break
        if not inside:
            continue
        m = VANILLA_ROW_RE.match(line.strip())
        if m:
            out[m.group(3)] = {"model": m.group(1), "renderer": m.group(2), "tier": int(m.group(5))}
    return out


def registry_tiers(tier_table: dict[str, dict[str, Any]]) -> dict[str, dict[str, Any]]:
    """registry -> {model, tier, animation_class, scale}; a registry served by several models keeps the first."""
    out: dict[str, dict[str, Any]] = {}
    for model, row in tier_table.items():
        for registry in row["served"]:
            out.setdefault(registry, {"model": model, "tier": row["tier"], "animation_class": row["animation_class"], "scale": row["scale"]})
    return out


def load_pins(paths: Paths) -> dict[str, dict[str, Any]]:
    if not paths.pins.exists():
        return {}
    data = load_json(paths.pins)
    return {e["entity"].casefold(): e for e in data.get("entries", [])}


def pin_for(registry: str, java_class: str, pins: dict[str, dict[str, Any]]) -> dict[str, Any] | None:
    """The reference pin (1.7.10 class name) for a port registry: the port class with its `Entity` prefix stripped."""
    candidates = [java_class, re.sub(r"^Entity", "", java_class), registry.replace("_", "")]
    for c in candidates:
        hit = pins.get(c.casefold())
        if hit:
            return hit
    return None


def load_profiles(paths: Paths) -> dict[str, dict[str, Any]]:
    out: dict[str, dict[str, Any]] = {}
    if paths.profiles.exists():
        for p in sorted(paths.profiles.glob("*.json")):
            out[p.stem] = load_json(p)
    return out


def load_sidecars(paths: Paths) -> dict[str, dict[str, Any]]:
    """model_id (e.g. model_beaver) -> conversion sidecar; the s4 set wins over the g1 set."""
    out: dict[str, dict[str, Any]] = {}
    for d in reversed(paths.sidecar_dirs):
        if d.exists():
            for p in sorted(d.glob("model_*.conversion.json")):
                out[load_json(p)["model_id"]] = load_json(p) | {"_path": str(p.relative_to(paths.root)).replace("\\", "/")}
    return out


def load_proof_ids(paths: Paths) -> dict[str, str]:
    out: dict[str, str] = {}
    for p in paths.proofs:
        if p.exists():
            for m in load_json(p).get("models", []):
                out.setdefault(m["id"], p.name)
    return out


def load_reference_clip_index(paths: Paths) -> dict[str, dict[str, Any]]:
    """registry -> the sampler's index row (`tools/reference_clips/reference_clips.json`, written by the g1 harness's
    ReferenceClipSampler: file, sha256, rule, span, keys, the sampled inputs); empty when the sampler has not run."""
    index = paths.reference_clips / REFERENCE_CLIP_INDEX
    if not index.exists():
        return {}
    return {row["registry"]: row for row in load_json(index).get("clips", [])}


def parse_provenance(paths: Paths) -> dict[str, str]:
    out: dict[str, str] = {}
    if not paths.provenance.exists():
        return out
    pat = re.compile(r"^\s*textures[\\/]entity[\\/](.+?)\s+<=\s+(.+?)\s*$", re.I)
    for line in read_text(paths.provenance).splitlines():
        m = pat.match(line)
        if m:
            out[m.group(1).casefold()] = m.group(2)
    return out


def geo_files(paths: Paths) -> dict[str, Path]:
    """geo stem (e.g. 'beaver', 'the_queen') -> path."""
    return {p.name[: -len(".geo.json")]: p for p in sorted(paths.geo_dir.glob("*.geo.json"))}


def animation_files(paths: Paths) -> dict[str, Path]:
    return {p.name[: -len(".animation.json")]: p for p in sorted(paths.anim_dir.glob("*.animation.json"))}


# ---------------------------------------------------------------------------
# client-side discovery: which client classes draw a registry, which geo/textures they name
# ---------------------------------------------------------------------------

CLIENT_CLASS_RE = re.compile(r"\b([A-Z]\w*(?:Renderer|Model|Replacement|ReplacedRenderer))\b")


def client_files_by_class(paths: Paths) -> dict[str, Path]:
    return {p.stem: p for p in paths.client_dir.glob("*.java")}


def renderer_registrations(paths: Paths, entities: dict[str, dict[str, Any]]) -> dict[str, list[str]]:
    """registry -> [client class names registered as its renderer(s)], from OreSpawnClient + PhaseGDevRenderers."""
    const_to_registry = {row["const"]: reg for reg, row in entities.items()}
    out: dict[str, list[str]] = defaultdict(list)
    client = read_text(paths.client_class) if paths.client_class.exists() else ""
    dev = read_text(paths.dev_renderers) if paths.dev_renderers.exists() else ""
    dev_methods: dict[str, tuple[str, list[str]]] = {}
    for m in re.finditer(r"(\w+)\(\)\s*\{[^}]*?select\(\s*\"([a-z0-9_]+)\"\s*,\s*(\w+)::new\s*,\s*(\w+)(?:\.Renderer)?::new", dev, re.S):
        dev_methods[m.group(1)] = (m.group(2), [m.group(3), m.group(4)])
    for m in re.finditer(r"registerEntityRenderer\(\s*ModEntities\.(\w+)\.get\(\)\s*,\s*([\w.]+)(?:::new|\(\))", client):
        registry = const_to_registry.get(m.group(1))
        if not registry:
            continue
        target = m.group(2)
        if target.startswith("PhaseGDevRenderers."):
            method = target.split(".", 1)[1]
            if method in dev_methods:
                out[registry].extend(dev_methods[method][1])
            continue
        out[registry].append(target.split(".")[0])
    return dict(out)


CLIENT_SUFFIX_RE = re.compile(r"(GeoReplacedRenderer|GeoReplacement|Renderer|Model)$")


def class_stem(name: str) -> str:
    """`Robot1Renderer` / `Robot1GeoReplacement` -> `Robot1`; `QueenModel` -> `Queen`."""
    return CLIENT_SUFFIX_RE.sub("", name)


def expand_client_files(classes: Iterable[str], by_class: dict[str, Path], hops: int = 2) -> list[Path]:
    """The registered classes plus the client classes they name that share a registered class's stem
    (bounded hops, client dir only). The stem rule keeps the shared seam classes (OreSpawnGeoReplacement,
    OreSpawnGeoReplacedEntityRenderer, ...) and other species' classes out of a species' file set."""
    seen: list[str] = []
    frontier = [c for c in classes if c in by_class]
    stems = {class_stem(c) for c in frontier}
    for _ in range(hops + 1):
        nxt: list[str] = []
        for c in frontier:
            if c in seen:
                continue
            seen.append(c)
            text = read_text(by_class[c])
            for ref in CLIENT_CLASS_RE.findall(text):
                if ref in by_class and ref not in seen and ref not in nxt and class_stem(ref) in stems:
                    nxt.append(ref)
        frontier = nxt
    return [by_class[c] for c in seen]


def textures_in_sources(files: Iterable[Path], catalog_names: Iterable[str]) -> list[str]:
    """Texture names a set of Java files reference: literal `textures/entity/x.png` and `textures/entity/prefix" + i + ".png` series."""
    names = set()
    catalog = list(catalog_names)
    lower = {n.casefold(): n for n in catalog}
    for f in files:
        text = read_text(f)
        for frag in re.findall(r"textures/entity/([A-Za-z0-9_./-]+)", text):
            if frag.endswith(".png"):
                names.add(lower.get(Path(frag).name.casefold(), Path(frag).name))
            else:
                prefix = Path(frag).name
                names.update(n for n in catalog if re.fullmatch(rf"{re.escape(prefix)}\d+\.png", n, re.I))
    return sorted(names, key=str.lower)


def geo_refs_in_sources(files: Iterable[Path]) -> list[str]:
    refs: list[str] = []
    for f in files:
        for g in re.findall(r"geo/entity/([a-z0-9_]+)\.geo\.json", read_text(f)):
            if g not in refs:
                refs.append(g)
    return refs


class Species:
    """Everything the generator knows about one registry name."""

    def __init__(self, registry: str, row: dict[str, Any]):
        self.registry = registry
        self.const = row["const"]
        self.java_class = row["java_class"]
        self.category = row["category"]
        self.width = row["width"]
        self.height = row["height"]
        self.model: str | None = None
        self.tier: int | None = None
        self.animation_class = ""
        self.design_scale = ""
        self.client_classes: list[str] = []
        self.client_files: list[Path] = []
        self.geo_stem: str | None = None
        self.geo_path: Path | None = None
        self.anim_path: Path | None = None
        self.textures: list[str] = []
        self.pin: dict[str, Any] | None = None
        self.profile: dict[str, Any] | None = None
        self.profile_name: str | None = None
        self.sidecar: dict[str, Any] | None = None
        self.proof: str | None = None
        self.seed: dict[str, Any] | None = None
        self.entity_file: Path | None = None
        self.status = "excluded"
        self.status_note = ""
        self.renders_nothing = False

    @property
    def landed(self) -> bool:
        return self.geo_path is not None

    @property
    def geo(self) -> dict[str, Any]:
        return load_json(self.geo_path)

    @property
    def animation(self) -> dict[str, Any]:
        if self.anim_path and self.anim_path.exists():
            return load_json(self.anim_path)
        return {"format_version": "1.8.0", "animations": {}}

    @property
    def is_mhlib_boss(self) -> bool:
        """A MultiHitboxLib profile with synced bones: the contract's Tier-1 (MHLib) case (§8.1)."""
        return bool(self.profile and self.profile.get("synched-bones"))

    @property
    def tier_label(self) -> str:
        """The tier as an artist reads it. The design's tier table says 0 for the Queen ('done' — a native rig);
        the animation contract calls the same species its Tier-1 (MHLib) boss (§8.1; ruled 2026-09-06, Q16 (a): the
        pilot boss): both are written, so neither document's number is contradicted."""
        if self.tier == 0 and self.is_mhlib_boss and self.landed:
            return "Tier 1 (boss; the design's 'done' row)"
        if self.tier is None:
            return "no tier (not in the G0 inventory)"
        if self.tier == 1:
            return "Tier 1 (boss)"
        return f"Tier {self.tier}"


PROJECTILE_BASES_RE = re.compile(r"Projectile|Arrow|Fireball|Throwable|Hurting|Bolt|FishingHook|Hook$|Ball$")  # `Ball$`: the mod's LaserBall family


def renders_nothing(client_files: Iterable[Path]) -> bool:
    """True when a species' renderer overrides shouldRender to return false (the head sidecars: invisible proxy hitboxes)."""
    for f in client_files:
        text = read_text(f)
        m = re.search(r"boolean\s+shouldRender\s*\([^)]*\)\s*\{(.*?)\}", text, re.S)
        if m and re.search(r"return\s+false\s*;", m.group(1)):
            return True
    return False


def excluded_note(species: "Species") -> str:
    """Why a registration with no rigged model is excluded, in the vocabulary that is true for it."""
    if species.entity_file:
        text = read_text(species.entity_file)
        ext = re.search(r"class\s+\w+\s+extends\s+([\w.<>]+)", text)
        base = ext.group(1).split("<")[0].split(".")[-1] if ext else "?"
        if base == "ItemEntity" or "ItemEntity" in base:
            return f"item entity (extends {base}): no rig, no artist work"
        if PROJECTILE_BASES_RE.search(base):
            kind = "fish hook" if "Hook" in base else "projectile"
            return f"{kind} (extends {base}): no rig, no artist work"
        return f"no rigged model in the G0 inventory (extends {base}; category {species.category})"
    return f"no rigged model in the G0 inventory (no entity source under entity/; category {species.category})"


class Repo:
    """All mechanical facts, loaded once."""

    def __init__(self, root: Path, warnings: Warnings | None = None):
        self.paths = Paths(root)
        self.warnings = warnings or Warnings()
        self.entities = parse_mod_entities(self.paths)
        self.tier_table = parse_tier_table(self.paths, self.warnings)
        self.tiers = registry_tiers(self.tier_table)
        self.vanilla_reuse = parse_vanilla_reuse_table(self.paths)
        self.pins = load_pins(self.paths)
        self.profiles = load_profiles(self.paths)
        self.sidecars = load_sidecars(self.paths)
        self.proof_ids = load_proof_ids(self.paths)
        self.reference_clips = load_reference_clip_index(self.paths)
        self.provenance = parse_provenance(self.paths)
        self.geos = geo_files(self.paths)
        self.anims = animation_files(self.paths)
        self.by_class = client_files_by_class(self.paths)
        self.registrations = renderer_registrations(self.paths, self.entities)
        self.texture_names = sorted((p.name for p in self.paths.tex_dir.glob("*.png")), key=str.lower)
        self.species: OrderedDict[str, Species] = OrderedDict()
        for registry, row in self.entities.items():
            self.species[registry] = self._build(registry, row)
        claimed = {s.geo_stem for s in self.species.values() if s.geo_stem}
        for stem in self.geos:
            if stem not in claimed:
                self.warnings.add("global", "GEO_UNCLAIMED", f"geo/entity/{stem}.geo.json is referenced by no registered renderer")

    def _build(self, registry: str, row: dict[str, Any]) -> Species:
        s = Species(registry, row)
        tier = self.tiers.get(registry)
        if tier:
            s.model, s.tier, s.animation_class, s.design_scale = tier["model"], tier["tier"], tier["animation_class"], tier["scale"]
        s.client_classes = self.registrations.get(registry, [])
        s.client_files = expand_client_files(s.client_classes, self.by_class)
        refs = [g for g in geo_refs_in_sources(s.client_files) if g in self.geos]
        if refs:
            s.geo_stem = refs[0]
            s.geo_path = self.geos[refs[0]]
            s.anim_path = self.anims.get(refs[0])
            if len(refs) > 1:
                self.warnings.add(registry, "GEO_AMBIGUOUS", f"client files name several geos {refs}; using {refs[0]}")
            if s.anim_path is None:
                self.warnings.add(registry, "ANIM_MISSING", f"no animations/entity/{refs[0]}.animation.json beside the geo")
        s.textures = textures_in_sources(s.client_files, self.texture_names)
        s.pin = pin_for(registry, s.java_class, self.pins)
        s.profile = self.profiles.get(registry)
        s.profile_name = registry if s.profile else None
        if s.geo_stem:
            model_id = "model_" + s.geo_stem.replace("_", "")
            s.sidecar = self.sidecars.get(model_id) or self.sidecars.get("model_" + s.geo_stem)
            s.proof = self.proof_ids.get(model_id) or self.proof_ids.get("model_" + s.geo_stem)
        entity_file = self.paths.entity_dir / f"{s.java_class}.java"
        s.entity_file = entity_file if entity_file.exists() else None
        seed_path = self.paths.specs_dir / f"{registry}.json"
        s.seed = load_json(seed_path) if seed_path.exists() else None
        vanilla = self.vanilla_reuse.get(registry)
        if vanilla and not s.model:
            s.model, s.tier, s.animation_class = f"{vanilla['model']} (vanilla)", vanilla["tier"], "vanilla model reuse"
        s.renders_nothing = renders_nothing(s.client_files)
        if s.landed:
            s.status = "landed candidate"
            if s.tier == 0 or (s.tier is None and s.profile):
                s.status_note = "native GeckoLib rig (no dev switch; classic renderer retired)"
            else:
                s.status_note = "behind -Dorespawn.dev.geckolibRenderers=candidate"
        elif vanilla and s.model and s.model.endswith("(vanilla)"):
            s.status = "excluded"
            s.status_note = (f"Tier {vanilla['tier']}: a vanilla {vanilla['model']} drawn by {vanilla['renderer']} (the design's "
                             f"'Vanilla-model reuse' table): no OreSpawn rig to convert, no artist work")
        elif s.model and s.tier in (1, 2, 3):
            s.status = "classic only"
            if s.renders_nothing:
                s.status_note = (f"Tier {s.tier} model {s.model}: a head sidecar — its renderer refuses to draw (shouldRender false); "
                                 f"an invisible gaze/damage hitbox, so it renders nothing and takes no artist work; no geo landed")
            else:
                s.status_note = f"Tier {s.tier} model {s.model}; no geo landed yet"
        elif s.model and s.tier == 0:
            s.status = "excluded"
            s.status_note = f"Tier 0 ({s.model}): solver-fed / native rig, no artist conversion"
        else:
            s.status = "excluded"
            s.status_note = excluded_note(s)
        return s

    def landed_species(self) -> list[Species]:
        return [s for s in self.species.values() if s.landed]

    def get(self, registry: str) -> Species:
        if registry not in self.species:
            raise SystemExit(f"unknown registry name {registry!r}; known landed species: "
                             + ", ".join(s.registry for s in self.landed_species()))
        return self.species[registry]


# ---------------------------------------------------------------------------
# textures: the catalog, dedupe by content, variant series, strays, consumers -> TEXTURE_MAP.csv
# ---------------------------------------------------------------------------

SERIES_RE = re.compile(r"^([a-z_]+?)(\d+)\.png$", re.I)
ARMOR_STRAY_RE = re.compile(r"^([a-z]+)_([12])\.png$", re.I)


def java_texture_consumers(paths: Paths, catalog_names: Iterable[str]) -> dict[str, list[str]]:
    """texture name -> [Java class simple names that reference it], over every file under src/main/java."""
    catalog = list(catalog_names)
    lower = {n.casefold(): n for n in catalog}
    out: dict[str, list[str]] = defaultdict(list)
    for f in sorted(paths.java.rglob("*.java")):
        text = read_text(f)
        if "textures/entity/" not in text:
            continue
        hits: set[str] = set()
        for frag in re.findall(r"textures/entity/([A-Za-z0-9_./-]+)", text):
            if frag.endswith(".png"):
                name = lower.get(Path(frag).name.casefold())
                if name:
                    hits.add(name)
            else:
                prefix = Path(frag).name
                hits.update(n for n in catalog if re.fullmatch(rf"{re.escape(prefix)}\d+\.png", n, re.I))
        for name in hits:
            out[name].append(f.stem)
    return {k: sorted(set(v)) for k, v in out.items()}


def other_directory_twins(paths: Paths) -> dict[str, list[str]]:
    """sha256 -> [texture paths outside textures/entity with that content] (gui / item / items / blocks / models/armor)."""
    out: dict[str, list[str]] = defaultdict(list)
    for sub in ("gui", "item", "items", "blocks", "models/armor"):
        d = paths.tex_root / sub
        if not d.exists():
            continue
        for p in sorted(d.rglob("*.png")):
            out[sha256_file(p)].append(str(p.relative_to(paths.tex_root)).replace("\\", "/"))
    return out


class TextureCatalog:
    """Every shipped textures/entity png with hash, size, twins, series, stray class and consumers."""

    def __init__(self, repo: "Repo", entity_textures: dict[str, list[str]] | None = None):
        self.repo = repo
        paths = repo.paths
        self.names = list(repo.texture_names)
        self.hash = {n: sha256_file(paths.tex_dir / n) for n in self.names}
        self.size = {n: png_size(paths.tex_dir / n) for n in self.names}
        groups: dict[str, list[str]] = defaultdict(list)
        for n in self.names:
            groups[self.hash[n]].append(n)
        self.groups = groups
        self.consumers = java_texture_consumers(paths, self.names)
        self.elsewhere = other_directory_twins(paths)
        self.entity_textures = entity_textures or {s.registry: s.textures for s in repo.species.values()}
        registry_names = set(repo.entities)
        self.canonical: dict[str, str] = {}
        for digest, members in groups.items():
            self.canonical[digest] = self._pick_canonical(members, registry_names)
        self.series = self._series()
        self.stray = {n: self._stray_class(n) for n in self.names}

    def _pick_canonical(self, members: list[str], registry_names: set[str]) -> str:
        """Appendix A.2: prefer a currently referenced, lowercase, registry-aligned name; then the shortest; then alphabetical."""
        def key(n: str) -> tuple:
            stem = n[:-4]
            referenced = bool(self.consumers.get(n))
            registry_aligned = stem in registry_names or stem.replace("_", "") in {r.replace("_", "") for r in registry_names}
            return (not referenced, not registry_aligned, n != n.lower(), len(n), n)
        return sorted(members, key=key)[0]

    def _series(self) -> dict[str, dict[str, Any]]:
        by_prefix: dict[str, list[int]] = defaultdict(list)
        for n in self.names:
            m = SERIES_RE.match(n)
            if m:
                by_prefix[m.group(1).lower()].append(int(m.group(2)))
        out: dict[str, dict[str, Any]] = {}
        for prefix, nums in by_prefix.items():
            nums = sorted(nums)
            lo, hi = nums[0], nums[-1]
            contiguous = nums == list(range(lo, hi + 1))
            law = LAW_SERIES.get(prefix)
            law_ok = None
            if law:
                law_ok = (lo, hi) == law and contiguous
                if not law_ok:
                    self.repo.warnings.add("global", "SERIES_LAW",
                                           f"series {prefix}{law[0]}..{law[1]} must be contiguous; found {lo}..{hi} ({len(nums)} files, contiguous={contiguous})")
            base = f"{prefix}.png" if f"{prefix}.png" in self.names else None
            out[prefix] = {"numbers": nums, "lo": lo, "hi": hi, "count": len(nums), "contiguous": contiguous,
                           "law": law, "law_ok": law_ok, "base": base}
        return out

    def series_of(self, name: str) -> str:
        m = SERIES_RE.match(name)
        if not m:
            return ""
        s = self.series[m.group(1).lower()]
        law = f" (law {s['law'][0]}..{s['law'][1]}: {'ok' if s['law_ok'] else 'VIOLATED'})" if s["law"] else ""
        gaps = "" if s["contiguous"] else " (non-contiguous)"
        return f"{m.group(1).lower()}{s['lo']}..{s['hi']} x{s['count']}{gaps}{law}"

    def _stray_class(self, name: str) -> str:
        """'' for an entity texture; otherwise why it is excluded from entity folders (kept in the global map)."""
        digest = self.hash[name]
        twins_elsewhere = self.elsewhere.get(digest, [])
        armor = ARMOR_STRAY_RE.match(name)
        if armor:
            layer = self.repo.paths.tex_root / "models/armor" / f"{armor.group(1).lower()}_layer_{armor.group(2)}.png"
            if layer.exists():
                same = "byte-identical" if sha256_file(layer) == digest else "DIFFERENT bytes"
                return f"armor sheet stray: the shipped armor layer is textures/models/armor/{layer.name} ({same})"
        if name.lower() in GUI_STRAY_NAMES or any(t.startswith("gui/") for t in twins_elsewhere):
            where = ", ".join(twins_elsewhere) if twins_elsewhere else "no twin outside textures/entity"
            return f"GUI/atlas stray ({where})"
        consumers = self.consumers.get(name, [])
        entity_consumers = [c for c in consumers if not c.endswith("ItemRenderer") and c != "OreSpawnClient"]
        if entity_consumers:
            return ""  # drawn on a mob by an entity renderer / descriptor: never a stray, whatever else shares its bytes
        if consumers and all(c.endswith("ItemRenderer") for c in consumers):
            return "item-renderer texture (a held weapon/tool model), not a mob"
        if twins_elsewhere:
            return "dormant twin of " + ", ".join(twins_elsewhere)
        return ""

    def twins_elsewhere(self, name: str) -> list[str]:
        return list(self.elsewhere.get(self.hash[name], []))

    def entity_folders_for(self, name: str) -> list[str]:
        """Registry folders that receive this texture (by the consumer scan of the species' client files, any twin)."""
        digest = self.hash[name]
        out = []
        for registry, names in self.entity_textures.items():
            if any(self.hash.get(n) == digest for n in names):
                out.append(registry)
        return sorted(out)

    def rows(self) -> tuple[list[str], list[list[Any]]]:
        header = ["file", "sha256_12", "canonical_id", "canonical_name", "is_canonical", "twins", "twins_elsewhere",
                  "width", "height", "series", "stray_class", "consumers", "entity_folders", "provenance_1_7_10",
                  "fan_out_targets"]
        rows: list[list[Any]] = []
        for n in self.names:
            digest = self.hash[n]
            members = self.groups[digest]
            canonical = self.canonical[digest]
            twins = [m for m in members if m != n]
            w, h = self.size[n]
            fan_out = [m for m in members if m != canonical]
            rows.append([
                n, digest[:12], "img_" + digest[:12], canonical, "yes" if n == canonical else "no",
                ";".join(twins), ";".join(self.twins_elsewhere(n)), w, h, self.series_of(n), self.stray[n],
                ";".join(self.consumers.get(n, [])), ";".join(self.entity_folders_for(n)),
                self.repo.provenance.get(n.casefold(), ""), ";".join(fan_out) if n == canonical else "",
            ])
        return header, rows

    def summary(self) -> dict[str, Any]:
        return {
            "shipped": len(self.names),
            "unique_payloads": len(self.groups),
            "duplicate_groups": sum(1 for g in self.groups.values() if len(g) > 1),
            "redundant_names": sum(len(g) - 1 for g in self.groups.values() if len(g) > 1),
            "referenced_by_java": sum(1 for n in self.names if self.consumers.get(n)),
            "strays": sum(1 for n in self.names if self.stray[n]),
            "law_series": {k: v["law_ok"] for k, v in self.series.items() if v["law"]},
        }

    def canonical_for_species(self, species: "Species") -> list[dict[str, Any]]:
        """The species' textures deduped to canonical images: [{canonical, aliases, width, height, stray}]."""
        seen: dict[str, dict[str, Any]] = OrderedDict()
        for n in species.textures:
            if n not in self.hash:
                self.repo.warnings.add(species.registry, "TEXTURE_UNMAPPED", f"referenced texture {n} is not a shipped textures/entity png")
                continue
            digest = self.hash[n]
            canonical = self.canonical[digest]
            entry = seen.setdefault(canonical, {"canonical": canonical, "aliases": [], "referenced_as": [],
                                                "width": self.size[canonical][0], "height": self.size[canonical][1],
                                                "stray": self.stray[canonical], "sha256": digest})
            entry["referenced_as"].append(n)
            entry["aliases"] = [m for m in self.groups[digest] if m != canonical]
        return list(seen.values())


def write_texture_map(repo: "Repo", out_dir: Path, catalog: TextureCatalog | None = None) -> tuple[Path, dict[str, Any]]:
    catalog = catalog or TextureCatalog(repo)
    header, rows = catalog.rows()
    path = out_dir / "TEXTURE_MAP.csv"
    csv_write(path, header, rows)
    return path, catalog.summary()


# ---------------------------------------------------------------------------
# the trigger inventory: AI goals, synched flags, combat sites and events, read from the entity's Java
# ---------------------------------------------------------------------------

# goal class -> (category, plain English, the contract clip / state it bears on)
GOAL_ROLES: dict[str, tuple[str, str, str]] = {
    "FloatGoal": ("locomotion", "bobs up to the surface in water (vanilla)", "swim state (client reads isInWater)"),
    "WaterAvoidingRandomStrollGoal": ("locomotion", "strolls to random spots, avoiding water (vanilla)", "walk"),
    "RandomStrollGoal": ("locomotion", "strolls to random spots (vanilla)", "walk"),
    "MyEntityAIWanderALot": ("locomotion", "OreSpawn's restless wander: picks a new spot often", "walk"),
    "MoveIndoorsGoal": ("locomotion", "heads indoors at night", "walk"),
    "BreedGoal": ("social", "walks to a mate and breeds (vanilla)", "walk"),
    "AvoidEntityGoal": ("flee", "runs away from a class of entities (vanilla)", "walk (fast)"),
    "PanicGoal": ("flee", "runs in a panic after taking damage (vanilla)", "walk (fast)"),
    "LookAtPlayerGoal": ("look", "turns the head toward a nearby player (vanilla; head yaw/pitch only)", "none: head look, not a clip"),
    "RandomLookAroundGoal": ("look", "looks around idly (vanilla; head yaw/pitch only)", "none: head look, not a clip"),
    "HurtByTargetGoal": ("targeting", "retaliates against whoever hurt it (vanilla target selector)", "aggro state (through the attacking flag where one exists)"),
    "RevengeGoal": ("targeting", "the species' own revenge target goal (an inner class)", "aggro state (through the attacking flag where one exists)"),
    "NearestAttackableTargetGoal": ("targeting", "hunts the nearest attackable target (vanilla)", "aggro state"),
    "MyEntityAINearestAttackableTargetGoal": ("targeting", "OreSpawn's nearest-attackable-target hunt", "aggro state"),
    "JealousyTargetGoal": ("targeting", "OreSpawn jealousy targeting", "aggro state"),
    "MeleeAttackGoal": ("attack", "closes in and strikes (vanilla melee)", "attack (event)"),
    "BugMeleeAttackGoal": ("attack", "OreSpawn bug melee", "attack (event)"),
    "DinosaurMeleeAttackGoal": ("attack", "OreSpawn dinosaur melee", "attack (event)"),
    "OwnerFollowAnyNavGoal": ("social", "follows its owner", "walk"),
    "OwnerHurtByTargetGoal": ("targeting", "defends its owner: targets whoever hurt the owner (vanilla tamed-pet goal)", "aggro state"),
    "OwnerHurtTargetGoal": ("targeting", "defends its owner: targets whatever the owner attacks (vanilla tamed-pet goal)", "aggro state"),
    "AmbientFlightGoal": ("locomotion", "ambient flight preset", "fly"),
    "QueenMoodGoal": ("boss", "fires once when attackLevel crosses 1000: happy = terraforms flowers/grass and spawns butterflies; mad = a cloud of PurplePower bombs (particles/spawns, no body motion)", "none (no clip; extra by SPEC if wanted)"),
    "QueenPrimaryGoal": ("boss", "flight pathing (follows the King when happy), target acquisition, the four contact attacks (bite / tail_left / tail_right / roar) and the ranged streams", "extras: bite, tail_whip_left, tail_whip_right, roar (native triggerable clips)"),
}

METHOD_HEADER_RE = re.compile(r"^\s*(?:public|protected|private)?\s*(?:static\s+)?(?:final\s+)?[\w<>\[\], .?]+\s+(\w+)\s*\([^;{]*\)\s*(?:throws [\w., ]+)?\s*\{", re.M)
JAVA_KEYWORDS = {"if", "else", "for", "while", "switch", "catch", "try", "synchronized", "return", "new", "do"}


def extract_method(text: str, name: str) -> tuple[str, int] | None:
    """Body text and start line of the first method called `name` (brace-matched)."""
    m = re.search(rf"\b{re.escape(name)}\s*\([^;{{]*\)\s*(?:throws [\w., ]+)?\s*\{{", text)
    if not m:
        return None
    depth, i = 0, m.end() - 1
    while i < len(text):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return text[m.end():i], text[: m.start()].count("\n") + 1
        i += 1
    return None


def enclosing_method(text: str, offset: int) -> str:
    last = "?"
    for m in METHOD_HEADER_RE.finditer(text):
        if m.start() > offset:
            break
        if m.group(1) in JAVA_KEYWORDS:
            continue
        last = m.group(1)
    return last


def line_of(text: str, offset: int) -> int:
    return text[:offset].count("\n") + 1


# --- Java block structure: comments/strings blanked, the enclosing block found by brace depth -----------------

def strip_java_noise(text: str) -> str:
    """The text with every comment body and string / char literal replaced by spaces (newlines kept), so that
    brace and parenthesis scans see code only. Offsets and line numbers are unchanged."""
    out = list(text)
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        if c == "/" and i + 1 < n and text[i + 1] == "/":
            j = text.find("\n", i)
            j = n if j < 0 else j
            for k in range(i, j):
                out[k] = " "
            i = j
        elif c == "/" and i + 1 < n and text[i + 1] == "*":
            j = text.find("*/", i + 2)
            j = n if j < 0 else j + 2
            for k in range(i, j):
                if out[k] != "\n":
                    out[k] = " "
            i = j
        elif c in ("\"", "'"):
            j = i + 1
            while j < n and text[j] != c and text[j] != "\n":
                if text[j] == "\\":
                    j += 1
                j += 1
            for k in range(i + 1, min(j, n)):
                out[k] = " "
            i = j + 1
        else:
            i += 1
    return "".join(out)


def enclosing_open_brace(clean: str, offset: int) -> int:
    """Index of the `{` that opens the innermost block containing `offset` (-1 at file level)."""
    depth = 0
    i = offset - 1
    while i >= 0:
        c = clean[i]
        if c == "}":
            depth += 1
        elif c == "{":
            if depth == 0:
                return i
            depth -= 1
        i -= 1
    return -1


def matching_open_brace(clean: str, close_idx: int) -> int:
    return enclosing_open_brace(clean, close_idx)


def block_header(clean: str, brace: int) -> tuple[str, int]:
    """The statement that owns the `{` at `brace` (back to the previous `;`, `{` or `}` at parenthesis depth 0)
    with its whitespace collapsed, and the index of that boundary character (-1 at the file start)."""
    depth = 0
    i = brace - 1
    while i >= 0:
        c = clean[i]
        if c == ")":
            depth += 1
        elif c == "(":
            if depth == 0:
                break  # an unclosed `(`: the block is a lambda / argument body; its header starts here
            depth -= 1
        elif depth == 0 and c in ";{}":
            break
        i -= 1
    return " ".join(clean[i + 1:brace].split()), i


def braceless_guard(clean: str, offset: int) -> tuple[str, str] | None:
    """A brace-less one-statement guard wrapping the statement at `offset` (`if (c) this.setAttacking(0);`,
    `} else this.x();`): (guard text, kind), or None when the statement stands on its own."""
    depth = 0
    i = offset - 1
    while i >= 0:
        c = clean[i]
        if c == ")":
            depth += 1
        elif c == "(":
            if depth == 0:
                return None
            depth -= 1
        elif depth == 0 and c in ";{}":
            break
        i -= 1
    prefix = " ".join(clean[i + 1:offset].split())
    prefix = re.sub(r"[\w.\s]*$", "", prefix).strip()  # drop the statement's own leading `this.` / `super.` / name
    if not prefix:
        return None
    m = IF_HEADER_RE.match(prefix)
    if m and not m.group(1):
        return f"if ({' '.join(m.group(2).split())})", "if"
    if prefix.startswith("else"):
        rest = prefix[4:].strip()
        prev = "(unparsed else)"
        if i >= 0 and clean[i] == "}":
            prev_open = matching_open_brace(clean, i)
            g, k = guard_of_block(clean, prev_open) if prev_open >= 0 else ("", "unparsed")
            if k in ("if", "else-if"):
                parts = g.split(" AND ")
                prev = " AND ".join(parts[:-1] + [f"NOT({parts[-1]})"])
        elif i >= 0 and clean[i] == ";":
            j = i - 1
            d2 = 0
            while j >= 0:
                if clean[j] == ")":
                    d2 += 1
                elif clean[j] == "(":
                    d2 -= 1
                elif d2 == 0 and clean[j] in ";{}":
                    break
                j -= 1
            stmt = " ".join(clean[j + 1:i].split())
            im = re.match(r"^if\s*\((.*?)\)\s*\S", stmt, re.S)
            if im:
                prev = f"NOT(if ({' '.join(im.group(1).split())}))"
        if not rest:
            return prev, "else"
        m2 = IF_HEADER_RE.match(rest)
        if m2:
            return f"{prev} AND else if ({' '.join(m2.group(2).split())})", "else-if"
    return f"(unparsed: {prefix[:80]})", "unparsed"


IF_HEADER_RE = re.compile(r"^(else\s+)?if\s*\((.*)\)$", re.S)
LOOP_HEADER_RE = re.compile(r"^(for|while|switch|synchronized|try|do|catch|finally)\b")
CASE_HEADER_RE = re.compile(r"^(case\b.*->|default\s*->)$", re.S)
METHOD_HEADER_FULL_RE = re.compile(r"^(?:@\w+(?:\([^)]*\))?\s+)*(?:(?:public|protected|private|static|final|synchronized|abstract|default)\s+)*"
                                   r"[\w<>\[\], .?]+\s+(\w+)\s*\(.*\)\s*(?:throws\s+[\w., ]+)?$", re.S)


def guard_of_block(clean: str, brace: int) -> tuple[str, str]:
    """(guard text, kind) for the block opened by the `{` at `brace`. Kinds: if / else-if / else / loop / case /
    method / class / unparsed. An `else` branch is rendered as the negation of the `if` chain it closes:
    `NOT(if (A))`, `NOT(if (A)) AND else if (B)`, `NOT(if (A)) AND NOT(else if (B))`."""
    if brace < 0:
        return "(file level)", "file"
    header, boundary = block_header(clean, brace)
    if header.startswith("else"):
        rest = header[4:].strip()
        chain = "(unparsed else)"
        if boundary >= 0 and clean[boundary] == "}":
            prev_open = matching_open_brace(clean, boundary)
            prev_guard, prev_kind = guard_of_block(clean, prev_open) if prev_open >= 0 else ("", "unparsed")
            if prev_kind in ("if", "else-if"):
                parts = prev_guard.split(" AND ")
                negated = parts[:-1] + [f"NOT({parts[-1]})"]
                chain = " AND ".join(negated)
        if not rest:
            return chain, "else"
        m = IF_HEADER_RE.match(rest)
        if m:
            return f"{chain} AND else if ({' '.join(m.group(2).split())})", "else-if"
        return f"(unparsed: {header[:80]})", "unparsed"
    m = IF_HEADER_RE.match(header)
    if m:
        return f"if ({' '.join(m.group(2).split())})", "if"
    if LOOP_HEADER_RE.match(header):
        return header, "loop"
    if CASE_HEADER_RE.match(header):
        return header, "case"
    if "->" in header:
        return "(unparsed: lambda)", "unparsed"
    if re.search(r"\bnew\s+[\w.<>]+\s*\(", header):
        return "(unparsed: anonymous class)", "unparsed"
    if re.match(r"^(?:(?:public|protected|private|static|final|abstract)\s+)*(?:class|interface|enum|record)\s+\w+", header):
        return f"(class body: {header.split()[-1]})", "class"
    mm = METHOD_HEADER_FULL_RE.match(header)
    if mm and mm.group(1) not in JAVA_KEYWORDS:
        return f"(unguarded: top level of {mm.group(1)})", "method"
    if not header:
        return "(unparsed: no header)", "unparsed"
    return f"(unparsed: {header[:80]})", "unparsed"


def enclosing_guards(clean: str, offset: int) -> list[tuple[str, str]]:
    """The guard chain from the innermost guard containing `offset` outwards (a brace-less one-statement `if` first,
    then the enclosing blocks by brace depth), stopping at the method body."""
    chain: list[tuple[str, str]] = []
    braceless = braceless_guard(clean, offset)
    if braceless:
        chain.append(braceless)
    b = enclosing_open_brace(clean, offset)
    while b >= 0:
        g, k = guard_of_block(clean, b)
        chain.append((g, k))
        if k in ("method", "class", "file"):
            break
        b = enclosing_open_brace(clean, b)
    return chain


def balanced_call_args(clean: str, open_paren: int) -> tuple[str, int] | None:
    """The argument text of the call whose `(` is at `open_paren`, and the index of its `)`."""
    depth = 0
    i = open_paren
    while i < len(clean):
        c = clean[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return clean[open_paren + 1:i], i
        i += 1
    return None


def split_top_level(args: str) -> list[str]:
    """Split call arguments on the commas at parenthesis / angle / brace depth 0."""
    parts, depth, cur = [], 0, []
    for c in args:
        if c in "(<{[":
            depth += 1
        elif c in ")>}]":
            depth -= 1
        if c == "," and depth == 0:
            parts.append("".join(cur))
            cur = []
        else:
            cur.append(c)
    parts.append("".join(cur))
    return [p.strip() for p in parts]


GOAL_NEW_RE = re.compile(r"^new\s+([\w.]+)(?:<[^>]*>)?\s*\((.*)\)\s*(?:\{.*\})?$", re.S)


def parse_goals(text: str, java_class: str) -> list[dict[str, Any]]:
    """Every addGoal registration of registerGoals: selector, priority, goal class, arguments, source line and the
    enclosing guard (`[modern: key]` for an OreSpawnConfig gate). A goal held in a local variable or registered
    with a non-literal priority is reported as UNPARSED (never skipped); a `this.<field>` assigned in the body
    resolves to its class."""
    found = extract_method(text, "registerGoals")
    if not found:
        return []
    body, start_line = found
    clean = strip_java_noise(body)
    fields: dict[str, str] = {}
    for m in re.finditer(r"this\.(\w+)\s*=\s*new\s+([\w.]+)", clean):
        fields[m.group(1)] = m.group(2).split(".")[-1]
    config_locals: dict[str, str] = {}
    for m in re.finditer(r"boolean\s+(\w+)\s*=\s*OreSpawnConfig\.(\w+)\(\)", clean):
        config_locals[m.group(1)] = m.group(2)
    goals: list[dict[str, Any]] = []
    for m in re.finditer(r"this\.(goalSelector|targetSelector)\.addGoal\s*\(", clean):
        selector = m.group(1)
        found_args = balanced_call_args(clean, m.end() - 1)
        if not found_args:
            continue
        raw_args, _ = found_args
        raw_args = body[m.end():m.end() + len(raw_args)]  # the original text (strings intact) for display
        parts = split_top_level(strip_java_noise(raw_args))
        raw_parts = split_top_level(raw_args)
        if len(raw_parts) == len(parts):
            parts = [p if p.strip() else q for p, q in zip(raw_parts, parts)]  # strings intact where the split agrees
        line = start_line + body[: m.start()].count("\n")
        entry: dict[str, Any] = {"selector": selector, "priority": None, "goal": "?", "args": " ".join(raw_args.split()),
                                 "line": line, "source": f"{java_class}.java", "guard": "", "unparsed": ""}
        prio_text = parts[0] if parts else ""
        goal_text = " ".join(",".join(parts[1:]).split()) if len(parts) > 1 else ""
        problems = []
        if re.fullmatch(r"\d+", prio_text):
            entry["priority"] = int(prio_text)
        else:
            problems.append(f"non-literal priority `{prio_text}`")
        gm = GOAL_NEW_RE.match(goal_text)
        if gm:
            entry["goal"] = gm.group(1).split(".")[-1]
            entry["args"] = " ".join(gm.group(2).split())
        elif re.fullmatch(r"this\.(\w+)", goal_text) and goal_text[5:] in fields:
            entry["goal"] = fields[goal_text[5:]]
            entry["args"] = goal_text
        else:
            problems.append(f"goal expression `{goal_text}` is not `new Class(...)` nor a field assigned in registerGoals")
        if problems:
            entry["goal"] = "UNPARSED"
            entry["unparsed"] = "; ".join(problems)
        guards = enclosing_guards(clean, m.start())
        conds = [g for g, k in guards if k in ("if", "else-if", "else", "loop", "case", "unparsed")]
        if conds:
            texts = []
            for g in conds:
                cm = re.search(r"OreSpawnConfig\.(\w+)\(\)", g)
                if cm:
                    texts.append(f"[modern: {cm.group(1)}]")
                    continue
                lm = re.fullmatch(r"if \((\w+)\)", g)
                if lm and lm.group(1) in config_locals:
                    texts.append(f"[modern: {config_locals[lm.group(1)]}]")
                    continue
                texts.append(f"[guard: {g}]")
            entry["guard"] = " ".join(texts)
        goals.append(entry)
    return goals


def classify_goal(goal: str) -> tuple[str, str, str]:
    if goal in GOAL_ROLES:
        return GOAL_ROLES[goal]
    if goal == "UNPARSED":
        return ("UNPARSED", "a registration shape the generator does not read (a local variable or a computed priority): the owner reads the line", "unknown")
    return ("UNCLASSIFIED", "no entry in the generator's goal dictionary", "unknown")


def parse_synched_data(text: str) -> list[dict[str, Any]]:
    out = []
    for m in re.finditer(r"EntityDataAccessor<(\w+)>\s+(\w+)\s*=", text):
        out.append({"name": m.group(2), "type": m.group(1), "line": line_of(text, m.start())})
    return out


def attacking_sites(text: str) -> list[dict[str, Any]]:
    """Every setAttacking(N) call with its method, the ACTUAL enclosing block's guard (found by brace depth over the
    comment-stripped source; an `else` branch is the negated `if` chain), the outer guard chain (`within`), and the
    trailing comment. A block whose header the parser cannot read is reported as `(unparsed: ...)`, never dropped."""
    lines = text.splitlines()
    clean = strip_java_noise(text)
    out = []
    for m in re.finditer(r"setAttacking\(\s*(\d+)\s*\)\s*;", clean):
        ln = line_of(clean, m.start())
        chain = enclosing_guards(clean, m.start())
        guard, kind = chain[0] if chain else ("(unparsed: no enclosing block)", "unparsed")
        if kind == "method":
            guard = ""  # the method body itself: an unguarded write, shown as such
        within = [g for g, k in chain[1:] if k not in ("method", "class", "file")]
        comment = ""
        cm = re.search(r"//\s*(.*)$", lines[ln - 1])
        if cm:
            comment = cm.group(1).strip()
        out.append({"value": int(m.group(1)), "line": ln, "method": enclosing_method(text, m.start()),
                    "guard": guard, "guard_kind": kind, "within": within, "comment": comment,
                    "code": strip_java_noise(lines[ln - 1]).strip() or lines[ln - 1].strip()})
    return out


PULSE_GUARD_RE = re.compile(r"[Tt]icker|reload|cooldown|[Tt]icks\s*[<>]|<\s*\d+\s*\)", re.I)
STATE_GUARD_RE = re.compile(r"target\s*==\s*null|getTarget\(\)\s*==\s*null|!=\s*null|dist|range|canSee|isAlive|PLAY_NICELY", re.I)


def ticker_facts(text: str, sites: list[dict[str, Any]]) -> dict[str, Any] | None:
    """For a reload-ticker species: the ticker's reset value and the clear threshold, so the pulse can be stated
    as `a (reset - threshold)-tick pulse per think tick (every reset ticks)`; None when the pattern is absent."""
    clean = strip_java_noise(text)
    reset = re.search(r"this\.(\w*[Tt]icker)\s*=\s*(\d+)\s*;", clean)
    if not reset:
        return None
    name, value = reset.group(1), int(reset.group(2))
    threshold = None
    for s in sites:
        if s["value"] == 0:
            tm = re.search(rf"{re.escape(name)}\s*<\s*(\d+)", s["guard"])
            if tm:
                threshold = int(tm.group(1))
                break
    if threshold is None:
        return None
    return {"ticker": name, "reset": value, "threshold": threshold, "pulse_ticks": value - threshold}


def classify_attacking(sites: list[dict[str, Any]], text: str = "") -> tuple[str, str, dict[str, Any]]:
    """STATE (held while engaged) / EVENT (pulsed at a strike) / MIXED / UNCLASSIFIED, the reason, and the facts the
    verdict rests on (pulse / held / other clears, the hurt() sets, the ticker). Heuristic: a mechanical reading — the owner
    confirms it against the sites (no ruling is pending behind it)."""
    facts: dict[str, Any] = {"pulse": [], "held": [], "other": [], "unguarded": [], "hurt_sets": [], "ticker": None, "caveats": []}
    if not sites:
        return "NONE", "no setAttacking sites", facts
    clears = [s for s in sites if s["value"] == 0]
    sets = [s for s in sites if s["value"] != 0]
    pulse = [s for s in clears if PULSE_GUARD_RE.search(s["guard"]) or PULSE_GUARD_RE.search(s["code"])]
    held = [s for s in clears if STATE_GUARD_RE.search(s["guard"]) and s not in pulse]
    unguarded = [s for s in clears if not s["guard"]]
    other = [s for s in clears if s not in pulse and s not in held and s not in unguarded]
    facts.update({"pulse": [s["line"] for s in pulse], "held": [s["line"] for s in held], "other": [s["line"] for s in other],
                  "unguarded": [s["line"] for s in unguarded], "hurt_sets": [s["line"] for s in sets if s["method"] == "hurt"],
                  "ticker": ticker_facts(text, sites) if text else None})
    if facts["hurt_sets"]:
        facts["caveats"].append(f"the flag is also RAISED in hurt() at line(s) {facts['hurt_sets']}: a client-observed rising edge would fire "
                                f"`attack` when the mob is HIT, not only when it strikes — transport 2 needs that edge masked (e.g. by hurtTime)")
    if other:
        facts["caveats"].append("other clears at line(s) " + str([s["line"] for s in other]) + " (guards: "
                                + "; ".join(s["guard"] for s in other) + ") — the owner reads them")
    tk = facts["ticker"]
    pulse_text = ""
    if tk and pulse:
        set_lines = [s["line"] for s in sets if s["method"] != "hurt"]
        pulse_text = (f"a {tk['pulse_ticks']}-tick pulse per in-range think tick: the flag is raised at line(s) {set_lines} every "
                      f"{tk['reset']} ticks ({tk['ticker']} reset) while a target is in range — before the line-of-sight gate, so it pulses "
                      f"whether or not a shot fires — and cleared when {tk['ticker']} < {tk['threshold']} at line(s) {facts['pulse']}")
    elif pulse:
        pulse_text = f"cleared by a ticker/reload guard at line(s) {facts['pulse']}"
    suffix = ("; " + "; ".join(facts["caveats"])) if facts["caveats"] else ""
    if pulse and not held:
        return "EVENT", pulse_text + suffix, facts
    if held and not pulse:
        return "STATE", f"cleared when the target is lost at line(s) {facts['held']}" + suffix, facts
    if pulse and held:
        return "MIXED", pulse_text + f" AND cleared on target loss at line(s) {facts['held']}" + suffix, facts
    if unguarded and not pulse and not held:
        return "STATE?", f"cleared unconditionally at line(s) {facts['unguarded']} (the enclosing branch decides; read the source)" + suffix, facts
    return "UNCLASSIFIED", "the clear sites' guards match neither the pulse nor the target-loss pattern" + suffix, facts


AREA_HELPERS = ("doAreaDamage",)  # the mod's own area-damage helpers: one call hurts every victim in a box


def combat_sites(files: list[tuple[str, str]]) -> dict[str, list[dict[str, Any]]]:
    """melee strike calls (doHurtTarget and the mod's area-damage helpers), projectile launches and native GeckoLib
    trigger calls, over (label, text) pairs. Definitions are skipped; a trigger call with a variable key lists the
    literals that variable is assigned in the enclosing method."""
    out: dict[str, list[dict[str, Any]]] = {"melee": [], "ranged": [], "native_triggers": []}
    for label, text in files:
        clean = strip_java_noise(text)
        lines = text.splitlines()
        helper_bodies = {name: extract_method(clean, name) for name in AREA_HELPERS}
        for m in re.finditer(r"\b(doHurtTarget|" + "|".join(AREA_HELPERS) + r")\s*\(", clean):
            before = clean[max(0, m.start() - 40):m.start()]
            if re.search(r"\b(void|boolean|int|double|float|long)\s+$", before):
                continue  # the method's own definition
            args = balanced_call_args(clean, m.end() - 1)
            arg_text = " ".join(text[m.end():m.end() + len(args[0])].split()) if args else "..."
            name = m.group(1)
            site = {"file": label, "line": line_of(clean, m.start()), "method": enclosing_method(text, m.start()),
                    "code": f"{name}({arg_text})", "kind": "area" if name in AREA_HELPERS else "single",
                    "guards": [g for g, k in enclosing_guards(clean, m.start()) if k not in ("method", "class", "file")]}
            if name in AREA_HELPERS and helper_bodies.get(name):
                body, start = helper_bodies[name]
                hurts = [start + body[: h.start()].count("\n") for h in re.finditer(r"\.hurt\s*\(", body)]
                site["hurts_per_victim"] = len(hurts)
                site["hurt_lines"] = hurts
                site["helper_line"] = start
            out["melee"].append(site)
        for m in re.finditer(r"new\s+(\w*(?:Ball|Acid|Arrow|Bolt|Rock|Fireball|Urchin|Shot)\w*)\s*\(", clean):
            out["ranged"].append({"file": label, "line": line_of(clean, m.start()), "method": enclosing_method(text, m.start()), "code": m.group(1)})
        for m in re.finditer(r"\b(trigger\w*Anim\w*|trigger\w+Action)\(\s*(\"[^\"]*\"|\w+)", text):
            line_text = lines[line_of(text, m.start()) - 1].strip()
            if line_text.startswith(("*", "//", "/*")) or re.match(r"(public|protected|private)\s", line_text):
                continue  # a javadoc mention or the method's own definition, not a call site
            arg = m.group(2)
            site = {"file": label, "line": line_of(text, m.start()), "method": enclosing_method(text, m.start()), "code": m.group(0),
                    "helper": m.group(1), "key": arg.strip("\"") if arg.startswith("\"") else None, "variable": None if arg.startswith("\"") else arg,
                    "guards": [g for g, k in enclosing_guards(clean, m.start()) if k not in ("method", "class", "file")]}
            if site["variable"]:
                # the literals the variable is assigned within the enclosing method (a `switch` pick, typically)
                meth = extract_method(text, site["method"]) if site["method"] != "?" else None
                scope = meth[0] if meth else text
                site["keys"] = sorted(set(re.findall(rf"\b{re.escape(arg)}\s*=\s*\"(\w+)\"", scope)))
            out["native_triggers"].append(site)
    return out


def overrides(text: str) -> list[str]:
    names = ["hurt", "die", "doHurtTarget", "aiStep", "tick", "customServerAiStep", "swing", "isPushable", "removeWhenFarAway"]
    return [n for n in names if re.search(rf"@Override\s+(?:public|protected)\s+[\w<>]+\s+{n}\s*\(", text)]


def locomotion_facts(text: str) -> dict[str, Any]:
    ext = re.search(r"class\s+\w+\s+extends\s+(\w+)", text)
    impl = re.search(r"implements\s+([\w<>, .]+?)\s*\{", text)
    return {
        "extends": ext.group(1) if ext else "?",
        "implements": [s.strip() for s in impl.group(1).split(",")] if impl else [],
        "no_physics": bool(re.search(r"this\.noPhysics\s*=\s*true", text)),
        "stationary": bool(re.search(r"Attributes\.MOVEMENT_SPEED,\s*0\.0\)", text)),
        "flying_move_control": bool(re.search(r"FlyingMoveControl|FlyingPathNavigation", text)),
        "rideable": "RideableFlyer" in text or "tickRidden" in text,
        "geo_entity": "implements GeoEntity" in text or ", GeoEntity" in text,
        "render_info": "RenderInfo" in text,
        "baby_form": "isBaby()" in text or "getBreedOffspring" in text,
    }


def native_controllers(text: str) -> dict[str, Any]:
    """GeckoLib-native species: the controllers, their triggerable keys (with the owning controller) and the
    state clips each controller's predicate selects (the `if (...)` guarding each `setAndContinue`, or "default"
    when no earlier branch took it), from registerControllers and the RawAnimation constants."""
    found = extract_method(text, "registerControllers")
    if not found:
        return {}
    body, start = found
    constants: dict[str, str] = {}
    for m in re.finditer(r"(\w+)\s*=\s*RawAnimation\.begin\(\)\.then(?:Loop|Play)\(\s*\"(\w+)\"", text):
        constants[m.group(1)] = m.group(2)
    controllers = []
    spans: list[tuple[int, int, str]] = []
    heads = list(re.finditer(r"new AnimationController<>\(\s*this\s*,\s*\"(\w+)\"\s*,\s*(\d+)", body))
    for i, m in enumerate(heads):
        end = heads[i + 1].start() if i + 1 < len(heads) else len(body)
        controllers.append({"name": m.group(1), "transition_ticks": int(m.group(2)), "line": start + body[: m.start()].count("\n")})
        spans.append((m.start(), end, m.group(1)))

    def controller_at(offset: int) -> str:
        for a, b, name in spans:
            if a <= offset < b:
                return name
        return "?"

    triggerable = [{"key": m.group(1), "clip": m.group(2), "line": start + body[: m.start()].count("\n"), "controller": controller_at(m.start())}
                   for m in re.finditer(r"triggerableAnim\(\s*\"(\w+)\"\s*,\s*RawAnimation\.begin\(\)\.then(?:Play|Loop)\(\s*\"(\w+)\"", body)]
    state_clips: dict[str, dict[str, Any]] = {}
    clean = strip_java_noise(body)
    for m in re.finditer(r"setAndContinue\(\s*(?:(\w+)|RawAnimation\.begin\(\)\.then(?:Loop|Play)\(\s*\"(\w+)\"\s*\))\s*\)", body):
        clip = constants.get(m.group(1), m.group(1)) if m.group(1) else m.group(2)
        guards = [g for g, k in enclosing_guards(clean, m.start()) if k in ("if", "else-if", "else", "loop", "case")]
        state_clips.setdefault(clip, {"controller": controller_at(m.start()), "condition": guards[0] if guards else "default (no earlier branch took it)",
                                      "line": start + body[: m.start()].count("\n")})
    loops = re.findall(r"thenLoop\(\s*\"(\w+)\"", text)
    plays = re.findall(r"thenPlay\(\s*\"(\w+)\"", text)
    conditions = [l.strip() for l in body.splitlines() if re.search(r"\bif \(|return state\.setAndContinue|PlayState\.", l)]
    stops = [{"controller": controller_at(m.start()), "condition": next((g for g, k in enclosing_guards(clean, m.start()) if k in ("if", "else-if", "else")), "always")}
             for m in re.finditer(r"PlayState\.STOP", body)]
    return {"controllers": controllers, "triggerable": triggerable, "loop_clips": sorted(set(loops)),
            "play_clips": sorted(set(plays)), "conditions": conditions, "state_clips": state_clips, "stops": stops}


def build_trigger_inventory(species: "Species", repo: "Repo") -> dict[str, Any]:
    """The mechanically generated trigger inventory (addendum A.3): states, goals, events and what fires each contract clip."""
    inv: dict[str, Any] = {"entity_file": None, "goals": [], "flags": [], "attacking": None, "combat": {},
                           "overrides": [], "locomotion": {}, "native": {}, "drives": [], "warnings": []}
    if not species.entity_file:
        repo.warnings.add(species.registry, "ENTITY_SOURCE_MISSING", f"no entity source for {species.java_class}")
        return inv
    text = read_text(species.entity_file)
    inv["entity_file"] = str(species.entity_file.relative_to(repo.paths.root)).replace("\\", "/")
    inv["goals"] = parse_goals(text, species.java_class)
    for g in inv["goals"]:
        g["category"], g["plain"], g["drives"] = classify_goal(g["goal"])
        if g["category"] == "UNCLASSIFIED" and g["goal"] != "UNPARSED":
            repo.warnings.add(species.registry, "GOAL_UNCLASSIFIED", f"goal {g['goal']} ({g['source']}:{g['line']}) has no dictionary entry")
    for g in inv["goals"]:
        if g["goal"] == "UNPARSED":
            repo.warnings.add(species.registry, "GOAL_UNPARSED", f"addGoal at {g['source']}:{g['line']} not parsed: {g['unparsed']}")
    inv["flags"] = parse_synched_data(text)
    sites = attacking_sites(text)
    verdict, reason, facts = classify_attacking(sites, text)
    inv["attacking"] = {"present": any(f["name"] == "DATA_ATTACKING" for f in inv["flags"]), "sites": sites,
                        "verdict": verdict, "reason": reason, "facts": facts}
    if inv["attacking"]["present"] and verdict in ("UNCLASSIFIED", "MIXED", "STATE?"):
        repo.warnings.add(species.registry, "ATTACKING_UNCLASSIFIED", f"DATA_ATTACKING classified {verdict}: {reason}")
    for s in sites:
        if s["guard_kind"] == "unparsed":
            repo.warnings.add(species.registry, "ATTACKING_GUARD_UNPARSED", f"setAttacking at line {s['line']}: {s['guard']}")
    goal_files: list[tuple[str, str]] = [(f"{species.java_class}.java", text)]
    for g in inv["goals"]:
        p = repo.paths.ai_dir / f"{g['goal']}.java"
        if p.exists():
            goal_files.append((f"ai/{p.name}", read_text(p)))
    inv["combat"] = combat_sites(goal_files)
    inv["overrides"] = overrides(text)
    inv["locomotion"] = locomotion_facts(text)
    inv["native"] = native_controllers(text) if inv["locomotion"]["geo_entity"] else {}
    if inv["native"]:
        inv["native"]["clip_triggers"] = native_clip_triggers(species, inv)
    inv["drives"] = contract_drives(species, inv)
    return inv


def native_clip_triggers(species: "Species", inv: dict[str, Any]) -> dict[str, dict[str, Any]]:
    """For a GeckoLib-native species: clip name -> {kind, controller, signal} read from registerControllers and the
    trigger call sites (a literal key, or a variable resolved to the literals it is assigned). The seed's per-clip
    `trigger` text, when present, overrides the mechanical sentence (an authored correction, marked as such)."""
    nat = inv.get("native") or {}
    seed_clips = {c["name"]: c for c in (species.seed or {}).get("clips", [])}
    by_key = {t["key"]: t for t in nat.get("triggerable", [])}
    calls: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for s in (inv.get("combat") or {}).get("native_triggers", []):
        if s["helper"] == "triggerableAnim":
            continue
        keys = [s["key"]] if s.get("key") else s.get("keys", [])
        for k in keys:
            if k in by_key:
                calls[by_key[k]["clip"]].append(s)
    out: dict[str, dict[str, Any]] = {}
    for name in species.animation.get("animations", {}):
        entry: dict[str, Any] = {"kind": "unreferenced", "controller": "", "signal": "unreferenced by the controllers (no trigger, no state selects it)"}
        trig = next((t for t in nat.get("triggerable", []) if t["clip"] == name), None)
        state = nat.get("state_clips", {}).get(name)
        if trig:
            sites = calls.get(name, [])
            where = "; ".join(f"{s['file']}:{s['line']} in `{s['method']}`" + (f" (picked among {', '.join(s['keys'])})" if s.get('keys') else "")
                              + (f", guard: {' within '.join(s['guards'][:3])}" if s.get("guards") else "") for s in sites) or "no call site found"
            entry = {"kind": "triggered", "controller": trig["controller"],
                     "signal": f"code: `triggerAnim(\"{trig['controller']}\", \"{trig['key']}\")` — server-side call at {where}"}
        elif state:
            entry = {"kind": "state", "controller": state["controller"],
                     "signal": f"state: the `{state['controller']}` controller selects it when {state['condition']} (registerControllers line {state['line']})"}
        if seed_clips.get(name, {}).get("trigger"):
            entry["signal"] = seed_clips[name]["trigger"] + " (AUTHORED override of the mechanical reading: " + entry["signal"] + ")"
        out[name] = entry
    return out


def melee_transport_note(inv: dict[str, Any]) -> str:
    """Transport 1 (LivingDamageEvent.Post per victim) stated for the strike sites found: an area helper multiplies it."""
    melee = (inv.get("combat") or {}).get("melee", [])
    area = [s for s in melee if s.get("kind") == "area"]
    base = "melee: LivingDamageEvent.Post -> triggerAnim packet (transport 1); ranged: named launch sites (transport 3)"
    if not area:
        return base
    s = area[0]
    n = s.get("hurts_per_victim") or 1
    return (base + f" — CAVEAT: the melee here is an AREA helper (`{s['code'].split('(')[0]}(...)` at line(s) "
            f"{[a['line'] for a in area]}) whose body hurts EACH victim {n}x per roll (line(s) {s.get('hurt_lines', [])}): a LivingDamageEvent.Post per "
            f"victim would fire `attack` per victim per roll ({n}x each) — transport 1 needs a per-roll dedupe, or a named-site trigger (transport 3)")


def contract_drives(species: "Species", inv: dict[str, Any]) -> list[dict[str, str]]:
    """What fires each contract clip for this species (contract_design.md §3-4), from the inventory above. A species
    with SHIPPED native clips gets the NATIVE table: its own clips with their real triggers, and the contract's
    generic names marked "not used by this species (native clip set)"."""
    loco = inv["locomotion"]
    seed = species.seed or {}
    kind = seed.get("locomotion") or ("stationary" if loco.get("stationary") else "flyer" if loco.get("no_physics") or loco.get("flying_move_control") else "walker")
    rows: list[dict[str, str]] = []
    native = inv.get("native") or {}
    shipped = list(species.animation.get("animations", {})) if species.landed else []
    if native and shipped:
        triggers = native.get("clip_triggers") or {}
        mapping = {c["name"]: c.get("contract", "") for c in seed.get("clips", [])}
        for name in shipped:
            t = triggers.get(name, {})
            role = mapping.get(name, "")
            verdict = {"triggered": "shipped native clip, code-triggered by name", "state": "shipped native clip, selected by controller state",
                       "unreferenced": "shipped but nothing plays it"}.get(t.get("kind", "unreferenced"), "shipped")
            if role:
                verdict += f"; stands in for the contract's `{role}` (the seed's mapping — ruled 2026-09-06, Q16 (a): the pilot boss keeps its native clip set)"
            rows.append({"clip": name, "signal": t.get("signal", "?"), "verdict": verdict})
        covered = {v for v in mapping.values() if v in CONTRACT_CLIP_NAMES}
        for generic in CONTRACT_CLIP_NAMES + ("idle_alt_N",):
            if generic in shipped:
                continue
            note = "not used by this species (native clip set)"
            if generic in covered:
                stand_in = next(n for n, v in mapping.items() if v == generic)
                note += f" — its role is carried by native `{stand_in}`"
            elif generic == "hurt":
                note += "; vanilla's red overlay is the only cue (ruled 2026-09-06, Q4 (a): the overlay stays); adding a hurt clip needs a controller change and a ruling"
            elif generic == "death":
                note += "; the native `death` is code-triggered from die(), not the client-observed deathTime"
            elif generic == "idle_alt_N":
                note += "; the native controllers roll no idle variants, so `check` does not accept idle_alt_N here"
            rows.append({"clip": generic, "signal": "—", "verdict": note})
        return rows
    if kind == "stationary":
        rows.append({"clip": "walk", "signal": "limbSwingAmount stays 0 (MOVEMENT_SPEED 0.0)", "verdict": "never plays: the mob does not walk"})
        rows.append({"clip": "idle", "signal": "w_idle = 1 whenever nothing else is weighted", "verdict": "the only locomotion loop"})
    elif kind == "flyer":
        rows.append({"clip": "fly", "signal": "species declared flyer (noPhysics / flight goals) and !onGround()", "verdict": "airborne loop"})
        rows.append({"clip": "idle", "signal": "w_idle = (1 - w_move)(1 - w_swim)(1 - w_fly)", "verdict": "on the ground or hovering still"})
        rows.append({"clip": "walk", "signal": "limbSwingAmount (vanilla walk animation state)", "verdict": "ground locomotion, if any"})
    else:
        rows.append({"clip": "walk", "signal": "limbSwingAmount from AnimationState (the seam's input; 0 at rest, 1 at full stride)", "verdict": "the gait group's weight and speed scale (P3)"})
        rows.append({"clip": "idle", "signal": "w_idle = (1 - w_move)(1 - w_swim)(1 - w_fly)", "verdict": "standing still"})
    rows.append({"clip": "swim", "signal": "Entity.isInWater() (client-evaluated)" + ("; FloatGoal keeps it at the surface" if any(g["goal"] == "FloatGoal" for g in inv["goals"]) else ""), "verdict": "falls back to walk then idle when absent (§2.4)"})
    att = inv.get("attacking") or {}
    facts = att.get("facts") or {}
    if att.get("present"):
        v = att["verdict"]
        if v == "STATE":
            rows.append({"clip": "aggro_idle / calm_idle", "signal": f"DATA_ATTACKING held while engaged ({att['reason']})", "verdict": "STATE flag drives w_aggro (§4.3)"})
            rows.append({"clip": "attack", "signal": melee_transport_note(inv),
                         "verdict": "the transport per species by the trigger inventory (ruled 2026-09-06, Q11 (a)): this flag is held, not pulsed at the strike, "
                                    "so the signal column's transport applies — the server LivingDamageEvent.Post -> triggerAnim packet for melee, a named launch site for ranged"})
        elif v == "EVENT":
            caveat = ""
            if facts.get("hurt_sets"):
                caveat = (f" — CAVEAT: hurt() also raises the flag at line(s) {facts['hurt_sets']}, so the client-observed edge fires when the mob is HIT "
                          f"as well as when it strikes; transport 2 must mask the hurt edge (e.g. ignore a rising edge while hurtTime > 0) or fall back to transport 1/3")
            rows.append({"clip": "attack", "signal": f"rising edge of DATA_ATTACKING ({att['reason']})",
                         "verdict": "EVENT flag: client-observed edge, transport 2 (§4.4)" + caveat
                                    + " — the transport per species by the trigger inventory (ruled 2026-09-06, Q11 (a)): the client-observed edge where the flag pulses at the strike, this species"})
            rows.append({"clip": "aggro_idle / calm_idle", "signal": "no held state: the flag pulses",
                         "verdict": "calm_idle only until this creature's SPEC adds a synched byte mirroring getTarget() != null (ruled 2026-09-06, Q12 (a): aggro_idle waits for that byte)"})
        else:
            rows.append({"clip": "aggro_idle / calm_idle / attack", "signal": f"DATA_ATTACKING classified {v}: {att['reason']}",
                         "verdict": "OWNER READS THE SITES (listed below) — a mechanical reading; the owner confirms which of the ruled transports applies (ruled 2026-09-06, Q11 (a) / Q12 (a))"})
    else:
        rows.append({"clip": "aggro_idle / calm_idle", "signal": "no synched attacking flag",
                     "verdict": "calm_idle only until this creature's SPEC adds a synched byte mirroring getTarget() != null (ruled 2026-09-06, Q12 (a): aggro_idle waits for that byte)"})
        if inv["combat"].get("melee") or inv["combat"].get("ranged"):
            rows.append({"clip": "attack", "signal": melee_transport_note(inv),
                         "verdict": "the transport per species by the trigger inventory (ruled 2026-09-06, Q11 (a)): no attacking flag here, so the signal column's transport applies — "
                                    "the server LivingDamageEvent.Post -> triggerAnim packet for melee, a named launch site for ranged"})
        else:
            rows.append({"clip": "attack", "signal": "no strike or launch site found", "verdict": "no attack clip: the mob does not attack"})
    rows.append({"clip": "hurt", "signal": "rising edge of LivingEntity.hurtTime (0 -> 10), client-observed", "verdict": "always available; the red overlay stays (ruled 2026-09-06, Q4 (a))"})
    rows.append({"clip": "death", "signal": "LivingEntity.deathTime > 0, client-observed",
                 "verdict": "hold_on_last_frame; the vanilla death flip stays and the pilot ships no death clip (ruled 2026-09-06, Q3 (a)); a creature whose JSON ships a `death` clip "
                            "gets the clip-replaces-flip mode, designed when the first such clip arrives (the Queen's own death clip the precedent) — until then a delivered `death` plays under the flip"})
    rows.append({"clip": "idle_alt_N", "signal": "a roll at each idle loop boundary (p = 0.15) while w_idle > 0.9",
                 "verdict": "optional; one roll per idle loop boundary, p = 0.15, a uniform choice, only while the idle weight is above 0.9 and no triggered clip plays, keyed on the clip-clock cycle index (ruled 2026-09-06, Q5 (a))"})
    return rows


# ---------------------------------------------------------------------------
# the SPEC: bone glossary, locked bones, tempo table, clip table, trigger inventory, manifest
# ---------------------------------------------------------------------------

DEFAULT_LABELS: dict[str, str] = {
    "root": "root (the whole body; the entity's yaw turns it — a key here moves every part with it: allowed and warned like any locked bone, leave it to the yaw)",
    "body": "body (torso)", "head": "head", "nose": "nose", "teeth": "front teeth", "tail": "tail",
    "neck": "neck", "chest": "chest", "torso": "torso", "hips": "hips", "stomach": "stomach",
    "lff": "left front foot", "lrf": "left rear foot", "rff": "right front foot", "rrf": "right rear foot",
    "lfoot": "left foot", "rfoot": "right foot",
    "lleg1": "left leg, upper", "lleg2": "left leg, lower", "rleg1": "right leg, upper", "rleg2": "right leg, lower",
    "larm1": "left arm, upper", "larm2": "left arm, lower", "larm3": "left hand / fist",
    "rarm1": "right arm, upper", "rarm2": "right arm, lower", "rarm3": "right hand / fist",
    "coin": "the coin disc", "lazer": "laser turret (sic: 'lazer')", "ammobox": "ammunition box", "axle": "wheel axle",
    "barrel1": "gun barrel 1", "barrel2": "gun barrel 2", "drivebox": "drive box (the chassis)", "stand": "turret stand",
    "swivel": "turret swivel", "lwheel1": "left wheel 1", "lwheel2": "left wheel 2", "rwheel1": "right wheel 1", "rwheel2": "right wheel 2",
    "key1": "wind-up key 1", "key2": "wind-up key 2", "key3": "wind-up key 3", "key4": "wind-up key 4", "key5": "wind-up key 5",
}


def humanize(name: str) -> str:
    """A readable fallback from a legacy name: leftkneegaurd -> 'left knee gaurd'; LLClaw3 -> 'l l claw 3'."""
    s = re.sub(r"__", " / ", name)
    s = re.sub(r"([a-z])([A-Z])", r"\1 \2", s)
    s = re.sub(r"([A-Za-z])(\d)", r"\1 \2", s)
    for word in ("left", "right", "upper", "lower", "front", "back", "base", "tip", "top", "middle", "piece", "cannon",
                 "shin", "calf", "thigh", "foot", "knee", "spine", "shoulder", "arm", "guard", "shield", "glowy", "bit",
                 "sholder", "gaurd", "sheild", "end", "ammo", "wheel", "leg", "head", "neck", "tail", "wing", "claw", "jaw"):
        s = re.sub(rf"(?<![a-z]){word}(?![a-z])", f" {word} ", s)
    return " ".join(s.lower().split())


def bone_maps(geo: dict[str, Any]) -> tuple[list[dict[str, Any]], dict[str, dict[str, Any]], dict[str, list[str]]]:
    bones = geo["minecraft:geometry"][0]["bones"]
    by_name = {b["name"]: b for b in bones}
    children: dict[str, list[str]] = defaultdict(list)
    for b in bones:
        if b.get("parent"):
            children[b["parent"]].append(b["name"])
    return bones, by_name, children


def ancestors(name: str, by_name: dict[str, dict[str, Any]]) -> list[str]:
    out = []
    cur = by_name.get(name, {}).get("parent")
    while cur:
        out.append(cur)
        cur = by_name.get(cur, {}).get("parent")
    return out


def locked_bones(species: "Species", geo: dict[str, Any]) -> dict[str, str]:
    """bone -> reason; a profile's synched-bones and every ancestor (contract §8.1 / P7)."""
    out: dict[str, str] = {}
    prof = species.profile
    if not prof or not prof.get("synched-bones"):
        return out
    bones, by_name, _ = bone_maps(geo)
    parts = {p["name"]: p for p in prof.get("parts", [])}
    for name in prof["synched-bones"]:
        if name not in by_name:
            continue
        part = parts.get(name, {})
        out[name] = f"carries hitbox part '{name}' (size {part.get('box', {}).get('size')}, damage x{part.get('damage-modifier')}) — synched-bones, {species.profile_name}.json"
        for anc in ancestors(name, by_name):
            out.setdefault(anc, f"ancestor of the synced part bone '{name}'")
    return out


def shipped_keyed_bones(anim: dict[str, Any]) -> set[str]:
    keyed: set[str] = set()
    for clip in anim.get("animations", {}).values():
        keyed.update(clip.get("bones", {}).keys())
    return keyed


def cube_hint(bone: dict[str, Any]) -> str:
    cubes = bone.get("cubes", [])
    if not cubes:
        return "no cubes (a pivot-only joint)"
    c = cubes[0]
    size = c.get("size", [0, 0, 0])
    org = c.get("origin", [0, 0, 0])
    more = f" (+{len(cubes) - 1} more)" if len(cubes) > 1 else ""
    return f"cube {fmt(size[0])}x{fmt(size[1])}x{fmt(size[2])} at ({fmt(org[0])}, {fmt(org[1])}, {fmt(org[2])}){more}"


def build_glossary(species: "Species", repo: "Repo", geo: dict[str, Any], groups: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """One row per bone, in the geo's own order (the draw order the G2 contract pins)."""
    seed = species.seed or {}
    labels = seed.get("labels", {})
    bones, by_name, children = bone_maps(geo)
    locked = locked_bones(species, geo)
    ri = (species.sidecar or {}).get("render_instances", {}).get("bones", {})
    group_of: dict[str, tuple[str, bool]] = {}
    for g in groups:
        for b in g.get("bones", []):
            group_of[b] = (g["name"], bool(g.get("gait_scaled")))
    rows = []
    for b in bones:
        name = b["name"]
        source = "seed"
        label = labels.get(name)
        if label is None and name in DEFAULT_LABELS:
            label, source = DEFAULT_LABELS[name], "default dictionary"
        if label is None and name in ri:
            info = ri[name]
            if info["role"] == "group":
                label = f"{info['source_part']} draw {info['draw_index']} fan group (static {fmt(math.degrees(info['static_rotation_radians'][2]) if info.get('static_rotation_radians') else 0, 1)} deg)" if info.get("draw_index") is not None else f"{info['source_part']} fan hub (the animated group; its clones are the blades)"
            else:
                label = f"{info['source_part']} draw {info['draw_index']} (a clone of the classic part; keep its bind rotation)"
            source = "render_instances (Slice 4c)"
        if label is None:
            label = f"(no label yet — owner to name; {humanize(name)}; {cube_hint(b)})"
            source = "NONE"
            repo.warnings.add(species.registry, "BONE_UNLABELLED", f"bone {name} has no glossary label")
        classic = name
        if name in ri:
            classic = f"{ri[name]['source_part']} ({ri[name]['role']}, draw {ri[name].get('draw_index')})"
        elif species.sidecar:
            classic = name + " (ModelPart of the same name)"
        else:
            classic = "(native rig: no classic part)"
        grp, gait = group_of.get(name, ("", False))
        rows.append({
            "name": name, "label": label, "label_source": source, "classic": classic,
            "parent": b.get("parent", ""), "pivot": b.get("pivot", [0, 0, 0]), "rotation": b.get("rotation"),
            "cubes": len(b.get("cubes", [])), "children": children.get(name, []),
            "group": grp, "gait_bone": gait, "locked": name in locked, "lock_reason": locked.get(name, ""),
            "hitbox_part": name in (species.profile or {}).get("synched-bones", []),
        })
    return rows


RECTIFIED_RE = re.compile(r"\|\s*(?:Math\.)?(?:cos|sin)\b|Math\.abs\(\s*(?:Math\.)?(?:cos|sin)\b|\babs\(\s*(?:cos|sin)\b")


def frequency_groups(species: "Species") -> list[dict[str, Any]]:
    """The seed's frequency groups with their natural periods (contract §7.1). A rectified shape (|cos|, |sin| —
    the seed's `rectified: true`, or the bars in its math / amplitude text) repeats visibly every HALF period:
    both the natural period 2π/ω and the visible period are stated."""
    out = []
    for g in (species.seed or {}).get("groups", []):
        omega = g.get("omega")
        row = dict(g)
        rectified = bool(g.get("rectified")) or bool(RECTIFIED_RE.search(str(g.get("math", "")) + " " + str(g.get("amplitude", ""))))
        row["rectified"] = rectified
        if omega:
            row["period_ticks"] = 2 * math.pi / omega
            row["period_seconds"] = row["period_ticks"] / TICKS_PER_SECOND
            row["clip_ticks_per_game_tick_at_1s"] = 20.0 / row["period_ticks"]
            if rectified:
                row["visible_period_ticks"] = row["period_ticks"] / 2
                row["visible_period_seconds"] = row["visible_period_ticks"] / TICKS_PER_SECOND
                row["visible_clip_ticks_per_game_tick_at_1s"] = 20.0 / row["visible_period_ticks"]
            else:
                row["visible_period_ticks"] = row["visible_period_seconds"] = row["visible_clip_ticks_per_game_tick_at_1s"] = None
        else:
            row["period_ticks"] = None
            row["period_seconds"] = None
            row["clip_ticks_per_game_tick_at_1s"] = None
            row["visible_period_ticks"] = row["visible_period_seconds"] = row["visible_clip_ticks_per_game_tick_at_1s"] = None
        out.append(row)
    return out


def exact_transcription(species: "Species", repo: "Repo") -> bool:
    """True where the shipped `.animation.json` carries the species' exact keyframe transcription: a clip manifest
    `tools/keyframe_clips/<registry>.json` exists (the generator's input, tools/keyframe_clip.py) and every clip its groups
    name (and `idle` where it declares one) is in the shipped file. False for a species running on its classic hook
    (Amendment 2, 2026-09-13) and for a native rig (the Queen: her own clips, no transcription)."""
    manifest_path = repo.paths.keyframe_clips / f"{species.registry}.json"
    if not manifest_path.exists():
        return False
    clip_manifest = load_json(manifest_path)
    shipped = species.animation.get("animations", {})
    names = [g["clip"] for g in clip_manifest.get("groups", [])]
    if clip_manifest.get("idle") is not None:
        names.append("idle")
    return bool(names) and all(n in shipped for n in names)


def reference_clip_facts(species: "Species", repo: "Repo", native: bool) -> tuple[dict[str, Any] | None, str]:
    """(the sampler's index row for this species with its file's sha256 verified, or None; the reason when None).
    A landed species whose descriptor has no classic hook (`native`: the SPEC's controller kind - a native GeckoLib rig, the
    Queen) has none by design; a hook species without a row means the sampler has not run (gradle referenceClips) - a
    warning, never a silent omission."""
    row = repo.reference_clips.get(species.registry)
    if row is None:
        if native:
            return None, ("no sampled reference clip: this creature has no classic hook (a native GeckoLib rig) — its own shipped "
                          "clips are its reference")
        repo.warnings.add(species.registry, "REFERENCE_CLIP_MISSING",
                          f"no row for {species.registry} in tools/reference_clips/{REFERENCE_CLIP_INDEX}: run gradle referenceClips "
                          "(the g1 harness's ReferenceClipSampler) before packaging")
        return None, "no sampled reference clip in this package (the sampler has not run for this creature: gradle referenceClips)"
    path = repo.paths.reference_clips / row["file"]
    if not path.exists():
        repo.warnings.add(species.registry, "REFERENCE_CLIP_MISSING", f"the index names {row['file']} but tools/reference_clips/ has no such file")
        return None, f"no sampled reference clip in this package (the index names `{row['file']}` but the file is missing)"
    actual = sha256_file(path)
    if actual != row.get("sha256"):
        repo.warnings.add(species.registry, "REFERENCE_CLIP_STALE",
                          f"{row['file']} sha256 {actual[:12]}... differs from the index's {str(row.get('sha256'))[:12]}...: re-run gradle referenceClips")
    return dict(row) | {"sha256": actual, "_path": path}, ""


def reference_clip_section(species: "Species", repo: "Repo", seed: dict[str, Any], exact: bool, native: bool) -> tuple[list[str], dict[str, Any] | None]:
    """SPEC §4.3 (owner 2026-09-13, second set, addendum item 27 (3)) and the manifest's `reference_clip` block: what the
    reference-only clip is, that it is never returned or shipped, and - for a species without an exact transcription - the
    plain-language transcription of its source formulas (migration design section 5: the source method and line quoted, each
    formula and constant in words; the seed's `formulas`)."""
    L: list[str] = []
    L.append("### 4.3 Reference clip (reference-only)")
    L.append("")
    row, reason = reference_clip_facts(species, repo, native)
    manifest_block: dict[str, Any] | None = None
    if row is None:
        L.append(f"_{reason}._")
    else:
        keys = int(row.get("keys_per_bone", 0))
        span_ticks = float(row.get("span_ticks", 0.0))
        rule = str(row.get("rule", ""))
        seam = float(row.get("seam_delta_degrees", 0.0))
        period_ticks = float(row.get("period_ticks") or 0.0)
        multiple = int(row.get("period_multiple_k") or 0)
        # the span rule (owner 2026-09-13, third set, item 28 (5)): the smallest multiple of the slowest rhythm's period at
        # which every rhythm returns within 5 degrees of its start, capped at 6 s; past the cap two seconds and the seam stated
        if rule == "one_key":
            span = "one key at the bind pose (nothing in this rig's code moves a bone at these inputs)"
        elif rule == "period_multiple" and multiple <= 1:
            span = (f"one period of its slowest rhythm — {fmt(span_ticks, 3)} ticks ({fmt(span_ticks / TICKS_PER_SECOND, 3)} s): every moving bone is back "
                    "within 5 degrees of its start there, so the last key closes the loop")
        elif rule == "period_multiple":
            span = (f"{multiple} periods of its slowest rhythm ({fmt(period_ticks, 3)} ticks each) — {fmt(span_ticks, 3)} ticks ({fmt(span_ticks / TICKS_PER_SECOND, 3)} s): "
                    "the smallest multiple at which EVERY rhythm returns within 5 degrees of its start (the rule caps this search at 6 s), so the last key closes the loop")
        elif rule == "two_seconds_past_cap":
            span = (f"two seconds ({fmt(span_ticks, 0)} ticks): this motion does not close within 6 s (its slowest rhythm is {fmt(period_ticks, 3)} ticks, and no multiple "
                    f"of it under 6 s brings every rhythm back within 5 degrees) — a two-second window, not a loop; the closing key differs from the first by {fmt(seam, 4)} degrees")
        else:
            span = f"two seconds ({fmt(span_ticks, 0)} ticks): this motion has no natural period — a two-second window, not a loop; the closing key differs from the first by {fmt(seam, 4)} degrees"
        L.append(f"`{row['file']}` (beside this sheet; sha256 `{row['sha256']}`) is NOT a clip to edit, improve, return or ship. It is the creature's "
                 "classic code — the motion the game draws today — SAMPLED by the harness at fixed inputs so you can open it beside the rig in "
                 "Blockbench and see that motion: full walking speed (limbSwingAmount 1, the walk position and the age advancing one tick per key), "
                 f"not attacking, looking straight ahead, every state flag at rest, full health; {span}; 20 keys per second ({keys} keys per bone), "
                 "linear keys; rotations are deltas from the bind pose under the same sign rule as the shipped clips (X as the classic degrees, Y and Z "
                 "negated), positions only where the code moves a bone.")
        L.append("")
        L.append(f"- Sampled from: `{row.get('hook', '')}`.")
        L.append(f"- Rule applied: {row.get('rule_note', '')}.")
        moving = row.get("moving_bones") or []
        positioned = row.get("position_bones") or []
        hidden = row.get("hidden_bones_at_rest") or []
        L.append(f"- Bones that move in it: {', '.join('`' + b + '`' for b in moving) if moving else 'none'}"
                 + (f"; bones the code also MOVES (position keys): {', '.join('`' + b + '`' for b in positioned)}" if positioned else "")
                 + (f"; bones the code hides at rest (no animation channel can express that; see the transcription below): {len(hidden)}" if hidden else "")
                 + ".")
        L.append(f"- Loop seam: the closing key differs from the first by at most {fmt(float(row.get('seam_delta_degrees', 0.0)), 4)} degrees (mod 360).")
        L.append("")
        L.append("`check` REJECTS a returned `*_reference.animation.json` by name, and the game's jar never carries one (the asset audit refuses it).")
        manifest_block = {
            "file": row["file"], "sha256": row["sha256"], "clip": REFERENCE_CLIP_NAME, "reference_only": True,
            "rule": rule, "rule_note": row.get("rule_note"), "span_ticks": span_ticks,
            "period_ticks": row.get("period_ticks"), "period_multiple_k": row.get("period_multiple_k"),
            "animation_length_seconds": row.get("animation_length_seconds"), "keys_per_bone": keys,
            "sampled_inputs": row.get("sampled_inputs"), "hook": row.get("hook"),
            "moving_bones": moving, "position_bones": positioned, "hidden_bones_at_rest": hidden,
            "seam_delta_degrees": row.get("seam_delta_degrees"),
        }
    L.append("")
    if native:
        L.append("This creature is a native GeckoLib rig: it has no classic code to transcribe, so there is neither an exact transcription "
                 "nor a plain-language one — its shipped clips (§5) are its reference.")
    elif exact:
        anim_name = species.anim_path.name if species.anim_path else f"{species.registry}.animation.json"
        L.append(f"This creature ships an EXACT keyframe transcription of its code in `{anim_name}` (§4.1, §5): the reference clip is the "
                 "code itself at the fixed inputs above, for comparison; the transcription is what you improve.")
    else:
        L.append("This creature has NO exact keyframe transcription (owner 2026-09-13, Amendment 2: its motion does not fit the current "
                 "transcription form, and the game runs its classic code until you deliver `idle` and `walk`). Its source formulas in plain "
                 "language (migration design section 5 — the source method and line quoted, each formula and constant in words):")
        L.append("")
        formulas = seed.get("formulas", [])
        for line in formulas:
            L.append(f"- {line}")
        if not formulas:
            L.append("- _(no `formulas` in the seed yet — the lane authors them from the model sources, the orig line cited)_")
            repo.warnings.add(species.registry, "FORMULAS_MISSING",
                              "no exact transcription and no `formulas` in the seed: the SPEC's plain-language transcription (design section 5) is missing")
    L.append("")
    return L, manifest_block


def lock_note(keyed_locked: list[str]) -> str:
    """The per-clip consequence of keying locked bones — the one policy sentence (D1: said wherever a verdict invites an edit)."""
    if not keyed_locked:
        return ""
    shown = ", ".join(keyed_locked[:4]) + (f", +{len(keyed_locked) - 4} more" if len(keyed_locked) > 4 else "")
    return f"keys {len(keyed_locked)} locked bone(s) ({shown}): {LOCK_POLICY}"


def primary_group(species: "Species", groups: list[dict[str, Any]], gait: list[dict[str, Any]]) -> dict[str, Any] | None:
    """The group whose clip carries the bare `walk` (the naming rule, owner 2026-09-13, addendum item 26 (2); contract
    §2.1 amended): the gait group when the rig has one, else the seed's `primary_group`, else the FIRST group; None for a
    species without groups. A seed naming an unknown group, or a group other than the gait group, is a seed error - the
    generator (tools/keyframe_clip.py) and the harness's twin (KeyframeLeg.primaryGroup) refuse the same manifest."""
    if not groups:
        return None
    declared = (species.seed or {}).get("primary_group")
    by_name = {g["name"]: g for g in groups}
    if len(gait) > 1:
        raise SystemExit(f"{species.registry}: more than one gait-scaled group {[g['name'] for g in gait]} (one gait group per rig)")
    if gait:
        if declared and declared != gait[0]["name"]:
            raise SystemExit(f"{species.registry}: seed primary_group {declared!r} names a group other than the gait group "
                             f"{gait[0]['name']!r} (the gait group carries the bare walk)")
        return gait[0]
    if declared:
        if declared not in by_name:
            raise SystemExit(f"{species.registry}: seed primary_group {declared!r} names no frequency group {list(by_name)}")
        return by_name[declared]
    return groups[0]


def clip_rows(species: "Species", inv: dict[str, Any], anim: dict[str, Any], groups: list[dict[str, Any]],
              bone_names: list[str] | None = None, locked: dict[str, str] | None = None) -> list[dict[str, Any]]:
    """The clip table: contract clips this species ships or would ship, with loop mode, layer, trigger, bones, rule.
    `bone_names` (the rig, geo order) lets the base loops list every bone (§2.1 / §8); `locked` lets every row say
    what its keys on locked bones mean under each policy."""
    seed = species.seed or {}
    verdicts = {c["name"]: c for c in seed.get("clips", [])}
    scope = seed.get("artist_scope", "")
    rows: list[dict[str, Any]] = []
    native = anim.get("animations", {})
    locked = locked or {}
    if species.tier == 0 or (native and inv.get("native")):
        # a native GeckoLib species: its shipped clips are the table; the contract mapping is the seed's per row (ruled
        # 2026-09-06, Q16 (a): the pilot boss keeps its native clip set). The trigger of each clip is read from
        # registerControllers and the call sites (native_clip_triggers) — nothing hard-coded.
        triggers = (inv.get("native") or {}).get("clip_triggers") or {}
        for name, clip in native.items():
            loop = clip.get("loop", False)
            loop_text = "hold_on_last_frame" if loop == "hold_on_last_frame" else ("true" if loop is True else "false")
            t = triggers.get(name, {})
            trigger = t.get("signal", "unreferenced by the controllers")
            layer = f"native `{t['controller']}` controller ({t['kind']})" if t.get("controller") else "native (unreferenced)"
            v = verdicts.get(name, {})
            keyed_locked = sorted(set(clip.get("bones", {})) & set(locked))
            note = v.get("note", "")
            role = v.get("contract", "")
            mapping = (f"mapped to the contract's `{role}` by the seed" if role else "no contract role in the seed") \
                + " (ruled 2026-09-06, Q16 (a): the pilot boss keeps its native clip set)"
            if "Q16 (a)" not in note:  # the mapping sentence precedes the lock note, so it never reads as the policy's
                note = (note + " — " if note else "") + mapping
            ln = lock_note(keyed_locked)
            if ln:
                note = note + " — " + ln
            rows.append({"name": name, "loop": loop_text, "layer": layer, "trigger": trigger,
                         "bones": f"{len(clip.get('bones', {}))} bones keyed" + (f", {len(keyed_locked)} of them locked" if keyed_locked else ""),
                         "length_seconds": clip.get("animation_length"),
                         "length_rule": f"near:{clip.get('animation_length')}", "code_triggered": True, "required": True,
                         "role": "native", "verdict": v.get("verdict", "leave"), "note": note, "keyed_locked": keyed_locked,
                         "contract": v.get("contract", "unmapped (a native clip the seed gives no contract role)")})
        return rows
    if species.tier == 3 or scope.startswith("none"):
        return rows  # no artist clips (P2; ruled 2026-09-06, Q7 (a): Tier 3 gets no artist clips)
    gait = [g for g in groups if g.get("gait_scaled")]
    # The naming rule (owner 2026-09-13, addendum item 26 (2); contract §2.1 amended): the bare `walk` belongs to the
    # gait group; a species WITHOUT a gait group names its primary locomotion group in its seed (`primary_group`, the
    # FIRST group when absent) and THAT group's clip carries the bare name - a label, not a semantic (the contract's
    # fly -> walk fallback plays a flyer's walk in flight); every other group's is `<state>_<group>`. So every species
    # with groups has the bare `idle` / `walk` pair (item 12's gate opens on both), and the first slice's optional bare
    # idle of a multi-group no-gait species (optional then) is gone.
    primary = primary_group(species, groups, gait)
    others = [g for g in groups if primary is None or g["name"] != primary["name"]]
    gait_name = primary["name"] if primary else ""
    all_bones = list(bone_names or [])
    other_group_bones = {b: g["name"] for g in others for b in g.get("bones", [])}

    def whole_rig(state: str) -> str:
        """Every bone of the rig for a base loop (§2.1: every group the species idles with; §8: any bone on a Tier-2 rig),
        the gait group (or the primary group) marked, other groups' bones pointed at their own `<state>_<group>` layer."""
        if not all_bones:
            return ", ".join(primary["bones"]) if primary else "(as SPEC)"
        gait_set = set(primary["bones"]) if primary else set()
        parts = []
        if gait_set:
            parts.append(("gait group (speed-scaled): " if gait else "the primary group (unscaled; the bare clip is its transcription, a label under the naming rule): ")
                         + ", ".join(b for b in all_bones if b in gait_set))
        free = [b for b in all_bones if b not in gait_set and b not in other_group_bones and b not in locked]
        if free:
            parts.append("free: " + ", ".join(free))
        grouped = [f"{b} (better left to `{state}_{other_group_bones[b]}`)" for b in all_bones if b in other_group_bones and b not in gait_set]
        if grouped:
            parts.append("other groups' bones: " + ", ".join(grouped))
        lk = [b for b in all_bones if b in locked and b not in gait_set and b not in other_group_bones]
        if lk:
            parts.append("locked (see §7): " + ", ".join(lk))
        return f"any of the {len(all_bones)} bones — " + "; ".join(parts)

    # The bare `walk` is the gait group's, else the primary group's (the naming rule); the bare `idle` is every species'
    # (§2.1: "standing, no target") and rule 3 (idle AND walk together) applies to every species - both bare rows are
    # required (a species without groups keeps the bare pair too: there is nothing to name).
    bare_walk = True

    def loop_row(name: str, layer: str, weight: str, bones: str, group: str, note: str = "",
                 verdict_default: str = "author") -> dict[str, Any]:
        # README rule 5, the phase-locked kind (ruled 2026-09-06, Q14 (a)): every loop authored at 1.0 s; in-game the
        # length is free (P6: the declared length is the shape's own timeline) and the tempo table gives the rate.
        v = verdicts.get(name, {})
        return {"name": name, "loop": "true", "layer": layer, "trigger": weight, "bones": bones or "(as SPEC)",
                "length_seconds": LOOP_AUTHORING_LENGTH_SECONDS, "length_rule": f"near:{LOOP_AUTHORING_LENGTH_SECONDS}",
                "code_triggered": False, "required": name in ("idle", "walk") and bare_walk, "role": "contract", "group": group,
                "verdict": v.get("verdict", verdict_default), "note": v.get("note", note), "contract": name}

    for state in ("idle", "walk"):
        weight = "w_idle = (1 - w_move)(1 - w_swim)(1 - w_fly)" if state == "idle" else "w_walk = w_move (1 - w_swim)(1 - w_fly); the gait group additionally x limbSwingAmount (P3)"
        note = ""
        if state == "walk" and primary is not None and not gait:
            note = (f"the bare walk is the primary group `{primary['name']}`'s (the seed's primary_group; the first group when it names none) "
                    "- a label, not a semantic: the fly -> walk fallback plays it in flight (owner 2026-09-13, addendum item 26 (2))")
        rows.append(loop_row(state, "base", weight, whole_rig(state), gait_name, note=note))
        for g in others:
            rows.append(loop_row(f"{state}_{g['name']}", "parallel layer", weight + f" (group {g['name']})", ", ".join(g["bones"]), g["name"]))
    loco = seed.get("locomotion", "walker")
    if loco in ("swimmer", "walker"):
        rows.append(loop_row("swim", "base", "w_swim from isInWater()", whole_rig("swim"), gait_name,
                             note="optional: falls back to walk then idle when absent (§2.4)"))
    if loco == "flyer":
        rows.append(loop_row("fly", "base", "w_fly (flyer species and !onGround)", whole_rig("fly"), gait_name))
    att = inv.get("attacking") or {}
    if att.get("present") and att.get("verdict") in ("STATE", "MIXED"):
        rows.append(loop_row("aggro_idle", "base", "w_idle x w_aggro (DATA_ATTACKING held)", whole_rig("idle"), gait_name))
        rows.append(loop_row("calm_idle", "base", "w_idle x (1 - w_aggro)", whole_rig("idle"), gait_name))
    else:
        rows.append(loop_row("calm_idle", "base", "= idle until this creature's SPEC adds a synched attacking byte (ruled 2026-09-06, Q12 (a))", "(as idle)", gait_name,
                             note="not needed while idle covers it; listed so the name stays reserved — not counted in the effort estimate",
                             verdict_default="covered by idle"))
    has_attack = bool(inv.get("combat", {}).get("melee") or inv.get("combat", {}).get("ranged") or att.get("present"))
    for name, loop in CONTRACT_CLIPS_TRIGGERED.items():
        if name == "attack" and not has_attack:
            continue
        v = verdicts.get(name, {})
        default_len = {"attack": 0.5, "hurt": 0.4, "death": 2.0}[name]
        length = v.get("length_seconds", default_len)
        trigger = {"attack": "event: a strike (the transport per species by the trigger inventory — ruled 2026-09-06, Q11 (a); §6 says which applies here)",
                   "hurt": "event: hurtTime rising edge (client-observed); the red overlay stays (ruled 2026-09-06, Q4 (a))",
                   "death": "event: deathTime > 0; the vanilla death flip stays — the pilot ships no death clip (ruled 2026-09-06, Q3 (a)); a creature whose "
                            "JSON ships one gets the clip-replaces-flip mode when the first such clip arrives, so a `death` delivered here plays under the flip until then"}[name]
        rows.append({"name": name, "loop": loop, "layer": "triggered controller", "trigger": trigger, "bones": "(any unlocked)",
                     "length_seconds": length, "length_rule": f"near:{length}", "code_triggered": True,
                     "required": name in native,  # a shipped code-triggered clip may not be renamed or dropped
                     "role": "contract", "group": "", "verdict": v.get("verdict", "author"), "note": v.get("note", ""),
                     "contract": name})
    for extra in seed.get("extras", []):
        looping_extra = str(extra.get("loop")).lower() == "true"
        rows.append({"name": extra["name"], "loop": str(extra.get("loop", "false")).lower(), "layer": extra.get("layer", "overlay"),
                     "trigger": extra.get("trigger", "(SPEC)"), "bones": ", ".join(extra.get("bones", [])) or "(SPEC)",
                     # a looping extra follows rule 5's phase-locked kind (1.0 s); a one-shot the length its seed states
                     "length_seconds": LOOP_AUTHORING_LENGTH_SECONDS if looping_extra else extra.get("length_seconds"),
                     "length_rule": f"near:{LOOP_AUTHORING_LENGTH_SECONDS}" if looping_extra else f"near:{extra.get('length_seconds', 1.0)}",
                     "code_triggered": bool(extra.get("code_triggered", False)), "required": False, "role": "extra", "group": "",
                     "verdict": "author", "note": extra.get("note", ""), "contract": "extra (§2.3; four per creature without a ruling — ruled 2026-09-06, Q6 (a))"})
    if len(seed.get("extras", [])) > 4:
        pass  # reported by the caller as a warning
    return rows


def effort_estimate(species: "Species", bones: int, clips: list[dict[str, Any]]) -> tuple[float, str]:
    seed = species.seed or {}
    if seed.get("effort_hours") is not None:
        return float(seed["effort_hours"]), "seed (owner-set)"
    to_author = [c for c in clips if c.get("verdict") in ("author", "improve")]  # 'leave' and 'covered by idle' are not artist work
    if species.tier == 3:
        return 0.0, "Tier 3: no artist pass (documentation only)"
    n = len(to_author)
    if species.tier in (0, 1):
        return round(8 + 0.15 * bones + 1.5 * n, 1), f"generator: 8 h + 0.15 h/bone x {bones} + 1.5 h/clip x {n} to author or improve (owner adjusts)"
    return round(4 + 0.2 * bones + 1.0 * n, 1), f"generator: 4 h + 0.2 h/bone x {bones} + 1 h/clip x {n} to author or improve (owner adjusts)"


def spec_document(species: "Species", repo: "Repo", catalog: "TextureCatalog", inv: dict[str, Any]) -> tuple[str, dict[str, Any]]:
    """SPEC.md text and the machine-readable manifest for `check`."""
    seed = species.seed or {}
    geo = species.geo
    anim = species.animation
    bones, by_name, _ = bone_maps(geo)
    desc = geo["minecraft:geometry"][0]["description"]
    groups = frequency_groups(species)
    glossary = build_glossary(species, repo, geo, groups)
    locked = {r["name"]: r["lock_reason"] for r in glossary if r["locked"]}
    clips = clip_rows(species, inv, anim, groups, bone_names=[b["name"] for b in bones], locked=locked)
    if len(seed.get("extras", [])) > 4:
        repo.warnings.add(species.registry, "EXTRAS_CAP", f"{len(seed['extras'])} extras exceed the cap of four per creature without a ruling (ruled 2026-09-06, Q6 (a): more needs the owner)")
    textures = catalog.canonical_for_species(species)
    keyed_locked = sorted(shipped_keyed_bones(anim) & set(locked))
    keyed_locked_by_clip = {name: sorted(set(clip.get("bones", {})) & set(locked)) for name, clip in anim.get("animations", {}).items()}
    keyed_locked_by_clip = {k: v for k, v in keyed_locked_by_clip.items() if v}
    lock_mode = LOCK_MODE_DEFAULT  # one policy for every species (LOCK_POLICY); the manifest key is the reject-mode switch
    if keyed_locked:
        repo.warnings.add(species.registry, "LOCKED_BONES_KEYED",
                          f"the shipped clips key {len(keyed_locked)} of the {len(locked)} SPEC-locked bones ({', '.join(keyed_locked[:6])}...): "
                          f"allowed and warned — {LOCK_POLICY}")
    accepted_clips = [c["name"] for c in clips]
    allow_idle_alt = bool(clips) and not any(c["role"] == "native" for c in clips)
    wishlist_notes: list[str] = []
    for w in seed.get("wishlist", []):
        named = re.findall(r"`([a-z0-9_]+)`", w)
        bad = [n for n in named if n not in accepted_clips and not (allow_idle_alt and re.fullmatch(r"idle_alt_\d+", n))
               and n not in [r["name"] for r in glossary]]
        if bad:
            wishlist_notes.append(w + f" — NOT accepted by `check` today: {', '.join('`' + b + '`' for b in bad)} is not in this creature's clip set"
                                  + ("; a new clip on a native rig needs a controller change and the owner's ruling (the pilot boss keeps its native clip set: "
                                     "ruled 2026-09-06, Q16 (a))" if not allow_idle_alt else ""))
            repo.warnings.add(species.registry, "WISHLIST_UNACCEPTED", f"wishlist names clip(s) {bad} that the manifest does not accept")
        else:
            wishlist_notes.append(w)
    if not seed:
        repo.warnings.add(species.registry, "SEED_MISSING", f"no tools/artist_specs/{species.registry}.json: character sheet, labels, groups and verdicts are missing")
    elif not seed.get("character_sheet"):
        repo.warnings.add(species.registry, "SEED_TEXT_MISSING", "the seed has no character_sheet paragraph")
    display = seed.get("display_name", species.java_class)
    hours, hours_source = effort_estimate(species, len(bones), clips)
    pin = species.pin or {}
    scale = pin.get("expected_scale", "?")
    shadow = pin.get("expected_shadow", "?")
    width, height = species.width, species.height
    sc = float(scale) if isinstance(scale, (int, float)) else None
    tw, th = desc.get("texture_width"), desc.get("texture_height")
    oq = "phase_g_reports/animation_contract/open_questions.md"

    L: list[str] = []
    L.append(f"# {display} — `{species.registry}` — artist SPEC")
    L.append("")
    L.append(f"Generated by `tools/artist_package.py` {TOOL_VERSION} from the repository's own sources; the paragraphs marked "
             f"AUTHORED come from `tools/artist_specs/{species.registry}.json` ({seed.get('status', 'no seed')}). "
             f"Every contract decision this sheet rests on was ruled by the mod's owner on 2026-09-06 (`{oq}` records each ruling); a line that cites "
             "one (\"ruled 2026-09-06, Q14 (a)\") states that ruling — nothing here invents an answer. The one open item is the lock reject mode (§7, README rule 6).")
    L.append("")
    L.append("## 1. What this mob is (AUTHORED — draft for the owner's edit)")
    L.append("")
    L.append(seed.get("character_sheet", "_(no character sheet yet — the owner or the lane authors this paragraph in the seed)_"))
    L.append("")
    L.append("## 2. Size and scale in-game")
    L.append("")
    dims = f"{fmt(width)} x {fmt(height)} blocks (wide x tall)" if width is not None else "?"
    L.append(f"- Hitbox (EntityType dims, `ModEntities.java`): {dims}.")
    L.append(f"- Renderer scale: x{scale}; shadow radius {shadow} (reference pin status: {pin.get('status', 'no pin')}, `tools/reference_renderer_pins.json`).")
    if sc and height is not None:
        L.append(f"- Rig units: 16 units = 1 block before the renderer scale, so a {fmt(height)}-block-tall hitbox is {fmt(height * 16 / sc, 1)} rig units tall at x{scale}.")
    L.append(f"- Texture canvas: {tw} x {th} (keep it; the canvas size is checked on return).")
    if seed.get("size_notes"):
        L.append(f"- Notes (AUTHORED): {seed['size_notes']}")
    L.append("")
    L.append("## 3. Bone glossary (locked legacy names — readable labels beside them)")
    L.append("")
    L.append("Never rename, delete or re-parent a bone: code and hitboxes reference them by name. Left/right in the labels follow the model "
             "author's own naming (the side the legacy name calls left); Blockbench mirrors X for display, so the author's left appears on your right when the mob faces you. "
             "`gait bone` = its motion is scaled by walking speed in-game (P3). `locked` = it carries or parents a hitbox part (contract §8.1). "
             + LOCK_POLICY)
    L.append("")
    rows = []
    for r in glossary:
        rows.append([f"`{r['name']}`", r["label"], r["classic"], r["parent"] or "-",
                     f"({fmt(r['pivot'][0])}, {fmt(r['pivot'][1])}, {fmt(r['pivot'][2])})",
                     r["group"] or "-", "yes" if r["gait_bone"] else "no",
                     ("yes: " + r["lock_reason"]) if r["locked"] else "no", r["cubes"]])
    L.append(md_table(["bone", "readable label", "classic part", "parent", "pivot (x, y, z)", "group", "gait bone", "locked", "cubes"], rows))
    L.append("")
    unl = [r for r in glossary if r["label_source"] == "NONE"]
    if unl:
        L.append(f"_{len(unl)} bone(s) still lack a label; the placeholder in parentheses is generated from the name and the geometry._")
        L.append("")
    L.append("## 4. Current animation behaviour — plain English and the source formulas")
    L.append("")
    for line in seed.get("behaviour", []):
        L.append(f"- {line}")
    if not seed.get("behaviour"):
        L.append("- _(no behaviour paragraph in the seed)_")
    L.append("")
    if groups:
        L.append("### 4.1 Tempo table (contract §7.1)")
        L.append("")
        trows = []
        any_rectified = any(g.get("rectified") for g in groups)
        for g in groups:
            if g.get("period_ticks"):
                tempo = f"{fmt(g['period_ticks'], 3)} ticks ({fmt(g['period_seconds'], 3)} s)"
                rate = f"{fmt(g['clip_ticks_per_game_tick_at_1s'], 2)} clip-ticks per game tick at a 1.0 s authoring length"
                if g.get("rectified"):
                    tempo += f"; VISIBLE: {fmt(g['visible_period_ticks'], 3)} ticks ({fmt(g['visible_period_seconds'], 3)} s) — a rectified shape (|cos|) repeats every half period"
                    rate += f" (author one visible pump: {fmt(g['visible_clip_ticks_per_game_tick_at_1s'], 2)} clip-ticks per game tick)"
            else:
                tempo, rate = "not time-based", g.get("phase", "phase from distance walked")
            trows.append([g["name"], ", ".join(g.get("bones", [])), g.get("axis", ""), fmt(g["omega"]) if g.get("omega") else "-", tempo,
                          g.get("amplitude", ""), "yes" if g.get("gait_scaled") else "no", rate, g.get("source", "")])
        L.append(md_table(["group", "bones", "axis", "omega (rad/tick)", "natural period", "amplitude", "speed-scaled", "Blockbench preview rate", "source"], trows))
        L.append("")
        primary_row = primary_group(species, groups, [g for g in groups if g.get("gait_scaled")])
        if primary_row is not None and primary_row.get("gait_scaled"):
            L.append(f"The bare `walk` clip is the gait group's (`{primary_row['name']}`, scaled by walking speed in-game); every other group keys "
                     "`walk_<group>` / `idle_<group>` (contract §2.1).")
            L.append("")
        elif primary_row is not None:
            L.append(f"The bare `walk` clip is the primary group's, `{primary_row['name']}` (the seed's `primary_group`; the first group when the seed names none) "
                     "— a label, not a semantic: the contract's fly → walk fallback is what plays a flyer's walk in flight (owner 2026-09-13, addendum item 26 (2)); "
                     "every other group keys `walk_<group>` / `idle_<group>`.")
            L.append("")
        if any_rectified:
            L.append("A rectified group (`|cos|`, `|sin|`) has TWO periods: the natural 2π/ω of the underlying wave, and the visible one — half of it — "
                     "at which the pump you see actually repeats; author the loop to the visible period.")
            L.append("")
        L.append("Author every loop at 1.0 s (README rule 5, the phase-locked kind; ruled 2026-09-06, Q14 (a)). In-game each loop plays once per natural "
                 "period whatever its length; your timeline's length is the SHAPE's timeline and does not change that (P6). Blockbench previews at the "
                 "declared length, so a 1.0 s preview of a fast group shows the shape slowed down (contract §9); a `_preview` copy stretched to the natural "
                 "periods is a Blockbench-only aid, never in the jar and never in a delivery (ruled 2026-09-06, Q15 (a)).")
        L.append("")
    L.append("### 4.2 The math")
    L.append("")
    for g in groups:
        L.append(f"- **{g['name']}**: {g.get('plain', '')} — `{g.get('math', '')}` ({g.get('source', '')})")
    for extra in seed.get("formulas", []):
        L.append(f"- {extra}")
    L.append("")
    # §4.3 (owner 2026-09-13, second set, item 27 (3)): the reference-only clip, and the plain-language transcription
    # where no exact transcription ships.
    is_native_rig = any(c["role"] == "native" for c in clips)
    exact = exact_transcription(species, repo)
    section, reference_clip = reference_clip_section(species, repo, seed, exact, is_native_rig)
    L.extend(section)
    L.append("## 5. Clips: what to improve, what to leave (AUTHORED verdicts on generated rows)")
    L.append("")
    if not clips:
        why = "Tier 3 (P2 / Amendment 1 point 1): this rig stays code-driven; its animation JSON stays `{}` and no artist clips are accepted (ruled 2026-09-06, Q7 (a): Tier 3 gets no artist clips, extras included)." if species.tier == 3 else "no clips"
        L.append(why)
    else:
        crows = []
        for c in clips:
            crows.append([f"`{c['name']}`", c["loop"], c["layer"], c["trigger"], c["bones"],
                          (fmt(c["length_seconds"], 3) + " s") if c.get("length_seconds") else "free",
                          "yes" if c["code_triggered"] else "no", c["verdict"], c.get("note", "").strip()])
        L.append(md_table(["clip", "loop", "layer", "weight / trigger", "bones it may animate", "length", "code-triggered", "verdict", "notes"], crows))
        L.append("")
        is_native = any(c["role"] == "native" for c in clips)
        if not is_native:
            L.append("Loop values are the JSON `loop` field exactly: `true` for cycles, `false` for one-shots, `hold_on_last_frame` only where written. "
                     "Every name in this table is fixed (contract §2.4); `idle_alt_1`, `idle_alt_2`, ... may be added freely. A sheet may carry up to four "
                     "extra clips of this creature's own beyond the contract's names (§2.3; ruled 2026-09-06, Q6 (a)) — more needs the owner's ruling; "
                     "the extras rows above, if any, are those.")
        else:
            L.append(f"This species already animates natively: the {len(clips)} names above are its ACCEPTED clip set — what the code selects or triggers today, "
                     "read from `registerControllers` and the trigger call sites. `check` rejects any other clip name (including `idle_alt_N`: the native "
                     "controllers roll no idle variants). Its mapping onto the standard contract is the seed's, row by row (ruled 2026-09-06, Q16 (a): this "
                     "species is the pilot boss and keeps its native clip set; the pilot's scope is in §10); a contract name that is not in this set is listed "
                     "under 'What fires each contract clip' as not used by this species. "
                     "Its controllers are its own GeckoLib controllers (controller kind `native` in the manifest), not phase-locked, so its LOOPING "
                     "clips may carry sound / particle keys — they fire once per loop (README rule 7's exception; owner 2026-09-13, addendum item 26 (6)). "
                     f"Locked bones: {LOCK_POLICY}")
        L.append("")
    if seed.get("wishlist"):
        L.append("### 5.1 Wishlist (AUTHORED — only what `check` accepts today; anything else is marked)")
        L.append("")
        for w in wishlist_notes:
            L.append(f"- {w}")
        L.append("")
    if seed.get("future"):
        L.append("### 5.2 Not accepted today (AUTHORED — needs a code change and the owner's ruling first; do not deliver these)")
        L.append("")
        for w in seed["future"]:
            L.append(f"- {w}")
        L.append("")
    L.append("## 6. Trigger inventory (generated from the entity's AI goals and state flags)")
    L.append("")
    L.append(f"Source: `{inv.get('entity_file')}`" + (f" ({inv['locomotion'].get('extends')}" + (", " + ", ".join(inv['locomotion'].get('implements', [])) if inv['locomotion'].get('implements') else "") + ")" if inv.get("locomotion") else ""))
    L.append("")
    if inv.get("goals"):
        L.append(md_table(["selector", "prio", "goal", "guard", "category", "what it does", "bears on"],
                          [[g["selector"], g["priority"] if g["priority"] is not None else "?",
                            f"`{g['goal']}`" + (f" ({g['unparsed']}; line {g['line']})" if g["goal"] == "UNPARSED" else ""),
                            g.get("guard") or "-", g["category"], g["plain"], g["drives"]] for g in inv["goals"]]))
        L.append("")
        L.append("A `[modern: key]` guard means the goal is registered only under the modern config key named (read once, at construction); "
                 "`UNPARSED` means the registration's shape is one the generator does not read (a local variable or a computed priority) — the owner reads that line.")
    else:
        L.append("_No AI goals registered (the behaviour lives in tick / customServerAiStep)._")
    L.append("")
    L.append("**Synched state flags** (what the client can see): " + (", ".join(f"`{f['name']}` ({f['type']}, line {f['line']})" for f in inv.get("flags", [])) or "none"))
    L.append("")
    att = inv.get("attacking") or {}
    if att.get("present"):
        L.append(f"**Attacking flag `DATA_ATTACKING`** — classified **{att['verdict']}** ({att['reason']}) — a mechanical reading; the owner confirms it against the sites:")
        L.append("")
        L.append(md_table(["line", "method", "sets", "guard (the enclosing block)", "within", "comment"],
                          [[s["line"], s["method"], s["value"], s["guard"] or "(unguarded: the method body)",
                            " within ".join(s.get("within", [])[:3]) or "-", s["comment"]] for s in att["sites"]]))
        L.append("")
        L.append("The guard is the header of the block that actually encloses the write (found by brace depth, comments and strings blanked); an `else` "
                 "branch is written as the negated `if` chain it closes; a header the parser cannot read is `(unparsed: ...)`, never dropped.")
        L.append("")
    loco = inv.get("locomotion", {})
    facts = []
    if loco.get("stationary"):
        facts.append("MOVEMENT_SPEED 0.0: never walks (limbSwingAmount stays 0)")
    if loco.get("no_physics"):
        facts.append("noPhysics: passes through blocks (a flyer / phantom)")
    if loco.get("flying_move_control"):
        facts.append("flying move control")
    if loco.get("rideable"):
        facts.append("player-ridden")
    if loco.get("render_info"):
        facts.append("uses a per-entity RenderInfo scratch (client-only, never synced)")
    if loco.get("baby_form"):
        facts.append("has a baby form (the renderer halves the scale)")
    L.append("**Locomotion facts:** " + ("; ".join(facts) or "a walker") + f". Overrides: {', '.join(inv.get('overrides', [])) or 'none'}.")
    L.append("")
    comb = inv.get("combat", {})
    if comb.get("melee") or comb.get("ranged") or comb.get("native_triggers"):
        L.append("**Strike and launch sites:**")
        L.append("")
        for s in comb.get("melee", []):
            if s.get("kind") == "area":
                L.append(f"- melee (AREA helper) `{s['code']}` — {s['file']}:{s['line']} in `{s['method']}`"
                         + (f", guard: {' within '.join(s['guards'][:3])}" if s.get("guards") else "")
                         + f"; the helper (line {s.get('helper_line')}) hurts EACH victim in its box {s.get('hurts_per_victim', '?')}x per roll "
                         f"(line(s) {s.get('hurt_lines', [])}) — a LivingDamageEvent.Post per victim would fire `attack` per victim per roll (the transport 1 caveat; "
                         "ruled 2026-09-06, Q11 (a): the transport is chosen per species by this inventory, and this caveat is what the choice weighs)")
            else:
                L.append(f"- melee `{s['code']}` — {s['file']}:{s['line']} in `{s['method']}`" + (f", guard: {' within '.join(s['guards'][:3])}" if s.get("guards") else ""))
        for s in comb.get("ranged", []):
            L.append(f"- ranged `new {s['code']}(...)` — {s['file']}:{s['line']} in `{s['method']}`")
        for s in comb.get("native_triggers", []):
            picked = f" (picked among {', '.join(s['keys'])})" if s.get("keys") else ""
            guard = f", guard: {' within '.join(s['guards'][:3])}" if s.get("guards") and s.get("helper") != "triggerableAnim" else ""
            L.append(f"- native trigger `{s['code']}`{picked} — {s['file']}:{s['line']} in `{s['method']}`{guard}")
        L.append("")
    nat = inv.get("native") or {}
    if nat:
        L.append("**Native GeckoLib controllers:** " + "; ".join(f"`{c['name']}` (transition {c['transition_ticks']} ticks)" for c in nat.get("controllers", [])) +
                 ". Triggerable: " + ", ".join(f"`{t['key']}` -> `{t['clip']}`" for t in nat.get("triggerable", [])) + ".")
        L.append("")
    L.append("**What fires each contract clip:**")
    L.append("")
    L.append(md_table(["clip", "signal", "verdict"], [[f"`{d['clip']}`", d["signal"], d["verdict"]] for d in inv.get("drives", [])]))
    L.append("")
    L.append("## 7. Hitbox bones that must keep their names")
    L.append("")
    if locked:
        prof = species.profile or {}
        L.append(f"MultiHitboxLib profile `{species.profile_name}.json` (sync-with-model {prof.get('sync-with-model')}, trust-client {prof.get('trust-client')}). "
                 f"The synced part bones and every ancestor are SPEC-locked (contract §8.1). {LOCK_POLICY} "
                 + (LOCK_REJECT_MODE if lock_mode == "warn"
                    else "This manifest's lock_mode is `reject`: `check` REJECTS keys on them (the reject mode, kept for the day the server-side hitbox evaluator lands)."))
        L.append("")
        if keyed_locked_by_clip:
            L.append(f"The shipped clips already key {len(keyed_locked)} of these {len(locked)} bones — "
                     + "; ".join(f"`{c}` keys {len(v)}" for c, v in keyed_locked_by_clip.items())
                     + " — allowed and warned as above: the hitbox parts follow those bones in-game, which is how the shipped boss already animates; "
                     "every §5 verdict that invites an edit to one of these clips repeats the consequence.")
            L.append("")
        L.append(md_table(["bone", "why"], [[f"`{b}`", why] for b, why in locked.items()]))
    else:
        L.append("No MultiHitboxLib profile: no locked bones. Every bone name is still immutable (the code poses bones by name).")
    L.append("")
    L.append("## 8. Textures")
    L.append("")
    if textures:
        L.append(md_table(["canonical file (edit this one)", "canvas", "also shipped as (fan-out on import)", "referenced by the code as", "note"],
                          [[f"`{t['canonical']}`", f"{t['width']}x{t['height']}", ", ".join(t["aliases"]) or "-", ", ".join(t["referenced_as"]), t["stray"] or ""] for t in textures]))
    else:
        L.append("_No textures referenced by this species' client classes._")
    L.append("")
    if seed.get("texture_notes"):
        L.append(seed["texture_notes"])
        L.append("")
    L.append("## 9. Reference screenshots")
    L.append("")
    L.append("`reference/` holds the slots listed in `reference/SLOTS.md`; the owner fills them from the dev client (fixed camera, light and state per slot). Empty until then.")
    L.append("")
    L.append("## 10. Effort and priority")
    L.append("")
    L.append(f"- Artist scope: {seed.get('artist_scope', 'full contract' if species.tier in (1, 2) else 'none (Tier 3)')}.")
    L.append(f"- Estimated effort: {fmt(hours, 1)} h ({hours_source}).")
    L.append(f"- {species.tier_label} ({species.animation_class or 'n/a'})."
             + (" The design's tier table says 0 ('done': a native rig); the animation contract calls this species its Tier-1 (MHLib) boss — both are true, so both are written."
                if species.tier == 0 and species.is_mhlib_boss else ""))
    if species.tier == 2:
        L.append("- Density statement (the harness's, not the artist's; ruled 2026-09-06, Q9 (a) and Q10): the classic transcription is verified at 2.5e-3 rad — "
                 "Beaver reference leg 15 / 13 / 8 catmullrom keys per bone with spline arguments repaired at load; wrap sample T−ε vs 0+ε included; the key "
                 "counts per bone are an output of the harness, re-derived on every re-transcription.")
    L.append("")
    L.append("## 11. What 'done' looks like for this entity")
    L.append("")
    anim_name = species.anim_path.name if species.anim_path else species.registry + ".animation.json"
    L.append(f"1. `{species.geo_path.name}` returned UNCHANGED (or not at all): every bone name, parent, pivot, rotation and cube as listed in §3 and the shipped file "
             "(a `locked` bone renamed, re-parented or deleted is refused by name).")
    if clips:
        L.append(f"2. `{anim_name}` — that exact file name, one file — (format 1.8.0) holding the clips of §5 with the loop values shown; keys on `locked` bones: {LOCK_POLICY}")
    else:
        L.append(f"2. No animation file (this tier takes no artist clips; an `{anim_name}` with zero clips is tolerated); texture edits only, if any.")
    L.append("3. Textures at their canvas sizes under the canonical names of §8 (aliases are fanned out on import).")
    L.append("4. Optionally the `.bbmodel` working file. `tools/artist_package.py check <your folder>` must pass before hand-in. It REJECTS: "
             + "; ".join(r.rstrip(".") for r in CHECK_REJECTS) + ". It WARNS on: " + "; ".join(w.rstrip(".") for w in CHECK_WARNS) + ".")  # an entry ending in a period joins without ".;"
    L.append("")

    manifest = {
        "generated_by": f"tools/artist_package.py {TOOL_VERSION}",
        "registry": species.registry, "display_name": display, "tier": species.tier, "tier_label": species.tier_label,
        "geo_file": species.geo_path.name, "animation_file": species.anim_path.name if species.anim_path else f"{species.registry}.animation.json",
        "bbmodel_file": f"{species.registry}.bbmodel",
        "texture_size": [tw, th],
        "bones": [{"name": r["name"], "parent": r["parent"] or None, "pivot": r["pivot"], "rotation": r["rotation"], "locked": r["locked"],
                   "mirror": bool(by_name[r["name"]].get("mirror", False)),
                   "cubes": [cube_signature(c, bool(by_name[r["name"]].get("mirror", False))) for c in by_name[r["name"]].get("cubes", [])]}
                  for r in glossary],
        "locked_bones": list(locked), "lock_mode": lock_mode, "lock_policy": LOCK_POLICY_ID, "lock_policy_text": LOCK_POLICY,
        "keyed_locked_by_shipped_clip": keyed_locked_by_clip,
        "native": any(c["role"] == "native" for c in clips),
        # the SPEC's controller kind (owner 2026-09-13, addendum item 26 (6)): `native` - the species' own GeckoLib controllers,
        # not phase-locked, so its loops may carry event keys; `phase_locked` - the contract's phase-locked keyframe layers
        "controller_kind": "native" if any(c["role"] == "native" for c in clips) else "phase_locked",
        # `verdict` (0.2.7): the seed's verdict per clip, so the README's priority table can say which clips a folder delivers
        # (a native creature's `leave` clips come back as shipped); the old per-clip marker flag is gone with the markers (item 28 (3))
        "clips": [{"name": c["name"], "loop": c["loop"], "role": c["role"], "code_triggered": c["code_triggered"], "required": c["required"],
                   "length_rule": c["length_rule"], "length_seconds": c.get("length_seconds"), "verdict": c.get("verdict", "author")} for c in clips],
        "allow_idle_alt": allow_idle_alt,
        "textures": [{"canonical": t["canonical"], "width": t["width"], "height": t["height"], "aliases": t["aliases"]} for t in textures],
        "effort_hours": hours, "effort_source": hours_source,
        # owner 2026-09-13, second set, item 27 (3): the reference-only clip beside the sheet (None where the species has no
        # classic hook, or the sampler has not run), and whether the shipped .animation.json carries the exact transcription
        "reference_clip": reference_clip,
        "exact_transcription": exact,
    }
    return "\n".join(L) + "\n", manifest


SLOTS = [
    ("screenshot_front.png", "front view at eye level, bind pose, day light, 6 blocks away"),
    ("screenshot_side.png", "left side view, same distance"),
    ("screenshot_top.png", "top-down view"),
    ("screenshot_scale_vs_player.png", "beside a standing player for scale"),
    ("screenshot_walk_mid.png", "mid-stride while walking (or the moving state the mob has)"),
    ("screenshot_state_attacking.png", "in its attacking state, if it has one"),
]


def slots_document(species: "Species") -> str:
    L = [f"# Reference capture slots — {species.registry}", "",
         "Empty placeholders the owner fills from the dev client (F3+B off, fixed camera, day light, `-Dorespawn.dev.geckolibRenderers=candidate` where the species has a candidate). "
         "One file per slot, named exactly as below.", ""]
    for name, what in SLOTS:
        L.append(f"- `{name}` — {what}")
    L.append("")
    return "\n".join(L)


# ---------------------------------------------------------------------------
# .bbmodel: a Blockbench "Bedrock Entity" project from the shipped geo + animation JSON, and the
# round-trip check (item 22 (11)): the Blockbench bedrock export rules emulated back to JSON, then a
# semantic diff against the shipped files. Format facts (Blockbench 4.x bedrock codec): Blockbench
# mirrors X for display — a bone pivot [x, y, z] becomes a group origin [-x, y, z]; rotations negate X
# and Y and keep Z; a cube origin+size becomes from [-(x+sx), y, z] / to [-x, y+sy, z+sz]; per-face UV
# {uv:[u,v], uv_size:[w,h]} becomes [u, v, u+w, v+h]; box UV keeps uv_offset; animation values are
# stored in the Bedrock convention verbatim; loop true / "hold_on_last_frame" / absent become
# "loop" / "hold" / "once". The DESCRIPTION's unknown keys (the parked `orespawn:bone_draw_order`) and
# the converter's private cube key `modelpart_mirror` have no Blockbench field: this writer stashes the
# description keys under `unhandled_root_fields` (a Blockbench project field it preserves on save) and
# the importer re-attaches them; a REAL Blockbench geo re-export drops both — which is why the artist
# contract asks for the animation file only, never a re-exported geo.
# ---------------------------------------------------------------------------

KNOWN_DESCRIPTION_KEYS = {"identifier", "texture_width", "texture_height", "visible_bounds_width",
                          "visible_bounds_height", "visible_bounds_offset"}
FACES = ("north", "east", "south", "west", "up", "down")
STASH_KEY = "orespawn:geo_description_extras"
DROPPED_CUBE_KEYS = {"modelpart_mirror"}


def bb_uuid(*parts: str) -> str:
    return str(uuid.uuid5(BB_NAMESPACE, "/".join(parts)))


def write_bbmodel(path: Path, bb: dict[str, Any]) -> None:
    """Compact JSON (Blockbench's own save format is single-line); LF, UTF-8."""
    write_text(path, json.dumps(bb, separators=(",", ":"), ensure_ascii=False) + "\n")


def _nz(value: float) -> float:
    return 0.0 if value == 0 else value  # no negative zero in the written files


def flip_x(v: Iterable[float]) -> list[float]:
    x, y, z = vec(v)
    return [_nz(-x), y, z]


def flip_rot(r: Iterable[float] | None) -> list[float]:
    x, y, z = vec(r)
    return [_nz(-x), _nz(-y), z]


def box_faces(u: float, v: float, size: list[float], mirror: bool) -> dict[str, dict[str, Any]]:
    w, h, d = size
    layout = {
        "east": ([0, d], [d, h]), "west": ([d + w, d], [d, h]), "up": ([d + w, d], [-w, -d]),
        "down": ([d + w * 2, 0], [-w, d]), "south": ([2 * d + w, d], [w, h]), "north": ([d, d], [w, h]),
    }
    faces = {}
    for face, (frm, sz) in layout.items():
        fx, fy = frm
        sx, sy = sz
        if mirror:
            fx, sx = fx + sx, -sx
        faces[face] = {"uv": [u + fx, v + fy, u + fx + sx, v + fy + sy], "texture": 0}
    if mirror:
        faces["east"], faces["west"] = faces["west"], faces["east"]
    return faces


def cube_element(registry: str, bone: dict[str, Any], index: int, cube: dict[str, Any], color: int) -> dict[str, Any]:
    origin = vec(cube.get("origin"))
    size = vec(cube.get("size"))
    frm = [-(origin[0] + size[0]), origin[1], origin[2]]
    to = [-origin[0], origin[1] + size[1], origin[2] + size[2]]
    pivot = vec(cube.get("pivot", bone.get("pivot", [0, 0, 0])))
    rotation = vec(cube.get("rotation"))
    is_box = not isinstance(cube.get("uv"), dict)
    bone_mirror = bool(bone.get("mirror", False))
    mirror = bool(cube.get("mirror", bone_mirror))
    count = len(bone.get("cubes", []))
    el: dict[str, Any] = {
        "name": bone["name"] if count == 1 else f"{bone['name']}_cube{index}",
        "box_uv": is_box, "rescale": False, "locked": False, "light_emission": 0, "render_order": "default",
        "allow_mirror_modeling": True, "from": frm, "to": to, "autouv": 0, "color": color,
        "inflate": cube.get("inflate", 0), "origin": flip_x(pivot), "rotation": flip_rot(rotation),
        "visibility": True, "type": "cube", "uuid": bb_uuid(registry, "cube", bone["name"], str(index)),
    }
    if is_box:
        uv = vec(cube.get("uv", [0, 0]), 2)
        el["uv_offset"] = uv
        el["mirror_uv"] = mirror
        el["faces"] = box_faces(uv[0], uv[1], size, mirror)
    else:
        faces = {}
        for face in FACES:
            f = cube["uv"].get(face)
            if f and "uv" in f:
                u, v = vec(f["uv"], 2)
                w, h = vec(f.get("uv_size", [0, 0]), 2)
                faces[face] = {"uv": [u, v, u + w, v + h], "texture": 0}
            else:
                faces[face] = {"uv": [0, 0, 0, 0], "texture": None}
        el["faces"] = faces
    return el


def build_bbmodel(species: "Species", geo: dict[str, Any], anim: dict[str, Any],
                  textures: list[tuple[str, bytes, tuple[int, int]]], warnings: Warnings) -> dict[str, Any]:
    registry = species.registry
    g = geo["minecraft:geometry"][0]
    desc = g["description"]
    tw, th = desc.get("texture_width", 64), desc.get("texture_height", 64)
    bones, by_name, _ = bone_maps(geo)
    all_box = all(not isinstance(c.get("uv"), dict) for b in bones for c in b.get("cubes", []))
    elements: list[dict[str, Any]] = []
    groups: dict[str, dict[str, Any]] = {}
    for idx, b in enumerate(bones):
        color = idx % 8
        grp = {
            "name": b["name"], "origin": flip_x(b.get("pivot", [0, 0, 0])), "rotation": flip_rot(b.get("rotation")),
            "bedrock_binding": b.get("binding", ""), "color": color, "uuid": bb_uuid(registry, "bone", b["name"]),
            "export": True, "mirror_uv": bool(b.get("mirror", False)), "isOpen": False, "locked": False,
            "visibility": True, "autouv": 0, "children": [],
        }
        for ci, c in enumerate(b.get("cubes", [])):
            el = cube_element(registry, b, ci, c, color)
            elements.append(el)
            grp["children"].append(el["uuid"])
        groups[b["name"]] = grp
    outliner: list[dict[str, Any]] = []
    for b in bones:
        grp = groups[b["name"]]
        parent = b.get("parent")
        if parent and parent in groups:
            groups[parent]["children"].append(grp)
        else:
            if parent:
                warnings.add(registry, "BONE_PARENT_MISSING", f"bone {b['name']} names parent {parent} which does not exist; placed at the root")
            outliner.append(grp)
    tex_entries = []
    for i, (name, data, (w, h)) in enumerate(textures):
        tex_entries.append({
            "path": "", "name": name, "folder": "", "namespace": "", "id": str(i), "width": w, "height": h,
            "uv_width": tw, "uv_height": th, "particle": False, "layers_enabled": False, "sync_to_project": "",
            "render_mode": "default", "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop",
            "frame_order": "", "frame_interpolate": False, "visible": True, "mode": "bitmap", "saved": True,
            "uuid": bb_uuid(registry, "texture", name), "relative_path": f"textures/{name}",
            "source": "data:image/png;base64," + base64.b64encode(data).decode("ascii"),
        })
    extras = {k: v for k, v in desc.items() if k not in KNOWN_DESCRIPTION_KEYS}
    identifier = str(desc.get("identifier", f"geometry.{registry}"))
    model_identifier = identifier[len("geometry."):] if identifier.startswith("geometry.") else identifier
    bb: dict[str, Any] = {
        "meta": {"format_version": "4.5", "model_format": "bedrock", "box_uv": all_box},
        "name": registry, "model_identifier": model_identifier,
        "visible_box": [desc.get("visible_bounds_width", 1), desc.get("visible_bounds_height", 1),
                        vec(desc.get("visible_bounds_offset", [0, 0, 0]))[1]],
        "variable_placeholders": "", "variable_placeholder_buttons": [], "timeline_setups": [],
        "unhandled_root_fields": {STASH_KEY: extras} if extras else {},
        "resolution": {"width": tw, "height": th},
        "elements": elements, "outliner": outliner, "textures": tex_entries,
        "animations": bb_animations(registry, anim, {name: grp["uuid"] for name, grp in groups.items()}, warnings),
    }
    return bb


def normalize_channel(keys: Any) -> list[tuple[float, dict[str, Any]]]:
    """A GeckoLib/Bedrock channel in any of its shapes -> [(time, {"points": [[x,y,z], ...], "lerp": str, "extra": {...}})]."""
    def points_of(value: Any) -> tuple[list[list[Any]], str, dict[str, Any]]:
        if isinstance(value, list):
            return [list(value)], "linear", {}
        if isinstance(value, (int, float, str)):
            return [[value, value, value]], "linear", {}
        if isinstance(value, dict):
            if "vector" in value:
                pts, _, _ = points_of(value["vector"])
                extra = {k: v for k, v in value.items() if k not in ("vector", "lerp_mode")}
                return pts, value.get("lerp_mode", "linear"), extra
            pts = []
            if "pre" in value:
                pts.extend(points_of(value["pre"])[0])
            if "post" in value:
                pts.extend(points_of(value["post"])[0])
            extra = {k: v for k, v in value.items() if k not in ("pre", "post", "lerp_mode")}
            return pts, value.get("lerp_mode", "linear"), extra
        return [[0, 0, 0]], "linear", {}

    if isinstance(keys, dict) and not any(k in keys for k in ("post", "pre", "vector", "lerp_mode")):
        out = []
        for t, value in keys.items():
            pts, lerp, extra = points_of(value)
            out.append((float(t), {"points": pts, "lerp": lerp, "extra": extra}))
        return sorted(out, key=lambda x: x[0])
    pts, lerp, extra = points_of(keys)
    return [(0.0, {"points": pts, "lerp": lerp, "extra": extra})]


def bb_animations(registry: str, anim: dict[str, Any], group_uuid: dict[str, str], warnings: Warnings) -> list[dict[str, Any]]:
    out = []
    for name, clip in anim.get("animations", {}).items():
        loop = clip.get("loop", False)
        bb_loop = "hold" if loop == "hold_on_last_frame" else ("loop" if loop is True else "once")
        animators: dict[str, dict[str, Any]] = {}
        for bone, chans in clip.get("bones", {}).items():
            gid = group_uuid.get(bone)
            if gid is None:
                gid = bb_uuid(registry, "missing-bone", bone)
                warnings.add(registry, "CLIP_BONE_MISSING", f"clip {name} keys bone {bone} which the geo does not have")
            kfs = []
            for channel, keys in chans.items():
                if channel not in ("rotation", "position", "scale"):
                    warnings.add(registry, "CLIP_CHANNEL_UNKNOWN", f"clip {name} bone {bone}: channel {channel} is not rotation/position/scale")
                    continue
                for t, k in normalize_channel(keys):
                    kf: dict[str, Any] = {
                        "channel": channel, "data_points": [{"x": p[0], "y": p[1], "z": p[2]} for p in k["points"]],
                        "uuid": bb_uuid(registry, "kf", name, bone, channel, repr(t)), "time": t, "color": -1,
                        "interpolation": "catmullrom" if k["lerp"] == "catmullrom" else "linear",
                        "bezier_linked": True, "bezier_left_time": [-0.1, -0.1, -0.1], "bezier_left_value": [0, 0, 0],
                        "bezier_right_time": [0.1, 0.1, 0.1], "bezier_right_value": [0, 0, 0],
                    }
                    if k["extra"]:
                        kf["easing"] = k["extra"].get("easing")
                        if "easingArgs" in k["extra"]:
                            kf["easingArgs"] = k["extra"]["easingArgs"]
                    kfs.append(kf)
            animators[gid] = {"name": bone, "type": "bone", "keyframes": kfs}
        out.append({
            "uuid": bb_uuid(registry, "anim", name), "name": name, "loop": bb_loop, "override": False,
            "length": float(clip.get("animation_length", 0) or 0), "snapping": 24, "selected": False,
            "anim_time_update": "", "blend_weight": "", "start_delay": "", "loop_delay": "", "animators": animators,
        })
    return out


# --- the export emulation (what Blockbench would write back) ------------------------------------

def compile_cube(el: dict[str, Any], bone_mirror: bool) -> dict[str, Any]:
    frm, to = vec(el["from"]), vec(el["to"])
    size = [to[i] - frm[i] for i in range(3)]
    cube: dict[str, Any] = {"origin": [-(frm[0] + size[0]), frm[1], frm[2]], "size": size}
    if el.get("inflate"):
        cube["inflate"] = el["inflate"]
    if el.get("box_uv"):
        cube["uv"] = vec(el.get("uv_offset", [0, 0]), 2)
        if bool(el.get("mirror_uv", False)) != bone_mirror:
            cube["mirror"] = bool(el.get("mirror_uv", False))
    else:
        uv = {}
        for face in FACES:
            f = el.get("faces", {}).get(face)
            if f and f.get("texture") is not None:
                u0, v0, u1, v1 = vec(f["uv"], 4)
                uv[face] = {"uv": [u0, v0], "uv_size": [u1 - u0, v1 - v0]}
        cube["uv"] = uv
    rot = vec(el.get("rotation"))
    if any(abs(r) > 0 for r in rot):
        cube["pivot"] = flip_x(el.get("origin", [0, 0, 0]))
        cube["rotation"] = flip_rot(rot)
    return cube


def bbmodel_to_geo(bb: dict[str, Any]) -> tuple[dict[str, Any], list[str]]:
    """The geo JSON a Blockbench bedrock export would write, plus what this importer re-attached from the stash."""
    res = bb.get("resolution", {})
    desc: dict[str, Any] = {"identifier": "geometry." + str(bb.get("model_identifier", bb.get("name", "unknown"))),
                            "texture_width": res.get("width", 16), "texture_height": res.get("height", 16)}
    vb = bb.get("visible_box")
    if vb and (vb[0] != 1 or vb[1] != 1 or vb[2] != 0):
        desc["visible_bounds_width"] = vb[0]
        desc["visible_bounds_height"] = vb[1]
        desc["visible_bounds_offset"] = [0, vb[2], 0]
    reattached = []
    for k, v in bb.get("unhandled_root_fields", {}).get(STASH_KEY, {}).items():
        desc[k] = v
        reattached.append(k)
    project_box = bool(bb.get("meta", {}).get("box_uv", False))
    elements = {e["uuid"]: e for e in bb.get("elements", [])}
    bones: list[dict[str, Any]] = []

    def walk(group: dict[str, Any], parent: str | None) -> None:
        bone: dict[str, Any] = {"name": group["name"]}
        if parent:
            bone["parent"] = parent
        bone["pivot"] = flip_x(group.get("origin", [0, 0, 0]))
        rot = flip_rot(group.get("rotation"))
        if any(abs(r) > 0 for r in rot):
            bone["rotation"] = rot
        if group.get("bedrock_binding"):
            bone["binding"] = group["bedrock_binding"]
        bone_mirror = bool(group.get("mirror_uv", False))
        if bone_mirror and project_box:
            bone["mirror"] = True
        cubes, child_groups = [], []
        for ch in group.get("children", []):
            if isinstance(ch, str):
                el = elements.get(ch)
                if el and el.get("export", True) and el.get("type") == "cube":
                    cubes.append(compile_cube(el, bool(bone.get("mirror", False))))
            elif isinstance(ch, dict):
                child_groups.append(ch)
        if cubes:
            bone["cubes"] = cubes
        bones.append(bone)
        for cg in child_groups:
            walk(cg, group["name"])

    for g in bb.get("outliner", []):
        if isinstance(g, dict):
            walk(g, None)
    return {"format_version": "1.12.0", "minecraft:geometry": [{"description": desc, "bones": bones}]}, reattached


def time_key(t: float) -> str:
    return fmt(t, 4) if t != int(t) else f"{int(t)}.0"


def bbmodel_to_animation(bb: dict[str, Any]) -> dict[str, Any]:
    anims: dict[str, Any] = {}
    for a in bb.get("animations", []):
        clip: dict[str, Any] = {}
        if a.get("loop") == "loop":
            clip["loop"] = True
        elif a.get("loop") == "hold":
            clip["loop"] = "hold_on_last_frame"
        clip["animation_length"] = a.get("length", 0)
        bones: dict[str, Any] = {}
        for animator in a.get("animators", {}).values():
            if animator.get("type") != "bone":
                continue
            chans: dict[str, dict[str, Any]] = defaultdict(dict)
            for kf in sorted(animator.get("keyframes", []), key=lambda k: k["time"]):
                vals = [[p["x"], p["y"], p["z"]] for p in kf["data_points"]]
                plain = kf.get("interpolation", "linear") == "linear" and not kf.get("easing") and len(vals) == 1
                if plain:
                    value: Any = vals[0]
                else:
                    value = {"post": vals[-1]}
                    if len(vals) == 2:
                        value["pre"] = vals[0]
                    if kf.get("interpolation") == "catmullrom":
                        value["lerp_mode"] = "catmullrom"
                    if kf.get("easing"):
                        value["easing"] = kf["easing"]
                        if "easingArgs" in kf:
                            value["easingArgs"] = kf["easingArgs"]
                chans[kf["channel"]][time_key(float(kf["time"]))] = value
            bones[animator["name"]] = dict(chans)
        clip["bones"] = bones
        anims[a["name"]] = clip
    return {"format_version": "1.8.0", "animations": anims}


# --- the semantic diff ---------------------------------------------------------------------------

def _close(a: Any, b: Any, tol: float = 1e-6) -> bool:
    if isinstance(a, (int, float)) and isinstance(b, (int, float)):
        return abs(float(a) - float(b)) <= tol
    if isinstance(a, list) and isinstance(b, list):
        return len(a) == len(b) and all(_close(x, y, tol) for x, y in zip(a, b))
    return a == b


def cube_signature(cube: dict[str, Any], bone_mirror: bool) -> dict[str, Any]:
    sig: dict[str, Any] = {"origin": vec(cube.get("origin")), "size": vec(cube.get("size")), "inflate": cube.get("inflate", 0),
                           "pivot": vec(cube.get("pivot")) if cube.get("rotation") else None,
                           "rotation": vec(cube.get("rotation")) if cube.get("rotation") else None}
    uv = cube.get("uv")
    if isinstance(uv, dict):
        sig["faces"] = {f: [*vec(d["uv"], 2), *vec(d.get("uv_size", [0, 0]), 2)] for f, d in uv.items() if "uv" in d}
    else:
        sig["box_uv"] = vec(uv, 2) if uv is not None else None
        sig["mirror"] = bool(cube.get("mirror", bone_mirror))
    return sig


def signature_differences(xs: dict[str, Any], ys: dict[str, Any]) -> list[str]:
    """The keys on which two cube signatures differ, as `key: a -> b` strings (faces compared per face)."""
    out: list[str] = []
    for k in xs:
        if k == "faces":
            for f, uvw in xs[k].items():
                if f not in ys.get("faces", {}) or not _close(uvw, ys["faces"][f]):
                    out.append(f"uv.{f}: {uvw} -> {ys.get('faces', {}).get(f)}")
        elif not _close(xs[k], ys.get(k)):
            out.append(f"{k}: {xs[k]} -> {ys.get(k)}")
    return out


ROUNDTRIP_TIME_TOLERANCE_SECONDS = 5e-5
ROUNDTRIP_TIME_NOTE = ("key TIMES compare within 5e-5 s (owner 2026-09-13, addendum item 26 (5)): the Blockbench emulation writes 4-decimal "
                       "timecodes while a transcription's key times are k/(N-1) s at 10 decimals (the first Tier-2 slice's Beaver round-trip "
                       "reported six time-only diffs); Blockbench's real timecode precision is read from its exporter source when the _preview "
                       "export (ruled 2026-09-06, Q15 (a): Blockbench-only, never shipped) is built, and the emulation follows it then. Values compare at 1e-6.")


def roundtrip_diff(shipped_geo: dict[str, Any], back_geo: dict[str, Any], shipped_anim: dict[str, Any],
                   back_anim: dict[str, Any], reattached: list[str]) -> dict[str, Any]:
    """Semantic diff of the shipped files against what the emulated Blockbench export wrote back. NOTE: the writer and
    the importer are this tool's own (the same memory of the Blockbench codec on both sides), so EQUAL proves the two
    halves agree with each other — the owner's hand-check of one real Blockbench export is the real test. Key times
    compare within ROUNDTRIP_TIME_TOLERANCE_SECONDS (ROUNDTRIP_TIME_NOTE), values within 1e-6."""
    diffs: list[str] = []
    dropped: list[str] = []
    sg, bg = shipped_geo["minecraft:geometry"][0], back_geo["minecraft:geometry"][0]
    sd, bd = sg["description"], bg["description"]
    for k in sd:
        if k not in bd:
            diffs.append(f"description key {k} lost")
        elif not _close(sd[k], bd[k]):
            diffs.append(f"description {k}: {sd[k]} -> {bd[k]}")
    s_names = [b["name"] for b in sg["bones"]]
    b_names = [b["name"] for b in bg["bones"]]
    order_ok = s_names == b_names
    if not order_ok:
        diffs.append(f"bone order/set differs: shipped {s_names[:12]}... vs back {b_names[:12]}...")
    s_by = {b["name"]: b for b in sg["bones"]}
    b_by = {b["name"]: b for b in bg["bones"]}
    for name in s_names:
        if name not in b_by:
            continue
        s, b = s_by[name], b_by[name]
        if s.get("parent") != b.get("parent"):
            diffs.append(f"{name}: parent {s.get('parent')} -> {b.get('parent')}")
        if not _close(vec(s.get("pivot")), vec(b.get("pivot"))):
            diffs.append(f"{name}: pivot {s.get('pivot')} -> {b.get('pivot')}")
        if not _close(vec(s.get("rotation")), vec(b.get("rotation"))):
            diffs.append(f"{name}: rotation {s.get('rotation')} -> {b.get('rotation')}")
        s_mirror, b_mirror = bool(s.get("mirror", False)), bool(b.get("mirror", False))
        sc, bc = s.get("cubes", []), b.get("cubes", [])
        if len(sc) != len(bc):
            diffs.append(f"{name}: cube count {len(sc)} -> {len(bc)}")
            continue
        for i, (x, y) in enumerate(zip(sc, bc)):
            for k in x:
                if k in DROPPED_CUBE_KEYS:
                    dropped.append(f"{name}.cubes[{i}].{k}")
            xs, ys = cube_signature(x, s_mirror), cube_signature(y, b_mirror)
            for d in signature_differences(xs, ys):
                diffs.append(f"{name}.cubes[{i}].{d}")
        for k in s:
            if k not in ("name", "parent", "pivot", "rotation", "mirror", "cubes", "binding"):
                dropped.append(f"{name}.{k}")
    # animations
    sa, ba = shipped_anim.get("animations", {}), back_anim.get("animations", {})
    if list(sa) != list(ba):
        diffs.append(f"clip names/order: {list(sa)} -> {list(ba)}")
    for name, clip in sa.items():
        back = ba.get(name)
        if back is None:
            continue
        s_loop = clip.get("loop", False)
        b_loop = back.get("loop", False)
        if s_loop != b_loop:
            diffs.append(f"clip {name}: loop {s_loop} -> {b_loop}")
        if not _close(clip.get("animation_length", 0), back.get("animation_length", 0)):
            diffs.append(f"clip {name}: length {clip.get('animation_length')} -> {back.get('animation_length')}")
        for bone, chans in clip.get("bones", {}).items():
            bchans = back.get("bones", {}).get(bone)
            if bchans is None:
                diffs.append(f"clip {name}: bone {bone} lost")
                continue
            for ch, keys in chans.items():
                s_keys = normalize_channel(keys)
                b_keys = normalize_channel(bchans.get(ch, {}))
                if len(s_keys) != len(b_keys):
                    diffs.append(f"clip {name} {bone}.{ch}: {len(s_keys)} keys -> {len(b_keys)}")
                    continue
                for (st, sk), (bt, bk) in zip(s_keys, b_keys):
                    if not _close(st, bt, ROUNDTRIP_TIME_TOLERANCE_SECONDS) or sk["lerp"] != bk["lerp"] or len(sk["points"]) != len(bk["points"]) \
                            or not all(_close(p, q) for p, q in zip(sk["points"], bk["points"])):
                        diffs.append(f"clip {name} {bone}.{ch} @ {st}: {sk} -> {bk}")
                        break
                    # easing and every other keyframe key beside the points: a dropped easing or an unknown key IS a difference
                    s_extra = {k: v for k, v in sk["extra"].items() if v is not None}
                    b_extra = {k: v for k, v in bk["extra"].items() if v is not None}
                    if s_extra != b_extra:
                        lost = sorted(set(s_extra) - set(b_extra))
                        diffs.append(f"clip {name} {bone}.{ch} @ {st}: keyframe keys {s_extra} -> {b_extra}" + (f" (lost: {lost})" if lost else ""))
                        for k in lost:
                            dropped.append(f"clip {name} {bone}.{ch} @ {st}: {k}")
                        break
    return {"equal": not diffs, "bone_order_preserved": order_ok, "differences": diffs, "dropped_keys": sorted(set(dropped)),
            "reattached_by_this_importer": reattached,
            "time_tolerance_seconds": ROUNDTRIP_TIME_TOLERANCE_SECONDS, "time_tolerance_note": ROUNDTRIP_TIME_NOTE,
            "note": "a real Blockbench geo re-export drops the dropped_keys and the reattached description keys; the artist returns the animation file, never the geo. "
                    "This round-trip is the tool's writer against the tool's importer (one memory of the Blockbench codec on both sides): EQUAL means they agree "
                    "with each other; the owner's hand-check of one real Blockbench export is the real test."}


# ---------------------------------------------------------------------------
# INVENTORY.csv, README_FIRST.md, the package tree, the returned-folder check, the CLI
# ---------------------------------------------------------------------------

def inventory_rows(repo: "Repo", catalog: "TextureCatalog", manifests: dict[str, dict[str, Any]] | None = None) -> tuple[list[str], list[list[Any]]]:
    """One row per registration. `effort_hours` is the generator's estimate (or the seed's owner-set figure) for every
    landed species, taken from the manifests passed in (the package run) or computed here (the `inventory` subcommand)."""
    header = ["registry_name", "java_class", "category", "width", "height", "model_class", "tier", "artist_tier", "animation_class",
              "status", "status_note", "geo_file", "animation_file", "bbmodel_file", "spec_file", "bones", "cubes",
              "clips_shipped", "textures_referenced", "canonical_textures", "expected_scale", "expected_shadow",
              "pin_status", "harness_proof", "hitbox_profile", "locked_bones", "artist_scope", "effort_hours", "effort_source",
              "client_files", "notes"]
    manifests = dict(manifests or {})
    rows = []
    for s in repo.species.values():
        bones = cubes = clips = ""
        locked = ""
        scope = ""
        hours: Any = ""
        hours_source = ""
        if s.landed:
            geo = s.geo
            bl = geo["minecraft:geometry"][0]["bones"]
            bones = len(bl)
            cubes = sum(len(b.get("cubes", [])) for b in bl)
            clips = len(s.animation.get("animations", {}))
            locked = len(locked_bones(s, geo))
            seed = s.seed or {}
            scope = seed.get("artist_scope", "full contract" if s.tier in (1, 2) else "none (Tier 3)" if s.tier == 3 else "native rig")
            if s.registry not in manifests:
                inv = build_trigger_inventory(s, repo)
                _, manifests[s.registry] = spec_document(s, repo, catalog, inv)
            hours = fmt(float(manifests[s.registry].get("effort_hours", 0) or 0), 1)
            hours_source = manifests[s.registry].get("effort_source", "")
        pin = s.pin or {}
        canon = len({catalog.canonical[catalog.hash[t]] for t in s.textures if t in catalog.hash})
        rows.append([
            s.registry, s.java_class, s.category, fmt(s.width) if s.width is not None else "", fmt(s.height) if s.height is not None else "",
            s.model or "", s.tier if s.tier is not None else "", s.tier_label, s.animation_class, s.status, s.status_note,
            s.geo_path.name if s.geo_path else "", s.anim_path.name if s.anim_path else "",
            f"{s.registry}.bbmodel" if s.landed else "", "SPEC.md" if s.landed else "",
            bones, cubes, clips, len(s.textures), canon,
            pin.get("expected_scale", ""), pin.get("expected_shadow", ""), pin.get("status", "no pin"),
            s.proof or "", s.profile_name or "", locked, scope, hours, hours_source,
            ";".join(p.stem for p in s.client_files), "",
        ])
    return header, rows


def write_inventory(repo: "Repo", out_dir: Path, catalog: "TextureCatalog", manifests: dict[str, dict[str, Any]] | None = None) -> Path:
    header, rows = inventory_rows(repo, catalog, manifests)
    path = out_dir / "INVENTORY.csv"
    csv_write(path, header, rows)
    return path


def readme_document(repo: "Repo", manifests: dict[str, dict[str, Any]], packaged: list[str] | None = None) -> str:
    """README_FIRST.md. `packaged` (owner 2026-09-13, third set, item 28 (4)): the registries THIS package run wrote (the
    `--entities` set; the whole landed set when none) — the priority table lists those folders only, with one line that
    more folders follow as creatures land; the manifests' keys when not given."""
    packaged_set = list(packaged) if packaged is not None else list(manifests)
    landed = [s for s in repo.landed_species() if s.registry in packaged_set]

    def prio(s: "Species") -> tuple:
        return (0 if s.tier in (0, 1) else 1 if s.tier == 2 else 2, s.registry)

    L: list[str] = []
    L.append("# READ ME FIRST — OreSpawn animation handoff")
    L.append("")
    L.append(f"_Generated by `tools/artist_package.py` {TOOL_VERSION}. Every rule below, and every contract line in the sheets, rests on the "
             "mod owner's rulings of 2026-09-06 (each sheet cites the ruling where it applies); the one open item is the lock reject mode in rule 6._")
    L.append("")
    L.append("## What this is")
    L.append("")
    L.append("OreSpawn is a large Minecraft creature mod from 2013–2015, being rebuilt for Minecraft 1.21.1. Its monsters were animated in code: "
             "a handful of sine-wave formulas per creature, written once and never revisited. We are hiring you to replace those baked "
             "animations with hand-made ones — idle, walk, attack, hurt and so on — and, where a creature's sheet lists a wishlist, to add new ones. "
             "The models, bone names and textures are FIXED; your work is the motion (and, if asked, the textures).")
    L.append("")
    L.append("## Toolchain")
    L.append("")
    L.append("- **Blockbench** (free, blockbench.net). Each entity folder holds a `.bbmodel` you can open directly, and the raw `.geo.json` "
             "(the rig) with its texture(s) beside it; if you open the `.geo.json` instead, choose a **Bedrock Entity** project.")
    L.append("- Deliver one **`.animation.json`** per entity (Bedrock animation format 1.8.0 — Blockbench's default export for a Bedrock Entity project), "
             "and optionally the `.bbmodel` working file. Do NOT re-export the `.geo.json`: a re-export silently drops data the game needs; the rig you were given is the rig that ships.")
    L.append("")
    L.append("## Hard rules (the game breaks if these are broken)")
    L.append("")
    L.append("1. **Never rename, delete or re-parent a bone.** Code and hitboxes find bones by name. The glossary in each `SPEC.md` gives readable labels beside the fixed names; use the labels to understand, the names to work.")
    L.append("2. **Never rename a clip.** Every clip name in a `SPEC.md` table is fixed; the ones marked code-triggered are fired by the game by that exact name. You may ADD clips only where the sheet says so (`idle_alt_1`, `idle_alt_2`, ...).")
    L.append("3. **Set each clip's loop mode exactly as its table says**: `true` for cycles (idle, walk), `false` for one-shot actions (attack, hurt), `hold_on_last_frame` only where written (death). "
             "**Deliver `idle` and `walk` together**: the game switches a creature to your animations only when BOTH are in the file — one without the other leaves the creature on its old code-driven motion, and `check` says so.")
    L.append("4. **Keep texture canvas sizes.** A 64x32 texture stays 64x32.")
    L.append("5. **Loop length by controller kind.** A phase-locked creature (every sheet but the Queen's today): author every loop at 1.0 s — in-game the "
             "length is free, the loop plays at the creature's own tempo and the sheet's tempo table gives the rate (contract §9; ruled 2026-09-06, Q14 (a)). "
             "A native creature (the Queen): keep each clip's shipped length — her controllers play the clip as authored. Each sheet's manifest says which "
             "kind applies (`controller_kind`: `phase_locked` or `native`), and one-shot clips keep the length their row states.")
    L.append(f"6. **A bone the sheet marks `locked` carries or parents a hitbox part (contract §8.1).** {LOCK_POLICY} {LOCK_REJECT_MODE}")
    L.append("7. Rotation / position / scale keys, linear or Catmull-Rom curves (and GeckoLib easings) are fine; sound and particle keys only on one-shot clips (attack, hurt, death). "
             "**No event keyframes on loops** (idle, walk and the other cycles) — code-fired events come from the trigger inventory in each sheet, not from keys on a cycle (a loop plays under a phase lock that would fire such a key once, ever). "
             "Exception: a creature whose controllers are its own GeckoLib controllers (its sheet says controller kind `native` — the Queen) is not phase-locked, so its loops may carry event keys; they fire once per loop, and `check` notes each one (owner 2026-09-13). "
             "No Molang expressions, no custom-instruction keys, "
             "no `_preview` files in a delivery (a Blockbench-only aid, never in the jar — ruled 2026-09-06, Q15 (a); the checker warns). "
             + README_REFERENCE_SENTENCE)
    L.append("")
    L.append("## What is in each entity folder")
    L.append("")
    L.append("```")
    L.append("entities/<registry_name>/")
    L.append("  <name>.geo.json          the rig (do not edit, do not re-export)")
    L.append("  <name>.animation.json    the current clips (empty for creatures that ship none yet)")
    L.append("  <registry_name>_reference.animation.json")
    L.append("                           REFERENCE-ONLY: the creature's classic code sampled at fixed inputs, to look at beside the rig")
    L.append("                           (SPEC §4.3); never edit, return or ship it — the checker rejects it by name")
    L.append("  <registry_name>.bbmodel  a Blockbench project of the same rig, textures embedded")
    L.append("  textures/                the texture(s) to edit, one canonical copy each")
    L.append("  SPEC.md                  the creature's sheet: what it is, size, bone glossary, current motion in plain English,")
    L.append("                           what to improve / leave, loop-mode table, what fires each clip, locked bones, wishlist")
    L.append("  spec.manifest.json       the machine-readable contract the checker reads (do not edit)")
    L.append("  reference/               screenshot slots (filled by the owner from the game)")
    L.append("```")
    L.append("")
    L.append("## What 'done' looks like and how files come back")
    L.append("")
    L.append("Return a folder per entity, mirroring `entities/` (same folder name, same file names): the `.animation.json` under the exact name the sheet gives, any edited textures under `textures/` with their original names, and optionally your `.bbmodel`. "
             "Before sending, run `python tools/artist_package.py check <your entity folder>` (or ask the owner to): it reports every rule above as PASS / WARN / REJECT and ends with a summary line. A folder with no REJECT is done.")
    L.append("")
    L.append("**What `check` REJECTS** (the folder is not done):")
    L.append("")
    for r in CHECK_REJECTS:
        L.append(f"- {r}")
    L.append("")
    L.append("**What `check` WARNS about** (delivered, but read the line):")
    L.append("")
    for w in CHECK_WARNS:
        L.append(f"- {w}")
    L.append("")
    L.append("## Priority order and effort")
    L.append("")
    L.append("Bosses first (they carry hitboxes and need the most care), then the ordinary creatures, then nothing for the code-driven props (no animation work: they are listed so their textures and sheets exist). "
             "The table lists the folders in THIS package only.")
    L.append("")
    rows = []
    for s in sorted(landed, key=prio):
        m = manifests.get(s.registry, {})
        clip_rows_ = m.get("clips", [])
        if m.get("controller_kind") == "native":
            # a native creature's row is its pilot scope (owner 2026-09-13, third set, item 28 (4)): the clips the seed marks
            # improve / author are the delivery; the `leave` clips come back as shipped, later
            deliver = [c["name"] for c in clip_rows_ if c.get("verdict", "author") in ("author", "improve")]
            later = [c["name"] for c in clip_rows_ if c.get("verdict") == "leave"]
            clips_text = ", ".join(f"`{n}`" for n in deliver) if deliver else "none (no animation work)"
            if later:
                clips_text += f" — {'her' if s.registry == 'the_queen' else 'its'} other {len(later)} clip{'s' if len(later) != 1 else ''}: later, returned as shipped"
        else:
            clips = [c["name"] for c in clip_rows_]
            clips_text = ", ".join(clips) if clips else "none (no animation work)"
        rows.append([s.registry, m.get("display_name", s.java_class), m.get("tier_label", s.tier_label), len(m.get("bones", [])),
                     clips_text, fmt(float(m.get("effort_hours", 0) or 0), 1) + " h" if clip_rows_ else "0 h"])
    L.append(md_table(["folder", "creature", "tier", "bones", "clips to deliver", "estimated effort"], rows))
    L.append("")
    L.append(f"More folders follow as creatures land through the seam; this package carries {len(rows)}.")
    L.append("")
    L.append("Effort figures are the generator's estimate (bosses 8 h + 0.15 h per bone + 1.5 h per clip to author or improve; others 4 h + 0.2 h per bone + 1 h per clip; "
             "a clip marked 'leave' or 'covered by idle' is not counted) unless the sheet states an owner-set figure. "
             "'Tier 1 (boss; the design's 'done' row)' is a creature the migration design lists as done (a native rig) and the animation contract treats as its Tier-1 boss — both are true.")
    L.append("")
    return "\n".join(L) + "\n"


def build_package(repo: "Repo", out_dir: Path, registries: list[str] | None = None, with_roundtrip: bool = True) -> dict[str, Any]:
    """The whole tree for the landed species (the dry run); returns the summary."""
    # The repository's artist_handoff/ was refused as an output until the mirror drop landed (owner 2026-09-05, addendum
    # item 23 (8)(f)); the drop landed (d51f06f) and the pilot pair's package is generated there (owner 2026-09-13, second
    # set, item 10) - an --out under it is an ordinary output now.
    catalog = TextureCatalog(repo)
    out_dir.mkdir(parents=True, exist_ok=True)
    species = [repo.get(r) for r in registries] if registries else repo.landed_species()
    manifests: dict[str, dict[str, Any]] = {}
    summary: dict[str, Any] = {"tool": TOOL_VERSION, "out_dir": str(out_dir), "entities": [], "texture_map": None, "warnings": []}
    for s in species:
        if not s.landed:
            repo.warnings.add(s.registry, "NOT_LANDED", "no geo: nothing to package")
            continue
        folder = out_dir / "entities" / s.registry
        folder.mkdir(parents=True, exist_ok=True)
        geo = s.geo
        anim = s.animation
        write_text(folder / s.geo_path.name, read_text(s.geo_path))
        anim_name = s.anim_path.name if s.anim_path else f"{s.registry}.animation.json"
        write_text(folder / anim_name, read_text(s.anim_path) if s.anim_path else json.dumps(anim, indent=2) + "\n")
        inv = build_trigger_inventory(s, repo)
        spec_md, manifest = spec_document(s, repo, catalog, inv)
        write_text(folder / "SPEC.md", spec_md)
        write_json(folder / "spec.manifest.json", manifest)
        write_text(folder / "reference" / "SLOTS.md", slots_document(s))
        # owner 2026-09-13, second set, item 27 (3): the reference-only clip beside the sheet, byte for byte from
        # tools/reference_clips/ (the sampler's output; its sha256 is in the manifest), under the same name.
        if manifest.get("reference_clip"):
            src = repo.paths.reference_clips / manifest["reference_clip"]["file"]
            (folder / src.name).write_bytes(src.read_bytes())
        textures = []
        for t in catalog.canonical_for_species(s):
            if t["stray"]:
                continue
            src = repo.paths.tex_dir / t["canonical"]
            data = src.read_bytes()
            (folder / "textures").mkdir(exist_ok=True)
            (folder / "textures" / t["canonical"]).write_bytes(data)
            textures.append((t["canonical"], data, (t["width"], t["height"])))
        bb = build_bbmodel(s, geo, anim, textures, repo.warnings)
        write_bbmodel(folder / f"{s.registry}.bbmodel", bb)
        rt = None
        if with_roundtrip:
            back_geo, reattached = bbmodel_to_geo(bb)
            back_anim = bbmodel_to_animation(bb)
            rt = roundtrip_diff(geo, back_geo, anim, back_anim, reattached)
            write_json(folder / "roundtrip.report.json", rt)
            if not rt["equal"]:
                repo.warnings.add(s.registry, "ROUNDTRIP_DIFF", f"{len(rt['differences'])} difference(s): {rt['differences'][:3]}")
        manifests[s.registry] = manifest
        bl = geo["minecraft:geometry"][0]["bones"]
        keyed_sets = list(manifest.get("keyed_locked_by_shipped_clip", {}).values())
        summary["entities"].append({
            "registry": s.registry, "tier": s.tier, "tier_label": s.tier_label,
            "files": sorted(str(p.relative_to(folder)).replace("\\", "/") for p in folder.rglob("*") if p.is_file()),
            "bones": len(bl), "cubes": sum(len(b.get("cubes", [])) for b in bl),
            "clips_shipped": len(anim.get("animations", {})), "clips_in_spec": len(manifest["clips"]),
            "goals": len(inv.get("goals", [])), "flags": len(inv.get("flags", [])),
            "attacking": (inv.get("attacking") or {}).get("verdict"),
            "strike_sites": len(inv.get("combat", {}).get("melee", [])) + len(inv.get("combat", {}).get("ranged", [])),
            "textures_mapped": len(textures), "locked_bones": len(manifest["locked_bones"]), "lock_mode": manifest["lock_mode"],
            "keyed_locked_bones": len(set().union(*keyed_sets)) if keyed_sets else 0,
            "effort_hours": manifest.get("effort_hours"),
            "unlabelled_bones": sum(1 for w in repo.warnings.for_scope(s.registry) if w[1] == "BONE_UNLABELLED"),
            "roundtrip": None if rt is None else {"equal": rt["equal"], "bone_order_preserved": rt["bone_order_preserved"],
                                                   "differences": len(rt["differences"]), "dropped_keys": len(rt["dropped_keys"]),
                                                   "reattached": rt["reattached_by_this_importer"]},
            "warnings": [f"{code}: {msg}" for _, code, msg in repo.warnings.for_scope(s.registry)],
        })
    # the README's priority table lists THIS run's folders only (owner 2026-09-13, third set, item 28 (4)); INVENTORY.csv and
    # TEXTURE_MAP.csv stay package-wide
    write_text(out_dir / "README_FIRST.md", readme_document(repo, manifests, packaged=list(manifests)))
    write_inventory(repo, out_dir, catalog, manifests)
    _, tex_summary = write_texture_map(repo, out_dir, catalog)
    summary["texture_map"] = tex_summary
    summary["warnings"] = [f"[{scope}] {code}: {msg}" for scope, code, msg in repo.warnings.items]
    write_json(out_dir / "dryrun_summary.json", summary)
    write_text(out_dir / "dryrun_summary.md", summary_markdown(summary))
    write_text(out_dir / "warnings.txt", "\n".join(summary["warnings"]) + "\n")
    return summary


def summary_markdown(summary: dict[str, Any]) -> str:
    L = [f"# Dry-run summary — {summary['tool']}", "", f"Output: `{summary['out_dir']}`", ""]
    rows = []
    for e in summary["entities"]:
        rt = e["roundtrip"]
        rt_text = "-" if rt is None else ("EQUAL, order kept" if rt["equal"] and rt["bone_order_preserved"] else f"{rt['differences']} diff(s), order {'kept' if rt['bone_order_preserved'] else 'CHANGED'}")
        locked_text = f"{e['locked_bones']} ({e.get('keyed_locked_bones', 0)} keyed by the shipped clips; {e['lock_mode']})" if e["locked_bones"] else "0"
        rows.append([e["registry"], e.get("tier_label", e["tier"]), len(e["files"]), e["bones"], e["cubes"], e["clips_shipped"], e["clips_in_spec"],
                     e["goals"], e["flags"], e["attacking"] or "-", e["strike_sites"], e["textures_mapped"],
                     locked_text, e["unlabelled_bones"], rt_text, fmt(float(e.get("effort_hours") or 0), 1) + " h", len(e["warnings"])])
    L.append(md_table(["entity", "tier", "files", "bones", "cubes", "clips shipped", "clips in SPEC", "goals", "flags", "attacking",
                       "strike/launch sites", "textures", "locked", "unlabelled", "round-trip", "effort", "warnings"], rows))
    L.append("")
    t = summary.get("texture_map") or {}
    if t:
        L.append(f"TEXTURE_MAP: {t.get('shipped')} shipped, {t.get('unique_payloads')} unique payloads, {t.get('duplicate_groups')} duplicate groups, "
                 f"{t.get('redundant_names')} redundant names, {t.get('referenced_by_java')} referenced by Java, {t.get('strays')} strays; law series {t.get('law_series')}.")
        L.append("")
    L.append("## Warnings")
    L.append("")
    for w in summary["warnings"]:
        L.append(f"- {w}")
    if not summary["warnings"]:
        L.append("- none")
    L.append("")
    return "\n".join(L)


# --- check: validate a returned artist folder ------------------------------------------------------

def normalize_loop(value: Any) -> str:
    if value is True or value == "true":
        return "true"
    if value == "hold_on_last_frame":
        return "hold_on_last_frame"
    return "false"


KEYFRAME_KEYS = {"vector", "pre", "post", "lerp_mode", "easing", "easingArgs"}
LERP_MODES = {"linear", "catmullrom", "bezier", "step"}
CHANNELS = ("rotation", "position", "scale")


def _finite_number(v: Any) -> bool:
    return isinstance(v, (int, float)) and not isinstance(v, bool) and math.isfinite(v)


def validate_channel(where: str, channel: str, keys: Any, findings: list[tuple[str, str]]) -> tuple[float, int]:
    """Every keyframe value of one channel (rule 7 and the numeric rules): a Molang string, a non-numeric or non-finite
    value, a rotation beyond ROTATION_BOUND_DEG or a position beyond POSITION_BOUND is a REJECT; a number written as a
    string, an unknown keyframe key or an unknown lerp_mode is a WARN. Returns (the last key time, values checked)."""
    checked = 0
    last_time = 0.0

    def component(v: Any, at: str) -> None:
        nonlocal checked
        checked += 1
        if isinstance(v, str):
            try:
                f = float(v)
            except ValueError:
                findings.append(("REJECT", f"{at}: value {v!r} is a Molang expression / text (rule 7: numbers only)"))
                return
            findings.append(("WARN", f"{at}: number written as a string ({v!r})"))
            v = f
        if not _finite_number(v):
            findings.append(("REJECT", f"{at}: value {v!r} is not a finite number"))
            return
        if channel == "rotation" and abs(v) > ROTATION_BOUND_DEG:
            findings.append(("REJECT", f"{at}: rotation {v} exceeds the bound |rotation| <= {ROTATION_BOUND_DEG:.0f} degrees"))
        elif channel == "position" and abs(v) > POSITION_BOUND:
            findings.append(("REJECT", f"{at}: position {v} exceeds the bound |position| <= {POSITION_BOUND:.0f} units"))

    def value(v: Any, at: str) -> None:
        if isinstance(v, list):
            if len(v) != 3:
                findings.append(("REJECT", f"{at}: a value must have three components, found {len(v)}"))
            for c in v:
                component(c, at)
        elif isinstance(v, dict):
            for k in v:
                if k not in KEYFRAME_KEYS:
                    findings.append(("WARN", f"{at}: unknown keyframe key '{k}' (the game ignores it; a re-export may drop it)"))
            for k in ("vector", "pre", "post"):
                if k in v:
                    value(v[k], f"{at}.{k}")
            if not any(k in v for k in ("vector", "pre", "post")):
                findings.append(("REJECT", f"{at}: a keyframe object without vector / pre / post"))
            if "lerp_mode" in v and v["lerp_mode"] not in LERP_MODES:
                findings.append(("WARN", f"{at}: lerp_mode {v['lerp_mode']!r} is not one GeckoLib knows ({', '.join(sorted(LERP_MODES))})"))
            if "easing" in v and not isinstance(v["easing"], str):
                findings.append(("REJECT", f"{at}: easing must be a name, found {v['easing']!r}"))
        else:
            component(v, at)  # a scalar applied to every axis

    if isinstance(keys, dict) and not any(k in keys for k in ("post", "pre", "vector", "lerp_mode")):
        for t, v in keys.items():
            try:
                tf = float(t)
            except (TypeError, ValueError):
                findings.append(("REJECT", f"{where}: key time {t!r} is not a number"))
                continue
            if not math.isfinite(tf) or tf < 0:
                findings.append(("REJECT", f"{where}: key time {t!r} must be a finite time >= 0"))
                continue
            last_time = max(last_time, tf)
            value(v, f"{where} @ {t}")
    else:
        value(keys, f"{where} @ 0")
    return last_time, checked


def _shown(v: Any) -> Any:
    """A manifest / geo value in a finding: `absent` when the file has none (a bone without a rotation), the value otherwise."""
    return "absent" if v is None else v


def _bone_fingerprint_matches(mb: dict[str, Any], b: dict[str, Any]) -> bool:
    """True when a returned bone carries the manifest bone's body — pivot, bind rotation and cube signatures — which is what a
    rename keeps and a deletion loses; the geo comparison uses it to tell a bone renamed from one deleted."""
    try:
        if not _close(vec(b.get("pivot")), vec(mb.get("pivot"))) or not _close(vec(b.get("rotation")), vec(mb.get("rotation"))):
            return False
        sigs = mb.get("cubes")
        if sigs is None:
            return True
        cubes = b.get("cubes") or []
        mirror = bool(b.get("mirror", False))
        return len(cubes) == len(sigs) and not any(signature_differences(sig, cube_signature(c, mirror)) for c, sig in zip(cubes, sigs))
    except (TypeError, ValueError, AttributeError, KeyError):
        return False


def check_folder(folder: Path, manifest_path: Path | None = None, lock_mode_override: str | None = None) -> tuple[list[tuple[str, str]], bool]:
    """(findings [(severity, message)], passed). Severity: REJECT / WARN / OK. Implements CHECK_REJECTS / CHECK_WARNS
    exactly (README_FIRST and SPEC §11 quote those lists); the last lines are always a locked-bone summary naming the
    policy (LOCK_POLICY: a key on a locked bone WARNS — a REJECT only under lock_mode `reject`; a locked bone renamed,
    re-parented or deleted in a returned geo is a REJECT that names the bone) and a "checked ..." summary — never
    "nothing to report". A returned geo is compared bone by bone; a bone missing from it is matched by its body (pivot, bind
    rotation, cubes) against the bones the shipped rig has not, to say `renamed to Y` or `deleted` (`renamed or deleted` only
    when several match); a duplicated name is refused by name and never compared; the set/order line's order verdict compares
    the ORDER of the names both rigs share."""
    findings: list[tuple[str, str]] = []
    if not folder.exists() or not folder.is_dir():
        return [("REJECT", f"folder {folder} does not exist: nothing was returned")], False
    manifest_path = manifest_path or folder / "spec.manifest.json"
    if not manifest_path.exists():
        return [("REJECT", f"no manifest at {manifest_path} (the entity folder's spec.manifest.json, or --manifest)")], False
    delivered = [p for p in folder.rglob("*") if p.is_file() and p.resolve() != manifest_path.resolve()]
    if not delivered:
        findings.append(("REJECT", f"folder {folder} is empty: nothing was returned"))
    m = load_json(manifest_path)
    # the SPEC's controller kind (owner 2026-09-13, addendum item 26 (6)); a 0.2.3 manifest carries only the `native` flag
    native_controllers = m.get("controller_kind", "native" if m.get("native") else "phase_locked") == "native"
    bone_names = [b["name"] for b in m["bones"]]
    by_manifest = {b["name"]: b for b in m["bones"]}
    locked = set(m.get("locked_bones", []))
    lock_mode = lock_mode_override or m.get("lock_mode", LOCK_MODE_DEFAULT)
    if lock_mode not in ("warn", "reject"):  # neither the ruled policy nor the reject mode: refused, never a silent warn
        source = "the --lock-mode override" if lock_mode_override else f"{manifest_path.name}'s lock_mode"
        findings.append(("REJECT", f"lock_mode '{lock_mode}' is not warn or reject ({source}; `warn` is the ruled policy, `reject` the mode kept for "
                                   "the day the server-side hitbox evaluator lands — regenerate the package or pass --lock-mode warn|reject)"))
    policy_seen = m.get("lock_policy")  # the value itself is never echoed: a 0.2.0 manifest's is the pre-ruling sentence
    if policy_seen != LOCK_POLICY_ID:  # a package generated before the ruling, or one that never carried the token
        findings.append(("WARN", f"{manifest_path.name}: lock_policy is {'absent, not' if policy_seen is None else 'not'} `{LOCK_POLICY_ID}` — "
                         "the package predates the 2026-09-06 ruling; regenerate it (the checker applies the ruled policy regardless)"))
    locked_structural: list[str] = []  # locked bones renamed, re-parented or deleted in a returned geo (each a REJECT, by name)
    clips_by_name = {c["name"]: c for c in m.get("clips", [])}
    expected_anim = m.get("animation_file") or f"{m.get('registry', 'entity')}.animation.json"
    for p in folder.rglob("*"):
        if p.is_file() and "_preview" in p.name:
            findings.append(("WARN", f"{p.name}: a _preview file is a Blockbench-only aid, never in the jar (ruled 2026-09-06, Q15 (a)); leave it out of the delivery"))
        if p.is_file() and p.name.endswith(REFERENCE_CLIP_SUFFIX):
            # owner 2026-09-13, second set, item 27 (3): the reference-only clip is never a delivery and never shipped. The package's
            # OWN copy coming back untouched (the manifest's file name and sha256) is not a delivery - a WARN naming it, so the
            # generated folder itself checks PASS; any other `*_reference.animation.json` (edited, or a name the sheet never gave)
            # is a REJECT by name.
            own = m.get("reference_clip") or {}
            # the sampler writes LF and the sheet's sha256 is of that file; a copy whose line endings a transfer changed is still untouched
            returned_sha = hashlib.sha256(p.read_bytes().replace(b"\r\n", b"\n")).hexdigest()
            if own and p.name == own.get("file") and returned_sha == own.get("sha256"):
                findings.append(("WARN", f"{p.name}: the package's own reference-only clip came back untouched — not a delivery and never "
                                         "shipped (the classic code sampled at fixed inputs, to be looked at); leave it out of the returned folder"))
            else:
                findings.append(("REJECT", f"{p.name}: a reference-only clip that is not the package's own untouched copy"
                                           + (" (its bytes differ from the sheet's)" if own and p.name == own.get("file") else " (a name the sheet never gave)")
                                           + " — the reference clip is the classic code sampled at fixed inputs, never edited, never delivered, "
                                           "never shipped; remove it from the returned folder"))
    added_names: set[str] = set()  # bones a returned geo lists that the shipped rig has not (a key on one is "an added bone")
    renamed_names: dict[str, str] = {}  # returned name -> shipped name, for every rename a returned geo's fingerprints identify
    # --- a returned geo: must equal the shipped rig in every respect the manifest records ---
    for gp in sorted(folder.glob("*.geo.json")):
        try:
            g = load_json(gp)["minecraft:geometry"][0]
        except Exception as exc:  # noqa: BLE001 - the artist's file may be anything
            findings.append(("REJECT", f"{gp.name}: unreadable geo ({exc})"))
            continue
        returned_bones = [b for b in g.get("bones", []) if isinstance(b, dict) and "name" in b]
        names = [b["name"] for b in returned_bones]
        manifest_set, returned_set = set(bone_names), set(names)
        dups = {n: c for n, c in Counter(names).items() if c > 1}  # a name listed twice is refused by name and never compared
        by = {b["name"]: b for b in returned_bones if b["name"] not in dups}
        missing = [n for n in bone_names if n not in returned_set]  # rig order
        added = list(dict.fromkeys(n for n in names if n not in manifest_set))
        added_names.update(added)
        # A rename keeps the bone's body (pivot, bind rotation, cubes), so a missing bone is matched by that fingerprint against
        # the bones the shipped rig has not: the added ones and every copy of a duplicated name (a rename onto an existing
        # name). One match each way identifies the rename; none is a deletion; several leave "renamed or deleted".
        candidates = [(i, b) for i, b in enumerate(returned_bones) if b["name"] not in manifest_set or b["name"] in dups]
        matches = {n: [i for i, b in candidates if _bone_fingerprint_matches(by_manifest[n], b)] for n in missing}
        claimed = Counter(i for hits in matches.values() for i in hits)
        renamed_bone = {n: returned_bones[hits[0]] for n, hits in matches.items() if len(hits) == 1 and claimed[hits[0]] == 1}
        renames = {n: b["name"] for n, b in renamed_bone.items()}  # shipped name -> the returned name that carries its body
        renamed_names.update({new: old for old, new in renames.items()})

        def follows_rename(mb: dict[str, Any], b: dict[str, Any]) -> bool:
            """The parent field changed from a renamed bone to that bone's new name: the child followed the rename, it was not re-parented."""
            old = mb.get("parent")
            return old is not None and old in renames and renames[old] == (b.get("parent") or None)

        followers: dict[str, list[str]] = defaultdict(list)  # renamed bone -> the children whose parent field followed it
        for mb in m["bones"]:
            b = by.get(mb["name"]) or renamed_bone.get(mb["name"])
            if b is not None and follows_rename(mb, b):
                followers[mb["parent"]].append(mb["name"])
        if names != bone_names:
            common_returned = list(dict.fromkeys(n for n in names if n in manifest_set))
            common_manifest = [n for n in bone_names if n in returned_set]
            order = "kept" if common_returned == common_manifest else "changed"  # the ORDER of the names both rigs share
            findings.append(("REJECT", f"{gp.name}: bone set/order changed (missing {sorted(missing)[:5]}, added {sorted(added)[:5]}, "
                             + (f"duplicated {sorted(dups)[:5]}, " if dups else "") + f"order {order})"))
        for n, c in sorted(dups.items()):
            findings.append(("REJECT", f"{gp.name}: duplicate bone name {n} ({c} times) — a name listed twice is refused by name; neither copy is compared"))
        refused = " — it carries or parents a hitbox part; renaming, re-parenting or deleting a locked bone is refused"
        fixed = " — every bone name is fixed (README rule 1)"
        for n in missing:
            mb = by_manifest[n]
            what, tail = ("locked bone", refused) if n in locked else ("bone", fixed)  # the second half of LOCK_POLICY, by name
            if n in renamed_bone:
                nb, new = renamed_bone[n], renames[n]
                where = (f"a name the rig already has: the returned rig has {dups[new]} bones named {new}" if new in dups
                         else f"{new} is not this rig's name")
                kids = followers.get(n, [])
                if kids:  # one rename is one finding: the children whose parent field followed it are listed here, not as re-parents
                    where += (f"; its {len(kids)} child bone(s) follow it ({', '.join(kids[:6])}"
                              + (f", +{len(kids) - 6} more" if len(kids) > 6 else "") + ") — one rename, reported once")
                verb = f"renamed to {new}"
                if (nb.get("parent") or None) != mb.get("parent") and not follows_rename(mb, nb):
                    verb += f" and re-parented ({mb.get('parent')} -> {nb.get('parent') or None})"
                findings.append(("REJECT", f"{gp.name}: {what} {n} {verb} ({where}){tail}"))
            elif not matches[n]:
                verb = "deleted"
                findings.append(("REJECT", f"{gp.name}: {what} {n} deleted (missing from the returned rig; no other bone carries its pivot and cubes){tail}"))
            else:  # several bones carry its body: which one is the rename cannot be told
                verb = "renamed or deleted"
                twins = sorted({returned_bones[i]["name"] for i in matches[n]})
                findings.append(("REJECT", f"{gp.name}: {what} {n} renamed or deleted (missing from the returned rig; the bones {twins[:5]} all carry its pivot and cubes){tail}"))
            if n in locked:
                locked_structural.append(f"{n} {verb}")
        # every bone present in both is compared whatever the set/order verdict (a re-parent beside a rename is still named);
        # a renamed bone's body already matched, a duplicated name is not compared (which copy would be the bone?)
        for mb in m["bones"]:
            b = by.get(mb["name"])
            if b is None:
                continue
            new_parent = b.get("parent") or None
            if new_parent != mb.get("parent") and not follows_rename(mb, b):
                if mb["name"] in locked:
                    locked_structural.append(f"{mb['name']} re-parented")
                    findings.append(("REJECT", f"{gp.name}: locked bone {mb['name']} re-parented ({mb.get('parent')} -> {new_parent}){refused}"))
                else:
                    findings.append(("REJECT", f"{gp.name}: bone {mb['name']} re-parented ({mb.get('parent')} -> {new_parent})"))
            if not _close(vec(b.get("pivot")), vec(mb.get("pivot"))):
                findings.append(("REJECT", f"{gp.name}: bone {mb['name']} pivot moved {_shown(mb.get('pivot'))} -> {_shown(b.get('pivot'))}"))
            if not _close(vec(b.get("rotation")), vec(mb.get("rotation"))):
                findings.append(("REJECT", f"{gp.name}: bone {mb['name']} bind rotation changed {_shown(mb.get('rotation'))} -> {_shown(b.get('rotation'))}"))
            if "cubes" in mb:
                mirror = bool(b.get("mirror", False))
                cubes = b.get("cubes", [])
                if len(cubes) != len(mb["cubes"]):
                    findings.append(("REJECT", f"{gp.name}: bone {mb['name']} cube count {len(mb['cubes'])} -> {len(cubes)}"))
                else:
                    for i, (c, sig) in enumerate(zip(cubes, mb["cubes"])):
                        for d in signature_differences(sig, cube_signature(c, mirror)):
                            findings.append(("REJECT", f"{gp.name}: bone {mb['name']} cube[{i}] {d} (cubes, sizes and UVs must stay as shipped)"))
        desc = g.get("description", {})
        ts = m.get("texture_size")
        if ts and "texture_width" in desc and [desc.get("texture_width"), desc.get("texture_height")] != list(ts):
            findings.append(("REJECT", f"{gp.name}: texture canvas {desc.get('texture_width')}x{desc.get('texture_height')} must stay {ts[0]}x{ts[1]}"))
        findings.append(("WARN", f"{gp.name}: a geo was returned; the shipped rig is used regardless (do not re-export the geo)"))
    # --- the animation file: exactly one, under the sheet's name ---
    # a `_preview` file is warned above and a `*_reference.animation.json` rejected above; neither counts as the delivery
    anim_files = [p for p in sorted(folder.glob("*.animation.json")) if "_preview" not in p.name and not p.name.endswith(REFERENCE_CLIP_SUFFIX)]
    if not anim_files and clips_by_name:
        findings.append(("REJECT", f"no `{expected_anim}` returned (the sheet names the file)"))
    if len(anim_files) > 1:
        findings.append(("REJECT", f"{len(anim_files)} animation files returned ({', '.join(p.name for p in anim_files)}): exactly one, `{expected_anim}`"))
    clips_checked = 0
    values_checked = 0
    locked_hits: dict[str, list[str]] = OrderedDict()
    for ap_ in anim_files:
        if ap_.name != expected_anim:
            findings.append(("REJECT", f"{ap_.name}: wrong file name — the sheet names it `{expected_anim}`"))
        try:
            a = load_json(ap_)
        except Exception as exc:  # noqa: BLE001
            findings.append(("REJECT", f"{ap_.name}: unreadable JSON ({exc})"))
            continue
        if not isinstance(a, dict):
            findings.append(("REJECT", f"{ap_.name}: not an animation file (a JSON object with `animations` is expected)"))
            continue
        if str(a.get("format_version", "")) != "1.8.0":
            findings.append(("WARN", f"{ap_.name}: format_version {a.get('format_version')} (expected 1.8.0)"))
        clips = a.get("animations", {})
        if not isinstance(clips, dict):
            findings.append(("REJECT", f"{ap_.name}: `animations` must be an object of clips"))
            continue
        if clips and not clips_by_name:
            findings.append(("REJECT", f"{ap_.name}: this entity takes no artist clips (Tier 3) but {len(clips)} clip(s) were delivered"))
        for name, clip in clips.items():
            spec = clips_by_name.get(name)
            if spec is None:
                if m.get("allow_idle_alt") and re.fullmatch(r"idle_alt_\d+", name):
                    spec = {"name": name, "loop": "false", "length_rule": "free", "code_triggered": False}
                else:
                    findings.append(("REJECT", f"{ap_.name}: clip '{name}' is not in the SPEC (renamed or added)"
                                     + ("; this creature's native clip set accepts no idle_alt_N" if m.get("native") and name.startswith("idle_alt_") else "")))
                    continue
            clips_checked += 1
            if not isinstance(clip, dict):
                findings.append(("REJECT", f"{ap_.name}: clip '{name}' is not an object"))
                continue
            loop = normalize_loop(clip.get("loop", False))
            if loop != spec["loop"]:
                findings.append(("REJECT", f"{ap_.name}: clip '{name}' loop is {loop}, the SPEC says {spec['loop']}"))
            if "timeline" in clip:
                findings.append(("REJECT", f"{ap_.name}: clip '{name}' has a `timeline` (custom-instruction keys are not handled; rule 7)"))
            if spec["loop"] == "true":  # owner 2026-09-12, item 11: no event keyframes on loops
                for event_key in ("sound_effects", "particle_effects"):
                    if clip.get(event_key):
                        if native_controllers:  # owner 2026-09-13, addendum item 26 (6): a native-controller species is exempt
                            findings.append(("NOTE", f"{ap_.name}: clip '{name}' carries `{event_key}` on a looping clip — allowed for this creature: "
                                                     "native controllers are not phase-locked; event keys fire per loop (controller kind native; rule 7's exception)"))
                        else:
                            findings.append(("REJECT", f"{ap_.name}: clip '{name}' carries `{event_key}` on a looping clip — no event keyframes on loops; "
                                                       "code-fired events come from the trigger inventory (rule 7)"))
            rule = str(spec.get("length_rule", "free"))
            declared = clip.get("animation_length")
            length = float(declared) if _finite_number(declared) else 0.0
            if declared is not None and not _finite_number(declared):
                findings.append(("REJECT", f"{ap_.name}: clip '{name}' animation_length {declared!r} is not a finite number"))
            if rule.startswith("near:") and declared is not None:
                target = float(rule.split(":", 1)[1])
                if target > 0 and abs(length - target) > 0.5 * target:
                    # README rule 5 by controller kind (owner 2026-09-13, third set, item 28 (3)): a phase-locked creature's loops
                    # against 1.0 s (ruled 2026-09-06, Q14 (a)); a native creature's clips against their shipped length
                    if native_controllers:
                        why = "rule 5, a native creature: keep each clip's shipped length — its controllers play the clip as authored"
                    elif str(spec.get("loop", "")).lower() == "true":
                        why = "rule 5, a phase-locked creature: author every loop at 1.0 s (ruled 2026-09-06, Q14 (a)); in-game the length is free and the tempo table gives the rate"
                    else:
                        why = "the length the sheet's row states for this one-shot"
                    findings.append(("WARN", f"{ap_.name}: clip '{name}' length {fmt(length, 3)} s is far from the stated {fmt(target, 3)} s ({why})"))
            last_key = 0.0
            bones = clip.get("bones", {})
            if not isinstance(bones, dict):
                findings.append(("REJECT", f"{ap_.name}: clip '{name}' `bones` must be an object"))
                bones = {}
            for bone, chans in bones.items():
                if bone not in bone_names:
                    if bone in renamed_names:  # a returned geo renamed a shipped bone to this name (refused above)
                        findings.append(("REJECT", f"{ap_.name}: clip '{name}' keys renamed bone '{bone}' (the shipped rig's '{renamed_names[bone]}', "
                                                   f"renamed in the returned geo — the rename is refused; key '{renamed_names[bone]}')"))
                    elif bone in added_names:  # a returned geo lists it as an added bone
                        findings.append(("REJECT", f"{ap_.name}: clip '{name}' keys added bone '{bone}' (an artist-added bone; not in the shipped rig)"))
                    else:
                        findings.append(("REJECT", f"{ap_.name}: clip '{name}' keys unknown bone '{bone}' (renamed?)"))
                elif bone in locked:
                    locked_hits.setdefault(name, []).append(bone)
                if not isinstance(chans, dict):
                    findings.append(("REJECT", f"{ap_.name}: clip '{name}' bone '{bone}': channels must be an object"))
                    continue
                for ch, keys in chans.items():
                    if ch not in CHANNELS:
                        findings.append(("REJECT", f"{ap_.name}: clip '{name}' bone '{bone}': unsupported channel '{ch}'"))
                        continue
                    t_last, n = validate_channel(f"{ap_.name}: clip '{name}' {bone}.{ch}", ch, keys, findings)
                    last_key = max(last_key, t_last)
                    values_checked += n
            if declared is None:
                if last_key > 0:
                    findings.append(("WARN", f"{ap_.name}: clip '{name}' has no animation_length; the game takes the last key ({fmt(last_key, 3)} s) — declare it"))
            elif _finite_number(declared) and last_key > length + 1e-6:
                findings.append(("REJECT", f"{ap_.name}: clip '{name}' has a key at {fmt(last_key, 3)} s beyond its animation_length {fmt(length, 3)} s (the declared length is shorter than the last key)"))
        # T2c (2026-09-13, decided under the sampler step's doctrine): a phase-locked creature on its classic code ships a file with
        # NO clip; its generated folder is not a delivery - every required row is "not delivered yet", a WARN, so the package's own
        # untouched folder checks PASS. A file with some clips but not the pair is a partial delivery and REJECTs as before; a native
        # creature's shipped file carries its clips, so an empty return from it is a deletion and REJECTs as before.
        nothing_delivered = (not clips) and m.get("exact_transcription") is False and not native_controllers
        for name, spec in clips_by_name.items():
            if name not in clips:
                if spec.get("required") and nothing_delivered:
                    findings.append(("WARN", f"{ap_.name}: required clip '{name}' not delivered yet - the file carries no clip at all "
                                             "(the package's own copy of a creature on its classic code, not a delivery; idle and walk "
                                             "open the game's switch only together - deliver both)"))
                elif spec.get("code_triggered") and spec.get("required"):
                    findings.append(("REJECT", f"{ap_.name}: code-triggered clip '{name}' is missing (renamed or removed)"))
                elif spec.get("required"):
                    findings.append(("REJECT", f"{ap_.name}: required clip '{name}' is missing (idle and walk open the game's switch only "
                                               "together — without it the creature stays on its code-driven motion)"))
                else:
                    findings.append(("WARN", f"{ap_.name}: optional clip '{name}' not delivered (falls back per the contract)"))
    lock_sev = "REJECT" if lock_mode == "reject" else "WARN"
    for name, bones_hit in locked_hits.items():
        shown = ", ".join(bones_hit[:6]) + (f", +{len(bones_hit) - 6} more" if len(bones_hit) > 6 else "")
        findings.append((lock_sev, f"clip '{name}' keys {len(bones_hit)} locked bone(s): {shown} (they carry or parent a hitbox part; "
                         + ("the part follows the bone in-game — allowed, keep it deliberate)" if lock_sev == "WARN" else "REJECTED under lock_mode reject)")))
    # --- textures ---
    tex_dir = folder / "textures"
    known = {t["canonical"]: t for t in m.get("textures", [])}
    aliases = {a: t for t in m.get("textures", []) for a in t.get("aliases", [])}
    textures_checked = 0
    if tex_dir.exists():
        for tp in sorted(tex_dir.glob("*.png")):
            t = known.get(tp.name) or aliases.get(tp.name)
            if t is None:
                findings.append(("REJECT", f"textures/{tp.name}: not one of this entity's textures"))
                continue
            try:
                w, h = png_size(tp)
            except ValueError as exc:
                findings.append(("REJECT", f"textures/{tp.name}: {exc}"))
                continue
            textures_checked += 1
            if (w, h) != (t["width"], t["height"]):
                findings.append(("REJECT", f"textures/{tp.name}: canvas {w}x{h}, must stay {t['width']}x{t['height']}"))
            if tp.name in aliases and tp.name not in known:
                findings.append(("WARN", f"textures/{tp.name}: an alias name; the canonical copy is {aliases[tp.name]['canonical']}"))
    for bp in folder.glob("*.bbmodel"):
        try:
            load_json(bp)
            findings.append(("OK", f"{bp.name}: parses"))
        except Exception as exc:  # noqa: BLE001
            findings.append(("WARN", f"{bp.name}: unreadable ({exc})"))
    # --- the summary lines (always present) ---
    keyed_total = len({b for bones_hit in locked_hits.values() for b in bones_hit})
    if locked:
        sev = "REJECT" if locked_structural else (lock_sev if locked_hits else "OK")
        structural = ((f"; {len(locked_structural)} renamed, re-parented or deleted ({', '.join(locked_structural[:6])}"
                       + (f", +{len(locked_structural) - 6} more" if len(locked_structural) > 6 else "") + ") — REJECTED")
                      if locked_structural else "")
        policy = (LOCK_POLICY if lock_mode != "reject"
                  else "keys on locked bones are REJECTED in this run (the reject mode, kept for the day the server-side hitbox evaluator lands); "
                       f"the ruled policy: {LOCK_POLICY}")
        findings.append((sev, f"locked bones: {keyed_total} of {len(locked)} keyed across {len(locked_hits)} clip(s){structural} "
                              f"[lock_mode {lock_mode}; {LOCK_POLICY_ID}] — {policy}"))
    else:
        findings.append(("OK", "locked bones: none on this rig"))
    passed = not any(sev == "REJECT" for sev, _ in findings)
    findings.append(("OK", f"checked {clips_checked} clip(s), {values_checked} keyframe value(s), {textures_checked} texture(s) against {manifest_path.name} ({m.get('registry', '?')})"))
    return findings, passed


# --- CLI ---------------------------------------------------------------------------------------------

def main(argv: list[str] | None = None) -> int:
    # The findings and warnings carry §, — and similar; a redirected stream on Windows defaults to the console code page.
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            try:
                stream.reconfigure(encoding="utf-8", errors="replace")
            except (ValueError, OSError):
                pass
    parser = argparse.ArgumentParser(description="OreSpawn artist handoff package generator (Phase G slice (f))")
    parser.add_argument("--root", type=Path, default=ROOT, help="repository root (default: the tool's parent directory)")
    sub = parser.add_subparsers(dest="command", required=True)
    p = sub.add_parser("inventory"); p.add_argument("--out", type=Path, required=True)
    p = sub.add_parser("texture-map"); p.add_argument("--out", type=Path, required=True)
    p = sub.add_parser("spec"); p.add_argument("entity"); p.add_argument("--out", type=Path, required=True)
    p = sub.add_parser("readme"); p.add_argument("--out", type=Path, required=True)
    p = sub.add_parser("bbmodel"); p.add_argument("entity"); p.add_argument("--out", type=Path, required=True)
    p = sub.add_parser("roundtrip"); p.add_argument("entity"); p.add_argument("--out", type=Path)
    p = sub.add_parser("package"); p.add_argument("--out", type=Path, required=True); p.add_argument("--entities", nargs="*"); p.add_argument("--no-roundtrip", action="store_true")
    p = sub.add_parser("check"); p.add_argument("folder", type=Path); p.add_argument("--manifest", type=Path)
    p.add_argument("--lock-mode", choices=("manifest", "warn", "reject"), default="manifest",
                   help="override the manifest's lock_mode: `warn` is the ruled policy (2026-09-06: a key on a locked bone warns; a locked bone "
                        "renamed, re-parented or deleted is refused); `reject` is the mode kept for the day the server-side hitbox evaluator lands")
    args = parser.parse_args(argv)

    if args.command == "check":
        findings, passed = check_folder(args.folder, args.manifest, None if args.lock_mode == "manifest" else args.lock_mode)
        for sev, msg in findings:
            print(f"{sev:6} {msg}")
        print("PASS" if passed else "FAIL")
        return 0 if passed else 1

    repo = Repo(args.root)
    if args.command == "inventory":
        path = write_inventory(repo, args.out, TextureCatalog(repo))
        print(f"wrote {path} ({len(repo.species)} rows)")
    elif args.command == "texture-map":
        path, summary = write_texture_map(repo, args.out)
        print(f"wrote {path}: {json.dumps(summary)}")
    elif args.command == "readme":
        catalog = TextureCatalog(repo)
        manifests = {}
        for s in repo.landed_species():
            inv = build_trigger_inventory(s, repo)
            _, manifests[s.registry] = spec_document(s, repo, catalog, inv)
        write_text(args.out / "README_FIRST.md", readme_document(repo, manifests))
        print(f"wrote {args.out / 'README_FIRST.md'}")
    elif args.command == "spec":
        s = repo.get(args.entity)
        inv = build_trigger_inventory(s, repo)
        md, manifest = spec_document(s, repo, TextureCatalog(repo), inv)
        folder = args.out / "entities" / s.registry
        write_text(folder / "SPEC.md", md)
        write_json(folder / "spec.manifest.json", manifest)
        write_text(folder / "reference" / "SLOTS.md", slots_document(s))
        print(f"wrote {folder / 'SPEC.md'} ({len(manifest['bones'])} bones, {len(manifest['clips'])} clips, {len(manifest['locked_bones'])} locked)")
    elif args.command in ("bbmodel", "roundtrip"):
        s = repo.get(args.entity)
        catalog = TextureCatalog(repo)
        textures = []
        for t in catalog.canonical_for_species(s):
            if not t["stray"]:
                textures.append((t["canonical"], (repo.paths.tex_dir / t["canonical"]).read_bytes(), (t["width"], t["height"])))
        geo, anim = s.geo, s.animation
        bb = build_bbmodel(s, geo, anim, textures, repo.warnings)
        if args.command == "bbmodel":
            path = args.out / "entities" / s.registry / f"{s.registry}.bbmodel"
            write_bbmodel(path, bb)
            print(f"wrote {path} ({len(bb['elements'])} cubes, {len(bb['animations'])} clips, {len(bb['textures'])} textures)")
        else:
            back_geo, reattached = bbmodel_to_geo(bb)
            back_anim = bbmodel_to_animation(bb)
            report = roundtrip_diff(geo, back_geo, anim, back_anim, reattached)
            if args.out:
                write_json(args.out / "entities" / s.registry / "roundtrip.report.json", report)
                write_json(args.out / "entities" / s.registry / f"{s.geo_stem}.roundtrip.geo.json", back_geo)
                write_json(args.out / "entities" / s.registry / f"{s.geo_stem}.roundtrip.animation.json", back_anim)
            print(json.dumps({k: v for k, v in report.items() if k != "differences"}, indent=2))
            for d in report["differences"][:40]:
                print("  DIFF", d)
            return 0 if report["equal"] else 1
    elif args.command == "package":
        summary = build_package(repo, args.out, args.entities, with_roundtrip=not args.no_roundtrip)
        print(summary_markdown(summary))
    for scope, code, msg in repo.warnings.items:
        print(f"WARNING [{scope}] {code}: {msg}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
