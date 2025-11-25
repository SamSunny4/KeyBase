import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../data/repositories/key_record_repository.dart';
import '../../domain/entities/duplicator_record.dart';

class RecordsPage extends StatelessWidget {
  const RecordsPage({super.key, required this.repository});

  final KeyRecordRepository repository;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('KeyBase Records')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/records/new'),
        icon: const Icon(Icons.add),
        label: const Text('Add Record'),
      ),
      body: StreamBuilder<List<DuplicatorRecord>>(
        stream: repository.watchAll(),
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }

          final records = snapshot.data ?? const <DuplicatorRecord>[];
          if (records.isEmpty) {
            return const _EmptyState();
          }

          return ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            itemCount: records.length,
            separatorBuilder: (_, __) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final record = records[index];
              return _RecordTile(record: record);
            },
          );
        },
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.key, size: 64),
          SizedBox(height: 16),
          Text('No records yet. Tap "Add Record" to get started.'),
        ],
      ),
    );
  }
}

class _RecordTile extends StatelessWidget {
  const _RecordTile({required this.record});

  final DuplicatorRecord record;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        title: Text(record.name),
        subtitle: Text('${record.phoneNumber} · ${record.idNo}'),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text('Qty: ${record.quantity}'),
            Text('₹${record.amount.toStringAsFixed(2)}'),
          ],
        ),
      ),
    );
  }
}
