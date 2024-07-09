import 'package:tsc_bluetooth_print/models/_models.dart';
import 'package:tsc_bluetooth_print/tsc_bluetooth_print_platform.dart';

export 'package:tsc_bluetooth_print/dpi_extensions.dart';

class TscBluetoothPrint {
  TscBluetoothPrintPlatform get _platform => TscBluetoothPrintPlatform.instance;

  Future<void> connect(Printer printer) async => _platform.connect(printer);

  Future<void> disconnect() async => _platform.disconnect();

  Future<void> print(PrintingConfiguration configuration) async {
    return _platform.print(configuration);
  }

  Stream<PrinterState> printerStateStream() => _platform.printerStateStream();
}
