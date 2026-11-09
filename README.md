# AniLog — Native Android Anime Journal & Personal Collection App

**Your Anime. Your Memories. Your Ratings.**

AniLog is a full-featured, offline-first personal Anime Journal and collection manager built natively for Android with Kotlin, Jetpack Compose, Material 3, and Room Database (SQLite).

---

## Official Project Identity

- **Application Name**: AniLog
- **Application ID**: `com.anilog.omor.dev`
- **Kotlin Namespace**: `com.anilog.omor.dev`
- **Tagline**: *Your Anime. Your Memories. Your Ratings.*
- **Developer Name**: Muhammed Omor
- **Official Shop**: [https://muhammedomor.supportkori.shop/](https://muhammedomor.supportkori.shop/)
- **Contact Email**: `muhammedomor.studio@gmail.com`

---

## Core Feature Overview

1. **Ranked Personal Anime Feed**
   - Automatically sorted by highest personal rating down to lowest, with tiebreaker fallback.
   - 7 flexible sort options: Highest Rated, Lowest Rated, Recently Added, Oldest Added, Alphabetical (A-Z), Recently Updated, Recently Completed.
   - Real-time search across titles, Japanese titles, multiline reviews, personal notes, genres, and tags.
   - Quick status count chips (Total, Watching, Completed, Avg Rating).
   - Toggleable Grid and List layouts.

2. **Complete Anime Management**
   - Title (required) and original Japanese title.
   - Android zero-permission Photo Picker integration with private file copy into app storage (`covers/`).
   - 10-point personal rating with 0.5 half-point step accuracy (0.0 to 10.0) and dedicated Unrated state.
   - Full multiline review editor with verbatim Unicode, Bangla (`বাংলা`), and emoji support.
   - Personal notes and viewing memories.
   - Release year, episode progress (watched vs total), and watching status.
   - Start date and completion date pickers.
   - Favorites toggle.

3. **Episode Tracker & Watchlist**
   - Filter by Watching, Plan to Watch, Completed, On Hold, and Dropped.
   - One-tap `+1` / `-1` episode progress counter.
   - Interactive progress bar with percentage indicator.
   - Automatic prompt to mark anime as Completed and record completion date when reaching total episodes.

4. **Custom Anime Collections**
   - Create custom curated collections (e.g., *My Top 10*, *All-Time Favorites*, *Best Romance*, *Anime to Rewatch*).
   - Add and remove anime from collections.
   - Share formatted collection summary text.
   - Referential integrity: deleting a collection **never** deletes its underlying anime entries.

5. **Statistics Dashboard**
   - 100% computed from real local database entries (zero mock data).
   - Total anime, Completed, Watching, Plan to Watch, On Hold, Dropped.
   - Accurate average rating (strictly excludes unrated titles).
   - Highest rated and lowest rated anime highlights.
   - Total episodes watched metric.
   - Rating distribution histogram (0 to 10 points).
   - Top genre distribution bars.
   - Chronological Anime Journey timeline milestones.

6. **Side-by-Side Anime Comparison**
   - Select any two anime entries from your library.
   - Compare ratings, watching status, episode progress, release years, genres, and review excerpts.
   - Clear handling of missing fields with "Not specified" indicators.

7. **Review Card Generator & Native Sharesheet**
   - 5 handcrafted design templates:
     1. *Premium Dark* (Charcoal with violet indigo accents and serif titles)
     2. *Minimal Light* (Clean white backdrop with refined typography)
     3. *Cinematic* (Full-bleed dimmed cover backdrop with dramatic scrim)
     4. *Compact Review* (Dense card layout for quick status bites)
     5. *Top-Rated Anime* (Gold and amber collectible theme for masterpieces)
   - Live canvas preview.
   - Customizable excerpt adjustment without modifying the saved review.
   - Off-main-thread high-resolution rendering.
   - Native Android Sharesheet sharing via secure `FileProvider` (`content://`).
   - Save image directly to device storage.

8. **Reliable Local Backup & Restore**
   - Full ZIP archive backup containing `manifest.json` and all cover artwork.
   - Pure JSON metadata export.
   - UTF-8 CSV list export with BOM for Excel and spreadsheet compatibility (Bangla-safe).
   - Pre-restore validation checking schema version, anime count, collection count, and image count.
   - Safe restore modes:
     - **Merge**: preserves current records and imports non-duplicate entries.
     - **Replace**: cleans and restores exact archive snapshot.
   - Zip Slip / path traversal protection.

9. **Themes & Privacy Lock**
   - 4 selectable themes: Follow System, Light Theme, Dark Theme, and true AMOLED Black Theme.
   - Optional 4-digit PIN Passcode Privacy Lock with SHA-256 salted hash verification.

10. **Optional Online Metadata Lookup**
    - Free public Jikan API integration for anime title, cover art, genres, and episode count.
    - User previews online results before applying.
    - Never overwrites user personal ratings, reviews, or notes.
    - Fully resilient offline when no network is available.

11. **Developer & Store Section**
    - Developer: Muhammed Omor.
    - Direct Shop button launching `https://muhammedomor.supportkori.shop/`.
    - Email Contact button launching `ACTION_SENDTO` to `muhammedomor.studio@gmail.com` with prefilled subject `"AniLog App Support"` and clipboard fallback.

---

## Architecture & Technology Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Local Database**: Room Database 2.7.0 with SQLite and Kotlin Symbol Processing (KSP)
- **Preferences**: Jetpack DataStore Preferences
- **Asynchronous Execution**: Kotlin Coroutines & Reactive `StateFlow`
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil 2.7.0
- **Networking**: OkHttp for Jikan API
- **Sharing**: Android `FileProvider` and Android Sharesheet
- **Testing**: JUnit 4, Robolectric 4.16.1

---

## Building the APK Locally

To assemble the debug APK from the command line:

```bash
gradle assembleDebug
```

The resulting APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

To run all unit and database tests:

```bash
gradle testDebugUnitTest
```

---

## Automated CI/CD (GitHub Actions)

AniLog includes a production-grade workflow at `.github/workflows/android-build.yml`.
On every push and pull request, the workflow:
1. Sets up Temurin JDK 17 and Android SDK.
2. Restores Gradle dependency caches.
3. Runs all unit and Robolectric database tests.
4. Assembles the debug APK.
5. Verifies the APK artifact exists.
6. Uploads `AniLog-Debug-APK` as a downloadable artifact.
