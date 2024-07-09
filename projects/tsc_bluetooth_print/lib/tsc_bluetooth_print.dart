import 'package:tsc_bluetooth_print/src/_src.dart';

export 'package:tsc_bluetooth_print/src/_src.dart'
    show
        DoubleExt,
        Printer,
        PrintingConfiguration,
        PrintableItem,
        PrintableText,
        PrintableImage,
        PrinterState,
        PrinterConnectingState,
        PrinterConnectedState,
        PrinterPrintingState,
        PrinterPrintedState,
        PrinterDisconnectingState,
        PrinterDisconnectedState,
        PrinterErrorState,
        PrinterException;

class TscBluetoothPrint {
  TscBluetoothPrintPlatform get _platform => TscBluetoothPrintPlatform.instance;

  Future<void> connect(Printer printer) async => _platform.connect(printer);

  Future<void> disconnect() async => _platform.disconnect();

  Future<void> print(PrintingConfiguration configuration) async {
    return _platform.print(configuration);
  }

  Stream<PrinterState> printerStateStream() => _platform.printerStateStream();
}
