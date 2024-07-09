package com.mancel.yann.tsc_bluetooth_print

enum class PrinterState {
    CONNECTING,
    CONNECTED,
    PRINTING,
    PRINTED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR;

    companion object Factory {
        fun fromOrdinal(ordinal: Int): PrinterState {
            return when (ordinal) {
                CONNECTING.ordinal -> CONNECTING
                CONNECTED.ordinal -> CONNECTED
                PRINTING.ordinal -> PRINTING
                PRINTED.ordinal -> PRINTED
                DISCONNECTING.ordinal -> DISCONNECTING
                DISCONNECTED.ordinal -> DISCONNECTED
                ERROR.ordinal -> ERROR
                else -> throw Exception("The ordinal $ordinal is not a correct value.")
            }
        }
    }
}
