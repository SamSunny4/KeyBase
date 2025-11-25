import 'package:go_router/go_router.dart';
import '../../features/records/data/repositories/key_record_repository.dart';
import '../../features/records/presentation/pages/add_record_page.dart';
import '../../features/records/presentation/pages/records_page.dart';
import '../database/app_database.dart';

class AppRouter {
  AppRouter({required AppDatabase database})
      : config = _buildRouter(database);

  final GoRouter config;

  static GoRouter _buildRouter(AppDatabase database) {
    final repository = KeyRecordRepository(database);
    return GoRouter(
      routes: <GoRoute>[
        GoRoute(
          path: '/',
          name: 'records',
          builder: (context, state) => RecordsPage(repository: repository),
        ),
        GoRoute(
          path: '/records/new',
          name: 'add-record',
          builder: (context, state) => AddRecordPage(repository: repository),
        ),
      ],
    );
  }
}
