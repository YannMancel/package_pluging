import 'package:battery_info/battery_info.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('getBatteryLevel test', (WidgetTester tester) async {
    final plugin = BatteryInfo();
    final batteryLevel = await plugin.getBatteryLevel();
    expect(!batteryLevel!.isNegative, true);
  });
}
