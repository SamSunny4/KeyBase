import '../../../../core/database/app_database.dart';
import '../../domain/entities/duplicator_record.dart';

class KeyRecordRepository {
  KeyRecordRepository(this._database);

  final AppDatabase _database;

  Future<int> create(KeyRecordsCompanion companion) =>
      _database.into(_database.keyRecords).insert(companion);

    Stream<List<DuplicatorRecord>> watchAll() => _database
        .select(_database.keyRecords)
        .watch()
        .map(
          (rows) =>
              rows.map<DuplicatorRecord>(DuplicatorRecord.fromData).toList(),
        );
}
