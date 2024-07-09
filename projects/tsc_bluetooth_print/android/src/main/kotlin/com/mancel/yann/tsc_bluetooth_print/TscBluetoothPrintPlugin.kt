package com.mancel.yann.tsc_bluetooth_print

import android.os.Handler
import android.os.Looper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class TscBluetoothPrintPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
  private lateinit var methodChannel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private var eventSink: EventChannel.EventSink? = null
  private lateinit var handler: Handler
  private lateinit var printingService: PrintingService

  private companion object DartCommunication {
    const val METHOD_CHANNEL_NAME = "tsc_bluetooth_print_method"
    const val EVENT_CHANNEL_NAME = "tsc_bluetooth_print_event"
    const val CONNECTION_ERROR = "CONNECTION_ERROR"
    const val DISCONNECTION_ERROR = "DISCONNECTION_ERROR"
    const val PRINTING_ERROR = "PRINTING_ERROR"
    const val PLUGIN_LIFE_ERROR = "PLUGIN_LIFE_ERROR"
    const val PLUGIN_LIFE_MESSAGE = "printerManager is not initialized."
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val context = flutterPluginBinding.applicationContext
    val mainLooper = Looper.getMainLooper()
    handler = Handler(mainLooper) { message ->
      val state = PrinterState.fromOrdinal(message.what)
      val json = mapOf(
        "state" to state.name,
        "reason" to message.obj as String?
      )
      eventSink?.success(json)
      true
    }
    printingService = TscPrintingService(context, handler)

    val binaryMessenger = flutterPluginBinding.binaryMessenger
    methodChannel = MethodChannel(binaryMessenger, METHOD_CHANNEL_NAME).apply {
      setMethodCallHandler(this@TscBluetoothPrintPlugin)
    }

    eventChannel = EventChannel(binaryMessenger, EVENT_CHANNEL_NAME).apply {
      setStreamHandler(this@TscBluetoothPrintPlugin)
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (::printingService.isInitialized) {
      when (call.method) {
        "connect" ->  printingService.connect(
          json = call.json(),
          onSuccess = { result.successWithNull() },
          onError = { result.errorWithSimpleMessage(CONNECTION_ERROR, it.message) }
        )
        "disconnect" -> printingService.disconnect(
          onSuccess = { result.successWithNull() },
          onError = { result.errorWithSimpleMessage(DISCONNECTION_ERROR, it.message) }
        )
        "print" -> printingService.print(
          json = call.json(),
          onSuccess = { result.successWithNull() },
          onError = { result.errorWithSimpleMessage(PRINTING_ERROR, it.message) }
        )
        else -> result.notImplemented()
      }
    } else {
      result.errorWithSimpleMessage(PLUGIN_LIFE_ERROR, PLUGIN_LIFE_MESSAGE)
    }
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    if (::methodChannel.isInitialized) methodChannel.setMethodCallHandler(null)
    if (::eventChannel.isInitialized) eventChannel.setStreamHandler(null)
  }
}
