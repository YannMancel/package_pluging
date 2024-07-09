package com.mancel.yann.tsc_bluetooth_print

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.RequiresPermission
import java.io.IOException
import java.util.UUID
import java.util.Vector

interface BluetoothPort {
  fun deviceInfo(): String
  fun openPort(context: Context)
  fun readData(buffer: ByteArray): Int
  fun writeDataImmediately(data: Vector<Byte>)
  fun closePort()
}

// See: https://developer.android.com/develop/connectivity/bluetooth?hl=fr

class PrinterBluetoothPort(private val address: String) : BluetoothPort {
  private var socket: BluetoothSocket? = null
  private var device: BluetoothDevice? = null

  private companion object PortTools {
    val serialPortServiceClassUUID: UUID by lazy {
      UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
  }

  @Throws(IOException::class)
  @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
  override fun deviceInfo(): String {
    if (device == null) throw IOException("Bluetooth device is not initialized.")
    return "${device?.name} (${device?.address})"
  }

  @Throws(IOException::class)
  @RequiresPermission(
    allOf = [
      "android.permission.BLUETOOTH_SCAN",
      "android.permission.BLUETOOTH_CONNECT"
    ]
  )
  override fun openPort(context: Context) {
    val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
      ?: throw IOException("Bluetooth is not support.")
    // Cancel discovery because it otherwise slows down the connection.
    adapter.cancelDiscovery()
    if (!adapter.isEnabled) throw IOException("Bluetooth is not open.")

    val isValidAddress = BluetoothAdapter.checkBluetoothAddress(address)
    if (!isValidAddress) throw IOException("Address is not valid.")

    device = adapter.getRemoteDevice(address)
      ?: throw IOException("Remote device is not found.")
    this.socket = device!!.createInsecureRfcommSocketToServiceRecord(
      serialPortServiceClassUUID
    ).apply {
      connect()
    }
  }

  @Throws(IOException::class)
  override fun writeDataImmediately(data: Vector<Byte>) {
    if (socket == null) throw IOException("The socket is null.")
    socket?.apply {
      outputStream.write(data.toByteArray(), 0, data.size)
      outputStream.flush()
    }
  }

  @Throws(IOException::class)
  override fun readData(buffer: ByteArray): Int {
    if (socket == null) throw IOException("The socket is null.")
    return socket?.inputStream?.read(buffer) ?: 0
  }

  @Throws(IOException::class)
  override fun closePort() {
    socket?.close()
    socket = null
    device = null
  }
}

