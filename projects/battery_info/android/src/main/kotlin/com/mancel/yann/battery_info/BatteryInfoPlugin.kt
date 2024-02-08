package com.mancel.yann.battery_info

import android.content.Context
import android.os.BatteryManager
import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class BatteryInfoPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel
    private lateinit var context : Context

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "battery_info")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getBatteryLevel" -> getBatteryLevel(result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun getBatteryLevel(result: Result) {
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            if (BuildConfig.DEBUG) {
                val message = """
                |BATTERY_INFO (getBatteryLevel):
                |  - batteryLevel: $batteryLevel
                """.trimMargin()
                Log.i("BATTERY_INFO", message)
            }

            result.success(batteryLevel)
        } catch (exception: Exception) {
            result.error("ERROR_GET_BATTERY_LEVEL", exception.message, null)
        }
    }
}
