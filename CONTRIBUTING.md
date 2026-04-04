# Contributing to OpenAscend

## Narrative flavor packs

Flavor JSON lives under `core/data/src/main/assets/narrative/`. Each file is named `{packId}.json` (for example `default.json`, `cozy.json`) and is loaded by `AssetNarrativeRepository` when the player picks a pack in **Settings → Narrative flavor**.

### Schema (high level)

- `id`: string, should match the filename stem.
- `actTitles`: twelve strings (one per calendar month slot; index wraps).
- `questActPrefix`: optional string prepended to generated daily quest titles.
- `bossTellTemplates`: lines with `{boss}` and `{stat}` placeholders for weekly boss whispers.
- `questTitleFlavorSuffixes`: list of optional suffixes appended to quest titles (deterministic rotation).

After adding or editing a pack:

1. Add the new `packId` chip in `SettingsScreen` if it should appear in the UI.
2. Run `./gradlew :core:domain:test` to ensure domain logic still passes.

## Room database

- Entities live under `core/data/.../db/`; the database version and migrations are in `OpenAscendDatabase.kt` and `app/.../di/DatabaseModule.kt`.
- If you add columns or tables, **bump the version** and add an `Migration(old, new)`; keep `fallbackToDestructiveMigration()` in mind for dev installs only—real users rely on migrations.

## Code style

Match existing Kotlin and Compose patterns in the repo; keep changes scoped to the feature you are shipping.

## Before you open a PR

Run unit tests and Lint (same gate as a full local check):

```bash
./gradlew check test
```
