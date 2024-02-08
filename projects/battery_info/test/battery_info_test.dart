import 'package:battery_info/battery_info.dart';
import 'package:battery_info/battery_info_method_channel.dart';
import 'package:battery_info/battery_info_platform_interface.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBatteryInfoPlatform
    with MockPlatformInterfaceMixin
    implements BatteryInfoPlatform {
  @override
  Future<int?> getBatteryLevel() => Future.value(42);
}

void main() {
  final BatteryInfoPlatform initialPlatform = BatteryInfoPlatform.instance;

  test('$MethodChannelBatteryInfo is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBatteryInfo>());
  });

  test('getBatteryLevel', () async {
    BatteryInfo batteryInfoPlugin = BatteryInfo();
    MockBatteryInfoPlatform fakePlatform = MockBatteryInfoPlatform();
    BatteryInfoPlatform.instance = fakePlatform;

    expect(await batteryInfoPlugin.getBatteryLevel(), 42);
  });
}
