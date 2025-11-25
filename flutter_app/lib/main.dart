import 'package:flutter/material.dart';
import 'src/app.dart';
import 'src/core/database/app_database.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final database = await AppDatabase.make();
  runApp(KeyBaseApp(database: database));
}
