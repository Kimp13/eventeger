package ru.labore.moderngymnasium.utils

import ru.labore.moderngymnasium.data.db.entities.ClassEntity
import ru.labore.moderngymnasium.data.db.entities.RoleEntity
import ru.labore.moderngymnasium.data.db.entities.UserEntity

fun announcementEntityToCaption(
    author: UserEntity?,
    noName: String,
    role: RoleEntity? = null,
    `class`: ClassEntity? = null
): String {
    var result = if (author != null) {
        if (author.firstName != null) {
            if (author.lastName != null) {
                author.firstName + " " +
                        author.lastName
            } else {
                author.firstName
            }
        } else author.lastName ?: noName
    } else {
        noName
    }

    if (role != null)
        result += ", ${role.name}"


    if (`class` != null)
        result += ", ${`class`.grade}${`class`.letter}"


    return result
}