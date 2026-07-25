"""Shared parse-result shapes for every source-specific parser.

Kept separate from any one parser module because more than one source now
uses it (`parser_nu_tahlil.py`, `parser_istighosah_nu.py`) — see
docs/operations/CONTENT_GOVERNANCE.md for how each source's draft still goes
through manual review before promotion, regardless of which parser produced it.
"""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class DraftStep:
    step_type: str
    title_id: str | None = None
    arabic_text: str | None = None
    translation_id: str | None = None
    repeat_target: int | None = None


@dataclass
class ParseReport:
    preamble_paragraphs_skipped: list[str] = field(default_factory=list)
    ambiguous_sections: list[dict] = field(default_factory=list)
    possible_quran_ayah_candidates: list[str] = field(default_factory=list)

    def add_ambiguous(self, reason: str, context: str) -> None:
        self.ambiguous_sections.append({"reason": reason, "context": context})


@dataclass
class ParseResult:
    steps: list[DraftStep]
    report: ParseReport
