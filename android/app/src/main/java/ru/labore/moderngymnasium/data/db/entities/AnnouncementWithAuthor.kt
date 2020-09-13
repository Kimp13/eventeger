package ru.labore.moderngymnasium.data.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class AnnouncementWithAuthor(
    @Embedded
    val author: UserEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "authorId"
    )
    val announcement: AnnouncementEntity
)