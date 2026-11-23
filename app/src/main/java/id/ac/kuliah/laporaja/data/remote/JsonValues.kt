package id.ac.kuliah.laporaja.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }
private const val AGGREGATE_COUNT = "count"

internal object JsonValues {

    fun rows(data: String?): List<JsonObject> {
        if (data.isNullOrBlank()) return emptyList()
        return try {
            when (val element = lenientJson.parseToJsonElement(data)) {
                is JsonArray -> element.mapNotNull { it as? JsonObject }
                is JsonObject -> listOf(element)
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun firstRow(data: String?): JsonObject? = rows(data).firstOrNull()
}

internal fun JsonObject.text(key: String): String? = when (val element = this[key]) {
    null, is JsonNull -> null
    is JsonPrimitive -> element.contentOrNull?.ifBlank { null }
    else -> null
}

internal fun JsonObject.number(key: String): Int? = when (val element = this[key]) {
    null, is JsonNull -> null
    is JsonPrimitive -> element.intOrNull ?: element.contentOrNull?.toDoubleOrNull()?.toInt()
    else -> null
}

internal fun JsonObject.flag(key: String): Boolean = (this[key] as? JsonPrimitive)?.booleanOrNull == true

internal fun JsonObject.child(key: String): JsonObject? = this[key] as? JsonObject

internal fun JsonObject.children(key: String): List<JsonObject> =
    (this[key] as? JsonArray)?.mapNotNull { it as? JsonObject } ?: emptyList()

internal fun JsonObject.countOf(key: String): Int =
    children(key).firstOrNull()?.number(AGGREGATE_COUNT)
        ?: (this[key] as? JsonPrimitive)?.let { primitive ->
            primitive.intOrNull ?: primitive.contentOrNull?.toIntOrNull()
        }
        ?: 0
