import 'package:battery_info/battery_info_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

abstract class BatteryInfoPlatform extends PlatformInterface {
  BatteryInfoPlatform() : super(token: _token);

  static final Object _token = Object();

  static BatteryInfoPlatform _instance = MethodChannelBatteryInfo();

  static BatteryInfoPlatform get instance => _instance;

  static set instance(BatteryInfoPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<int?> getBatteryLevel() {
    throw UnimplementedError('getBatteryLevel() has not been implemented.');
  }
}
