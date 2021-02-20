package ru.labore.moderngymnasium.data.db.entities

import androidx.room.Entity
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "comment")
class CommentEntity(
    id: Int,

    authorId: Int,
    val announcementId: Int,
    val replyTo: Int?,
    val text: String,
    createdAt: ZonedDateTime,
    updatedAt: ZonedDateTime
) : AuthoredEntity(
    id,
    authorId,
    createdAt,
    updatedAt
)