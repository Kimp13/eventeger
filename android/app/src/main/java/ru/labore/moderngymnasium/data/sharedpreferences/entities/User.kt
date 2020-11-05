package ru.labore.moderngymnasium.data.sharedpreferences.entities
import com.google.gson.*
import com.google.gson.annotations.SerializedName

val gson = Gson()

class ActionPermissionsTargets {
    val all: Boolean
    val contents: Array<Int>

    constructor(wildcard: Boolean) {
        all = wildcard
        contents = arrayOf()
    }

    constructor(targets: Array<Int>) {
        all = false
        contents = targets
    }

    fun serialize (): JsonElement {
        return if (all) {
            JsonPrimitive(true)
        } else {
            val result = JsonArray()

            result.add(gson.toJson(contents, Array<Int>::class.java))

            result
        }
    }
}

class ActionPermissions {
    val all: Boolean
    val create: ActionPermissionsTargets?
    val read: ActionPermissionsTargets?
    val update: ActionPermissionsTargets?
    val delete: ActionPermissionsTargets?
    val comment: ActionPermissionsTargets?

    constructor (wildcard: Boolean) {
        all = wildcard

        create = ActionPermissionsTargets(wildcard)
        read = ActionPermissionsTargets(wildcard)
        update = ActionPermissionsTargets(wildcard)
        delete = ActionPermissionsTargets(wildcard)
        comment = ActionPermissionsTargets(wildcard)
    }

    constructor (json: JsonObject) {
        all = false

        json.get("create").let {
            create = if (it == null) {
                null
            } else {
                if (it.isJsonArray) {
                    ActionPermissionsTargets(
                        gson.fromJson(it, Array<Int>::class.java)
                    )
                } else {
                    ActionPermissionsTargets(
                        it.asBoolean
                    )
                }
            }
        }

        json.get("read").let {
            read = if (it == null) {
                null
            } else {
                if (it.isJsonArray) {
                    ActionPermissionsTargets(
                        gson.fromJson(it, Array<Int>::class.java)
                    )
                } else {
                    ActionPermissionsTargets(
                        it.asBoolean
                    )
                }
            }
        }

        json.get("update").let {
            update = if (it == null) {
                null
            } else {
                if (it.isJsonArray) {
                    ActionPermissionsTargets(
                        gson.fromJson(it, Array<Int>::class.java)
                    )
                } else {
                    ActionPermissionsTargets(
                        it.asBoolean
                    )
                }
            }
        }

        json.get("delete").let {
            delete = if (it == null) {
                null
            } else {
                if (it.isJsonArray) {
                    ActionPermissionsTargets(
                        gson.fromJson(it, Array<Int>::class.java)
                    )
                } else {
                    ActionPermissionsTargets(
                        it.asBoolean
                    )
                }
            }
        }

        json.get("comment").let {
            comment = if (it == null) {
                null
            } else {
                if (it.isJsonArray) {
                    ActionPermissionsTargets(
                        gson.fromJson(it, Array<Int>::class.java)
                    )
                } else {
                    ActionPermissionsTargets(
                        it.asBoolean
                    )
                }
            }
        }
    }

    fun serialize (): JsonElement {
        return if (all) {
            JsonPrimitive(true)
        } else {
            val result = JsonObject()

            if (create != null) {
                result.add("create", create.serialize())
            }

            if (read != null) {
                result.add("read", read.serialize())
            }

            if (update != null) {
                result.add("update", update.serialize())
            }

            if (delete != null) {
                result.add("delete", delete.serialize())
            }

            if (comment != null) {
                result.add("comment", comment.serialize())
            }

            result
        }
    }
}

class AllPermissions {
    val all: Boolean
    val announcement: ActionPermissions?
    val profile: ActionPermissions?

    constructor (wildcard: Boolean) {
        all = wildcard

        announcement = ActionPermissions(wildcard)
        profile = ActionPermissions(wildcard)
    }

    constructor (json: JsonObject) {
        all = false

        json.get("announcement").let {
            announcement = if (it == null) {
                null
            } else {
                if (it.isJsonObject) {
                    ActionPermissions(
                        it.asJsonObject
                    )
                } else {
                    ActionPermissions(
                        it.asBoolean
                    )
                }
            }
        }

        json.get("profile").let {
            profile = if (it == null) {
                null
            } else {
                if (it.isJsonObject) {
                    ActionPermissions(
                        it.asJsonObject
                    )
                } else {
                    ActionPermissions(
                        it.asBoolean
                    )
                }
            }
        }
    }
}

data class UserData(
    @SerializedName("first_name")
    val firstName: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("class_id")
    val classId: Int? = null,

    @SerializedName("role_id")
    val roleId: Int? = null,

    val username: String,
    val permissions: AllPermissions? = null
)

data class User(
    val jwt: String,
    val data: UserData
)