package ru.labore.moderngymnasium.data.sharedpreferences.entities

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

class AnnounceMap {
    private val contents =
        HashMap<Int, User.Companion.ActionPermissionsTargets>()

    val rolesIds: Array<Int>
    val classesIds: Array<Int>

    val size
        get() = contents.size
    val values
        get() = contents.values
    val keys
        get() = contents.keys
    val entries
        get() = contents.entries

    constructor () {
        rolesIds = emptyArray()
        classesIds = emptyArray()
    }

    constructor (json: JsonObject) {
        val entrySet: Set<Map.Entry<String?, JsonElement?>> = json.entrySet()
        val rolesIdsList = arrayListOf<Int>()
        val classesIdsSet = HashSet<Int>()

        for ((key, value) in entrySet) {
            if (key != null && value?.isJsonArray == true) {
                val array = value.asJsonArray
                val roleId = key.toInt()

                rolesIdsList.add(roleId)

                contents[roleId] = User.Companion.ActionPermissionsTargets(
                    Array(array.size()) {
                        val classId = array[it].asInt

                        classesIdsSet.add(classId)
                        classId
                    }
                )
            }
        }

        classesIds = classesIdsSet.toTypedArray()
        rolesIds = rolesIdsList.toTypedArray()
    }

    operator fun iterator() = contents.iterator()

    operator fun get(key: Int): User.Companion.ActionPermissionsTargets =
        contents[key] ?: User.Companion.ActionPermissionsTargets(false)

    fun serialize () = when (contents.size) {
        0 -> JsonPrimitive(false)
        else -> {
            val result = JsonObject()

            for ((key, value) in contents) {
                val array = JsonArray()

                for (i in 0 until value.size) {
                    array.add(value[i])
                }

                result.add(key.toString(), array)
            }

            result
        }
    }
}
