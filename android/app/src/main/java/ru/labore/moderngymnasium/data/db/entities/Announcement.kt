package ru.labore.moderngymnasium.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "announcement")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    @SerializedName("created_at")
    val createdAt: Int,

    @SerializedName("updated_at")
    val updatedAt: Int,

    val text: String,
    val authorId: Int
)