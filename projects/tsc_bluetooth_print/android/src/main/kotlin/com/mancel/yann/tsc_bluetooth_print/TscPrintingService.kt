package com.mancel.yann.tsc_bluetooth_print

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import androidx.annotation.RequiresPermission
import com.gprinter.command.EscCommand
import com.gprinter.command.LabelCommand
import java.util.Vector
import kotlin.experimental.and
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

typealias OnSuccess = () -> Unit
typealias OnError = (Exception) -> Unit
typealias Json = Map<String, Any>

interface PrintingService {
  fun connect(
    json: Json,
    onSuccess: OnSuccess,
    onError: OnError,
  )
  fun disconnect(
    onSuccess: OnSuccess,
    onError: OnError,
  )
  fun print(
    json: Json,
    onSuccess: OnSuccess,
    onError: OnError,
  )
}

class TscPrintingService(
  private val context: Context,
  private val handler: Handler,
) : PrintingService {

  private var port: BluetoothPort? = null
  private var cyclicPingThread: CyclicPingThread? = null

  private companion object TscTools {
    private val tscPingBytes = byteArrayOf(0x1b, '!'.code.toByte(), '?'.code.toByte())
    private val tscPingCommand = Vector(tscPingBytes.toList())

    const val TSC_READ_BYTES_LENGTH_WHEN_AVAILABLE_PRINTER_STATE = 10
    const val TSC_OUT_OF_PAPER_ERROR = 0x04
    const val TSC_OPEN_COVER_ERROR = 0x01
    const val TSC_UNKNOWN_ERROR = 0x80
    fun hasOutOfPaperError(byte: Byte): Boolean = byte.and(TSC_OUT_OF_PAPER_ERROR.toByte()) > 0
    fun hasOpenCoverError(byte: Byte): Boolean = byte.and(TSC_OPEN_COVER_ERROR.toByte()) > 0
    fun hasUnknownError(byte: Byte): Boolean = byte.and(TSC_UNKNOWN_ERROR.toByte()) > 0
  }

  override fun connect(
    json: Json,
    onSuccess: OnSuccess,
    onError: OnError,
  ) {
    onGuarded(
      onSuccess,
      onError
    ) {
      sendPrinterStateToUI(PrinterState.CONNECTING)
      disconnectPreviousConnection()
      startConnectThread(json)
    }
  }

  override fun disconnect(
    onSuccess: OnSuccess,
    onError: OnError,
  ) {
    onGuarded(
      onSuccess,
      onError
    ) {
      sendPrinterStateToUI(PrinterState.DISCONNECTING)
      stopCyclicPingThread()
      startDisconnectThread()
    }
  }

  override fun print(
    json: Json,
    onSuccess: OnSuccess,
    onError: OnError,
  ) {
    onGuarded(
      onSuccess,
      onError
    ) {
      sendPrinterStateToUI(PrinterState.PRINTING)
      stopCyclicPingThread()
      startPrintThread(json)
    }
  }

  private fun onGuarded(
    onSuccess: OnSuccess? = null,
    onError: OnError,
    callback: () -> Unit,
  ) {
    try {
      callback()
      onSuccess?.invoke()
    } catch (e: Exception) {
      onError(e)
    }
  }

  @Throws(Exception::class)
  private fun disconnectPreviousConnection() {
    if (port != null) {
      stopCyclicPingThread()
      val reason = "The device ${port!!.deviceInfo()} was already connected. " +
              "It is now disconnected."
      closePort()
      sendPrinterStateToUI(PrinterState.ERROR, reason)
      throw Exception(reason)
    }
  }

  private fun sendPrinterStateToUI(state: PrinterState, reason: String? = null) {
    debugLog {
      val additionalMessage = if (reason.isNullOrEmpty()) "" else " ($reason)"
      "Printer state: $state$additionalMessage."
    }
    val message = handler.obtainMessage(state.ordinal, reason)
    message.sendToTarget()
  }

  @Throws(Exception::class)
  @RequiresPermission(
    allOf = [
      "android.permission.BLUETOOTH_SCAN",
      "android.permission.BLUETOOTH_CONNECT"
    ]
  )
  private fun openPort(json: Json) {
    val address = json["address"] as String?
      ?: throw Exception("No address in argument to open the port.")
    debugLog { "Address: $address." }
    port = PrinterBluetoothPort(address).apply {
      openPort(context)
    }
  }

  @Throws(Exception::class)
  private fun closePort() {
    if (port == null) throw Exception("The port is either uninitialised or already closed.")
    port?.closePort()
    port = null
  }

  @Throws(Exception::class)
  private fun generatePrintingCommand(json: Json) : Vector<Byte> {
    if (json.isEmpty()) throw Exception("No argument to generate printing command.")
    val configuration = PrintingConfiguration.fromJson(json)
    debugLog { configuration.toLog() }

    val tsc = LabelCommand().apply {
      addSize(configuration.size.width, configuration.size.height)
      addGap(configuration.gap)
      addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
      addQueryPrinterStatus(LabelCommand.RESPONSE_MODE.ON)
      addReference(0, 0)
      addDensity(LabelCommand.DENSITY.DNESITY4)
      addTear(EscCommand.ENABLE.ON)
      addCls()

      for (printableItem in configuration.printableItems) {
        when (printableItem) {
          is PrintableItem.Text ->
            addText(
              printableItem.origin.x, printableItem.origin.y,
              LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE,
              LabelCommand.ROTATION.ROTATION_0,
              LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
              printableItem.label
            )
          is PrintableItem.Image ->
            addBitmap(
              printableItem.origin.x, printableItem.origin.y,
              LabelCommand.BITMAP_MODE.OVERWRITE,
              printableItem.width,
              BitmapFactory.decodeByteArray(
                printableItem.bytes,
                0,
                printableItem.bytes.size
              )
            )
        }
      }

      addPrint(1, configuration.count)
      addSound(2, 100)
      addCashdrwer(LabelCommand.FOOT.F5, 255, 255)
    }

    return tsc.command
  }

  @Throws(Exception::class)
  private fun sendCommandImmediately(command: Vector<Byte>) {
    if (port == null) throw Exception("The port is either uninitialised or already closed.")
    port?.writeDataImmediately(command)
  }

  @Throws(Exception::class)
  private fun waitPrinterAvailability() {
    var isAvailable = false
    while (!isAvailable) {
      val readBytesLength = readDataImmediately()
      // After printing:
      //  ├ 1st call:
      //  │ ├ The read bytes length is 10.
      //  │ └ Buffer: [123, 0, 44, 48, 48, 48, 48, 48, 49, 125]
      //  └ 2nd call:
      //    ├ The read bytes length is 1.
      //    └ Buffer: [32]
      if (readBytesLength == TSC_READ_BYTES_LENGTH_WHEN_AVAILABLE_PRINTER_STATE) isAvailable = true
      debugLog { "The printer availability is $isAvailable." }
    }
  }

  @Throws(Exception::class)
  private fun readDataImmediately(): Int {
    if (port == null) throw Exception("The port is either uninitialised or already closed.")
    val buffer = ByteArray(1024)
    val readBytesLength = port?.readData(buffer) ?: 0
    debugLog {
      val builder = StringBuilder().apply {
        append("The read bytes length is $readBytesLength.\n")
        append("Buffer: ")
        for (i in 0 until readBytesLength) {
          append("($i: ${buffer[i]})")
        }
      }
      builder.toString()
    }

    if (readBytesLength.isEqualTo1()) {
      val byte = buffer.first()
      if (hasOutOfPaperError(byte)) throw Exception("There is no paper in the printer.")
      if (hasOpenCoverError(byte)) throw Exception("The printer cover is open.")
      if (hasUnknownError(byte)) throw Exception("An unknown error has occurred.")
    }

    return readBytesLength
  }

  @Throws(Exception::class)
  private fun postToHandler(
    thread: Thread,
    onError: () -> String,
    needThrowOnError: Boolean = true
  ) {
    val isSuccess = handler.post(thread)
    if (!isSuccess) {
      val reason = onError()
      sendPrinterStateToUI(PrinterState.ERROR, reason)
      if (needThrowOnError) throw Exception(reason)
    }
  }

  @Throws(Exception::class)
  private fun startConnectThread(json: Json) {
    val thread = ConnectThread(json)
    postToHandler(
      thread,
      onError = { "The connect thread was not placed in to the message queue." }
    )
  }

  @Throws(Exception::class)
  private fun startDisconnectThread() {
    val thread = DisconnectThread()
    postToHandler(
      thread,
      onError = { "The disconnect thread was not placed in to the message queue." }
    )
  }

  @Throws(Exception::class)
  private fun startPrintThread(json: Json) {
    val thread = PrintThread(json)
    postToHandler(
      thread,
      onError = { "The print thread was not placed in to the message queue." }
    )
  }

  @Throws(Exception::class)
  private fun startCyclicPingThread(
    delayBetweenEachCycle: Duration = 2.toDuration(DurationUnit.SECONDS),
  ) {
    cyclicPingThread = CyclicPingThread(delayBetweenEachCycle)
    postToHandler(
      cyclicPingThread!!,
      onError = { "The cyclic ping thread was not placed in to the message queue." },
      // The exception will never try/catch because this method is called in a thread. Nevertheless,
      // printer state will be in ERROR.
      needThrowOnError = false
    )
  }

  @Throws(Exception::class)
  private fun stopCyclicPingThread() {
    cyclicPingThread?.apply {
      isUsable = false
      interrupt()
      join()
    }
    cyclicPingThread = null
  }

  private inner class ConnectThread(private val json: Json) : Thread("Connect Thread") {
    @RequiresPermission(
      allOf = [
        "android.permission.BLUETOOTH_SCAN",
        "android.permission.BLUETOOTH_CONNECT"
      ]
    )
    override fun run() {
      onGuarded(
        onSuccess = { startCyclicPingThread() },
        onError = { e ->
          onGuarded(
            onError = { debugLog { "Attempted disconnection ${it.message ?: it.toString()}" } }
          ) { closePort() }
          val reason = e.message ?: e.toString()
          sendPrinterStateToUI(PrinterState.ERROR, reason)
        }
      ) {
        openPort(json)
        sendPrinterStateToUI(PrinterState.CONNECTED)
      }
    }
  }

  private inner class DisconnectThread : Thread("Disconnect Thread") {
    override fun run() {
      onGuarded(
        onError = { e ->
          val reason = e.message ?: e.toString()
          sendPrinterStateToUI(PrinterState.ERROR, reason)
        }
      ) {
        closePort()
        sendPrinterStateToUI(PrinterState.DISCONNECTED)
      }
    }
  }

  private inner class PrintThread(private val json: Json) : Thread("Print Thread") {
    override fun run() {
      onGuarded(
        onSuccess = { startCyclicPingThread() },
        onError = { e ->
          onGuarded(
            onError = { debugLog { "Attempted disconnection ${it.message ?: it.toString()}" } }
          ) { closePort() }
          val reason = e.message ?: e.toString()
          sendPrinterStateToUI(PrinterState.ERROR, reason)
        }
      ) {
        val command = generatePrintingCommand(json)
        sendCommandImmediately(command)
        waitPrinterAvailability()
        sendPrinterStateToUI(PrinterState.PRINTED)
      }
    }
  }

  private inner class CyclicPingThread(
    private val delayBetweenEachCycle: Duration,
    var isUsable: Boolean = true,
  ) : Thread("Cyclic Ping Thread") {
    override fun run() {
      onGuarded(
        onError = { e ->
          onGuarded(
            onError = { debugLog { "Attempted disconnection ${it.message ?: it.toString()}" } }
          ) { closePort() }
          val reason = e.message ?: e.toString()
          sendPrinterStateToUI(PrinterState.ERROR, reason)
        }
      ) {
        if(isUsable) {
          sendCommandImmediately(tscPingCommand)
          readDataImmediately()
          if (cyclicPingThread != null && isUsable) {
            // Keep listening to the InputStream until an exception occurs.
            handler.postDelayed(
              cyclicPingThread!!,
              delayBetweenEachCycle.toLong(DurationUnit.MILLISECONDS)
            )
          }
        }
      }
    }
  }
}
