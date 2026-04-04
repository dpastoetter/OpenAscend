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

## Home, widget, and reminders (voice)

- **Home** — Act line + **days left in the calendar-month “act”**; optional **boss-week** banner (`BossWeekArc`); **streak armor** chip when armor is high enough to matter; mood headline from yesterday’s check-in when present.
- **Companion / familiar** — Copy can reference **yesterday’s** sealed check-in, habits/quests, and evening mood—keep lines warm, non-judgmental, second-person.
- **Daily sigil** — Plain-text share blurb: chronicle tone + one-line disclaimer; not a medical/finance claim.
- **Widget** — Short **rotating story lines** (`WidgetStoryLines`) keyed by epoch day + flavor pack id; should read as in-world nudges, not chores.
- **Notifications** — Same voice as the chronicle (see `strings.xml` `notify_*`); copy changes should stay aligned with this doc.

## README pointer

Product vision and player-feel principles live in the root [README.md](../README.md).
