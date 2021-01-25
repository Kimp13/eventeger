package ru.labore.moderngymnasium.utils

import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import java.util.*

fun announcementEntityToCaption(entity: AnnouncementEntity, noName: String): String {
    var result = if (entity.author!!.firstName != null) {
        if (entity.author!!.lastName != null) {
            entity.author!!.firstName + " " +
                    entity.author!!.lastName
        } else {
            entity.author!!.firstName!!
        }
    } else if (entity.author!!.lastName != null) {
        entity.author!!.lastName!!
    } else {
        noName
    }

    if (entity.authorRole != null) {
        result += ", ${entity.authorRole!!.name}"
    }

    if (entity.authorClass != null) {
        result += ", ${entity.authorClass!!.grade}${entity.authorClass!!.letter}"
    }

    return result
}