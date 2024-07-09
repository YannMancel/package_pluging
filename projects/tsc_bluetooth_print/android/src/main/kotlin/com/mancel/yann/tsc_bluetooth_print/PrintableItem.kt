package com.mancel.yann.tsc_bluetooth_print

import android.graphics.Point

sealed class PrintableItem {
  open fun toLog():String { throw Exception("toLog method is not implemented.") }

  data class Text(
    val origin: Point,
    val label: String,
  ): PrintableItem() {
    override fun toLog(): String {
      return """
      |      PrintableItem (Text):
      |        - origin: $origin
      |        - label: $label
      """.trimMargin()
    }
  }

  data class Image(
    val origin: Point,
    val width: Int,
    val bytes: ByteArray,
  ): PrintableItem() {
    override fun toLog(): String {
      return """
      |      PrintableItem (Image):
      |        - origin: $origin
      |        - width: $width
      |        - bytes (length): ${bytes.size}
      """.trimMargin()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Image

      if (origin != other.origin) return false
      if (width != other.width) return false
      if (!bytes.contentEquals(other.bytes)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = origin.hashCode()
      result = 31 * result + width
      result = 31 * result + bytes.contentHashCode()
      return result
    }
  }
}
