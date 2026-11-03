# Hostel Maintenance Management System

A Kotlin project with a Jetpack Compose Android client and a Ktor REST backend. The backend is the only component that connects to MongoDB.

## Project layout

- `android/app`: Android client, Compose screens, networking, and authentication.
- `backend`: Ktor REST API, JWT/password authentication, MongoDB repositories, and tests.
- `.env.example`: environment-variable names only. The backend reads process environment variables; it does not load a `.env` file.

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

The database password in the shared screenshot must be rotated before use. In Atlas, create a replacement database user with only the required database access, add the development computer's current public IP to Network Access, and copy the cluster hostname. Do not use `0.0.0.0/0` for routine development.

The following keeps the newly rotated password out of command history and source control. Enter it in the secure credential prompt; the URI-encoded value is held only in this PowerShell process:

```powershell
$credential = Get-Credential -UserName "<new-atlas-database-user>"
$clusterHost = "<your-cluster-host>"
$encodedUser = [Uri]::EscapeDataString($credential.UserName)
$encodedPassword = [Uri]::EscapeDataString($credential.GetNetworkCredential().Password)
$env:MONGODB_URI = "mongodb+srv://${encodedUser}:${encodedPassword}@${clusterHost}/hostel_maintenance?retryWrites=true&w=majority&appName=HostelMaintenance"

$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$secretBytes = New-Object byte[] 48
$rng.GetBytes($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)

# Optional: create the first administrator. Use a unique password of at least 12 characters.
$env:BOOTSTRAP_ADMIN_EMAIL = "<administrator-email>"
$env:BOOTSTRAP_ADMIN_PASSWORD = "<strong-administrator-password>"

.\gradlew.bat :backend:run
```

The backend requires `MONGODB_URI` and a `JWT_SECRET` of at least 32 bytes. It creates unique user indexes during startup. The optional administrator is created once and can provision maintenance staff through the protected admin API. Atlas connectivity must be verified in your environment after setting the rotated credentials.

## Android app

The Android client currently supports resident registration/sign-in against the API, encrypted local token storage, resident navigation, and a validated report form with gallery image selection. The report form clearly reports that submission is not yet connected. Maintenance request processing, staff/admin screens, and notifications are not yet implemented.

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
