import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'battery_info_method_channel.dart';

abstract class BatteryInfoPlatform extends PlatformInterface {
  /// Constructs a BatteryInfoPlatform.
  BatteryInfoPlatform() : super(token: _token);

  static final Object _token = Object();

  static BatteryInfoPlatform _instance = MethodChannelBatteryInfo();

  /// The default instance of [BatteryInfoPlatform] to use.
  ///
  /// Defaults to [MethodChannelBatteryInfo].
  static BatteryInfoPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BatteryInfoPlatform] when
  /// they register themselves.
  static set instance(BatteryInfoPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<double?> getBatteryLevel() {
    throw UnimplementedError('getBatteryLevel() has not been implemented.');
  }
}
