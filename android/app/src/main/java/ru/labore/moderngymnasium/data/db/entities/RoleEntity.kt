package ru.labore.moderngymnasium.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "role")
class RoleEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    @SerializedName("name_ru")
    val nameRu: String,

    val type: String,
    val name: String,
    var updatedAt: ZonedDateTime? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "Безымянная роль",
        parcel.readString() ?: "No type",
        parcel.readString() ?: "Noname role",
        ZonedDateTime.parse(parcel.readString())
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(nameRu)
        parcel.writeString(type)
        parcel.writeString(name)
        parcel.writeString(updatedAt.toString())
    }

    companion object CREATOR : Parcelable.Creator<RoleEntity> {
        override fun createFromParcel(parcel: Parcel): RoleEntity {
            return RoleEntity(parcel)
        }

        override fun newArray(size: Int): Array<RoleEntity?> {
            return arrayOfNulls(size)
        }
    }
}