import 'package:battery_info_example/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('Verify Battery Level', (tester) async {
    await tester.pumpWidget(const MyApp());

    expect(
      find.byWidgetPredicate(
        (widget) => widget is Text && widget.data!.startsWith('Battery Level:'),
      ),
      findsOneWidget,
    );
  });
}
