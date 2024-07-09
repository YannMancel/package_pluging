import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:tsc_bluetooth_print/android_tsc_bluetooth_print.dart';
import 'package:tsc_bluetooth_print/models/_models.dart';

abstract class TscBluetoothPrintPlatform extends PlatformInterface {
  TscBluetoothPrintPlatform() : super(token: _token);

  static final Object _token = Object();

  static TscBluetoothPrintPlatform _instance = AndroidTscBluetoothPrint();

  static TscBluetoothPrintPlatform get instance => _instance;

  static set instance(TscBluetoothPrintPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<void> connect(Printer printer);

  Future<void> disconnect();

  Future<void> print(PrintingConfiguration configuration);

  Stream<PrinterState> printerStateStream();
}
