# FamilyCountdown (Android kiosk)

A native Android port of the iPad app: a black "train-station board" listing
events by name with a split-flap countdown of days / hours / minutes / seconds
until each one. On the event's day the digits are replaced by a red **ARRIVED**;
the day after, repeating events (birthdays) roll to next year and one-time
events drop off. US holidays (New Year's, Easter, July 4th, Thanksgiving,
Christmas) are added automatically. The list is edited and stored on-device —
no server or password.

Built for a 15.6" 1920×1080 Android 12 touch kiosk (HIGOLE all-in-one) running
in landscape, but it works on any landscape tablet from Android 8 (API 26) up.

The gear (top-right) opens the editor: set the display time zone, shift saved
event times between zones, add / edit / delete events, and grant the one
permission needed for auto-start after a reboot.

## Build & run

Uses the toolchain described in `../../CLAUDE.md` (AGP 9.3.2, Kotlin 2.4.10,
Compose BOM 2024.09.02, JDK 17; the SDK lives on the `StudioMacCharlie` volume).

```sh
./gradlew assembleDebug testDebugUnitTest      # APK + unit tests
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.dialogs.familycountdown/.MainActivity
adb exec-out screencap -p > /tmp/board.png
```

A 1080p landscape emulator that mirrors the kiosk panel:

```sh
avdmanager create avd -n kiosk_1080p \
  -k "system-images;android-37.0;google_apis;arm64-v8a" -d "10.1in WXGA (Tablet)"
# then in ~/.android/avd/kiosk_1080p.avd/config.ini:
#   hw.lcd.width=1920  hw.lcd.height=1080  hw.lcd.density=160  hw.initialOrientation=landscape
emulator -avd kiosk_1080p -no-snapshot-save -no-boot-anim &
```

## Kiosk setup on the tablet

1. Install the APK with `adb install --no-streaming -r app-debug.apk` (or copy
   it over and open it). On the HIGOLE firmware a default *streamed* install
   produces an APK the package manager fails to parse at the next boot and
   silently uninstalls.
2. Launch it once. It runs fullscreen (immersive) in landscape and keeps the
   screen awake for as long as it is in the foreground.
3. Open the gear → **Kiosk → Auto-start after reboot → Allow…** and turn on
   *Display over other apps* for FamilyCountdown. Android 10+ only lets an app
   relaunch itself from the boot broadcast when this is granted. After that the
   board comes back by itself after a power cycle. Over adb the same grant is
   `adb shell appops set com.dialogs.familycountdown SYSTEM_ALERT_WINDOW allow`
   followed by `adb shell appops write-settings` (without the second command the
   grant is lost on reboot).
4. Optional: set the tablet's own screen-timeout to "never" as a belt-and-braces
   measure, and disable the lock screen so the boot receiver lands on the board.

## Layout

```
app/src/main/java/com/dialogs/familycountdown/
  model/      CountdownEvent, Iso8601, CountdownEngine, HolidayProvider, TimeShift,
              EventStore (JSON file), SettingsStore (SharedPreferences)
  ui/board/   BoardScreen (board + 1 s tick), BoardLayout (font-size / rows math),
              EventRow, FlipClock/FlipGroup/TileText/ClockHeader, FlipDigit/TileFace (split-flap)
  ui/editor/  EditorOverlay (in-window sheet + back stack), EventListScreen, EventEditorScreen
              (+ date / time pickers), TimeZonePickerScreen, ShiftTimesScreen
  MainActivity (immersive, keep-screen-on), BootReceiver, FamilyCountdownApp
app/src/main/assets/SeedEvents.json        first-launch seed (same file as iOS)
app/src/main/res/font/                     JetBrains Mono ExtraBold
app/src/test/.../CountdownTests.kt         holiday math, roll-forward, ARRIVED, store round-trip, time shift
```

## Notes

- **Split-flap animation**: `FlipDigit` recreates the PQINA two-leaf mechanism —
  the old top leaf folds down (0°→90°), then the new bottom leaf drops and bounces
  to settle, 800 ms with an ease-out-bounce curve, staggered 50 ms right-to-left
  across each digit group. Animation progress is read only inside `graphicsLayer`
  and the draw lambda, so the board never recomposes while flipping.
- **Board sizing** is done in physical pixels (not dp) using the same ratios as
  the iOS app, so any 1080p panel fills the same way regardless of the density
  it reports. The editor UI uses normal dp.
- **Storage**: `files/FamilyCountdownEvents.json` in the app's private storage,
  seeded on first launch from the bundled `SeedEvents.json`. The JSON format
  (`label`, `targetDate` ISO-8601 with offset, `pinned`, `repeats`) matches the
  original web page and the iPad app, so files interchange.
  Inspect it with `adb shell run-as com.dialogs.familycountdown cat files/FamilyCountdownEvents.json`.
- **Time zone**: gear → Display → Time Zone. Defaults to Automatic (follows the
  device). The zone controls day boundaries (ARRIVED / roll-forward), holiday
  midnights, and the editor's date fields; the countdown numbers are absolute.
- **Editor windows**: the editor, confirmations and pickers are drawn inside the
  activity window (no Dialogs) so the system bars never reappear on the kiosk.

## License

The code is released under the [MIT License](LICENSE).

The bundled font is **not** covered by that license: JetBrains Mono is
distributed under the [SIL Open Font License 1.1](JetBrainsMono-OFL.txt)
and keeps its own terms, which permit bundling it in this app.
