import 'dart:async';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:permission_handler/permission_handler.dart';
import 'package:printing/printing.dart' hide Printer;
import 'package:tsc_bluetooth_print/tsc_bluetooth_print.dart';

final plugin = TscBluetoothPrint();
const kPrinterAddress = 'DC:1D:30:63:C0:3C';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  Future<void> _onGuarded({required AsyncCallback run}) async {
    try {
      await run();
    } on PrinterException catch (e) {
      if (kDebugMode) print('[Example] ${e.message}');
    }
  }

  Future<void> _connect() async {
    _onGuarded(
      run: () async {
        final statuses = await const <Permission>[
          Permission.bluetooth,
          Permission.bluetoothScan,
          Permission.bluetoothConnect,
          Permission.location,
        ].request();

        if (statuses.values.every((status) => status.isGranted)) {
          const kPrinter = Printer(address: kPrinterAddress);
          await plugin.connect(kPrinter);
        }
      },
    );
  }

  Future<void> _print() async {
    _onGuarded(
      run: () async {
        final document = pw.Document()
          ..addPage(
            pw.Page(
              pageFormat: const PdfPageFormat(
                55 * PdfPageFormat.mm,
                29 * PdfPageFormat.mm,
              ),
              build: (_) => pw.DecoratedBox(
                decoration: pw.BoxDecoration(
                  color: PdfColor.fromInt(Colors.white.value),
                ),
                child: pw.Center(
                  child: pw.Text('Hello World'),
                ),
              ),
            ),
          );
        final documentBytes = await document.save();

        final raster = await Printing.raster(
          documentBytes,
          pages: const <int>[0],
          dpi: PdfPageFormat.inch.toDpi,
        ).first;
        final pngBytes = await raster.toPng();

        final configuration = PrintingConfiguration(
          count: 1,
          size: const Point<int>(55, 29),
          gap: 3,
          printableItems: <PrintableItem>[
            PrintableImage(
              origin: const Point<int>(0, 0),
              width: 55,
              bytes: pngBytes,
            ),
            const PrintableText(
              origin: Point<int>(5, 5),
              label: 'Fake label',
            ),
          ],
        );
        await plugin.print(configuration);
      },
    );
  }

  Future<void> _disconnect() async {
    _onGuarded(
      run: () async {
        await plugin.disconnect();
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Tsc Bluetooth Print'),
          centerTitle: true,
        ),
        body: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0),
          child: StreamBuilder<PrinterState>(
            stream: plugin.printerStateStream(),
            builder: (_, snapshot) {
              final label = snapshot.hasError
                  ? Text(
                      snapshot.error?.toString() ?? 'Unknown error',
                      textAlign: TextAlign.center,
                    )
                  : snapshot.hasData
                      ? _PrinterStateName(printerState: snapshot.data!)
                      : const Text('Data in waiting...');

              return Stack(
                alignment: Alignment.bottomCenter,
                children: <Widget>[
                  Center(child: label),
                  _Actions(
                    snapshot,
                    onConnect: _connect,
                    onPrint: _print,
                    onDisconnect: _disconnect,
                  ),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

class _PrinterStateName extends StatelessWidget {
  const _PrinterStateName({required this.printerState});

  final PrinterState printerState;

  @override
  Widget build(BuildContext context) {
    return Text(
      switch (printerState) {
        PrinterConnectingState() => 'Connecting...',
        PrinterConnectedState() => 'Connected',
        PrinterPrintingState() => 'Printing...',
        PrinterPrintedState() => 'Printed',
        PrinterDisconnectingState() => 'Disconnecting...',
        PrinterDisconnectedState() => 'Disconnected',
        PrinterErrorState(:final message) => 'Error\n$message',
      },
      textAlign: TextAlign.center,
    );
  }
}

class _Actions extends StatelessWidget {
  const _Actions(
    this.snapshot, {
    required this.onConnect,
    required this.onPrint,
    required this.onDisconnect,
  });

  final AsyncSnapshot<PrinterState> snapshot;
  final VoidCallback onConnect;
  final VoidCallback onPrint;
  final VoidCallback onDisconnect;

  @override
  Widget build(BuildContext context) {
    final connectButton = ElevatedButton(
      onPressed: onConnect,
      child: const Text("Connect"),
    );

    final printButton = ElevatedButton(
      onPressed: onPrint,
      child: const Text("Print"),
    );

    final disconnectButton = ElevatedButton(
      onPressed: onDisconnect,
      child: const Text("Disconnect"),
    );

    final buttons = snapshot.hasError
        ? const <Widget>[]
        : snapshot.hasData
            ? switch (snapshot.data!) {
                PrinterConnectingState() ||
                PrinterPrintingState() ||
                PrinterDisconnectingState() =>
                  const <Widget>[],
                PrinterConnectedState() || PrinterPrintedState() => <Widget>[
                    printButton,
                    disconnectButton,
                  ],
                PrinterDisconnectedState() || PrinterErrorState() => <Widget>[
                    connectButton,
                  ],
              }
            : <Widget>[connectButton];

    return Padding(
      padding: const EdgeInsets.only(bottom: 16.0),
      child: OverflowBar(
        spacing: 16.0,
        alignment: MainAxisAlignment.center,
        children: buttons,
      ),
    );
  }
}
