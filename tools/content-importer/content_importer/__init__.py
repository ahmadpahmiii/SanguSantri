"""Developer-only content draft pipeline (SanguSantri Milestone 3.5).

Converts a locally saved snapshot of one allowlisted, publicly available
source page into a structured JSON draft compatible with the Android app's
seed content schema (docs/content-schema.md). Never runs at application
runtime, never publishes content, never invents missing Arabic text or
translation. See docs/operations/CONTENT_GOVERNANCE.md for the full
editorial workflow this tool feeds into.
"""
