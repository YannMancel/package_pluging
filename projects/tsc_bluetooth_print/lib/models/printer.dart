import 'package:flutter/foundation.dart';

@immutable
class Printer {
  const Printer({
    required this.address,
  });

  final String address;

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (runtimeType == other.runtimeType &&
            other is Printer &&
            address == other.address);
  }

  @override
  int get hashCode {
    return Object.hashAll(
      <Object?>[
        runtimeType,
        address,
      ],
    );
  }
}
