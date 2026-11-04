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

The default `http://10.0.2.2:8080/` address is for the Android emulator only. A physical phone must use the development PC's LAN IP. Set it in the ignored `local.properties` file (preserve its existing `sdk.dir` line):

```powershell
$pcIp = Get-NetIPAddress -AddressFamily IPv4 -InterfaceAlias "Wi-Fi" |
    Where-Object { $_.IPAddress -notmatch '^(127|169\.254)\.' } |
    Select-Object -First 1 -ExpandProperty IPAddress
$properties = Get-Content .\local.properties | Where-Object { $_ -notmatch '^api\.baseUrl=' }
$properties += "api.baseUrl=http://${pcIp}:8080/"
Set-Content .\local.properties $properties
.\gradlew.bat :android:app:printApiBaseUrl
.\gradlew.bat :android:app:assembleDebug
```

Restarting the laptop stops the backend; it must be started again before the phone can connect. After reboot, run the backend startup commands above in a PowerShell window, then confirm `http://<PC-LAN-IP>:8080/health` returns `{"status":"ok"}` from both the PC and the phone's browser. Those commands prompt for the database password again because the credentials are held only in that PowerShell process. The backend must finish its MongoDB Atlas initialization before it starts listening. Keep the PC and phone on the same Wi-Fi. If the laptop receives a different Wi-Fi IP after reboot, rerun the Android URL/build commands above and reinstall the APK on the phone; the API address is embedded in the app at build time. Reserving the laptop's IP in the router's DHCP settings avoids that rebuild after future restarts. If the PC can reach `/health` but the phone cannot, allow inbound TCP port 8080 on the PC's **Private** Windows Firewall profile:

```powershell
# Run in an elevated PowerShell window; do not disable the firewall.
New-NetFirewallRule -DisplayName "Hostel Maintenance API (Private)" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
```

The app's sign-in and report error messages show the API URL embedded in that build and remind physical-phone users that `10.0.2.2` is emulator-only. Use `-PapiBaseUrl=http://<PC-LAN-IP>:8080/` to override the local setting for one build. Debug builds allow local HTTP; production connections must use HTTPS.

## Tests and debug APK

```powershell
.\gradlew.bat :backend:test :android:app:assembleDebug
```

On a Windows checkout inside OneDrive, Gradle writes build outputs outside the synchronized folder to avoid OneDrive locking generated files. The APK is at `%LOCALAPPDATA%\HostelMaintenance\gradle-build\_android_app\outputs\apk\debug\app-debug.apk`. Other checkouts use `android/app/build/outputs/apk/debug/app-debug.apk`. Install it on an authorized connected phone with Android platform tools:

```powershell
adb install -r android\app\build\outputs\apk\debug\app-debug.apk
```

Keep real database credentials and JWT secrets out of `.env.example`, source files, screenshots, and commits.
