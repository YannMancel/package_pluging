sealed class PrinterState {
  const PrinterState();

  factory PrinterState.fromJson(Map<String, dynamic> json) {
    final state = json['state'] as String? ?? '';
    return switch (state) {
      'CONNECTING' => const PrinterConnectingState(),
      'CONNECTED' => const PrinterConnectedState(),
      'PRINTING' => const PrinterPrintingState(),
      'PRINTED' => const PrinterPrintedState(),
      'DISCONNECTING' => const PrinterDisconnectingState(),
      'DISCONNECTED' => const PrinterDisconnectedState(),
      'ERROR' => PrinterErrorState(message: json['reason'] as String? ?? ''),
      _ => throw Exception('The state $state is not a correct value.'),
    };
  }
}

final class PrinterConnectingState extends PrinterState {
  const PrinterConnectingState();
}

final class PrinterConnectedState extends PrinterState {
  const PrinterConnectedState();
}

final class PrinterPrintingState extends PrinterState {
  const PrinterPrintingState();
}

final class PrinterPrintedState extends PrinterState {
  const PrinterPrintedState();
}

final class PrinterDisconnectingState extends PrinterState {
  const PrinterDisconnectingState();
}

final class PrinterDisconnectedState extends PrinterState {
  const PrinterDisconnectedState();
}

final class PrinterErrorState extends PrinterState {
  const PrinterErrorState({required this.message});

  final String message;
}
