import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

part 'app_database.g.dart';

class KeyRecords extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get name => text().withLength(min: 1, max: 100)();
  TextColumn get phoneNumber => text().withLength(min: 1, max: 15)();
  TextColumn get idNo => text().withLength(min: 1, max: 50)();
  TextColumn get vehicleNo => text().nullable().withLength(min: 0, max: 50)();
  TextColumn get keyNo => text().nullable().withLength(min: 0, max: 50)();
  TextColumn get keyType => text().nullable().withLength(min: 0, max: 50)();
  TextColumn get purpose => text().nullable().withLength(min: 0, max: 50)();
  DateTimeColumn get dateAdded => dateTime().nullable()();
  TextColumn get remarks => text().nullable().withLength(min: 0, max: 500)();
  IntColumn get quantity => integer().withDefault(const Constant(1))();
  RealColumn get amount => real().withDefault(const Constant(0.0))();
  TextColumn get imagePath => text().nullable().withLength(min: 0, max: 255)();
  DateTimeColumn get createdAt =>
      dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt => dateTime().withDefault(currentDateAndTime)();
}

@DriftDatabase(tables: [KeyRecords])
class AppDatabase extends _$AppDatabase {
  AppDatabase._internal(super.executor);

  static Future<AppDatabase> make() async {
    final directory = await getApplicationDocumentsDirectory();
    final file = File(p.join(directory.path, 'keybase', 'keybase.sqlite'));
    await file.parent.create(recursive: true);
    final executor = NativeDatabase.createInBackground(file);
    return AppDatabase._internal(executor);
  }

  @override
  int get schemaVersion => 1;
}
