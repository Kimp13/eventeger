package ru.labore.moderngymnasium.data.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Transaction

data class UserWithRole(
    @Embedded
    val role: RoleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "roleId"
    )
    val user: UserEntity
)