"""Source-specific parser for the Quran NU Online Istighotsah & Mujahadah page.

Deliberately narrow, like `parser_nu_tahlil.py`: this module only knows how
to read one specific reading section — "Istighotsah (KH Romli Tamim)", the
first of the seven readings listed on the allowlisted URL in `config.py` —
out of that one page's specific (Tailwind/Next.js, class-based, no stable
container id) layout. It never invents Arabic text or a translation not
present in the source, and reports rather than guesses whenever a repetition
count cannot be confirmed from more than one place in the source. See
docs/operations/CONTENT_GOVERNANCE.md.

Page layout (verified against a live fetch): the target reading is delimited
by its own `<h1>...</h1>` heading and the next reading's `<h1>`. Inside that
span, each reading item's content column is a `<div class="flex-grow ...">`
containing exactly three sibling `<span>` elements in document order: Arabic
text (`dir="rtl"`), a Latin transliteration, and the Indonesian translation.
The Latin transliteration is parsed only to keep the sibling-order state
machine correct — its text is never stored (SanguSantri currently uses
Arabic and Indonesian translation only; CLAUDE.md). (Each item also has a
sibling `<div class="nui-ActionVerse ...">` for its action-button column,
which closes before the content column opens — not a useful container for
the spans themselves, hence keying off `flex-grow` instead.)

A reading may also contain a bare `dir="rtl"` `<span>` sub-heading (e.g.
"Sayyidul Istighfar") sitting directly in the bordered row div, with no
`flex-grow` content-column wrapper, immediately before the verse it names —
verse detection is scoped to inside `flex-grow` specifically so this kind of
heading is never mistaken for a verse's Arabic text (confirmed against the
live page: without this scoping, the sub-heading eats one verse's Arabic
slot and shifts the remaining two spans of that verse into the wrong roles).
"""

from __future__ import annotations

import re
from html.parser import HTMLParser

from .draft_model import DraftStep, ParseReport, ParseResult

TARGET_HEADING = "Istighotsah (KH Romli Tamim)"

VOID_TAGS = {"br", "img", "input", "hr", "meta", "link", "area", "base", "col", "embed", "source", "track", "wbr"}

_WHITESPACE_RE = re.compile(r"\s+")
_ARABIC_INDIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"
_AR_REPEAT_RE = re.compile(r"[×xX]\s*([" + _ARABIC_INDIC_DIGITS + r"]+)\s*$")
# Indonesian numeric convention uses "." as a thousands separator (e.g. "30.000x)").
_IDN_REPEAT_RE = re.compile(r"\(([\d.]+)\s*x\)\.?\s*$", re.IGNORECASE)
_QURAN_HINT_RE = re.compile(r"\b(surat|ayat)\b", re.IGNORECASE)


def _normalise(text: str) -> str:
    """Collapse HTML whitespace runs to a single space; never touches non-whitespace
    characters, so Arabic harakat (combining marks) are always preserved untouched."""
    return _WHITESPACE_RE.sub(" ", text.replace("\xa0", " ")).strip()


def _arabic_indic_to_int(digits: str) -> int:
    return int("".join(str(_ARABIC_INDIC_DIGITS.index(ch)) for ch in digits))


class _IstighosahSectionExtractor(HTMLParser):
    """Walks the whole page once, collecting ordered heading/verse items found
    only between the target reading's `<h1>` and the next reading's `<h1>`."""

    def __init__(self, target_heading: str) -> None:
        super().__init__(convert_charrefs=True)
        self._target_heading = target_heading
        self._stack: list[str] = []

        self._in_h1 = False
        self._h1_depth: int | None = None
        self._h1_parts: list[str] = []
        self.section_state = "before"  # "before" -> "in_target" -> "after"

        self._verse_content_depth: int | None = None  # stack depth of the enclosing flex-grow content div
        self._await = "arabic"  # which verse span slot is expected next, while inside a verse container
        self._capture_role: str | None = None  # "heading" | "arabic" | "latin" | "indonesian"
        self._capture_depth: int | None = None
        self._capture_parts: list[str] = []
        self._pending_arabic: str | None = None

        # Ordered, interleaved: ("heading", text) or ("verse", arabic_text, indonesian_text).
        self.items: list[tuple[str, ...]] = []

    def handle_starttag(self, tag: str, attrs_list) -> None:
        if tag in VOID_TAGS:
            return
        attrs = {k: (v or "") for k, v in attrs_list}
        self._stack.append(tag)

        if tag == "h1":
            self._in_h1 = True
            self._h1_depth = len(self._stack)
            self._h1_parts = []
            return

        if self._in_h1 or self.section_state != "in_target":
            return

        if tag == "div" and self._verse_content_depth is None and "flex-grow" in attrs.get("class", ""):
            self._verse_content_depth = len(self._stack)
            self._await = "arabic"
            return

        if tag != "span" or self._capture_role is not None:
            return

        if self._verse_content_depth is not None:
            # Inside a verse content container: only the sequential arabic/latin/indonesian slots capture.
            if self._await == "arabic" and attrs.get("dir") != "rtl":
                return
            self._capture_role = self._await
            self._capture_depth = len(self._stack)
            self._capture_parts = []
        elif attrs.get("dir") == "rtl":
            # A bare dir="rtl" span outside any verse container is a sub-heading.
            self._capture_role = "heading"
            self._capture_depth = len(self._stack)
            self._capture_parts = []

    def handle_endtag(self, tag: str) -> None:
        if tag in VOID_TAGS:
            return

        depth = len(self._stack)
        if self._stack and self._stack[-1] == tag:
            self._stack.pop()
        elif tag in self._stack:
            while self._stack and self._stack[-1] != tag:
                self._stack.pop()
            if self._stack:
                self._stack.pop()

        if tag == "h1" and self._in_h1 and depth == self._h1_depth:
            self._finish_h1()
            return

        if tag == "div" and self._verse_content_depth is not None and depth == self._verse_content_depth:
            self._verse_content_depth = None
            self._pending_arabic = None
            self._await = "arabic"
            return

        if tag == "span" and self._capture_depth is not None and depth == self._capture_depth:
            self._finish_span()

    def handle_data(self, data: str) -> None:
        if self._in_h1:
            self._h1_parts.append(data)
            return
        if self._capture_depth is not None:
            self._capture_parts.append(data)

    def _finish_h1(self) -> None:
        heading_text = _normalise("".join(self._h1_parts))
        self._in_h1 = False
        self._h1_depth = None
        if self.section_state == "before" and heading_text == self._target_heading:
            self.section_state = "in_target"
        elif self.section_state == "in_target" and heading_text != self._target_heading:
            self.section_state = "after"

    def _finish_span(self) -> None:
        text = _normalise("".join(self._capture_parts))
        role = self._capture_role
        self._capture_role = None
        self._capture_depth = None
        self._capture_parts = []

        if role == "heading":
            if text:
                self.items.append(("heading", text))
            return

        if role == "arabic":
            self._pending_arabic = text
            self._await = "latin"
        elif role == "latin":
            # Latin transliteration text is intentionally discarded — never stored.
            self._await = "indonesian"
        elif role == "indonesian":
            if self._pending_arabic:
                self.items.append(("verse", self._pending_arabic, text))
            self._pending_arabic = None
            self._await = "arabic"


def _extract_repeat_target(
    arabic_text: str,
    indonesian_text: str,
    report: ParseReport,
) -> int | None:
    ar_match = _AR_REPEAT_RE.search(arabic_text)
    idn_match = _IDN_REPEAT_RE.search(indonesian_text)
    ar_count = _arabic_indic_to_int(ar_match.group(1)) if ar_match else None
    idn_digits = idn_match.group(1).replace(".", "") if idn_match else ""
    idn_count = int(idn_digits) if idn_digits else None

    if ar_count is not None and idn_count is not None:
        if ar_count == idn_count:
            return ar_count
        report.add_ambiguous(
            reason=(
                f"Conflicting repetition counts: Arabic-embedded marker says {ar_count}, "
                f"Indonesian translation says {idn_count}. Not auto-resolved — repeatTarget "
                "left null pending manual review."
            ),
            context=arabic_text[:60],
        )
        return None

    if ar_count is not None:
        report.add_ambiguous(
            reason=(
                f"Repetition count ({ar_count}) taken from the Arabic-embedded marker only — "
                "the Indonesian translation does not restate it. Verify against the source "
                "before treating this as confirmed."
            ),
            context=arabic_text[:60],
        )
        return ar_count

    if idn_count is not None:
        report.add_ambiguous(
            reason=(
                f"Repetition count ({idn_count}) taken from the Indonesian translation only — "
                "no matching marker found in the Arabic text. Verify against the source before "
                "treating this as confirmed."
            ),
            context=indonesian_text[:60],
        )
        return idn_count

    return None


def parse_istighosah_html(html: str) -> ParseResult:
    extractor = _IstighosahSectionExtractor(TARGET_HEADING)
    extractor.feed(html)
    extractor.close()

    report = ParseReport()
    steps: list[DraftStep] = [DraftStep(step_type="HEADING", title_id=TARGET_HEADING)]

    if extractor.section_state == "before":
        report.add_ambiguous(
            reason=f"Heading {TARGET_HEADING!r} was not found on the page at all — the source "
            "layout may have changed. No steps extracted beyond the heading.",
            context=TARGET_HEADING,
        )
        return ParseResult(steps=[], report=report)

    if _QURAN_HINT_RE.search(TARGET_HEADING):
        report.possible_quran_ayah_candidates.append(TARGET_HEADING)

    for item in extractor.items:
        if item[0] == "heading":
            heading_text = item[1]
            if _QURAN_HINT_RE.search(heading_text):
                report.possible_quran_ayah_candidates.append(heading_text)
            steps.append(DraftStep(step_type="HEADING", title_id=heading_text))
            continue

        _, arabic_text, indonesian_text = item
        if _QURAN_HINT_RE.search(indonesian_text):
            report.possible_quran_ayah_candidates.append(indonesian_text[:80])
        repeat_target = _extract_repeat_target(arabic_text, indonesian_text, report)
        steps.append(
            DraftStep(
                step_type="PRAYER",
                arabic_text=arabic_text,
                translation_id=indonesian_text,
                repeat_target=repeat_target,
            ),
        )

    return ParseResult(steps=steps, report=report)
