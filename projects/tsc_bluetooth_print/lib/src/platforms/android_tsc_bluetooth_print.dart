import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:tsc_bluetooth_print/src/_src.dart';

class AndroidTscBluetoothPrint extends TscBluetoothPrintPlatform {
  @visibleForTesting
  static const kMethodChannel = MethodChannel('tsc_bluetooth_print_method');

  @visibleForTesting
  static const kEventChannel = EventChannel('tsc_bluetooth_print_event');

  @override
  Future<void> connect(Printer printer) async {
    try {
      await kMethodChannel.invokeMethod<void>(
        'connect',
        <String, String>{'address': printer.address},
      );
    } on PlatformException catch (e) {
      final message = e.message ?? 'Connection is failed from native.';
      throw ConnectionException(message);
    } catch (e) {
      final message = e.toString();
      throw ConnectionException(message);
    }
  }

  @override
  Future<void> disconnect() async {
    try {
      await kMethodChannel.invokeMethod<void>('disconnect');
    } on PlatformException catch (e) {
      final message = e.message ?? 'Disconnection is failed from native.';
      throw DisconnectionException(message);
    } catch (e) {
      final message = e.toString();
      throw DisconnectionException(message);
    }
  }

  @override
  Future<void> print(PrintingConfiguration configuration) async {
    try {
      await kMethodChannel.invokeMethod<bool>(
        'print',
        configuration.toJson(),
      );
    } on PlatformException catch (e) {
      final message = e.message ?? 'Printing is failed from native.';
      throw PrintingException(message);
    } catch (e) {
      final message = e.toString();
      throw PrintingException(message);
    }
  }

  @override
  Stream<PrinterState> printerStateStream() {
    return kEventChannel.receiveBroadcastStream().map<PrinterState>(
      (event) {
        final json = (event as Map<dynamic, dynamic>).cast<String, dynamic>();
        return PrinterState.fromJson(json);
      },
    );
  }
}
