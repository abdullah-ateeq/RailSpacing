package com.example.railspacing.data

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.UUID

data class SavedCalculation(
    val id: String = UUID.randomUUID().toString(),
    val mode: RailSpacingMode,
    val span: BigDecimal,
    val unit: MeasurementUnit,
    val railWidth: BigDecimal,
    val railDepth: BigDecimal? = null,
    val maximumGap: BigDecimal? = null,
    val railCount: Int,
    val startOffset: BigDecimal,
    val endOffset: BigDecimal,
    val actualGap: BigDecimal,
    val centerToCenter: BigDecimal,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("mode", mode.name)
            put("span", span.toPlainString())
            put("unit", unit.id)
            put("railWidth", railWidth.toPlainString())
            put("railDepth", railDepth?.toPlainString() ?: JSONObject.NULL)
            put("maximumGap", maximumGap?.toPlainString() ?: JSONObject.NULL)
            put("railCount", railCount)
            put("startOffset", startOffset.toPlainString())
            put("endOffset", endOffset.toPlainString())
            put("actualGap", actualGap.toPlainString())
            put("centerToCenter", centerToCenter.toPlainString())
            put("timestamp", timestamp)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): SavedCalculation {
            val railDepthStr = if (json.isNull("railDepth")) null else json.optString("railDepth")
            val maxGapStr = if (json.isNull("maximumGap")) null else json.optString("maximumGap")

            return SavedCalculation(
                id = json.optString("id", UUID.randomUUID().toString()),
                mode = RailSpacingMode.fromName(json.optString("mode", RailSpacingMode.MAX_GAP.name)),
                span = BigDecimal(json.optString("span", "240")),
                unit = MeasurementUnit.fromId(json.optString("unit", "cm")),
                railWidth = BigDecimal(json.optString("railWidth", "4")),
                railDepth = if (railDepthStr.isNullOrEmpty()) null else BigDecimal(railDepthStr),
                maximumGap = if (maxGapStr.isNullOrEmpty()) null else BigDecimal(maxGapStr),
                railCount = json.optInt("railCount", 1),
                startOffset = BigDecimal(json.optString("startOffset", "0")),
                endOffset = BigDecimal(json.optString("endOffset", "0")),
                actualGap = BigDecimal(json.optString("actualGap", "0")),
                centerToCenter = BigDecimal(json.optString("centerToCenter", "0")),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }

        fun listToJsonString(list: List<SavedCalculation>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJsonString(jsonString: String?): List<SavedCalculation> {
            if (jsonString.isNullOrEmpty()) return emptyList()
            return try {
                val array = JSONArray(jsonString)
                val list = ArrayList<SavedCalculation>(array.length())
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(fromJson(obj))
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
