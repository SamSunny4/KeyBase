import 'package:drift/drift.dart' as drift;
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/database/app_database.dart';
import '../../data/repositories/key_record_repository.dart';

class AddRecordPage extends StatefulWidget {
  const AddRecordPage({super.key, required this.repository});

  final KeyRecordRepository repository;

  @override
  State<AddRecordPage> createState() => _AddRecordPageState();
}

class _AddRecordPageState extends State<AddRecordPage> {
  final _formKey = GlobalKey<FormState>();

  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _idController = TextEditingController();
  final _vehicleNoController = TextEditingController();
  final _keyNoController = TextEditingController();
  final _keyTypeController = TextEditingController();
  final _remarksController = TextEditingController();
  final _amountController = TextEditingController();
  DateTime? _dateAdded;
  int _quantity = 1;
  String? _purpose;
  bool _isSaving = false;

  static const _purposeOptions = <String>[
    'Home',
    'Office',
    'Locker',
    'Department',
    'Suspicious',
  ];

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _idController.dispose();
    _vehicleNoController.dispose();
    _keyNoController.dispose();
    _keyTypeController.dispose();
    _remarksController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  Future<void> _selectDate() async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: _dateAdded ?? now,
      firstDate: DateTime(now.year - 5),
      lastDate: DateTime(now.year + 5),
    );
    if (picked != null) {
      setState(() => _dateAdded = picked);
    }
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() => _isSaving = true);
    try {
      final amount = double.tryParse(_amountController.text.trim()) ?? 0;

      await widget.repository.create(
        KeyRecordsCompanion.insert(
          name: _nameController.text.trim(),
          phoneNumber: _phoneController.text.trim(),
          idNo: _idController.text.trim(),
          vehicleNo: drift.Value(_vehicleNoController.text.trim().isEmpty
              ? null
              : _vehicleNoController.text.trim()),
          keyNo: drift.Value(
              _keyNoController.text.trim().isEmpty ? null : _keyNoController.text.trim()),
          keyType: drift.Value(_keyTypeController.text.trim().isEmpty
              ? null
              : _keyTypeController.text.trim()),
          purpose: drift.Value(_purpose),
          dateAdded: drift.Value(_dateAdded),
          remarks: drift.Value(
              _remarksController.text.trim().isEmpty ? null : _remarksController.text.trim()),
          quantity: drift.Value(_quantity),
          amount: drift.Value(amount),
          imagePath: const drift.Value(null),
          createdAt: drift.Value(DateTime.now()),
          updatedAt: drift.Value(DateTime.now()),
        ),
      );

      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Record created successfully.')),
      );
      context.pop();
    } catch (error, stackTrace) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to save: $error')),
        );
      }
      // ignore: avoid_print
      print(stackTrace);
    } finally {
      if (mounted) {
        setState(() => _isSaving = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Add Record')),
      body: SafeArea(
        child: Form(
          key: _formKey,
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _buildTextField(
                controller: _nameController,
                label: 'Name *',
                keyboardType: TextInputType.name,
                validator: (value) =>
                    (value == null || value.trim().isEmpty) ? 'Name is required' : null,
              ),
              _buildTextField(
                controller: _phoneController,
                label: 'Phone Number *',
                keyboardType: TextInputType.phone,
                validator: (value) {
                  final digits = value?.trim() ?? '';
                  if (digits.length != 10) {
                    return 'Phone must be 10 digits';
                  }
                  return null;
                },
              ),
              _buildTextField(
                controller: _idController,
                label: 'ID Number *',
                validator: (value) =>
                    (value == null || value.trim().isEmpty) ? 'ID number is required' : null,
              ),
              _buildTextField(
                controller: _vehicleNoController,
                label: 'Vehicle No',
              ),
              _buildTextField(
                controller: _keyNoController,
                label: 'Key No',
              ),
              _buildTextField(
                controller: _keyTypeController,
                label: 'Key Type',
              ),
              DropdownButtonFormField<String>(
                // ignore: deprecated_member_use
                value: _purpose,
                decoration: const InputDecoration(labelText: 'Purpose'),
                items: _purposeOptions
                    .map((value) =>
                        DropdownMenuItem(value: value, child: Text(value)))
                    .toList(),
                onChanged: (value) => setState(() => _purpose = value),
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      _dateAdded == null
                          ? 'No date selected'
                          : 'Date: ${_dateAdded!.toLocal().toString().split(' ').first}',
                    ),
                  ),
                  TextButton(
                    onPressed: _selectDate,
                    child: const Text('Pick Date'),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Expanded(
                    child: InputDecorator(
                      decoration: const InputDecoration(labelText: 'Quantity'),
                      child: Row(
                        children: [
                          IconButton(
                            onPressed: _quantity > 1
                                ? () => setState(() => _quantity--)
                                : null,
                            icon: const Icon(Icons.remove),
                          ),
                          Text('$_quantity'),
                          IconButton(
                            onPressed: () => setState(() => _quantity++),
                            icon: const Icon(Icons.add),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _buildTextField(
                      controller: _amountController,
                      label: 'Amount',
                      keyboardType:
                          const TextInputType.numberWithOptions(decimal: true),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return null;
                        }
                        final parsed = double.tryParse(value.trim());
                        if (parsed == null || parsed < 0) {
                          return 'Enter a valid amount';
                        }
                        return null;
                      },
                    ),
                  ),
                ],
              ),
              _buildTextField(
                controller: _remarksController,
                label: 'Remarks',
                maxLines: 3,
              ),
              const SizedBox(height: 24),
              FilledButton.icon(
                onPressed: _isSaving ? null : _save,
                icon: _isSaving
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.save),
                label: const Text('Save Record'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    TextInputType? keyboardType,
    String? Function(String?)? validator,
    int maxLines = 1,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: TextFormField(
        controller: controller,
        keyboardType: keyboardType,
        validator: validator,
        maxLines: maxLines,
        decoration: InputDecoration(labelText: label),
      ),
    );
  }
}
