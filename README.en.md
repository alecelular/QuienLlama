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
   - several contacts → an amber banner lists **all matching names** (handy for spotting duplicate contacts).
4. It never falls back to a shorter match once it has found a longer one.

Details: a contact matches if its digits *end with* the tested suffix; contacts are counted per contact (two numbers of the same person are not ambiguous); numbers shorter than the minimum (147, 911…) only match exactly. The logic lives in
[`SuffixMatcher.kt`](app/src/main/java/com/quienllama/SuffixMatcher.kt), with tests in
[`SuffixMatcherTest.kt`](app/src/test/java/com/quienllama/SuffixMatcherTest.kt).

## Privacy

The app has **no Internet permission** and does not store or send your contacts: the address book is read into memory only to compare numbers. You can check this in the
[`AndroidManifest.xml`](app/src/main/AndroidManifest.xml).

## Requirements

- Android 10 or newer.
- Permissions: read contacts, display over other apps, and being the device's "caller ID & spam app".

## Setup

Open the app and tap the three buttons at the top until all three show ✔. On Samsung devices the third one is also under
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
- The system dialer may not notify the app about numbers it already recognizes as contacts.

## License

[PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0): you can use, copy and modify the app for free for noncommercial purposes. Selling it or using it commercially requires the author's permission.

© 2026 Alejandro F. Fernández (@alecelular on X, Instagram and other networks)

**Contributions:** by sending changes (pull requests) you agree to license them under this same license and allow the author to reuse them, including in commercial versions.
