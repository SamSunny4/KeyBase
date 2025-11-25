# KeyBase Flutter Port

This folder contains the Flutter application that will deliver KeyBase functionality on Android.

## Getting Started
1. Install Flutter 3.24+ and run `flutter doctor`.
2. From the repository root: `cd flutter_app` then run `flutter pub get`.
3. (Optional) Generate native platform folders if missing by running `flutter create .`.
4. Launch the Android emulator or connect a device, then run `flutter run`.

## Architecture Overview
- **State management**: `flutter_bloc` for screens that mirror the Swing forms.
- **Navigation**: `go_router` with typed routes.
- **Dependency injection**: `get_it` to expose repositories and services.
- **Theming**: `ThemeData` defined in `lib/src/core/theme/app_theme.dart` to keep parity with desktop branding.
- **Modules**: Each feature (records, search, exports, licensing, settings, analytics) receives its own folder under `lib/src/features`.

## Database Choice
The mobile client uses **Drift** on top of `sqlite3_flutter_libs`, giving:
- Full SQLite reliability on Android with WAL support for concurrent reads.
- Compile-time safe queries that keep business rules consistent with the existing H2 schema.
- Easy migration scripts so we can evolve the schema to match `config/init_h2_database.sql`.

This offline-first database mirrors the current `duplicator` table and provides Room-like ergonomics while staying pure Dart. Server synchronization can be added later without changing the UI layer.

## Next Steps
- Model the `duplicator` table in `AppDatabase`.
- Flesh out repositories/use cases for CRUD, search, metrics, exports.
- Implement UI flows (add/save, search, stats dashboards, licensing) feature by feature.
