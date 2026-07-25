"""Source-specific parser for the NU Online Tahlil article.

Deliberately narrow: this module only knows how to read the specific
`#detail-content` layout of the one allowlisted Tahlil URL in `config.py`
(see the fetched snapshot inspected during Milestone 3.5). It must never
invent Arabic text or a translation that is not present in the source, and
it must report — not guess — any paragraph it cannot classify with
confidence. See docs/operations/CONTENT_GOVERNANCE.md.
"""

from __future__ import annotations

import re

from .draft_model import DraftStep, ParseReport, ParseResult
from .html_blocks import extract_paragraph_blocks

CONTAINER_ID = "detail-content"

_HEADING_RE = re.compile(r"^(\d{1,3})\.\s*(.+)$", re.DOTALL)
_REPEAT_RE = re.compile(r"(\d+)\s*(?:kali|[xX×])\b")
_ARTINYA_PREFIX_RE = re.compile(r"^Artinya,?\s*[“”\"']?\s*", re.IGNORECASE)
_QUOTE_CHARS = "\"'“”‘’"
_EMBEDDED_DIGIT_RE = re.compile(r"[0-9]")
_QURAN_HINT_RE = re.compile(r"\b(surat|ayat)\b", re.IGNORECASE)
_END_OF_ARTICLE_RE = re.compile(r"^pewarta\b", re.IGNORECASE)

_WHITESPACE_RE = re.compile(r"\s+")


def _normalise(text: str) -> str:
    """Collapse HTML whitespace runs to a single space; never touches non-whitespace
    characters, so Arabic harakat (combining marks) are always preserved untouched."""
    return _WHITESPACE_RE.sub(" ", text.replace("\xa0", " ")).strip()


def _strip_artinya_wrapper(text: str) -> str:
    """Strip the source's stock 'Artinya, "..."' quoting around every translation.

    Only removes the single opening quote right after "Artinya," and a single
    trailing quote character at the very end of the string — both pair with
    that same stock prefix, so removing them is deterministic formatting
    clean-up, not a content edit. Quote characters appearing anywhere else
    (nested quotations inside a translation, e.g. quoted speech) are left
    untouched.
    """
    stripped = _ARTINYA_PREFIX_RE.sub("", text, count=1).strip()
    if stripped and stripped[-1] in _QUOTE_CHARS:
        stripped = stripped[:-1].rstrip()
    return stripped


def parse_tahlil_html(html: str) -> ParseResult:
    blocks = extract_paragraph_blocks(html, CONTAINER_ID)

    report = ParseReport()
    steps: list[DraftStep] = []

    seen_first_heading = False
    current_heading_title: str | None = None
    current_is_prayer = False
    current_repeat_target: int | None = None
    repeat_target_claimed = True  # no heading-derived count is "pending" until a heading sets one
    pending_arabic: str | None = None

    def flush_unpaired_arabic(context: str) -> None:
        nonlocal pending_arabic, repeat_target_claimed
        if pending_arabic is None:
            return
        report.add_ambiguous(
            reason="Arabic paragraph had no following translation paragraph; "
            "kept with translationId=null for manual review.",
            context=context,
        )
        steps.append(
            DraftStep(
                step_type="PRAYER" if current_is_prayer else "ARABIC_TEXT",
                arabic_text=pending_arabic,
                translation_id=None,
                repeat_target=_take_repeat_target(),
            )
        )
        pending_arabic = None

    def _take_repeat_target() -> int | None:
        nonlocal repeat_target_claimed, current_repeat_target
        if repeat_target_claimed or current_repeat_target is None:
            return None
        repeat_target_claimed = True
        return current_repeat_target

    for block in blocks:
        text = _normalise(block.text)
        css_class = block.css_class or ""

        if _END_OF_ARTICLE_RE.match(text):
            # Byline ("Pewarta: ...\nEditor: ..."): reliable end-of-article marker for
            # this template. Nothing after this line is Tahlil content.
            break

        if "arabic" in css_class:
            if not text:
                report.add_ambiguous(
                    reason="Empty Arabic paragraph in source HTML (formatting artifact); "
                    "skipped rather than guessed. If a real Arabic segment is missing here, "
                    "the source must be re-checked manually.",
                    context=f"after heading: {current_heading_title!r}",
                )
                continue
            if pending_arabic is not None:
                flush_unpaired_arabic(context=f"heading: {current_heading_title!r}")
            if _EMBEDDED_DIGIT_RE.search(text):
                report.add_ambiguous(
                    reason="Arabic text contains an embedded digit (likely a repetition "
                    "marker mixed into the recitation text, e.g. '3x' or '* 10'). "
                    "Extracted verbatim, not cleaned up automatically — review before "
                    "production.",
                    context=text[:60],
                )
            pending_arabic = text
            continue

        heading_match = _HEADING_RE.match(text) if text else None
        if heading_match and not text.lower().startswith("artinya"):
            flush_unpaired_arabic(context=f"heading: {current_heading_title!r}")
            seen_first_heading = True
            current_heading_title = heading_match.group(2).strip()
            current_is_prayer = "doa" in current_heading_title.lower()
            repeat_match = _REPEAT_RE.search(current_heading_title)
            current_repeat_target = int(repeat_match.group(1)) if repeat_match else None
            repeat_target_claimed = current_repeat_target is None
            if _QURAN_HINT_RE.search(current_heading_title):
                report.possible_quran_ayah_candidates.append(current_heading_title)
            steps.append(DraftStep(step_type="HEADING", title_id=current_heading_title))
            continue

        if text.lower().startswith("artinya"):
            translation = _strip_artinya_wrapper(text)
            if pending_arabic is None:
                report.add_ambiguous(
                    reason="Translation paragraph had no preceding Arabic paragraph to "
                    "pair with; dropped.",
                    context=translation[:60],
                )
                continue
            steps.append(
                DraftStep(
                    step_type="PRAYER" if current_is_prayer else "ARABIC_TEXT",
                    arabic_text=pending_arabic,
                    translation_id=translation,
                    repeat_target=_take_repeat_target(),
                )
            )
            pending_arabic = None
            continue

        if not text:
            continue

        if not seen_first_heading:
            report.preamble_paragraphs_skipped.append(text[:80])
            continue

        report.add_ambiguous(
            reason="Paragraph did not match the heading, Arabic, or translation pattern.",
            context=text[:80],
        )

    flush_unpaired_arabic(context=f"end of document, last heading: {current_heading_title!r}")

    return ParseResult(steps=steps, report=report)
