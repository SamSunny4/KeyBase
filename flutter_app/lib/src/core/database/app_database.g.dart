// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $KeyRecordsTable extends KeyRecords
    with TableInfo<$KeyRecordsTable, KeyRecord> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $KeyRecordsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
      'id', aliasedName, false,
      hasAutoIncrement: true,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('PRIMARY KEY AUTOINCREMENT'));
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 1, maxTextLength: 100),
      type: DriftSqlType.string,
      requiredDuringInsert: true);
  static const VerificationMeta _phoneNumberMeta =
      const VerificationMeta('phoneNumber');
  @override
  late final GeneratedColumn<String> phoneNumber = GeneratedColumn<String>(
      'phone_number', aliasedName, false,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 1, maxTextLength: 15),
      type: DriftSqlType.string,
      requiredDuringInsert: true);
  static const VerificationMeta _idNoMeta = const VerificationMeta('idNo');
  @override
  late final GeneratedColumn<String> idNo = GeneratedColumn<String>(
      'id_no', aliasedName, false,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 1, maxTextLength: 50),
      type: DriftSqlType.string,
      requiredDuringInsert: true);
  static const VerificationMeta _vehicleNoMeta =
      const VerificationMeta('vehicleNo');
  @override
  late final GeneratedColumn<String> vehicleNo = GeneratedColumn<String>(
      'vehicle_no', aliasedName, true,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 0, maxTextLength: 50),
      type: DriftSqlType.string,
      requiredDuringInsert: false);
  static const VerificationMeta _keyNoMeta = const VerificationMeta('keyNo');
  @override
  late final GeneratedColumn<String> keyNo = GeneratedColumn<String>(
      'key_no', aliasedName, true,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 0, maxTextLength: 50),
      type: DriftSqlType.string,
      requiredDuringInsert: false);
  static const VerificationMeta _keyTypeMeta =
      const VerificationMeta('keyType');
  @override
  late final GeneratedColumn<String> keyType = GeneratedColumn<String>(
      'key_type', aliasedName, true,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 0, maxTextLength: 50),
      type: DriftSqlType.string,
      requiredDuringInsert: false);
  static const VerificationMeta _purposeMeta =
      const VerificationMeta('purpose');
  @override
  late final GeneratedColumn<String> purpose = GeneratedColumn<String>(
      'purpose', aliasedName, true,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 0, maxTextLength: 50),
      type: DriftSqlType.string,
      requiredDuringInsert: false);
  static const VerificationMeta _dateAddedMeta =
      const VerificationMeta('dateAdded');
  @override
  late final GeneratedColumn<DateTime> dateAdded = GeneratedColumn<DateTime>(
      'date_added', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  static const VerificationMeta _remarksMeta =
      const VerificationMeta('remarks');
  @override
  late final GeneratedColumn<String> remarks = GeneratedColumn<String>(
      'remarks', aliasedName, true,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 0, maxTextLength: 500),
      type: DriftSqlType.string,
      requiredDuringInsert: false);
  static const VerificationMeta _quantityMeta =
      const VerificationMeta('quantity');
  @override
  late final GeneratedColumn<int> quantity = GeneratedColumn<int>(
      'quantity', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(1));
  static const VerificationMeta _amountMeta = const VerificationMeta('amount');
  @override
  late final GeneratedColumn<double> amount = GeneratedColumn<double>(
      'amount', aliasedName, false,
      type: DriftSqlType.double,
      requiredDuringInsert: false,
      defaultValue: const Constant(0.0));
  static const VerificationMeta _imagePathMeta =
      const VerificationMeta('imagePath');
  @override
  late final GeneratedColumn<String> imagePath = GeneratedColumn<String>(
      'image_path', aliasedName, true,
      additionalChecks:
          GeneratedColumn.checkTextLength(minTextLength: 0, maxTextLength: 255),
      type: DriftSqlType.string,
      requiredDuringInsert: false);
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        name,
        phoneNumber,
        idNo,
        vehicleNo,
        keyNo,
        keyType,
        purpose,
        dateAdded,
        remarks,
        quantity,
        amount,
        imagePath,
        createdAt,
        updatedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'key_records';
  @override
  VerificationContext validateIntegrity(Insertable<KeyRecord> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('name')) {
      context.handle(
          _nameMeta, name.isAcceptableOrUnknown(data['name']!, _nameMeta));
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('phone_number')) {
      context.handle(
          _phoneNumberMeta,
          phoneNumber.isAcceptableOrUnknown(
              data['phone_number']!, _phoneNumberMeta));
    } else if (isInserting) {
      context.missing(_phoneNumberMeta);
    }
    if (data.containsKey('id_no')) {
      context.handle(
          _idNoMeta, idNo.isAcceptableOrUnknown(data['id_no']!, _idNoMeta));
    } else if (isInserting) {
      context.missing(_idNoMeta);
    }
    if (data.containsKey('vehicle_no')) {
      context.handle(_vehicleNoMeta,
          vehicleNo.isAcceptableOrUnknown(data['vehicle_no']!, _vehicleNoMeta));
    }
    if (data.containsKey('key_no')) {
      context.handle(
          _keyNoMeta, keyNo.isAcceptableOrUnknown(data['key_no']!, _keyNoMeta));
    }
    if (data.containsKey('key_type')) {
      context.handle(_keyTypeMeta,
          keyType.isAcceptableOrUnknown(data['key_type']!, _keyTypeMeta));
    }
    if (data.containsKey('purpose')) {
      context.handle(_purposeMeta,
          purpose.isAcceptableOrUnknown(data['purpose']!, _purposeMeta));
    }
    if (data.containsKey('date_added')) {
      context.handle(_dateAddedMeta,
          dateAdded.isAcceptableOrUnknown(data['date_added']!, _dateAddedMeta));
    }
    if (data.containsKey('remarks')) {
      context.handle(_remarksMeta,
          remarks.isAcceptableOrUnknown(data['remarks']!, _remarksMeta));
    }
    if (data.containsKey('quantity')) {
      context.handle(_quantityMeta,
          quantity.isAcceptableOrUnknown(data['quantity']!, _quantityMeta));
    }
    if (data.containsKey('amount')) {
      context.handle(_amountMeta,
          amount.isAcceptableOrUnknown(data['amount']!, _amountMeta));
    }
    if (data.containsKey('image_path')) {
      context.handle(_imagePathMeta,
          imagePath.isAcceptableOrUnknown(data['image_path']!, _imagePathMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  KeyRecord map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return KeyRecord(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      phoneNumber: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}phone_number'])!,
      idNo: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id_no'])!,
      vehicleNo: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}vehicle_no']),
      keyNo: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}key_no']),
      keyType: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}key_type']),
      purpose: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}purpose']),
      dateAdded: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}date_added']),
      remarks: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}remarks']),
      quantity: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}quantity'])!,
      amount: attachedDatabase.typeMapping
          .read(DriftSqlType.double, data['${effectivePrefix}amount'])!,
      imagePath: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}image_path']),
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
    );
  }

  @override
  $KeyRecordsTable createAlias(String alias) {
    return $KeyRecordsTable(attachedDatabase, alias);
  }
}

class KeyRecord extends DataClass implements Insertable<KeyRecord> {
  final int id;
  final String name;
  final String phoneNumber;
  final String idNo;
  final String? vehicleNo;
  final String? keyNo;
  final String? keyType;
  final String? purpose;
  final DateTime? dateAdded;
  final String? remarks;
  final int quantity;
  final double amount;
  final String? imagePath;
  final DateTime createdAt;
  final DateTime updatedAt;
  const KeyRecord(
      {required this.id,
      required this.name,
      required this.phoneNumber,
      required this.idNo,
      this.vehicleNo,
      this.keyNo,
      this.keyType,
      this.purpose,
      this.dateAdded,
      this.remarks,
      required this.quantity,
      required this.amount,
      this.imagePath,
      required this.createdAt,
      required this.updatedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['name'] = Variable<String>(name);
    map['phone_number'] = Variable<String>(phoneNumber);
    map['id_no'] = Variable<String>(idNo);
    if (!nullToAbsent || vehicleNo != null) {
      map['vehicle_no'] = Variable<String>(vehicleNo);
    }
    if (!nullToAbsent || keyNo != null) {
      map['key_no'] = Variable<String>(keyNo);
    }
    if (!nullToAbsent || keyType != null) {
      map['key_type'] = Variable<String>(keyType);
    }
    if (!nullToAbsent || purpose != null) {
      map['purpose'] = Variable<String>(purpose);
    }
    if (!nullToAbsent || dateAdded != null) {
      map['date_added'] = Variable<DateTime>(dateAdded);
    }
    if (!nullToAbsent || remarks != null) {
      map['remarks'] = Variable<String>(remarks);
    }
    map['quantity'] = Variable<int>(quantity);
    map['amount'] = Variable<double>(amount);
    if (!nullToAbsent || imagePath != null) {
      map['image_path'] = Variable<String>(imagePath);
    }
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    return map;
  }

  KeyRecordsCompanion toCompanion(bool nullToAbsent) {
    return KeyRecordsCompanion(
      id: Value(id),
      name: Value(name),
      phoneNumber: Value(phoneNumber),
      idNo: Value(idNo),
      vehicleNo: vehicleNo == null && nullToAbsent
          ? const Value.absent()
          : Value(vehicleNo),
      keyNo:
          keyNo == null && nullToAbsent ? const Value.absent() : Value(keyNo),
      keyType: keyType == null && nullToAbsent
          ? const Value.absent()
          : Value(keyType),
      purpose: purpose == null && nullToAbsent
          ? const Value.absent()
          : Value(purpose),
      dateAdded: dateAdded == null && nullToAbsent
          ? const Value.absent()
          : Value(dateAdded),
      remarks: remarks == null && nullToAbsent
          ? const Value.absent()
          : Value(remarks),
      quantity: Value(quantity),
      amount: Value(amount),
      imagePath: imagePath == null && nullToAbsent
          ? const Value.absent()
          : Value(imagePath),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
    );
  }

  factory KeyRecord.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return KeyRecord(
      id: serializer.fromJson<int>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      phoneNumber: serializer.fromJson<String>(json['phoneNumber']),
      idNo: serializer.fromJson<String>(json['idNo']),
      vehicleNo: serializer.fromJson<String?>(json['vehicleNo']),
      keyNo: serializer.fromJson<String?>(json['keyNo']),
      keyType: serializer.fromJson<String?>(json['keyType']),
      purpose: serializer.fromJson<String?>(json['purpose']),
      dateAdded: serializer.fromJson<DateTime?>(json['dateAdded']),
      remarks: serializer.fromJson<String?>(json['remarks']),
      quantity: serializer.fromJson<int>(json['quantity']),
      amount: serializer.fromJson<double>(json['amount']),
      imagePath: serializer.fromJson<String?>(json['imagePath']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'name': serializer.toJson<String>(name),
      'phoneNumber': serializer.toJson<String>(phoneNumber),
      'idNo': serializer.toJson<String>(idNo),
      'vehicleNo': serializer.toJson<String?>(vehicleNo),
      'keyNo': serializer.toJson<String?>(keyNo),
      'keyType': serializer.toJson<String?>(keyType),
      'purpose': serializer.toJson<String?>(purpose),
      'dateAdded': serializer.toJson<DateTime?>(dateAdded),
      'remarks': serializer.toJson<String?>(remarks),
      'quantity': serializer.toJson<int>(quantity),
      'amount': serializer.toJson<double>(amount),
      'imagePath': serializer.toJson<String?>(imagePath),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
    };
  }

  KeyRecord copyWith(
          {int? id,
          String? name,
          String? phoneNumber,
          String? idNo,
          Value<String?> vehicleNo = const Value.absent(),
          Value<String?> keyNo = const Value.absent(),
          Value<String?> keyType = const Value.absent(),
          Value<String?> purpose = const Value.absent(),
          Value<DateTime?> dateAdded = const Value.absent(),
          Value<String?> remarks = const Value.absent(),
          int? quantity,
          double? amount,
          Value<String?> imagePath = const Value.absent(),
          DateTime? createdAt,
          DateTime? updatedAt}) =>
      KeyRecord(
        id: id ?? this.id,
        name: name ?? this.name,
        phoneNumber: phoneNumber ?? this.phoneNumber,
        idNo: idNo ?? this.idNo,
        vehicleNo: vehicleNo.present ? vehicleNo.value : this.vehicleNo,
        keyNo: keyNo.present ? keyNo.value : this.keyNo,
        keyType: keyType.present ? keyType.value : this.keyType,
        purpose: purpose.present ? purpose.value : this.purpose,
        dateAdded: dateAdded.present ? dateAdded.value : this.dateAdded,
        remarks: remarks.present ? remarks.value : this.remarks,
        quantity: quantity ?? this.quantity,
        amount: amount ?? this.amount,
        imagePath: imagePath.present ? imagePath.value : this.imagePath,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
      );
  KeyRecord copyWithCompanion(KeyRecordsCompanion data) {
    return KeyRecord(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      phoneNumber:
          data.phoneNumber.present ? data.phoneNumber.value : this.phoneNumber,
      idNo: data.idNo.present ? data.idNo.value : this.idNo,
      vehicleNo: data.vehicleNo.present ? data.vehicleNo.value : this.vehicleNo,
      keyNo: data.keyNo.present ? data.keyNo.value : this.keyNo,
      keyType: data.keyType.present ? data.keyType.value : this.keyType,
      purpose: data.purpose.present ? data.purpose.value : this.purpose,
      dateAdded: data.dateAdded.present ? data.dateAdded.value : this.dateAdded,
      remarks: data.remarks.present ? data.remarks.value : this.remarks,
      quantity: data.quantity.present ? data.quantity.value : this.quantity,
      amount: data.amount.present ? data.amount.value : this.amount,
      imagePath: data.imagePath.present ? data.imagePath.value : this.imagePath,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('KeyRecord(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('phoneNumber: $phoneNumber, ')
          ..write('idNo: $idNo, ')
          ..write('vehicleNo: $vehicleNo, ')
          ..write('keyNo: $keyNo, ')
          ..write('keyType: $keyType, ')
          ..write('purpose: $purpose, ')
          ..write('dateAdded: $dateAdded, ')
          ..write('remarks: $remarks, ')
          ..write('quantity: $quantity, ')
          ..write('amount: $amount, ')
          ..write('imagePath: $imagePath, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id,
      name,
      phoneNumber,
      idNo,
      vehicleNo,
      keyNo,
      keyType,
      purpose,
      dateAdded,
      remarks,
      quantity,
      amount,
      imagePath,
      createdAt,
      updatedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is KeyRecord &&
          other.id == this.id &&
          other.name == this.name &&
          other.phoneNumber == this.phoneNumber &&
          other.idNo == this.idNo &&
          other.vehicleNo == this.vehicleNo &&
          other.keyNo == this.keyNo &&
          other.keyType == this.keyType &&
          other.purpose == this.purpose &&
          other.dateAdded == this.dateAdded &&
          other.remarks == this.remarks &&
          other.quantity == this.quantity &&
          other.amount == this.amount &&
          other.imagePath == this.imagePath &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt);
}

class KeyRecordsCompanion extends UpdateCompanion<KeyRecord> {
  final Value<int> id;
  final Value<String> name;
  final Value<String> phoneNumber;
  final Value<String> idNo;
  final Value<String?> vehicleNo;
  final Value<String?> keyNo;
  final Value<String?> keyType;
  final Value<String?> purpose;
  final Value<DateTime?> dateAdded;
  final Value<String?> remarks;
  final Value<int> quantity;
  final Value<double> amount;
  final Value<String?> imagePath;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  const KeyRecordsCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.phoneNumber = const Value.absent(),
    this.idNo = const Value.absent(),
    this.vehicleNo = const Value.absent(),
    this.keyNo = const Value.absent(),
    this.keyType = const Value.absent(),
    this.purpose = const Value.absent(),
    this.dateAdded = const Value.absent(),
    this.remarks = const Value.absent(),
    this.quantity = const Value.absent(),
    this.amount = const Value.absent(),
    this.imagePath = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
  });
  KeyRecordsCompanion.insert({
    this.id = const Value.absent(),
    required String name,
    required String phoneNumber,
    required String idNo,
    this.vehicleNo = const Value.absent(),
    this.keyNo = const Value.absent(),
    this.keyType = const Value.absent(),
    this.purpose = const Value.absent(),
    this.dateAdded = const Value.absent(),
    this.remarks = const Value.absent(),
    this.quantity = const Value.absent(),
    this.amount = const Value.absent(),
    this.imagePath = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
  })  : name = Value(name),
        phoneNumber = Value(phoneNumber),
        idNo = Value(idNo);
  static Insertable<KeyRecord> custom({
    Expression<int>? id,
    Expression<String>? name,
    Expression<String>? phoneNumber,
    Expression<String>? idNo,
    Expression<String>? vehicleNo,
    Expression<String>? keyNo,
    Expression<String>? keyType,
    Expression<String>? purpose,
    Expression<DateTime>? dateAdded,
    Expression<String>? remarks,
    Expression<int>? quantity,
    Expression<double>? amount,
    Expression<String>? imagePath,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (phoneNumber != null) 'phone_number': phoneNumber,
      if (idNo != null) 'id_no': idNo,
      if (vehicleNo != null) 'vehicle_no': vehicleNo,
      if (keyNo != null) 'key_no': keyNo,
      if (keyType != null) 'key_type': keyType,
      if (purpose != null) 'purpose': purpose,
      if (dateAdded != null) 'date_added': dateAdded,
      if (remarks != null) 'remarks': remarks,
      if (quantity != null) 'quantity': quantity,
      if (amount != null) 'amount': amount,
      if (imagePath != null) 'image_path': imagePath,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
    });
  }

  KeyRecordsCompanion copyWith(
      {Value<int>? id,
      Value<String>? name,
      Value<String>? phoneNumber,
      Value<String>? idNo,
      Value<String?>? vehicleNo,
      Value<String?>? keyNo,
      Value<String?>? keyType,
      Value<String?>? purpose,
      Value<DateTime?>? dateAdded,
      Value<String?>? remarks,
      Value<int>? quantity,
      Value<double>? amount,
      Value<String?>? imagePath,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt}) {
    return KeyRecordsCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      idNo: idNo ?? this.idNo,
      vehicleNo: vehicleNo ?? this.vehicleNo,
      keyNo: keyNo ?? this.keyNo,
      keyType: keyType ?? this.keyType,
      purpose: purpose ?? this.purpose,
      dateAdded: dateAdded ?? this.dateAdded,
      remarks: remarks ?? this.remarks,
      quantity: quantity ?? this.quantity,
      amount: amount ?? this.amount,
      imagePath: imagePath ?? this.imagePath,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (phoneNumber.present) {
      map['phone_number'] = Variable<String>(phoneNumber.value);
    }
    if (idNo.present) {
      map['id_no'] = Variable<String>(idNo.value);
    }
    if (vehicleNo.present) {
      map['vehicle_no'] = Variable<String>(vehicleNo.value);
    }
    if (keyNo.present) {
      map['key_no'] = Variable<String>(keyNo.value);
    }
    if (keyType.present) {
      map['key_type'] = Variable<String>(keyType.value);
    }
    if (purpose.present) {
      map['purpose'] = Variable<String>(purpose.value);
    }
    if (dateAdded.present) {
      map['date_added'] = Variable<DateTime>(dateAdded.value);
    }
    if (remarks.present) {
      map['remarks'] = Variable<String>(remarks.value);
    }
    if (quantity.present) {
      map['quantity'] = Variable<int>(quantity.value);
    }
    if (amount.present) {
      map['amount'] = Variable<double>(amount.value);
    }
    if (imagePath.present) {
      map['image_path'] = Variable<String>(imagePath.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('KeyRecordsCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('phoneNumber: $phoneNumber, ')
          ..write('idNo: $idNo, ')
          ..write('vehicleNo: $vehicleNo, ')
          ..write('keyNo: $keyNo, ')
          ..write('keyType: $keyType, ')
          ..write('purpose: $purpose, ')
          ..write('dateAdded: $dateAdded, ')
          ..write('remarks: $remarks, ')
          ..write('quantity: $quantity, ')
          ..write('amount: $amount, ')
          ..write('imagePath: $imagePath, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $KeyRecordsTable keyRecords = $KeyRecordsTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [keyRecords];
}

typedef $$KeyRecordsTableCreateCompanionBuilder = KeyRecordsCompanion Function({
  Value<int> id,
  required String name,
  required String phoneNumber,
  required String idNo,
  Value<String?> vehicleNo,
  Value<String?> keyNo,
  Value<String?> keyType,
  Value<String?> purpose,
  Value<DateTime?> dateAdded,
  Value<String?> remarks,
  Value<int> quantity,
  Value<double> amount,
  Value<String?> imagePath,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
});
typedef $$KeyRecordsTableUpdateCompanionBuilder = KeyRecordsCompanion Function({
  Value<int> id,
  Value<String> name,
  Value<String> phoneNumber,
  Value<String> idNo,
  Value<String?> vehicleNo,
  Value<String?> keyNo,
  Value<String?> keyType,
  Value<String?> purpose,
  Value<DateTime?> dateAdded,
  Value<String?> remarks,
  Value<int> quantity,
  Value<double> amount,
  Value<String?> imagePath,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
});

class $$KeyRecordsTableFilterComposer
    extends Composer<_$AppDatabase, $KeyRecordsTable> {
  $$KeyRecordsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get phoneNumber => $composableBuilder(
      column: $table.phoneNumber, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get idNo => $composableBuilder(
      column: $table.idNo, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get vehicleNo => $composableBuilder(
      column: $table.vehicleNo, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get keyNo => $composableBuilder(
      column: $table.keyNo, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get keyType => $composableBuilder(
      column: $table.keyType, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get purpose => $composableBuilder(
      column: $table.purpose, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get dateAdded => $composableBuilder(
      column: $table.dateAdded, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get remarks => $composableBuilder(
      column: $table.remarks, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get quantity => $composableBuilder(
      column: $table.quantity, builder: (column) => ColumnFilters(column));

  ColumnFilters<double> get amount => $composableBuilder(
      column: $table.amount, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get imagePath => $composableBuilder(
      column: $table.imagePath, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));
}

class $$KeyRecordsTableOrderingComposer
    extends Composer<_$AppDatabase, $KeyRecordsTable> {
  $$KeyRecordsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get phoneNumber => $composableBuilder(
      column: $table.phoneNumber, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get idNo => $composableBuilder(
      column: $table.idNo, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get vehicleNo => $composableBuilder(
      column: $table.vehicleNo, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get keyNo => $composableBuilder(
      column: $table.keyNo, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get keyType => $composableBuilder(
      column: $table.keyType, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get purpose => $composableBuilder(
      column: $table.purpose, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get dateAdded => $composableBuilder(
      column: $table.dateAdded, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get remarks => $composableBuilder(
      column: $table.remarks, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get quantity => $composableBuilder(
      column: $table.quantity, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<double> get amount => $composableBuilder(
      column: $table.amount, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get imagePath => $composableBuilder(
      column: $table.imagePath, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));
}

class $$KeyRecordsTableAnnotationComposer
    extends Composer<_$AppDatabase, $KeyRecordsTable> {
  $$KeyRecordsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get phoneNumber => $composableBuilder(
      column: $table.phoneNumber, builder: (column) => column);

  GeneratedColumn<String> get idNo =>
      $composableBuilder(column: $table.idNo, builder: (column) => column);

  GeneratedColumn<String> get vehicleNo =>
      $composableBuilder(column: $table.vehicleNo, builder: (column) => column);

  GeneratedColumn<String> get keyNo =>
      $composableBuilder(column: $table.keyNo, builder: (column) => column);

  GeneratedColumn<String> get keyType =>
      $composableBuilder(column: $table.keyType, builder: (column) => column);

  GeneratedColumn<String> get purpose =>
      $composableBuilder(column: $table.purpose, builder: (column) => column);

  GeneratedColumn<DateTime> get dateAdded =>
      $composableBuilder(column: $table.dateAdded, builder: (column) => column);

  GeneratedColumn<String> get remarks =>
      $composableBuilder(column: $table.remarks, builder: (column) => column);

  GeneratedColumn<int> get quantity =>
      $composableBuilder(column: $table.quantity, builder: (column) => column);

  GeneratedColumn<double> get amount =>
      $composableBuilder(column: $table.amount, builder: (column) => column);

  GeneratedColumn<String> get imagePath =>
      $composableBuilder(column: $table.imagePath, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);
}

class $$KeyRecordsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $KeyRecordsTable,
    KeyRecord,
    $$KeyRecordsTableFilterComposer,
    $$KeyRecordsTableOrderingComposer,
    $$KeyRecordsTableAnnotationComposer,
    $$KeyRecordsTableCreateCompanionBuilder,
    $$KeyRecordsTableUpdateCompanionBuilder,
    (KeyRecord, BaseReferences<_$AppDatabase, $KeyRecordsTable, KeyRecord>),
    KeyRecord,
    PrefetchHooks Function()> {
  $$KeyRecordsTableTableManager(_$AppDatabase db, $KeyRecordsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$KeyRecordsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$KeyRecordsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$KeyRecordsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<int> id = const Value.absent(),
            Value<String> name = const Value.absent(),
            Value<String> phoneNumber = const Value.absent(),
            Value<String> idNo = const Value.absent(),
            Value<String?> vehicleNo = const Value.absent(),
            Value<String?> keyNo = const Value.absent(),
            Value<String?> keyType = const Value.absent(),
            Value<String?> purpose = const Value.absent(),
            Value<DateTime?> dateAdded = const Value.absent(),
            Value<String?> remarks = const Value.absent(),
            Value<int> quantity = const Value.absent(),
            Value<double> amount = const Value.absent(),
            Value<String?> imagePath = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
          }) =>
              KeyRecordsCompanion(
            id: id,
            name: name,
            phoneNumber: phoneNumber,
            idNo: idNo,
            vehicleNo: vehicleNo,
            keyNo: keyNo,
            keyType: keyType,
            purpose: purpose,
            dateAdded: dateAdded,
            remarks: remarks,
            quantity: quantity,
            amount: amount,
            imagePath: imagePath,
            createdAt: createdAt,
            updatedAt: updatedAt,
          ),
          createCompanionCallback: ({
            Value<int> id = const Value.absent(),
            required String name,
            required String phoneNumber,
            required String idNo,
            Value<String?> vehicleNo = const Value.absent(),
            Value<String?> keyNo = const Value.absent(),
            Value<String?> keyType = const Value.absent(),
            Value<String?> purpose = const Value.absent(),
            Value<DateTime?> dateAdded = const Value.absent(),
            Value<String?> remarks = const Value.absent(),
            Value<int> quantity = const Value.absent(),
            Value<double> amount = const Value.absent(),
            Value<String?> imagePath = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
          }) =>
              KeyRecordsCompanion.insert(
            id: id,
            name: name,
            phoneNumber: phoneNumber,
            idNo: idNo,
            vehicleNo: vehicleNo,
            keyNo: keyNo,
            keyType: keyType,
            purpose: purpose,
            dateAdded: dateAdded,
            remarks: remarks,
            quantity: quantity,
            amount: amount,
            imagePath: imagePath,
            createdAt: createdAt,
            updatedAt: updatedAt,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$KeyRecordsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $KeyRecordsTable,
    KeyRecord,
    $$KeyRecordsTableFilterComposer,
    $$KeyRecordsTableOrderingComposer,
    $$KeyRecordsTableAnnotationComposer,
    $$KeyRecordsTableCreateCompanionBuilder,
    $$KeyRecordsTableUpdateCompanionBuilder,
    (KeyRecord, BaseReferences<_$AppDatabase, $KeyRecordsTable, KeyRecord>),
    KeyRecord,
    PrefetchHooks Function()>;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$KeyRecordsTableTableManager get keyRecords =>
      $$KeyRecordsTableTableManager(_db, _db.keyRecords);
}
