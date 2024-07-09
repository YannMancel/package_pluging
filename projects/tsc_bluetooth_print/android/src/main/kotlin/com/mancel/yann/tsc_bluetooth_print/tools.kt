package com.mancel.yann.tsc_bluetooth_print

import android.util.Log
import io.flutter.BuildConfig
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel.Result

const val TAG = "TSC BLUETOOTH PRINT"

fun debugLog(messageBuilder: () -> String) { if (BuildConfig.DEBUG) Log.d(TAG, messageBuilder()) }

fun MethodCall.json(): Map<String, Any> = arguments<Map<String, Any>>() ?: emptyMap()

fun Int.toDpi(): Int = this * 8

fun Int.isEqualTo1(): Boolean = this == 1

fun Result.successWithNull() = this.success(null)

fun Result.errorWithSimpleMessage(errorCode: String, errorMessage: String?) {
    return this.error(errorCode, errorMessage, null)
}
