import 'package:battery_info/battery_info_platform_interface.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class MethodChannelBatteryInfo extends BatteryInfoPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('battery_info');

  @override
  Future<int?> getBatteryLevel() async {
    int? level;
    try {
      level = await methodChannel.invokeMethod<int>('getBatteryLevel');
      if (kDebugMode) print('Battery level at $level% .');
    } on PlatformException catch (e) {
      if (kDebugMode) print('Failed to get battery level: ${e.message}.');
    }

    return level;
  }
}
