"""Generic, dependency-free HTML block extraction (stdlib `html.parser` only).

This is intentionally narrow: it extracts `<p>` elements from inside one named
container element, skipping known non-editorial subtrees (ads, "read also"
boxes). It is not a general-purpose HTML-to-text converter and is not meant
to be reused for a different page layout — a new source gets its own parser
module, not a generic option on this one (CLAUDE.md: no generic universal
scraper).
"""

from __future__ import annotations

from dataclasses import dataclass
from html.parser import HTMLParser

VOID_TAGS = {"br", "img", "input", "hr", "meta", "link", "area", "base", "col", "embed", "source", "track", "wbr"}

# Subtrees inside the target container that are never editorial Tahlil content:
# related-article recommendation boxes, inline ad slots, print-only spacers.
SKIP_DIV_MARKERS_ID_PREFIXES = ("paragraph-news-",)
SKIP_DIV_MARKERS_ID_SUBSTRINGS = ("w2g-slot",)
SKIP_DIV_MARKERS_CLASS_SUBSTRINGS = ("adsbygoogle", "print:hidden")


@dataclass(frozen=True)
class TextBlock:
    tag: str
    css_class: str | None
    text: str


class _ContainerBlockExtractor(HTMLParser):
    def __init__(self, container_id: str) -> None:
        super().__init__(convert_charrefs=True)
        self._container_id = container_id
        self.in_target = False
        self._stack: list[str] = []
        self._skip_from: int | None = None
        self._current_p: dict | None = None
        self.blocks: list[TextBlock] = []

    def _is_skip_div(self, attrs: dict[str, str]) -> bool:
        div_id = attrs.get("id", "")
        div_class = attrs.get("class", "")
        if any(div_id.startswith(prefix) for prefix in SKIP_DIV_MARKERS_ID_PREFIXES):
            return True
        if any(needle in div_id for needle in SKIP_DIV_MARKERS_ID_SUBSTRINGS):
            return True
        if any(needle in div_class for needle in SKIP_DIV_MARKERS_CLASS_SUBSTRINGS):
            return True
        return False

    def handle_starttag(self, tag: str, attrs_list) -> None:
        attrs = {k: (v or "") for k, v in attrs_list}

        if not self.in_target:
            if tag == "div" and attrs.get("id") == self._container_id:
                self.in_target = True
                self._stack = ["div"]
            return

        if tag in VOID_TAGS:
            if tag == "br" and self._current_p is not None and self._skip_from is None:
                self._current_p["parts"].append("\n")
            return

        self._stack.append(tag)

        if self._skip_from is not None:
            return

        if tag == "div" and self._is_skip_div(attrs):
            self._skip_from = len(self._stack) - 1
            return

        if tag == "p":
            self._current_p = {"class": attrs.get("class"), "parts": []}

    def handle_endtag(self, tag: str) -> None:
        if not self.in_target:
            return

        if tag in VOID_TAGS:
            return

        if tag == "p" and self._skip_from is None and self._current_p is not None:
            text = "".join(self._current_p["parts"])
            self.blocks.append(TextBlock(tag="p", css_class=self._current_p["class"], text=text))
            self._current_p = None

        if self._stack and self._stack[-1] == tag:
            self._stack.pop()
        elif tag in self._stack:
            while self._stack and self._stack[-1] != tag:
                self._stack.pop()
            if self._stack:
                self._stack.pop()

        if self._skip_from is not None and len(self._stack) <= self._skip_from:
            self._skip_from = None

        if not self._stack:
            self.in_target = False

    def handle_data(self, data: str) -> None:
        if not self.in_target or self._skip_from is not None:
            return
        if self._current_p is not None:
            self._current_p["parts"].append(data)


def extract_paragraph_blocks(html: str, container_id: str) -> list[TextBlock]:
    """Return, in document order, every `<p>` block inside `#container_id`.

    Paragraphs inside recognised non-editorial subtrees (see
    SKIP_DIV_MARKERS_*) are omitted entirely, never surfaced as ambiguous —
    they are not Tahlil content in the first place.
    """
    parser = _ContainerBlockExtractor(container_id)
    parser.feed(html)
    parser.close()
    return parser.blocks
