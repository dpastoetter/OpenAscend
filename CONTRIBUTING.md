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

## Code style

Match existing Kotlin and Compose patterns in the repo; keep changes scoped to the feature you are shipping.
