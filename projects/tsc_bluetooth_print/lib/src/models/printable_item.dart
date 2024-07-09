import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:tsc_bluetooth_print/src/_src.dart';

enum PrintableType { text, image }

sealed class PrintableItem {
  const PrintableItem({
    required this.type,
    required this.origin,
  });

  final PrintableType type;

  /// It must be in mm. It will be convert in dpi (1mm = 8dpi) for printing.
  final Point<int> origin;

  Map<String, dynamic> toJson() => throw UnimplementedError();
}

@immutable
class PrintableText extends PrintableItem {
  const PrintableText({
    required super.origin,
    required this.label,
  }) : super(type: PrintableType.text);

  final String label;

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (runtimeType == other.runtimeType &&
            other is PrintableText &&
            type == other.type &&
            origin == other.origin &&
            label == other.label);
  }

  @override
  int get hashCode {
    return Object.hashAll(
      <Object?>[
        runtimeType,
        type,
        origin,
        label,
      ],
    );
  }

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': type.name,
      'x': origin.x.toDpi,
      'y': origin.y.toDpi,
      'label': label,
    };
  }
}

@immutable
class PrintableImage extends PrintableItem {
  const PrintableImage({
    required super.origin,
    required this.width,
    required this.bytes,
  }) : super(type: PrintableType.image);

  /// It must be in mm. It will be convert in dpi (1mm = 8dpi) for printing.
  final int width;
  final Uint8List bytes;

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (runtimeType == other.runtimeType &&
            other is PrintableImage &&
            type == other.type &&
            origin == other.origin &&
            width == other.width &&
            // TODO(Yann): add Collection package to compare List<int>
            bytes == other.bytes);
  }

  @override
  int get hashCode {
    return Object.hashAll(
      <Object?>[
        runtimeType,
        type,
        origin,
        width,
        bytes,
      ],
    );
  }

  @override
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'type': type.name,
      'x': origin.x.toDpi,
      'y': origin.y.toDpi,
      'width': width.toDpi,
      'bytes': bytes,
    };
  }
}
