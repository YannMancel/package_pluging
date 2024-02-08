import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'battery_info_platform_interface.dart';

/// An implementation of [BatteryInfoPlatform] that uses method channels.
class MethodChannelBatteryInfo extends BatteryInfoPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('battery_info');

  @override
  Future<double?> getBatteryLevel() async {
    return methodChannel.invokeMethod<double>('getBatteryLevel');
  }
}
