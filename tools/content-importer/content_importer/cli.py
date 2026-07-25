"""Command-line entry points: `python -m content_importer {fetch,parse,validate}`."""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path

from . import fetch as fetch_module
from .builder import build_draft_package, build_provenance, report_to_dict
from .config import DEFAULT_SOURCE_ID, OUTPUT_DIR_NAME, SNAPSHOT_DIR_NAME, SOURCES
from .parser_nu_tahlil import parse_tahlil_html
from .validate import validate_package

TOOL_ROOT = Path(__file__).resolve().parent.parent


def _snapshot_dir() -> Path:
    return TOOL_ROOT / SNAPSHOT_DIR_NAME


def _output_dir() -> Path:
    return TOOL_ROOT / OUTPUT_DIR_NAME


def _cmd_fetch(args: argparse.Namespace) -> int:
    try:
        metadata = fetch_module.fetch_source(args.source, _snapshot_dir())
    except fetch_module.FetchError as exc:
        print(f"fetch failed: {exc}", file=sys.stderr)
        return 1

    print(f"saved snapshot: {_snapshot_dir() / metadata.snapshotFile}")
    print(f"retrieved at:   {metadata.retrievedAtUtc}")
    print(f"bytes:          {metadata.byteLength}")
    print(f"sha256:         {metadata.sha256}")
    return 0


def _cmd_parse(args: argparse.Namespace) -> int:
    snapshot_dir = _snapshot_dir()
    if args.snapshot:
        snapshot_path = Path(args.snapshot)
    else:
        try:
            snapshot_path = fetch_module.latest_snapshot(args.source, snapshot_dir)
        except fetch_module.FetchError as exc:
            print(f"parse failed: {exc}", file=sys.stderr)
            return 1

    meta_path = snapshot_path.with_suffix(snapshot_path.suffix + ".meta.json")
    if not meta_path.exists():
        print(f"parse failed: missing snapshot metadata sidecar {meta_path}", file=sys.stderr)
        return 1
    snapshot_meta_dict = json.loads(meta_path.read_text(encoding="utf-8"))
    snapshot = fetch_module.SnapshotMetadata(**snapshot_meta_dict)

    html = snapshot_path.read_text(encoding="utf-8")
    result = parse_tahlil_html(html)

    package = build_draft_package(result, snapshot)
    package_json = json.dumps(package, indent=2, ensure_ascii=False) + "\n"
    package_checksum = hashlib.sha256(package_json.encode("utf-8")).hexdigest()
    provenance = build_provenance(snapshot, package_checksum)
    report = report_to_dict(result)

    out_dir = _output_dir()
    out_dir.mkdir(parents=True, exist_ok=True)
    source_spec = SOURCES[args.source]
    base_name = f"{source_spec.snapshot_prefix.replace('-nu-online', '-general-v1')}"

    package_path = out_dir / f"{base_name}.draft.json"
    provenance_path = out_dir / f"{base_name}.provenance.json"
    report_path = out_dir / f"{base_name}.report.json"

    package_path.write_text(package_json, encoding="utf-8")
    provenance_path.write_text(json.dumps(provenance, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    report_path.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    print(f"wrote draft package: {package_path}")
    print(f"wrote provenance:    {provenance_path}")
    print(f"wrote report:        {report_path}")
    print(f"package sha256:      {package_checksum}")
    print()
    print(f"steps extracted:               {report['stepsExtracted']}")
    print(f"preamble paragraphs skipped:   {len(report['preambleParagraphsSkipped'])}")
    print(f"ambiguous sections flagged:    {len(report['ambiguousSections'])}")
    print(f"possible QURAN_AYAH candidates: {len(report['possibleQuranAyahCandidates'])}")
    if report["ambiguousSections"]:
        print()
        print("Ambiguous sections (manual review required):")
        for item in report["ambiguousSections"]:
            print(f"  - {item['reason']}")
            print(f"    context: {item['context']!r}")
    return 0


def _cmd_validate(args: argparse.Namespace) -> int:
    if args.package:
        package_path = Path(args.package)
    else:
        source_spec = SOURCES[args.source]
        base_name = f"{source_spec.snapshot_prefix.replace('-nu-online', '-general-v1')}"
        package_path = _output_dir() / f"{base_name}.draft.json"

    if not package_path.exists():
        print(f"validate failed: {package_path} does not exist — run `parse` first", file=sys.stderr)
        return 1

    package = json.loads(package_path.read_text(encoding="utf-8"))
    errors = validate_package(package)

    if errors:
        print(f"INVALID: {package_path}")
        for error in errors:
            print(f"  - {error}")
        return 1

    print(f"VALID: {package_path}")
    print(f"  {len(package.get('steps', []))} steps, schemaVersion={package.get('schemaVersion')}, "
          f"version.status={package.get('version', {}).get('status')}, "
          f"approval.status={package.get('approval', {}).get('status')}")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="content_importer",
        description="Developer-only content draft pipeline (SanguSantri Milestone 3.5). "
        "Never runs at application runtime.",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    fetch_parser = subparsers.add_parser("fetch", help="download the allowlisted source page")
    fetch_parser.add_argument("--source", default=DEFAULT_SOURCE_ID, choices=sorted(SOURCES))
    fetch_parser.set_defaults(func=_cmd_fetch)

    parse_parser = subparsers.add_parser("parse", help="parse a snapshot into a draft JSON package")
    parse_parser.add_argument("--source", default=DEFAULT_SOURCE_ID, choices=sorted(SOURCES))
    parse_parser.add_argument("--snapshot", help="path to a specific snapshot HTML file (default: latest fetched)")
    parse_parser.set_defaults(func=_cmd_parse)

    validate_parser = subparsers.add_parser("validate", help="structurally validate a generated draft package")
    validate_parser.add_argument("--source", default=DEFAULT_SOURCE_ID, choices=sorted(SOURCES))
    validate_parser.add_argument("--package", help="path to a specific draft JSON file (default: latest parsed)")
    validate_parser.set_defaults(func=_cmd_validate)

    return parser


def main(argv: list[str] | None = None) -> int:
    parser = build_parser()
    args = parser.parse_args(argv)
    return args.func(args)
