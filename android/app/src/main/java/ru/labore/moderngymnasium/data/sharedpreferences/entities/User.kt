package ru.labore.moderngymnasium.data.sharedpreferences.entities
import com.google.gson.annotations.SerializedName

data class PermissionsForRoles(
    val student: Boolean? = null,
    @SerializedName("head_class_teacher")
    val headClassTeacher: Boolean? = null,
    val teacher: Boolean? = null,
    @SerializedName("head_class_student")
    val headClassStudent: Boolean? = null,
    @SerializedName("head_teacher")
    val headTeacher: Boolean? = null,
    val headmaster: Boolean? = null,
    val admin: Boolean? = null
)

data class ActionPermissions(
    val create: PermissionsForRoles? = null,
    val read: PermissionsForRoles? = null,
    val update: PermissionsForRoles? = null,
    val delete: PermissionsForRoles? = null,
    val comment: PermissionsForRoles? = null
)

data class AllPermissions(
    @SerializedName("*")
    val all: Boolean? = null,
    val announcement: ActionPermissions? = null,
    val profile: ActionPermissions? = null
)

data class UserData(
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    val username: String,
    val permissions: AllPermissions? = null
)

data class User(
    val jwt: String,
    val data: UserData
)