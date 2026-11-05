# Quién llama ("Who's calling")

**[⬇ Download the APK](https://github.com/alecelular/QuienLlama/releases/latest)** (Android 10 or newer). After installing, follow the "Setup" section.

An Android app that shows the **contact's name** on incoming calls even when the carrier sends the number in a different format from the one saved in your address book. (Spanish version: [README.md](README.md))

**The problem:** some carriers send the caller's number with the country code and mobile prefix (e.g. Argentina: `+549 + area + number`), while others send it in national format (`0 + area + number`). If your contacts are saved in one format, some phones (for example Samsung devices with firmware from another region) fail to match them and show only the raw number.

**The fix:** instead of duplicating every contact in a second format, the app compares the **last N digits** of the incoming number against your contacts.

## How it works

1. It first tries to match all the digits of the incoming number.
2. If nothing matches, it drops one digit from the left and tries again, down to a configurable minimum (default 10).
3. At the first level that has matches:
   - exactly one contact → its name is shown;
   - several contacts → an amber banner. You choose in the app what it shows: **all matching names** (handy for
     spotting duplicate contacts), **only the first one** (alphabetical order, saying how many matched) or just a warning.
4. It never falls back to a shorter match once it has found a longer one.

Details: a contact matches if its digits *end with* the tested suffix; contacts are counted per contact (two numbers of the same person are not ambiguous); numbers shorter than the minimum (147, 911…) only match exactly. The logic lives in
[`SuffixMatcher.kt`](app/src/main/java/com/quienllama/SuffixMatcher.kt), with tests in
[`SuffixMatcherTest.kt`](app/src/test/java/com/quienllama/SuffixMatcherTest.kt).

## Recent calls

The phone's dialer draws its own recents list and another app cannot change it. So the app has its own
**"Últimas llamadas" (recent calls)** screen, with names identified by the same rule as the banner.

There is also an **experimental** option (off by default): writing the name into the call log's "cached name" field,
only for calls that were left without a name and that the app identifies with a single contact. If your phone's dialer
displays that field, the name shows up in its own list. It needs the permission to write the call log and never
modifies calls that already have a name.

## Privacy

The app has **no Internet permission** and does not store or send your contacts: the address book is read into memory only to compare numbers. The call log permissions are optional and are used only on the phone.
You can check this in the
[`AndroidManifest.xml`](app/src/main/AndroidManifest.xml).

## Requirements

- Android 10 or newer.
- Permissions: read contacts, display over other apps, being the device's "caller ID & spam app" and, to show the
  name while the phone is locked, notifications and "full-screen notifications". Optional: reading the call log ("Últimas llamadas" screen) and
  writing it (names experiment).

## Setup

Open the app and tap the four buttons at the top until all four show ✔. On Samsung devices the third one is also under
Settings → Apps → Choose default apps → Caller ID & spam app. Then use "Probar sin llamar" (test without calling) to see the result for any number.

## Build

You need JDK 17 and the Android SDK with platform 36 and build-tools 35.0.0.

```
echo "sdk.dir=/path/to/your/android-sdk" > local.properties
./gradlew assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/app-debug.apk`. You can also open the folder in Android Studio.
To set up an Ubuntu machine from scratch, see [COMPILAR.md](COMPILAR.md) (in Spanish).

## Known limitations

- Tested on a Samsung Galaxy A17 with two Argentine carriers. Other manufacturers may behave differently.
- The banner floats over the call screen (it does not replace it); its vertical position can be adjusted in the app.
- The names experiment in the call log depends on the dialer displaying the "cached name" field; it is not
  guaranteed.
- While the phone is locked Android does not allow floating windows, so the banner is opened as a full-screen
  notification instead (this needs the fourth permission). It is requested ~1.5 s after the call starts ringing so it
  ends up above the call screen. To diagnose: `adb logcat -s QuienLlama`.
- The app's interface is currently in Spanish only.
- The system dialer may not notify the app about numbers it already recognizes as contacts.

## License

[PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0): you can use, copy and modify the app for free for noncommercial purposes. Selling it or using it commercially requires the author's permission.

© 2026 Alejandro F. Fernández (@alecelular on X, Instagram and other networks)

**Contributions:** by sending changes (pull requests) you agree to license them under this same license and allow the author to reuse them, including in commercial versions.

## Support the project

If you found this useful, you can buy me a coffee:
[![Buy me a coffee](https://cdn.cafecito.app/img/buttons/button_1.svg)](https://cafecito.app/rsp148)
(there is also a button on the app's screen; it only opens this page in your browser).

---

*I hope you enjoy this project as much as I enjoyed developing it.*
