# Tic Tac Toe (Kotlin/Android) — Project Notes for Claude

## What this project is

A native **Kotlin + Android Views** rewrite of a Flutter app
(https://github.com/ahmadabdullah414/Tic-Tac-Toe-Game-AI). The original was a
Flutter/Dart project; this is a from-scratch port that reproduces the same UI,
features, and behavior using plain Android (no Jetpack Compose), because the
target is **minSdk 19 (Android 4.4 KitKat)**, which Compose does not support.

Package: `com.game.tiktactoe`. No `app:` custom attributes are used in any XML
layout — see "The aapt2 landmine" below before adding any.

## Features (all implemented and manually verified on an emulator)

- Splash screen → main menu (player name inputs, Single Match / Best of 3 toggle,
  light/dark theme toggle persisted via SharedPreferences)
- 2-player mode: turn-shuffle animation, win-line highlight, best-of-3 tournament
  logic with a predetermined 1:2 turn-order ratio (matches original exactly)
- vs-AI mode: Easy/Medium/Hard difficulty. AI is a **heuristic**, not minimax:
  win → block → center → corner → edge, with a random "suboptimal" branch for
  Easy (80% suboptimal) and Medium (50% suboptimal) — see
  `services/LocalAI.kt`. This is a deliberate port of the original Dart logic,
  not a bug — do not "upgrade" it to a real minimax without being asked.
- Match history (SharedPreferences + JSON via `org.json`, no Room/Gson), with
  relative timestamps and a clear-with-confirmation dialog
- Full light/dark theme (DayNight) with persisted preference

## Build toolchain (chosen for a reason — read before changing)

- **AGP 8.13.2** + **Gradle 9.5.1** + Kotlin plugin **2.0.21** (separate
  `org.jetbrains.kotlin.android` plugin, not AGP's built-in Kotlin support)
- `compileSdk 34`, `minSdk 19`, `targetSdk 34`
- Library versions are **deliberately pinned low**, not just "old":
  - `androidx.appcompat:appcompat:1.6.1` — 1.7.0+ raises minSdk to 21
  - `androidx.core:core-ktx:1.13.1` — 1.15.0+ raises minSdk to 21
  - `com.google.android.material:material:1.12.0` — 1.13.0+ raises minSdk to 21
  - No `androidx.constraintlayout` — removed entirely (see below)
- `multiDexEnabled = true` (required for minSdk < 21 with desugaring) +
  `androidx.multidex:multidex` with `TicTacToeApp : MultiDexApplication()`
- `compileOptions`/Kotlin `jvmTarget` = **11**, not 8 — some AndroidX inline
  functions ship compiled for JVM 11 and fail to inline into JVM 8 bytecode

**Before bumping any AndroidX/Material version**, check its bundled
`AndroidManifest.xml` for `minSdkVersion` — if it's above 19, the manifest
merger will hard-fail the build. This project already found the exact
version ceiling for each library; don't casually "update to latest."

## The aapt2 landmine (read this before touching layouts)

In the environment this was built in, aapt2's resource linker **could not
resolve any custom `app:`-namespaced attribute contributed by a library AAR**
— not `app:tint` (AppCompat), not `app:icon`/`app:cornerRadius`/
`app:boxBackgroundColor` (Material), not `app:layout_constraintWidth_percent`
(ConstraintLayout) — across every AGP 8.x/9.x and Gradle 8.x/9.x combination
tried, and across multiple library versions. Only plain `android:` framework
attributes resolved correctly.

The workaround applied throughout this codebase: **Material/AppCompat widget
*classes* are used freely** (`MaterialButton`, `TextInputEditText` was
dropped in favor of plain `EditText`, etc.) but **all custom-attribute
styling is done programmatically in Kotlin**, not via XML. See:
- `ui/Shapes.kt` — builds `GradientDrawable` backgrounds (rounded rects,
  circles, alpha blending) in code instead of drawable XML + `app:` shape attrs
- `ui/Shapes.kt#styleButtonIcon` — sets `MaterialButton.icon/cornerRadius/
  iconTint/iconGravity` in Kotlin instead of `app:icon` etc. in XML
- `ui/ThemeToggle.kt` — tints ImageButtons via `ImageViewCompat.setImageTintList`
  instead of `app:tint`
- `ui/SquareBoardContainer.kt` / `ui/SquareCell.kt` — custom `onMeasure()`
  overrides replace what would normally be
  `app:layout_constraintDimensionRatio`/`layout_constraintWidth_percent`

**If you hit "attribute auto:xxx not found" errors on Windows too**: don't
assume it's the same root cause without checking — it's possible the Windows
toolchain (different AGP/Gradle/JDK combo, different aapt2 binary) doesn't
have this problem at all, and you can use normal `app:` attributes in XML.
Try a simple layout with `app:tint` first before assuming you need to keep
following the programmatic-styling pattern. If it *does* reproduce, the fix
is the same: style in Kotlin, not XML.

## Known limitations / things not yet verified

- **Never tested on a real Android 4.4 (API 19) device or emulator** — no API
  19 system image was available in the build environment. Tested on a
  modern emulator (API 37) with `minSdk=19` enforced at the manifest level.
  If you have a real KitKat device or can get an API 19 AVD on Windows,
  that's the most valuable remaining test.
- No unit/instrumented tests exist yet.
- No CI.

## If asked to keep working on this project

- Preserve the "port fidelity" intent: this is meant to be behaviorally
  identical to the original Flutter app, including its quirks (e.g. the
  AI's randomness percentages, the exact best-of-3 turn-order shuffle logic).
  Don't silently "improve" game logic without flagging it.
- Prefer fixing bugs found via actually running the app (emulator/device) over
  fixing them from code review alone — several real bugs in this codebase
  (a crash in `TurnShuffleView`, a text-truncation bug in the tournament
  dialog, dark-mode button text contrast) were only caught by running the app
  and screenshotting it, not by reading the code.
- Icons are hand-drawn simplified vector drawables in `res/drawable/ic_*.xml`
  (not the real Material Symbols set) — geometric/simple by design to avoid
  needing an icon font/library dependency.
