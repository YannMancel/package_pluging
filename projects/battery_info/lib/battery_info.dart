import 'package:battery_info/battery_info_platform_interface.dart';

class BatteryInfo {
  Future<int?> getBatteryLevel() {
    return BatteryInfoPlatform.instance.getBatteryLevel();
  }
}
