# BLS AutoBooking App

This is an Android mobile application designed to automate the booking of visa appointments from the BLS Spain Visa Application Center in Algeria. It features automated monitoring, CAPTCHA solving, and appointment booking capabilities.

## Project Structure

```
BLSAutoBookingApp/
├── app/                         # Android application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml  # Application manifest
│   │   │   ├── java/                # Kotlin/Java source code
│   │   │   │   └── com/blsautobooking/app/
│   │   │   │       ├── data/            # Data layer (repositories, data sources, models)
│   │   │   │       │   ├── database/    # Room database and DAOs
│   │   │   │       │   └── model/       # Data models (Applicant, AppointmentSlot, etc.)
│   │   │   │       ├── network/         # Network operations (BLSWebClient, CaptchaSolver, EmailService)
│   │   │   │       ├── ui/              # UI layer (Activities, ViewModels)
│   │   │   │       │   ├── login/       # Login related activities and viewmodels
│   │   │   │       │   └── main/        # Main activity and dashboard
│   │   │   │       ├── utils/           # Utility classes (NotificationHelper, SecurityHelper, StealthAutomation, WebViewHelper)
│   │   │   │       └── workers/         # WorkManager workers (AppointmentMonitorWorker)
│   │   │   └── res/                 # Android resources (layouts, values, drawables)
│   │   │       ├── drawable/
│   │   │       ├── layout/
│   │   │       ├── values/
│   │   │       └── mipmap/
│   ├── build.gradle             # App-level Gradle build file
│   └── proguard-rules.pro       # ProGuard rules for release builds
├── build.gradle                 # Project-level Gradle build file
├── settings.gradle              # Gradle settings file
└── README.md                    # This documentation file
```

## Features Implemented

- **Initial Android Project Setup**: Basic project structure, Gradle files, and `AndroidManifest.xml`.
- **Login and Session Management**: `LoginActivity`, `LoginViewModel`, `LoginRepository`, `LoginDataSource`, and `BLSWebClient` for simulating web login.
- **Appointment Slot Monitoring**: `MainActivity` to start/stop monitoring, `AppointmentMonitorWorker` for background checks, and `AppointmentService` to simulate slot checking.
- **CAPTCHA Solving Integration**: `CaptchaSolver` with a placeholder for API integration and `CaptchaResult` model.
- **Auto-Booking Engine**: `BookingEngine` to simulate the booking process, including visa type selection, CAPTCHA handling, and form submission.
- **Email Notification Functionality**: `NotificationHelper` for local notifications and `EmailService` for sending emails via EmailJS.
- **Stealth Automation**: `StealthAutomation` utility for human-like delays, typing, and bot detection bypass.
- **Data Management**: Room database setup with `AppDatabase` and `ApplicantDao` for managing applicant data.
- **Security**: `SecurityHelper` for secure storage of credentials using EncryptedSharedPreferences.

## Setup and Build Instructions

To set up and build this Android application, you will need Android Studio installed on your machine. This project uses Kotlin and Gradle.

### Prerequisites

- **Android Studio**: Download and install the latest version from [developer.android.com](https://developer.android.com/studio).
- **Java Development Kit (JDK)**: Android Studio typically bundles a suitable JDK. If not, ensure you have JDK 11 or 17 installed.
- **Internet Connection**: Required for Gradle to download dependencies.

### Steps to Build and Run

1.  **Clone the Repository (or extract the project files)**:
    If you received this as a zip file, extract it to your desired location.

2.  **Open Project in Android Studio**:
    -   Launch Android Studio.
    -   Click on `Open an existing Android Studio project`.
    -   Navigate to the `BLSAutoBookingApp` directory (the root of this project) and click `OK`.

3.  **Gradle Sync**: 
    Android Studio will automatically start syncing the project with Gradle. This may take some time as it downloads all necessary dependencies. Ensure your internet connection is stable.
    If the sync fails, try `File > Sync Project with Gradle Files`.

4.  **Configure `local.properties` (if necessary)**:
    Sometimes, Android Studio might not automatically detect your Android SDK location. If you encounter issues, create a `local.properties` file in the root of the `BLSAutoBookingApp` directory with the following content, replacing `/path/to/your/android/sdk` with your actual SDK path:
    ```properties
    sdk.dir=/path/to/your/android/sdk
    ```

5.  **Build the Application**:
    -   Go to `Build > Make Project` or click the hammer icon in the toolbar.
    -   This will compile the application and generate an APK file.

6.  **Run on an Emulator or Device**:
    -   Connect an Android device to your computer with USB debugging enabled, or set up an Android Emulator in Android Studio.
    -   Select your target device/emulator from the dropdown in the toolbar.
    -   Click the `Run` button (green triangle icon) in the toolbar.
    -   The app will be installed and launched on your selected device/emulator.

## Important Notes

-   **CAPTCHA API**: The `CaptchaSolver.kt` file contains a placeholder for the CAPTCHA API base URL (`http://your-captcha-api-base-url/`). You **must** replace this with the actual API endpoint for your CAPTCHA solving service.
-   **EmailJS Configuration**: The `EmailService.kt` uses hardcoded EmailJS `serviceId`, `templateId`, and `publicKey`. While `SecurityHelper` is provided for secure storage, for initial testing, these values are directly in the code. For production, consider loading these securely.
-   **Simulated Web Interactions**: The `BLSWebClient` and `BookingEngine` contain simulated web interactions. In a real deployment, these would interact with a WebView instance to perform actual DOM manipulation and navigation. The `WebViewHelper` provides the necessary abstractions for this.
-   **User Credentials**: The `LoginDataSource` currently simulates login success. In a real application, it would authenticate against the BLS website. The `SecurityHelper` can be used to securely store user credentials.
-   **Testing**: Due to the nature of web automation and external services (BLS website, CAPTCHA API, EmailJS), thorough testing on a live environment is crucial.

This project provides a solid foundation for the BLS AutoBooking App. Further development would involve refining the web interaction logic, integrating with actual APIs, and extensive testing.

