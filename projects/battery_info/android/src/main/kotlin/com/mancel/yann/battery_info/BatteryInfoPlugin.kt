package com.mancel.yann.battery_info

import android.content.Context
import android.os.BatteryManager
import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BatteryInfoPlugin */
class BatteryInfoPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private lateinit var context : Context
  private lateinit var batteryManager: BatteryManager

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "battery_info")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
    batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getBatteryLevel" -> getBatteryLevel(batteryManager, result)
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun getBatteryLevel(batteryManager: BatteryManager, result: Result) {
    try {
      val batteryLevel: Float = batteryManager.let { manager ->
        val level: Int = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val scale: Int = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)

        if (BuildConfig.DEBUG) {
          val message = """
            |BATTERY_INFO (getBatteryLevel):
            |  - level: $level
            |  - scale: $scale
            """.trimMargin()
          Log.i("BATTERY_INFO", message)
        }

        level.toFloat() * 100F / scale.toFloat()
      }
      result.success(batteryLevel)
    } catch (exception: Exception) {
      result.error("ERROR_GET_BATTERY_LEVEL", exception.message, null)
    }
  }
}
