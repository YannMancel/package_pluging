// import 'package:flutter_test/flutter_test.dart';
// import 'package:tsc_bluetooth_print/tsc_bluetooth_print.dart';
// import 'package:tsc_bluetooth_print/tsc_bluetooth_print_platform_interface.dart';
// import 'package:tsc_bluetooth_print/tsc_bluetooth_print_method_channel.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';
//
// class MockTscBluetoothPrintPlatform
//     with MockPlatformInterfaceMixin
//     implements TscBluetoothPrintPlatform {
//
//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }
//
// void main() {
//   final TscBluetoothPrintPlatform initialPlatform = TscBluetoothPrintPlatform.instance;
//
//   test('$MethodChannelTscBluetoothPrint is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelTscBluetoothPrint>());
//   });
//
//   test('getPlatformVersion', () async {
//     TscBluetoothPrint tscBluetoothPrintPlugin = TscBluetoothPrint();
//     MockTscBluetoothPrintPlatform fakePlatform = MockTscBluetoothPrintPlatform();
//     TscBluetoothPrintPlatform.instance = fakePlatform;
//
//     expect(await tscBluetoothPrintPlugin.getPlatformVersion(), '42');
//   });
// }
