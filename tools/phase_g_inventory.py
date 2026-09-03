#!/usr/bin/env python3
"""Generate the Phase G0 entity-model inventory from repository sources.

The factual inventory is deliberately derived from the Java registrations,
renderer/model sources, entity textures, and provenance ledger.  The proposed
tier sets are policy inputs kept near the top of this file so an owner ruling
can be applied without changing the extraction machinery.

Examples:
    python tools/phase_g_inventory.py
    python tools/phase_g_inventory.py --format json
    python tools/phase_g_inventory.py --update-design phase_g_reports/geckolib_migration_design.md
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import struct
import sys
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any, Iterable


EXPECTED_MODEL_COUNT = 109
EXPECTED_HAND_CODED_COUNT = 108
EXPECTED_MODEL_LOC = 36_403
EXPECTED_TEXTURE_COUNT = 428
EXPECTED_PROVENANCE_TEXTURE_COUNT = 426
EXPECTED_VANILLA_REUSE_COUNT = 7

# G0 proposal only.  These are not conversion instructions.
TIER_0_MODELS = {
    "ModelAntRobot",
    "ModelSpiderRobot",
    "QueenModel",
}
TIER_1_MODELS = {
    "ButterflyModel",
    "EmperorScorpionModel",
    "LeonModel",
    "ModelAlien",
    "ModelBasilisk",
    "ModelCephadrome",
    "ModelDragon",
    "ModelGiantRobot",
    "ModelGodzilla",
    "ModelHammerhead",
    "ModelKraken",
    "ModelPitchBlack",
    "ModelSeaMonster",
    "ModelTheKing",
    "ModelThePrince",
    "ModelThePrinceAdult",
    "ModelThePrinceTeen",
    "ModelThePrincess",
    "ModelTRex",
    "ModelWaterDragon",
}
TIER_3_MODELS = {
    "ModelCoin",
    "ModelElevator",
    "ModelGodzillaHead",
    "ModelIsland",
    "ModelIslandToo",
    "ModelKingHead",
    "ModelPurplePower",
    "ModelQueenHead",
    "ModelRobot1",
    "ModelRobot2",
    "ModelRobot3",
    "ModelRobot4",
    "ModelRobot5",
    "ModelRockBase",
}

# Source-sensitive cases whose intent cannot be represented by a lone number.
# Entries are checked against their renderer sources before use.
SPECIAL_SCALE_NOTES = {
    "CrabRenderer": ("dynamic: x entity.getCrabScale()", "getCrabScale"),
    "EasterBunnyRenderer": ("x1 adult / x0.5 baby", "entity.isBaby"),
    "GirlfriendRenderer": ("x5 valentine variant / x1 otherwise", "5.0f"),
    "GodzillaRenderer": ("x2 hostile / x0.5 PlayNicely (divide by 4)", "SCALE / 4.0F"),
    "PeacockRenderer": ("x1 adult / x0.5 baby", "entity.isBaby"),
    "PitchBlackRenderer": ("dynamic: x entity.getPitchBlackScale()", "getPitchBlackScale"),
    "PurplePowerRenderer": ("x1 all types (ENT-S-092: the port's type-based 0.55 was an invention; texture still keyed on the type)", "getPurpleType"),
    "QueenRenderer": ("x2 hostile / x0.5 PlayNicely (divide by 4), applied in scaleModelForRender", "SCALE / 4.0F"),
    "TheKingRenderer": ("x2.1 hostile / x0.525 PlayNicely (divide by 4)", "SCALE / 4.0F"),
}

FK_MODELS = {"EmperorScorpionModel", "ModelAlien"}
SOLVER_MODELS = {"ModelAntRobot", "ModelSpiderRobot"}
DESIGN_BEGIN = "<!-- BEGIN GENERATED G0 INVENTORY -->"
DESIGN_END = "<!-- END GENERATED G0 INVENTORY -->"


class InventoryError(RuntimeError):
    """Raised when the source snapshot cannot be inventoried losslessly."""


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def one_line(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def markdown_cell(value: object) -> str:
    text = str(value) if value not in (None, "") else "—"
    return text.replace("|", "\\|").replace("\n", " ")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def png_dimensions(path: Path) -> tuple[int, int]:
    with path.open("rb") as handle:
        header = handle.read(24)
    if len(header) != 24 or header[:8] != b"\x89PNG\r\n\x1a\n" or header[12:16] != b"IHDR":
        raise InventoryError(f"not a readable PNG: {path}")
    return struct.unpack(">II", header[16:24])


def extract_braced_method(source: str, name: str) -> str:
    match = re.search(rf"\b{name}\s*\([^)]*\)\s*\{{", source, flags=re.DOTALL)
    if not match:
        return ""
    start = source.find("{", match.start())
    depth = 0
    for index in range(start, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start + 1 : index]
    raise InventoryError(f"unterminated {name} method")


def classify_animation(model_name: str, base: str, source: str) -> tuple[str, str]:
    if model_name == "QueenModel":
        return "state-branching", "existing GeckoLib controller state machine"
    if model_name in SOLVER_MODELS:
        return "FK-chained", "external server-authoritative gait/IK solver"
    if model_name in FK_MODELS:
        return "FK-chained", "procedural child transforms form an FK chain"

    body = extract_braced_method(source, "setupAnim")
    normalized = one_line(body)
    if not body:
        if base == "HumanoidModel":
            return "gait-scaled", "inherits HumanoidModel movement-scaled gait"
        return "static", "no setupAnim override"

    has_trig = bool(re.search(r"\b(?:Mth|Math)\.(?:cos|sin)\s*\(", body))
    branch_conditions: list[str] = []
    for branch in re.finditer(r"\b(?:if|switch)\s*\(", body):
        opening = body.find("(", branch.start())
        depth = 0
        for index in range(opening, len(body)):
            if body[index] == "(":
                depth += 1
            elif body[index] == ")":
                depth -= 1
                if depth == 0:
                    branch_conditions.append(body[opening + 1 : index])
                    break
    has_state_branch = any(
        re.search(r"\b(?:entity|[A-Za-z]+Entity)\b", condition)
        or re.search(r"\.\s*(?:get|is|has)[A-Z]", condition)
        for condition in branch_conditions
    )
    if has_state_branch:
        return "state-branching", "setupAnim branches on entity state"
    if has_trig and re.search(r"\blimbSwingAmount\b", body):
        return "gait-scaled", "trigonometric pose depends on limbSwingAmount"
    if has_trig:
        return "simple cyclic", "time/phase-driven trigonometric pose"

    assignments = re.findall(r"\.(?:xRot|yRot|zRot|x|y|z)\s*=", body)
    if assignments or "super.setupAnim" in body:
        return "gait-scaled", "non-cyclic pose/head or inherited gait inputs"
    if normalized:
        return "static", "setupAnim contains no changing part transform"
    return "static", "empty setupAnim"


def parse_models(client_dir: Path) -> dict[str, dict[str, Any]]:
    models: dict[str, dict[str, Any]] = {}
    class_pattern = re.compile(
        r"public\s+class\s+(\w+)(?:\s*<[^>{}]+>)?\s+extends\s+"
        r"(EntityModel|HumanoidModel|GeoModel)\s*<"
    )
    for path in sorted(client_dir.glob("*.java"), key=lambda item: item.name.lower()):
        source = read_text(path)
        match = class_pattern.search(source)
        if not match:
            continue
        name, base = match.groups()
        complexity, basis = classify_animation(name, base, source)
        models[name] = {
            "model": name,
            "path": path,
            "source": source,
            "base": base,
            "loc": len(source.splitlines()),
            "complexity": complexity,
            "complexity_basis": basis,
        }
    return models


def parse_entity_types(mod_entities_path: Path) -> dict[str, dict[str, str]]:
    source = read_text(mod_entities_path)
    declaration = re.compile(
        r"public\s+static\s+final\s+DeferredHolder<EntityType<\?>,\s*EntityType<(?P<class>\w+)>>\s+"
        r"(?P<const>\w+)\s*=",
        flags=re.MULTILINE,
    )
    matches = list(declaration.finditer(source))
    entities: dict[str, dict[str, str]] = {}
    for index, match in enumerate(matches):
        end = matches[index + 1].start() if index + 1 < len(matches) else len(source)
        block = source[match.start() : end]
        registry_match = re.search(r'ENTITY_TYPES\.register\("([^"]+)"', block)
        size_match = re.search(
            r"\.sized\(\s*([0-9.]+)[fFdD]?\s*,\s*([0-9.]+)[fFdD]?\s*\)", block
        )
        if not registry_match:
            raise InventoryError(f"could not find registry name for {match.group('const')}")
        entities[match.group("const")] = {
            "constant": match.group("const"),
            "class": match.group("class"),
            "registry": registry_match.group(1),
            "width": size_match.group(1) if size_match else "implicit",
            "height": size_match.group(2) if size_match else "implicit",
        }
    return entities


def parse_renderer_registrations(client_path: Path) -> dict[str, list[str]]:
    source = read_text(client_path)
    mapping: dict[str, list[str]] = defaultdict(list)
    pattern = re.compile(
        r"registerEntityRenderer\(\s*ModEntities\.(\w+)\.get\(\)\s*,\s*(\w+)::new\s*\)"
    )
    for entity_constant, renderer in pattern.findall(source):
        mapping[renderer].append(entity_constant)
    return dict(mapping)


def renderer_model_token(source: str) -> str | None:
    match = re.search(
        r"extends\s+MobRenderer\s*<\s*[^,>]+\s*,\s*([A-Za-z0-9_]+)", source
    )
    return match.group(1) if match else None


def map_renderers_to_models(
    client_dir: Path,
    models: dict[str, dict[str, Any]],
) -> tuple[dict[str, dict[str, Any]], dict[str, str]]:
    renderers: dict[str, dict[str, Any]] = {}
    external_models: dict[str, str] = {}
    model_names = set(models)
    for path in sorted(client_dir.glob("*Renderer.java"), key=lambda item: item.name.lower()):
        source = read_text(path)
        name_match = re.search(r"public\s+class\s+(\w+Renderer)\b", source)
        if not name_match:
            continue
        renderer_name = name_match.group(1)
        referenced = sorted(
            name for name in model_names if re.search(rf"\b{re.escape(name)}\b", source)
        )
        generic_model = renderer_model_token(source)
        if generic_model and generic_model not in model_names:
            external_models[renderer_name] = generic_model
        renderers[renderer_name] = {
            "renderer": renderer_name,
            "path": path,
            "source": source,
            "models": referenced,
        }
    return renderers, external_models


def parse_provenance(path: Path) -> dict[str, str]:
    provenance: dict[str, str] = {}
    pattern = re.compile(
        r"^\s*textures[\\/]entity[\\/](.+?)\s+<=\s+(.+?)\s*$", flags=re.IGNORECASE
    )
    for line in read_text(path).splitlines():
        match = pattern.match(line)
        if match:
            provenance[match.group(1).replace("\\", "/")] = match.group(2)
    return provenance


def texture_catalog(texture_dir: Path, provenance: dict[str, str]) -> dict[str, dict[str, Any]]:
    paths = sorted(texture_dir.glob("*.png"), key=lambda item: item.name.lower())
    provenance_by_casefold = {name.casefold(): source for name, source in provenance.items()}
    hashes = {path.name: sha256(path) for path in paths}
    groups: dict[str, list[str]] = defaultdict(list)
    for name, digest in hashes.items():
        groups[digest].append(name)

    catalog: dict[str, dict[str, Any]] = {}
    for path in paths:
        width, height = png_dimensions(path)
        catalog[path.name] = {
            "name": path.name,
            "width": width,
            "height": height,
            "sha256": hashes[path.name],
            "twins": sorted(name for name in groups[hashes[path.name]] if name != path.name),
            "provenance": provenance_by_casefold.get(path.name.casefold()),
        }
    return catalog


def textures_from_source(source: str, catalog: dict[str, dict[str, Any]]) -> list[str]:
    names: set[str] = set()
    actual_name = {name.casefold(): name for name in catalog}
    # Literal paths, including Javadoc paths, are harmlessly de-duplicated.
    fragments = re.findall(r"textures/entity/([A-Za-z0-9_./-]+)", source)
    for fragment in fragments:
        if fragment.endswith(".png"):
            literal = Path(fragment).name
            names.add(actual_name.get(literal.casefold(), literal))
            continue
        prefix = Path(fragment).name
        names.update(
            name
            for name in catalog
            if re.fullmatch(rf"{re.escape(prefix)}\d+\.png", name, flags=re.IGNORECASE)
        )
    return sorted(names, key=str.lower)


def scale_note(renderer_name: str, source: str) -> str:
    special = SPECIAL_SCALE_NOTES.get(renderer_name)
    if special:
        note, evidence = special
        if evidence not in source:
            raise InventoryError(f"scale evidence drifted for {renderer_name}: {evidence!r}")
        return note

    constant = re.search(
        r"private\s+static\s+final\s+(?:float|double)\s+SCALE\s*=\s*([0-9.]+)[fFdD]?", source
    )
    if constant and "poseStack.scale" in source:
        value = float(constant.group(1))
        if "isBaby()" in source and "SCALE / 2" in source:
            return f"x{value:g} adult / x{value / 2:g} baby"
        if value != 1.0:
            return f"x{value:g}"

    numeric = re.search(
        r"poseStack\.scale\(\s*([0-9.]+)[fF]?\s*,\s*\1[fF]?\s*,\s*\1[fF]?\s*\)", source
    )
    if numeric and float(numeric.group(1)) != 1.0:
        return f"x{float(numeric.group(1)):g}"
    return "—"


def parse_findings(path: Path) -> list[dict[str, str]]:
    source = read_text(path)
    heading = re.compile(r"^###\s+([A-Z][A-Z0-9-]*-\d+)\s+[^\n]*$", flags=re.MULTILINE)
    matches = list(heading.finditer(source))
    findings: list[dict[str, str]] = []
    for index, match in enumerate(matches):
        end = matches[index + 1].start() if index + 1 < len(matches) else len(source)
        findings.append({"id": match.group(1), "text": source[match.start() : end]})
    return findings


def finding_ids_for(
    model_name: str,
    renderer_names: Iterable[str],
    entity_records: Iterable[dict[str, str]],
    findings: list[dict[str, str]],
) -> list[str]:
    tokens = {model_name, *renderer_names}
    stopwords = {"entity", "model", "renderer", "the"}
    for entity in entity_records:
        tokens.add(entity["class"])
        tokens.add(entity["registry"])
        tokens.update(piece for piece in entity["registry"].split("_") if piece not in stopwords)
    patterns = [
        re.compile(rf"(?<![A-Za-z0-9_]){re.escape(token)}(?![A-Za-z0-9_])", re.IGNORECASE)
        for token in tokens
        if len(token) >= 3 and token.lower() not in stopwords
    ]
    return [finding["id"] for finding in findings if any(p.search(finding["text"]) for p in patterns)]


def proposed_tier(model_name: str, complexity: str) -> str:
    if model_name in TIER_0_MODELS:
        return "0"
    if model_name in TIER_1_MODELS:
        return "1"
    if model_name in TIER_3_MODELS or complexity == "static":
        return "3"
    return "2"


def describe_texture(texture: dict[str, Any]) -> str:
    details = [f"{texture['name']} {texture['width']}x{texture['height']}"]
    if texture["twins"]:
        details.append("twins: " + ", ".join(texture["twins"]))
    details.append("src: " + (texture["provenance"] or "new/no byte-identical source"))
    return " (".join([details[0], "; ".join(details[1:])]) + ")"


def source_fingerprint(root: Path, paths: Iterable[Path]) -> str:
    digest = hashlib.sha256()
    for path in sorted(paths, key=lambda item: item.as_posix().lower()):
        digest.update(path.resolve().relative_to(root.resolve()).as_posix().encode("utf-8"))
        digest.update(b"\0")
        digest.update(path.read_bytes())
        digest.update(b"\0")
    return digest.hexdigest()


def build_inventory(root: Path) -> dict[str, Any]:
    client_dir = root / "src/main/java/danger/orespawn/entity/client"
    mod_entities_path = root / "src/main/java/danger/orespawn/ModEntities.java"
    client_path = root / "src/main/java/danger/orespawn/OreSpawnClient.java"
    texture_dir = root / "src/main/resources/assets/orespawn/textures/entity"
    provenance_path = root / "provenance_byte_identical_assets.txt"
    findings_path = root / "AUDIT_FINDINGS.md"

    models = parse_models(client_dir)
    entity_types = parse_entity_types(mod_entities_path)
    registrations = parse_renderer_registrations(client_path)
    renderers, external_models = map_renderers_to_models(client_dir, models)
    provenance = parse_provenance(provenance_path)
    textures = texture_catalog(texture_dir, provenance)
    findings = parse_findings(findings_path)

    errors: list[str] = []
    if len(models) != EXPECTED_MODEL_COUNT:
        errors.append(f"models: expected {EXPECTED_MODEL_COUNT}, found {len(models)}")
    hand_coded = sum(model["base"] != "GeoModel" for model in models.values())
    if hand_coded != EXPECTED_HAND_CODED_COUNT:
        errors.append(f"hand-coded models: expected {EXPECTED_HAND_CODED_COUNT}, found {hand_coded}")
    total_loc = sum(model["loc"] for model in models.values())
    if total_loc != EXPECTED_MODEL_LOC:
        errors.append(f"model LOC: expected {EXPECTED_MODEL_LOC}, found {total_loc}")
    if len(textures) != EXPECTED_TEXTURE_COUNT:
        errors.append(f"entity textures: expected {EXPECTED_TEXTURE_COUNT}, found {len(textures)}")
    if len(provenance) != EXPECTED_PROVENANCE_TEXTURE_COUNT:
        errors.append(
            f"entity provenance entries: expected {EXPECTED_PROVENANCE_TEXTURE_COUNT}, found {len(provenance)}"
        )
    unknown_policy = (TIER_0_MODELS | TIER_1_MODELS | TIER_3_MODELS) - set(models)
    if unknown_policy:
        errors.append("unknown models in tier policy: " + ", ".join(sorted(unknown_policy)))

    renderer_entities: dict[str, list[dict[str, str]]] = {}
    for renderer_name, constants in registrations.items():
        missing_constants = [constant for constant in constants if constant not in entity_types]
        if missing_constants:
            errors.append(f"{renderer_name}: missing entity declarations {missing_constants}")
            continue
        renderer_entities[renderer_name] = [entity_types[constant] for constant in constants]

    rows: list[dict[str, Any]] = []
    all_referenced_textures: set[str] = set()
    for model_name in sorted(models, key=str.lower):
        model = models[model_name]
        serving_renderers = sorted(
            name for name, renderer in renderers.items() if model_name in renderer["models"]
        )
        if not serving_renderers:
            errors.append(f"model has no renderer consumer: {model_name}")
        served_entities: list[dict[str, str]] = []
        texture_names: set[str] = set(textures_from_source(model["source"], textures))
        scales: list[str] = []
        for renderer_name in serving_renderers:
            served_entities.extend(renderer_entities.get(renderer_name, []))
            renderer_source = renderers[renderer_name]["source"]
            texture_names.update(textures_from_source(renderer_source, textures))
            note = scale_note(renderer_name, renderer_source)
            if note != "—":
                scales.append(f"{renderer_name}: {note}")

        unknown_textures = sorted(texture_names - set(textures))
        if unknown_textures:
            errors.append(f"{model_name}: referenced textures not found: {unknown_textures}")
        all_referenced_textures.update(texture_names)
        served_entities = sorted(
            {entity["constant"]: entity for entity in served_entities}.values(),
            key=lambda entity: entity["registry"],
        )
        rows.append(
            {
                "model": model_name,
                "loc": model["loc"],
                "base": model["base"],
                "renderers": serving_renderers,
                "entities": served_entities,
                "textures": [textures[name] for name in sorted(texture_names, key=str.lower)],
                "complexity": model["complexity"],
                "complexity_basis": model["complexity_basis"],
                "scale_overrides": scales,
                "findings": finding_ids_for(
                    model_name, serving_renderers, served_entities, findings
                ),
                "tier": proposed_tier(model_name, model["complexity"]),
            }
        )

    vanilla_rows: list[dict[str, Any]] = []
    for renderer_name, external_model in sorted(external_models.items()):
        entities = renderer_entities.get(renderer_name, [])
        # Projectile/item renderers are not MobRenderer subclasses and never enter
        # external_models, so every row here is a deliberate vanilla-model reuse.
        if not entities:
            continue
        renderer_source = renderers[renderer_name]["source"]
        texture_names = textures_from_source(renderer_source, textures)
        vanilla_rows.append(
            {
                "model": external_model,
                "renderer": renderer_name,
                "entities": entities,
                "textures": [textures[name] for name in texture_names],
                "tier": "0",
            }
        )
        all_referenced_textures.update(texture_names)
    if len(vanilla_rows) != EXPECTED_VANILLA_REUSE_COUNT:
        errors.append(
            f"vanilla model reuse rows: expected {EXPECTED_VANILLA_REUSE_COUNT}, found {len(vanilla_rows)}"
        )

    if errors:
        raise InventoryError("inventory validation failed:\n- " + "\n- ".join(errors))

    digest_groups: dict[str, list[str]] = defaultdict(list)
    for texture in textures.values():
        digest_groups[texture["sha256"]].append(texture["name"])
    unique_payloads = len(digest_groups)
    duplicate_groups = sum(len(group) > 1 for group in digest_groups.values())

    input_paths = [
        *client_dir.glob("*.java"),
        mod_entities_path,
        client_path,
        *texture_dir.glob("*.png"),
        provenance_path,
        findings_path,
    ]
    return {
        "summary": {
            "models": len(models),
            "hand_coded_models": hand_coded,
            "geckolib_models": len(models) - hand_coded,
            "model_loc": total_loc,
            "served_registry_types": len(
                {entity["registry"] for row in rows for entity in row["entities"]}
            ),
            "vanilla_reuse_rows": len(vanilla_rows),
            "entity_textures": len(textures),
            "referenced_entity_textures": len(all_referenced_textures),
            "unique_texture_payloads": unique_payloads,
            "duplicate_texture_groups": duplicate_groups,
            "redundant_texture_names": len(textures) - unique_payloads,
            "provenance_entries": len(provenance),
            "tier_counts": dict(sorted(Counter(row["tier"] for row in rows).items())),
            "complexity_counts": dict(
                sorted(Counter(row["complexity"] for row in rows).items())
            ),
            "source_fingerprint_sha256": source_fingerprint(root, input_paths),
        },
        "models": rows,
        "vanilla_reuse": vanilla_rows,
        "texture_catalog": [textures[name] for name in sorted(textures, key=str.lower)],
        "unreferenced_textures": sorted(set(textures) - all_referenced_textures, key=str.lower),
        "textures_without_provenance": sorted(
            (name for name, texture in textures.items() if not texture["provenance"]),
            key=str.lower,
        ),
    }


def entity_cell(entities: list[dict[str, str]]) -> str:
    if not entities:
        return "—"
    return "; ".join(
        f"{entity['registry']} [{entity['width']}x{entity['height']}]" for entity in entities
    )


def render_markdown(inventory: dict[str, Any]) -> str:
    summary = inventory["summary"]
    lines = [
        "### Mechanical inventory snapshot",
        "",
        "Generated by `python tools/phase_g_inventory.py --update-design "
        "phase_g_reports/geckolib_migration_design.md`. The generator aborts if the "
        "brief's exact 109/108/36,403/428/426/7 source invariants drift.",
        "",
        f"- Models: **{summary['models']}** total = {summary['hand_coded_models']} hand-coded "
        f"`ModelPart`/humanoid models + {summary['geckolib_models']} existing GeckoLib model.",
        f"- Model source: **{summary['model_loc']:,} LOC**; "
        f"{summary['served_registry_types']} custom-model registry consumers plus "
        f"{summary['vanilla_reuse_rows']} vanilla-model reuse consumers.",
        f"- Entity texture directory: **{summary['entity_textures']} PNG names**, "
        f"{summary['unique_texture_payloads']} unique byte payloads, "
        f"{summary['duplicate_texture_groups']} multi-name duplicate groups, and "
        f"{summary['redundant_texture_names']} redundant names.",
        f"- Renderer/model references reach {summary['referenced_entity_textures']} of the "
        f"{summary['entity_textures']} PNGs; the appendix accounts for the remainder.",
        f"- Provenance covers **{summary['provenance_entries']}/{summary['entity_textures']}** "
        f"entity PNGs; no-source entries: "
        f"{', '.join(inventory['textures_without_provenance']) or 'none'}.",
        f"- Proposed custom-model tier counts: "
        + ", ".join(f"Tier {tier}: {count}" for tier, count in summary["tier_counts"].items())
        + ".",
        f"- Animation classes: "
        + ", ".join(
            f"{name}: {count}" for name, count in summary["complexity_counts"].items()
        )
        + ".",
        f"- Input fingerprint (SHA-256): `{summary['source_fingerprint_sha256']}`.",
        "",
        "Dimensions are the registered `EntityType.Builder.sized(width,height)` values, "
        "not visual model extents. `src` is the byte-identical 1.7.10 provenance name; "
        "`new/no byte-identical source` is explicit. Audit IDs are mechanical whole-token "
        "matches over finding blocks, so they intentionally include entity behavior findings "
        "as risk context even when the finding is not a renderer defect.",
        "",
        "| # | Model (LOC) | Served entity type(s) [W x H] | Texture(s): dimensions, twins, provenance | setupAnim class (basis) | Renderer scale override(s) | Audit finding IDs | Proposed tier |",
        "|---:|---|---|---|---|---|---|---:|",
    ]
    for index, row in enumerate(inventory["models"], start=1):
        texture_description = "; ".join(describe_texture(texture) for texture in row["textures"])
        values = [
            index,
            f"`{row['model']}` ({row['loc']})",
            entity_cell(row["entities"]),
            texture_description or "—",
            f"{row['complexity']} — {row['complexity_basis']}",
            "; ".join(row["scale_overrides"]) or "—",
            ", ".join(row["findings"]) or "—",
            row["tier"],
        ]
        lines.append("| " + " | ".join(markdown_cell(value) for value in values) + " |")

    lines.extend(
        [
            "",
            "#### Vanilla-model reuse (proposed Tier 0)",
            "",
            "These seven registered consumers have no OreSpawn model class to convert. "
            "Termite is not in this table: its renderer deliberately reuses the custom "
            "`AntModel`, so it remains part of that model's inventory row.",
            "",
            "| Vanilla model | Renderer | Entity type [W x H] | Texture(s) | Tier |",
            "|---|---|---|---|---:|",
        ]
    )
    for row in inventory["vanilla_reuse"]:
        values = [
            f"`{row['model']}`",
            f"`{row['renderer']}`",
            entity_cell(row["entities"]),
            "; ".join(describe_texture(texture) for texture in row["textures"]),
            row["tier"],
        ]
        lines.append("| " + " | ".join(markdown_cell(value) for value in values) + " |")
    return "\n".join(lines) + "\n"


def update_design(path: Path, generated: str) -> None:
    source = read_text(path)
    if source.count(DESIGN_BEGIN) != 1 or source.count(DESIGN_END) != 1:
        raise InventoryError(f"design must contain exactly one generated marker pair: {path}")
    start = source.index(DESIGN_BEGIN) + len(DESIGN_BEGIN)
    end = source.index(DESIGN_END)
    if start > end:
        raise InventoryError(f"generated markers are reversed: {path}")
    replacement = f"{DESIGN_BEGIN}\n\n{generated.rstrip()}\n\n{DESIGN_END}"
    updated = source[: source.index(DESIGN_BEGIN)] + replacement + source[end + len(DESIGN_END) :]
    path.write_text(updated, encoding="utf-8", newline="\n")


def json_ready(value: Any) -> Any:
    if isinstance(value, Path):
        return value.as_posix()
    if isinstance(value, dict):
        return {key: json_ready(item) for key, item in value.items()}
    if isinstance(value, list):
        return [json_ready(item) for item in value]
    return value


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, help="repository root (defaults to script parent)")
    parser.add_argument("--format", choices=("markdown", "json"), default="markdown")
    parser.add_argument(
        "--update-design",
        type=Path,
        metavar="PATH",
        help="replace the generated inventory block in an existing design document",
    )
    args = parser.parse_args()
    root = (args.root or Path(__file__).resolve().parents[1]).resolve()
    try:
        inventory = build_inventory(root)
        markdown = render_markdown(inventory)
        if args.update_design:
            design_path = args.update_design
            if not design_path.is_absolute():
                design_path = root / design_path
            update_design(design_path.resolve(), markdown)
            print(f"updated {design_path.resolve()}")
        elif args.format == "json":
            print(json.dumps(json_ready(inventory), indent=2, sort_keys=True))
        else:
            print(markdown, end="")
    except (InventoryError, OSError, UnicodeError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
