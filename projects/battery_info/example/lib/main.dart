import 'dart:async';

import 'package:battery_info/battery_info.dart';
import 'package:flutter/material.dart';

void main() => runApp(const MyApp());

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

const kDefaultLevel = -1;

class _MyAppState extends State<MyApp> {
  final _batteryInfoPlugin = BatteryInfo();
  int _level = kDefaultLevel;

  @override
  void initState() {
    super.initState();
    _initPlatformState();
  }

  Future<void> _initPlatformState() async {
    final level = await _batteryInfoPlugin.getBatteryLevel() ?? kDefaultLevel;
    if (mounted) setState(() => _level = level);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Battery Level: $_level%'),
        ),
      ),
    );
  }
}
