# RailSpacing

RailSpacing is an Android application built with Jetpack Compose that helps calculate and optimize the placement and spacing of rails (like balcony rails, balusters, deck rails, or fence pickets) to ensure compliance with structural and safety limits.

## Features

- **Max Gap Calculation**: Calculate the required number of rails and exact spacing based on maximum allowable gap constraints (e.g., building codes).
- **Fixed Rail Count Layout**: Calculate the clear gaps and positions when a specific number of rails is required.
- **Detailed Measurements**: Get precise measurements including center-to-center spacing, clear gaps, and outer edge / center positions for every single rail.
- **Calculation History**: Keep track of previous calculations for quick reference.
- **Dynamic Theming**: Supports Material 3 dynamic color and dark/light modes.
- **Customizable Offsets**: Set start and end offsets to accommodate end posts or margins.

## Tech Stack

- [Kotlin](https://kotlinlang.org/) - 100% Kotlin
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Declarative UI framework
- [Material 3](https://m3.material.io/) - Modern design system components
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - UI state management
- [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) - Local settings storage
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) - App routing and screen navigation

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/RailSpacing.git
   ```
2. Open the project in **Android Studio**.
3. Build and run the app on an emulator or physical device running Android 7.0 (API 24) or higher.
