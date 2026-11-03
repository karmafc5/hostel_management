# Hostel Maintenance Management System

A Kotlin project with a Jetpack Compose Android client and a Ktor REST backend. The backend is the only component that connects to MongoDB.

## Project layout

- `android/app/src/main/java/com/hostelmaintenance/app`: Android application setup and `MainActivity` entry point.
- `android/app/src/main/java/com/hostelmaintenance/di`: app dependency wiring and ViewModel factories.
- `android/app/src/main/java/com/hostelmaintenance/data/local`: encrypted on-device token storage.
- `android/app/src/main/java/com/hostelmaintenance/data/remote`: Retrofit API and authorization interceptor.
- `android/app/src/main/java/com/hostelmaintenance/data/auth` and `data/report`: authentication and report DTOs/repositories.
- `android/app/src/main/java/com/hostelmaintenance/presentation`: Compose screens, ViewModels, and theme.
- `backend/src/main/kotlin/com/hostelmaintenance`: Ktor REST API, authentication, and report features.
- `backend/src/main/kotlin/com/hostelmaintenance/database`: MongoDB repositories and database access. The Android app talks to MongoDB through the backend API.
- `backend/src/test/kotlin`: backend tests.
- `**/build`: generated Gradle output, ignored by Git; source stays under each module's `src` directory.
- `.env.example`: environment-variable names only. The backend reads process environment variables; it does not load a `.env` file.

## Report data path

The report code is separated by responsibility:

1. Android form: `android/app/src/main/java/com/hostelmaintenance/presentation/report/ReportProblemScreen.kt`.
2. Android state and network access: `presentation/report/ReportsViewModel.kt` → `data/report/ReportRepository.kt` → `data/remote/MaintenanceReportApi.kt`.
3. Backend endpoints and validation: `backend/src/main/kotlin/com/hostelmaintenance/reports/MaintenanceReportRoutes.kt` and `MaintenanceReport.kt`.
4. MongoDB writes and reads: `backend/src/main/kotlin/com/hostelmaintenance/database/MongoMaintenanceReportRepository.kt`, using the `maintenanceReports` collection.

`POST /reports` inserts a resident-owned report; `GET /reports/me` returns that resident's saved reports newest first. The Android client sends both requests through the same authenticated Retrofit client. MongoDB credentials stay on the backend; configure `MONGODB_URI` and `JWT_SECRET` before starting it.

## Prerequisites

- JDK 17 or newer.
- Android SDK Platform 35 and Android SDK Build-Tools.
- A MongoDB Atlas cluster and database user.

Point Gradle to the Android SDK in PowerShell:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

The checked-in Gradle wrapper downloads the required Gradle version.

## Configure MongoDB Atlas and start the backend

The database password shown in the shared screenshot has been rotated. The app uses a dedicated database user limited to `readWrite` access on `hostel_maintenance`. In Atlas, add the development computer's current public IP to Network Access. Do not use `0.0.0.0/0` for routine development.

The following keeps the newly rotated password out of command history and source control. Enter it in the secure credential prompt; the URI-encoded value is held only in this PowerShell process:

```powershell
$credential = Get-Credential -UserName "deere7653_db_user"
$clusterHost = "cluster0.t871kb0.mongodb.net"
$encodedUser = [Uri]::EscapeDataString($credential.UserName)
$encodedPassword = [Uri]::EscapeDataString($credential.GetNetworkCredential().Password)
$env:MONGODB_URI = "mongodb+srv://${encodedUser}:${encodedPassword}@${clusterHost}/hostel_maintenance?retryWrites=true&w=majority&appName=HostelMaintenance"

$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$secretBytes = New-Object byte[] 48
$rng.GetBytes($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)

# Optional: set BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD from secure local values.
# Do not put real administrator credentials in this file or source control.

.\gradlew.bat :backend:run
```

The backend requires `MONGODB_URI` and a `JWT_SECRET` of at least 32 bytes. It creates unique user indexes during startup. The optional administrator is created once and can provision maintenance staff through the protected admin API. Atlas connectivity must be verified in your environment after setting the rotated credentials.

Residents can submit maintenance reports with a title, category, description, hostel, room, and priority. The authenticated API stores each report in the `maintenanceReports` collection, ties it to the signed-in resident, and returns the resident's saved reports in newest-first order. The phone's **My Reports** tab reads those records from the backend.

To load sample reports for an existing resident account, set `MOCK_DATA_STUDENT_EMAIL` to that resident's email before starting the backend. On startup, the backend adds three clearly marked sample reports only when that resident has no reports yet. It does not create demo accounts or use built-in demo passwords.

## Android app

The Android client supports resident registration/sign-in against the API, encrypted local token storage, resident navigation, and report submission/history backed by MongoDB through the API. The report form validates its fields on the phone and the server; it shows a save error if the request fails. Image attachments, staff/admin screens, and notifications are not implemented.

The emulator uses `http://10.0.2.2:8080/` by default. To connect a physical phone on the same local network, build with the development computer's LAN IP:

```powershell
.\gradlew.bat -PapiBaseUrl=http://192.168.1.20:8080/ :android:app:assembleDebug
```

Replace `192.168.1.20` with the computer's actual LAN IP. Debug builds allow local HTTP; production connections must use HTTPS.

## Tests and debug APK

```powershell
.\gradlew.bat :backend:test :android:app:assembleDebug
```

The APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected phone with Android platform tools:

```powershell
adb install -r android\app\build\outputs\apk\debug\app-debug.apk
```

Keep real database credentials and JWT secrets out of `.env.example`, source files, screenshots, and commits.
