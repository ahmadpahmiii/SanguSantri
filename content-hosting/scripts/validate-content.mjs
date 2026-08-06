#!/usr/bin/env node
/**
 * Validates `public/content/` before every `firebase deploy --only hosting` (ADR 0015). Mirrors
 * the same structural rules `ContentValidator`/`ContentImporter` enforce on the Android side
 * (`app/src/main/java/com/sangusantri/app/data/content/ContentValidator.kt`) — this script is
 * intentionally simple tooling, not a second copy of that Kotlin logic; the Android importer
 * remains the authoritative, final validation gate regardless of what this script checks.
 *
 * Usage: node scripts/validate-content.mjs [--previous <path-to-previous-catalog.json>]
 * Exit code 0 = valid, non-zero = at least one error printed to stderr.
 */
import { readFileSync, existsSync } from "node:fs";
import { fileURLToPath } from "node:url";
import path from "node:path";

const SUPPORTED_SCHEMA_VERSION = 1;
const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const contentDir = path.resolve(scriptDir, "..", "public", "content");
const catalogPath = path.join(contentDir, "catalog.json");

const errors = [];

function readJson(filePath, label) {
  if (!existsSync(filePath)) {
    errors.push(`${label}: file not found at ${filePath}`);
    return null;
  }
  const raw = readFileSync(filePath, "utf-8");
  try {
    return JSON.parse(raw);
  } catch (cause) {
    errors.push(`${label}: invalid JSON (${cause.message})`);
    return null;
  }
}

function isBlank(value) {
  return typeof value !== "string" || value.trim().length === 0;
}

function validateCatalog(catalog) {
  if (catalog.schemaVersion !== SUPPORTED_SCHEMA_VERSION) {
    errors.push(`catalog.json: unsupported schemaVersion ${catalog.schemaVersion}`);
  }
  if (!Array.isArray(catalog.items)) {
    errors.push("catalog.json: items must be an array");
    return;
  }

  const seenIds = new Set();
  for (const item of catalog.items) {
    if (isBlank(item.id)) {
      errors.push("catalog.json: item id must not be blank");
      continue;
    }
    if (seenIds.has(item.id)) {
      errors.push(`catalog.json: duplicate item id "${item.id}"`);
    }
    seenIds.add(item.id);

    if (!Number.isInteger(item.version) || item.version <= 0) {
      errors.push(`catalog.json item "${item.id}": version must be a positive integer`);
    }
    if (isBlank(item.title)) errors.push(`catalog.json item "${item.id}": title must not be blank`);
    if (isBlank(item.description)) errors.push(`catalog.json item "${item.id}": description must not be blank`);
    if (isBlank(item.contentUrl)) errors.push(`catalog.json item "${item.id}": contentUrl must not be blank`);
  }
}

/** contentUrl is always rooted at "/content/..." (ADR 0015) — resolve it to a real file on disk. */
function resolveContentFilePath(contentUrl) {
  const relative = contentUrl.replace(/^\/content\//, "");
  return path.join(contentDir, relative);
}

function validateContentFile(item) {
  const filePath = resolveContentFilePath(item.contentUrl);
  const file = readJson(filePath, `content file for "${item.id}" (${item.contentUrl})`);
  if (file === null) return;

  if (file.schemaVersion !== SUPPORTED_SCHEMA_VERSION) {
    errors.push(`${item.contentUrl}: unsupported schemaVersion ${file.schemaVersion}`);
  }
  if (file.id !== item.id) {
    errors.push(`${item.contentUrl}: file id "${file.id}" does not match catalog id "${item.id}"`);
  }
  if (file.version !== item.version) {
    errors.push(
      `${item.contentUrl}: file version ${file.version} does not match catalog version ${item.version}`,
    );
  }
  if (isBlank(file.sourceName)) errors.push(`${item.contentUrl}: sourceName must not be blank`);
  if (isBlank(file.sourceUrl)) errors.push(`${item.contentUrl}: sourceUrl must not be blank`);

  if (!Array.isArray(file.steps) || file.steps.length === 0) {
    errors.push(`${item.contentUrl}: steps must not be empty`);
    return;
  }

  const seenStepIds = new Set();
  for (const step of file.steps) {
    if (isBlank(step.id)) {
      errors.push(`${item.contentUrl}: step id must not be blank`);
    } else if (seenStepIds.has(step.id)) {
      errors.push(`${item.contentUrl}: duplicate step id "${step.id}"`);
    } else {
      seenStepIds.add(step.id);
    }
    if (isBlank(step.arabicText)) errors.push(`${item.contentUrl} step "${step.id}": arabicText must not be blank`);
    if (isBlank(step.translation)) errors.push(`${item.contentUrl} step "${step.id}": translation must not be blank`);
    if (!Number.isInteger(step.repeatTarget) || step.repeatTarget < 1) {
      errors.push(`${item.contentUrl} step "${step.id}": repeatTarget must be at least 1`);
    }
  }
}

function validateNoVersionRegression(catalog, previousCatalogPath) {
  if (!previousCatalogPath) return;
  const previous = readJson(previousCatalogPath, "previous catalog");
  if (previous === null || !Array.isArray(previous.items)) return;

  const previousVersionById = new Map(previous.items.map((item) => [item.id, item.version]));
  for (const item of catalog.items) {
    const previousVersion = previousVersionById.get(item.id);
    if (previousVersion !== undefined && item.version < previousVersion) {
      errors.push(
        `catalog.json item "${item.id}": version ${item.version} regresses from previously deployed ${previousVersion}`,
      );
    }
  }
}

function parseArgs(argv) {
  const previousIndex = argv.indexOf("--previous");
  return {
    previousCatalogPath: previousIndex >= 0 ? argv[previousIndex + 1] : null,
  };
}

function main() {
  const { previousCatalogPath } = parseArgs(process.argv.slice(2));
  const catalog = readJson(catalogPath, "catalog.json");
  if (catalog === null) {
    printResultAndExit();
    return;
  }

  validateCatalog(catalog);
  if (Array.isArray(catalog.items)) {
    for (const item of catalog.items) {
      if (!isBlank(item.id) && !isBlank(item.contentUrl)) {
        validateContentFile(item);
      }
    }
    validateNoVersionRegression(catalog, previousCatalogPath);
  }

  printResultAndExit();
}

function printResultAndExit() {
  if (errors.length === 0) {
    console.log(`content-hosting validation passed (${contentDir})`);
    process.exit(0);
  }
  console.error(`content-hosting validation failed with ${errors.length} error(s):`);
  for (const error of errors) {
    console.error(`  - ${error}`);
  }
  process.exit(1);
}

main();
