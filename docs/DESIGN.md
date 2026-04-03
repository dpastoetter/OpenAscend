# OpenAscend design & voice

## Visual tokens

- **Color system:** [`Theme.kt`](../app/src/main/kotlin/com/openascend/app/ui/theme/Theme.kt) — `AscendLight` / `AscendDark` fixed palettes; **System** theme may use Material 3 **dynamic color** on Android 12+.
- **Semantic roles:** `primary` = main actions / links; `surfaceVariant` = secondary panels; `primaryContainer` = highlighted cards (e.g. share preview).
- **Typography:** Use `MaterialTheme.typography` — `headlineMedium` for screen titles, `titleMedium` for section headers, `bodySmall` + `onSurfaceVariant` for helper / disclaimer text.

## Voice & tone

- **Fantasy layer:** Stats, quests, bosses, chronicle, realm — player-first RPG framing.
- **Safety layer:** One clear line where needed: not medical or financial advice; playful mirror only.
- **Buttons:** Prefer in-world verbs (*Seal*, *Enter the realm*) when clarity allows; keep destructive actions literal.

## Narrative flavor packs

- JSON under merged assets: `narrative/{id}.json` (e.g. [`default.json`](../core/data/src/main/assets/narrative/default.json)).
- **Contributors:** add a new file + optional Settings entry; keep keys stable (`actTitles`, `bossTellTemplates`, placeholders `{boss}`, `{stat}`).

## README pointer

Product vision and player-feel principles live in the root [README.md](../README.md).
