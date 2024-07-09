import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:tsc_bluetooth_print/models/_models.dart';

@immutable
class PrintingConfiguration {
  const PrintingConfiguration({
    required this.count,
    required this.size,
    required this.gap,
    required this.printableItems,
  });

  final int count;

  /// It must be in mm.
  final Point<int> size;

  /// It must be in mm.
  final int gap;

  /// Warning: Order may be important for printing.
  final List<PrintableItem> printableItems;

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (runtimeType == other.runtimeType &&
            other is PrintingConfiguration &&
            count == other.count &&
            size == other.size &&
            gap == other.gap &&
            // TODO(Yann): add Collection package to compare List<PrintableItem>
            printableItems == other.printableItems);
  }

  @override
  int get hashCode {
    return Object.hashAll(
      <Object?>[
        runtimeType,
        count,
        size,
        gap,
        printableItems,
      ],
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'count': count,
      'width': size.x,
      'height': size.y,
      'gap': gap,
      'printableItems': <Map<String, dynamic>>[
        for (final printableItem in printableItems) printableItem.toJson(),
      ],
    };
  }
}
