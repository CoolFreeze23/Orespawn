# Renderer pin scanner: reassigned-local tightening, lab record (2026-09-03)

Owner ruling: "Scanner blind spot: tighten it; any pin that changes as a result is presented before its gate."

Scanner: `tools/reference_renderer_pins.py` (the `referenceRenderers` leg under `check`). Three tool states are compared:
ORIG = the committed scanner before the tightening (a float local read only at its declaration); LAB = the first
tightened copy the refuters examined; FIX2 = the installed scanner after the refuters' minors (`%=`, `++`, `--`
unbind as unread forms; nested braceless ifs and dangling elses unbind; a braceless `while`/`do` wrapping a write
unbinds). Each case is a synthetic `CoinRenderer.java` (or `QueenRenderer.java` / candidate `CoinGeoReplacement.java`)
dropped over the real client sources; the probed entry is Coin (Queen for the Q cases). A pin PASSes only when the
value on the default path is provably the manifest value; every other outcome DIVERGES (a wrong value or an
unbound local with a reason), which fails the leg and sends a human to read the file.

## Repo result

PASS 120 / PENDING 0 / MOD 0 / NOT_APPLICABLE 13 / DIVERGES 0 / MANIFEST_DRIFT 0 before and after; no entry's
status or detail changed, so nothing needed presenting before the gate.

## Lab cases

| case | ORIG | LAB | FIX2 | FIX2 detail (probe entry) |
|---|---|---|---|---|
| A1_lambda_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| A2_anon_class_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| A3_anon_class_shadow_redecl_SCALE | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| A4_anon_class_shadow_redecl_literal | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s); SCALE constant exists but scale() scales by s / port CoinRenderer.java shadow 0.09375 (SHAD |
| A6_task_shadow_s2_example | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s2); SCALE constant exists but scale() scales by s2 / port CoinRenderer.java shadow 0.09375 (SH |
| C1_candidate_unconditional_reassign | - | - | DIVERGES | candidate scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.1 |
| C2_candidate_loop_write | - | - | DIVERGES | candidate scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09 |
| C3_candidate_if_reassign_default_SCALE | - | - | DIVERGES | candidate applyScale does not scale by CoinRenderer.SCALE / port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional SCALE |
| L1_while_braced | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L2_while_braceless | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L3_do_while_braced | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L4_do_while_braceless | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L5_for_braced | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L6_for_braceless_wrapping_braced_if | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| L7_while_braceless_wrapping_braced_if | PASS | PASS | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| L8_foreach_braceless | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| Q1_if_reassign_default_SCALE | PASS | PASS | PASS | port QueenRenderer.java shadow 3.8 (SHADOW); scale 2 (unconditional effectiveScale (if-reassignment default branch SCALE; writes: effectiveS |
| Q2_unconditional_reassign_literal | PASS | DIVERGES | DIVERGES | scale expected 2, found 3 (unconditional effectiveScale (reassigned: effectiveScale = 3.0F), scaleModelForRender); SCALE constant exists but |
| Q3_loop_write | PASS | DIVERGES | DIVERGES | port scale unparseable: scaleModelForRender: local effectiveScale is written inside a block that is neither if nor else (`effectiveScale = 3 |
| Q4_if_else_default_literal | PASS | DIVERGES | DIVERGES | scale expected 2, found 3 (unconditional effectiveScale (if/else-reassignment default branch 3.0F; writes: effectiveScale = SCALE / 4.0F; ef |
| Q5_unevaluable_default_write | PASS | DIVERGES | DIVERGES | port scale unparseable: scaleModelForRender: local effectiveScale is written on the default path with an expression that cannot be evaluated |
| R1_write_after_return_in_if | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| R2_if_write_then_return | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| R3_if_write_else_return | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| R4_guard_return_then_write | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)); SCALE constant exists but scale() scales by s (holds 2.0F on the def |
| S1_switch_arrow_statement | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| S2_switch_expression_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s = switch (entity.getId() % 2) |
| S3_switch_old_default_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| T1_try_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| T2_finally_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W10_line_comment_write | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W11_block_comment_write | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W12_block_comment_if_prefix | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)); SCALE constant exists but scale() scales by s (holds 2.0F on the def |
| W13_string_close_brace_in_if | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 1 (no scale call); SCALE constant exists but no unconditional scale site (poseStack.scale, or a scaleModelForRen |
| W14_string_close_brace_pops_if_SCALE | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 1 (no scale call); SCALE constant exists but no unconditional scale site (poseStack.scale, or a scaleModelForRen |
| W15_if_true | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F)); cand |
| W16_prefix_names | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W17_prefix_names_reverse | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s2); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRender |
| W19_chained_assignment | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s = t = 2.0F`: t = 2.0F) / port |
| W1_cast_write_literal | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = (float) 2.0)); SCALE constant exists but scale() scales by s (holds (float)  |
| W20_nested_ternary_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s = entity.isBaby() ? SCALE : ( |
| W21_unconditional_after_conditional | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 3 (unconditional s (reassigned: s = 2.0F; s = 3.0F)); SCALE constant exists but scale() scales by s (holds 3.0F  |
| W22_conditional_after_unconditional_SCALE | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = SCALE; s = 4 |
| W24_dangling_else_default_SCALE | DIVERGES | PASS | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 3.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W25_dangling_else_default_literal | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W26_two_stmts_after_braceless_if | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 3 (unconditional s (reassigned: s = 2.0F; s = 3.0F)); SCALE constant exists but scale() scales by s (holds 3.0F  |
| W27_braceless_else_after_braced_if | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment default branch SCALE; writes: s = 2.0F; s |
| W28_else_if_chain_final_else_literal | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 4 (unconditional s (if/else-reassignment default branch 4.0F; writes: s = 2.0F; s = 3.0F; s = 4.0F)); SCALE cons |
| W29_two_sites_compound | DIVERGES | DIVERGES | DIVERGES | scale applied more than once, rendering at 0.25 (2 unconditional scales compound to 0.25: scale(): s = 0.125; scale(): s = 2); a pin needs e |
| W2_cast_write_SCALE | PASS | DIVERGES | DIVERGES | SCALE constant exists but scale() scales by s (holds (float) SCALE on the default path; writes: s = (float) SCALE) / port CoinRenderer.java  |
| W30_site_in_loop_write_after | DIVERGES | DIVERGES | DIVERGES | port scale unparseable: a scale inside a block that is neither if nor else / port CoinRenderer.java shadow 0.09375 (SHADOW); scale UNPARSED  |
| W31_bare_block_write | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W32_if_nested_bare_block | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W33_lambda_block_in_if_condition | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written inside a block that is neither if nor else (`s = 2.0F`) / port CoinRenderer.java shadow 0.09375 ( |
| W34_self_referencing_write | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 0.25 (unconditional s (reassigned: s = s * 2.0F)); SCALE constant exists but scale() scales by s (holds s * 2.0F |
| W35_final_decl | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W37_paren_args_if_reassign | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional (s) (if-reassignment default branch SCALE; writes: s = 2.0F)); ca |
| W38_if_unevaluable_else_SCALE | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment default branch SCALE; writes: s = entity. |
| W3_same_line_decl_and_write | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (reassigned: s = 2.0F)); SCALE constant exists but scale() scales by s (holds 2.0F on the def |
| W42_loop_scoped_decl_then_redecl | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s); candidate CoinGeoReplacement.java shadow 0.09375 (CoinRendere |
| W43_ternary_decl_then_compound_one | PASS | DIVERGES | DIVERGES | SCALE constant exists but scale() scales by s (holds (SCALE) * (1.0F) on the default path; writes: s *= 1.0F) / port CoinRenderer.java shado |
| W45_multiline_write_expr | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (reassigned: s = SCALE)); candidate CoinGeoReplacement.java sha |
| W46_write_in_site_args | DIVERGES | DIVERGES | DIVERGES | port scale unparseable: scale argument not evaluable/uniform: [['s = 2.0F', 's', 's']] / port CoinRenderer.java shadow 0.09375 (SHADOW); sca |
| W47_else_write_after_if_without_write | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if/else-reassignment default branch SCALE; writes: s = SCALE)) |
| W48_if_write_SCALE_decl_literal | DIVERGES | DIVERGES | DIVERGES | scale expected 0.125, found 2 (unconditional s (if-reassignment default branch 2.0F; writes: s = SCALE)); SCALE constant exists but scale()  |
| W49_cast_args_plain_local | DIVERGES | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional (float) s); candidate CoinGeoReplacement.java shadow 0.09375 (Coi |
| W4_compound_div | PASS | DIVERGES | DIVERGES | scale expected 0.125, found 0.0625 (unconditional s (reassigned: s /= 2.0F)); SCALE constant exists but scale() scales by s (holds (SCALE) / |
| W50_string_semicolon_if_in_prefix | PASS | PASS | PASS | port CoinRenderer.java shadow 0.09375 (SHADOW); scale 0.125 (unconditional s (if-reassignment default branch SCALE; writes: s = 2.0F))); can |
| W5_compound_div_zero | PASS | DIVERGES | DIVERGES | port scale unparseable: local s is written on the default path with an expression that cannot be evaluated (`s /= 0.0F`: division by zero) / |
| W6_compound_mod | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`s %= 0.1F;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); |
| W7_postincrement | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`s++;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale |
| W8_preincrement | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`++s;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale |
| W9_postdecrement | PASS | PASS | DIVERGES | port scale unparseable: local s is written in a form the scan does not read (`s--;`) / port CoinRenderer.java shadow 0.09375 (SHADOW); scale |

Cases: 73 (3 added after the LAB run: C1_candidate_unconditional_reassign, C2_candidate_loop_write, C3_candidate_if_reassign_default_SCALE). Flips LAB -> FIX2 among the 70 shared cases:
6 (L7_while_braceless_wrapping_braced_if, W24_dangling_else_default_SCALE, W6_compound_mod, W7_postincrement, W8_preincrement, W9_postdecrement), all PASS -> DIVERGES, each where a refuter showed the LAB copy trusting a value it
had not read.

## Documented limitations

- A braceless `for` wrapping a braced `if` (case L6) reads as a plain if-write with the declaration
  value as the default, because the for-header's semicolons hide the loop from the statement look-back. The
  value on the never-taken path is still the declaration's, so the PASS is the same convention as any if-write;
  the braceless `while` and `do` forms (L7, L2, L4) unbind.
- A non-evaluable write on a non-default branch is treated like a ternary's taken branch (the default path
  keeps the declaration value) — an interpretation presented for ruling, not a silent choice.
- Locals written through a method call, an array element, or a field are outside the scan by design; a scale
  site that reads anything but a literal, a constant, or a tracked float local is UNPARSED, which DIVERGES.

