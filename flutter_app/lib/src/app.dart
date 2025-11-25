import 'package:flutter/material.dart';
import 'core/routing/app_router.dart';
import 'core/theme/app_theme.dart';
import 'core/database/app_database.dart';

class KeyBaseApp extends StatefulWidget {
  const KeyBaseApp({super.key, required this.database});

  final AppDatabase database;

  @override
  State<KeyBaseApp> createState() => _KeyBaseAppState();
}

class _KeyBaseAppState extends State<KeyBaseApp> {
  late final AppRouter _router;

  @override
  void initState() {
    super.initState();
    _router = AppRouter(database: widget.database);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      routerConfig: _router.config,
      title: 'KeyBase Mobile',
      theme: buildLightTheme(),
      debugShowCheckedModeBanner: false,
    );
  }
}
