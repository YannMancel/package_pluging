import 'battery_info_platform_interface.dart';

class BatteryInfo {
  Future<double?> getBatteryLevel() {
    return BatteryInfoPlatform.instance.getBatteryLevel();
  }
}
