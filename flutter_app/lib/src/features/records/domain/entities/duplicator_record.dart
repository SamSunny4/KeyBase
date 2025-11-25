import '../../../../core/database/app_database.dart';

class DuplicatorRecord {
  const DuplicatorRecord({
    required this.id,
    required this.name,
    required this.phoneNumber,
    required this.idNo,
    this.vehicleNo,
    this.keyNo,
    this.keyType,
    this.purpose,
    this.dateAdded,
    required this.createdAt,
    required this.updatedAt,
    required this.quantity,
    required this.amount,
    this.remarks,
    this.imagePath,
  });

  factory DuplicatorRecord.fromData(KeyRecord data) => DuplicatorRecord(
        id: data.id,
        name: data.name,
        phoneNumber: data.phoneNumber,
        idNo: data.idNo,
        vehicleNo: data.vehicleNo,
        keyNo: data.keyNo,
        keyType: data.keyType,
        purpose: data.purpose,
        dateAdded: data.dateAdded,
        createdAt: data.createdAt,
        updatedAt: data.updatedAt,
        quantity: data.quantity,
        amount: data.amount,
        remarks: data.remarks,
        imagePath: data.imagePath,
      );

  final int id;
  final String name;
  final String phoneNumber;
  final String idNo;
  final String? vehicleNo;
  final String? keyNo;
  final String? keyType;
  final String? purpose;
  final DateTime? dateAdded;
  final DateTime createdAt;
  final DateTime updatedAt;
  final int quantity;
  final double amount;
  final String? remarks;
  final String? imagePath;
}
