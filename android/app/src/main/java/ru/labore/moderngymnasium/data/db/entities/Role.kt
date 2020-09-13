package ru.labore.moderngymnasium.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "role")
data class RoleEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val type: String,
    val name: String,
    @SerializedName("name_ru")
    val nameRu: String
)