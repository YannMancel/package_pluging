package com.mancel.yann.tsc_bluetooth_print

import android.graphics.Point
import android.util.Size

class PrintingConfiguration (
  val count: Int,
  val size: Size,
  val gap: Int,
  val printableItems: List<PrintableItem>,
) {
  companion object Factory {
    fun fromJson(json: Map<String, Any>): PrintingConfiguration {
      val count =  (json["count"] as Int?) ?: 1

      // width, height and gap in mm
      val width = (json["width"] as Int?) ?: 55
      val height = (json["height"] as Int?) ?: 29
      val gap = (json["gap"] as Int?) ?: 3

      // x, y and imageWidth in dpi where 1 mm corresponds to about 8 points
      val printableItems = (json["printableItems"] as List<*>?)?.map {
        val map = it as HashMap<*,*>
        val type = (map["type"] as String?) ?: ""
        val origin = Point(
          (map["x"] as Int?) ?: 0,
          (map["y"] as Int?) ?: 0
        )
        when(type) {
          "text" -> PrintableItem.Text(
            origin,
            (map["label"] as String?) ?: ""
          )
          "image" -> PrintableItem.Image(
            origin,
            (map["width"] as Int?) ?: width.toDpi(),
            (map["bytes"] as ByteArray?) ?: ByteArray(0)
          )
          else -> throw Exception("The type called $type is not implemented.")
        }
      } ?: emptyList()

      return PrintingConfiguration(
        count,
        Size(width, height),
        gap,
        printableItems
      )
    }
  }

  fun toLog(): String {
    val builder = StringBuilder("\n")
    val indexes = printableItems.indices
    for (i in indexes) {
      val log = printableItems[i].toLog()
      builder.append(log)
      if (i != indexes.last) builder.append("\n")
    }
    return """
      |PrintingConfiguration:
      |  - count: $count
      |  - size: $size
      |  - gap: $gap
      |  - printableItems: $builder
      """.trimMargin()
  }
}
