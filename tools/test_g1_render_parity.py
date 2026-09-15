#!/usr/bin/env python3
"""Pins for the visual leg's pair-contested rule and the undrawn-parts list (owner 2026-09-15, closing set continued,
items 1 and 2; tools/g1_render_parity.py and tools/layer_definition_to_geo.py) - a `unittest` runner, no pytest.

    python tools/test_g1_render_parity.py

The fixtures are synthetic captures through the production code paths (quad_face_ids, render_capture, pixel_diff,
visual_parity, candidate_bone_names, draw_order_parity; undrawn_parts_declared, derive_bone_draw_order): a four-texel
texture, a camera looking down Z, an alpha-0 frame quad that fixes the camera fit, and two coplanar faces of one flat
"wing" cube whose nearer face SWAPS between the two sides by 2e-6 blocks - past the 1e-6 contest window (the TEST-008
mechanism), inside the 1e-5 attribution window.
"""

from __future__ import annotations

import json
import math
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import g1_render_parity as parity  # noqa: E402
import layer_definition_to_geo as converter  # noqa: E402
from PIL import Image  # noqa: E402

THRESHOLDS = {
    "pixel_channel_tolerance": 2,
    "pixel_changed_fraction": 0.001,
    "pixel_mean_absolute_error": 0.25,
    "minimum_foreground_fraction": 0.001,
}
RED, BLUE, CLEAR = (0, 0), (1, 0), (1, 1)
GREEN = (0, 1)


def texel_uv(texel: tuple[int, int]) -> list[float]:
    return [texel[0] / 2.0 + 0.25, texel[1] / 2.0 + 0.25]


def square(z: float, half: float, texel: tuple[int, int], normal_z: float) -> list[dict]:
    corners = [(-half, -half), (half, -half), (half, half), (-half, half)]
    return [{"position": [x, y, z], "uv": texel_uv(texel), "normal": [0.0, 0.0, normal_z]} for x, y in corners]


def capture(front_z: float, back_z: float, half: float, back_bone: str = "wing",
            front_texel: tuple[int, int] = RED) -> dict:
    """One full capture: an invisible frame quad (the camera fit), then a flat cube's two coplanar faces - the +Z face
    red at ``front_z`` (``front_texel``: another texel keeps the face's identity and changes its colour, the tightening's
    case), the -Z face blue at ``back_z`` (a smaller depth is nearer). ``back_bone`` names the blue face's owner: the
    same cube by default, another bone to make the two sides' pairs differ."""
    frame = square(5.0, 10.0, CLEAR, 1.0)
    red = square(front_z, half, front_texel, 1.0)
    blue = square(back_z, half, BLUE, -1.0)
    cubes = [{"bone": "frame", "cube_index": 0, "vertices": frame}]
    if back_bone == "wing":
        cubes.append({"bone": "wing", "cube_index": 0, "vertices": red + blue})
        order = ["frame", "wing"]
    else:
        cubes.append({"bone": "wing", "cube_index": 0, "vertices": red})
        cubes.append({"bone": back_bone, "cube_index": 0, "vertices": blue})
        order = ["frame", "wing", back_bone]
    return {"id": "bind", "capture_kind": "full", "render_vertices": frame + red + blue, "cubes": cubes, "draw_order": order}


class PairContestedRule(unittest.TestCase):
    def setUp(self) -> None:
        self.root = Path(tempfile.mkdtemp(prefix="g1_pair_"))
        texture = Image.new("RGBA", (2, 2))
        texture.putpixel(RED, (255, 0, 0, 255))
        texture.putpixel(BLUE, (0, 0, 255, 255))
        texture.putpixel((0, 1), (0, 255, 0, 255))
        texture.putpixel(CLEAR, (0, 0, 0, 0))
        texture.save(self.root / "texture.png")
        self.spec = {"id": "synthetic", "texture": "texture.png", "visual_sample_ids": ["bind"],
                     "camera": {"yaw_degrees": 0.0, "pitch_degrees": 0.0}}

    def tearDown(self) -> None:
        shutil.rmtree(self.root, ignore_errors=True)

    def visual(self, vanilla: dict, geo: dict) -> dict:
        return parity.visual_parity("synthetic", self.spec, {"samples": [vanilla]}, {"samples": [geo]},
                                    self.root, self.root / "out", THRESHOLDS)

    def test_constants_are_the_rulings(self) -> None:
        self.assertEqual(parity.PAIR_ATTRIBUTION_WINDOW, 1.0e-5)
        self.assertEqual(parity.PAIR_CONTESTED_CAP, 0.01)
        self.assertEqual(parity.CONTEST_DEPTH_EPSILON, 1.0e-6)

    def test_face_identities_agree_across_sides_and_count_every_quad(self) -> None:
        faces: dict = {}
        vanilla = parity.quad_face_ids("synthetic", "bind", capture(0.0, 2.0e-6, 0.6), faces)
        geo = parity.quad_face_ids("synthetic", "bind", capture(2.0e-6, 0.0, 0.6), faces)
        self.assertEqual(vanilla, geo)
        self.assertEqual(len(vanilla), 3)
        self.assertEqual(len(set(vanilla)), 3, "the frame, the +Z face and the -Z face are three identities")
        broken = capture(0.0, 2.0e-6, 0.6)
        broken["draw_order"] = ["frame"]
        with self.assertRaisesRegex(AssertionError, "cannot attribute its faces"):
            parity.quad_face_ids("synthetic", "bind", broken, {})

    def test_swapped_coplanar_pair_is_pair_contested_never_a_mismatch(self) -> None:
        report = self.visual(capture(0.0, 2.0e-6, 0.6), capture(2.0e-6, 0.0, 0.6))
        row = report["samples"][0]
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(row["changed_fraction"], 0.0, "every differing pixel is pair-contested, none is a mismatch")
        self.assertGreater(row["pair_contested_fraction"], 0.001, "the swapped faces are counted")
        self.assertLessEqual(row["pair_contested_fraction"], parity.PAIR_CONTESTED_CAP)
        self.assertEqual(row["contested_fraction"], 0.0, "a 2e-6 gap is outside the contest window: not a z-fight")
        self.assertEqual(report["max_pair_contested_fraction"], row["pair_contested_fraction"])
        self.assertEqual(report["pair_attribution_window_blocks"], parity.PAIR_ATTRIBUTION_WINDOW)
        self.assertEqual(report["pair_contested_cap"], parity.PAIR_CONTESTED_CAP)
        self.assertIn("pair-contested", report["z_fight_policy"])
        diff = Image.open(self.root / "out" / row["diff_capture"]).convert("RGBA")
        self.assertIn(parity.PAIR_CONTESTED_MARKER, set(diff.get_flattened_data()), "the diff paints the pair-contested pixels")

    def test_the_tightening_a_pixel_showing_the_same_face_on_both_sides_stays_a_changed_pixel(self) -> None:
        # refuter A's tightening (owner 2026-09-15, item 35 (5)): both sides show the +Z face (the -Z face 2e-6 behind on
        # both, the same pair) and differ only through that face's own texel - not pair-contested, a changed pixel
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: changed fraction"):
            self.visual(capture(0.0, 2.0e-6, 0.6), capture(0.0, 2.0e-6, 0.6, front_texel=GREEN))
        # the shown faces per pixel come from the rasteriser's owner quad
        faces: dict = {}
        sample = capture(0.0, 2.0e-6, 0.6)
        ids = parity.quad_face_ids("synthetic", "bind", sample, faces)
        texture = Image.open(self.root / "texture.png")
        camera = parity.Camera(parity.all_vertices({"bind": sample}, ["bind"]), 0.0, 0.0)
        _image, _contested, pairs, shown = parity.render_capture(sample, texture, camera, ids)
        paired = [index for index, pair in enumerate(pairs) if pair is not None]
        self.assertTrue(paired, "the flat cube's two faces form a front pair")
        self.assertTrue(all(shown[index] in pairs[index] for index in paired), "the shown face is one of the pair")
        self.assertEqual({shown[index] for index in paired}, {ids[1]}, "the red +Z face is the one shown (first-wins)")

    def test_a_different_pair_stays_a_changed_pixel(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: changed fraction"):
            self.visual(capture(0.0, 2.0e-6, 0.6), capture(2.0e-6, 0.0, 0.6, back_bone="other"))

    def test_a_pair_beyond_the_attribution_window_stays_a_changed_pixel(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: changed fraction"):
            self.visual(capture(0.0, 2.0e-5 + 1.0e-6, 0.6), capture(2.0e-5 + 1.0e-6, 0.0, 0.6))

    def test_the_cap_fails_a_rig_that_ties_everywhere(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: pair-contested fraction .* > 0.01"):
            self.visual(capture(0.0, 2.0e-6, 6.0), capture(2.0e-6, 0.0, 6.0))

    def test_identical_sides_report_nothing(self) -> None:
        report = self.visual(capture(0.0, 2.0e-6, 0.6), capture(0.0, 2.0e-6, 0.6))
        row = report["samples"][0]
        self.assertEqual((row["changed_fraction"], row["pair_contested_fraction"], row["mean_absolute_error"]), (0.0, 0.0, 0.0))


class EntityFrame(unittest.TestCase):
    """TEST-015 (owner 2026-09-15, closing set continued, second, item 35): the converter's Bedrock convention, the sign
    rule, the face labels and islands re-derived for the entity frame, the parity tool's frame helpers, the camera's view
    through the classic flip, and the one thing the old frame could never show - a seam rig drawn as the classic's mirror
    failing the visual leg."""

    def test_converter_writes_the_bedrock_convention(self) -> None:
        cube = {"origin": [1.0, -3.0, -2.0], "size": [2.0, 4.0, 6.0], "deformation": [0.0, 0.0, 0.0], "uv": [0.0, 0.0],
                "texture_scale": [1.0, 1.0], "mirror": False,
                "visible_faces": ["down", "east", "north", "south", "up", "west"]}
        converted = converter.convert_cube(cube, [2.0, 8.0, 0.0])
        # the ModelPart corner (3, 5, -2) .. (5, 9, 4): x kept, y up from the 24 datum (24 - 9 = 15), z kept
        self.assertEqual(converted["origin"], [3, 15, -2])
        self.assertEqual(converted["size"], [2, 4, 6])
        self.assertEqual(converter.json_rotation([0.1, 0.2, -0.3]),
                         converter.clean_vector([math.degrees(0.1), math.degrees(0.2), math.degrees(-0.3)]))
        self.assertEqual(converter.json_rotation_delta([0.5, 0.25, -0.75], [0.1, 0.05, 0.05]),
                         [math.degrees(0.4), math.degrees(0.2), math.degrees(-0.8)])
        geometry, _summary = converter.convert_geometry(HierarchyForm.compiled(["arm", "claw"]))
        bones = {bone["name"]: bone for bone in geometry["minecraft:geometry"][0]["bones"]}
        self.assertEqual(bones["claw"]["pivot"], [11, 22, -1])  # (x, 24 - y, z) of the classic (11, 2, -1)
        self.assertEqual(bones["claw"]["rotation"], converter.clean_vector([0.0, math.degrees(0.8552113), 0.0]))

    def test_face_labels_and_islands_follow_the_reflection(self) -> None:
        # the classic WEST slot (normal -x) lands on GeckoLib's east quad, DOWN (the y-down top) on its up quad
        self.assertEqual(converter.classic_face_labels(False), ["up", "down", "east", "north", "west", "south"])
        self.assertEqual(converter.classic_face_labels(True), ["up", "down", "west", "north", "east", "south"])
        self.assertEqual(converter.classic_face_normals(False),
                         [(0.0, 1.0, 0.0), (0.0, -1.0, 0.0), (1.0, 0.0, 0.0), (0.0, 0.0, -1.0), (-1.0, 0.0, 0.0), (0.0, 0.0, 1.0)])
        self.assertEqual(converter.entity_frame_normal((-1.0, 0.0, 0.0)), (1.0, 0.0, 0.0))
        cube = {"uv": [0.0, 0.0], "size": [2.0, 4.0, 6.0], "mirror": False}
        faces = converter.modelpart_face_uv(cube)
        # the ModelPart WEST island (at u = 0) on the east quad, its EAST island (at u + dz + dx = 8) on the west quad,
        # every island with its own u direction (a positive uv_size in u) for a plain cube
        self.assertEqual(faces["east"], {"uv": [0, 6], "uv_size": [6, 4]})
        self.assertEqual(faces["west"], {"uv": [8, 6], "uv_size": [6, 4]})
        self.assertEqual(faces["north"], {"uv": [6, 6], "uv_size": [2, 4]})
        self.assertEqual(faces["south"], {"uv": [14, 6], "uv_size": [2, 4]})
        self.assertEqual(faces["up"], {"uv": [6, 0], "uv_size": [2, 6]})
        self.assertEqual(faces["down"], {"uv": [8, 6], "uv_size": [2, -6]})
        mirrored = converter.modelpart_face_uv(dict(cube, mirror=True))
        # a mirrored cube's polygons carry their islands u-reversed: the u origin at the far edge, a negative width
        self.assertEqual(mirrored["west"], {"uv": [6, 6], "uv_size": [-6, 4]})
        self.assertEqual(mirrored["east"], {"uv": [14, 6], "uv_size": [-6, 4]})
        self.assertEqual(mirrored["north"], {"uv": [8, 6], "uv_size": [-2, 4]})

    def test_frame_helpers_are_vanillas_chain(self) -> None:
        moved = parity.entity_frame([[1.0, 0.0, 0.0, 2.0], [0.0, 1.0, 0.0, 0.5], [0.0, 0.0, 1.0, -1.0], [0.0, 0.0, 0.0, 1.0]])
        # M (2, 0.5, -1) = (-2, 1.501 - 0.5, -1): the flip and the lift
        self.assertLess(converter_delta([row[3] for row in moved[:3]], [-2.0, 1.001, -1.0]), 1.0e-12)
        self.assertEqual([row[:3] for row in moved[:3]], [[-1.0, 0.0, 0.0], [0.0, -1.0, 0.0], [0.0, 0.0, 1.0]])
        self.assertEqual(parity.geo_pivot_classic_blocks([3.0, 16.0, -2.0]), [3.0 / 16.0, 0.5, -2.0 / 16.0])
        camera = parity.Camera([(-1.0, -1.0, -1.0), (1.0, 1.0, 1.0)], 0.0, 0.0)
        # the view looks through the entity frame's reflection: a point at (+1, +1) projects where ModelPart (-1, -1) did
        self.assertEqual(camera.project_unscaled((1.0, 1.0, 0.0)), (-1.0, -1.0, 0.0))

    def test_visual_leg_sees_a_mirrored_rig(self) -> None:
        root = Path(tempfile.mkdtemp(prefix="g1_mirror_"))
        try:
            texture = Image.new("RGBA", (2, 2))
            texture.putpixel(RED, (255, 0, 0, 255))
            texture.putpixel(BLUE, (0, 0, 255, 255))
            texture.putpixel(CLEAR, (0, 0, 0, 0))
            texture.save(root / "texture.png")
            spec = {"id": "synthetic", "texture": "texture.png", "visual_sample_ids": ["bind"],
                    "camera": {"yaw_degrees": 0.0, "pitch_degrees": 0.0}}

            def asymmetric(mirror_x: float) -> dict:
                # an invisible frame quad (the camera fit) and one red quad to the +x side of the origin
                frame = square(5.0, 10.0, CLEAR, 1.0)
                quad = [{"position": [x * mirror_x, y, 0.0], "uv": texel_uv(RED), "normal": [0.0, 0.0, 1.0]}
                        for x, y in ((0.2, -0.6), (1.4, -0.6), (1.4, 0.6), (0.2, 0.6))]
                return {"id": "bind", "capture_kind": "full", "render_vertices": frame + quad,
                        "cubes": [{"bone": "frame", "cube_index": 0, "vertices": frame},
                                  {"bone": "wing", "cube_index": 0, "vertices": quad}],
                        "draw_order": ["frame", "wing"]}

            same = parity.visual_parity("synthetic", spec, {"samples": [asymmetric(1.0)]}, {"samples": [asymmetric(1.0)]},
                                        root, root / "same", THRESHOLDS)
            self.assertEqual(same["samples"][0]["changed_fraction"], 0.0)
            with self.assertRaisesRegex(AssertionError, r"VISUAL MISMATCH synthetic/bind: changed fraction"):
                # the geo side drawn as the classic's left-right mirror: invisible to the old ModelPart-space compare
                # of a symmetric rig, a different picture here
                parity.visual_parity("synthetic", spec, {"samples": [asymmetric(1.0)]}, {"samples": [asymmetric(-1.0)]},
                                     root, root / "mirrored", THRESHOLDS)
        finally:
            shutil.rmtree(root, ignore_errors=True)


class UndrawnParts(unittest.TestCase):
    COMPILED = {"bone_names": ["body", "claw", "toe"], "definition": {"name": None, "cubes": [], "children": [
        {"name": "body", "cubes": [{}], "children": [{"name": "claw", "cubes": [{}], "children": []}]},
        {"name": "toe", "cubes": [{}], "children": []}]}}

    def test_candidate_bone_names_drop_the_undrawn_parts(self) -> None:
        spec = {"undrawn_parts": ["toe"]}
        self.assertEqual(parity.candidate_bone_names("m", spec, self.COMPILED, {}), ["body", "claw"])
        self.assertEqual(parity.candidate_bone_names("m", {}, self.COMPILED, {}), ["body", "claw", "toe"])
        with self.assertRaisesRegex(AssertionError, "lacks"):
            parity.candidate_bone_names("m", {"undrawn_parts": ["nope"]}, self.COMPILED, {})
        with self.assertRaisesRegex(AssertionError, "repeats"):
            parity.candidate_bone_names("m", {"undrawn_parts": ["toe", "toe"]}, self.COMPILED, {})

    @staticmethod
    def draw_order_inputs(classic: list[str]) -> tuple[dict, dict, dict, dict]:
        compiled = {"samples": [{"id": "bind", "capture_kind": "full", "draw_order": classic}]}
        geo_render = {"bone_draw_order": ["body", "claw"], "baked_bone_order": ["body", "claw"],
                      "samples": [{"id": "bind", "capture_kind": "full", "draw_order": ["body", "claw"]}]}
        geometry = {"minecraft:geometry": [{"description": {parity.DRAW_ORDER_KEY: ["body", "claw"]}}]}
        conversion = {"bone_draw_order": ["body", "claw"], "exact_bone_names": ["body", "claw"],
                      "draw_order_evidence": {"unobserved_units": []}}
        return compiled, geo_render, geometry, conversion

    def test_draw_order_leg_refuses_a_drawn_undrawn_part_loudly(self) -> None:
        with self.assertRaisesRegex(AssertionError, r"UNDRAWN PART DRAWN m/bind: .*toe"):
            parity.draw_order_parity("m", *self.draw_order_inputs(["body", "claw", "toe"]), undrawn=frozenset({"toe"}))
        with self.assertRaisesRegex(AssertionError, r"UNDRAWN PART DRAWN m/bind: .*toe__i1"):
            parity.draw_order_parity("m", *self.draw_order_inputs(["body", "toe__i0", "toe__i1", "claw"]),
                                     undrawn=frozenset({"toe"}))

    def test_draw_order_leg_passes_without_the_undrawn_part_and_records_it(self) -> None:
        report = parity.draw_order_parity("m", *self.draw_order_inputs(["body", "claw"]), undrawn=frozenset({"toe"}))
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(report["undrawn_parts"], ["toe"])
        self.assertNotIn("undrawn_parts", parity.draw_order_parity("m", *self.draw_order_inputs(["body", "claw"])))

    def test_converter_validates_the_list(self) -> None:
        self.assertEqual(converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["toe"]}, self.COMPILED), frozenset({"toe"}))
        self.assertEqual(converter.undrawn_parts_declared({"id": "m"}, self.COMPILED), frozenset())
        with self.assertRaisesRegex(ValueError, "children"):
            converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["body"]}, self.COMPILED)
        with self.assertRaisesRegex(ValueError, "lacks"):
            converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["nope"]}, self.COMPILED)
        with self.assertRaisesRegex(ValueError, "render_instances"):
            converter.undrawn_parts_declared({"id": "m", "undrawn_parts": ["toe"], "render_instances": {"toe": {}}}, self.COMPILED)

    def test_converter_refuses_a_drawn_undrawn_part_loudly(self) -> None:
        bones = [{"name": "body", "cubes": [{}]}, {"name": "claw", "cubes": [{}], "parent": "body"}]

        def compiled(classic: list[str]) -> dict:
            return {"model_id": "m", "draw_order_source": "test",
                    "samples": [{"id": "bind", "capture_kind": "full", "draw_order": classic}]}

        order, evidence = converter.derive_bone_draw_order(compiled(["body", "claw"]), bones, frozenset({"toe"}))
        self.assertEqual(order, ["body", "claw"])
        self.assertEqual(evidence["unobserved_units"], [])
        with self.assertRaisesRegex(ValueError, r"UNDRAWN PART DRAWN m: capture bind draws \['toe'\]"):
            converter.derive_bone_draw_order(compiled(["body", "toe", "claw"]), bones, frozenset({"toe"}))
        with self.assertRaisesRegex(ValueError, "not cube-bearing geo bones"):
            converter.derive_bone_draw_order(compiled(["body", "toe", "claw"]), bones)


class HierarchyForm(unittest.TestCase):
    """The FK slice (owner 2026-09-15, closing set, item 4; design section 5): the converter's hierarchy form - a declared
    child parented to its chain parent with the LOCAL bind rotation and the DERIVED pivot (the classic pivot carried back
    through the parent's bind rotation), the classic-branch convention the hook's FlatRig reads the flat bind back with,
    the pre-order RULE of a hierarchy entry (owner 2026-09-15, item 35 (5): the key is the tree's pre-order, the classic
    order's deviation recorded; the first attempt's measurement field retired) - the parity tool's chain-link leg, its
    draw-order leg's pre-order rule and its surface leg's named epsilon with the chain's accumulation, on a synthetic
    two-link chain.
    The synthetic rig: ``arm`` at (2, -1, -6) rotated (0, -0.5236, 0.1745) (the Alien's arml1) and its child ``claw`` at
    (11, 2, -1) rotated (0, 0.8552, 0) (arml2), one unit cube each."""
    ARM = {"pivot": [2.0, -1.0, -6.0], "rotation": [0.0, -0.5235988, 0.1745329]}
    CLAW = {"pivot": [11.0, 2.0, -1.0], "rotation": [0.0, 0.8552113, 0.0]}

    @classmethod
    def compiled(cls, classic_order: list[str]) -> dict:
        def part(name: str, spec: dict) -> dict:
            return {"name": name, "path": "/" + name, "local_pivot": spec["pivot"], "absolute_pivot": spec["pivot"],
                    "initial_rotation_radians": spec["rotation"], "children": [],
                    "cubes": [{"origin": [0.0, 0.0, 0.0], "size": [1.0, 1.0, 1.0], "deformation": [0.0, 0.0, 0.0],
                               "uv": [0.0, 0.0], "texture_scale": [1.0, 1.0], "mirror": False,
                               "visible_faces": ["down", "east", "north", "south", "up", "west"]}]}
        transforms = {name: {"position": spec["pivot"], "rotation": spec["rotation"], "scale": [1.0, 1.0, 1.0]}
                      for name, spec in (("arm", cls.ARM), ("claw", cls.CLAW))}
        return {"model_id": "m", "bone_names": ["arm", "claw"], "texture_width": 64, "texture_height": 32,
                "draw_order_source": "test",
                "definition": {"name": None, "path": "", "cubes": [], "children": [part("arm", cls.ARM), part("claw", cls.CLAW)]},
                "samples": [{"id": "bind", "capture_kind": "full", "draw_order": classic_order, "transforms": transforms}]}

    def test_converter_parents_the_child_with_the_local_rotation_and_the_derived_pivot(self) -> None:
        geometry, summary = converter.convert_geometry(self.compiled(["arm", "claw"]), hierarchy={"claw": "arm"})
        bones = {bone["name"]: bone for bone in geometry["minecraft:geometry"][0]["bones"]}
        self.assertNotIn("parent", bones["arm"])
        self.assertEqual(bones["claw"]["parent"], "arm")
        # the local bind rotation recomposes the child's flat (world) bind rotation under the parent's
        local = summary["hierarchy"]["local_bind_rotations_radians"]["claw"]
        parent = converter.rotation_matrix_zyx(self.ARM["rotation"])
        recomposed = converter.matrix_multiply(parent, converter.rotation_matrix_zyx(local))
        child = converter.rotation_matrix_zyx(self.CLAW["rotation"])
        self.assertLess(max(abs(recomposed[r][c] - child[r][c]) for r in range(3) for c in range(3)), 1.0e-9)
        self.assertTrue(any(abs(value) > 0.1 for value in local[0::2]), "a rolled parent makes the local a full triple")
        # the derived pivot: the classic pivot carried back through the parent's bind rotation, and the cubes at it
        inverse = converter.matrix_transpose(parent)
        offset = [self.CLAW["pivot"][i] - self.ARM["pivot"][i] for i in range(3)]
        expected = [self.ARM["pivot"][r] + sum(inverse[r][c] * offset[c] for c in range(3)) for r in range(3)]
        derived = summary["hierarchy"]["derived_pivots_classic"]["claw"]
        self.assertLess(converter_delta(derived, expected), 1.0e-9)
        # the Bedrock convention (TEST-015): the geo pivot keeps the classic x, y up from the 24-unit datum
        self.assertLess(converter_delta(bones["claw"]["pivot"], [expected[0], 24.0 - expected[1], expected[2]]), 1.0e-9)
        self.assertEqual(bones["claw"]["cubes"][0]["origin"],
                         converter.convert_cube(self.compiled(["arm", "claw"])["definition"]["children"][1]["cubes"][0], expected)["origin"])
        self.assertLess(converter_delta(bones["arm"]["pivot"], [2.0, 25.0, -6.0]), 1.0e-12)
        # the local bind rotation is written as +classic degrees on every axis (the sign rule, TEST-015)
        self.assertLess(converter_delta(bones["claw"]["rotation"], [math.degrees(value) for value in local]), 1.0e-9)
        self.assertLess(converter_delta(bones["arm"]["rotation"], [0.0, math.degrees(-0.5235988), math.degrees(0.1745329)]), 1.0e-9)
        # the flat (world) pivot the bake gives the child at bind is the classic one: P_p + R_p * (P_c - P_p)
        forward = [self.ARM["pivot"][r] + sum(parent[r][c] * (derived[c] - self.ARM["pivot"][c]) for c in range(3)) for r in range(3)]
        self.assertLess(converter_delta(forward, self.CLAW["pivot"]), 1.0e-9)
        self.assertEqual(geometry["minecraft:geometry"][0]["description"][converter.DRAW_ORDER_KEY], ["arm", "claw"])
        self.assertNotIn("hierarchy", converter.convert_geometry(self.compiled(["arm", "claw"]))[1])

    def test_hierarchy_geo_lists_the_bones_in_preorder(self) -> None:
        # the tree's pre-order (a parent before its children, siblings in the compiled order): the compiled tree lists the
        # child before its parent here, the flat geo keeps that order, the hierarchy geo puts the parent first
        compiled = self.compiled(["arm", "claw"])
        compiled["definition"]["children"].reverse()  # claw, then arm
        flat = converter.convert_geometry(compiled)[0]["minecraft:geometry"][0]["bones"]
        self.assertEqual([bone["name"] for bone in flat], ["claw", "arm"])
        nested = converter.convert_geometry(compiled, hierarchy={"claw": "arm"})[0]["minecraft:geometry"][0]["bones"]
        self.assertEqual([bone["name"] for bone in nested], ["arm", "claw"])
        self.assertEqual(converter.preorder_bones([{"name": "b"}, {"name": "a"}, {"name": "b1", "parent": "b"}, {"name": "a1", "parent": "a"}]),
                         [{"name": "b"}, {"name": "b1", "parent": "b"}, {"name": "a"}, {"name": "a1", "parent": "a"}])
        with self.assertRaisesRegex(ValueError, "does not carry"):
            converter.preorder_bones([{"name": "a", "parent": "nope"}])

    def test_classic_branch_recovers_a_yaw_past_a_quarter_turn(self) -> None:
        for triple in ([0.0, 2.268928, 0.0], [0.1745329, 2.70526, 0.0], [-2.602503, 0.0, 0.0], [0.0, 0.0, 2.240008],
                       [0.4505939, -1.3700851, -0.594227]):
            recovered = converter.euler_angles_zyx_classic_branch(converter.rotation_matrix_zyx(triple))
            self.assertLess(max(abs(converter.wrap_angle(recovered[i] - triple[i])) for i in range(3)), 1.0e-9, triple)
        self.assertIsNotNone(converter.local_bind_rotation(self.ARM["rotation"], [0.0, 2.268928, 0.0]))
        with self.assertRaisesRegex(ValueError, "convention"):
            # a child authored with a half-turn pitch AND roll beside a yaw is the other branch: refused, never silently held
            converter.local_bind_rotation([0.0, 0.0, 0.0], [3.0, 0.5, 3.0])

    def test_converter_validates_the_hierarchy(self) -> None:
        compiled = self.compiled(["arm", "claw"])
        self.assertEqual(converter.hierarchy_declared({"id": "m"}, compiled), {})
        self.assertEqual(converter.hierarchy_declared({"id": "m", "hierarchy": {"claw": "arm"}}, compiled), {"claw": "arm"})
        for bad, reason in (({"claw": "nope"}, "lacks"), ({"claw": "claw"}, "itself"), ({"claw": "arm", "arm": "claw"}, "cycle"),
                            ({}, "non-empty"), ({"claw": 3}, "non-empty")):
            with self.assertRaisesRegex(ValueError, reason):
                converter.hierarchy_declared({"id": "m", "hierarchy": bad}, compiled)
        with self.assertRaisesRegex(ValueError, "undrawn_parts"):
            converter.hierarchy_declared({"id": "m", "hierarchy": {"claw": "arm"}, "undrawn_parts": ["claw"]}, compiled)
        with self.assertRaisesRegex(ValueError, "top-level"):
            nested = self.compiled(["arm", "claw"])
            nested["definition"]["children"][0]["children"] = [nested["definition"]["children"].pop(1)]
            converter.hierarchy_declared({"id": "m", "hierarchy": {"claw": "arm"}}, nested)
        # the first attempt's measurement field is retired: refused, never read (the pre-order is the rule)
        with self.assertRaisesRegex(ValueError, "retired"):
            converter.hierarchy_declared({"id": "m", "hierarchy": {"claw": "arm"}, "hierarchy_draw_order": "preorder_diagnostic"}, compiled)
        with self.assertRaisesRegex(ValueError, "retired"):
            converter.hierarchy_declared({"id": "m", "hierarchy_draw_order": "classic"}, compiled)
        self.assertFalse(hasattr(converter, "hierarchy_draw_order_declared"))
        self.assertFalse(hasattr(converter, "HIERARCHY_DRAW_ORDER_PREORDER_DIAGNOSTIC"))

    def test_converter_emits_the_preorder_for_a_hierarchy_entry_and_records_the_classic_orders_deviation(self) -> None:
        # the hierarchy rules (owner 2026-09-15, item 35 (5)): a classic order that is not a pre-order of the declared
        # tree is not refused - the key IS the pre-order and the deviation is recorded for the visual leg to judge
        geometry, summary = converter.convert_geometry(self.compiled(["claw", "arm"]), hierarchy={"claw": "arm"})
        self.assertEqual(geometry["minecraft:geometry"][0]["description"][converter.DRAW_ORDER_KEY], ["arm", "claw"])
        recorded = summary["draw_order_evidence"]["hierarchy_preorder"]
        self.assertEqual(recorded["rule"], converter.HIERARCHY_PREORDER_RULE)
        self.assertEqual(recorded["classic_order"], ["claw", "arm"])
        self.assertEqual(recorded["preorder"], ["arm", "claw"])
        self.assertEqual(recorded["moved_units"], ["claw", "arm"])
        self.assertTrue(any("drawn after one of its descendants" in finding for finding in recorded["findings"]))
        self.assertEqual(summary["hierarchy"]["draw_order"], "preorder")
        self.assertEqual(summary["hierarchy"]["draw_order_rule"], converter.HIERARCHY_PREORDER_RULE)
        # a classic order that IS a pre-order records no moved unit and no finding
        agreeing = converter.convert_geometry(self.compiled(["arm", "claw"]), hierarchy={"claw": "arm"})[1]
        self.assertEqual(agreeing["draw_order_evidence"]["hierarchy_preorder"]["moved_units"], [])
        self.assertEqual(agreeing["draw_order_evidence"]["hierarchy_preorder"]["findings"], [])
        # a flat entry (no hierarchy) keeps the refusal: a nested compiled tree drawn child-first
        nested = self.compiled(["claw", "arm"])
        nested["definition"]["children"][0]["children"] = [nested["definition"]["children"].pop(1)]
        with self.assertRaisesRegex(ValueError, "drawn after one of its descendants"):
            converter.convert_geometry(nested)
        self.assertNotIn("hierarchy_preorder", converter.convert_geometry(self.compiled(["arm", "claw"]))[1]["draw_order_evidence"])

    def test_draw_order_leg_of_a_hierarchy_entry_compares_the_keys_preorder_and_records_the_deviation(self) -> None:
        compiled = self.compiled(["claw", "arm"])
        geometry, summary = converter.convert_geometry(compiled, hierarchy={"claw": "arm"})
        key = geometry["minecraft:geometry"][0]["description"][converter.DRAW_ORDER_KEY]
        conversion = {"bone_draw_order": summary["bone_draw_order"], "exact_bone_names": summary["exact_bone_names"],
                      "draw_order_evidence": summary["draw_order_evidence"], "hierarchy": summary["hierarchy"]}

        def geo_render(drawn: list[str]) -> dict:
            return {"bone_draw_order": key, "baked_bone_order": key,
                    "samples": [{"id": "bind", "capture_kind": "full", "draw_order": drawn}]}

        report = parity.draw_order_parity("m", compiled, geo_render(["arm", "claw"]), geometry, conversion, 1.0e-6,
                                          frozenset(), {"claw": "arm"})
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(report["hierarchy_rule"], "the key's pre-order")
        self.assertEqual((report["captures_checked"], report["draws_checked"]), (1, 2))
        deviation = report["classic_order_deviation"]
        self.assertEqual((deviation["captures_deviating"], deviation["max_moved_units"]), (1, 2))
        self.assertEqual(deviation["classic_order"], ["claw", "arm"])
        self.assertTrue(deviation["findings"])
        self.assertIn("parent-first", report["policy"])
        # the bake drawing anything but the key's pre-order is a mismatch; so is the classic drawing other units
        with self.assertRaisesRegex(AssertionError, r"DRAW ORDER MISMATCH m/bind: GeoRenderer drew .* key's pre-order"):
            parity.draw_order_parity("m", compiled, geo_render(["claw", "arm"]), geometry, conversion, 1.0e-6,
                                     frozenset(), {"claw": "arm"})
        # a flat entry on the same captures is judged the old way: the classic order, refused
        with self.assertRaisesRegex(AssertionError, r"DRAW ORDER MISMATCH m/bind: classic renderToBuffer drew"):
            parity.draw_order_parity("m", compiled, geo_render(["arm", "claw"]), geometry, conversion, 1.0e-6)

    @staticmethod
    def surface_sample(claw_normal: tuple[float, float, float]) -> dict:
        """One full capture of two one-quad cubes: the arm's +Z face and the claw's, the claw's normal as given."""
        def quad(bone: str, normal: tuple[float, float, float]) -> dict:
            vertices = [{"position": [x, y, 0.0], "uv": [0.25, 0.25], "normal": list(normal)}
                        for x, y in ((0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0))]
            return {"bone": bone, "cube_index": 0, "vertices": vertices}
        return {"id": "bind", "capture_kind": "full", "cubes": [quad("arm", (0.0, 0.0, 1.0)), quad("claw", claw_normal)]}

    def test_surface_leg_of_a_hierarchy_entry_uses_the_named_epsilon_and_records_the_accumulation(self) -> None:
        self.assertEqual(parity.HIERARCHY_NORMAL_EPSILON, 1.0e-5)
        vanilla = {"samples": [self.surface_sample((0.0, 0.0, 1.0))]}
        geo = {"samples": [self.surface_sample((3.0e-6, 0.0, 1.0))]}
        # a flat entry holds the normals to the manifest's epsilon: 3e-6 against 1e-6 is a mismatch
        with self.assertRaisesRegex(AssertionError, "RENDERER MAPPING MISMATCH m/bind/claw#0"):
            parity.surface_mapping_parity("m", vanilla, geo, 1.0e-5, 1.0e-6, 1.0e-7)
        # a hierarchy entry is held to the named HIERARCHY_NORMAL_EPSILON and records the chain's accumulation
        report = parity.surface_mapping_parity("m", vanilla, geo, 1.0e-5, 1.0e-6, 1.0e-7, frozenset(), {"claw": "arm"})
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(report["normal_epsilon"], parity.HIERARCHY_NORMAL_EPSILON)
        self.assertIn("HIERARCHY_NORMAL_EPSILON", report["normal_epsilon_rule"])
        self.assertAlmostEqual(report["max_normal_delta"], 3.0e-6, places=12)
        accumulation = report["chain_accumulation"]
        self.assertEqual(accumulation["worst_link"]["bone"], "claw")
        self.assertEqual((accumulation["worst_link"]["parent"], accumulation["worst_link"]["depth"]), ("arm", 1))
        self.assertAlmostEqual(accumulation["worst_link"]["max_normal_delta"], 3.0e-6, places=12)
        self.assertEqual(accumulation["worst_link"]["at"], "bind:claw#0")
        self.assertEqual(sorted(accumulation["max_normal_delta_by_depth"]), ["0", "1"])
        self.assertEqual(accumulation["max_normal_delta_by_depth"]["0"], 0.0)
        self.assertAlmostEqual(accumulation["max_normal_delta_by_depth"]["1"], 3.0e-6, places=12)
        self.assertEqual(parity.chain_depth("claw", {"claw": "arm"}), 1)
        self.assertEqual(parity.chain_depth("arm", {"claw": "arm"}), 0)
        # a flat entry's report carries none of it (byte-identical reports for every other rig)
        flat = parity.surface_mapping_parity("m", vanilla, vanilla, 1.0e-5, 1.0e-6, 1.0e-7)
        self.assertNotIn("chain_accumulation", flat)
        self.assertNotIn("normal_epsilon_rule", flat)
        self.assertEqual(flat["normal_epsilon"], 1.0e-6)

    @classmethod
    def chain_inputs(cls, claw_world_offset_blocks: float = 0.0, capture_kind: str = "full") -> tuple[dict, dict, dict, dict]:
        """The synthetic chain posed: the arm yawed to -0.9 and the claw's classic pivot rewritten as the Alien's follow
        does (9 units along the arm's yaw); the bake's world matrices derived from the classic ones through the pivot
        relation in the entity frame (M * classic_world == bone_pose * T(geo pivot / 16), TEST-015), the claw's
        translation moved by the given offset."""
        compiled = cls.compiled(["arm", "claw"])
        geometry, conversion_summary = converter.convert_geometry(compiled, hierarchy={"claw": "arm"})
        conversion = {"hierarchy": conversion_summary["hierarchy"]}
        arm_rot = [0.0, -0.9, 0.1745329]
        claw_pos = [2.0 + math.cos(-0.9) * 9.0, 2.0, -6.0 - math.sin(-0.9) * 9.0]
        transforms = {"arm": {"position": [2.0, -1.0, -6.0], "rotation": arm_rot, "scale": [1.0, 1.0, 1.0]},
                      "claw": {"position": claw_pos, "rotation": [0.0, 1.2, 0.0], "scale": [1.0, 1.0, 1.0]}}
        compiled["samples"] = [{"id": "bind", "capture_kind": "full", "draw_order": ["arm", "claw"],
                                "transforms": compiled["samples"][0]["transforms"]},
                               {"id": "t0", "capture_kind": capture_kind, "draw_order": ["arm", "claw"], "transforms": transforms}]
        bones = {bone["name"]: bone for bone in geometry["minecraft:geometry"][0]["bones"]}
        poses = {}
        for sample in compiled["samples"]:
            poses[sample["id"]] = {}
            for name, transform in sample["transforms"].items():
                classic_pivot = parity.geo_pivot_classic_blocks(bones[name]["pivot"])
                world = parity.entity_frame(parity.classic_world_matrix(transform))
                pose = parity.matrix_translate(world, [-value for value in classic_pivot])  # M world * T(-pivot) = bone pose
                if name == "claw" and sample["id"] == "t0":
                    pose[0][3] += claw_world_offset_blocks
                poses[sample["id"]][name] = pose
        geo_render = {"samples": [{"id": sample["id"], "capture_kind": sample["capture_kind"], parity.BONE_POSES_FIELD: poses[sample["id"]]}
                                  for sample in compiled["samples"]]}
        return compiled, geo_render, geometry, conversion

    def test_chain_link_leg_compares_world_transforms_at_every_link(self) -> None:
        compiled, geo_render, geometry, conversion = self.chain_inputs()
        hierarchy = parity.hierarchy_declared("m", {"hierarchy": {"claw": "arm"}}, compiled, conversion, geometry)
        report = parity.chain_link_parity("m", compiled, geo_render, geometry, hierarchy, 1.0e-5)
        self.assertEqual(report["status"], "PASS")
        self.assertEqual((report["links"], report["chain_roots"], report["samples_checked"], report["bone_samples_compared"]),
                         (1, ["arm"], 2, 4))
        self.assertLess(max(report["max_linear_delta"], report["max_translation_delta_blocks"]), 1.0e-9)
        self.assertEqual(report["per_link"]["claw"]["parent"], "arm")
        moved = self.chain_inputs(claw_world_offset_blocks=2.0e-5)
        with self.assertRaisesRegex(AssertionError, r"CHAIN LINK MISMATCH m/t0/claw \(child of arm\)"):
            parity.chain_link_parity("m", moved[0], moved[1], moved[2], hierarchy, 1.0e-5)
        partial = self.chain_inputs(capture_kind="transform_only")
        with self.assertRaisesRegex(AssertionError, "not a full capture"):
            parity.chain_link_parity("m", partial[0], partial[1], partial[2], hierarchy, 1.0e-5)

    def test_parity_hierarchy_declared_checks_the_converter_and_the_geo(self) -> None:
        compiled, _geo_render, geometry, conversion = self.chain_inputs()
        self.assertEqual(parity.hierarchy_declared("m", {}, compiled, {}, geometry), {})
        with self.assertRaisesRegex(AssertionError, "does not declare"):
            parity.hierarchy_declared("m", {}, compiled, conversion, geometry)
        with self.assertRaisesRegex(AssertionError, "drift"):
            parity.hierarchy_declared("m", {"hierarchy": {"claw": "arm"}}, compiled, {"hierarchy": {"declared": {}}}, geometry)
        flat = {"minecraft:geometry": [{"bones": [{"name": "arm"}, {"name": "claw"}]}]}
        with self.assertRaisesRegex(AssertionError, "parents claw to None"):
            parity.hierarchy_declared("m", {"hierarchy": {"claw": "arm"}}, compiled, conversion, flat)
        for bad, reason in (({"claw": "nope"}, "lacks"), ({"claw": "claw"}, "itself"), ({"claw": "arm", "arm": "claw"}, "cycle")):
            with self.assertRaisesRegex(AssertionError, reason):
                parity.hierarchy_declared("m", {"hierarchy": bad}, compiled, conversion, geometry)


def converter_delta(left: list, right: list) -> float:
    return max(abs(float(a) - float(b)) for a, b in zip(left, right))


if __name__ == "__main__":
    unittest.main(verbosity=1)
