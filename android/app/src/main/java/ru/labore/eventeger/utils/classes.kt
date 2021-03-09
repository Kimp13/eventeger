package ru.labore.eventeger.utils

import com.google.gson.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import ru.labore.eventeger.data.sharedpreferences.entities.AnnounceMap
import ru.labore.eventeger.data.sharedpreferences.entities.User
import java.lang.reflect.Type

class JsonPermissionsDeserializerImpl :
    JsonDeserializer<User.Companion.AllPermissions> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): User.Companion.AllPermissions? {
        return if (json != null) {
            println(json.toString())

            if (json.isJsonObject) {
                User.Companion.AllPermissions(json.asJsonObject)
            } else {
                User.Companion.AllPermissions(json.asBoolean)
            }
        } else {
            null
        }
    }
}

class JsonPermissionsSerializerImpl : JsonSerializer<User.Companion.AllPermissions> {
    override fun serialize(
        src: User.Companion.AllPermissions?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement = when {
        src == null -> JsonPrimitive(false)
        src.all -> JsonPrimitive(true)
        else -> src.serialize()
    }
}

class JsonDateSerializerImpl : JsonSerializer<ZonedDateTime> {
    override fun serialize(
        src: ZonedDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (src == null) {
            JsonPrimitive("null")
        } else {
            JsonPrimitive(src.toString())
        }
    }
}

class JsonDateDeserializerImpl : JsonDeserializer<ZonedDateTime> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ZonedDateTime {
        return if (json == null) {
            ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.UTC)
        } else {
            ZonedDateTime.parse(json.asString)
        }
    }
}

class JsonAnnounceMapDeserializerImpl : JsonDeserializer<AnnounceMap> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AnnounceMap {
        return if (json != null && json.isJsonObject) {
            AnnounceMap(json.asJsonObject)
        } else {
            AnnounceMap()
        }
    }
}

class JsonAnnounceMapSerializerImpl : JsonSerializer<AnnounceMap> {
    override fun serialize(
        src: AnnounceMap?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement = when (src) {
        null -> JsonPrimitive(false)
        else -> src.serialize()
    }
}