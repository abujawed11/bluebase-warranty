# Bluebase Warranty: Native Android Rewrite

This file is the complete handoff guide for this project. It is written for both humans and AI coding agents (Claude Code reads `CLAUDE.md` automatically; other agents are pointed here by `AGENTS.md`). Read it fully before writing code.

---

## 1. What this project is

**Bluebase Warranty** is a native Android (Kotlin + Jetpack Compose) rewrite of an existing, working **React Native / Expo** app called **"Sunrack Warranty App"**.

The app belongs to **Sunrack**, a manufacturer of solar mounting structures (RCC kits: columns, rafters, purlins and bracing that hold solar panels). The app lets:

- **Clients** (installers/customers) scan the QR code on a delivered kit, see their orders and kits, and **file a warranty claim** with a checklist, GPS location and step-by-step photos of the installed structure. Once approved, they get a **digital warranty card**.
- **Admins** (Sunrack staff) manage clients, orders and kits, **review warranty claims** (view all photos/videos, export a PDF), approve or reject them, and issue the warranty card.
- **Developers** (hidden role) delete test users and test claims.

**Goal of this repository:** reproduce the React Native app's behavior **screen for screen** in native Android, talking to the **same, unchanged backend**. The backend is not part of this repo and must not need any changes.

### Why native
The owner chose native Android for performance, a smaller APK, and direct access to platform APIs (CameraX, ML Kit, Media3). This is an Android-only app, and there is no iOS or web target.

---

## 2. The two codebases

| | Reference app (source of truth) | This repo (the rewrite) |
|---|---|---|
| Tech | React Native 0.81, Expo SDK 54, expo-router, TypeScript | Kotlin, Jetpack Compose, Material 3 |
| GitHub | https://github.com/abujawed11/product-qr-scanner | https://github.com/abujawed11/bluebase-warranty |
| Package id | `com.abujawed11.reactnativeqrscanner` | `com.sunrack.bluebase` |
| Status | In production, complete | Skeleton only (theme + placeholder screen) |

### ⚠️ Locating the reference app on this machine

**The React Native app's folder path is different on each computer.** Never hard-code it.
On the original machine it was `D:\react\product-qr-scanner`, but do not assume that.

**Agents: before reading the reference code, find it like this:**

1. Check `CLAUDE.local.md` in this repo root (git-ignored, per-machine). It should contain a line like:
   `REFERENCE_APP_PATH=<absolute path to product-qr-scanner>`
2. If that file or line doesn't exist, look for a sibling or nearby folder named `product-qr-scanner` (for example `../product-qr-scanner`, `../../react/product-qr-scanner`). A match is confirmed when its `app.json` contains `"name": "Sunrack Warranty App"`.
3. If still not found, **ask the user** for the path, or offer to clone it:
   `git clone https://github.com/abujawed11/product-qr-scanner.git`
4. Once found, create or update `CLAUDE.local.md` with `REFERENCE_APP_PATH=...` so later sessions don't have to search again.

In the rest of this document, **`<RN>`** means the reference app's root folder.

The reference app is **read-only**. Never modify it; only read it to understand behavior.

---

## 3. Backend (unchanged, shared by both apps)

- **Base URL:** `https://rcckitportal.sun-rack.com/api` (defined in `<RN>/utils/constants.ts`)
- **Media/document base:** `https://rcckitportal.sun-rack.com` (for `media_file`, `pdf_url` and other relative file paths)
- **Server type:** Django REST Framework with JWT (SimpleJWT style). List endpoints for admin use DRF pagination (`{count, next, previous, results}`); `next`/`previous` are absolute URLs.
- **Auth header:** `Authorization: Bearer <access>`
- The RN app enables `usesCleartextTraffic`, but production is HTTPS. Use a debug-only network-security config if you need a LAN dev server (commented-out URLs in `constants.ts`, like `http://192.168.x.x:8000`).
- Request timeout in RN is **5 minutes** (large media uploads). Keep long timeouts on the upload client.

### Authentication flow (from `<RN>/context/AuthContext.tsx` and `<RN>/utils/api.ts`)
- `POST /token/` with `{username, password}` returns `{access, refresh, user}`
- `POST /token/refresh/` with `{refresh}` returns `{access, refresh?}`. The refresh token may or may not rotate, so keep the old one if none is returned.
- `POST /token/logout/` with `{refresh}` (best effort; clear local state even if it fails)
- Tokens and the `user` JSON are stored in **SecureStore** under the keys `access`, `refresh` and `user`.
- A proactive refresh is scheduled **10 s before the access token's `exp`**, decoded from the JWT.
- On any **401**, the app refreshes once and retries the original request. If refresh fails, it shows "Session Expired, please login again" and logs out.
- On app start, if all three stored values exist, the user is logged in without a network call.

### User model
```ts
User { id, username, email, client_id, account_type: 'admin' | 'client' | 'developer', is_active, company_name }
```
**Routing after login:**
- `account_type == 'admin'` goes to the Admin section.
- A developer goes to the Developer section. In RN this is detected by `username in ('developer', 'dev_admin', 'abujawed11')` or `email == 'developer@company.com'`. See `<RN>/DEVELOPER_README.md`, which also documents a hidden "tap the welcome text 7 times" entry.
- Everyone else (client) goes to the Main/Client section.

### Endpoint catalogue (every call the RN app makes)

**Auth / account**
| Method | Path | Used in |
|---|---|---|
| POST | `/token/` | login |
| POST | `/token/refresh/` | auth |
| POST | `/token/logout/` | logout |
| POST | `/send-otp/` | register, forgot password |
| POST | `/verify-otp/` | forgot password |
| POST | `/reset-password/` | forgot password |
| POST | `/register/` | register (`{client_id, username, email, password, otp}`, called **without** an auth header) |

**Client: scanning, orders, kits**
| Method | Path | Notes |
|---|---|---|
| POST | `/save-order/` | Body is from the scanned QR JSON (see §5.2). Returns `{scan_id, all_scanned, total_kits}` or `{kit_id}` |
| POST | `/upload-qr/` | multipart: an image of a QR code picked from the gallery, same response as above |
| GET | `/saved-orders/` | "My Scans" |
| GET | `/orders/` | my orders / all orders |
| GET | `/orders/{order_id}/` | order details |
| GET | `/kit/{kit_id}/` | kit details |
| GET | `/kit-scan-details/{scan_id}/` | kit details after a scan |
| GET | `/kits/` | product info catalogue |
| GET | `/warranty-dashboard-counts/` | home dashboard counters |

**Client: warranty**
| Method | Path | Notes |
|---|---|---|
| POST | `/warranty-claims/` | multipart claim submission (see §5.3) |
| GET | `/warranty-claims-status/` | my claims list |
| GET | `/warranty-claims-status-byid/{war_req_id}/` | claim status detail |
| GET | `/warranty-cards/my/` | my warranty cards |
| GET | `/warranty-cards/by-claim/{war_req_id}/` | card for one claim |

**Notifications** (in-app list from the API; there are **no push tokens and no FCM**)
| Method | Path |
|---|---|
| GET | `/notifications/` |
| GET | `/notifications/unread_count/` (bell badge) |
| PATCH | `/notifications/{id}/{mark_read or similar}/` (check `<RN>/app/(main)/notifications.tsx`) |
| PATCH | `/notifications/mark_all_read/` |
| DELETE | `/notifications/{id}/` |
| DELETE | `/notifications/clear_all/` |

**Admin**
| Method | Path |
|---|---|
| GET | `/admin/orders/` (paginated, with filter/sort query params) |
| GET | `/admin/orders/{order_id}/` and `/admin/orders/{order_id}/kits/` |
| GET | `/admin/kits/`; PUT `/admin/kits/{kit_id}/update/` |
| POST | `/admin/users/create/`; PUT `/admin/users/{id}/update/`; DELETE `/admin/users/{id}/delete/` |
| GET | `/warranty-claims-status-summary/` (admin warranty dashboard) |
| GET | `/warranty-claims-by-status/?status=...` |
| GET | `/warranty-claim-clients/` then `/warranty-claim-orders/?client_id=...` then `/warranty-claims-by-order/?order_id=...` |
| GET | `/warranty-claims/{war_req_id}/` (full detail, including uploads and warranty_card) |
| PATCH | `/warranty-claims/{war_req_id}/update/` (status + warranty card fields) |

**Developer**
| Method | Path |
|---|---|
| GET / DELETE | `/developer/users/`, `/developer/users/{id}/delete/` |
| GET / DELETE | `/developer/warranty-claims/`, `/developer/warranty-claims/{id}/delete/` |

> Request and response field names aren't fully listed here. **Always open the matching RN screen** to confirm the exact JSON shape before writing a Kotlin DTO. Types live in `<RN>/types/*.ts`, but many screens define their own inline types.

---

## 4. Screen inventory (reference app, about 14k lines of TypeScript)

Paths are relative to `<RN>/app/`. Group folders in parentheses are expo-router route groups. The line counts show how much work each screen is.

### Entry
- `_layout.tsx`: root providers (React Query config, Auth, Refresh), notification permission and channel, network listener, hides the Android nav bar.
- `index.tsx`: splash/redirect based on login state and `account_type`.

### (auth): stack, no header
- `login.tsx` (197), `register.tsx` (277, OTP flow), `forgot-password.tsx` (261, send OTP, verify, reset)

### (main): client area, uses a **Drawer** (`components/CustomDrawer.tsx`)
- `dashboard/` (Stack)
  - `index.tsx`, `home.tsx` (142, counters)
  - `qr-scanner.tsx` (303, camera + upload QR image)
  - `my-orders.tsx` (113), `my-scans.tsx` (260)
- `warranty/` (Stack)
  - `index.tsx`, `warranty-status.tsx` (332), `warranty-status-page.tsx` (271)
  - `claim-form.tsx` (380): contact details plus checklist, autofilled from route params
  - `claim-media-wizard.tsx` (414): step-by-step photos, location, upload with progress
  - `claim-steps.ts` (258): **the list of 14 required photo steps**
  - `my-cards.tsx` (235), `warranty-card.tsx`
- `all-orders.tsx`, `order-details.tsx` (313), `kit-details.tsx` (330), `product-info.tsx`
- `notifications.tsx` (345), `installation-manual.tsx` (345), `about.tsx` (248)
- `profile.tsx`, `settings.tsx`, `support.tsx` are **placeholder stubs** (11 lines each)
- `test-upload.tsx`: a debug screen; **don't port it**

### (adminDashboard): admin area, **Drawer** (`components/CustomAdminDrawer.tsx`)
- `index.tsx` (Admin Home)
- `manage-clients.tsx` (413, CRUD), `manage-orders.tsx` (185, paginated + `FilterSortBar`), `admin-order-details.tsx` (255), `manage-kits.tsx` (252)
- `review-req-dashboard.tsx` (status summary), `review-req-warranty-status.tsx` (294)
- `warranty-clients.tsx` (301), `warranty-orders.tsx`, `warranty-requests.tsx` (a clients, orders, claims drill-down)
- `review-req-warranty.tsx`, `review-claim-fulldetails.tsx` (**985, the hardest screen**: image gallery, video player, PDF export via print, save media to Downloads)
- `review-claim-update.tsx` (549): set the status (`under_review` / `approved` / `rejected`); when approved, fill in the warranty card (type, duration in months, start date, expiry, coverage description, invoice number and date)

### (developer): Stack, red header
- `index.tsx`, `delete-users.tsx` (342), `delete-claims.tsx` (274)

### Shared components (`<RN>/components/`)
`MediaStep.tsx` (645, capture/pick/compress one step's media), `ChecklistStep.tsx`, `ReviewStep.tsx`, `WarrantyCard.tsx`, `AdminOrderCard.tsx`, `FilterSortBar.tsx`, `FloatingInput.tsx`, `FloatingPicker.tsx`, `CustomHeader.tsx`, `HeaderIcons.tsx`, `NotificationBell.tsx`, `NotificationBadge.tsx`, `UploadModal.tsx`

### Utilities (`<RN>/utils/`)
`api.ts`, `constants.ts`, `theme.ts` (colors, already ported), `mediaUtils.ts` (image compression, file sizes), `locationUtils.ts`, `downloadExcel.ts`, `formatDate.ts`, `statusColor.ts`, `mapCodeToCity.ts`, `networkListener.ts`, `useNotificationBadge.ts`, `taskUtils.ts`

---

## 5. Key business flows (port these exactly)

### 5.1 Login and session
See §3. Persist tokens encrypted. Expose the session as a `StateFlow<User?>` so navigation reacts to login and logout.

### 5.2 QR scan (`dashboard/qr-scanner.tsx`)
- Scan **QR codes only**. The payload is **JSON**:
  `{"kit_id": "...", "prod_unit": "...", "warehouse": "...", "project_id": "...", "date": "..."}`
  All five fields are required; otherwise show an error.
- `POST /save-order/` with those fields. Then:
  - `scan_id` with `all_scanned == false` goes to Kit Details with `scan_id`
  - `scan_id` with `all_scanned == true` goes to Kit Details with `scan_id`, `all_scanned=true`, `total_kits`
  - `kit_id` only goes to Kit Details with `kit_id`
- Show the server's `error` message in a dialog.
- Alternative: pick an image from the gallery, then `POST /upload-qr/` (multipart), same routing.
- Reset the "scanned" lock whenever the screen regains focus.

### 5.3 Warranty claim (`warranty/claim-form.tsx`, then `claim-media-wizard.tsx`)
1. The claim form is prefilled from navigation params: `client_id, company_name, order_id, kit_id, kit_no, project_id, purchase_date, email`. The user adds a contact name and phone and answers the checklist (`ChecklistStep`, boolean answers keyed by question).
2. The media wizard walks through the **14 steps in `claim-steps.ts`** (keys like `full_front_view`, `full_rear_view`, `column_web_level`, `rafter_column_lip1` and so on, through `bracing_rafter_rafter_side`), each with a title, description and demo image URL. Each step needs a photo from the camera or gallery. Images are **compressed to JPEG quality 0.5 with the original dimensions kept** (`mediaUtils.compressImage`). Video upload is currently **disabled** ("coming soon").
3. It gets the device location (foreground permission) and needs the declaration checkbox (`accepted_statement`) to be accepted.
4. It submits `POST /warranty-claims/` as **multipart**:
   - text fields: `clientId, companyName, clientName, phone, email, orderId, kitId, kitNo, projectId, purchaseDate` (camelCase, as sent by RN)
   - `accepted_statement` = `"true"` / `"false"`
   - `latitude`, `longitude` (if available)
   - `checklist` = JSON string
   - for each media file, three parts in order: `files` (file, named `step_<key>_<image|video>.<ext>`, `image/jpeg` or `video/mp4`), then `step_key`, then `media_type`
5. It shows upload progress. On success it replaces the screen stack with the dashboard.

### 5.4 Admin review
Drill down (clients, orders, claims) or go by status, open the full details, update the status, and fill in the warranty card when approving. PDF export in RN uses `expo-print` with an HTML template. Media can be saved to Downloads.

---

## 6. Target architecture (this repo)

- **Language/UI:** Kotlin, Jetpack Compose, Material 3. **Single activity** (`MainActivity`), no Fragments, no XML layouts.
- **Package root:** `com.sunrack.bluebase`
- **Suggested package layout:**
  ```
  com.sunrack.bluebase
  ├── BluebaseApp.kt              // Application class
  ├── MainActivity.kt
  ├── core/
  │   ├── network/                // Retrofit, OkHttp, AuthInterceptor, TokenAuthenticator, ApiResult
  │   ├── auth/                   // SessionManager / TokenStore (encrypted), JWT exp decode
  │   ├── storage/                // DataStore prefs
  │   └── util/                   // date formatting, status colors, image compression, files
  ├── data/
  │   ├── model/                  // @Serializable DTOs mirroring backend JSON
  │   ├── api/                    // Retrofit interfaces per feature (AuthApi, OrdersApi, WarrantyApi, AdminApi, ...)
  │   └── repository/
  ├── ui/
  │   ├── theme/                  // Color.kt, Theme.kt, Type.kt (exists)
  │   ├── components/             // shared composables (ports of RN components/)
  │   ├── navigation/             // NavHost, routes, drawers per role
  │   └── feature/
  │       ├── auth/  (login, register, forgotpassword)
  │       ├── client/ (dashboard, scanner, orders, kits, warranty, notifications, about, manual)
  │       ├── admin/
  │       └── developer/
  ```
- **Pattern:** MVVM with one `ViewModel` per screen exposing `StateFlow<UiState>`, repositories returning `Result`/sealed types, and coroutines.
- **Recommended libraries** (add to `gradle/libs.versions.toml`, checking AGP compatibility, see §8):

  | Need | Library | Replaces (RN) |
  |---|---|---|
  | HTTP | Retrofit + OkHttp (+ logging-interceptor in debug) | axios |
  | JSON | kotlinx.serialization (+ Retrofit converter) | JSON |
  | DI | Hilt (or manual DI via Application if you prefer simpler) | React context |
  | Token storage | DataStore + Tink, or `EncryptedSharedPreferences` | expo-secure-store |
  | Images | Coil 3 (`coil-compose`, `coil-network-okhttp`) | expo-image |
  | QR scanning | CameraX + ML Kit Barcode Scanning | expo-camera |
  | Photo capture/pick | `ActivityResultContracts.TakePicture` + Photo Picker (`PickVisualMedia`) | expo-image-picker |
  | Video playback | Media3 ExoPlayer + `media3-ui` | expo-av |
  | Location | play-services-location (FusedLocationProviderClient) | expo-location |
  | Save to Downloads | MediaStore API | expo-media-library |
  | Share / open files | FileProvider + `Intent.ACTION_SEND` | expo-sharing |
  | Document pick | `ActivityResultContracts.OpenDocument` | expo-document-picker |
  | PDF | `android.graphics.pdf.PdfDocument`, or WebView + `PrintManager` for the HTML template | expo-print |
  | Connectivity | `ConnectivityManager.NetworkCallback` | NetInfo |
  | Notifications | `NotificationCompat` + channel + `POST_NOTIFICATIONS` permission (API 33+) | expo-notifications |
  | Navigation | Navigation Compose (type-safe routes with `@Serializable`) + `ModalNavigationDrawer` | expo-router + drawer |

- **Can't be ported:** `expo-updates` (over-the-air JS updates). Native releases go through the Play Store, so consider the Play **In-App Updates** API.

### UI / theme
- Light theme only. Brand yellow `#FAD90E` (primary), `#FACC15`, `#E5C100`; black drawer; header `#FACC15`; nav bar `#0A0A0A`. The full palette is in `<RN>/utils/theme.ts`, and part of it is already ported in `ui/theme/Color.kt`.
- The RN app uses NativeWind (Tailwind classes). Translate the visual design; don't try to reproduce Tailwind.
- App icon and splash: `<RN>/assets/images/app_icon.png`. Use the Android Studio Image Asset tool to generate mipmaps, and the SplashScreen API (`core-splashscreen`).

### Permissions needed (manifest)
`INTERNET`, `ACCESS_NETWORK_STATE`, `CAMERA`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, and optionally `RECORD_AUDIO` if video capture is re-enabled.
**Don't** request `READ/WRITE_EXTERNAL_STORAGE`. Use the Photo Picker and MediaStore instead (minSdk 24, so add `WRITE_EXTERNAL_STORAGE` with `maxSdkVersion="28"` only if saving to Downloads on API ≤ 28).

---

## 7. Current state of this repo

Done:
- [x] Project created (AGP 8.13.2, Gradle 8.14.5, compileSdk/targetSdk 36, minSdk 24, Java/JVM target 11)
- [x] Converted from Java/XML to **Kotlin 2.4.20 + Jetpack Compose** (Compose BOM 2026.06.01, i.e. Compose 1.11.x, Material 3, navigation-compose 2.9.8, lifecycle 2.10.0, core-ktx 1.18.0)
- [x] `ui/theme` with brand colors; `MainActivity` shows a placeholder "Bluebase Warranty" screen
- [x] `.gitignore` hardened (keystores, APK/AAB, IDE files, `CLAUDE.local.md`)
- [x] `./gradlew assembleDebug testDebugUnitTest` passes
- [x] **Phase 1 (Foundation):** Retrofit + OkHttp + kotlinx.serialization + Coil3 + DataStore + `androidx.security-crypto` added to `gradle/libs.versions.toml`. `BuildConfig.BASE_URL` / `DOC_BASE_URL`. `TokenStore` (EncryptedSharedPreferences, mirrors the `access`/`refresh`/`user` SecureStore keys). `SessionManager` (`StateFlow<User?>`, login/refresh/logout, proactive refresh 10s before JWT `exp`, mutex-coalesced refresh). `AuthInterceptor` + `TokenAuthenticator` (refresh-once-on-401 via a second, unauthenticated Retrofit client used only for `/token/*`, avoiding a circular dependency). `BluebaseApp` + a small hand-rolled `AppContainer` for DI — **Hilt was skipped** in favor of manual DI per §6's "or manual DI" option, to avoid pinning a KSP version alongside the already-finicky AGP/Kotlin/Compose versions (§8). Note: `com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter` failed to resolve on the Kotlin compile classpath (unresolved reference despite Gradle showing it resolved) — replaced with Retrofit's own official `com.squareup.retrofit2:converter-kotlinx-serialization` (same version train as Retrofit itself), same package/API.
- [x] **Phase 2 (Auth screens):** Login, Register (send-otp → register with otp), Forgot Password (send-otp → verify-otp → reset-password) — ViewModels + Compose screens under `ui/feature/auth/`. `ApiError.kt` parses DRF `{"error"}`/`{"detail"}`/field-error bodies. `UserRole.kt` ports the hidden-developer detection (`username`/`email` allowlist) and the account_type→section routing from `login.tsx`. Minimal type-safe Navigation Compose (`ui/navigation/Routes.kt` + `BluebaseNavHost.kt`) reacts to `SessionManager.user`/`loading` for the splash + role-based redirect (`index.tsx`); the Client/Admin/Developer "home" destinations are placeholders until Phase 3 builds the real drawers. Icons need `androidx.compose.material:material-icons-extended` (not `-core`, which lacks `Visibility`/`VisibilityOff`).
  - **Real-world fix**: `User.is_active` is sometimes absent from the backend's `/token/` response (confirmed in production logs) — given a default (`= true`) instead of being required, since kotlinx.serialization fails deserialization on a missing non-optional field where TS silently tolerates `undefined`.
  - **Ported RN quirk (flagged, not fixed)**: `SessionManager.login()` always fails with the literal message `"Login failed"` on any error, discarding the real backend reason — matches `AuthContext.tsx`'s `login()`, which does the same. Revisit if the owner wants better UX here.
  - Debug builds currently log full request/response bodies (`HttpLoggingInterceptor.Level.BODY` in `NetworkModule.kt`) — was BASIC, bumped for debugging, consider dialing back once auth flows are stable.
- [x] **Phase 3 (Navigation shell):** `ClientShell`/`AdminShell` (`ui/feature/{client,admin}/`) port `CustomDrawer.tsx`/`CustomAdminDrawer.tsx` — a `ModalNavigationDrawer` with the profile header, menu items, and logout, each owning its own nested type-safe NavHost (`ClientRoutes.kt`/`AdminRoutes.kt`). `DeveloperShell` (`ui/feature/developer/`) is a plain Scaffold + NavHost with a red top bar, no drawer, matching the RN `(developer)` Stack; `DeveloperDashboardScreen` is a full port of `(developer)/index.tsx` (action cards, warning banner, logout confirmation). `BluebaseTopBar` (`ui/components/`) ports `CustomMainHeader`: hamburger + title + notification bell with an unread-count dot, refreshed via `NotificationBellViewModel`/`NotificationsApi.unreadCount()` on first composition and `ON_RESUME` (mirrors `useNotificationBadge`'s `refetchOnWindowFocus`; no fixed polling interval, matching the RN app). All destination *contents* other than the Developer Dashboard are `PlaceholderScreen`s — Phases 4+ replace them one at a time.
  - Root `Routes.kt`'s `ClientHomeRoute`/`AdminHomeRoute`/`DeveloperHomeRoute` were renamed to `*SectionRoute` now that each renders a full shell rather than a placeholder.
  - Gotcha for later phases: `NavDestination.hierarchy` and `NavDestination.hasRoute(KClass<*>)` are companion-object extensions, not top-level or member functions — each needs its own explicit `import androidx.navigation.NavDestination.Companion.hierarchy` / `.hasRoute`, or you'll get "unresolved reference" even though the class itself is in scope. Same for `androidx.lifecycle.viewmodel.viewModelFactory` + its `initializer { }` builder — both need their own imports.
- [x] **Phase 4 (Client core):** Dashboard home (`ui/feature/client/dashboard/`, `/warranty-dashboard-counts/`), All Orders (`ui/feature/client/orders/`, `/orders/`), Order Details (`/orders/{id}/`, groups items by kit like `order-details.tsx`), Product Info (`ui/feature/client/productinfo/`, `/kits/`, grouped by tilt angle + region client-side), Kit Details (`ui/feature/client/kitdetails/`, `/kit/{kit_id}/` or `/kit-scan-details/{scan_id}/` + a cross-reference against `/warranty-claims-status/` for the "already claimed" state). New routes carry real params now (`ClientOrderDetailsRoute(orderId)`, `ClientKitDetailsRoute(kitId?, scanId?, allScanned, totalKits?)`) via Navigation Compose's `toRoute<T>()`; their ViewModels are built with an inline `viewModelFactory { initializer { ... } }` per destination (not the shared `AppContainer.viewModelFactory()`, which only fits parameterless ViewModels).
  - **Deliberately skipped**: `dashboard/my-orders.tsx` — it's registered as a Drawer.Screen in the RN app but nothing in the reachable UI (drawer menu, home screen) navigates to it; flagged rather than ported speculatively. Revisit if the owner says otherwise.
  - **Still placeholders reachable from here**: QR Scanner (Phase 5), Warranty status/claim-form/status-page (Phases 6–7) — `ClientKitDetailsScreen`'s claim buttons already navigate to their future routes (`ClientClaimFormRoute`, `ClientWarrantyStatusPageRoute(warReqId)`), so Phase 7 only needs to replace the destination content.
  - **Real-world fix**: this backend serializes Django `DecimalField`s (`clearance`, `price`) as JSON *strings* even though numeric, confirmed from live `/orders/` responses — `KitInfo.clearance`/`ProductKit.clearance` are `String?`, not `Double?`. Also `OrderItem.id` is a plain JSON integer, not a string. kotlinx.serialization has no silent JS-style string↔number coercion, so a wrong guess here throws instead of silently working like it would in the RN app.
- [x] **Phase 5 (QR scanner):** `ui/feature/client/scanner/` — `QrCodeAnalyzer` (CameraX `ImageAnalysis.Analyzer` wrapping ML Kit's bundled `barcode-scanning`, QR-only per `qr-scanner.tsx`), `QrScannerViewModel` (parses the 5-field QR JSON, `POST /save-order/`, routes to `ClientKitDetailsRoute` by `scan_id`/`all_scanned`/`kit_id` exactly like the RN screen, 900ms centering delay preserved), `QrScannerScreen` (`LifecycleCameraController` + `PreviewView` via `AndroidView`, Canvas-drawn yellow scan-box corners, torch toggle, gallery upload via the Photo Picker → `POST /upload-qr/` multipart). Camera permission requested at runtime; `CAMERA` + camera hardware `<uses-feature>` added to the manifest.
  - Same "always generic message on non-HTTP failures" RN quirk as login is preserved here (`apiErrorMessage(fallback)` returns the fallback for anything that isn't an `HttpException`), since `qr-scanner.tsx`'s catch block does the same (only shows the backend's `error` field for real Axios errors, otherwise always "Invalid QR code or server error.").
  - Minor gotcha: `androidx.compose.ui.platform.LocalLifecycleOwner` is deprecated on this Compose BOM in favor of `androidx.lifecycle.compose.LocalLifecycleOwner` (already available via `lifecycle-runtime-compose`, no new dependency needed).
- [x] **Phase 6 (My scans + warranty status):** `ui/feature/client/dashboard/MyScansScreen.kt` (project→kit accordion of `/saved-orders/`, cross-referenced against `/warranty-claims-status/` for "already claimed"), `ui/feature/client/warranty/` — `ClaimStatusScreen` (same accordion pattern over claims, View Details/View Card), `WarrantyStatusPageScreen` (full claim detail incl. checklist + PDF link), `MyWarrantyCardsScreen` + `WarrantyCardDetailScreen` sharing `ui/components/WarrantyCardView.kt` (the certificate-styled card, ports `components/WarrantyCard.tsx`).
  - **Important correction to Phase 4**: `CustomDrawer.tsx`'s "Home" (`dashboard/index.tsx`) and "Warranty" (`warranty/index.tsx`) drawer items are each a **Material Top Tabs pair**, not a single screen — Phase 4 only built the Home tab's content. Fixed now: `ClientDashboardRoute` renders `DashboardTabsScreen` (Home + My Scans) and `ClientWarrantyRoute` renders `WarrantyTabsScreen` (Warranty Status + My Warranty Cards), via a shared `ui/components/TwoTabScaffold.kt`. Worth double-checking any earlier-ported RN screen list for other tab navigators hiding behind what looked like a single Stack/Drawer screen.
  - **Real-world fix**: `core/util/DateFormat.kt`'s `formatDateTime`/`formatDateOnly` previously used a fixed set of `SimpleDateFormat` patterns that didn't match this backend's actual date format (`"2025-11-15T09:33:24.989899+05:30"` — 6-digit fractional seconds + numeric UTC offset, confirmed from the live `/orders/` response in Phase 4/5), so they were silently returning "Invalid Date" everywhere. Replaced with a regex that extracts y/M/d/H/m/s directly (timezone offset intentionally dropped — rendered as wall-clock numbers, matching what `toDateString()`/`toLocaleString()` effectively show in the RN app). This fixes date display retroactively across every screen already using these helpers (Order Details' dates, etc.) — no java.time available below API 26 without desugaring, which isn't configured, hence the manual regex approach instead.
  - `installation_latitude`/`installation_longitude` on `WarrantyCardDetail` are `String?`, following the same Django-`DecimalField`-as-JSON-string pattern as `clearance`/`price` — RN's own `WarrantyCard.tsx` defensively handles both string and number for this exact field, which was the tell.
  - `ClientClaimFormRoute` stays a placeholder (Phase 7 builds it); the "Request Warranty" paths from My Scans, Kit Details, and Claim Status all already navigate there.

Nothing else is implemented yet.

---

## 8. Build notes and gotchas

- Build: `./gradlew assembleDebug` · Unit tests: `./gradlew testDebugUnitTest` · Install: `./gradlew installDebug` (Windows: `gradlew.bat`)
- `local.properties` (SDK path) is git-ignored. Android Studio recreates it on first open, or create it by hand with `sdk.dir=...`.
- **Version pinning:** the newest AndroidX releases (Compose 1.12+, core 1.19+, lifecycle 2.11+, navigation 2.10+) **require AGP 9.1+ and compileSdk 37**. This project is on AGP 8.13 because that is what the installed Android Studio supports. When you add a library, pick a version compatible with AGP 8.13 / compileSdk 36. If the build says "requires Android Gradle plugin 9.1.0", downgrade that library, or upgrade AGP deliberately (AGP 9 has built-in Kotlin, so the `kotlin-android` plugin setup changes).
- Kotlin sources live under `app/src/main/java/` (standard for Android Studio templates).
- **Play Store:** if this app should *replace* the existing RN app for current users, the `applicationId` must become `com.abujawed11.reactnativeqrscanner` **and** it must be signed with the **same upload key**. Otherwise it's a separate listing. **This isn't decided yet; ask the owner before release.**
- Never commit keystores or secrets (already ignored).

---

## 9. Implementation roadmap

Work in this order. Each phase should build, run and be committed before moving on.

1. **Foundation:** add Retrofit/OkHttp/serialization/Hilt/Coil/DataStore; `BluebaseApp`; `BuildConfig.BASE_URL`; `TokenStore` (encrypted); `AuthInterceptor` + `TokenAuthenticator` (refresh once on 401, then global logout); `SessionManager` with a proactive refresh timer.
2. **Auth screens:** login, register (OTP), forgot password (send OTP, verify, reset). Role-based start destination.
3. **Navigation shell:** client drawer (port `CustomDrawer`), admin drawer (`CustomAdminDrawer`), developer stack. Top bar with the notification bell and unread badge.
4. **Client core:** dashboard home (counters), my orders, all orders, order details, product info, kit details.
5. **QR scanner:** CameraX + ML Kit, JSON parsing, `/save-order/`, gallery QR upload `/upload-qr/`, routing.
6. **My scans + warranty status:** lists, status detail, warranty cards list + card view.
7. **Claim flow:** claim form + checklist, then the 14-step media wizard (capture/pick, compress), location, declaration, multipart upload with progress.
8. **Notifications:** list, mark read, mark all read, delete, clear all, badge polling; local notification channel.
9. **Admin:** manage clients (CRUD), orders (paginated + filter/sort), order details + kits, manage kits, warranty dashboard, drill-downs.
10. **Admin claim review:** full-details screen (gallery, ExoPlayer, PDF export, save to Downloads), status update + warranty card form.
11. **Developer tools:** delete users/claims (with confirmations).
12. **Static screens:** About, Installation Manual, Profile/Settings/Support.
13. **Polish and release:** icon, splash, network-error handling and retry (RN retries 3× with exponential backoff and never retries 4xx), offline states, ProGuard/R8 rules, signing config, versioning, Play Store decision (§8).

---

## 10. Rules for AI agents working here

1. **The RN app is the spec.** For any screen, read the matching `<RN>` file(s) first, and match the fields, validation, API calls, navigation and error messages. If the RN behavior looks like a bug, point it out to the user rather than silently changing it.
2. **Find `<RN>` using §2**, and never hard-code a machine-specific path into committed files.
3. Don't change backend contracts, and don't invent endpoints. If something is unclear, read the RN code or ask.
4. Keep the build green: run `./gradlew assembleDebug` (and tests) after changes.
5. Match the existing code style: Kotlin official style, Compose best practices (state hoisting, `collectAsStateWithLifecycle`, previews for UI components).
6. Commit only when the user asks. Never commit secrets, keystores or `local.properties`.
7. Update **§7 "Current state"** and tick off roadmap phases in this file as work is completed, so the next session (on any machine) knows where things stand.
